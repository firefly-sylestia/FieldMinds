package fieldmind.research.app.infrastructure.widget.glance

import android.content.Context
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import fieldmind.research.app.R
import kotlinx.coroutines.flow.first

/**
 * Research Dashboard Widget — 4×3 cells
 * Glassmorphic stats dashboard with real data from the database.
 * Shows observation, project, note, source, report counts.
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

        /**
         * Immediately queries the database and pushes updated counts to widget state.
         * Call from any coroutine/IO context (e.g. ViewModel, WorkManager, BroadcastReceiver).
         */
        suspend fun updateData(context: Context) {
            val dao = fieldmind.research.app.features.field.data.database.FieldMindDatabase.getInstance(context).fieldMindDao()
            val obsCount = dao.observeObservations().let { flow ->
                var result = 0
                flow.first { list ->
                    result = list.size
                    true
                }
                result
            }
            val noteCount = dao.observeNotes().let { flow ->
                var result = 0
                flow.first { list ->
                    result = list.size
                    true
                }
                result
            }
            val questionCount = dao.observeQuestions().let { flow ->
                var result = 0
                flow.first { list ->
                    result = list.size
                    true
                }
                result
            }
            val projectCount = dao.observeProjects().let { flow ->
                var result = 0
                flow.first { list ->
                    result = list.size
                    true
                }
                result
            }
            val sourceCount = dao.observeSources().let { flow ->
                var result = 0
                flow.first { list ->
                    result = list.size
                    true
                }
                result
            }
            val reportCount = dao.observeReports().let { flow ->
                var result = 0
                flow.first { list ->
                    result = list.size
                    true
                }
                result
            }

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putInt(KEY_OBSERVATION_COUNT, obsCount)
                .putInt(KEY_NOTE_COUNT, noteCount)
                .putInt(KEY_QUESTION_COUNT, questionCount)
                .putInt(KEY_PROJECT_COUNT, projectCount)
                .putInt(KEY_SOURCE_COUNT, sourceCount)
                .putInt(KEY_REPORT_COUNT, reportCount)
                .apply()

            // Push update to all instances of this widget
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
                DashboardUi(
                    currentSize,
                    obsCount, noteCount, questionCount,
                    projectCount, sourceCount, reportCount
                )
            }
        }
    }

    @Composable
    private fun DashboardUi(
        size: DpSize,
        obsCount: String, noteCount: String, questionCount: String,
        projectCount: String, sourceCount: String, reportCount: String
    ) {
        val minWidth = size.width.value.toInt()
        val isWide = minWidth >= 250

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(32.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            // Glass background
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(GlanceTheme.colors.surfaceVariant)
                    .cornerRadius(32.dp)
            ) { }
            // Accent top bar
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(ColorProvider(Color(0xFF6750A4.toInt())))
                    .cornerRadius(1.5f.dp)
            ) { }

            Box(modifier = GlanceModifier.fillMaxSize().padding(18.dp)) {
                if (isWide) {
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        // Header with logo icon
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = GlanceModifier
                                    .size(36.dp)
                                    .background(GlanceTheme.colors.primaryContainer)
                                    .cornerRadius(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_notification),
                                    contentDescription = null,
                                    modifier = GlanceModifier.size(22.dp)
                                )
                            }
                            Spacer(GlanceModifier.width(10.dp))
                            Text(
                                text = "FieldMind",
                                style = TextStyle(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlanceTheme.colors.onSurface
                                )
                            )
                            Spacer(GlanceModifier.defaultWeight())
                            Text(
                                text = "Research",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = GlanceTheme.colors.onSurfaceVariant
                                )
                            )
                        }

                        Spacer(GlanceModifier.height(14.dp))

                        // Stats grid — 3 rows × 2 columns
                        Row(modifier = GlanceModifier.fillMaxWidth()) {
                            GlassStatCard("Observations", obsCount, GlanceTheme.colors.primary, GlanceModifier.defaultWeight())
                            Spacer(GlanceModifier.width(8.dp))
                            GlassStatCard("Questions", questionCount, ColorProvider(Color(0xFF3B82F6.toInt())), GlanceModifier.defaultWeight())
                        }
                        Spacer(GlanceModifier.height(8.dp))
                        Row(modifier = GlanceModifier.fillMaxWidth()) {
                            GlassStatCard("Projects", projectCount, GlanceTheme.colors.tertiary, GlanceModifier.defaultWeight())
                            Spacer(GlanceModifier.width(8.dp))
                            GlassStatCard("Sources", sourceCount, GlanceTheme.colors.error, GlanceModifier.defaultWeight())
                        }
                        Spacer(GlanceModifier.height(8.dp))
                        Row(modifier = GlanceModifier.fillMaxWidth()) {
                            GlassStatCard("Notes", noteCount, ColorProvider(Color(0xFF22C55E.toInt())), GlanceModifier.defaultWeight())
                            Spacer(GlanceModifier.width(8.dp))
                            GlassStatCard("Reports", reportCount, ColorProvider(Color(0xFFF59E0B.toInt())), GlanceModifier.defaultWeight())
                        }

                        Spacer(GlanceModifier.defaultWeight())

                        // Footer
                        Text(
                            text = "Tap to open FieldMind",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = GlanceTheme.colors.onSurfaceVariant
                            )
                        )
                    }
                } else {
                    // Narrow layout: compact vertical list with real data
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = GlanceModifier
                                    .size(28.dp)
                                    .background(GlanceTheme.colors.primaryContainer)
                                    .cornerRadius(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_notification),
                                    contentDescription = null,
                                    modifier = GlanceModifier.size(16.dp)
                                )
                            }
                            Spacer(GlanceModifier.width(8.dp))
                            Text(
                                text = "FieldMind",
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurface)
                            )
                        }
                        Spacer(GlanceModifier.height(10.dp))
                        CompactStatRow("Observations", obsCount, GlanceTheme.colors.primary)
                        CompactStatRow("Questions", questionCount, ColorProvider(Color(0xFF3B82F6.toInt())))
                        CompactStatRow("Projects", projectCount, GlanceTheme.colors.tertiary)
                        CompactStatRow("Notes", noteCount, ColorProvider(Color(0xFF22C55E.toInt())))

                        Spacer(GlanceModifier.defaultWeight())
                        Text(
                            text = "Tap to open",
                            style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun GlassStatCard(label: String, count: String, accent: ColorProvider, modifier: GlanceModifier) {
        Column(
            modifier = modifier
                .background(GlanceTheme.colors.surface)
                .cornerRadius(14.dp)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            )
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 11.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }

    @Composable
    private fun CompactStatRow(label: String, count: String, accent: ColorProvider) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .size(6.dp)
                    .background(accent)
                    .cornerRadius(3.dp)
            ) { }
            Spacer(GlanceModifier.width(8.dp))
            Text(
                text = label,
                style = TextStyle(fontSize = 13.sp, color = GlanceTheme.colors.onSurface),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = count,
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accent)
            )
        }
    }
}
