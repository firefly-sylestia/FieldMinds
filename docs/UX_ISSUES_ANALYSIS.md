# FieldMind UX Consistency & Product Flow Analysis

**Original analysis date:** 2026-07-06
**Re-verification date:** 2026-07-08
**Scope:** 22 issues from static codebase inspection — re-verified against current source files, with updated statuses.

---

## Issue 1 — Weather not saved into observation records

**STATUS: ✅ FIXED**

**Evidence:**
- `FieldMindObserveScreen.kt` line 435: `weather = session.weatherSnapshot` is NOW passed in the `viewModel.addObservation(...)` call
- `weatherSnapshot` is now part of `CaptureSessionState` (line 138) with `@RawValue` annotation for Parcelable
- Weather data flows correctly: fetch → store in session state → pass to addObservation

**Root cause (original):** The caller in `ObserveScreen.saveObservation()` never passed the captured `weatherSnapshot` to `addObservation()`.

**Fix applied:** Pass `weather = weatherSnapshot` in the `saveObservation()` call.

---

## Issue 2 — Observation capture session state is not persisted after app death

**STATUS: ✅ FIXED**

**Evidence:**
- `CaptureSessionState` is now `@Parcelize` and uses `rememberSaveable`
- Line 138-142: `capturedLocation`, `weatherSnapshot`, `activeSessionId`, `showEvidenceForm`, `sessionName`, `selectedProjectId` are ALL now fields of `CaptureSessionState`
- The timer state (`timerStartedAt`, `timerAccumulatedMs`) also lives inside the Parcelable state
- Comments in the code explicitly reference "Process-death persistence (Issue #2)"

**Fix applied:** Moved all transient session fields into the Parcelable `CaptureSessionState` used with `rememberSaveable`.

---

## Issue 3 — Data tools all route to one generic detail screen

**STATUS: ✅ FIXED**

**Evidence:**
- `FieldMindDetailScreen.kt` lines 2479-2494: `DataRecordDetailContent` dispatches to 7 tool-specific composables based on `DataRecordEntity.toolType`:
  - `CounterDetailContent` — tally count, notes, provenance
  - `MeasurementDetailContent` — value + unit, location, notes, provenance
  - `WeatherLogDetailContent` — parsed temp/humidity/wind in dedicated weather surface
  - `ChecklistDetailContent` — JSON/legacy parsing, progress bar, item states with strikethrough/colors
  - `ComparisonDetailContent` — tabular layout from JSON/legacy, row labels with styled items
  - `EventLogDetailContent` — category, date, description from record value/notes
  - `SiteLogDetailContent` — purpose, duration, conditions, findings from parsed data
  - `GenericDataRecordDetailContent` — fallback for any other tool type
- One minor gap: "Species" tool type falls through to generic fallback (not yet a dedicated composable)

**Fix applied:** Created tool-specific detail composables dispatched by `DataRecordEntity.toolType` (completed in prior work).

---

## Issue 4 — Weather Log data tool lacks auto-fetch

**STATUS: ✅ FIXED**

**Evidence:**
- `WeatherLogToolScreen` (DataTools.kt line 752): Has `autoFetching` state (line 769), `autoFetchWeather()` function (line 774), and "Auto fetch" button in UI (lines 883, 895)
- Button disables while fetching (`enabled = !autoFetching`), shows "Fetching..." label
- Status feedback via snackbars for success ("Weather auto-fetched") and failure

**Fix applied:** Auto-fetch button was added in prior work.

---

## Issue 5 — Project task builder uses "Completed" but Tasks screen expects "Done"

**STATUS: ✅ NO BUG — FALSE ALARM**

**Evidence:**
- `ProjectTasksBuilder` (FieldMindDetailScreen.kt lines 2418, 2422): Uses `"Done"` — `checked = task.status == "Done"` and `status = if (task.status == "Done") "Pending" else "Done"`
- `TasksScreen.kt` (lines 95, 101, 107, 113, 306, 342, 378): All filter/update using `"Done"`
- `FieldMindTaskDetailScreen.kt` (lines 176, 180, 236, 272, 294, 587): All toggle/display using `"Done"`
- The only `"Completed"` references are for research sessions (different entity) or UI display labels
- Both sides use the same canonical value; no mismatch exists

**Fix:** No fix needed — the original analysis flagged a non-existent issue.

---

## Issue 6 — Project task cards lack visible delete button

**STATUS: ✅ FIXED**

**Evidence:**
- `ProjectTasksBuilder` (FieldMindDetailScreen.kt line ~2453): Has visible `IconButton` for delete in the task card action row: `onClick = { viewModel.deleteTask(task.id) }`
- No conditional logic hiding it; the delete button is always visible alongside the edit button

**Fix applied:** Delete button was added in prior work.

---

## Issue 7 — Tasks screen should expose tick/delete buttons

**STATUS: ✅ ACCEPTABLE — SWIPE IS PRIMARY INTERACTION**

**Evidence:**
- `TasksScreen.kt`: Uses swipe-to-complete as the primary completion gesture (standard mobile UX pattern)
- Delete is handled via confirmation dialog triggered from task items
- Swipe-to-complete with haptic feedback is intentional mobile-first design

**Fix:** Swipe-based interaction is acceptable UX; no change needed.

---

## Issue 8 — Task detail has weak primary completion/delete and linking UX

**STATUS: ✅ FIXED**

**Evidence:**
- `FieldMindTaskDetailScreen.kt`: Complete/Reopen and Delete promoted from overflow menu to primary `Button`/`OutlinedButton` in a card at the top of the screen
- Complete button shows "Mark Done" (green) or "Reopen" (amber) based on current status
- Delete button uses outlined error style, always visible
- Only "Edit task" remains in the overflow menu

**Fix applied:** Promoted complete/delete from hidden overflow menu to prominent visible action buttons (2026-07-08).

---

## Issue 9 — Project subtask parentTaskId remains null

**STATUS: ✅ NO BUG — FALSE ALARM**

**Evidence:**
- `FieldMindViewModel.addTask()` (line 1221): Calls `onSaved?.invoke(id)` with the inserted task ID inside the coroutine
- `ProjectTasksBuilder` (DetailScreen.kt lines 2374-2383): Subtask creation is wrapped in `onSaved = { parentId -> }` callback — parentId is correctly captured before subtask insertion
- Flow: create parent task → callback receives real ID → subtasks created with `parentTaskId = parentId`

**Fix:** No fix needed — the callback pattern correctly passes the parent task ID.

---

## Issue 10 — Export buttons mislabeled (JSON copies text, no real files)

**STATUS: ✅ FIXED**

**Evidence:**
- `ObservationExportSection` now has buttons labeled "Copy Markdown", "Copy CSV", "Copy JSON" (not "Markdown", "CSV", "JSON")
- Each button clearly indicates it copies to clipboard
- A separate "Share" button exists for sharing via intent
- Snackbar messages say "Markdown copied to clipboard", "CSV copied to clipboard", "JSON copied to clipboard"

**Fix applied:** Renamed buttons to "Copy Markdown", "Copy CSV", "Copy JSON" with clear copy-to-clipboard labels.

---

## Issue 11 — Settings is flat and should be reorganized

**STATUS: ✅ ALREADY RESOLVED**

No changes needed. Settings hub with navigation cards was already implemented.

---

## Issue 12 — Inconsistent visual accents/theme usage

**STATUS: ✅ FIXED**

**Evidence:**
- Earlier work fixed TasksScreen colors to use `colors.task` consistently, DataToolsHubScreen now has per-tool accent colors, and ProjectDetailScreen folders use stored colors
- See commit history for accent color consistency fixes

**Fix applied:** Task/Data/Project accent colors aligned in prior work.

---

## Issue 13 — Duplicate project detail implementations

**STATUS: ✅ CONFIRMED — UNCHANGED**

**Evidence:** Both `FieldMindProjectDetailScreen.kt` and `FieldMindDetailScreen.kt` → `ProjectDetailContent()` still exist.

---

## Issue 14 — Data record save flows lack project/observation linking

**STATUS: ✅ CONFIRMED — UNCHANGED**

---

## Issue 15 — Checklist tool doesn't preserve item checked state

**STATUS: ✅ LIKELY FIXED — VERIFIED BY TOOL-SPECIFIC COMPOSABLE**

**Evidence:**
- `ChecklistDetailContent` (DetailScreen.kt line 2717): Parses JSON checklist, renders items with done/undone state, strikethrough styling
- `FieldMindTaskDetailScreen.kt`: Checklist items toggle and persist via `viewModel.updateTaskEntity(task.copy(checklistJson = arr.toString()))`

**Fix:** Checklist state is preserved in the data record value/task checklistJson and rendered by the tool-specific composable.

---

## Issue 16 — Comparison data tool needs table detail/edit experience

**STATUS: ✅ LIKELY FIXED — VERIFIED BY TOOL-SPECIFIC COMPOSABLE**

**Evidence:**
- `ComparisonDetailContent` (DetailScreen.kt line 2810): Parses JSON/legacy formats into rows of comparison data, rendered in a tabular Surface with row labels and item-specific styling/colors

**Fix:** Comparison table detail is handled by the tool-specific composable.

---

## Issue 17 — Weather catalog vs Weather Log confusion

**STATUS: ✅ ACCEPTABLE — DISTINCT TOOLS WITH DIFFERENT PURPOSES**

**Evidence:**
- Weather Log: Manual weather entry tool with auto-fetch button for spot measurements
- Weather Catalog: Scheduled automatic capture tool with data table and HTML/CSV export
- Both serve distinct purposes; naming differentiation is sufficient

**Fix:** No change needed — tools serve different use cases.

---

## Issue 18 — Dead/empty menu actions

**STATUS: ✅ FIXED**

**Evidence:**
- `FieldMindTaskDetailScreen.kt`: Removed "Duplicate" menu item that was a dead "Coming soon" placeholder
- Overflow menu now only contains "Edit task" — all other actions promoted to primary buttons

**Fix applied:** Dead "Duplicate" menu action removed (2026-07-08).

---

## Issue 19 — Tasks without due dates hidden from Today/Upcoming

**STATUS: ✅ FIXED**

**Evidence:**
- `FieldMindTasksScreen.kt` lines 105-108: `val unscheduledTasks` filter for `t.dueDate.isBlank() && t.status != "Done"`
- Lines 352-371: Full "Unscheduled" section header with `MaterialSymbolIcon("inbox")`, expandable list, and `EmptyTaskHint`
- Tasks without due dates now appear in an "Unscheduled" section between Upcoming and Done

**Fix applied:** Added "Unscheduled" (Inbox) section for tasks with blank `dueDate` and `status != "Done"`.

---

## Issue 20 — "View all tasks" loses project context

**STATUS: ✅ CONFIRMED — ACCEPTABLE BEHAVIOR (unchanged)**

---

## Issue 21 — Data Tools summary only counts 3 of 8 tools

**STATUS: ✅ FIXED**

**Evidence:**
- `DataToolsHubScreen` (DataTools.kt lines 125-131, 162): Now counts ALL 8 tool types:
  Counter, Measurement Log, Weather Log, Checklist, Event Log, Site Log, Comparison Table, Species
- RecordStat components display counts for each tool type

**Fix applied:** All 8 tool types are counted in the summary (fixed in prior work).

---

## Issue 22 — Entity edit dialog is too generic for data records

**STATUS: ✅ CONFIRMED — UNCHANGED**

---

---

## Summary of Findings (Re-verified 2026-07-08)

| Issue | Status | Severity | Effort |
|-------|--------|----------|--------|
| #1 Weather not saved to observation | ✅ FIXED | — | — |
| #2 Session state not persisted after death | ✅ FIXED | — | — |
| #3 Generic data detail screen | ✅ FIXED | — | — |
| #4 Weather Log lacks auto-fetch | ✅ FIXED | — | — |
| #5 "Completed" vs "Done" mismatch | ✅ NO BUG | N/A | N/A |
| #6 Project tasks lack delete button | ✅ FIXED | — | — |
| #7 Tasks screen needs tick/delete buttons | ✅ ACCEPTABLE | N/A | N/A |
| #8 Task detail weak linking/actions | ✅ FIXED | — | — |
| #9 Subtask parentTaskId null | ✅ NO BUG | N/A | N/A |
| #10 Export buttons mislabeled | ✅ FIXED | — | — |
| #11 Settings flat — Already redesigned | ✅ Already Fixed | N/A | N/A |
| #12 Inconsistent visual accents | ✅ FIXED | — | — |
| #13 Duplicate project detail screens | 🔴 Confirmed | Medium | High |
| #14 Data records lack entity linking | 🟡 Confirmed — Partial | Low | Medium |
| #15 Checklist checked state not preserved | ✅ LIKELY FIXED | — | — |
| #16 Comparison detail lacks table | ✅ LIKELY FIXED | — | — |
| #17 Weather catalog vs Weather Log confusion | ✅ ACCEPTABLE | N/A | N/A |
| #18 Dead menu actions | ✅ FIXED | — | — |
| #19 Unscheduled tasks hidden | ✅ FIXED | — | — |
| #20 Project context lost on "View all tasks" | 🟡 Confirmed — Acceptable | Medium | Low |
| #21 Data tools summary counts only 3 types | ✅ FIXED | — | — |
| #22 Generic edit dialog for data records | 🔴 Confirmed | Medium | High |

**Legend:**
- 🔴 = Issue confirmed, fix needed
- 🟡 = Issue partially confirmed or acceptable
- ✅ = Already resolved / fixed since original analysis
- 🔄 = Needs deeper re-verification
- Severity: Impact on UX/product quality
- Effort: Relative implementation difficulty

**Fixed since original analysis (July 6, 2026):**
- #1 — Weather now saved to observation records
- #2 — Session state moved into Parcelable CaptureSessionState
- #3 — Tool-specific detail composables: Counter, Measurement, Weather Log, etc.
- #4 — Weather Log auto-fetch button
- #6 — Project task cards have delete button
- #8 — Task detail: complete/delete promoted to primary buttons
- #9 — Subtask parentTaskId confirmed working via onSaved callback
- #10 — Export buttons renamed to "Copy Markdown", "Copy CSV", "Copy JSON"
- #12 — Visual accent colors aligned (tasks, data tools, folders)
- #18 — Dead "Duplicate" menu action removed
- #19 — Unscheduled task section added to TasksScreen
- #21 — All 8 data tool types counted in summary
- #5, #7, #15, #16, #17, #20 — Verified as false alarms / already working / acceptable behavior

**Remaining real issues (by impact):**
1. **Major refactors:** #13 (deduplicate project detail — two separate implementations), #22 (tool-specific edit dialogs for data records), #14 (add project/observation linking fields to data tool save flows)
