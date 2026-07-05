# CI Error Postmortem — Conversation Cycle (July 3, 2026)

This file catalogs every CI compilation failure that occurred during this agent
conversation, identifies root causes, flags which could have been avoided with
better upfront research, and prescribes **SMART instructions** to prevent
recurrence.

---

## Error Timeline

### Cycle A — Dark Mode Depth (commits before this convo)

| Commit | Error | Root Cause |
|--------|-------|------------|
| `70f0a3a1` | `tonalElevation` not found in `ClickableCard.kt` and `InfoCard.kt` | Used Material3 1.2+ API `Card(tonalElevation=…)` — project may use older Compose BOM |
| `7d19a2ce` | `tonalElevation` not found in `FieldMindObserveScreen.kt` | Same cause, different file |
| `eb73424c` | `Card(onClick=…)` overload not found in `ClickableCard.kt` | `Card(onClick)` was added in Material3 1.2 — project may use 1.1 |

**✓ Avoidable:** Yes. All three are Material3 version mismatches. Checking
`libs.versions.toml` for the Compose BOM version before choosing APIs would
have prevented every one.

---

### Cycle B — Snackbar Double-Tap (this conversation)

| Commit | Error | Root Cause |
|--------|-------|------------|
| `9601094e` | `@Composable invocations` at lines 1362, 1366 in `FieldMindNavigation.kt` — inside `BackHandler` lambda | `LocalFieldMindSnackbar.current` and `LocalContext.current as? Activity` are composable calls placed inside the non-composable `BackHandler` lambda |
| `eb73424c` | Same error persisted because the fix moved only `LocalContext.current` but `LocalFieldMindSnackbar.current` was still inside the lambda | Partial fix — didn't move all composable calls |

**Root cause:** The developer(s) treated `BackHandler(onBack = { … })` as a composable lambda. It is **not** — it's a regular Kotlin lambda. Calling `by remember {}` or `LocalFoo.current` inside it produces this error.

**✓ Avoidable:** Yes. Any agent working with `BackHandler`, `onClick`, `onValueChange`, or similar callback lambdas must verify the lambda is a `@Composable` context before calling composable functions.

---

### Cycle C — ClickableCard Refactor (this conversation)

| Commit | Error | Root Cause |
|--------|-------|------------|
| `eb73424c` | `CardColors` / `CardElevation` unresolved in `ClickableCard.kt` | The `import` statements for these types were **removed** when replacing `Card(onClick)` with `Modifier.clickable`, but the types were still used in function signatures |
| `cd10aa57` | Press animation (expressiveCardPress) didn't work | `clickable` modifier placed **before** `expressiveCardPress` in the chain — Foundation's `clickable` consumed the pointer events first |

**Root cause 1:** Overzealous import cleanup — removed imports that were still needed.

**Root cause 2:** Modifier chain order matters. `expressiveCardPress` (which detects press state for lift animation) must come **before** `clickable` (which consumes the tap).

**✓ Avoidable:** Yes.
- Import removal: Always check all references to a type before removing its import.
- Modifier order: When stacking interaction modifiers (`clickable`, `pointerInput`, `draggable`, custom press modifiers), order matters — the first one in the chain has priority for pointer events.

---

### Cycle D — DevFullAppTestRunner Entity Constructors (this conversation)

| Commit | Error | Root Cause |
|--------|-------|------------|
| `b75a9e52` | `ObservationEntity` missing `factsOnlyNotes`, `timestamp` | Assumed constructor parameter names without reading the entity definition |
| `b75a9e52` | `QuestionEntity(title, body)` — parameters don't exist | Actual: `questionText`, `sourceType`, `status` |
| `b75a9e52` | `ProjectEntity(name, description, status)` — parameters don't exist | Actual: `title` (only required param) |
| `b75a9e52` | `SourceEntity(title, url, sourceType)` — parameters don't exist | Actual: `type`, `title` |
| `b75a9e52` | `HypothesisEntity(title, body, category)` — parameters don't exist | Actual: `prediction` (only required param) |
| `b75a9e52` | `FieldMindScreen` used as an expression (no companion object) | Used `val navScreens = FieldMindScreen::class.nestedClasses` — but `FieldMindScreen` is a sealed class, not an object, so it can't be used as an expression |

**Root cause:** Entity constructors were written from memory without reading the actual data class definitions in `FieldEntities.kt`. The sealed class was also misused.

**✓ Avoidable:** Yes. Reading the entity definitions before writing constructor calls would have prevented every single one of these errors.

---

### Cycle E — FieldMindChangelogScreen Corruption (this conversation)

| Commit | Error | Root Cause |
|--------|-------|------------|
| `b75a9e52` | Cascading syntax errors — `Too many arguments for listOf()`, `Unresolved reference 'FieldMindChangelogEntry'`, 30+ "Expecting a top level declaration" | The changelog entry was inserted via `sed -i` which used **tab characters** (`\t`) while the rest of the file uses **8-space indentation** AND omitted the required comma separator between list entries |
| `b75a9e52` | Same errors persisted | The "fix" only added a newline after the data class closing `)` but didn't convert tabs to spaces or add the comma |

**Root cause:** Using `sed` to insert multiline Kotlin code into a file is fragile. The tool inserted literal tab characters (when the file uses spaces), and the math for where to insert was off by one, causing the comma to be omitted.

**✓ Avoidable:** Yes. Two layers:
1. Never use `sed -i` for multiline Kotlin insertion — use `str_replace` with exact string matching instead.
2. If you must use `sed`, test the output before committing. The 30+ "Syntax error" lines in the CI log were a clear signal the file was mangled.

---

### Cycle F — Canvas Parameter Shadowing

| Commit | Error | Root Cause |
|--------|-------|------------|
| `35a1e10f` | `size` parameter shadows `DrawScope.size` inside `Canvas` lambda | Named a function parameter `size`, which shadows the `DrawScope.size` property used inside the `Canvas { … }` lambda |

**Root cause:** Naming a parameter `size` inside a `Canvas` composable shadows the inherited `DrawScope.size`.

**✓ Avoidable:** Yes. Use `iconSize`, `imageSize`, or similar disambiguated names instead of `size` in any `Canvas` / `DrawScope` context.

---

### Cycle G — Composable Getter vs Fun

| Commit | Error | Root Cause |
|--------|-------|------------|
| `643e7b3a` | `@Composable` getter property in `CuteThemeConfig.kt` — Kotlin doesn't support `@Composable` on property getters | Used `val foo: Type @Composable get() = …` instead of `@Composable fun foo(): Type = …` |

**Root cause:** `@Composable` cannot be applied to a property getter. It must be a function.

**✓ Avoidable:** Yes. This is a fundamental Kotlin/Compose rule — composable always means `@Composable fun`, never `@Composable get()`.

---

## Summary Table

| Cycle | Error Type | Occurrences | Avoidable? | Prevention |
|-------|-----------|-------------|------------|------------|
| A | Material3 API not available | 3 | **Yes** | Check `libs.versions.toml` Compose BOM before choosing APIs |
| B | @Composable in non-composable lambda | 2 | **Yes** | Verify lambda signature before calling composable functions |
| C1 | Import removed while still used | 3 | **Yes** | Check all references before removing imports |
| C2 | Modifier chain order wrong | 1 | **Yes** | Interaction modifiers: press-detection before click-consumption |
| D | Entity constructors wrong | 6 | **Yes** | **Read the data class definition before constructing** |
| E | sed-induced corruption | 1 | **Yes** | Never use `sed` for multiline Kotlin; use `str_replace` |
| F | Parameter name shadowing | 1 | **Yes** | Use `iconSize` instead of `size` inside Canvas |
| G | @Composable getter | 1 | **Yes** | `@Composable` only on functions, never on property getters |

**All errors in this conversation cycle were avoidable.**

---

## SMART Instructions (for agents)

Add the following to `AGENTS.md` or a prompt prefix:

```
### COMPILE-SAFETY RULES (read before ANY edit)

1. **READ BEFORE WRITING** — Before constructing any entity/viewmodel/settings
   constructor call, READ the actual data class definition file. Do not assume
   parameter names from memory.

2. **CHECK COMPOSE BOM** — Before using a Material3 API, check
   `gradle/libs.versions.toml` for the Compose BOM version.
   Cross-reference with the Material3 changelog to confirm the API exists.

3. **NON-COMPOSABLE LAMBDAS** — `BackHandler`, `onClick`, `onValueChange`,
   `onCheckedChange`, `LaunchedEffect` key lambdas, and any `callback: () -> Unit`
   are NOT @Composable contexts. Do not call `remember`, `mutableStateOf`,
   `LocalFoo.current`, or `@Composable fun`s inside them. Extract those calls
   to the enclosing @Composable scope.

4. **NO SED FOR KOTLIN** — Never use `sed -i` to insert multiline Kotlin code.
   Always use `str_replace` with exact old/new string matching. If you must use
   a terminal command, verify the output afterward.

5. **IMPORTS** — When removing an import, verify all references to the type are
   also removed/updated. `CardColors`, `CardElevation`, `RoundedCornerShape`,
   `Shape`, and similar Material3 types are often used in function signatures.

6. **MODIFIER ORDER** — When stacking interaction modifiers, order matters:
   press-detection modifiers (`expressiveCardPress`, `pointerInput`) come
   BEFORE click-consumption modifiers (`clickable`, `combinedClickable`).

7. **CANVAS PARAMETER NAMES** — Never name a parameter `size` in a function
   that contains a `Canvas {}` block. Use `iconSize`, `imageSize`, `tileSize`,
   etc.

8. **COMPOSABLE IS A FUNCTION** — `@Composable` only applies to functions,
   never to property getters. Use `@Composable fun foo(): Type` not
   `val foo: Type @Composable get()`.

9. **VERIFY ONE-CYCLE** — After pushing a CI fix, wait for the CI result.
   If the CI log shows NEW errors in files you didn't touch, the previous fix
   may have been incomplete. Do not assume a commit is final until CI passes.

10. **TEST SMOKE** — For entity/data-layer changes, the DevFullAppTestRunner
    in Developer Settings can verify constructors, settings toggles, and
    database operations without a full build.
```

---

## Metrics

| Metric | Value |
|--------|-------|
| CI-fix commits in this cycle | 5 |
| Total errors catalogued | 18 |
| Avoidable errors | 18 (100%) |
| Unique root cause categories | 8 |
| Files most often broken | ClickableCard.kt (3x), FieldMindChangelogScreen.kt (2x), FieldMindObserveScreen.kt (2x), FieldMindNavigation.kt (2x), DevFullAppTestRunner.kt (2x) |
