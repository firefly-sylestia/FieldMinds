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
data class SpinSessionState(
    val categoryId: CategoryId,
    val filterChips: List<String>,
    val landedTopic: CurioTopic?
)

/**
 * Persists the Spin screen's session across process death and app
 * restarts (see the Spin redesign — "the landed topic shouldn't be lost").
 *
 * Backed by SharedPreferences with topic data stored as JSON.
 * Call [restoreSession] on mount and [saveSession] when the user lands
 * a topic or applies filters.
 */
class SpinSessionStore(private val context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(
        "curio_spin_session",
        Context.MODE_PRIVATE
    )

    fun restoreSession(): SpinSessionState? {
        val catName = prefs.getString("category_id", null) ?: return null
        val categoryId = runCatching { CategoryId.valueOf(catName) }.getOrNull() ?: return null

        val filterChips = prefs.getString("filter_chips", "")
            .orEmpty()
            .split("|")
            .filter { it.isNotBlank() }

        val topicJson = prefs.getString("landed_topic_json", null)
        val landedTopic = if (topicJson != null) {
            runCatching {
                com.google.gson.Gson().fromJson(topicJson, CurioTopic::class.java)
            }.getOrNull()
        } else null

        return SpinSessionState(
            categoryId = categoryId,
            filterChips = filterChips,
            landedTopic = landedTopic
        )
    }

    fun saveSession(
        categoryId: CategoryId,
        filterChips: List<String>,
        landedTopic: CurioTopic
    ) {
        val gson = com.google.gson.Gson()
        val topicJson = gson.toJson(landedTopic)
        val filterStr = filterChips.joinToString("|")

        prefs.edit()
            .putString("category_id", categoryId.name)
            .putString("filter_chips", filterStr)
            .putString("landed_topic_json", topicJson)
            .apply()
    }

    fun clearLandedTopic() {
        prefs.edit().remove("landed_topic_json").apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
