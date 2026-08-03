package com.curio.app.data

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

/**
 * Simple SharedPreferences wrapper for Curio user preferences.
 *
 * Stores display name, theme mode, daily reminder, and data flags
 * across app restarts. Used by ProfileScreen and SettingsScreen.
 */
/**
 * Spin density sizing strength (v7.4) — replaces the old on/off switch with
 * three levels:
 *  - [OFF] — density sizing disabled entirely (the DIMENSION rule can still
 *    compact short screens via "Smart Spin layout").
 *  - [COMPACT] — classic rule: under 440 dpi the deck compacts, 440+ dpi
 *    gets a roomier deck.
 *  - [EXTRA_COMPACT] — adds a 2x tier: under ~350 dpi the deck shrinks even
 *    further so very low-dpi phones fit everything comfortably.
 */
enum class SmartDensityMode { OFF, COMPACT, EXTRA_COMPACT }

object AppPreferences {

    /** Theme mode constants used across ProfileScreen, SettingsScreen, CurioTheme. */
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val THEME_SYSTEM = "system"

    /**
     * Theme style constants — the visual identity Curio wears:
     *  - [THEME_STYLE_DEFAULT] — Curio's warm cream palette + category tints.
     *  - [THEME_STYLE_AMOLED] — forced dark, pure-black surfaces, tints off.
     *  - [THEME_STYLE_MATERIAL] — the device's Material dynamic colors for
     *    surfaces/backgrounds/controls; category accents stay the true
     *    researched colors (the old ~40% blend toward the dynamic primary
     *    made reds turn purple and teals turn olive — dropped), tints off.
     */
    const val THEME_STYLE_DEFAULT = "default"
    const val THEME_STYLE_AMOLED = "amoled"
    const val THEME_STYLE_MATERIAL = "material"

    /** Topic sentiment constants — like/dislike from Topic Reveal feeds the
     *  Spin shuffle weighting (liked topics + their category get more weight,
     *  disliked get less — never fully blocked). [SENTIMENT_NONE] clears. */
    const val SENTIMENT_LIKE = "like"
    const val SENTIMENT_DISLIKE = "dislike"
    const val SENTIMENT_NONE = "none"

    private const val NAME = "curio_app_prefs"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_THEME_MODE = "theme_mode"        // "light", "dark", "system"
    private const val KEY_THEME_STYLE = "theme_style"      // "default", "amoled", "material"
    private const val KEY_REMINDER_ENABLED = "reminder_enabled"
    private const val KEY_REMINDER_HOUR = "reminder_hour"
    private const val KEY_TINT_WASH_ENABLED = "tint_wash_enabled"
    private const val KEY_ENTRY_META_ENABLED = "entry_meta_enabled"
    private const val KEY_SMART_SPIN_LAYOUT = "smart_spin_layout"
    // v7.4 — the density rule is a 3-way STRENGTH picker now. The legacy
    // boolean key below is read once for migration and then removed.
    private const val KEY_SMART_DENSITY_MODE = "smart_density_mode"
    private const val KEY_LEGACY_SMART_DENSITY_LAYOUT = "smart_density_layout"
    private const val KEY_EXPLORE_SESSIONS_ENABLED = "explore_sessions_enabled"
    private const val KEY_LIVE_NOTIFICATIONS_ENABLED = "live_notifications_enabled"
    private const val KEY_OVERLAY_BUBBLE_ENABLED = "overlay_bubble_enabled"
    private const val KEY_PINNED_TOPICS = "pinned_topics"   // JSON array of PinnedTopic
    private const val KEY_SAVED_QUOTES = "saved_quotes"      // JSON array of SavedQuote
    private const val KEY_TOPIC_SENTIMENTS = "topic_sentiments"  // JSON object: "CATEGORY:topicId" -> "like"/"dislike"
    private const val KEY_LAST_SPIN_CATEGORY = "last_spin_category"
    private const val KEY_LAST_SPIN_CATEGORIES = "last_spin_categories"   // comma-joined set
    private const val KEY_LANDED_TOPIC_PREFIX = "landed_topic_"

    // ── Display name ─────────────────────────────────────────────────
    fun getDisplayName(context: Context): String =
        prefs(context).getString(KEY_DISPLAY_NAME, null) ?: "Curious Explorer"

    fun setDisplayName(context: Context, name: String) =
        prefs(context).edit().putString(KEY_DISPLAY_NAME, name).apply()

    /**
     * Reactive theme mode state — updated by [setThemeMode] so [CurioTheme]
     * recomposes instantly when the user toggles the theme in settings.
     * Call [initThemeMode] once at app startup to seed it from prefs.
     */
    var themeModeState by mutableStateOf(THEME_SYSTEM)
        private set

    /**
     * Reactive theme-style state — updated by [setThemeStyle] so [CurioTheme]
     * (and every theme-aware helper) recomposes instantly when the user picks
     * a style in Settings. Seeded from prefs in [initThemeMode].
     */
    var themeStyleState by mutableStateOf(THEME_STYLE_DEFAULT)
        private set

    var reminderEnabledState by mutableStateOf(false)
        private set

    /**
     * Reactive category-tint state — updated by [setTintWashEnabled] so page
     * backgrounds (via categoryBackgroundWash) instantly revert to the plain
     * theme background when the user toggles it in settings.
     */
    var tintWashEnabledState by mutableStateOf(true)
        private set

    /**
     * Reactive entry-meta state — updated by [setEntryMetaEnabled] so the
     * saved-entry meta card (date & time / mood / type), the "Captured
     * today · 3:42 PM" time, and the journal's mood + attachment sections
     * recompose instantly when the user toggles it in Settings. Default ON.
     * Seeded from prefs in [initThemeMode].
     */
    var entryMetaEnabledState by mutableStateOf(true)
    // Smart Spin layout — the DIMENSION rule of the Spin page's smart
    // compact system: short screens get the compact (or extra-compact)
    // layout. Default ON. Seeded from prefs in [initThemeMode].
    var smartSpinLayoutState by mutableStateOf(true)
    // Smart density mode — the DENSITY rule of the Spin page's smart
    // sizing, now a 3-way strength picker (v7.4): OFF disables density
    // sizing, COMPACT keeps the classic rule (under 440 dpi → smaller,
    // 440+ dpi → roomier), EXTRA_COMPACT adds a 2x tier for very low dpi
    // (under 350 dpi → even smaller deck). Default COMPACT (preserves the
    // pre-v7.4 always-on behavior). Seeded from prefs in [initThemeMode].
    var smartDensityModeState by mutableStateOf(SmartDensityMode.COMPACT)
    // Explore sessions — the explore-now timer/reminder/done flow. Default
    // ON; off disables the timer notification + reminder + done prompt while
    // Explore-now still opens the browser and records recently-explored.
    var exploreSessionsEnabledState by mutableStateOf(true)
        private set

    // Live explore notifications — the persistent chronometer notification
    // with Pause/Stop controls shown while exploring (like Samsung/Google's
    // live-updating ongoing notifications). Default ON; off means no ongoing
    // notification at all — only the end-of-session reminder + bubble.
    var liveNotificationsEnabledState by mutableStateOf(true)
        private set

    // Floating explore bubble — a Messenger-style timer bubble drawn over
    // OTHER apps (the browser) via SYSTEM_ALERT_WINDOW. Default ON; off
    // means the timer lives only in the notification (when live
    // notifications are on) — there is no in-app pill fallback.
    var overlayBubbleEnabledState by mutableStateOf(true)
        private set

    /**
     * Reactive pinned-topics state — updated by [pinTopic] / [unpinTopic] so
     * the Topic Reveal pin button and the Topic History "Pinned" section
     * recompose instantly. Seeded from prefs in [initThemeMode].
     */
    var pinnedTopicsState by mutableStateOf<List<PinnedTopic>>(emptyList())
        private set

    /**
     * Reactive saved-quotes state — updated by [saveQuote] /
     * [removeSavedQuote] so the saved-entry bookmark buttons and the Home
     * screen's "Saved" shelf recompose instantly. Seeded from prefs in
     * [initThemeMode].
     */
    var savedQuotesState by mutableStateOf<List<SavedQuote>>(emptyList())
        private set

    /**
     * Reactive topic-sentiment state — keyed "CATEGORY:topicId" → "like" /
     * "dislike". Updated by [setTopicSentiment] so the Topic Reveal buttons
     * and the Spin shuffle recompose/pick with the latest votes. Seeded from
     * prefs in [initThemeMode].
     */
    var topicSentimentsState by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    fun initThemeMode(context: Context) {
        themeModeState = getThemeMode(context)
        themeStyleState = getThemeStyle(context)
        reminderEnabledState = isReminderEnabled(context)
        tintWashEnabledState = isTintWashEnabled(context)
        entryMetaEnabledState = isEntryMetaEnabled(context)
        smartSpinLayoutState = isSmartSpinLayoutEnabled(context)
        smartDensityModeState = getSmartDensityMode(context)
        exploreSessionsEnabledState = isExploreSessionsEnabled(context)
        liveNotificationsEnabledState = isLiveNotificationsEnabled(context)
        overlayBubbleEnabledState = isOverlayBubbleEnabled(context)
        pinnedTopicsState = getPinnedTopics(context)
        savedQuotesState = getSavedQuotes(context)
        topicSentimentsState = getTopicSentiments(context)
    }

    // ── Theme ────────────────────────────────────────────────────────
    fun getThemeMode(context: Context): String =
        prefs(context).getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM

    fun setThemeMode(context: Context, mode: String) {
        prefs(context).edit().putString(KEY_THEME_MODE, mode).apply()
        themeModeState = mode
    }

    // ── Theme style ──────────────────────────────────────────────────
    fun getThemeStyle(context: Context): String =
        prefs(context).getString(KEY_THEME_STYLE, THEME_STYLE_DEFAULT) ?: THEME_STYLE_DEFAULT

    fun setThemeStyle(context: Context, style: String) {
        prefs(context).edit().putString(KEY_THEME_STYLE, style).apply()
        themeStyleState = style
    }

    // ── Category tint wash ────────────────────────────────────────────
    /** Whether category-tinted page backgrounds are enabled (default on). */
    fun isTintWashEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_TINT_WASH_ENABLED, true)

    /**
     * Whether category-tinted backgrounds are EFFECTIVELY on: the user
     * toggle AND the theme style must both allow them. The AMOLED and
     * Material styles force the tint off (pure black / device colors)
     * regardless of the toggle, so [CurioTheme] and the wash helpers read
     * this instead of the raw toggle.
     */
    fun tintWashEffective(): Boolean =
        tintWashEnabledState && themeStyleState == THEME_STYLE_DEFAULT

    fun setTintWashEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_TINT_WASH_ENABLED, enabled).apply()
        tintWashEnabledState = enabled
    }

    fun isEntryMetaEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENTRY_META_ENABLED, true)

    fun setEntryMetaEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENTRY_META_ENABLED, enabled).apply()
        entryMetaEnabledState = enabled
    }

    fun isSmartSpinLayoutEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SMART_SPIN_LAYOUT, true)

    fun setSmartSpinLayoutEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_SMART_SPIN_LAYOUT, enabled).apply()
        smartSpinLayoutState = enabled
    }

    /**
     * The Spin density strength — see [SmartDensityMode].
     *
     * v7.4 — migrates the pre-v7.4 boolean switch on first read: true →
     * COMPACT, false → OFF. The legacy key is removed after migration so
     * the mode key becomes the single source of truth.
     */
    fun getSmartDensityMode(context: Context): SmartDensityMode {
        val prefs = prefs(context)
        val stored = prefs.getString(KEY_SMART_DENSITY_MODE, null)
        if (stored != null) {
            return runCatching { SmartDensityMode.valueOf(stored) }
                .getOrDefault(SmartDensityMode.COMPACT)
        }
        val legacy = prefs.getBoolean(KEY_LEGACY_SMART_DENSITY_LAYOUT, true)
        val migrated = if (legacy) SmartDensityMode.COMPACT else SmartDensityMode.OFF
        prefs.edit()
            .putString(KEY_SMART_DENSITY_MODE, migrated.name)
            .remove(KEY_LEGACY_SMART_DENSITY_LAYOUT)
            .apply()
        return migrated
    }

    fun setSmartDensityMode(context: Context, mode: SmartDensityMode) {
        prefs(context).edit().putString(KEY_SMART_DENSITY_MODE, mode.name).apply()
        smartDensityModeState = mode
    }

    /** Whether the explore-session flow (timer/reminder/done prompt) is on. */
    fun isExploreSessionsEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_EXPLORE_SESSIONS_ENABLED, true)

    fun setExploreSessionsEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_EXPLORE_SESSIONS_ENABLED, enabled).apply()
        exploreSessionsEnabledState = enabled
        if (!enabled) {
            // Turning the feature off mid-session: tear the live session
            // down so the timer, reminder and done-prompt all stop.
            ExploreSessionStore.clearSession(context)
            ExploreReminderScheduler.cancel(context)
            com.curio.app.infrastructure.ExploreSessionService.stop(context)
        }
    }

    /**
     * Whether the persistent live explore notification is on. Default ON.
     * Off = no ongoing notification; the end reminder + bubble stay.
     */
    fun isLiveNotificationsEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_LIVE_NOTIFICATIONS_ENABLED, true)

    fun setLiveNotificationsEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_LIVE_NOTIFICATIONS_ENABLED, enabled).apply()
        liveNotificationsEnabledState = enabled
        val session = ExploreSessionStore.getActiveSession(context) ?: return
        if (enabled) {
            // Flipped ON mid-session: bring the live notification back for
            // the currently active session (the bubble stays if wanted).
            com.curio.app.infrastructure.ExploreSessionService.start(context, session)
        } else {
            // Flipped OFF mid-session: drop the chronometer notification.
            // Keep the service alive when the floating bubble still wants it
            // (it swaps to the minimal bubble-active notification); otherwise
            // stop it — the session + reminder survive either way.
            if (isOverlayBubbleEnabled(context) && Settings.canDrawOverlays(context)) {
                com.curio.app.infrastructure.ExploreSessionService.sync(context)
            } else {
                com.curio.app.infrastructure.ExploreSessionService.stop(context)
            }
        }
    }

    /**
     * Whether the floating explore bubble is on. Default ON. Off = the
     * timer lives only in the notification (when live notifications are on).
     */
    fun isOverlayBubbleEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_OVERLAY_BUBBLE_ENABLED, true)

    fun setOverlayBubbleEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_OVERLAY_BUBBLE_ENABLED, enabled).apply()
        overlayBubbleEnabledState = enabled
        val session = ExploreSessionStore.getActiveSession(context) ?: return
        if (enabled) {
            // Flipped ON mid-session: bring the bubble back (permission must
            // be granted — callers gate on it; the render decides).
            if (Settings.canDrawOverlays(context)) {
                com.curio.app.infrastructure.ExploreSessionService.start(context, session)
            }
        } else {
            // Flipped OFF mid-session: drop the bubble. Keep the service
            // alive when live notifications still want it, else stop it.
            if (isLiveNotificationsEnabled(context)) {
                com.curio.app.infrastructure.ExploreSessionService.sync(context)
            } else {
                com.curio.app.infrastructure.ExploreSessionService.stop(context)
            }
        }
    }

    /**
     * Whether the explore foreground service should run: live notifications
     * ON, OR the floating bubble is enabled AND the "Display over other
     * apps" permission is granted (the overlay is what needs the service
     * when live notifications are off).
     */
    fun exploreServiceShouldRun(context: Context): Boolean =
        isExploreSessionsEnabled(context) && (
            isLiveNotificationsEnabled(context) ||
                (isOverlayBubbleEnabled(context) && Settings.canDrawOverlays(context))
            )


    // ── Pinned topics (Topic Reveal → "Pin for later") ─────────────────
    /**
     * Returns all pinned topics, newest first. Persisted as a JSON array so
     * topic names with delimiters survive round-trips.
     */
    fun getPinnedTopics(context: Context): List<PinnedTopic> {
        val raw = prefs(context).getString(KEY_PINNED_TOPICS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                val id = obj.optString("categoryId")
                val cat = CategoryId.values().firstOrNull { it.name == id } ?: return@List null
                PinnedTopic(
                    categoryId = cat,
                    topicName = obj.optString("topicName"),
                    pinnedAtMillis = obj.optLong("pinnedAtMillis", System.currentTimeMillis())
                )
            }.filterNotNull()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun isTopicPinned(context: Context, categoryId: CategoryId, topicName: String): Boolean =
        getPinnedTopics(context).any {
            it.categoryId == categoryId && it.topicName == topicName
        }

    /** Pins a topic (newest first, deduped). No-op when already pinned. */
    fun pinTopic(context: Context, categoryId: CategoryId, topicName: String) {
        if (topicName.isBlank()) return
        val current = getPinnedTopics(context)
        if (current.any { it.categoryId == categoryId && it.topicName == topicName }) return
        val updated = listOf(PinnedTopic(categoryId, topicName, System.currentTimeMillis())) + current
        savePinnedTopics(context, updated)
    }

    fun unpinTopic(context: Context, categoryId: CategoryId, topicName: String) {
        val updated = getPinnedTopics(context).filterNot {
            it.categoryId == categoryId && it.topicName == topicName
        }
        savePinnedTopics(context, updated)
    }

    private fun savePinnedTopics(context: Context, topics: List<PinnedTopic>) {
        val arr = JSONArray()
        topics.forEach {
            arr.put(
                JSONObject()
                    .put("categoryId", it.categoryId.name)
                    .put("topicName", it.topicName)
                    .put("pinnedAtMillis", it.pinnedAtMillis)
            )
        }
        prefs(context).edit().putString(KEY_PINNED_TOPICS, arr.toString()).apply()
        pinnedTopicsState = topics
    }

    // ── Saved quotes (saved entry → bookmark icon on quote cards) ───────
    /**
     * Returns all bookmarked quotes, newest first. Persisted as a JSON
     * array (same pattern as [PinnedTopic]) so quote text with any
     * characters survives round-trips.
     */
    fun getSavedQuotes(context: Context): List<SavedQuote> {
        val raw = prefs(context).getString(KEY_SAVED_QUOTES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                val id = obj.optString("categoryId")
                val cat = CategoryId.values().firstOrNull { it.name == id } ?: return@List null
                SavedQuote(
                    entryId = obj.optString("entryId"),
                    topicName = obj.optString("topicName"),
                    categoryId = cat,
                    quoteText = obj.optString("quoteText"),
                    savedAtMillis = obj.optLong("savedAtMillis", System.currentTimeMillis())
                )
            }.filterNotNull()
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Bookmarks a quote (newest first, deduped by entry + quote text).
     * No-op when already saved. (The saved/unsaved check in the UI reads
     * the reactive [savedQuotesState] directly, not prefs.)
     */
    fun saveQuote(context: Context, entryId: String, topicName: String, categoryId: CategoryId, quoteText: String) {
        if (entryId.isBlank() || quoteText.isBlank()) return
        val current = getSavedQuotes(context)
        if (current.any { it.entryId == entryId && it.quoteText == quoteText }) return
        val updated = listOf(
            SavedQuote(entryId, topicName, categoryId, quoteText, System.currentTimeMillis())
        ) + current
        saveSavedQuotes(context, updated)
    }

    fun removeSavedQuote(context: Context, entryId: String, quoteText: String) {
        val updated = getSavedQuotes(context).filterNot {
            it.entryId == entryId && it.quoteText == quoteText
        }
        saveSavedQuotes(context, updated)
    }

    private fun saveSavedQuotes(context: Context, quotes: List<SavedQuote>) {
        val arr = JSONArray()
        quotes.forEach {
            arr.put(
                JSONObject()
                    .put("entryId", it.entryId)
                    .put("topicName", it.topicName)
                    .put("categoryId", it.categoryId.name)
                    .put("quoteText", it.quoteText)
                    .put("savedAtMillis", it.savedAtMillis)
            )
        }
        prefs(context).edit().putString(KEY_SAVED_QUOTES, arr.toString()).apply()
        savedQuotesState = quotes
    }

    // ── Topic sentiment (Topic Reveal → like/dislike feeds the shuffle) ──
    /**
     * Returns all topic sentiments keyed "CATEGORY:topicId" → "like" /
     * "dislike". Persisted as a JSON object so any topic id survives.
     */
    fun getTopicSentiments(context: Context): Map<String, String> {
        val raw = prefs(context).getString(KEY_TOPIC_SENTIMENTS, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(raw)
            buildMap { obj.keys().forEach { key -> put(key, obj.optString(key)) } }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    /** Reactive sentiment for a topic — null / [SENTIMENT_LIKE] / [SENTIMENT_DISLIKE]. */
    fun topicSentiment(categoryId: CategoryId, topicId: String): String? =
        topicSentimentsState["${categoryId.name}:$topicId"]

    /** Sets or clears a topic's sentiment ([SENTIMENT_NONE] removes it). */
    fun setTopicSentiment(context: Context, categoryId: CategoryId, topicId: String, sentiment: String) {
        if (topicId.isBlank()) return
        val key = "${categoryId.name}:$topicId"
        val updated = getTopicSentiments(context).toMutableMap()
        if (sentiment == SENTIMENT_NONE) updated.remove(key) else updated[key] = sentiment
        saveTopicSentiments(context, updated)
    }

    private fun saveTopicSentiments(context: Context, sentiments: Map<String, String>) {
        val obj = JSONObject()
        sentiments.forEach { (k, v) -> obj.put(k, v) }
        prefs(context).edit().putString(KEY_TOPIC_SENTIMENTS, obj.toString()).apply()
        topicSentimentsState = sentiments
    }

    /**
     * Net like-minus-dislike count per CATEGORY name — drives the shuffle's
     * category factor (a category with more likes shows more, more dislikes
     * shows less, never zero). Computed from the reactive state.
     */
    fun categoryAffinityMap(): Map<String, Int> {
        val acc = mutableMapOf<String, Int>()
        topicSentimentsState.forEach { (key, sentiment) ->
            val catName = key.substringBefore(':')
            val delta = when (sentiment) {
                SENTIMENT_LIKE -> 1
                SENTIMENT_DISLIKE -> -1
                else -> 0
            }
            if (delta != 0) acc[catName] = (acc[catName] ?: 0) + delta
        }
        return acc
    }

    // ── Daily reminder ───────────────────────────────────────────────
    fun isReminderEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_REMINDER_ENABLED, false)

    fun setReminderEnabled(context: Context, enabled: Boolean) {
        reminderEnabledState = enabled
        prefs(context).edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
        if (enabled) {
            DailyReminderScheduler.schedule(context, getReminderHour(context))
        } else {
            DailyReminderScheduler.cancel(context)
        }
    }

    fun getReminderHour(context: Context): Int =
        prefs(context).getInt(KEY_REMINDER_HOUR, 18)   // default 6 PM

    fun setReminderHour(context: Context, hour: Int) {
        val safeHour = hour.coerceIn(0, 23)
        prefs(context).edit().putInt(KEY_REMINDER_HOUR, safeHour).apply()
        if (isReminderEnabled(context)) {
            DailyReminderScheduler.schedule(context, safeHour)
        }
    }

    // ── Last-used Spin category — persisted so the Spin tab opens where ─
    //    the user left off, even across app launches (v5.5). Falls back
    //    to WILDCARD when unset or when a stored name no longer exists.
    fun getLastSpinCategory(context: Context): CategoryId {
        val name = prefs(context).getString(KEY_LAST_SPIN_CATEGORY, null)
        return name?.let { n ->
            CategoryId.values().firstOrNull { it.name == n }
        } ?: CategoryId.WILDCARD
    }

    fun setLastSpinCategory(context: Context, id: CategoryId) =
        prefs(context).edit().putString(KEY_LAST_SPIN_CATEGORY, id.name).apply()

    /**
     * Full last-used Spin category SET (single or mixed multi-select) —
     * persisted so the Shuffle tab reopens the same deck after back
     * navigation, tab switches and app restarts. The single-category key
     * is kept in sync with the first entry for backwards compat with
     * [getLastSpinCategory].
     */
    fun getLastSpinCategories(context: Context): List<CategoryId> {
        val raw = prefs(context).getString(KEY_LAST_SPIN_CATEGORIES, null)
        val ids = raw
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.mapNotNull { name -> CategoryId.values().firstOrNull { it.name == name } }
            .orEmpty()
        return if (ids.isNotEmpty()) ids else listOf(getLastSpinCategory(context))
    }

    fun setLastSpinCategories(context: Context, ids: List<CategoryId>) {
        val names = ids.map { it.name }.distinct()
        if (names.isEmpty()) return
        prefs(context).edit().putString(KEY_LAST_SPIN_CATEGORIES, names.joinToString(",")).apply()
        setLastSpinCategory(context, ids.first())
    }

    // ── Landed Spin topic (per category) — persisted so the landed card ──
    //    survives ANY navigation. rememberSaveable alone dies when the
    //    Spin back-stack entry is popped (e.g. top-bar back arrow to
    //    Home); mirroring the topic name here lets Spin restore it the
    //    next time it's composed. Cleared when a new spin starts.
    fun getLandedTopic(context: Context, categoryId: CategoryId): String? =
        prefs(context).getString(KEY_LANDED_TOPIC_PREFIX + categoryId.name, null)

    fun setLandedTopic(context: Context, categoryId: CategoryId, topicName: String?) {
        prefs(context).edit()
            .putString(KEY_LANDED_TOPIC_PREFIX + categoryId.name, topicName)
            .apply()
    }

    // ── Internal ─────────────────────────────────────────────────────
    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)
}

/**
 * A topic the user pinned on the Topic Reveal screen so they can revisit it
 * later (listed under "Pinned for later" in Topic History).
 */
data class PinnedTopic(
    val categoryId: CategoryId,
    val topicName: String,
    val pinnedAtMillis: Long
)

/**
 * A quote the user bookmarked from a saved entry (bookmark icon on the
 * quote card in the entry detail view). Listed on the Home screen's
 * "Saved" shelf together with [PinnedTopic]s.
 */
data class SavedQuote(
    val entryId: String,
    val topicName: String,
    val categoryId: CategoryId,
    val quoteText: String,
    val savedAtMillis: Long
)
