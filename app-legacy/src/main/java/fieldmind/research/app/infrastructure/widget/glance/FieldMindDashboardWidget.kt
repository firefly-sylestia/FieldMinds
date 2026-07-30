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

private val WIDGET_SURFACE = ColorProvider(Color(0xFFEAF3EC))
private val WIDGET_SURFACE_HIGH = ColorProvider(Color(0xFFE1ECE4))

// ── FieldMind entity accent colors ──
private val OBSERVATION = Color(0xFF1F6B4C)
private val QUESTION = Color(0xFF1565C0)
private val PROJECT = Color(0xFF00695C)
private val SOURCE = Color(0xFF5E35B1)
private val NOTE = Color(0xFF8E24AA)
private val REPORT = Color(0xFFA1531F)
private val BRAND_PRIMARY = Color(0xFF1F6B4C)

/**
 * FieldMind Research Dashboard Widget — 4×3 cells
 * Premium glassmorphic stats dashboard with FieldMind brand colors.
 * Shows real-time entity counts with entity-specific accent colors.
 * Responsive: wide grid (3x2) vs. compact list layout.
 */
class FieldMindDashboardWidget : GlanceAppWidget() {

    companion object {
        const val KEY_OBSERVATION_COUNT = "observation_count"
        const val KEY_NOTE_COUNT = "note_count"
        const val KEY_QUESTION_COUNT = "question_count"
        const val KEY_PROJECT_COUNT = "project_count"
        const val KEY_SOURCE_COUNT = "source_count"
        const val KEY_REPORT_COUNT = "report_count"
        const val KEY_SESSION_ACTIVE = "session_active"
        const val KEY_SESSION_MINUTES = "session_minutes"

        const val PREFS_NAME = "fieldmind_dashboard_widget"

        suspend fun updateData(context: Context) {
            val dao = fieldmind.research.app.features.field.data.database.FieldMindDatabase.getInstance(context).fieldMindDao()
            val obsCount = dao.observeObservations().first().size
            val noteCount = dao.observeNotes().first().size
            val questionCount = dao.observeQuestions().first().size
            val projectCount = dao.observeProjects().first().size
            val sourceCount = dao.observeSources().first().size
            val reportCount = dao.observeReports().first().size

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putInt(KEY_OBSERVATION_COUNT, obsCount)
                .putInt(KEY_NOTE_COUNT, noteCount)
                .putInt(KEY_QUESTION_COUNT, questionCount)
                .putInt(KEY_PROJECT_COUNT, projectCount)
                .putInt(KEY_SOURCE_COUNT, sourceCount)
                .putInt(KEY_REPORT_COUNT, reportCount)
                .apply()

            val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(FieldMindDashboardWidget::class.java)
            glanceIds.forEach { id -> FieldMindDashboardWidget().update(context, id) }
        }
    }

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val obsCount = prefs.getInt(KEY_OBSERVATION_COUNT, 0).toString()
        val noteCount = prefs.getInt(KEY_NOTE_COUNT, 0).toString()
        val questionCount = prefs.getInt(KEY_QUESTION_COUNT, 0).toString()
        val projectCount = prefs.getInt(KEY_PROJECT_COUNT, 0).toString()
        val sourceCount = prefs.getInt(KEY_SOURCE_COUNT, 0).toString()
        val reportCount = prefs.getInt(KEY_REPORT_COUNT, 0).toString()

        provideContent {
            val currentSize = LocalSize.current
            GlanceTheme {
                DashboardUi(currentSize, obsCount, noteCount, questionCount, projectCount, sourceCount, reportCount)
            }
        }
    }

    @Composable
    private fun DashboardUi(
        size: DpSize,
        obsCount: String, noteCount: String, questionCount: String,
        projectCount: String, sourceCount: String, reportCount: String
    ) {
        val isWide = size.width.value.toInt() >= 250

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(WIDGET_SURFACE)
                .cornerRadius(32.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            // ── Brand accent top bar ──
            Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(ColorProvider(BRAND_PRIMARY)).cornerRadius(1.5f.dp)) { }

            Box(modifier = GlanceModifier.fillMaxSize().padding(16.dp)) {
                if (isWide) {
                    // ── Wide layout: header + 3x2 stat grid ──
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        // Header row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = GlanceModifier.size(34.dp)
                                    .background(ColorProvider(BRAND_PRIMARY.copy(alpha = 0.15f)))
                                    .cornerRadius(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(provider = ImageProvider(R.drawable.ic_notification), contentDescription = null, modifier = GlanceModifier.size(20.dp))
                            }
                            Spacer(GlanceModifier.width(10.dp))
                            Text("FieldMind", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurface))
                            Spacer(GlanceModifier.defaultWeight())
                            Text("Research", style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.onSurfaceVariant))
                        }

                        Spacer(GlanceModifier.height(12.dp))

                        // ── 3 rows × 2 columns stat grid ──
                        Row(modifier = GlanceModifier.fillMaxWidth()) {
                            EntityStatCard("Observations", obsCount, ColorProvider(OBSERVATION), GlanceModifier.defaultWeight())
                            Spacer(GlanceModifier.width(8.dp))
                            EntityStatCard("Questions", questionCount, ColorProvider(QUESTION), GlanceModifier.defaultWeight())
                        }
                        Spacer(GlanceModifier.height(8.dp))
                        Row(modifier = GlanceModifier.fillMaxWidth()) {
                            EntityStatCard("Projects", projectCount, ColorProvider(PROJECT), GlanceModifier.defaultWeight())
                            Spacer(GlanceModifier.width(8.dp))
                            EntityStatCard("Sources", sourceCount, ColorProvider(SOURCE), GlanceModifier.defaultWeight())
                        }
                        Spacer(GlanceModifier.height(8.dp))
                        Row(modifier = GlanceModifier.fillMaxWidth()) {
                            EntityStatCard("Notes", noteCount, ColorProvider(NOTE), GlanceModifier.defaultWeight())
                            Spacer(GlanceModifier.width(8.dp))
                            EntityStatCard("Reports", reportCount, ColorProvider(REPORT), GlanceModifier.defaultWeight())
                        }

                        Spacer(GlanceModifier.defaultWeight())
                        Text("Tap to open FieldMind", style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant))
                    }
                } else {
                    // ── Compact vertical layout ──
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = GlanceModifier.size(26.dp)
                                    .background(ColorProvider(BRAND_PRIMARY.copy(alpha = 0.15f)))
                                    .cornerRadius(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(provider = ImageProvider(R.drawable.ic_notification), contentDescription = null, modifier = GlanceModifier.size(15.dp))
                            }
                            Spacer(GlanceModifier.width(8.dp))
                            Text("FieldMind", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurface))
                        }
                        Spacer(GlanceModifier.height(10.dp))
                        CompactEntityRow("Observations", obsCount, ColorProvider(OBSERVATION))
                        CompactEntityRow("Questions", questionCount, ColorProvider(QUESTION))
                        CompactEntityRow("Projects", projectCount, ColorProvider(PROJECT))
                        CompactEntityRow("Notes", noteCount, ColorProvider(NOTE))
                        Spacer(GlanceModifier.defaultWeight())
                        Text("Tap", style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant))
                    }
                }
            }
        }
    }

    @Composable
    private fun EntityStatCard(label: String, count: String, accent: ColorProvider, modifier: GlanceModifier) {
        Column(
            modifier = modifier
                .background(WIDGET_SURFACE_HIGH)
                .cornerRadius(14.dp)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = accent))
            Spacer(GlanceModifier.height(2.dp))
            Text(label, style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant))
        }
    }

    @Composable
    private fun CompactEntityRow(label: String, count: String, accent: ColorProvider) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = GlanceModifier.size(5.dp).background(accent).cornerRadius(3.dp)) { }
            Spacer(GlanceModifier.width(8.dp))
            Text(label, style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSurface), modifier = GlanceModifier.defaultWeight())
            Text(count, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accent))
        }
    }
}
