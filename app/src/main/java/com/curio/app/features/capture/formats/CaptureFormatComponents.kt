package com.curio.app.features.capture.formats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import coil.compose.rememberAsyncImagePainter
import kotlin.math.cos
import kotlin.math.sin

/**
 * Shared composable components used by the 6 capture format bodies.
 *
 * Each format owns its own state; these are pure render-only widgets
 * that get composed into the format-specific bodies. Keeping them here
 * avoids copy-paste across the format files (Reel Notes + Open Notebook
 * both need ImageThumb, Field Notes uses CollapsibleSectionHeader, etc.).
 */

/**
 * A truly FILLED 5-pointed star drawn as a solid Canvas path.
 *
 * The bundled icon font is Material Symbols *Outlined*, where even the
 * `star` ligature renders as a hollow outline — so filled rating stars
 * were just outlines with a tint. Drawing the path directly guarantees a
 * solid fill in any color. When [filled] is false the SAME solid path is
 * drawn at low alpha, so a rating row reads filled-or-ghost, never hollow.
 */
@Composable
fun FilledStar(
    color: Color,
    starSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    filled: Boolean = true
) {
    Canvas(modifier = modifier.size(starSize)) {
        val radius = this.size.minDimension / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val star = Path()
        // 5-pointed star: outer points at full radius, inner at ~42%.
        for (i in 0 until 10) {
            val angle = Math.toRadians(-90.0 + i * 36.0)
            val r = if (i % 2 == 0) radius else radius * 0.42f
            val x = center.x + (r * cos(angle)).toFloat()
            val y = center.y + (r * sin(angle)).toFloat()
            if (i == 0) star.moveTo(x, y) else star.lineTo(x, y)
        }
        star.close()
        drawPath(star, color = if (filled) color else color.copy(alpha = 0.25f))
    }
}

/**
 * 1-5 star rating row — CURIO_SPEC §8.2 ReelNotes ("optional star rating,
 * 1-5"). Tap a star to set; tap the currently-set star to clear (return
 * to 0). [accent] controls the filled-star color in the category palette.
 * Stars are [FilledStar] Canvas paths — solid fills, never outlines.
 */
@Composable
fun StarRating(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(5) { i ->
            val starNumber = i + 1
            val filled = i < rating
            FilledStar(
                color = if (filled) accent else MaterialTheme.colorScheme.outline,
                filled = filled,
                starSize = 32.dp,
                modifier = Modifier
                    .semantics {
                        // Canvas has no automatic label — restore the label
                        // the old CurioIcon used to carry.
                        contentDescription = "$starNumber star${if (starNumber == 1) "" else "s"}"
                    }
                    .clickable {
                        onRatingChange(if (rating == starNumber) 0 else starNumber)
                    }
            )
        }
    }
}

/**
 * Small image placeholder thumbnail — 80dp square with rounded corners.
 * Used by Reel Notes (poster/still attach) and Field Notes (single photo
 * attach). [onClick] opens the image in lightbox (Phase 4), [onRemove]
 * detaches the image from the format's state list.
 */
@Composable
fun ImageThumb(
    index: Int,
    accent: Color,
    tint: Color,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    imageUri: String? = null
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tint)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Attached image $index",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                CurioIcon(
                    name = CurioIcons.Image,
                    contentDescription = "Attached image $index",
                    tint = accent,
                    size = 28.dp
                )
            }
        }
    }
}

/**
 * Dashed-outline placeholder for adding a new image — used as a sibling
 * tile next to existing [ImageThumb]s. Tapping invokes [onClick] which
 * the format uses to append a placeholder to its image list.
 */
@Composable
fun AddImageButton(
    accent: Color,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Add"
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = tint,
        border = BorderStroke(
            width = 1.dp,
            color = accent.copy(alpha = 0.5f)
        ),
        modifier = modifier.size(80.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                CurioIcon(
                    name = CurioIcons.Add,
                    contentDescription = "Add image",
                    tint = accent,
                    size = 22.dp
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = accent
                )
            }
        }
    }
}

/**
 * Collapsible section header with chevron — CURIO_SPEC §12.2 disclosure
 * pattern. Used by Field Notes (§8.5) and can be reused elsewhere.
 *
 * When [expanded] is false and [preview] is non-null, the preview snippet
 * is shown trailing the header so the user can see a glimpse of what's
 * hidden inside without expanding (per spec §12.2: "If a section has
 * content and gets collapsed, show a single-line grey preview snippet").
 */
@Composable
fun CollapsibleSectionHeader(
    label: String,
    accent: Color,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    preview: String? = null
) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CurioIcon(
                name = if (expanded) CurioIcons.KeyboardArrowUp
                        else CurioIcons.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = accent,
                size = 20.dp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (!expanded && preview != null) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(2f)
                )
            }
        }
    }
}