package com.curio.app.features.capture.formats

import com.curio.app.data.CaptureData
import com.curio.app.data.NotePaperColor
import com.curio.app.data.NotePaperStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.curio.app.ui.components.RichTextEditor
import com.curio.app.ui.components.RichTextToolbarMode
import com.curio.app.ui.theme.paperInk

/**
 * Marginalia format body — CURIO_SPEC §8.3 (Books / Authors).
 *
 * The quotes entry wears a NOTE-PAPER look instead of the category tint:
 * journal + quote cards sit on warm cream paper (warm off-black toned paper
 * in dark mode) with faint ruled lines, and each field carries a compact
 * rich-text toolbar (B / I / highlighter) over the current selection.
 *
 * Layout:
 *   - "My thoughts" — paper journal page with an always-visible toolbar
 *   - "Favorite quotes" — compact paper quote cards, each with its own
 *     toolbar, slightly rotated (±1.5°) for a hand-placed feel. Tap
 *     "+ Add quote" below to create a new blank card; "Remove" deletes it.
 *
 * [onCanSaveChange] fires true when journal OR at least one quote card
 * has content.
 */
@Composable
fun MarginaliaFormat(
    accent: Color,
    tint: Color,
    onCanSaveChange: (Boolean) -> Unit,
    onDataChanged: (CaptureData?) -> Unit = {},
    initialData: CaptureData.Marginalia? = null
) {
    // Edit mode: restore journal text + spans + quote cards so re-saving
    // preserves the original capture instead of silently wiping it.
    // Legacy entries lack the span fields (Gson → null), guard with orEmpty().
    var journalText by remember(initialData) { mutableStateOf(initialData?.journalText ?: "") }
    var journalSpans by remember(initialData) { mutableStateOf(initialData?.journalSpans.orEmpty()) }
    // Note-paper style per text box — the journal page and EACH quote card
    // wear their own choice. Legacy entries lack the per-field fields (Gson
    // → null), fall back to the take-level paperStyle → RULED.
    var journalStyle by remember(initialData) {
        mutableStateOf(initialData?.journalStyle ?: initialData?.paperStyle ?: NotePaperStyle.RULED)
    }
    // Note-paper color per text box — the journal page and EACH quote card
    // wear their own color. Legacy entries lack the per-field fields (Gson
    // → null/empty), fall back to CREAM.
    var journalColor by remember(initialData) {
        mutableStateOf(initialData?.journalColor ?: NotePaperColor.CREAM)
    }
    // Quote cards — the SHARED hand-placed paper notecard section (same
    // component Reel Notes / Sound Bite / Mood Board use). Owns the parallel
    // lists (text / spans / tilt / style / color) and never re-rolls a
    // card's tilt after it's created.
    val quoteCards = rememberQuoteCardsState(
        initialQuotes = initialData?.quotes.orEmpty(),
        initialSpans = initialData?.quoteSpans.orEmpty(),
        initialTilts = initialData?.quoteTilts.orEmpty(),
        initialStyles = initialData?.quoteStyles.orEmpty(),
        initialColors = initialData?.quoteColors.orEmpty(),
        defaultStyle = initialData?.paperStyle ?: NotePaperStyle.RULED,
        defaultColor = NotePaperColor.CREAM
    )

    val canSave = journalText.isNotBlank() || quoteCards.hasContent
    // Key on the content too: journal text typed or quotes added AFTER the
    // first character must re-emit, or saving would persist stale data
    // (later text/quotes silently dropped from the saved entry).
    LaunchedEffect(
        canSave, journalText, journalSpans, quoteCards.quotes.toList(),
        quoteCards.spans.toList(), quoteCards.tilts.toList(), journalStyle,
        quoteCards.styles.toList(), journalColor, quoteCards.colors.toList()
    ) {
        onCanSaveChange(canSave)
        onDataChanged(
            if (canSave) CaptureData.Marginalia(
                journalText = journalText,
                quotes = quoteCards.quotes.toList(),
                journalSpans = journalSpans,
                quoteSpans = quoteCards.spans.toList(),
                quoteTilts = quoteCards.tilts.toList(),
                journalStyle = journalStyle,
                quoteStyles = quoteCards.styles.toList(),
                journalColor = journalColor,
                quoteColors = quoteCards.colors.toList(),
                // Legacy fallback — mirror the journal's style.
                paperStyle = journalStyle
            )
            else null
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Journal page ──────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "My thoughts",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // The toolbar renders OUTSIDE the paper slip (paper mode) so the
            // ruled lines line up under the text while typing.
            RichTextEditor(
                modifier = Modifier.fillMaxWidth(),
                text = journalText,
                spans = journalSpans,
                onRichTextChange = { newText, newSpans ->
                    journalText = newText
                    journalSpans = newSpans
                },
                placeholder = "What did this book make you think about?",
                toolbarMode = RichTextToolbarMode.MAIN,
                minHeight = 140.dp,
                ink = paperInk(),
                accent = MaterialTheme.colorScheme.tertiary,
                paper = true,
                paperStyle = journalStyle,
                onPaperStyleChange = { journalStyle = it },
                paperColor = journalColor,
                onPaperColorChange = { journalColor = it },
                paperContentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
            )
        }

        // ── Quote cards — the SHARED hand-placed paper notecard section ──
        // New cards inherit the journal's current paper style + color.
        QuoteCardsSection(
            state = quoteCards,
            newCardStyle = { journalStyle },
            newCardColor = { journalColor }
        )
    }
}
