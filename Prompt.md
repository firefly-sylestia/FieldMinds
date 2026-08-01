# Current Request

## Status: IN PROGRESS — edits applied, review passed, commit pending

"chnage the category selection multiple option of done to say mix"

## Changes (2 files)

1. **`app/src/main/java/com/curio/app/features/spin/SpinScreen.kt`**
   - `CategoryPickerSheet` multi-select confirm button label: "Done" /
     "Done · N" → "Mix" / "Mix · N". Row comment + v5.11 code comment
     updated to match.

2. **`app/src/main/java/com/curio/app/features/picker/CategoryPickerScreen.kt`**
   - Full-screen picker multi-select confirm button label: "Done" /
     "Done · N" → "Mix" / "Mix · N". Row comment updated to match.

## Notes
- The header count badge ("Select decks" / "N selected") left unchanged —
  user only asked about the button.
- No other "Done" labels remain in either picker (Settings alert "Done"
  and IME `ImeAction.Done` are unrelated).

## Review
- code-reviewer-deepseek-flash: clean — string interpolation intact, no
  stale references (v5.11 comment nitpick fixed).

## CI
- Compile gate = GitHub Actions on push (per AGENTS.md — no local Gradle).
