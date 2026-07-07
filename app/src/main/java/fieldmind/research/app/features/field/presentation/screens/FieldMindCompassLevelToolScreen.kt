package fieldmind.research.app.features.field.presentation.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fieldmind.research.app.features.field.presentation.components.*
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.features.field.presentation.viewmodel.FieldMindViewModel
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

// ══════════════════════════════════════════════════════════════════════
//  Compass Tool — Real-time compass heading using magnetometer + accelerometer
//  Features:
//    - Rotating needle points toward magnetic north
//    - Fixed compass rose with cardinal letters (N, E, S, W)
//    - Low-pass filtered sensor values for smooth readings
//    - Tilt-compensated heading via getRotationMatrix + getOrientation
//    - Pitch/roll displayed for orientation awareness
// ══════════════════════════════════════════════════════════════════════

@Composable
fun CompassToolScreen(
    viewModel: FieldMindViewModel? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = FieldMindTheme.colors
    val snackbar = remember { SnackbarHostState() }

    // ── Sensor state ──
    var azimuth by remember { mutableFloatStateOf(0f) }        // degrees from north
    var magneticField by remember { mutableFloatStateOf(0f) }  // μT
    var accuracy by remember { mutableStateOf("Unknown") }
    var compassPitch by remember { mutableFloatStateOf(0f) }   // tilt from orientation (deg)
    var compassRoll by remember { mutableFloatStateOf(0f) }    // roll from orientation (deg)

    // ── Calibration state ──
    var needsCalibration by remember { mutableStateOf(false) }
    var calibrationProgress by remember { mutableFloatStateOf(0f) }

    // ── Sensor listener ──
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val magnetometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) }

    DisposableEffect(Unit) {
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        var calibrationSamples = 0
        var firstGravity = true
        var firstGeomagnetic = true
        val alpha = 0.18f  // Low-pass filter coefficient (lower = smoother but slower)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        if (firstGravity) {
                            gravity[0] = event.values[0]
                            gravity[1] = event.values[1]
                            gravity[2] = event.values[2]
                            firstGravity = false
                        } else {
                            gravity[0] = gravity[0] * (1 - alpha) + event.values[0] * alpha
                            gravity[1] = gravity[1] * (1 - alpha) + event.values[1] * alpha
                            gravity[2] = gravity[2] * (1 - alpha) + event.values[2] * alpha
                        }
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        if (firstGeomagnetic) {
                            geomagnetic[0] = event.values[0]
                            geomagnetic[1] = event.values[1]
                            geomagnetic[2] = event.values[2]
                            firstGeomagnetic = false
                        } else {
                            geomagnetic[0] = geomagnetic[0] * (1 - alpha) + event.values[0] * alpha
                            geomagnetic[1] = geomagnetic[1] * (1 - alpha) + event.values[1] * alpha
                            geomagnetic[2] = geomagnetic[2] * (1 - alpha) + event.values[2] * alpha
                        }
                        magneticField = sqrt(
                            geomagnetic[0] * geomagnetic[0] +
                            geomagnetic[1] * geomagnetic[1] +
                            geomagnetic[2] * geomagnetic[2]
                        )
                    }
                }

                if (!firstGravity && !firstGeomagnetic &&
                    SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    // orientation[0] = azimuth
                    val azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    azimuth = (azimuthDeg + 360) % 360
                    // orientation[1] = pitch, orientation[2] = roll
                    compassPitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
                    compassRoll = Math.toDegrees(orientation[2].toDouble()).toFloat()

                    // Calibration quality estimate: accumulating samples
                    calibrationSamples++
                    if (calibrationSamples <= 60) {
                        calibrationProgress = calibrationSamples / 60f
                    } else {
                        needsCalibration = false
                        calibrationProgress = 1f
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, acc: Int) {
                accuracy = when (acc) {
                    SensorManager.SENSOR_STATUS_UNRELIABLE -> "Unreliable"
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low"
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium"
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "High"
                    else -> "Unknown"
                }
                needsCalibration = acc < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
            }
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // ── Smooth rotation animation ──
    val smoothAzimuth by animateFloatAsState(
        targetValue = azimuth,
        animationSpec = tween(durationMillis = 250),
        label = "azimuth"
    )

    // ── Cardinal direction ──
    val cardinal = remember(azimuth) {
        val dirs = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val index = ((azimuth + 11.25f) / 22.5f).roundToInt() % 16
        dirs[index]
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = {}
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Header ──
                StandardScreenHeader(
                    title = "Compass",
                    subtitle = "Real-time magnetic heading using device sensors.",
                    icon = MaterialSymbolIcon("explore"),
                    heroColor = colors.info,
                    trailing = { BackButton(onClick = onBack) }
                )

                // ── Compass rose ──
                Card(
                    shape = RoundedCornerShape(40.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Compass face
                        Box(
                            modifier = Modifier.size(280.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val compassSurfaceHighest = MaterialTheme.colorScheme.surfaceContainerHighest
                            val compassOutlineVariant = MaterialTheme.colorScheme.outlineVariant
                            val compassOnSurface = MaterialTheme.colorScheme.onSurface
                            val compassOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
                            Canvas(Modifier.fillMaxSize()) {
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val radius = minOf(cx, cy) * 0.85f

                                // ── Outer ring ──
                                drawCircle(
                                    color = compassSurfaceHighest,
                                    radius = radius + 12f,
                                    center = Offset(cx, cy)
                                )
                                drawCircle(
                                    color = compassOutlineVariant.copy(alpha = 0.3f),
                                    radius = radius + 4f,
                                    center = Offset(cx, cy),
                                    style = Stroke(width = 2f)
                                )

                                // Compass face
                                drawCircle(
                                    color = Color.Transparent,
                                    radius = radius,
                                    center = Offset(cx, cy)
                                )

                                // ── Fixed degree ticks (do NOT rotate) ──
                                for (deg in 0 until 360 step 2) {
                                    val rad = Math.toRadians(deg.toDouble())
                                    val isMajor = deg % 90 == 0
                                    val isMinor = deg % 10 == 0
                                    val tickLen = when {
                                        isMajor -> radius * 0.25f
                                        isMinor -> radius * 0.12f
                                        else -> radius * 0.06f
                                    }
                                    val tickWidth = when {
                                        isMajor -> 3f
                                        isMinor -> 2f
                                        else -> 1f
                                    }
                                    val tickColor = when {
                                        isMajor -> compassOnSurface
                                        isMinor -> compassOnSurfaceVariant.copy(alpha = 0.5f)
                                        else -> compassOutlineVariant
                                    }
                                    val innerR = radius - tickLen
                                    // Canvas convention: 0° = up (-y), angle increases clockwise
                                    val tx = cx + (innerR * sin(rad)).toFloat()
                                    val ty = cy - (innerR * cos(rad)).toFloat()
                                    val ex = cx + (radius * sin(rad)).toFloat()
                                    val ey = cy - (radius * cos(rad)).toFloat()
                                    drawLine(
                                        color = tickColor,
                                        start = Offset(tx, ty),
                                        end = Offset(ex, ey),
                                        strokeWidth = tickWidth,
                                        cap = StrokeCap.Round
                                    )
                                }

                                // ── Fixed cardinal letters (N, E, S, W) ──
                                val cardinals = listOf(
                                    "N" to Color(0xFFE53935), // Red for North
                                    "E" to compassOnSurface,
                                    "S" to compassOnSurface,
                                    "W" to compassOnSurface
                                )
                                val paint = android.graphics.Paint().apply {
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isAntiAlias = true
                                }
                                cardinals.forEachIndexed { i, (label, color) ->
                                    val angle = i * 90.0
                                    val rad = Math.toRadians(angle)
                                    val labelR = radius * 0.72f
                                    val x = cx + (labelR * sin(rad)).toFloat()
                                    val y = cy - (labelR * cos(rad)).toFloat()

                                    paint.color = color.toArgb()
                                    paint.textSize = when (label) {
                                        "N" -> 42f
                                        else -> 32f
                                    }
                                    paint.setFakeBoldText(label == "N")
                                    drawContext.canvas.nativeCanvas.drawText(
                                        label, x, y + paint.textSize / 3f, paint
                                    )
                                }

                                // ── Rotating compass needle ──
                                // The needle rotates so its red tip always points toward magnetic north.
                                // When heading=0° (facing N), the needle points up toward the 'N' marker.
                                // When heading=90° (facing E), the needle rotates -90° to point left (west =
                                // magnetic north relative to the user's heading).
                                val needleLen = radius * 0.70f
                                val southLen = radius * 0.50f
                                val needleWidth = radius * 0.07f

                                // Rotate the entire needle so the red tip aims at magnetic north.
                                // Canvas rotate() rotates clockwise for positive degrees; we want the needle
                                // to rotate counterclockwise when the heading increases, so we use -smoothAzimuth.
                                rotate(degrees = -smoothAzimuth, pivot = Offset(cx, cy)) {
                                    // North half (red) — triangle pointing up
                                    val northPath = Path().apply {
                                        moveTo(cx, cy - needleLen)
                                        lineTo(cx - needleWidth, cy)
                                        lineTo(cx + needleWidth, cy)
                                        close()
                                    }
                                    drawPath(northPath, color = Color(0xFFE53935))

                                    // South half (gray) — triangle pointing down
                                    val southPath = Path().apply {
                                        moveTo(cx, cy + southLen)
                                        lineTo(cx - needleWidth, cy)
                                        lineTo(cx + needleWidth, cy)
                                        close()
                                    }
                                    drawPath(southPath, color = Color(0xFFBDBDBD))

                                    // Needle outline for definition
                                    val outlinePath = Path().apply {
                                        moveTo(cx, cy - needleLen)
                                        lineTo(cx - needleWidth, cy)
                                        lineTo(cx, cy + southLen)
                                        lineTo(cx + needleWidth, cy)
                                        close()
                                    }
                                    drawPath(outlinePath,
                                        color = Color(0xFF888888).copy(alpha = 0.3f),
                                        style = Stroke(width = 1f))

                                    // Center pivot
                                    drawCircle(color = Color.White, radius = 6f, center = Offset(cx, cy))
                                    drawCircle(color = Color(0xFFE53935), radius = 3.5f, center = Offset(cx, cy))
                                }
                            }
                        }

                        // ── Heading display ──
                        Text(
                            "%.1f°".format(azimuth),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 48.sp
                            ),
                            color = colors.info
                        )
                        Text(
                            "Heading: $cardinal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // ── Sensor data card ──
                Card(
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Sensor data", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            SensorDataItem("Magnetic field", "%.1f μT".format(magneticField), MaterialSymbolIcon("magnet"))
                            SensorDataItem("Accuracy", accuracy, when (accuracy) {
                                "High" -> MaterialSymbolIcon("check_circle", filled = true)
                                "Medium" -> MaterialSymbolIcon("radio_button_partial")
                                else -> MaterialSymbolIcon("warning")
                            })
                        }

                        // Tilt info (from rotation matrix — works in any orientation)
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            SensorDataItem(
                                "Pitch", "%.1f°".format(compassPitch),
                                MaterialSymbolIcon("tilt_shift")
                            )
                            SensorDataItem(
                                "Roll", "%.1f°".format(compassRoll),
                                MaterialSymbolIcon("3d_rotation")
                            )
                        }

                        // Calibration indicator
                        if (needsCalibration) {
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(MaterialSymbolIcon("info"), null, tint = MaterialTheme.colorScheme.error, size = 18.dp)
                                    Text(
                                        "Move your device in a figure-8 pattern to calibrate",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        // Calibration progress
                        if (calibrationProgress < 1f) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "Calibrating... ${(calibrationProgress * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                LinearProgressIndicator(
                                    progress = { calibrationProgress },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                    color = colors.info,
                                    trackColor = colors.info.copy(alpha = 0.12f)
                                )
                            }
                        }
                    }
                }

                // ── Usage tips ──
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.info.copy(alpha = 0.06f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Tips", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = colors.info)
                        Text("• The red needle end always points toward magnetic north", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Pitch/Roll show the device's tilt in the current orientation", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Keep away from metal objects and magnets", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Wave in a figure-8 pattern to re-calibrate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        FieldMindSnackbarOverlay(hostState = snackbar, modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp, start = 16.dp, end = 16.dp))
    }
}

@Composable
private fun SensorDataItem(label: String, value: String, icon: MaterialSymbolIcon) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = FieldMindTheme.colors.info, size = 18.dp)
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Level Tool — Real-time spirit level using accelerometer + magnetometer
//  Features:
//    - Proper pitch/roll calculation using getRotationMatrix for all orientations
//    - Bubble level visualization with dynamic bubble offset
//    - "Set reference" allows zeroing at any angle for checking different positions
//    - Works flat on table, against walls, at any angle
// ══════════════════════════════════════════════════════════════════════

@Composable
fun LevelToolScreen(
    viewModel: FieldMindViewModel? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = FieldMindTheme.colors
    val snackbar = remember { SnackbarHostState() }

    // ── Sensor state ──
    var pitch by remember { mutableFloatStateOf(0f) }   // tilt forward/backward (deg)
    var roll by remember { mutableFloatStateOf(0f) }    // tilt left/right (deg)

    // ── Reference state (for "Set reference" feature) ──
    var isReferenced by remember { mutableStateOf(false) }
    var referencePitch by remember { mutableFloatStateOf(0f) }
    var referenceRoll by remember { mutableFloatStateOf(0f) }

    // ── Sensor listener (uses getRotationMatrix for all-orientation correctness) ──
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val magnetometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) }

    DisposableEffect(Unit) {
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        var firstGravity = true
        var firstGeomagnetic = true
        val alpha = 0.12f  // Heavier low-pass filter for stable level readings

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        if (firstGravity) {
                            gravity[0] = event.values[0]
                            gravity[1] = event.values[1]
                            gravity[2] = event.values[2]
                            firstGravity = false
                        } else {
                            gravity[0] = gravity[0] * (1 - alpha) + event.values[0] * alpha
                            gravity[1] = gravity[1] * (1 - alpha) + event.values[1] * alpha
                            gravity[2] = gravity[2] * (1 - alpha) + event.values[2] * alpha
                        }
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        if (firstGeomagnetic) {
                            geomagnetic[0] = event.values[0]
                            geomagnetic[1] = event.values[1]
                            geomagnetic[2] = event.values[2]
                            firstGeomagnetic = false
                        } else {
                            geomagnetic[0] = geomagnetic[0] * (1 - alpha) + event.values[0] * alpha
                            geomagnetic[1] = geomagnetic[1] * (1 - alpha) + event.values[1] * alpha
                            geomagnetic[2] = geomagnetic[2] * (1 - alpha) + event.values[2] * alpha
                        }
                    }
                }

                if (!firstGravity && !firstGeomagnetic &&
                    SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    // orientation[1] = pitch (rotation around X-axis), range -π to π
                    // orientation[2] = roll (rotation around Y-axis), range -π/2 to π/2
                    pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
                    roll = Math.toDegrees(orientation[2].toDouble()).toFloat()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, acc: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // ── Smooth animations for raw values (shown when no reference) ──
    val smoothPitch by animateFloatAsState(
        targetValue = pitch,
        animationSpec = tween(durationMillis = 100),
        label = "pitch"
    )
    val smoothRoll by animateFloatAsState(
        targetValue = roll,
        animationSpec = tween(durationMillis = 100),
        label = "roll"
    )

    // ── Effective tilt (relative to reference if set) — animated ──
    val effectivePitch: Float by remember(pitch, referencePitch, isReferenced) {
        derivedStateOf {
            if (isReferenced) smoothPitch - referencePitch else smoothPitch
        }
    }
    val effectiveRoll: Float by remember(roll, referenceRoll, isReferenced) {
        derivedStateOf {
            if (isReferenced) smoothRoll - referenceRoll else smoothRoll
        }
    }
    val isLevel: Boolean by remember(effectivePitch, effectiveRoll) {
        derivedStateOf { abs(effectivePitch) < 2f && abs(effectiveRoll) < 2f }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header ──
            StandardScreenHeader(
                title = "Level",
                subtitle = "Real-time spirit level using accelerometer.",
                icon = MaterialSymbolIcon("straighten"),
                heroColor = colors.data,
                trailing = { BackButton(onClick = onBack) }
            )

            // ── Level display ──
            Card(
                shape = RoundedCornerShape(40.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLevel) colors.positive.copy(alpha = 0.08f)
                    else MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Bubble level canvas
                    Box(
                        modifier = Modifier.size(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val levelSurfaceHighest = MaterialTheme.colorScheme.surfaceContainerHighest
                        val levelOutlineVariant = MaterialTheme.colorScheme.outlineVariant
                        val levelOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
                        Canvas(Modifier.fillMaxSize()) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val outerRadius = minOf(cx, cy) * 0.95f
                            val bubbleRadius = outerRadius * 0.12f

                            // Outer circle
                            drawCircle(
                                color = levelSurfaceHighest,
                                radius = outerRadius,
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = levelOutlineVariant.copy(alpha = 0.3f),
                                radius = outerRadius,
                                center = Offset(cx, cy),
                                style = Stroke(width = 2f)
                            )

                            // Inner ring
                            drawCircle(
                                color = levelOutlineVariant.copy(alpha = 0.15f),
                                radius = outerRadius * 0.6f,
                                center = Offset(cx, cy),
                                style = Stroke(width = 1f)
                            )

                            // Crosshair lines
                            val crossColor = levelOnSurfaceVariant.copy(alpha = 0.2f)
                            drawLine(crossColor, Offset(cx, cy - outerRadius * 0.85f), Offset(cx, cy + outerRadius * 0.85f), 1f)
                            drawLine(crossColor, Offset(cx - outerRadius * 0.85f, cy), Offset(cx + outerRadius * 0.85f, cy), 1f)

                            // Bubble — offset from center based on effective pitch and roll
                            // Map ±15° to ±60% of outerRadius
                            val maxTilt = 15f
                            val sensitivity = 0.6f
                            val bubbleX = cx + (effectiveRoll.coerceIn(-maxTilt, maxTilt) / maxTilt * outerRadius * sensitivity)
                            val bubbleY = cy + (effectivePitch.coerceIn(-maxTilt, maxTilt) / maxTilt * outerRadius * sensitivity)

                            // Bubble glow
                            val bubbleColor = if (isLevel) colors.positive else colors.info
                            drawCircle(
                                color = bubbleColor.copy(alpha = 0.08f),
                                radius = bubbleRadius * 2.5f,
                                center = Offset(bubbleX, bubbleY)
                            )

                            // Bubble
                            drawCircle(
                                color = bubbleColor,
                                radius = bubbleRadius,
                                center = Offset(bubbleX, bubbleY)
                            )

                            // Bubble highlight
                            drawCircle(
                                color = Color.White.copy(alpha = 0.3f),
                                radius = bubbleRadius * 0.4f,
                                center = Offset(bubbleX - bubbleRadius * 0.2f, bubbleY - bubbleRadius * 0.2f)
                            )

                            // Center dot
                            drawCircle(
                                color = levelOnSurfaceVariant.copy(alpha = 0.3f),
                                radius = 3f,
                                center = Offset(cx, cy)
                            )
                        }
                    }

                    // ── Level indicator ──
                    if (isLevel) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = colors.positive.copy(alpha = 0.15f)
                        ) {
                            Row(
                                Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(MaterialSymbolIcon("check_circle", filled = true), null,
                                    tint = colors.positive, size = 24.dp)
                                Text("Level!", style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold, color = colors.positive)
                            }
                        }
                    }
                }
            }

            // ── Tilt values + reference controls ──
            Card(
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Tilt angles", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        TiltGauge("Pitch", effectivePitch, "Forward/backward", colors.info)
                        TiltGauge("Roll", effectiveRoll, "Left/right", colors.data)
                    }

                    // Reference controls
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isReferenced) {
                            // Show absolute reference angles
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.info.copy(alpha = 0.08f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(Modifier.padding(10.dp)) {
                                    Text(
                                        "Reference set",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.info
                                    )
                                    Text(
                                        "Pitch: %.1f°  Roll: %.1f°".format(referencePitch, referenceRoll),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    isReferenced = false
                                    referencePitch = 0f
                                    referenceRoll = 0f
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    MaterialSymbolIcon("clear"),
                                    null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Clear", style = MaterialTheme.typography.labelMedium)
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    referencePitch = pitch
                                    referenceRoll = roll
                                    isReferenced = true
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colors.info
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    MaterialSymbolIcon("my_location"),
                                    null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Set reference", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    Text(
                        if (isReferenced) "Deviations shown relative to set reference. Use to check any surface angle."
                        else "Hold device flat to check level surfaces. Use 'Set reference' to check any position.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        FieldMindSnackbarOverlay(hostState = snackbar, modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp, start = 16.dp, end = 16.dp))
    }
}

@Composable
private fun TiltGauge(label: String, degrees: Float, description: String, color: Color, modifier: Modifier = Modifier) {
    val absDeg = abs(degrees)
    val isLevel = absDeg < 2f

    Column(
        modifier = modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            "%.1f°".format(degrees),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp
            ),
            color = if (isLevel) FieldMindTheme.colors.positive else color
        )
        Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
