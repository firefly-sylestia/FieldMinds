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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategory
import com.curio.app.data.TopicJsonLoader
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion

/**
 * Compact category card shared by the standalone category picker and the
 * Spin page picker sheet — icon badge, category name, live topic count,
 * and an optional selected state (white outline + check). One component so
 * the two pickers can never drift apart visually.
 */
@Composable
fun CurioCategoryCard(
    category: CurioCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = CurioMotion.Springs.Press,
        label = "categoryCardScale"
    )

    val isWildcard = category.id == CategoryId.WILDCARD
    val cardColor = if (isWildcard) CurioColors.CoralBlush else category.accent
    val topicCount = remember(category.id) { TopicJsonLoader.cached(category.id)?.size ?: 0 }

    Surface(
        onClick = {
            pressed = true
            onClick()
        },
        shape = RoundedCornerShape(22.dp),
        color = cardColor,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        border = if (isSelected) BorderStroke(2.dp, Color.White.copy(alpha = 0.7f)) else null,
        modifier = modifier
            .fillMaxWidth()
            .height(104.dp)
            .scale(scale)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Ghost icon — decorative
            CurioIcon(
                name = category.iconGlyph,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.14f),
                size = 84.dp,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Icon badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CurioIcon(
                            name = category.iconGlyph,
                            contentDescription = null,
                            tint = Color.White,
                            size = 20.dp
                        )
                    }
                }
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
                    Surface(
                        shape = CircleShape,
                        color = Color.White
                    ) {
                        CurioIcon(
                            CurioIcons.Check, null,
                            tint = cardColor,
                            size = 16.dp,
                            modifier = Modifier.padding(3.dp)
                        )
                    }
                }
            }
        }
    }
}
