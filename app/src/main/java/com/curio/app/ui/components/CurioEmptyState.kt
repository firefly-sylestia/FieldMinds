package com.curio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.curio.app.ui.theme.CurioIcon

/**
 * Curio's universal empty-state skeleton — see CURIO_SPEC.md §13.7.
 *
 * Used by:
 *  - Home (no captures yet)
 *  - Cabinet (no entries at all / filtered empty)
 *  - Topic History (no spins yet)
 *  - Cabinet search (no results)
 *
 * Layout (centered column):
 *  ```
 *      [ glyph @ 96dp in category accent tint ]
 *      Headline (geom, 24sp heavy)
 *      Subtext (Inter neutral, 16sp muted, 1-2 lines, centered)
 *      [ optional Primary CTA — full-width pill ]
 *  ```
 *
 * The glyph receives a [tint] parameter (typically the category accent at
 * 20% alpha) so it can match the surrounding category surface. If null,
 * it falls back to onSurfaceVariant.
 */
@Composable
fun CurioEmptyState(
    glyph: String,
    headline: String,
    subtext: String,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color? = MaterialTheme.colorScheme.primary.copy(alpha = 0.40f),
    ctaLabel: String? = null,
    onCtaClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(
                    name = glyph,
                    contentDescription = null,
                    tint = tint ?: MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 96.dp
                )
            }

            Text(
                text = headline,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (ctaLabel != null) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onCtaClick,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = ctaLabel,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
