package com.curio.app.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons

/**
 * Home screen's hero "Spin the Wheel" card — see CURIO_SPEC.md §3.
 *
 * The single largest tap target on the home screen (~40% vertical space).
 * The placeholder design is intentionally graphic + tactile:
 * - Big rounded card with the category's accent at full alpha (or
 *   coral primary when no category is preselected).
 * - Big "SPIN THE WHEEL" title in geom at display weight.
 * - Helper subtitle below ("Tap to discover" or "Spin for {Category}").
 * - A secondary `casino` Material Symbols glyph sitting in the background,
 *   ~150dp, at 30% alpha — gives the card a "there's a wheel here" feel
 *   without fully rendering the actual dial component (that's Phase 3+).
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
    val isWildcard = wildcardSelected || selectedCategory?.id == com.curio.app.data.CategoryId.WILDCARD
    val activeAccent: Color = when {
        isWildcard -> CurioColors.CoralBlush
        selectedCategory != null -> selectedCategory.accent
        else -> CurioColors.CoralBlush
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(28.dp),
        color = activeAccent,
        shadowElevation = 6.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background wheel glyph — large, faint, suggests the dial
            CurioIcon(
                name = CurioIcons.AutoAwesome,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.20f),
                size = 180.dp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SPIN",
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = CurioColors.DeepPlum,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Column {
                    Text(
                        text = "the wheel",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = CurioColors.DeepPlum
                        )
                    )
                    Spacer(Modifier.height(4.dp))
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
