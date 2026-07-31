package com.curio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.curio.app.data.CaptureFormat
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioEntry
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion

/**
 * Cabinet's entry card — used in the 2-col grid (CURIO_SPEC.md §9).
 *
 * Upgraded with:
 *  - Press scale animation for tactile feel
 *  - Breathing shimmer on card image header
 */
@Composable
fun CurioEntryCard(
    entry: CurioEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val cat = CurioCategories.byId(entry.topic.categoryId)

    val pressScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = CurioMotion.Springs.Press,
        label = "cardPress"
    )

    // Reset press state after navigation
    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(300)
            pressed = false
        }
    }

    Surface(
        onClick = {
            pressed = true
            onClick()
        },
        modifier = modifier.scale(pressScale),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        if (cat.id == CategoryId.WILDCARD)
                            Brush.horizontalGradient(CurioGradients.WildcardGradientStops)
                        else Brush.horizontalGradient(listOf(cat.accent, cat.tint))
                    ),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(
                    name = cat.iconGlyph,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    size = 56.dp
                )
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = entry.topic.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.bodyPreview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimeAgo(entry.capturedAtDaysAgo),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CurioIcon(
                        name = formatGlyph(entry.format),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 16.dp
                    )
                }
            }
        }
    }
}

private fun formatGlyph(format: CaptureFormat): String = when (format) {
    CaptureFormat.SoundBite -> CurioIcons.Mic
    CaptureFormat.ReelNotes -> CurioIcons.Edit
    CaptureFormat.Marginalia -> CurioIcons.MenuBook
    CaptureFormat.GalleryWall -> CurioIcons.Image
    CaptureFormat.FieldNotes -> CurioIcons.AutoAwesome
    CaptureFormat.OpenNotebook -> CurioIcons.MenuBook
}

private fun formatTimeAgo(daysAgo: Int): String = when (daysAgo) {
    0 -> "Today"
    1 -> "Yesterday"
    else -> "${daysAgo}d ago"
}
