# Fix Back-Stack Duplication (Same Screen Repeatedly on Back)

## Request

User reported: navigation "stacks" — when tapping around, pressing back walks through the same screens multiple times if they were opened before.

## Analysis

Root cause: the bottom-nav tab pattern anchored `popUpTo(graph.findStartDestination().id)`. The NavHost's declared start destination is `SPLASH`, but SplashScreen pops itself on launch (`popUpTo(SPLASH) { inclusive = true }`), so the anchor is no longer in the back stack. Per Navigation docs, `popUpTo` with a missing destination is a silent no-op — every tab switch and every re-opened push screen piled up duplicate entries, and back walked through them all.

## Plan

- Read DOX chain + all navigation call sites (CurioNavHost, CurioRoutes, CurioBottomNav, Home/Spin/Cabinet/Profile/Settings/Picker/Detail/Capture/Reveal/History/Crash/Lightbox/Onboarding/Splash).
- Add `NavController.navigateToTab(route)` in `CurioRoutes.kt`: `popUpTo(HOME) { saveState = true } + launchSingleTop = true + restoreState = true` (HOME is the persistent root that always remains).
- Switch bottom nav + Home quest card (both branches) + Open Cabinet arrow + empty-state shuffle to `navigateToTab`.
- Add `launchSingleTop = true` to every plain push navigation (Home drawer/avatar/All chip/recent entry/reminder, Settings Manage Categories + Replay Intro, Spin Picker, EntryDetail lightbox ×3, Onboarding finish).
- Update app/AGENTS.md Navigation contract; commit + push.

## Completion Summary

- `CurioRoutes.kt`: added top-level `fun NavController.navigateToTab(route)` (anchor HOME, not start destination; KDoc explains why).
- `CurioBottomNav.kt`: tab onClick uses `navigateToTab`; removed now-unused `findStartDestination` + `NavController` imports.
- `HomeScreen.kt`: quest card (wildcard + category) → `navigateToTab`; Open Cabinet arrow → `navigateToTab(CABINET)`; empty-state shuffle → `navigateToTab(SPIN)`; drawer onNavigate, avatar PROFILE, All chip PICKER, recent entry, reminder card → `launchSingleTop = true`; added `navigateToTab` import, removed `findStartDestination` import.
- `SettingsScreen.kt`: Manage Categories + Replay Intro → `launchSingleTop = true`.
- `SpinScreen.kt`: onBrowseAll PICKER → `launchSingleTop = true`.
- `EntryDetailScreen.kt`: 3 lightbox navigations → `launchSingleTop = true` (safe — lightbox is fullscreen, URI re-set before each navigate).
- `OnboardingScreen.kt`: `finishOnboarding` adds `launchSingleTop = true` (fixes replay-path duplicate HOME: [HOME, ONBOARDING] → pops onboarding → HOME already on top → no second push).
- `app/AGENTS.md`: Navigation contract now mandates `navigateToTab` for tab switching and `launchSingleTop` for push destinations.
- Code review (deepseek-flash) ×2: clean; only a non-blocking note that deep-link/restored-root stacks without HOME degrade to old behavior (parity, not regression).
- Static checks: braces/parens balanced in all 7 changed files; no remaining unguarded `navigate()` calls. Gradle build/lint NOT run (forbidden in this environment; CI validates on push).
