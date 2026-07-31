package com.curio.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.curio.app.data.AppPreferences
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Curio's M3 theme wrapper.
 *
 * Pastel-warm palette: soft pink primary, warm butter secondary, mint tertiary.
 * No blue tones — dark mode uses deep maroon family, light mode uses warm
 * cream/sand surfaces.
 */

private val CurioLightColorScheme = lightColorScheme(
    primary           = CurioColors.CoralBlush,
    onPrimary         = CurioColors.CreamWhite,
    primaryContainer  = CurioColors.CoralBlush.copy(alpha = 0.18f),
    onPrimaryContainer = CurioColors.DeepPlum,

    secondary           = CurioColors.ButterYellow,
    onSecondary         = CurioColors.DeepPlum,
    secondaryContainer  = CurioColors.ButterYellow.copy(alpha = 0.30f),
    onSecondaryContainer = CurioColors.DeepPlum,

    tertiary           = CurioColors.SkyMint,
    onTertiary         = CurioColors.DeepPlum,
    tertiaryContainer  = CurioColors.SkyMint.copy(alpha = 0.30f),
    onTertiaryContainer = CurioColors.DeepPlum,

    background = CurioColors.CreamWhite,
    onBackground = CurioColors.DeepPlum,

    surface                  = CurioColors.CreamWhite,
    onSurface                = CurioColors.DeepPlum,
    surfaceVariant           = CurioColors.SoftSand,
    onSurfaceVariant         = CurioColors.DeepPlum.copy(alpha = 0.75f),
    surfaceContainerLowest   = CurioColors.CreamWhite,
    surfaceContainerLow      = CurioColors.SoftSand,
    surfaceContainer         = CurioColors.SoftSand,
    surfaceContainerHigh     = Color(0xFFEDE4D6),
    surfaceContainerHighest  = Color(0xFFE3D9C8),

    error             = CurioColors.WarmCoralRed,
    onError           = CurioColors.CreamWhite,

    outline           = CurioColors.DeepPlum.copy(alpha = 0.15f),
    outlineVariant    = CurioColors.DeepPlum.copy(alpha = 0.08f)
)

private val CurioDarkColorScheme = darkColorScheme(
    primary           = CurioColors.CoralBlush,
    onPrimary         = CurioColors.DeepPlum,
    primaryContainer  = CurioColors.CoralBlush.copy(alpha = 0.25f),
    onPrimaryContainer = Color.White,

    secondary           = CurioColors.ButterYellow,
    onSecondary         = CurioColors.DeepPlum,
    secondaryContainer  = CurioColors.ButterYellow.copy(alpha = 0.18f),
    onSecondaryContainer = Color.White,

    tertiary           = CurioColors.SkyMint,
    onTertiary         = CurioColors.DeepPlum,
    tertiaryContainer  = CurioColors.SkyMint.copy(alpha = 0.18f),
    onTertiaryContainer = Color.White,

    // Android 17-style midnight layers: darker, cleaner, and more
    // dimensional while preserving the light palette untouched.
    background = Color(0xFF0B1018),
    onBackground = Color(0xFFF7F2FA),

    surface                  = Color(0xFF111722),
    onSurface                = Color(0xFFF7F2FA),
    surfaceVariant           = Color(0xFF1C2432),
    onSurfaceVariant         = Color(0xFFD4CAD3),
    surfaceContainerLowest   = Color(0xFF070B11),
    surfaceContainerLow      = Color(0xFF0E141E),
    surfaceContainer         = Color(0xFF141B27),
    surfaceContainerHigh     = Color(0xFF1D2634),
    surfaceContainerHighest  = Color(0xFF283244),

    error             = CurioColors.WarmCoralRed,
    onError           = Color.White,

    outline           = Color.White.copy(alpha = 0.15f),
    outlineVariant    = Color.White.copy(alpha = 0.08f)
)

/**
 * App-theme-aware dark check. Reads the current theme mode reactively from
 * [AppPreferences.themeModeState] so that toggling Light/Dark/System in
 * settings takes effect immediately without restarting the app.
 */
@Composable
fun isCurioDarkTheme(): Boolean = when (AppPreferences.themeModeState) {
    "light"  -> false
    "dark"   -> true
    else     -> isSystemInDarkTheme()
}

@Composable
fun CurioTheme(
    content: @Composable () -> Unit
) {
    val isDark = isCurioDarkTheme()
    val colorScheme = if (isDark) CurioDarkColorScheme else CurioLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = AndroidColor.TRANSPARENT
            @Suppress("DEPRECATION")
            window.navigationBarColor = AndroidColor.TRANSPARENT
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = CurioTypography,
        shapes      = CurioShapes,
        content     = content
    )
}
