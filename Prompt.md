# Current Request

## Status: IN PROGRESS — edits applied, review passed, commit pending

"when edit entry when we switch while editing show save and switch and keep editing then discard at the left."

## Changes (1 file)

1. **`app/src/main/java/com/curio/app/features/capture/SaveCaptureScreen.kt`**
   - The leave-with-unsaved-edits dialog is now a three-way choice:
     - **Discard** (error color) in the `dismissButton` slot → LEFT, as
       requested — pops back without saving.
     - **Keep editing** TextButton + **Save and switch** primary Button in a
       Row in the `confirmButton` slot (right). "Save and switch" calls
       `performSave()`, which saves and auto-returns to the detail screen
       in edit mode.
   - Title/message updated ("Unsaved changes").
   - Added `BackHandler(enabled = canSave)` so the SYSTEM back button also
     opens the dialog (previously only the top-bar arrow did).

## Review
- code-reviewer-deepseek-flash: clean — Discard correctly on the left
  (dismissButton slot), performSave in scope, BackHandler import + usage
  correct (non-composable lambda), all referenced imports already present,
  no leftover old dialog code. Nit (accepted): "Save and switch" routes
  through the normal save flow (brief confetti before popping back) — kept
  for consistency with the bottom Save CTA.

## CI
- Compile gate = GitHub Actions on push (per AGENTS.md — no local Gradle).
