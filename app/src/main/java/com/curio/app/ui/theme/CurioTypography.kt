package com.curio.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.curio.app.R

/**
 * Curio's typography — see CURIO_SPEC.md §0.4.
 *
 * Display / headline: `geom.ttf` (variable font, inherited from the legacy
 * FieldMind app at `app-legacy/src/main/res/font/geom.ttf`). Heavy weight
 * (700+) for headlines and titles.
 *
 * Body / UI text: a clean neutral sans (M3 default) for readability in
 * long essay/journal entries.
 *
 * Rule of thumb: `geom` for anything short and emotional (titles, empty-state
 * copy, button labels). Neutral sans for anything long or functional.
 */

/** Display/headline font family — geom.ttf. Variable font, all weights. */
val GeomFontFamily: FontFamily = FontFamily(
    Font(R.font.geom, FontWeight.Normal),
    Font(R.font.geom, FontWeight.Medium),
    Font(R.font.geom, FontWeight.SemiBold),
    Font(R.font.geom, FontWeight.Bold),
    Font(R.font.geom, FontWeight.ExtraBold)
)

/** Material Symbols glyph font family — for CurioIcon rendering. */
val MaterialSymbolsFontFamily: FontFamily = FontFamily(
    Font(R.font.material_symbols_outlined, FontWeight.Normal),
    Font(R.font.material_symbols_outlined, FontWeight.Bold)
)

/**
 * Curio typography set — Material 3 defaults overridden with geom where appropriate.
 *
 * Display family uses geom (700+ weight). Body family uses M3 default sans.
 */
val CurioTypography: Typography = Typography(
    // Display — big hero numbers, app name in splash, section openers
    displayLarge = TextStyle(
        fontFamily = GeomFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = GeomFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = GeomFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    // Headline — screen titles, big buttons
    headlineLarge = TextStyle(
        fontFamily = GeomFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = GeomFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = GeomFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    // Title — section headers, card titles, dialog titles
    titleLarge = TextStyle(
        fontFamily = GeomFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = GeomFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = GeomFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    // Body — long-form content, form fields, settings copy (M3 default neutral)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    // Label — buttons, chips, captions
    labelLarge = TextStyle(
        fontFamily = GeomFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = GeomFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = GeomFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)