package fieldmind.research.app.features.field.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fieldmind.research.app.shared.presentation.theme.CardBorderStyle
import fieldmind.research.app.shared.presentation.theme.JournalConfig
import fieldmind.research.app.shared.presentation.theme.LocalJournalStyle

// ════════════════════════════════════════════════════════════════════════
//  JournalDecorations — shared journal-aware styling primitives
//
//  Phase 3 of the Whimsical Redesign. Every universal card composable in
//  ClickableCard.kt / SettingsComponents.kt / FieldMindComponents.kt reads
//  LocalJournalStyle and applies these primitives uniformly.
//
//  Round 11 (v0.50.3) — Stripped ~350 lines of dormant per-style drawing code:
//  - drawJournalTexture (parchment / paper / dotgrid / watercolor routines)
//  - JournalOrnament decorative branches (Victorian fleuron, Ghibli cloud)
//  - JournalDivider decorative branches (ornamental rule, wavy path, pencil marks, dot row)
//  - journalCardBrush gradient branches (per-style linearGradient / radialGradient)
//  All stripped because showTexture / showOrnaments / decorativeDividers /
//  useGradientCards are false for all 4 journal presets (v0.50.2 unified them).
//  The functions now short-circuit to clean fallbacks while preserving the
//  public API signatures for backwards compatibility with all call sites.
//
//  Live public API (still called across screens):
//  - [journalBorderStroke]    — BorderStroke derived from JournalConfig (active: Rounded)
//  - [journalTextureModifier] — Modifier (no-op; texture routines removed)
//  - [journalCardBrush]       — SolidColor Brush (gradient branches removed)
//  - [journalCardShape]       — Shape with the active cardCornerRadius (24dp)
//  - [journalChipShape]       — Shape with the active chipCornerRadius (16dp)
//  - [JournalOrnament]        — Composable no-op (ornament branches removed)
//  - [JournalDivider]         — Thin wrapper over HorizontalDivider (decorative branches removed)
// ════════════════════════════════════════════════════════════════════════

/**
 * Returns a [BorderStroke] derived from the active journal's border config.
 * - [CardBorderStyle.Rounded]: subtle rounded outline (the only style used in v0.50.2+)
 * - [CardBorderStyle.Irregular]: thin sketch-like outline (unused, kept for future re-enable)
 * - [CardBorderStyle.Minimal]: no border (unused, kept for future re-enable)
 */
@Composable
fun journalBorderStroke(config: JournalConfig = LocalJournalStyle.current): BorderStroke? {
    if (config.borderWidth <= 0.dp) return null
    val color = when (config.borderStyle) {
        CardBorderStyle.Minimal -> Color.Transparent
        CardBorderStyle.Rounded -> MaterialTheme.colorScheme.outlineVariant
        CardBorderStyle.Irregular -> MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)
    }
    return BorderStroke(config.borderWidth, color)
}

/**
 * Modifier that overlays the journal's paper / parchment / dot-grid / watercolor
 * texture on top of the card surface. Stripped in v0.50.3 — showTexture is false
 * for all 4 presets, so the texture routines never fire. Returns [Modifier] (no-op)
 * for backwards compatibility with every `Modifier.then(journalTextureModifier(journal))`
 * call site. Re-add the drawBehind + drawJournalTexture chain here if textures
 * are re-enabled in a future round.
 */
@Composable
fun journalTextureModifier(
    config: JournalConfig = LocalJournalStyle.current,
    alphaScale: Float = 1f
): Modifier = Modifier

/**
 * Brush for the active journal's card container. Stripped in v0.50.3 —
 * useGradientCards is false for all 4 presets, so the per-style gradient
 * (Victorian linearGradient, Ghibli radialGradient, Sketchbook linearGradient)
 * never renders. Returns a [SolidColor] of the fallback color for backwards
 * compatibility with `SettingsGroupCard`. Re-add the per-style when (config.style)
 * gradient block here if gradients are re-enabled in a future round.
 */
@Composable
fun journalCardBrush(
    config: JournalConfig = LocalJournalStyle.current,
    fallbackColor: Color = MaterialTheme.colorScheme.surfaceContainerLow
): Brush = SolidColor(fallbackColor)

/**
 * Returns the [Shape] to use for the journal's primary card surface. Single
 * source of truth that replaces every site that previously hardcoded
 * `RoundedCornerShape(28.dp / 32.dp / 34.dp / 38.dp)` directly inside
 * private screen composables. All 4 presets now use 24.dp.
 */
@Composable
fun journalCardShape(config: JournalConfig = LocalJournalStyle.current): Shape =
    RoundedCornerShape(config.cardCornerRadius)

/**
 * Returns the [Shape] to use for small chips, badges, and pill-style controls
 * (HeroActionChip, Export / Import / Backup tab pills, InfoChip). All 4 presets
 * now use 16.dp.
 */
@Composable
fun journalChipShape(config: JournalConfig = LocalJournalStyle.current): Shape =
    RoundedCornerShape(config.chipCornerRadius)

/**
 * Per-style ornamental flourish rendered above section headers. Stripped in
 * v0.50.3 — showOrnaments is false for all 4 presets, so the Victorian
 * copperplate fleuron and the Ghibli soft cloud never render. The function
 * is preserved as a no-op for backwards compatibility with [SectionHeader] /
 * [FieldScreenHeader] call sites. Re-add the `when (config.style)` block
 * with the Victorian / Ghibli icon branches here if ornaments are re-enabled.
 */
@Composable
fun JournalOrnament(
    modifier: Modifier = Modifier,
    tint: Color? = null
) {
    // No-op: showOrnaments is false for all 4 journal presets.
}

/**
 * Per-style divider rendered in place of a plain [HorizontalDivider]. Stripped
 * in v0.50.3 — decorativeDividers is false for all 4 presets, so the
 * Victorian ornamental rule + center dot, the Ghibli soft wavy path, the
 * Sketchbook three diagonal pencil marks, and the BulletJournal dot row
 * never render. The function is preserved as a thin wrapper that always
 * delegates to [HorizontalDivider] for backwards compatibility. Re-add the
 * `when (config.style)` block with the per-style Canvas branches here if
 * decorative dividers are re-enabled.
 */
@Composable
fun JournalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outlineVariant
) {
    HorizontalDivider(thickness = thickness, color = color, modifier = modifier)
}
