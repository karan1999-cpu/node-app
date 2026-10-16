package com.cybersync.nodd.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cybersync.nodd.R
import com.cybersync.nodd.data.Field
import com.cybersync.nodd.data.Profile
import com.cybersync.nodd.ui.Eyebrow
import com.cybersync.nodd.ui.Panel
import com.cybersync.nodd.ui.PrimaryButton
import com.cybersync.nodd.ui.SecondaryButton
import com.cybersync.nodd.ui.Wordmark
import com.cybersync.nodd.ui.icon
import com.cybersync.nodd.ui.label

@Composable
fun CardScreen(
    profile: Profile,
    onEdit: () -> Unit,
    onShowQr: () -> Unit,
    onSendFile: () -> Unit,
    onToggle: (Field, Boolean) -> Unit,
    onGoDark: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Long-pressing the wordmark opens Go Dark. Nothing on screen points to it.
            Wordmark(
                Modifier
                    .weight(1f)
                    .pointerInput(Unit) { detectTapGestures(onLongPress = { onGoDark() }) }
            )
            if (!profile.isBlank) {
                IconButton(onClick = onEdit) {
                    Icon(painterResource(R.drawable.ic_edit), contentDescription = "Edit my details")
                }
            }
        }

        if (profile.isBlank) {
            SetupPrompt(onEdit)
            HowItWorks()
        } else {
            CardPreview(profile)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Eyebrow("On your QR")
                FieldSwitches(profile, onToggle, onEdit)
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryButton("Show my QR", R.drawable.ic_qr, onShowQr)
                SecondaryButton("Send as contact file", R.drawable.ic_share, onSendFile)
            }
            Text(
                text = "Whoever scans your QR gets every detail that is switched on above, including your number when Mobile is on.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SetupPrompt(onEdit: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(24.dp)) {
            Text("Make your card", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Add your name and number once. After that, anyone can save you to their contacts by scanning your QR. No typing, no spelling out your number.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.size(4.dp))
            Button(
                onClick = onEdit,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text("Add my details", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun HowItWorks() {
    val steps = listOf(
        "Add your details" to "Name, mobile, and anything else you want to share.",
        "Show your QR" to "Hold up your phone. The screen brightens so it scans fast.",
        "They tap Save" to "Their camera opens Add contact with your details filled in.",
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Eyebrow("How it works")
        Panel {
            Column {
                steps.forEachIndexed { index, (title, body) ->
                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(title, style = MaterialTheme.typography.titleMedium)
                            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardPreview(profile: Profile) {
    val onCard = MaterialTheme.colorScheme.onPrimary
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = onCard,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(54.dp).clip(CircleShape).background(onCard),
                ) {
                    Text(profile.initials, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(profile.fullName, style = MaterialTheme.typography.headlineSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    val work = profile.valueOf(Field.Work)
                    if (work.isNotEmpty() && profile.shows(Field.Work)) {
                        Text(work, style = MaterialTheme.typography.bodyMedium, color = onCard.copy(alpha = 0.85f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            val count = Field.entries.count { profile.shows(it) }
            Text(
                text = if (count == 1) "Name + 1 detail on your QR" else "Name + $count details on your QR",
                style = MaterialTheme.typography.labelSmall,
                color = onCard.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun FieldSwitches(profile: Profile, onToggle: (Field, Boolean) -> Unit, onEdit: () -> Unit) {
    val present = Field.entries.filter { profile.has(it) }
    Panel {
        Column {
            present.forEachIndexed { index, field ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                val on = profile.shows(field)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(value = on, role = Role.Switch, onValueChange = { onToggle(field, it) })
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Icon(
                        painter = painterResource(field.icon()),
                        contentDescription = null,
                        tint = if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(field.label(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = profile.valueOf(field),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (on) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(checked = on, onCheckedChange = null)
                }
            }
            if (present.size < Field.entries.size) {
                if (present.isNotEmpty()) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                TextButton(onClick = onEdit, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("Add more details", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
