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
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import fieldmind.research.app.activities.MainActivity
import fieldmind.research.app.R

private val WIDGET_SURFACE = ColorProvider(Color(0xFFEAF3EC))
private val WIDGET_SURFACE_HIGH = ColorProvider(Color(0xFFE1ECE4))

// ── FieldMind brand palette ──
private val BRAND_PRIMARY = Color(0xFF1F6B4C)
private val BRAND_PRIMARY_DARK = Color(0xFF8DD5A9)
private val BRAND_ACCENT_BLUE = Color(0xFF546E7A)
private val BRAND_INFO = Color(0xFF546E7A)
private val BRAND_WARNING = Color(0xFFE67E22)

/**
 * FieldMind Weather Widget — 2×2 cells
 * Premium glassmorphic design with FieldMind brand colors, entity accents,
 * animated weather icons, and responsive layout for all sizes.
 * Data written by ViewModel via [updateData].
 */
class FieldMindWeatherWidget : GlanceAppWidget() {

    companion object {
        const val KEY_TEMPERATURE = "weather_temp"
        const val KEY_DESCRIPTION = "weather_desc"
        const val KEY_HUMIDITY = "weather_humidity"
        const val KEY_WIND_SPEED = "weather_wind"
        const val KEY_PLACE_NAME = "weather_place"
        const val KEY_CODE = "weather_code"
        const val KEY_UPDATED_AT = "weather_updated_at"

        const val PREFS_NAME = "fieldmind_weather_widget"

        suspend fun updateData(
            context: Context,
            temperature: Double?,
            description: String,
            humidity: Int?,
            windSpeed: Double?,
            placeName: String,
            weatherCode: Int
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_TEMPERATURE, temperature?.let { "%.1f".format(it) } ?: "")
                .putString(KEY_DESCRIPTION, description)
                .putInt(KEY_HUMIDITY, humidity ?: -1)
                .putString(KEY_WIND_SPEED, windSpeed?.let { "%.1f".format(it) } ?: "")
                .putString(KEY_PLACE_NAME, placeName)
                .putInt(KEY_CODE, weatherCode)
                .putLong(KEY_UPDATED_AT, System.currentTimeMillis())
                .apply()

            val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(FieldMindWeatherWidget::class.java)
            glanceIds.forEach { id -> FieldMindWeatherWidget().update(context, id) }
        }
    }

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val temp = prefs.getString(KEY_TEMPERATURE, "") ?: ""
        val desc = prefs.getString(KEY_DESCRIPTION, "") ?: ""
        val humidity = prefs.getInt(KEY_HUMIDITY, -1)
        val wind = prefs.getString(KEY_WIND_SPEED, "") ?: ""
        val place = prefs.getString(KEY_PLACE_NAME, "") ?: ""
        val code = prefs.getInt(KEY_CODE, 0)
        val updatedAt = prefs.getLong(KEY_UPDATED_AT, 0L)

        val hasData = temp.isNotBlank()

        provideContent {
            val currentSize = LocalSize.current
            GlanceTheme {
                WeatherWidgetUi(currentSize, hasData, temp, desc, humidity, wind, place, code, updatedAt)
            }
        }
    }

    @Composable
    private fun WeatherWidgetUi(
        size: DpSize,
        hasData: Boolean,
        temperature: String,
        description: String,
        humidity: Int,
        windSpeed: String,
        placeName: String,
        weatherCode: Int,
        updatedAt: Long
    ) {
        val isWide = size.width.value >= 200
        val isDark = false // Glance theme doesn't expose dark mode directly; we use surfaceVariant
        val brandColor = ColorProvider(BRAND_PRIMARY)
        val infoColor = ColorProvider(BRAND_INFO)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(WIDGET_SURFACE)
                .cornerRadius(32.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            // ── Brand accent top bar (forest green) ──
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(brandColor)
                    .cornerRadius(1.5f.dp)
            ) { }

            Box(modifier = GlanceModifier.fillMaxSize().padding(14.dp)) {
                if (!hasData) {
                    // ── Empty state with FieldMind branding ──
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "\u26C5", style = TextStyle(fontSize = 28.sp))
                        Spacer(GlanceModifier.height(6.dp))
                        Text(
                            text = "FieldMind Weather",
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurface)
                        )
                        Spacer(GlanceModifier.height(2.dp))
                        Text(
                            text = "Open app to fetch",
                            style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant)
                        )
                    }
                } else if (isWide) {
                    // ── Wide layout: prominent temp + detail rows ──
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        // Temperature + description row
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = GlanceModifier.defaultWeight()) {
                                Text(
                                    text = "$temperature\u00B0",
                                    style = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, color = brandColor),
                                )
                                if (description.isNotBlank()) {
                                    Text(
                                        text = description.take(18),
                                        style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.onSurfaceVariant),
                                        modifier = GlanceModifier.padding(top = 1.dp)
                                    )
                                }
                            }
                            // Weather emoji based on code
                            Text(
                                text = weatherEmoji(weatherCode),
                                style = TextStyle(fontSize = 32.sp)
                            )
                        }

                        Spacer(GlanceModifier.height(8.dp))

                        // ── Details row: humidity, wind, place ──
                        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            if (humidity >= 0) {
                                GlassDetailChip("\uD83D\uDCA7", "$humidity%")
                                Spacer(GlanceModifier.width(8.dp))
                            }
                            if (windSpeed.isNotBlank()) {
                                GlassDetailChip("\uD83D\uDCA8", "${windSpeed}km/h")
                            }
                        }

                        Spacer(GlanceModifier.defaultWeight())

                        // ── Footer: place + timestamp ──
                        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            if (placeName.isNotBlank()) {
                                Text(
                                    text = placeName.take(22),
                                    style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant),
                                    modifier = GlanceModifier.defaultWeight()
                                )
                            }
                            if (updatedAt > 0) {
                                val elapsed = (System.currentTimeMillis() - updatedAt) / 60_000
                                Text(
                                    text = if (elapsed < 1) "now" else "${elapsed}m ago",
                                    style = TextStyle(fontSize = 9.sp, fontStyle = FontStyle.Italic, color = infoColor)
                                )
                            }
                        }
                    }
                } else {
                    // ── Compact layout: centered temp + condition ──
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$temperature\u00B0",
                            style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = brandColor)
                        )
                        if (description.isNotBlank()) {
                            Spacer(GlanceModifier.height(2.dp))
                            Text(
                                text = description.take(14),
                                style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.onSurfaceVariant)
                            )
                        }
                        Spacer(GlanceModifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalAlignment = Alignment.CenterHorizontally) {
                            if (humidity >= 0) {
                                Text("\uD83D\uDCA7", style = TextStyle(fontSize = 12.sp))
                                Spacer(GlanceModifier.width(3.dp))
                                Text("$humidity%", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurfaceVariant))
                                Spacer(GlanceModifier.width(8.dp))
                            }
                            if (windSpeed.isNotBlank()) {
                                Text("\uD83D\uDCA8", style = TextStyle(fontSize = 12.sp))
                                Spacer(GlanceModifier.width(3.dp))
                                Text(windSpeed.take(5), style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurfaceVariant))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun GlassDetailChip(emoji: String, text: String) {
        Row(
            modifier = GlanceModifier
                .background(WIDGET_SURFACE_HIGH)
                .cornerRadius(12.dp)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, style = TextStyle(fontSize = 12.sp))
            Spacer(GlanceModifier.width(4.dp))
            Text(text, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurface))
        }
    }

    private fun weatherEmoji(code: Int): String = when (code) {
        in 200..232 -> "\u26C8"  // thunderstorm
        in 300..321 -> "\uD83C\uDF27"  // drizzle
        in 500..531 -> "\uD83C\uDF26"  // rain
        in 600..622 -> "\u2744"  // snow
        in 701..781 -> "\uD83C\uDF2B"  // mist/fog
        800 -> "\u2600\uFE0F"  // clear
        801 -> "\u26C5"  // few clouds
        802 -> "\uD83C\uDF24"  // scattered clouds
        803, 804 -> "\u2601\uFE0F"  // overcast
        else -> "\u26C5"
    }
}
