# Category Pickers — Tap-to-Open Default, Long-Press Multi-Select, No Tick

## Request

User (design direction): "by default make the both category picker screen use tap to open that category in spin page like before but then add a tap to hold and select multiple categories and make the done button appear only then, and the look is bad i asked you to give 2 different active and inactive state for the category screen not the tick remove the tick"

## Analysis

- After the multi-select feature, `CategoryPickerScreen` was ALL multi-select (tap toggled, always-visible Done). The Spin sheet (`CategoryPickerSheet`) was still tap-to-open single but had no multi-select at all.
- `CurioCategoryCard` showed the active state as a **check badge** (accent-filled circle) — user explicitly dislikes the tick and wants two distinct card looks instead.

## Plan

1. **`CurioCategoryCard.kt`** — removed the check badge entirely; added `onLongClick: (() -> Unit)? = null` via the **stable** `Modifier.combinedClickable(onClick, onLongClick, interactionSource, indication = null)` overload (wrapped with `HapticFeedbackType.LongPress`). Active state is now a distinct raised treatment: 2dp white border + scale 1.03 + soft white 0.16 glow sheen over the gradient — two clearly different card looks, no tick.
2. **`CategoryPickerScreen.kt`** — default tap navigates straight to `spinWithCategory(slug)` (restores "like before"); **long-press** sets `multiSelectMode = true` and selects that card; in multi-select mode taps toggle; a Done row (Done button + Cancel) appears **only** while `multiSelectMode` is active; helper text switches per mode.
3. **`SpinScreen.kt` `CategoryPickerSheet`** — same tap/long-press split; new `onCategoriesSelected: (List<CurioCategory>) -> Unit` callback; header shows a "N selected" count chip in multi-select mode; Done row replaces the Browse-all link only in that mode; call site maps selections → `activeCatIds` (multi-category pool already supported) and persists the first.

## Completion Summary

- Validation green: braces (card 11/11, picker 26/26, Spin 292/292), `CurioIcons` dead import removed from the card (flagged by reviewer), both `CurioCategoryCard` call sites updated, sheet dismiss path intact (`onDismiss()` fires via the visible side-effect).
- Code review clean after the import fix: stable `combinedClickable` overload confirmed, haptic call legal in the non-composable lambda, sheet `remember`-scoped state resets on reopen.
- Store changelog `20260730.txt` updated. Gradle build/lint NOT run (forbidden in this env; CI validates on push).
