package com.curio.app.data

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Curio's in-app backup & restore.
 *
 * Exports the two things that make up a user's data — the Room `captures`
 * table and the SharedPreferences files that hold real user state — into a
 * single portable JSON file the user saves anywhere they like (Downloads,
 * Drive, a USB drive…). Restore reads that file back and replaces the
 * current data atomically.
 *
 * **What is backed up:**
 *  - all [CaptureEntity] rows (topic, format, notes, timestamps)
 *  - the user-facing prefs: [AppPreferences], [AudioQualitySettings],
 *    [StreakTracker] and the onboarding-completed flag
 *
 * **What is not backed up:** audio recordings (they live in internal
 * `filesDir/audio/` and can be large — out of scope for the JSON file; the
 * cloud Auto Backup also excludes them via `data_extraction_rules.xml` so
 * the 25 MB quota stays safe) and crash-log prefs (device noise).
 *
 * Pref values are stored as typed [PrefEntry]s (boolean/int/long/float/
 * string/stringset) because SharedPreferences is type-strict: a value
 * written with `putLong` must be read with `getLong` or AOSP throws
 * ClassCastException. JSON only knows numbers, so without the recorded type
 * a Long like the streak epoch-day would round-trip as an Int and crash the
 * next streak read.
 *
 * The file starts with a versioned envelope so a future app version can
 * keep reading old backups; restore refuses files from a NEWER app version.
 */
object CurioBackupManager {

    /** Bump when the payload shape changes. Restore accepts version <= this. */
    const val FORMAT_VERSION = 1

    /** MIME type used by the file pickers. */
    const val MIME_TYPE = "application/json"

    private const val FORMAT_NAME = "curio-backup"
    private const val META_PREFS = "curio_backup_meta"
    private const val KEY_LAST_BACKUP_AT = "last_backup_at"
    private const val KEY_LAST_BACKUP_COUNT = "last_backup_count"

    private const val TYPE_BOOLEAN = "boolean"
    private const val TYPE_INT = "int"
    private const val TYPE_LONG = "long"
    private const val TYPE_FLOAT = "float"
    private const val TYPE_STRING = "string"
    private const val TYPE_STRING_SET = "stringset"

    /**
     * The SharedPreferences files holding genuine user data. Crash logs
     * (`curio_crash_logs`) are deliberately excluded — they're device noise,
     * not user state.
     */
    private val USER_PREF_FILES = listOf(
        "curio_app_prefs",        // AppPreferences — name, theme, reminder
        "curio_audio_quality",    // AudioQualitySettings
        "curio_streak",           // StreakTracker
        "curio_onboarding"        // onboarding-completed flag
    )

    /** Result of a successful export. */
    data class ExportResult(val captureCount: Int, val uri: Uri)

    /** Result of a successful restore. */
    data class RestoreResult(val captureCount: Int, val preferenceFiles: Int)

    /** Suggested file name for the export picker, e.g. curio-backup-20260731-1430.json. */
    fun suggestedFileName(): String {
        val stamp = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
        return "curio-backup-$stamp.json"
    }

    /**
     * Write the current captures + user prefs to [uri] as a versioned JSON
     * file. Runs on the caller's coroutine (I/O is off the main thread).
     */
    suspend fun export(context: Context, uri: Uri): ExportResult {
        val dao = CurioDatabase.getInstance(context).captureDao()
        val captures = dao.getAll()
        val prefs = USER_PREF_FILES.associateWith { name ->
            context.getSharedPreferences(name, Context.MODE_PRIVATE)
                .all
                .mapValues { (_, v) -> v.toTypedEntry() }
        }
        val payload = BackupPayload(
            format = FORMAT_NAME,
            version = FORMAT_VERSION,
            exportedAtMillis = System.currentTimeMillis(),
            captures = captures,
            preferences = prefs
        )
        val json = Gson().toJson(payload)
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(json.toByteArray(Charsets.UTF_8))
        } ?: throw IllegalStateException("Could not open the chosen location for writing")

        // Remember the last successful backup so Settings can show it.
        context.getSharedPreferences(META_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_BACKUP_AT, payload.exportedAtMillis)
            .putInt(KEY_LAST_BACKUP_COUNT, captures.size)
            .apply()
        return ExportResult(captures.size, uri)
    }

    /**
     * Replace the current data with the contents of a Curio backup file.
     *
     * The captures table is wiped and re-inserted inside a single Room
     * transaction (either the whole restore lands or none of it). Prefs are
     * cleared per file then re-written using each entry's recorded type —
     * Gson decodes every JSON number as Double, so the recorded type is what
     * maps it back to the exact Int/Long/Float the app's getters expect.
     */
    suspend fun restore(context: Context, uri: Uri): RestoreResult {
        val json = context.contentResolver.openInputStream(uri)?.use { input ->
            input.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } ?: throw IllegalStateException("Could not open the backup file")

        val payload = Gson().fromJson(json, BackupPayload::class.java)
        require(payload.format == FORMAT_NAME) { "That file isn't a Curio backup" }
        require(payload.version <= FORMAT_VERSION) {
            "This backup was made by a newer version of Curio"
        }

        val db = CurioDatabase.getInstance(context)
        val dao = db.captureDao()
        db.withTransaction {
            dao.clearAll()
            payload.captures.forEach { dao.insert(it) }
        }

        payload.preferences.forEach { (name, entries) ->
            val prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)
            val editor = prefs.edit().clear()
            entries.forEach { (key, entry) ->
                val v = entry.value
                when (entry.type) {
                    TYPE_BOOLEAN -> (v as? Boolean)?.let { editor.putBoolean(key, it) }
                    TYPE_INT -> (v as? Number)?.let { editor.putInt(key, it.toInt()) }
                    TYPE_LONG -> (v as? Number)?.let { editor.putLong(key, it.toLong()) }
                    TYPE_FLOAT -> (v as? Number)?.let { editor.putFloat(key, it.toFloat()) }
                    TYPE_STRING -> (v as? String)?.let { editor.putString(key, it) }
                    TYPE_STRING_SET -> {
                        val list = v as? List<*>
                        if (list != null) {
                            val strings = list.mapNotNull { it as? String }
                            if (strings.isNotEmpty()) {
                                editor.putStringSet(key, strings.toSet())
                            }
                        }
                    }
                }
            }
            editor.apply()
        }
        return RestoreResult(payload.captures.size, payload.preferences.size)
    }

    /** Milliseconds of the last successful export, or 0 if never backed up. */
    fun lastBackupAtMillis(context: Context): Long =
        context.getSharedPreferences(META_PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_BACKUP_AT, 0L)

    /** Capture count captured in the last successful export. */
    fun lastBackupCount(context: Context): Int =
        context.getSharedPreferences(META_PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_LAST_BACKUP_COUNT, 0)

    /**
     * Tag a live SharedPreferences value with its concrete type so restore
     * can write it back with the exact putX call the app's getter expects.
     */
    private fun Any?.toTypedEntry(): PrefEntry = when (this) {
        is Boolean -> PrefEntry(TYPE_BOOLEAN, this)
        is Int -> PrefEntry(TYPE_INT, this)
        is Long -> PrefEntry(TYPE_LONG, this)
        is Float -> PrefEntry(TYPE_FLOAT, this)
        is String -> PrefEntry(TYPE_STRING, this)
        is Set<*> -> PrefEntry(TYPE_STRING_SET, this.map { it.toString() })
        else -> PrefEntry(TYPE_STRING, this?.toString() ?: "")
    }
}

/**
 * A SharedPreferences value tagged with its storage type — preserves exact
 * putInt/putLong/putFloat semantics through the JSON round-trip.
 */
data class PrefEntry(val type: String, val value: Any?)

/** Versioned backup envelope — serialized with Gson. */
data class BackupPayload(
    val format: String,
    val version: Int,
    val exportedAtMillis: Long,
    val captures: List<CaptureEntity>,
    val preferences: Map<String, Map<String, PrefEntry>>
)
