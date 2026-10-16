package com.cybersync.nodd.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cybersync.nodd.R
import com.cybersync.nodd.data.ContactActions
import com.cybersync.nodd.data.ScannedContact
import com.cybersync.nodd.data.toScannedContact
import com.cybersync.nodd.ui.Eyebrow
import com.cybersync.nodd.ui.Panel
import com.cybersync.nodd.ui.PrimaryButton
import com.cybersync.nodd.ui.Tip
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

private sealed interface ScanResult {
    data class Found(val contact: ScannedContact, val opened: Boolean) : ScanResult
    data class NotContact(val text: String) : ScanResult
    data class Failed(val message: String) : ScanResult
}

@Composable
fun ScanScreen() {
    val context = LocalContext.current
    var result by remember { mutableStateOf<ScanResult?>(null) }
    val scanner = remember(context) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()
        GmsBarcodeScanning.getClient(context, options)
    }

    fun scan() {
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val contact = barcode.toScannedContact()
                result = if (contact == null) {
                    ScanResult.NotContact(barcode.rawValue.orEmpty())
                } else {
                    ScanResult.Found(contact, ContactActions.openAddContact(context, contact))
                }
            }
            .addOnFailureListener { e ->
                val text = e.message.orEmpty()
                result = ScanResult.Failed(
                    if (text.contains("download", ignoreCase = true)) {
                        "Google Play services is still setting up the scanner. Wait a minute, check you're online, and try again."
                    } else {
                        "The scanner couldn't start. Make sure Google Play services is up to date, then try again."
                    },
                )
            }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text("Scan", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Scan someone's Nodd QR, or any contact QR. Your phone's Add contact screen opens with their details filled in. Tap Save and you're done.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PrimaryButton("Scan a QR code", R.drawable.ic_scan, ::scan)

        when (val r = result) {
            is ScanResult.Found -> FoundPanel(r) { ContactActions.openAddContact(context, r.contact) }
            is ScanResult.NotContact -> MessagePanel(
                title = "This QR isn't a contact card",
                body = "It contains:",
                detail = r.text.ifBlank { "(no text)" },
            )
            is ScanResult.Failed -> MessagePanel(title = "Couldn't scan", body = r.message)
            null -> Unit
        }

        Tip("People without Nodd can still save you. Their normal camera app or Google Lens reads your QR, and so does the iPhone camera.")
    }
}

@Composable
private fun FoundPanel(result: ScanResult.Found, onOpenAgain: () -> Unit) {
    val c = result.contact
    Panel {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(18.dp)) {
            Eyebrow("Last scanned")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_user_plus),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = c.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
            val lines = c.phones + c.emails + listOf(c.title, c.company).filter { it.isNotBlank() }
            if (lines.isNotEmpty()) {
                Text(lines.joinToString("\n"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!result.opened) {
                Text(
                    text = "No contacts app was found on this phone to save it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            TextButton(onClick = onOpenAgain) {
                Text("Open Add contact again", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun MessagePanel(title: String, body: String, detail: String? = null) {
    Panel {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            detail?.let {
                SelectionContainer {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
