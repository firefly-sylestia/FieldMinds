# Request: Settings option to turn off the category tint

## Request
Add an option in Settings to turn off the category tint and use the plain theme background (the cream/midnight "something else" colors from before the wash rollout) instead of the tinted wash.

## Changes
1. `AppPreferences.kt` — added `KEY_TINT_WASH_ENABLED`; reactive `tintWashEnabledState` (private set, default true) seeded in `initThemeMode`; `isTintWashEnabled`/`setTintWashEnabled` (default on). Matches the themeModeState/reminderEnabledState pattern; backup restore re-seeds it via initThemeMode.
2. `CategoryInk.kt` — `categoryBackgroundWash()` returns `MaterialTheme.colorScheme.background` early when the toggle is off. Single choke point gates Spin (root + BottomCta), Topic Reveal, Save/Capture (root + tray), Cabinet, Picker, EntryDetail (root + expanded wash) automatically.
3. `SaveCaptureScreen.kt` — Save button: containerColor `if (tintWash) cat.tint else cat.accent`, content/spinner/check `if (tintWash) cat.categoryInk() else Color.White`. Follow-up: `tintWash` moved up to one shared declaration; the topic-reminder strip (Surface color, icon chip, inks) now falls back to `surfaceContainerHigh` + theme neutrals when the toggle is off, and the CTA top gradient edge falls back to a neutral outline gradient.
4. `GalleryWallFormat.kt` — mood board canvas Surface: `if (tintWashEnabledState) tint else Color.Transparent` (pre-tint state).
5. `EntryDetailScreen.kt` — saved board Surface: `if (tintWashEnabledState) category.tint else surfaceContainerHigh` (pre-tint state).
6. `SettingsScreen.kt` — Appearance card gained a "Category tint" row (Palette icon + reactive subtitle + Switch) with a CurioSettingsDivider above it, matching card conventions.

## Validation
- Code reviewer passed (2 passes): reactive mutableStateOf read valid in composition, fallbacks match original pre-tint colors, no dead imports, restore re-seeds state, single `tintWash` declaration in scope with no shadowing, all fallback colors are colorScheme members (no new imports). Non-blocking note: the dark-mode neutral gradient edge is subtle — acceptable for a 1dp divider.

## Completion summary
- Committed & pushed (daa9e22e + follow-up).
