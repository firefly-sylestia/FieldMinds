package com.curio.app.data

import android.content.Context
import java.io.File

/**
 * Manages persistent audio file storage beyond the temp cache.
 *
 * Audio recordings start in the cache directory (created by [AudioRecorder]).
 * On save, they're copied to the app's internal storage (`filesDir/audio/`)
 * so they survive cache eviction. On entry deletion, the file is cleaned up.
 */
object AudioStorageManager {

    private const val AUDIO_DIR = "audio"

    /**
     * Copy an audio file from a cache path to persistent internal storage.
     *
     * @param context       Android context for accessing filesDir.
     * @param cacheFilePath Absolute path to the temp cache file.
     * @param entryId       The capture entry ID (used as the persistent filename).
     * @return The absolute path of the persisted file, or the original cache
     *         path if the copy fails (graceful degradation).
     */
    fun persistAudio(context: Context, cacheFilePath: String, entryId: String): String {
        val cacheFile = File(cacheFilePath)
        if (!cacheFile.exists()) return cacheFilePath

        val audioDir = File(context.filesDir, AUDIO_DIR).apply { mkdirs() }
        val destFile = File(audioDir, "${entryId}.m4a")

        return try {
            cacheFile.copyTo(destFile, overwrite = true)
            destFile.absolutePath
        } catch (_: Exception) {
            cacheFilePath // fallback to original cache path
        }
    }

    /**
     * Delete an audio file at [audioFilePath] if it's within the app's
     * internal storage audio directory. Safe to call with any path — only
     * deletes if the file exists under `filesDir/audio/`.
     */
    fun deleteAudio(context: Context, audioFilePath: String?) {
        if (audioFilePath.isNullOrBlank()) return
        val file = File(audioFilePath)
        if (!file.exists()) return

        // Only delete files under our audio directory (safety guard)
        val audioDir = File(context.filesDir, AUDIO_DIR)
        if (file.canonicalPath.startsWith(audioDir.canonicalPath)) {
            file.delete()
        }
    }

    /** Delete all audio files in the persistent audio directory. */
    fun deleteAllAudio(context: Context) {
        val audioDir = File(context.filesDir, AUDIO_DIR)
        if (audioDir.exists()) {
            audioDir.listFiles()?.forEach { it.delete() }
        }
    }
}
