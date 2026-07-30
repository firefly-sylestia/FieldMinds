package com.curio.app.features.capture.formats

import com.curio.app.data.CaptureData
import com.curio.app.data.CaptureFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons

/**
 * Open Notebook format body — CURIO_SPEC §8.6 (Wildcard).
 *
 * Layout (top to bottom):
 *   - Radio-style format picker with 5 options (Voice note / Written
 *     review / Journal + quotes / Moodboard / Field notes) — each
 *     showing its glyph + label + 1-line description
 *   - Selected format body below — re-uses the formats from §8.1-§8.5
 *     wholesale (no new designs, per spec §8.6)
 *
 * Cross-fade transition (~200ms) when the user picks a different
 * format. [onCanSaveChange] is delegated to the selected sub-format so
 * SaveCaptureScreen's CTA behaves identically regardless of choice.
 *
 * The entry is still tagged "Wildcard" in the Cabinet regardless of
 * which sub-format was used.
 */
@Composable
fun OpenNotebookFormat(
    accent: Color,
    tint: Color,
    onCanSaveChange: (Boolean) -> Unit,
    onDataChanged: (CaptureData?) -> Unit = {}
) {
    var selectedFormat by remember { mutableStateOf(NotebookChoice.VOICE) }
    var subCanSave by remember { mutableStateOf(false) }
    var subData by remember { mutableStateOf<CaptureData?>(null) }

    LaunchedEffect(subCanSave, subData, selectedFormat) {
        onCanSaveChange(subCanSave)
        onDataChanged(
            if (subCanSave && subData != null)
                CaptureData.OpenNotebook(notebookToCaptureFormat(selectedFormat), subData!!)
            else null
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Format picker ───────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "How do you want to capture this one?",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            NotebookChoice.values().forEach { choice ->
                NotebookChoiceRow(
                    choice = choice,
                    selected = selectedFormat == choice,
                    accent = accent,
                    tint = tint,
                    onClick = { selectedFormat = choice }
                )
            }
        }

        // ── Selected format body (cross-fade swap) ──────────────────────────
        AnimatedContent(
            targetState = selectedFormat,
            transitionSpec = {
                (fadeIn() togetherWith fadeOut())
            },
            label = "notebookSwap"
        ) { choice ->
            when (choice) {
                NotebookChoice.VOICE -> SoundBiteFormat(
                    accent, tint, { subCanSave = it }, { subData = it }
                )
                NotebookChoice.REVIEW -> ReelNotesFormat(
                    accent, tint, { subCanSave = it }, { subData = it }
                )
                NotebookChoice.JOURNAL -> MarginaliaFormat(
                    accent, tint, { subCanSave = it }, { subData = it }
                )
                NotebookChoice.MOODBOARD -> GalleryWallFormat(
                    accent, tint, { subCanSave = it }, { subData = it }
                )
                NotebookChoice.FIELD -> FieldNotesFormat(
                    accent, tint, { subCanSave = it }, { subData = it }
                )
            }
        }
    }
}

/** Maps NotebookChoice to its equivalent CaptureFormat for storage. */
private fun notebookToCaptureFormat(choice: NotebookChoice): CaptureFormat = when (choice) {
    NotebookChoice.VOICE -> CaptureFormat.SoundBite
    NotebookChoice.REVIEW -> CaptureFormat.ReelNotes
    NotebookChoice.JOURNAL -> CaptureFormat.Marginalia
    NotebookChoice.MOODBOARD -> CaptureFormat.GalleryWall
    NotebookChoice.FIELD -> CaptureFormat.FieldNotes
}

/**
 * The 5 format choices offered by the Open Notebook picker.
 *
 * [label] / [glyph] / [description] are render-only metadata; the actual
 * format body is selected by enum match in the [AnimatedContent] below.
 */
internal enum class NotebookChoice(
    val label: String,
    val glyph: String,
    val description: String
) {
    VOICE(
        label = "Voice note",
        glyph = CurioIcons.Mic,
        description = "Record a voice memo about this topic"
    ),
    REVIEW(
        label = "Written review",
        glyph = CurioIcons.Edit,
        description = "Rate + write a short review"
    ),
    JOURNAL(
        label = "Journal + quotes",
        glyph = CurioIcons.MenuBook,
        description = "Thoughts + favorite quotes"
    ),
    MOODBOARD(
        label = "Moodboard",
        glyph = CurioIcons.Image,
        description = "A collage of images + caption"
    ),
    FIELD(
        label = "Field notes",
        glyph = CurioIcons.AutoAwesome,
        description = "What you observed / learned"
    )
}

@Composable
private fun NotebookChoiceRow(
    choice: NotebookChoice,
    selected: Boolean,
    accent: Color,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) tint else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) accent.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Radio indicator (filled circle inside ring when selected)
            Surface(
                shape = CircleShape,
                color = if (selected) accent else Color.Transparent,
                border = BorderStroke(
                    width = 2.dp,
                    color = if (selected) accent
                            else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.size(20.dp)
            ) {
                if (selected) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            shape = CircleShape,
                            color = CurioColors.CreamWhite,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }
            }
            // Glyph
            CurioIcon(
                name = choice.glyph,
                contentDescription = null,
                tint = if (selected) accent
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                size = 20.dp
            )
            // Label + description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = choice.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) accent
                           else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = choice.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}