package com.cybersync.nodd.ui.vault

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.cybersync.nodd.data.vault.Vault
import com.cybersync.nodd.data.vault.VaultCategory
import com.cybersync.nodd.data.vault.VaultItem
import com.cybersync.nodd.data.vault.VaultManager

/**
 * Hosts one open vault: lists items, imports files (via the system file picker, so no storage
 * permission is needed), opens them, and deletes them. Owns the small dialogs for links,
 * contacts and delete confirmation.
 */
@Composable
fun VaultHost(
    manager: VaultManager,
    vault: Vault,
    title: String,
    isUltra: Boolean,
    onLock: () -> Unit,
) {
    val context = LocalContext.current
    var items by remember(vault) { mutableStateOf(manager.loadItems(vault)) }
    var viewingImage by remember { mutableStateOf<VaultItem?>(null) }
    var viewingText by remember { mutableStateOf<VaultItem?>(null) }
    var pendingDelete by remember { mutableStateOf<VaultItem?>(null) }
    var addTextFor by remember { mutableStateOf<VaultCategory?>(null) }
    var pendingCategory by remember { mutableStateOf(VaultCategory.Images) }

    fun reload() {
        items = manager.loadItems(vault)
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            var failed = 0
            uris.forEach { uri ->
                runCatching { manager.addFile(vault, uri, pendingCategory) }.onFailure { failed++ }
            }
            reload()
            if (failed > 0) Toast.makeText(context, "Couldn't add $failed file(s)", Toast.LENGTH_SHORT).show()
        }
    }

    fun add(category: VaultCategory) {
        pendingCategory = category
        when (category) {
            VaultCategory.Images -> picker.launch(arrayOf("image/*"))
            VaultCategory.Videos -> picker.launch(arrayOf("video/*"))
            VaultCategory.Documents -> picker.launch(arrayOf("*/*"))
            VaultCategory.Links, VaultCategory.Contacts -> addTextFor = category
        }
    }

    fun open(item: VaultItem) {
        when (item.category) {
            VaultCategory.Images -> viewingImage = item
            VaultCategory.Videos, VaultCategory.Documents -> openExternally(context, manager, vault, item)
            VaultCategory.Links -> openLink(context, item.text)
            VaultCategory.Contacts -> viewingText = item
        }
    }

    val image = viewingImage
    if (image != null) {
        VaultViewerScreen(
            manager = manager,
            vault = vault,
            item = image,
            onBack = { viewingImage = null },
            onDelete = { pendingDelete = image },
        )
    } else {
        VaultScreen(
            title = title,
            isUltra = isUltra,
            items = items,
            onAdd = ::add,
            onOpen = ::open,
            onDelete = { pendingDelete = it },
            onLock = onLock,
        )
    }

    addTextFor?.let { category ->
        AddTextDialog(
            category = category,
            onDismiss = { addTextFor = null },
            onAdd = { name, value ->
                manager.addText(vault, category, name, value)
                reload()
                addTextFor = null
            },
        )
    }

    viewingText?.let { item ->
        AlertDialog(
            onDismissRequest = { viewingText = null },
            title = { Text(item.displayName) },
            text = { SelectionContainer { Text(item.text.orEmpty(), style = MaterialTheme.typography.bodyMedium) } },
            confirmButton = { TextButton(onClick = { viewingText = null }) { Text("Close") } },
        )
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this item?") },
            text = { Text("\"${item.displayName}\" will be permanently deleted from this vault.") },
            confirmButton = {
                TextButton(onClick = {
                    manager.remove(vault, item)
                    if (viewingImage?.id == item.id) viewingImage = null
                    reload()
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun AddTextDialog(
    category: VaultCategory,
    onDismiss: () -> Unit,
    onAdd: (name: String, value: String) -> Unit,
) {
    var name by remember(category) { mutableStateOf("") }
    var value by remember(category) { mutableStateOf("") }
    val isLink = category == VaultCategory.Links
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isLink) "Add a link" else "Add a contact") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isLink) "Label (optional)" else "Name") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(if (isLink) "URL" else "Phone, email or notes") },
                    singleLine = !isLink,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = value.isNotBlank(),
                onClick = { onAdd(name, value.trim()) },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun openExternally(context: Context, manager: VaultManager, vault: Vault, item: VaultItem) {
    try {
        val file = manager.decryptToCache(vault, item)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val view = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, item.mimeType ?: "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(view, "Open with"))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app can open this file", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Couldn't open this item", Toast.LENGTH_SHORT).show()
    }
}

private fun openLink(context: Context, text: String?) {
    val raw = text?.trim().orEmpty()
    if (raw.isEmpty()) return
    val url = if (raw.startsWith("http://") || raw.startsWith("https://")) raw else "https://$raw"
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: Exception) {
        Toast.makeText(context, "Couldn't open this link", Toast.LENGTH_SHORT).show()
    }
}
