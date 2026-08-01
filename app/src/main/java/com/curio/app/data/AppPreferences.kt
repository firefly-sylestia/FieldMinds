package com.curio.app.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Simple SharedPreferences wrapper for Curio user preferences.
 *
 * Stores display name, theme mode, daily reminder, and data flags
 * across app restarts. Used by ProfileScreen and SettingsScreen.
 */
object AppPreferences {

    /** Theme mode constants used across ProfileScreen, SettingsScreen, CurioTheme. */
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val THEME_SYSTEM = "system"

    private const val NAME = "curio_app_prefs"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_THEME_MODE = "theme_mode"        // "light", "dark", "system"
    private const val KEY_REMINDER_ENABLED = "reminder_enabled"
    private const val KEY_REMINDER_HOUR = "reminder_hour"
    private const val KEY_TINT_WASH_ENABLED = "tint_wash_enabled"
    private const val KEY_HOME_TINT_ENABLED = "home_tint_enabled"
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
     * Reactive HOME-screen-only tint state — independent of the global tint
     * toggle. When off, the Home page keeps the plain theme background even
     * while every other screen wears its category tint.
     */
    var homeTintEnabledState by mutableStateOf(true)
        private set

    fun initThemeMode(context: Context) {
        themeModeState = getThemeMode(context)
        reminderEnabledState = isReminderEnabled(context)
        tintWashEnabledState = isTintWashEnabled(context)
        homeTintEnabledState = isHomeTintEnabled(context)
    }

    // ── Theme ────────────────────────────────────────────────────────
    fun getThemeMode(context: Context): String =
        prefs(context).getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM

    fun setThemeMode(context: Context, mode: String) {
        prefs(context).edit().putString(KEY_THEME_MODE, mode).apply()
        themeModeState = mode
    }

    // ── Category tint wash ────────────────────────────────────────────
    /** Whether category-tinted page backgrounds are enabled (default on). */
    fun isTintWashEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_TINT_WASH_ENABLED, true)

    fun setTintWashEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_TINT_WASH_ENABLED, enabled).apply()
        tintWashEnabledState = enabled
    }

    // ── Home screen tint (separate from the global wash) ───────────────
    /** Whether the Home page wears a category tint (default on). */
    fun isHomeTintEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HOME_TINT_ENABLED, true)

    fun setHomeTintEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_HOME_TINT_ENABLED, enabled).apply()
        homeTintEnabledState = enabled
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
