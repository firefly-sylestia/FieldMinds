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
import fieldmind.research.app.ui.theme.CuteCardDefaults
import fieldmind.research.app.ui.theme.CuteElevations

// ════════════════════════════════════════════════════════════════════════
//  🌿 JournalCard — Journal-aware card components
//
//  v0.51.0 — Unified around CuteCardDefaults. The journal style system
//  has been retired in favour of a single "cute rounded" design language.
//
//  Replace plain `Card(...)` / `Surface(...)` calls with these to
//  maintain a consistent rounded card aesthetic across the app.
// ════════════════════════════════════════════════════════════════════════

/**
 * Non-clickable information card with cute rounded styling.
 *
 * @param modifier Modifier for the card.
 * @param shape Corner shape (defaults to [CuteCardDefaults.ShapeCompact], 24dp).
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
    val cardShape = shape ?: CuteCardDefaults.ShapeCompact
    val effectiveBorder = border ?: journalBorderStroke()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = cardShape,
        color = colors.containerColor,
        contentColor = colors.contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = effectiveBorder
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

/**
 * Clickable card with cute rounded styling + press animation.
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
    val cardShape = shape ?: CuteCardDefaults.ShapeCompact
    val effectiveBorder = border ?: journalBorderStroke()

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .expressiveCardPress(liftDp = liftDp, scaleDown = scaleDown),
        shape = cardShape,
        color = colors.containerColor,
        contentColor = colors.contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = effectiveBorder
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
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
