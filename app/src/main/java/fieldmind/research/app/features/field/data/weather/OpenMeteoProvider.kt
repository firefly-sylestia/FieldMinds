package fieldmind.research.app.features.field.data.weather

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Open-Meteo provider (free + commercial tiers).
 *
 * **Free tier** (no API key required, 10,000 requests/day):
 * Uses `https://api.open-meteo.com/v1/forecast`.
 *
 * **Commercial tier** (paid subscription, higher rate limits):
 * Provide an API key via `apiKey`. When present, the provider switches to
 * `https://customer-api.open-meteo.com/v1/forecast`.
 *
 * Official docs: https://open-meteo.com/en/docs
 */
class OpenMeteoProvider : WeatherProvider {
    override val slug: String = "open-meteo"
    override val displayName: String = "Open-Meteo"

    /** Free tier works without a key; provide one for commercial access. */
    override val requiresApiKey: Boolean = false
    override val apiKeyLabel: String = "Open-Meteo commercial API key (optional)"
    override val apiKeyPlaceholder: String =
        "Leave blank for free tier. Get a key at open-meteo.com for commercial access."

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    override suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
        apiKey: String?
    ): WeatherSnapshot? = withContext(Dispatchers.IO) {
        try {
            // Use commercial endpoint if an API key is provided
            val baseUrl = if (!apiKey.isNullOrBlank()) {
                "https://customer-api.open-meteo.com/v1/forecast"
            } else {
                "https://api.open-meteo.com/v1/forecast"
            }

            val currentParams =
                "temperature_2m,relative_humidity_2m,weather_code,cloud_cover,surface_pressure,wind_speed_10m,wind_direction_10m"
            val dailyParams =
                "temperature_2m_max,temperature_2m_min,weather_code,precipitation_sum,wind_speed_10m_max,apparent_temperature_max,apparent_temperature_min,sunrise,sunset"

            val urlBuilder = baseUrl.toHttpUrl().newBuilder()
                .addQueryParameter("latitude", latitude.toString())
                .addQueryParameter("longitude", longitude.toString())
                .addQueryParameter("current", currentParams)
                .addQueryParameter("daily", dailyParams)
                .addQueryParameter("timezone", "auto")
            if (!apiKey.isNullOrBlank()) {
                // Official Open-Meteo customer endpoint uses the `apikey` query parameter.
                urlBuilder.addQueryParameter("apikey", apiKey)
            }
            val url = urlBuilder.build()

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "FieldMind/1.0 (field-research-app; open-meteo)")
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.w(
                    "OpenMeteo",
                    "HTTP ${response.code} for $latitude,$longitude — body: ${response.body?.string()?.take(200)}"
                )
                return@withContext null
            }

            val body = response.body?.string() ?: return@withContext null
            val data = gson.fromJson(body, OpenMeteoFullResponse::class.java)

            val current = data.current ?: run {
                Log.w("OpenMeteo", "No 'current' object in response for $latitude,$longitude")
                return@withContext null
            }

            val temp = current.temperature
            val code = current.weatherCode ?: 0
            val humidity = current.humidity
            val windSpeed = current.windSpeed
            val windDir = current.windDirection
            val cloudCover = current.cloudCover
            val pressure = current.pressure
            val sunrise = data.daily?.sunrise?.firstOrNull { it.isNotBlank() }
            val sunset = data.daily?.sunset?.firstOrNull { it.isNotBlank() }

            // Parse daily forecasts
            val daily = data.daily
            val forecasts = if (daily?.time != null && daily.tempMax != null && daily.tempMin != null) {
                daily.time.zip(daily.tempMax.zip(daily.tempMin)).mapIndexed { index, (date, temps) ->
                    val (tMax, tMin) = temps
                    val wCode = daily.weatherCode?.getOrNull(index) ?: 0
                    val precip = daily.precipitationSum?.getOrNull(index)
                    val windMax = daily.windSpeedMax?.getOrNull(index)
                    val apparentMax = daily.apparentTempMax?.getOrNull(index)
                    val apparentMin = daily.apparentTempMin?.getOrNull(index)
                    val apparentAvg =
                        if (apparentMax != null && apparentMin != null) (apparentMax + apparentMin) / 2.0 else null
                    DailyForecast(
                        date = date,
                        temperatureMax = tMax,
                        temperatureMin = tMin,
                        weatherCode = wCode,
                        weatherDescription = WeatherSnapshot.descriptionForCode(wCode),
                        precipitationSum = precip,
                        windSpeedMax = windMax,
                        humidityMax = null,
                        apparentTemperature = apparentAvg
                    )
                }
            } else emptyList()

            WeatherSnapshot(
                temperature = temp,
                weatherCode = code,
                weatherDescription = WeatherSnapshot.descriptionForCode(code),
                humidity = humidity,
                windSpeed = windSpeed,
                windDirection = windDir,
                cloudCover = cloudCover,
                pressure = pressure,
                sunrise = sunrise,
                sunset = sunset,
                dailyForecasts = forecasts
            )
        } catch (e: Exception) {
            Log.e("OpenMeteo", "fetchWeather failed for $latitude,$longitude", e)
            null
        }
    }
}

// ── Response models ──

internal data class OpenMeteoFullResponse(
    @SerializedName("current") val current: OpenMeteoCurrent? = null,
    @SerializedName("daily") val daily: OpenMeteoDaily? = null
)

internal data class OpenMeteoCurrent(
    @SerializedName("temperature_2m") val temperature: Double? = null,
    @SerializedName("relative_humidity_2m") val humidity: Int? = null,
    @SerializedName("weather_code") val weatherCode: Int? = null,
    @SerializedName("cloud_cover") val cloudCover: Int? = null,
    @SerializedName("surface_pressure") val pressure: Double? = null,
    @SerializedName("wind_speed_10m") val windSpeed: Double? = null,
    @SerializedName("wind_direction_10m") val windDirection: Int? = null
)

internal data class OpenMeteoDaily(
    @SerializedName("time") val time: List<String>? = null,
    @SerializedName("temperature_2m_max") val tempMax: List<Double>? = null,
    @SerializedName("temperature_2m_min") val tempMin: List<Double>? = null,
    @SerializedName("weather_code") val weatherCode: List<Int>? = null,
    @SerializedName("precipitation_sum") val precipitationSum: List<Double>? = null,
    @SerializedName("wind_speed_10m_max") val windSpeedMax: List<Double>? = null,
    @SerializedName("apparent_temperature_max") val apparentTempMax: List<Double>? = null,
    @SerializedName("apparent_temperature_min") val apparentTempMin: List<Double>? = null,
    @SerializedName("sunrise") val sunrise: List<String>? = null,
    @SerializedName("sunset") val sunset: List<String>? = null
)
