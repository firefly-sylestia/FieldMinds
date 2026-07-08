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
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import fieldmind.research.app.activities.MainActivity
import fieldmind.research.app.R
import kotlinx.coroutines.flow.first

private val WIDGET_SURFACE_LOW = ColorProvider(Color(0xFFF7FBF7))
private val WIDGET_SURFACE = ColorProvider(Color(0xFFEAF3EC))
private val WIDGET_SURFACE_HIGH = ColorProvider(Color(0xFFE1ECE4))

private val BRAND_PRIMARY = Color(0xFF1F6B4C)
private val OBSERVATION_GREEN = Color(0xFF1F6B4C)
private val INFO_BLUE = Color(0xFF546E7A)
private val TASK_TEAL = Color(0xFF00897B)

/**
 * FieldMind QuickStats Combo Widget — 2×2 cells
 * Combines quick capture button with mini research stats.
 * Premium glassmorphic design matching FieldMind brand.
 */
class FieldMindQuickStatsWidget : GlanceAppWidget() {

    companion object {
        const val KEY_OBSERVATIONS = "combo_observations"
        const val KEY_PROJECTS = "combo_projects"
        const val KEY_TODAY = "combo_today"
        const val PREFS_NAME = "fieldmind_quickstats_widget"

        suspend fun updateData(context: Context) {
            val dao = fieldmind.research.app.features.field.data.database.FieldMindDatabase.getInstance(context).fieldMindDao()
            val observations = dao.observeObservations().first()
            val projects = dao.observeProjects().first()
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val todayCount = observations.count { it.date == today }

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putInt(KEY_OBSERVATIONS, observations.size)
                .putInt(KEY_PROJECTS, projects.size)
                .putInt(KEY_TODAY, todayCount)
                .apply()

            val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(FieldMindQuickStatsWidget::class.java)
            glanceIds.forEach { id -> FieldMindQuickStatsWidget().update(context, id) }
        }
    }

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val obsCount = prefs.getInt(KEY_OBSERVATIONS, 0).toString()
        val projectCount = prefs.getInt(KEY_PROJECTS, 0).toString()
        val todayCount = prefs.getInt(KEY_TODAY, 0).toString()

        provideContent {
            GlanceTheme { QuickStatsUi(obsCount, projectCount, todayCount) }
        }
    }

    @Composable
    private fun QuickStatsUi(obsCount: String, projectCount: String, todayCount: String) {
        Box(
            modifier = GlanceModifier.fillMaxSize().cornerRadius(32.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Box(modifier = GlanceModifier.fillMaxSize().background(WIDGET_SURFACE_LOW).cornerRadius(32.dp)) { }
            Box(modifier = GlanceModifier.fillMaxSize().background(WIDGET_SURFACE).cornerRadius(32.dp)) { }
            Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(ColorProvider(BRAND_PRIMARY)).cornerRadius(1.5f.dp)) { }

            Box(modifier = GlanceModifier.fillMaxSize().padding(14.dp)) {
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    // ── Quick capture header ──
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = GlanceModifier.size(34.dp)
                                .background(ColorProvider(BRAND_PRIMARY.copy(alpha = 0.12f)))
                                .cornerRadius(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(provider = ImageProvider(R.drawable.ic_notification), contentDescription = "Capture", modifier = GlanceModifier.size(20.dp))
                        }
                        Spacer(GlanceModifier.width(10.dp))
                        Text("Quick Observe", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurface))
                    }

                    Spacer(GlanceModifier.height(10.dp))

                    // ── Stats row ──
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        MiniStatItem(obsCount, "Total", ColorProvider(OBSERVATION_GREEN), GlanceModifier.defaultWeight())
                        MiniStatItem(projectCount, "Projects", ColorProvider(TASK_TEAL), GlanceModifier.defaultWeight())
                        MiniStatItem(todayCount, "Today", ColorProvider(INFO_BLUE), GlanceModifier.defaultWeight())
                    }

                    Spacer(GlanceModifier.defaultWeight())

                    Text("Tap to capture \\u2192", style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant))
                }
            }
        }
    }

    @Composable
    private fun MiniStatItem(value: String, label: String, accent: ColorProvider, modifier: GlanceModifier) {
        Column(
            modifier = modifier.padding(horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = accent))
            Text(label, style = TextStyle(fontSize = 9.sp, color = GlanceTheme.colors.onSurfaceVariant))
        }
    }
}
