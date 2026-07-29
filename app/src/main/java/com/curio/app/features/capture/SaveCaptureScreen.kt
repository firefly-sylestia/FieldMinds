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
import com.curio.app.ui.components.EmberBurst
import com.curio.app.ui.components.MorphEntrance
import com.curio.app.ui.components.ScreenEntrance
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioMotion
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import kotlinx.coroutines.delay

/**
 * Save / Capture — see CURIO_SPEC.md §8.
 *
 * Upgraded with:
 *  - Confetti burst + Ember burst on save success (dual effect)
 *  - MorphEntrance for the format body content
 *  - Save button shimmer effect while saving
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

    val topic by produceState<CurioTopic?>(initialValue = null, topicName, cat.id) {
        val cached = TopicCatalog.findByName(topicName)
        if (cached != null) {
            value = cached
            return@produceState
        }
        val pool = TopicJsonLoader.load(cat.id)
        value = pool.firstOrNull { it.name == topicName } ?: pool.firstOrNull()
    }

    var canSave by remember { mutableStateOf(false) }
    var saveInProgress by remember { mutableStateOf(false) }
    var confettiTrigger by remember { mutableIntStateOf(0) }
    var emberTrigger by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top bar ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = { navController.popBackStack() },
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
                CurioIcon(name = cat.iconGlyph, contentDescription = null, tint = cat.accent, size = 20.dp)
                Text(
                    text = "${topic?.name ?: "Loading…"} · ${cat.displayName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = cat.accent
                )
            }
        }

        // ── Format body (morph entrance) ────────────────────────────────────
        MorphEntrance {
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
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                        Text(text = "Saving…", style = MaterialTheme.typography.titleMedium)
                    } else {
                        Text(text = "Save entry", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    // ── Save flow: 400ms shimmer → confetti + embers → 700ms pause → navigate
    LaunchedEffect(saveInProgress) {
        if (saveInProgress) {
            delay(400)
            confettiTrigger++
            emberTrigger++
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

    // Dual effect: confetti + embers
    if (confettiTrigger > 0) {
        ConfettiBurst(
            colors = listOf(cat.accent, cat.tint),
            trigger = confettiTrigger,
            particleCount = CurioMotion.ConfettiParticleCountLarge,
            modifier = Modifier.fillMaxSize(),
            onComplete = {}
        )
    }
    if (emberTrigger > 0) {
        EmberBurst(
            colors = listOf(cat.accent, CurioColors.ButterYellow),
            trigger = emberTrigger,
            particleCount = 10,
            modifier = Modifier.fillMaxSize(),
            onComplete = {}
        )
    }
}

/**
 * Dispatch the right format body based on the active category's
 * [CurioCategory.defaultFormat].
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
