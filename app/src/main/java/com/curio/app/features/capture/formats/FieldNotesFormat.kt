package com.curio.app.features.capture.formats

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.curio.app.data.CaptureData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

/**
 * Field Notes format body — CURIO_SPEC §8.5 (Science & Nature).
 *
 * Three pre-labeled sections give gentle structure (mirrors a real
 * field journal) without forcing all three to be filled:
 *   - "What I observed"
 *   - "What surprised me"
 *   - "What I want to learn next"
 *
 * Each section is independently collapsible/expandable via
 * [CollapsibleSectionHeader] (CURIO_SPEC §12.2 disclosure pattern). When
 * collapsed with content, a grey preview snippet trails the header so
 * nothing feels hidden-and-forgotten.
 *
 * Optional single photo attach at the bottom (placeholder thumbnail for
 * now; Phase 4 wires the picker).
 *
 * [onCanSaveChange] fires true when at least ONE of the three sections
 * has content.
 */
@Composable
fun FieldNotesFormat(
    accent: Color,
    tint: Color,
    onCanSaveChange: (Boolean) -> Unit,
    onDataChanged: (CaptureData?) -> Unit = {},
    initialData: CaptureData.FieldNotes? = null
) {
    val context = LocalContext.current
    // Edit mode: restore the three sections + photos so re-saving preserves
    // the original capture instead of silently wiping it.
    var observed by remember(initialData) { mutableStateOf(initialData?.observed ?: "") }
    var surprised by remember(initialData) { mutableStateOf(initialData?.surprised ?: "") }
    var learnNext by remember(initialData) { mutableStateOf(initialData?.learnNext ?: "") }
    var imageUris by remember(initialData) { mutableStateOf(initialData?.imageUris.orEmpty()) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
        imageUris = (imageUris + uris.map { it.toString() }).take(6)
    }

    var observedExpanded by remember { mutableStateOf(true) }
    var surprisedExpanded by remember { mutableStateOf(true) }
    var learnNextExpanded by remember { mutableStateOf(true) }

    val canSave = observed.isNotBlank() ||
                  surprised.isNotBlank() ||
                  learnNext.isNotBlank() ||
                  imageUris.isNotEmpty()
    LaunchedEffect(canSave, observed, surprised, learnNext, imageUris) {
        onCanSaveChange(canSave)
        onDataChanged(
            if (canSave) CaptureData.FieldNotes(observed, surprised, learnNext, imageUris)
            else null
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Section 1: What I observed ──────────────────────────────────────
        CollapsibleSectionHeader(
            label = "What I observed",
            accent = accent,
            expanded = observedExpanded,
            onToggle = { observedExpanded = !observedExpanded },
            preview = previewSnippet(observed, !observedExpanded)
        )
        AnimatedVisibility(
            visible = observedExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            OutlinedTextField(
                value = observed,
                onValueChange = { observed = it },
                placeholder = {
                    Text("What did you see, hear, notice?")
                },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .heightIn(min = 100.dp)
            )
        }

        // ── Section 2: What surprised me ───────────────────────────────────
        CollapsibleSectionHeader(
            label = "What surprised me",
            accent = accent,
            expanded = surprisedExpanded,
            onToggle = { surprisedExpanded = !surprisedExpanded },
            preview = previewSnippet(surprised, !surprisedExpanded)
        )
        AnimatedVisibility(
            visible = surprisedExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            OutlinedTextField(
                value = surprised,
                onValueChange = { surprised = it },
                placeholder = {
                    Text("What was unexpected or delightful?")
                },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .heightIn(min = 100.dp)
            )
        }

        // ── Section 3: What I want to learn next ───────────────────────────
        CollapsibleSectionHeader(
            label = "What I want to learn next",
            accent = accent,
            expanded = learnNextExpanded,
            onToggle = { learnNextExpanded = !learnNextExpanded },
            preview = previewSnippet(learnNext, !learnNextExpanded)
        )
        AnimatedVisibility(
            visible = learnNextExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            OutlinedTextField(
                value = learnNext,
                onValueChange = { learnNext = it },
                placeholder = {
                    Text("Where does this lead?")
                },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .heightIn(min = 100.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── Optional photo attach (CURIO_SPEC §8.5) ────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Attach a photo (optional)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                imageUris.forEachIndexed { index, uri ->
                    ImageThumb(
                        index = index + 1,
                        accent = accent,
                        tint = tint,
                        imageUri = uri,
                        onClick = { },
                        onRemove = { imageUris = imageUris.filterIndexed { i, _ -> i != index } }
                    )
                }
                AddImageButton(
                    accent = accent,
                    tint = tint,
                    label = "Add",
                    onClick = { imagePicker.launch(arrayOf("image/*")) }
                )
            }
        }
    }
}

/**
 * Returns a short preview snippet ("first 40 chars + ellipsis") for a
 * collapsible section header when the section is collapsed and has
 * content. Null otherwise — header renders without the preview row.
 */
private fun previewSnippet(text: String, collapsed: Boolean): String? =
    if (collapsed && text.isNotBlank()) text.take(40) + "\u2026" else null