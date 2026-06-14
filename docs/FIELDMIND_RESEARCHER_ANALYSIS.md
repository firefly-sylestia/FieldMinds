# FieldMind — Field-Researcher Analysis & Improvement Plan

> Written from the perspective of a working field scientist who actually carries this app into
> the field. This is a code-grounded audit (not guesswork): every observation below maps to a
> real file in `app/src/main/java/chromahub/rhythm/app/features/field/`. It supersedes the older
> redesign/phase/PR markdown files, which have been removed.

**Date:** 2026-06-14
**Scope reviewed:** navigation, Today/Home, Capture, Workspace/Projects, Library, Insights, Map,
Export Studio, Settings, Detail/view screens, weather service, data model, animation patterns.

---

## 1. Executive summary (the scientist's verdict)

FieldMind is genuinely ambitious and already covers an impressive surface: observations with
evidence, projects, questions, hypotheses, sources, data records, reports, flashcards/spaced
repetition, research sessions, weather capture, GPS, backups, and a local/cloud AI assistant.
The *data model* is strong and research-literate.

But as a daily field tool it suffers from three systemic problems:

1. **Navigation bloat and duplication.** Many "destinations" are the same screen with a different
   start tab. This makes the app feel bigger and more confusing than it is, and dilutes the core
   loop (Capture → Workspace → Insight → Export).
2. **The map is a second-class citizen.** It is embedded inside a vertically-scrolling list, which
   fights the map's own pan/zoom gestures. Markers carry no real information (just lat/long), there
   is no clustering, no layers, no timeline, and no proper full-screen detail view.
3. **The "feel" is inconsistent.** Some screens have lovely spring animations (the nav bar), but
   evidence/view/weather surfaces are static, and the weather card is a flat gradient box rather
   than something alive and condition-aware. Micro-interactions are missing in the places a user
   touches most (saving an observation, attaching evidence, opening a record).

Fix these three and FieldMind moves from "feature-rich prototype" to "trusted instrument."

---

## 2. Critical bugs & broken things (fix first)

| # | Severity | Where | Problem | Fix |
|---|----------|-------|---------|-----|
| 1 | 🔴 Crash (FIXED) | `FieldMindHomeScreen.kt` weather widget | `Brush.horizontalGradient(listOf(singleColor))` crashed with *"colors must have length of at least 2"* right after onboarding while weather was still loading. | Already patched in branch `fix/onboarding-gradient-crash` (added a 2nd color stop). |
| 2 | 🔴 Gesture conflict | `FieldMindMapScreen.kt`, `OsmMap` in `FieldMindCharts.kt` | The OSM `MapView` is placed **inside a `LazyColumn`**. Vertical scroll and map pan/pinch compete; users "swipe the page instead of the map." This is the exact complaint about the map being "swipe with the map view." | Make the map a true full-screen destination (see §5). Never nest an interactive map in a scrolling list; if a preview is needed in a list, render a static snapshot/thumbnail that opens the full map on tap. |
| 3 | 🟠 Information-poor markers | `OsmMap` | Every marker uses the stock Android `ic_menu_mylocation` drawable and a title of `"%.5f, %.5f"`. No subject, category, photo, date, or confidence. | Custom marker per category + a marker-tap bottom sheet with the observation preview and "Open" action. |
| 4 | 🟠 No marker clustering | `OsmMap` | With dozens/hundreds of GPS observations the map becomes an unreadable pile of identical pins. | Add `osmdroid`'s `RadiusMarkerClusterer` (or migrate to MapLibre, §5). |
| 5 | 🟡 Weather is home-only & flat | `FieldMindHomeScreen.kt` | Weather is fetched only on the Today screen and rendered as a static gradient box; conditions/temperature don't animate. The weather snapshot model exists per-observation but isn't celebrated in the view. | Condition-aware animated weather hero (see §6). |
| 6 | 🟡 Silent weather failures | `WeatherApiService.fetchWeather` | Returns `null` on any error with no differentiation (offline vs. denied location vs. API error). The UI just says "Weather unavailable." | Return a typed result (Offline / NoLocation / ApiError / Ok) so the UI can guide the user. |
| 7 | 🟡 PDF/Reader limitations | `FieldMindLibraryScreen.kt` | `content://` PDFs can't render in WebView; falls back to "open externally." Acceptable, but the in-app reader is fragile. | Use a real PDF renderer (`PdfRenderer`) for local files. |

---

## 3. Redundant / overlapping pages (delete or merge)

The `FieldMindScreen` sealed class declares **30+ destinations**, but the `NavHost` wires several of
them to the *same composable*:

```
Hypotheses   -> ProjectsScreen(startTab = 2)
DataTools    -> ProjectsScreen(startTab = 2)
Analysis     -> ProjectsScreen(startTab = 2)
Reports      -> ProjectsScreen(startTab = 2)   // four routes, one screen, identical tab
Learn        -> KnowledgeLibraryScreen(startTab = 3)   // duplicate of Library
FieldMode    -> ObserveScreen(compactFieldMode = true) // a mode, not a page
Search       -> ArchiveScreen                          // fine, but overlaps Library search
```

**Problems this creates for a researcher:**
- "Analysis," "Data," "Reports," and "Hypotheses" all dump me on the **same Workspace tab**, so I
  can never tell where I actually am or build muscle memory.
- "Learn" and "Library" are the same screen — two doors to one room.
- Insights exists both as a **bottom-tab** *and* is reachable from the Map ("Open Insights"), and a
  mini-map lives inside Insights while a full Map tab also exists. The Map ↔ Insights boundary is
  blurry.

**Recommendation — collapse to 5 primary tabs + clearly-scoped sub-areas:**

| Keep as primary tab | Role |
|---|---|
| **Today** | Dashboard, live weather, streak, quick capture entry, "continue session." |
| **Capture** | The evidence-first observation flow (already good bones). |
| **Workspace** | Projects + Questions + Hypotheses + Data + Reports as *real tabs within one screen* (not 4 fake routes). |
| **Library** | Sources + Learn + Flashcards + Archive/Search (merge "Learn"). |
| **Map** | Promoted to a first-class spatial view (see §5). |

Move **Insights** out of the bottom bar and into Today (as a "View analytics" card) or into
Workspace, since it is analytical, not a daily destination. Delete the standalone `Learn`,
`DataTools`, `Analysis`, `Reports`, `Hypotheses` *routes* and replace them with deep-links to the
correct Workspace/Library tab so existing entry points keep working.

---

## 4. Per-screen findings & UI suggestions

### 4.1 Today / Home (`FieldMindHomeScreen.kt`)
- **Good:** weather gradient idea, live pulse dot, progress toward daily goal.
- **Missing for research:** "yesterday vs today" delta, nearest active project, last session
  resume, sunrise/sunset & golden-hour (critical for wildlife/botany), moon phase, and a
  "conditions are good/poor for fieldwork" nudge.
- **UI:** the weather hero should be the animated centerpiece (§6), with the rest of the dashboard
  in calm cards beneath it.

### 4.2 Capture / Observe (`FieldMindObserveScreen.kt`)
- **Good:** genuinely well thought-out — evidence-first, live timer, structured fields,
  multi-select categories, confidence levels.
- **Missing:**
  - **Quantitative measurements** (count, size, sex, life-stage, abundance scale) as first-class
    fields, not free text — researchers need structured, exportable numbers.
  - **Taxon/species field** with optional offline checklist autocomplete.
  - **Voice-to-text** for hands-free field notes (mic exists for audio attach, but no transcription).
  - **Templated protocols** (point count, transect, quadrat) that pre-fill the form.
  - **Re-observation linking** ("same individual/site as…") for longitudinal studies.
- **UI:** add bouncy save confirmation, evidence thumbnails that pop in, and an explicit
  "GPS acquired / accuracy ±Xm" indicator so the user trusts the coordinate.

### 4.3 Workspace / Projects (`FieldMindProjectsScreen.kt`, `ProjectPhase5Components.kt`)
- Consolidate the four duplicate routes here into clear in-screen tabs:
  **Overview · Observations · Hypotheses · Data · Reports.**
- Add a **project dashboard**: observation count over time, sites covered, evidence completeness,
  open questions, hypothesis support status.
- Add **sampling-effort tracking** (hours in field, sessions) per project — essential for any real
  study and for honest reporting.

### 4.4 Evidence Hub & view pages (`EvidenceHubPhase6.kt`, `FieldMindDetailScreen.kt`)
- The detail/view screens are **functional but text-heavy** (`DetailRow` label/value lists). For a
  scientist reviewing evidence, this reads like a database dump, not a record.
- **Improve the observation view specifically:**
  - Hero media carousel (photos/audio) at top, swipeable, full-bleed.
  - A compact **map snapshot** of the single point (tap → full map) instead of raw lat/long text.
  - The **weather snapshot** rendered as a chip row with icons (it's captured but barely shown).
  - **Backlinks panel** (already exists) is great — surface it higher; it's the knowledge-graph
    payoff.
  - EXIF/time/accuracy metadata in a collapsible "provenance" section to support data integrity.
- Evidence Hub: add bulk-tagging, "missing evidence" filter, and a gallery/grid mode.

### 4.5 Insights (`InsightsScreen.kt`, `FieldMindCharts*.kt`)
- Strong start (charts + mini-map + knowledge graph). Decide its home (see §3) so it isn't both a
  tab and a sub-page.
- Add research-grade analytics: **species accumulation curve**, observations-per-effort,
  temporal/diel activity histograms, and weather-vs-activity correlation (you already store both —
  this is a high-value, low-cost win).

### 4.6 Settings (`FieldMindSettingsScreen.kt`)
- **Good:** clean nav-card hub, sensible grouping (Profile, Appearance, Capture, AI, Local model,
  Backup, Security, Export, About), offline-first messaging.
- **Suggestions:**
  - Add **Units & format** (metric/imperial, coordinate format DD/DMS/UTM/MGRS, date format) — field
    scientists are particular about this and it affects export.
  - Add **Map settings** (default base layer, offline tile downloads, default zoom).
  - Add a **Data integrity / about-my-data** page (record counts, storage used, last backup) — turn
    the existing "Reset onboarding" debug button into a proper Developer/Advanced section.
  - Surface **field-mode defaults** (screen-on, high-contrast, large touch targets, haptics).

### 4.7 Export Studio (`FieldMindBackupExportScreen.kt`, `FieldMindExport.kt`)
- **Good:** scoped export (All/Projects/Observations/Sources/Reports), formats CSV, JSON, HTML→PDF,
  Markdown, SVG/PNG dashboard, auto-backup worker, archive import with preview.
- **Missing for science (important):**
  - **GeoJSON / KML export** of GPS observations — the single most-requested format for spatial data
    and the obvious pairing with the map upgrade. (You already have lat/long; this is cheap.)
  - **Darwin Core Archive (DwC-A)** export for biodiversity records (GBIF/iNaturalist
    interoperability) — this is what makes the app credible to institutions.
  - **Citations**: BibTeX / RIS export for the Sources you already model.
  - **Per-project export bundle** (PDF report + CSV + media folder + GeoJSON in one zip).
  - **Field-data CSV with the structured measurement columns** suggested in §4.2.
- **UI:** the export screen is a long config list; add a **format gallery** (cards with icons +
  one-line "best for…") and a live **preview/row-count** before exporting.

---

## 5. Map: a proper new map system (the big one)

The current map (`OsmMap` in `FieldMindCharts.kt`, used by `FieldMindMapScreen.kt`,
`InsightsScreen.kt`, `FieldMindDialogs.kt`) is an `osmdroid` `MapView` dropped into a `LazyColumn`
with stock pins. Recommended redesign:

**Architecture**
- Promote Map to a **full-screen, non-scrolling destination** (a `Box`, never inside `LazyColumn`).
  The list of "recent tagged observations" becomes a **bottom sheet** that slides over the map, so
  the map always owns the full gesture surface.
- Consider migrating from `osmdroid` to **MapLibre GL (org.maplibre.gl)** for vector tiles, smooth
  GPU rendering, rotation/tilt, and offline-region downloads. If staying on osmdroid, at minimum add
  the clustering overlay.

**Detailed view & interaction (replacing the swipe-with-map experience)**
- **Marker bottom sheet:** tapping a marker raises a Material bottom sheet with the observation's
  photo, subject, category chip, date, weather chip, and an "Open record" button.
- **Category-colored, icon-bearing markers** (reuse `FieldMindTheme.colors` per category).
- **Clustering** with count badges; tapping a cluster zooms to fit.
- **Layers control:** base map (Streets/Satellite/Topo/Terrain), heatmap of observation density,
  and a per-project filter.
- **Timeline scrubber:** a bottom slider that filters markers by date range to *watch a study unfold
  over time* — extremely compelling for fieldwork.
- **My-location + "fit all" + "go to project area"** FAB cluster.
- **Offline tiles:** let users pre-download a region before going off-grid (core to the
  offline-first promise).
- **Measure tool:** distance/area between points for transects and plots.

**Where the map appears**
- Full Map tab = the experience above.
- Observation detail = a **static map thumbnail** (snapshot) that opens the full map centered on
  that point. No interactive map inside any scrolling screen.

---

## 6. Weather: bring it to life (animated, condition- & temperature-aware)

Today the weather card is a flat `Brush` gradient with a single static icon. Make it an animated
hero that reflects *actual* conditions from the WMO `weatherCode` and temperature already returned by
`WeatherApiService`.

**Animation system (Compose-native, no heavy deps required):**
- A reusable `AnimatedWeatherScene(weatherCode, temperature, isDay)` composable driving a
  `Canvas` + `rememberInfiniteTransition`:
  - **Clear (0–1):** sun with slow rotating rays / moon + drifting stars at night.
  - **Cloudy (2–3, 45–48):** layered clouds parallax-drifting at different speeds.
  - **Drizzle/Rain (51–67, 80–82):** falling rain streaks, density scaled to severity; ripples.
  - **Snow (71–77, 85–86):** drifting snowflakes with gentle sway.
  - **Thunderstorm (95–99):** periodic lightning flash + screen-edge glow.
  - **Fog (45–48):** translucent drifting fog bands.
- **Temperature drives the palette** (the existing freezing→hot gradient logic is a good seed): cool
  blues below 0°C → warm ambers/oranges above ~30°C, with smooth `animateColorAsState` transitions
  when conditions change.
- **Day/night** from sunrise/sunset (add to the Open-Meteo call) swaps sun↔moon and dims the scene.

**Assets:** prefer **vector/Canvas-drawn** elements (crisp at any size, themeable, tiny). If raster
is desired, generate high-quality PNG sprite sets per condition and animate with frame/`graphicsLayer`
transforms. Avoid emoji and avoid low-res clip-art.

**Placement:** animated scene as the Today hero, a compact looping version as the weather chip on the
observation detail and in the per-observation history, and a static frame in exports.

---

## 7. Motion & micro-interactions (make it smooth and "bouncy")

The nav bar already uses lovely springs (`DampingRatioMediumBouncy`). Extend that vocabulary
everywhere the user touches:

- **Standardize a motion spec** (e.g. `FieldMindMotion` object): one bouncy spring for selection/press,
  one gentle spring for layout, consistent 180–220ms tweens for fades. Use it app-wide.
- **Press feedback:** scale-down + haptic on all cards/buttons (haptics already exist via
  `rememberFieldMindHaptics`).
- **Save observation:** success spring + checkmark draw-on animation + subtle confetti/pulse; evidence
  thumbnails should **pop in** with `scaleIn + fadeIn`.
- **List items:** `animateItemPlacement()` so adding/removing observations animates instead of
  snapping.
- **Shared element transitions:** you already have `FieldMindSharedTransitions.kt` — use it for
  card → detail (photo expands into the hero) for a premium feel.
- **Skeleton loaders** with shimmer instead of blank space while weather/data loads.
- **Number roll-ups** on dashboard stats (animate 0 → value).
- Respect **reduce-motion** accessibility settings.

---

## 8. Missing features that would make me trust & adopt FieldMind

Ranked by research value:

1. **GeoJSON/KML + Darwin Core export** (interoperability with GIS, GBIF, iNaturalist).
2. **Structured measurements** (counts, sizes, scales) — turns prose into analyzable data.
3. **Map upgrade with clustering, layers, timeline, offline tiles** (§5).
4. **Sampling-effort & re-observation tracking** for longitudinal/quantitative rigor.
5. **Species/taxon field** with offline checklist autocomplete.
6. **Voice-to-text** field notes and **offline** capture guarantees end-to-end.
7. **Photo provenance** (EXIF, GPS accuracy, timestamp integrity) surfaced in the record.
8. **Collaborator/export-to-team** (even just shareable project bundles) for multi-observer studies.
9. **Citations export** (BibTeX/RIS) from the Sources module.
10. **Data-quality dashboard** (missing GPS, missing evidence, low-confidence flags).

---

## 9. Suggested execution order

**Phase A — Stabilize & de-clutter**
1. ✅ Onboarding weather-gradient crash (done).
2. Collapse duplicate nav routes to 5 tabs + Workspace/Library sub-tabs (§3).
3. Fix map gesture conflict by making Map full-screen with a bottom sheet (§5, first step).

**Phase B — Signature experiences**
4. Animated, condition-aware weather hero (§6).
5. New map system: clustered category markers, marker detail sheet, layers, "fit all" (§5).
6. App-wide motion spec + key micro-interactions (§7).

**Phase C — Research credibility**
7. Structured measurement fields in Capture (§4.2).
8. GeoJSON/KML + Darwin Core + BibTeX export (§4.7).
9. Research analytics in Insights (accumulation curve, effort, weather correlation) (§4.5).
10. Offline map tiles + sampling-effort tracking (§5, §4.3).

---

## 10. File reference map (for whoever implements this)

| Area | Primary file(s) |
|---|---|
| Navigation / routes | `presentation/navigation/FieldMindNavigation.kt` |
| Today / weather hero | `presentation/screens/FieldMindHomeScreen.kt` |
| Capture flow | `presentation/screens/FieldMindObserveScreen.kt` |
| Workspace / projects | `presentation/screens/FieldMindProjectsScreen.kt`, `components/ProjectPhase5Components.kt` |
| Evidence hub | `components/EvidenceHubPhase6.kt`, `components/DataWorkspacePhase7.kt` |
| Detail / view pages | `presentation/screens/FieldMindDetailScreen.kt` |
| Insights & charts | `presentation/screens/InsightsScreen.kt`, `components/FieldMindCharts.kt`, `components/FieldMindChartsExtended.kt` |
| Map | `presentation/screens/FieldMindMapScreen.kt`, `OsmMap` in `components/FieldMindCharts.kt` |
| Export & backup | `presentation/screens/FieldMindBackupExportScreen.kt`, `data/export/FieldMindExport.kt` |
| Settings | `presentation/screens/FieldMindSettingsScreen.kt`, `data/settings/FieldMindSettings.kt` |
| Weather data | `data/weather/WeatherApiService.kt` |
| Data model | `data/database/entity/FieldEntities.kt` |
| Motion / shared transitions | `components/FieldMindSharedTransitions.kt` |
| Theme & colors | `presentation/theme/FieldMindTheme.kt` |

---

*This analysis is intentionally implementation-agnostic on the smaller points and prescriptive on
the three systemic issues (navigation duplication, the map, and motion/weather feel) because those
are what stand between FieldMind and being a tool a scientist relies on every day.*
