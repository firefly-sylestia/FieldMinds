package fieldmind.research.app.infrastructure.widget.glance

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Receiver for the FieldMind Quick Capture widget.
 * Opens the FieldMind capture screen when tapped.
 */
class FieldMindQuickCaptureWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FieldMindQuickCaptureWidget()
}

/**
 * Receiver for the FieldMind Research Dashboard widget.
 * Shows research stats and opens the FieldMind home screen when tapped.
 */
class FieldMindDashboardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FieldMindDashboardWidget()
}

/**
 * Receiver for the FieldMind Weather widget.
 * Shows current temperature and conditions with FieldMind brand design.
 */
class FieldMindWeatherWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FieldMindWeatherWidget()
}

/**
 * Receiver for the FieldMind Species Sighting widget.
 * Shows species count, today's sightings, and last species seen.
 */
class FieldMindSpeciesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FieldMindSpeciesWidget()
}

/**
 * Receiver for the FieldMind Research Streak widget.
 * Shows current observation streak, best streak, and today's status.
 */
class FieldMindResearchStreakWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FieldMindResearchStreakWidget()
}

/**
 * Receiver for the FieldMind QuickStats Combo widget.
 * Combines quick capture access with mini research stats.
 */
class FieldMindQuickStatsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FieldMindQuickStatsWidget()
}
