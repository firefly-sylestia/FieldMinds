package fieldmind.research.app.features.field.presentation.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fieldmind.research.app.features.field.presentation.components.*
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import fieldmind.research.app.features.field.presentation.viewmodel.FieldMindViewModel
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

// ══════════════════════════════════════════════════════════════════════
//  Compass Tool — Real-time compass heading using magnetometer + accelerometer
// ══════════════════════════════════════════════════════════════════════

@Composable
fun CompassToolScreen(
    viewModel: FieldMindViewModel? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = FieldMindTheme.colors
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ── Sensor state ──
    var azimuth by remember { mutableFloatStateOf(0f) }        // degrees from north
    var magneticField by remember { mutableFloatStateOf(0f) }  // μT
    var accuracy by remember { mutableStateOf("Unknown") }

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

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        gravity[0] = event.values[0]
                        gravity[1] = event.values[1]
                        gravity[2] = event.values[2]
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        geomagnetic[0] = event.values[0]
                        geomagnetic[1] = event.values[1]
                        geomagnetic[2] = event.values[2]
                        magneticField = sqrt(event.values[0] * event.values[0] +
                            event.values[1] * event.values[1] +
                            event.values[2] * event.values[2])
                    }
                }

                if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    azimuth = (azimuthDeg + 360) % 360

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

                                // Outer ring
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

                                // Degree ticks and cardinal markers
                                for (deg in 0 until 360 step 2) {
                                    val rad = Math.toRadians(deg.toDouble() - smoothAzimuth.toDouble())
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
                                    drawLine(
                                        color = tickColor,
                                        start = Offset(
                                            cx + (innerR * cos(rad)).toFloat(),
                                            cy + (innerR * sin(rad)).toFloat()
                                        ),
                                        end = Offset(
                                            cx + (radius * cos(rad)).toFloat(),
                                            cy + (radius * sin(rad)).toFloat()
                                        ),
                                        strokeWidth = tickWidth,
                                        cap = StrokeCap.Round
                                    )
                                }

                                // Cardinal letters (N, E, S, W) — always upright
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

                                // Center triangle pointer (points toward heading)
                                val pointerLen = radius * 0.35f
                                val pointerColor = Color(0xFFE53935)
                                // Top triangle
                                val topPath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(cx, cy - radius + 8f)
                                    lineTo(cx - 14f, cy - radius + 44f)
                                    lineTo(cx + 14f, cy - radius + 44f)
                                    close()
                                }
                                drawPath(topPath, color = pointerColor)
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
                            "Heading: $cardinal (${cardinal})",
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
                        Text("• Hold your device flat and level for best accuracy", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
//  Level Tool — Real-time spirit level using accelerometer
// ══════════════════════════════════════════════════════════════════════

@Composable
fun LevelToolScreen(
    viewModel: FieldMindViewModel? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = FieldMindTheme.colors

    // ── Sensor state ──
    var pitch by remember { mutableFloatStateOf(0f) }   // tilt forward/backward (deg)
    var roll by remember { mutableFloatStateOf(0f) }    // tilt left/right (deg)
    var isFlat by remember { mutableStateOf(false) }

    // ── Sensor listener ──
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // Calculate pitch and roll from accelerometer
                // When device is flat on its back: x=0, y=0, z=9.8
                // Pitch: rotation around X axis (tilting forward/backward)
                // Roll: rotation around Y axis (tilting left/right)
                pitch = Math.toDegrees(atan2(x.toDouble(), sqrt((y * y + z * z).toDouble()))).toFloat()
                roll = Math.toDegrees(atan2(y.toDouble(), sqrt((x * x + z * z).toDouble()))).toFloat()

                isFlat = abs(pitch) < 2f && abs(roll) < 2f
            }

            override fun onAccuracyChanged(sensor: Sensor, acc: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // ── Smooth animations ──
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
                    containerColor = if (isFlat) colors.positive.copy(alpha = 0.08f)
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
                            // Vertical
                            drawLine(crossColor, Offset(cx, cy - outerRadius * 0.85f), Offset(cx, cy + outerRadius * 0.85f), 1f)
                            // Horizontal
                            drawLine(crossColor, Offset(cx - outerRadius * 0.85f, cy), Offset(cx + outerRadius * 0.85f, cy), 1f)

                            // Bubble — offset from center based on pitch and roll
                            // Map ±15° to ±60% of outerRadius
                            val maxTilt = 15f
                            val sensitivity = 0.6f
                            val bubbleX = cx + (smoothRoll.coerceIn(-maxTilt, maxTilt) / maxTilt * outerRadius * sensitivity)
                            val bubbleY = cy + (smoothPitch.coerceIn(-maxTilt, maxTilt) / maxTilt * outerRadius * sensitivity)

                            // Bubble glow
                            val bubbleColor = if (isFlat) colors.positive else colors.info
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

                    // ── Flat indicator ──
                    if (isFlat) {
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

            // ── Tilt values ──
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
                        TiltGauge("Pitch", smoothPitch, "Forward/backward", colors.info, modifier = Modifier.weight(1f))
                        TiltGauge("Roll", smoothRoll, "Left/right", colors.data, modifier = Modifier.weight(1f))
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    Text(
                        "Hold device flat to check level surfaces. Pitch and roll should be near 0°.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
