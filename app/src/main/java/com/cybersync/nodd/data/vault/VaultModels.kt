package com.cybersync.nodd.data.vault

import org.json.JSONObject
import javax.crypto.SecretKey

enum class VaultCategory { Images, Documents, Videos, Links, Contacts }

/** One hidden thing: a file-backed item (blob set) or a text item (text set, for links/contacts). */
data class VaultItem(
    val id: String,
    val category: VaultCategory,
    val displayName: String,
    val mimeType: String? = null,
    val blob: String? = null,
    val text: String? = null,
    val size: Long = 0,
    val addedAt: Long = System.currentTimeMillis(),
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("cat", category.name)
        put("name", displayName)
        mimeType?.let { put("mime", it) }
        blob?.let { put("blob", it) }
        text?.let { put("text", it) }
        put("size", size)
        put("at", addedAt)
    }

    companion object {
        fun fromJson(o: JSONObject): VaultItem = VaultItem(
            id = o.getString("id"),
            category = VaultCategory.valueOf(o.getString("cat")),
            displayName = o.getString("name"),
            mimeType = o.optString("mime").ifEmpty { null },
            blob = o.optString("blob").ifEmpty { null },
            text = o.optString("text").ifEmpty { null },
            size = o.optLong("size"),
            addedAt = o.optLong("at"),
        )
    }
}

/** An open vault: its id and the in-memory key. Lives only until the vault is locked. */
class Vault(val id: String, val key: SecretKey)
