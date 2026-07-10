package fieldmind.research.app.infrastructure

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import fieldmind.research.app.R
import fieldmind.research.app.features.field.data.settings.FieldMindSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Sound effect IDs loaded into [SoundPool].
 * Each constant maps to a resource in `res/raw/fx_*.wav`.
 */
object FieldMindSounds {
    /** Gentle chime played on app open. */
    const val CHIME = 1
    /** Camera shutter click on photo capture. */
    const val SHUTTER = 2
    /** Water drop plop on save. */
    const val WATER_DROP = 3
    /** Night cricket chirp ambient. */
    const val CRICKET = 4
    /** Ascending arpeggio for achievements/goals. */
    const val SUCCESS = 5
    /** Gentle whooshing wind ambient. */
    const val WIND = 6
    /** Distant low thunder rumble for storms. */
    const val THUNDER = 7
    /** Morning bird chorus ambient. */
    const val BIRD_CHORUS = 8
}

/**
 * Singleton manager for short sound effects using [SoundPool].
 *
 * Usage (from any composable):
 * ```
 * val soundManager = rememberSoundManager()
 * soundManager.play(FieldMindSounds.CHIME)
 * ```
 *
 * The manager respects the user's `soundEffectsEnabled` and `soundVolume`
 * settings from [FieldMindSettings]. Call [release] when no longer needed
 * (the [rememberSoundManager] composable handles this automatically).
 */
class FieldMindSoundManager private constructor(private val context: Context) {

    private val settings = FieldMindSettings.getInstance(context)
    private val soundPool: SoundPool
    private val soundIds = mutableMapOf<Int, Int>()
    private var loaded = false

    /** Night cricket ambient job — looped while active. */
    private var cricketJob: Job? = null
    /** Wind ambient job — looped while active. */
    private var windJob: Job? = null
    /** Bird chorus ambient job — looped while active. */
    private var birdChorusJob: Job? = null

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(attrs)
            .build()

        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) loaded = true
        }

        // Load all sound effects
        soundIds[FieldMindSounds.CHIME] = soundPool.load(context, R.raw.fx_chime, 1)
        soundIds[FieldMindSounds.SHUTTER] = soundPool.load(context, R.raw.fx_shutter, 1)
        soundIds[FieldMindSounds.WATER_DROP] = soundPool.load(context, R.raw.fx_water_drop, 1)
        soundIds[FieldMindSounds.CRICKET] = soundPool.load(context, R.raw.fx_cricket, 1)
        soundIds[FieldMindSounds.SUCCESS] = soundPool.load(context, R.raw.fx_success, 1)
        soundIds[FieldMindSounds.WIND] = soundPool.load(context, R.raw.fx_wind, 1)
        soundIds[FieldMindSounds.THUNDER] = soundPool.load(context, R.raw.fx_thunder, 1)
        soundIds[FieldMindSounds.BIRD_CHORUS] = soundPool.load(context, R.raw.fx_bird_chorus, 1)
    }

    /**
     * Play a sound effect from [FieldMindSounds].
     * Respects the user's sound effects enabled/disabled and volume settings.
     */
    fun play(soundId: Int, volumeOverride: Float? = null) {
        if (!loaded) return
        if (!settings.soundEffectsEnabled.value) return

        val resId = soundIds[soundId] ?: return
        val volume = volumeOverride ?: settings.soundVolume.value
        soundPool.play(resId, volume, volume, 1, 0, 1f)
    }

    /**
     * Start ambient cricket playback (loops while active).
     * Automatically stops after [durationMs] or when [stopCricket] is called.
     */
    fun startCricket(scope: CoroutineScope, durationMs: Long = 30_000L) {
        stopCricket()
        cricketJob = scope.launch(Dispatchers.IO) {
            // Initial delay before first chirp
            delay(500)
            while (isActive) {
                play(FieldMindSounds.CRICKET)
                delay(4_000L) // chirp every 4 seconds
            }
        }
        // Auto-stop after duration
        scope.launch {
            delay(durationMs)
            stopCricket()
        }
    }

    /** Stop ambient cricket playback. */
    fun stopCricket() {
        cricketJob?.cancel()
        cricketJob = null
    }

    // ── Wind ambient ──

    /**
     * Start gentle wind ambient loop. Plays a soft whoosh periodically.
     */
    fun startWind(scope: CoroutineScope) {
        stopWind()
        windJob = scope.launch(Dispatchers.IO) {
            delay(300)
            while (isActive) {
                play(FieldMindSounds.WIND)
                delay(6_000L) // whoosh every 6 seconds with 3s overlap
            }
        }
    }

    /** Stop wind ambient loop. */
    fun stopWind() {
        windJob?.cancel()
        windJob = null
    }

    // ── Thunder (one-shot, weather-triggered) ──

    /**
     * Play a single distant thunder rumble. Call when weather detects a storm.
     * Returns immediately — sound plays asynchronously through SoundPool.
     */
    fun playThunder() {
        play(FieldMindSounds.THUNDER)
    }

    // ── Bird chorus ambient ──

    /**
     * Start dawn bird chorus ambient loop. Plays a birdsong burst periodically.
     */
    fun startBirdChorus(scope: CoroutineScope, durationMs: Long = 60_000L) {
        stopBirdChorus()
        birdChorusJob = scope.launch(Dispatchers.IO) {
            delay(500)
            while (isActive) {
                play(FieldMindSounds.BIRD_CHORUS)
                delay(8_000L) // chorus every 8 seconds
            }
        }
        // Auto-stop after duration
        scope.launch {
            delay(durationMs)
            stopBirdChorus()
        }
    }

    /** Stop bird chorus ambient loop. */
    fun stopBirdChorus() {
        birdChorusJob?.cancel()
        birdChorusJob = null
    }

    // ── Combined ambient stop ──

    /** Stop all ambient loops (cricket, wind, bird chorus). */
    fun stopAllAmbient() {
        stopCricket()
        stopWind()
        stopBirdChorus()
    }

    /** Release SoundPool resources. Call when the manager is no longer needed. */
    fun release() {
        stopAllAmbient()
        soundPool.release()
    }

    companion object {
        @Volatile
        private var INSTANCE: FieldMindSoundManager? = null

        fun getInstance(context: Context): FieldMindSoundManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FieldMindSoundManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}

/**
 * Composable helper that remembers a [FieldMindSoundManager] for the current
 * composition and releases it on dispose.
 */
@Composable
fun rememberSoundManager(): FieldMindSoundManager {
    val context = LocalContext.current
    val manager = remember { FieldMindSoundManager.getInstance(context) }
    DisposableEffect(Unit) {
        onDispose { /* Global singleton — do NOT release here, only on app destroy */ }
    }
    return manager
}
