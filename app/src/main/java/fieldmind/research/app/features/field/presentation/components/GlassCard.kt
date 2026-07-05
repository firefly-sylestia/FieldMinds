package fieldmind.research.app.features.field.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme

/**
 * CompositionLocal to provide a [HazeState] from a parent composable down to
 * child [GlassCard] composables. When set, [GlassCard] applies real backdrop
 * blur via Haze; when null, a visual-only glass effect is used.
 */
val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

/**
 * A glassmorphism card with real backdrop blur via Haze.
 *
 * When [LocalHazeState] is provided by a parent along with `.haze(state = hazeState)`
 * on the background content, this card renders with true GPU-accelerated backdrop blur.
 * Otherwise, it falls back to a visually similar semi-transparent style.
 *
 * Usage:
 * ```kotlin
 * // In parent:
 * val hazeState = remember { HazeState() }
 * Box {
 *     Content(modifier = Modifier.haze(state = hazeState))
 *     CompositionLocalProvider(LocalHazeState provides hazeState) {
 *         GlassCard { Text("Glass content") }
 *     }
 * }
 * ```
 *
 * @param modifier Modifier for the card.
 * @param shape Corner shape (defaults to 28dp rounded).
 * @param blurRadius Blur radius for the Haze effect (defaults to 24dp).
 * @param tintAlpha Background tint opacity (defaults to 0.78).
 * @param content The card content.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    blurRadius: Dp = 24.dp,
    tintAlpha: Float = 0.78f,
    content: @Composable ColumnScope.() -> Unit
) {
    val hazeState = LocalHazeState.current
    val isDark = FieldMindTheme.colors.isDark

    val glassColor = MaterialTheme.colorScheme.surfaceContainer.copy(
        alpha = if (isDark) tintAlpha else tintAlpha.coerceIn(0f, 0.85f)
    )

    val cardModifier = if (hazeState != null) {
        modifier
            .fillMaxWidth()
            .clip(shape)
            hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    blurRadius = blurRadius,
                    noiseFactor = 0.04f,
                    tints = listOf(
                        HazeTint(color = glassColor)
                    )
                )
            )
    } else {
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(glassColor, shape)
    }

    Card(
        modifier = cardModifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(content = content)
        }
    }
}
