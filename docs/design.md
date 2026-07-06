# FieldMind — App Architecture & Design

> **Last updated:** 2026-07-06
> **Project:** FieldMind — field research tool for scientists and citizen scientists

---

## 1. Project Overview

FieldMind is a single-module Android application for field research. It supports observations, species identification, hypotheses, evidence collection, offline mapping, weather tracking, AI-assisted research, flashcard-based learning, data analysis, and project management.

- **Language:** Kotlin 2.3.x
- **Minimum SDK:** 26 (Android 8.0)
- **Target SDK:** 37 (Android 15)
- **UI:** Jetpack Compose + Material3
- **Build system:** Gradle with version catalog (`gradle/libs.versions.toml`)
- **Distribution:** Two flavors — `fdroid` (F-Droid) and `github` (GitHub Releases)

---

## 2. Module Architecture

The project is a **single Android application module** (`:app`) with no library modules. All code lives under `app/src/main/java/fieldmind/research/app/`.

### 2.1 Layer Diagram

```
┌────────────────────────────────────────────────────────────────┐
│                       Android Application                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    Activities Layer                       │  │
│  │  MainActivity  │  FieldMindCrashActivity                 │  │
│  └────────────────────────┬─────────────────────────────────┘  │
│                            │                                    │
│  ┌────────────────────────▼─────────────────────────────────┐  │
│  │                 Presentation Layer                        │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌────────────────┐  │  │
│  │  │   Screens    │  │  Components  │  │  Navigation    │  │  │
│  │  │  (50+ Comp.) │  │  (Reusable)  │  │  (Nav Graph)   │  │  │
│  │  └──────────────┘  └──────────────┘  └────────────────┘  │  │
│  │                                                           │  │
│  │  ┌──────────────┐  ┌──────────────┐                      │  │
│  │  │  ViewModel   │  │   Theme      │                      │  │
│  │  │  (StateFlow) │  │  (Colors/    │                      │  │
│  │  │              │  │   Typography) │                      │  │
│  │  └──────────────┘  └──────────────┘                      │  │
│  └────────────────────────┬─────────────────────────────────┘  │
│                            │                                    │
│  ┌────────────────────────▼─────────────────────────────────┐  │
│  │                    Data Layer                              │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐   │  │
│  │  │   Room   │  │ Weather  │  │  Vision  │  │  AI     │   │  │
│  │  │  (Local) │  │ (7 Prov.)│  │ (Species)│  │(Gemini) │   │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └─────────┘   │  │
│  │                                                           │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐   │  │
│  │  │ Security │  │  Export  │  │ Location │  │  Stats  │   │  │
│  │  │ (Biomet.)│  │(Encrypt) │  │ (GPS/    │  │(Streaks)│   │  │
│  │  │          │  │          │  │  Maplibre││  │         │   │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └─────────┘   │  │
│  └────────────────────────┬─────────────────────────────────┘  │
│                            │                                    │
│  ┌────────────────────────▼─────────────────────────────────┐  │
│  │                Infrastructure Layer                       │  │
│  │  ┌──────────────────┐  ┌──────────────────────────────┐   │  │
│  │  │  WorkManager     │  │  Glance App Widgets           │   │  │
│  │  │  (Background     │  │  (Dashboard, Quick Capture)   │   │  │
│  │  │   Jobs)          │  │                              │   │  │
│  │  └──────────────────┘  └──────────────────────────────┘   │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────┘
```

### 2.2 Package Organization

```
fieldmind.research.app/
├── activities/              # Android activities
├── features/field/          # Core feature module
│   ├── data/                # Data layer
│   │   ├── ai/              # AI assistants (Gemini)
│   │   ├── analysis/        # Pattern detection
│   │   ├── attachment/      # File attachment management
│   │   ├── background/      # Background workers
│   │   ├── bulk/            # Bulk operations
│   │   ├── database/        # Room database (entities, DAOs)
│   │   ├── export/          # Export/import with encryption
│   │   ├── flashcard/       # SM-2 spaced repetition
│   │   ├── learn/           # Learning library content
│   │   ├── location/        # GPS, maps, geo-fencing
│   │   ├── question/        # Research question generator
│   │   ├── repository/      # Central data access (FieldMindRepository)
│   │   ├── security/        # Privacy manager, biometrics
│   │   ├── settings/        # App settings persistence
│   │   ├── stats/           # Streaks and statistics
│   │   ├── timer/           # Timer utilities
│   │   ├── undo/            # Undo/redo support
│   │   ├── vision/          # Species classification
│   │   └── weather/         # 7 weather providers
│   └── presentation/        # UI layer
│       ├── canvas/          # Infinite + page canvas (blocks)
│       ├── components/      # Reusable composables
│       ├── navigation/      # Navigation graph and routes
│       ├── screens/         # 50+ screen composables
│       ├── theme/           # FieldMind theme
│       ├── utils/           # Lifecycle manager
│       └── viewmodel/       # FieldMindViewModel
├── infrastructure/          # Background infrastructure
│   ├── widget/glance/       # App widgets
│   └── worker/              # WorkManager jobs
├── shared/                  # Shared code
│   ├── data/model/          # Shared data models
│   └── presentation/        # Base theme, icons
└── util/                    # Crash reporter, ANR watchdog
```

---

## 3. Technology Stack

### 3.1 Core

| Technology | Version | Purpose |
|-----------|---------|---------|
| Kotlin | 2.3.21 | Primary language |
| AGP | 9.2.1 | Android Gradle Plugin |
| Compose BOM | 2026.05.01 | Compose dependency management |
| Material3 | 1.5.0-alpha20 | Material Design 3 |
| KSP | 2.3.6 | Annotation processing |
| Min SDK | 26 | Android 8.0 |
| Target/Compile SDK | 37 | Android 15 |

### 3.2 Key Libraries

| Library | Purpose |
|---------|---------|
| Jetpack Compose | UI framework |
| Room (KSP) | Local database (SQLite ORM) |
| Navigation Compose | Screen routing |
| Coil | Async image loading |
| Retrofit + OkHttp | HTTP networking |
| Glance | App widgets |
| Media3 ExoPlayer | Audio/video playback |
| Maplibre GL | Offline OpenStreetMap maps |
| CameraX | In-app camera capture |
| Haze | Backdrop blur (glassmorphism) |
| osmdroid | Offline map tile management |
| Gson | JSON serialization |
| WorkManager | Background job scheduling |
| Coroutines + StateFlow | Async + state management |
| Gemini AI | AI-powered research assistant |
| ML Kit | On-device object detection |
| Biometric | Privacy/biometric lock |

### 3.3 No DI Framework

The project deliberately avoids a dependency injection framework. Dependencies are wired manually:
- `FieldMindRepository` is constructed in `FieldMindApplication` and passed to `MainActivity`
- `FieldMindViewModel` receives the repository and settings via constructor
- Weather providers are registered in the repository manually

---

## 4. State Management

### 4.1 Pattern: ViewModel + MutableStateFlow

```kotlin
// In ViewModel
private val _observations = MutableStateFlow<List<ObservationEntity>>(emptyList())
val observations: StateFlow<List<ObservationEntity>> = _observations.asStateFlow()

// In Composable
val observations by viewModel.observations.collectAsState()
```

- All UI state flows through `FieldMindViewModel` (central ViewModel)
- No per-screen ViewModels except `CanvasViewModel` (due to complexity)
- State is exposed as `StateFlow` and collected via `collectAsState()`

### 4.2 UI State Pattern

Screens use sealed classes for multi-state rendering:

```kotlin
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

### 4.3 Settings

App settings use a custom `SharedPreferences` wrapper (`FieldMindSettings`) with reactive `StateFlow` properties for each setting. Settings are organized by category (appearance, weather, security, etc.) in `AppSettings.kt`.

---

## 5. Navigation Architecture

### 5.1 Route Structure

Navigation is centralized in `FieldMindNavigation.kt` using Jetpack Navigation Compose.

```
FieldMindScreens (sealed class hierarchy)
├── Tab screens
│   ├── Home
│   ├── Observe
│   ├── Library (Knowledge Hub)
│   ├── Projects
│   └── Settings
├── Detail screens
│   ├── ObservationDetail
│   ├── SourceDetail
│   ├── NoteDetail
│   ├── FlashcardDetail
│   └── ProjectDetail
├── Tool screens
│   ├── WeatherDatabase
│   ├── SpeciesBrowser / SpeciesDetail
│   ├── TaxonomicBrowser
│   ├── MapScreen
│   ├── Canvas (Infinite / Page)
│   ├── Flashcards (Review mode)
│   ├── BackupExport
│   ├── Collaboration
│   ├── DataTools / MediaGallery / Bibliography
│   └── LearnHub
├── System screens
│   ├── LockScreen
│   ├── Onboarding
│   ├── Changelog
│   └── DevFullAppTestRunner
```

### 5.2 Navigation Principles

- Single `NavHost` with all routes registered
- `SharedTransitionScope` enabled for shared element transitions
- Swipe-back gesture via `SwipeBackHost` (wraps content)
- Each screen receives `onNavigate: (FieldMindScreen) -> Unit` lambda
- `BackHandler` for hardware back button handling

---

## 6. Theme & Styling System

### 6.1 Theme Architecture

```
shared/presentation/theme/
├── Color.kt          # Color constants per scheme
├── Theme.kt          # M3 theme composition
├── Type.kt           # Typography scale (Geom font)
├── Shape.kt          # Shape tokens
├── Dimensions.kt     # Spacing/sizing tokens

features/field/presentation/theme/
├── FieldMindTheme.kt # Entity accent colors, runtime lookups

shared/presentation/theme/
├── CuteThemeConfig.kt # Elevation tiers, shadows, card presets
├── FestiveTheme.kt    # Seasonal overlays
```

### 6.2 Color Schemes

10 color schemes available: Default, Pastel, Warm, Cool, Forest, Rose, Lavender, Mint, Ocean, Monochrome.

Each scheme defines:
- Primary/secondary/tertiary Material3 palette
- Entity accent colors (12 research entity types)
- Confidence colors (3 levels)
- Chart colors (10 categorical)
- Dark mode variants

### 6.3 Elevation System

```kotlin
object CuteElevations {
    val nonClickableTier = 4.dp   // InfoCard default
    val clickableTier = 6.dp      // ClickableCard default
    val plushTier1 = 2.dp         // Background surfaces
    val plushTier2 = 4.dp         // Standard cards
    val plushTier3 = 6.dp         // Featured cards
    val plushTier4 = 8.dp         // Dialogs, bottom sheets
    val plushTier5 = 12.dp        // Modals, overlays
}
```

### 6.4 Card Composables

The project defines custom card composables that wrap `Surface` to properly support `tonalElevation` and `shadowElevation`:

| Composable | Clickable | Elevation | Use Case |
|-----------|-----------|-----------|----------|
| `ClickableCard` | ✅ Yes | 6dp | Interactive list items |
| `InfoCard` | ❌ No | 4dp | Information display |
| `EntityCard` | ✅ Via param | 4dp | Entity list items |
| `GlassCard` | ❌ No | 0dp | Glassmorphism overlays |
| `MetricTile` | ✅ Via param | 4dp | Stat/metric displays |

> **Important:** Raw Material3 `Card` does NOT support `tonalElevation`/`shadowElevation` as named parameters (those are `Surface`-only). Always use the custom composables above when you need to control elevation.

---

## 7. Data Layer

### 7.1 Room Database

- **Entities:** ObservationEntity, ProjectEntity, QuestionEntity, HypothesisEntity, SourceEntity, NoteEntity, DataRecordEntity, ReportEntity, FlashcardEntity, SpeciesEntity, EvidenceAttachmentEntity, ResearchSessionEntity, TaskEntity, TagEntity, CanvasBlockEntity, CrossRef tables
- **DAO pattern:** One DAO per entity group
- **Schema exports:** Committed for migration history
- **Relations:** Room `@Relation` for one-to-many/many-to-many

### 7.2 Weather Providers

7 interchangeable providers implementing a common `WeatherProvider` interface:

| Provider | API Key Required | Region |
|----------|-----------------|--------|
| Open-Meteo (free) | No | Global |
| Open-Meteo (commercial) | Yes | Global |
| OpenWeatherMap | Yes | Global |
| Met Norway | No | Nordic |
| WeatherAPI | Yes | Global |
| IMD | No | India |
| NWS | No | USA |

Provider selection is configurable in Settings.

### 7.3 Species Classification

- `SpeciesDatabase` — Singleton-backed SQLite database of species catalog (~3000 species)
- Classification via: ML Kit object detection, pHash-based image matching, Perenual API
- Taxonomy data stored as JSON in `assets/species/species_catalog.json`

### 7.4 Export Pipeline

Two export paths:
1. **JSON archive** — Plain JSON of all entities (no media); used for Share/Save
2. **`.fieldmind` package** — Encrypted archive with media files; used for Backup/Auto-backup

Both use the same `archiveJson()`/`parseArchiveJson()` core (covering all entity fields since Phase A-C fixes).

---

## 8. Build System

### 8.1 Version Catalog

Single `gradle/libs.versions.toml` controls all dependency versions:
- `[versions]` — Version numbers
- `[libraries]` — Library coordinates
- `[plugins]` — Plugin declarations

Naming convention: `{group}-{artifact}` with dashes replacing dots.

### 8.2 Versioning

- **versionName:** From latest Git tag (e.g., `v0.43.0` → `0.43.0`)
- **versionCode:** From `git rev-list --count HEAD`
- Fallback: `1.0.0` / `1`

### 8.3 Build Variants

Two product flavors × two build types:
- `fdroidDebug`, `fdroidRelease`
- `githubDebug`, `githubRelease`

Release builds: R8 minified + shrunk, signed (CI or local keystore).
ABI splits: arm64-v8a, armeabi-v7a, x86, x86_64.

---

## 9. CI/CD Pipeline

**GitHub Actions** (.github/workflows/):

### `android.yml` — PR/Branch CI
- Triggers: push/PR on `main`
- Jobs: `check` (lint) + `build` (assemble debug + release APKs)
- Outputs signed universal APKs (14-day retention)

### `release.yml` — Release Build
- Triggers: tag push matching `v*`
- Builds both flavor releases
- Creates GitHub Release with generated notes

**Secrets:** KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD

---

## 10. Key Design Patterns

### 10.1 Central ViewModel

`FieldMindViewModel` is the single ViewModel for all screen state. This simplifies state sharing between screens (e.g., creating an observation in one tab and seeing it update in another) but means the ViewModel is large (~2000+ lines).

### 10.2 Manual Constructor Injection

No DI framework. Dependencies are created in `FieldMindApplication` and passed down:

```kotlin
class FieldMindApplication : Application() {
    lateinit var repository: FieldMindRepository
        private set
    
    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        repository = FieldMindRepository(database, weatherProviders, ...)
    }
}
```

### 10.3 Compile-Safety Rules

From `docs/CI_ERROR_POSTMORTEM.md` — critical rules for AI agents:

1. **Read before writing** — Always read data class definitions before constructing
2. **Check Compose BOM** — Verify API exists in project's version
3. **Non-composable lambdas** — `BackHandler`, `onClick`, etc. are not `@Composable` contexts
4. **No sed for Kotlin** — Use `str_replace` not `sed`
5. **Check imports** — Don't remove still-used imports
6. **Modifier order** — Press-detection before click-consumption
7. **Canvas params** — Never name a param `size` in Canvas scope
8. **Composable is a function** — Not a property getter
9. **Card vs Surface params** — `tonalElevation`/`shadowElevation` are Surface-only; use `InfoCard`/`ClickableCard`
10. **Verify one-cycle** — Wait for CI before declaring fix complete

### 10.4 Compose Architecture Principles

- **Staggered entrance animations** — Items fade+slide up with cascading delays via `staggeredEntrance()` modifier
- **Expressive press feedback** — Cards lift and scale on press via `expressiveCardPress()` modifier
- **Spring animations preferred** — Spring over tween for organic feel (damping ~0.94, stiffness varies)
- **Reduce-motion aware** — All animations respect system accessibility setting
- **Consistent card rounding** — 32-34dp for cards, 28dp for section headers, 999dp (pill) for chips

---

## 11. Security Architecture

### 11.1 App Lock

- In-app PIN (4/5/6 digits) using custom numpad (not device keyboard)
- Biometric authentication (BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
- Failed attempt policy: 5 attempts → cooldown (configurable duration)
- Panic lock: optionally wipes app data on excessive failures
- Decoy PIN: opens clean empty version of the app
- Auto-lock: configurable timeout on app backgrounding

### 11.2 Data Protection

- Export encryption via `FieldMindExportEncryption` (AES-based)
- Screen capture prevention when privacy lock is active
- Clipboard security: clears clipboard after timeout

---

## 12. Accessibility & Internationalization

- English-only locale (saves APK size)
- System reduce-motion respected for all animations
- Content descriptions on interactive icons
- Compose accessibility semantics on custom components
- Font scaling respects system font size

---

## 13. Performance Considerations

- **Canvas drag latency:** Currently ~32ms (Room round-trip per frame); target is <16ms via local state overrides
- **Card entrance animations:** Staggered delays and spring animations run on Compose animation thread
- **ABI splits:** Device downloads only relevant native code
- **Locale filters:** Only English locale included (~5-8MB APK size saving)
- **LeakCanary:** Active in debug builds only

---

## 14. Documentation Structure

```
/ (project root)
├── AGENTS.md              # Root DOX rail — project-wide rules
├── master.md              # DOX framework definition
├── Prompt.md              # Running analysis log
├── docs/                  # Planning & design docs
│   ├── design.md          # This file — full architecture
│   ├── CI_ERROR_POSTMORTEM.md     # CI error catalog
│   ├── QA_ANALYSIS_IMPLEMENTATION_STATUS.md  # QA audit
│   ├── CUTIFYING_THE_APP_PLAN.md  # Beautification plan
│   ├── CANVAS_AUDIT_AND_PLAN.md   # Canvas audit
│   ├── ExportPipelineAudit.md     # Export pipeline analysis
│   └── ...                       # Other planning docs
├── wiki/                  # User-facing documentation
├── fastlane/              # Store metadata
└── .github/               # CI/CD, issue templates
```

---

## 15. Evolution & Future Directions

- **Canvas improvements:** Local state overrides for smooth drag, alignment guides, multi-block selection, block grouping
- **Export completeness:** Fix auto-backup worker to include all entity types, add cross-ref tables
- **Offline-first:** Continued Maplibre offline map improvements
- **AI integration:** Deeper research assistant integration (Gemini/OpenAI)
- **Species ID:** More on-device classification models
- **Widgets:** Expand Glance widget capabilities
