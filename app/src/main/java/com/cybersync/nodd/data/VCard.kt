package com.cybersync.nodd.data

/**
 * Builds the text inside the QR. It is a standard vCard 3.0 contact card, which the Android
 * camera, Google Lens and the iPhone camera all recognise and offer to save as a contact.
 */
object VCard {

    fun build(profile: Profile): String {
        val lines = mutableListOf("BEGIN:VCARD", "VERSION:3.0")
        lines += "N:${escape(profile.lastName.trim())};${escape(profile.firstName.trim())};;;"
        lines += "FN:${escape(profile.fullName)}"
        if (profile.shows(Field.Work)) {
            if (profile.company.isNotBlank()) lines += "ORG:${escape(profile.company.trim())}"
            if (profile.jobTitle.isNotBlank()) lines += "TITLE:${escape(profile.jobTitle.trim())}"
        }
        if (profile.shows(Field.Phone)) lines += "TEL;TYPE=CELL:${profile.phone.trim()}"
        if (profile.shows(Field.Email)) lines += "EMAIL:${profile.email.trim()}"
        if (profile.shows(Field.Instagram)) profile.instagramUrl()?.let { lines += "URL:$it" }
        if (profile.shows(Field.LinkedIn)) profile.linkedinUrl()?.let { lines += "URL:$it" }
        if (profile.shows(Field.Website)) profile.websiteUrl()?.let { lines += "URL:$it" }
        lines += "END:VCARD"
        return lines.joinToString("\r\n")
    }

    /**
     * Reads a vCard or MECARD from raw QR text. The scanner usually parses contact QRs itself;
     * this is the fallback for codes it returns as plain text.
     */
    fun parse(raw: String): ScannedContact? {
        val text = raw.trim()
        val contact = when {
            text.startsWith("BEGIN:VCARD", ignoreCase = true) -> parseVCard(text)
            text.startsWith("MECARD:", ignoreCase = true) -> parseMeCard(text.substring(7))
            else -> null
        }
        return contact?.takeUnless { it.isEmpty }
    }

    internal fun escape(value: String): String = value
        .replace("\\", "\\\\")
        .replace(",", "\\,")
        .replace(";", "\\;")
        .replace("\r\n", "\\n")
        .replace("\n", "\\n")

    private fun parseVCard(text: String): ScannedContact {
        // Folded lines continue with a leading space or tab.
        val lines = text.replace("\r\n", "\n").replace("\n ", "").replace("\n\t", "").lines()
        var fullName = ""
        var structuredName = ""
        val phones = mutableListOf<String>()
        val emails = mutableListOf<String>()
        val urls = mutableListOf<String>()
        var company = ""
        var title = ""
        for (line in lines) {
            val colon = line.indexOf(':')
            if (colon <= 0) continue
            val key = line.substring(0, colon).substringBefore(';').substringAfterLast('.').uppercase()
            val value = line.substring(colon + 1).trim()
            if (value.isEmpty()) continue
            when (key) {
                "FN" -> fullName = unescape(value)
                "N" -> {
                    val parts = splitUnescaped(value, ';').map { unescape(it).trim() }
                    structuredName = listOfNotNull(parts.getOrNull(1), parts.getOrNull(0))
                        .filter { it.isNotEmpty() }
                        .joinToString(" ")
                }
                "TEL" -> phones += value.removePrefix("tel:")
                "EMAIL" -> emails += value.removePrefix("mailto:")
                "URL" -> urls += unescape(value)
                "ORG" -> company = unescape(splitUnescaped(value, ';').first())
                "TITLE" -> title = unescape(value)
            }
        }
        return ScannedContact(fullName.ifBlank { structuredName }, phones, emails, company, title, urls)
    }

    private fun parseMeCard(body: String): ScannedContact {
        var name = ""
        val phones = mutableListOf<String>()
        val emails = mutableListOf<String>()
        val urls = mutableListOf<String>()
        var company = ""
        for (part in splitUnescaped(body, ';')) {
            val colon = part.indexOf(':')
            if (colon <= 0) continue
            val key = part.substring(0, colon).uppercase()
            val value = unescape(part.substring(colon + 1)).trim()
            if (value.isEmpty()) continue
            when (key) {
                // MECARD writes "Last,First".
                "N" -> name = value.split(',').map { it.trim() }.reversed().filter { it.isNotEmpty() }.joinToString(" ")
                "TEL" -> phones += value
                "EMAIL" -> emails += value
                "URL" -> urls += value
                "ORG" -> company = value
            }
        }
        return ScannedContact(name, phones, emails, company, "", urls)
    }

    private fun splitUnescaped(value: String, separator: Char): List<String> {
        val parts = mutableListOf<String>()
        val current = StringBuilder()
        var i = 0
        while (i < value.length) {
            val c = value[i]
            when {
                c == '\\' && i + 1 < value.length -> {
                    current.append(c).append(value[i + 1])
                    i++
                }
                c == separator -> {
                    parts += current.toString()
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        parts += current.toString()
        return parts
    }

    private fun unescape(value: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < value.length) {
            val c = value[i]
            if (c == '\\' && i + 1 < value.length) {
                val next = value[i + 1]
                out.append(if (next == 'n' || next == 'N') ' ' else next)
                i += 2
            } else {
                out.append(c)
                i++
            }
        }
        return out.toString()
    }
}
