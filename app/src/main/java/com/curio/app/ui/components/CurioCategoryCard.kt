package com.curio.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategory
import com.curio.app.data.TopicJsonLoader
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion

/**
 * Compact category card shared by the standalone category picker and the
 * Spin page picker sheet — category name, live topic count, a subtle ghost
 * watermark of the category glyph on the right edge, and an optional
 * selected state (crisp white rule + accent check badge). One component so
 * the two pickers can never drift apart visually.
 *
 * The full card surface uses the same theme-aware gradient as the main hero
 * cards (accent → softened toward the surface) so the tiles share the deck's
 * shade language, and the watermark is tinted with that gradient's accent
 * color instead of a flat white ghost.
 */
@Composable
fun CurioCategoryCard(
    category: CurioCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    // True press tracking (not a sticky click flag): the card returns to
    // rest scale after every tap — important now that cards are toggle
    // targets in the multi-select picker and get tapped repeatedly.
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.96f
            isSelected -> 1.03f
            else -> 1f
        },
        animationSpec = CurioMotion.Springs.Press,
        label = "categoryCardScale"
    )

    val isWildcard = category.id == CategoryId.WILDCARD
    // Full-card gradient — the same theme-aware treatment as the main cards,
    // so tiles and hero tickets always share one shade language.
    val gradient = CurioGradients.cardGradient(category.accent)
    val cardColor = CurioGradients.categoryCardFill(category.accent)
    val topicCount = remember(category.id) { TopicJsonLoader.cached(category.id)?.size ?: 0 }

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        border = if (isSelected) BorderStroke(2.dp, Color.White) else null,
        modifier = modifier
            .fillMaxWidth()
            .height(104.dp)
            .scale(scale)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(gradient),
                    RoundedCornerShape(22.dp)
                )
        ) {
            // Ghost icon — tinted with the card's gradient accent color
            // (echoed softly toward white) so the watermark carries the
            // same palette as the main-card gradient, not a flat white ghost.
            CurioIcon(
                name = category.iconGlyph,
                contentDescription = null,
                tint = lerp(cardColor, Color.White, 0.55f).copy(alpha = 0.18f),
                size = 64.dp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isWildcard) "Surprise mix" else "$topicCount topics",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1
                    )
                }
                if (isSelected) {
                    // Active state — accent-filled check badge on a crisp
                    // white rule, distinct from the idle tile.
                    Surface(
                        shape = CircleShape,
                        color = cardColor
                    ) {
                        CurioIcon(
                            name = CurioIcons.Check,
                            contentDescription = null,
                            tint = Color.White,
                            size = 16.dp,
                            modifier = Modifier.padding(3.dp)
                        )
                    }
                }
            }
        }
    }
}
