package com.curio.app.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text

/**
 * Curio's icon system — see CURIO_SPEC.md §0.6.
 *
 * **NO emoji anywhere** in the app. All icons come from the Material Symbols
 * variable font (inherited from the legacy FieldMind app at
 * `app-legacy/src/main/res/font/material_symbols_outlined.ttf`, copied into
 * `app/src/main/res/font/`).
 *
 * Icons are rendered as Text composables using the ligature names from the
 * Material Symbols glyph catalog. The font is bound to [MaterialSymbolsFontFamily]
 * (see CurioTypography.kt).
 *
 * Glyph constants live in [CurioIcons]. Glyph names use snake_case to match
 * the Material Symbols catalog exactly.
 */
object CurioIcons {

    // ── Category glyphs (CURIO_SPEC.md §0.6 — used everywhere a category appears)
    const val Music       = "album"        // vinyl record
    const val Movies      = "movie"        // clapperboard
    const val Books       = "menu_book"    // open book
    const val VisualArt   = "palette"      // artist palette
    const val Science     = "science"      // atom/flask
    const val Wildcard    = "casino"       // die

    // ── UI affordance glyphs (from Material Symbols catalog)
    const val Menu        = "menu"             // ☰ — top-left
    const val Home        = "home"             // house — bottom nav Home tab
    const val Person      = "person"           // top-right avatar
    const val Search      = "search"           // top-right magnifier
    const val Settings    = "settings"         // cog
    const val MoreVert    = "more_vert"        // ⋮ — overflow
    const val Close       = "close"            // X
    const val ArrowBack   = "arrow_back"       // ← — top-left back arrow
    const val ArrowForward = "arrow_forward"
    const val Check       = "check"            // ✓
    const val Add         = "add"              // +
    const val AutoAwesome = "auto_awesome"     // sparkles / logomark
    const val Inventory2  = "inventory_2"      // cabinet empty state
    const val SearchOff   = "search_off"       // no-results state
    const val History     = "history"          // topic history empty
    const val DragHandle  = "drag_handle"      // ⋮ — manage categories drag
    const val Info        = "info"
    const val Edit        = "edit"
    const val Share       = "share"
    const val Delete      = "delete"
    const val Replay      = "replay"
    const val Refresh     = "refresh"
    const val Star        = "star"
    const val StarOutline = "star_outline"
    const val Mic         = "mic"
    const val MicNone     = "mic_none"
    const val Image       = "image"
    const val PlayArrow   = "play_arrow"
    const val Pause       = "pause"
    const val Stop        = "stop"
    const val Timer       = "timer"
    const val KeyboardArrowDown = "keyboard_arrow_down"  // ▼ — chevron
    const val KeyboardArrowUp   = "keyboard_arrow_up"    // ▲ — chevron
    const val Casino      = "casino"
    const val Album       = "album"
    const val Movie       = "movie"
    const val MenuBook    = "menu_book"
    const val Palette     = "palette"
    const val ScienceGlyph = "science"

    // ── Backup / restore glyphs (Settings → Backup & restore)
    const val Backup       = "backup"        // cloud upload — export data
    const val Restore      = "restore"       // cloud download — import data

    // ── Status / report glyphs
    const val ErrorOutline = "error_outline"
    const val BugReport     = "bug_report"
    const val Warning       = "warning"
    const val Notifications = "notifications"
    const val Schedule      = "schedule"
    const val LocalFire     = "local_fire_department"
    const val DarkMode      = "dark_mode"
}

/**
 * Renders a Material Symbols glyph via ligature.
 *
 * @param name Material Symbols ligature name (e.g. "play_arrow"). See [CurioIcons].
 * @param contentDescription Accessibility description.
 * @param modifier Standard [Modifier].
 * @param tint Glyph tint. Defaults to [LocalContentColor.current].
 * @param size Glyph box size (also used for sp size).
 * @param weight Font weight (Normal/Bold). Defaults to Normal.
 */
@Composable
fun CurioIcon(
    name: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp,
    weight: FontWeight = FontWeight.Normal
) {
    Box(
        modifier = modifier
            .size(size)
            .semantics(mergeDescendants = true) {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
                this.role = Role.Image
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            fontFamily = MaterialSymbolsFontFamily,
            fontWeight = weight,
            fontSize = size.value.sp,
            color = tint,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            style = TextStyle(lineHeight = size.value.sp)
        )
    }
}