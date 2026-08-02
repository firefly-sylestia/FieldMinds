package com.curio.app.features.capture.formats

import com.curio.app.data.CaptureData
import com.curio.app.data.TextSpan
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.curio.app.ui.components.RichTextEditor
import com.curio.app.ui.components.RichTextToolbarMode
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.paperBorder
import com.curio.app.ui.theme.paperInk
import com.curio.app.ui.theme.paperSurface

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
    val quotes = remember(initialData) {
        mutableStateListOf<String>().apply { addAll(initialData?.quotes.orEmpty()) }
    }
    val quoteSpans = remember(initialData) {
        mutableStateListOf<List<TextSpan>>().apply {
            addAll(initialData?.quoteSpans.orEmpty())
            // Pad any missing per-quote span lists (legacy entries) so the
            // parallel list always matches quotes 1:1.
            while (size < quotes.size) add(emptyList())
        }
    }

    val canSave = journalText.isNotBlank() ||
                  quotes.any { it.isNotBlank() }
    // Key on the content too: journal text typed or quotes added AFTER the
    // first character must re-emit, or saving would persist stale data
    // (later text/quotes silently dropped from the saved entry).
    LaunchedEffect(canSave, journalText, journalSpans, quotes.toList(), quoteSpans.toList()) {
        onCanSaveChange(canSave)
        onDataChanged(
            if (canSave) CaptureData.Marginalia(
                journalText = journalText,
                quotes = quotes.toList(),
                journalSpans = journalSpans,
                quoteSpans = quoteSpans.toList()
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
                paperContentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
            )
        }

        // ── Quote cards ───────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Favorite quotes",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (quotes.isNotEmpty()) {
                    Text(
                        text = "${quotes.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            quotes.forEachIndexed { i, quote ->
                // Random hand-placed tilt (−2.5°..2.5°), stable per card
                // (keyed by index) so recomposition and typing never
                // re-roll the rotation mid-edit.
                val rotation = remember(i) { kotlin.random.Random.nextFloat() * 5f - 2.5f }
                QuoteCard(
                    index = i,
                    text = quote,
                    spans = quoteSpans.getOrElse(i) { emptyList() },
                    rotation = rotation,
                    onTextChange = { newText, newSpans ->
                        quotes[i] = newText
                        quoteSpans[i] = newSpans
                    },
                    onRemove = {
                        quotes.removeAt(i)
                        if (i < quoteSpans.size) quoteSpans.removeAt(i)
                    }
                )
            }

            // Add-quote button (dashed-outline placeholder style)
            Surface(
                onClick = {
                    quotes.add("")
                    quoteSpans.add(emptyList())
                },
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 18.dp
                    )
                    Text(
                        text = "Add quote",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun QuoteCard(
    index: Int,
    text: String,
    spans: List<TextSpan>,
    rotation: Float,
    onTextChange: (text: String, spans: List<TextSpan>) -> Unit,
    onRemove: () -> Unit
) {
    // The header row + toolbar render ABOVE the paper slip (the card holds
    // only the field), so the ruled lines line up under the quote text.
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
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                ),
                color = paperInk().copy(alpha = 0.7f),
                modifier = Modifier.weight(1f)
            )
            Surface(
                onClick = onRemove,
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
            text = text,
            spans = spans,
            onRichTextChange = onTextChange,
            placeholder = "\u201C...\u201D",
            toolbarMode = RichTextToolbarMode.MAIN,
            minHeight = 64.dp,
            ink = paperInk(),
            accent = MaterialTheme.colorScheme.tertiary,
            paper = true,
            paperContentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}
