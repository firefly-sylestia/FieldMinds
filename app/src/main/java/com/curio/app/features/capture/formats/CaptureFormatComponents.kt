package com.curio.app.features.capture.formats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.curio.app.data.JournalMood
import com.curio.app.data.NotePaperColor
import com.curio.app.data.NotePaperStyle
import com.curio.app.data.TextSpan
import com.curio.app.ui.components.NotePaperCard
import com.curio.app.ui.components.NotePaperColorToggle
import com.curio.app.ui.components.NotePaperStyleToggle
import com.curio.app.ui.components.RichTextEditor
import com.curio.app.ui.components.RichTextToolbarMode
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion
import com.curio.app.ui.theme.glyph
import com.curio.app.ui.theme.notePaperInk
import com.curio.app.ui.theme.paperAccent
import com.curio.app.ui.theme.paperBorder
import com.curio.app.ui.theme.paperInk
import com.curio.app.ui.theme.paperSurface
import com.curio.app.ui.theme.PatrickHandFontFamily
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
    accent: Color = paperAccent(),
    imeAction: ImeAction = ImeAction.Done,
    /** Note-paper style — ruled page / torn note / torn with ruled lines. */
    paperStyle: NotePaperStyle = NotePaperStyle.RULED,
    /** When provided, shows the per-field style toggle next to the label. */
    onPaperStyleChange: ((NotePaperStyle) -> Unit)? = null,
    /** Note-paper COLOR — cream / butter / pink / mint / sky / lilac. */
    paperColor: NotePaperColor = NotePaperColor.CREAM,
    /** When provided, shows the per-field color swatches next to the label. */
    onPaperColorChange: ((NotePaperColor) -> Unit)? = null
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // The style chips live on their OWN full-width scrollable row — six
        // styles + the rules chip overflow a phone-width label row (Rows
        // don't wrap), so the chips scroll instead of pushing the label off.
        if (onPaperStyleChange != null) {
            NotePaperStyleToggle(
                style = paperStyle,
                onStyleChange = onPaperStyleChange,
                accent = accent,
                modifier = Modifier.fillMaxWidth()
            )
        }
        // The color swatches live on their OWN row behind a toggle chip.
        if (onPaperColorChange != null) {
            NotePaperColorToggle(
                color = paperColor,
                onColorChange = onPaperColorChange,
                accent = accent
            )
        }
        // Ink follows the chosen sheet color so text stays readable on every
        // pastel — resolved in the composable scope, then passed in.
        val ink = notePaperInk(paperColor)
        // NotePaperCard dispatches EVERY style (ruled / torn / torn+rules /
        // coffee / folded / red-margin) so the single-line field wears the
        // same paper look as the rich-text fields — one source of truth.
        NotePaperCard(
            style = paperStyle,
            modifier = Modifier.fillMaxWidth(),
            paperColor = paperColor,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = PatrickHandFontFamily,
                    color = ink
                ),
                cursorBrush = SolidColor(accent),
                keyboardOptions = KeyboardOptions(imeAction = imeAction),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = PatrickHandFontFamily,
                                            color = ink.copy(alpha = 0.45f)
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

// ═══════════════════════════════════════════════════════════════════════════
// Quote cards — the hand-placed paper notecards (originally Marginalia's
// "Favorite quotes") extracted into a SHARED section so Reel Notes, Sound
// Bite and the Mood Board can offer the same quote field without copying
// the machinery. One [QuoteCardsState] per format body owns the parallel
// lists (text / spans / tilt / style / color); [QuoteCardsSection] renders
// the header + cards + add button; [RenderQuoteCards] is the saved-view
// counterpart in EntryDetailScreen.
// ═══════════════════════════════════════════════════════════════════════════

/** Hand-placed tilt in degrees (−2.5°..2.5°) for a quote card. */
private fun randomQuoteTilt(): Float = kotlin.random.Random.nextFloat() * 5f - 2.5f

/**
 * State holder for the reusable "Favorite quotes" section — owns the five
 * PARALLEL lists that describe every card (text, rich-text spans, hand-
 * placed tilt, per-card paper style, per-card paper color). All mutations
 * go through [addCard] / [removeCard] / [setText] / [setStyle] / [setColor]
 * so the lists never drift out of sync.
 */
class QuoteCardsState(
    initialQuotes: List<String>,
    initialSpans: List<List<TextSpan>>,
    initialTilts: List<Float>,
    initialStyles: List<NotePaperStyle>,
    initialColors: List<NotePaperColor>,
    defaultStyle: NotePaperStyle,
    defaultColor: NotePaperColor
) {
    val quotes = mutableStateListOf<String>().apply { addAll(initialQuotes) }
    val spans = mutableStateListOf<List<TextSpan>>().apply {
        addAll(initialSpans)
        // Pad any missing per-quote span lists (legacy entries) so the
        // parallel list always matches quotes 1:1.
        while (size < quotes.size) add(emptyList())
    }
    // Hand-placed tilt per card — generated ONCE when a card is born and
    // saved with the entry, so the angle the user adds with is the angle
    // that persists (never re-rolled by recomposition or revisits).
    val tilts = mutableStateListOf<Float>().apply {
        addAll(initialTilts)
        while (size < quotes.size) add(randomQuoteTilt())
    }
    val styles = mutableStateListOf<NotePaperStyle>().apply {
        addAll(initialStyles)
        // Pad any missing per-quote styles so the list matches 1:1.
        while (size < quotes.size) add(defaultStyle)
    }
    val colors = mutableStateListOf<NotePaperColor>().apply {
        addAll(initialColors)
        // Pad any missing per-quote colors so the list matches 1:1.
        while (size < quotes.size) add(defaultColor)
    }

    /** Whether any card has real text — drives the format's canSave. */
    val hasContent: Boolean get() = quotes.any { it.isNotBlank() }

    fun addCard(style: NotePaperStyle, color: NotePaperColor) {
        quotes.add("")
        spans.add(emptyList())
        // A fresh card gets its own tilt — and it STAYS that way.
        tilts.add(randomQuoteTilt())
        styles.add(style)
        colors.add(color)
    }

    fun removeCard(index: Int) {
        if (index !in quotes.indices) return
        quotes.removeAt(index)
        if (index < spans.size) spans.removeAt(index)
        if (index < tilts.size) tilts.removeAt(index)
        if (index < styles.size) styles.removeAt(index)
        if (index < colors.size) colors.removeAt(index)
    }

    fun setText(index: Int, text: String, cardSpans: List<TextSpan>) {
        if (index !in quotes.indices) return
        quotes[index] = text
        spans[index] = cardSpans
    }

    fun setStyle(index: Int, style: NotePaperStyle) {
        if (index in styles.indices) styles[index] = style
    }

    fun setColor(index: Int, color: NotePaperColor) {
        if (index in colors.indices) colors[index] = color
    }
}

/**
 * Creates the [QuoteCardsState] for a format body, seeded from saved
 * [CaptureData] (edit mode) with [defaultStyle]/[defaultColor] as the
 * fallback paper look for legacy entries that predate per-card styles.
 */
@Composable
fun rememberQuoteCardsState(
    initialQuotes: List<String>,
    initialSpans: List<List<TextSpan>>,
    initialTilts: List<Float>,
    initialStyles: List<NotePaperStyle>,
    initialColors: List<NotePaperColor>,
    defaultStyle: NotePaperStyle,
    defaultColor: NotePaperColor
): QuoteCardsState = remember(
    initialQuotes, initialSpans, initialTilts, initialStyles, initialColors,
    defaultStyle, defaultColor
) {
    QuoteCardsState(initialQuotes, initialSpans, initialTilts, initialStyles, initialColors, defaultStyle, defaultColor)
}

/**
 * The shared "Favorite quotes" editor section — a header row with the card
 * count, one hand-placed paper notecard per quote (each with its own
 * rich-text toolbar + Ruled/Torn/color toggles + Remove), and a dashed
 * "Add quote" button. [newCardStyle]/[newCardColor] supply the paper look
 * NEW cards inherit (e.g. the format's primary field), and [enabled] lets
 * recording formats freeze the cards mid-capture.
 */
@Composable
fun QuoteCardsSection(
    state: QuoteCardsState,
    modifier: Modifier = Modifier,
    header: String = "Favorite quotes",
    placeholder: String = "\u201C...\u201D",
    enabled: Boolean = true,
    accent: Color = paperAccent(),
    newCardStyle: () -> NotePaperStyle = { NotePaperStyle.RULED },
    newCardColor: () -> NotePaperColor = { NotePaperColor.CREAM }
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = header,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (state.quotes.isNotEmpty()) {
                Text(
                    text = "${state.quotes.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        state.quotes.forEachIndexed { i, _ ->
            QuoteCard(
                index = i,
                state = state,
                enabled = enabled,
                accent = accent,
                placeholder = placeholder
            )
        }

        // Add-quote button (dashed-outline placeholder style)
        Surface(
            onClick = { state.addCard(newCardStyle(), newCardColor()) },
            enabled = enabled,
            shape = RoundedCornerShape(14.dp),
            color = paperSurface().copy(alpha = 0.6f),
            border = BorderStroke(1.dp, paperBorder()),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CurioIcon(
                    name = CurioIcons.Add,
                    contentDescription = null,
                    // NOT theme-aware: the cream button keeps the warm paper
                    // ink in both themes — the theme's onSurfaceVariant turns
                    // light-gray in dark mode and washes out on the cream.
                    tint = paperInk(),
                    size = 18.dp
                )
                Text(
                    text = "Add quote",
                    style = MaterialTheme.typography.labelLarge,
                    color = paperInk()
                )
            }
        }
    }
}

/**
 * One quote card — a hand-placed paper notecard (rotated a few degrees)
 * with its own rich-text toolbar + paper-style/color toggles. The header
 * row + toolbar render ABOVE the paper slip so the ruled lines line up
 * under the text while typing.
 */
@Composable
private fun QuoteCard(
    index: Int,
    state: QuoteCardsState,
    enabled: Boolean,
    accent: Color,
    placeholder: String
) {
    // The tilt SAVED with this card — generated at creation, never re-rolled
    // by recomposition, typing, or section switches.
    val rotation = state.tilts.getOrElse(index) { randomQuoteTilt() }
    val style = state.styles.getOrElse(index) { NotePaperStyle.RULED }
    val color = state.colors.getOrElse(index) { NotePaperColor.CREAM }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .rotate(rotation)
    ) {
        // ── Card header — quote mark + number, Remove on the right ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CurioIcon(
                name = CurioIcons.FormatQuote,
                contentDescription = null,
                tint = paperInk().copy(alpha = 0.55f),
                size = 16.dp
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Quote ${index + 1}",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = paperInk().copy(alpha = 0.7f),
                modifier = Modifier.weight(1f)
            )
            Surface(
                onClick = { state.removeCard(index) },
                enabled = enabled,
                shape = RoundedCornerShape(8.dp),
                color = Color.Transparent
            ) {
                Text(
                    text = "Remove",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
        RichTextEditor(
            modifier = Modifier.fillMaxWidth(),
            text = state.quotes.getOrElse(index) { "" },
            spans = state.spans.getOrElse(index) { emptyList() },
            onRichTextChange = { newText, newSpans -> state.setText(index, newText, newSpans) },
            placeholder = placeholder,
            toolbarMode = RichTextToolbarMode.MAIN,
            minHeight = 64.dp,
            enabled = enabled,
            ink = paperInk(),
            accent = accent,
            paper = true,
            paperStyle = style,
            onPaperStyleChange = { state.setStyle(index, it) },
            paperColor = color,
            onPaperColorChange = { state.setColor(index, it) },
            paperContentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}

/**
 * The shared "How did it make you feel?" mood row — a horizontally
 * scrollable chip row of every [JournalMood]. Tap a mood to set it, tap
 * again to clear. Rendered ONCE per capture screen (right above the format
 * options, by the caller) so every format/take shares the same picker; the
 * saved-entry meta card reads the stored mood.
 *
 * Selection is animated: the chosen chip pops up on a bouncy spring while
 * its fill + ink crossfade to the accent, so changing moods reads as a
 * physical press-and-settle instead of a hard state snap.
 */
@Composable
fun MoodChipsRow(
    mood: JournalMood?,
    accent: Color,
    onMoodChange: (JournalMood?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "How did it make you feel?",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            JournalMood.entries.forEach { m ->
                val selected = mood == m
                // Selected chip springs up (gummy overshoot) + its fill and
                // ink crossfade to the accent — the change animates instead
                // of snapping, matching Curio's chip-selection language.
                val chipScale by animateFloatAsState(
                    targetValue = if (selected) 1.08f else 1f,
                    animationSpec = CurioMotion.Springs.Bouncy,
                    label = "moodChipScale"
                )
                val chipBg by animateColorAsState(
                    targetValue = if (selected) accent
                                  else MaterialTheme.colorScheme.surfaceVariant,
                    animationSpec = tween(CurioMotion.Durations.Standard),
                    label = "moodChipBg"
                )
                val chipInk by animateColorAsState(
                    targetValue = if (selected) Color.White
                                  else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(CurioMotion.Durations.Standard),
                    label = "moodChipInk"
                )
                Surface(
                    onClick = { onMoodChange(if (selected) null else m) },
                    shape = RoundedCornerShape(50),
                    color = chipBg,
                    border = if (selected) null
                            else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.scale(chipScale)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CurioIcon(
                            name = m.glyph,
                            contentDescription = null,
                            tint = chipInk,
                            size = 16.dp
                        )
                        Text(
                            text = m.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = chipInk
                        )
                    }
                }
            }
        }
    }
}