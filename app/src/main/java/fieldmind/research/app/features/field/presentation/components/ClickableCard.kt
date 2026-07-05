package fieldmind.research.app.features.field.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.heightIn
import fieldmind.research.app.ui.theme.CuteElevations
/**
 * A [Card] with built-in [expressiveCardPress] animation (lift + scale) and [onClick].
 *
 * This is the primary clickable card wrapper for FieldMind. Use it anywhere a card
 * should respond to tap with the signature iOS-style lift-and-scale feedback.
 *
 * Defaults mirror the project conventions:
 * - RoundedCornerShape(34.dp)
 * - surfaceContainerLow background (subtle contrast from screen background)
 * - 6dp plush elevation (clickableTier) with soft shadow
 * - 1.5dp lift, 0.985 scale-down on press
 */
@Composable
fun ClickableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = 68.dp,
    shape: Shape = RoundedCornerShape(34.dp),
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.clickableTier),
    tonalElevation: Dp = CuteElevations.clickableTier,
    liftDp: Float = 1.5f,
    scaleDown: Float = 0.985f,
    border: androidx.compose.foundation.BorderStroke? = null,
    index: Int = 0,
    animate: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) = Card(
    modifier = modifier
        .fillMaxWidth()
        .heightIn(min = minHeight)
        .staggeredEntrance(index = index, animate = animate)
        .expressiveCardPress(liftDp = liftDp, scaleDown = scaleDown)
        .clickable(onClick = onClick),
    shape = shape,
    colors = colors,
    elevation = elevation,
    tonalElevation = tonalElevation,
    border = border,
    content = content
)

/**
 * A non-clickable information card with the same visual style as [ClickableCard].
 * Use this when you need the same card look but without interactive behavior.
 */
@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    minHeight: Dp = 64.dp,
    shape: Shape = RoundedCornerShape(34.dp),
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier),
    tonalElevation: Dp = CuteElevations.nonClickableTier,
    border: androidx.compose.foundation.BorderStroke? = null,
    index: Int = 0,
    animate: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) = Card(
    modifier = modifier
        .fillMaxWidth()
        .heightIn(min = minHeight)
        .staggeredEntrance(index = index, animate = animate),
    shape = shape,
    colors = colors,
    elevation = elevation,
    tonalElevation = tonalElevation,
    border = border,
    content = content
)

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
    shape: Shape = RoundedCornerShape(34.dp),
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.clickableTier),
    tonalElevation: Dp = CuteElevations.clickableTier,
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
        elevation = elevation,
        tonalElevation = tonalElevation,
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
 *
 * @param onClick click handler
 * @param accentColor the accent color for tinting the card background
 * @param tintStrength how strong the tint should be (default 0.06f for subtle, 0.10f for noticeable)
 * @param modifier modifier
 * @param shape corner shape
 * @param elevation card elevation
 * @param liftDp lift amount on press
 * @param scaleDown scale amount on press
 * @param content card content
 */
@Composable
fun ClickableCard(
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
    tintStrength: Float = 0.08f,
    minHeight: Dp = 68.dp,
    shape: Shape = RoundedCornerShape(34.dp),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.clickableTier),
    tonalElevation: Dp = CuteElevations.clickableTier,
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
    elevation = elevation,
    tonalElevation = tonalElevation,
    liftDp = liftDp,
    scaleDown = scaleDown,
    content = content
)
