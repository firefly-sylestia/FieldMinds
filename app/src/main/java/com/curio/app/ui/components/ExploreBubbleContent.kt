package com.curio.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.data.ExploreSession
import com.curio.app.data.formatElapsed
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.categoryInk
import com.curio.app.ui.theme.themedAccent
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/**
 * The explore-timer bubble's visual content — kept SHORT on purpose: just
 * the category glyph, the topic name and the live elapsed time. No
 * verb/target lines or descriptions; those live in the done-prompt, not on
 * a floating pill.
 *
 * Two shapes, animated between on expand/collapse (v6.12 — a Transition
 * springs the size, morphs the corner radius and crossfades the content
 * instead of the old instant swap):
 *  - **Minimized** (default): a compact capsule pill — category glyph chip,
 *    the topic name, and a chronometer-style elapsed readout. Tapping it
 *    expands; long topic names slow-scroll (marquee) inside the pill so the
 *    full name is readable without stretching it.
 *  - **Expanded**: a rounded card panel (NOT a full capsule — a capsule
 *    with that much content reads as a circle). A header row with the glyph
 *    chip + topic + elapsed + a Minimize chevron, then a row of labeled
 *    controls: **Pause / Resume**, **Stop**, **Hide**.
 *
 * While the transition runs, [onSizeChanged] reports the growing/shrinking
 * pixel size so the service can keep the window centered and clamped —
 * timer ticks are excluded (they fire outside the transition window).
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
    // Called with the new pixel size whenever the bubble resizes WHILE the
    // expand/collapse transition is running — the service keeps the window
    // centered/clamped so the growth reads as unfurling in place instead of
    // an anchored jump. Timer ticks never forward (see [sizeAnimating]).
    onSizeChanged: (wPx: Int, hPx: Int) -> Unit = { _, _ -> },
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

    // ── Expand/collapse transition (v6.12) ────────────────────────────
    // The bubble used to swap between the pill and the panel INSTANTLY —
    // the window snapped to the new size with no motion. Now a Transition
    // springs the size (pill ⇄ panel), morphs the corner radius (pill ⇄
    // card) and crossfades the content, and the window position follows the
    // animated size frame-by-frame via [onSizeChanged] — gated to this
    // transition so the per-second timer tick can never nudge the bubble.
    val transition = updateTransition(targetState = minimized, label = "bubbleExpand")
    val corner by transition.animateDp(
        transitionSpec = { tween(280, easing = FastOutSlowInEasing) },
        label = "bubbleCorner"
    ) { isMinimized -> if (isMinimized) PILL_CORNER_RADIUS else PANEL_CORNER_RADIUS }
    // True while the size/corner animation runs — size callbacks are only
    // forwarded to the service during this window.
    val sizeAnimating = transition.isRunning

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
        // Shape morphs with the transition: a near-capsule pill when
        // minimized, a rounded card when expanded — animated, so the two
        // shapes melt into each other instead of hard-swapping.
        shape = RoundedCornerShape(corner),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.50f)),
        // No elevation shadow: the old 8dp shadow rendered BEYOND the
        // overlay window's bounds and the window clipped it into a hard,
        // boxy edge around the pill. The crisp accent border carries the
        // definition now.
        shadowElevation = 0.dp,
        modifier = modifier
            .then(dragModifier)
            .onSizeChanged { size ->
                // Only while the expand/collapse transition runs — timer
                // ticks change the pill width by a pixel and must not move
                // the window.
                if (sizeAnimating) onSizeChanged(size.width, size.height)
            }
            // The minimized pill is tappable anywhere to expand; the expanded
            // bubble's buttons handle their own input. Applied conditionally
            // so the expanded bubble carries no dead clickable semantics.
            .then(if (minimized) Modifier.clickable { minimized = false } else Modifier)
    ) {
        // v6.12.1 — the custom transitionSpec (slide + fade + SizeTransform)
        // didn't resolve against the pinned animation 1.11.2 API, so the
        // bubble uses the DEFAULT AnimatedContent transition instead: it
        // crossfades the pill ⇄ panel AND animates the size via its built-in
        // SizeTransform, so the overlay window still grows/shrinks smoothly
        // (no more instant resize). The updateTransition above keeps driving
        // the corner morph + the isRunning size-gate; all animate on the
        // same `minimized` flip.
        AnimatedContent(
            targetState = minimized,
            label = "bubbleState"
        ) { isMinimized ->
            if (isMinimized) {
                MinimizedPill(
                    session = session,
                    category = category,
                    accent = accent,
                    ink = ink,
                    elapsed = elapsed,
                    onExpand = { minimized = false }
                )
            } else {
                ExpandedPanel(
                    session = session,
                    category = category,
                    accent = accent,
                    ink = ink,
                    elapsed = elapsed,
                    onTogglePause = onTogglePause,
                    onStop = onStop,
                    onHide = onHide,
                    onMinimize = { minimized = true }
                )
            }
        }
    }
}

/** The compact capsule pill — glyph chip + scrolling topic + compact timer. */
@Composable
private fun MinimizedPill(
    session: ExploreSession,
    category: CurioCategory,
    accent: Color,
    ink: Color,
    elapsed: Long,
    onExpand: () -> Unit
) {
    Row(
        modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CategoryGlyphChip(category = category, accent = accent, ink = ink)

        // Topic + live timer — the topic caps at [MINIMIZED_TOPIC_WIDTH] and
        // slow-scrolls (marquee) when it's longer, so the pill stays small.
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .widthIn(max = MINIMIZED_TOPIC_WIDTH)
        ) {
            MarqueeTopicText(
                text = session.topicName,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxWidth = MINIMIZED_TOPIC_WIDTH,
                paused = session.paused
            )
            Text(
                text = if (session.paused) "Paused · ${compactElapsed(elapsed)}"
                       else compactElapsed(elapsed),
                style = MaterialTheme.typography.labelSmall,
                color = if (session.paused) accent
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ── Expand to the full controls ────────────────────────────
        BubbleIconButton(
            icon = CurioIcons.KeyboardArrowUp,
            contentDescription = "Expand timer",
            tint = accent,
            onClick = onExpand
        )
    }
}

/**
 * The expanded card panel — header (glyph chip + topic + elapsed + Minimize
 * chevron) over a row of labeled controls (Pause/Resume, Stop, Hide).
 * Deliberately a rounded rectangle, not a capsule, so it never reads as a
 * circle.
 */
@Composable
private fun ExpandedPanel(
    session: ExploreSession,
    category: CurioCategory,
    accent: Color,
    ink: Color,
    elapsed: Long,
    onTogglePause: () -> Unit,
    onStop: () -> Unit,
    onHide: () -> Unit,
    onMinimize: () -> Unit
) {
    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── Header: glyph chip + topic + elapsed + minimize ────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CategoryGlyphChip(category = category, accent = accent, ink = ink)
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .widthIn(max = EXPANDED_TOPIC_WIDTH)
            ) {
                MarqueeTopicText(
                    text = session.topicName,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxWidth = EXPANDED_TOPIC_WIDTH,
                    paused = session.paused
                )
                Text(
                    text = if (session.paused) "Paused · ${formatElapsed(elapsed)}"
                           else formatElapsed(elapsed),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (session.paused) accent
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BubbleIconButton(
                icon = CurioIcons.KeyboardArrowDown,
                contentDescription = "Minimize timer",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onMinimize
            )
        }

        // ── Controls: labeled actions ──────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LabeledBubbleButton(
                icon = if (session.paused) CurioIcons.PlayArrow else CurioIcons.Pause,
                label = if (session.paused) "Resume" else "Pause",
                tint = accent,
                onClick = onTogglePause
            )
            LabeledBubbleButton(
                icon = CurioIcons.Stop,
                label = "Stop",
                tint = MaterialTheme.colorScheme.error,
                onClick = onStop
            )
            LabeledBubbleButton(
                icon = CurioIcons.Close,
                label = "Hide",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onHide
            )
        }
    }
}

/** The small category-glyph circle shown at the pill's start. */
@Composable
private fun CategoryGlyphChip(
    category: CurioCategory,
    accent: Color,
    ink: Color
) {
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
}

/**
 * Single-line topic text that slow-scrolls (marquee) when it's longer than
 * [maxWidth]: it holds at the start, glides left to reveal the full name,
 * holds, then glides back — so the complete topic is always readable inside
 * a small pill. Fits text simply sits still (no scrolling); while [paused]
 * the topic freezes at the start, matching the frozen timer.
 *
 * The visible box is `min(textWidth, maxWidth)` wide and clips; the text
 * inside is drawn at its full measured width and translated by the scroll
 * offset, so the overflowing tail actually appears instead of being
 * ellipsized away.
 */
@Composable
private fun MarqueeTopicText(
    text: String,
    style: TextStyle,
    color: Color,
    maxWidth: Dp,
    paused: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val textLayout = remember(text, style, density) {
        textMeasurer.measure(
            text = text,
            style = style,
            overflow = TextOverflow.Clip,
            softWrap = false,
            maxLines = 1
        )
    }
    val textWidthPx = textLayout.size.width
    val capPx = with(density) { maxWidth.toPx() }.roundToInt()
    val boxWidthPx = minOf(textWidthPx, capPx)

    val scrollX = remember { Animatable(0f) }
    val scrollDistance = (textWidthPx - boxWidthPx).coerceAtLeast(0)
    LaunchedEffect(scrollDistance, text, paused) {
        scrollX.snapTo(0f)
        if (paused || scrollDistance <= 0) return@LaunchedEffect
        // Cap the one-way glide (~12s) so an absurdly long topic never
        // crawls; the speed constant already makes the normal case slow.
        val travelMs = (scrollDistance / MARQUEE_PX_PER_MS).toInt().coerceIn(1, 12_000)
        while (true) {
            delay(MARQUEE_START_HOLD_MS)
            scrollX.animateTo(scrollDistance.toFloat(), tween(travelMs, easing = LinearEasing))
            delay(MARQUEE_END_HOLD_MS)
            scrollX.animateTo(0f, tween(travelMs, easing = LinearEasing))
            delay(MARQUEE_END_HOLD_MS)
        }
    }

    Box(
        modifier = modifier
            .requiredWidth(with(density) { boxWidthPx.toDp() })
            .clipToBounds()
    ) {
        Text(
            text = text,
            style = style,
            color = color,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier = Modifier
                .requiredWidth(with(density) { textWidthPx.toDp() })
                .graphicsLayer { translationX = -scrollX.value }
        )
    }
}

/** Small circular icon button used by the bubble's expand/minimize controls. */
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

/** Small labeled pill button used by the expanded panel's control row. */
@Composable
private fun LabeledBubbleButton(
    icon: String,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CurioIcon(
                name = icon,
                contentDescription = null,
                tint = tint,
                size = 16.dp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = tint
            )
        }
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

// ── Tuning constants ────────────────────────────────────────────────────
// Corner radii the shape animates between: a near-capsule pill when
// minimized (24dp ≈ the pill's half-height, so the ends stay fully rounded)
// and a refined card when expanded.
private val PILL_CORNER_RADIUS = 24.dp
private val PANEL_CORNER_RADIUS = 18.dp

// Topic area width caps: tight in the minimized pill, roomier in the
// expanded panel. Longer topics slow-scroll within these bounds.
private val MINIMIZED_TOPIC_WIDTH = 110.dp
private val EXPANDED_TOPIC_WIDTH = 180.dp

// Marquee tuning — a slow ticker (~42 px/s) that holds briefly at each end
// before gliding back, so the full topic name reveals itself at a readable
// pace without feeling restless.
private const val MARQUEE_PX_PER_MS = 0.042f
private const val MARQUEE_START_HOLD_MS = 900L
private const val MARQUEE_END_HOLD_MS = 1_100L
