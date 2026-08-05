package com.curio.app.features.detail

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import org.json.JSONArray
import org.json.JSONObject
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.components.CurioWatermarkBackdrop
import com.curio.app.ui.components.NotePaperCard
import com.curio.app.ui.components.WaveformExtractor
import com.curio.app.ui.components.buildRichAnnotated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import com.curio.app.data.AudioStorageManager
import com.curio.app.data.CaptureData
import com.curio.app.data.ImageStorageManager
import com.curio.app.data.CaptureFormat
import com.curio.app.data.CurioCategories
import com.curio.app.data.JournalMood
import com.curio.app.data.NotePaperColor
import com.curio.app.data.NotePaperStyle
import com.curio.app.data.TextSpan
import com.curio.app.data.CurioCategory
import com.curio.app.data.CurioEntry
import com.curio.app.data.CurioRepositoryHolder
import com.curio.app.data.FieldMindMetadata
import com.curio.app.data.formatElapsed
import com.curio.app.data.TopicCatalog
import com.curio.app.data.shortName
import com.curio.app.features.capture.formats.FilledStar
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioMoodBoardBackdrop
import com.curio.app.ui.components.MoodBoardExport
import com.curio.app.ui.components.MoodBoardFloatingCards
import com.curio.app.ui.components.MoodBoardTiles
import com.curio.app.ui.components.MoodBoardZoomOverlay
import com.curio.app.ui.components.MorphEntrance
import com.curio.app.ui.components.formatGlyph
import com.curio.app.ui.components.rememberMoodBoardZoomState
import com.curio.app.ui.components.shareComposableCard
import com.curio.app.ui.components.SoftTornBottomShape
import com.curio.app.ui.components.SoftTornSheetShape
import com.curio.app.data.AppPreferences
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.categoryBackgroundWash
import com.curio.app.ui.theme.categoryBorder
import com.curio.app.ui.theme.categoryInk
import com.curio.app.ui.theme.categorySurface
import com.curio.app.ui.theme.categorySurfaceMoodBoard
import com.curio.app.ui.theme.lightAccentTint
import com.curio.app.ui.theme.onAccent
import com.curio.app.ui.theme.pastelFillInk
import com.curio.app.ui.theme.themedAccent
import com.curio.app.ui.theme.isCurioDarkTheme
import com.curio.app.ui.theme.glyph
import com.curio.app.ui.theme.notePaperHighlight
import com.curio.app.ui.theme.notePaperInk
import com.curio.app.ui.theme.PatrickHandFontFamily
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Entry Detail — see CURIO_SPEC.md §10. Framed presentation of a saved capture.
 *
 * Upgraded with:
 *  - Room database persistence (loads from CaptureRepository)
 *  - Structured CaptureData rendering per format
 *  - MorphEntrance for hero image; topic meta + format body render at once
 *  - Delete functionality with Room
 */
@Composable
fun EntryDetailScreen(entryId: String, navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val authority = remember { "${context.packageName}.fileprovider" }
    // Observe the repository flow so edits (mood-board re-save) and deletes
    // reflect instantly when this screen regains focus.
    val entry by produceState<CurioEntry?>(initialValue = null, entryId) {
        runCatching {
            CurioRepositoryHolder.repo.observeAll().collect { entries ->
                value = entries.find { it.id == entryId }
                    ?: TopicCatalog.sampleEntries().find { it.id == entryId }
            }
        }
    }

    LaunchedEffect(entry) {
        if (entry == null) {
            kotlinx.coroutines.delay(400)
            if (entry == null) navController.popBackStack()
        }
    }

    val resolvedEntry = entry ?: return
    val cat = CurioCategories.byId(resolvedEntry.topic.categoryId)
    // The page's category tint wash — the saved-entry page wears the entry's
    // wash over the theme background (same as Spin / Save / Cabinet), so a
    // capture from the Cabinet reads in its category's color story instead of
    // a plain patch. Hoisted once and shared with the hero's gradient so the
    // hero's final stop is, by construction, exactly the page color behind it.
    val wash = cat.categoryBackgroundWash()
    // v7.5 — pastel mode lightens the hero gradient, so the hero content
    // (glyph, title, frosted bar, watermark scatter) flips from white to the
    // theme-aware onAccent ink — deep accent in light, light twin in dark.
    // White when pastel mode is off, preserving the exact pre-pastel look.
    val heroInk = cat.onAccent()
    // Frosted grid-card ink — the Date · Mood · Type card is frosted WHITE
    // glass (see the pane below), so its content flips to a deep slate that
    // reads on white in every theme — heroInk (white / accent) would vanish
    // against the white pane.
    val heroCardInk = Color(0xFF232A35)
    // v5.8 — saveable so rotation doesn't close the menu/dialog unexpectedly.
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var deleteDialogVisible by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(wash)
    ) {
        // Muted category-glyph watermark behind the content — the same
        // backdrop language as Home / Spin / the mood boards, so a saved
        // entry reads as part of the app's paper-and-glyph world.
        // Every glyph stays BELOW the hero banner: the hero's gradient
        // blend used to chop the top glyphs at its bottom edge (the "cut"
        // look). [EntryDetailHeroClearance] clears the hero with a small
        // gap; the hero card's own symbol scatter is untouched.
        CurioWatermarkBackdrop(
            activeCat = cat,
            topClearance = EntryDetailHeroClearance,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
        // ── Expressive hero banner — one composed card: the category glyph
        // watermark with the topic title UNDER it, both on a SOLID category
        // color (no gradient — the depth comes from the torn-paper seam at
        // the bottom edge), plus a frosted (blurred-glass) date + mood + type
        // grid card below the title.
        //
        // The banner runs edge-to-edge (square corners — no rounded card
        // look). Its bottom edge is clipped by a seeded SOFT torn shape with
        // ONE SOLID white under-sheet behind it: the white sheet extends
        // visibly below the seam as a very thin, uneven paper lip. Its broad
        // wave rhythm follows the hero while its fine tooth stays independent;
        // the page's wash starts only after the white sheet, so no background
        // gaps show through the teeth. The tear is seeded from the entry id,
        // so every detail page gets its own stable texture that never changes
        // when reopened.
        val blendActive = AppPreferences.themeStyleState == AppPreferences.THEME_STYLE_MATERIAL &&
            AppPreferences.materialCardBlendsState &&
            !(isCurioDarkTheme() && !AppPreferences.pastelColorsState)
        val heroStart = if (blendActive) {
            val blendStart = CurioGradients.cardGradient(cat.themedAccent()).first()
            // Keep the frosted-glass ink legible: if the blend's first stop
            // is too pale against [heroInk] (the white/onAccent content),
            // fall back to the deep category hold instead.
            if (contrastRatio(blendStart, heroInk) >= 3.0f) blendStart
            else CurioGradients.categoryCardFill(cat.themedAccent())
        } else {
            CurioGradients.categoryCardFill(cat.themedAccent())
        }
        // v7.28 — the hero is a SOLID category color, no gradient. The depth
        // comes from the torn-paper seam: the solid banner is clipped by a
        // seeded soft tear, ONE white sheet sits just behind it, and the
        // page's wash starts right after the sheet's lip.
        val tearSeed = remember(entryId) { entryId.hashCode() and 0x7fffffff }
        // v7.29 — the torn SEAM cants, not the card. The per-entry slant
        // lives INSIDE the seeded tear path itself (SoftTearParams.tilt —
        // seeded from the same tearSeed, shared by the hero and its white
        // under-sheet so the two edges stay pixel-aligned), so every detail
        // page wears its own stable hand-torn angle (reopens identically,
        // never re-rolls) while the card rectangle — the title, the frosted
        // Date · Mood · Type card and the back / more controls — stays
        // perfectly LEVEL.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(EntryDetailHeroHeight + EntryDetailSheetExtent)
        ) {
            // ── White under-sheet — ONE SOLID white sheet layered BEHIND
            // the hero's torn bottom edge. The tear lives ONLY on the hero
            // card: the sheet's top edge is the SAME seeded torn curve as
            // the hero's bottom edge (same seed → pixel-perfect alignment,
            // so the sheet's torn top hides behind the opaque hero and the
            // wavy bite marks read white through the hero's up-bites), and
            // the sheet's lower edge follows the same broad waves with a
            // thin uneven lip. Its small tooth is independent, creating a
            // believable layered-paper tear without a rigid parallel line or
            // visible gaps. (tearSeed is declared above, with the tilt.)
            // Remembered Shape instances so their internal outline caches
            // survive recompositions (built fresh in the modifier chain, the
            // caches would never hit).
            val heroTornShape = remember(tearSeed) { SoftTornBottomShape(tearSeed) }
            val sheetShape = remember(tearSeed) {
                SoftTornSheetShape(tearSeed, lip = 10.dp, baseline = 14.dp)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    // Baseline lifts the sheet's torn top above this box's
                    // own top edge (behind the hero), while the sheet extends
                    // far enough below the hero that anti-aliased wave edges
                    // cannot reveal a page-wash gap.
                    .offset(y = EntryDetailHeroHeight - 18.dp)
                    .clip(sheetShape)
                    .background(Color(0xFFFDFCF9))
            )

            // ── Hero backdrop — the SOLID category color + symbol scatter.
            // No gradient: the depth comes from the torn seam below. The
            // banner itself is NOT blurred (the frosted look belongs to the
            // date / mood / type grid card below, which carries its own
            // blurred glass pane); the glyph scatter stays sharp so it reads
            // as a deliberate patterned backdrop. The bottom edge is torn
            // with the SOFT rounded shape (small rounded textures, canted a
            // touch, NOT the sharp jagged [TornPaperShape] of the note
            // cards) — the solid hero ends in a real torn-paper seam into
            // the white sheet + page wash below instead of a gradient
            // dissolve.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(EntryDetailHeroHeight)
                    .clip(heroTornShape)
                    .background(heroStart)
            ) {
                // ── Hero watermark — a scatter of the entry's category-family
                //     symbols (instruments for Music, camera kit for Movies,
                //     books for Books, art tools for Visual Art, lab symbols
                //     for Science, curiosities for Wildcard) pinned around the
                //     banner's perimeter. Only in the hero — the page backdrop
                //     keeps its own muted glyph wash.
                HeroSymbolScatter(cat = cat)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(EntryDetailHeroHeight),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(horizontal = 28.dp)
                        // Keep the centered content (glyph + title + frosted bar)
                        // clear of the overlaid back / more buttons at the top of
                        // the banner — without this floor, a two-line title (or
                        // the frosted bar) pushes the column up under the buttons.
                        .padding(top = 80.dp, bottom = 16.dp)
                ) {
                    CurioIcon(
                        name = cat.iconGlyph,
                        contentDescription = null,
                        tint = heroInk.copy(alpha = 0.92f),
                        size = 76.dp
                    )
                    Spacer(Modifier.height(14.dp))

                    // ── Title — the topic name on a TRANSPARENT glass pane.
                    // The letters are crisp ExtraBold geom in the hero ink
                    // (no gradient inside the glyphs); the pane behind them is
                    // a translucent tint + hairline rim — the same transparent
                    // glass pill language as the Home hero's top-bar controls
                    // — so the banner color shows straight through the plate
                    // with NO blur.
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        // 18% — strong enough that the pane still READS as a
                        // card on an airy pastel banner, transparent enough
                        // that the banner color shows straight through.
                        color = heroInk.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, heroInk.copy(alpha = 0.26f)),
                        shadowElevation = 0.dp
                    ) {
                        Text(
                            text = resolvedEntry.topic.name,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = heroInk,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                    Spacer(Modifier.height(18.dp))

                    // ── Frosted date / mood / type grid card — the meta
                    // card's date, mood and type segments moved into the hero
                    // on a genuine frosted-glass pane: a translucent layer
                    // that samples the gradient behind the bar, BLURS it, and
                    // renders it clipped to the card, with a white frosted-
                    // glass tint and a hairline rim so the card reads as
                    // frosted glass while
                    // the crisp hero backdrop stays sharp around it. Mood
                    // shows only when the entry has one.
                    val heroMood = resolvedEntry.moodOf()
                    val heroTypeLabel = if (resolvedEntry.captureData is CaptureData.Portfolio)
                        "Portfolio" else resolvedEntry.format.shortName
                    val heroTypeGlyph = if (resolvedEntry.captureData is CaptureData.Portfolio)
                        CurioIcons.Inventory2 else formatGlyph(resolvedEntry.format)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, heroCardInk.copy(alpha = 0.32f)),
                        shadowElevation = 0.dp
                    ) {
                        // The card's content Box: the Row below defines the
                        // height, and the frosted pane + glass tint match its
                        // size (BoxScope — the Surface content scope is NOT
                        // BoxScope, so matchParentSize must live in an
                        // explicit Box).
                        Box(Modifier.fillMaxWidth()) {
                            // ── Frosted pane — a blurred bloom of the hero's
                            // color behind the glass, clipped to the card and
                            // sitting BEHIND the crisp segments. Strong
                            // enough that the card visibly glows with its
                            // banner's color (RenderEffect on API 31+;
                            // software blur below).
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(heroStart.copy(alpha = 0.24f))
                                    .blur(18.dp)
                                    .clip(RoundedCornerShape(18.dp))
                            )
                            // Frosty white gradient — bright frosted glass at
                            // the top that lets the banner's color bloom
                            // through more and more toward the bottom edge,
                            // so the card reads as lit, colored frost rather
                            // than flat white (light passing through real
                            // frosted glass), with deep-slate content on top.
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        Brush.verticalGradient(
                                            0f to Color.White.copy(alpha = 0.95f),
                                            0.55f to Color.White.copy(alpha = 0.84f),
                                            1f to Color.White.copy(alpha = 0.68f)
                                        )
                                    )
                            )
                            Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FrostedSegment(
                                icon = CurioIcons.CalendarToday,
                                title = formatCapturedDate(resolvedEntry.capturedAtMillis),
                                subtitle = "Date",
                                ink = heroCardInk,
                                modifier = Modifier.weight(1f)
                            )
                            VerticalDivider(
                                modifier = Modifier.height(30.dp),
                                color = heroCardInk.copy(alpha = 0.25f)
                            )
                            if (heroMood != null) {
                                FrostedSegment(
                                    icon = heroMood.glyph,
                                    title = heroMood.label,
                                    subtitle = "Mood",
                                    ink = heroCardInk,
                                    modifier = Modifier.weight(1f)
                                )
                                VerticalDivider(
                                    modifier = Modifier.height(30.dp),
                                    color = heroCardInk.copy(alpha = 0.25f)
                                )
                            }
                            FrostedSegment(
                                icon = heroTypeGlyph,
                                title = heroTypeLabel,
                                subtitle = "Type",
                                ink = heroCardInk,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    // Buttons anchored in the icon's band — previously they
                    // floated mid-card (Box contentAlignment centers every
                    // child), sitting at the title's level. Now they sit at
                    // the top corners, dropped down far enough (72dp below
                    // the status bar) to sit level with the centered glyph.
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 72.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Frosted-glass controls — the same bright-white plate + deep
                // slate ink as the Date · Mood · Type card.
                CurioBackButton(
                    onClick = { navController.popBackStack() },
                    containerColor = Color.Transparent,
                    contentColor = heroCardInk,
                    modifier = Modifier.heroFrostPlate(heroCardInk, RoundedCornerShape(50))
                )
                Box {
                    Surface(
                        onClick = { menuExpanded = true },
                        shape = RoundedCornerShape(50),
                        color = Color.Transparent,
                        modifier = Modifier.heroFrostPlate(heroCardInk, RoundedCornerShape(50))
                    ) {
                        CurioIcon(
                            name = CurioIcons.MoreVert,
                            contentDescription = "More",
                            tint = heroCardInk,
                            size = 24.dp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        // Frosted-glass pane — bright white glass, rounded,
                        // with the hero's slate hairline rim: the same frost
                        // language as the back / more buttons and the
                        // Date · Mood · Type card, so the menu reads as a
                        // glass panel over the banner (no blur needed — the
                        // translucent white lets the banner's color wash
                        // through the pane).
                        containerColor = Color(0xFFF2F5F8).copy(alpha = 0.92f),
                        shape = RoundedCornerShape(18.dp),
                        tonalElevation = 0.dp,
                        shadowElevation = 10.dp,
                        border = BorderStroke(1.dp, heroCardInk.copy(alpha = 0.22f))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Share", color = heroCardInk) },
                            onClick = {
                                menuExpanded = false
                                shareComposableCard(
                                    context = context,
                                    cardSize = DpSize(400.dp, 400.dp),
                                    authority = authority,
                                    card = { CurioShareCard(entry = resolvedEntry, category = cat) }
                                )
                            },
                            leadingIcon = { CurioIcon(name = CurioIcons.Share, contentDescription = null, tint = heroCardInk, size = 20.dp) }
                        )
                        if (isMultiSectionEntry(resolvedEntry)) {
                            // Multi-section (Portfolio): reopen EVERY take in
                            // the universal editor — not just the mood board.
                            DropdownMenuItem(
                                text = { Text("Edit entry", color = heroCardInk) },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate(CurioRoutes.editEntry(resolvedEntry.id)) {
                                        launchSingleTop = true
                                    }
                                },
                                leadingIcon = { CurioIcon(name = CurioIcons.Edit, contentDescription = null, tint = heroCardInk, size = 20.dp) }
                            )
                        } else if (isMoodBoardEntry(resolvedEntry)) {
                            DropdownMenuItem(
                                text = { Text("Edit mood board", color = heroCardInk) },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate(CurioRoutes.editMoodBoard(resolvedEntry.id)) {
                                        launchSingleTop = true
                                    }
                                },
                                leadingIcon = { CurioIcon(name = CurioIcons.Edit, contentDescription = null, tint = heroCardInk, size = 20.dp) }
                            )
                        } else {
                            // Every other saved format (SoundBite, ReelNotes,
                            // Marginalia, FieldNotes, non-moodboard OpenNotebook)
                            // reopens in the universal editor preloaded with its
                            // saved data — the editEntry route already dispatches
                            // on the entry's own format.
                            DropdownMenuItem(
                                text = { Text("Edit entry", color = heroCardInk) },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate(CurioRoutes.editEntry(resolvedEntry.id)) {
                                        launchSingleTop = true
                                    }
                                },
                                leadingIcon = { CurioIcon(name = CurioIcons.Edit, contentDescription = null, tint = heroCardInk, size = 20.dp) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                deleteDialogVisible = true
                            },
                            leadingIcon = {
                                CurioIcon(name = CurioIcons.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, size = 20.dp)
                            }
                        )
                    }
                }
            }

        }

        // ── Topic meta — the title now lives INSIDE the hero above, so
        // this block keeps the chips + captured-at + meta card only.
        // 28dp top padding keeps the chips clear of the white under-sheet
        // lip (which now reaches roughly 36dp past the hero's nominal bottom).
        MorphEntrance {
            Column(modifier = Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (AppPreferences.tintWashEffective()) cat.tint
                                else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CurioIcon(name = cat.iconGlyph, contentDescription = null, tint = cat.categoryInk(), size = 14.dp)
                            Text(text = cat.displayName, style = MaterialTheme.typography.labelMedium, color = cat.categoryInk())
                        }
                    }
                    if (resolvedEntry.title != null) {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text(
                                text = resolvedEntry.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    // Legacy chip — entries imported from a FieldMind archive
                    // wear this so they stay recognizable on the detail page.
                    if (resolvedEntry.isLegacy) {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                CurioIcon(
                                    name = CurioIcons.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    size = 14.dp
                                )
                                Text(
                                    text = "Legacy",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Text(
                    text = if (AppPreferences.entryMetaEnabledState) {
                        capturedAtLabel(resolvedEntry) + " · " +
                            formatCapturedTime(resolvedEntry.capturedAtMillis)
                    } else capturedAtLabel(resolvedEntry),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // ── Custom tags (v7.17) — the labels added on the save page,
                // rendered as small #chips under the captured-at line.
                if (resolvedEntry.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        resolvedEntry.tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (AppPreferences.tintWashEffective()) cat.tint.copy(alpha = 0.14f)
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, cat.themedAccent().copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = if (AppPreferences.tintWashEffective()) cat.categoryInk() else cat.themedAccent(),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // ── Mood meta ───────────────────────────────────────────────
                // The mood is shown once, in the hero's frosted bar above — the
                // standalone mood card below was removed to avoid a duplicate.
            }
        }

        // ── Format body ────────────────────────────────────────────────
        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            FormatBody(entry = resolvedEntry, category = cat, navController = navController)
        }

        Spacer(Modifier.height(32.dp))
        }
    }

    if (deleteDialogVisible) {
        AlertDialog(
            onDismissRequest = { deleteDialogVisible = false },
            title = { Text("Delete this entry?") },
            text = { Text("This capture will be permanently removed from your Cabinet.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteDialogVisible = false
                    scope.launch {
                        // Delete every SoundBite recording — recursing through
                        // OpenNotebook wrappers and Portfolio sections.
                        resolvedEntry.captureData.audioFilePaths().forEach { path ->
                            AudioStorageManager.deleteAudio(context, path)
                        }
                        // Delete restored-from-backup image files for this
                        // entry (provider-picked photos live in their source
                        // app and need no cleanup here).
                        ImageStorageManager.deleteImagesForEntry(context, resolvedEntry.id)
                        runCatching { CurioRepositoryHolder.repo.deleteById(resolvedEntry.id) }
                        navController.popBackStack()
                    }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogVisible = false }) { Text("Cancel") }
            }
        )
    }
}

/**
 * The hero banner's height — the page watermark must stay entirely below
 * it (the backdrop's [com.curio.app.ui.components.CurioWatermarkBackdrop]
 * call passes [EntryDetailHeroClearance]), so the two are defined together
 * here and a hero-height change can't silently put glyphs back behind it.
 */
private val EntryDetailHeroHeight = 380.dp
/** Extra layout space reserved for the white sheet below the clipped hero. */
private val EntryDetailSheetExtent = 24.dp

/** Hero height + a small gap — the watermark's top clearance on this page
 *  (keeps the backdrop glyphs clear of the thin white under-sheet lip below
 *  the hero's torn edge). */
private val EntryDetailHeroClearance = EntryDetailHeroHeight + 30.dp

/**
 * The hero's frosted-glass language for small controls — bright frosted
 * WHITE (near-opaque, brighter at the top) with a hairline rim and
 * deep-slate content. Worn by the banner's back / more buttons so the
 * controls stay legible in every theme; the big Date · Mood · Type card
 * below wears the same rim and slate but a more translucent frost that
 * lets the banner color bloom through — it is the showpiece of the
 * family. (The title now sits on its own TRANSPARENT tint pill, not this
 * plate.)
 */
private val heroFrostGradient = Brush.verticalGradient(
    0f to Color.White.copy(alpha = 0.99f),
    1f to Color.White.copy(alpha = 0.94f)
)

/** [heroFrostGradient] clipped to [shape] with a hairline rim in [ink]. */
private fun Modifier.heroFrostPlate(ink: Color, shape: Shape): Modifier =
    clip(shape)
        .background(heroFrostGradient)
        .border(1.dp, ink.copy(alpha = 0.32f), shape)

/**
 * Decorative watermark for the saved-entry hero — a scatter of the entry's
 * category-family symbols (instruments for Music, camera kit for Movies,
 * books for Books, art tools for Visual Art, lab symbols for Science,
 * curiosities for Wildcard) pinned around the banner's perimeter as
 * MIRRORED PAIRS — each glyph on the left is mirrored by an equal glyph on
 * the right (same size, same alpha, opposite rotation), so the scatter
 * reads as a deliberate symmetric frame around the title instead of
 * randomly placed icons. Five tiers keep the glyphs clear of each other,
 * the centered content column (icon + title + frosted bar) and the top
 * back/more buttons, drawn in the hero's onAccent ink (white when pastel
 * mode is off) at a soft alpha so they read clearly against the gradient
 * (never a transparent wash).
 */
@Composable
private fun BoxScope.HeroSymbolScatter(cat: CurioCategory) {
    val symbols = CurioIcons.heroWatermarkSymbols(cat.family)
    // v7.5 — the scatter draws in the theme-aware onAccent ink (deep in
    // light, light twin in dark) so it stays visible on the pastel-lightened
    // gradient; solid white when pastel mode is off.
    val ink = cat.onAccent()
    // Mirrored pairs: biasX magnitude + biasY (-1..1), glyph size, rotation
    // magnitude, alpha. The left glyph is drawn at (-biasX, biasY) with
    // -rotation, the right at (+biasX, biasY) with +rotation.
    val pairs = listOf(
        // Top corners — just below the status-bar band, above the buttons.
        HeroWatermarkPair(biasX = 0.93f, biasY = -0.85f, size = 44.dp, rotation = 12f, alpha = 0.16f),
        // Inner pair under the corners — clear of the centered icon.
        HeroWatermarkPair(biasX = 0.55f, biasY = -0.64f, size = 48.dp, rotation = 8f, alpha = 0.19f),
        // Mid-edge pair — the widest, at the title's height, outside its width.
        HeroWatermarkPair(biasX = 0.94f, biasY = -0.12f, size = 56.dp, rotation = 14f, alpha = 0.21f),
        // Lower inner pair — outside the frosted bar's width.
        HeroWatermarkPair(biasX = 0.56f, biasY = 0.54f, size = 50.dp, rotation = 10f, alpha = 0.19f),
        // Bottom corners — biasY 0.80 keeps them clear of the hero's torn
        // bottom edge (the soft tear's broad up-bites can reach ~20dp into
        // the banner).
        HeroWatermarkPair(biasX = 0.94f, biasY = 0.80f, size = 44.dp, rotation = 6f, alpha = 0.16f)
    )
    pairs.forEachIndexed { i, pair ->
        // The 10-symbol family list maps 1:1 onto the 5 mirrored pairs.
        HeroWatermarkGlyph(symbols[i * 2], BiasAlignment(-pair.biasX, pair.biasY), pair.size, -pair.rotation, pair.alpha, ink)
        HeroWatermarkGlyph(symbols[i * 2 + 1], BiasAlignment(pair.biasX, pair.biasY), pair.size, pair.rotation, pair.alpha, ink)
    }
}

/** One mirrored hero watermark glyph — solid white at a soft alpha. */
@Composable
private fun BoxScope.HeroWatermarkGlyph(
    glyph: String,
    alignment: Alignment,
    size: Dp,
    rotation: Float,
    alpha: Float,
    tint: Color = Color.White
) {
    CurioIcon(
        name = glyph,
        contentDescription = null,
        tint = tint.copy(alpha = alpha),
        size = size,
        modifier = Modifier
            .align(alignment)
            .graphicsLayer { rotationZ = rotation }
    )
}

/** One mirrored hero watermark pair — the left glyph mirrors the right. */
private data class HeroWatermarkPair(
    val biasX: Float,
    val biasY: Float,
    val size: Dp,
    val rotation: Float,
    val alpha: Float
)

/**
 * True when an entry is a multi-section Portfolio (2+ takes) — these reopen
 * with EVERY take via the universal "Edit entry" flow, not just one board.
 */
private fun isMultiSectionEntry(entry: CurioEntry): Boolean =
    entry.captureData is CaptureData.Portfolio

/**
 * True when an entry renders as a plain mood board — a direct GalleryWall or
 * a Wildcard Open Notebook whose chosen sub-format is a GalleryWall. (A
 * multi-section Portfolio containing a GalleryWall is handled by the
 * "Edit entry" flow instead of this mood-board label.)
 */
private fun isMoodBoardEntry(entry: CurioEntry): Boolean =
    entry.format == CaptureFormat.GalleryWall ||
        (entry.captureData as? CaptureData.OpenNotebook)?.subFormat == CaptureFormat.GalleryWall

/** "Captured today" / "Captured yesterday" / "Captured Nd ago" label. */
private fun capturedAtLabel(entry: CurioEntry): String = when (entry.capturedAtDaysAgo) {
    0 -> "Captured today"
    1 -> "Captured yesterday"
    else -> "Captured ${entry.capturedAtDaysAgo}d ago"
}

/** Wall-clock time of a capture, e.g. "3:42 PM". */
private fun formatCapturedTime(millis: Long): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))

/** Calendar date of a capture, e.g. "Aug 2, 2026". */
private fun formatCapturedDate(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))

/**
 * Theme-aware entry meta card — sits right below the "Captured today ·
 * 3:42 PM" line and above the format body. The date and type segments now
 * live in the hero's frosted bar, so this card keeps ONLY the mood (with
 * its icon) and hides entirely when there's no mood. Plain theme surface
 * (no category tint), so it stays neutral in every theme style.
 */
@Composable
/**
 * The entry's mood — every format carries the shared mood row, and
 * OpenNotebook wildcard takes keep theirs inside
 * [CaptureData.OpenNotebook.subData], so unwrap those before reporting.
 * Shared by the hero's frosted bar and the meta card.
 */
private fun CurioEntry.moodOf(): JournalMood? = when (val d = captureData) {
    is CaptureData.Marginalia -> d.mood
    is CaptureData.ReelNotes -> d.mood
    is CaptureData.SoundBite -> d.mood
    is CaptureData.FieldNotes -> d.mood
    is CaptureData.GalleryWall -> d.mood
    is CaptureData.OpenNotebook -> when (val sub = d.subData) {
        is CaptureData.Marginalia -> sub.mood
        is CaptureData.ReelNotes -> sub.mood
        is CaptureData.SoundBite -> sub.mood
        is CaptureData.FieldNotes -> sub.mood
        is CaptureData.GalleryWall -> sub.mood
        else -> null
    }
    else -> null
}


/**
 * One half of the hero's frosted date/type bar — icon over value over a
 * "Date"/"Type" label, in the card's ink ([ink], deep slate, defined with
 * the hero's inks) so it reads on the frosted white glass in every theme
 * and pastel mode.
 */
@Composable
private fun FrostedSegment(
    icon: String,
    title: String,
    subtitle: String,
    ink: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        CurioIcon(
            name = icon,
            contentDescription = null,
            tint = ink.copy(alpha = 0.95f),
            size = 18.dp
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = ink,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = ink.copy(alpha = 0.85f)
        )
    }
}


@Composable
private fun FormatBody(
    entry: CurioEntry,
    category: CurioCategory,
    navController: NavController,
    // v7.18 — whether a SoundBite body may persist dictation straight to
    // Room. False for synthesized sub-entries (Portfolio sections /
    // OpenNotebook takes share the parent entry's id — a save there would
    // overwrite the whole entry with just that section).
    allowTranscribe: Boolean = true
) {
    // Multi-section entries render a compact section switcher that flips
    // between the individual format bodies (never merged into one page).
    if (entry.captureData is CaptureData.Portfolio) {
        PortfolioRender(entry, category, navController)
        return
    }
    when (entry.format) {
        CaptureFormat.SoundBite -> SoundBiteRender(entry, category, allowTranscribe)
        CaptureFormat.ReelNotes -> ReelNotesRender(entry, category)
        CaptureFormat.Marginalia -> MarginaliaRender(entry, category, navController)
        CaptureFormat.GalleryWall -> GalleryWallRender(entry, category, navController)
        CaptureFormat.FieldNotes -> FieldNotesRender(entry, category, navController)
        CaptureFormat.OpenNotebook -> OpenNotebookRender(entry, category, navController)
    }
}

/**
 * Multi-section render — a compact chip row switches between the entry's
 * sections; the active section's own format body renders below. Each chip
 * shows the section's format glyph + short name.
 */
@Composable
private fun PortfolioRender(entry: CurioEntry, category: CurioCategory, navController: NavController) {
    val data = entry.captureData as? CaptureData.Portfolio ?: return
    var activeIndex by rememberSaveable(entry.id) { mutableIntStateOf(0) }
    val section = data.sections.getOrNull(activeIndex) ?: return

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // ── Section switcher chips ────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            data.sections.forEachIndexed { i, s ->
                val selected = i == activeIndex
                Surface(
                    onClick = { activeIndex = i },
                    shape = RoundedCornerShape(50),
                    color = if (selected) category.themedAccent()
                            else category.categorySurface(MaterialTheme.colorScheme.surfaceVariant),
                    border = if (selected) null else category.categoryBorder(),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CurioIcon(
                            name = formatGlyph(s.format),
                            contentDescription = null,
                            tint = if (selected) category.onAccent() else MaterialTheme.colorScheme.onSurfaceVariant,
                            size = 14.dp
                        )
                        Text(
                            text = s.format.shortName,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = if (selected) category.onAccent() else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // ── Active section's format body ──────────────────────────────
        val subEntry = CurioEntry(
            id = entry.id,
            topic = entry.topic,
            format = section.format,
            captureData = section.data,
            title = section.title ?: entry.title,
            capturedAtMillis = entry.capturedAtMillis
        )
        // Synthesized section entry — must NOT persist dictation (it shares
        // the parent entry's id; a save would overwrite the whole portfolio).
        FormatBody(entry = subEntry, category = category, navController = navController, allowTranscribe = false)
    }
}

// ── Per-format render composables ─────────────────────────────────────

@Composable
private fun SoundBiteRender(
    entry: CurioEntry,
    category: CurioCategory,
    allowTranscribe: Boolean = true
) {
    val data = entry.captureData as? CaptureData.SoundBite ?: return
    val voiceToTextEnabled = AppPreferences.voiceToTextEnabledState

    // ── Transcribe on the saved note (v7.18) — a small mic chip lets a
    // saved voice note grow its text afterwards: dictation lands in the
    // entry's note field and is persisted to Room, so a take can hold BOTH
    // the voice recording and its text alongside each other.
    val detailContext = LocalContext.current
    val detailScope = rememberCoroutineScope()
    var noteTranscribing by remember { mutableStateOf(false) }
    var notePartial by remember { mutableStateOf("") }
    var noteError by remember { mutableStateOf<String?>(null) }
    val noteRecognizer = remember(detailContext, voiceToTextEnabled) {
        if (voiceToTextEnabled && SpeechRecognizer.isRecognitionAvailable(detailContext)) {
            SpeechRecognizer.createSpeechRecognizer(detailContext)
        } else {
            null
        }
    }
    fun startNoteTranscription() {
        if (!voiceToTextEnabled) return
        val recognizer = noteRecognizer ?: run {
            noteError = "Speech recognition isn't available on this device."
            return
        }
        noteTranscribing = true
        noteError = null
        notePartial = ""
        // v7.25 — cancel any prior session before starting a new one (a
        // reused recognizer can throw / go silent otherwise).
        runCatching { recognizer.cancel() }
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                noteTranscribing = false
                noteError = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech heard — try again."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out — try again."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone access is needed to transcribe."
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                        "Speech service unreachable — check your connection."
                    SpeechRecognizer.ERROR_CLIENT -> "Speech recognition isn't available on this device."
                    else -> "Couldn't transcribe — try again."
                }
                notePartial = ""
            }
            override fun onResults(results: Bundle?) {
                noteTranscribing = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                // v7.25 — some engines finish with an empty RESULTS list but
                // deliver the text as the final partial — fall back to it so
                // a finished session never lands nothing in the note.
                val final = matches?.firstOrNull { it.isNotBlank() }
                    ?: notePartial.takeIf { it.isNotBlank() }.orEmpty()
                notePartial = ""
                if (final.isNotBlank()) {
                    // Append to the saved note and persist the updated entry
                    // (REPLACE by id — the detail flow refreshes reactively).
                    detailScope.launch {
                        val merged = if (data.note.isNullOrBlank()) final else "${data.note}\n$final"
                        runCatching {
                            CurioRepositoryHolder.repo.save(
                                entry.copy(captureData = data.copy(note = merged))
                            )
                        }
                    }
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                notePartial = matches?.firstOrNull().orEmpty()
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        recognizer.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                // Same generous silence windows as the editor's dictation.
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2_500)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1_500)
            }
        )
    }
    val notePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startNoteTranscription()
        else noteError = "Microphone access is needed to transcribe."
    }
    LaunchedEffect(voiceToTextEnabled) {
        if (!voiceToTextEnabled) {
            noteRecognizer?.cancel()
            noteTranscribing = false
            notePartial = ""
            noteError = null
        }
    }
    DisposableEffect(noteRecognizer) {
        onDispose { noteRecognizer?.destroy() }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (AppPreferences.tintWashEffective()) category.tint
                else MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = category.themedAccent()
                ) {
                    CurioIcon(
                        name = CurioIcons.PlayArrow,
                        contentDescription = "Play",
                        tint = category.onAccent(),
                        size = 32.dp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        buildString {
                            append("Voice note · ${data.durationSeconds}s")
                            if (data.fileSizeBytes > 0) {
                                append(" · ${formatFileSize(data.fileSizeBytes)}")
                            }
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!data.title.isNullOrBlank()) {
                            Text(
                                data.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (data.fileSizeBytes > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = category.themedAccent().copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = data.encodingFormat,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = category.categoryInk(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── Real audio player (when file path is available) ─────────
            if (!data.audioFilePath.isNullOrBlank()) {
                AudioPlayerBar(
                    audioFilePath = data.audioFilePath,
                    accent = category.themedAccent(),
                    // Played waveform bars: keep the SAME hue family as the
                    // unplayed ones in dark mode (pastel twin, differing only
                    // by alpha) so the progress split reads consistently — a
                    // deep accent would be darker than the unplayed pastel and
                    // invert the readout on dark surfaces.
                    playedAccent = if (isCurioDarkTheme()) category.categoryInk() else category.themedAccent(),
                    // Unplayed waveform bars: the 20% tint wash is a light-
                    // mode-only color (murky, near-invisible on dark); in dark
                    // mode use the pastel ink twin so the capsule bars read.
                    tint = if (isCurioDarkTheme()) category.categoryInk() else category.tint,
                    surface = category.categorySurface(MaterialTheme.colorScheme.surfaceContainerHigh),
                    border = category.categoryBorder()
                )
            }

            // ── Transcribe the saved note (v7.18) — small mic chip that
            // dictates straight into the entry's note (persisted to Room).
            // Hidden for synthesized sub-entries (Portfolio/OpenNotebook
            // sections — a save there would overwrite the parent entry).
            if (allowTranscribe && voiceToTextEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (noteTranscribing || noteError != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = category.themedAccent().copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, category.themedAccent().copy(alpha = 0.35f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    // noteError is a delegated var → no smart
                                    // cast; Elvis keeps the arg non-null.
                                    text = noteError ?: "Listening… speak now",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (noteError != null) MaterialTheme.colorScheme.error
                                            else category.themedAccent()
                                )
                                if (notePartial.isNotBlank()) {
                                    Text(
                                        text = notePartial,
                                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        Surface(
                            onClick = {
                                noteRecognizer?.cancel()
                                noteTranscribing = false
                                notePartial = ""
                                noteError = null
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            CurioIcon(
                                name = CurioIcons.Close,
                                contentDescription = "Stop transcribing",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 16.dp,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    } else {
                        Surface(
                            onClick = {
                                val granted = ContextCompat.checkSelfPermission(
                                    detailContext, Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (voiceToTextEnabled && granted) startNoteTranscription()
                                else if (voiceToTextEnabled) notePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = category.themedAccent().copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, category.themedAccent().copy(alpha = 0.40f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CurioIcon(
                                    name = CurioIcons.Mic,
                                    contentDescription = null,
                                    tint = category.themedAccent(),
                                    size = 16.dp
                                )
                                Text(
                                    text = "Transcribe note",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = category.themedAccent()
                                )
                            }
                        }
                    }
                }
            }

            // ── Note — shown on the same note-paper slip the editor used ──
            if (!data.note.isNullOrBlank()) {
                val noteSheet = data.noteColor ?: NotePaperColor.CREAM
                NotePaperCard(
                    style = data.noteStyle ?: data.notePaperStyle(),
                    paperColor = noteSheet,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                    minHeight = 96.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        buildRichAnnotated(data.note, data.noteSpans.orEmpty(), notePaperHighlight(noteSheet)),
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = PatrickHandFontFamily),
                        color = notePaperInk(noteSheet)
                    )
                }
            }

            // ── Quote cards — shared hand-placed paper notecards ─────────
            RenderQuoteCards(
                // orEmpty() guards legacy Gson blobs where the quotes field is
            // absent — missing Kotlin-default List fields decode to null, not
            // empty (the mood-board crash).
            quotes = data.quotes.orEmpty(),
                spans = data.quoteSpans.orEmpty(),
                tilts = data.quoteTilts.orEmpty(),
                styles = data.quoteStyles.orEmpty(),
                colors = data.quoteColors.orEmpty(),
                fallbackStyle = data.notePaperStyle(),
                entryId = entry.id,
                topicName = entry.topic.name,
                category = category
            )
        }
    }
}

/**
 * Single-capsule ExoPlayer audio bar — ONE play/pause button with a real
 * waveform flowing past it, styled to mirror the recording visualizer's
 * capsule bars. The waveform is extracted from the audio file using
 * [WaveformExtractor]; played bars show in [accent], unplayed in [tint].
 * Tap or drag on the bars to seek.
 */
@Composable
private fun AudioPlayerBar(
    audioFilePath: String,
    accent: Color,
    playedAccent: Color,
    tint: Color,
    surface: Color,
    border: BorderStroke?
) {
    val context = LocalContext.current
    // v5.8 — saveable so rotation keeps the playback position + playing
    // state; the recreated player below re-seeks/resumes from them.
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var currentPosition by rememberSaveable { mutableLongStateOf(0L) }
    var duration by rememberSaveable { mutableLongStateOf(0L) }
    var sliderPosition by rememberSaveable { mutableFloatStateOf(0f) }

    // Extract waveform samples off the main thread. Same bar language as the
    // recording visualizer (LiveWaveform uses 36 capsule bars) so the saved
    // view looks identical to the meter the user recorded into.
    val waveformSamples by produceState<FloatArray>(
        initialValue = FloatArray(36),
        key1 = audioFilePath
    ) {
        value = withContext(kotlinx.coroutines.Dispatchers.Default) {
            WaveformExtractor.extract(audioFilePath, barCount = 36)
        } ?: FloatArray(36) { kotlin.random.Random.nextFloat() * 0.6f + 0.2f }
    }

    // The stored audioFilePath is a RAW absolute filesystem path (e.g.
    // /data/user/0/com.curio.app/files/audio/xyz.m4a) — feeding that string
    // straight to MediaItem.fromUri() parses it as a schemeless URI that
    // ExoPlayer's DefaultDataSource cannot resolve, so the audio would never
    // play. Wrap it in a file:// URI instead (pass through unchanged if the
    // path ever arrives already schemed, e.g. content:// from a picker).
    val audioUri = remember(audioFilePath) {
        val parsed = Uri.parse(audioFilePath)
        if (parsed.scheme != null) parsed else Uri.fromFile(File(audioFilePath))
    }
    val player = remember(audioUri) {
        ExoPlayer.Builder(context.applicationContext).build().apply {
            // Route to the media audio stream with proper focus handling —
            // without AudioAttributes some devices route to a silent output
            // or duck audio, which reads as "plays but no sound".
            setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus = */ true)
            setHandleAudioBecomingNoisy(true)
            setVolume(1f)
            setMediaItem(MediaItem.fromUri(audioUri))
            prepare()
            playWhenReady = false
        }
    }

    // v5.8 — after rotation the player is recreated fresh; resume from the
    // saveable position/state so a voice note keeps its place.
    LaunchedEffect(player) {
        if (currentPosition > 0L) player.seekTo(currentPosition)
        if (isPlaying) player.play()
    }

    // ── Observe player state ────────────────────────────────────────────
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        duration = player.duration.coerceAtLeast(0)
                    }
                    Player.STATE_ENDED -> {
                        isPlaying = false
                        // Park the player back at the start so the next tap
                        // on the play button replays instead of dead-ending.
                        currentPosition = 0L
                        sliderPosition = 0f
                        player.seekTo(0)
                    }
                    Player.STATE_IDLE -> {
                        // A failed load (missing/corrupt file) leaves the
                        // player IDLE — don't leave the UI stuck "playing".
                        isPlaying = false
                    }
                }
            }
            override fun onPlayerError(error: PlaybackException) {
                // Broken file or decode failure: reset the UI so the button
                // doesn't look stuck. DON'T seek here — by the time this
                // fires the player is typically already in the errored IDLE
                // state where seek commands are unavailable, and the call
                // would throw. The play-button retry path (prepare() on
                // IDLE) restarts from the top on the next tap.
                isPlaying = false
                currentPosition = 0L
                sliderPosition = 0f
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    // ── Poll position while playing ─────────────────────────────────────
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = player.currentPosition.coerceAtLeast(0)
            sliderPosition = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
            kotlinx.coroutines.delay(200)
        }
    }

    // ── Single-capsule player — one button, capsule bars ───────────────
    // Mirrors the recording visualizer (rounded capsule bars): the ONE
    // play/pause button sits inside the capsule with the waveform flowing
    // past it, so playback reads like the live meter seen while recording.
    Surface(
        shape = RoundedCornerShape(50),
        color = surface,
        border = border,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── The one button ─────────────────────────────────────────
            Surface(
                onClick = {
                    if (isPlaying) {
                        player.pause()
                    } else {
                        // Replay from the start: if the clip ended, ExoPlayer
                        // won't restart on play() alone — re-seek to 0 first.
                        // If it errored into IDLE, play() also won't restart
                        // it: re-prepare the media item so the retry loads.
                        if (player.playbackState == Player.STATE_ENDED) {
                            player.seekTo(0)
                        } else if (player.playbackState == Player.STATE_IDLE) {
                            player.prepare()
                        }
                        player.play()
                    }
                },
                shape = RoundedCornerShape(50),
                color = accent,
                shadowElevation = 0.dp,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CurioIcon(
                        name = if (isPlaying) CurioIcons.Pause else CurioIcons.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        // v7.5 — pastel mode lightens the accent fill, so the
                        // glyph flips to a deep ink of the accent.
                        tint = pastelFillInk(accent),
                        size = 24.dp
                    )
                }
            }

            // ── Capsule-bar visualizer (tap/drag to seek) ─────────────
            WaveformCanvas(
                samples = waveformSamples,
                progress = sliderPosition,
                accent = playedAccent,
                tint = tint,
                onSeek = { fraction ->
                    sliderPosition = fraction.coerceIn(0f, 1f)
                    val seekMs = (fraction * duration).toLong()
                    player.seekTo(seekMs)
                    currentPosition = seekMs
                },
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(17.dp))
            )

            // ── Elapsed / total ────────────────────────────────────────
            Text(
                text = "${formatMs(currentPosition)} / ${formatMs(duration)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Renders waveform capsule bars with progress coloring + seek support.
 *
 * Same bar language as the recording [LiveWaveform] (rounded capsule bars,
 * no indicator line — the accent/tint split IS the progress readout).
 *
 * @param samples  Normalized amplitude values (0.0–1.0) from [WaveformExtractor].
 * @param progress Playback progress fraction (0.0–1.0).
 * @param accent   Color for the played portion of the waveform.
 * @param tint     Color for the unplayed portion.
 * @param onSeek   Called with fraction (0.0–1.0) when the user taps or drags.
 */
@Composable
private fun WaveformCanvas(
    samples: FloatArray,
    progress: Float,
    accent: Color,
    tint: Color,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var dragFraction by remember { mutableFloatStateOf(-1f) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onSeek((offset.x / size.width).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        dragFraction = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek(dragFraction)
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragFraction = (dragFraction + dragAmount / size.width).coerceIn(0f, 1f)
                        onSeek(dragFraction)
                    },
                    onDragEnd = { dragFraction = -1f }
                )
            }
    ) {
        if (samples.isEmpty()) return@Canvas

        val barCount = samples.size
        val gap = 2.dp.toPx()
        val totalGap = gap * (barCount - 1)
        val barWidth = ((size.width - totalGap) / barCount).coerceAtLeast(1f)
        val playedIndex = (progress * barCount).toInt().coerceIn(0, barCount)

        // Capsule bars — played in accent, unplayed in tint. Matches the
        // recording LiveWaveform so saved voice notes look like the meter
        // the user recorded into.
        for (i in 0 until barCount) {
            val barHeight = samples[i] * size.height * 0.92f
            val x = i * (barWidth + gap)
            val y = (size.height - barHeight) / 2f
            val color = if (i <= playedIndex) accent else tint

            drawRoundRect(
                color = color.copy(alpha = if (i <= playedIndex) 0.95f else 0.5f),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight.coerceAtLeast(2.dp.toPx())),
                cornerRadius = CornerRadius(barWidth / 2f)
            )
        }
    }
}
private fun formatMs(ms: Long): String {
    val totalSecs = (ms / 1000).coerceAtLeast(0)
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return "%d:%02d".format(mins, secs)
}

/** Format bytes to a human-readable size string (e.g. "1.2 MB"). */
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "%.1f MB".format(bytes.toDouble() / (1024 * 1024))
    }
}

/** WCAG contrast ratio of [a] against [b] — used to keep the hero's frosted
 *  glass legible when the blend's first stop comes from the pale non-dynamic
 *  fallback palette. */
private fun contrastRatio(a: Color, b: Color): Float {
    fun linear(c: Float): Float =
        if (c <= 0.03928f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)
    fun luminance(c: Color): Float =
        0.2126f * linear(c.red) + 0.7152f * linear(c.green) + 0.0722f * linear(c.blue)
    val la = luminance(a)
    val lb = luminance(b)
    return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
}

@Composable
private fun ReelNotesRender(entry: CurioEntry, category: CurioCategory) {
    val data = entry.captureData as? CaptureData.ReelNotes
    
    // Handle null or malformed data gracefully
    if (data == null) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (AppPreferences.tintWashEffective()) category.tint.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurioIcon(
                    CurioIcons.Movie, null,
                    tint = category.categoryInk().copy(alpha = 0.5f),
                    size = 48.dp
                )
                Text(
                    "No review data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // ── Rating — Canvas stars (the Material Symbols Outlined font renders
        // even `star` as a hollow outline, so filled stars are solid paths
        // and the remainder ghost at low alpha as a 5-slot scale) ─────────
        if (data.rating > 0) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                // Soft pastel card that matches the app's palette — a light,
                // barely-there whisper of the category in light mode (lighter
                // and less saturated than the other cards on the page), and
                // the palette-matched mid-tone in dark/AMOLED where a pale
                // tint would be near-invisible.
                color = if (isCurioDarkTheme()) {
                    category.categorySurface(MaterialTheme.colorScheme.surfaceContainerHigh)
                } else {
                    lightAccentTint(category.accent, saturation = 0.18f, lightness = 0.93f)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) { i ->
                            val starFilled = i < data.rating.coerceIn(0, 5)
                            FilledStar(
                                color = if (starFilled) category.categoryInk()
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                filled = starFilled,
                                starSize = 24.dp
                            )
                        }
                    }
                    // Subtle caption under the stars — the same help language
                    // as the capture editor's rating row, so the saved card
                    // reads as the rating's label.
                    Text(
                        text = "Rate quality",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }
        }
        
        // Attached images — ALL of them, in a scrollable strip that fills
        // each tile edge-to-edge (Crop, so landscape shots read properly
        // instead of letterboxing). Tapping one magnifies it IN PLACE with
        // the same spring zoom as the saved mood board — pinch/pan refine,
        // tap closes, double-tap resets — no Lightbox navigation. Legacy
        // entries only stored a count, so keep the badge fallback below.
        // orEmpty() guards legacy Gson blobs where the imageUris field is
        // absent (missing Kotlin-default fields decode to null, not default).
        val attachedUris = data.imageUris.orEmpty()
        if (attachedUris.isNotEmpty()) {
            // v7.19 — scale + pan animate inside [MoodBoardZoomOverlay] now
            // (call-site springs removed; close uses a fast tween).
            val imageZoom = rememberMoodBoardZoomState()
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth().height(if (attachedUris.size == 1) 280.dp else 240.dp)
            ) {
                // This Compose version's BoxWithConstraintsScope is NOT a
                // Density (maxWidth.toPx() doesn't resolve), and its
                // maxWidth/maxHeight aren't reachable inside the Row lambda
                // (implicit receiver is RowScope there) — so capture the box
                // size as Dp here and convert px via LocalDensity explicitly.
                val density = LocalDensity.current
                val boxMaxWidth = maxWidth
                val boxMaxHeight = maxHeight
                // A single image goes full-width (proper landscape view);
                // multiple images are 170.dp tiles in the scrollable strip.
                val singleImage = attachedUris.size == 1
                val tileSize = 170.dp
                val tileW = if (singleImage) with(density) { boxMaxWidth.toPx() }
                else with(density) { tileSize.toPx() }
                val tileH = if (singleImage) with(density) { boxMaxHeight.toPx() }
                else with(density) { tileSize.toPx() }
                val viewW = with(density) { boxMaxWidth.toPx() }
                val viewH = with(density) { boxMaxHeight.toPx() }
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier.fillMaxSize().horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    attachedUris.forEachIndexed { idx, uri ->
                        Surface(
                            onClick = {
                                // v7.24 — report the tile's VIEWPORT spot so
                                // the image glides from exactly where it sits
                                // (scroll offset applied) to the strip's
                                // center. Single full-width images sit at 0.
                                val viewportX = if (singleImage) 0f
                                else idx * (tileW + with(density) { 8.dp.toPx() }) - scrollState.value
                                imageZoom.zoomIn(
                                    uri,
                                    centerX = viewportX + tileW / 2f,
                                    centerY = tileH / 2f,
                                    tileW = tileW,
                                    tileH = tileH,
                                    viewW = viewW,
                                    viewH = viewH
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 0.dp,
                            modifier = Modifier.size(
                                if (singleImage) boxMaxWidth else tileSize,
                                if (singleImage) boxMaxHeight else tileSize
                            )
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Attached image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                // In-place zoom overlay — LAST child of the box (same as the
                // saved mood board): glides the tapped image from its spot to
                // the strip's center (arc), pinch/pan refine, tap closes.
                attachedUris.firstOrNull { it == imageZoom.zoomedUri }?.let { uri ->
                    MoodBoardZoomOverlay(
                        zoomState = imageZoom,
                        tileUri = uri,
                        tileX = if (singleImage) 0f
                            else attachedUris.indexOf(uri) * (tileW + with(density) { 8.dp.toPx() }) - scrollState.value,
                        tileY = 0f,
                        widthPx = tileW,
                        heightPx = tileH,
                        viewW = viewW,
                        viewH = viewH
                    )
                }
            }
        } else if (data.imageCount > 0) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (AppPreferences.tintWashEffective()) category.tint
                        else MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CurioIcon(
                        CurioIcons.Image, null,
                        tint = category.categoryInk(),
                        size = 18.dp
                    )
                    Text(
                        "${data.imageCount} image${if (data.imageCount != 1) "s" else ""} attached",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        // Review text with better styling — wears the same note-paper box
        // as the Marginalia journal, so the saved review reads as written
        // on paper in light AND dark mode (NotePaperCard is theme-aware and
        // renders the torn-note style when that's what was chosen).
        val reviewSheet = data.reviewColor ?: NotePaperColor.CREAM
        if (!data.reviewText.isNullOrBlank()) {
            NotePaperCard(
                style = data.reviewStyle ?: data.notePaperStyle(),
                paperColor = reviewSheet,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                minHeight = 96.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    buildRichAnnotated(data.reviewText, data.reviewSpans.orEmpty(), notePaperHighlight(reviewSheet)),
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = PatrickHandFontFamily),
                    color = notePaperInk(reviewSheet)
                )
            }
        } else {
            // Fallback when no review text
            NotePaperCard(
                style = data.reviewStyle ?: data.notePaperStyle(),
                paperColor = reviewSheet,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                minHeight = 96.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "No review written yet",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PatrickHandFontFamily),
                    color = notePaperInk(reviewSheet).copy(alpha = 0.55f)
                )
            }
        }

        // ── Quote cards — shared hand-placed paper notecards ─────────────
        RenderQuoteCards(
            // orEmpty() guards legacy Gson blobs where the quotes field is
            // absent — missing Kotlin-default List fields decode to null, not
            // empty (the mood-board crash).
            quotes = data.quotes.orEmpty(),
            spans = data.quoteSpans.orEmpty(),
            tilts = data.quoteTilts.orEmpty(),
            styles = data.quoteStyles.orEmpty(),
            colors = data.quoteColors.orEmpty(),
            fallbackStyle = data.notePaperStyle(),
            entryId = entry.id,
            topicName = entry.topic.name,
            category = category
        )
    }
}

@Composable
private fun MarginaliaRender(entry: CurioEntry, category: CurioCategory, navController: NavController) {
    val data = entry.captureData as? CaptureData.Marginalia ?: return
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        data.fieldMindMetadata?.let { metadata ->
            FieldMindMetadataCard(metadata = metadata, category = category)
        }
        // ── Journal — "My thoughts" on a note-paper page ──────────────
        if (!data.journalText.isNullOrBlank()) {
            MarginaliaSectionHeader(label = "My thoughts", category = category)
            val journalSheet = data.journalColor ?: NotePaperColor.CREAM
            NotePaperCard(
                style = data.journalStyle ?: data.notePaperStyle(),
                paperColor = journalSheet,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                minHeight = 96.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    buildRichAnnotated(
                        data.journalText,
                        data.journalSpans.orEmpty(),
                        notePaperHighlight(journalSheet)
                    ),
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = PatrickHandFontFamily),
                    color = notePaperInk(journalSheet)
                )
            }
        }

        // ── Favorite quotes — shared hand-placed paper notecards ─────────
        RenderQuoteCards(
            // orEmpty() guards legacy Gson blobs where the quotes field is
            // absent — missing Kotlin-default List fields decode to null, not
            // empty (the mood-board crash).
            quotes = data.quotes.orEmpty(),
            spans = data.quoteSpans.orEmpty(),
            tilts = data.quoteTilts.orEmpty(),
            styles = data.quoteStyles.orEmpty(),
            colors = data.quoteColors.orEmpty(),
            fallbackStyle = data.notePaperStyle(),
            entryId = entry.id,
            topicName = entry.topic.name,
            category = category
        )

        // ── Attachments — gallery images + optional voice note ─────────
        // (orEmpty() guards legacy blobs where imageUris is absent.)
        // ALL attached images show in a scrollable strip — the journal can
        // hold up to 6 now, and a fixed-width tile lets them all be seen
        // (the old weight row silently dropped everything past 3).
        val attachedUris = data.imageUris.orEmpty()
        if (attachedUris.isNotEmpty()) {
            // A lone image goes full-width (proper landscape view, matching
            // Reel Notes); multiple images are fixed tiles in the scrollable
            // strip so all of them are reachable (the old weight row
            // silently dropped anything past 3).
            val singleImage = attachedUris.size == 1
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            ) {
                attachedUris.forEach { uri ->
                    Surface(
                        onClick = {
                            navController.navigate(CurioRoutes.lightbox(uri)) {
                                launchSingleTop = true
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 0.dp,
                        modifier = if (singleImage) Modifier.fillMaxWidth().height(280.dp)
                                   else Modifier.size(150.dp, 120.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Open image",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(4.dp)
                        )
                    }
                }
            }
        }
        if (!data.audioFilePath.isNullOrBlank()) {
            AudioPlayerBar(
                audioFilePath = data.audioFilePath,
                accent = category.themedAccent(),
                // Same theme-aware played/unplayed split as the SoundBite
                // section (pastel pair in dark, deep-accent pair in light).
                playedAccent = if (isCurioDarkTheme()) category.categoryInk() else category.themedAccent(),
                tint = if (isCurioDarkTheme()) category.categoryInk() else category.tint,
                surface = category.categorySurface(MaterialTheme.colorScheme.surfaceContainerHigh),
                border = category.categoryBorder()
            )
        }
    }
}

/**
 * Shared saved-view quote cards — the hand-placed paper notecards used by
 * Marginalia, Reel Notes, Sound Bite and the Mood Board detail pages. Pads
 * quoteSpans to the quotes length (legacy Gson blobs may carry fewer/absent
 * span lists), keeps the ORIGINAL index through the blank filter so each
 * card can find its saved tilt (quoteTilts parallels quotes 1:1), and
 * renders the section header + a rotated paper card per non-blank quote.
 */
@Composable
private fun RenderQuoteCards(
    quotes: List<String>,
    spans: List<List<TextSpan>>,
    tilts: List<Float>,
    styles: List<NotePaperStyle>,
    colors: List<NotePaperColor>,
    fallbackStyle: NotePaperStyle,
    entryId: String,
    topicName: String,
    category: CurioCategory,
    label: String = "Favorite quotes",
    // v7.22 — optional per-index filter (mood board: render only the
    // BELOW-board subset; the on-board ones float on the collage).
    includeIndex: ((Int) -> Boolean)? = null
) {
    val context = LocalContext.current
    // Legacy Gson blobs decode missing Kotlin-default List fields to NULL
    // (not empty) — a null [quotes] crashed the saved mood-board detail view
    // here (.size() on null). Guard defensively so no caller can reintroduce
    // the crash.
    val safeQuotes = quotes.orEmpty()
    // Pad spans to the quotes length first (legacy Gson blobs may carry
    // fewer/absent span lists), then zip so the spans stay aligned with
    // their quote even when blank cards are filtered out. Keep the ORIGINAL
    // index through the blank filter so each card can find its saved tilt.
    val spansPadded = spans.toMutableList()
    while (spansPadded.size < safeQuotes.size) spansPadded.add(emptyList())
    val quotePairs = safeQuotes.zip(spansPadded).mapIndexedNotNull { i, pair ->
        if (pair.first.isNullOrBlank()) null
        // Skip indices the caller excluded (mood board below-board split).
        else if (includeIndex != null && !includeIndex(i)) null
        else i to pair
    }
    if (quotePairs.isNotEmpty()) {
        MarginaliaSectionHeader(label = label, category = category, count = quotePairs.size)
        quotePairs.forEach { (origIndex, pair) ->
            val (quote, cardSpans) = pair
            // The tilt SAVED with this card (the exact angle the user added
            // with — never re-rolled). Legacy entries lack the field → fall
            // back to a stable random tilt keyed by the original index so
            // viewing never re-rolls it either.
            val rotation = tilts.getOrNull(origIndex)
                ?: remember(origIndex) { kotlin.random.Random.nextFloat() * 5f - 2.5f }
            val quoteSheet = colors.getOrNull(origIndex) ?: NotePaperColor.CREAM
            NotePaperCard(
                style = styles.getOrNull(origIndex) ?: fallbackStyle,
                paperColor = quoteSheet,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                corner = 12.dp,
                // Hoist the 72dp floor INTO the modifier chain BEFORE the tilt
                // rotate: passing it as NotePaperCard's minHeight param appends
                // heightIn AFTER the call-site rotate (the card layer grew to
                // 72dp and the rotation pivot shifted for single-line quotes).
                // With heightIn first, the tilt pivots around the CONTENT's
                // center and stays put whether the quote is one line or five.
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .rotate(rotation)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CurioIcon(
                        name = CurioIcons.FormatQuote,
                        contentDescription = null,
                        tint = notePaperInk(quoteSheet).copy(alpha = 0.45f),
                        size = 20.dp
                    )
                    Text(
                        // Spans shift +1 to account for the curly-quote
                        // wrapper added around the saved quote text.
                        text = buildRichAnnotated(
                            "\u201C$quote\u201D",
                            cardSpans.map { it.copy(start = it.start + 1, end = it.end + 1) },
                            notePaperHighlight(quoteSheet)
                        ),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = PatrickHandFontFamily),
                        color = notePaperInk(quoteSheet)
                    )
                    // ── Bookmark — saves the quote to the Home "Saved" shelf ──
                    val saved = AppPreferences.savedQuotesState.any {
                        it.entryId == entryId && it.quoteText == quote
                    }
                    Surface(
                        onClick = {
                            // Re-read the LATEST saved state on tap instead of
                            // trusting the composition-time snapshot, so the
                            // toggle always flips exactly ONE card's bookmark
                            // (each card is keyed by entryId + its own text).
                            val isSavedNow = AppPreferences.savedQuotesState.any {
                                it.entryId == entryId && it.quoteText == quote
                            }
                            if (isSavedNow) {
                                AppPreferences.removeSavedQuote(context, entryId, quote)
                            } else {
                                AppPreferences.saveQuote(
                                    context, entryId, topicName, category.id, quote
                                )
                            }
                        },
                        shape = CircleShape,
                        color = if (saved) category.themedAccent().copy(alpha = 0.16f)
                                else Color.Transparent
                    ) {
                        CurioIcon(
                            name = if (saved) CurioIcons.Bookmark else CurioIcons.BookmarkBorder,
                            contentDescription = if (saved) "Remove bookmark" else "Bookmark quote",
                            tint = if (saved) category.themedAccent()
                                   else notePaperInk(quoteSheet).copy(alpha = 0.45f),
                            size = 18.dp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Marginalia section header — small quote-mark glyph + label (+ count)
 * above the journal / quotes sections, mirroring the capture form's
 * section labels so the saved view matches what the user wrote into.
 */
@Composable
private fun MarginaliaSectionHeader(
    label: String,
    category: CurioCategory,
    count: Int? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CurioIcon(
            name = CurioIcons.FormatQuote,
            contentDescription = null,
            tint = category.categoryInk(),
            size = 16.dp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        if (count != null) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Result of fitting a saved tile collage into a viewport — the uniformly
 * scaled tiles, the scaled board bounds and the scale. Shared by the inline
 * card (cover/crop) and the full-screen expanded dialog (contain/fit) so
 * both derive their geometry from the same helper: the expanded dialog
 * shows the whole collage, the inline card crops to its middle.
 */
private data class FitTileLayout(
    val scaledTiles: List<CaptureData.TileLayout>,
    val boardW: Float,
    val boardH: Float,
    val scale: Float
)

/**
 * Fits [tiles] into a [viewW]×[viewH] viewport: computes the collage's
 * bounds, scales uniformly, and returns the scaled tiles + scaled board
 * size + the scale. [cover] = false scales to the largest size that fits
 * BOTH dimensions (contain — the expanded full-board view); [cover] = true
 * scales to FILL the viewport (cover — the inline card's center-crop
 * "cropper" look, where the collage's middle art fills the card and the
 * overflow is clipped). With no tiles it returns an empty fit (scale 1f)
 * so callers can use the result unconditionally.
 */
private fun fitTileLayout(
    tiles: List<CaptureData.TileLayout>,
    viewW: Float,
    viewH: Float,
    cover: Boolean = false
): FitTileLayout {
    if (tiles.isEmpty()) return FitTileLayout(emptyList(), 0f, 0f, 1f)
    val maxX = tiles.maxOf { it.offsetXPx + it.widthPx }
    val maxY = tiles.maxOf { it.offsetYPx + it.heightPx }
    val scale = if (maxX > 0f && maxY > 0f) {
        if (cover) (viewW / maxX).coerceAtLeast(viewH / maxY)
        else (viewW / maxX).coerceAtMost(viewH / maxY)
    } else 1f
    val scaledTiles = tiles.map {
        CaptureData.TileLayout(
            uri = it.uri,
            offsetXPx = it.offsetXPx * scale,
            offsetYPx = it.offsetYPx * scale,
            rotationDeg = it.rotationDeg,
            widthPx = it.widthPx * scale,
            heightPx = it.heightPx * scale
        )
    }
    val boardW = scaledTiles.maxOfOrNull { it.offsetXPx + it.widthPx } ?: 0f
    val boardH = scaledTiles.maxOfOrNull { it.offsetYPx + it.heightPx } ?: 0f
    return FitTileLayout(scaledTiles, boardW, boardH, scale)
}

/**
 * v7.23 — Save / Share the full mood board as a high-res PNG. Both actions
 * render the complete board off-screen (see [MoodBoardExport]) so the image
 * is sharp at any zoom. Shows a short toast confirming where it went.
 */
@Composable
private fun MoodBoardExportActions(
    data: CaptureData.GalleryWall,
    category: CurioCategory,
    boardSeed: Int,
    authority: String,
    entryId: String
) {
    val context = LocalContext.current
    var busy by remember { mutableStateOf(false) }
    // v7.26 — Save/Share wear the same WHITE FROSTED-GLASS look as the hero's
    // Date · Mood · Type grid card: a translucent pane (the page wash blurred)
    // under a white 0.78 glass tint, a hairline slate rim, and deep-slate ink
    // that reads on white in every theme.
    val frostInk = Color(0xFF232A35)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FrostedExportButton(
            busy = busy,
            icon = CurioIcons.Image,
            label = "Save PNG",
            frostInk = frostInk,
            category = category,
            onClick = {
                if (busy) return@FrostedExportButton
                busy = true
                MoodBoardExport.saveMoodBoardPng(
                    context = context,
                    authority = authority,
                    data = data,
                    category = category,
                    boardSeed = boardSeed,
                    entryId = entryId
                ) { path ->
                    busy = false
                    android.widget.Toast.makeText(
                        context,
                        if (path != null) "Mood board saved to Gallery"
                        else "Couldn't save the mood board",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.weight(1f)
        )
        FrostedExportButton(
            busy = busy,
            icon = CurioIcons.Share,
            label = "Share PNG",
            frostInk = frostInk,
            category = category,
            onClick = {
                if (busy) return@FrostedExportButton
                busy = true
                MoodBoardExport.shareMoodBoardPng(
                    context = context,
                    authority = authority,
                    data = data,
                    category = category,
                    boardSeed = boardSeed,
                    entryId = entryId
                ) {
                    busy = false
                }
            },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * v7.26 — one Save/Share export button in the WHITE FROSTED-GLASS language
 * of the hero's Date · Mood · Type grid card: a translucent pane carrying
 * the page's category wash blurred + clipped to the button, a white 0.78
 * glass tint on top, a hairline deep-slate rim, and deep-slate ink content
 * that reads on white in every theme (mirrors the hero card exactly — the
 * slate was chosen because white/onAccent ink would vanish on the white
 * pane). [busy] swaps the label to "Rendering…" and the disabled state.
 */
@Composable
private fun FrostedExportButton(
    busy: Boolean,
    icon: String,
    label: String,
    frostInk: Color,
    category: CurioCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, frostInk.copy(alpha = 0.20f)),
        modifier = modifier
    ) {
        Box(Modifier.fillMaxWidth()) {
            // ── Frosted pane: the page's category wash blurred + clipped to
            // the button, sitting BEHIND the crisp content — same structure
            // as the hero grid card's pane (the wash is what's actually
            // behind these buttons).
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Brush.verticalGradient(listOf(category.categoryBackgroundWash(), category.categorySurface(MaterialTheme.colorScheme.surface))))
                    .blur(18.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            // ── Frosted white glass tint ── the blurred color blooms behind
            // stay faintly visible, but the button reads as WHITE frosted
            // glass with deep-slate content (no theme tint).
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.78f))
            )
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CurioIcon(
                    name = icon,
                    contentDescription = null,
                    tint = frostInk.copy(alpha = 0.95f),
                    size = 16.dp
                )
                Text(
                    text = if (busy) "Rendering…" else label,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = frostInk
                )
            }
        }
    }
}

@Composable
private fun GalleryWallRender(entry: CurioEntry, category: CurioCategory, navController: NavController) {
    val data = entry.captureData as? CaptureData.GalleryWall ?: return
    val density = androidx.compose.ui.platform.LocalDensity.current
    var boardExpanded by remember { mutableStateOf(false) }
    // v7.24 — in-place SINGLE-IMAGE zoom: double-tapping a tile glides only
    // that image from its spot on the collage to the card's center (arc),
    // where pinch/pan refine it — the board around it never moves.
    val zoomState = rememberMoodBoardZoomState()

    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // ── Mood board canvas with tile positions ──────────────────────
        // The board's watermark pattern is seeded from the entry id so each
        // saved mood board keeps its own stable background collage.
        val boardSeed = remember(entry.id) { entry.id.hashCode() }
        Surface(
            shape = RoundedCornerShape(28.dp),
        // The saved board wears an OPAQUE category-tinted card surface
        // ([categorySurfaceMoodBoard] — the same card language as the rest
        // of the page, honoring the manual Settings tint toggle but NOT the
        // AMOLED theme style, so the board keeps its tint on pure black).
        // The old 20%-alpha [CurioCategory.tint] let the page-level
        // watermark glyphs bleed through and collide with the board's own
        // seeded glyph pattern (two overlapping watermarks); an opaque
        // surface hides the page watermark so only the board's
        // [CurioMoodBoardBackdrop] shows.
        color = category.categorySurfaceMoodBoard(),
            shadowElevation = 0.dp,
            // Faint category rule — the saved board sits on the tinted page,
            // so a slim theme-aware border (accent in light, light twin in
            // dark via categoryInk) keeps it from blending into the wash.
            border = BorderStroke(1.dp, category.categoryInk().copy(alpha = 0.26f)),
            // v7.17 — the whole board (and its in-place zoom overlay) draws
            // ABOVE the caption + quote cards that follow it in this Column:
            // the zoomed image overflows the card, and later siblings would
            // otherwise paint over it (the "zoomed image behind the text
            // box" bug). zIndex only matters while zoomed — at rest nothing
            // overlaps.
            modifier = Modifier.fillMaxWidth().height(460.dp).zIndex(1000f)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val canvasW = with(density) { maxWidth.toPx() }
                val canvasH = with(density) { 460.dp.toPx() }

                // ── Theme-aware watermark backdrop (random per board) ──
                CurioMoodBoardBackdrop(
                    seed = boardSeed,
                    accent = category.themedAccent(),
                    modifier = Modifier.fillMaxSize()
                )

                // v7.18 — the inline card is a MIDDLE-BAND CROP of the FULL
                // collage: the board is width-fit (the SAME arrangement the
                // expanded dialog shows — portrait collages there are also
                // width-driven), then the card's clip trims to the center
                // band, so art pinned near the top or bottom of the expanded
                // board stays hidden in the small view (the cropper look).
                // The old cover-fit re-scaled the whole collage to squeeze
                // into the card, shifting every tile's size/position — the
                // "arrangements look different" bug.
                // maxOfOrNull: legacy GalleryWall entries store only
                // imageCount (no tileLayouts) — an empty-list maxOf would
                // crash the detail screen before the isNotEmpty() guard.
                val maxX = data.tileLayouts.maxOfOrNull { it.offsetXPx + it.widthPx } ?: 0f
                val maxY = data.tileLayouts.maxOfOrNull { it.offsetYPx + it.heightPx } ?: 0f
                val widthScale = if (maxX > 0f) canvasW / maxX else 1f
                // Edge case for wide/short collages: a width-fit board would
                // shrink to a sliver below the card — height-fit instead so
                // the collage stays presentable (nothing to crop then).
                val boardScale = if (maxY * widthScale < canvasH * 0.55f && maxY > 0f)
                    canvasH / maxY else widthScale
                val scaledTiles = data.tileLayouts.map {
                    CaptureData.TileLayout(
                        uri = it.uri,
                        offsetXPx = it.offsetXPx * boardScale,
                        offsetYPx = it.offsetYPx * boardScale,
                        rotationDeg = it.rotationDeg,
                        widthPx = it.widthPx * boardScale,
                        heightPx = it.heightPx * boardScale
                    )
                }
                val boardW = maxX * boardScale
                val boardH = maxY * boardScale

                if (data.tileLayouts.isNotEmpty()) {
                    // ── Edit button — reopen this board in the editor ──────
                    Surface(
                        onClick = { navController.navigate(CurioRoutes.editMoodBoard(entry.id)) { launchSingleTop = true } },
                        shape = RoundedCornerShape(50),
                        color = category.categorySurface(MaterialTheme.colorScheme.surface).copy(alpha = 0.9f),
                        border = category.categoryBorder(),
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .size(36.dp)
                            .zIndex(999f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            CurioIcon(
                                name = CurioIcons.Edit,
                                contentDescription = "Edit mood board",
                                tint = MaterialTheme.colorScheme.onSurface,
                                size = 18.dp
                            )
                        }
                    }

                    // ── Expand button — full-screen collage ──────────────
                    Surface(
                        onClick = { boardExpanded = true },
                        shape = RoundedCornerShape(50),
                        color = category.categorySurface(MaterialTheme.colorScheme.surface).copy(alpha = 0.9f),
                        border = category.categoryBorder(),
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(36.dp)
                            .zIndex(999f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            CurioIcon(
                                name = CurioIcons.Fullscreen,
                                contentDescription = "Expand mood board",
                                tint = MaterialTheme.colorScheme.onSurface,
                                size = 18.dp
                            )
                        }
                    }

                    // Inline (non-expanded) board: double-tap a tile to
                    // magnify it centered + straight (same gesture as the
                    // editor). Board-level pinch zoom is only enabled in the
                    // expanded full-screen dialog, so a stray two-finger
                    // pinch on the inline card can't hijack the page scroll.
                    // Cover-scaled + clipped to the card: the board's Surface
                    // doesn't clip overflowing children, so the explicit
                    // clip makes the crop clean (tiles past the card's edges
                    // stay hidden instead of spilling onto the page).
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(28.dp))
                            .offset {
                                IntOffset(
                                    ((canvasW - boardW) / 2f).roundToInt(),
                                    ((canvasH - boardH) / 2f).roundToInt()
                                )
                            }
                    ) {
                        MoodBoardTiles(
                            tiles = scaledTiles,
                            canvasWPx = boardW,
                            canvasHPx = boardH,
                            onTileZoom = { uri, tx, ty, w, h, vw, vh ->
                                // v7.24 — only the tapped IMAGE zooms; the
                                // board stays still. The tile's resting spot
                                // is its position in VIEWPORT coords (the
                                // board is offset within the card by the
                                // center-crop), and the image glides from
                                // there to the card's center.
                                val boardOffsetX = (canvasW - boardW) / 2f
                                val boardOffsetY = (canvasH - boardH) / 2f
                                zoomState.zoomIn(
                                    uri = uri,
                                    centerX = boardOffsetX + tx + w / 2f,
                                    centerY = boardOffsetY + ty + h / 2f,
                                    tileW = w,
                                    tileH = h,
                                    viewW = canvasW,
                                    viewH = canvasH
                                )
                            }
                        )

                        // ── Floating quote cards (v7.20) — the board's paper
                        // notes float ON the saved collage too, at the exact
                        // spots they were dragged to in the editor (scaled
                        // with the same fit the tiles use). Never-dragged
                        // cards fall back to their deterministic slot. The
                        // Box above is offset by the center-crop, so the
                        // cards live in the same board space as the tiles.
                        // v7.22 — only ON-board cards float here; below-board
                        // ones render under the board in their own section.
                        MoodBoardFloatingCards(
                            quotes = data.quotes.orEmpty(),
                            styles = data.quoteStyles.orEmpty(),
                            colors = data.quoteColors.orEmpty(),
                            tilts = data.quoteTilts.orEmpty(),
                            positions = data.quotePositions.orEmpty(),
                            onBoard = data.quoteOnBoard.orEmpty(),
                            canvasWPx = boardW,
                            canvasHPx = boardH,
                            boardScale = boardScale
                        )
                    }
                } else {
                    // Fallback: show images in a grid if no tile data
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CurioIcon(CurioIcons.Image, null, tint = category.categoryInk().copy(alpha = 0.3f), size = 48.dp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${data.imageCount} image${if (data.imageCount != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ── Single-image zoom (v7.24) — only the tapped image
                // glides up and zooms; the board around it stays still.
                // Same fit-scaled geometry so what you magnify matches the
                // card.
                scaledTiles.firstOrNull { it.uri == zoomState.zoomedUri }?.let { zoomTile ->
                    // Clip the gliding image to the card's rounded corners —
                    // the board Surface doesn't clip children, so without
                    // this a tile near the edge bleeds past the card onto
                    // the page while it glides to the center.
                    MoodBoardZoomOverlay(
                        zoomState = zoomState,
                        tileUri = zoomTile.uri,
                        tileX = (canvasW - boardW) / 2f + zoomTile.offsetXPx,
                        tileY = (canvasH - boardH) / 2f + zoomTile.offsetYPx,
                        widthPx = zoomTile.widthPx,
                        heightPx = zoomTile.heightPx,
                        viewW = canvasW,
                        viewH = canvasH,
                        modifier = Modifier.clip(RoundedCornerShape(28.dp))
                    )
                }
            }
        }

        // v7.23 — export the FULL board as a high-res PNG: save to the
        // gallery or share via the system sheet. Both render the same
        // complete picture (surface + watermark + collage + floating quotes
        // + caption + below-board quotes) off-screen at export resolution,
        // so the saved image stays sharp even zoomed in.
        val exportAuthority = remember(entry.id) {
            "${context.packageName}.fileprovider"
        }
        MoodBoardExportActions(
            data = data,
            category = category,
            boardSeed = boardSeed,
            authority = exportAuthority,
            entryId = entry.id
        )

        if (!data.caption.isNullOrBlank()) {
            // Caption wears the same note-paper slip as the editor's field.
            val captionSheet = data.captionColor ?: NotePaperColor.CREAM
            NotePaperCard(
                style = data.captionStyle ?: data.notePaperStyle(),
                paperColor = captionSheet,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                minHeight = 72.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    data.caption,
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = PatrickHandFontFamily),
                    color = notePaperInk(captionSheet)
                )
            }
        }

        // v7.22 — the ON-board quote cards float on the collage above; the
        // BELOW-board cards (added via the bottom Add-quote button) render
        // here as separate quote boxes under the board. Legacy entries lack
        // the flag → all cards are on-board → nothing renders below.
        val quoteOnBoardFlags = data.quoteOnBoard.orEmpty()
        val hasBelowBoard = data.quotes.orEmpty().indices.any {
            quoteOnBoardFlags.getOrElse(it) { true } == false
        }
        if (hasBelowBoard) {
            RenderQuoteCards(
                quotes = data.quotes.orEmpty(),
                spans = data.quoteSpans.orEmpty(),
                tilts = data.quoteTilts.orEmpty(),
                styles = data.quoteStyles.orEmpty(),
                colors = data.quoteColors.orEmpty(),
                fallbackStyle = data.notePaperStyle(),
                entryId = entry.id,
                topicName = entry.topic.name,
                category = category,
                label = "Quote boxes",
                includeIndex = { i -> quoteOnBoardFlags.getOrElse(i) { true } == false }
            )
        }

        if (boardExpanded) {
            ExpandedMoodBoardDialog(
                data = data,
                seed = boardSeed,
                accent = category.themedAccent(),
                // The expanded board rests on the SAME AMOLED-immune tinted
                // surface as the inline card — never the page wash (which
                // blacks out in AMOLED and makes the full-screen board look
                // pitch dark without its tint).
                boardSurface = category.categorySurfaceMoodBoard(),
                onDismiss = { boardExpanded = false },
                onEdit = {
                    navController.navigate(CurioRoutes.editMoodBoard(entry.id)) { launchSingleTop = true }
                }
            )
        }
    }
}

/**
 * Full-screen expanded mood board — scales the tile collage up to fill the
 * screen, centers it, and keeps per-tile tap → Lightbox. Close button
 * top-right; back/outside tap dismisses. Rests on the same theme-aware
 * watermark backdrop as the inline board (seeded from the entry id) and on
 * the same AMOLED-immune tinted surface ([categorySurfaceMoodBoard]) so the
 * full-screen board keeps its category tint even in AMOLED.
 */
@Composable
private fun ExpandedMoodBoardDialog(
    data: CaptureData.GalleryWall,
    seed: Int,
    accent: Color,
    boardSurface: Color,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val density = LocalDensity.current
    // In-place tile zoom inside the expanded board — pinch/tap, no Lightbox.
    // v7.19 — offsets animate inside the canvas; double-tap glides the
    // whole board (with its backdrop) to the tile, then the image pops.
    val zoomState = rememberMoodBoardZoomState()
    Dialog(
        onDismissRequest = onDismiss,
        // True full screen: the dialog draws behind the system bars so the
        // collage fills the whole display instead of floating like a dialog
        // page. The controls below pad for the bars.
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(boardSurface)
        ) {
            // ── Theme-aware watermark backdrop (matches inline board) ──
            CurioMoodBoardBackdrop(
                seed = seed,
                accent = accent,
                modifier = Modifier.fillMaxSize()
            )
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val dialogW = with(density) { maxWidth.toPx() }
                val dialogH = with(density) { maxHeight.toPx() }

                if (data.tileLayouts.isNotEmpty()) {
                    // Fit the collage to the dialog with the SAME shared
                    // [fitTileLayout] the inline card uses — bounds →
                    // uniform scale → centered — so the full-screen board
                    // always matches the small card. Pinch on the board
                    // magnifies it; double-tap a tile magnifies the tile
                    // centered + straight.
                    val fit = fitTileLayout(data.tileLayouts, dialogW, dialogH)
                    val scaledTiles = fit.scaledTiles
                    val boardW = fit.boardW
                    val boardH = fit.boardH
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset {
                                IntOffset(
                                    ((dialogW - boardW) / 2f).roundToInt(),
                                    ((dialogH - boardH) / 2f).roundToInt()
                                )
                            }
                    ) {
                        MoodBoardTiles(
                            tiles = scaledTiles,
                            canvasWPx = boardW,
                            canvasHPx = boardH,
                            onTileZoom = { uri, tx, ty, w, h, vw, vh ->
                                // v7.24 — only the tapped IMAGE zooms; the
                                // board stays still. Resting spot = the
                                // tile's VIEWPORT position (board centered
                                // in the dialog by the contain-fit).
                                val boardOffsetX = (dialogW - boardW) / 2f
                                val boardOffsetY = (dialogH - boardH) / 2f
                                zoomState.zoomIn(
                                    uri = uri,
                                    centerX = boardOffsetX + tx + w / 2f,
                                    centerY = boardOffsetY + ty + h / 2f,
                                    tileW = w,
                                    tileH = h,
                                    viewW = dialogW,
                                    viewH = dialogH
                                )
                            }
                        )

                        // ── Floating quote cards (v7.20) — same layer as the
                        // inline card: saved positions scaled by the fit, in
                        // the already-centered Box's board space. v7.22 — only
                        // on-board cards float; below-board ones stay under the
                        // board (never inside the expanded dialog).
                        MoodBoardFloatingCards(
                            quotes = data.quotes.orEmpty(),
                            styles = data.quoteStyles.orEmpty(),
                            colors = data.quoteColors.orEmpty(),
                            tilts = data.quoteTilts.orEmpty(),
                            positions = data.quotePositions.orEmpty(),
                            onBoard = data.quoteOnBoard.orEmpty(),
                            canvasWPx = boardW,
                            canvasHPx = boardH,
                            boardScale = fit.scale
                        )
                    }

                    // ── Single-image zoom (v7.24) — only the tapped image
                    // glides up and zooms; the board stays still. ─────────
                    scaledTiles.firstOrNull { it.uri == zoomState.zoomedUri }?.let { zoomTile ->
                        MoodBoardZoomOverlay(
                            zoomState = zoomState,
                            tileUri = zoomTile.uri,
                            tileX = (dialogW - boardW) / 2f + zoomTile.offsetXPx,
                            tileY = (dialogH - boardH) / 2f + zoomTile.offsetYPx,
                            widthPx = zoomTile.widthPx,
                            heightPx = zoomTile.heightPx,
                            viewW = dialogW,
                            viewH = dialogH
                        )
                    }
                }

                // ── Edit button — reopen this board in the editor ─────────
                Surface(
                    onClick = onEdit,
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(16.dp)
                        .size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CurioIcon(
                            name = CurioIcons.Edit,
                            contentDescription = "Edit mood board",
                            tint = Color.White,
                            size = 22.dp
                        )
                    }
                }

                // ── Close button ─────────────────────────────────────────
                Surface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp)
                        .size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CurioIcon(
                            name = CurioIcons.Close,
                            contentDescription = "Close expanded mood board",
                            tint = Color.White,
                            size = 22.dp
                        )
                    }
                }

                // ── Hint ─────────────────────────────────────────────────
                Text(
                    text = "Double-tap a tile to zoom · pinch to magnify",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun FieldNotesRender(entry: CurioEntry, category: CurioCategory, navController: NavController) {
    val data = entry.captureData as? CaptureData.FieldNotes ?: return
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        data.fieldMindMetadata?.let { metadata ->
            FieldMindMetadataCard(metadata = metadata, category = category)
        }
        data.observed.takeIf { it.isNotBlank() }?.let { text ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Observed", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = category.categoryInk())
                val observedSheet = data.observedColor ?: NotePaperColor.CREAM
                NotePaperCard(
                    style = data.observedStyle ?: data.notePaperStyle(),
                    paperColor = observedSheet,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                    minHeight = 96.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        buildRichAnnotated(text, data.observedSpans.orEmpty(), notePaperHighlight(observedSheet)),
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = PatrickHandFontFamily),
                        color = notePaperInk(observedSheet)
                    )
                }
            }
        }
        data.surprised.takeIf { it.isNotBlank() }?.let { text ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Surprised me", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = category.categoryInk())
                val surprisedSheet = data.surprisedColor ?: NotePaperColor.CREAM
                NotePaperCard(
                    style = data.surprisedStyle ?: data.notePaperStyle(),
                    paperColor = surprisedSheet,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                    minHeight = 96.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        buildRichAnnotated(text, data.surprisedSpans.orEmpty(), notePaperHighlight(surprisedSheet)),
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = PatrickHandFontFamily),
                        color = notePaperInk(surprisedSheet)
                    )
                }
            }
        }
        data.learnNext.takeIf { it.isNotBlank() }?.let { text ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Want to learn next", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = category.categoryInk())
                val learnNextSheet = data.learnNextColor ?: NotePaperColor.CREAM
                NotePaperCard(
                    style = data.learnNextStyle ?: data.notePaperStyle(),
                    paperColor = learnNextSheet,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                    minHeight = 96.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        buildRichAnnotated(text, data.learnNextSpans.orEmpty(), notePaperHighlight(learnNextSheet)),
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = PatrickHandFontFamily),
                        color = notePaperInk(learnNextSheet)
                    )
                }
            }
        }
        if (data.imageUris.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                data.imageUris.take(3).forEach { uri ->
                    Surface(
                        onClick = { navController.navigate(CurioRoutes.lightbox(uri)) { launchSingleTop = true } },
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 0.dp,
                        modifier = Modifier.weight(1f).height(120.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Open image",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun structuredDetailRows(raw: String): List<Pair<String, String>> {
    if (raw.isBlank()) return emptyList()
    val root = runCatching { JSONObject(raw) }.getOrNull() ?: return emptyList()
    val rows = mutableListOf<Pair<String, String>>()

    fun titleize(key: String): String = key
        .replace(Regex("([a-z])([A-Z])"), "$1 $2")
        .replace('_', ' ')
        .replace('-', ' ')
        .split(' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

    fun flatten(value: Any?, path: String) {
        when (value) {
            is JSONObject -> {
                value.keys().forEach { key ->
                    val childPath = if (path.isBlank()) titleize(key) else "$path · ${titleize(key)}"
                    flatten(value.opt(key), childPath)
                }
            }
            is JSONArray -> {
                val values = (0 until value.length()).mapNotNull { index -> value.opt(index) }
                if (values.isNotEmpty()) rows += path to values.joinToString(", ") { it.toString() }
            }
            null, JSONObject.NULL -> Unit
            else -> {
                val text = value.toString().trim()
                if (text.isNotBlank()) rows += path to text
            }
        }
    }

    flatten(root, "")
    return rows
}

@Composable
private fun formatMetadataTimestamp(millis: Long): String {
    // Lint (NonObservableLocale): read the locale through Compose's observable
    // state so the timestamp re-formats when the user changes the system locale.
    val locale = LocalLocale.current.platformLocale
    return SimpleDateFormat("MMM d, yyyy · h:mm a", locale).format(Date(millis))
}

@Composable
private fun FieldMindMetadataCard(metadata: FieldMindMetadata, category: CurioCategory) {
    val species = metadata.species
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = category.categorySurface(MaterialTheme.colorScheme.surfaceContainerHigh),
        border = category.categoryBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CurioIcon(CurioIcons.ScienceGlyph, null, tint = category.themedAccent(), size = 22.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text("FieldMind metadata", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text(metadata.recordType.orEmpty().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            val rows = buildList {
                fun addRow(label: String, value: String?) { if (!value.isNullOrBlank()) add(label to value) }
                addRow("Category", metadata.category)
                addRow("Confidence", metadata.confidence)
                addRow("Date", listOf(metadata.date.orEmpty(), metadata.time.orEmpty()).filter { it.isNotBlank() }.joinToString(" · "))
                addRow("Location", metadata.location)
                if (metadata.latitude != null && metadata.longitude != null) addRow("Coordinates", "${metadata.latitude}, ${metadata.longitude}")
                addRow("Weather", listOf(metadata.weather.orEmpty(), metadata.weatherCondition.orEmpty()).filter { it.isNotBlank() }.distinct().joinToString(" · "))
                metadata.weatherTemperature?.let { addRow("Temperature", "${it}°") }
                metadata.humidity?.let { addRow("Humidity", "$it%") }
                metadata.windSpeed?.let { addRow("Wind", it.toString()) }
                metadata.cloudCover?.let { addRow("Cloud cover", "$it%") }
                metadata.pressure?.let { addRow("Pressure", it.toString()) }
                metadata.durationMs?.let { addRow("Duration", formatElapsed(it)) }
                metadata.startedAt?.let { addRow("Started", formatMetadataTimestamp(it)) }
                metadata.endedAt?.let { addRow("Ended", formatMetadataTimestamp(it)) }
                metadata.changeObservedAt?.let { addRow("Change observed", formatMetadataTimestamp(it)) }
                metadata.changeDurationMs?.let { addRow("Change duration", formatElapsed(it)) }
                metadata.weatherSnapshotAt?.let { addRow("Weather snapshot", formatMetadataTimestamp(it)) }
                addRow("Status", metadata.status)
                metadata.projectId?.let { addRow("Project", it.toString()) }
                metadata.sourceId?.let { addRow("Source", it.toString()) }
                metadata.parentObservationId?.let { addRow("Parent observation", it.toString()) }
                metadata.followUpScheduledAt?.let { addRow("Follow-up", formatMetadataTimestamp(it)) }
                metadata.archivedAt?.let { addRow("Archived", formatMetadataTimestamp(it)) }
                metadata.deletedAt?.let { addRow("Deleted", formatMetadataTimestamp(it)) }
                metadata.createdAt?.let { addRow("Created", formatMetadataTimestamp(it)) }
                metadata.updatedAt?.let { addRow("Updated", formatMetadataTimestamp(it)) }
                metadata.archivedAt?.let { addRow("Archived", formatMetadataTimestamp(it)) }
                metadata.deletedAt?.let { addRow("Deleted", formatMetadataTimestamp(it)) }
                metadata.createdAt?.let { addRow("Created", formatMetadataTimestamp(it)) }
                metadata.updatedAt?.let { addRow("Updated", formatMetadataTimestamp(it)) }
                metadata.qualityScore?.let { addRow("Quality", it.toString()) }
                addRow("Time note", metadata.timeNote)
            }
            rows.forEach { (label, value) ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(label, style = MaterialTheme.typography.labelMedium, color = category.categoryInk(), modifier = Modifier.width(92.dp))
                    Text(value, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                }
            }
            if (!metadata.tags.isNullOrEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    metadata.tags.orEmpty().forEach { tag ->
                        Surface(shape = RoundedCornerShape(50), color = category.themedAccent().copy(alpha = 0.12f)) {
                            Text("#$tag", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }
            val structuredRows = structuredDetailRows(metadata.structuredDetailsJson)
            if (structuredRows.isNotEmpty()) {
                Text(
                    "Structured details",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = category.categoryInk()
                )
                structuredRows.forEach { (label, value) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                        border = category.categoryBorder(
                            fallback = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelMedium,
                                color = category.categoryInk(),
                                modifier = Modifier.width(112.dp)
                            )
                            Text(
                                value,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            species?.let { item ->
                Surface(shape = RoundedCornerShape(16.dp), color = category.themedAccent().copy(alpha = 0.10f), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CurioIcon(CurioIcons.ScienceGlyph, null, tint = category.themedAccent(), size = 18.dp)
                            Text("Species", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Text(item.commonName.orEmpty().ifBlank { item.scientificName.orEmpty().ifBlank { "Unknown species" } }, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                        if (!item.scientificName.isNullOrBlank()) Text(item.scientificName.orEmpty(), style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val taxonomy = listOf(
                            "Kingdom" to item.kingdom,
                            "Phylum" to item.phylum,
                            "Class" to item.className,
                            "Order" to item.order,
                            "Family" to item.family,
                            "Genus" to item.genus,
                            "Species" to item.species,
                            "Life stage" to item.lifeStage,
                            "Sex" to item.sex,
                            "Conservation" to item.conservationStatus
                        ).filter { it.second.isNotBlank() }
                        taxonomy.forEach { (label, value) ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall, color = category.categoryInk(), modifier = Modifier.width(92.dp))
                                    Text(value, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        item.observationCount?.let { Text("Recorded observations · $it", style = MaterialTheme.typography.labelSmall) }
                        if (!item.notes.isNullOrBlank()) Text(item.notes.orEmpty(), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun OpenNotebookRender(entry: CurioEntry, category: CurioCategory, navController: NavController) {
    val data = entry.captureData as? CaptureData.OpenNotebook ?: return
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Format: ${data.subFormat.name}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        // Recursively render the sub-data
        val subEntry = CurioEntry(
            id = entry.id,
            topic = entry.topic,
            format = data.subFormat,
            captureData = data.subData
        )
        // Synthesized sub-entry — must NOT persist dictation (shares the
        // parent entry's id; a save would overwrite the whole OpenNotebook).
        FormatBody(entry = subEntry, category = category, navController = navController, allowTranscribe = false)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Share Card — rendered off-screen, captured as PNG, shared via Intent.ACTION_SEND
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Self-contained share card composable designed for bitmap capture.
 *
 * Rendered off-screen by [shareComposableCard] at 400×400 dp, captured
 * as a PNG, and shared via [Intent.ACTION_SEND] + [FileProvider].
 *
 * Layout (top to bottom):
 *   - Category gradient background (full-bleed, rounded corners)
 *   - Large category icon watermark (centered, low alpha)
 *   - Topic name (large, bold, white)
 *   - Category chip
 *   - Teaser text (3 lines max)
 *   - Format badge
 *   - "Curio ✦" branding footer
 */
@Composable
private fun CurioShareCard(
    entry: CurioEntry,
    category: CurioCategory
) {
    val bgColor = category.themedAccent().copy(alpha = 0.9f)
    // v7.5 — pastel mode lightens the card fill, so the share card content
    // flips from white to a deep ink of the accent. White when pastel mode
    // is off, preserving the exact pre-pastel share card.
    val ink = pastelFillInk(category.themedAccent())

    val daysAgoText = when (entry.capturedAtDaysAgo) {
        0 -> "Captured today"
        1 -> "Captured yesterday"
        else -> "Captured ${entry.capturedAtDaysAgo}d ago"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor, RoundedCornerShape(28.dp))
    ) {
        // ── Watermark icon ────────────────────────────────────────────
        CurioIcon(
            name = category.iconGlyph,
            contentDescription = null,
            tint = ink.copy(alpha = 0.08f),
            size = 200.dp,
            modifier = Modifier.align(Alignment.Center)
        )

        // ── Content ───────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: category chip + sparkle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ink.copy(alpha = 0.18f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CurioIcon(
                            name = category.iconGlyph,
                            contentDescription = null,
                            tint = ink,
                            size = 14.dp
                        )
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = ink
                        )
                    }
                }
                CurioIcon(
                    name = CurioIcons.AutoAwesome,
                    contentDescription = null,
                    tint = ink.copy(alpha = 0.5f),
                    size = 20.dp
                )
            }

            // Middle: topic name + teaser
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = entry.topic.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = ink,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.topic.teaser,
                    style = MaterialTheme.typography.bodyLarge,
                    color = ink.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom: format badge + branding
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Format + date row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ink.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = entry.format.name.replace(Regex("([a-z])([A-Z])"), "$1 $2"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = ink,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ink.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = daysAgoText,
                            style = MaterialTheme.typography.labelSmall,
                            color = ink.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Branding
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CurioIcon(
                        name = CurioIcons.AutoAwesome,
                        contentDescription = null,
                        tint = ink.copy(alpha = 0.6f),
                        size = 18.dp
                    )
                    Text(
                        text = "Curio",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = ink.copy(alpha = 0.9f)
                    )
                }
                Text(
                    text = "Stay curious",
                    style = MaterialTheme.typography.labelSmall,
                    color = ink.copy(alpha = 0.5f)
                )
            }
        }
    }
}
