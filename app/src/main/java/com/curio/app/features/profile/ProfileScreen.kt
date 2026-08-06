package com.curio.app.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.curio.app.data.AppPreferences
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioRepositoryHolder
import com.curio.app.data.StreakTracker
import com.curio.app.infrastructure.CurioCrashReporter
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.components.CurioCardHeader
import com.curio.app.ui.components.CurioForwardArrow
import com.curio.app.ui.components.CurioSettingsCard
import com.curio.app.ui.components.CurioSettingsDivider
import com.curio.app.ui.components.CurioSettingsRow
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.pastelFillInk
import com.curio.app.ui.theme.themedAccent
import kotlinx.coroutines.launch

/**
 * Profile hub — identity + stats only (v7.3 revamp).
 *
 * Personalization lives entirely in Settings (appearance, notifications,
 * categories, backup — plus the Experimental section). Profile keeps the
 * quest-card hero, glanceable stats, the level tracker, your lanes, a single
 * Settings entry card, and the support/diagnostics card. Every visual
 * follows the app's shared language: paper cards with hairline borders,
 * solid surface stat pills, and a category-tinted gradient hero (the accent
 * of your most-explored lane, brand coral before your first save).
 */
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var displayName by remember { mutableStateOf(AppPreferences.getDisplayName(context)) }
    var showNameDialog by remember { mutableStateOf(false) }
    var nameInput by remember(displayName) { mutableStateOf(displayName) }
    var crashCount by remember { mutableIntStateOf(0) }
    var totalSaved by remember { mutableIntStateOf(0) }
    var categoryCounts by remember { mutableStateOf<Map<CategoryId, Int>>(emptyMap()) }
    val scope = rememberCoroutineScope()

    // Reloads stats on composition entry (nav return) AND on ON_RESUME
    // (returning from the app switcher), so the hero, pills, and lanes
    // always match the journal.
    fun refreshStats() {
        scope.launch {
            runCatching {
                val entries = CurioRepositoryHolder.repo.getAll()
                totalSaved = entries.size
                categoryCounts = entries.groupingBy { it.topic.categoryId }.eachCount()
            }.onFailure { android.util.Log.e("ProfileScreen", "Failed to load entries", it) }
            crashCount = CurioCrashReporter.getCrashHistory(context).size
        }
    }

    LaunchedEffect(Unit) { refreshStats() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                displayName = AppPreferences.getDisplayName(context)
                refreshStats()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val streakDays = StreakTracker.getStreak(context)
    val level = levelFor(totalSaved)
    val progress = progressTowardsNextLevel(totalSaved)

    // Hero accent/glyph follow your most-explored lane; brand coral +
    // sparkles before the first save (mirrors the Home quest card's
    // wildcard treatment). themedAccent() is @Composable, so these live
    // in the body (not a remember block).
    val topLane = categoryCounts.maxByOrNull { it.value }?.key
    val heroAccent = topLane?.let { CurioCategories.byId(it).themedAccent() }
        ?: CurioColors.CategoryCoral
    val heroGlyph = topLane?.let { CurioCategories.byId(it).iconGlyph }
        ?: CurioIcons.AutoAwesome

    ProfileDialogs(
        showNameDialog = showNameDialog,
        nameInput = nameInput,
        onNameInputChange = { nameInput = it },
        onDismissName = { showNameDialog = false },
        onSaveName = {
            displayName = nameInput.trim().ifBlank { "Curious Explorer" }
            AppPreferences.setDisplayName(context, displayName)
            showNameDialog = false
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CurioBackButton(onClick = { navController.popBackStack() })
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Profile",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Your curiosity, in one place",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Gear entry → full Settings screen (appearance, notifications,
            // categories, backup, experimental). Mirror of CurioBackButton so
            // the top bar stays balanced: back arrow left, gear right.
            Surface(
                onClick = { navController.navigate(CurioRoutes.SETTINGS) { launchSingleTop = true } },
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                CurioIcon(
                    name = CurioIcons.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface,
                    size = 24.dp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                ProfileHero(
                    name = displayName,
                    streakDays = streakDays,
                    accent = heroAccent,
                    glyph = heroGlyph,
                    onEditName = {
                        nameInput = displayName
                        showNameDialog = true
                    }
                )
            }
            item {
                StatsStrip(
                    streak = streakDays,
                    saved = totalSaved,
                    // Used lanes once entries exist (matches the Lanes card);
                    // the visible lane count before the first save.
                    lanes = if (categoryCounts.isEmpty()) CurioCategories.visible.size else categoryCounts.size
                )
            }
            item {
                LevelCard(
                    level = level,
                    saved = totalSaved,
                    progress = progress.first,
                    nextThreshold = progress.second,
                    isMaxLevel = level >= 9
                )
            }
            if (categoryCounts.isNotEmpty()) {
                item {
                    LanesCard(
                        counts = categoryCounts,
                        onCabinet = { navController.navigate(CurioRoutes.CABINET) { launchSingleTop = true } }
                    )
                }
            }
            item {
                SettingsNavCard(
                    onOpenSettings = { navController.navigate(CurioRoutes.SETTINGS) { launchSingleTop = true } }
                )
            }
            item {
                SupportCard(
                    crashCount = crashCount,
                    onTestCrash = { CurioCrashReporter.testCrash() },
                    onCrashLogs = { navController.navigate(CurioRoutes.CRASH) { launchSingleTop = true } },
                    onReportBug = { navController.navigate(CurioRoutes.BUG_REPORT) { launchSingleTop = true } }
                )
            }
            item { Spacer(Modifier.navigationBarsPadding().height(4.dp)) }
        }
    }
}

@Composable
private fun ProfileDialogs(
    showNameDialog: Boolean,
    nameInput: String,
    onNameInputChange: (String) -> Unit,
    onDismissName: () -> Unit,
    onSaveName: () -> Unit
) {
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = onDismissName,
            shape = RoundedCornerShape(28.dp),
            title = { Text("Display name", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("This is how Curio greets you.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = onNameInputChange,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = { TextButton(onClick = onSaveName) { Text("Save", fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = onDismissName) { Text("Cancel") } }
        )
    }
}

/**
 * Quest-card hero (v7.3) — same treatment as Home's quest card so Profile
 * reads as part of the family: category-tinted gradient (your most-explored
 * lane's accent), a watermark glyph in the corner, a letter-spaced kicker,
 * and white content. Edit + streak ride as white-glass pills.
 */
@Composable
private fun ProfileHero(
    name: String,
    streakDays: Int,
    accent: Color,
    glyph: String,
    onEditName: () -> Unit
) {
    val initial = name.firstOrNull()?.uppercase().orEmpty()
    val gradient = CurioGradients.cardGradient(accent)
    // v7.5 — pastel mode lightens the hero gradient, so the hero content
    // flips from white to a deep ink of the lane accent (brand maroon on the
    // coral before your first save). White when pastel mode is off.
    val ink = pastelFillInk(accent)
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(gradient), RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            // Watermark glyph — the lane you explore most (sparkles before
            // your first save), always a whisper behind the content.
            CurioIcon(
                glyph,
                null,
                tint = ink.copy(alpha = 0.20f),
                size = 120.dp,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // ── Kicker — mirrors the quest card's "TODAY'S QUEST" ──
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(16.dp)
                            .background(ink.copy(alpha = 0.60f), RoundedCornerShape(2.dp))
                    )
                    Text(
                        "YOUR PROFILE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        ),
                        color = ink.copy(alpha = 0.88f)
                    )
                }
                // ── Avatar + name ──────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(ink.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            initial,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = ink
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            name,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            taglineForStreak(streakDays),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ink.copy(alpha = 0.78f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // ── Edit + streak — white-glass pills like the quest CTA ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = onEditName,
                        shape = RoundedCornerShape(50),
                        color = ink.copy(alpha = 0.18f),
                        contentColor = ink,
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CurioIcon(CurioIcons.Edit, null, tint = ink, size = 16.dp)
                            Text("Edit profile", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = ink)
                        }
                    }
                    if (streakDays > 0) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = ink.copy(alpha = 0.18f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                CurioIcon(CurioIcons.LocalFire, null, tint = ink, size = 16.dp)
                                Text("$streakDays-day streak", style = MaterialTheme.typography.labelMedium, color = ink, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Solid stat pills — the same language as Home's stat strip (surface pill,
 * tinted icon, onSurface value + label) instead of the old gradient-and-
 * plum treatment that didn't belong to the app.
 */
@Composable
private fun StatsStrip(streak: Int, saved: Int, lanes: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileStat(Modifier.weight(1f), CurioIcons.LocalFire, "$streak", "Streak", CurioColors.CoralBlush)
        ProfileStat(Modifier.weight(1f), CurioIcons.Inventory2, "$saved", "Saved", CurioColors.Sage)
        ProfileStat(Modifier.weight(1f), CurioIcons.Palette, "$lanes", "Lanes", CurioColors.Teal)
    }
}

@Composable
private fun ProfileStat(modifier: Modifier, icon: String, value: String, label: String, tint: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            CurioIcon(icon, null, tint = tint, size = 18.dp)
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LevelCard(level: Int, saved: Int, progress: Float, nextThreshold: Int, isMaxLevel: Boolean) {
    CurioSettingsCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(CurioGradients.WildcardGradientStops.take(3))),
                contentAlignment = Alignment.Center
            ) {
                Text("$level", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Level $level · ${levelTitle(level)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(
                    if (isMaxLevel) "Your curiosity has no ceiling."
                    else "$saved / $nextThreshold saved · ${nextThreshold - saved} to next level",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!isMaxLevel) {
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                color = CurioColors.CoralBlush,
                trackColor = CurioColors.CoralBlush.copy(alpha = 0.14f)
            )
        }
    }
}

/** Single Settings entry — Profile owns identity/stats, Settings owns every preference. */
@Composable
private fun SettingsNavCard(onOpenSettings: () -> Unit) {
    CurioSettingsCard {
        Surface(
            onClick = onOpenSettings,
            color = Color.Transparent,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Brush.verticalGradient(CurioGradients.cardGradient(CurioColors.CoralBlush))),
                    contentAlignment = Alignment.Center
                ) {
                    CurioIcon(CurioIcons.Settings, null, tint = Color.White, size = 23.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Settings & preferences", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                    Text(
                        "Appearance · notifications · backup",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                CurioForwardArrow(tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f), size = 18.dp)
            }
        }
    }
}

@Composable
private fun LanesCard(counts: Map<CategoryId, Int>, onCabinet: () -> Unit) {
    CurioSettingsCard {
        CurioCardHeader(CurioIcons.Palette, "Your lanes", "Where you've been exploring")
        Spacer(Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(counts.entries.sortedByDescending { it.value }.take(4)) { (categoryId, count) ->
                val category = CurioCategories.byId(categoryId)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = category.themedAccent().copy(alpha = 0.14f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, category.themedAccent().copy(alpha = 0.20f))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        CurioIcon(category.iconGlyph, null, tint = category.themedAccent(), size = 20.dp)
                        Spacer(Modifier.height(4.dp))
                        Text(category.displayName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                        Text("$count saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Surface(
            onClick = onCabinet,
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                CurioIcon(CurioIcons.Inventory2, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp)
                Spacer(Modifier.width(8.dp))
                Text("Open the Cabinet", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                CurioForwardArrow(size = 16.dp)
            }
        }
    }
}

@Composable
private fun SupportCard(
    crashCount: Int,
    onTestCrash: () -> Unit,
    onCrashLogs: () -> Unit,
    onReportBug: () -> Unit
) {
    CurioSettingsCard {
        CurioCardHeader(CurioIcons.Info, "Support & diagnostics", "Help, reports, and developer tools")
        CurioSettingsRow(CurioIcons.BugReport, "Report a bug", "Send feedback or an issue", onReportBug)
        if (crashCount > 0) {
            CurioSettingsDivider()
            CurioSettingsRow(CurioIcons.History, "Crash logs", "$crashCount saved report${if (crashCount == 1) "" else "s"}", onCrashLogs)
        }
        CurioSettingsDivider()
        CurioSettingsRow(CurioIcons.ErrorOutline, "Test crash", "Diagnostic tool", onTestCrash)
    }
}

private fun taglineForStreak(streakDays: Int): String = when {
    streakDays >= 30 -> "Marathon explorer · beautifully consistent."
    streakDays >= 7 -> "A strong curiosity streak is underway."
    streakDays > 0 -> "Keep the spark going today."
    else -> "Stay curious. There is always more to find."
}

private val levelThresholds = listOf(0, 1, 5, 15, 30, 60, 100, 250, 500)

private fun levelFor(saved: Int): Int {
    var level = 1
    levelThresholds.forEachIndexed { index, threshold -> if (saved >= threshold) level = index + 1 }
    return level.coerceIn(1, 9)
}

private fun progressTowardsNextLevel(saved: Int): Pair<Float, Int> {
    val level = levelFor(saved)
    if (level >= 8) return 1f to 500
    val from = levelThresholds[level - 1]
    val to = levelThresholds[level]
    return ((saved - from).toFloat() / (to - from).coerceAtLeast(1)) to to
}

private fun levelTitle(level: Int): String = when (level) {
    1 -> "First spark"
    2 -> "Curious newcomer"
    3 -> "Tuned ear"
    4 -> "Pattern spotter"
    5 -> "Comparator"
    6 -> "Synthesizer"
    7 -> "Curator"
    8 -> "Master curator"
    else -> "Master explorer"
}
