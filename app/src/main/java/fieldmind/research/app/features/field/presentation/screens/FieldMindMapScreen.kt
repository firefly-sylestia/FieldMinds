package fieldmind.research.app.features.field.presentation.screens
import fieldmind.research.app.ui.theme.CuteCardDefaults

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import fieldmind.research.app.features.field.data.database.entity.*
import fieldmind.research.app.features.field.data.location.*
import fieldmind.research.app.features.field.presentation.components.*
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import fieldmind.research.app.features.field.presentation.navigation.FieldMindScreen
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.features.field.presentation.viewmodel.FieldMindViewModel
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import kotlinx.coroutines.launch
import androidx.compose.foundation.border
import org.json.JSONObject

// ══════════════════════════════════════════════════════════════════════
// ══════════════════════════════════════════════════════════════════════
//  Map Screen — full-screen osmdroid map with offline tiles, drawing tools,
//  track recording, and geo-fence reminders (PRO FEATURE)
// ══════════════════════════════════════════════════════════════════════

private enum class MapTab { Map, OfflineTiles, Drawings, Tracks, Geofences }

@Composable
fun MapFieldScreen(
    viewModel: FieldMindViewModel,
    onNavigate: (FieldMindScreen) -> Unit = {},
    onOpenDetail: (String, Long) -> Unit = { _, _ -> },
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val observations by viewModel.observations.collectAsState()
    val points = observations.mapNotNull { o ->
        o.latitude?.let { lat -> o.longitude?.let { lon -> lat to lon } }
    }
    val colors = FieldMindTheme.colors
    var fullScreen by remember { mutableStateOf(false) }

    // ── Pro Feature Managers (shared across tabs) ──
    val tileManager = remember { OsmTileManager(context) }
    val trackRecorder = remember { TrackRecorder(context) }
    val geoFenceReminder = remember { GeoFenceReminder(context) }
    val geofenceRegions by geoFenceReminder.activeRegions.collectAsState()

    val isRecording by trackRecorder.isRecording.collectAsState()
    val currentTrack by trackRecorder.currentRecording.collectAsState()
    val savedTracks by trackRecorder.savedTracks.collectAsState()

    // Drawing tools state — persisted to SharedPreferences across app restarts
    val prefs = remember { context.getSharedPreferences("fieldmind_map", Context.MODE_PRIVATE) }
    var savedOverlays by remember {
        mutableStateOf(MapOverlayUtils.deserializeOverlays(prefs.getString("savedOverlays", "") ?: ""))
    }
    // Persist overlays whenever they change
    LaunchedEffect(savedOverlays) {
        prefs.edit().putString("savedOverlays", MapOverlayUtils.serializeOverlays(savedOverlays)).apply()
    }

    // Persist tracks to SharedPreferences
    var persistedTracks by remember {
        mutableStateOf(loadTracksFromPrefs(prefs))
    }
    // Sync from TrackRecorder into persisted list, and vice versa
    LaunchedEffect(savedTracks) {
        if (savedTracks.isNotEmpty()) {
            persistedTracks = savedTracks
            saveTracksToPrefs(prefs, savedTracks)
        }
    }
    // On first launch, restore persisted tracks into the recorder
    LaunchedEffect(Unit) {
        if (persistedTracks.isNotEmpty() && savedTracks.isEmpty()) {
            persistedTracks.forEach { trackRecorder.restoreTrack(it) }
        }
    }

    var drawingMode by remember { mutableStateOf(DrawingMode.View) }
    var editingOverlay by remember { mutableStateOf<MapOverlay?>(null) }
    val cachedRegions by tileManager.cachedRegions.collectAsState()
    // cachedRegions type is now List<OsmTileRegion>

    // Restore saved geofences on first launch
    LaunchedEffect(Unit) {
        geoFenceReminder.restoreRegions()
    }

    // Tab state
    var activeTab by remember { mutableStateOf(MapTab.Map) }

    // ── Full-screen map mode ──
    if (fullScreen && points.isNotEmpty()) {
        FullScreenMapView(
            points = points,
            savedOverlays = savedOverlays,
            drawingMode = drawingMode,
            currentTrack = currentTrack,
            tileManager = tileManager,
            geofenceRegions = geofenceRegions,
            onClose = { fullScreen = false },
            onDrawingModeChanged = { drawingMode = it },
            onOverlaysChanged = { savedOverlays = it },
            onPointCreated = { overlay ->
                savedOverlays = savedOverlays + overlay
                // Auto-create geofence for new points
                geoFenceReminder.addRegion(
                    geoFenceReminder.regionFromPointOverlay(overlay)
                )
            },
            onLineCreated = { savedOverlays = savedOverlays + it },
            onPolygonCreated = { savedOverlays = savedOverlays + it }
        )
        return
    }

    // ── Main screen with tabs ──
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            onClick = { onNavigate(FieldMindScreen.Home) },
                            shape = CuteCardDefaults.ButtonShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(44.dp)
                        ) { Box(contentAlignment = Alignment.Center) { Icon(FieldMindIcons.Back, null, size = 22.dp) } }
                        Column {
                            Text("Field Map", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("PRO Feature", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.weight(1f))
                        if (points.isNotEmpty()) {
                            Surface(
                                onClick = { fullScreen = true },
                                shape = CuteCardDefaults.ButtonShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(44.dp)
                            ) { Box(contentAlignment = Alignment.Center) { Icon(icon = MaterialSymbolIcon("fullscreen"), contentDescription = "Fullscreen map", size = 22.dp) } }
                        }
                    }
                    PrimaryScrollableTabRow(
                        selectedTabIndex = activeTab.ordinal,
                        edgePadding = 20.dp,
                        divider = {}
                    ) {
                        MapTab.entries.forEach { tab ->
                            Tab(
                                selected = activeTab == tab,
                                onClick = { activeTab = tab },
                                text = {
                                    Text(
                                        when (tab) {
                                            MapTab.Map -> "Map"
                                            MapTab.OfflineTiles -> "Offline tiles"
                                            MapTab.Drawings -> "Drawings"
                                            MapTab.Tracks -> "Tracks"
                                            MapTab.Geofences -> "Geo-fences"
                                        },
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        when (activeTab) {
            MapTab.Map -> MapViewTab(
                modifier = Modifier.padding(padding),
                points = points,
                savedOverlays = savedOverlays,
                drawingMode = drawingMode,
                currentTrack = currentTrack,
                isRecording = isRecording,
                tileManager = tileManager,
                onFullScreen = { fullScreen = true },
                onDrawingModeChanged = { drawingMode = it },
                onOverlaysChanged = { savedOverlays = it },
                onPointCreated = { overlay ->
                    savedOverlays = savedOverlays + overlay
                    geoFenceReminder.addRegion(geoFenceReminder.regionFromPointOverlay(overlay))
                },
                onLineCreated = { savedOverlays = savedOverlays + it },
                onPolygonCreated = { savedOverlays = savedOverlays + it },                    onStartTrack = { trackRecorder.startRecording("Field session") },
                onStopTrack = { trackRecorder.stopRecording() },
                onToggleTrackPause = { trackRecorder.togglePause() },
                geofenceRegions = geofenceRegions
            )
            MapTab.OfflineTiles -> OfflineTilesTab(
                modifier = Modifier.padding(padding),
                tileManager = tileManager,
                cachedRegions = cachedRegions,
                points = points
            )
            MapTab.Drawings -> DrawingsTab(
                modifier = Modifier.padding(padding),
                overlays = savedOverlays,
                onDeleteOverlay = { id -> savedOverlays = savedOverlays.filter { it.id != id } },
                onClearAll = { savedOverlays = emptyList() },
                onEditOverlay = { editingOverlay = it }
            )
            MapTab.Tracks -> TracksTab(
                modifier = Modifier.padding(padding),
                savedTracks = savedTracks,
                currentTrack = currentTrack,
                isRecording = isRecording,
                trackRecorder = trackRecorder,
                context = context
            )
            MapTab.Geofences -> GeofencesTab(
                modifier = Modifier.padding(padding),
                geoFenceReminder = geoFenceReminder,
                geofenceRegions = geofenceRegions
            )
        }

        // ── Overlay edit dialog ──
        editingOverlay?.let { overlay ->
            EditOverlayDialog(
                overlay = overlay,
                onSave = { edited ->
                    val current = savedOverlays.toMutableList()
                    val idx = current.indexOfFirst { it.id == overlay.id }
                    if (idx >= 0) {
                        current[idx] = edited
                        savedOverlays = current
                    }
                    editingOverlay = null
                },
                onDismiss = { editingOverlay = null }
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Full-Screen Map View (with drawing toolbar)
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun FullScreenMapView(
    points: List<Pair<Double, Double>>,
    savedOverlays: List<MapOverlay>,
    drawingMode: DrawingMode,
    currentTrack: TrackRecording?,
    tileManager: OsmTileManager? = null,
    geofenceRegions: List<GeofenceRegion> = emptyList(),
    onClose: () -> Unit,
    onDrawingModeChanged: (DrawingMode) -> Unit,
    onOverlaysChanged: (List<MapOverlay>) -> Unit,
    onPointCreated: (MapOverlay.PointOverlay) -> Unit,
    onLineCreated: (MapOverlay.LineOverlay) -> Unit,
    onPolygonCreated: (MapOverlay.PolygonOverlay) -> Unit
) {
    val colors = FieldMindTheme.colors

    Box(Modifier.fillMaxSize()) {
        OsmMapView(
            points = points,
            savedOverlays = savedOverlays,
            drawingMode = drawingMode,
            currentTrackPoints = currentTrack?.points?.map { it.latitude to it.longitude } ?: emptyList(),
            tileManager = tileManager,
            geofenceRegions = geofenceRegions,
            onPointCreated = onPointCreated,
            onLineCreated = onLineCreated,
            onPolygonCreated = onPolygonCreated,
            onOverlaysChanged = onOverlaysChanged,
            modifier = Modifier.fillMaxSize()
        )

        // Top bar
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilledTonalIconButton(onClick = onClose) {
                Icon(FieldMindIcons.Close, null, size = 20.dp)
            }
            if (drawingMode != DrawingMode.View) {
                Surface(
                    shape = CuteCardDefaults.Shape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        when (drawingMode) {
                            DrawingMode.PlacePoint -> "Tap to place point"
                            DrawingMode.DrawLine -> "Tap points to draw transect"
                            DrawingMode.DrawPolygon -> "Tap boundary points (tap first to close)"
                            else -> ""
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.size(40.dp))
        }

        // Drawing toolbar (bottom-center)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            shape = CuteCardDefaults.ShapeHero,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 8.dp
        ) {
            Row(
                Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DrawToolButton(
                    icon = FieldMindIcons.Location,
                    label = "Point",
                    isActive = drawingMode == DrawingMode.PlacePoint,
                    onClick = { onDrawingModeChanged(if (drawingMode == DrawingMode.PlacePoint) DrawingMode.View else DrawingMode.PlacePoint) }
                )
                DrawToolButton(
                    icon = FieldMindIcons.Line,
                    label = "Line",
                    isActive = drawingMode == DrawingMode.DrawLine,
                    onClick = { onDrawingModeChanged(if (drawingMode == DrawingMode.DrawLine) DrawingMode.View else DrawingMode.DrawLine) }
                )
                DrawToolButton(
                    icon = FieldMindIcons.Shape,
                    label = "Polygon",
                    isActive = drawingMode == DrawingMode.DrawPolygon,
                    onClick = { onDrawingModeChanged(if (drawingMode == DrawingMode.DrawPolygon) DrawingMode.View else DrawingMode.DrawPolygon) }
                )
                DrawToolHorizontalDivider()
                DrawToolButton(
                    icon = FieldMindIcons.Select,
                    label = "Select",
                    isActive = drawingMode == DrawingMode.Select,
                    onClick = { onDrawingModeChanged(if (drawingMode == DrawingMode.Select) DrawingMode.View else DrawingMode.Select) }
                )
                DrawToolButton(
                    icon = FieldMindIcons.Delete,
                    label = "Clear",
                    isActive = false,
                    onClick = { onOverlaysChanged(emptyList()) }
                )
            }
        }

        // Attribution
        Text(
            "© OpenStreetMap contributors",
            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun DrawToolButton(icon: MaterialSymbolIcon, label: String, isActive: Boolean, onClick: () -> Unit) {
    val bgColor = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    else MaterialTheme.colorScheme.surfaceContainerHigh
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CuteCardDefaults.ShapeCompact)
            .clickable(onClick = onClick)
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            icon = icon,
            contentDescription = label,
            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            size = 22.dp
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DrawToolHorizontalDivider() {
    Box(
        Modifier
            .width(1.dp)
            .height(40.dp)
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    )
}

// ══════════════════════════════════════════════════════════════════════
//  Tabs
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun MapViewTab(
    modifier: Modifier,
    points: List<Pair<Double, Double>>,
    savedOverlays: List<MapOverlay>,
    drawingMode: DrawingMode,
    currentTrack: TrackRecording?,
    isRecording: Boolean,
    tileManager: OsmTileManager? = null,
    geofenceRegions: List<GeofenceRegion> = emptyList(),
    onFullScreen: () -> Unit,
    onDrawingModeChanged: (DrawingMode) -> Unit,
    onOverlaysChanged: (List<MapOverlay>) -> Unit,
    onPointCreated: (MapOverlay.PointOverlay) -> Unit,
    onLineCreated: (MapOverlay.LineOverlay) -> Unit,
    onPolygonCreated: (MapOverlay.PolygonOverlay) -> Unit,
    onStartTrack: () -> Unit,
    onStopTrack: () -> Unit,
    onToggleTrackPause: () -> Unit
) {
    val colors = FieldMindTheme.colors

    Column(modifier.fillMaxSize()) {
        // Map preview
        Box(
            Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {                OsmMapView(
                    points = points,
                    savedOverlays = savedOverlays,
                    drawingMode = drawingMode,
                    currentTrackPoints = currentTrack?.points?.map { it.latitude to it.longitude } ?: emptyList(),
                    tileManager = tileManager,
                    geofenceRegions = geofenceRegions,
                    onPointCreated = onPointCreated,
                    onLineCreated = onLineCreated,
                    onPolygonCreated = onPolygonCreated,
                    onOverlaysChanged = onOverlaysChanged,
                    modifier = Modifier.fillMaxSize()
                )
            // Track recording indicator
            if (isRecording) {
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error))
                        Text("REC ${currentTrack?.points?.size ?: 0} pts", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
            // Full-screen button
            IconButton(
                onClick = onFullScreen,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
            ) {
                Icon(FieldMindIcons.MapFull, null, tint = MaterialTheme.colorScheme.onSurface, size = 24.dp)
            }
        }

        // Action bar
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Drawing mode chips
            item {
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Drawing tools", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = drawingMode == DrawingMode.PlacePoint, onClick = { onDrawingModeChanged(if (drawingMode == DrawingMode.PlacePoint) DrawingMode.View else DrawingMode.PlacePoint) }, label = { Text("Point") }, leadingIcon = { Icon(FieldMindIcons.Location, null, size = 16.dp) })
                            FilterChip(selected = drawingMode == DrawingMode.DrawLine, onClick = { onDrawingModeChanged(if (drawingMode == DrawingMode.DrawLine) DrawingMode.View else DrawingMode.DrawLine) }, label = { Text("Line") }, leadingIcon = { Icon(FieldMindIcons.Line, null, size = 16.dp) })
                            FilterChip(selected = drawingMode == DrawingMode.DrawPolygon, onClick = { onDrawingModeChanged(if (drawingMode == DrawingMode.DrawPolygon) DrawingMode.View else DrawingMode.DrawPolygon) }, label = { Text("Polygon") }, leadingIcon = { Icon(FieldMindIcons.Shape, null, size = 16.dp) })
                        }
                    }
                }
            }

            // Track recording card
            item {
                TrackRecordingCard(
                    isRecording = isRecording,
                    currentTrack = currentTrack,
                    onStart = onStartTrack,
                    onStop = onStopTrack,
                    onTogglePause = onToggleTrackPause
                )
            }

            // Stats card
            item {
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${points.size} observation${if (points.size == 1) "" else "s"} with GPS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Use the full-screen map for drawing tools, track recording, and geo-fencing.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackRecordingCard(
    isRecording: Boolean,
    currentTrack: TrackRecording?,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onTogglePause: () -> Unit
) {
    val colors = FieldMindTheme.colors
    Card(
        shape = CuteCardDefaults.Shape,
        colors = CardDefaults.cardColors(containerColor = if (isRecording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(FieldMindIcons.Track, null, tint = if (isRecording) MaterialTheme.colorScheme.error else colors.info, size = 20.dp)
                Text("Track recording", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            if (isRecording && currentTrack != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${currentTrack.points.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text("Points", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val dist = currentTrack.distanceMeters
                        Text(if (dist < 1000) "%.0f m".format(dist) else "%.2f km".format(dist / 1000), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Distance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val elapsed = System.currentTimeMillis() - currentTrack.startedAt
                        val sec = elapsed / 1000
                        Text("%02d:%02d".format(sec / 60, sec % 60), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Elapsed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onStop, shape = CuteCardDefaults.ButtonShape, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Stop") }
                    OutlinedButton(onClick = onTogglePause, shape = CuteCardDefaults.ButtonShape) { Text(if (currentTrack.isPaused) "Resume" else "Pause") }
                }
            } else {
                Text("Record GPS tracks during your field sessions to map your survey path.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = onStart, shape = CuteCardDefaults.ButtonShape) { Icon(FieldMindIcons.Track, null, size = 18.dp); Spacer(Modifier.size(6.dp)); Text("Start recording") }
            }
        }
    }
}

@Composable
private fun OfflineTilesTab(
    modifier: Modifier,
    tileManager: OsmTileManager,
    cachedRegions: List<OsmTileRegion>,
    points: List<Pair<Double, Double>> = emptyList()
) {
    val scope = rememberCoroutineScope()
    val isDownloading by tileManager.isDownloading.collectAsState()
    val downloadProgress by tileManager.downloadProgress.collectAsState()
    var showDownloadDialog by remember { mutableStateOf(false) }
    var cacheSize by remember { mutableStateOf("—") }

    LaunchedEffect(Unit) {
        tileManager.refreshCachedRegions()
        val bytes = tileManager.getCacheSizeBytes()
        cacheSize = if (bytes < 1_000_000) "${bytes / 1024} KB" else "%.1f MB".format(bytes / 1_000_000.0)
    }

    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(FieldMindIcons.Download, null, tint = FieldMindTheme.colors.info, size = 20.dp)
                        Text("Offline tile cache", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Text(                "Download tiles for offline use so the map works without internet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Cache: $cacheSize", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${cachedRegions.size} region${if (cachedRegions.size == 1) "" else "s"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = { showDownloadDialog = true }, shape = CuteCardDefaults.ButtonShape, enabled = !isDownloading) { Text("Download new region") }
                    if (cachedRegions.isNotEmpty()) {
                        OutlinedButton(onClick = {
                            scope.launch { tileManager.clearAllCaches(); cacheSize = "0 KB" }
                        }, shape = CuteCardDefaults.ButtonShape) { Text("Clear all") }
                    }
                }
            }
        }
        if (cachedRegions.isNotEmpty()) {
            item { Text("Cached regions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }
        itemsIndexed(cachedRegions) { i, region ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CuteCardDefaults.ShapeCompact,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(region.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("Z${region.minZoom}-${region.maxZoom}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("%.4f, %.4f — %.4f, %.4f".format(region.north, region.west, region.south, region.east), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("%.1f%%".format(region.progress * 100), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            if (region.downloadedAt > 0) {
                                Text(java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(java.util.Date(region.downloadedAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDownloadDialog) {
        DownloadRegionDialog(
            onDismiss = { showDownloadDialog = false },
            tileManager = tileManager,
            points = points
        )
    }
}

@Composable
private fun DownloadRegionDialog(
    onDismiss: () -> Unit,
    tileManager: OsmTileManager,
    points: List<Pair<Double, Double>> = emptyList()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var latNorth by remember { mutableStateOf("") }
    var latSouth by remember { mutableStateOf("") }
    var lonEast by remember { mutableStateOf("") }
    var lonWest by remember { mutableStateOf("") }
    var minZoom by remember { mutableStateOf("10") }
    var maxZoom by remember { mutableStateOf("16") }
    var useLocationText by remember { mutableStateOf("Use my location") }
    var showRegionPicker by remember { mutableStateOf(false) }

    // ── Region picker (full-screen map selection) ──
    if (showRegionPicker) {
        RegionPickerOverlay(
            points = points,
            onRegionSelected = { north, south, east, west ->
                latNorth = "%.4f".format(north)
                latSouth = "%.4f".format(south)
                lonEast = "%.4f".format(east)
                lonWest = "%.4f".format(west)
                if (name.isBlank()) name = "Selected area"
                showRegionPicker = false
            },
            onCancel = { showRegionPicker = false }
        )
        return
    }

    SwipeableAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Download tile region") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Download tiles for offline use. Enter the bounding box coordinates, or use your current GPS location.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                // ── Action buttons row ──
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showRegionPicker = true },
                        shape = CuteCardDefaults.ButtonShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(FieldMindIcons.MapFull, null, size = 18.dp)
                        Spacer(Modifier.size(6.dp))
                        Text("Pick from map")
                    }
                    OutlinedButton(
                        onClick = {
                            useLocationText = "Getting location…"
                            val provider = FieldLocationProvider(context)
                            provider.requestCurrentLocation(timeoutMs = 15_000L) { loc ->
                                if (loc != null) {
                                    val offset = 0.045 // ~5km in decimal degrees
                                    latNorth = "%.4f".format(loc.latitude + offset)
                                    latSouth = "%.4f".format(loc.latitude - offset)
                                    lonEast = "%.4f".format(loc.longitude + offset)
                                    lonWest = "%.4f".format(loc.longitude - offset)
                                    name = loc.placeName?.takeIf { it.isNotBlank() } ?: "My area"
                                    useLocationText = "✓ Location set"
                                } else {
                                    useLocationText = "GPS unavailable — enable location"
                                }
                            }
                        },
                        shape = CuteCardDefaults.ButtonShape,
                        enabled = useLocationText != "Getting location…",
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(FieldMindIcons.Location, null, size = 18.dp)
                        Spacer(Modifier.size(6.dp))
                        Text(useLocationText)
                    }
                }

                FieldTextField(name, { name = it }, "Region name (e.g. Study Area)")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldTextField(latNorth, { latNorth = it }, "Lat N", modifier = Modifier.weight(1f))
                    FieldTextField(latSouth, { latSouth = it }, "Lat S", modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldTextField(lonEast, { lonEast = it }, "Lon E", modifier = Modifier.weight(1f))
                    FieldTextField(lonWest, { lonWest = it }, "Lon W", modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldTextField(minZoom, { minZoom = it }, "Min zoom", modifier = Modifier.weight(1f))
                    FieldTextField(maxZoom, { maxZoom = it }, "Max zoom", modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        tileManager.downloadRegion(
                            name = name.ifBlank { "Study area" },
                            north = latNorth.toDoubleOrNull() ?: 0.0,
                            south = latSouth.toDoubleOrNull() ?: 0.0,
                            east = lonEast.toDoubleOrNull() ?: 0.0,
                            west = lonWest.toDoubleOrNull() ?: 0.0,
                            minZoom = minZoom.toIntOrNull() ?: 10,
                            maxZoom = maxZoom.toIntOrNull() ?: 16
                        )
                    }
                    onDismiss()
                },
                shape = CuteCardDefaults.ButtonShape,
                enabled = name.isNotBlank() && latNorth.isNotBlank() && latSouth.isNotBlank() && lonEast.isNotBlank() && lonWest.isNotBlank()
            ) { Text("Download") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/**
 * Full-screen map overlay for selecting a rectangular bounding box by tapping two corners.
 * First tap marks the NW corner, second tap marks the SE corner and draws the rectangle.
 * Tapping again resets and starts over.
 */
@Composable
private fun RegionPickerOverlay(
    points: List<Pair<Double, Double>> = emptyList(),
    onRegionSelected: (north: Double, south: Double, east: Double, west: Double) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    val locationProvider = remember { FieldLocationProvider(context) }
    var zoomingToLocation by remember { mutableStateOf(false) }

    // Store observation markers so drawRegionRect can preserve them across taps
    val obsMarkers = remember { mutableListOf<Marker>() }

    // Two corners of the bounding box
    var corner1 by remember { mutableStateOf<GeoPoint?>(null) }
    var corner2 by remember { mutableStateOf<GeoPoint?>(null) }

    // Derived bounding box (sorted)
    val north = remember(corner1, corner2) {
        if (corner1 != null && corner2 != null) maxOf(corner1!!.latitude, corner2!!.latitude) else null
    }
    val south = remember(corner1, corner2) {
        if (corner1 != null && corner2 != null) minOf(corner1!!.latitude, corner2!!.latitude) else null
    }
    val east = remember(corner1, corner2) {
        if (corner1 != null && corner2 != null) maxOf(corner1!!.longitude, corner2!!.longitude) else null
    }
    val west = remember(corner1, corner2) {
        if (corner1 != null && corner2 != null) minOf(corner1!!.longitude, corner2!!.longitude) else null
    }

    // Rectangle polygon overlay
    val rectPoints = remember(north, south, east, west) {
        if (north != null && south != null && east != null && west != null) {
            listOf(
                north to west,  // NW
                north to east,  // NE
                south to east,  // SE
                south to west   // SW
            )
        } else null
    }

    val statusText = when {
        corner1 == null -> "Tap the NW corner of the area"
        corner2 == null -> "Tap the SE corner"
        else -> "Tap again to reset, or confirm below"
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // ── osmdroid map ──
        AndroidView(
            factory = { ctx ->
                org.osmdroid.config.Configuration.getInstance().apply {
                    userAgentValue = context.packageName
                    osmdroidBasePath = context.cacheDir.resolve("osmdroid")
                    osmdroidTileCache = context.cacheDir.resolve("osmdroid/tiles")
                }
                MapView(ctx).also { mv ->
                    mapView = mv
                    mv.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                    mv.setMultiTouchControls(true)
                    mv.setTilesScaledToDpi(true)
                    mv.controller.setZoom(13.0)
                    mv.controller.setCenter(GeoPoint(20.0, 78.0)) // Default center India

                    // Render existing observation points as markers (stored for drawRegionRect preservation)
                    obsMarkers.clear()
                    val obsIcon = observationMarkerBitmap(context)
                    points.forEach { (lat, lon) ->
                        val marker = Marker(mv).apply {
                            position = GeoPoint(lat, lon)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            setInfoWindow(null)
                            icon = obsIcon
                        }
                        obsMarkers.add(marker)
                        mv.overlays.add(marker)
                    }

                    // Tap handler for region selection
                    val eventsOverlay = org.osmdroid.views.overlay.MapEventsOverlay(object : org.osmdroid.events.MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                            if (corner1 == null || (corner1 != null && corner2 != null)) {
                                // First tap or reset: set corner1, clear corner2
                                corner1 = p
                                corner2 = null
                            } else {
                                // Second tap: set corner2
                                corner2 = p
                            }
                            // Redraw rectangle (preserving observation markers)
                            drawRegionRect(mv, corner1, corner2, obsMarkers)
                            return true
                        }
                        override fun longPressHelper(p: GeoPoint): Boolean = false
                    })
                    mv.overlays.add(0, eventsOverlay)
                    mv.onResume()
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mv ->
                drawRegionRect(mv, corner1, corner2, obsMarkers)
            }
        )

        // ── Top bar with status ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f),
            shadowElevation = 4.dp
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalIconButton(onClick = onCancel, modifier = Modifier.size(40.dp)) {
                    Icon(FieldMindIcons.Close, null, size = 20.dp)
                }
                Column(Modifier.weight(1f)) {
                    Text("Select region", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(statusText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Zoom to my location button
                val hasLocPerm = locationProvider.hasAnyLocationPermission()
                FilledTonalIconButton(
                    onClick = {
                        if (hasLocPerm) {
                            zoomingToLocation = true
                            locationProvider.requestCurrentLocation(timeoutMs = 8_000L) { loc ->
                                zoomingToLocation = false
                                if (loc != null) {
                                    mapView?.controller?.animateTo(
                                        GeoPoint(loc.latitude, loc.longitude),
                                        15.0, // street-level zoom
                                        1000L // duration ms
                                    )
                                }
                            }
                        }
                    },
                    enabled = !zoomingToLocation && hasLocPerm,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        FieldMindIcons.Location,
                        null,
                        size = 20.dp
                    )
                }
                if (rectPoints != null) {
                    FilledTonalIconButton(
                        onClick = { corner1 = null; corner2 = null },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(FieldMindIcons.Refresh, null, size = 20.dp)
                    }
                }
            }
        }

        // ── Bottom confirm bar ──
        if (rectPoints != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f),
                shadowElevation = 8.dp
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("%.4f N, %.4f S".format(north!!, south!!), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("%.4f E, %.4f W".format(east!!, west!!), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(onClick = { corner1 = null; corner2 = null }, shape = CuteCardDefaults.ButtonShape) {
                        Text("Reset")
                    }
                    Button(
                        onClick = { onRegionSelected(north!!, south!!, east!!, west!!) },
                        shape = CuteCardDefaults.ButtonShape
                    ) {
                        Icon(FieldMindIcons.Check, null, size = 18.dp)
                        Spacer(Modifier.size(6.dp))
                        Text("Use this area")
                    }
                }
            }
        }

        // ── Attribution ──
        Text(
            "© OpenStreetMap contributors",
            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }

    // Lifecycle
    DisposableEffect(Unit) {
        onDispose {
            mapView?.onPause()
            mapView?.onDetach()
        }
    }
}

/**
 * Creates a small circular bitmap drawable for rendering observation points
 * as subtle map markers. Uses a semi-transparent teal glow with a solid
 * center dot for clear but unobtrusive visibility.
 */
private fun observationMarkerBitmap(context: Context): BitmapDrawable {
    val size = 24
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val cx = (size / 2).toFloat()
    val cy = (size / 2).toFloat()

    // Outer glow ring (faint)
    val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(50, 0, 150, 136) // Teal with ~20% opacity
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, 10f, glowPaint)

    // Solid center dot
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(220, 0, 150, 136) // Teal with ~86% opacity
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, 4f, dotPaint)

    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Draws (or clears) the rectangular bounding box overlay on the map.
 * Removes all overlays except the events overlay (preserved at index 0)
 * and re-adds any observation markers from [obsMarkers] so they persist
 * across tap interactions.
 */
private fun drawRegionRect(
    mapView: MapView?,
    corner1: GeoPoint?,
    corner2: GeoPoint?,
    obsMarkers: List<Marker> = emptyList()
) {
    if (mapView == null) return
    // Preserve the events overlay and re-add observation markers
    val eventsOverlay = mapView.overlays.getOrNull(0)
    mapView.overlays.clear()
    if (eventsOverlay != null) mapView.overlays.add(eventsOverlay)
    obsMarkers.forEach { m -> mapView.overlays.add(m) }

    if (corner1 != null && corner2 != null) {
        val n = maxOf(corner1.latitude, corner2.latitude)
        val s = minOf(corner1.latitude, corner2.latitude)
        val e = maxOf(corner1.longitude, corner2.longitude)
        val w = minOf(corner1.longitude, corner2.longitude)

        val rect = org.osmdroid.views.overlay.Polygon().apply {
            setPoints(listOf(
                GeoPoint(n, w), // NW
                GeoPoint(n, e), // NE
                GeoPoint(s, e), // SE
                GeoPoint(s, w)  // SW
            ))
            fillPaint.color = android.graphics.Color.argb(30, 33, 150, 243)
            outlinePaint.color = android.graphics.Color.parseColor("#2196F3")
            outlinePaint.strokeWidth = 3f
        }
        mapView.overlays.add(rect)

        // Show corner markers
        listOf(corner1, corner2).forEach { p ->
            val marker = Marker(mapView).apply {
                position = p
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                setInfoWindow(null)
                title = ""
            }
            mapView.overlays.add(marker)
        }

        // Zoom to fit the bounding box
        mapView.zoomToBoundingBox(
            org.osmdroid.util.BoundingBox(n, e, s, w),
            true,
            48
        )
    }

    mapView.invalidate()
}

@Composable
private fun DrawingsTab(
    modifier: Modifier,
    overlays: List<MapOverlay>,
    onDeleteOverlay: (String) -> Unit,
    onClearAll: () -> Unit,
    onEditOverlay: (MapOverlay) -> Unit
) {
    val colors = FieldMindTheme.colors
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(FieldMindIcons.Shape, null, tint = colors.info, size = 20.dp)
                        Text("Saved drawings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Text("${overlays.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (overlays.isEmpty()) {
                        Text("No drawings yet. Use the drawing tools in full-screen map mode to mark sites, transects, and survey boundaries.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (overlays.isNotEmpty()) {
                        OutlinedButton(onClick = onClearAll, shape = CuteCardDefaults.ButtonShape, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Clear all drawings") }
                    }
                }
            }
        }
        itemsIndexed(overlays) { i, overlay ->
            ClickableCard(
                onClick = { onEditOverlay(overlay) },
                index = i,
                animate = true,
                shape = CuteCardDefaults.ShapeCompact,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val icon = when (overlay) {
                        is MapOverlay.PointOverlay -> FieldMindIcons.Location
                        is MapOverlay.LineOverlay -> FieldMindIcons.Line
                        is MapOverlay.PolygonOverlay -> FieldMindIcons.Shape
                    }
                    Icon(icon, null, tint = Color(overlay.color.toInt()), size = 22.dp)
                    Column(Modifier.weight(1f)) {
                        Text(overlay.label.ifBlank { when (overlay) {
                            is MapOverlay.PointOverlay -> "Site point"
                            is MapOverlay.LineOverlay -> "Transect"
                            is MapOverlay.PolygonOverlay -> "Survey boundary"
                        } }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(when (overlay) {
                            is MapOverlay.PointOverlay -> "%.5f, %.5f".format(overlay.latitude, overlay.longitude)
                            is MapOverlay.LineOverlay -> "${overlay.points.size} points"
                            is MapOverlay.PolygonOverlay -> "${overlay.points.size} vertices"
                        }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onDeleteOverlay(overlay.id) }, modifier = Modifier.size(36.dp)) {
                        Icon(FieldMindIcons.Delete, null, tint = MaterialTheme.colorScheme.error, size = 18.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TracksTab(
    modifier: Modifier,
    savedTracks: List<TrackRecording>,
    currentTrack: TrackRecording?,
    isRecording: Boolean,
    trackRecorder: TrackRecorder,
    context: Context
) {
    val scope = rememberCoroutineScope()
    val colors = FieldMindTheme.colors

    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(FieldMindIcons.Track, null, tint = colors.info, size = 20.dp)
                        Text("Track recordings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Text("Recorded GPS tracks appear here. Export them as GPX for use in other mapping tools.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${savedTracks.size} saved track${if (savedTracks.size == 1) "" else "s"} · %.0f m today".format(trackRecorder.todayDistanceMeters()), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (currentTrack != null && isRecording) {
            item {
                Card(shape = CuteCardDefaults.ShapeCompact, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error))
                            Text("Recording: ${currentTrack.name}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Text("${currentTrack.points.size} points · %.0f m distance".format(currentTrack.distanceMeters), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        itemsIndexed(savedTracks.sortedByDescending { it.startedAt }) { i, track ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CuteCardDefaults.ShapeCompact,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(track.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(track.startedAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("%.0f m".format(track.distanceMeters), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.info)
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${track.points.size} points · ${track.totalDurationMs / 1000}s", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = {
                                scope.launch {
                                    trackRecorder.exportToGpx(track.id)
                                }
                            }, modifier = Modifier.size(32.dp)) {
                                Icon(FieldMindIcons.Export, null, size = 16.dp)
                            }
                            IconButton(onClick = { trackRecorder.deleteTrack(track.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(FieldMindIcons.Delete, null, size = 16.dp)
                            }
                        }
                    }
                }
            }
        }
        if (savedTracks.isEmpty() && !isRecording) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No tracks recorded yet. Start a track recording from the Map tab to log your survey path.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun GeofencesTab(
    modifier: Modifier,
    geoFenceReminder: GeoFenceReminder,
    geofenceRegions: List<GeofenceRegion>
) {
    val colors = FieldMindTheme.colors

    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(FieldMindIcons.Notifications, null, tint = colors.info, size = 20.dp)
                        Text("Geo-fence reminders", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Text("Mark sites and get notified when you arrive. Points drawn on the map are automatically registered as geo-fence zones.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${geofenceRegions.size} active region${if (geofenceRegions.size == 1) "" else "s"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (geofenceRegions.isNotEmpty()) {
                        OutlinedButton(onClick = { geoFenceReminder.clearAllRegions() }, shape = CuteCardDefaults.ButtonShape, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Clear all") }
                    }
                }
            }
        }
        itemsIndexed(geofenceRegions) { i, region ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CuteCardDefaults.ShapeCompact,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier.size(40.dp).clip(MaterialTheme.shapes.medium)
                            .background(if (region.isActive) colors.observation.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(FieldMindIcons.Notifications, null, tint = if (region.isActive) colors.observation else MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(region.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text("%.5f, %.5f".format(region.latitude, region.longitude), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${region.radiusMeters.toInt()}m", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (region.triggerOnEntry) Text("Entry", style = MaterialTheme.typography.labelSmall, color = colors.positive)
                            if (region.triggerOnExit) Text("Exit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    if (region.note.isNotBlank()) {
                        Text(region.note, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                    IconButton(onClick = { geoFenceReminder.removeRegion(region.id) }, modifier = Modifier.size(36.dp)) {
                        Icon(FieldMindIcons.Delete, null, tint = MaterialTheme.colorScheme.error, size = 18.dp)
                    }
                }
            }
        }
        if (geofenceRegions.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No geo-fence regions. Draw points on the map and they'll be registered as geo-fences automatically.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ── Track persistence helpers ──

private fun saveTracksToPrefs(prefs: android.content.SharedPreferences, tracks: List<TrackRecording>) {
    val json = org.json.JSONArray()
    tracks.forEach { t ->
        val j = org.json.JSONObject().apply {
            put("id", t.id)
            put("name", t.name)
            put("startedAt", t.startedAt)
            put("endedAt", t.endedAt ?: JSONObject.NULL)
            put("distanceMeters", t.distanceMeters)
            put("totalDurationMs", t.totalDurationMs)
            val pts = org.json.JSONArray()
            t.points.forEach { p ->
                pts.put(org.json.JSONObject().apply {
                    put("lat", p.latitude)
                    put("lon", p.longitude)
                    put("alt", p.altitude ?: JSONObject.NULL)
                    put("acc", p.accuracy ?: JSONObject.NULL)
                    put("ts", p.timestamp)
                })
            }
            put("points", pts)
        }
        json.put(j)
    }
    prefs.edit().putString("savedTracks", json.toString()).apply()
}

private fun loadTracksFromPrefs(prefs: android.content.SharedPreferences): List<TrackRecording> {
    val raw = prefs.getString("savedTracks", "") ?: ""
    if (raw.isBlank()) return emptyList()
    return try {
        val arr = org.json.JSONArray(raw)
        (0 until arr.length()).map { i ->
            val j = arr.getJSONObject(i)
            val pts = org.json.JSONArray(j.optString("points", "[]"))
            TrackRecording(
                id = j.optString("id", java.util.UUID.randomUUID().toString()),
                name = j.optString("name", "Track"),
                startedAt = j.optLong("startedAt", System.currentTimeMillis()),
                endedAt = j.optLong("endedAt", -1L).let { if (it < 0) null else it },
                distanceMeters = j.optDouble("distanceMeters", 0.0),
                totalDurationMs = j.optLong("totalDurationMs", 0L),
                points = (0 until pts.length()).map { k ->
                    val p = pts.getJSONObject(k)
                    TrackPoint(
                        latitude = p.optDouble("lat", 0.0),
                        longitude = p.optDouble("lon", 0.0),
                        altitude = p.optDouble("alt", Double.NaN).let { if (it.isNaN()) null else it },
                        accuracy = p.optDouble("acc", Double.NaN).let { if (it.isNaN()) null else it.toFloat() },
                        timestamp = p.optLong("ts", System.currentTimeMillis())
                    )
                }
            )
        }
    } catch (_: Exception) { emptyList() }
}

// ══════════════════════════════════════════════════════════════════════
//  Edit Overlay Dialog — rename label & pick color
// ══════════════════════════════════════════════════════════════════════

/**
 * Dialog for editing a map overlay's label and color.
 * Fills in the existing values and saves on confirm.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun EditOverlayDialog(
    overlay: MapOverlay,
    onSave: (MapOverlay) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var label by remember { mutableStateOf(overlay.label) }
    val palette = listOf(
        0xFFF44336 to "Red",
        0xFFFF5722 to "Deep orange",
        0xFFFF9800 to "Amber",
        0xFFFFC107 to "Yellow",
        0xFF4CAF50 to "Green",
        0xFF009688 to "Teal",
        0xFF2196F3 to "Blue",
        0xFF3F51B5 to "Indigo",
        0xFF9C27B0 to "Purple",
        0xFF607D8B to "Blue grey"
    )
    var selectedColor by remember { mutableStateOf(overlay.color) }

    SwipeableAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit overlay", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    placeholder = { Text(when (overlay) {
                        is MapOverlay.PointOverlay -> "Site point"
                        is MapOverlay.LineOverlay -> "Transect"
                        is MapOverlay.PolygonOverlay -> "Survey boundary"
                    }) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = CuteCardDefaults.ButtonShape
                )

                Text("Color", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    palette.forEach { (colorLong, name) ->
                        val colorInt = colorLong.toInt()
                        val isSelected = selectedColor == colorLong
                        Surface(
                            onClick = { selectedColor = colorLong },
                            shape = CuteCardDefaults.ChipShape,
                            color = Color(colorInt),
                            modifier = Modifier
                                .size(42.dp)
                                .then(
                                    if (isSelected) Modifier.border(
                                        2.5.dp,
                                        MaterialTheme.colorScheme.onSurface,
                                        CuteCardDefaults.ChipShape
                                    ) else Modifier
                                )
                        ) {
                            if (isSelected) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        FieldMindIcons.Check,
                                        null,
                                        tint = Color.White,
                                        size = 20.dp
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    when (overlay) {
                        is MapOverlay.PointOverlay -> "Point · %.5f, %.5f".format(overlay.latitude, overlay.longitude)
                        is MapOverlay.LineOverlay -> "Line · ${overlay.points.size} points"
                        is MapOverlay.PolygonOverlay -> "Polygon · ${overlay.points.size} vertices"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = when (overlay) {
                        is MapOverlay.PointOverlay -> overlay.copy(label = label, color = selectedColor)
                        is MapOverlay.LineOverlay -> overlay.copy(label = label, color = selectedColor)
                        is MapOverlay.PolygonOverlay -> overlay.copy(label = label, color = selectedColor)
                    }
                    onSave(updated)
                },
                shape = CuteCardDefaults.ButtonShape
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = CuteCardDefaults.Shape
    )
}
