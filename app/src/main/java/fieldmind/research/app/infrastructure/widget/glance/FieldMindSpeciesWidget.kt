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
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import fieldmind.research.app.activities.MainActivity
import fieldmind.research.app.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BRAND_PRIMARY = Color(0xFF1F6B4C)
private val SPECIES_GREEN = Color(0xFF2E7D32)
private val INFO_BLUE = Color(0xFF546E7A)

/**
 * FieldMind Species Sighting Widget — 2×2 cells
 * Shows species count, today's sightings, and last species seen.
 * Premium glassmorphic design matching FieldMind brand.
 */
class FieldMindSpeciesWidget : GlanceAppWidget() {

    companion object {
        const val KEY_SPECIES_COUNT = "species_count"
        const val KEY_TOTAL_SIGHTINGS = "total_sightings"
        const val KEY_TODAY_COUNT = "today_count"
        const val KEY_LAST_SPECIES = "last_species"
        const val PREFS_NAME = "fieldmind_species_widget"

        suspend fun updateData(context: Context) {
            val dao = fieldmind.research.app.features.field.data.database.FieldMindDatabase.getInstance(context).fieldMindDao()
            val observations = dao.observeObservations().let { flow ->
                var result = emptyList<fieldmind.research.app.features.field.data.database.entity.ObservationEntity>()
                kotlinx.coroutines.flow.first(flow) { list -> result = list; true }
                result
            }

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val totalSightings = observations.size
            val uniqueSpecies = observations.map { it.subject.lowercase() }.distinct().size
            val todayCount = observations.count { it.date == today }
            val lastSpecies = observations.maxByOrNull { it.createdAt }?.subject ?: ""

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putInt(KEY_SPECIES_COUNT, uniqueSpecies)
                .putInt(KEY_TOTAL_SIGHTINGS, totalSightings)
                .putInt(KEY_TODAY_COUNT, todayCount)
                .putString(KEY_LAST_SPECIES, lastSpecies)
                .apply()

            val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(FieldMindSpeciesWidget::class.java)
            glanceIds.forEach { id -> FieldMindSpeciesWidget().update(context, id) }
        }
    }

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val speciesCount = prefs.getInt(KEY_SPECIES_COUNT, 0).toString()
        val totalSightings = prefs.getInt(KEY_TOTAL_SIGHTINGS, 0).toString()
        val todayCount = prefs.getInt(KEY_TODAY_COUNT, 0).toString()
        val lastSpecies = prefs.getString(KEY_LAST_SPECIES, "") ?: ""

        provideContent {
            val currentSize = LocalSize.current
            GlanceTheme { SpeciesWidgetUi(currentSize, speciesCount, totalSightings, todayCount, lastSpecies) }
        }
    }

    @Composable
    private fun SpeciesWidgetUi(
        size: DpSize,
        speciesCount: String, totalSightings: String, todayCount: String, lastSpecies: String
    ) {
        val isWide = size.width.value >= 200

        Box(
            modifier = GlanceModifier.fillMaxSize().cornerRadius(32.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Box(modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.surfaceContainerLow).cornerRadius(32.dp)) { }
            Box(modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.surfaceVariant.copy(alpha = 0.5f)).cornerRadius(32.dp)) { }
            Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(ColorProvider(SPECIES_GREEN)).cornerRadius(1.5f.dp)) { }

            Box(modifier = GlanceModifier.fillMaxSize().padding(14.dp)) {
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    // Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("\\uD83D\\uDC3E", style = TextStyle(fontSize = 20.sp))
                        Spacer(GlanceModifier.width(8.dp))
                        Text("Species", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurface))
                    }

                    Spacer(GlanceModifier.height(10.dp))

                    if (isWide) {
                        // ── Stats row: 3 columns ──
                        Row(modifier = GlanceModifier.fillMaxWidth()) {
                            SpeciesStatItem("Species", speciesCount, ColorProvider(SPECIES_GREEN), GlanceModifier.defaultWeight())
                            SpeciesStatItem("Sightings", totalSightings, ColorProvider(BRAND_PRIMARY), GlanceModifier.defaultWeight())
                            SpeciesStatItem("Today", todayCount, ColorProvider(INFO_BLUE), GlanceModifier.defaultWeight())
                        }
                        Spacer(GlanceModifier.height(8.dp))
                        if (lastSpecies.isNotBlank()) {
                            Text("Last: ${lastSpecies.take(18)}", style = TextStyle(fontSize = 9.sp, fontStyle = FontStyle.Italic, color = GlanceTheme.colors.onSurfaceVariant))
                        }
                    } else {
                        SpeciesStatItem("Species", speciesCount, ColorProvider(SPECIES_GREEN), GlanceModifier.fillMaxWidth())
                        Spacer(GlanceModifier.height(4.dp))
                        SpeciesStatItem("Sightings", totalSightings, ColorProvider(BRAND_PRIMARY), GlanceModifier.fillMaxWidth())
                        Spacer(GlanceModifier.height(4.dp))
                        SpeciesStatItem("Today", todayCount, ColorProvider(INFO_BLUE), GlanceModifier.fillMaxWidth())
                    }
                }
            }
        }
    }

    @Composable
    private fun SpeciesStatItem(label: String, value: String, accent: ColorProvider, modifier: GlanceModifier) {
        Column(
            modifier = modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = accent))
            Spacer(GlanceModifier.height(2.dp))
            Text(label, style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant))
        }
    }
}
