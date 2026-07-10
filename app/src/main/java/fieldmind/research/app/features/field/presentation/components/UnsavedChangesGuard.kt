package fieldmind.research.app.features.field.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Reusable guard that intercepts back navigation when the form has unsaved changes.
 *
 * Shows a [SwipeableAlertDialog] confirmation dialog when the user tries to navigate
 * back with dirty form data. Supports two modes:
 * - **Save mode**: Shows "Save & exit" / "Discard" / "Keep editing"
 * - **Simple mode**: Shows "Discard" / "Keep editing"
 *
 * Usage:
 * ```
 * UnsavedChangesGuard(
 *     isDirty = title.isNotBlank() || body.isNotBlank(),
 *     onSave = { save(); onBack() },
 *     onDiscard = { onBack() }
 * )
 * ```
 */
@Composable
fun UnsavedChangesGuard(
    isDirty: Boolean,
    onDiscard: () -> Unit,
    onSave: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = enabled && isDirty) {
        showDialog = true
    }

    if (showDialog) {
        SwipeableAlertDialog(
            onDismissRequest = { showDialog = false },
            icon = { Icon(icon = FieldMindIcons.Info, contentDescription = null, size = 28.dp) },
            title = { Text("Unsaved changes") },
            text = {
                Text(
                    "You have unsaved changes. Are you sure you want to go back?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                if (onSave != null) {
                    Button(
                        onClick = {
                            showDialog = false
                            onSave()
                        },
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Text("Save & exit")
                    }
                } else {
                    Button(
                        onClick = {
                            showDialog = false
                            onDiscard()
                        },
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Discard")
                    }
                }
            },
            dismissButton = {
                if (onSave != null) {
                    TextButton(
                        onClick = {
                            showDialog = false
                            onDiscard()
                        }
                    ) {
                        Text("Discard", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = { showDialog = false }) {
                    Text("Keep editing")
                }
            }
        )
    }
}
