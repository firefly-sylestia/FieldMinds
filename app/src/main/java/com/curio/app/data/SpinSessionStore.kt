package com.curio.app.data

import android.content.Context

/**
 * A remembered Spin session — the topic the wheel last landed on, the
 * category it was drawn from, and any filters that were active.
 *
 * The Spin screen restores this on mount so a user who leaves the app to
 * research / watch / listen to their landed topic finds it exactly where
 * they left it instead of an empty wheel.
 */
data class SpinSession(
    val categoryId: CategoryId,
    val topicId: String?,
    val topicName: String?,
    val tags: Set<String>,
    val subtypes: Set<String>,
    val savedAtMillis: Long
) {
    val hasLandedTopic: Boolean get() = !topicId.isNullOrBlank()
}

/**
 * Persists the Spin screen's session across process death and app
 * restarts (see the Spin redesign — "the landed topic shouldn't be lost").
 *
 * Backed by its own SharedPreferences file so clearing a spin session can
 * never disturb [AppPreferences]. Values are stored as primitives + a
 * delimiter-joined string set, so there is no JSON parsing on the startup
 * path.
 */
object SpinSessionStore {

    private const val NAME = "curio_spin_session"
    private const val KEY_CATEGORY = "category_id"
    private const val KEY_TOPIC_ID = "topic_id"
    private const val KEY_TOPIC_NAME = "topic_name"
    private const val KEY_TAGS = "tags"
    private const val KEY_SUBTYPES = "subtypes"
    private const val KEY_SAVED_AT = "saved_at"

    /** Separator for the joined filter sets. Tags never contain a newline. */
    private const val SEP = "\n"

    /**
     * Persists the full session. Pass a null [topicId] to remember only the
     * category + filters (e.g. the user changed filters but hasn't spun yet).
     */
    fun save(
        context: Context,
        categoryId: CategoryId,
        topicId: String?,
        topicName: String?,
        tags: Set<String>,
        subtypes: Set<String>
    ) {
        prefs(context).edit()
            .putString(KEY_CATEGORY, categoryId.name)
            .putString(KEY_TOPIC_ID, topicId)
            .putString(KEY_TOPIC_NAME, topicName)
            .putString(KEY_TAGS, tags.joinToString(SEP))
            .putString(KEY_SUBTYPES, subtypes.joinToString(SEP))
            .putLong(KEY_SAVED_AT, System.currentTimeMillis())
            .apply()
    }

    /**
     * Updates only the filter selection, leaving any landed topic intact.
     * Used when the user applies filters without re-spinning.
     */
    fun saveFilters(
        context: Context,
        categoryId: CategoryId,
        tags: Set<String>,
        subtypes: Set<String>
    ) {
        prefs(context).edit()
            .putString(KEY_CATEGORY, categoryId.name)
            .putString(KEY_TAGS, tags.joinToString(SEP))
            .putString(KEY_SUBTYPES, subtypes.joinToString(SEP))
            .putLong(KEY_SAVED_AT, System.currentTimeMillis())
            .apply()
    }

    /** Returns the remembered session, or null if nothing was ever saved. */
    fun load(context: Context): SpinSession? {
        val p = prefs(context)
        val catName = p.getString(KEY_CATEGORY, null) ?: return null
        val categoryId = runCatching { CategoryId.valueOf(catName) }.getOrNull() ?: return null
        return SpinSession(
            categoryId = categoryId,
            topicId = p.getString(KEY_TOPIC_ID, null)?.takeIf { it.isNotBlank() },
            topicName = p.getString(KEY_TOPIC_NAME, null)?.takeIf { it.isNotBlank() },
            tags = p.getString(KEY_TAGS, "").orEmpty().splitToSet(),
            subtypes = p.getString(KEY_SUBTYPES, "").orEmpty().splitToSet(),
            savedAtMillis = p.getLong(KEY_SAVED_AT, 0L)
        )
    }

    /** Drops the landed topic but keeps the category + filters. */
    fun clearLandedTopic(context: Context) {
        prefs(context).edit()
            .remove(KEY_TOPIC_ID)
            .remove(KEY_TOPIC_NAME)
            .apply()
    }

    /** Wipes the entire remembered session. */
    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }

    private fun String.splitToSet(): Set<String> =
        if (isBlank()) emptySet() else split(SEP).filter { it.isNotBlank() }.toSet()

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)
}
