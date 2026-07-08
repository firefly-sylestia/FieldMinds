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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

// ══════════════════════════════════════════════════════════════════════
//  Compass Tool — Real-time compass heading using magnetometer + accelerometer
//  Features:
//    - Rotating compass rose with cardinal letters (N, E, S, W)
//    - Fixed heading indicator shows device direction on the dial
//    - remapCoordinateSystem for tilt-compensated heading in any orientation
//    - Low-pass filtered sensor values for smooth readings
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
    var needsCalibration by remember { mutableStateOf(true) }

    // ── Sensor listener ──
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
        val alpha = 0.30f  // Low-pass filter coefficient (higher = more responsive)

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
                    // Pass rotationMatrix directly to getOrientation — no remapping needed
                    // (remapCoordinateSystem with AXIS_X/AXIS_Z forces AR/vertical orientation
                    //  and produces wrong headings when the device is held flat)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    // orientation[0] = azimuth (tilt-compensated)
                    val azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    azimuth = (azimuthDeg + 360) % 360
                    // orientation[1] = pitch, orientation[2] = roll
                    compassPitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
                    compassRoll = Math.toDegrees(orientation[2].toDouble()).toFloat()
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

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // ── Short animation for jitter dampening on noisy magnetometers.
    // 30ms is fast enough to feel instant but smooths out micro-jitter
    // without causing the 359°→0° wrap-around visual glitch.
    val smoothAzimuth by animateFloatAsState(
        targetValue = azimuth,
        animationSpec = tween(durationMillis = 30),
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

                                // Compass face background
                                drawCircle(
                                    color = Color.Transparent,
                                    radius = radius,
                                    center = Offset(cx, cy)
                                )

                                // ── Rotating compass rose (ticks + cardinal labels) ──
                                // The entire face rotates so the N label always points to magnetic north.
                                // Rotate by -smoothAzimuth: positive heading means device faces clockwise
                                // from north, so the face rotates counterclockwise to compensate.
                                withTransform({
                                    rotate(degrees = -smoothAzimuth, pivot = Offset(cx, cy))
                                }) {
                                    // Degree ticks
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

                                    // Cardinal letters (N, E, S, W)
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
                                        val labelR = radius * 0.78f
                                        val x = cx + (labelR * sin(rad)).toFloat()
                                        val y = cy - (labelR * cos(rad)).toFloat()

                                        paint.color = color.toArgb()
                                        paint.textSize = when (label) {
                                            "N" -> 42f
                                            else -> 32f
                                        }
                                        paint.isFakeBoldText = label == "N"
                                        drawContext.canvas.nativeCanvas.drawText(
                                            label, x, y + paint.textSize / 3f, paint
                                        )
                                    }
                                }

                                // ── Fixed heading indicator (red triangle at top of compass) ──
                                // Points to the top of the screen — shows which way the device is facing
                                val indicatorLen = radius * 0.22f
                                val indicatorWidth = radius * 0.07f
                                val headingPath = Path().apply {
                                    moveTo(cx, cy - radius + 4f)
                                    lineTo(cx - indicatorWidth, cy - radius + 4f + indicatorLen)
                                    lineTo(cx + indicatorWidth, cy - radius + 4f + indicatorLen)
                                    close()
                                }
                                drawPath(headingPath, color = Color(0xFFE53935))

                                // South indicator (small gray notch at bottom)
                                val southMarkPath = Path().apply {
                                    moveTo(cx - indicatorWidth * 0.6f, cy + radius - 4f - indicatorLen * 0.5f)
                                    lineTo(cx, cy + radius - 4f)
                                    lineTo(cx + indicatorWidth * 0.6f, cy + radius - 4f - indicatorLen * 0.5f)
                                    close()
                                }
                                drawPath(southMarkPath, color = Color(0xFF9E9E9E))

                                // Center pivot
                                drawCircle(color = Color.White, radius = 5f, center = Offset(cx, cy))
                                drawCircle(color = Color(0xFFE53935), radius = 2.5f, center = Offset(cx, cy))
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
                        Text("• The red heading indicator (top) shows the direction your device is pointing", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
//  Level Tool — Real-time spirit level using accelerometer gravity vector
//  Features:
//    - Auto-detects orientation: flat (surface level) vs vertical (plumb)
//    - Circular bubble level when phone is flat on a surface
//    - Vertical tube level when phone is held against a wall
//    - Raw gravity-based tilt computation (no magnetometer dependency)
//    - "Set reference" allows zeroing at any angle for checking different positions
// ══════════════════════════════════════════════════════════════════════

private enum class OrientationMode { FLAT, VERTICAL_PORTRAIT, VERTICAL_LANDSCAPE }

@Composable
fun LevelToolScreen(
    viewModel: FieldMindViewModel? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = FieldMindTheme.colors
    val snackbar = remember { SnackbarHostState() }

    // ── Raw gravity state ──
    var gravityX by remember { mutableFloatStateOf(0f) }
    var gravityY by remember { mutableFloatStateOf(-SensorManager.GRAVITY_EARTH) }
    var gravityZ by remember { mutableFloatStateOf(0f) }

    // ── Reference state ──
    var isReferenced by remember { mutableStateOf(false) }
    var referencePitch by remember { mutableFloatStateOf(0f) }
    var referenceRoll by remember { mutableFloatStateOf(0f) }

    // ── Sensor listener (accelerometer only — gravity vector is all we need) ──
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    DisposableEffect(Unit) {
        val gravity = FloatArray(3)
        var firstGravity = true
        val alpha = 0.12f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
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
                    gravityX = gravity[0]
                    gravityY = gravity[1]
                    gravityZ = gravity[2]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, acc: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // ── Orientation detection (threshold = cos(45°) × 9.81 ≈ 6.94) ──
    val orientationMode: OrientationMode by remember(gravityX, gravityY, gravityZ) {
        derivedStateOf {
            when {
                abs(gravityZ) > 6.94f -> OrientationMode.FLAT
                abs(gravityY) > abs(gravityX) -> OrientationMode.VERTICAL_PORTRAIT
                else -> OrientationMode.VERTICAL_LANDSCAPE
            }
        }
    }

    // ── Tilt angles computed from raw gravity (sign-preserving via atan2) ──
    // FLAT: pitch = tilt forward/back, roll = tilt left/right
    // VERTICAL_PORTRAIT: pitch = in/out from wall, roll = left/right lean
    // VERTICAL_LANDSCAPE: pitch = in/out from wall, roll = left/right lean
    val rawPitch: Float by remember(gravityX, gravityY, gravityZ, orientationMode) {
        derivedStateOf {
            when (orientationMode) {
                OrientationMode.FLAT ->
                    Math.toDegrees(atan2(gravityX.toDouble(), abs(gravityZ).toDouble())).toFloat()
                OrientationMode.VERTICAL_PORTRAIT ->
                    Math.toDegrees(atan2(gravityZ.toDouble(), gravityY.toDouble())).toFloat()
                OrientationMode.VERTICAL_LANDSCAPE ->
                    Math.toDegrees(atan2(gravityZ.toDouble(), gravityX.toDouble())).toFloat()
            }
        }
    }
    val rawRoll: Float by remember(gravityX, gravityY, gravityZ, orientationMode) {
        derivedStateOf {
            when (orientationMode) {
                OrientationMode.FLAT ->
                    Math.toDegrees(atan2(gravityY.toDouble(), abs(gravityZ).toDouble())).toFloat()
                OrientationMode.VERTICAL_PORTRAIT ->
                    Math.toDegrees(atan2(gravityX.toDouble(), gravityY.toDouble())).toFloat()
                OrientationMode.VERTICAL_LANDSCAPE ->
                    Math.toDegrees(atan2(gravityY.toDouble(), gravityX.toDouble())).toFloat()
            }
        }
    }

    // ── Smooth animation ──
    val smoothPitch by animateFloatAsState(rawPitch, animationSpec = tween(100), label = "pitch")
    val smoothRoll by animateFloatAsState(rawRoll, animationSpec = tween(100), label = "roll")

    // ── Effective tilt (relative to reference if set) ──
    val effectivePitch by remember(smoothPitch, referencePitch, isReferenced) {
        derivedStateOf { if (isReferenced) smoothPitch - referencePitch else smoothPitch }
    }
    val effectiveRoll by remember(smoothRoll, referenceRoll, isReferenced) {
        derivedStateOf { if (isReferenced) smoothRoll - referenceRoll else smoothRoll }
    }
    val isLevel by remember(effectivePitch, effectiveRoll) {
        derivedStateOf { abs(effectivePitch) < 2f && abs(effectiveRoll) < 2f }
    }

    // ── Mode label for header ──
    val modeLabel by remember(orientationMode) {
        derivedStateOf {
            when (orientationMode) {
                OrientationMode.FLAT -> "Surface level — place device flat"
                OrientationMode.VERTICAL_PORTRAIT -> "Plumb — hold against wall"
                OrientationMode.VERTICAL_LANDSCAPE -> "Plumb — hold against wall"
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StandardScreenHeader(
                title = "Level",
                subtitle = modeLabel,
                icon = MaterialSymbolIcon("straighten"),
                heroColor = colors.data,
                trailing = { BackButton(onClick = onBack) }
            )

            // ── Level display (mode-aware) ──
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
                    // ── Mode-specific display ──
                    when (orientationMode) {
                        OrientationMode.FLAT -> CircularBubbleLevel(
                            effectivePitch, effectiveRoll, isLevel, colors
                        )
                        OrientationMode.VERTICAL_PORTRAIT,
                        OrientationMode.VERTICAL_LANDSCAPE -> VerticalTubeLevel(
                            effectiveRoll, isLevel, colors
                        )
                    }

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
                        TiltGauge("Pitch", effectivePitch, "Forward/backward", colors.info, Modifier.weight(1f))
                        TiltGauge("Roll", effectiveRoll, "Left/right", colors.data, Modifier.weight(1f))
                    }

                    // Reference controls
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isReferenced) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.info.copy(alpha = 0.08f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(Modifier.padding(10.dp)) {
                                    Text("Reference set", style = MaterialTheme.typography.labelSmall, color = colors.info)
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
                                Icon(MaterialSymbolIcon("clear"), null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Clear", style = MaterialTheme.typography.labelMedium)
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    referencePitch = smoothPitch
                                    referenceRoll = smoothRoll
                                    isReferenced = true
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.info),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(MaterialSymbolIcon("my_location"), null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Set reference", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    Text(
                        if (isReferenced) "Deviations shown relative to set reference."
                        else "Place device on a surface or against a wall. Use 'Set reference' to zero at any angle.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        FieldMindSnackbarOverlay(hostState = snackbar, modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp, start = 16.dp, end = 16.dp))
    }
}

// ── Circular bubble level for flat (horizontal) mode ──
@Composable
private fun CircularBubbleLevel(
    pitch: Float, roll: Float, isLevel: Boolean, colors: fieldmind.research.app.features.field.presentation.theme.FieldMindColors
) {
    Box(modifier = Modifier.size(260.dp), contentAlignment = Alignment.Center) {
        val levelSurfaceHighest = MaterialTheme.colorScheme.surfaceContainerHighest
        val levelOutlineVariant = MaterialTheme.colorScheme.outlineVariant
        val levelOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val outerRadius = minOf(cx, cy) * 0.95f
            val bubbleRadius = outerRadius * 0.12f

            drawCircle(color = levelSurfaceHighest, radius = outerRadius, center = Offset(cx, cy))
            drawCircle(color = levelOutlineVariant.copy(alpha = 0.3f), radius = outerRadius, center = Offset(cx, cy), style = Stroke(width = 2f))
            drawCircle(color = levelOutlineVariant.copy(alpha = 0.15f), radius = outerRadius * 0.6f, center = Offset(cx, cy), style = Stroke(width = 1f))

            val crossColor = levelOnSurfaceVariant.copy(alpha = 0.2f)
            drawLine(crossColor, Offset(cx, cy - outerRadius * 0.85f), Offset(cx, cy + outerRadius * 0.85f), 1f)
            drawLine(crossColor, Offset(cx - outerRadius * 0.85f, cy), Offset(cx + outerRadius * 0.85f, cy), 1f)

            val maxTilt = 15f
            val sensitivity = 0.6f
            val bubbleX = cx + (roll.coerceIn(-maxTilt, maxTilt) / maxTilt * outerRadius * sensitivity)
            val bubbleY = cy + (pitch.coerceIn(-maxTilt, maxTilt) / maxTilt * outerRadius * sensitivity)

            val bubbleColor = if (isLevel) colors.positive else colors.info
            drawCircle(color = bubbleColor.copy(alpha = 0.08f), radius = bubbleRadius * 2.5f, center = Offset(bubbleX, bubbleY))
            drawCircle(color = bubbleColor, radius = bubbleRadius, center = Offset(bubbleX, bubbleY))
            drawCircle(color = Color.White.copy(alpha = 0.3f), radius = bubbleRadius * 0.4f, center = Offset(bubbleX - bubbleRadius * 0.2f, bubbleY - bubbleRadius * 0.2f))
            drawCircle(color = levelOnSurfaceVariant.copy(alpha = 0.3f), radius = 3f, center = Offset(cx, cy))
        }
    }
}

// ── Vertical tube level for plumb (vertical) mode ──
@Composable
private fun VerticalTubeLevel(
    tilt: Float, isLevel: Boolean, colors: fieldmind.research.app.features.field.presentation.theme.FieldMindColors
) {
    val paint = remember {
        android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) {
        val levelSurfaceHighest = MaterialTheme.colorScheme.surfaceContainerHighest
        val levelOutlineVariant = MaterialTheme.colorScheme.outlineVariant
        val levelOnSurface = MaterialTheme.colorScheme.onSurface
        val levelOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val tubeWidth = size.width * 0.20f
            val tubeHeight = size.height * 0.82f
            val tubeTop = (size.height - tubeHeight) / 2f
            val tubeCornerR = tubeWidth / 2f

            // Tube body
            drawRoundRect(
                color = levelSurfaceHighest,
                topLeft = Offset(cx - tubeWidth / 2f, tubeTop),
                size = Size(tubeWidth, tubeHeight),
                cornerRadius = CornerRadius(tubeCornerR, tubeCornerR)
            )
            drawRoundRect(
                color = levelOutlineVariant.copy(alpha = 0.3f),
                topLeft = Offset(cx - tubeWidth / 2f, tubeTop),
                size = Size(tubeWidth, tubeHeight),
                cornerRadius = CornerRadius(tubeCornerR, tubeCornerR),
                style = Stroke(width = 2f)
            )

            // Center line (0° mark)
            val centerY = size.height / 2f
            drawLine(
                color = levelOutlineVariant.copy(alpha = 0.4f),
                start = Offset(cx - tubeWidth * 0.55f, centerY),
                end = Offset(cx + tubeWidth * 0.55f, centerY),
                strokeWidth = 1.5f
            )

            // Degree markers: draw tick marks at ±5°, ±10°, ±15°
            val marks = listOf(-15f, -10f, -5f, 5f, 10f, 15f)
            val maxTilt = 15f
            marks.forEach { deg ->
                val y = centerY + (deg / maxTilt * tubeHeight * 0.42f)
                val tickW = if (abs(deg) % 5f == 0f) tubeWidth * 0.5f else tubeWidth * 0.3f
                drawLine(
                    color = levelOutlineVariant.copy(alpha = 0.3f),
                    start = Offset(cx + tubeWidth / 2f, y),
                    end = Offset(cx + tubeWidth / 2f + tickW, y),
                    strokeWidth = if (abs(deg) % 5f == 0f) 1.5f else 1f
                )
                paint.color = levelOnSurfaceVariant.toArgb()
                paint.textSize = 28f
                paint.isFakeBoldText = false
                drawContext.canvas.nativeCanvas.drawText(
                    "%.0f°".format(deg), cx + tubeWidth / 2f + tickW + 20f, y + paint.textSize / 3f, paint
                )
            }

            // 0° label
            paint.color = levelOnSurface.toArgb()
            paint.textSize = 30f
            paint.isFakeBoldText = true
            drawContext.canvas.nativeCanvas.drawText(
                "0°", cx + tubeWidth / 2f + 24f, centerY + paint.textSize / 3f, paint
            )

            // Bubble
            val bubbleR = tubeWidth * 0.30f
            val normalizedTilt = (tilt.coerceIn(-maxTilt, maxTilt) / maxTilt)
            val bubbleY = centerY + normalizedTilt * tubeHeight * 0.42f
            val bubbleColor = if (isLevel) colors.positive else colors.info

            drawCircle(color = bubbleColor.copy(alpha = 0.08f), radius = bubbleR * 2f, center = Offset(cx, bubbleY))
            drawCircle(color = bubbleColor, radius = bubbleR, center = Offset(cx, bubbleY))
            drawCircle(color = Color.White.copy(alpha = 0.3f), radius = bubbleR * 0.35f,
                center = Offset(cx - bubbleR * 0.2f, bubbleY - bubbleR * 0.2f))
        }
    }
}

@Composable
private fun TiltGauge(label: String, degrees: Float, description: String, color: Color, modifier: Modifier = Modifier) {
    val absDeg = abs(degrees)
    val isLevel = absDeg < 2f

    Column(
        modifier = modifier,
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
