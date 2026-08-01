package com.curio.app.features.capture.formats

import com.curio.app.data.CaptureData
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons

/**
 * Marginalia format body — CURIO_SPEC §8.3 (Books / Authors).
 *
 * Layout:
 *   - "My thoughts" — journal text field (full multi-line, sentences
 *     capitalization, rich text via soft-keyboard toolbar)
 *   - "Favorite quotes" — quote "cards" each its own mini text field,
 *     styled like a torn notecard with subtle rotation (±2° alternating)
 *     for a hand-placed feel. Tap "+ Add quote" below to create a new
 *     blank card. Tap "Remove" on a card to delete it (slides the
 *     remaining cards reflow smoothly via the AnimatedContent-style
 *     recomposition).
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
    // Edit mode: restore journal text + quote cards so re-saving preserves
    // the original capture instead of silently wiping it.
    var journalText by remember(initialData) { mutableStateOf(initialData?.journalText ?: "") }
    val quotes = remember(initialData) {
        mutableStateListOf<String>().apply { addAll(initialData?.quotes.orEmpty()) }
    }

    val canSave = journalText.isNotBlank() ||
                  quotes.any { it.isNotBlank() }
    // Key on the content too: journal text typed or quotes added AFTER the
    // first character must re-emit, or saving would persist stale data
    // (later text/quotes silently dropped from the saved entry).
    LaunchedEffect(canSave, journalText, quotes.toList()) {
        onCanSaveChange(canSave)
        onDataChanged(
            if (canSave) CaptureData.Marginalia(journalText, quotes.toList())
            else null
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Journal field ──────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "My thoughts",
                style = MaterialTheme.typography.titleSmall,
                color = accent
            )
            OutlinedTextField(
                value = journalText,
                onValueChange = { journalText = it },
                placeholder = {
                    Text("What did this book make you think about?")
                },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
            )
        }

        // ── Quote cards ────────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Favorite quotes",
                    style = MaterialTheme.typography.titleSmall,
                    color = accent
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
                val rotation = if (i % 2 == 0) 2f else -2f
                QuoteCard(
                    index = i,
                    text = quote,
                    accent = accent,
                    tint = tint,
                    rotation = rotation,
                    onTextChange = { quotes[i] = it },
                    onRemove = { quotes.removeAt(i) }
                )
            }

            // Add-quote button (dashed-outline placeholder style)
            Surface(
                onClick = { quotes.add("") },
                shape = RoundedCornerShape(16.dp),
                color = tint,
                border = BorderStroke(
                    width = 1.dp,
                    color = accent.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CurioIcon(
                        name = CurioIcons.Add,
                        contentDescription = null,
                        tint = accent,
                        size = 18.dp
                    )
                    Text(
                        text = "Add quote",
                        style = MaterialTheme.typography.labelLarge,
                        color = accent
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
    accent: Color,
    tint: Color,
    rotation: Float,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = tint,
        // Slim accent rule + a soft lift so each card reads as a placed
        // notecard, not a flat field floating on the page.
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .rotate(rotation)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // ── Card header — quote mark + number, Remove on the right ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CurioIcon(
                    name = CurioIcons.FormatQuote,
                    contentDescription = null,
                    tint = accent,
                    size = 18.dp
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Quote ${index + 1}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    color = accent,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onRemove,
                    contentPadding = PaddingValues(
                        horizontal = 8.dp,
                        vertical = 2.dp
                    )
                ) {
                    Text(
                        text = "Remove",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent
                    )
                }
            }
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        text = "\u201C...\u201D",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}