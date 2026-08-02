# Prompt.md — Request Log

## Latest Request (COMPLETED)

**CI failure: unresolved references `calculateTopPadding` / `calculateLeftPadding` / `isUnspecified` + move the toolbar outside the paper card so ruled lines align with the text in edit mode**

### What was wrong (CI)

`app:compileDebugKotlin` failed on 4 unresolved references, all in files touched by the paper-style work:
- `PaperCard.kt:9` — `import androidx.compose.foundation.layout.calculateTopPadding` (no such top-level symbol)
- `PaperCard.kt:66` — `bodyLineHeight.isUnspecified`
- `RichTextEditor.kt:14-15` — `import androidx.compose.foundation.layout.calculateLeftPadding` / `calculateTopPadding`

Root cause: in this Compose generation (BOM 2026.05.01 → foundation/ui-unit 1.11.2), `PaddingValues.calculateTopPadding()` / `calculateLeftPadding(layoutDirection)` are **member functions** of the interface, not top-level extensions — so the imports were invalid (the call sites themselves were never flagged). `TextUnit.isUnspecified` was replaced by the `TextUnit.Unspecified` companion constant.

### What was changed

**CI fixes**
- `PaperCard.kt` — dropped the bogus `calculateTopPadding` import; added `import androidx.compose.ui.unit.TextUnit`; `bodyLineHeight.isUnspecified` → `bodyLineHeight == TextUnit.Unspecified`.
- `RichTextEditor.kt` — dropped both bogus imports; the member-function calls now resolve without imports.

**Toolbar moved OUTSIDE the paper card (the pending request)**
- `RichTextEditor` gained `paper: Boolean = false` + `paperContentPadding` params. When `paper = true`, the toolbar renders **above** a `PaperCard` that wraps only the field (a local `@Composable () -> Unit` `fieldBlock`), so the ruled lines line up under the field text while typing — the same cadence as the saved detail view. Paper mode forces `surface = Color.Transparent`, `border = null`, and `effectiveFieldPadding = 0.dp`.
- Call sites switched from `PaperCard { RichTextEditor(...) }` to `RichTextEditor(paper = true, paperContentPadding = ...)`:
  - Marginalia journal + quote cards (quote card header now sits above the slip)
  - Reel Notes review field
  - SoundBite note
  - Field Notes all 3 sections
- Removed now-unused imports: `PaperCard` (Marginalia, ReelNotes, SoundBite, FieldNotes). Restored `Color` import in FieldNotesFormat after review (function signature still uses it).

### Review
code-reviewer-deepseek-flash found ONE critical issue — the `Color` import removal in FieldNotesFormat.kt was wrong (the composable signature still takes `accent: Color, tint: Color`) — fixed. Everything else clean: member-function padding calls verified, `fieldBlock` local composable lambda is valid, unused-import leftovers (`heightIn` Marginalia, `RoundedCornerShape` ReelNotes, `TextSpan` SoundBite) are pre-existing warning-only.

### Follow-ups / notes
- NO local Gradle build (per AGENTS.md) — CI validates compilation on push.
