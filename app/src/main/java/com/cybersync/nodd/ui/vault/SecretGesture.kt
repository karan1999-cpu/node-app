package com.cybersync.nodd.ui.vault

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Watches for the hidden sequence to open Ultra Go Dark: tap the top-right corner three
 * times, then the bottom-left corner twice, within a few seconds.
 *
 * It observes taps in the Initial pass without consuming them, so every normal button and
 * tab underneath keeps working. Nothing on screen hints that the sequence exists.
 */
fun Modifier.cornerTapSequence(onTrigger: () -> Unit): Modifier = pointerInput(onTrigger) {
    val pattern = intArrayOf(TOP_RIGHT, TOP_RIGHT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_LEFT)
    val taps = ArrayDeque<Pair<Int, Long>>()
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val down = event.changes.firstOrNull { it.pressed && !it.previousPressed } ?: continue
            val x = down.position.x
            val y = down.position.y
            val w = size.width.toFloat()
            val h = size.height.toFloat()
            val zone = when {
                x > w * 0.6f && y < h * 0.22f -> TOP_RIGHT
                x < w * 0.4f && y > h * 0.78f -> BOTTOM_LEFT
                else -> NEUTRAL
            }
            if (zone == NEUTRAL) continue
            val now = System.currentTimeMillis()
            while (taps.isNotEmpty() && now - taps.first().second > WINDOW_MS) taps.removeFirst()
            taps.addLast(zone to now)
            while (taps.size > pattern.size) taps.removeFirst()
            if (taps.size == pattern.size && taps.mapIndexed { i, p -> p.first == pattern[i] }.all { it }) {
                taps.clear()
                onTrigger()
            }
        }
    }
}

private const val NEUTRAL = 0
private const val TOP_RIGHT = 1
private const val BOTTOM_LEFT = 2
private const val WINDOW_MS = 5000L
