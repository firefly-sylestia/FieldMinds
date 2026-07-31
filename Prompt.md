# Profile Preferences and Picker Redesign

## Request

User asked to redesign only the Profile page content below the hero/preferences area, fix the Edit profile button in dark mode, fix the daily reminder timer inline selection box, refresh the What are we exploring page, remove the Choose a lane card, and make cards feel like a beautiful Material 4 / Material Expressive design.

## Plan

- Read DOX chain and relevant Curio files.
- Update CategoryPickerScreen to remove the top Choose a lane hero card and replace it with expressive header/chip treatment plus redesigned category cards.
- Update ProfileScreen cards/rows to use expressive elevated surfaces, fix dark-mode edit button contrast, and replace reminder time dialog with inline time chips.
- Update Curio spec and Fastlane changelog to match the UI polish.
- Run non-build static checks only; Gradle build/test/lint commands are forbidden by root DOX in this environment.

## Completion Summary

- Removed the Category Picker's large "Choose a lane" card and replaced it with compact guidance pills and redesigned expressive category deck tiles.
- Refined Profile cards/rows, fixed the Edit profile button contrast in dark mode, and replaced the reminder time dialog with inline selectable time chips.
- Updated the Curio spec and Fastlane changelog.
