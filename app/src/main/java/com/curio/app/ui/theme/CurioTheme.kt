package com.curio.app.ui.theme

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
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
import com.curio.app.data.AppPreferences
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

    background = CurioColors.SoftCream,
    onBackground = CurioColors.DeepPlum,

    surface                  = CurioColors.SoftCream,
    onSurface                = CurioColors.DeepPlum,
    surfaceVariant           = Color(0xFFECE2CE),
    onSurfaceVariant         = CurioColors.DeepPlum.copy(alpha = 0.75f),
    surfaceContainerLowest   = CurioColors.SoftCream,
    surfaceContainerLow      = Color(0xFFF0E8D6),
    surfaceContainer         = Color(0xFFECE2CE),
    surfaceContainerHigh     = Color(0xFFE4D7BF),
    surfaceContainerHighest  = Color(0xFFDCCDB2),

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
 * AMOLED theme style — true black. Always dark; background and surfaces are
 * pure black so OLED pixels switch fully off, with only the faintest grey
 * steps keeping cards/sheets distinguishable. Category tints are off (plain
 * theme surfaces) but the warm pastel accents stay so cards still pop.
 */
private val CurioAmoledColorScheme = darkColorScheme(
    primary           = CurioColors.CoralBlush,
    onPrimary         = CurioColors.DeepPlum,
    primaryContainer  = CurioColors.CoralBlush.copy(alpha = 0.22f),
    onPrimaryContainer = Color.White,

    secondary           = CurioColors.ButterYellow,
    onSecondary         = CurioColors.DeepPlum,
    secondaryContainer  = CurioColors.ButterYellow.copy(alpha = 0.16f),
    onSecondaryContainer = Color.White,

    tertiary           = CurioColors.SkyMint,
    onTertiary         = CurioColors.DeepPlum,
    tertiaryContainer  = CurioColors.SkyMint.copy(alpha = 0.16f),
    onTertiaryContainer = Color.White,

    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF141414),
    onSurfaceVariant = Color(0xFFB4B4B4),
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0A0A0A),
    surfaceContainer = Color(0xFF111111),
    surfaceContainerHigh = Color(0xFF181818),
    surfaceContainerHighest = Color(0xFF202020),

    error = CurioColors.WarmCoralRed,
    onError = Color.White,

    outline = Color.White.copy(alpha = 0.14f),
    outlineVariant = Color.White.copy(alpha = 0.07f)
)

/**
 * App-theme-aware dark check. Reads the current theme mode reactively from
 * [AppPreferences.themeModeState] so that toggling Light/Dark/System in
 * settings takes effect immediately without restarting the app.
 */
@Composable
fun isCurioDarkTheme(): Boolean {
    // AMOLED is always dark by definition (pure-black surfaces).
    if (AppPreferences.themeStyleState == AppPreferences.THEME_STYLE_AMOLED) return true
    return when (AppPreferences.themeModeState) {
        "light"  -> false
        "dark"   -> true
        else     -> isSystemInDarkTheme()
    }
}

/**
 * Non-composable dark check for services/workers — mirrors [isCurioDarkTheme]
 * but reads the system night flag from [Context] instead of the @Composable
 * [isSystemInDarkTheme], so plain functions (e.g. notification tinting in
 * [com.curio.app.infrastructure.ExploreSessionService]) can resolve the same
 * dark/light state the UI uses.
 */
fun isCurioDarkThemeForContext(context: Context): Boolean {
    // AMOLED is always dark by definition (pure-black surfaces).
    if (AppPreferences.themeStyleState == AppPreferences.THEME_STYLE_AMOLED) return true
    return when (AppPreferences.themeModeState) {
        "light" -> false
        "dark" -> true
        else -> (context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}

/**
 * The Material style's CALM palette — the device's Material You hues (the
 * proper Material color given by the device, from the wallpaper) kept as
 * the identity, but MUTED into non-vibrant pastels on airy LIGHT surfaces
 * instead of the stock dynamic scheme's vivid primaries and grey
 * containers.
 *
 *  - Light mode: near-white surfaces with a whisper of the device hue
 *    (very low saturation — calm, non-vibrant), muted pastel accents
 *    (same hue, low saturation, airy lightness) with deep same-hue ink.
 *  - Dark mode: a SOFT pastel-tinted dark — lighter and calmer than the
 *    Curio midnight — carrying the same muted pastel accents, so the
 *    Material style reads light and gentle in both modes.
 */
private fun calmMaterialColorScheme(dynamic: ColorScheme, dark: Boolean): ColorScheme {
    // Every accent keeps the device hue; only saturation + lightness move.
    // Saturation is held LOW everywhere (0.08–0.36) so nothing reads
    // vibrant — the palette stays calm and pastel in every mode.
    fun hueOf(c: Color): Float = toHsl(c).h

    // Muted pastel fill + deep same-hue ink: light = airy (l 0.80), dark =
    // soft mid-tone (l 0.62) so buttons/chips read as gentle pastels over
    // the dark page with crisp deep ink on top.
    fun fill(c: Color) = fromHsl(hueOf(c), if (dark) 0.34f else 0.30f, if (dark) 0.62f else 0.80f)
    fun onFill(c: Color) = fromHsl(hueOf(c), if (dark) 0.30f else 0.36f, if (dark) 0.18f else 0.24f)
    fun container(c: Color) = fromHsl(hueOf(c), if (dark) 0.16f else 0.16f, if (dark) 0.34f else 0.92f)
    fun onContainer(c: Color) = fromHsl(hueOf(c), if (dark) 0.10f else 0.34f, if (dark) 0.90f else 0.28f)

    val ph = hueOf(dynamic.primary)
    val sh = hueOf(dynamic.secondary)
    val th = hueOf(dynamic.tertiary)

    // Surfaces — tinted with the device hue (unique, not stock grey):
    // light = near-white airy paper, dark = soft pastel-tinted night.
    val surfaceBg = if (dark) fromHsl(ph, 0.14f, 0.17f) else fromHsl(ph, 0.10f, 0.95f)
    val surfaceMain = if (dark) fromHsl(ph, 0.13f, 0.19f) else fromHsl(ph, 0.10f, 0.95f)
    val onSurface = if (dark) fromHsl(ph, 0.08f, 0.92f) else fromHsl(ph, 0.24f, 0.20f)
    val variant = if (dark) fromHsl(ph, 0.12f, 0.22f) else fromHsl(ph, 0.12f, 0.90f)
    val onVariant = if (dark) fromHsl(ph, 0.08f, 0.74f) else fromHsl(ph, 0.18f, 0.42f)

    return if (dark) darkColorScheme(
        primary = fill(dynamic.primary),
        onPrimary = onFill(dynamic.primary),
        primaryContainer = container(dynamic.primary),
        onPrimaryContainer = onContainer(dynamic.primary),
        secondary = fill(dynamic.secondary),
        onSecondary = onFill(dynamic.secondary),
        secondaryContainer = container(dynamic.secondary),
        onSecondaryContainer = onContainer(dynamic.secondary),
        tertiary = fill(dynamic.tertiary),
        onTertiary = onFill(dynamic.tertiary),
        tertiaryContainer = container(dynamic.tertiary),
        onTertiaryContainer = onContainer(dynamic.tertiary),
        background = surfaceBg,
        onBackground = onSurface,
        surface = surfaceMain,
        onSurface = onSurface,
        surfaceVariant = variant,
        onSurfaceVariant = onVariant,
        surfaceContainerLowest = fromHsl(ph, 0.15f, 0.14f),
        surfaceContainerLow = fromHsl(ph, 0.13f, 0.20f),
        surfaceContainer = fromHsl(ph, 0.12f, 0.23f),
        surfaceContainerHigh = fromHsl(ph, 0.11f, 0.27f),
        surfaceContainerHighest = fromHsl(ph, 0.10f, 0.31f),
        error = CurioColors.WarmCoralRed,
        onError = Color.White,
        outline = fromHsl(ph, 0.10f, 0.50f),
        outlineVariant = fromHsl(ph, 0.10f, 0.30f)
    ) else lightColorScheme(
        primary = fill(dynamic.primary),
        onPrimary = onFill(dynamic.primary),
        primaryContainer = container(dynamic.primary),
        onPrimaryContainer = onContainer(dynamic.primary),
        secondary = fill(dynamic.secondary),
        onSecondary = onFill(dynamic.secondary),
        secondaryContainer = container(dynamic.secondary),
        onSecondaryContainer = onContainer(dynamic.secondary),
        tertiary = fill(dynamic.tertiary),
        onTertiary = onFill(dynamic.tertiary),
        tertiaryContainer = container(dynamic.tertiary),
        onTertiaryContainer = onContainer(dynamic.tertiary),
        background = surfaceBg,
        onBackground = onSurface,
        surface = surfaceMain,
        onSurface = onSurface,
        surfaceVariant = variant,
        onSurfaceVariant = onVariant,
        surfaceContainerLowest = fromHsl(ph, 0.07f, 0.97f),
        surfaceContainerLow = fromHsl(ph, 0.11f, 0.93f),
        surfaceContainer = fromHsl(ph, 0.12f, 0.90f),
        surfaceContainerHigh = fromHsl(ph, 0.13f, 0.87f),
        surfaceContainerHighest = fromHsl(ph, 0.14f, 0.84f),
        error = CurioColors.WarmCoralRed,
        onError = CurioColors.CreamWhite,
        outline = fromHsl(ph, 0.16f, 0.55f),
        outlineVariant = fromHsl(ph, 0.12f, 0.80f)
    )
}

/**
 * The [ColorScheme] the active theme style wears — Curio (warm cream /
 * midnight), AMOLED (pure black), or the device's Material hues calmed
 * into muted pastels (see [calmMaterialColorScheme]). Shared by
 * [CurioTheme] and the floating explore bubble, which renders outside an
 * Activity window and therefore can't use the [CurioTheme] window
 * SideEffect.
 */
@Composable
fun curioColorScheme(): ColorScheme {
    val context = LocalContext.current
    val isDark = isCurioDarkTheme()
    // Theme style decides the color scheme:
    //  - Curio (default): the warm cream/midnight palettes, unchanged.
    //  - AMOLED: the pure-black scheme (always dark).
    //  - Material: the device's Material You hues from the wallpaper,
    //    CALMED into non-vibrant pastels on light airy surfaces (light) or
    //    a soft pastel-tinted dark (dark) — still following the
    //    Light/Dark/System setting.
    return when (AppPreferences.themeStyleState) {
        AppPreferences.THEME_STYLE_AMOLED -> CurioAmoledColorScheme
        AppPreferences.THEME_STYLE_MATERIAL ->
            // Material You's dynamic palette requires API 31 (Android 12);
            // on older devices fall back to the Curio palettes so the
            // style toggle stays harmless everywhere.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val dynamic = if (isDark) dynamicDarkColorScheme(context)
                              else dynamicLightColorScheme(context)
                calmMaterialColorScheme(dynamic, isDark)
            } else {
                if (isDark) CurioDarkColorScheme else CurioLightColorScheme
            }
        else -> if (isDark) CurioDarkColorScheme else CurioLightColorScheme
    }
}

@Composable
fun CurioTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = curioColorScheme()
    // The SideEffect block below is NOT a @Composable context, so resolve
    // the theme-mode dark check here (isCurioDarkTheme is @Composable).
    val isDark = isCurioDarkTheme()

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
