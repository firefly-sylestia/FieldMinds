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
 * Shows current temperature and conditions.
 */
class FieldMindWeatherWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FieldMindWeatherWidget()
}
