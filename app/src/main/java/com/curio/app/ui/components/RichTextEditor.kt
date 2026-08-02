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
import com.curio.app.data.NotePaperColor
import com.curio.app.data.NotePaperStyle
import com.curio.app.data.TextSpan
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.notePaperInk
import com.curio.app.ui.theme.paperAccent
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
 * Rebases [spans] (in OLD-text coordinates) onto [newText] after an edit,
 * using the same common-prefix / common-suffix diff as [findInsertedRange].
 * A span fully before the changed region keeps its offsets; a span fully
 * after it shifts by the length delta; a span overlapping the changed region
 * is clipped to its untouched head/tail parts (the replaced text inside the
 * diff is dropped — the armed sticky format re-applies to exactly the typed
 * range). The result is what OUR editor state should carry; BasicTextField's
 * own reported AnnotatedString is NOT used because it can silently drop the
 * styles we set programmatically.
 */
private fun rebaseSpans(oldText: String, newText: String, spans: List<TextSpan>): List<TextSpan> {
    if (spans.isEmpty()) return emptyList()
    if (oldText == newText) return spans
    var prefix = 0
    while (prefix < oldText.length && prefix < newText.length &&
        oldText[prefix] == newText[prefix]
    ) prefix++
    var suffix = 0
    while (suffix < oldText.length - prefix && suffix < newText.length - prefix &&
        oldText[oldText.length - 1 - suffix] == newText[newText.length - 1 - suffix]
    ) suffix++
    val oldEnd = oldText.length - suffix
    val newEnd = newText.length - suffix
    val delta = newEnd - oldEnd
    val out = mutableListOf<TextSpan>()
    for (sp in spans) {
        val s = sp.start.coerceIn(0, oldText.length)
        val e = sp.end.coerceIn(s, oldText.length)
        when {
            // Fully before the changed region — same coordinates.
            e <= prefix -> out.add(sp)
            // Fully after the changed region — shift by the length delta.
            s >= oldEnd -> out.add(
                TextSpan(s + delta, e + delta, sp.bold, sp.italic, sp.highlight)
            )
            // Overlaps the changed region — keep only the untouched parts.
            else -> {
                if (s < prefix) out.add(TextSpan(s, prefix, sp.bold, sp.italic, sp.highlight))
                if (e > oldEnd) out.add(
                    TextSpan(maxOf(s, oldEnd) + delta, e + delta, sp.bold, sp.italic, sp.highlight)
                )
            }
        }
    }
    return out.merged()
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
    highlightColor: Color = paperHighlight(),
    /** Renders the field on a note-paper card with the toolbar OUTSIDE the
     *  card, so the ruled lines line up under the field text while typing —
     *  matching the saved detail view's paper pages. [paperStyle] chooses
     *  the slip: [NotePaperStyle.RULED] classic ruled page,
     *  [NotePaperStyle.TORN] torn note, [NotePaperStyle.TORN_RULED] torn
     *  note with ruled lines. When [onPaperStyleChange] is provided, a
     *  compact Ruled/Torn/rules toggle appears in this field's own toolbar.
     *  Default false keeps the plain surface field. */
    paper: Boolean = false,
    paperStyle: NotePaperStyle = NotePaperStyle.RULED,
    onPaperStyleChange: (NotePaperStyle) -> Unit = {},
    /** Note-paper COLOR of the slip when [paper] — chosen per text box via
     *  the swatch picker next to the Ruled/Torn toggle. The ink follows the
     *  sheet so text stays readable on every pastel. */
    paperColor: NotePaperColor = NotePaperColor.CREAM,
    onPaperColorChange: (NotePaperColor) -> Unit = {},
    /** Content inset of the paper card when [paper] is true. */
    paperContentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
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
    // Paper mode: the field floats directly on the card's paper — no inner
    // padding of its own (the card owns the margins). The toolbar + cursor
    // also switch to the warm paper accent: these controls sit on cream in
    // BOTH themes, so a theme-aware accent (e.g. the dark-mode tertiary) can
    // read washed-out against the paper. The ink follows the chosen sheet
    // color so text stays readable on every pastel.
    val effectiveFieldPadding = if (paper) PaddingValues(0.dp) else fieldPadding
    val effectiveAccent = if (paper) paperAccent() else accent
    val effectiveInk = if (paper) notePaperInk(paperColor) else ink

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
        val oldText = tfv.text
        // The text itself changed (a real user edit) — NEVER trust what
        // BasicTextField reports back as its AnnotatedString: it can silently
        // drop the styles we set programmatically, which made bold/italic/
        // highlight vanish moments after applying. Instead rebase OUR OWN
        // spans (from tfv, which we always build ourselves) across the edit,
        // then merge in any caret-inherited styles the field DID report for
        // the new characters (e.g. typing inside an existing bold span keeps
        // inheriting bold without an explicit arm).
        val textChanged = new.text != oldText
        var spans = if (textChanged) {
            rebaseSpans(oldText, new.text, extractRichSpans(tfv.annotatedString))
        } else {
            // Text unchanged — a caret/selection move or an IME re-report
            // (e.g. the extra event that follows committing a space). NEVER
            // trust the field's reported AnnotatedString here: BasicTextField
            // can silently drop the styles we set programmatically, which is
            // exactly how bold/italic/highlight used to vanish right after
            // typing a space. Keep OUR spans — tfv is always built by us.
            extractRichSpans(tfv.annotatedString)
        }
        // Caret inheritance from OUR spans: typing inside an already-styled
        // run (e.g. mid-bold word) — or immediately after one — must keep
        // that style on the new characters. BasicTextField's reported
        // AnnotatedString can silently drop the styles we set programmatically,
        // so we can't rely on it to re-add them — emulate inheritance from
        // our own tracked spans instead (the caret sits at the diff's start ==
        // old/new common prefix, which is unchanged by the edit). Inclusive
        // at the span END so typing right after a styled word continues it.
        val insertedRange = if (textChanged) findInsertedRange(oldText, new.text) else null
        if (insertedRange != null) {
            val caret = insertedRange.first
            val inherited = extractRichSpans(tfv.annotatedString).filter { sp ->
                sp.start <= caret && caret <= sp.end
            }
            for (sp in inherited) {
                if (sp.bold) {
                    spans = toggleSpanFlag(spans, caret, insertedRange.last + 1, RichFlag.BOLD, true)
                }
                if (sp.italic) {
                    spans = toggleSpanFlag(spans, caret, insertedRange.last + 1, RichFlag.ITALIC, true)
                }
                if (sp.highlight) {
                    spans = toggleSpanFlag(spans, caret, insertedRange.last + 1, RichFlag.HIGHLIGHT, true)
                }
            }
        }
        // Sticky format: when a format is armed and the user just typed
        // (or typed over a selection), apply it to exactly the changed
        // characters so typing continues in that style (BasicTextField only
        // inherits the style under the caret, so an armed format needs
        // explicit application). Pure deletions diff to null and are skipped.
        if (pendingBold || pendingItalic || pendingHighlight) {
            insertedRange?.let { range ->
                if (pendingBold) {
                    spans = toggleSpanFlag(spans, range.first, range.last + 1, RichFlag.BOLD, true)
                }
                if (pendingItalic) {
                    spans = toggleSpanFlag(spans, range.first, range.last + 1, RichFlag.ITALIC, true)
                }
                if (pendingHighlight) {
                    spans = toggleSpanFlag(spans, range.first, range.last + 1, RichFlag.HIGHLIGHT, true)
                }
            }
        }
        val result = TextFieldValue(
            buildRichAnnotated(new.text, spans, highlightColor),
            selection = new.selection,
            composition = new.composition
        )
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
        // Apply directly (not via emit): emit derives spans from OUR tracked
        // tfv, which isn't updated yet at this point — we built the styled
        // value ourselves, so set it and report it here. This also avoids
        // trusting any field-reported AnnotatedString for span content.
        val styled = TextFieldValue(
            buildRichAnnotated(tfv.text, updated, highlightColor),
            selection = sel
        )
        tfv = styled
        onRichTextChange(styled.text, extractRichSpans(styled.annotatedString))
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
            // The paper-style toggle rides the same row as the format tools
            // (per text box — never a section-level row above the field).
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FormatToolbar(
                    boldActive = hasFlagAt(RichFlag.BOLD),
                    italicActive = hasFlagAt(RichFlag.ITALIC),
                    highlightActive = hasFlagAt(RichFlag.HIGHLIGHT),
                    accent = effectiveAccent,
                    enabled = enabled,
                    onBold = { applyFlag(RichFlag.BOLD) },
                    onItalic = { applyFlag(RichFlag.ITALIC) },
                    onHighlight = { applyFlag(RichFlag.HIGHLIGHT) }
                )
                if (paper) {
                    NotePaperStyleToggle(
                        style = paperStyle,
                        onStyleChange = onPaperStyleChange,
                        accent = effectiveAccent,
                        enabled = enabled
                    )
                }
            }
            if (paper) {
                // The color swatches sit on their OWN row — the format tools
                // + style chips already fill a phone-width row, and Rows
                // don't wrap, so six more swatches would get clipped.
                NotePaperColorToggle(
                    color = paperColor,
                    onColorChange = onPaperColorChange,
                    accent = effectiveAccent,
                    enabled = enabled,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (paper) Arrangement.SpaceBetween else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (paper) {
                    // Per-field paper toggle stays visible without expanding
                    // the format row (SoundBite note, ReelNotes, Field Notes
                    // sections use TOGGLE mode).
                    NotePaperStyleToggle(
                        style = paperStyle,
                        onStyleChange = onPaperStyleChange,
                        accent = effectiveAccent,
                        enabled = enabled,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Surface(
                    onClick = { toolbarExpanded = !toolbarExpanded },
                    shape = RoundedCornerShape(8.dp),
                    color = if (toolbarExpanded) effectiveAccent.copy(alpha = 0.15f)
                            else Color.Transparent,
                    border = BorderStroke(1.dp, effectiveAccent.copy(alpha = 0.35f)),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    CurioIcon(
                        name = CurioIcons.FormatText,
                        contentDescription = if (toolbarExpanded) "Hide formatting" else "Show formatting",
                        tint = effectiveAccent,
                        size = 18.dp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            if (paper) {
                // Color swatches on their OWN row below — keeps the style +
                // format row from overflowing on narrow screens.
                NotePaperColorToggle(
                    color = paperColor,
                    onColorChange = onPaperColorChange,
                    accent = effectiveAccent,
                    enabled = enabled,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
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
                    accent = effectiveAccent,
                    enabled = enabled,
                    onBold = { applyFlag(RichFlag.BOLD) },
                    onItalic = { applyFlag(RichFlag.ITALIC) },
                    onHighlight = { applyFlag(RichFlag.HIGHLIGHT) }
                )
            }
        }

        // ── The field ───────────────────────────────────────────────────
        // On note-paper ([paper]) the field renders inside a PaperCard with
        // the toolbar OUTSIDE the card, so the ruled lines line up under the
        // text while typing — matching the saved detail view's paper pages.
        val fieldBlock: @Composable () -> Unit = {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (paper) Color.Transparent else surface,
                    border = if (paper || !showFieldBorder) null
                            else BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BasicTextField(
                        value = tfv,
                        onValueChange = { emit(it) },
                        enabled = enabled,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = effectiveInk),
                        cursorBrush = SolidColor(effectiveAccent),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Default
                        ),
                        onTextLayout = { layoutResult = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = minHeight)
                            .padding(effectiveFieldPadding)
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
                        val padLeft = with(density) { effectiveFieldPadding.calculateLeftPadding(LayoutDirection.Ltr).toPx() }
                        val padTop = with(density) { effectiveFieldPadding.calculateTopPadding().toPx() }
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
                                accent = effectiveAccent,
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
                    style = MaterialTheme.typography.bodyLarge.copy(color = effectiveInk.copy(alpha = 0.45f)),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
        if (paper) {
            when (paperStyle) {
                NotePaperStyle.RULED -> PaperCard(
                    modifier = Modifier.fillMaxWidth(),
                    ruled = true,
                    paperColor = paperColor,
                    contentPadding = paperContentPadding
                ) {
                    fieldBlock()
                }
                NotePaperStyle.TORN -> TornPaperCard(
                    modifier = Modifier.fillMaxWidth(),
                    ruled = false,
                    paperColor = paperColor,
                    contentPadding = paperContentPadding
                ) {
                    fieldBlock()
                }
                NotePaperStyle.TORN_RULED -> TornPaperCard(
                    modifier = Modifier.fillMaxWidth(),
                    ruled = true,
                    paperColor = paperColor,
                    contentPadding = paperContentPadding
                ) {
                    fieldBlock()
                }
            }
        } else {
            fieldBlock()
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
            // Inactive buttons fade the accent itself — on cream paper (both
            // themes) the theme's onSurfaceVariant reads washed-out/wrong.
            tint = if (active) accent else accent.copy(alpha = 0.45f),
            size = 16.dp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}
