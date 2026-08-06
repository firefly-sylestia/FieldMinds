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

    /** Result of persisting an audio file: the destination path and file size. */
    data class PersistResult(
        val persistentPath: String,
        val fileSizeBytes: Long
    )

    /**
     * Copy an audio file from a cache path to persistent internal storage.
     *
     * @param context       Android context for accessing filesDir.
     * @param cacheFilePath Absolute path to the temp cache file.
     * @param entryId       The capture entry ID (used as the persistent filename).
     * @return [PersistResult] with the persisted path and file size in bytes.
     * @throws IllegalArgumentException if the source is missing/empty or the
     *         destination copy does not produce a non-empty file.
     */
    fun persistAudio(context: Context, cacheFilePath: String, entryId: String): PersistResult {
        val cacheFile = File(cacheFilePath)
        require(cacheFile.isFile && cacheFile.length() > 0L) {
            "The temporary recording is missing or empty."
        }

        val audioDir = File(context.filesDir, AUDIO_DIR).apply { mkdirs() }
        val destFile = File(audioDir, "${entryId}.m4a")
        // Editing an existing voice note already points at this destination.
        // Do not copy a file onto itself; just reuse the verified persistent
        // file so editing title/notes never makes the entry disappear.
        if (cacheFile.canonicalFile != destFile.canonicalFile) {
            cacheFile.copyTo(destFile, overwrite = true)
        }
        require(destFile.isFile && destFile.length() > 0L) {
            "The recording could not be persisted."
        }
        return PersistResult(destFile.absolutePath, destFile.length())
    }

    /**
     * Restore an audio file bundled in a backup into persistent storage.
     *
     * Writes the exact bytes to `filesDir/audio/{entryId}.m4a` — the same
     * name convention as [persistAudio] — so the restored capture's
     * `audioFilePath` resolves immediately.
     *
     * @param entryId The capture entry ID (used as the persistent filename).
     * @param bytes   The audio bytes from the backup payload.
     * @return The absolute path the file was written to.
     */
    fun restoreAudio(context: Context, entryId: String, bytes: ByteArray): String {
        val audioDir = File(context.filesDir, AUDIO_DIR).apply { mkdirs() }
        val destFile = File(audioDir, "${entryId}.m4a")
        destFile.writeBytes(bytes)
        return destFile.absolutePath
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
