# Static QA Analysis — Implementation Status

**Generated:** July 6, 2026  
**Source:** Static QA analysis of crash-prone areas, broken lock/security flows, Open-Meteo, crash reporter, DevFullAppTestRunner  
**Method:** Read-only static inspection of actual source files

---

## Legend

| Status | Meaning |
|--------|---------|
| ✅ **FIXED** | Issue has been fully addressed in the source code |
| ✅ **ADDRESSED** | Issue has been resolved with appropriate mitigation (may include documentation of limitations) |
| 🔶 **PARTIALLY FIXED** | Issue has been partially addressed; some work remains |
| ❌ **NOT FIXED** | Issue remains as described in the original analysis |

---

## 1. Crash reporter can fail to show crash UI reliably
**Status:** ✅ **FIXED**

**Evidence:**
- `CrashReporter.init()` stores `previousHandler = Thread.getDefaultUncaughtExceptionHandler()`
- Uses `AtomicBoolean handlingCrash` to prevent recursive crashes
- On failure to launch crash activity, delegates to `previousHandler?.uncaughtException(thread, throwable)`
- Calls `Process.killProcess(Process.myPid())` + `exitProcess(10)` after launching crash activity
- Filters `isRuntimeShutdownThrowable()` and delegates those to previous handler

**Remaining work:** None. The crash reporter is now robust with proper delegation, recursive crash protection, and forced process termination.

---

## 2. Crash screen too dependent on normal app Compose/theme stack
**Status:** 🔶 **PARTIALLY FIXED**

**Evidence:**
- `showNativeFallback()` method creates a pure Android `LinearLayout`/`TextView`/`Button` fallback when Compose fails
- `runCatching` wraps the `setContent` call; on failure it falls back to native views
- Primary path still uses Compose `MaterialTheme` which could fail if resources are corrupted

**Remaining work:** The native fallback is good but the Compose path still uses `MaterialTheme`. Consider using `MaterialTheme(minimumComponentLevel)` or a minimal theme for the crash screen to minimize dependency on the app's full theme stack.

**Prompt:** `Make the Compose crash screen use a minimal theme (no custom colors, no custom fonts, no entity color overrides) so it's less likely to crash if theme resources are corrupted. The native fallback already exists for total failure.`

---

## 3. Collaboration "Share link" / "Invite" can crash if no share target exists
**Status:** ✅ **FIXED**

**Evidence:**
- `safeShareText()` utility function:
  - Checks `shareIntent.resolveActivity(context.packageManager) != null` before calling `startActivity`
  - Wraps `startActivity` in `runCatching`
  - On failure (including `ActivityNotFoundException`), copies text to clipboard
  - Shows snackbar: "No share app found — copied text instead"

**Remaining work:** None. All share intents in `CollaborationScreen` use `safeShareText()`.

---

## 4. Collaboration "Export" button is a placeholder, not a real export
**Status:** ❌ **NOT FIXED**

**Evidence:**
- `onClick = { showFastSnackbar(snackbar, scope, "Opening Export Studio for $shareFormat…"); onOpenExport() }` — just navigates to Export studio
- The "Share link" OutlinedButton sends `"Shared from FieldMind: ${observations.size} observations across ${projects.size} projects."` — a plain text summary, not a real export file
- No format-specific export pipeline is invoked

**Remaining work:** Connect the collaboration export actions to the real export pipeline (`FieldMindExport`), generating actual files in the selected format.

**Prompt:** `Replace the placeholder "Export" and "Share link" actions in CollaborationScreen with calls to the real export pipeline (FieldMindExport.archiveJson / FieldMindExport.fieldMindReport). The format picker (CSV/JSON/PDF Report/FieldMind Archive) should drive the actual export format, and "Share link" should share the generated file via a content URI.`

---

## 5. App lock cooldown UI says "After 5 failed attempts" but code locks after 3 attempts
**Status:** ✅ **FIXED**

**Evidence:**
- `LockSecurityPolicy.FAILED_UNLOCK_THRESHOLD = 5` (consistent constant)
- Settings UI says "After 5 failed attempts"
- Lock screen checks `LockSecurityPolicy.shouldTriggerFailedPolicy(pinAttempts)` which requires `failedAttempts >= FAILED_UNLOCK_THRESHOLD`
- DevFullAppTestRunner asserts `LockSecurityPolicy.FAILED_UNLOCK_THRESHOLD == 5`

**Remaining work:** None. Threshold is consistently 5.

---

## 6. "Do Nothing" failed-unlock setting still applies a 30-second cooldown
**Status:** ✅ **FIXED**

**Evidence:**
- `LockSecurityPolicy.failedUnlockCooldownMs("Do Nothing")` returns `0L`
- Lock screen uses `LockSecurityPolicy.failedUnlockCooldownMs(settings.failedUnlockCooldown.value)`
- DevFullAppTestRunner asserts `LockSecurityPolicy.failedUnlockCooldownMs("Do Nothing") == 0L`

**Remaining work:** None. "Do Nothing" correctly has zero cooldown.

---

## 7. PIN input is not disabled during cooldown
**Status:** ✅ **FIXED**

**Evidence:**
- `FieldMindPinNumpad` receives `enabled = !isPinLocked`
- `isPinLocked = pinLockedUntil > now` (calculated via `LaunchedEffect` polling every second)
- All numpad handlers check `isPinLocked` before processing input: `if (isPinLocked || pin.length >= pinRequiredLength) return@FieldMindPinNumpad`
- Numpad buttons visually dim when disabled `color = if (enabled) ... else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)`

**Remaining work:** None. Cooldown fully disables PIN entry.

---

## 8. "Require biometrics after failure" setting is never enforced for PIN failures
**Status:** ✅ **FIXED**

**Evidence:**
- Lock screen checks: `val requireBiometric = LockSecurityPolicy.shouldRequireBiometricsAfterFailure(pinAttempts, settings.failedUnlockRequireBiometrics.value, hasDeviceAuth || hasDeviceCredential)`
- When triggered: `biometricRequiredAfterFailure = true; startBiometricAuth()`
- When `biometricRequiredAfterFailure` is true, the PIN numpad is hidden and a message is shown: "Biometric or device unlock is required after repeated failures."
- `LockSecurityPolicy.shouldRequireBiometricsAfterFailure()` requires: settingEnabled && deviceAuthAvailable && shouldTriggerFailedPolicy

**Remaining work:** None. Enforcement is fully implemented.

---

## 9. "Panic lock after failure" setting is not implemented
**Status:** ✅ **FIXED**

**Evidence:**
- Lock screen checks: `if (settings.failedUnlockPanicLock.value) { settings.performPanicLockReset() }`
- `FieldMindSettings.performPanicLockReset()` clears:
  - `setAppPinEnabled(false)` + `setAppPinHash("")`
  - `setDecoyPinEnabled(false)` + `setDecoyPinHash("")` + `setDecoyPinLabel("")`
  - All API keys (OpenAI, Gemini, Weather, OpenWeatherMap, WeatherAPI, IMD, Open-Meteo)
  - `setExportPasswordProtectionEnabled(false)` + `setExportPasswordHash("")`
  - `setFailedUnlockPanicLock(false)`

**Remaining work:** The reset is labeled "non-destructive" — user research data is preserved. If the UI/documentation claims it "wipes data", there's a minor mismatch. The implementation is intentional about preserving research data.

---

## 10. In-app PIN uses the device keyboard, not an in-app numpad
**Status:** ✅ **FIXED**

**Evidence:**
- `FieldMindPinNumpad` composable renders a custom numpad with `Surface` buttons for digits 0-9, Clear, and ⌫
- Comment: "Uses an app-rendered numpad so the device keyboard is never opened for unlock."
- `OutlinedTextField` is NOT used for PIN entry in the lock screen
- `PinProgressDots` shows visual feedback (filled dots) instead of text characters
- Haptic feedback on button press: `haptics.performHapticFeedback(HapticFeedbackType.LongPress)`

**Remaining work:** None. The in-app numpad is fully implemented.

---

## 11. PIN length handling accepts too many digits for 4- and 5-digit PINs
**Status:** ✅ **FIXED**

**Evidence:**
- Uses exact `pinRequiredLength = LockSecurityPolicy.pinLengthForLabel(appPinLength)` (4, 5, or 6)
- Numpad entrance guard: `if (isPinLocked || pin.length >= pinRequiredLength) return@FieldMindPinNumpad`
- Verification triggers only when `nextPin.length == pinRequiredLength`
- No `maxLen` default that allows extra digits

**Remaining work:** None. PIN length is exact.

---

## 12. Biometric/device lock option is mislabeled and can behave unexpectedly
**Status:** ✅ **FIXED**

**Evidence:**
- Uses `BIOMETRIC_WEAK or DEVICE_CREDENTIAL` as the allowed authenticators
- Checks `canAuthenticate(deviceAuthenticators)` for overall availability
- Checks `canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)` separately for biometric-only
- Falls back to `KeyguardManager.createConfirmDeviceCredentialIntent()` when biometric fails
- Cancels previous biometric prompt before retrying: `currentBiometricPrompt?.cancelAuthentication()`
- Prevents concurrent auth: `if (isAuthenticating) return`

**Remaining work:** None. The biometric auth flow is robust with proper fallback chains.

---

## 13. Auto-lock timeout appears incompletely enforced
**Status:** ✅ **FIXED**

**Evidence:**
- `AppLifecycleManager.initialize(this)` called in `MainActivity.onCreate()`
- `AppLifecycleManager.onActivityPaused(autoLockEnabled, timeoutLabel)` called in `onPause()`
- `AppLifecycleManager.onActivityResumed(autoLockEnabled, timeoutLabel)` called in `onResume()`
- Lock timeout setting: `settings.lockTimeout` (Immediate / 1 minute / 5 minutes / 15 minutes)
- `settings.autoLockOnBackground` toggle controls whether to lock on background

**Remaining work:** None. Auto-lock timeout is centrally managed via `AppLifecycleManager`.

---

## 14. Privacy keyboard is implemented only as a best-effort keyboard hint
**Status:** ✅ **ADDRESSED**

**Evidence:**
- `PrivacyTextInputWrapper` uses `InterceptPlatformTextInput` to set `IME_FLAG_NO_PERSONALIZED_LEARNING` globally on ALL text fields
- `withPrivacyTyping()` extension on `KeyboardOptions` sets private IME options for Gboard (`nm`), SwiftKey, Samsung Keyboard, and AOSP
- `FieldMindPrivateTextField` uses native `EditText` with `configureFieldMindPrivacy()` setting: `IME_FLAG_NO_PERSONALIZED_LEARNING`, disabled autofill (`IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS`), disabled content capture on API 30+, cleared autofill hints
- Documentation clearly states: "The flag is a request — not every keyboard guarantees compliance"
- `PrivacyTypingIndicator()` shows a subtle lock icon when active
- `PrivacyStatusCard` note: "Requested — depends on keyboard support"

**Remaining work:** None. The implementation is appropriately multi-layered for the Android platform constraints, with honest documentation about limitations.

---

## 15. Open-Meteo is marked as requiring an API key even though the free tier works without one
**Status:** ✅ **FIXED**

**Evidence:**
- `override val requiresApiKey: Boolean = false`
- Comment: "Free tier works without a key; provide one for commercial access."
- `apiKeyLabel: String = "Open-Meteo commercial API key (optional)"`
- `apiKeyPlaceholder` says "Leave blank for free tier. Get a key at open-meteo.com for commercial access."
- DevFullAppTestRunner asserts `!OpenMeteoProvider().requiresApiKey`

**Remaining work:** None. Open-Meteo correctly allows free tier without a key.

---

## 16. Open-Meteo commercial endpoint likely uses the wrong authentication parameter
**Status:** ✅ **FIXED**

**Evidence:**
- Uses `urlBuilder.addQueryParameter("apikey", apiKey)` — this is the correct parameter for Open-Meteo's customer API
- Commercial endpoint: `https://customer-api.open-meteo.com/v1/forecast`
- Free endpoint: `https://api.open-meteo.com/v1/forecast`
- API key is only added when `!apiKey.isNullOrBlank()`

**Remaining work:** None. The authentication parameter is correct per Open-Meteo documentation.

---

## 17. Open-Meteo and other weather failures are swallowed as null, giving poor UI feedback
**Status:** 🔶 **PARTIALLY FIXED**

**Evidence:**
- Individual providers still return `null` on failure (catch-and-return-null pattern)
- But the ViewModel now has `_weatherDiagnostics: StateFlow<WeatherDiagnosticState>` with:
  - `isLoading`, `message`, `provider`, `updatedAt`, `locationStatus`, `lastError`
- `WeatherFetchError` sealed class: `Provider`, `NoLocationPermission`, `NoLocationAvailable`, `Network`, `Unknown`, `Auth`
- `fetchWeatherForLocation()` sets diagnostics with error details when result is null
- `refreshWeatherFromLocation()` sets `_weatherDiagnostics` with specific error messages at each failure point
- `fetchWeatherSnapshot()` just returns null (the catch-and-return-null remains)

**Remaining work:** The individual provider's catch-and-return-null is acceptable since the ViewModel layer now has diagnostic state. However, the error messages could flow back more precisely. The OpenMeteoProvider logs HTTP errors with body snippets.

**Prompt:** `Add a typed error result (sealed class or Result type) to WeatherProvider.fetchWeather() so that the ViewModel can surface specific API errors (rate limit, auth failure, network timeout, etc.) instead of just null. Update OpenMeteoProvider, OpenWeatherMapProvider, and WeatherApiDotComProvider to return detailed errors.`

---

## 18. Weather refresh depends only on lastKnownLocation()
**Status:** ✅ **FIXED**

**Evidence:**
- `refreshWeatherFromLocation()` uses `provider.lastKnownLocation() ?: suspendCancellableCoroutine<CapturedLocation?> { cont -> provider.requestCurrentLocation(timeoutMs = 12_000L) { fresh -> ... } }`
- Falls back to requesting a fresh GPS location when no cached location exists
- 12-second timeout on fresh location request
- Handles `loc == null` case with diagnostic message

**Remaining work:** None. Fresh location is requested when no cached location exists.

---

## 19. DevFullAppTestRunner can hang for a long time
**Status:** ✅ **FIXED**

**Evidence:**
- Each test wrapped in `withTimeout(2_500L)` — 2.5 second limit
- Cancel button that calls `testJob?.cancel()`
- Elapsed time display (seconds counter)
- Progress text updates showing current test phase
- `LinearProgressIndicator` while running

**Remaining work:** None. The test runner is fast and visibly cancellable.

---

## 20. DevFullAppTestRunner mutates real user settings and may not restore them if interrupted
**Status:** ✅ **FIXED**

**Evidence:**
- `TestSettingsSnapshot.capture(viewModel)` captures 18 settings at start
- `TestSettingsSnapshot.restore(viewModel)` restores all captured settings
- Restore is in a `finally` block: `try { ... } catch (e: Exception) { ... } finally { restore.restore(viewModel) }`
- Also catches `CancellationException` separately

**Remaining work:** None. Settings are properly snapshot/restored with `finally` guarantee.

---

## 21. DevFullAppTestRunner does not test the broken security behaviors users care about
**Status:** 🔶 **PARTIALLY FIXED**

**Tests that DO exist:**
| Test | Coverage |
|------|----------|
| PIN hash non-plaintext | ✅ |
| PIN verification (correct + wrong) | ✅ |
| Export password hashing | ✅ |
| Privacy lock toggles | ✅ |
| Screen capture toggles | ✅ |
| Clipboard cleanup toggles | ✅ |
| Lock timeout is valid | ✅ |
| LockSecurityPolicy threshold = 5 | ✅ |
| pinLengthForLabel (4/5/6) | ✅ |
| failedUnlockCooldownMs (Do Nothing, 30s, 5m) | ✅ |
| shouldRequireBiometricsAfterFailure | ✅ |
| Open-Meteo free tier no key | ✅ |
| Crash activity intent construction | ✅ |

**Tests that are MISSING:**
| Test | Coverage |
|------|----------|
| Actual cooldown enforcement in lock screen (timing) | ❌ |
| "Do Nothing" behavior (no cooldown applied) | ❌ |
| Biometric-required-after-failure enforcement flow | ❌ |
| Panic lock execution (data cleared) | ❌ |
| Exact 4/5/6 digit PIN behavior | ❌ |
| In-app numpad rendering | ❌ |
| Auto-lock timeout (background + resume) | ❌ |
| Crash reporter installation | ❌ |

**Prompt:** `Add DevFullAppTestRunner tests for: (1) cooldown enforcement by simulating failed PIN attempts and verifying lock state, (2) "Do Nothing" setting produces zero cooldown, (3) biometric-required-after-failure triggers biometric prompt, (4) panic lock clears API keys and PIN, (5) exact 4, 5, and 6 digit PIN behavior, (6) auto-lock timeout simulation via AppLifecycleManager.`

---

## 22. Security settings overpromise features that are only partially wired
**Status:** 🔶 **PARTIALLY FIXED**

**Feature audit:**

| Setting | Wired? | Evidence |
|---------|--------|----------|
| Failed unlock cooldown | ✅ | Lock screen + LockSecurityPolicy |
| Require biometrics after failure | ✅ | Lock screen calls `startBiometricAuth()` |
| Panic lock | ✅ | Lock screen calls `performPanicLockReset()` |
| Privacy keyboard | ✅ | Multiple layers (see item 14) |
| App preview privacy | ✅ | `appPreviewMode` in settings + MainActivity `applyScreenCaptureProtection` |
| Screenshot block | ✅ | `screenCaptureProtectionEnabled` + `applyScreenCaptureProtection` in MainActivity |
| Clipboard cleanup | ✅ | `onPause()` in MainActivity clears clipboard |
| Auto-lock | ✅ | `AppLifecycleManager` + `MainActivity` lifecycle |
| Encryption levels (Standard/Strong/Maximum) | ⚠️ | Setting exists but `performPanicLockReset` seems to be the only consumer; actual encryption strength may not vary |
| Decoy PIN | ✅ | Full implementation in lock screen + settings |
| Metadata removal (GPS/Camera/Device/EXIF) | ❓ | Settings toggles exist; need to check if export pipeline honors them |

**Remaining work:** The core security features are all wired. A final audit should verify that `metadataRemove*` and `exportEncryptionLevel` settings actually affect the export output. Also verify `clearClipboardAfterExport` is honored.

**Prompt:** `Audit whether metadata removal settings (metadataRemoveGps, metadataRemoveCamera, metadataRemoveDevice, metadataRemoveExif) and export encryption level (exportEncryptionLevel) actually affect the export pipeline output. Ensure `clearClipboardAfterExport` is honored after export operations.`

---

## 23. Share intents in other screens may have the same crash risk
**Status:** ✅ **FIXED**

**Evidence:**
- `FieldMindCrashActivity.shareCrashLog()` wrapped in `runCatching` with clipboard fallback
- `DevFullAppTestRunner` share button wrapped in `runCatching` with clipboard fallback
- `CollaborationScreen` uses centralized `safeShareText()` with `resolveActivity` check + clipboard fallback

**Remaining work:** None. Key share intents are now safe.

---

## 24. Weather provider string state can be inconsistent
**Status:** ✅ **FIXED**

**Evidence:**
- `setWeatherProvider(value)` updates BOTH `_weatherProvider` AND `_weatherProviders`
- `setWeatherProviders(value)` updates both, splitting first provider for `_weatherProvider`
- `setWeatherProviderEnabled(slug, enabled)` calls `setWeatherProviders()` which keeps both in sync
- `performPanicLockReset()` clears weather API keys but does NOT reset provider selection

**Remaining work:** None. Both fields are kept in sync by all setter methods.

---

## Summary

| Status | Count | Items |
|--------|-------|-------|
| ✅ **FIXED** | 18 | 1, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15, 16, 18, 19, 20, 23, 24 |
| ✅ **ADDRESSED** | 1 | 14 |
| 🔶 **PARTIALLY FIXED** | 4 | 2, 17, 21, 22 |
| ❌ **NOT FIXED** | 1 | 4 |

**18 of 24 items fully fixed.**  
**1 item appropriately addressed with multi-layered solution and documentation.**  
**4 items partially fixed with concrete remaining work identified.**  
**1 item entirely unfixed.**

---

## Priority Prompts for Remaining Work

### P0 — Immediate
1. **Item 4:** Replace placeholder CollaborationScreen export/share with real export pipeline calls.

### P1 — Important
2. **Item 21:** Expand DevFullAppTestRunner with missing security behavior tests.
3. **Item 2:** Make crash screen Compose path use minimal theme to reduce dependency risk.

### P2 — Polish
4. **Item 17:** Add typed error results to `WeatherProvider.fetchWeather()` for precise error surfacing.
5. **Item 22:** Verify metadata removal and encryption level settings affect export output.
