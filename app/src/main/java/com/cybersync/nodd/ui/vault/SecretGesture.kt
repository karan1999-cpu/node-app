package com.cybersync.nodd.ui.vault

import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

/**
 * The hidden way into Ultra Go Dark: tap anywhere on the screen [count] times in a quick burst.
 *
 * It watches taps in the Initial pass without consuming them, so every button, switch and tab
 * underneath keeps working normally. Only a fast burst ([count] taps within [windowMs]) triggers
 * it, so ordinary, spaced-out tapping doesn't. Put this on an outer Box that wraps the whole
 * screen, so taps anywhere — including the navigation bar area — are counted.
 */
fun Modifier.tapAnywhere(
    count: Int = 5,
    windowMs: Long = 1500L,
    onTrigger: () -> Unit,
): Modifier = composed {
    val latest = rememberUpdatedState(onTrigger)
    val times = remember { ArrayDeque<Long>() }
    pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val isDown = event.changes.any { it.pressed && !it.previousPressed }
                if (!isDown) continue
                val now = System.currentTimeMillis()
                while (times.isNotEmpty() && now - times.first() > windowMs) times.removeFirst()
                times.addLast(now)
                if (times.size >= count) {
                    times.clear()
                    latest.value()
                }
            }
        }
    }
}
