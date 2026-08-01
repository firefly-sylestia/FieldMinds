# Request: Profile page — proper Settings entry + remove duplicate settings

## Analysis

The Profile page (`ProfileScreen.kt`) duplicated the entire Settings screen
(`SettingsScreen.kt`):

- `PreferencesCard` on Profile inlined Display name, Theme, Audio quality,
  Daily shuffle reminder, and Manage categories — every one of these already
  lives in Settings (Profile/Appearance/Recording/Notifications/Categories
  cards).
- `DeveloperCard` on Profile repeated Replay intro + Version, which also live
  in Settings' About card.
- The only Settings entry on Profile was a small gear icon in the top bar —
  easy to miss.

## Changes (app/src/main/java/com/curio/app/features/profile/ProfileScreen.kt)

1. **Added a proper Settings card** — new `SettingsCard` composable placed
   right after the Level card: gradient icon chip + "Settings" + subtitle
   "Theme · reminders · audio · backup" + forward arrow, navigates to
   `CurioRoutes.SETTINGS`. The top-bar gear shortcut stays.
2. **Removed the duplicated PreferencesCard** + `InlineReminderSelector`
   (theme segmented buttons, audio-quality dialog, reminder switch/time
   picker, manage-categories row all deleted — Settings owns them now).
3. **Trimmed DeveloperCard** to only Profile-unique items: Report a bug,
   Crash logs, Test crash. Replay intro + Version rows removed (in Settings'
   About card); header renamed "Support & diagnostics".
4. **Trimmed CategoriesCard** — removed the "Manage" TextButton (duplicates
   Settings → Manage categories); "Open the Cabinet" kept (Profile-unique).
5. **Cleanup** — removed now-unused state (themeMode, audioQuality,
   reminderEnabled, reminderHour, showQualityDialog, showVersionDialog), the
   notification-permission launcher + enableReminder/setReminder, the
   quality/version dialogs (ProfileDialogs is display-name only), the
   versionName val, and 16 unused imports. Class doc comment updated.

Nothing was lost: theme/audio/reminder/display-name/manage-categories stay in
Settings; Replay intro + Version remain in Settings' About card.

## Validation

- code-searcher: 0 matches for all removed symbols
  (PreferencesCard/InlineReminderSelector/formatHour/SegmentedButton/
  RadioButton/Switch) in ProfileScreen.kt.
- code-reviewer-deepseek-flash: clean pass — removed imports all verified
  unused, kept imports all still referenced, new SettingsCard uses only
  imported APIs, CurioRoutes.SETTINGS exists, no feature lost.
- No local gradle build per AGENTS.md — CI owns compilation on push.

## Status

Complete. Commit `TBD` on branch `revamp`.
