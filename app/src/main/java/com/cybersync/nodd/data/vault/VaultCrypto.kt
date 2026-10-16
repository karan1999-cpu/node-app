package com.cybersync.nodd.data.vault

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-256-GCM encryption with a key derived from the vault's PIN.
 *
 * The key is never stored. It exists only in memory while a vault is open, and is recomputed
 * from the PIN each time, so the files cannot be read without the PIN — not by the gallery,
 * another app, or a copy of the app's data pulled off the phone.
 */
object VaultCrypto {
    private const val KDF = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 210_000
    private const val KEY_BITS = 256
    private const val TRANSFORM = "AES/GCM/NoPadding"
    private const val IV_LEN = 12
    private const val TAG_BITS = 128
    const val SALT_LEN = 16
    private const val VERSION: Byte = 1

    private val random = SecureRandom()

    fun newSalt(): ByteArray = ByteArray(SALT_LEN).also { random.nextBytes(it) }

    /**
     * A key kept in the Android Keystore, for a vault that opens without a PIN. The key never
     * leaves the phone's secure hardware, so the files stay encrypted on disk and unreadable
     * if copied off the device, but anyone who can open the vault in the app can see them.
     */
    fun getOrCreateDeviceKey(alias: String): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                // We supply our own random IV for each file, the same as for the PIN vault.
                .setRandomizedEncryptionRequired(false)
                .build(),
        )
        return generator.generateKey()
    }

    fun deriveKey(pin: CharArray, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(pin, salt, ITERATIONS, KEY_BITS)
        try {
            val bytes = SecretKeyFactory.getInstance(KDF).generateSecret(spec).encoded
            return SecretKeySpec(bytes, "AES")
        } finally {
            spec.clearPassword()
        }
    }

    /** Encrypts a small block (the index, the verifier). Output: [version][iv][ciphertext+tag]. */
    fun encrypt(key: SecretKey, plain: ByteArray): ByteArray {
        val iv = ByteArray(IV_LEN).also { random.nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORM).apply {
            init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        }
        val ct = cipher.doFinal(plain)
        return ByteArray(1 + IV_LEN + ct.size).also {
            it[0] = VERSION
            System.arraycopy(iv, 0, it, 1, IV_LEN)
            System.arraycopy(ct, 0, it, 1 + IV_LEN, ct.size)
        }
    }

    fun decrypt(key: SecretKey, data: ByteArray): ByteArray {
        require(data.size > 1 + IV_LEN && data[0] == VERSION) { "Not a Nodd vault block" }
        val iv = data.copyOfRange(1, 1 + IV_LEN)
        val ct = data.copyOfRange(1 + IV_LEN, data.size)
        val cipher = Cipher.getInstance(TRANSFORM).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        }
        return cipher.doFinal(ct)
    }

    /** Streams a file in, encrypted, so even large videos never load fully into memory. */
    fun encryptStream(key: SecretKey, input: InputStream, output: OutputStream) {
        val iv = ByteArray(IV_LEN).also { random.nextBytes(it) }
        output.write(byteArrayOf(VERSION))
        output.write(iv)
        val cipher = Cipher.getInstance(TRANSFORM).apply {
            init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        }
        CipherOutputStream(output, cipher).use { input.copyTo(it) }
    }

    fun decryptStream(key: SecretKey, input: InputStream, output: OutputStream) {
        val version = input.read()
        require(version == VERSION.toInt()) { "Not a Nodd vault file" }
        val iv = ByteArray(IV_LEN)
        var read = 0
        while (read < IV_LEN) {
            val n = input.read(iv, read, IV_LEN - read)
            require(n >= 0) { "Truncated vault file" }
            read += n
        }
        val cipher = Cipher.getInstance(TRANSFORM).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        }
        CipherInputStream(input, cipher).use { it.copyTo(output) }
    }
}
