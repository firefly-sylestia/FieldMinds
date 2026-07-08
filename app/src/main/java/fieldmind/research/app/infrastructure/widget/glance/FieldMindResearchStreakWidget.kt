package fieldmind.research.app.infrastructure.widget.glance

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import fieldmind.research.app.activities.MainActivity

private val WIDGET_SURFACE_LOW = ColorProvider(Color(0xFFF7FBF7))
private val WIDGET_SURFACE = ColorProvider(Color(0xFFEAF3EC))
private val WIDGET_SURFACE_HIGH = ColorProvider(Color(0xFFE1ECE4))

private val BRAND_POSITIVE = Color(0xFF1F6B4C)
private val BRAND_WARNING = Color(0xFFE67E22)
private val BRAND_INFO = Color(0xFF546E7A)

/**
 * FieldMind Research Streak Widget — 2×2 cells
 * Shows current observation streak, best streak, and today's status.
 * Premium glassmorphic design matching FieldMind brand.
 * Data is populated by FieldMindStreakWorker or ViewModel.
 */
class FieldMindResearchStreakWidget : GlanceAppWidget() {

    companion object {
        const val KEY_CURRENT_STREAK = "current_streak"
        const val KEY_BEST_STREAK = "best_streak"
        const val KEY_HAS_TODAY_ENTRY = "has_today_entry"
        const val KEY_TOTAL_SIGHTINGS = "total_sightings"
        const val PREFS_NAME = "fieldmind_streak_widget"

        suspend fun updateData(context: Context, currentStreak: Int, bestStreak: Int, hasTodayEntry: Boolean, totalSightings: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putInt(KEY_CURRENT_STREAK, currentStreak)
                .putInt(KEY_BEST_STREAK, bestStreak)
                .putBoolean(KEY_HAS_TODAY_ENTRY, hasTodayEntry)
                .putInt(KEY_TOTAL_SIGHTINGS, totalSightings)
                .apply()

            val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(FieldMindResearchStreakWidget::class.java)
            glanceIds.forEach { id -> FieldMindResearchStreakWidget().update(context, id) }
        }
    }

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)
        val bestStreak = prefs.getInt(KEY_BEST_STREAK, 0)
        val hasTodayEntry = prefs.getBoolean(KEY_HAS_TODAY_ENTRY, false)
        val totalSightings = prefs.getInt(KEY_TOTAL_SIGHTINGS, 0)

        provideContent {
            GlanceTheme { StreakWidgetUi(currentStreak, bestStreak, hasTodayEntry, totalSightings) }
        }
    }

    @Composable
    private fun StreakWidgetUi(currentStreak: Int, bestStreak: Int, hasTodayEntry: Boolean, totalSightings: Int) {
        val accentColor = if (currentStreak > 0) ColorProvider(BRAND_POSITIVE) else ColorProvider(BRAND_WARNING)
        val streakLabel = if (currentStreak > 0) "$currentStreak days" else "No streak"

        Box(
            modifier = GlanceModifier.fillMaxSize().cornerRadius(32.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Box(modifier = GlanceModifier.fillMaxSize().background(WIDGET_SURFACE_LOW).cornerRadius(32.dp)) { }
            Box(modifier = GlanceModifier.fillMaxSize().background(WIDGET_SURFACE).cornerRadius(32.dp)) { }
            Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(accentColor).cornerRadius(1.5f.dp)) { }

            Box(modifier = GlanceModifier.fillMaxSize().padding(14.dp)) {
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    // ── Header ──
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("\\uD83D\\uDD25", style = TextStyle(fontSize = 18.sp))
                        Spacer(GlanceModifier.width(8.dp))
                        Text("Research Streak", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurface))
                    }

                    Spacer(GlanceModifier.height(10.dp))

                    // ── Streak count ──
                    Text(streakLabel, style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = accentColor))

                    Spacer(GlanceModifier.height(4.dp))

                    // ── Today status ──
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (hasTodayEntry) "\\u2705 Today: recorded" else "\\u26A0\\uFE0F No entry today", style = TextStyle(fontSize = 10.sp, color = if (hasTodayEntry) GlanceTheme.colors.onSurface else ColorProvider(BRAND_WARNING)))
                    }

                    Spacer(GlanceModifier.defaultWeight())

                    // ── Best streak ──
                    Text("Best: $bestStreak days \\u2022 $totalSightings total", style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant))
                }
            }
        }
    }
}
