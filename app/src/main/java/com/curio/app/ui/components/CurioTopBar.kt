package com.curio.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons

/**
 * Shared top-bar primitives — see CURIO_SPEC.md §0.5 ("M3 surface tokens").
 *
 * Currently houses the standardized [CurioBackButton] used by every
 * push-destination screen (Settings, ManageCategories, TopicHistory,
 * Lightbox, plus the 6 main feature screens). Kept here so the back-button
 * appearance + theming can be tuned in one place if the visual language
 * ever needs to change (e.g. swap the arrow glyph, change the size, or
 * add a haptic on press).
 */

/**
 * The canonical "← Back" button. Circular Surface in surfaceVariant
 * containing the ArrowBack Material Symbol. Sized at 40dp total (24dp
 * glyph + 8dp padding on each side) to match the standard Android
 * touch-target minimum.
 */
@Composable
fun CurioBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Back"
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        CurioIcon(
            name = CurioIcons.ArrowBack,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            size = 24.dp,
            modifier = Modifier.padding(8.dp)
        )
    }
}
