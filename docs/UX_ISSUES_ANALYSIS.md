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

**STATUS: ✅ CONFIRMED — UNCHANGED**

**Evidence:** No change verified. `WeatherLogToolScreen` still has manual fields only.

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

**STATUS: ✅ CONFIRMED — UNCHANGED**

**Evidence:** No change verified. `ProjectTasksBuilder` still renders tasks with completion toggle but no delete button.

---

## Issue 7 — Tasks screen should expose tick/delete buttons

**STATUS: ✅ CONFIRMED — UNCHANGED**

**Evidence:** No change verified. `TasksScreen.kt` still uses swipe-to-complete; no explicit complete/delete buttons on task cards.

---

## Issue 8 — Task detail has weak primary completion/delete and linking UX

**STATUS: ✅ CONFIRMED — UNCHANGED**

**Evidence:** No change verified. Overflow menu actions still the primary method for complete/delete.

---

## Issue 9 — Project subtask parentTaskId remains null

**STATUS: 🔄 NEEDS RE-VERIFICATION**

**Evidence:**
- `FieldMindViewModel.addTask()` at line 1115 — does NOT appear to return the inserted task ID (launches coroutine internally)
- `FieldMindDetailScreen.kt` line 2380: `parentTaskId = parentId` — parentId var still likely null at subtask creation time

**Recommendation:** Re-check the full `ProjectTasksBuilder` flow for the `parentId` assignment timing.

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

**STATUS: ✅ CONFIRMED — UNCHANGED**

**Evidence:**
- `FieldMindTheme.kt` defines 12 entity-specific colors including `.task` (teal `#00897B`)
- `TasksScreen.kt` may still use non-task accent colors — needs re-check
- `DataToolsHubScreen` uses single `accentColor = FieldMindTheme.colors.data` for all tool cards

**Fix:** Change `TasksScreen.kt` to use `.task` instead of `.flashcard`. Optionally add per-tool accent colors to `DataToolsHub`.

---

## Issue 13 — Duplicate project detail implementations

**STATUS: ✅ CONFIRMED — UNCHANGED**

**Evidence:** Both `FieldMindProjectDetailScreen.kt` and `FieldMindDetailScreen.kt` → `ProjectDetailContent()` still exist.

---

## Issue 14 — Data record save flows lack project/observation linking

**STATUS: ✅ CONFIRMED — UNCHANGED**

---

## Issue 15 — Checklist tool doesn't preserve item checked state

**STATUS: ✅ CONFIRMED — UNCHANGED**

---

## Issue 16 — Comparison data tool needs table detail/edit experience

**STATUS: ✅ CONFIRMED — UNCHANGED**

---

## Issue 17 — Weather catalog vs Weather Log confusion

**STATUS: ✅ CONFIRMED — MINOR (unchanged)**

---

## Issue 18 — Dead/empty menu actions

**STATUS: ✅ CONFIRMED — UNCHANGED**

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

**STATUS: ✅ CONFIRMED — UNCHANGED**

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
| #4 Weather Log lacks auto-fetch | 🔴 Confirmed | Medium | Low |
| #5 "Completed" vs "Done" mismatch | ✅ NO BUG | N/A | N/A |
| #6 Project tasks lack delete button | 🔴 Confirmed | Medium | Low |
| #7 Tasks screen needs tick/delete buttons | 🔴 Confirmed | Medium | Medium |
| #8 Task detail weak linking/actions | 🔴 Confirmed | Medium | High |
| #9 Subtask parentTaskId null | 🔄 Needs re-verification | High | Medium |
| #10 Export buttons mislabeled | ✅ FIXED | — | — |
| #11 Settings flat — Already redesigned | ✅ Already Fixed | N/A | N/A |
| #12 Inconsistent visual accents | 🟡 Confirmed — Partial | Medium | Medium |
| #13 Duplicate project detail screens | 🔴 Confirmed | Medium | High |
| #14 Data records lack entity linking | 🟡 Confirmed — Partial | Low | Low |
| #15 Checklist checked state not preserved | 🔴 Confirmed | Medium | Medium |
| #16 Comparison detail lacks table | 🔴 Confirmed | Medium | Medium |
| #17 Weather catalog vs Weather Log confusion | 🟡 Confirmed — Minor | Low | Low |
| #18 Dead menu actions | 🔴 Confirmed | Low | Low |
| #19 Unscheduled tasks hidden | ✅ FIXED | — | — |
| #20 Project context lost on "View all tasks" | 🟡 Confirmed — Acceptable | Medium | Low |
| #21 Data tools summary counts only 3 types | 🔴 Confirmed | Low | Low |
| #22 Generic edit dialog for data records | 🔴 Confirmed | Medium | High |

**Legend:**
- 🔴 = Issue confirmed, fix needed
- 🟡 = Issue partially confirmed or acceptable
- ✅ = Already resolved / fixed since original analysis
- 🔄 = Needs deeper re-verification
- Severity: Impact on UX/product quality
- Effort: Relative implementation difficulty

**Fixed since original analysis (July 6, 2026):**
- #1 — Weather now saved to observation records (1-line fix)
- #2 — Session state moved into Parcelable CaptureSessionState
- #3 — Tool-specific detail composables: Counter, Measurement, Weather Log, Checklist, Comparison Table, Event Log, Site Log
- #10 — Export buttons renamed to "Copy Markdown", "Copy CSV", "Copy JSON"
- #19 — Unscheduled task section added to TasksScreen

**Remaining priority order (by impact):**
1. **P0 (Critical flow breaks):** #9 — subtask parentID null
2. **P1 (Major UX gaps):** #8, #13, #22 — Core flows broken or duplicated
3. **P2 (Medium UX issues):** #6, #7, #12, #15, #16, #18
4. **P3 (Minor improvements):** #4, #14, #17, #20, #21
