package com.cybersync.nodd.data.vault

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

/**
 * Stores each vault's files, encrypted, in the app's private storage:
 *   files/v/[id].c   PIN salt + an encrypted check token
 *   files/v/[id].i   the encrypted list of items
 *   files/v/[id]/    one encrypted blob per file-backed item
 *
 * Nothing here is readable without the vault's PIN, and nothing is written to the shared
 * gallery/MediaStore, so other apps and the gallery never see it.
 */
class VaultManager(context: Context) {

    private val appContext = context.applicationContext
    private val root = File(appContext.filesDir, "v")
    private val tmp = File(appContext.cacheDir, "vault_tmp")

    private fun cfg(id: String) = File(root, "$id.c")
    private fun idx(id: String) = File(root, "$id.i")
    private fun blobDir(id: String) = File(root, id)

    fun isSetUp(id: String): Boolean = cfg(id).exists()

    fun setup(id: String, pin: CharArray): Vault {
        root.mkdirs()
        val salt = VaultCrypto.newSalt()
        val key = VaultCrypto.deriveKey(pin, salt)
        val verifier = VaultCrypto.encrypt(key, VERIFIER)
        cfg(id).writeBytes(salt + verifier)
        val vault = Vault(id, key)
        saveItems(vault, emptyList())
        return vault
    }

    /** Returns an open vault if the PIN is right, or null if it's wrong or not set up. */
    fun unlock(id: String, pin: CharArray): Vault? {
        val file = cfg(id)
        if (!file.exists()) return null
        val bytes = file.readBytes()
        if (bytes.size <= VaultCrypto.SALT_LEN) return null
        val salt = bytes.copyOfRange(0, VaultCrypto.SALT_LEN)
        val verifier = bytes.copyOfRange(VaultCrypto.SALT_LEN, bytes.size)
        val key = VaultCrypto.deriveKey(pin, salt)
        return try {
            if (VaultCrypto.decrypt(key, verifier).contentEquals(VERIFIER)) Vault(id, key) else null
        } catch (e: Exception) {
            null
        }
    }

    /** Opens Ultra Go Dark. No PIN: its key lives in the Android Keystore on this phone. */
    fun openUltra(): Vault {
        root.mkdirs()
        val vault = Vault(ULTRA, VaultCrypto.getOrCreateDeviceKey(ULTRA_KEY_ALIAS))
        if (!idx(ULTRA).exists()) saveItems(vault, emptyList())
        return vault
    }

    fun loadItems(vault: Vault): List<VaultItem> {
        val file = idx(vault.id)
        if (!file.exists()) return emptyList()
        return try {
            val json = String(VaultCrypto.decrypt(vault.key, file.readBytes()), Charsets.UTF_8)
            val arr = JSONArray(json)
            (0 until arr.length()).map { VaultItem.fromJson(arr.getJSONObject(it)) }
                .sortedByDescending { it.addedAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveItems(vault: Vault, items: List<VaultItem>) {
        val arr = JSONArray()
        items.forEach { arr.put(it.toJson()) }
        idx(vault.id).writeBytes(VaultCrypto.encrypt(vault.key, arr.toString().toByteArray(Charsets.UTF_8)))
    }

    fun addFile(vault: Vault, uri: Uri, category: VaultCategory): VaultItem {
        val (name, mime, size) = meta(uri)
        val blobName = UUID.randomUUID().toString()
        blobDir(vault.id).mkdirs()
        appContext.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Can't read the chosen file" }
            File(blobDir(vault.id), blobName).outputStream().use { out ->
                VaultCrypto.encryptStream(vault.key, input, out)
            }
        }
        val item = VaultItem(UUID.randomUUID().toString(), category, name, mime, blobName, null, size)
        saveItems(vault, loadItems(vault) + item)
        return item
    }

    fun addText(vault: Vault, category: VaultCategory, name: String, text: String): VaultItem {
        val item = VaultItem(
            id = UUID.randomUUID().toString(),
            category = category,
            displayName = name.ifBlank { text },
            text = text,
        )
        saveItems(vault, loadItems(vault) + item)
        return item
    }

    fun remove(vault: Vault, item: VaultItem) {
        item.blob?.let { File(blobDir(vault.id), it).delete() }
        saveItems(vault, loadItems(vault).filterNot { it.id == item.id })
    }

    /** Decrypts an image to a (possibly downsampled) bitmap, kept in memory only. */
    fun decryptBitmap(vault: Vault, item: VaultItem): Bitmap? {
        val bytes = decryptBytes(vault, item) ?: return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        var sample = 1
        val max = 2048
        while (bounds.outWidth / sample > max || bounds.outHeight / sample > max) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
    }

    private fun decryptBytes(vault: Vault, item: VaultItem): ByteArray? {
        val blob = item.blob ?: return null
        val file = File(blobDir(vault.id), blob)
        if (!file.exists()) return null
        val out = ByteArrayOutputStream()
        file.inputStream().use { VaultCrypto.decryptStream(vault.key, it, out) }
        return out.toByteArray()
    }

    /**
     * Decrypts a file to a temporary copy in the app's private cache so another app can open it.
     * That copy is deleted when the vault is locked or the app goes to the background.
     */
    fun decryptToCache(vault: Vault, item: VaultItem): File {
        val blob = requireNotNull(item.blob) { "No file for this item" }
        tmp.mkdirs()
        val out = File(tmp, safeName(item.displayName))
        File(blobDir(vault.id), blob).inputStream().use { input ->
            out.outputStream().use { o -> VaultCrypto.decryptStream(vault.key, input, o) }
        }
        return out
    }

    fun clearTmp() {
        tmp.deleteRecursively()
    }

    private fun meta(uri: Uri): Triple<String, String?, Long> {
        var name = "file"
        var size = 0L
        val mime = appContext.contentResolver.getType(uri)
        appContext.contentResolver.query(uri, null, null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val ni = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val si = c.getColumnIndex(OpenableColumns.SIZE)
                if (ni >= 0 && !c.isNull(ni)) name = c.getString(ni) ?: name
                if (si >= 0 && !c.isNull(si)) size = c.getLong(si)
            }
        }
        return Triple(name, mime, size)
    }

    private fun safeName(name: String): String =
        name.replace(Regex("[^A-Za-z0-9._-]"), "_").take(80).ifEmpty { "file" }

    private companion object {
        val VERIFIER = "NODDY-VAULT-OK".toByteArray(Charsets.UTF_8)
        const val MAIN = "m"
        const val ULTRA = "u"
        const val ULTRA_KEY_ALIAS = "noddy_vault_u"
    }

    fun mainId() = MAIN
    fun ultraId() = ULTRA
}
