package com.curio.app.features.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.CaptureFormat
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.data.CurioTopic
import com.curio.app.data.TopicCatalog
import com.curio.app.data.TopicJsonLoader
import com.curio.app.features.capture.formats.FieldNotesFormat
import com.curio.app.features.capture.formats.GalleryWallFormat
import com.curio.app.features.capture.formats.MarginaliaFormat
import com.curio.app.features.capture.formats.OpenNotebookFormat
import com.curio.app.features.capture.formats.ReelNotesFormat
import com.curio.app.features.capture.formats.SoundBiteFormat
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.ConfettiBurst
import com.curio.app.ui.components.ScreenEntrance
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import kotlinx.coroutines.delay

/**
 * Save / Capture — see CURIO_SPEC.md §8.
 *
 * Shared outer shell (consistent across all formats) + format-specific
 * body dispatched from the active category. Each format body lives in
 * its own file under `formats/` and owns its own state; the screen
 * just hosts the shell, the format body, and the bottom Save CTA.
 *
 * Format dispatch uses [CurioCategory.defaultFormat] so the 11
 * categories map cleanly onto the 6 reusable format bodies — no
 * per-category `when` block needed. See the doc comment on
 * [CurioCategories] for the mapping table.
 *
 * Flow:
 *   1. Resolve category from routeSlug
 *   2. Async-load the topic (Loader-backed) via [produceState]
 *   3. Render shell: top bar + topic reminder strip + format body +
 *      sticky Save CTA
 *   4. Each format body calls [FormatBodyForCategory]'s onCanSaveChange
 *      callback when its internal canSave state changes
 *   5. Save button click triggers simulated 400ms save → confetti →
 *      700ms pause → navigate to EntryDetail
 */
@Composable
fun SaveCaptureScreen(
    categorySlug: String,
    topicName: String,
    navController: NavController
) {
    val cat = remember(categorySlug) {
        CurioCategories.byRouteSlug(categorySlug)
            ?: CurioCategories.byId(CategoryId.WILDCARD)
    }

    // ── Async topic lookup (loader-backed) ─────────────────────────────────
    //
    // Produces a non-null topic whenever the loader resolves the name. While
    // loading, falls back to the first topic in the category pool so the
    // shell has something to display.
    val topic by produceState<CurioTopic?>(initialValue = null, topicName, cat.id) {
        val cached = TopicCatalog.findByName(topicName)
        if (cached != null) {
            value = cached
            return@produceState
        }
        val pool = TopicJsonLoader.load(cat.id)
        value = pool.firstOrNull { it.name == topicName } ?: pool.firstOrNull()
    }

    // ── Format-specific save-ability (driven by format body callback) ─────
    var canSave by remember { mutableStateOf(false) }

    // ── Save-in-progress + confetti flow ──────────────────────────────────
    var saveInProgress by remember { mutableStateOf(false) }
    var confettiTrigger by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top bar: ← "Save your take" ─────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = {
                    // TODO Phase 4: confirm-discard dialog if format has content
                    navController.popBackStack()
                },
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                CurioIcon(
                    name = CurioIcons.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    size = 24.dp,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Text(
                text = "Save your take",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // ── Topic reminder strip ────────────────────────────────────────────
        Surface(
            color = cat.tint,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurioIcon(
                    name = cat.iconGlyph,
                    contentDescription = null,
                    tint = cat.accent,
                    size = 20.dp
                )
                Text(
                    text = "${topic?.name ?: "Loading…"} · ${cat.displayName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = cat.accent
                )
            }
        }

        // ── Format body (dispatched from category.defaultFormat) ────────────
        ScreenEntrance {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                FormatBodyForCategory(
                    category = cat,
                    onCanSaveChange = { canSave = it }
                )
            }
        }

        // ── Sticky bottom Save CTA ──────────────────────────────────────────
        Surface(
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { saveInProgress = true },
                    enabled = canSave && !saveInProgress,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cat.accent,
                        contentColor = CurioColors.DeepPlum,
                        disabledContainerColor =
                            MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (saveInProgress) {
                        CircularProgressIndicator(
                            color = CurioColors.DeepPlum,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Saving…",
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
                        Text(
                            text = "Save entry",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }

    // Simulated save: 400ms shimmer → confetti → 700ms pause → navigate
    LaunchedEffect(saveInProgress) {
        if (saveInProgress) {
            delay(400)
            confettiTrigger++
            saveInProgress = false
        }
    }
    LaunchedEffect(confettiTrigger) {
        if (confettiTrigger > 0) {
            delay(700)
            navController.navigate(
                CurioRoutes.entryDetail("entry-new-${System.currentTimeMillis()}")
            )
        }
    }

    if (confettiTrigger > 0) {
        ConfettiBurst(
            color = cat.accent,
            trigger = confettiTrigger,
            modifier = Modifier.fillMaxSize(),
            onComplete = {}
        )
    }
}

/**
 * Dispatch the right format body based on the active category's
 * [CurioCategory.defaultFormat]. With 11 categories mapping onto 6
 * format bodies, this single `when` covers the whole matrix; adding
 * a new category just means adding it to [CurioCategories] with the
 * appropriate defaultFormat, not editing this function.
 *
 * Each format owns its own state and notifies [onCanSaveChange] when
 * its internal save-ability flips. The shell (SaveCaptureScreen)
 * tracks the latest value and uses it to enable/disable the bottom
 * Save CTA.
 */
@Composable
private fun FormatBodyForCategory(
    category: CurioCategory,
    onCanSaveChange: (Boolean) -> Unit
) {
    when (category.defaultFormat) {
        CaptureFormat.SoundBite    -> SoundBiteFormat(category.accent, category.tint, onCanSaveChange)
        CaptureFormat.ReelNotes    -> ReelNotesFormat(category.accent, category.tint, onCanSaveChange)
        CaptureFormat.Marginalia   -> MarginaliaFormat(category.accent, category.tint, onCanSaveChange)
        CaptureFormat.GalleryWall  -> GalleryWallFormat(category.accent, category.tint, onCanSaveChange)
        CaptureFormat.FieldNotes   -> FieldNotesFormat(category.accent, category.tint, onCanSaveChange)
        CaptureFormat.OpenNotebook -> OpenNotebookFormat(category.accent, category.tint, onCanSaveChange)
    }
}