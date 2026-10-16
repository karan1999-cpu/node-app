package com.cybersync.nodd.data

/** Details read from someone else's QR, ready for the phone's "Add contact" screen. */
data class ScannedContact(
    val name: String = "",
    val phones: List<String> = emptyList(),
    val emails: List<String> = emptyList(),
    val company: String = "",
    val title: String = "",
    val urls: List<String> = emptyList(),
) {
    val isEmpty: Boolean
        get() = name.isBlank() && phones.isEmpty() && emails.isEmpty()

    val displayName: String
        get() = name.ifBlank { phones.firstOrNull() ?: emails.firstOrNull() ?: "New contact" }
}
