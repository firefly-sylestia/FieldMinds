package com.curio.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateLeftPadding
import androidx.compose.foundation.layout.calculateTopPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt
import com.curio.app.data.TextSpan
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.paperHighlight

/**
 * The rich-text flags the toolbar can apply. [TextSpan] stores each as a
 * boolean so saved captures stay plain data + offsets.
 */
private enum class RichFlag { BOLD, ITALIC, HIGHLIGHT }

/** How the formatting toolbar is presented. */
enum class RichTextToolbarMode {
    /** Always visible — the Marginalia journal + quote cards (main option). */
    MAIN,

    /** Behind a small format button that expands the row (other fields). */
    TOGGLE
}

/**
 * Builds an [AnnotatedString] from plain [text] + [spans] — the shared
 * render path for the editor AND the saved-entry detail view, so bold /
 * italic / highlight look identical while editing and after saving.
 */
fun buildRichAnnotated(text: String, spans: List<TextSpan>, highlightColor: Color): AnnotatedString =
    buildAnnotatedString {
        append(text)
        for (sp in spans) {
            val s = sp.start.coerceIn(0, text.length)
            val e = sp.end.coerceIn(s, text.length)
            if (e > s) {
                addStyle(
                    SpanStyle(
                        fontWeight = if (sp.bold) FontWeight.Bold else null,
                        fontStyle = if (sp.italic) FontStyle.Italic else null,
                        // Non-null Color in this Compose version — the "no
                        // highlight" sentinel is Color.Unspecified.
                        background = if (sp.highlight) highlightColor else Color.Unspecified
                    ),
                    s, e
                )
            }
        }
    }

/**
 * Extracts the styled [TextSpan]s carried by an [AnnotatedString] — used to
 * read the editor's spans back out after Compose merges them while typing
 * (BasicTextField preserves span styles across edits, so no manual diffing).
 */
fun extractRichSpans(annotated: AnnotatedString): List<TextSpan> =
    annotated.spanStyles.mapNotNull { range ->
        val bold = range.item.fontWeight == FontWeight.Bold
        val italic = range.item.fontStyle == FontStyle.Italic
        val highlight = range.item.background != Color.Unspecified
        if (!bold && !italic && !highlight) null
        else TextSpan(range.start, range.end, bold, italic, highlight)
    }.merged()

/** Sorts and merges adjacent/overlapping spans with identical flags. */
private fun List<TextSpan>.merged(): List<TextSpan> {
    if (isEmpty()) return emptyList()
    val sorted = sortedWith(compareBy<TextSpan> { it.start }.thenBy { it.end })
    val out = mutableListOf<TextSpan>()
    for (sp in sorted) {
        val last = out.lastOrNull()
        if (last != null && last.end >= sp.start &&
            last.bold == sp.bold && last.italic == sp.italic && last.highlight == sp.highlight
        ) {
            out[out.size - 1] = last.copy(end = maxOf(last.end, sp.end))
        } else {
            out.add(sp)
        }
    }
    return out
}

private fun TextSpan.has(flag: RichFlag): Boolean = when (flag) {
    RichFlag.BOLD -> bold
    RichFlag.ITALIC -> italic
    RichFlag.HIGHLIGHT -> highlight
}

/** True when every character of [s, e) is covered by a span carrying [flag]. */
private fun spansFullyCovered(spans: List<TextSpan>, s: Int, e: Int, flag: RichFlag): Boolean {
    var pos = s
    for (sp in spans.filter { it.end > s && it.start < e }.sortedBy { it.start }) {
        if (sp.start > pos || !sp.has(flag)) return false
        pos = maxOf(pos, sp.end)
        if (pos >= e) return true
    }
    return pos >= e
}

/**
 * Finds the range of characters that changed between [oldText] and
 * [newText] (common-prefix / common-suffix diff, reported in NEW-text
 * coordinates). Used to apply an armed (sticky) format to exactly the
 * characters the user just typed — including typing over a selection
 * (replace) — while pure deletions and unchanged text return null.
 */
private fun findInsertedRange(oldText: String, newText: String): IntRange? {
    if (oldText == newText) return null
    var prefix = 0
    while (prefix < oldText.length && prefix < newText.length &&
        oldText[prefix] == newText[prefix]
    ) prefix++
    var suffix = 0
    while (suffix < oldText.length - prefix && suffix < newText.length - prefix &&
        oldText[oldText.length - 1 - suffix] == newText[newText.length - 1 - suffix]
    ) suffix++
    val start = prefix
    val end = newText.length - suffix
    return if (end > start) start until end else null
}

/**
 * Adds or removes [flag] over [s, e). Adding merges a new span in; removing
 * splits every overlapping span so the un-styled middle drops its flag while
 * the parts outside the selection keep theirs.
 */
private fun toggleSpanFlag(spans: List<TextSpan>, s: Int, e: Int, flag: RichFlag, add: Boolean): List<TextSpan> {
    if (add) {
        return (spans + TextSpan(
            start = s,
            end = e,
            bold = flag == RichFlag.BOLD,
            italic = flag == RichFlag.ITALIC,
            highlight = flag == RichFlag.HIGHLIGHT
        )).merged()
    }
    val out = mutableListOf<TextSpan>()
    for (sp in spans) {
        if (sp.end <= s || sp.start >= e) {
            out.add(sp)
            continue
        }
        if (sp.start < s) out.add(sp.copy(end = s))
        val midStart = maxOf(sp.start, s)
        val midEnd = minOf(sp.end, e)
        val mid = sp.copy(
            start = midStart,
            end = midEnd,
            bold = if (flag == RichFlag.BOLD) false else sp.bold,
            italic = if (flag == RichFlag.ITALIC) false else sp.italic,
            highlight = if (flag == RichFlag.HIGHLIGHT) false else sp.highlight
        )
        if (mid.bold || mid.italic || mid.highlight) out.add(mid)
        if (sp.end > e) out.add(sp.copy(start = e))
    }
    return out.merged()
}

/**
 * Rich-text editor shared by the capture formats — bold / italic / highlight
 * over the current selection, with an always-visible toolbar ([RichTextToolbarMode.MAIN],
 * the Marginalia journal + quotes) or a small format button that expands the
 * row ([RichTextToolbarMode.TOGGLE], other text fields).
 *
 * Edits flow through `BasicTextField`'s AnnotatedString value, so span styles
 * are preserved while typing; a small common-prefix/suffix diff re-applies an
 * armed (sticky) format to newly typed characters. Each change reports the
 * plain text + spans back via [onRichTextChange].
 */
@Composable
fun RichTextEditor(
    text: String,
    spans: List<TextSpan>,
    onRichTextChange: (text: String, spans: List<TextSpan>) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    minHeight: Dp = 96.dp,
    toolbarMode: RichTextToolbarMode = RichTextToolbarMode.MAIN,
    enabled: Boolean = true,
    accent: Color = MaterialTheme.colorScheme.primary,
    ink: Color = MaterialTheme.colorScheme.onSurface,
    surface: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    /** Inner padding of the text field — zero when the editor sits directly
     *  on note-paper (the surrounding card owns the margins). */
    fieldPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    /** Draws the field's hairline border — off when the editor sits on paper. */
    showFieldBorder: Boolean = true,
    highlightColor: Color = paperHighlight()
) {
    // NOTE: NOT keyed on [text] — the parent echoes our edits back, so a
    // keyed remember would rebuild the field (and drop the cursor) on every
    // keystroke. Hold the value unkeyed and reseed only when the parent
    // pushes a DIFFERENT text (e.g. editing a different saved entry).
    var tfv by remember {
        mutableStateOf(TextFieldValue(buildRichAnnotated(text, spans, highlightColor)))
    }
    var toolbarExpanded by remember { mutableStateOf(false) }
    // Text layout of the field — anchors the floating format bar to the
    // current selection so formatting existing text is discoverable.
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    // Armed (sticky) formats: tapping a toolbar button without a selection
    // arms it so the NEXT characters typed carry the format; applying a
    // format to a selection also arms it, so typing continues in that style.
    var pendingBold by remember { mutableStateOf(false) }
    var pendingItalic by remember { mutableStateOf(false) }
    var pendingHighlight by remember { mutableStateOf(false) }

    LaunchedEffect(text, spans) {
        if (tfv.text != text) {
            tfv = TextFieldValue(buildRichAnnotated(text, spans, highlightColor))
            // Different content loaded (e.g. editing another saved entry) —
            // drop any armed format from the previous text.
            pendingBold = false
            pendingItalic = false
            pendingHighlight = false
        }
    }

    fun emit(new: TextFieldValue) {
        var result = new
        val oldText = tfv.text
        // Sticky format: when a format is armed and the user just typed
        // (or typed over a selection), apply it to exactly the changed
        // characters so typing continues in that style (BasicTextField only
        // inherits the style under the caret, so an armed format needs
        // explicit application). Pure deletions diff to null and are skipped.
        if (pendingBold || pendingItalic || pendingHighlight) {
            findInsertedRange(oldText, new.text)?.let { range ->
                var spans = extractRichSpans(new.annotatedString)
                if (pendingBold) {
                    spans = toggleSpanFlag(spans, range.first, range.last + 1, RichFlag.BOLD, true)
                }
                if (pendingItalic) {
                    spans = toggleSpanFlag(spans, range.first, range.last + 1, RichFlag.ITALIC, true)
                }
                if (pendingHighlight) {
                    spans = toggleSpanFlag(spans, range.first, range.last + 1, RichFlag.HIGHLIGHT, true)
                }
                result = TextFieldValue(
                    buildRichAnnotated(new.text, spans, highlightColor),
                    selection = new.selection,
                    composition = new.composition
                )
            }
        }
        tfv = result
        // `text` is plain String in this Compose version; the styled
        // AnnotatedString lives on `annotatedString`.
        onRichTextChange(result.text, extractRichSpans(result.annotatedString))
    }

    fun applyFlag(flag: RichFlag) {
        val sel = tfv.selection
        if (sel.collapsed) {
            // No selection — arm the format so the next characters typed
            // carry it (and the toolbar shows it as active).
            when (flag) {
                RichFlag.BOLD -> pendingBold = !pendingBold
                RichFlag.ITALIC -> pendingItalic = !pendingItalic
                RichFlag.HIGHLIGHT -> pendingHighlight = !pendingHighlight
            }
            return
        }
        val s = minOf(sel.start, sel.end)
        val e = maxOf(sel.start, sel.end)
        val current = extractRichSpans(tfv.annotatedString)
        val add = !spansFullyCovered(current, s, e, flag)
        val updated = toggleSpanFlag(current, s, e, flag, add)
        emit(TextFieldValue(buildRichAnnotated(tfv.text, updated, highlightColor), selection = sel))
        // Apply the format you just used to the selection to the NEXT text
        // typed, so "make this bold, then keep typing" works.
        when (flag) {
            RichFlag.BOLD -> pendingBold = add
            RichFlag.ITALIC -> pendingItalic = add
            RichFlag.HIGHLIGHT -> pendingHighlight = add
        }
    }

    fun hasFlagAt(flag: RichFlag): Boolean {
        val sel = tfv.selection
        val s = minOf(sel.start, sel.end)
        val e = maxOf(sel.start, sel.end)
        val current = extractRichSpans(tfv.annotatedString)
        if (sel.collapsed) {
            // Caret: an armed (sticky) format wins so the toolbar shows what
            // the next typed characters will look like; otherwise fall back
            // to the char under the caret.
            val pos = s
            val underCaret = current.any { sp -> sp.start <= pos && pos < sp.end && sp.has(flag) }
            return when (flag) {
                RichFlag.BOLD -> pendingBold || underCaret
                RichFlag.ITALIC -> pendingItalic || underCaret
                RichFlag.HIGHLIGHT -> pendingHighlight || underCaret
            }
        }
        return spansFullyCovered(current, s, e, flag)
    }

    Column(modifier = modifier) {
        // ── Toolbar ─────────────────────────────────────────────────────
        if (toolbarMode == RichTextToolbarMode.MAIN) {
            FormatToolbar(
                boldActive = hasFlagAt(RichFlag.BOLD),
                italicActive = hasFlagAt(RichFlag.ITALIC),
                highlightActive = hasFlagAt(RichFlag.HIGHLIGHT),
                accent = accent,
                enabled = enabled,
                onBold = { applyFlag(RichFlag.BOLD) },
                onItalic = { applyFlag(RichFlag.ITALIC) },
                onHighlight = { applyFlag(RichFlag.HIGHLIGHT) }
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    onClick = { toolbarExpanded = !toolbarExpanded },
                    shape = RoundedCornerShape(8.dp),
                    color = if (toolbarExpanded) accent.copy(alpha = 0.15f)
                            else Color.Transparent,
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    CurioIcon(
                        name = CurioIcons.FormatText,
                        contentDescription = if (toolbarExpanded) "Hide formatting" else "Show formatting",
                        tint = accent,
                        size = 18.dp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            AnimatedVisibility(
                visible = toolbarExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                FormatToolbar(
                    boldActive = hasFlagAt(RichFlag.BOLD),
                    italicActive = hasFlagAt(RichFlag.ITALIC),
                    highlightActive = hasFlagAt(RichFlag.HIGHLIGHT),
                    accent = accent,
                    enabled = enabled,
                    onBold = { applyFlag(RichFlag.BOLD) },
                    onItalic = { applyFlag(RichFlag.ITALIC) },
                    onHighlight = { applyFlag(RichFlag.HIGHLIGHT) }
                )
            }
        }

        // ── The field ───────────────────────────────────────────────────
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = surface,
                border = if (showFieldBorder) BorderStroke(1.dp, accent.copy(alpha = 0.25f)) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                BasicTextField(
                    value = tfv,
                    onValueChange = { emit(it) },
                    enabled = enabled,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = ink),
                    cursorBrush = SolidColor(accent),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Default
                    ),
                    onTextLayout = { layoutResult = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = minHeight)
                        .padding(fieldPadding)
                )
            }

            // ── Floating format bar — appears above the text selection so
            // formatting EXISTING text is discoverable: select words, then
            // tap B / I / highlight right there (the main toolbar still
            // works too — this is an extra, selection-local entry point).
            val selection = tfv.selection
            val layout = layoutResult
            if (enabled && !selection.collapsed && layout != null) {
                val density = LocalDensity.current
                val caretRect = runCatching { layout.getCursorRect(selection.max) }.getOrNull()
                if (caretRect != null) {
                    val padLeft = with(density) { fieldPadding.calculateLeftPadding(LayoutDirection.Ltr).toPx() }
                    val padTop = with(density) { fieldPadding.calculateTopPadding().toPx() }
                    val barHeight = with(density) { 40.dp.toPx() }
                    val barWidth = with(density) { 132.dp.toPx() }
                    val gap = with(density) { 8.dp.toPx() }
                    // Float above the selection; drop below it when the
                    // selection is at the very top of the field.
                    val aboveY = padTop + caretRect.top - barHeight - gap
                    val y = if (aboveY >= 0f) aboveY else padTop + caretRect.bottom + gap
                    // Center the bar on the selection end, clamped so it
                    // never runs off the field's left/right edge.
                    val maxX = (with(density) { maxWidth.toPx() } - barWidth).coerceAtLeast(0f)
                    val x = (padLeft + caretRect.left - barWidth / 2f).coerceIn(0f, maxX)
                    Popup(
                        alignment = Alignment.TopStart,
                        offset = IntOffset(x.roundToInt(), y.roundToInt()),
                        properties = PopupProperties(focusable = false)
                    ) {
                        SelectionFormatBar(
                            boldActive = hasFlagAt(RichFlag.BOLD),
                            italicActive = hasFlagAt(RichFlag.ITALIC),
                            highlightActive = hasFlagAt(RichFlag.HIGHLIGHT),
                            accent = accent,
                            enabled = enabled,
                            onBold = { applyFlag(RichFlag.BOLD) },
                            onItalic = { applyFlag(RichFlag.ITALIC) },
                            onHighlight = { applyFlag(RichFlag.HIGHLIGHT) }
                        )
                    }
                }
            }
        }
        if (tfv.text.isEmpty() && placeholder.isNotEmpty()) {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge.copy(color = ink.copy(alpha = 0.45f)),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * Floating mini-toolbar shown at the text selection — B / I / highlight
 * that apply to the selected characters. Mirrors [FormatToolbar]'s buttons
 * in a compact floating chip so formatting existing text is discoverable.
 */
@Composable
private fun SelectionFormatBar(
    boldActive: Boolean,
    italicActive: Boolean,
    highlightActive: Boolean,
    accent: Color,
    enabled: Boolean,
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onHighlight: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        modifier = Modifier.padding(bottom = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 3.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FormatToolButton(
                icon = CurioIcons.FormatBold,
                label = "Bold",
                active = boldActive,
                accent = accent,
                enabled = enabled,
                onClick = onBold
            )
            FormatToolButton(
                icon = CurioIcons.FormatItalic,
                label = "Italic",
                active = italicActive,
                accent = accent,
                enabled = enabled,
                onClick = onItalic
            )
            FormatToolButton(
                icon = CurioIcons.FormatHighlight,
                label = "Highlight",
                active = highlightActive,
                accent = accent,
                enabled = enabled,
                onClick = onHighlight
            )
        }
    }
}

/** Compact B / I / highlighter toolbar row. */
@Composable
private fun FormatToolbar(
    boldActive: Boolean,
    italicActive: Boolean,
    highlightActive: Boolean,
    accent: Color,
    enabled: Boolean,
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onHighlight: () -> Unit
) {
    Row(
        modifier = Modifier.padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FormatToolButton(
            icon = CurioIcons.FormatBold,
            label = "Bold",
            active = boldActive,
            accent = accent,
            enabled = enabled,
            onClick = onBold
        )
        FormatToolButton(
            icon = CurioIcons.FormatItalic,
            label = "Italic",
            active = italicActive,
            accent = accent,
            enabled = enabled,
            onClick = onItalic
        )
        FormatToolButton(
            icon = CurioIcons.FormatHighlight,
            label = "Highlight",
            active = highlightActive,
            accent = accent,
            enabled = enabled,
            onClick = onHighlight
        )
    }
}

@Composable
private fun FormatToolButton(
    icon: String,
    label: String,
    active: Boolean,
    accent: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = if (active) accent.copy(alpha = 0.18f) else Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (active) accent.copy(alpha = 0.6f) else accent.copy(alpha = 0.25f)
        )
    ) {
        CurioIcon(
            name = icon,
            contentDescription = label,
            tint = if (active) accent else MaterialTheme.colorScheme.onSurfaceVariant,
            size = 16.dp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}
