# FieldMind UX Consistency & Product Flow Analysis

**Analysis date:** 2026-07-06
**Scope:** 22 issues from static codebase inspection — verified against actual source files, with researched fix recommendations.

---

## Issue 1 — Weather not saved into observation records

**STATUS: ✅ CONFIRMED**

**Evidence:**
- `FieldMindObserveScreen.kt` fetches weather into `weatherSnapshot` (`CaptureSessionState`) and displays it via `AutoMetadataStatusCard`
- `saveObservation()` calls `viewModel.addObservation(...)` WITHOUT the `weather` parameter — the call signature shows no `weather:` argument
- `FieldMindViewModel.addObservation(...)` **does** support `weather: WeatherSnapshot?` and correctly maps it into `ObservationEntity.weatherTemperature`, `weatherCondition`, `weatherHumidity`, weatherWindSpeed`, `weatherPressure`, `weatherDescription`, `weatherSnapshotAt`

**Root cause:** The caller in `ObserveScreen.saveObservation()` never passes the captured `weatherSnapshot` to `addObservation()`.

**Fix:** Pass `weather = weatherSnapshot` in the `saveObservation()` call to `viewModel.addObservation(...)`.

---

## Issue 2 — Observation capture session state is not persisted after app death

**STATUS: ✅ CONFIRMED — PARTIAL**

**Evidence:**
- `FieldMindHomeScreen.kt` detects active `ResearchSessionEntity` with `status == "Active"` and shows "Live Session Active" card
- `FieldMindObserveScreen.kt` holds state in `CaptureSessionState` which IS wrapped in `rememberSaveable` (Parcelable)
- However, `activeSessionId`, `capturedLocation`, `weatherSnapshot`, and `showEvidenceForm` are NOT in `rememberSaveable` — they use plain `mutableStateOf`
- `viewModel.captureSessionActive` is purely in-memory (`mutableStateOf`) — not persisted
- After process death, `rememberSaveable` fields may survive if Bundle can hold them, but `activeSessionId` (a Long in `mutableStateOf`) will be lost
- The timer state (`timerStartedAt`, `timerAccumulatedMs`) IS in the Parcelable `CaptureSessionState` = survives config changes

**Fix:** Move `activeSessionId`, `capturedLocation` (needs to be Parcelable), and `weatherSnapshot` (needs to be Parcelable) into `CaptureSessionState` OR persist session metadata in a lightweight Room table. For process death survival, use SavedStateHandle in ViewModel.

---

## Issue 3 — Data tools all route to one generic detail screen

**STATUS: ✅ CONFIRMED**

**Evidence:**
- `DataToolsHubScreen` navigates via `onOpenDetail("data", record.id)`
- `FieldMindDetailScreen.kt` renders `DataRecordDetailContent(d)` for all `kind == "data"` — a single generic card showing label, tool type, value/unit, location, notes
- No differentiation between Counter, Measurement, Weather Log, Species, Checklist, Event Log, Site Log, or Comparison records
- The underlying tool-specific screens (`CounterToolScreen`, `MeasurementToolScreen`, `WeatherLogToolScreen`, etc.) build rich structured data, but detail display loses all structure

**Fix:** Create tool-specific detail composables dispatched by `DataRecordEntity.toolType`:
- `Counter` → show count value, timestamp, label
- `Measurement` → show value + unit + notes in structured card
- `Weather Log` → show temperature, condition, humidity, wind as weather card
- `Checklist` → render checked/unchecked items from value string
- `Comparison` → parse and render table

---

## Issue 4 — Weather Log data tool lacks auto-fetch

**STATUS: ✅ CONFIRMED**

**Evidence:**
- `WeatherLogToolScreen` in `FieldMindDataTools.kt` has manual temperature, condition, humidity, wind fields
- `FieldMindViewModel` has `fetchWeatherSnapshot()` and `refreshWeatherFromLocation()` methods
- Observation capture (`ObserveScreen`) has auto-fetch via `AutoMetadataStatusCard` but Weather Log tool does not
- No "Auto fetch" button exists in `WeatherLogToolScreen`

**Fix:** Add an "Auto Fetch" button to `WeatherLogToolScreen` that calls `viewModel.refreshWeatherFromLocation()` and populates the temperature/condition/humidity/wind fields from the returned snapshot.

---

## Issue 5 — Project task builder uses "Completed" but Tasks screen expects "Done"

**STATUS: ✅ CONFIRMED**

**Evidence:**
- `TasksScreen.kt` filters completed tasks using `t.status == "Done"`
- `TaskDetailScreen.kt` (in `FieldMindTaskDetailScreen.kt`) toggles between `"Done"` and `"Pending"`
- `ProjectTasksBuilder` (inside `FieldMindDetailScreen.kt`'s `ProjectDetailContent`) toggles `"Completed"`/`"Pending"` and checks `task.status == "Completed"`
- `TaskEntity.status` defaults to `"Pending"` per entity definition

**Fix:** Normalize all completion status checks to `"Done"` (the dominant value). Change `ProjectTasksBuilder` to use `"Done"` instead of `"Completed"`.

---

## Issue 6 — Project task cards lack visible delete button

**STATUS: ✅ CONFIRMED**

**Evidence:**
- `ProjectTasksBuilder` renders tasks with a completion toggle but no visible delete button
- Users cannot delete tasks directly from the project detail view without navigating elsewhere

**Fix:** Add a delete `IconButton` (trash icon) to each task row in `ProjectTasksBuilder`, calling `viewModel.deleteTask(task.id)` with a confirmation dialog.

---

## Issue 7 — Tasks screen should expose tick/delete buttons

**STATUS: ✅ CONFIRMED — PARTIAL**

**Evidence:**
- `TasksScreen.kt` uses `SwipeToCompleteTaskCard` and `TaskCard` — tapping a task navigates to detail
- No visible tick/delete buttons on task cards; completion is swipe-only or via detail screen
- `TaskDetailScreen.kt` has complete/delete in an overflow menu

**Fix:** Add explicit check-circle (complete) and delete IconButtons to `TaskCard` so users can complete/delete without swiping or navigating to detail.

---

## Issue 8 — Task detail has weak primary completion/delete and linking UX

**STATUS: ✅ CONFIRMED**

**Evidence:**
- `TaskDetailScreen.kt` has overflow menu actions for "Mark done" and "Delete" — not visible affordances
- No "Assign to" or observation/species/evidence linking UI
- `TaskEntity` supports `assignedTo`, `linkedObservationId`, `linkedSpeciesId`, `linkedEvidenceId`, `linkedQuestionId` but no UI to set these from detail

**Fix:**
1. Move "Complete"/"Delete" to prominent button row instead of overflow menu
2. Add "Assign to" text field
3. Add "Link observation", "Link species", "Link evidence" picker buttons that open entity picker dialogs

---

## Issue 9 — Project subtask parentTaskId remains null

**STATUS: ✅ CONFIRMED**

**Evidence:**
- In `ProjectTasksBuilder` within `FieldMindDetailScreen.kt`:
  ```
  var parentId: Long? = null
  // ... adds parent task ...
  viewModel.addTask(...) // async, no return value captured
  // Immediately uses parentId (still null) for subtasks
  ```
- `viewModel.addTask()` launches a coroutine and does not return the generated ID
- Subtasks get `parentTaskId = null` because `parentId` hasn't been set yet

**Fix:** Make `viewModel.addTask()` return the newly inserted task ID (via suspend function returning Long or a callback), then set `parentId` to the returned ID before creating subtasks.

---

## Issue 10 — Export buttons mislabeled (JSON copies text, no real files)

**STATUS: ✅ CONFIRMED**

**Evidence:**
- `ObservationDetailContent`'s `ObservationExportSection` has "Markdown", "CSV", "JSON", "Share" buttons
- All three (Markdown, CSV, JSON) buttons **copy to clipboard** — they do not generate files or share via intent
- The "JSON" button literally calls `clipboard.setText(AnnotatedString(jsonText))`
- No PDF or HTML export exists in the detail screen

**Fix:** Either:
- (a) Rename buttons to "Copy Markdown", "Copy CSV", "Copy JSON" with clear labels
- (b) Add real file generation + share intent for each format
- (c) Both: copy to clipboard + offer share intent as secondary action

---

## Issue 11 — Settings is flat and should be reorganized

**STATUS: ✅ ALREADY RESOLVED**

**Evidence:**
- `FieldMindSettingsScreen.kt` already has been redesigned as a settings hub with navigation cards:
  - "Research profile", "Appearance", "Capture defaults", "Weather", "Species tools", "AI assistant", "Local model", "Auto generation", "Backup & Restore", "Security", "Data integrity", "What's new", "About", "Developer options"
- Each navigates to a sub-page (e.g., `SecuritySettingsPage`, `AppearanceSettingsPage`, `AiAssistantSettingsPage`)
- The sub-pages exist and are well-structured with `SettingsSubPage` helper
- Search functionality is built in

**Conclusion:** Issue 11 is already fixed. No changes needed.

---

## Issue 12 — Inconsistent visual accents/theme usage

**STATUS: ✅ CONFIRMED — PARTIAL**

**Evidence:**
- `FieldMindTheme.kt` defines `FieldMindColors` with 12 entity-specific colors (observation, question, hypothesis, project, source, note, task, folder, species, data, report, flashcard)
- `TasksScreen.kt` uses `FieldMindTheme.colors.flashcard` for task accents — but there IS a dedicated `FieldMindTheme.colors.task` color (teal `#00897B` in light mode)
- `DataToolsHubScreen` uses a single `accentColor = FieldMindTheme.colors.data` for all 8 tool cards — no per-tool differentiation
- CuteElevations usage is inconsistent across components

**Visual consistency score break-down:**
- Tasks using `flashcard` instead of `task` color = **confirmed inconsistency**
- Data tools using single `data` accent = deliberate design choice but could be improved
- `Color.kt` in shared theme vs `FieldMindTheme.kt` = two theme files exist: `app/src/shared/presentation/theme/Color.kt` and `features/field/presentation/theme/FieldMindTheme.kt`. The `Color.kt` file is the older system, `FieldMindTheme.kt` is the newer one.

**Fix:**
1. Change `TasksScreen.kt` to use `FieldMindTheme.colors.task` instead of `.flashcard`
2. Optionally add per-tool accent colors to `DataToolsHub` tool definitions
3. Deprecate/funnel all color usage to `FieldMindTheme.colors`

---

## Issue 13 — Duplicate project detail implementations

**STATUS: ✅ CONFIRMED**

**Evidence:**
- `FieldMindProjectDetailScreen.kt` — standalone project detail screen with feed, tabs, create sheet
- `FieldMindDetailScreen.kt` — `ProjectDetailContent()` composable within the generic DetailScreen, also with tabs including Species and Tasks

**Fix:** Either:
- Route all project detail navigation to `FieldMindProjectDetailScreen.kt` (the richer standalone version)
- Or move `ProjectDetailContent` out of `DetailScreen` and have it delegate to the standalone version

---

## Issue 14 — Data record save flows lack project/observation linking

**STATUS: ✅ CONFIRMED — PARTIAL**

**Evidence:**
- `DataRecordEntity` supports `projectId` and `observationId`
- `FieldMindViewModel.addDataRecord()` accepts `projectId` and `observationId` params
- However, the tool-specific screens (`CounterToolScreen`, `MeasurementToolScreen`, `WeatherLogToolScreen`, etc.) do NOT offer project/observation pickers
- Only `NewDataRecordScreen` (full-screen creation) does not have entity linking UI

**Fix:** Add "Link to project" and "Link to observation" optional picker fields to each tool screen's save form.

---

## Issue 15 — Checklist tool doesn't preserve item checked state

**STATUS: ✅ CONFIRMED**

**Evidence:**
- `ChecklistToolScreen` stores checklist as a string value: `items.joinToString("; ") { (name, checked) -> "${if (checked) "✓" else "○"} $name" }`
- The checked state is encoded as text prefix (✓/○), not structured data
- `DataRecordDetailContent` cannot parse this back into individual items with checked status
- The task system's `checklistJson` is properly structured JSON — the data tool's checklist should follow the same pattern

**Fix:** Store checklist as structured JSON (like tasks do) with `[{"text": "...", "done": true/false}, ...]` and create a dedicated detail component that renders checked/unchecked items.

---

## Issue 16 — Comparison data tool needs table detail/edit experience

**STATUS: ✅ CONFIRMED**

**Evidence:**
- `ComparisonTableScreen` builds rows/columns but stores as flat string:
  ```
  summary = rows.joinToString("; ") { "${it.label}: ${it.items.joinToString(" vs ")}" }`
  ```
- `DataRecordDetailContent` shows this as plain text — no table rendering
- Cannot re-edit or re-view a saved comparison as a table

**Fix:** Store comparison data as structured JSON with rows/columns schema, create a `ComparisonDetailContent` that renders the table, and allow re-opening in the comparison editor.

---

## Issue 17 — Weather catalog vs Weather Log confusion

**STATUS: ✅ CONFIRMED — MINOR**

**Evidence:**
- `WeatherCatalogEntity` is the offline cache for fetched weather snapshots (from providers like Open-Meteo, OpenWeatherMap)
- `Weather Log` data tool creates `DataRecordEntity` with `toolType = "Weather Log"` — a user-entered weather record
- These are stored in different tables and used for different purposes, which is architecturally correct
- However, the UI doesn't clearly distinguish them: the Weather Log tool doesn't indicate it's creating a manual data record vs the observation weather which uses the catalog

**Fix:** Add clarifying labels/tooltips: "Weather Log creates a standalone weather data record" vs "Observation weather uses auto-fetched provider data."

---

## Issue 18 — Dead/empty menu actions

**STATUS: ✅ CONFIRMED**

**Evidence:**
- `TaskDetailScreen.kt` has overflow menu items like "Edit task" and "Duplicate" with `onClick = { showOverflow = false }` — they do nothing
- Other screens may have similar dead actions

**Fix:** Either implement the actions or remove them. For "coming soon" items, show a snackbar: `showFastSnackbar(snackbar, scope, "Coming soon")` instead of silently doing nothing.

---

## Issue 19 — Tasks without due dates hidden from Today/Upcoming

**STATUS: ✅ CONFIRMED**

**Evidence:**
- `TasksScreen.kt` filtering logic:
  - `todayTasks`: `t.dueDate == todayDate && t.status != "Done"`
  - `upcomingTasks`: `t.dueDate.isNotBlank() && t.dueDate != todayDate && t.status != "Done"`
  - `doneTasks`: `t.status == "Done" || completedTaskIds[t.id] == true`
- Tasks with blank `dueDate` and `status == "Pending"` don't match any section → invisible to users

**Fix:** Add an "Unscheduled" (or "Inbox") section for tasks where `dueDate.isBlank() && status != "Done"`. Place it between "Upcoming" and "Done".

---

## Issue 20 — "View all tasks" loses project context

**STATUS: ✅ CONFIRMED — ACCEPTABLE BEHAVIOR**

**Evidence:**
- `FieldMindProjectDetailScreen.kt` navigates to global `TasksScreen` via `onNavigate?.invoke(FieldMindScreen.Tasks)`
- This opens the unfiltered global task list, losing the project filter
- `FieldMindDetailScreen.kt`'s `ProjectDetailContent` does the same via `ProjectTasksBuilder`

**Fix:** Either:
- (a) Pass the project ID to the Tasks screen and filter by it (requires routing change)
- (b) Show "View all X tasks in this project" as an expandable inline list instead of navigating away

---

## Issue 21 — Data Tools summary only counts 3 of 8 tools

**STATUS: ✅ CONFIRMED**

**Evidence:**
- `DataToolsHubScreen` saved records summary:
  ```kotlin
  val counterCount = dataRecords.count { it.toolType == "Counter" }
  val measurementCount = dataRecords.count { it.toolType == "Measurement Log" }
  val weatherCount = dataRecords.count { it.toolType == "Weather Log" }
  ```
- Other 5 tool types (Species, Checklist, Event Log, Site Log, Comparison Table) are not counted
- The "View all" expandable section does show all records though

**Fix:** Add counts for all 8 tool types in the summary section, or show `dataRecords.size` total with per-tool breakdown.

---

## Issue 22 — Entity edit dialog is too generic for data records

**STATUS: ✅ CONFIRMED**

**Evidence:**
- `FieldMindDetailScreen.kt` opens `EditEntityDialog(kind, id, viewModel)` for `kind == "data"`
- `EditEntityDialog` is a generic dialog that can't handle weather-specific fields, checklist items, comparison tables, etc.
- Data records with complex structure lose all tool-specific meaning when edited generically

**Fix:** Route data record editing to the corresponding tool screen (e.g., for `toolType == "Checklist"`, open `ChecklistToolScreen` with the existing record pre-loaded). This requires adding a "re-edit" mode to each tool screen.

---

## Summary of Findings

| Issue | Status | Severity | Effort |
|-------|--------|----------|--------|
| #1 Weather not saved to observation | 🔴 Confirmed | High | Low (1 line) |
| #2 Session state not persisted after death | 🟡 Confirmed — Partial | Medium | High |
| #3 Generic data detail screen | 🔴 Confirmed | High | High |
| #4 Weather Log lacks auto-fetch | 🔴 Confirmed | Medium | Low |
| #5 "Completed" vs "Done" mismatch | 🔴 Confirmed | High | Low |
| #6 Project tasks lack delete button | 🔴 Confirmed | Medium | Low |
| #7 Tasks screen needs tick/delete buttons | 🟡 Confirmed — Partial | Medium | Medium |
| #8 Task detail weak linking/actions | 🔴 Confirmed | Medium | High |
| #9 Subtask parentTaskId null | 🔴 Confirmed | High | Medium |
| #10 Export buttons mislabeled | 🔴 Confirmed | Medium | Low |
| #11 Settings flat — Already redesigned | ✅ Already Fixed | N/A | N/A |
| #12 Inconsistent visual accents | 🟡 Confirmed — Partial | Medium | Medium |
| #13 Duplicate project detail screens | 🔴 Confirmed | Medium | High |
| #14 Data records lack entity linking | 🟡 Confirmed — Partial | Low | Low |
| #15 Checklist checked state not preserved | 🔴 Confirmed | Medium | Medium |
| #16 Comparison detail lacks table | 🔴 Confirmed | Medium | Medium |
| #17 Weather catalog vs Weather Log confusion | 🟡 Confirmed — Minor | Low | Low |
| #18 Dead menu actions | 🔴 Confirmed | Low | Low |
| #19 Unscheduled tasks hidden | 🔴 Confirmed | High | Low |
| #20 Project context lost on "View all tasks" | 🟡 Confirmed — Acceptable | Medium | Low |
| #21 Data tools summary counts only 3 types | 🔴 Confirmed | Low | Low |
| #22 Generic edit dialog for data records | 🔴 Confirmed | Medium | High |

**Legend:**
- 🔴 = Issue confirmed, fix needed
- 🟡 = Issue partially confirmed or acceptable
- ✅ = Already resolved
- Severity: Impact on UX/product quality
- Effort: Relative implementation difficulty

**Priority order (by impact):**
1. **P0 (Critical flow breaks):** #1, #5, #9, #19 — Users lose data or tasks are invisible
2. **P1 (Major UX gaps):** #3, #8, #13, #22 — Core flows are broken or duplicated
3. **P2 (Medium UX issues):** #2, #6, #7, #10, #12, #15, #16, #18
4. **P3 (Minor improvements):** #4, #14, #17, #20, #21
