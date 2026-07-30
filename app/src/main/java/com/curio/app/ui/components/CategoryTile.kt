package com.curio.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategory
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion

/**
 * The canonical Curio category tile — a big saturated card with a
 * watermark glyph, a glassy icon chip, and the category name.
 *
 * Extracted from the Category Picker ("explore categories") so the Spin
 * screen's category sheet can render the exact same selection UI instead
 * of a cramped dropdown. [selected] draws a light ring + check badge for
 * use in the Spin sheet; the Picker leaves it false.
 *
 * @param compact Shrinks the tile to a height that fits comfortably
 *   inside a bottom sheet (used by Spin). The Picker uses the full size.
 */
@Composable
fun CategoryTile(
    category: CurioCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    compact: Boolean = false
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else if (selected) 1.02f else 1f,
        animationSpec = CurioMotion.Springs.Press,
        label = "tileScale"
    )

    val isWildcard = category.id == CategoryId.WILDCARD
    val cardColor = if (isWildcard) {
        CurioColors.CoralBlush.copy(alpha = 0.85f)
    } else {
        category.accent
    }

    val tileHeight: Dp = if (compact) 112.dp else 156.dp
    val corner: Dp = if (compact) 24.dp else 28.dp

    Surface(
        onClick = {
            pressed = true
            onClick()
        },
        shape = RoundedCornerShape(corner),
        color = cardColor,
        shadowElevation = if (selected) 12.dp else 8.dp,
        tonalElevation = 4.dp,
        border = if (selected) BorderStroke(2.5.dp, Color.White.copy(alpha = 0.9f)) else null,
        modifier = modifier
            .fillMaxWidth()
            .height(tileHeight)
            .scale(scale)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CurioIcon(
                name = category.iconGlyph,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.16f),
                size = if (compact) 76.dp else 104.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 4.dp)
            )
            TileContent(category = category, compact = compact)

            if (selected) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CurioIcon(
                            name = CurioIcons.Check,
                            contentDescription = "Selected",
                            tint = cardColor,
                            size = 16.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TileContent(category: CurioCategory, compact: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (compact) 14.dp else 18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            shape = RoundedCornerShape(if (compact) 14.dp else 18.dp),
            color = CurioColors.CreamWhite.copy(alpha = 0.22f)
        ) {
            CurioIcon(
                name = category.iconGlyph,
                contentDescription = null,
                tint = Color.White,
                size = if (compact) 26.dp else 34.dp,
                modifier = Modifier.padding(if (compact) 8.dp else 10.dp)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = category.displayName,
                style = if (compact) {
                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                } else {
                    MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                },
                color = Color.White
            )
        }
    }
}
