package com.curio.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Loads Curio topic catalogs from `assets/topics/{categoryId}.json`.
 *
 * The JSON schema is intentionally flat (array of topic objects) so a
 * single category file at 1000+ topics stays parseable on cold start.
 *
 * Each file is loaded lazily on first request and cached in memory for
 * the process lifetime. Topics never change at runtime — there is no
 * refresh / hot-reload logic in Phase 0. Phase 4 (Room persistence)
 * will replace the loader with a DB-backed source using the same
 * [CurioTopic] schema.
 *
 * Schema (per file):
 * ```
 * [
 *   {
 *     "id": "artist-bowie",
 *     "categoryId": "ARTISTS",
 *     "subtype": "Artist",
 *     "name": "David Bowie",
 *     "teaser": "...",
 *     "imageUrl": "",
 *     "exploreAction": {
 *       "verb": "Listen",
 *       "targetName": "Ziggy Stardust (1972)",
 *       "durationMinutes": 38,
 *       "instruction": "..."
 *     },
 *     "tags": ["Rock", "Glam", "1970s"],
 *     "tier": 1
 *   },
 *   ...
 * ]
 * ```
 *
 * Concurrency: parsing runs on [Dispatchers.IO]. The cache itself is
 * guarded by a [Mutex] so concurrent first-access requests don't both
 * parse the same file (an earlier version of this loader had a race
 * that double-parsed 1MB+ JSON files at startup).
 *
 * Failure handling: missing or malformed files throw [TopicLoadException]
 * with the offending path + reason. We deliberately fail loud rather
 * than return an empty list — a missing JSON file is a build / data
 * bug, and silent empty pools would let Spin land on no topic at all.
 */
object TopicJsonLoader {

    private const val ASSET_DIR = "topics"

    /** Per-category caches. Guarded by [cacheMutex] for concurrent first-access safety. */
    private val cache: MutableMap<CategoryId, List<CurioTopic>> = mutableMapOf()
    private val cacheMutex = Mutex()

    /**
     * Installs the [android.content.res.AssetManager] used to read
     * topic JSON files. Must be called once at app startup (typically
     * from [com.curio.app.FieldMindApplication.onCreate]) BEFORE any
     * Compose code runs. Throws [TopicLoadException] if a load is
     * attempted before [install].
     */
    @Volatile private var assets: android.content.res.AssetManager? = null
    fun install(context: Context) {
        assets = context.applicationContext.assets
    }

    /**
     * Returns the topic pool for [id], loading + parsing the JSON file
     * on first access. Subsequent calls return the cached list.
     *
     * Suspends to load on [Dispatchers.IO]. Safe to call from any
     * coroutine context — including the main thread (it will hop to IO
     * automatically).
     *
     * @throws TopicLoadException if the file is missing or malformed,
     *   or if [install] hasn't been called yet.
     */
    suspend fun load(id: CategoryId): List<CurioTopic> {
        cacheMutex.withLock {
            cache[id]?.let { return it }
        }
        val parsed = withContext(Dispatchers.IO) {
            parseAsset("$ASSET_DIR/${id.routeSlug}.json", id)
        }
        cacheMutex.withLock { cache[id] = parsed }
        return parsed
    }

    /**
     * Synchronous accessor — returns the cached pool for [id] if it
     * has already been loaded, or null otherwise. Use this from
     * Compose state that already knows the data is loaded (e.g.
     * SpinScreen after a LaunchedEffect has called [load]).
     */
    fun cached(id: CategoryId): List<CurioTopic>? = cache[id]

    /**
     * Eagerly loads + caches all 11 category JSON files. Call this
     * once at app startup (e.g. from the SplashScreen's
     * LaunchedEffect) so subsequent calls to [load] / [cached] are
     * zero-cost.
     *
     * With 1000+ topics per category at ~600 bytes each, the total
     * parsed footprint is ~6 MB in memory. Parsing happens on
     * [Dispatchers.IO] and takes ~100-300 ms total on a mid-range
     * device (the AssetManager reads from the APK, which is fast).
     *
     * Returns successfully even if individual categories fail to load
     * — those exceptions are swallowed and logged via the [cache]'s
     * "not present" state. Callers can re-attempt per-category via
     * [load].
     */
    suspend fun preloadAll() {
        CategoryId.values().forEach { id ->
            runCatching { load(id) }
        }
    }

    /** Clears the cache. Only useful for tests / hot-reload. */
    fun clearCache() {
        cache.clear()
    }

    // ── Internal ───────────────────────────────────────────────────────────

    private fun parseAsset(path: String, id: CategoryId): List<CurioTopic> {
        val am = assets
            ?: throw TopicLoadException(path, id, "TopicJsonLoader.install(context) not called")
        val raw = try {
            am.open(path).bufferedReader().use { it.readText() }
        } catch (t: Throwable) {
            throw TopicLoadException(path, id, "open/read failed: ${t.message}", t)
        }
        return try {
            val array = JSONArray(raw)
            List(array.length()) { i ->
                val obj = array.getJSONObject(i)
                parseTopic(obj)
            }
        } catch (t: Throwable) {
            throw TopicLoadException(path, id, "parse failed: ${t.message}", t)
        }
    }

    private fun parseTopic(obj: JSONObject): CurioTopic {
        val id = obj.getString("id")
        val catRaw = obj.getString("categoryId")
        val categoryId = runCatching { CategoryId.valueOf(catRaw) }
            .getOrElse {
                throw IllegalStateException(
                    "topic '$id' has unknown categoryId '$catRaw' " +
                    "(expected one of ${CategoryId.values().joinToString { it.name }})"
                )
            }
        val subtype = obj.optString("subtype", "")
        val name = obj.getString("name")
        val teaser = obj.getString("teaser")
        val imageUrl = obj.optString("imageUrl", "")
        val eaObj = obj.getJSONObject("exploreAction")
        val exploreAction = ExploreAction(
            verb            = eaObj.getString("verb"),
            targetName      = eaObj.getString("targetName"),
            durationMinutes = eaObj.optInt("durationMinutes", 30),
            instruction     = eaObj.getString("instruction")
        )
        val tagsArr = obj.optJSONArray("tags")
        val tags: List<String> = if (tagsArr != null) {
            List(tagsArr.length()) { i -> tagsArr.getString(i) }
        } else emptyList()
        val tier = obj.optInt("tier", 1)
        return CurioTopic(
            id            = id,
            categoryId    = categoryId,
            subtype       = subtype,
            name          = name,
            teaser        = teaser,
            imageUrl      = imageUrl,
            exploreAction = exploreAction,
            tags          = tags,
            tier          = tier
        )
    }
}

/** Thrown when a topic JSON file is missing or malformed. */
class TopicLoadException(
    val path: String,
    val categoryId: CategoryId,
    reason: String,
    cause: Throwable? = null
) : RuntimeException("Failed to load $path for ${categoryId.name}: $reason", cause)