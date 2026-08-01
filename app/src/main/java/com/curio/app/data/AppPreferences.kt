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
    private const val KEY_LAST_SPIN_CATEGORY = "last_spin_category"
    private const val KEY_LANDED_TOPIC_PREFIX = "landed_topic_"

    // ── Spin page feature toggles (v5.9) — each is independent: turning
    //    one off reverts that piece of the Spin screen to its previous
    //    design. Seeded by [initThemeMode] at startup, reactive mirrors
    //    below update instantly when toggled in Settings.
    private const val KEY_SPIN_DIAL = "spin_dial_enabled"
    private const val KEY_SPIN_RITUAL = "spin_ritual_enabled"
    private const val KEY_SPIN_DECK_ENRICH = "spin_deck_enrich_enabled"
    private const val KEY_SPIN_FURNITURE = "spin_furniture_enabled"

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

    // ── Reactive Spin page feature mirrors — updated by the setters so
    //    toggling in Settings recomposes the Spin screen instantly.
    var spinDialState by mutableStateOf(true)
        private set
    var spinRitualState by mutableStateOf(true)
        private set
    var spinDeckEnrichState by mutableStateOf(true)
        private set
    var spinFurnitureState by mutableStateOf(true)
        private set

    fun initThemeMode(context: Context) {
        themeModeState = getThemeMode(context)
        reminderEnabledState = isReminderEnabled(context)
        spinDialState = isSpinDialEnabled(context)
        spinRitualState = isSpinRitualEnabled(context)
        spinDeckEnrichState = isSpinDeckEnrichEnabled(context)
        spinFurnitureState = isSpinFurnitureEnabled(context)
    }

    // ── Theme ────────────────────────────────────────────────────────
    fun getThemeMode(context: Context): String =
        prefs(context).getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM

    fun setThemeMode(context: Context, mode: String) {
        prefs(context).edit().putString(KEY_THEME_MODE, mode).apply()
        themeModeState = mode
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

    // ── Spin page features (v5.9) ────────────────────────────────────
    fun isSpinDialEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SPIN_DIAL, true)

    fun setSpinDialEnabled(context: Context, enabled: Boolean) {
        spinDialState = enabled
        prefs(context).edit().putBoolean(KEY_SPIN_DIAL, enabled).apply()
    }

    fun isSpinRitualEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SPIN_RITUAL, true)

    fun setSpinRitualEnabled(context: Context, enabled: Boolean) {
        spinRitualState = enabled
        prefs(context).edit().putBoolean(KEY_SPIN_RITUAL, enabled).apply()
    }

    fun isSpinDeckEnrichEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SPIN_DECK_ENRICH, true)

    fun setSpinDeckEnrichEnabled(context: Context, enabled: Boolean) {
        spinDeckEnrichState = enabled
        prefs(context).edit().putBoolean(KEY_SPIN_DECK_ENRICH, enabled).apply()
    }

    fun isSpinFurnitureEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SPIN_FURNITURE, true)

    fun setSpinFurnitureEnabled(context: Context, enabled: Boolean) {
        spinFurnitureState = enabled
        prefs(context).edit().putBoolean(KEY_SPIN_FURNITURE, enabled).apply()
    }

    // ── Internal ─────────────────────────────────────────────────────
    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)
}
