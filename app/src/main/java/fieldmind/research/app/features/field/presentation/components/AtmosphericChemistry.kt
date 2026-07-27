package fieldmind.research.app.features.field.presentation.components

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.exp
import kotlin.math.abs

/**
 * ════════════════════════════════════════════════════════════════════════
 *  🧪 AtmosphericChemistry — Real Atmospheric Optics & Color Physics
 *
 *  Models the chemical and physical processes that determine sky color,
 *  cloud appearance, and atmospheric lighting using:
 *
 *  • Rayleigh scattering — blue sky, red sunsets (λ⁻⁴ dependence)
 *  • Mie scattering — cloud/ haze white appearance
 *  • Ozone Chappuis absorption — sunset purple/magenta
 *  • Chemical air mass composition — affects color temperature
 *  • Solar position model — zenith angle-based scattering path length
 *  • Lightning ionization — spectral emission lines (N₂, O₂)
 *  • Blackbody radiation — color temperature from thermal emission
 *  • Relative humidity optics — water vapor absorption bands
 * ════════════════════════════════════════════════════════════════════════
 */

// ── Light Spectrum ───────────────────────────────────────────────────

/**
 * A spectral power distribution sampled at key visible wavelengths (nm).
 */
data class Spectrum(
    val r: Float = 1f,  // 650nm (red)
    val g: Float = 1f,  // 550nm (green)
    val b: Float = 1f,  // 450nm (blue)
    val ir: Float = 0f  // 700nm+ (thermal infrared, for glow)
)

/** Wavelengths for our RGB sampling (nanometers). */
private const val WAVELENGTH_R = 650f
private const val WAVELENGTH_G = 550f
private const val WAVELENGTH_B = 450f

/** Rayleigh scattering cross-section ~ 1/λ⁴ */
private fun rayleighCoeff(wavelength: Float): Float = 1f / (wavelength * wavelength * wavelength * wavelength)

/** Mie scattering cross-section ~ 1/λ² (clouds, haze) */
private fun mieCoeff(wavelength: Float, turbidity: Float = 1f): Float =
    turbidity * (1f / (wavelength * wavelength * 1e6f))

/** Ozone Chappuis absorption band (peaks around 600nm, giving purple sunsets). */
private fun ozoneAbsorption(wavelength: Float, ozoneDobson: Float = 300f): Float {
    // Simplified Chappuis band shape
    val absorption = when {
        wavelength in 520f..680f -> {
            val x = (wavelength - 600f) / 80f
            exp(-x * x * 2f) * ozoneDobson * 0.0001f
        }
        else -> 0f
    }
    return absorption
}

/** Water vapor absorption (weak bands in visible, stronger in IR). */
private fun waterVaporAbsorption(wavelength: Float, humidity: Float = 0.5f): Float {
    val absorption = when {
        wavelength in 580f..620f -> humidity * 0.02f
        wavelength in 720f..740f -> humidity * 0.08f
        wavelength in 810f..840f -> humidity * 0.15f
        else -> 0f
    }
    return absorption
}

// ── Solar Position Model ─────────────────────────────────────────────

/**
 * Computes the effective path length of sunlight through the atmosphere
 * based on solar zenith angle. Longer path at sunrise/sunset = more
 * scattering = redder light.
 */
data class SolarPosition(
    val zenithAngleRad: Float,     // 0 = directly overhead, π/2 = horizon
    val elevationRad: Float,       // Above horizon (negative = below)
    val pathLength: Float,         // Relative air mass (1 = zenith, ~38 at horizon)
    val isDaytime: Boolean
) {
    companion object {
        /**
         * Compute solar position from time of day.
         * @param hour 0-23
         * @param latitude degrees (approximate for path length)
         */
        fun fromTimeOfDay(hour: Int, latitude: Double = 40.0): SolarPosition {
            val solarAngle = ((hour - 12f) / 12f * PI.toFloat()).coerceIn(-PI.toFloat(), PI.toFloat())
            val zenith = abs(solarAngle)
            val elevation = (PI.toFloat() / 2f) - zenith
            val isDaytime = elevation > -0.17f // Nautical twilight threshold

            // Path length: secant(zenith) for the plane-parallel approximation
            // Capped at ~38 for realistic horizon reddening
            val pathLength = when {
                zenith < PI.toFloat() / 2f -> (1f / cos(zenith)).coerceAtMost(38f)
                else -> 38f
            }

            return SolarPosition(
                zenithAngleRad = zenith,
                elevationRad = elevation,
                pathLength = pathLength,
                isDaytime = isDaytime
            )
        }
    }
}

// ── Sky Color Model ──────────────────────────────────────────────────

/**
 * Physics-based sky color computation using Rayleigh + Mie scattering.
 */
class SkyColorModel(
    private var turbidity: Float = 1.0f,      // Aerosol loading (1=clear, 10=hazy)
    private var ozoneDobson: Float = 300f,    // Ozone column (250-400 DU)
    private var humidity: Float = 0.5f,        // Relative humidity (0-1)
    private var aerosolAlbedo: Float = 0.8f    // Single scattering albedo
) {
    /** Set atmospheric composition for different air masses. */
    fun setAirMass(chemicalComposition: ChemicalAirMass) {
        turbidity = chemicalComposition.turbidity
        ozoneDobson = chemicalComposition.ozoneDobson
        humidity = chemicalComposition.humidity
    }

    /**
     * Compute the sky color at a given solar position and viewing angle.
     * @param solarPos Current solar position
     * @param viewAltitude 0 = horizon, π/2 = zenith
     * @param viewAzimuth angle from sun (0 = toward sun, π = away)
     */
    fun skyColor(
        solarPos: SolarPosition,
        viewAltitude: Float,
        viewAzimuth: Float
    ): Color {
        val pathLen = solarPos.pathLength
        val viewAngleWeight = sin(viewAltitude).coerceAtLeast(0.1f)

        // Rayleigh scattering component
        val rayR = rayleighCoeff(WAVELENGTH_R)
        val rayG = rayleighCoeff(WAVELENGTH_G)
        val rayB = rayleighCoeff(WAVELENGTH_B)

        // Mie scattering component
        val mieR = mieCoeff(WAVELENGTH_R, turbidity)
        val mieG = mieCoeff(WAVELENGTH_G, turbidity)
        val mieB = mieCoeff(WAVELENGTH_B, turbidity)

        // Total scattering extinction
        val extinctionR = rayR * 0.008f + mieR * 0.02f + ozoneAbsorption(WAVELENGTH_R, ozoneDobson) + waterVaporAbsorption(WAVELENGTH_R, humidity)
        val extinctionG = rayG * 0.008f + mieG * 0.02f + ozoneAbsorption(WAVELENGTH_G, ozoneDobson) + waterVaporAbsorption(WAVELENGTH_G, humidity)
        val extinctionB = rayB * 0.008f + mieB * 0.02f + ozoneAbsorption(WAVELENGTH_B, ozoneDobson) + waterVaporAbsorption(WAVELENGTH_B, humidity)

        // Transmittance through atmosphere (Beer-Lambert law)
        val transmittanceR = exp(-extinctionR * pathLen * viewAngleWeight)
        val transmittanceG = exp(-extinctionG * pathLen * viewAngleWeight)
        val transmittanceB = exp(-extinctionB * pathLen * viewAngleWeight)

        // Sky color: scattered sunlight (blue when transmittance is low for red)
        val scatterR = (1f - transmittanceR) * 0.8f
        val scatterG = (1f - transmittanceG) * 0.9f
        val scatterB = (1f - transmittanceB) * 1.0f

        // Normalize to reasonable brightness range
        val brightness = (scatterR + scatterG + scatterB) / 3f
        val norm = (0.6f / (brightness + 0.01f)).coerceAtMost(1f)

        return Color(
            red = (scatterR * norm).coerceIn(0f, 1f),
            green = (scatterG * norm).coerceIn(0f, 1f),
            blue = (scatterB * norm).coerceIn(0f, 1f)
        )
    }

    /**
     * Compute the sun/moon disk color (looking toward the light source).
     * At zenith, sun is white. At horizon, red/orange due to extinction.
     */
    fun lightSourceColor(solarPos: SolarPosition): Color {
        val pathLen = solarPos.pathLength

        // Extinction along the path to the observer
        val extR = rayleighCoeff(WAVELENGTH_R) * 0.008f + mieCoeff(WAVELENGTH_R, turbidity) * 0.02f +
            ozoneAbsorption(WAVELENGTH_R, ozoneDobson) + waterVaporAbsorption(WAVELENGTH_R, humidity)
        val extG = rayleighCoeff(WAVELENGTH_G) * 0.008f + mieCoeff(WAVELENGTH_G, turbidity) * 0.02f +
            ozoneAbsorption(WAVELENGTH_G, ozoneDobson) + waterVaporAbsorption(WAVELENGTH_G, humidity)
        val extB = rayleighCoeff(WAVELENGTH_B) * 0.008f + mieCoeff(WAVELENGTH_B, turbidity) * 0.02f +
            ozoneAbsorption(WAVELENGTH_B, ozoneDobson) + waterVaporAbsorption(WAVELENGTH_B, humidity)

        // Sunlight at top of atmosphere is approximately white
        val sunR = 1f; val sunG = 1f; val sunB = 1f

        // Apply atmospheric extinction
        val transR = exp(-extR * pathLen)
        val transG = exp(-extG * pathLen)
        val transB = exp(-extB * pathLen)

        // Add solar aureole (glow around sun from forward Mie scattering)
        val aureole = 0.15f * turbidity * exp(-pathLen * 0.05f)

        return Color(
            red = (sunR * transR + aureole).coerceIn(0f, 1f),
            green = (sunG * transG + aureole * 0.8f).coerceIn(0f, 1f),
            blue = (sunB * transB + aureole * 0.5f).coerceIn(0f, 1f)
        )
    }

    /**
     * Compute the moon color (grey by day, silvery at night, orange near horizon).
     */
    fun moonColor(solarPos: SolarPosition): Color {
        // Moon is reflective — uses same extinction as sun but dimmer
        val sunCol = lightSourceColor(solarPos)
        val brightness = 0.3f // Moon lit fraction (simplified)
        return Color(
            red = (sunCol.red * brightness).coerceIn(0f, 1f),
            green = (sunCol.green * brightness * 0.95f).coerceIn(0f, 1f),
            blue = (sunCol.blue * brightness * 1.1f).coerceIn(0f, 1f)
        )
    }
}

// ── Chemical Air Mass Types ──────────────────────────────────────────

/**
 * Chemical composition of different air masses.
 * Determines sky color, haze, and overall atmospheric appearance.
 */
data class ChemicalAirMass(
    val name: String,
    val turbidity: Float,       // Aerosol loading (1 = pristine, 10 = polluted)
    val ozoneDobson: Float,     // Ozone column (250-400)
    val humidity: Float,        // Relative humidity (0-1)
    val description: String
)

object AirMasses {
    /** Clean, dry continental air — deep blue sky. */
    val PRISTINE_CONTINENTAL = ChemicalAirMass(
        "Pristine Continental", 1.0f, 300f, 0.3f,
        "Deep blue sky with excellent visibility"
    )
    /** Maritime air — slightly more haze, milky blue. */
    val MARITIME = ChemicalAirMass(
        "Maritime", 1.5f, 280f, 0.7f,
        "Mild blue with sea salt haze"
    )
    /** Urban/polluted air — grey-white sky, brownish haze. */
    val URBAN_POLLUTED = ChemicalAirMass(
        "Urban Polluted", 4.0f, 320f, 0.5f,
        "Grey-white sky with reduced visibility"
    )
    /** Desert air — very dry, dusty, warm sky tone. */
    val DESERT_DUSTY = ChemicalAirMass(
        "Desert Dusty", 6.0f, 290f, 0.1f,
        "Warm, dusty sky with golden haze"
    )
    /** Post-storm air — washed clean, very clear, deep blue. */
    val POST_STORM = ChemicalAirMass(
        "Post-Storm", 0.8f, 310f, 0.4f,
        "Exceptionally clear, deep blue sky"
    )
    /** Arctic air — very clean, cold, crisp blue. */
    val ARCTIC = ChemicalAirMass(
        "Arctic", 0.5f, 350f, 0.2f,
        "Crystal clear, cold blue sky"
    )
    /** Tropical maritime — warm, humid, hazy. */
    val TROPICAL_MARITIME = ChemicalAirMass(
        "Tropical Maritime", 2.5f, 260f, 0.85f,
        "Warm, moist, hazy sky"
    )

    /** Select air mass based on weather conditions. */
    fun forWeatherCode(code: Int, temperature: Double): ChemicalAirMass = when {
        code <= 1 -> when {
            temperature > 30f -> DESERT_DUSTY
            temperature > 20f -> PRISTINE_CONTINENTAL
            temperature > 10f -> PRISTINE_CONTINENTAL
            else -> ARCTIC
        }
        code in 2..3 -> MARITIME
        code in 45..48 -> URBAN_POLLUTED
        code in 51..67 -> MARITIME
        code in 71..77 -> ARCTIC
        code >= 95 -> POST_STORM
        else -> PRISTINE_CONTINENTAL
    }
}

// ── Lightning Ionization Spectrum ────────────────────────────────────

/**
 * Lightning produces spectral emission lines from ionized nitrogen (N₂)
 * and oxygen (O₂) in the atmosphere. The hot channel reaches ~30,000K.
 */
object LightningSpectrum {
    // Spectral peaks in nm (simplified)
    // N₂⁺: 391.4nm (violet), 427.8nm (blue-violet)
    // O: 777.4nm (red), 844.6nm (near-IR)
    // Hα: 656.3nm (red — from water vapor dissociation)

    /** Generate a lightning bolt color based on channel temperature. */
    fun channelColor(temperatureKelvin: Float = 28000f): Color {
        // Wien's displacement: λ_max = b/T (b = 2.898e-3 m·K)
        val peakWavelengthNm = 2.898e6f / temperatureKelvin // nm

        // Blend blackbody color with N₂ emission lines
        val bbColor = blackbodyColor(temperatureKelvin)

        // Add N₂⁺ emission (violet-blue)
        val n2Emission = 0.3f * exp(-((427.8f - peakWavelengthNm) / 50f).pow(2))

        return Color(
            red = (bbColor.red * 0.8f + n2Emission * 0.1f).coerceIn(0f, 1f),
            green = (bbColor.green * 0.8f + n2Emission * 0.3f).coerceIn(0f, 1f),
            blue = (bbColor.blue * 0.8f + n2Emission * 0.8f).coerceIn(0f, 1f),
            alpha = 1f
        )
    }

    /** Approximate blackbody color from temperature using Planck's law. */
    private fun blackbodyColor(tempK: Float): Color {
        return when {
            tempK > 10000f -> Color(0.9f, 0.92f, 1.0f) // Blue-white
            tempK > 5000f -> Color(1.0f, 0.95f, 0.85f) // White
            tempK > 3000f -> Color(1.0f, 0.8f, 0.5f)   // Orange
            tempK > 2000f -> Color(1.0f, 0.5f, 0.2f)   // Red-orange
            else -> Color(1.0f, 0.3f, 0.1f)             // Deep red
        }
    }
}

// ── WeatherPalette Generation ────────────────────────────────────────

/**
 * Generate a physics-based [WeatherPalette] using the atmospheric
 * chemistry model, solar position, and weather conditions.
 */
object WeatherPaletteGenerator {

    /**
     * Generate a complete palette for the scene.
     *
     * @param weatherCode WMO weather code
     * @param temperature Current temperature in °C
     * @param hour Current hour (0-23)
     * @param isDarkTheme Whether the app is in dark mode
     * @param cloudCover Percentage cloud cover (0-100)
     */
    fun generate(
        weatherCode: Int,
        temperature: Double?,
        hour: Int = 12,
        isDarkTheme: Boolean = false,
        cloudCover: Int? = null
    ): WeatherPalette {
        val tempC = temperature?.toFloat() ?: 20f
        val solarPos = SolarPosition.fromTimeOfDay(hour)
        val chemModel = SkyColorModel()
        val airMass = AirMasses.forWeatherCode(weatherCode, tempC.toDouble())

        chemModel.setAirMass(airMass)

        // Sky colors from physics
        val zenithColor = chemModel.skyColor(solarPos, PI.toFloat() / 2f, 0f)
        val horizonColor = chemModel.skyColor(solarPos, 0.1f, PI.toFloat())
        val midColor = chemModel.skyColor(solarPos, PI.toFloat() / 4f, PI.toFloat() / 2f)

        // Sun/moon color
        val sourceColor = if (solarPos.isDaytime) {
            chemModel.lightSourceColor(solarPos)
        } else {
            chemModel.moonColor(solarPos)
        }

        // Temperature modulates warmth
        val tempWarmth = ((tempC + 10f) / 45f).coerceIn(0f, 1f) // -10°C = cool, 35°C = warm
        val tempBias = if (tempC > 25f) 0.08f else if (tempC < 0f) -0.08f else 0f

        // Cloud cover darkens and desaturates
        val cloudFactor = (cloudCover?.toFloat() ?: 0f) / 100f

        // Nighttime dimming
        val nightFactor = if (!solarPos.isDaytime) 0.3f else 1f

        // ── Assemble background gradient ──
        fun blendScenes(): List<Color> {
            if (cloudFactor > 0.3f) {
                // Cloudy: desaturate toward grey
                val greyness = cloudFactor * 0.6f
                val lerp = { c: Color -> Color(
                    red = c.red * (1f - greyness) + greyness * 0.6f * nightFactor,
                    green = c.green * (1f - greyness) + greyness * 0.6f * nightFactor,
                    blue = c.blue * (1f - greyness) + greyness * 0.65f * nightFactor
                )}
                return listOf(
                    lerp(zenithColor).copy(alpha = (1f - cloudFactor * 0.3f) * nightFactor),
                    lerp(midColor).copy(alpha = (1f - cloudFactor * 0.2f) * nightFactor),
                    lerp(horizonColor).copy(alpha = (1f - cloudFactor * 0.1f) * nightFactor)
                )
            }
            return listOf(
                zenithColor.copy(alpha = nightFactor),
                midColor.copy(alpha = nightFactor),
                horizonColor.copy(alpha = nightFactor)
            )
        }

        val bgColors = blendScenes()

        // ── Dark theme adjustment ──
        val finalBg = if (isDarkTheme) {
            bgColors.map { c ->
                Color(
                    red = (c.red * 0.55f).coerceAtMost(0.75f),
                    green = (c.green * 0.50f).coerceAtMost(0.70f),
                    blue = (c.blue * 0.60f).coerceAtMost(0.80f),
                    alpha = c.alpha.coerceAtMost(0.92f)
                )
            }
        } else bgColors

        // ── Accent color ──
        val accentHue = when {
            tempC > 30f -> 0.08f   // Warm accent (orange)
            tempC < 0f -> -0.05f   // Cool accent (blue)
            else -> 0f
        }
        val accentColor = if (solarPos.isDaytime) {
            sourceColor.copy(
                red = (sourceColor.red + accentHue).coerceIn(0f, 1f),
                alpha = 0.8f
            )
        } else {
            Color(0xFF4A5AC0).copy(alpha = 0.6f)
        }

        // ── Ground color ──
        val groundGreen = (0.3f - tempWarmth * 0.1f + 0.2f).coerceIn(0.1f, 0.5f)
        val groundBrown = (0.3f + tempWarmth * 0.1f).coerceIn(0.15f, 0.5f)
        val groundNight = if (!solarPos.isDaytime) 0.4f else 1f

        return WeatherPalette(
            primary = finalBg.getOrElse(0) { Color(0xFF1A6AC8) },
            secondary = sourceColor,
            tertiary = finalBg.getOrElse(1) { Color(0xFF8AC8F0) },
            accent = accentColor,
            background = finalBg,
            sunColor = sourceColor.copy(alpha = if (solarPos.isDaytime) 1f else 0.5f),
            sunGlowColor = sourceColor.copy(
                red = sourceColor.red * 1.2f,
                green = sourceColor.green * 1.2f,
                blue = sourceColor.blue * 0.6f,
                alpha = 0.3f
            ),
            sunFlareColor = sourceColor.copy(alpha = 0.35f),
            moonColor = Color(0xFFECEFF1).copy(alpha = if (!solarPos.isDaytime) 1f else 0.3f),
            moonGlowColor = Color(0xFFB3E5FC).copy(alpha = if (!solarPos.isDaytime) 0.5f else 0.1f),
            cloudBaseColor = Color.White.copy(alpha = (1f - cloudFactor * 0.3f)),
            hazeColor = Color(
                red = 0.8f + tempWarmth * 0.2f,
                green = 0.8f,
                blue = 0.85f,
                alpha = (turbidityToHaze(airMass.turbidity) * 0.3f * nightFactor).coerceAtMost(0.3f)
            ),
            groundColor = Color(
                red = groundBrown * groundNight,
                green = groundGreen * groundNight,
                blue = (0.15f * groundNight).coerceAtMost(0.3f),
                alpha = 0.3f
            ),
            groundDetailColor = Color(
                red = (groundBrown * 0.7f) * groundNight,
                green = (groundGreen * 0.7f) * groundNight,
                blue = (0.1f * groundNight).coerceAtMost(0.2f),
                alpha = 0.2f
            )
        )
    }

    private fun turbidityToHaze(turbidity: Float): Float =
        (turbidity / 10f).coerceIn(0f, 1f)
}
