package com.cybersync.nodd.ui.vault

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/**
 * The hidden way into Ultra Go Dark: tap the bottom-right corner five times quickly.
 *
 * It's an invisible hotspot pinned to the bottom-right, sized as a fraction of the screen
 * so it lands in the same place on any phone. It captures its own taps, so it never switches
 * tabs or presses a button underneath, and nothing on screen hints that it's there.
 *
 * Call it as the last child of a [Box] so it sits on top of whatever the screen is showing.
 */
@Composable
fun BoxScope.SecretCornerHotspot(
    taps: Int = 5,
    windowMs: Long = 4000L,
    widthFraction: Float = 0.42f,
    heightFraction: Float = 0.15f,
    onTrigger: () -> Unit,
) {
    val latest = rememberUpdatedState(onTrigger)
    val times = remember { ArrayDeque<Long>() }
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .fillMaxWidth(widthFraction)
            .fillMaxHeight(heightFraction)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    val now = System.currentTimeMillis()
                    while (times.isNotEmpty() && now - times.first() > windowMs) times.removeFirst()
                    times.addLast(now)
                    if (times.size >= taps) {
                        times.clear()
                        latest.value()
                    }
                })
            },
    )
}
