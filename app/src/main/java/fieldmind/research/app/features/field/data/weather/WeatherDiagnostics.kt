package fieldmind.research.app.features.field.data.weather

/** Lightweight diagnostics for the most recent weather refresh. */
data class WeatherDiagnosticState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val provider: String? = null,
    val locationStatus: String? = null,
    val updatedAt: Long = 0L,
    val lastError: WeatherFetchError? = null
)

sealed class WeatherFetchError(open val provider: String? = null, open val message: String) {
    data object NoLocationPermission : WeatherFetchError(message = "Location permission is required to fetch local weather.")
    data object NoLocationAvailable : WeatherFetchError(message = "No current or cached location was available for weather.")
    data class Network(override val provider: String, override val message: String) : WeatherFetchError(provider, message)
    data class Provider(override val provider: String, val statusCode: Int? = null, override val message: String) : WeatherFetchError(provider, message)
    data class Parse(override val provider: String, override val message: String) : WeatherFetchError(provider, message)
}
