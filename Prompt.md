# Request: Audio visualizer redesign — saved entry + recording

## Request
Redesign the audio player visualizer in saved entries (only ONE button), and make the recording visualizer use a similar style but driven by real microphone input.

## Analysis
- Saved-entry playback: `AudioPlayerBar` + `WaveformCanvas` in `EntryDetailScreen.kt` — had a separate waveform + a detached play/pause row below (2 visual elements, an extra button).
- Recording: `LiveWaveform` in `CurioAnimations.kt` was a fake sine-wave animation (`sin`, `PI` imports) — user wants the same capsule-bar style but REAL mic amplitude.
- `AudioRecorder` (MediaRecorder) had no way to read live level — added `maxAmplitude` accessor (`MediaRecorder.maxAmplitude`, valid API).

## Plan
1. ✅ `AudioRecorder.kt` — add `maxAmplitude: Int` accessor (`mediaRecorder?.maxAmplitude ?: 0`).
2. ✅ `CurioAnimations.kt` — rewrite `LiveWaveform` to be real-amplitude driven: `level: Float = 0f` param, `rememberUpdatedState` + history ring (`FloatArray`), `historyTick` state bumped at 70ms cadence, front bar eases toward live level (0.65), older bars decay (*0.86), inactive decays to a flat 0.06–0.2 floor. Capsule bars (cornerRadius barWidth/2). Removed dead `kotlin.math.PI`/`sin` imports. Removed reviewer-flagged dead `var carry`.
3. ✅ `SoundBiteFormat.kt` — poll `recorder.maxAmplitude / 32767f` every 70ms while RECORDING into `micLevel`, thread through `LiveControls` → `LiveWaveform(level = ...)`.
4. ✅ `EntryDetailScreen.kt` — `AudioPlayerBar` redesigned into ONE capsule (RoundedCornerShape(50), surfaceContainerHigh): single 44dp accent play/pause button + seekable capsule-bar `WaveformCanvas` (weight 1f, 34dp, clip 17dp) + time text. `WaveformCanvas` now draws capsule bars (cornerRadius barWidth/2, played accent 0.95 / unplayed tint 0.5), no indicator line. Added `CornerRadius` import.
5. ✅ Reviewed by code-reviewer-deepseek-flash (passed; one nit — dead `carry` var — fixed).
6. Commit & push.

## Completion summary
- Saved entries: single-capsule player with ONE play/pause button, capsule-bar waveform matching the recording meter. No indicator line — accent/tint split is the progress readout.
- Recording: `LiveWaveform` now reacts to real mic amplitude (MediaRecorder.maxAmplitude) with a decaying history tail, styled the same as playback.
- Committed & pushed.
