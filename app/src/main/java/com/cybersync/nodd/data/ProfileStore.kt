package com.cybersync.nodd.data

import android.content.Context
import androidx.core.content.edit

/** Keeps the card on this phone only. Nothing leaves the device unless the user shares it. */
class ProfileStore(context: Context) {
    private val prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE)

    fun load(): Profile = Profile(
        firstName = prefs.getString(FIRST_NAME, "").orEmpty(),
        lastName = prefs.getString(LAST_NAME, "").orEmpty(),
        phone = prefs.getString(PHONE, "").orEmpty(),
        email = prefs.getString(EMAIL, "").orEmpty(),
        jobTitle = prefs.getString(JOB_TITLE, "").orEmpty(),
        company = prefs.getString(COMPANY, "").orEmpty(),
        instagram = prefs.getString(INSTAGRAM, "").orEmpty(),
        linkedin = prefs.getString(LINKEDIN, "").orEmpty(),
        website = prefs.getString(WEBSITE, "").orEmpty(),
        hidden = prefs.getStringSet(HIDDEN, emptySet()).orEmpty()
            .mapNotNull { name -> Field.entries.find { it.name == name } }
            .toSet(),
    )

    fun save(profile: Profile) {
        prefs.edit {
            putString(FIRST_NAME, profile.firstName.trim())
            putString(LAST_NAME, profile.lastName.trim())
            putString(PHONE, profile.phone.trim())
            putString(EMAIL, profile.email.trim())
            putString(JOB_TITLE, profile.jobTitle.trim())
            putString(COMPANY, profile.company.trim())
            putString(INSTAGRAM, profile.instagram.trim())
            putString(LINKEDIN, profile.linkedin.trim())
            putString(WEBSITE, profile.website.trim())
            putStringSet(HIDDEN, profile.hidden.map { it.name }.toSet())
        }
    }

    private companion object {
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
        const val PHONE = "phone"
        const val EMAIL = "email"
        const val JOB_TITLE = "job_title"
        const val COMPANY = "company"
        const val INSTAGRAM = "instagram"
        const val LINKEDIN = "linkedin"
        const val WEBSITE = "website"
        const val HIDDEN = "hidden"
    }
}
