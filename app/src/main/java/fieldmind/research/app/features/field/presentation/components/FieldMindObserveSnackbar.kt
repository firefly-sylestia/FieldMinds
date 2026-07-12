package fieldmind.research.app.features.field.presentation.components
import fieldmind.research.app.ui.theme.CuteCardDefaults

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Shows a snackbar message. Cancels any previous snackbar first so messages never stack.
 * Uses Short duration by default for fast, unobtrusive feedback.
 * Supports interactive action buttons.
 */
fun showFastSnackbar(
    hostState: SnackbarHostState,
    scope: CoroutineScope,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    customDuration: SnackbarDuration = SnackbarDuration.Short
) {
    scope.launch {
        hostState.currentSnackbarData?.dismiss()
        val result = hostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = customDuration
        )
        if (result == SnackbarResult.ActionPerformed) onAction?.invoke()
    }
}

/**
 * Determine the visual style for a snackbar based on its message content.
 * Returns (isSave, isError, isWarning, isAchievement).
 */
private data class SnackbarStyle(
    val isSave: Boolean = false,
    val isError: Boolean = false,
    val isWarning: Boolean = false,
    val isAchievement: Boolean = false
)

private fun snackbarStyle(message: String): SnackbarStyle {
    val isSave = message.startsWith("Observation saved") ||
        message.startsWith("Saved") ||
        message.startsWith("Quick snap saved") ||
        message.startsWith("Photo captured") ||
        message.startsWith("$") && !message.startsWith("🏆")
    val isAchievement = message.startsWith("🏆") || message.contains("unlocked!")
    val isError = message.startsWith("⚠") ||
        message.contains("required") ||
        message.contains("denied") ||
        message.contains("Couldn't") ||
        message.contains("cancelled")
    val isWarning = !isSave && !isError && !isAchievement &&
        (message.contains("empty") || message.contains("unavailable"))
    return SnackbarStyle(isSave, isError, isWarning, isAchievement)
}

/**
 * A unified top-positioned, glassmorphic, swipeable, interactive snackbar overlay.
 *
 * Features:
 * - Top-positioned with slide-in + bouncy scale animation
 * - Glassmorphic styling matching compass/level aesthetic
 * - Swipe horizontally to dismiss
 * - Tap anywhere on the snackbar to dismiss
 * - Styled by message content: save (green), error (red), warning (amber), achievement (gold)
 * - Icon in a colored circle with 12% alpha (glass metric pattern)
 * - Gradient accent top bar (3dp)
 * - Action buttons remain interactive (tap to act)
 * - Fully theme-aware via MaterialTheme color scheme
 */
@Composable
fun FieldMindSnackbarOverlay(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onSwipeDismiss: (() -> Unit)? = null
) {
    val data = hostState.currentSnackbarData
    val message = data?.visuals?.message.orEmpty()
    val style = snackbarStyle(message)
    val hasData = data != null
    val colors = FieldMindTheme.colors

    // ── Accent color derived from message style ──
    // Extract composable values BEFORE remember (remember's lambda has @DisallowComposableCalls)
    val themePositive = FieldMindTheme.colors.positive
    val themeError = MaterialTheme.colorScheme.error
    val themeWarning = FieldMindTheme.colors.warning
    val themeInfo = FieldMindTheme.colors.info
    val accentColor = remember(style) {
        when {
            style.isAchievement -> Color(0xFFFFD700) // gold
            style.isSave -> themePositive
            style.isError -> themeError
            style.isWarning -> themeWarning
            else -> themeInfo
        }
    }

    // Swipe-to-dismiss state
    var offsetX by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val dismissThreshold = with(density) { 120.dp.toPx() }

    // Bouncy spring for save/achievement, smooth for others
    val animSpec: FiniteAnimationSpec<Float> = if (style.isSave || style.isAchievement)
        spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    else
        spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)

    val scale by animateFloatAsState(
        targetValue = if (hasData) 1f else 0.85f,
        animationSpec = animSpec,
        label = "snackbarScale"
    )

    AnimatedVisibility(
        visible = hasData,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring<IntOffset>(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = tween(200)),
        exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut(animationSpec = tween(150)),
        modifier = modifier
    ) {
        data?.let { snackbarData ->
            // Glass background: surfaceContainerLow with subtle accent tint
            val bgColor = MaterialTheme.colorScheme.surfaceContainerLow

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(offsetX.toInt(), 0) }
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .shadow(16.dp, CuteCardDefaults.Shape, ambientColor = accentColor.copy(alpha = 0.08f), spotColor = accentColor.copy(alpha = 0.12f))
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (kotlin.math.abs(offsetX) > dismissThreshold) {
                                    onSwipeDismiss?.invoke()
                                    snackbarData.dismiss()
                                }
                                offsetX = 0f
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                offsetX = (offsetX + dragAmount)
                                    .coerceIn(-dismissThreshold * 2, dismissThreshold * 2)
                            }
                        )
                    },
                shape = CuteCardDefaults.Shape,
                color = bgColor,
                tonalElevation = 0.dp
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // ── Gradient accent top bar (3dp, matching glass card style) ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(accentColor.copy(alpha = 0.7f), accentColor)
                                )
                            )
                    )

                    // Whole snackbar is clickable to dismiss (but allow action buttons through)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { snackbarData.dismiss() }
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 18.dp, top = 16.dp, bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // ── Glass icon circle (32dp, 12% alpha, matching glass metric pattern) ──
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CuteCardDefaults.ChipShape)
                                    .background(accentColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when {
                                    style.isAchievement -> MaterialSymbolIcon("emoji_events")
                                    style.isSave -> MaterialSymbolIcon("check_circle")
                                    style.isError -> MaterialSymbolIcon("error")
                                    style.isWarning -> MaterialSymbolIcon("warning")
                                    else -> MaterialSymbolIcon("info")
                                }
                                Icon(
                                    icon = icon,
                                    contentDescription = null,
                                    tint = accentColor,
                                    size = 20.dp
                                )
                            }

                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (style.isSave || style.isAchievement)
                                    FontWeight.SemiBold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            val actionLabel = snackbarData.visuals.actionLabel
                            if (actionLabel != null) {
                                Spacer(Modifier.width(4.dp))
                                TextButton(onClick = {
                                    snackbarData.dismiss()
                                }) {
                                    Text(
                                        actionLabel,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
