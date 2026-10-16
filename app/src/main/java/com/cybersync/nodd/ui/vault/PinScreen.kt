package com.cybersync.nodd.ui.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cybersync.nodd.R

/**
 * Numeric PIN pad. In setup mode it asks for the PIN twice. On unlock, the parent passes
 * [error] after a wrong PIN.
 */
@Composable
fun PinScreen(
    title: String,
    subtitle: String,
    isSetup: Boolean,
    error: String?,
    onSubmit: (CharArray) -> Unit,
    onBack: () -> Unit,
) {
    var entered by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf<String?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(error) {
        if (error != null) {
            localError = error
            entered = ""
        }
    }

    val confirming = isSetup && firstPin != null
    val heading = when {
        !isSetup -> title
        confirming -> "Confirm your PIN"
        else -> "Create a PIN"
    }
    val hint = when {
        !isSetup -> subtitle
        confirming -> "Enter the same PIN again"
        else -> "$subtitle Choose a 4–8 digit PIN. If you forget it, what's inside can't be recovered."
    }

    fun submit() {
        if (entered.length < 4) {
            localError = "Use at least 4 digits"
            return
        }
        if (isSetup) {
            val first = firstPin
            if (first == null) {
                firstPin = entered
                entered = ""
                localError = null
            } else if (first == entered) {
                onSubmit(entered.toCharArray())
            } else {
                localError = "Those didn't match. Start again."
                firstPin = null
                entered = ""
            }
        } else {
            onSubmit(entered.toCharArray())
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) {
                Icon(painterResource(R.drawable.ic_back), contentDescription = "Back")
            }
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            Icon(
                painterResource(R.drawable.ic_lock),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(heading, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            repeat(8) { i ->
                val filled = i < entered.length
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(
                            if (filled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = localError ?: " ",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.height(20.dp),
        )

        Spacer(Modifier.weight(1f))
        Keypad(
            onDigit = { if (entered.length < 8) { entered += it; localError = null } },
            onBackspace = { if (entered.isNotEmpty()) entered = entered.dropLast(1) },
            onEnter = ::submit,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Keypad(onDigit: (Char) -> Unit, onBackspace: () -> Unit, onEnter: () -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { d -> Key(Modifier.weight(1f), label = d, onClick = { onDigit(d[0]) }) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Key(Modifier.weight(1f), iconRes = R.drawable.ic_backspace, onClick = onBackspace)
            Key(Modifier.weight(1f), label = "0", onClick = { onDigit('0') })
            Key(Modifier.weight(1f), iconRes = R.drawable.ic_check, accent = true, onClick = onEnter)
        }
    }
}

@Composable
private fun Key(
    modifier: Modifier,
    label: String? = null,
    iconRes: Int? = null,
    accent: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (accent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLowest,
        contentColor = if (accent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        modifier = modifier.aspectRatio(1.6f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            when {
                iconRes != null -> Icon(painterResource(iconRes), contentDescription = null, modifier = Modifier.size(24.dp))
                label != null -> Text(label, style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}
