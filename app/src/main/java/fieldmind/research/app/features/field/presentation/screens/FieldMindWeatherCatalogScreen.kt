package fieldmind.research.app.features.field.presentation.screens
import fieldmind.research.app.ui.theme.CuteCardDefaults

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import fieldmind.research.app.features.field.data.database.entity.WeatherCatalogEntity
import fieldmind.research.app.features.field.data.weather.WeatherSnapshot
import fieldmind.research.app.features.field.data.weather.WeatherUnitConverter
import fieldmind.research.app.features.field.presentation.components.*
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.features.field.presentation.viewmodel.FieldMindViewModel
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// ══════════════════════════════════════════════════════════════════════
//  Weather Catalog Screen — Scheduled weather data collection
//  Features:
//    - Auto-capture weather at user-chosen times throughout the day
//    - Display captured data in a table with date grouping
//    - Export to HTML (with temperature chart) and Excel (CSV)
// ══════════════════════════════════════════════════════════════════════

/** Available capture schedule slots (24h format). */
private val DEFAULT_SCHEDULE_SLOTS = listOf(6, 9, 12, 15, 18, 21) // 6AM, 9AM, 12PM, 3PM, 6PM, 9PM

/** Interval-based mode: capture every N hours. */
private val INTERVAL_OPTIONS = listOf(1, 2, 3, 4, 6, 8, 12)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherCatalogScreen(
    viewModel: FieldMindViewModel,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val colors = FieldMindTheme.colors
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ── Weather data ──
    val weatherCatalog by viewModel.weatherCatalog.collectAsState()
    val tempUnit by viewModel.fieldSettings.tempUnit.collectAsState()
    val windUnit by viewModel.fieldSettings.windSpeedUnit.collectAsState()

    // ── Live weather (updated reactively from ViewModel) ──
    var currentWeather by remember { mutableStateOf(viewModel.lastWeatherSnapshot) }
    var isRefreshing by remember { mutableStateOf(false) }
    var weatherError by remember { mutableStateOf(false) }

    // Keep currentWeather in sync with ViewModel's last snapshot
    LaunchedEffect(viewModel.lastWeatherSnapshot) {
        viewModel.lastWeatherSnapshot?.let { currentWeather = it }
    }

    // ── Capture state lives in the ViewModel so it survives screen navigation ──

    // ── Fetch weather on open ──
    LaunchedEffect(Unit) {
        isRefreshing = true
        val cached = viewModel.lastWeatherSnapshot
        if (cached != null) {
            weatherError = false
        }
        val snapshot = viewModel.refreshWeatherFromLocation()
        if (snapshot != null) {
            weatherError = false
        } else if (viewModel.lastWeatherSnapshot == null) {
            weatherError = true
        }
        isRefreshing = false
    }

    // ── Auto-capture loop runs in ViewModel (survives screen navigation) ──

    // ── Group catalog by date ──
    val groupedByDate = remember(weatherCatalog) {
        weatherCatalog.groupBy { w ->
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(w.fetchedAt))
        }.entries.sortedByDescending { it.key }
    }

    // ── Selector state ──
    var selectedDateGroup by remember { mutableStateOf(groupedByDate.firstOrNull()?.key) }
    val displayRecords = remember(weatherCatalog, selectedDateGroup) {
        if (selectedDateGroup != null) weatherCatalog.filter {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.fetchedAt)) == selectedDateGroup
        } else weatherCatalog
    }

    // ── Update selected group when data changes ──
    LaunchedEffect(groupedByDate.firstOrNull()?.key) {
        if (selectedDateGroup == null && groupedByDate.isNotEmpty()) {
            selectedDateGroup = groupedByDate.first().key
        }
    }

    // ── Stats ──
    val stats = remember(displayRecords) {
        val temps = displayRecords.mapNotNull { it.temperature }
        val hums = displayRecords.mapNotNull { it.humidity?.toDouble() }
        val winds = displayRecords.mapNotNull { it.windSpeed }
        CatalogStats(
            count = displayRecords.size,
            avgTemp = temps.average().takeIf { temps.isNotEmpty() },
            minTemp = temps.minOrNull(),
            maxTemp = temps.maxOrNull(),
            avgHumidity = hums.average().takeIf { hums.isNotEmpty() },
            avgWind = winds.average().takeIf { winds.isNotEmpty() }
        )
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { FieldMindSnackbarOverlay(hostState = snackbar) }
        ) { padding ->
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Header ──
                item {
                    StandardScreenHeader(
                        title = "Weather Catalog",
                        subtitle = "Scheduled weather data collection",
                        icon = MaterialSymbolIcon("cloud"),
                        heroColor = colors.info,
                        trailing = { BackButton(onClick = onBack) }
                    )
                }

                // ── Current weather card ──
                item {
                    WeatherCatalogCurrentCard(
                        weather = currentWeather,
                        hasError = weatherError,
                        isRefreshing = isRefreshing,
                        tempUnit = tempUnit,
                        windUnit = windUnit
                    )
                }

                // ── Schedule controls ──
                item {
                    ScheduleControlCard(
                        useIntervalMode = viewModel.weatherCatalogUseInterval,
                        onToggleMode = { viewModel.weatherCatalogUseInterval = it },
                        selectedSlots = viewModel.weatherCatalogSelectedSlots,
                        onToggleSlot = { hour ->
                            viewModel.weatherCatalogSelectedSlots = if (hour in viewModel.weatherCatalogSelectedSlots) viewModel.weatherCatalogSelectedSlots - hour else viewModel.weatherCatalogSelectedSlots + hour
                        },
                        selectedInterval = viewModel.weatherCatalogSelectedInterval,
                        onSelectInterval = { viewModel.weatherCatalogSelectedInterval = it },
                        isAutoCapturing = viewModel.isWeatherCatalogCapturing,
                        onStartCapture = { viewModel.startWeatherCatalogCapture() },
                        onStopCapture = { viewModel.stopWeatherCatalogCapture() },
                        colors = colors
                    )
                }

                // ── Stats row ──
                if (weatherCatalog.isNotEmpty()) {
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WeatherStatMini("Total", "${weatherCatalog.size}", colors.info, Modifier.weight(1f))
                            WeatherStatMini("Avg Temp", stats.avgTemp?.let { WeatherUnitConverter.formatTemp(it, tempUnit) } ?: "--", colors.observation, Modifier.weight(1f))
                            WeatherStatMini("Avg Humidity", stats.avgHumidity?.let { "%.0f%%".format(it) } ?: "--", colors.data, Modifier.weight(1f))
                            WeatherStatMini("Avg Wind", stats.avgWind?.let { WeatherUnitConverter.formatWind(it, windUnit) } ?: "--", colors.warning, Modifier.weight(1f))
                        }
                    }
                }

                // ── Day selector ──
                if (groupedByDate.isNotEmpty()) {
                    item {
                        DateGroupSelector(
                            groups = groupedByDate.map { it.key },
                            selected = selectedDateGroup,
                            onSelect = { selectedDateGroup = it }
                        )
                    }
                }

                // ── Export buttons ──
                if (weatherCatalog.isNotEmpty()) {
                    item {
                        ExportButtonsRow(
                            onExportHtml = {
                                scope.launch {
                                    exportWeatherHtml(context, weatherCatalog, tempUnit, windUnit, snackbar)
                                }
                            },
                            onExportCsv = {
                                scope.launch {
                                    exportWeatherCsv(context, weatherCatalog, tempUnit, snackbar)
                                }
                            }
                        )
                    }
                }

                // ── Empty state ──
                if (weatherCatalog.isEmpty()) {
                    item {
                        Card(
                            shape = CuteCardDefaults.Shape,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(MaterialSymbolIcon("cloud"), null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 56.dp)
                                Text("No weather data yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Use the schedule controls above to start capturing weather data automatically throughout the day.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                // ── Weather data table ──
                if (displayRecords.isNotEmpty()) {
                    item {
                        Text(
                            "Captured data (${displayRecords.size} records)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(displayRecords.sortedByDescending { it.fetchedAt }) { record ->
                        WeatherCatalogRecordCard(record, colors, tempUnit, windUnit)
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Sub-composables
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun WeatherCatalogCurrentCard(
    weather: WeatherSnapshot?,
    hasError: Boolean,
    isRefreshing: Boolean,
    tempUnit: String,
    windUnit: String
) {
    val colors = FieldMindTheme.colors
    val isDark = colors.isDark
    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val isNight = currentHour < 5 || currentHour >= 20

    // Temperature gradient
    val tempDisplay = weather?.temperature ?: 20.0
    val displayColors = when {
        tempDisplay < 0 -> listOf(Color(0xFF1A237E), Color(0xFF42A5F5))
        tempDisplay < 10 -> listOf(Color(0xFF1565C0), Color(0xFF64B5F6))
        tempDisplay < 20 -> listOf(Color(0xFF0D47A1), Color(0xFF66BB6A))
        tempDisplay < 30 -> listOf(Color(0xFFE65100), Color(0xFFFFB74D))
        else -> listOf(Color(0xFFBF360C), Color(0xFFE57373))
    }
    val textOnScene = when {
        isDark -> Color.White
        isNight -> Color.White
        else -> Color(0xFF1A1A3E)
    }

    Card(
        shape = CuteCardDefaults.ShapeHero,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (weather == null) Modifier.background(
                        MaterialTheme.colorScheme.surfaceContainerLow,
                        CuteCardDefaults.ShapeHero
                    ) else Modifier
                )
        ) {
            // Animated weather scene background
            if (weather != null) {
                Box(modifier = Modifier.matchParentSize()) {
                    AnimatedWeatherScene(
                        weatherCode = weather.weatherCode,
                        temperature = weather.temperature,
                        sunrise = weather.sunrise,
                        sunset = weather.sunset,
                        compact = false,
                        showCloudAnimation = true
                    )
                }
            }

            Column(
                Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (weather != null) {
                    // Current weather header
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Current conditions",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = textOnScene.copy(alpha = 0.7f)
                        )
                        // WeatherSnapshot does not include place name
                    }

                    // Gradient temperature
                    Text(
                        WeatherUnitConverter.formatTemp(weather.temperature, tempUnit),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            brush = Brush.horizontalGradient(displayColors)
                        )
                    )

                    // Condition + description
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(MaterialSymbolIcon(iconNameForWeatherCode(weather.weatherCode, isNight)), null, tint = textOnScene, size = 24.dp)
                        Text(
                            weather.weatherDescription,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textOnScene.copy(alpha = 0.9f)
                        )
                    }

                    // Glass metrics overlay
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = if (isDark || isNight) Color.White.copy(alpha = 0.12f) else Color(0xFF1A1A3E).copy(alpha = 0.06f),
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            weather.humidity?.let {
                                GlassMetric(value = "$it%", label = "Humidity", icon = MaterialSymbolIcon("water_drop"), accent = colors.data, textColor = textOnScene)
                            }
                            weather.windSpeed?.let { ws ->
                                GlassMetric(value = WeatherUnitConverter.formatWind(ws, windUnit), label = "Wind", icon = MaterialSymbolIcon("air"), accent = colors.warning, textColor = textOnScene)
                            }
                            weather.cloudCover?.let {
                                GlassMetric(value = "$it%", label = "Clouds", icon = MaterialSymbolIcon("cloud"), accent = colors.hypothesis, textColor = textOnScene)
                            }
                            weather.pressure?.let {
                                GlassMetric(value = "%.0f".format(it), label = "hPa", icon = MaterialSymbolIcon("speed"), accent = colors.project, textColor = textOnScene)
                            }
                        }
                    }

                    // Sunrise / Sunset chips
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        weather.sunrise?.let { sunrise ->
                            Surface(shape = RoundedCornerShape(18.dp), color = textOnScene.copy(alpha = 0.12f), modifier = Modifier.weight(1f)) {
                                Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(MaterialSymbolIcon("sunny"), null, tint = textOnScene.copy(alpha = 0.7f), size = 14.dp)
                                    Text("Sunrise ${formatTimeFromIso(sunrise)}", style = MaterialTheme.typography.labelSmall, color = textOnScene, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        weather.sunset?.let { sunset ->
                            Surface(shape = RoundedCornerShape(18.dp), color = textOnScene.copy(alpha = 0.12f), modifier = Modifier.weight(1f)) {
                                Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(MaterialSymbolIcon("clear_night"), null, tint = textOnScene.copy(alpha = 0.7f), size = 14.dp)
                                    Text("Sunset ${formatTimeFromIso(sunset)}", style = MaterialTheme.typography.labelSmall, color = textOnScene, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                } else if (hasError) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(MaterialSymbolIcon("cloud_off"), null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), size = 48.dp)
                            Text("Weather unavailable — enable GPS", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (isRefreshing) {
                                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.info)
                            }
                            Text(if (isRefreshing) "Refreshing…" else "Fetching weather…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleControlCard(
    useIntervalMode: Boolean,
    onToggleMode: (Boolean) -> Unit,
    selectedSlots: Set<Int>,
    onToggleSlot: (Int) -> Unit,
    selectedInterval: Int,
    onSelectInterval: (Int) -> Unit,
    isAutoCapturing: Boolean,
    onStartCapture: () -> Unit,
    onStopCapture: () -> Unit,
    colors: fieldmind.research.app.features.field.presentation.theme.FieldMindColors
) {
    Card(
        shape = CuteCardDefaults.Shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Capture schedule", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (isAutoCapturing) {
                    FilledTonalButton(
                        onClick = onStopCapture,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f), contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(MaterialSymbolIcon("stop", filled = true), null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Stop", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    FilledTonalButton(
                        onClick = onStartCapture,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = colors.info.copy(alpha = 0.12f), contentColor = colors.info),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(MaterialSymbolIcon("play_arrow", filled = true), null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Start", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Mode toggle
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !useIntervalMode,
                    onClick = { onToggleMode(false) },
                    label = { Text("Specific times", style = MaterialTheme.typography.labelSmall) },
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = useIntervalMode,
                    onClick = { onToggleMode(true) },
                    label = { Text("Every N hours", style = MaterialTheme.typography.labelSmall) },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (useIntervalMode) {
                Text("Capture every:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    INTERVAL_OPTIONS.forEach { interval ->
                        FilterChip(
                            selected = selectedInterval == interval,
                            onClick = { onSelectInterval(interval) },
                            label = { Text("$interval h", style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            } else {
                Text("Times of day (24h):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val allSlots = (0..23).toList()
                for (row in 0..3) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        allSlots.drop(row * 6).take(6).forEach { hour ->
                            val label = if (hour == 0) "0h" else if (hour == 12) "12h" else if (hour < 12) "${hour}h" else "${hour}h"
                            val isSelected = hour in selectedSlots
                            Surface(
                                onClick = { onToggleSlot(hour) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) colors.info.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(width = 44.dp, height = 32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) colors.info else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Status
            if (isAutoCapturing) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.positive.copy(alpha = 0.1f)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(colors.positive))
                        Text(
                            if (useIntervalMode) "Capturing every $selectedInterval hours"
                            else "Capturing at: ${selectedSlots.sorted().joinToString(", ") { "${it}:00" }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.positive,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateGroupSelector(
    groups: List<String>,
    selected: String?,
    onSelect: (String) -> Unit
) {
    val allSelected = selected == null
    val colors = FieldMindTheme.colors

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape).background(colors.info.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(MaterialSymbolIcon("calendar_month"), null, tint = colors.info, size = 16.dp)
                }
                Text("Select day", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = allSelected,
                    onClick = { onSelect("") },
                    label = { Text("All", style = MaterialTheme.typography.labelSmall, fontWeight = if (allSelected) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = if (allSelected) {{
                        Icon(MaterialSymbolIcon("check"), null, size = 14.dp)
                    }} else null
                )
                groups.take(10).forEach { date ->
                    val label = try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val d = sdf.parse(date) ?: Date()
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        when {
                            date == today -> "Today"
                            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(d)
                        }
                    } catch (_: Exception) { date.takeLast(5) }
                    FilterChip(
                        selected = date == selected,
                        onClick = { onSelect(date) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = if (date == selected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = if (date == selected) {{
                            Icon(MaterialSymbolIcon("check"), null, size = 14.dp)
                        }} else null
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportButtonsRow(
    onExportHtml: () -> Unit,
    onExportCsv: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onExportHtml,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = FieldMindTheme.colors.observation)
        ) {
            Icon(MaterialSymbolIcon("description"), null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Export HTML", style = MaterialTheme.typography.labelMedium)
        }
        OutlinedButton(
            onClick = onExportCsv,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = FieldMindTheme.colors.data)
        ) {
            Icon(MaterialSymbolIcon("table_chart"), null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Export CSV", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun WeatherCatalogRecordCard(
    record: WeatherCatalogEntity,
    colors: fieldmind.research.app.features.field.presentation.theme.FieldMindColors,
    tempUnit: String,
    windUnit: String
) {
    val timeStr = remember(record.fetchedAt) {
        try {
            SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(record.fetchedAt))
        } catch (_: Exception) { "Unknown" }
    }
    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val isNight = currentHour < 5 || currentHour >= 20

    // Temperature gradient
    val tempDisplay = record.temperature ?: 20.0
    val displayColors = when {
        tempDisplay < 0 -> listOf(Color(0xFF1A237E), Color(0xFF42A5F5))
        tempDisplay < 10 -> listOf(Color(0xFF1565C0), Color(0xFF64B5F6))
        tempDisplay < 20 -> listOf(Color(0xFF0D47A1), Color(0xFF66BB6A))
        tempDisplay < 30 -> listOf(Color(0xFFE65100), Color(0xFFFFB74D))
        else -> listOf(Color(0xFFBF360C), Color(0xFFE57373))
    }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Weather icon with colored background
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.info.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    MaterialSymbolIcon(iconNameForWeatherCode(record.weatherCode, isNight)),
                    null,
                    tint = colors.info,
                    size = 28.dp
                )
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Temperature with gradient accent
                    Text(
                        record.temperature?.let { WeatherUnitConverter.formatTemp(it, tempUnit) } ?: "--",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            brush = Brush.horizontalGradient(displayColors)
                        )
                    )
                    Text(timeStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }

                // Condition + details row
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (record.weatherDescription.isNotBlank()) {
                        Surface(shape = RoundedCornerShape(10.dp), color = colors.info.copy(alpha = 0.08f)) {
                            Text(
                                record.weatherDescription,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.info,
                                maxLines = 1
                            )
                        }
                    }
                    record.humidity?.let {
                        Text("💧 $it%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    record.windSpeed?.let { ws ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(MaterialSymbolIcon("air"), null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), size = 12.dp)
                            Text(WeatherUnitConverter.formatWind(ws, windUnit), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    record.cloudCover?.let {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(MaterialSymbolIcon("cloud"), null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), size = 12.dp)
                            Text("$it%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    record.pressure?.let {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(MaterialSymbolIcon("speed"), null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), size = 12.dp)
                            Text("${it.roundToInt()} hPa", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (record.placeName.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(MaterialSymbolIcon("location_on"), null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), size = 11.dp)
                        Text(record.placeName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherStatMini(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(
            Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (label) {
                        "Total" -> MaterialSymbolIcon("database")
                        "Avg Temp" -> MaterialSymbolIcon("thermostat")
                        "Avg Humidity" -> MaterialSymbolIcon("water_drop")
                        "Avg Wind" -> MaterialSymbolIcon("air")
                        else -> MaterialSymbolIcon("info")
                    },
                    null, tint = color, size = 20.dp
                )
            }
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Export functions
// ══════════════════════════════════════════════════════════════════════

private suspend fun exportWeatherHtml(
    context: Context,
    catalog: List<WeatherCatalogEntity>,
    tempUnit: String,
    windUnit: String,
    snackbar: SnackbarHostState
) {
    try {
        val sorted = catalog.sortedByDescending { it.fetchedAt }
        val html = buildWeatherHtml(sorted, tempUnit, windUnit)
        val fileName = "weather_catalog_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.html"
        val file = File(context.cacheDir, fileName)
        file.writeText(html)

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Weather Report"))
        snackbar.showSnackbar("HTML report generated")
    } catch (e: Exception) {
        snackbar.showSnackbar("Export failed: ${e.message}")
    }
}

private suspend fun exportWeatherCsv(
    context: Context,
    catalog: List<WeatherCatalogEntity>,
    tempUnit: String,
    snackbar: SnackbarHostState
) {
    try {
        val sorted = catalog.sortedByDescending { it.fetchedAt }
        val csv = buildString {
            appendLine("Timestamp,Temperature (${tempUnit}),Condition,Humidity (%),Wind Speed,Wind Direction,Cloud Cover (%),Pressure (hPa),Place")
            sorted.forEach { w ->
                val ts = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(w.fetchedAt))
                val temp = w.temperature?.let { WeatherUnitConverter.formatTemp(it, tempUnit).replace("°", "") } ?: ""
                val desc = escapeCsv(w.weatherDescription)
                val hum = w.humidity?.toString() ?: ""
                val wind = w.windSpeed?.let { "%s".format(WeatherUnitConverter.formatWind(it, "km/h")) } ?: ""
                val dir = w.windDirection?.toString() ?: ""
                val cloud = w.cloudCover?.toString() ?: ""
                val press = w.pressure?.let { "%.1f".format(it) } ?: ""
                val place = escapeCsv(w.placeName)
                appendLine("$ts,$temp,$desc,$hum,$wind,$dir,$cloud,$press,$place")
            }
        }

        val fileName = "weather_catalog_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
        val file = File(context.cacheDir, fileName)
        file.writeText(csv)

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Weather CSV"))
        snackbar.showSnackbar("CSV exported (${sorted.size} records)")
    } catch (e: Exception) {
        snackbar.showSnackbar("Export failed: ${e.message}")
    }
}

private fun buildWeatherHtml(
    catalog: List<WeatherCatalogEntity>,
    tempUnit: String,
    windUnit: String
): String = buildString {
    appendLine("<!DOCTYPE html><html><head><meta charset=\"utf-8\">")
    appendLine("<title>Weather Catalog Report — FieldMind</title>")
    appendLine("""
    <style>
      * { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Segoe UI', system-ui, sans-serif; }
      body { background: #f5f7f3; color: #1a1a1a; padding: 24px; max-width: 1000px; margin: 0 auto; }
      h1 { font-size: 1.8em; font-weight: 700; color: #1565C0; margin-bottom: 4px; }
      .meta { color: #666; font-size: 0.85em; margin-bottom: 20px; }
      .stats { display: flex; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; }
      .stat { background: white; border-radius: 14px; padding: 14px 20px; flex: 1; min-width: 100px; text-align: center; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
      .stat-value { font-size: 1.6em; font-weight: 800; }
      .stat-label { font-size: 0.8em; color: #666; margin-top: 2px; }
      table { width: 100%; border-collapse: collapse; background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.05); }
      th { background: #e3edf7; color: #1565C0; font-weight: 600; text-align: left; padding: 10px 12px; font-size: 0.82em; }
      td { padding: 8px 12px; border-bottom: 1px solid #eef1f0; font-size: 0.82em; }
      tr:last-child td { border-bottom: none; }
      tr:hover td { background: #f5f9fc; }
      .temp { font-weight: 700; color: #1565C0; }
      .chart-container { background: white; border-radius: 16px; padding: 16px; margin-bottom: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.05); }
      .chart-container svg { max-width: 100%; height: auto; }
      .footer { text-align: center; padding: 24px; font-size: 0.8em; color: #999; }
      @media print { body { padding: 0; } .stat, table, .chart-container { break-inside: avoid; } }
    </style>
    """.trimIndent())
    appendLine("</head><body>")

    // Header
    val dateRange = if (catalog.isNotEmpty()) {
        val dates = catalog.map { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(it.fetchedAt)) }
        "${dates.min()} — ${dates.max()}"
    } else "—"
    appendLine("<h1>🌤 Weather Catalog Report</h1>")
    appendLine("<p class=\"meta\">Generated ${SimpleDateFormat("EEEE, MMMM d, yyyy 'at' HH:mm", Locale.getDefault()).format(Date())} • ${catalog.size} records • $dateRange</p>")

    // Stats
    val temps = catalog.mapNotNull { it.temperature }
    val hums = catalog.mapNotNull { it.humidity?.toDouble() }
    val winds = catalog.mapNotNull { it.windSpeed }
    appendLine("<div class=\"stats\">")
    appendLine(statBox("${catalog.size}", "Records", "#1565C0"))
    appendLine(statBox(temps.average().takeIf { it.isFinite() }?.let { "%.1f°".format(it) } ?: "--", "Avg Temp", "#1565C0"))
    appendLine(statBox(temps.minOrNull()?.let { "%.1f°".format(it) } ?: "--", "Min Temp", "#2E7D32"))
    appendLine(statBox(temps.maxOrNull()?.let { "%.1f°".format(it) } ?: "--", "Max Temp", "#E65100"))
    appendLine(statBox(hums.average().takeIf { it.isFinite() }?.let { "%.0f%%".format(it) } ?: "--", "Avg Humidity", "#00838F"))
    appendLine("</div>")

    // Temperature line chart SVG
    if (temps.size >= 2) {
        appendLine("<div class=\"chart-container\">")
        appendLine("<h2 style=\"font-size:1.1em;margin-bottom:10px;color:#1565C0;\">🌡 Temperature Chart</h2>")
        appendLine(buildTempChartSvg(temps, catalog.map { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(Date(it.fetchedAt)) }))
        appendLine("</div>")
    }

    // Data table
    appendLine("<table><thead><tr>")
    appendLine("<th>Time</th><th>Temp</th><th>Condition</th><th>Humidity</th><th>Wind</th><th>Cloud</th><th>Pressure</th><th>Location</th>")
    appendLine("</tr></thead><tbody>")
    catalog.forEach { w ->
        val ts = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(w.fetchedAt))
        val temp = w.temperature?.let { "%.1f°".format(it) } ?: "--"
        val cond = htmlEscape(w.weatherDescription.ifBlank { "—" })
        val hum = w.humidity?.let { "$it%" } ?: "—"
        val wind = w.windSpeed?.let { "%.1f km/h".format(it) } ?: "—"
        val cloud = w.cloudCover?.let { "$it%" } ?: "—"
        val press = w.pressure?.let { "%.0f".format(it) } ?: "—"
        val place = htmlEscape(w.placeName.ifBlank { "—" })
        appendLine("<tr><td>$ts</td><td class=\"temp\">$temp</td><td>$cond</td><td>$hum</td><td>$wind</td><td>$cloud</td><td>$press hPa</td><td>$place</td></tr>")
    }
    appendLine("</tbody></table>")

    appendLine("<div class=\"footer\">Generated by FieldMind</div>")
    appendLine("</body></html>")
}

private fun buildTempChartSvg(temps: List<Double>, labels: List<String>): String {
    if (temps.size < 2) return ""
    val width = 700
    val height = 250
    val max = temps.max().coerceAtLeast(temps.min() + 5.0)
    val min = temps.min().coerceAtMost(max - 5.0)
    val range = (max - min).coerceAtLeast(1.0)
    val leftMargin = 50
    val rightMargin = 20
    val topMargin = 20
    val bottomMargin = 40
    val chartW = width - leftMargin - rightMargin
    val chartH = height - topMargin - bottomMargin
    val stepX = chartW.toFloat() / (temps.size - 1).coerceAtLeast(1)

    val sb = StringBuilder()
    sb.appendLine("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"$width\" height=\"$height\" viewBox=\"0 0 $width $height\">")
    sb.appendLine("<defs><linearGradient id=\"tempBg\" x1=\"0\" y1=\"0\" x2=\"0\" y2=\"1\"><stop offset=\"0%\" stop-color=\"#f8faf7\"/><stop offset=\"100%\" stop-color=\"#f0f4ec\"/></linearGradient></defs>")
    sb.appendLine("<rect width=\"$width\" height=\"$height\" rx=\"16\" fill=\"url(#tempBg)\"/>")

    // Gridlines
    for (i in 0..4) {
        val y = topMargin + chartH * (1f - i / 4f)
        val valLabel = min + range * i / 4.0
        sb.appendLine("<line x1=\"$leftMargin\" y1=\"$y\" x2=\"${width - rightMargin}\" y2=\"$y\" stroke=\"#ddd\" stroke-width=\"0.5\"/>")
        sb.appendLine("<text x=\"${leftMargin - 8}\" y=\"${y + 4}\" text-anchor=\"end\" font-family=\"sans-serif\" font-size=\"10\" fill=\"#999\">${"%.1f".format(valLabel)}</text>")
    }

    // Line
    val points = temps.mapIndexed { i, v ->
        val x = leftMargin + i * stepX
        val y = topMargin + chartH - ((v - min) / range * chartH).toFloat()
        Pair(x, y)
    }
    val lineD = StringBuilder("M ${points[0].first},${points[0].second}")
    points.drop(1).forEach { (x, y) -> lineD.append(" L $x,$y") }

    // Area fill
    val areaD = StringBuilder("M ${points[0].first},${topMargin + chartH} L ${points[0].first},${points[0].second}")
    points.drop(1).forEach { (x, y) -> areaD.append(" L $x,$y") }
    areaD.append(" L ${points.last().first},${topMargin + chartH} Z")
    sb.appendLine("<path d=\"$areaD\" fill=\"#1565C0\" opacity=\"0.06\"/>")
    sb.appendLine("<path d=\"$lineD\" stroke=\"#1565C0\" stroke-width=\"2.5\" fill=\"none\" stroke-linejoin=\"round\" stroke-linecap=\"round\"/>")

    // Dots
    points.forEach { (x, y) ->
        sb.appendLine("<circle cx=\"$x\" cy=\"$y\" r=\"3.5\" fill=\"white\" stroke=\"#1565C0\" stroke-width=\"2\"/>")
    }

    // Labels
    val labelStep = (temps.size / 8f).coerceAtLeast(1f).toInt()
    temps.forEachIndexed { i, _ ->
        if (i % labelStep == 0 || i == temps.lastIndex) {
            val x = leftMargin + i * stepX
            val l = labels.getOrElse(i) { "" }
            sb.appendLine("<text x=\"$x\" y=\"${height - bottomMargin + 18}\" text-anchor=\"middle\" font-family=\"sans-serif\" font-size=\"8\" fill=\"#666\" transform=\"rotate(-30, $x, ${height - bottomMargin + 18})\">${htmlEscape(l.take(10))}</text>")
        }
    }

    sb.appendLine("</svg>")
    return sb.toString()
}

private fun statBox(value: String, label: String, color: String): String = """
    <div class="stat" style="border-left: 4px solid $color">
      <div class="stat-value" style="color: $color">$value</div>
      <div class="stat-label">$label</div>
    </div>
""".trimIndent()

private fun htmlEscape(s: String): String = s
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")

private fun escapeCsv(s: String): String = if (s.contains(',') || s.contains('"') || s.contains('\n')) {
    "\"${s.replace("\"", "\"\"")}\""
} else s

private data class CatalogStats(
    val count: Int = 0,
    val avgTemp: Double? = null,
    val minTemp: Double? = null,
    val maxTemp: Double? = null,
    val avgHumidity: Double? = null,
    val avgWind: Double? = null
)

@Composable
private fun GlassMetric(
    value: String,
    label: String,
    icon: MaterialSymbolIcon,
    accent: Color,
    textColor: Color = Color.White
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(accent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accent, size = 20.dp)
        }
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.7f))
    }
}

private fun iconNameForWeatherCode(code: Int, isNight: Boolean = false): String = when {
    code <= 1 -> if (isNight) "clear_night" else "sunny"
    code in 2..3 -> "cloud"
    code in 45..48 -> "foggy"
    code in 51..67 || code in 80..82 -> "rainy"
    code in 71..77 || code in 85..86 -> "weather_snowy"
    code >= 95 -> "thunderstorm"
    else -> if (isNight) "partly_cloudy_night" else "partly_cloudy_day"
}

// formatTimeFromIso is defined in FieldMindHomeScreen.kt (internal)
