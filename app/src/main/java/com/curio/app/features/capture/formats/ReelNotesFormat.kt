package com.curio.app.features.capture.formats

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.curio.app.data.CaptureData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

/**
 * Reel Notes format body — CURIO_SPEC §8.2 (Movies / Directors).
 *
 * Layout (top to bottom):
 *   - Optional 1-5 star rating row (tap to set, tap same star to clear)
 *   - Multi-line review text field (sentences capitalization, returns
 *     wrap; the soft-keyboard rich-text toolbar handles bold/italic/bullets)
 *   - Optional up-to-3 image attachments (poster / stills) shown as a
 *     row of [ImageThumb] placeholders + an [AddImageButton] until 3 reached
 *
 * [onCanSaveChange] fires true when the review text field has any content.
 * (Star rating and images stay optional, per spec §8.2.)
 */
@Composable
fun ReelNotesFormat(
    accent: Color,
    tint: Color,
    onCanSaveChange: (Boolean) -> Unit,
    onDataChanged: (CaptureData?) -> Unit = {},
    initialData: CaptureData.ReelNotes? = null
) {
    val context = LocalContext.current
    // Edit mode: restore rating / review / images so re-saving an entry
    // preserves the original capture instead of silently wiping it.
    var rating by remember(initialData) { mutableStateOf(initialData?.rating ?: 0) }
    var reviewText by remember(initialData) { mutableStateOf(initialData?.reviewText ?: "") }
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
        imageUris = (imageUris + uris.map { it.toString() }).take(3)
    }

    val canSave = reviewText.isNotBlank()
    // Key on every input, not just canSave: rating, review text and images
    // added AFTER the first character must re-emit, or saving would persist
    // stale data (text/rating/images silently dropped from the saved entry).
    LaunchedEffect(canSave, rating, reviewText, imageUris) {
        onCanSaveChange(canSave)
        onDataChanged(
            if (canSave) CaptureData.ReelNotes(rating, reviewText, imageUris.size, imageUris)
            else null
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Star rating row ────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Your rating (optional)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            StarRating(
                rating = rating,
                onRatingChange = { newRating ->
                    rating = if (newRating == rating) 0 else newRating
                },
                accent = accent
            )
        }

        // ── Review text field ──────────────────────────────────────────────
        OutlinedTextField(
            value = reviewText,
            onValueChange = { reviewText = it },
            label = { Text("Write your review") },
            placeholder = { Text("What did you think of the film?") },
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Default
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 140.dp)
        )

        // ── Image attach row ───────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Attach images (optional, up to 3)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                imageUris.forEachIndexed { i, uri ->
                    ImageThumb(
                        index = i + 1,
                        accent = accent,
                        tint = tint,
                        imageUri = uri,
                        onClick = { /* TODO Phase 4: open lightbox */ },
                        onRemove = { imageUris = imageUris.filterIndexed { idx, _ -> idx != i } }
                    )
                }
                if (imageUris.size < 3) {
                    AddImageButton(
                        accent = accent,
                        tint = tint,
                        onClick = { imagePicker.launch(arrayOf("image/*")) }
                    )
                }
            }
        }
    }
}