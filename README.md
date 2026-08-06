# Curio

**Explore something. Notice more. Keep the discovery.**

Curio is an Android discovery app built with Kotlin and Jetpack Compose. It gives you a topic through **The Spin**, helps you explore it in the real world, and lets you save what you found into **The Cabinet**.

## Current app

- Topic discovery across Curio's curated categories
- Capture formats including Field Notes, Marginalia, Gallery Wall, Reel Notes, Sound Bite, and Open Notebook
- Rich paper-style editing with quotes, images, audio, and tags
- Cabinet browsing, search, recent discoveries, and entry detail views
- Profile, Settings, Experiments, backup/restore, and crash recovery
- Additive import of FieldMind V3 `.fieldmind` archives and plain archive JSON
- Offline-first local storage with Room
- No analytics or tracking

## Project structure

```text
app/                     Active Curio Android application
app/src/main/assets/     Curated topic JSON and schema reference
fastlane/                Android store metadata and release notes
scripts/                 Topic authoring, validation, and maintenance utilities
gradle/                  Version catalog and Gradle wrapper configuration
.github/                 Android CI, release workflow, and issue templates
AGENTS.md                Project-wide development contract
master.md               DOX framework definition
Prompt.md               Running request and validation log
```

## Build

Requirements:

- Android Studio with Android SDK 37
- JDK 17
- Android device or emulator running API 26+

Build the active module with:

```bash
./gradlew assembleDebug
```

Run topic validation with:

```bash
./gradlew validateTopics
```

The repository's Android SDK/build environment is provided by CI. See `.github/workflows/` for the authoritative build and release workflows.

## Topic data

Topic catalogs live in `app/src/main/assets/topics/`. The schema and authoring rules are documented in:

- `app/CURIO_DATA_PLAN.md` — canonical data plan and authoring workflow
- `app/src/main/assets/topics/SCHEMA.md` — quick reference beside the JSON files
- `scripts/validate_topics.py` — standalone validation helper

## Release

Store metadata and versioned release notes live under `fastlane/`. Release builds are produced by the GitHub Actions workflows and signed from repository secrets.

## License

Curio is free and open source software.
