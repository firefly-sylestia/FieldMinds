package fieldmind.research.app.features.field.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import fieldmind.research.app.ui.theme.glassCard
import fieldmind.research.app.ui.theme.gradientBorder

/**
 * A clickable card with built-in [expressiveCardPress] animation (lift + scale),
 * glassmorphic frosted-glass surface, luminous gradient border, and soft shadow.
 *
 * Uses [Surface] internally with [glassCard] + [gradientBorder] modifiers for
 * a premium glass look that auto-adapts to dark/light mode.
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

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .glassCard(shape = effectiveShape)
            .gradientBorder(shape = effectiveShape)
            .expressiveCardPress(liftDp = liftDp, scaleDown = scaleDown),
        shape = effectiveShape,
        color = Color.Transparent,
        contentColor = colors.contentColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

/**
 * A non-clickable information card with glassmorphic styling.
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

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .glassCard(shape = effectiveShape)
            .gradientBorder(shape = effectiveShape),
        shape = effectiveShape,
        color = Color.Transparent,
        contentColor = colors.contentColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

/**
 * Overload that controls whether [fillMaxWidth] is applied.
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
