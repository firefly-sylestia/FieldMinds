# Current Request

## Status: COMPLETED — committed and pushed to `revamp`

CI compile fix: `Task :app:compileDebugKotlin FAILED` with
`Unresolved reference 'solidColor'` at CurioCategoryCard.kt:122.

## Root cause
The idle-branch card background used `Brush.solidColor(idleSurface)` — the
`Brush.solidColor` factory isn't resolvable against the Compose BOM this
project resolves (2026.05.01). The reviewer previously asserted it was a
standard API; it isn't available here.

## Change (1 file)
**`app/src/main/java/com/curio/app/ui/components/CurioCategoryCard.kt`**
- Replaced `Brush.solidColor(idleSurface)` with `SolidColor(idleSurface)` —
  the always-available `Brush` subclass constructor (same flat-fill result).
- Added `import androidx.compose.ui.graphics.SolidColor`.
- Both `background()` branches remain `Brush`-typed (selected =
  `Brush.verticalGradient`, idle = `SolidColor`), so no type mismatch.
- `Brush` import stays used by the selected branch — no unused import.
- Grep confirms no other `Brush.solidColor` usages remain in the app.

## Review
- code-reviewer-deepseek-flash: clean — SolidColor is the always-available
  equivalent, both branches Brush-typed, imports correct and in order.

## CI
- Compile gate = GitHub Actions on push (per AGENTS.md — no local Gradle).
  This fix targets the exact reported failure; the re-run on push is the gate.
