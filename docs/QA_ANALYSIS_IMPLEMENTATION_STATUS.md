# Static QA Analysis: Placeholder, Disconnected & No-Op Patterns

This document audits the codebase for patterns where UI actions, settings toggles, or navigation callbacks **appear to do work but don't actually do it** — similar to the CollaborationScreen export placeholder (formerly QA item 4). Each item includes assessment, evidence, and implementation prompt.

---

## 1. CollaborationScreen: "Generate portfolio" button — snackbar only
**Status: ❌ Not fixed**

Button labeled "Generate portfolio" shows a hardcoded snackbar instead of generating anything:
```kotlin
Button(onClick = { showFastSnackbar(snackbar, scope, "Portfolio generated!") })
```
No portfolio is generated, no file created, no data aggregated.

**Evidence:** `CollaborationScreen.kt:437`

**Prompt:** Wire "Generate portfolio" to the real export/report pipeline — aggregate observations, notes, and projects into a shareable Markdown/PDF portfolio document and share via Intent.

---

## 2. CollaborationScreen: "Export all" button (Publish section) — navigation only
**Status: ❌ Not fixed**

The "Export all" button in the Publish section only calls `onOpenExport()` which navigates to the ExportStudio screen:
```kotlin
OutlinedButton(onClick = { onOpenExport() }, shape = RoundedCornerShape(22.dp))
```
This is a navigation redirection, not an actual export action. User expects tapping "Export all" to export everything, not go to a different screen.

**Evidence:** `CollaborationScreen.kt:440`

**Prompt:** Replace navigation-only callback with an actual "export all" action that generates a complete FieldMind Archive of all entities and shares it.

---

## 3. FestiveOverlay: Halloween and Valentine's effects — TODO placeholders
**Status: ❌ Not fixed**

Four TODO markers in `FestiveOverlay.kt` indicate effects for Halloween and Valentine's Day exist only as empty branches:
```kotlin
// Placeholder for future Halloween effects (falling leaves, bats, etc.)
// TODO: Implement Halloween effects

// Placeholder for future Valentine's effects (hearts, rose petals, etc.)
// TODO: Implement Valentine's effects
```

**Evidence:** `FestiveOverlay.kt:55-60`, `200-205`

**Prompt:** Implement Halloween effects (falling leaves, bats, dark overlay with orange glow) and Valentine's effects (floating hearts, rose petals, pink gradient overlay) matching the existing Christmas/Snowfall pattern.

---

## 4. FigureSidePanel: "For now, insert a placeholder interpretation" — placeholder text
**Status: ❌ Not fixed**

When a user selects a figure in the canvas, the interpretation panel inserts a hardcoded placeholder string:
```kotlin
// For now, insert a placeholder interpretation.
val placeholderText = "This image appears to contain..."
```

**Evidence:** `FigureSidePanel.kt:455`

**Prompt:** Connect interpretation text generation to the selected AI provider (Gemini/OpenAI) for actual image description/analysis. Show a loading state while generating.

---

## 5. MediaGalleryScreen: Audio/video player — empty placeholder
**Status: ❌ Not fixed**

Media items that are audio or video show only a comment placeholder with no player:
```kotlin
// Audio or video placeholder
```

**Evidence:** `MediaGalleryScreen.kt:207`

**Prompt:** Implement audio playback UI (play/pause, seek bar, waveform visualization) and video playback (ExoPlayer/Media3 integration) for gallery media items.

---

## 6. FieldMindObserveScreen: Timer — UNUSED_EXPRESSION suppressed
**Status: ❌ Not fixed**

The timer display code has a suppressed unused expression:
```kotlin
@Suppress("UNUSED_EXPRESSION")
```

**Evidence:** `FieldMindObserveScreen.kt:1077`

**Prompt:** Clean up the timer elapsed-time calculation — either use the computed value or remove the dead code.

---

## 7. CollaborationScreen "Invite" — only shares marketing text, no actual invite
**Status: ❌ Partially fixed**

The Invite button shares a hardcoded marketing one-liner instead of generating a real collaboration invite link:

```kotlin
text = "Join my FieldMind research workspace! Download FieldMind to collaborate."
```

This was made safe (wrapped in safe share with clipboard fallback) but still doesn't generate a real invite — no deep link, no workspace ID, no project reference.

**Evidence:** `CollaborationScreen.kt:299`

**Prompt:** Generate a proper collaboration invite deep link (e.g., `fieldmind://invite/{sessionId}` or similar) when server-based collaboration is supported. Until then, include specific project names and observation counts in the shared text.

---

## 8. FieldMindCameraCapture: Deprecated, still importable and referenced
**Status: ❌ Not fixed**

`FieldMindCameraCapture` is annotated `@Deprecated("Use FieldMindCameraV2 instead")` but still exists in the codebase and may still be referenced from other parts of the app.

**Evidence:** `FieldMindCameraCapture.kt:51-54`

**Prompt:** Audit all references to `FieldMindCameraCapture`, migrate remaining usages to `FieldMindCameraV2`, then remove the deprecated file.

---

## 9. FieldMindMotion: Two deprecated functions that render nothing
**Status: ❌ Not fixed**

Two functions kept for binary compatibility that render nothing:
```kotlin
/** DEPRECATED — kept for binary compatibility. Renders nothing. */
@Suppress("UNUSED_PARAMETER")
```

**Evidence:** `FieldMindMotion.kt:478-490`

**Prompt:** Search for callers of these deprecated functions (expected to be zero after major releases), remove functions and all references.

---

## 10. lock/unlock: `exportProgress` state written but never displayed
**Status: ❌ Minor — not fixed**

In both `FieldMindBackupExportScreen.kt` and `CollaborationScreen.kt`, `exportProgress` (Float state) is assigned during export steps but the `LinearProgressIndicator` doesn't use it as a determinate progress indicator — it shows indeterminate animation:

```kotlin
// exportProgress is assigned 0.3f, 0.7f, 0f... but LinearProgressIndicator is:
LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(4.dp)...)
```

**Evidence:** `CollaborationScreen.kt:70`, `FieldMindBackupExportScreen.kt:124`

**Prompt:** Wire `exportProgress` into `LinearProgressIndicator` as `progress = { exportProgress }` so users see actual progress instead of an indeterminate spinner.

---

## 11. DevFullAppTestRunner: Test name strings differ from actual test behavior
**Status: ❌ Minor — not fixed**

Several test names describe behavior that doesn't match what the test actually validates. For example:
- "Lock cooldown enforcement" test checks PIN hashing, not actual cooldown
- "Biometric required after failure" doesn't actually test biometric enforcement

**Evidence:** `DevFullAppTestRunner.kt` — test names vs test implementations

**Prompt:** Rename tests to match their actual behavior, or expand tests to cover what the names describe.

---

## 12. Expired/Failed data in weather catalog — silent null path
**Status: ❌ Not fixed**

Weather database screen handles expired/failed weather entries as silent:
```kotlin
// No retry UI when weather fetch fails — user must navigate away and back
```

**Evidence:** `WeatherDatabaseScreen.kt` around retry logic

**Prompt:** Add a "Retry" button/icon next to expired or failed weather entries in the weather database/history views.

---

## 13. Open-Meteo commercial API uses wrong auth parameter
**Status: ❌ Partially fixed (key requirement corrected, but parameter format untested)**

`OpenMeteoProvider.kt` appends `&apikey=$apiKey` for commercial API. The correct parameter format for Open-Meteo commercial API needs verification — Open-Meteo commercial may use a different parameter name or header.

**Evidence:** `OpenMeteoProvider.kt` — `&apikey=` parameter

**Prompt:** Verify Open-Meteo commercial API authentication docs. Test with a real commercial API key to confirm the parameter works or switch to the correct mechanism.

---

## 14. "Do Nothing" cooldown still applies 30-second lock in LockScreen
**Status: ⚡ Fixed via prior work**

Original QA item 6. The lock screen's cooldown mapping defaulted to 30_000L for unrecognized settings, so "Do Nothing" still locked for 30 seconds. Fixed by checking the cooldown setting name explicitly in `FieldMindLockScreen.kt`.

**Evidence:** Fixed in a prior pass — no further action needed.

---

## 15. Settings label says "5 failed attempts" but code triggers at 3
**Status: ⚡ Fixed via prior work**

Original QA item 5. `FieldMindSettingsScreen.kt` says "After 5 failed attempts" but lock screen uses `pinAttempts >= 3`.

**Evidence:** Fixed in a prior pass — UI label and enforcement threshold now match.

---

## 16. PIN input not disabled during cooldown
**Status: ⚡ Fixed via prior work**

Original QA item 7. The PIN text field remained active during cooldown, allowing bypass.

**Evidence:** Fixed in a prior pass — added `enabled = !isPinLocked` to OutlinedTextField.

---

## 17. Require Biometrics after failure — never enforced
**Status: ⚡ Fixed via prior work**

Original QA item 8. `failedUnlockRequireBiometrics` setting was saved but never checked in lock screen.

**Evidence:** Fixed in a prior pass — lock screen now checks the setting after failed attempts.

---

## 18. Panic Lock after failure — never implemented
**Status: ⚡ Fixed via prior work**

Original QA item 9. `failedUnlockPanicLock` setting was exposed in UI and warned about wiping data but never executed.

**Evidence:** Fixed in a prior pass — panic lock now clears app data via `AppLifecycleManager.clearAllData()`.

---

## 19. PIN length accepts too many digits — maxLen off by one
**Status: ⚡ Fixed via prior work**

Original QA item 11. PIN input allowed up to 6 digits for 4-digit PINs.

**Evidence:** Fixed in a prior pass — `maxLen` now matches `pinRequiredLength` exactly.

---

## 20. In-app PIN uses device keyboard, not in-app numpad
**Status: ⚡ Fixed via prior work**

Original QA item 10. `OutlinedTextField` with `KeyboardType.NumberPassword` triggered device keyboard.

**Evidence:** Fixed in a prior pass — replaced with an in-app `LazyVerticalGrid` numpad with digit buttons.

---

## 21. LockSecurityPolicy needs runtime permission rationale
**Status: ⚡ Fixed via prior work**

Devices without biometric hardware showed confusing error.

**Evidence:** Fixed in a prior pass — added `showBiometricUnavailableDialog()` and fallback to device credentials.

---

## 22. Biometric/device lock authentication path has fallback issues
**Status: ⚡ Fixed via prior work**

`canAuthenticate(BIOMETRIC_WEAK)` fails on some devices where `BIOMETRIC_WEAK | DEVICE_CREDENTIAL` succeeds.

**Evidence:** Fixed in a prior pass — authentication now tries `BIOMETRIC_WEAK or DEVICE_CREDENTIAL` first, then falls back to `KeyguardManager`.

---

## 23. Auto-lock timeout not centrally enforced
**Status: ⚡ Fixed via prior work**

Auto-lock timeout setting was saved but not enforced in `MainActivity` lifecycle.

**Evidence:** Fixed in a prior pass — `MainActivity` now checks `autoLockTimeout` on resume and compares with background timestamp.

---

## 24. Weather provider string state inconsistency (`weatherProvider` vs `weatherProviders`)
**Status: ⚡ Fixed via prior work**

`setWeatherProvider()` only updated the single field while some code used the list field.

**Evidence:** Fixed in a prior pass — both fields are now updated atomically.

---

## 25. Weather refresh depends only on `lastKnownLocation()` — stale data
**Status: ⚡ Fixed via prior work**

`refreshWeatherFromLocation()` returned null when no last known location existed.

**Evidence:** Fixed in a prior pass — now requests a fresh one-shot location when `lastKnownLocation()` is null.

---

## 26. Open-Meteo `requiresApiKey` is true but free tier works without one
**Status: ⚡ Fixed via prior work**

`OpenMeteoProvider.requiresApiKey` was `true`, blocking free-tier usage.

**Evidence:** Fixed in a prior pass — now requires API key only for commercial endpoint.

---

## 27. Weather failures swallowed as null with no error state in ViewModel
**Status: ⚡ Fixed via prior work**

`fetchWeather()` caught all exceptions and returned null with no error signal.

**Evidence:** Fixed in a prior pass — `WeatherSnapshot` now includes error state, and error status is surfaced in UI.

---

## 28. Crash Reporter doesn't delegate to previous exception handler
**Status: ⚡ Fixed via prior work**

`CrashReporter.init()` didn't chain to the previous/default uncaught exception handler.

**Evidence:** Fixed in a prior pass — now delegates to `previousHandler` after processing.

---

## 29. FieldMindCrashActivity too dependent on app theme stack
**Status: ⚡ Fixed via prior work**

CrashActivity used `FieldMindTheme` with custom colors and fonts.

**Evidence:** Fixed in a prior pass — now uses a self-contained `CrashTheme` with hardcoded `SafeColors` and explicit Material overrides.

---

## 30. Collaboration share intents — ActivityNotFoundException risk
**Status: ⚡ Fixed via prior work**

`context.startActivity(Intent.createChooser(...))` was called directly without try/catch.

**Evidence:** Fixed in a prior pass — all share intents now use `safeShareText()` helper with `runCatching` and clipboard fallback.

---

## 31. DevFullAppTestRunner — Flow `.first()` can hang for 10+ seconds per call
**Status: ⚡ Fixed via prior work**

Each `StateFlow.first()` call could block until emission, causing the test runner to appear stuck.

**Evidence:** Fixed in a prior pass — individual test timeout reduced and cancellable scope added.

---

## 32. DevFullAppTestRunner mutates real settings without restore on crash
**Status: ⚡ Fixed via prior work**

The test runner toggles settings but wouldn't restore them if cancelled mid-run.

**Evidence:** Fixed in a prior pass — snapshot/restore pattern added with `finally` block.

---

## Summary

| Category | Count | Status |
|----------|-------|--------|
| ❌ Not fixed (new findings) | 14 items | #1-14 |
| ⚡ Fixed (original QA items) | 18 items | #14-32 |

### Top priority unfixed items

1. **#1** — "Generate portfolio" is a snackbar-only placeholder
2. **#4** — Figure interpretation uses placeholder text instead of AI
3. **#3** — Halloween and Valentine's effects are empty TODO branches
4. **#6** — Timer has suppressed unused expression
5. **#7** — Collaboration invite shares marketing text, not real invite
6. **#13** — Open-Meteo commercial auth parameter may be wrong
7. **#8** — Deprecated camera component still in codebase
