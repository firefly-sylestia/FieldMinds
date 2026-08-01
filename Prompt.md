# Current Request

## Status: COMPLETED (pushed)

"fix dark-mode pink/brown/red/blue tints; add tint to category selection page; expand tint to full app incl. Home; fix remaining cream buttons; separate Home-tint-only toggle"

## Changes (5 files)

1. **`app/src/main/java/com/curio/app/ui/theme/CategoryInk.kt`**
   - `DarkWashTuning` gained optional `deepTwin: Color?`; `resolveMidTone` lerps toward `deepTwin` (or black) when `darken > 0`.
   - Dark-mode tuning: **MOVIES** (red) → hug deep accent + darken 0.10; **SCIENCE** (dark blue) → nudge darker (darken 0.10); **BOOKS** (brown) → new entry, dark coffee twin `0xFF78350F`; **WILDCARD** (pink) → dark rose-pink twin `0xFFBE185D` (fixes muddy grey-pink).
   - Untuned families (indigo/teal) still fall through to `DEFAULT_DARK_WASH` (darken 0) → no behavior drift.

2. **`app/src/main/java/com/curio/app/data/AppPreferences.kt`**
   - New independent `homeTintEnabledState` + `KEY_HOME_TINT_ENABLED`, `isHomeTintEnabled`/`setHomeTintEnabled`, seeded in `initThemeMode`.

3. **`app/src/main/java/com/curio/app/features/home/HomeScreen.kt`**
   - Home background now wears the category wash (`selectedCategory ?: WILDCARD`), gated by BOTH global tint + new Home toggle.
   - New `homeTintSurface(washCat, base)` helper; stat pills, menu/avatar pills, All button, category chips, recent rows, first-time card, reminder card, and "Pick a lane" button all derive from the tinted surface (no more foreign cream on tint).

4. **`app/src/main/java/com/curio/app/features/settings/SettingsScreen.kt`**
   - New "Home screen tint" switch row (Appearance card) — toggles Home tint independently.

5. **`app/src/main/java/com/curio/app/features/spin/SpinScreen.kt`**
   - Full-screen `CategoryPickerSheet` now wears `currentCat.categoryBackgroundWash()` (matches the standalone picker which already had it).

## Review
- code-reviewer-deepseek-flash: clean ×2 (caught + fixed the "Pick a lane" cream button; indentation + kdoc nits fixed).

## CI
- Compile gate = GitHub Actions on push (per AGENTS.md — no local Gradle).
