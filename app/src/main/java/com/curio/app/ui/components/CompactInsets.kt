package com.curio.app.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Status-bar padding minus [trim], floored at zero.
 *
 * Replaces the old `statusBarsPadding().offset(y = -6.dp)` hack used across
 * the Spin / Home top bars. `offset` shifts a laid-out element without
 * reclaiming its space, so the row still reserved the full inset height and
 * could visually collide with the status bar on short devices. Trimming the
 * padding value itself actually removes the dead space above the content
 * while still guaranteeing we never draw *under* the status bar.
 */
@Composable
fun Modifier.compactStatusBarPadding(trim: Dp = 8.dp): Modifier {
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    return this.padding(top = (top - trim).coerceAtLeast(0.dp))
}

/** Raw status-bar top inset, for callers that need the value itself. */
@Composable
fun statusBarTopInset(): Dp =
    WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
