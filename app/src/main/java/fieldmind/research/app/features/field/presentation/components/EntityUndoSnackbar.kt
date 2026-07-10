package fieldmind.research.app.features.field.presentation.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Shows an undo snackbar after an entity is created.
 *
 * The snackbar displays "Created! Undo?" with a 5-second timeout.
 * If the user taps Undo, [onUndo] is called to delete the entity.
 * If the timeout expires, the snackbar dismisses automatically.
 *
 * Usage:
 * ```
 * UndoSnackbar(
 *     hostState = snackbar,
 *     entityName = "observation",
 *     entityId = newId,
 *     onUndo = { viewModel.deleteObservation(newId) }
 * )
 * ```
 */
@Composable
fun UndoSnackbar(
    hostState: SnackbarHostState,
    entityName: String,
    entityId: Long,
    onUndo: (Long) -> Unit,
    timeoutMs: Long = 5000L
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(entityId) {
        if (entityId <= 0L) return@LaunchedEffect

        scope.launch {
            val result = hostState.showSnackbar(
                message = "$entityName created",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )

            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                onUndo(entityId)
            }
        }
    }
}

/**
 * Helper function to show an undo snackbar for any entity type.
 *
 * @param hostState The snackbar host state
 * @param scope The coroutine scope
 * @param entityName The display name of the entity (e.g., "Observation", "Note")
 * @param entityId The ID of the newly created entity
 * @param onUndo Callback to delete the entity if undo is tapped
 * @param timeoutMs How long to show the snackbar (default 5 seconds)
 */
fun showUndoSnackbar(
    hostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope,
    entityName: String,
    entityId: Long,
    onUndo: (Long) -> Unit,
    timeoutMs: Long = 5000L
) {
    scope.launch {
        val result = hostState.showSnackbar(
            message = "$entityName created",
            actionLabel = "Undo",
            duration = SnackbarDuration.Short
        )

        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
            onUndo(entityId)
        }
    }
}
