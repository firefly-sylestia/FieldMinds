# Request: Saved-entry audio player broken + visualizer redesign

## Request
The saved-entry audio player shows but plays no sound, won't replay after ending, and the visualizer renders badly. The saved view should use the same visualizer design as the recording meter.

## Root causes found
1. **No sound** — ExoPlayer had no `AudioAttributes`/volume/focus routing; some devices route to a silent output or duck → "plays but no sound".
2. **Won't replay** — after `STATE_ENDED`, `play()` alone doesn't restart; after a load error the player sits in `STATE_IDLE` where `play()` can't restart and `seekTo(0)` can even throw.
3. **Broken visualizer** — `WaveformExtractor.extractPcmShorts` read PCM shorts with the buffer's default BIG_ENDIAN order, but MediaCodec decoders emit little-endian PCM → every sample byte-swapped → noise. Also the saved view used 120 bars squeezed into a ~250dp canvas (bars ≈0.1dp, coerced to 1dp → overflow), unlike the recording's 36 capsule bars.

## Changes
- `WaveformExtractor.kt` — `extractPcmShorts` pins the duplicated buffer to `LITTLE_ENDIAN` before the `dup.short` read loop (`dup.order(ByteOrder.LITTLE_ENDIAN)`); new import `java.nio.ByteOrder`. This is the only PCM read path.
- `EntryDetailScreen.kt`:
  - Waveform extraction now `barCount = 36` with `FloatArray(36)` initial/fallback — same bar language as the recording `LiveWaveform`.
  - `ExoPlayer` now `setAudioAttributes(AudioAttributes.DEFAULT, handleAudioFocus = true)` + `setHandleAudioBecomingNoisy(true)` + `setVolume(1f)`.
  - `STATE_ENDED` → reset UI + park at start (`seekTo(0)`); `STATE_IDLE` → reset `isPlaying`; `onPlayerError` → reset UI only (no seek — unavailable once errored into IDLE).
  - Play button: `STATE_ENDED` → `seekTo(0)`, `STATE_IDLE` → `prepare()` (documented retry path), then `play()`.
  - New imports `androidx.media3.common.AudioAttributes` + `PlaybackException` (ordered AudioAttributes, MediaItem, PlaybackException, Player, ExoPlayer).
  - `WaveformCanvas` already rendered 36 capsule bars mirroring `LiveWaveform` (accent/tint split is the progress readout) — kept.

## Validation
- Code reviewer passed 4 passes. Findings applied as prescribed: (1) `PlaybackException` import must precede `Player` alphabetically; (2) `STATE_IDLE` retry needs `prepare()`, not just seek; (3) `onPlayerError`'s unconditional `seekTo(0)` could throw `IllegalStateException` once the player errors into IDLE — removed. All scopes/braces/imports verified; reviewer confirmed ready to push.

## Completion summary
- Committed & pushed.
