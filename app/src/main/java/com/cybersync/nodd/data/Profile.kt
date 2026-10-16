package com.cybersync.nodd.data

/** Optional details a person can switch on or off for their QR. The name is always included. */
enum class Field { Phone, Email, Work, Instagram, LinkedIn, Website }

data class Profile(
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val email: String = "",
    val jobTitle: String = "",
    val company: String = "",
    val instagram: String = "",
    val linkedin: String = "",
    val website: String = "",
    val hidden: Set<Field> = emptySet(),
) {
    val fullName: String
        get() = listOf(firstName, lastName).map { it.trim() }.filter { it.isNotEmpty() }.joinToString(" ")

    val isBlank: Boolean
        get() = fullName.isEmpty()

    val initials: String
        get() = listOf(firstName, lastName)
            .mapNotNull { it.trim().firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifEmpty { "?" }

    /** The text shown for a field on the card, or "" when the field is empty. */
    fun valueOf(field: Field): String = when (field) {
        Field.Phone -> phone.trim()
        Field.Email -> email.trim()
        Field.Work -> listOf(jobTitle, company).map { it.trim() }.filter { it.isNotEmpty() }.joinToString(" · ")
        Field.Instagram -> instagramHandle()?.let { "@$it" }.orEmpty()
        Field.LinkedIn -> linkedin.trim()
        Field.Website -> website.trim()
    }

    fun has(field: Field): Boolean = valueOf(field).isNotEmpty()

    /** True when the field has a value and is switched on for the QR. */
    fun shows(field: Field): Boolean = has(field) && field !in hidden

    /** Accepts "@name", "name" or a full instagram.com link. */
    fun instagramHandle(): String? = instagram.trim()
        .substringAfter("instagram.com/")
        .trim('/', '@', ' ')
        .substringBefore('/')
        .substringBefore('?')
        .takeIf { it.isNotEmpty() }

    fun instagramUrl(): String? = instagramHandle()?.let { "https://instagram.com/$it" }

    /** Accepts a profile link or just the part after linkedin.com/in/. */
    fun linkedinUrl(): String? {
        val value = linkedin.trim().trim('/')
        if (value.isEmpty()) return null
        return when {
            value.startsWith("http://") || value.startsWith("https://") -> value
            "linkedin.com" in value -> "https://$value"
            else -> "https://www.linkedin.com/in/${value.removePrefix("@")}"
        }
    }

    fun websiteUrl(): String? {
        val value = website.trim()
        if (value.isEmpty()) return null
        return if (value.startsWith("http://") || value.startsWith("https://")) value else "https://$value"
    }
}
