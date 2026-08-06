package com.curio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.curio.app.data.CaptureData
import com.curio.app.data.CaptureFormat
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioEntry
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion
import com.curio.app.ui.theme.categoryBorder
import com.curio.app.ui.theme.categoryInk
import com.curio.app.ui.theme.categorySurface
import com.curio.app.ui.theme.onAccent
import com.curio.app.ui.theme.themedAccent

/**
 * Cabinet's entry card — used in the 2-col grid (Curio Cabinet contract).
 *
 * Upgraded with:
 *  - Press scale animation for tactile feel
 *  - Breathing shimmer on card image header
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CurioEntryCard(
    entry: CurioEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onLongClick: (() -> Unit)? = null
) {
    var pressed by remember { mutableStateOf(false) }
    val cat = CurioCategories.byId(entry.topic.categoryId)
    val accent = cat.themedAccent()
    // Cabinet cards are recomposed while the grid settles and while the
    // toolbar/search state changes. Keep the header brush stable per accent
    // so opening the Cabinet does not allocate a new gradient for every card.
    // cardGradient reads MaterialTheme, so resolve it in the composable
    // scope before remembering the non-composable Brush allocation.
    val headerGradient = CurioGradients.cardGradient(accent)
    val headerBrush = remember(headerGradient) {
        Brush.verticalGradient(headerGradient)
    }

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
        modifier = modifier
            .scale(pressScale)
            .combinedClickable(
                onClick = {
                    pressed = true
                    onClick()
                },
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(20.dp),
        // surfaceContainerHigh (not plain surface): in the AMOLED style
        // `surface` is pure black, which made the whole Cabinet grid of
        // cards invisible on the black page. The high container step keeps
        // a faint grey lift so cards read as boxes in every theme.
        color = cat.categorySurface(MaterialTheme.colorScheme.surfaceContainerHigh),
        border = if (selected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            cat.categoryBorder(
                fallback = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            )
        },
        shadowElevation = 0.dp,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image placeholder — richer theme-aware gradient that ends on
            // the ACTIVE background (cream / midnight / pure black / dynamic)
            // so the header melts into the surface behind the card, with the
            // category glyph as a bright watermark.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .background(headerBrush),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(
                    name = cat.iconGlyph,
                    contentDescription = null,
                    tint = cat.onAccent().copy(alpha = 0.9f),
                    size = 60.dp
                )
                if (selected) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            CurioIcon(
                                name = CurioIcons.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                size = 18.dp
                            )
                        }
                    }
                }
                // Legacy badge — restored FieldMind entries wear a small
                // dark pill in the header corner so they stay recognizable
                // next to native Curio captures.
                if (entry.isLegacy) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.30f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "LEGACY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = entry.topic.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
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
                // v7.17 — custom tags, shown as up to 2 tiny #chips so the
                // Cabinet card previews the labels added on the save page.
                if (entry.tags.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        entry.tags.take(2).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = cat.themedAccent().copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, cat.themedAccent().copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = cat.categoryInk(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        if (entry.tags.size > 2) {
                            Text(
                                text = "+${entry.tags.size - 2}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
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
                    EntryFormatBadges(entry)
                }
            }
        }
    }
}

/**
 * Bottom-right format indicator on a Cabinet card: a plain glyph for
 * single-format entries, or a small STACKED badge — one circle per take's
 * format (capped at 3, with a "+N" overflow chip) — for multi-section
 * Portfolio entries, so the whole take composition is visible at a glance.
 */
@Composable
private fun EntryFormatBadges(entry: CurioEntry) {
    val sections = (entry.captureData as? CaptureData.Portfolio)?.sections.orEmpty()
    if (sections.isEmpty()) {
        // Single-format entry (or an empty/malformed Portfolio): keep the
        // existing single-glyph look.
        CurioIcon(
            name = formatGlyph(entry.format),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            size = 16.dp
        )
        return
    }
    val visible = sections.take(3)
    val extra = sections.size - visible.size
    Row(
        // Negative spacing makes each badge overlap the previous one, like an
        // avatar stack; later children draw on top.
        horizontalArrangement = Arrangement.spacedBy((-6).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        visible.forEach { section ->
            FormatBadgeCircle(glyph = formatGlyph(section.format))
        }
        if (extra > 0) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
                modifier = Modifier.size(18.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "+$extra",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** One circular format badge in the stacked [EntryFormatBadges] cluster. */
@Composable
private fun FormatBadgeCircle(glyph: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
        modifier = Modifier.size(18.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            CurioIcon(
                name = glyph,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                size = 12.dp
            )
        }
    }
}

internal fun formatGlyph(format: CaptureFormat): String = when (format) {
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
