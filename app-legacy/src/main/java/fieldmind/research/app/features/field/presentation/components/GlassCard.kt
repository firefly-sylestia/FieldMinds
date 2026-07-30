package fieldmind.research.app.features.field.presentation.components
import fieldmind.research.app.ui.theme.CuteCardDefaults

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme

/** CompositionLocal for Haze backdrop blur. */
val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

/**
 * A glassmorphism card with real backdrop blur via Haze.
 *
 * When [LocalHazeState] is provided, renders with GPU-accelerated backdrop blur.
 * Otherwise falls back to a semi-transparent frosted style.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = CuteCardDefaults.OptionShape,
    blurRadius: Dp = 24.dp,
    tintAlpha: Float = 0.55f,
    content: @Composable ColumnScope.() -> Unit
) {
    val hazeState = LocalHazeState.current
    val isDark = FieldMindTheme.colors.isDark

    val glassColor = MaterialTheme.colorScheme.surfaceContainer.copy(
        alpha = if (isDark) tintAlpha.coerceIn(0f, 0.55f) else tintAlpha.coerceIn(0f, 0.45f)
    )

    val cardModifier = if (hazeState != null) {
        modifier
            .fillMaxWidth()
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    blurRadius = blurRadius,
                    noiseFactor = 0.04f,
                    tints = listOf(HazeTint(color = glassColor))
                )
            )
    } else {
        modifier
            .fillMaxWidth()
            .clip(shape)
    }

    val cardColor = if (hazeState != null) Color.Transparent else glassColor
    Card(
        modifier = cardModifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(content = content)
        }
    }
}
