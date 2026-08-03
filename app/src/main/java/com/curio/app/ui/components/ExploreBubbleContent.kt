package com.curio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.curio.app.data.CurioCategories
import com.curio.app.data.ExploreSession
import com.curio.app.data.formatElapsed
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.categoryInk
import com.curio.app.ui.theme.themedAccent
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * The explore-timer bubble's visual content — the pill-shaped surface with
 * the category glyph chip, the topic name, a live elapsed chronometer, and
 * controls:
 *  - **Pause / Resume** — freezes/resumes the visible timer only (the
 *    end-of-session reminder still fires at the original start + duration).
 *  - **Stop** — ends the session, clears it, cancels the reminder.
 *  - **Minimize** — collapses to the compact chip+timer pill. The bubble
 *    STARTS minimized (small by default); tap the pill to expand it.
 *  - **Hide** — dismisses the bubble for the session; the notification
 *    (when live notifications are on) becomes the controller. Hidden is
 *    persisted on the session so the bubble doesn't pop back.
 *
 * Dragging lives HERE (Compose), not on the window: a system-overlay
 * ComposeView's composed child consumes every View-level touch, so a View
 * drag listener never fires. The pill reports raw drag deltas via [onDragBy]
 * and release via [onDragEnd] — the owning service moves the window. The
 * detector is slop-gated, so taps on the pill/buttons still land while real
 * drags reposition the bubble.
 *
 * Used by [com.curio.app.infrastructure.ExploreSessionService] inside a
 * system overlay window (`TYPE_APPLICATION_OVERLAY`), so it renders over
 * other apps — including the browser — while an explore session runs.
 * Pure presentation apart from the drag deltas and the transient minimize
 * state (which resets to small whenever the window is rebuilt).
 *
 * Theme-aware: surfaces, ink and borders come from [MaterialTheme] (so the
 * bubble follows light / dark / AMOLED / Material styles) and the accent is
 * the session category's theme-resolved color. The live elapsed value ticks
 * once per second while NOT paused (pause freezes it via the session).
 */
@Composable
fun ExploreBubbleContent(
    session: ExploreSession,
    onTogglePause: () -> Unit,
    onStop: () -> Unit,
    onHide: () -> Unit,
    onDragBy: (dx: Float, dy: Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = CurioCategories.byId(session.categoryId)
    val accent = category.themedAccent()
    val ink = category.categoryInk()

    // Live elapsed — recomputed every second while NOT paused. When paused
    // the value freezes (session.elapsedMillis handles the freeze itself).
    var now by remember(session.paused, session.startMillis) {
        mutableLongStateOf(System.currentTimeMillis())
    }
    LaunchedEffect(session.paused, session.startMillis) {
        if (session.paused) return@LaunchedEffect
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }
    val elapsed = session.elapsedMillis(now)

    // Minimized by default — a compact chip + timer pill. Expanded shows the
    // full controls. Transient UI state: whenever the window is rebuilt
    // (hide → re-show, service restart) the bubble comes back small.
    var minimized by remember { mutableStateOf(true) }

    // Drag — slop-gated Compose detector: taps on the pill/buttons still
    // land, real drags report deltas for the service to move the window.
    // Placed OUTER to the clickable so drags win over taps (a clickable that
    // consumes the up cancels the tap, and a consumed down/move cancels it).
    val dragModifier = Modifier.pointerInput(Unit) {
        detectDragGestures(onDragEnd = { onDragEnd() }) { change, dragAmount ->
            change.consume()
            onDragBy(dragAmount.x, dragAmount.y)
        }
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
        shadowElevation = 8.dp,
        modifier = modifier
            .then(dragModifier)
            // The minimized pill is tappable anywhere to expand; the expanded
            // bubble's buttons handle their own input. Applied conditionally
            // so the expanded bubble carries no dead clickable semantics.
            .then(if (minimized) Modifier.clickable { minimized = false } else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Category glyph chip ─────────────────────────────────
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(
                    name = category.iconGlyph,
                    contentDescription = null,
                    tint = ink,
                    size = 18.dp
                )
            }

            // ── Topic + live timer (capped width so long topic
            //    names ellipsize instead of stretching the bubble;
            //    tighter while minimized) ──
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .widthIn(max = if (minimized) 110.dp else 150.dp)
            ) {
                Text(
                    text = session.topicName,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when {
                        session.paused -> "Paused · ${compactElapsed(elapsed)}"
                        minimized -> compactElapsed(elapsed)
                        else -> "${formatElapsed(elapsed)} · ${session.verb.lowercase()} ${session.targetName}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (session.paused) accent
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (minimized) {
                // ── Expand back to the full controls ───────────────
                BubbleIconButton(
                    icon = CurioIcons.KeyboardArrowUp,
                    contentDescription = "Expand timer",
                    tint = accent,
                    onClick = { minimized = false }
                )
            } else {
                // ── Pause / Resume ─────────────────────────────────
                BubbleIconButton(
                    icon = if (session.paused) CurioIcons.PlayArrow else CurioIcons.Pause,
                    contentDescription = if (session.paused) "Resume exploring" else "Pause exploring",
                    tint = accent,
                    onClick = onTogglePause
                )

                // ── Stop ───────────────────────────────────────────
                BubbleIconButton(
                    icon = CurioIcons.Stop,
                    contentDescription = "Stop exploring",
                    tint = MaterialTheme.colorScheme.error,
                    onClick = onStop
                )

                // ── Minimize (collapse to the small pill) ─────────
                BubbleIconButton(
                    icon = CurioIcons.KeyboardArrowDown,
                    contentDescription = "Minimize timer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { minimized = true }
                )

                // ── Hide (notification takes over) ────────────────
                BubbleIconButton(
                    icon = CurioIcons.Close,
                    contentDescription = "Hide this timer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onHide
                )
            }
        }
    }
}

/** Small circular icon button used by the bubble's Pause/Stop/Minimize/Hide controls. */
@Composable
private fun BubbleIconButton(
    icon: String,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
        shadowElevation = 0.dp
    ) {
        CurioIcon(
            name = icon,
            contentDescription = contentDescription,
            tint = tint,
            size = 20.dp,
            modifier = Modifier.padding(8.dp)
        )
    }
}

/**
 * Compact chronometer-style reading for the minimized pill ("12:34",
 * "1:02:34") — tighter than the friendly [formatElapsed] ("12m 5s") so the
 * small bubble stays small.
 */
private fun compactElapsed(millis: Long): String {
    val totalSeconds = (millis / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(Locale.US, hours, minutes, seconds)
    } else {
        "%02d:%02d".format(Locale.US, minutes, seconds)
    }
}
