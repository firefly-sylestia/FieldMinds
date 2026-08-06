# Curio

**Explore something. Notice more. Keep the discovery.**

Curio is an Android discovery app built with Kotlin and Jetpack Compose. **The Spin** gives you a topic to explore in the real world, and **The Cabinet** keeps the things you noticed, made, questioned, and saved.

## What Curio includes

- Curated topic catalogs across music, movies, books, visual art, science, and wildcard discoveries
- The Spin roulette with category selection, filters, mixed decks, anti-repeat history, and topic reveals
- Capture formats for voice notes, reviews, journals, gallery walls, field notes, and open notebooks
- Rich paper editing with quotes, images, audio, tags, formatting, ruled lines, torn edges, and paper styles
- Cabinet browsing, search, recent discoveries, entry details, image lightbox, and editing
- Profile, Settings, Experiments, reminders, crash recovery, backup, and restore
- Local Room persistence with offline-first capture storage
- Material 3 theming, Curio typography, Material Symbols icons, motion, and category color systems
- No analytics or tracking

## Repository layout

```text
app/                    Active Curio Android application
app/src/main/assets/    Curated topic JSON catalogs and schema reference
scripts/                Topic authoring, validation, and maintenance utilities
gradle/                 Version catalog and Gradle wrapper configuration
.github/                Android CI, release workflow, and issue templates
fastlane/               Android store metadata and release notes
AGENTS.md               Project-wide development contract
master.md               DOX framework definition
DOX_TREE.md             Current instruction hierarchy
Prompt.md              Request and validation log
```

## Requirements

- Android Studio with Android SDK 37
- JDK 17
- Android device or emulator running API 26 or newer

## Build and validation

The active Android module is `:app`, with application ID `com.curio.app` and a `.debug` suffix for debug builds.

Gradle compilation, packaging, lint, and Gradle-based validation run in GitHub Actions. These are the CI commands:

```bash
# CI: debug APK
./gradlew assembleDebug

# CI: release APK
./gradlew assembleRelease

# CI: topic schema validation
./gradlew validateTopics
```

The local workspace does not provide the Android SDK required for Gradle compilation, and the repository explicitly forbids running those Gradle commands locally. See `.github/workflows/android.yml` and `.github/workflows/release.yml` for the CI configuration.

For a lightweight local content-only check without Gradle:

```bash
python3 scripts/validate_topics.py
```

## Topic data

Topic catalogs are JSON arrays under `app/src/main/assets/topics/`. Every topic has a category, subtype, name, teaser, image URL, and structured explore action.

Read these before changing content:

- `app/CURIO_DATA_PLAN.md` — canonical taxonomy, schema, authoring workflow, and rollout plan
- `app/src/main/assets/topics/SCHEMA.md` — quick reference beside the catalogs
- `scripts/validate_topics.py` — standalone validation helper

The current catalogs contain 2,312 topics across 11 JSON files. Topic IDs must remain globally unique, and instructions must stay within the repository's validation limits.

## Development workflow

1. Read `master.md`, the root `AGENTS.md`, and the nearest child contract before editing.
2. Keep Android source and resources under `app/`.
3. Keep topic authoring and validation utilities under `scripts/`.
4. Do not run Gradle compile, build, lint, or test commands in the local workspace; CI performs those checks.
5. Run safe static checks and `python3 scripts/validate_topics.py` when relevant.
6. Update `Prompt.md` for substantial work and use a conventional commit before pushing.

## Release metadata

Store descriptions and date-based release notes live under `fastlane/metadata/android/en-US/`. Release signing is supplied through GitHub Actions secrets; private keystores are never committed.

## License

Curio is free and open source software.
