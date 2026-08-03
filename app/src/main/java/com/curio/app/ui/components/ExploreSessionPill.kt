package com.curio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.curio.app.data.CurioCategories
import com.curio.app.data.ExploreSession
import com.curio.app.data.formatElapsed
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.categoryInk
import com.curio.app.ui.theme.themedAccent
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Draggable floating pill — the live explore-timer controller that appears
 * while an explore session is active. Shows the topic, a live elapsed
 * chronometer, and three controls:
 *  - **Pause / Resume** — freezes/resumes the visible timer only (the
 *    end-of-session reminder still fires at the original start + duration);
 *    the persistent notification re-renders to match via [sync] on the
 *    service.
 *  - **Stop** — ends the session, clears it, cancels the reminder.
 *  - **Hide** — dismisses the pill for the session; the notification (when
 *    live notifications are on) becomes the controller. Hidden is persisted
 *    on the session so the pill doesn't pop back on recomposition.
 *
 * Drag anywhere (Messenger-bubble style): the pill snaps to the nearest
 * horizontal edge on release and stays clear of the status/navigation bars.
 *
 * Theme-aware: surfaces, ink and borders come from [MaterialTheme] (so the
 * pill follows light / dark / AMOLED / Material styles) and the accent is
 * the session category's theme-resolved color.
 */
@Composable
fun ExploreSessionPill(
    session: ExploreSession,
    onTogglePause: () -> Unit,
    onStop: () -> Unit,
    onHide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = CurioCategories.byId(session.categoryId)
    val accent = category.themedAccent()
    val ink = category.categoryInk()
    val density = LocalDensity.current
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Pill size (needed to clamp/snap) — measured once it composes.
    var pillW by remember { mutableStateOf(0) }
    var pillH by remember { mutableStateOf(0) }
    // Offset from the top-left of the full-screen host, in px. Null until
    // the first layout gives us the host + pill sizes for the initial
    // bottom-center placement.
    var offsetX by remember { mutableStateOf<Float?>(null) }
    var offsetY by remember { mutableStateOf<Float?>(null) }

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

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart
    ) {
        val hostW = maxWidth
        val hostH = maxHeight

        // Snap to the nearest horizontal edge and clamp vertically between
        // the status bar and the navigation bar (with a small margin).
        // Local lambda so the pointerInput below can call it without a
        // forward reference (Kotlin doesn't hoist local functions).
        val snapToEdge = {
            val curX = offsetX
            val curY = offsetY
            if (curX != null && curY != null) {
                val hw = with(density) { hostW.toPx() }
                val hh = with(density) { hostH.toPx() }
                val margin = with(density) { 12.dp.toPx() }
                val snapLeft = curX + pillW / 2f <= hw / 2f
                val snappedX = if (snapLeft) margin else (hw - pillW - margin).coerceAtLeast(margin)
                val topBound = with(density) { statusTop.toPx() } + margin
                val bottomBound = hh - pillH - with(density) { navBottom.toPx() } - margin
                offsetX = snappedX
                offsetY = curY.coerceIn(topBound, bottomBound.coerceAtLeast(topBound))
            }
        }

        // Initial placement: bottom-center, clear of the nav bar. Runs once
        // the host + pill are both measured.
        LaunchedEffect(hostW, hostH, pillW, pillH, offsetX == null) {
            if (pillW > 0 && pillH > 0 && offsetX == null) {
                val wPx = with(density) { hostW.toPx() }
                val hPx = with(density) { hostH.toPx() }
                val navPx = with(density) { navBottom.toPx() }
                // Taller margin on FIRST placement so the pill starts above
                // the bottom nav bar (it overlays the Scaffold as a sibling
                // and never sees CurioBottomBar itself).
                val bottomPad = with(density) { 88.dp.toPx() }
                offsetX = ((wPx - pillW) / 2f).coerceAtLeast(0f)
                offsetY = (hPx - pillH - navPx - bottomPad).coerceAtLeast(0f)
            }
        }

        val x = offsetX
        val y = offsetY
        if (x != null && y != null) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                    .onSizeChanged { pillW = it.width; pillH = it.height }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX = (offsetX ?: 0f) + dragAmount.x
                                offsetY = (offsetY ?: 0f) + dragAmount.y
                            },
                            onDragEnd = { snapToEdge() },
                            onDragCancel = { snapToEdge() }
                        )
                    }
            ) {
                Row(
                    modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // ── Category glyph chip ─────────────────────────────
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
                    //    names ellipsize instead of stretching the pill) ──
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .widthIn(max = 150.dp)
                    ) {
                        Text(
                            text = session.topicName,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (session.paused)
                                "Paused · ${formatElapsed(elapsed)}"
                            else
                                "${formatElapsed(elapsed)} · ${session.verb.lowercase()} ${session.targetName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (session.paused) accent
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // ── Pause / Resume ─────────────────────────────────
                    PillIconButton(
                        icon = if (session.paused) CurioIcons.PlayArrow else CurioIcons.Pause,
                        contentDescription = if (session.paused) "Resume exploring" else "Pause exploring",
                        tint = accent,
                        onClick = onTogglePause
                    )

                    // ── Stop ───────────────────────────────────────────
                    PillIconButton(
                        icon = CurioIcons.Stop,
                        contentDescription = "Stop exploring",
                        tint = MaterialTheme.colorScheme.error,
                        onClick = onStop
                    )

                    // ── Hide (notification takes over) ─────────────────
                    PillIconButton(
                        icon = CurioIcons.KeyboardArrowDown,
                        contentDescription = "Hide this timer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onHide
                    )
                }
            }
        }
    }
}

/** Small circular icon button used by the pill's Pause/Stop/Hide controls. */
@Composable
private fun PillIconButton(
    icon: String,
    contentDescription: String,
    tint: androidx.compose.ui.graphics.Color,
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
