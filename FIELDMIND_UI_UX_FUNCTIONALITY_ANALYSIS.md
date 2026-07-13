# FieldMind — UI/UX & Functionality Analysis

**Generated:** 2026-07-13  
**Scope:** Purely UI/UX and functionality/features. No refactor analysis.  
**Covers:** `app/src/main/java/fieldmind/research/app/features/field/` and shared presentation layers.

---

## How to read this document

- **🔴 Critical** — breaks a core flow, causes data loss, or makes a feature unusable.
- **🟠 High** — significantly degrades UX or blocks expected functionality.
- **🟡 Medium** — noticeable polish/behavior gap, but workarounds exist.
- **🟢 Low** — minor visual or interaction inconsistency.

Each item lists the **user-visible impact**, **affected files**, and **recommended fix direction**.

---

## 1. UI/UX Issues

### 1.1 Empty-state inconsistency and weak messaging

**Severity:** 🟠 High  
**Impact:** Users see two different empty-state designs across the app. Some screens use the legacy `EmptyState()` block, others use the newer `DelightfulEmptyState()` with animated nature scenes. The legacy blocks feel flatter and less on-brand, especially in dark mode.

**Affected screens/components:**
- `FieldMindLibraryScreen.kt` — legacy `EmptyState` for sources/notes/flashcards
- `FlashcardSessionScreen.kt` — legacy `EmptyState`
- `FieldLogScreen.kt` — legacy `EmptyState`
- `FieldMindQuestionsScreen.kt` — legacy `EmptyState`
- `InsightsScreen.kt` — uses `DelightfulEmptyState`
- `FieldMindObserveScreen.kt` — uses `DelightfulEmptyState`
- `FieldMindArchiveScreen.kt` — uses `DelightfulEmptyState`

**Fix direction:** Migrate all primary empty states to `DelightfulEmptyState` with context-appropriate copy. Keep `EmptyState` only for compact inline cases (e.g., table cells).

---

### 1.2 Search empty state is hard to see in dark mode

**Severity:** 🟠 High  
**Impact:** The search/filter empty states in Library, Archive, and Questions use `onSurfaceVariant` at default alpha. In dark mode the contrast against `surfaceContainer` backgrounds is low, making the "No results" message hard to read. This is the issue the user already flagged.

**Affected files:**
- `FieldMindLibraryScreen.kt`
- `FieldMindArchiveScreen.kt`
- `FieldMindQuestionsScreen.kt`
- `FieldLogScreen.kt`

**Fix direction:** Boost text contrast for empty states in dark mode (use `onSurface` or higher alpha) and add a subtle icon/illustration so the state is visually anchored.

---

### 1.3 Insights screen empty-state "word glitch box"

**Severity:** 🟠 High  
**Impact:** When there are no observations, `InsightsScreen` shows `DelightfulEmptyState`, but the surrounding card layout can clip the animated scene or the tip text, producing a visible rectangular box/gitch around the text block on certain screen sizes. The user explicitly called this out.

**Affected file:**
- `InsightsScreen.kt` (around the `DelightfulEmptyState` call)

**Fix direction:** Remove any boxed background behind the tip text, ensure the card uses `clip` correctly, and give the empty state full-width padding without a nested Surface.

---

### 1.4 Hardcoded colors break theme consistency

**Severity:** 🟡 Medium  
**Impact:** Several components use hardcoded `Color(...)` values instead of `MaterialTheme.colorScheme`. This causes jarring visuals when the user switches between light/dark or dynamic color.

**Affected files:**
- `FieldMindCameraV2.kt` — `Color.Black`, `Color.White`, `Color(0xFF4CAF50)` for focus/overlay
- `FieldMindPdfViewer.kt` — hardcoded black/white for PDF viewer chrome
- `shared/presentation/theme/festive/ChristmasDecorations.kt` — festive colors are intentional, but should still respect a dark-mode variant
- `shared/presentation/theme/festive/FestiveSplashGreeting.kt` — holiday greeting colors
- `MoonPhaseIcon.kt` — shadow/highlight colors assume dark background

**Fix direction:** Replace hardcoded colors with theme-aware equivalents or add `isDarkMode` branches. Camera overlay is especially important because it appears over a live preview.

---

### 1.5 Button press modifiers can conflict with `clickable`

**Severity:** 🟠 High  
**Impact:** Many screens stack `.pressScale()` or `.expressivePress()` before `.clickable()`. If the press modifier consumes pointer events, the `clickable` may never fire. Even when it works, the visual scale and the ripple can fight each other, producing a "jittery" feel.

**Affected files (sample):**
- `FieldMindProjectsScreen.kt`
- `FieldMindLibraryScreen.kt`
- `FieldMindObserveScreen.kt`
- `FieldMindHomeScreen.kt`
- `FieldMindSettingsScreen.kt`
- `FieldMindComponents.kt` (`EntityCard`, `MetricTile`)

**Fix direction:** Use a single interaction modifier per element. Either rely on `Surface(onClick = ...)` / `Button` ripple, or use a custom press modifier that does **not** consume events and is applied **after** `clickable`.

---

### 1.6 Selection controls (Checkbox/RadioButton/Switch) lack custom styling

**Severity:** 🟡 Medium  
**Impact:** The app uses stock Material3 Checkbox/RadioButton/Switch without theme overrides. They look fine but do not match the app's rounded, "cute" aesthetic. More importantly, some checkbox touch targets are small (e.g., `Modifier.size(24.dp)` in `FieldMindLibraryScreen.kt`), making them hard to tap.

**Affected files:**
- `FieldMindLibraryScreen.kt`
- `FieldDataTable.kt`
- `EvidenceHubPhase6.kt`
- `ProjectPhase5Components.kt`
- `FieldMindDetailScreen.kt`
- `FieldMindDataTools.kt`
- `FieldMindReportScreen.kt`

**Fix direction:** Create a reusable `FieldMindCheckbox`/`FieldMindRadioButton` with larger minimum touch target (48 dp), rounded checkmark shape, and accent color. Already started in `SelectionIndicator.kt` — finish wiring it everywhere.

---

### 1.7 Rectangular boxes and sharp corners still appear in several places

**Severity:** 🟡 Medium  
**Impact:** Despite the "cute" rounded design language, some surfaces still use sharp corners or rectangular borders:
- Segmented button in `FieldMindOnboardingScreen.kt` used `RoundedCornerShape(0.dp)` for middle items (recently changed to 8 dp, but verify visually)
- `OutlinedTextField` shapes vary between `CuteCardDefaults.FieldShape`, `MaterialTheme.shapes.medium`, and no explicit shape
- Some `Surface` components have no shape parameter and default to rectangular

**Affected files:**
- `FieldMindOnboardingScreen.kt`
- `FieldMindSettingsScreen.kt`
- `FieldMindComponents.kt`
- `FieldMindDialogs.kt`

**Fix direction:** Audit all `Surface`, `Card`, `OutlinedTextField`, and `Button` usages for shape consistency. Standardize on `CuteCardDefaults` shapes.

---

### 1.8 Text field style inconsistency

**Severity:** 🟡 Medium  
**Impact:** Some search/input fields use `TextField` (filled style) while others use `OutlinedTextField`. The filled style visually clashes with the outlined style used everywhere else.

**Affected files:**
- `FieldMindSettingsScreen.kt` — search uses `OutlinedTextField` (good)
- `FieldMindLibraryScreen.kt` — source search uses `TextField`
- `FieldMindQuestionsScreen.kt` — question search uses `TextField`
- `FieldMindOnboardingScreen.kt` — API key inputs use `TextField`

**Fix direction:** Replace all `TextField` usages with `OutlinedTextField` + `OutlinedTextFieldDefaults.colors(...)` for visual consistency.

---

### 1.9 Scroll position lost on rotation for several screens

**Severity:** 🟡 Medium  
**Impact:** Some screens use plain `LazyColumn` without `rememberSaveable(saver = LazyListState.Saver)`. After device rotation, the scroll position resets to the top.

**Affected screens (confirmed from search):**
- `FieldMindObserveScreen.kt`
- `FieldMindProjectsScreen.kt`
- `FieldMindTasksScreen.kt`
- `FieldMindTimerToolScreen.kt`
- `FieldMindLearnScreen.kt`
- `VoiceNotesScreen.kt`
- `FieldMindReportScreen.kt`
- `MediaGalleryScreen.kt`

**Fix direction:** Add `rememberSaveable(saver = LazyListState.Saver) { LazyListState() }` to each affected screen.

---

### 1.10 Weather widget text contrast varies by scene

**Severity:** 🟡 Medium  
**Impact:** The home weather widget hardcodes `Color(0xFF1A1A3E)` for light-mode day scenes. This dark navy may not contrast well against bright sunny/fog/snow backgrounds. Night scenes use `Color.White`, which works better but still lacks adaptive contrast.

**Affected file:**
- `FieldMindHomeScreen.kt`

**Fix direction:** Compute text color from the scene's dominant background or use `MaterialTheme.colorScheme.onSurface` with a scrim/overlay to guarantee contrast.

---

### 1.11 Compass tool tips are static and non-contextual

**Severity:** 🟢 Low  
**Impact:** The compass/level tool shows the same three bullet points regardless of whether the device needs calibration, is experiencing magnetic interference, or is level. Users in those states do not get actionable guidance.

**Affected file:**
- `FieldMindCompassLevelToolScreen.kt`

**Fix direction:** Switch tips based on sensor state: calibration tips when `needsCalibration`, interference tips when `isInterference`, normal tips otherwise.

---

### 1.12 Past sessions list capped at 10 with no "Show more"

**Severity:** 🟡 Medium  
**Impact:** On the Observe screen, `completedSessions.take(10)` limits past sessions to 10. Heavy users cannot see older sessions without navigating to the full session log.

**Affected file:**
- `FieldMindObserveScreen.kt`

**Fix direction:** Add a "View all sessions" button or expandable section.

---

## 2. Functionality / Feature Issues

### 2.1 Halloween and Valentine's festive effects are TODO placeholders

**Severity:** 🟡 Medium  
**Impact:** `FestiveOverlay.kt` has explicit `// TODO: Implement Halloween effects` and `// TODO: Implement Valentine's effects` comments. During those holidays the app falls back to no effect or Christmas-only behavior, which feels unfinished.

**Affected file:**
- `shared/presentation/theme/festive/FestiveOverlay.kt`

**Fix direction:** Implement Halloween (falling leaves, bats, dark orange overlay) and Valentine's (floating hearts, rose petals, pink gradient) effects following the existing Christmas/Snowfall pattern.

---

### 2.2 "Encrypted backups" toggle actually toggles auto-backup

**Severity:** 🔴 Critical  
**Impact:** In `SecuritySettingsPage`, the toggle labeled "Encrypted backups" calls `settings::setAutoBackupEnabled`. This is misleading: it turns automatic backup scheduling on/off, not export encryption. The actual encryption controls (`exportPasswordProtectionEnabled`, `exportEncryptionLevel`) are separate.

**Affected file:**
- `FieldMindSettingsScreen.kt` — `SecuritySettingsPage`

**Fix direction:** Rename the toggle to "Auto backup" or wire it to the actual encryption/password-protection settings.

---

### 2.3 Several security/privacy settings exist but have no UI

**Severity:** 🟠 High  
**Impact:** Settings are stored and exported but never exposed to the user, so users cannot change them:
- `alwaysOnScreenDuration` — no duration picker
- `clipboardCleanupDelay` — no delay picker
- `clearClipboardAfterExport` — no toggle
- `exportGpsPrivacy` — no UI
- `exportExcludeMedia` — no UI

**Affected file:**
- `FieldMindSettings.kt`
- `FieldMindSettingsScreen.kt`

**Fix direction:** Add the missing controls in the appropriate settings sub-pages (Capture/Security/Backup).

---

### 2.4 `alwaysOnScreenDuration` and `clipboardCleanupDelay` are not enforced

**Severity:** 🟠 High  
**Impact:** Even if the UI existed, the behavior is not wired:
- `MainActivity.kt` adds/clears `FLAG_KEEP_SCREEN_ON` but never schedules a timeout based on `alwaysOnScreenDuration`.
- Clipboard cleanup in `MainActivity.onPause()` runs immediately, ignoring `clipboardCleanupDelay`.

**Affected files:**
- `MainActivity.kt`

**Fix direction:** Parse the duration strings and post delayed `Handler` tasks.

---

### 2.5 Local model settings are confusing / possibly misleading

**Severity:** 🟠 High  
**Impact:** The "Local model" settings page tells the user "No internet connection, no external server, no model files to download." Yet the settings include `localModelEnabled`, `localModelOption`, `localModelDownloaded`, and `localModelUseForStudy`, implying there is (or will be) a downloadable model. If the feature is purely on-device rule generation, the copy and settings names should reflect that.

**Affected file:**
- `FieldMindSettingsScreen.kt` — `LocalModelSettingsPage`

**Fix direction:** Clarify whether this is a real ML model download or on-device generation. If the latter, rename settings and remove download-related fields from the UI.

---

### 2.6 Decoy mode has no exit path

**Severity:** 🟠 High  
**Impact:** Once decoy mode is active, `DecoyAppContent` provides an `onExitDecoy` callback that is currently a no-op. An honest user who enters the decoy PIN accidentally must force-kill the app to return to the real lock screen.

**Affected file:**
- `FieldMindLockScreen.kt`

**Fix direction:** Add a hidden exit gesture (e.g., 5-tap on logo) that returns to the real lock screen and requires re-authentication.

---

### 2.7 Auto-lock timeout not passed to lifecycle manager

**Severity:** 🟠 High  
**Impact:** `MainActivity.kt` calls `AppLifecycleManager.initialize(this)` without passing the user's `lockTimeout` setting. The lifecycle manager defaults to immediate lock, so the "1 minute / 5 minute / 15 minute" setting has no effect.

**Affected files:**
- `MainActivity.kt`
- `AppLifecycleManager` (if it accepts timeout)

**Fix direction:** Parse `lockTimeout` and pass it to `AppLifecycleManager`.

---

### 2.8 Export/Backup screen has limited actual export actions

**Severity:** 🟠 High  
**Impact:** The Backup/Export screen presents scope and format selectors, but the actual export pipeline is not fully wired in the UI. Users cannot pick a destination folder, see real-time progress, or get a share preview from the screen itself.

**Affected files:**
- `FieldMindBackupExportScreen.kt`
- `FieldMindBackupExportComponents.kt`

**Fix direction:** Add SAF folder picker, determinate progress, and share/save actions. The old `BACKUP_IMPORT_EXPORT_ANALYSIS.md` contained a full redesign plan for this.

---

### 2.9 FigureSidePanel image interpretation is a placeholder

**Severity:** 🟡 Medium  
**Impact:** When a user selects a figure in the canvas, the interpretation panel inserts hardcoded placeholder text (`"This image appears to contain..."`) instead of using the configured AI provider.

**Affected file:**
- `FigureSidePanel.kt`

**Fix direction:** Wire the panel to Gemini/OpenAI with a loading state and error handling.

---

### 2.10 MediaGallery audio/video player is a placeholder

**Severity:** 🟡 Medium  
**Impact:** Audio and video media items in the gallery show only a comment placeholder. There is no actual playback UI.

**Affected file:**
- `MediaGalleryScreen.kt`

**Fix direction:** Implement audio playback (play/pause, seek, waveform) and video playback (ExoPlayer/Media3).

---

### 2.11 Collaboration "Invite" only shares marketing text

**Severity:** 🟡 Medium  
**Impact:** The Invite button in `CollaborationScreen` shares a generic marketing sentence instead of a real invite link or project reference.

**Affected file:**
- `CollaborationScreen.kt`

**Fix direction:** Include project name, observation count, and a deep link (or at least richer context) in the shared text.

---

### 2.12 Weather database retry UI missing

**Severity:** 🟡 Medium  
**Impact:** When a weather catalog entry fails to fetch, there is no retry button. The user must navigate away and back to trigger a refresh.

**Affected file:**
- `WeatherDatabaseScreen.kt`

**Fix direction:** Add a retry action on failed/expired entries.

---

### 2.13 Home screen data-tools card shows only 4 of 8 tools

**Severity:** 🟡 Medium  
**Impact:** The Home screen quick-access card displays only Counter, Measurement, Weather Log, and Species. Checklist, Event Log, Site Log, and Comparison Table are hidden behind an extra tap.

**Affected file:**
- `FieldMindHomeScreen.kt`

**Fix direction:** Add a second row or an expandable "Show more" action within the card.

---

### 2.14 ObserveScreen BackHandler always fires confirmation

**Severity:** 🟡 Medium  
**Impact:** The back handler on the capture screen is enabled unconditionally. Even when there is no dirty content and no active session, pressing back triggers the unsaved-data confirmation flow instead of navigating back directly.

**Affected file:**
- `FieldMindObserveScreen.kt`

**Fix direction:** Set `BackHandler(enabled = hasDirtyContent || session.isActive)` and only show the dialog when actually needed.

---

### 2.15 Settings sub-page BackHandler is redundant

**Severity:** 🟢 Low  
**Impact:** Every settings sub-page uses `BackHandler(enabled = true) { onBack() }`. The `enabled = true` is the default and is duplicated across all sub-pages via `SettingsSubPage`.

**Affected file:**
- `FieldMindSettingsScreen.kt`

**Fix direction:** Simplify to `BackHandler { onBack() }` in `SettingsSubPage`.

---

## 3. Settings / Configuration Issues

### 3.1 Many settings are stored but never read

**Severity:** 🟠 High  
**Impact:** The following settings have full storage/export/import support but no observable behavior in the UI or business logic:
- `alwaysOnScreenDuration`
- `clipboardCleanupDelay`
- `clearClipboardAfterExport`
- `exportGpsPrivacy`
- `exportExcludeMedia`
- `localModelDownloaded`
- `localModelUseForStudy`
- `fieldModeAutoStartTimer`
- `fieldModeObservationSpacing`

**Affected file:**
- `FieldMindSettings.kt`
- `MainActivity.kt`
- Various screens

**Fix direction:** Either implement the behavior or remove the settings until the feature is ready.

---

### 3.2 Security settings page mixes unrelated concepts

**Severity:** 🟡 Medium  
**Impact:** The Security page groups app lock, PIN, decoy, export encryption, metadata removal, clipboard, and app preview. This is overwhelming and leads to the mislabeled "Encrypted backups" toggle described in 2.2.

**Affected file:**
- `FieldMindSettingsScreen.kt`

**Fix direction:** Split into logical groups: "App Lock", "Export Security", "Metadata Privacy", and "Screen Privacy".

---

### 3.3 Animation tuning sliders are exposed but hard to discover

**Severity:** 🟢 Low  
**Impact:** The animation tuning page has many sliders (damping, stiffness, morph duration, shimmer speed, etc.). They are powerful but buried deep in settings and lack live preview.

**Affected file:**
- `FieldMindSettingsScreen.kt` — animation settings

**Fix direction:** Add a small live-preview card that animates when any slider changes.

---

## 4. Navigation / Interaction Issues

### 4.1 Predictive back peek is a mock preview

**Severity:** 🟡 Medium (by design limitation)  
**Impact:** The swipe-back peek shows a generic mock preview (placeholder cards + route label) instead of the real previous screen. This is due to Compose Navigation only composing one destination at a time, but users may expect a real preview.

**Affected files:**
- `FieldMindMotion.kt` — `SwipeBackHost`
- `FieldMindNavigation.kt`

**Fix direction:** Improve the mock per route type (observation, project, settings) so it at least resembles the previous screen. A true real-screen preview requires framework support not yet available.

---

### 4.2 Bottom navigation state can be lost after deep navigation

**Severity:** 🟡 Medium  
**Impact:** After navigating deep into detail screens and pressing back, the bottom tab selection sometimes does not match the currently shown screen. This was partially addressed in the changelog but remains a risk with nested graphs.

**Affected file:**
- `FieldMindNavigation.kt`

**Fix direction:** Ensure `NavController.currentDestination` is used to derive the selected tab, not a separate remembered index.

---

### 4.3 Some dialogs lack scroll persistence or state loss on rotation

**Severity:** 🟢 Low  
**Impact:** Several bottom sheets and dialogs use plain `remember` instead of `rememberSaveable` for form state. Rotating the device while a dialog is open can reset the form.

**Affected files:**
- `FieldMindDialogs.kt`
- `SpeciesIdentificationSheet.kt`
- `FieldMindQuestionsScreen.kt`

**Fix direction:** Audit dialog state and apply `rememberSaveable` where appropriate.

---

## 5. Visual / Theming Issues

### 5.1 Dark-mode visibility of animated empty scene

**Severity:** 🟠 High  
**Impact:** The `AnimatedEmptyScene` nature scene uses colors and alphas that look good in light mode but can become muddy or low-contrast in dark mode. The user explicitly mentioned search empty states are hard to see in dark mode.

**Affected files:**
- `AnimatedEmptyScene.kt`
- `DelightfulEmptyState.kt`

**Fix direction:** Pass `isDarkMode` into the scene and use brighter accent colors / higher alpha for dark backgrounds.

---

### 5.2 Card gradient opacity slider has no live preview

**Severity:** 🟢 Low  
**Impact:** The Appearance settings slider for "Gradient intensity" updates a setting but does not show a live preview of how cards will look.

**Affected file:**
- `FieldMindSettingsScreen.kt`

**Fix direction:** Add a small preview card above the slider that updates in real time.

---

### 5.3 Seasonal color shift is calendar-based only

**Severity:** 🟢 Low  
**Impact:** The seasonal color shift uses the current calendar month. It does not account for user location (southern hemisphere seasons are reversed) or user preference.

**Affected file:**
- `FieldMindSettingsScreen.kt`

**Fix direction:** Add a hemisphere toggle or derive season from location if available.

---

## 6. Summary Table

| # | Issue | Severity | Category | Recommended Fix |
|---|-------|----------|----------|-----------------|
| 1 | Empty-state inconsistency | 🟠 High | UI/UX | Migrate all to `DelightfulEmptyState` |
| 2 | Search empty state low contrast in dark mode | 🟠 High | UI/UX | Boost contrast/add icon |
| 3 | Insights empty-state "word glitch box" | 🟠 High | UI/UX | Remove boxed background, fix clip |
| 4 | Hardcoded colors | 🟡 Medium | UI/UX | Theme-aware colors |
| 5 | Press modifiers conflict with clickable | 🟠 High | UI/UX | Single interaction modifier |
| 6 | Selection controls unstyled/small | 🟡 Medium | UI/UX | Reusable styled controls |
| 7 | Rectangular boxes / sharp corners | 🟡 Medium | UI/UX | Standardize shapes |
| 8 | Text field style inconsistency | 🟡 Medium | UI/UX | Use `OutlinedTextField` |
| 9 | Scroll position lost on rotation | 🟡 Medium | UI/UX | `rememberSaveable` |
| 10 | Weather widget text contrast | 🟡 Medium | UI/UX | Adaptive text color |
| 11 | Compass tips static | 🟢 Low | UI/UX | Contextual tips |
| 12 | Past sessions capped at 10 | 🟡 Medium | UI/UX | "View all" button |
| 13 | Halloween/Valentine effects TODO | 🟡 Medium | Functionality | Implement effects |
| 14 | "Encrypted backups" mislabeled | 🔴 Critical | Functionality | Rename or rewire |
| 15 | Security/privacy settings missing UI | 🟠 High | Functionality | Add missing controls |
| 16 | Always-on/clipboard delays not enforced | 🟠 High | Functionality | Wire behavior |
| 17 | Local model settings confusing | 🟠 High | Functionality | Clarify model vs on-device |
| 18 | Decoy mode no exit | 🟠 High | Functionality | Hidden exit gesture |
| 19 | Auto-lock timeout ignored | 🟠 High | Functionality | Pass timeout to manager |
| 20 | Export/Backup screen limited | 🟠 High | Functionality | Full export flow |
| 21 | FigureSidePanel placeholder | 🟡 Medium | Functionality | AI integration |
| 22 | MediaGallery AV placeholder | 🟡 Medium | Functionality | Media3 playback |
| 23 | Collaboration invite weak | 🟡 Medium | Functionality | Richer share text |
| 24 | Weather DB retry missing | 🟡 Medium | Functionality | Retry action |
| 25 | Home data-tools only 4 of 8 | 🟡 Medium | Functionality | Show all tools |
| 26 | Observe back handler always fires | 🟡 Medium | Functionality | Conditional handler |
| 27 | Predictive back peek is mock | 🟡 Medium | Navigation | Per-route mock |
| 28 | Bottom nav state drift | 🟡 Medium | Navigation | Derive from NavController |
| 29 | Dialog state not saved | 🟢 Low | Navigation | `rememberSaveable` |
| 30 | Animated empty scene dark mode | 🟠 High | Visual | Theme-aware colors |

---

## 7. Top Priorities for the Next Sprint

If only a few items can be fixed, focus on these because they are user-visible and high-impact:

1. **Fix the mislabeled "Encrypted backups" toggle** (2.2) — this is actively misleading.
2. **Improve dark-mode empty states** (1.2, 1.3, 5.1) — directly addresses the user's complaint.
3. **Wire the missing security/privacy settings** (2.3, 2.4, 2.7) — settings that do nothing erode trust.
4. **Resolve press-modifier conflicts** (1.5) — makes the whole app feel smoother.
5. **Implement Halloween/Valentine effects** (2.1) — small, fun, and closes an obvious TODO.

---

*This analysis focuses exclusively on UI/UX and functionality. Code-structure or architectural refactor suggestions are intentionally excluded.*
