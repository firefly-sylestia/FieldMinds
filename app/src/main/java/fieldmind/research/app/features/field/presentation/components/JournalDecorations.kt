package fieldmind.research.app.features.field.presentation.components
import fieldmind.research.app.ui.theme.CuteCardDefaults

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ════════════════════════════════════════════════════════════════════════
//  JournalDecorations — shared border & divider primitives
//
//  v0.51.0 — Retired per-style journal shapes (journalCardShape,
//  journalChipShape) in favour of the unified CuteCardDefaults system
//  (CuteThemeConfig.kt). The 4 journal styles (Victorian, Sketchbook,
//  BulletJournal, Ghibli) have been retired — all cards now use
//  CuteCardDefaults.Shape / ShapeCompact / ShapeHero directly.
//
//  Live public API:
//  - [journalBorderStroke] — BorderStroke with outlineVariant colour
//  - [JournalDivider]      — Thin wrapper over HorizontalDivider
// ════════════════════════════════════════════════════════════════════════

/**
 * Returns a subtle 0.5dp outlineVariant border at very low opacity — just enough
 * to define card shapes without harsh rectangular edges. Prevents shadow-rendering
 * artifacts on cards that would otherwise have no visible edge definition.
 */
@Composable
fun journalBorderStroke(): BorderStroke? =
    BorderStroke(
        width = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.12f)
    )

/**
 * Simple styled divider. Delegates to [HorizontalDivider] with soft
 * outlineVariant colour by default.
 */
@Composable
fun JournalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outlineVariant
) {
    HorizontalDivider(thickness = thickness, color = color, modifier = modifier)
}
