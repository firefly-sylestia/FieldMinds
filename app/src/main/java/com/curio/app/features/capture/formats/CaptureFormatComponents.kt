package com.curio.app.features.capture.formats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.curio.app.data.NotePaperStyle
import com.curio.app.ui.components.NotePaperStyleToggle
import com.curio.app.ui.components.PaperCard
import com.curio.app.ui.components.TornPaperCard
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.paperInk
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
 * A FILLED 5-pointed star with a crisp outline — drawn as a solid Canvas
 * path plus a stroke.
 *
 * The bundled icon font is Material Symbols *Outlined*, where even the
 * `star` ligature renders as a hollow outline — so filled rating stars
 * were just outlines with a tint. Drawing the path directly guarantees a
 * solid fill in any color. When [filled] is false the SAME solid path is
 * drawn at low alpha, so a rating row reads filled-or-ghost, never hollow.
 * Both states carry an outline stroke so the stars read as outlined.
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
        // Solid fill (ghost at low alpha when unrated) PLUS an outline
        // stroke, so the stars read as outlined instead of flat blobs.
        drawPath(star, color = if (filled) color else color.copy(alpha = 0.25f))
        drawPath(
            star,
            color = color.copy(alpha = if (filled) 0.85f else 0.45f),
            style = Stroke(width = this.size.minDimension * 0.07f)
        )
    }
}

/**
 * 1-5 star rating row — CURIO_SPEC §8.2 ReelNotes ("optional star rating,
 * 1-5"). Tap a star to set; tap the currently-set star to clear (return
 * to 0). [accent] controls the filled-star color in the category palette.
 * Stars are [FilledStar] Canvas paths — solid fills with an outline stroke.
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

/**
 * A single-line text field that wears the note-paper look — a small paper
 * slip with the text written in paper ink (cream in both themes), so short
 * inputs like titles and captions match the notebook style of the rich-text
 * fields instead of a plain outline box. [paperStyle] picks the slip: the
 * classic ruled [PaperCard], a torn note, or a torn note with ruled lines.
 * A thin [label] sits above the slip (same label language as the other
 * format fields); the field itself is the paper, no inner box or borders.
 * When [onPaperStyleChange] is provided, a compact Ruled/Torn/rules toggle
 * renders next to the label so the field keeps its own paper style.
 */
@Composable
fun PaperLineField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    enabled: Boolean = true,
    accent: Color = MaterialTheme.colorScheme.primary,
    imeAction: ImeAction = ImeAction.Done,
    /** Note-paper style — ruled page / torn note / torn with ruled lines. */
    paperStyle: NotePaperStyle = NotePaperStyle.RULED,
    /** When provided, shows the per-field style toggle next to the label. */
    onPaperStyleChange: ((NotePaperStyle) -> Unit)? = null
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (onPaperStyleChange != null) {
                    NotePaperStyleToggle(
                        style = paperStyle,
                        onStyleChange = onPaperStyleChange,
                        accent = accent
                    )
                }
            }
        } else if (onPaperStyleChange != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                NotePaperStyleToggle(
                    style = paperStyle,
                    onStyleChange = onPaperStyleChange,
                    accent = accent
                )
            }
        }
        val torn = paperStyle != NotePaperStyle.RULED
        val card: @Composable (PaddingValues) -> Unit = { pad ->
            if (torn) {
                TornPaperCard(
                    modifier = Modifier.fillMaxWidth(),
                    ruled = paperStyle == NotePaperStyle.TORN_RULED,
                    contentPadding = pad
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        enabled = enabled,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = paperInk()),
                        cursorBrush = SolidColor(accent),
                        keyboardOptions = KeyboardOptions(imeAction = imeAction),
                        decorationBox = { innerTextField ->
                            Box {
                                if (value.isEmpty() && placeholder.isNotEmpty()) {
                                    Text(
                                        text = placeholder,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = paperInk().copy(alpha = 0.45f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                PaperCard(
                    modifier = Modifier.fillMaxWidth(),
                    ruled = true,
                    contentPadding = pad
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        enabled = enabled,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = paperInk()),
                        cursorBrush = SolidColor(accent),
                        keyboardOptions = KeyboardOptions(imeAction = imeAction),
                        decorationBox = { innerTextField ->
                            Box {
                                if (value.isEmpty() && placeholder.isNotEmpty()) {
                                    Text(
                                        text = placeholder,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = paperInk().copy(alpha = 0.45f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        card(PaddingValues(horizontal = 14.dp, vertical = 12.dp))
    }
}