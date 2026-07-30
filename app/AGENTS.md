# Curio App Module (Active Build) — AGENTS.md

## DOX Framework

This file is a child of the DOX hierarchy defined in `master.md`. It follows the root `AGENTS.md` as its parent DOX rail.

**DOX chain:** `master.md` ← `AGENTS.md` (root) ← `app/AGENTS.md` (this file)

Read `master.md` and root `AGENTS.md` first, then this file for app-module-specific contracts.

## Purpose

The `app/` module is the active Android application — **Curio**, a discovery app that hands the user a topic (via "The Spin" roulette) to explore in the real world, then captures what they found into "The Cabinet" library. The full UX/UI spec lives at [`CURIO_SPEC.md`](CURIO_SPEC.md) at the repo root of this module. Every screen and component decision must reference this spec — it is the source of truth for what users see.

The **data layer** (category taxonomy, topic schema, `ExploreAction` prompt format, authoring pipeline, rollout cadence) is documented separately in [`CURIO_DATA_PLAN.md`](CURIO_DATA_PLAN.md). It expands the category palette from 6 → 10 and ships 150+ topics per category authored via LLM-draft + human-review. **Read both docs together** before any feature work that touches data or content.

The legacy FieldMind codebase is preserved at `app-legacy/` (frozen, never modified). Curio inherits two things from it: the **Material Symbols** variable font and **geom.ttf** display typography (see `CURIO_SPEC.md` §0.4 + §0.6). Both font files are **copied** into `app/src/main/res/font/`; `app-legacy/` is never read at runtime by Curio except via the curated copy.

## Ownership

### Package layout (current, Phase 2 scaffold)

```
app/src/main/java/com/curio/app/
├── MainActivity.kt                 # single Activity, edge-to-edge + CurioTheme + NavHost
├── data/
│   └── Category.kt                 # CategoryId enum + CurioCategory data class + canonical 6
├── navigation/
│   ├── CurioRoutes.kt              # all route constants + builders + bottomNavRoutes set
│   └── CurioNavHost.kt             # Scaffold-wrapped NavHost with conditional bottom nav
├── ui/
│   ├── theme/                      # design system primitives
│   │   ├── CurioColors.kt          # pastel coral palette + 6 category accents + wildcard gradient
│   │   ├── CurioTypography.kt      # geom.ttf for display/headline/label; M3 default for body
│   │   ├── CurioShapes.kt          # 16/24/32/48 corner tokens
│   │   ├── CurioIcons.kt           # glyph constants + CurioIcon(name, ...) ligature renderer
│   │   └── CurioTheme.kt           # light/dark M3 color schemes + edge-to-edge SideEffect
│   └── components/                 # reusable building blocks
│       ├── CurioBottomNav.kt       # 3-tab M3 NavigationBar with saveState/restoreState
│       ├── CurioCategoryChip.kt    # FilterChip per category + CurioWildcardChip
│       ├── CurioEmptyState.kt      # universal §13.7 empty-state skeleton
│       ├── CurioHeroCard.kt        # ~40% vertical hero Spin card on Home
│       └── CurioStreakPill.kt      # streak indicator pill + CurioSecondaryAction helper
└── features/
    ├── splash/SplashScreen.kt      # §13.1 splash — auto_awesome glyph + "Curio" + 3-dot pulse, 800ms → HOME
    ├── home/HomeScreen.kt          # §3 home — top bar, greeting, streak, hero, chips, recently explored empty state
    └── PlaceholderScreens.kt       # ONE file containing 11 stubs: Spin, Cabinet, CategoryPicker, TopicReveal, SaveCapture, EntryDetail, Settings, Onboarding, ManageCategories, TopicHistory, Lightbox. Each uses a shared `PlaceholderScaffold` with back arrow + glyph + title + subtitle + "Design phase · logic comes later". Real implementations replace these one-by-one in later phases.
```

### Resources

- `app/src/main/res/font/geom.ttf` — display/headline typography (copied from `app-legacy/src/main/res/font/geom.ttf`)
- `app/src/main/res/font/material_symbols_outlined.ttf` — UI + category icons (copied from `app-legacy/src/main/res/font/material_symbols_outlined.ttf`)
- `app/src/main/res/values/strings.xml` — Curio app name + screen titles + category display names
- `app/src/main/res/values/themes.xml` — `Theme.Curio` (M3 DayNight no-actionbar, cream surface)
- `app/src/main/res/values/colors.xml` — XML color resources (cream + coral + plum) used at the OS-level splash background before Compose takes over
- `app/src/main/res/drawable/ic_launcher_{background,foreground}.xml` — adaptive launcher icon (coral background + cream wheel + 3 colored wedges + plum pointer + butter yellow sparkle)
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher{,_round}.xml` — adaptive-icon declarations referencing the drawables above
- `app/src/main/assets/topics/` — Curio topic data files (one per ready category; see Content authoring below)

### Canonical design doc

- `app/CURIO_SPEC.md` — Curio's full UI/UX spec (v2). **Read this before adding any screen or component** — it is the source of truth for all design decisions (colors, typography, shapes, motion, navigation, screen layouts, empty states, etc.).

## Local Contracts

### Identity
- `namespace = "com.curio.app"` (new package, separate from FieldMind)
- `applicationId = "com.curio.app"` (new install, separate from FieldMind; users install Curio as a separate app)
- `minSdk = 26`, `targetSdk = 37`, `compileSdk = 37`
- `versionName = "0.1.0-curio"`, `versionCode = 1`
- No product flavors yet — flavors will be reintroduced in a later phase when Room DB + storage permissions land
- Debug builds append `.debug` to `applicationId` → `com.curio.app.debug` so both can coexist on one device
- Inherits `material_symbols_outlined.ttf` + `geom.ttf` from `app-legacy/src/main/res/font/` (copied once during the Phase 2 scaffold, never re-read at runtime)

### Curio Database (separate from FieldMind)
- Curio installs as a separate app under `applicationId = "com.curio.app"` — its data directory is `/data/data/com.curio.app/databases/` (DB name TBD when persistence lands).
- FieldMind's data lives in FieldMind's separate install at `/data/data/fieldmind.research.app/databases/fieldmind_database`. Curio CANNOT access it directly.
- FieldMind data is recoverable only by sideloading the legacy FieldMind APK (built from `app-legacy/`) and using its built-in V3 backup exporter (see `app-legacy/src/main/java/.../data/export/FieldMindExport.kt`).
- The two apps do not share DB names, schemas, or SharedPreferences namespaces — fully isolated.

### UI
- All UI is 100% Jetpack Compose. No XML layouts for screens, ever.
- `MainActivity` is the only entry point. It hosts `CurioNavHost` inside `CurioTheme`.
- Edge-to-edge is enabled at the Activity level; the system bars are themed by `CurioTheme`'s `SideEffect` to match the current color scheme + light/dark mode.
- **NO emoji anywhere** in user-facing copy or visuals. Use `CurioIcon(name = CurioIcons.X)` with the Material Symbols ligature font instead. See `CURIO_SPEC.md` §0.6.
- All glyph names used by `CurioIcon` are declared in `CurioIcons.kt` (single source of truth for icon names). Adding a glyph = adding a `const val` there first.

### Navigation
- Single NavHost with flat routes (see `CurioRoutes.kt`). Bottom nav visibility is gated by the `CurioRoutes.bottomNavRoutes` set (`HOME`, `SPIN`, `CABINET`).
- Bottom-nav switching uses the standard Compose pattern: `popUpTo(startDestination) { saveState = true }` + `launchSingleTop = true` + `restoreState = true`.
- Tab routes also accept a `categorySlug` argument so the same `Spin` screen renders both as a tab target (`categorySlug = null`) and as a pushed destination (`categorySlug = "music"` etc.).

## Work Guidance

### Adding a new screen
1. Read `CURIO_SPEC.md` section for the screen.
2. Create the file at `app/src/main/java/com/curio/app/features/{feature}/{Feature}Screen.kt`.
3. If it's a stack of related sub-screens, group them in one file like `PlaceholderScreens.kt` does today, with a shared `*Scaffold` private helper at the top.
4. Add a route constant + (if needed) a route builder to `CurioRoutes.kt`.
5. Register the `composable(route) { ... }` block in `CurioNavHost.kt`.
6. If the screen should hide the bottom nav, make sure its route is NOT in `CurioRoutes.bottomNavRoutes`. Add a per-feature AGENTS.md if the screen has non-obvious contracts.

### Adding a new design system primitive
- Add to `ui/theme/` (colors → `CurioColors.kt`, glyphs → `CurioIcons.kt`, etc.).
- New colors must be justified against `CURIO_SPEC.md` §0.2. If the spec doesn't mention it, push back and update the spec first.
- New icons must be declared in the `CurioIcons` object (snake_case ligature names) — do NOT inline glyph names in screens.
- **All design-system primitives (the `CurioIcon` composable + `CurioIcons` glyph constants object) live under `ui/theme/`.** Components in `ui/components/` consume them via import — they do not re-export them. Wrong-package imports (e.g. `import com.curio.app.ui.components.CurioIcon`) compile silently against an empty package and only fail in CI's `compileDebugKotlin`. Always import from `ui.theme.*`.

### Phase plan (current & next)
- **Phase 2 (current)**: Design-system + NavHost + Home/Splash screens + 11 placeholder stub screens. CI gate verifies compilation. No business logic, no Room, no DataStore wiring yet.
- **Phase 3 (next)**: Spin dial rendering, Onboarding flow, Reel/Marginalia/Gallery Wall/Field Notes capture format bodies, Cabinet grid rendering.
- **Phase 4**: Per-entry persistence (ViewModels + Room), state preservation across spins. Also: **first content drop** — seed Music per `CURIO_DATA_PLAN.md` §5.1 (150 topics, LLM-drafted + human-reviewed, ships as `assets/topics/music.json` + a `validatetopics` Gradle task).
- **Phase 5+**: Streak tracking, share-card generation, Emergency Recovery hooks for FieldMind data. Per-category content drops (Movies, Books, Art, Science, then the 4 new categories) continue at one-per-PR cadence per `CURIO_DATA_PLAN.md` §5.1.

### Content authoring (CURIO_DATA_PLAN.md §2 + §6)

Topic data lives in JSON files under `app/src/main/assets/topics/{category}.json`. The schema is `CurioTopic` + `ExploreAction` — see [`assets/topics/SCHEMA.md`](src/main/assets/topics/SCHEMA.md) for the in-folder quick reference and `CURIO_DATA_PLAN.md` §2 for the full source-of-truth.

- **Validation:** `./gradlew validateTopics` parses every JSON file in `assets/topics/` and asserts the §2 schema. The task is wired into `preBuild` automatically when JSON files exist, so a malformed entry fails `assembleDebug` / `assembleRelease`.
- **Adding a new topic:** see `SCHEMA.md` "Authoring a new topic (quick recipe)". For the full §6 LLM authoring prompt template, see `CURIO_DATA_PLAN.md` §6.
- **Adding a new category:** see `CURIO_DATA_PLAN.md` §5.2 step 5 — toggle `isReady = true` on `CurioCategory` only when 100+ topics are authored + reviewed. Categories with `isReady = false` are filtered out of the Home chip row + Category Picker and surface as "Coming soon" empty-state slots.

## Verification

- `MainActivity` compiles and runs as `com.curio.app` on debug builds with `applicationId = "com.curio.app.debug"`.
- No background workers, no widgets, no Room/SharedPreferences persistence wiring yet — those arrive in Phase 4+.
- **CI gate**: this environment has no Android SDK, so CI on push to `revamp` is the source of truth for compilation. Local `gradlew assemble*` is explicitly forbidden by root AGENTS.md.
- **CI expectations (flavorless)**: the new `app/` does NOT define `github`/`fdroid` product flavors. CI workflows call `./gradlew assembleDebug assembleRelease` (PRs) and `./gradlew assembleRelease` (tagged releases). Output APKs are at `app/build/outputs/apk/{debug,release}/`. The legacy fieldmind-* APK naming, the keystore + env-var pipeline, and the abi-split custom-renaming are NOT carried over. Release-key signing is **deferred to Phase 4+** when Curio's distribution-channel logic lands — until then, AGP's default debug-key fallback makes the release APK installable for PR previews.
- All placeholder screens route correctly: tapping the Home hero with no chip → `PICKER`; with a chip → `spin/{slug}`; bottom-nav switching preserves each tab's back stack; back arrow pops the current route.

## Child DOX Index

- [`CURIO_SPEC.md`](CURIO_SPEC.md) — Canonical **UX/UI** spec for Curio (per `master.md` Update After Editing rule: changes affecting screen design, navigation, or component behavior go in this doc, not buried in code comments).
- [`CURIO_DATA_PLAN.md`](CURIO_DATA_PLAN.md) — Canonical **data layer** spec (companion to CURIO_SPEC.md). Owns: category taxonomy expansion (6 → 10), `CurioTopic` + `ExploreAction` schema, JSON-on-disk canonical format, Room DB seed flow, image strategy (URL + Coil, no bundling), authoring pipeline (LLM-draft + human-review + smoke test), per-category rollout cadence (one category per PR, Music first). Read this BEFORE adding any topic data, category entry, or capture-format prompt.
- [`src/main/assets/topics/SCHEMA.md`](src/main/assets/topics/SCHEMA.md) — Quick-reference schema doc for topic JSON files. Lives next to `music.json` so authors have the schema at their fingertips without opening the larger `CURIO_DATA_PLAN.md`. Points back to the full source-of-truth for anything not covered.
- (Future) `app/src/main/java/com/curio/app/features/{home,spin,cabinet,capture}/AGENTS.md` — per-screen feature contracts, added when each screen gets real implementation in Phase 3+.
- (Future) `app/src/main/java/com/curio/app/ui/theme/AGENTS.md` — design system primitive contracts, added when the theme system grows (Phase 3+ when dark-mode polish, motion tokens, etc. land).
- (Future) `app/src/main/java/com/curio/app/data/AGENTS.md` — data-model contracts, added when Room + repositories land in Phase 4.
