package fieldmind.research.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.SchemeTonalSpot
import com.google.android.material.color.utilities.SchemeVibrant
import com.google.android.material.color.utilities.SchemeExpressive
import com.google.android.material.color.utilities.SchemeFruitSalad

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    scrim = Color.Black,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    inversePrimary = InversePrimaryDark,
    surfaceDim = SurfaceContainerLowestDark,
    surfaceBright = SurfaceContainerHighestDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    scrim = Color.Black,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    inversePrimary = InversePrimaryLight,
    surfaceDim = SurfaceContainerLowestLight,
    surfaceBright = SurfaceContainerHighestLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight
)

/**
 * Get custom color scheme based on preset name
 */
fun getCustomColorScheme(schemeName: String, darkTheme: Boolean): androidx.compose.material3.ColorScheme {
    // Check if it's a custom color scheme first
    val customScheme = parseCustomColorScheme(schemeName, darkTheme)
    if (customScheme != null) {
        return customScheme
    }
    
    return when (schemeName) {
        "Midnight Flora" -> if (darkTheme) {
            darkColorScheme(
                primary = FloraPrimaryDark,
                onPrimary = FloraOnPrimaryDark,
                primaryContainer = FloraPrimaryContainerDark,
                onPrimaryContainer = FloraOnPrimaryContainerDark,
                secondary = FloraSecondaryDark,
                onSecondary = FloraOnSecondaryDark,
                secondaryContainer = FloraSecondaryContainerDark,
                onSecondaryContainer = FloraOnSecondaryContainerDark,
                tertiary = FloraTertiaryDark,
                onTertiary = FloraOnTertiaryDark,
                tertiaryContainer = FloraTertiaryContainerDark,
                onTertiaryContainer = FloraOnTertiaryContainerDark,
                error = FloraErrorDark,
                onError = FloraOnErrorDark,
                errorContainer = FloraErrorContainerDark,
                onErrorContainer = FloraOnErrorContainerDark,
                background = FloraBackgroundDark,
                onBackground = FloraOnBackgroundDark,
                surface = FloraSurfaceDark,
                onSurface = FloraOnSurfaceDark,
                surfaceVariant = FloraSurfaceVariantDark,
                onSurfaceVariant = FloraOnSurfaceVariantDark,
                outline = FloraOutlineDark,
                outlineVariant = FloraOutlineVariantDark,
                scrim = Color.Black,
                inverseSurface = FloraInverseSurfaceDark,
                inverseOnSurface = FloraInverseOnSurfaceDark,
                inversePrimary = FloraInversePrimaryDark,
                surfaceDim = FloraSurfaceDimDark,
                surfaceBright = FloraSurfaceBrightDark,
                surfaceContainerLowest = FloraSurfaceContainerLowestDark,
                surfaceContainerLow = FloraSurfaceContainerLowDark,
                surfaceContainer = FloraSurfaceContainerDark,
                surfaceContainerHigh = FloraSurfaceContainerHighDark,
                surfaceContainerHighest = FloraSurfaceContainerHighestDark
            )
        } else {
            lightColorScheme(
                primary = FloraPrimaryLight,
                onPrimary = FloraOnPrimaryLight,
                primaryContainer = FloraPrimaryContainerLight,
                onPrimaryContainer = FloraOnPrimaryContainerLight,
                secondary = FloraSecondaryLight,
                onSecondary = FloraOnSecondaryLight,
                secondaryContainer = FloraSecondaryContainerLight,
                onSecondaryContainer = FloraOnSecondaryContainerLight,
                tertiary = FloraTertiaryLight,
                onTertiary = FloraOnTertiaryLight,
                tertiaryContainer = FloraTertiaryContainerLight,
                onTertiaryContainer = FloraOnTertiaryContainerLight,
                error = FloraErrorLight,
                onError = FloraOnErrorLight,
                errorContainer = FloraErrorContainerLight,
                onErrorContainer = FloraOnErrorContainerLight,
                background = FloraBackgroundLight,
                onBackground = FloraOnBackgroundLight,
                surface = FloraSurfaceLight,
                onSurface = FloraOnSurfaceLight,
                surfaceVariant = FloraSurfaceVariantLight,
                onSurfaceVariant = FloraOnSurfaceVariantLight,
                outline = FloraOutlineLight,
                outlineVariant = FloraOutlineVariantLight,
                scrim = Color.Black,
                inverseSurface = FloraInverseSurfaceLight,
                inverseOnSurface = FloraInverseOnSurfaceLight,
                inversePrimary = FloraInversePrimaryLight,
                surfaceDim = FloraSurfaceDimLight,
                surfaceBright = FloraSurfaceBrightLight,
                surfaceContainerLowest = FloraSurfaceContainerLowestLight,
                surfaceContainerLow = FloraSurfaceContainerLowLight,
                surfaceContainer = FloraSurfaceContainerLight,
                surfaceContainerHigh = FloraSurfaceContainerHighLight,
                surfaceContainerHighest = FloraSurfaceContainerHighestLight
            )
        }
        "Noir Amethyst" -> if (darkTheme) {
            darkColorScheme(
                primary = AmethystPrimaryDark,
                onPrimary = AmethystOnPrimaryDark,
                primaryContainer = AmethystPrimaryContainerDark,
                onPrimaryContainer = AmethystOnPrimaryContainerDark,
                secondary = AmethystSecondaryDark,
                onSecondary = AmethystOnSecondaryDark,
                secondaryContainer = AmethystSecondaryContainerDark,
                onSecondaryContainer = AmethystOnSecondaryContainerDark,
                tertiary = AmethystTertiaryDark,
                onTertiary = AmethystOnTertiaryDark,
                tertiaryContainer = AmethystTertiaryContainerDark,
                onTertiaryContainer = AmethystOnTertiaryContainerDark,
                error = AmethystErrorDark,
                onError = AmethystOnErrorDark,
                errorContainer = AmethystErrorContainerDark,
                onErrorContainer = AmethystOnErrorContainerDark,
                background = AmethystBackgroundDark,
                onBackground = AmethystOnBackgroundDark,
                surface = AmethystSurfaceDark,
                onSurface = AmethystOnSurfaceDark,
                surfaceVariant = AmethystSurfaceVariantDark,
                onSurfaceVariant = AmethystOnSurfaceVariantDark,
                outline = AmethystOutlineDark,
                outlineVariant = AmethystOutlineVariantDark,
                scrim = Color.Black,
                inverseSurface = AmethystInverseSurfaceDark,
                inverseOnSurface = AmethystInverseOnSurfaceDark,
                inversePrimary = AmethystInversePrimaryDark,
                surfaceDim = AmethystSurfaceDimDark,
                surfaceBright = AmethystSurfaceBrightDark,
                surfaceContainerLowest = AmethystSurfaceContainerLowestDark,
                surfaceContainerLow = AmethystSurfaceContainerLowDark,
                surfaceContainer = AmethystSurfaceContainerDark,
                surfaceContainerHigh = AmethystSurfaceContainerHighDark,
                surfaceContainerHighest = AmethystSurfaceContainerHighestDark
            )
        } else {
            lightColorScheme(
                primary = AmethystPrimaryLight,
                onPrimary = AmethystOnPrimaryLight,
                primaryContainer = AmethystPrimaryContainerLight,
                onPrimaryContainer = AmethystOnPrimaryContainerLight,
                secondary = AmethystSecondaryLight,
                onSecondary = AmethystOnSecondaryLight,
                secondaryContainer = AmethystSecondaryContainerLight,
                onSecondaryContainer = AmethystOnSecondaryContainerLight,
                tertiary = AmethystTertiaryLight,
                onTertiary = AmethystOnTertiaryLight,
                tertiaryContainer = AmethystTertiaryContainerLight,
                onTertiaryContainer = AmethystOnTertiaryContainerLight,
                error = AmethystErrorLight,
                onError = AmethystOnErrorLight,
                errorContainer = AmethystErrorContainerLight,
                onErrorContainer = AmethystOnErrorContainerLight,
                background = AmethystBackgroundLight,
                onBackground = AmethystOnBackgroundLight,
                surface = AmethystSurfaceLight,
                onSurface = AmethystOnSurfaceLight,
                surfaceVariant = AmethystSurfaceVariantLight,
                onSurfaceVariant = AmethystOnSurfaceVariantLight,
                outline = AmethystOutlineLight,
                outlineVariant = AmethystOutlineVariantLight,
                scrim = Color.Black,
                inverseSurface = AmethystInverseSurfaceLight,
                inverseOnSurface = AmethystInverseOnSurfaceLight,
                inversePrimary = AmethystInversePrimaryLight,
                surfaceDim = AmethystSurfaceDimLight,
                surfaceBright = AmethystSurfaceBrightLight,
                surfaceContainerLowest = AmethystSurfaceContainerLowestLight,
                surfaceContainerLow = AmethystSurfaceContainerLowLight,
                surfaceContainer = AmethystSurfaceContainerLight,
                surfaceContainerHigh = AmethystSurfaceContainerHighLight,
                surfaceContainerHighest = AmethystSurfaceContainerHighestLight
            )
        }
        "Warm Terrain" -> if (darkTheme) {
            darkColorScheme(
                primary = TerrainPrimaryDark,
                onPrimary = TerrainOnPrimaryDark,
                primaryContainer = TerrainPrimaryContainerDark,
                onPrimaryContainer = TerrainOnPrimaryContainerDark,
                secondary = TerrainSecondaryDark,
                onSecondary = TerrainOnSecondaryDark,
                secondaryContainer = TerrainSecondaryContainerDark,
                onSecondaryContainer = TerrainOnSecondaryContainerDark,
                tertiary = TerrainTertiaryDark,
                onTertiary = TerrainOnTertiaryDark,
                tertiaryContainer = TerrainTertiaryContainerDark,
                onTertiaryContainer = TerrainOnTertiaryContainerDark,
                error = TerrainErrorDark,
                onError = TerrainOnErrorDark,
                errorContainer = TerrainErrorContainerDark,
                onErrorContainer = TerrainOnErrorContainerDark,
                background = TerrainBackgroundDark,
                onBackground = TerrainOnBackgroundDark,
                surface = TerrainSurfaceDark,
                onSurface = TerrainOnSurfaceDark,
                surfaceVariant = TerrainSurfaceVariantDark,
                onSurfaceVariant = TerrainOnSurfaceVariantDark,
                outline = TerrainOutlineDark,
                outlineVariant = TerrainOutlineVariantDark,
                scrim = Color.Black,
                inverseSurface = TerrainInverseSurfaceDark,
                inverseOnSurface = TerrainInverseOnSurfaceDark,
                inversePrimary = TerrainInversePrimaryDark,
                surfaceDim = TerrainSurfaceDimDark,
                surfaceBright = TerrainSurfaceBrightDark,
                surfaceContainerLowest = TerrainSurfaceContainerLowestDark,
                surfaceContainerLow = TerrainSurfaceContainerLowDark,
                surfaceContainer = TerrainSurfaceContainerDark,
                surfaceContainerHigh = TerrainSurfaceContainerHighDark,
                surfaceContainerHighest = TerrainSurfaceContainerHighestDark
            )
        } else {
            lightColorScheme(
                primary = TerrainPrimaryLight,
                onPrimary = TerrainOnPrimaryLight,
                primaryContainer = TerrainPrimaryContainerLight,
                onPrimaryContainer = TerrainOnPrimaryContainerLight,
                secondary = TerrainSecondaryLight,
                onSecondary = TerrainOnSecondaryLight,
                secondaryContainer = TerrainSecondaryContainerLight,
                onSecondaryContainer = TerrainOnSecondaryContainerLight,
                tertiary = TerrainTertiaryLight,
                onTertiary = TerrainOnTertiaryLight,
                tertiaryContainer = TerrainTertiaryContainerLight,
                onTertiaryContainer = TerrainOnTertiaryContainerLight,
                error = TerrainErrorLight,
                onError = TerrainOnErrorLight,
                errorContainer = TerrainErrorContainerLight,
                onErrorContainer = TerrainOnErrorContainerLight,
                background = TerrainBackgroundLight,
                onBackground = TerrainOnBackgroundLight,
                surface = TerrainSurfaceLight,
                onSurface = TerrainOnSurfaceLight,
                surfaceVariant = TerrainSurfaceVariantLight,
                onSurfaceVariant = TerrainOnSurfaceVariantLight,
                outline = TerrainOutlineLight,
                outlineVariant = TerrainOutlineVariantLight,
                scrim = Color.Black,
                inverseSurface = TerrainInverseSurfaceLight,
                inverseOnSurface = TerrainInverseOnSurfaceLight,
                inversePrimary = TerrainInversePrimaryLight,
                surfaceDim = TerrainSurfaceDimLight,
                surfaceBright = TerrainSurfaceBrightLight,
                surfaceContainerLowest = TerrainSurfaceContainerLowestLight,
                surfaceContainerLow = TerrainSurfaceContainerLowLight,
                surfaceContainer = TerrainSurfaceContainerLight,
                surfaceContainerHigh = TerrainSurfaceContainerHighLight,
                surfaceContainerHighest = TerrainSurfaceContainerHighestLight
            )
        }
        else -> if (darkTheme) DarkColorScheme else LightColorScheme
    }
}

/**
 * Parse custom color scheme from format: custom_primaryHex_secondaryHex_tertiaryHex
 */
fun parseCustomColorScheme(schemeName: String, darkTheme: Boolean): androidx.compose.material3.ColorScheme? {
    if (!schemeName.startsWith("custom_")) return null
    
    val parts = schemeName.split("_")
    if (parts.size != 4) return null
    
    try {
        val primaryHex = parts[1].padStart(6, '0')
        val secondaryHex = parts[2].padStart(6, '0') 
        val tertiaryHex = parts[3].padStart(6, '0')
        
        val primary = Color(("FF$primaryHex").toLong(16))
        val secondary = Color(("FF$secondaryHex").toLong(16))
        val tertiary = Color(("FF$tertiaryHex").toLong(16))
        
        // Generate proper container tints instead of using raw colors directly.
        // For light mode: container = color mixed with white (lighter tint)
        // For dark mode: container = color mixed with black (darker shade)
        val isPrimaryLight = primary.luminance() > 0.5f
        val isSecondaryLight = secondary.luminance() > 0.5f
        val isTertiaryLight = tertiary.luminance() > 0.5f
        
        val primaryContainer = if (darkTheme) {
            // Dark mode: darken the color for container
            if (isPrimaryLight) {
                val mix = 0.3f
                Color(
                    primary.red * mix + Color.Black.red * (1 - mix),
                    primary.green * mix + Color.Black.green * (1 - mix),
                    primary.blue * mix + Color.Black.blue * (1 - mix),
                    primary.alpha
                )
            } else {
                // Already dark, use muted version
                val mix = 0.5f
                Color(
                    primary.red * mix + Color(0xFF333333).red * (1 - mix),
                    primary.green * mix + Color(0xFF333333).green * (1 - mix),
                    primary.blue * mix + Color(0xFF333333).blue * (1 - mix),
                    primary.alpha
                )
            }
        } else {
            // Light mode: lighten the color for container
            val mix = if (isPrimaryLight) 0.4f else 0.25f
            Color(
                primary.red * mix + Color.White.red * (1 - mix),
                primary.green * mix + Color.White.green * (1 - mix),
                primary.blue * mix + Color.White.blue * (1 - mix),
                primary.alpha
            )
        }
        
        return if (darkTheme) {
            darkColorScheme(
                primary = primary,
                onPrimary = if (isPrimaryLight) Color(0xFF1C1B1F) else Color.White,
                primaryContainer = primaryContainer,
                onPrimaryContainer = if (primaryContainer.luminance() > 0.5f) Color(0xFF1C1B1F) else Color.White,
                secondary = secondary,
                onSecondary = if (isSecondaryLight) Color(0xFF1C1B1F) else Color.White,
                secondaryContainer = Color(
                    secondary.red * 0.3f + Color.Black.red * 0.7f,
                    secondary.green * 0.3f + Color.Black.green * 0.7f,
                    secondary.blue * 0.3f + Color.Black.blue * 0.7f,
                    secondary.alpha
                ),
                onSecondaryContainer = Color.White,
                tertiary = tertiary,
                onTertiary = if (isTertiaryLight) Color(0xFF1C1B1F) else Color.White,
                tertiaryContainer = Color(
                    tertiary.red * 0.3f + Color.Black.red * 0.7f,
                    tertiary.green * 0.3f + Color.Black.green * 0.7f,
                    tertiary.blue * 0.3f + Color.Black.blue * 0.7f,
                    tertiary.alpha
                ),
                onTertiaryContainer = Color.White,
                error = ErrorDark,
                onError = OnErrorDark,
                errorContainer = ErrorContainerDark,
                onErrorContainer = OnErrorContainerDark,
                background = BackgroundDark,
                onBackground = OnBackgroundDark,
                surface = SurfaceDark,
                onSurface = OnSurfaceDark,
                surfaceVariant = SurfaceVariantDark,
                onSurfaceVariant = OnSurfaceVariantDark,
                outline = OutlineDark,
                outlineVariant = OutlineVariantDark,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceDark,
                inverseOnSurface = InverseOnSurfaceDark,
                inversePrimary = InversePrimaryDark,
                surfaceDim = SurfaceContainerLowestDark,
                surfaceBright = SurfaceContainerHighestDark,
                surfaceContainerLowest = SurfaceContainerLowestDark,
                surfaceContainerLow = SurfaceContainerLowDark,
                surfaceContainer = SurfaceContainerDark,
                surfaceContainerHigh = SurfaceContainerHighDark,
                surfaceContainerHighest = SurfaceContainerHighestDark
            )
        } else {
            lightColorScheme(
                primary = primary,
                onPrimary = if (isPrimaryLight) Color(0xFF1C1B1F) else Color.White,
                primaryContainer = primaryContainer,
                onPrimaryContainer = if (primaryContainer.luminance() > 0.5f) Color(0xFF1C1B1F) else Color.White,
                secondary = secondary,
                onSecondary = if (isSecondaryLight) Color(0xFF1C1B1F) else Color.White,
                secondaryContainer = Color(
                    secondary.red * 0.25f + Color.White.red * 0.75f,
                    secondary.green * 0.25f + Color.White.green * 0.75f,
                    secondary.blue * 0.25f + Color.White.blue * 0.75f,
                    secondary.alpha
                ),
                onSecondaryContainer = if (isSecondaryLight) Color(0xFF1C1B1F) else Color.White,
                tertiary = tertiary,
                onTertiary = if (isTertiaryLight) Color(0xFF1C1B1F) else Color.White,
                tertiaryContainer = Color(
                    tertiary.red * 0.25f + Color.White.red * 0.75f,
                    tertiary.green * 0.25f + Color.White.green * 0.75f,
                    tertiary.blue * 0.25f + Color.White.blue * 0.75f,
                    tertiary.alpha
                ),
                onTertiaryContainer = if (isTertiaryLight) Color(0xFF1C1B1F) else Color.White,
                error = ErrorLight,
                onError = OnErrorLight,
                errorContainer = ErrorContainerLight,
                onErrorContainer = OnErrorContainerLight,
                background = BackgroundLight,
                onBackground = OnBackgroundLight,
                surface = SurfaceLight,
                onSurface = OnSurfaceLight,
                surfaceVariant = SurfaceVariantLight,
                onSurfaceVariant = OnSurfaceVariantLight,
                outline = OutlineLight,
                outlineVariant = OutlineVariantLight,
                scrim = Color.Black,
                inverseSurface = InverseSurfaceLight,
                inverseOnSurface = InverseOnSurfaceLight,
                inversePrimary = InversePrimaryLight,
                surfaceDim = SurfaceContainerLowestLight,
                surfaceBright = SurfaceContainerHighestLight,
                surfaceContainerLowest = SurfaceContainerLowestLight,
                surfaceContainerLow = SurfaceContainerLowLight,
                surfaceContainer = SurfaceContainerLight,
                surfaceContainerHigh = SurfaceContainerHighLight,
                surfaceContainerHighest = SurfaceContainerHighestLight
            )
        }
    } catch (e: Exception) {
        // Invalid custom scheme format, return null
        return null
    }
}

/**
 * Create a dynamic color scheme from HCT color
 */

@Composable
fun RhythmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    amoledTheme: Boolean = false,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to use our expressive theme
    customColorScheme: String = "Default",
    customFont: String = "System",
    fontSource: String = "SYSTEM",
    customFontPath: String? = null,
    colorSource: String = "CUSTOM",
    extractedAlbumColorsJson: String? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    val colorScheme = when {
        // Dynamic Material You colors
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Custom preset color schemes
        customColorScheme != "Default" -> getCustomColorScheme(customColorScheme, darkTheme)
        // Default Rhythm color scheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }.let { scheme ->
        // Apply AMOLED theme modifications if enabled and in dark mode
        if (amoledTheme && darkTheme) {
            scheme.copy(
                background = Color.Black,
                surface = Color.Black,
                surfaceVariant = Color(0xFF121212),
                surfaceContainer = Color(0xFF121212),
                surfaceContainerLow = Color(0xFF0A0A0A),
                surfaceContainerLowest = Color.Black,
                surfaceContainerHigh = Color(0xFF1E1E1E),
                surfaceContainerHighest = Color(0xFF2A2A2A),
                surfaceDim = Color.Black,
                surfaceBright = Color(0xFF2A2A2A)
            )
        } else scheme
    }
    
    // Load typography based on font source
    // FontLoader was part of the deleted music-player code; always use system fonts
    val typography = getTypographyForFont(customFont)
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // Enable edge-to-edge display
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            // Set system bar colors to transparent for true edge-to-edge
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT)
            window.setNavigationBarColor(android.graphics.Color.TRANSPARENT)
            
            // Handle system bar appearance based on theme
            val insetsController = WindowCompat.getInsetsController(window, view)
            
            // Status bar icons/text color
            insetsController.isAppearanceLightStatusBars = !darkTheme
            
            // Navigation bar icons/buttons color
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = Shapes,
        content = content
    )
}
