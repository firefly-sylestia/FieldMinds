package fieldmind.research.app.features.field.presentation.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.draw.clip
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

    // ── Interference state ──
    var isInterference by remember { mutableStateOf(false) }
    var interferenceLabel by remember { mutableStateOf("") }
    var wasInterference by remember { mutableStateOf(false) }

    // ── Interference dismissed state ──
    var dismissedInterference by remember { mutableStateOf(false) }

    // ── Magnetic field chart buffer (rolling window of readings) ──
    val fieldReadings = remember { mutableStateListOf<Float>() }

    // ── Calibration state ──
    var needsCalibration by remember { mutableStateOf(true) }
    var showCalibrationGuide by remember { mutableStateOf(false) }
    val haptics = rememberFieldMindHaptics()

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
        val alpha = 0.12f  // Low-pass filter coefficient (lower = more smoothing)

        var magnetometerCounter = 0

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
                        val newField = sqrt(
                            geomagnetic[0] * geomagnetic[0] +
                            geomagnetic[1] * geomagnetic[1] +
                            geomagnetic[2] * geomagnetic[2]
                        )
                        magneticField = newField

                        // ── Throttled sampling for mini chart (~12.5 samples/sec at GAME rate) ──
                        magnetometerCounter++
                        if (magnetometerCounter % 4 == 0) {
                            fieldReadings.add(newField)
                            if (fieldReadings.size > 60) {
                                fieldReadings.removeAt(0)
                            }
                        }

                        // Magnetic interference detection (Earth's field: 25-65 μT)
                        isInterference = newField < 15f || newField > 100f
                        interferenceLabel = when {
                            newField > 200f -> "Strong magnetic source nearby — move away from electronics or metal"
                            newField > 100f -> "Elevated magnetic field — may affect accuracy"
                            newField < 15f -> "Weak magnetic field — shielded environment or sensor issue"
                            else -> ""
                        }
                    }
                }

                if (!firstGravity && !firstGeomagnetic &&
                    SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                    // Pass rotationMatrix directly to getOrientation — no remapping needed
                    // (remapCoordinateSystem with AXIS_X/AXIS_Z forces AR/vertical orientation
                    //  and produces wrong headings when the device is held flat)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    // orientation[0] = azimuth (tilt-compensated)
                    val newAzimuth = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360) % 360
                    // Dead-zone filter: only update if change exceeds 0.5° (eliminates micro-jitter)
                    if (abs(newAzimuth - azimuth) > 0.5f) {
                        azimuth = newAzimuth
                    }
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
    // ── Smooth animation for jitter dampening without the 359°→0° wrap-around glitch. ──
    val smoothAzimuth by animateFloatAsState(
        targetValue = azimuth,
        animationSpec = tween(durationMillis = 50),
        label = "azimuth"
    )

    // ── Cardinal direction ──
    val cardinal = remember(azimuth) {
        val dirs = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val index = ((azimuth + 11.25f) / 22.5f).roundToInt() % 16
        dirs[index]
    }

    // ── Haptic + dismissed reset when interference state changes ──
    LaunchedEffect(isInterference) {
        if (isInterference && !wasInterference) {
            haptics.light()
        }
        if (!isInterference) {
            dismissedInterference = false // reset dismiss so card reappears next time
        }
        wasInterference = isInterference
    }

    // ── Field status for display ──
    val fieldStatus = remember(magneticField) {
        when {
            magneticField > 200f -> Triple("Strong", MaterialTheme.colorScheme.error, "magnet")
            magneticField > 100f -> Triple("Elevated", FieldMindTheme.colors.warning, "warning")
            magneticField < 15f -> Triple("Weak", MaterialTheme.colorScheme.error, "error")
            else -> Triple("Normal", FieldMindTheme.colors.positive, "check_circle")
        }
    }

    // ── Subtle haptic pulse when passing N/E/S/W cardinals ──
    // Uses a ±3° detection zone; fires haptics.light() only on entry
    var nearN by remember { mutableStateOf(false) }
    var nearE by remember { mutableStateOf(false) }
    var nearS by remember { mutableStateOf(false) }
    var nearW by remember { mutableStateOf(false) }
    LaunchedEffect(azimuth) {
        // N (0° / 360°): wrap-around via minOf
        val enteringN = minOf(azimuth, 360f - azimuth) < 3f
        if (enteringN && !nearN) haptics.light()
        nearN = enteringN

        // E (90°)
        val enteringE = abs(azimuth - 90f) < 3f
        if (enteringE && !nearE) haptics.light()
        nearE = enteringE

        // S (180°)
        val enteringS = abs(azimuth - 180f) < 3f
        if (enteringS && !nearS) haptics.light()
        nearS = enteringS

        // W (270°)
        val enteringW = abs(azimuth - 270f) < 3f
        if (enteringW && !nearW) haptics.light()
        nearW = enteringW
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
                            SensorDataItem(
                                "Magnetic field",
                                "%.1f μT".format(magneticField),
                                MaterialSymbolIcon("magnet"),
                                valueColor = fieldStatus.second
                            )
                            SensorDataItem(
                                "Field",
                                fieldStatus.first,
                                when (fieldStatus.first) {
                                    "Normal" -> MaterialSymbolIcon("check_circle", filled = true)
                                    "Elevated" -> MaterialSymbolIcon("warning")
                                    else -> MaterialSymbolIcon("error")
                                },
                                valueColor = fieldStatus.second
                            )
                            SensorDataItem("Accuracy", accuracy, when (accuracy) {
                                "High" -> MaterialSymbolIcon("check_circle", filled = true)
                                "Medium" -> MaterialSymbolIcon("radio_button_partial")
                                else -> MaterialSymbolIcon("warning")
                            })
                        }

                        // ── Magnetic interference warning ──
                        if (isInterference && interferenceLabel.isNotBlank() && !dismissedInterference) {
                            MagneticInterferenceCard(
                                label = interferenceLabel,
                                isStrong = magneticField > 200f,
                                onDismiss = { dismissedInterference = true }
                            )
                        }

                        // ── Magnetic field mini chart ──
                        if (fieldReadings.size >= 2) {
                            Spacer(Modifier.height(2.dp))
                            MagneticFieldChart(fieldReadings.toList())
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

                        // ── Calibration guide (animated figure-8 with accuracy progress) ──
                        if (needsCalibration || showCalibrationGuide) {
                            CalibrationGuideCard(
                                accuracy = accuracy,
                                haptics = haptics,
                                onCalibrated = { showCalibrationGuide = false }
                            )
                        } else if (accuracy == "Medium" || accuracy == "High") {
                            // ── Recalibrate button (only when already calibrated) ──
                            Spacer(Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = {
                                    haptics.light()
                                    showCalibrationGuide = true
                                },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(MaterialSymbolIcon("tune"), null, size = 16.dp)
                                Spacer(Modifier.size(6.dp))
                                Text("Recalibrate", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
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
                    }
                }
            }
        }
        FieldMindSnackbarOverlay(hostState = snackbar, modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp, start = 16.dp, end = 16.dp))
    }
}

@Composable
private fun CalibrationGuideCard(accuracy: String, haptics: FieldMindHaptics, onCalibrated: () -> Unit = {}) {
    val colors = FieldMindTheme.colors
    val surfaceHighest = MaterialTheme.colorScheme.surfaceContainerHighest
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceV = MaterialTheme.colorScheme.onSurfaceVariant

    // ── Accuracy progress: ordered levels ──
    val accuracyLevels = listOf("Unreliable", "Low", "Medium", "High")
    val currentLevel = accuracyLevels.indexOf(accuracy).coerceAtLeast(0)
    val progress = currentLevel.toFloat() / (accuracyLevels.size - 1)

    // ── Haptic on calibration completion (first time reaching Medium or High) ──
    var wasCalibrated by remember { mutableStateOf(false) }
    val isCalibrated = currentLevel >= 2 // Medium or better
    LaunchedEffect(isCalibrated) {
        if (isCalibrated && !wasCalibrated) {
            haptics.confirm()
            onCalibrated() // notify parent to dismiss manual override
        }
        wasCalibrated = isCalibrated
    }

    // ── Animated figure-8 tracing dot ──
    val infiniteTransition = rememberInfiniteTransition(label = "figure8")
    val figure8Progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "figure8Progress"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.info.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // ── Header row ──
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(MaterialSymbolIcon("tune"), null, tint = colors.info, size = 20.dp)
                Text("Calibrate compass", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            // ── Animated figure-8 guide ──
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val cxa = size.width / 2f
                    val cya = size.height / 2f
                    val radius = minOf(cxa, cya) * 0.38f

                    // Draw the figure-8 path
                    val path = Path().apply {
                        // Figure-8 parametric: x = R*sin(t), y = R*sin(2t)/2
                        val steps = 60
                        for (i in 0..steps) {
                            val t = (i.toFloat() / steps) * (2f * kotlin.math.PI.toFloat())
                            val px = cxa + radius * sin(t)
                            val py = cya + radius * 0.5f * sin(2f * t)
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    drawPath(
                        path, color = outlineVariant.copy(alpha = 0.3f),
                        style = Stroke(width = 2f, cap = StrokeCap.Round)
                    )

                    // Moving dot along the path
                    val dotT = figure8Progress * 2f * kotlin.math.PI.toFloat()
                    val dotX = cxa + radius * sin(dotT)
                    val dotY = cya + radius * 0.5f * sin(2f * dotT)
                    drawCircle(color = colors.info, radius = 6f, center = Offset(dotX, dotY))
                    drawCircle(color = colors.info.copy(alpha = 0.2f), radius = 12f, center = Offset(dotX, dotY))
                }
            }

            // ── Actionable instruction ──
            Text(
                "Rotate your device in a figure-8 pattern until accuracy reaches Medium or High.",
                style = MaterialTheme.typography.bodySmall,
                color = onSurfaceV
            )

            // ── Accuracy progress bar ──
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Accuracy", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold, color = onSurfaceV)
                    Text(accuracy, style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (accuracy) {
                            "High" -> colors.positive
                            "Medium" -> colors.warning
                            else -> MaterialTheme.colorScheme.error
                        })
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = when {
                        progress >= 1f -> colors.positive
                        progress >= 0.66f -> colors.warning
                        else -> MaterialTheme.colorScheme.error
                    },
                    trackColor = surfaceHighest
                )

                // Step labels
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    accuracyLevels.forEach { level ->
                        val idx = accuracyLevels.indexOf(level)
                        Text(
                            level,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = if (idx <= currentLevel) MaterialTheme.colorScheme.onSurface
                                    else onSurfaceV.copy(alpha = 0.4f),
                            fontWeight = if (idx == currentLevel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // Completion message
                if (accuracy == "High") {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.positive.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(MaterialSymbolIcon("check_circle", filled = true), null,
                                tint = colors.positive, size = 18.dp)
                            Text("Calibrated — heading is now accurate.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.positive)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SensorDataItem(label: String, value: String, icon: MaterialSymbolIcon, valueColor: Color? = null) {
    val displayColor = valueColor ?: MaterialTheme.colorScheme.onSurface
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = displayColor, size = 18.dp)
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = displayColor)
        }
    }
}

@Composable
private fun MagneticInterferenceCard(label: String, isStrong: Boolean, onDismiss: () -> Unit = {}) {
    val colors = FieldMindTheme.colors
    val bgColor = if (isStrong) MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                 else colors.warning.copy(alpha = 0.08f)
    val iconColor = if (isStrong) MaterialTheme.colorScheme.error else colors.warning
    val title = if (isStrong) "Strong interference" else "Magnetic interference"

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(MaterialSymbolIcon(if (isStrong) "gpp_bad" else "warning"), null,
                tint = iconColor, size = 20.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold, color = iconColor)
                Text(label, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // ── Dismiss button ──
            TextButton(
                onClick = onDismiss,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.align(Alignment.Top)
            ) {
                Icon(
                    MaterialSymbolIcon("close"),
                    contentDescription = "Dismiss",
                    modifier = Modifier.size(16.dp),
                    tint = iconColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun MagneticFieldChart(readings: List<Float>) {
    if (readings.size < 2) return

    val colors = FieldMindTheme.colors
    val maxY = 200f // μT — Earth's field range is 25-65 μT, so 0-200 covers everything

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        val chartW = size.width
        val chartH = size.height

        // ── Reference lines: 15 μT (weak boundary) and 100 μT (elevated boundary) ──
        val refColor = Color.Gray.copy(alpha = 0.15f)
        listOf(15f, 100f).forEach { refValue ->
            val y = chartH - (refValue / maxY * chartH)
            drawLine(refColor, Offset(0f, y), Offset(chartW, y), strokeWidth = 1f)
        }

        // ── Data line ──
        val stepX = chartW / (readings.size - 1).coerceAtLeast(1)
        val latest = readings.last()
        val lineColor = when {
            latest > 200f -> MaterialTheme.colorScheme.error
            latest > 100f -> FieldMindTheme.colors.warning
            latest < 15f -> MaterialTheme.colorScheme.error
            else -> FieldMindTheme.colors.positive
        }

        val path = Path()
        readings.forEachIndexed { i, value ->
            val x = i * stepX
            val y = chartH - (value.coerceIn(0f, maxY) / maxY * chartH)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = lineColor, style = Stroke(width = 2f, cap = StrokeCap.Round))

        // ── Current value dot at the end of the line ──
        val lastX = (readings.size - 1) * stepX
        val lastY = chartH - (readings.last().coerceIn(0f, maxY) / maxY * chartH)
        drawCircle(lineColor, radius = 3f, center = Offset(lastX, lastY))
        drawCircle(lineColor.copy(alpha = 0.2f), radius = 7f, center = Offset(lastX, lastY))
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
                    // Use -gravityY to normalize: atan2(0, 9.81) = 0 when device is perfectly vertical
                    Math.toDegrees(atan2(gravityZ.toDouble(), -(gravityY.toDouble()))).toFloat()
                OrientationMode.VERTICAL_LANDSCAPE ->
                    // Use -gravityX to normalize: atan2(0, 9.81) = 0 when device is perfectly vertical
                    Math.toDegrees(atan2(gravityZ.toDouble(), -(gravityX.toDouble()))).toFloat()
            }
        }
    }
    val rawRoll: Float by remember(gravityX, gravityY, gravityZ, orientationMode) {
        derivedStateOf {
            when (orientationMode) {
                OrientationMode.FLAT ->
                    Math.toDegrees(atan2(gravityY.toDouble(), abs(gravityZ).toDouble())).toFloat()
                OrientationMode.VERTICAL_PORTRAIT ->
                    // Use -gravityY to normalize: atan2(0, 9.81) = 0 when device is perfectly vertical
                    Math.toDegrees(atan2(gravityX.toDouble(), -(gravityY.toDouble()))).toFloat()
                OrientationMode.VERTICAL_LANDSCAPE ->
                    // Use -gravityX to normalize: atan2(0, 9.81) = 0 when device is perfectly vertical
                    Math.toDegrees(atan2(gravityY.toDouble(), -(gravityX.toDouble()))).toFloat()
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

    // ── Haptic feedback within ±1° (tighter than visual ±2° indicator) ──
    val haptics = rememberFieldMindHaptics()
    val isHapticLevel = abs(effectivePitch) < 1f && abs(effectiveRoll) < 1f
    var wasHapticLevel by remember { mutableStateOf(false) }
    LaunchedEffect(isHapticLevel) {
        if (isHapticLevel && !wasHapticLevel) {
            haptics.confirm()
        }
        wasHapticLevel = isHapticLevel
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

            val maxTilt = 45f
            val sensitivity = 0.88f
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

            // Degree markers: draw tick marks at ±5°, ±10°, ±15°, ±30°, ±45°
            val marks = listOf(-45f, -30f, -15f, -10f, -5f, 5f, 10f, 15f, 30f, 45f)
            val maxTilt = 45f
            marks.forEach { deg ->
                val y = centerY + (deg / maxTilt * tubeHeight * 0.42f)
                val tickW = if (abs(deg) % 15f == 0f) tubeWidth * 0.65f else if (abs(deg) % 5f == 0f) tubeWidth * 0.5f else tubeWidth * 0.3f
                drawLine(
                    color = levelOutlineVariant.copy(alpha = 0.3f),
                    start = Offset(cx + tubeWidth / 2f, y),
                    end = Offset(cx + tubeWidth / 2f + tickW, y),
                    strokeWidth = if (abs(deg) % 15f == 0f) 2.5f else if (abs(deg) % 5f == 0f) 1.5f else 1f
                )
                paint.color = levelOnSurfaceVariant.toArgb()
                paint.textSize = 26f
                paint.isFakeBoldText = abs(deg) % 15f == 0f
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
