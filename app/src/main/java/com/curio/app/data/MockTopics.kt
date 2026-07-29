package com.curio.app.data

/**
 * SUPERSEDED by the new schema in this package:
 *
 *   - [MusicGenre]    — enum of Music genres (Rock / Jazz / Classical / etc.)
 *   - [CurioTopic]    — unified topic data class (was MockTopic) with
 *                        optional [CurioTopic.musicGenre] for Music
 *   - [ExploreAction] — what the user does with a topic (was MockExploreAction)
 *   - [CurioEntry]    — captured entry (was MockEntry)
 *   - [CaptureFormat] — format enum (preserved)
 *   - [TopicCatalog]  — the full catalog with 115+ topics across 6
 *                        categories, with per-genre music lookup
 *
 * All consumers (SpinScreen, TopicRevealScreen, SaveCaptureScreen,
 * CabinetScreen, EntryDetailScreen) have migrated to the new schema.
 * This file is intentionally empty so the once-conflicting type names
 * (`MockTopic`, `MockEntry`, `MockExploreAction`) are no longer exported
 * and the migration is unambiguous.
 */