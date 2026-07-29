package com.curio.app.features.spin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.MockTopics
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.ConfettiBurst
import com.curio.app.ui.components.ScreenEntrance
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * The Spin — see CURIO_SPEC.md §5. The signature roulette moment.
 *
 * Layout:
 *   - Top bar: ← "{Category} · Spin"
 *   - Big circular dial (280dp), 6 wedges in the category's accent family
 *   - Pointer at top (coral)
 *   - "Tap to spin" helper text (fades while spinning)
 *   - Big "SPIN" pill button below the dial
 *
 * Behavior:
 *   - Tap dial OR button to spin (redundant targets)
 *   - Rotation: 3-5 turns + landing offset, 2.5-3.5s, eased deceleration
 *   - On landing: winning wedge pulses, confetti burst in category accent,
 *     ~400ms pause, auto-navigate to Topic Reveal
 *   - Cannot re-tap mid-spin (button disabled, dial inert)
 */
@Composable
fun SpinScreen(categorySlug: String?, navController: NavController) {
    val cat = remember(categorySlug) {
        val resolved = categorySlug?.let { CurioCategories.byRouteSlug(it) }
            ?: CurioCategories.byId(CategoryId.WILDCARD)
        resolved
    }

    var spinning by remember { mutableStateOf(false) }
    var spinCount by remember { mutableIntStateOf(0) }
    var confettiTrigger by remember { mutableIntStateOf(0) }
    var winningSegment by remember { mutableIntStateOf(-1) }
    var landed by remember { mutableStateOf(false) }

    // One Animatable per spin (keyed on spinCount) — each spins from 0
    // and accumulates 3-5 turns + landing offset.
    val rotation = remember(spinCount) { Animatable(0f) }

    LaunchedEffect(spinCount) {
        if (spinCount == 0) return@LaunchedEffect
        spinning = true
        landed = false
        winningSegment = -1

        val turns = Random.nextInt(
            CurioMotion.MinSpinTurns,
            CurioMotion.MaxSpinTurns + 1
        )
        val targetSegment = Random.nextInt(CurioMotion.DialWedgeCount)
        val segmentAngle = 360f / CurioMotion.DialWedgeCount
        // Add a small jitter so the dial doesn't land dead-center every time
        val jitter = (Random.nextFloat() - 0.5f) * segmentAngle * 0.4f
        val targetAngle =
            turns * 360f +
            targetSegment * segmentAngle +
            segmentAngle / 2f +
            jitter

        rotation.animateTo(
            targetValue = targetAngle,
            animationSpec = tween(
                durationMillis = Random.nextInt(
                    CurioMotion.Durations.SpinMin,
                    CurioMotion.Durations.SpinMax + 1
                ),
                easing = CubicBezierEasing(0.15f, 0.85f, 0.2f, 1f)
            )
        )

        spinning = false
        landed = true
        winningSegment = targetSegment
        confettiTrigger++
    }

    // After landing: pause then navigate to TopicReveal with a random
    // topic from MockTopics.
    LaunchedEffect(confettiTrigger) {
        if (confettiTrigger == 0) return@LaunchedEffect
        delay(CurioMotion.Durations.RevealHold.toLong())
        val topic = MockTopics.randomPick()
        navController.navigate(
            CurioRoutes.revealFor(cat.id.routeSlug, topic.name)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // ── Top bar ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = { navController.popBackStack() },
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                CurioIcon(
                    name = CurioIcons.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    size = 24.dp,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Text(
                text = "${cat.displayName} \u00B7 Spin",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        ScreenEntrance {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // ── Dial ────────────────────────────────────────────────────
                Box(
                    modifier = Modifier.size(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DialCanvas(
                        rotation = rotation.value,
                        accent = cat.accent,
                        tint = cat.tint,
                        glyph = cat.iconGlyph,
                        winningSegment = winningSegment,
                        onTap = {
                            if (!spinning) spinCount++
                        }
                    )
                }

                // ── Helper text (fades while spinning) ──────────────────────
                AnimatedVisibility(
                    visible = !spinning,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = if (landed) "Spinning\u2026" else "Tap to spin",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ── SPIN button ─────────────────────────────────────────────
                Button(
                    onClick = {
                        if (!spinning) spinCount++
                    },
                    enabled = !spinning,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cat.accent,
                        contentColor = CurioColors.DeepPlum,
                        disabledContainerColor = cat.tint,
                        disabledContentColor =
                            CurioColors.DeepPlum.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 56.dp,
                        vertical = 20.dp
                    ),
                    modifier = Modifier.scale(if (landed) 0.95f else 1f)
                ) {
                    Text(
                        text = if (spinning) "Spinning\u2026"
                               else if (landed) "Spin again"
                               else "SPIN",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }
        }
    }

    if (confettiTrigger > 0) {
        ConfettiBurst(
            color = cat.accent,
            trigger = confettiTrigger,
            modifier = Modifier.fillMaxSize(),
            onComplete = {}
        )
    }
}

/**
 * The roulette dial — Canvas-rendered so the rotation stays buttery.
 *
 * Layers (back to front):
 *   1. Outer disc (tint)
 *   2. 6 wedges alternating accent (full) and accent @ 45% (low)
 *   3. Winning-wedge glow (cream @ 30% alpha, pulses after landing)
 *   4. Inner cream center disc
 *   5. Inner accent ring (stroke)
 *   6. Center category glyph (does not rotate — stays upright)
 *   7. Pointer triangle at top (does not rotate)
 */
@Composable
private fun DialCanvas(
    rotation: Float,
    accent: Color,
    tint: Color,
    glyph: String,
    winningSegment: Int,
    onTap: () -> Unit
) {
    // Winning wedge pulse — only animates while winningSegment >= 0
    val winningPulse by rememberInfiniteTransition(label = "winPulse")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 800,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "winPulseScale"
        )

    Box(
        modifier = Modifier
            .size(280.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onTap() })
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.minDimension
            val r = w / 2f
            val cx = size.width / 2f
            val cy = size.height / 2f

            // ── Rotating dial ────────────────────────────────────────────
            rotate(rotation, pivot = Offset(cx, cy)) {
                // Outer disc
                drawCircle(
                    color = tint,
                    radius = r * 0.96f,
                    center = Offset(cx, cy)
                )

                // Wedges (alternating full accent + accent @ 45% alpha)
                val wedges = CurioMotion.DialWedgeCount
                val wedgeAngle = 360f / wedges
                for (i in 0 until wedges) {
                    val startAngle = -90f + i * wedgeAngle
                    val isWinning = i == winningSegment && winningSegment >= 0
                    val baseColor = if (i % 2 == 0) accent
                                    else accent.copy(alpha = 0.45f)
                    drawArc(
                        color = baseColor,
                        startAngle = startAngle,
                        sweepAngle = wedgeAngle - 2f,
                        useCenter = true,
                        topLeft = Offset(cx - r, cy - r),
                        size = Size(r * 2, r * 2)
                    )
                    if (isWinning) {
                        drawArc(
                            color = CurioColors.CreamWhite.copy(
                                alpha = 0.30f * winningPulse
                            ),
                            startAngle = startAngle,
                            sweepAngle = wedgeAngle - 2f,
                            useCenter = true,
                            topLeft = Offset(cx - r * 0.92f, cy - r * 0.92f),
                            size = Size(r * 2 * 0.92f, r * 2 * 0.92f)
                        )
                    }
                }

                // Inner cream center disc
                drawCircle(
                    color = CurioColors.CreamWhite,
                    radius = r * 0.55f,
                    center = Offset(cx, cy)
                )
                // Inner accent ring
                drawCircle(
                    color = accent.copy(alpha = 0.3f),
                    radius = r * 0.55f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 4f)
                )
            }

            // ── Pointer (fixed at top, doesn't rotate) ───────────────────
            val pointerSize = r * 0.08f
            val path = Path().apply {
                moveTo(cx - pointerSize, 0f)
                lineTo(cx + pointerSize, 0f)
                lineTo(cx, pointerSize * 2f)
                close()
            }
            drawPath(path = path, color = CurioColors.CoralBlush)
            drawCircle(
                color = CurioColors.DeepPlum,
                radius = pointerSize * 0.35f,
                center = Offset(cx, pointerSize * 2f)
            )
        }

        // Center glyph — stays upright (the dial rotates beneath it)
        CurioIcon(
            name = glyph,
            contentDescription = null,
            tint = accent,
            size = 56.dp
        )
    }
}