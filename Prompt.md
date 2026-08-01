# Current Request

## Status: COMPLETED — committed and pushed to `revamp`

Two-part request:

1. "make the button nav home button etc pick the tint too but only on the
   screen tint applies not in home" — the bottom nav bar should wear the
   category tint on tinted screens, but stay plain on Home.
2. "when tapping shuffle for directors or any category from the empty state
   cabinet it opens up the shuffle but also breaks the navigation or corrupts
   them" — analyse and fix the navigation break.

## Changes (3 files)

1. **`app/src/main/java/com/curio/app/ui/components/CurioBottomNav.kt`**
   - New `CurioNavTint` object (mutableStateOf<Color?> handoff — mirrors the
     existing `LightboxTarget` out-of-band pattern) so the Scaffold-level
     nav bar can read the Spin page wash without reaching into NavHost
     content state.
   - `CurioBottomBar` containerColor = `CurioNavTint.spinWash` when
     `routePrefix == SPIN` (covers both the `spin` tab and `spin/{categorySlug}`
     via the `/`-prefix), else plain `surface`. Home and Cabinet stay plain —
     the bar only tints where the page actually tints.

2. **`app/src/main/java/com/curio/app/features/spin/SpinScreen.kt`**
   - Publishes `deckCat.categoryBackgroundWash()` to `CurioNavTint` via
     `LaunchedEffect(spinPageWash)` — keyed on the resolved color so category
     switches, dark-mode and the tint toggle republish automatically.
   - `DisposableEffect(Unit)` onDispose clears the handoff (hygiene).

3. **`app/src/main/java/com/curio/app/features/cabinet/CabinetScreen.kt`**
   - **Navigation fix**: both empty-state CTAs ("Discover something" and
     "Shuffle for {category}") changed from plain `navigate(...)` +
     launchSingleTop to `navigateToTab(...)`. Cabinet is itself a tab, so a
     plain push stacked `spin/{categorySlug}` ON TOP of the Cabinet entry
     → hybrid back stack ([HOME, CABINET, spin/directors]) where back walked
     through Cabinet and tab switches piled duplicates. `navigateToTab`
     anchors `popUpTo(HOME){saveState}` + restoreState — the app-mandated
     tab switch (AGENTS.md), matching Home's quest cards and the picker.
   - The remaining plain `navigate(` (entry-card → entryDetail) is a genuine
     push destination and stays.

## Review
- code-reviewer-deepseek-flash: clean on both changes — route-gated tint
  correct (spin only, first-frame `?: surface` fallback), LaunchedEffect /
  onDispose lambdas non-composable safe, imports present, no dead code, no
  leftover plain-navigate Spin CTAs in Cabinet, style consistent.
- Behavior note (intended): back from Shuffle now lands on Home (tab
  semantics) instead of returning to Cabinet — that's the fix.

## CI
- Compile gate = GitHub Actions on push (per AGENTS.md — no local Gradle).
