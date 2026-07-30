package com.curio.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Curio's M3 theme wrapper — see CURIO_SPEC.md §0.
 *
 * Combines:
 * - CurioColors (light + dark variants — pastel & playful palette)
 * - CurioTypography (geom for display, M3 default for body)
 * - CurioShapes (rounder than M3 default per §0.3)
 *
 * Usage:
 * ```kotlin
 * CurioTheme {
 *     // your composables
 * }
 * ```
 */

private val CurioLightColorScheme = lightColorScheme(
    primary           = CurioColors.CoralBlush,
    onPrimary         = CurioColors.DeepPlum,
    primaryContainer  = CurioColors.Lilac,            // soft tint base
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

    surface           = CurioColors.CreamWhite,
    onSurface         = CurioColors.DeepPlum,
    surfaceVariant    = CurioColors.SoftSand,
    onSurfaceVariant  = CurioColors.DeepPlum.copy(alpha = 0.75f),

    error             = CurioColors.WarmCoralRed,
    onError           = CurioColors.CreamWhite,

    outline           = CurioColors.DeepPlum.copy(alpha = 0.20f),
    outlineVariant    = CurioColors.DeepPlum.copy(alpha = 0.10f)
)

private val CurioDarkColorScheme = darkColorScheme(
    primary           = CurioColors.CoralBlush,
    onPrimary         = CurioColors.DeepPlum,
    primaryContainer  = CurioColors.CoralBlush.copy(alpha = 0.30f),
    onPrimaryContainer = CurioColors.CreamWhite,

    secondary           = CurioColors.ButterYellow,
    onSecondary         = CurioColors.DeepPlum,
    secondaryContainer  = CurioColors.ButterYellow.copy(alpha = 0.20f),
    onSecondaryContainer = CurioColors.CreamWhite,

    tertiary           = CurioColors.SkyMint,
    onTertiary         = CurioColors.DeepPlum,
    tertiaryContainer  = CurioColors.SkyMint.copy(alpha = 0.20f),
    onTertiaryContainer = CurioColors.CreamWhite,

    background = Color(0xFF1A1219),  // warm dark plum (not pure black)
    onBackground = CurioColors.CreamWhite,

    surface           = Color(0xFF221820),
    onSurface         = CurioColors.CreamWhite,
    surfaceVariant    = Color(0xFF2D2229),
    onSurfaceVariant  = CurioColors.CreamWhite.copy(alpha = 0.75f),

    error             = CurioColors.WarmCoralRed,
    onError           = CurioColors.CreamWhite,

    outline           = CurioColors.CreamWhite.copy(alpha = 0.20f),
    outlineVariant    = CurioColors.CreamWhite.copy(alpha = 0.10f)
)

/**
 * @param themeMode "light", "dark", or "system" — persisted via [AppPreferences].
 */
@Composable
fun CurioTheme(
    themeMode: String = "system",
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "light"  -> false
        "dark"   -> true
        else     -> isSystemInDarkTheme()  // "system" or unknown
    }
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