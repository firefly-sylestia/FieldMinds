# Prompt.md — Request Log

## Latest Request (IN PROGRESS)

**Floating explore bubble not appearing over other apps + compact layout default OFF**

### Requested

1. The floating pill overlay above other apps stopped appearing — even after
   clearing app data and re-granting the permission. Fix it.
2. Turn off the compact layout by default (the Spin page's smart compact
   system should ship roomy; compact becomes opt-in).

### Root cause (researched)

Android 15+ introduced a FIRST-TIME overlay pending state: when "Display
over other apps" is granted for the first time — which includes a grant made
right after clearing app data or reinstalling — the system can hold the
grant in a PENDING state. In that state:

- `Settings.canDrawOverlays()` returns **true** (so every gate in the app
  believed the overlay was usable and never re-prompted), but
- overlay windows are silently **never shown**, and
- the AppOps state (`OPSTR_SYSTEM_ALERT_WINDOW`) stays `MODE_IGNORED` until
  the permission settles (the user toggling the special access off/on
  resolves it).

That exactly matches the report: the bubble was "enabled" and the permission
looked granted, but nothing ever floated — and clearing data re-enters the
pending state, so it survived a data clear.

### Plan

1. `AppPreferences.kt` — add `overlayActuallyUsable(context)`: true only when
   `canDrawOverlays()` AND (on Q+) the AppOps mode is `MODE_ALLOWED`
   (guarded by runCatching → default true on error). Use it in
   `exploreServiceShouldRun()` and in the live-notification/overlay toggle
   paths that decided whether the service should keep running.
2. `ExploreSessionService.kt` — `render()` gates the bubble on
   `overlayActuallyUsable` instead of raw `canDrawOverlays`; bubble
   composition deferred to `doOnAttach` (Android 16 attach race — a plain
   post() could run before the window was installed and skip composition
   forever); self-heal budget raised (max 3) with a verify-after-attach that
   rebuilds once if the window attached but composed empty.
3. Gate every permission prompt on `overlayActuallyUsable` so the app
   RE-ASKS instead of silently no-op'ing during the pending state:
   - `TopicRevealScreen.kt` — bubbleWillShow / needsOverlay / ON_RESUME
     handoff all use `overlayActuallyUsable`.
   - `OnboardingScreen.kt` — the overlay card's granted state + ON_RESUME
     refresh use `overlayActuallyUsable` (card stays "Allow" until it
     truly settles).
   - `SettingsScreen.kt` — the bubble row, the toggle-on path and the
     settings-return callback use `overlayActuallyUsable`.
4. Compact layout defaults OFF (`AppPreferences.kt`): `smartSpinLayoutState`
   false, `smartDensityModeState` OFF, `isSmartSpinLayoutEnabled` default
   false, legacy smart-density key default false (fresh install ships roomy;
   users who explicitly picked a mode keep it).
5. Changelog + Prompt.md.

### Status

- All code edits applied: AppPreferences, ExploreSessionService,
  TopicRevealScreen, OnboardingScreen, SettingsScreen.
- Only remaining `Settings.canDrawOverlays` reference is inside
  `overlayActuallyUsable()` itself (verified via code search).
- Reviewer feedback incorporated: pending-state resolution hint added to
  the TopicReveal overlay dialog, the Settings bubble row now explains
  "Granted but not showing yet — toggle off and on once" when stuck in
  the pending state, and the Smart density helper copy is now
  mode-aware (no longer claims shrinking is active while Off).
- Reviewed, committed, pushed as `d7ae65c3`. Done.

## Previous Request (COMPLETE)

**Home + detail tear shadows, hero name height, detail pop-up pill ripple**
— hairline dark rim under both hero tears, hero name scaled to fill the
gap, rippleless detail back/more pills — shipped in `3a03c353`.
