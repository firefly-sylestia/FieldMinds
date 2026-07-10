package fieldmind.research.app.features.field.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fieldmind.research.app.shared.presentation.theme.JournalConfig
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
    val textureModifier = journalTextureModifier(journalConfig)

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(textureModifier)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
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
    val textureModifier = journalTextureModifier(journalConfig)

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(textureModifier)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
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
//  Styling Utilities — moved to JournalDecorations.kt (Phase 3)
// ════════════════════════════════════════════════════════════════════════

/**
 * Returns a modifier that applies irregular border shaping for sketch-like cards.
 * Kept private here because only JournalCard uses it — the broader helpers
 * ([journalBorderStroke], [journalTextureModifier]) were promoted to
 * JournalDecorations.kt so all universal cards share them.
 */
private fun journalShapeModifier(
    config: JournalConfig,
    shape: Shape
): Modifier {
    // For Irregular border style, add a subtle clip to give the card
    // a slightly uneven edge feel. For other styles, no extra modifier.
    return when (config.borderStyle) {
        fieldmind.research.app.shared.presentation.theme.CardBorderStyle.Irregular -> Modifier.clip(shape)
        else -> Modifier
    }
}
