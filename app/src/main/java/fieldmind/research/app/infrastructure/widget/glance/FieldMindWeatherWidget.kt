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

/**
 * Weather Widget — 2×2 cells
 * Glassmorphic card showing current temperature, condition, humidity, and wind.
 * Data is written by the ViewModel whenever weather is fetched, via [updateData].
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

        /**
         * Store the latest weather snapshot into widget preferences so the
         * weather widget can display it. Call from ViewModel whenever weather
         * is fetched (e.g. in refreshWeatherFromLocation).
         */
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

            // Trigger widget update
            GlanceAppWidgetManager(context).update(FieldMindWeatherWidget::class.java)
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
                WeatherWidgetUi(
                    currentSize,
                    hasData,
                    temp, desc, humidity, wind, place, code, updatedAt
                )
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
                    .background(ColorProvider(Color(0xFF3B82F6.toInt())))
                    .cornerRadius(1.5f.dp)
            ) { }

            Box(modifier = GlanceModifier.fillMaxSize().padding(14.dp)) {
                if (!hasData) {
                    // Empty state
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "\uD83C\uDF24",
                            style = TextStyle(fontSize = 28.sp)
                        )
                        Spacer(GlanceModifier.height(6.dp))
                        Text(
                            text = "Weather",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlanceTheme.colors.onSurface
                            )
                        )
                        Spacer(GlanceModifier.height(2.dp))
                        Text(
                            text = "Open app to fetch",
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = GlanceTheme.colors.onSurfaceVariant
                            )
                        )
                    }
                } else if (isWide) {
                    // Wide layout: temp + details
                    Column(
                        modifier = GlanceModifier.fillMaxSize()
                    ) {
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$temperature\u00B0",
                                style = TextStyle(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlanceTheme.colors.primary
                                ),
                                modifier = GlanceModifier.defaultWeight()
                            )
                            if (description.isNotBlank()) {
                                Spacer(GlanceModifier.width(8.dp))
                                Text(
                                    text = description.take(15),
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        color = GlanceTheme.colors.onSurfaceVariant
                                    ),
                                    modifier = GlanceModifier.defaultWeight()
                                )
                            }
                        }

                        Spacer(GlanceModifier.height(4.dp))

                        // Details row
                        Row(
                            modifier = GlanceModifier.fillMaxWidth()
                        ) {
                            if (humidity >= 0) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("\uD83D\uDCA7", style = TextStyle(fontSize = 16.sp))
                                    Text("$humidity%", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurface))
                                }
                                Spacer(GlanceModifier.width(12.dp))
                            }
                            if (windSpeed.isNotBlank()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("\uD83D\uDCA8", style = TextStyle(fontSize = 16.sp))
                                    Text("${windSpeed}km/h", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurface))
                                }
                            }
                        }

                        Spacer(GlanceModifier.defaultWeight())

                        if (placeName.isNotBlank()) {
                            Text(
                                text = placeName.take(20),
                                style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant)
                            )
                        }
                        if (updatedAt > 0) {
                            val elapsed = (System.currentTimeMillis() - updatedAt) / 60_000
                            Text(
                                text = if (elapsed < 1) "Just now" else "${elapsed}m ago",
                                style = TextStyle(fontSize = 9.sp, fontStyle = FontStyle.Italic, color = ColorProvider(Color(0xFF8B8B9E.toInt())))
                            )
                        }
                    }
                } else {
                    // Narrow layout: stacked
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$temperature\u00B0",
                            style = TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlanceTheme.colors.primary
                            )
                        )
                        if (description.isNotBlank()) {
                            Spacer(GlanceModifier.height(4.dp))
                            Text(
                                text = description.take(12),
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = GlanceTheme.colors.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
