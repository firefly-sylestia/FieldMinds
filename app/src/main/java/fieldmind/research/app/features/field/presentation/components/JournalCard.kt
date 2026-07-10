package fieldmind.research.app.features.field.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.shared.presentation.theme.CardBorderStyle
import fieldmind.research.app.shared.presentation.theme.JournalConfig
import fieldmind.research.app.shared.presentation.theme.JournalPresets
import fieldmind.research.app.shared.presentation.theme.LocalJournalStyle
import fieldmind.research.app.ui.theme.CuteElevations

// ════════════════════════════════════════════════════════════════════════
//  🌿 JournalCard — Journal-aware card components
//
//  These composables automatically read the active [JournalConfig] from
//  [LocalJournalStyle] and apply the aesthetic's card shape, border
//  treatment, shadow warmth, and optional texture overlay.
//
//  Replace plain `Card(...)` / `Surface(...)` calls with these to make
//  cards feel like they belong in the chosen journal style.
// ════════════════════════════════════════════════════════════════════════

/**
 * Non-clickable information card with journal-aware styling.
 * Reads [LocalJournalStyle] to pick corner radius, border, and shadow.
 *
 * @param modifier Modifier for the card.
 * @param shape Corner shape (defaults to journalConfig.cardCornerRadius).
 * @param colors Card colors (defaults to surfaceContainerLow themed).
 * @param tonalElevation Tonal elevation (defaults to non-clickable tier).
 * @param shadowElevation Shadow elevation (defaults to non-clickable tier).
 * @param border Optional border override.
 * @param content The card content.
 */
@Composable
fun JournalCard(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ),
    tonalElevation: Dp = CuteElevations.nonClickableTier,
    shadowElevation: Dp = CuteElevations.nonClickableTier,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val journalConfig = LocalJournalStyle.current
    val cardShape = shape ?: RoundedCornerShape(journalConfig.cardCornerRadius)
    val effectiveBorder = border ?: journalBorderStroke(journalConfig)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(journalShapeModifier(journalConfig, cardShape)),
        shape = cardShape,
        color = colors.containerColor,
        contentColor = colors.contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = effectiveBorder
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Optional journal texture overlay for card surface
            if (journalConfig.showTexture && journalConfig.textureOpacity > 0.01f) {
                JournalCardTextureOverlay(
                    journalConfig = journalConfig,
                    cardShape = cardShape
                )
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

/**
 * Clickable card with journal-aware styling + press animation.
 *
 * @param onClick Click handler.
 * @param modifier Modifier for the card.
 * @param shape Corner shape (defaults to journalConfig.cardCornerRadius).
 * @param colors Card colors (defaults to surfaceContainerLow themed).
 * @param tonalElevation Tonal elevation (defaults to clickable tier).
 * @param shadowElevation Shadow elevation (defaults to clickable tier).
 * @param liftDp Lift amount on press.
 * @param scaleDown Scale amount on press.
 * @param border Optional border override.
 * @param content The card content.
 */
@Composable
fun JournalClickableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ),
    tonalElevation: Dp = CuteElevations.clickableTier,
    shadowElevation: Dp = CuteElevations.clickableTier,
    liftDp: Float = 1.5f,
    scaleDown: Float = 0.985f,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val journalConfig = LocalJournalStyle.current
    val cardShape = shape ?: RoundedCornerShape(journalConfig.cardCornerRadius)
    val effectiveBorder = border ?: journalBorderStroke(journalConfig)

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .then(journalShapeModifier(journalConfig, cardShape))
            .expressiveCardPress(liftDp = liftDp, scaleDown = scaleDown),
        shape = cardShape,
        color = colors.containerColor,
        contentColor = colors.contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = effectiveBorder
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (journalConfig.showTexture && journalConfig.textureOpacity > 0.01f) {
                JournalCardTextureOverlay(
                    journalConfig = journalConfig,
                    cardShape = cardShape
                )
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

/**
 * Accent-tinted clickable card — uses an accent color for the card background.
 */
@Composable
fun JournalTintedCard(
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
    tintStrength: Float = 0.08f,
    shape: Shape? = null,
    liftDp: Float = 1.5f,
    scaleDown: Float = 0.985f,
    content: @Composable ColumnScope.() -> Unit
) = JournalClickableCard(
    onClick = onClick,
    modifier = modifier,
    shape = shape,
    colors = CardDefaults.cardColors(
        containerColor = accentColor.copy(alpha = tintStrength)
    ),
    tonalElevation = CuteElevations.clickableTier,
    shadowElevation = CuteElevations.clickableTier,
    liftDp = liftDp,
    scaleDown = scaleDown,
    content = content
)

// ════════════════════════════════════════════════════════════════════════
//  Styling Utilities
// ════════════════════════════════════════════════════════════════════════

/**
 * Returns a [BorderStroke] based on the journal config's border style.
 * - [CardBorderStyle.Irregular]: thin sketch-like border
 * - [CardBorderStyle.Rounded]: subtle rounded border
 * - [CardBorderStyle.Minimal]: no border
 */
private fun journalBorderStroke(config: JournalConfig): BorderStroke? {
    if (config.borderWidth <= 0.dp) return null
    val borderColor = when (config.borderStyle) {
        CardBorderStyle.Minimal -> Color.Transparent
        CardBorderStyle.Rounded -> MaterialTheme.colorScheme.outlineVariant
        CardBorderStyle.Irregular -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    return BorderStroke(config.borderWidth, borderColor)
}

/**
 * Returns a modifier that applies irregular border shaping for sketch-like cards.
 * For irregular style, we clip to a slightly different shape for a hand-drawn feel.
 */
private fun journalShapeModifier(
    config: JournalConfig,
    shape: Shape
): Modifier {
    // For Irregular border style, add a subtle clip to give the card
    // a slightly uneven edge feel. For other styles, no extra modifier.
    return when (config.borderStyle) {
        CardBorderStyle.Irregular -> Modifier.clip(shape)
        else -> Modifier
    }
}

/**
 * Overlays the journal's paper texture on a card surface.
 * Uses Canvas drawing for the texture pattern matching the active journal style.
 */
@Composable
private fun JournalCardTextureOverlay(
    journalConfig: JournalConfig,
    cardShape: Shape
) {
    val textureAlpha = (journalConfig.textureOpacity * 0.6f).coerceIn(0f, 0.15f)
    if (textureAlpha <= 0.001f) return

    // Use a Box overlay with a clipped Canvas
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
    ) {
        // This is rendered as a background texture using the AnimatedBackgroundScene's
        // texture utilities — but for card surfaces we use a lightweight Canvas approach
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth()) {
            val textureName = journalConfig.textureName
            val rng = rememberTextureRng("card_$textureName")

            when (textureName) {
                "parchment" -> {
                    // Subtle warm mottling
                    val warmColor = Color(0xFF8B6914)
                    for (i in 0..4) {
                        val cx = size.width * (rng[i * 3])
                        val cy = size.height * (rng[i * 3 + 1])
                        val cr = size.maxDimension * (0.03f + rng[i * 3 + 2] * 0.05f)
                        drawCircle(
                            color = warmColor.copy(alpha = textureAlpha * 0.3f * rng[i * 3 + 2]),
                            radius = cr,
                            center = androidx.compose.ui.geometry.Offset(cx, cy)
                        )
                    }
                }
                "paper" -> {
                    // Fine fiber-like lines
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
                            start = androidx.compose.ui.geometry.Offset(cx, cy),
                            end = androidx.compose.ui.geometry.Offset(endX, endY),
                            strokeWidth = 0.5f
                        )
                    }
                }
                "dotgrid" -> {
                    // Subtle dot grid
                    val dotColor = Color(0xFF9E9E9E)
                    val spacing = size.minDimension / 32f
                    var x = 0f
                    while (x < size.width) {
                        var y = 0f
                        while (y < size.height) {
                            drawCircle(
                                color = dotColor.copy(alpha = textureAlpha * 0.4f),
                                radius = 0.8f,
                                center = androidx.compose.ui.geometry.Offset(x, y)
                            )
                            y += spacing
                        }
                        x += spacing
                    }
                }
                "watercolor" -> {
                    // Soft color washes
                    val washColor = Color(0xFFD4A574)
                    for (i in 0..3) {
                        val cx = size.width * rng[i * 11]
                        val cy = size.height * rng[i * 11 + 1]
                        val cr = size.maxDimension * (0.08f + rng[i * 11 + 2] * 0.12f)
                        drawCircle(
                            color = washColor.copy(alpha = textureAlpha * 0.4f * rng[i * 11 + 3]),
                            radius = cr,
                            center = androidx.compose.ui.geometry.Offset(cx, cy)
                        )
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  Texture RNG Cache (shared with AnimatedBackgroundScene)
// ════════════════════════════════════════════════════════════════════════

private val cardTextureRngCache = mutableMapOf<String, List<Float>>()

@Composable
private fun rememberTextureRng(name: String): List<Float> {
    return remember(name) {
        cardTextureRngCache.getOrPut(name) {
            val rng = kotlin.random.Random(name.hashCode() + 42) // different seed from bg
            List(100) { rng.nextFloat() }
        }
    }
}
