package fieldmind.research.app.features.field.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import fieldmind.research.app.shared.presentation.theme.CardBorderStyle
import fieldmind.research.app.shared.presentation.theme.JournalConfig
import fieldmind.research.app.shared.presentation.theme.JournalStyle
import fieldmind.research.app.shared.presentation.theme.LocalJournalStyle

// ════════════════════════════════════════════════════════════════════════
//  JournalDecorations — shared journal-aware styling primitives
//
//  Phase 3 of the Whimsical Redesign. Every universal card composable in
//  ClickableCard.kt / SettingsComponents.kt / FieldMindComponents.kt reads
//  LocalJournalStyle and applies these primitives uniformly.
//
//  Public API used across screens:
//  - [journalBorderStroke]    — BorderStroke derived from JournalConfig
//  - [journalTextureModifier] — Modifier that draws the texture overlay
//  - [journalCardBrush]       — Brush for the card's container background
//                                (gradient vs flat)
//  - [JournalOrnament]        — small per-style flourish for section headers
//  - [JournalDivider]         — per-style divider rule
// ════════════════════════════════════════════════════════════════════════

/**
 * Returns a [BorderStroke] derived from the active journal's border config.
 * - [CardBorderStyle.Irregular]: thin sketch-like outline
 * - [CardBorderStyle.Rounded]: subtle rounded outline
 * - [CardBorderStyle.Minimal]: no border
 */
@Composable
fun journalBorderStroke(config: JournalConfig = LocalJournalStyle.current): BorderStroke? {
    if (config.borderWidth <= 0.dp) return null
    val color = when (config.borderStyle) {
        CardBorderStyle.Minimal -> Color.Transparent
        CardBorderStyle.Rounded -> MaterialTheme.colorScheme.outlineVariant
        CardBorderStyle.Irregular -> MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)
    }
    return BorderStroke(config.borderWidth, color)
}

/**
 * Draws the journal's paper / parchment / dot-grid / watercolor texture on
 * top of the card surface, via [Modifier.drawBehind]. Add to any Surface
 * modifier chain BEFORE press/click modifiers.
 */
@Composable
fun journalTextureModifier(
    config: JournalConfig = LocalJournalStyle.current,
    alphaScale: Float = 1f
): Modifier {
    if (!config.showTexture || config.textureOpacity <= 0.01f) return Modifier
    val effectiveAlpha = (config.textureOpacity * 0.6f * alphaScale).coerceIn(0f, 0.4f)
    return Modifier.drawBehind {
        drawJournalTexture(config, effectiveAlpha)
    }
}

/**
 * Returns a [Brush] for the active journal's card container — a warm gradient
 * for Victorian/Ghibli, a subtle paper-warmth overlay for Sketchbook, or a
 * solid color for BulletJournal.
 *
 * Combine with a base surface tint (e.g. surfaceContainerLow) to taste.
 */
@Composable
fun journalCardBrush(
    config: JournalConfig = LocalJournalStyle.current,
    fallbackColor: Color = MaterialTheme.colorScheme.surfaceContainerLow
): Brush {
    if (!config.useGradientCards) return SolidColor(fallbackColor)
    val warmth = config.accentWarmth
    val tint = config.cardSurfaceTint.copy(alpha = warmth.alpha)
    return when (config.style) {
        JournalStyle.Victorian -> Brush.linearGradient(
            colors = listOf(fallbackColor, tint, fallbackColor),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
        JournalStyle.Ghibli -> Brush.radialGradient(
            colors = listOf(tint, fallbackColor, fallbackColor),
            center = Offset.Unspecified,
            radius = Float.POSITIVE_INFINITY
        )
        JournalStyle.Sketchbook -> Brush.linearGradient(
            colors = listOf(fallbackColor, tint.copy(alpha = warmth.alpha * 0.5f), fallbackColor),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, 0f)
        )
        JournalStyle.BulletJournal -> SolidColor(fallbackColor)
    }
}

// ════════════════════════════════════════════════════════════════════════
//  Texture drawing (move from JournalCard.kt so all cards can use it)
// ════════════════════════════════════════════════════════════════════════

/**
 * Stable per-texture RNG pool — bit-identical to JournalCard.kt's prior
 * implementation, so transitions from the old code path render unchanged
 * on first paint.
 */
private val cardTextureRngValues = mutableMapOf<String, List<Float>>()

private fun cardTextureRng(name: String): List<Float> = cardTextureRngValues.getOrPut(name) {
    val rng = kotlin.random.Random(name.hashCode() + 42) // matches prior seed
    List(100) { rng.nextFloat() }
}

/**
 * Draws the journal's paper / parchment / dotgrid / watercolor texture into
 * the receiver DrawScope. Pattern matches [JournalConfig.textureName].
 */
fun DrawScope.drawJournalTexture(
    journalConfig: JournalConfig,
    textureAlpha: Float
) {
    if (textureAlpha <= 0.001f) return
    val textureName = journalConfig.textureName
    val rng = cardTextureRng(textureName)

    when (textureName) {
        "parchment" -> {
            val warmColor = Color(0xFF8B6914)
            for (i in 0..4) {
                val cx = size.width * rng[i * 3]
                val cy = size.height * rng[i * 3 + 1]
                val cr = size.maxDimension * (0.03f + rng[i * 3 + 2] * 0.05f)
                drawCircle(
                    color = warmColor.copy(alpha = textureAlpha * 0.3f * rng[i * 3 + 2]),
                    radius = cr,
                    center = Offset(cx, cy)
                )
            }
        }
        "paper" -> {
            val fiberColor = Color(0xFF8B7355)
            for (i in 0..8) {
                val cx = size.width * rng[i * 5]
                val cy = size.height * rng[i * 5 + 1]
                val length = size.maxDimension * (0.01f + rng[i * 5 + 2] * 0.02f)
                val angle = rng[i * 5 + 3] * 360f
                val endX = cx + kotlin.math.cos(angle) * length
                val endY = cy + kotlin.math.sin(angle) * length
                drawLine(
                    color = fiberColor.copy(alpha = textureAlpha * 0.5f * rng[i * 5 + 4]),
                    start = Offset(cx, cy),
                    end = Offset(endX, endY),
                    strokeWidth = 0.5f
                )
            }
        }
        "dotgrid" -> {
            val dotColor = Color(0xFF9E9E9E)
            val spacing = size.minDimension / 32f
            var x = 0f
            while (x < size.width) {
                var y = 0f
                while (y < size.height) {
                    drawCircle(
                        color = dotColor.copy(alpha = textureAlpha * 0.4f),
                        radius = 0.8f,
                        center = Offset(x, y)
                    )
                    y += spacing
                }
                x += spacing
            }
        }
        "watercolor" -> {
            val washColor = Color(0xFFD4A574)
            for (i in 0..3) {
                val cx = size.width * rng[i * 11]
                val cy = size.height * rng[i * 11 + 1]
                val cr = size.maxDimension * (0.08f + rng[i * 11 + 2] * 0.12f)
                drawCircle(
                    color = washColor.copy(alpha = textureAlpha * 0.4f * rng[i * 11 + 3]),
                    radius = cr,
                    center = Offset(cx, cy)
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  JournalOrnament — per-style flourish above section headers
// ════════════════════════════════════════════════════════════════════════

/**
 * Per-style ornamental flourish rendered above section headers (or list
 * cards) when the active journal style has [JournalConfig.showOrnaments]
 * set to `true`. Each style emits a different visual cue:
 *
 * - **Victorian**: tiny copperplate fleuron (local_florist) flanked by short
 *   ornamental rules
 * - **Ghibli**: soft cloud (cloud icon) + sparkles
 * - **Sketchbook**: subtle pencil dash
 * - **BulletJournal**: tiny dot trio
 *
 * The composing site decides layout (it's just a Composable, not a Box).
 */
@Composable
fun JournalOrnament(
    modifier: Modifier = Modifier,
    tint: Color? = null
) {
    val config = LocalJournalStyle.current
    if (!config.showOrnaments) return
    val effectiveTint = tint ?: FieldMindTheme.colors.accentFor("journal")

    when (config.style) {
        JournalStyle.Victorian -> {
            Icon(
                icon = MaterialSymbolIcon("local_florist"),
                contentDescription = null,
                tint = effectiveTint.copy(alpha = 0.7f),
                modifier = modifier.size(18.dp)
            )
        }
        JournalStyle.Ghibli -> {
            Box(modifier = modifier.size(22.dp), contentAlignment = Alignment.Center) {
                Icon(
                    icon = MaterialSymbolIcon("cloud"),
                    contentDescription = null,
                    tint = effectiveTint.copy(alpha = 0.65f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        JournalStyle.Sketchbook -> {
            // No icon — the "irregular" border does the visual work. Skip.
        }
        JournalStyle.BulletJournal -> {
            // No icon — minimal border does the work. Skip.
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  JournalDivider — per-style divider rule
// ════════════════════════════════════════════════════════════════════════

/**
 * Per-style divider rendered in place of a plain [HorizontalDivider].
 * Falls back to a thin [HorizontalDivider] when the active style has
 * [JournalConfig.decorativeDividers] set to `false`.
 *
 * Visual variants:
 * - **Victorian**: thin gold-toned rule with a center dot
 * - **Ghibli**:   soft wavy line (drawn as a Path)
 * - **Sketchbook**: three diagonal pencil marks
 * - **BulletJournal**: a row of evenly spaced tiny dots
 */
@Composable
fun JournalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outlineVariant
) {
    val config = LocalJournalStyle.current
    if (!config.decorativeDividers) {
        HorizontalDivider(thickness = thickness, color = color, modifier = modifier)
        return
    }
    val accent = FieldMindTheme.colors.accentFor("journal")

    when (config.style) {
        JournalStyle.Victorian -> {
            Canvas(
                modifier = modifier
                    .fillMaxWidth()
                    .height(thickness + 6.dp)
            ) {
                val midY = size.height / 2f
                val center = size.width / 2f
                val gap = 18f
                // Left rule
                drawLine(
                    color = color,
                    start = Offset(0f, midY),
                    end = Offset(center - gap, midY),
                    strokeWidth = 1f
                )
                // Right rule
                drawLine(
                    color = color,
                    start = Offset(center + gap, midY),
                    end = Offset(size.width, midY),
                    strokeWidth = 1f
                )
                // Center fleuron dot
                drawCircle(
                    color = accent.copy(alpha = 0.55f),
                    radius = 2.5f,
                    center = Offset(center, midY)
                )
            }
        }
        JournalStyle.Ghibli -> {
            Canvas(
                modifier = modifier
                    .fillMaxWidth()
                    .height(thickness + 8.dp)
            ) {
                val path = Path()
                val midY = size.height / 2f
                val stepCount = 24
                var x = 0f
                path.moveTo(0f, midY)
                repeat(stepCount) { i ->
                    x = size.width * (i + 1) / stepCount
                    val y = midY + kotlin.math.sin(i * 0.9f) * (size.height * 0.32f)
                    path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = color.copy(alpha = 0.7f),
                    style = Stroke(width = 1.4f)
                )
            }
        }
        JournalStyle.Sketchbook -> {
            Canvas(
                modifier = modifier
                    .fillMaxWidth()
                    .height(thickness + 6.dp)
            ) {
                val midY = size.height / 2f
                // three diagonal pencil marks \u2014 the irregular stroke gives the
                // divider a hand-drawn feel even without per-seed jitter
                drawLine(
                    color = color.copy(alpha = 0.55f),
                    start = Offset(size.width * 0.15f, midY - 3f),
                    end = Offset(size.width * 0.30f, midY + 3f),
                    strokeWidth = 1.1f
                )
                drawLine(
                    color = color.copy(alpha = 0.55f),
                    start = Offset(size.width * 0.45f, midY - 2.5f),
                    end = Offset(size.width * 0.60f, midY + 2.5f),
                    strokeWidth = 1.1f
                )
                drawLine(
                    color = color.copy(alpha = 0.55f),
                    start = Offset(size.width * 0.75f, midY - 3f),
                    end = Offset(size.width * 0.90f, midY + 3f),
                    strokeWidth = 1.1f
                )
            }
        }
        JournalStyle.BulletJournal -> {
            Canvas(
                modifier = modifier
                    .fillMaxWidth()
                    .height(thickness + 4.dp)
            ) {
                val midY = size.height / 2f
                val count = 32
                val gap = size.width / count
                for (i in 0 until count) {
                    drawCircle(
                        color = color.copy(alpha = 0.5f),
                        radius = 1.2f,
                        center = Offset(gap * (i + 0.5f), midY)
                    )
                }
            }
        }
    }
}
