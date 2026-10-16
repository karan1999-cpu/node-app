package com.cybersync.nodd.data

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds
import androidx.core.content.FileProvider
import com.google.mlkit.vision.barcode.common.Barcode
import java.io.File

object ContactActions {

    /**
     * Opens the phone's own "Add contact" screen with the details filled in. The user only taps Save,
     * so the app never needs permission to read or write contacts.
     */
    fun openAddContact(context: Context, contact: ScannedContact): Boolean {
        val intent =Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
            if (contact.name.isNotBlank()) putExtra(ContactsContract.Intents.Insert.NAME, contact.name)
            contact.phones.getOrNull(0)?.let {
                putExtra(ContactsContract.Intents.Insert.PHONE, it)
                putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, CommonDataKinds.Phone.TYPE_MOBILE)
            }
            contact.phones.getOrNull(1)?.let { putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, it) }
            contact.emails.getOrNull(0)?.let { putExtra(ContactsContract.Intents.Insert.EMAIL, it) }
            contact.emails.getOrNull(1)?.let { putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL, it) }
            if (contact.company.isNotBlank()) putExtra(ContactsContract.Intents.Insert.COMPANY, contact.company)
            if (contact.title.isNotBlank()) putExtra(ContactsContract.Intents.Insert.JOB_TITLE, contact.title)
            if (contact.urls.isNotEmpty()) {
                val rows = ArrayList(contact.urls.map { url ->
                    ContentValues().apply {
                        put(ContactsContract.Data.MIMETYPE, CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                        put(CommonDataKinds.Website.URL, url)
                        put(CommonDataKinds.Website.TYPE, CommonDataKinds.Website.TYPE_OTHER)
                    }
                })
                putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, rows)
            }
            // Return to Nodd after the contact is saved.
            putExtra("finishActivityOnSaveCompleted", true)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    /** Shares the card as a .vcf file through WhatsApp, Quick Share, Bluetooth or any other app. */
    fun shareCard(context: Context, profile: Profile) {
        val dir = File(context.cacheDir, "shared").apply {
            deleteRecursively()
            mkdirs()
        }
        val fileName = profile.fullName.replace(Regex("[^A-Za-z0-9 _-]"), "").trim().ifEmpty { "contact" }
        val file = File(dir, "$fileName.vcf").apply { writeText(VCard.build(profile)) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/x-vcard"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Send your contact"))
    }
}

/** Turns whatever the scanner read into contact details, or null when the QR isn't a contact. */
fun Barcode.toScannedContact(): ScannedContact? {
    contactInfo?.let { info ->
        val personName = info.name
        val name = personName?.formattedName?.takeIf { it.isNotBlank() }
            ?: listOfNotNull(personName?.first, personName?.last).filter { it.isNotBlank() }.joinToString(" ")
        val contact = ScannedContact(
            name = name,
            phones = info.phones.mapNotNull { it.number?.takeIf { number -> number.isNotBlank() } },
            emails = info.emails.mapNotNull { it.address?.takeIf { address -> address.isNotBlank() } },
            company = info.organization.orEmpty(),
            title = info.title.orEmpty(),
            urls = info.urls.filter { it.isNotBlank() },
        )
        if (!contact.isEmpty) return contact
    }
    phone?.number?.takeIf { it.isNotBlank() }?.let { return ScannedContact(phones = listOf(it)) }
    return rawValue?.let { VCard.parse(it) }
}
