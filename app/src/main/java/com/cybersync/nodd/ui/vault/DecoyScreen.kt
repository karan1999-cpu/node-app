package com.cybersync.nodd.ui.vault

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/**
 * The quiet screen shown after the secret gesture, before the Ultra PIN. It looks like an
 * ordinary "offline" placeholder; a tap anywhere continues to the PIN.
 */
@Composable
fun DecoyScreen(onContinue: () -> Unit) {
    val stars = remember { List(60) { Triple(Random.nextFloat(), Random.nextFloat() * 0.75f, Random.nextFloat()) } }
    val sky = Color(0xFF0B1030)
    val horizon = Color(0xFF141A44)
    val moon = Color(0xFFFFE9A8)
    val ridge = Color(0xFF070A1E)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(sky)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onContinue,
            ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(sky)
            drawRect(
                color = horizon,
                topLeft = Offset(0f, size.height * 0.55f),
                size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.45f),
            )
            stars.forEach { (fx, fy, fr) ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.25f + fr * 0.55f),
                    radius = 1f + fr * 2.2f,
                    center = Offset(fx * size.width, fy * size.height),
                )
            }
            drawCircle(
                color = moon,
                radius = size.minDimension * 0.08f,
                center = Offset(size.width * 0.72f, size.height * 0.24f),
            )
            drawCircle(
                color = sky,
                radius = size.minDimension * 0.07f,
                center = Offset(size.width * 0.68f, size.height * 0.21f),
            )
            val ridgePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, size.height * 0.7f)
                lineTo(size.width * 0.2f, size.height * 0.55f)
                lineTo(size.width * 0.38f, size.height * 0.66f)
                lineTo(size.width * 0.58f, size.height * 0.48f)
                lineTo(size.width * 0.78f, size.height * 0.64f)
                lineTo(size.width, size.height * 0.56f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(ridgePath, ridge)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                "You're offline",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Nothing to sync right now.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))
            Text(
                "Tap to continue",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.45f),
            )
        }
    }
}
