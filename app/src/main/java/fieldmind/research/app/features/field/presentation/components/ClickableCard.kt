package fieldmind.research.app.features.field.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fieldmind.research.app.ui.theme.CuteCardDefaults
import fieldmind.research.app.ui.theme.CuteElevations

/**
 * A clickable card with built-in [expressiveCardPress] animation (lift + scale),
 * explicit [tonalElevation] for visible dark-mode depth (surfaceTint overlay),
 * and [shadowElevation] for the drop shadow.
 *
 * Uses [Surface] internally (instead of [androidx.compose.material3.Card]) so that
 * [tonalElevation] and [shadowElevation] can be controlled separately.
 *
 * Defaults:
 * - [CuteCardDefaults.Shape] (32dp)
 * - surfaceContainerLow background
 * - tonalElevation = clickableTier (8dp) — produces visible primary-tint overlay on dark backgrounds
 * - shadowElevation = clickableTier (8dp)
 * - 1.5dp lift, 0.985 scale-down on press
 */
@Composable
fun ClickableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = 68.dp,
    shape: Shape? = null,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ),
    tonalElevation: Dp = CuteElevations.clickableTier,
    shadowElevation: Dp = CuteElevations.clickableTier,
    liftDp: Float = 1.5f,
    scaleDown: Float = 0.985f,
    border: BorderStroke? = null,
    index: Int = 0,
    animate: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val effectiveShape = shape ?: CuteCardDefaults.Shape
    val effectiveBorder = border ?: journalBorderStroke()

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .expressiveCardPress(liftDp = liftDp, scaleDown = scaleDown),
        shape = effectiveShape,
        color = colors.containerColor,
        contentColor = colors.contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = effectiveBorder
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

/**
 * A non-clickable information card with the same visual style as [ClickableCard].
 * Uses [Surface] with explicit [tonalElevation] / [shadowElevation] for dark-mode depth.
 */
@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    minHeight: Dp = 64.dp,
    shape: Shape? = null,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ),
    tonalElevation: Dp = CuteElevations.nonClickableTier,
    shadowElevation: Dp = CuteElevations.nonClickableTier,
    border: BorderStroke? = null,
    index: Int = 0,
    animate: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val effectiveShape = shape ?: CuteCardDefaults.Shape
    val effectiveBorder = border ?: journalBorderStroke()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight),
        shape = effectiveShape,
        color = colors.containerColor,
        contentColor = colors.contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = effectiveBorder
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

/**
 * Overload that controls whether [fillMaxWidth] is applied.
 * Pass `false` for inline or weighted layouts where the parent manages width.
 */
@Composable
fun ClickableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fillMaxWidth: Boolean = true,
    minHeight: Dp = 68.dp,
    shape: Shape = CuteCardDefaults.Shape,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ),
    tonalElevation: Dp = CuteElevations.clickableTier,
    shadowElevation: Dp = CuteElevations.clickableTier,
    liftDp: Float = 1.5f,
    scaleDown: Float = 0.985f,
    border: androidx.compose.foundation.BorderStroke? = null,
    index: Int = 0,
    animate: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val effectiveModifier = if (fillMaxWidth) modifier.fillMaxWidth() else modifier
    ClickableCard(
        onClick = onClick,
        modifier = effectiveModifier,
        minHeight = minHeight,
        shape = shape,
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        liftDp = liftDp,
        scaleDown = scaleDown,
        border = border,
        index = index,
        animate = animate,
        content = content
    )
}

/**
 * Convenience overload with an [accentColor] parameter that tints the card container.
 * Use this for colorful, visually distinct cards across the UI.
 */
@Composable
fun ClickableCard(
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
    tintStrength: Float = 0.08f,
    minHeight: Dp = 68.dp,
    shape: Shape = CuteCardDefaults.Shape,
    tonalElevation: Dp = CuteElevations.clickableTier,
    shadowElevation: Dp = CuteElevations.clickableTier,
    liftDp: Float = 1.5f,
    scaleDown: Float = 0.985f,
    content: @Composable ColumnScope.() -> Unit
) = ClickableCard(
    onClick = onClick,
    modifier = modifier,
    minHeight = minHeight,
    shape = shape,
    colors = CardDefaults.cardColors(
        containerColor = accentColor.copy(alpha = tintStrength)
    ),
    tonalElevation = tonalElevation,
    shadowElevation = shadowElevation,
    liftDp = liftDp,
    scaleDown = scaleDown,
    content = content
)
