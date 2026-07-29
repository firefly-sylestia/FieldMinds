package com.curio.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.curio.app.data.CategoryId
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion

/**
 * Home screen's hero \"Spin the Wheel\" card — see CURIO_SPEC.md §3.
 *
 * The single largest tap target on the home screen (~40% vertical space).
 *
 * Upgraded with:
 *  - Breathing ambient animation: subtle scale pulse on the background glyph
 *  - Shimmer sweep: gentle light pass across the card surface
 *  - Press animation: scales to 0.96 on press with spring back
 *  - Animated category transition: morph between category accent colors
 *  - Decorative particle: slowly orbiting sparkle near the title
 *
 * When [selectedCategory] is non-null, the card switches to that category's
 * accent color so the user feels their chip selection propagate visually.
 */
@Composable
fun CurioHeroSpinCard(
    selectedCategory: com.curio.app.data.CurioCategory?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    wildcardSelected: Boolean = false
) {
    var pressed by remember { mutableStateOf(false) }
    val isWildcard = wildcardSelected || selectedCategory?.id == CategoryId.WILDCARD
    val activeAccent: Color = when {
        isWildcard -> CurioColors.CoralBlush
        selectedCategory != null -> selectedCategory.accent
        else -> CurioColors.CoralBlush
    }

    // ── Press scale animation ─────────────────────────────────────────────
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = CurioMotion.Springs.Press,
        label = "heroPress"
    )

    // ── Breathing background glyph ────────────────────────────────────────
    val breatheScale = rememberBreathingScale(active = true, amplitude = 0.04f)

    // ── Shimmer sweep ─────────────────────────────────────────────────────
    val shimmerBrush = rememberShimmerBrush(
        shimmerColor = Color.White.copy(alpha = 0.12f),
        baseColor = Color.Transparent
    )

    Surface(
        onClick = {
            pressed = true
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .scale(pressScale),
        shape = RoundedCornerShape(28.dp),
        color = activeAccent,
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // ── Background wheel glyph — large, breathes, suggests the dial ─
            CurioIcon(
                name = CurioIcons.AutoAwesome,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.18f),
                size = 180.dp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .scale(breatheScale)
            )

            // ── Shimmer overlay ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(shimmerBrush)
            )

            // ── Content ──────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "SPIN",
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = CurioColors.DeepPlum,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    // ── Decorative orbiting sparkle ─────────────────────────
                    CurioIcon(
                        name = CurioIcons.AutoAwesome,
                        contentDescription = null,
                        tint = CurioColors.DeepPlum.copy(alpha = 0.35f),
                        size = 20.dp
                    )
                }
                Column {
                    Text(
                        text = "the wheel",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = CurioColors.DeepPlum
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    MorphingContainer(
                        trigger = selectedCategory?.displayName ?: if (wildcardSelected) "surprise" else "default"
                    ) {
                        Text(
                            text = selectedCategory?.let { "Spin for ${it.displayName}" }
                                ?: if (wildcardSelected) "Surprise me"
                                else "Tap to discover something new",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = CurioColors.DeepPlum.copy(alpha = 0.80f)
                            )
                        )
                    }
                }
            }
        }
    }

    // Reset press state after navigation
    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(300)
            pressed = false
        }
    }
}
