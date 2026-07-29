package fieldmind.research.app.features.field.presentation.components

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import org.json.JSONObject

/**
 * SharedPreferences-backed draft auto-save for creation forms.
 *
 * Saves form state every [saveIntervalMs] milliseconds when the form is dirty.
 * Drafts are keyed by [draftKey] and automatically cleared on successful save.
 *
 * Usage:
 * ```
 * val draftHelper = rememberDraftAutoSave(key = "new_observation")
 * draftHelper.RestoreDraft(
 *     onRestore = { json ->
 *         val obj = JSONObject(json)
 *         subject = obj.optString("subject", "")
 *         facts = obj.optString("facts", "")
 *     }
 * )
 * // In your save function:
 * draftHelper.ClearDraft()
 * ```
 */
class DraftAutoSave internal constructor(
    private val prefs: SharedPreferences,
    private val draftKey: String,
    private val saveIntervalMs: Long
) {
    private var lastSaveTime = 0L

    /**
     * Saves the current form state as a JSON string.
     * Throttled to [saveIntervalMs] to avoid excessive writes.
     */
    fun SaveDraft(json: String) {
        val now = System.currentTimeMillis()
        if (now - lastSaveTime < saveIntervalMs) return
        lastSaveTime = now
        prefs.edit().putString(DRAFT_PREFIX + draftKey, json).apply()
    }

    /**
     * Returns the saved draft JSON, or null if no draft exists.
     */
    fun GetDraft(): String? {
        return prefs.getString(DRAFT_PREFIX + draftKey, null)
    }

    /**
     * Clears the saved draft. Call this after successful entity creation.
     */
    fun ClearDraft() {
        prefs.edit().remove(DRAFT_PREFIX + draftKey).apply()
    }

    /**
     * Returns true if a draft exists for this key.
     */
    fun HasDraft(): Boolean {
        return prefs.contains(DRAFT_PREFIX + draftKey)
    }

    companion object {
        private const val DRAFT_PREFIX = "fieldmind_draft_"
        private const val PREFS_NAME = "fieldmind_drafts"

        fun getInstance(context: Context): DraftAutoSave {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return DraftAutoSave(prefs, "", 30_000L)
        }
    }
}

/**
 * Remember a [DraftAutoSave] instance for the given draft key.
 *
 * Automatically saves form state every 30 seconds when [isDirty] is true.
 * Calls [onSaveToJson] to serialize form state to JSON.
 */
@Composable
fun rememberDraftAutoSave(
    key: String,
    isDirty: Boolean,
    onSaveToJson: () -> String,
    onRestoreFromJson: (String) -> Unit,
    saveIntervalMs: Long = 30_000L
): DraftAutoSave {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("fieldmind_drafts", Context.MODE_PRIVATE) }
    val draftHelper = remember { DraftAutoSave(prefs, key, saveIntervalMs) }

    // Restore draft on first composition
    LaunchedEffect(Unit) {
        val draft = draftHelper.GetDraft()
        if (draft != null) {
            onRestoreFromJson(draft)
        }
    }

    // Auto-save every interval when dirty
    LaunchedEffect(isDirty) {
        if (isDirty) {
            while (true) {
                delay(saveIntervalMs)
                if (isDirty) {
                    val json = onSaveToJson()
                    draftHelper.SaveDraft(json)
                }
            }
        }
    }

    // Clean up on dispose (when leaving screen)
    DisposableEffect(Unit) {
        onDispose {
            // Don't clear draft here — let the save function handle it
        }
    }

    return draftHelper
}
