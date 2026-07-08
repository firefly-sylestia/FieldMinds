package fieldmind.research.app.features.field.presentation.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fieldmind.research.app.features.field.presentation.components.*
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.features.field.presentation.viewmodel.FieldMindViewModel
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

// ══════════════════════════════════════════════════════════════════════
//  Compass Tool — Premium Redesign
//  Features:
//    - Glassmorphic compass face with animated gradient ring
//    - Rotating rose with refined ticks and glowing cardinal labels
//    - Smooth animated heading display with cardinal pill
//    - Mini gradient gauges for sensor data
//    - Elegant calibration guide with animated figure-8
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
    var azimuth by remember { mutableFloatStateOf(0f) }
    var magneticField by remember { mutableFloatStateOf(0f) }
    var accuracy by remember { mutableStateOf("Unknown") }
    var compassPitch by remember { mutableFloatStateOf(0f) }
    var compassRoll by remember { mutableFloatStateOf(0f) }

    // ── Interference state ──
    var isInterference by remember { mutableStateOf(false) }
    var interferenceLabel by remember { mutableStateOf("") }
    var wasInterference by remember { mutableStateOf(false) }
    var dismissedInterference by remember { mutableStateOf(false) }

    // ── Chart buffers ──
    val fieldReadings = remember { mutableStateListOf<Float>() }
    val pitchReadings = remember { mutableStateListOf<Float>() }
    val rollReadings = remember { mutableStateListOf<Float>() }

    // ── Calibration state ──
    var needsCalibration by remember { mutableStateOf(true) }
    var showCalibrationGuide by remember { mutableStateOf(false) }
    val haptics = rememberFieldMindHaptics()

    // ── True north / declination ──
    var useTrueNorth by remember { mutableStateOf(false) }
    var declination by remember { mutableFloatStateOf(0f) }
    var locationLabel by remember { mutableStateOf("—") }

    // Look up magnetic declination from last known location
    LaunchedEffect(Unit) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
            val location = locationManager?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                ?: locationManager?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
            if (location != null) {
                val geoField = android.location.GeomagneticField(
                    location.latitude.toFloat(),
                    location.longitude.toFloat(),
                    location.altitude.toFloat(),
                    System.currentTimeMillis()
                )
                declination = geoField.declination
                locationLabel = "%.1f° %.1f°".format(location.latitude, location.longitude)
            }
        } catch (_: SecurityException) {
            // Location permission not granted — declination stays 0
        }
    }

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
        val alpha = 0.12f
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

                        magnetometerCounter++
                        if (magnetometerCounter % 4 == 0) {
                            fieldReadings.add(newField)
                            if (fieldReadings.size > 60) fieldReadings.removeAt(0)
                        }

                        // Buffer pitch/roll at same cadence
                        if (magnetometerCounter % 4 == 0 && !firstGravity && !firstGeomagnetic) {
                            SensorManager.getOrientation(rotationMatrix, orientation)
                            pitchReadings.add(Math.toDegrees(orientation[1].toDouble()).toFloat())
                            rollReadings.add(Math.toDegrees(orientation[2].toDouble()).toFloat())
                            if (pitchReadings.size > 40) {
                                pitchReadings.removeAt(0)
                                rollReadings.removeAt(0)
                            }
                        }

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
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val newAzimuth = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360) % 360
                    if (abs(newAzimuth - azimuth) > 0.5f) azimuth = newAzimuth
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
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // ── Display azimuth: magnetic or true north corrected ──
    val displayAzimuth by remember(azimuth, declination, useTrueNorth) {
        derivedStateOf {
            if (useTrueNorth) (azimuth + declination + 360) % 360 else azimuth
        }
    }
    val headingLabelVersion = if (useTrueNorth) "True" else "Magnetic"

    // ── Smooth animated display azimuth ──
    val smoothDisplayAzimuth by animateFloatAsState(
        targetValue = displayAzimuth,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f),
        label = "displayAzimuth"
    )

    // ── Cardinal direction ──
    val cardinal = remember(displayAzimuth) {
        val dirs = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val index = ((displayAzimuth + 11.25f) / 22.5f).roundToInt() % 16
        dirs[index]
    }

    // ── Haptic + dismissed reset when interference state changes ──
    LaunchedEffect(isInterference) {
        if (isInterference && !wasInterference) haptics.light()
        if (!isInterference) dismissedInterference = false
        wasInterference = isInterference
    }

    // ── Field status colors ──
    val statusErrorColor = MaterialTheme.colorScheme.error
    val statusWarningColor = FieldMindTheme.colors.warning
    val statusPositiveColor = FieldMindTheme.colors.positive
    val fieldStatus = remember(magneticField, statusErrorColor, statusWarningColor, statusPositiveColor) {
        when {
            magneticField > 200f -> Triple("Strong", statusErrorColor, "gpp_bad")
            magneticField > 100f -> Triple("Elevated", statusWarningColor, "warning")
            magneticField < 15f -> Triple("Weak", statusErrorColor, "error")
            else -> Triple("Normal", statusPositiveColor, "check_circle")
        }
    }
    val fieldRange = remember(magneticField) {
        magneticField.coerceIn(0f, 200f) / 200f
    }

    // ── Haptic on cardinal crossings ──
    var nearN by remember { mutableStateOf(false) }
    var nearE by remember { mutableStateOf(false) }
    var nearS by remember { mutableStateOf(false) }
    var nearW by remember { mutableStateOf(false) }
    LaunchedEffect(displayAzimuth) {
        val az = displayAzimuth
        val enteringN = minOf(az, 360f - az) < 3f
        if (enteringN && !nearN) haptics.light()
        nearN = enteringN
        val enteringE = abs(az - 90f) < 3f
        if (enteringE && !nearE) haptics.light()
        nearE = enteringE
        val enteringS = abs(az - 180f) < 3f
        if (enteringS && !nearS) haptics.light()
        nearS = enteringS
        val enteringW = abs(az - 270f) < 3f
        if (enteringW && !nearW) haptics.light()
        nearW = enteringW
    }

    // ── Animated interference banner ──
    val showInterferenceBanner = isInterference && interferenceLabel.isNotBlank() && !dismissedInterference
    val bannerAlpha by animateFloatAsState(
        targetValue = if (showInterferenceBanner) 1f else 0f,
        animationSpec = tween(300), label = "bannerAlpha"
    )

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Header ──
            StandardScreenHeader(
                title = "Compass",
                subtitle = "Real-time $headingLabelVersion heading" + if (useTrueNorth && abs(declination) > 0.5f) " (δ ${\"%.1f°\".format(abs(declination))})" else "",
                icon = MaterialSymbolIcon("explore"),
                heroColor = colors.info,
                trailing = { BackButton(onClick = onBack) }
            )

            // ── Glassmorphic Compass Face ──
            Card(
                shape = RoundedCornerShape(44.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // ── Premium compass rose ──
                        CompassRoseCanvas(
                            azimuth = smoothDisplayAzimuth,
                            isInterference = isInterference,
                            magneticField = magneticField
                        )

                        // ── Heading display ──
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "%.1f°".format(displayAzimuth),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 52.sp,
                                    letterSpacing = (-2).sp
                                ),
                                color = colors.info
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = colors.info.copy(alpha = 0.12f),
                                    tonalElevation = 0.dp
                                ) {
                                    Row(
                                        Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            MaterialSymbolIcon("explore"),
                                            null,
                                            tint = colors.info,
                                            size = 20.dp
                                        )
                                        Text(
                                            "Heading $cardinal",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.info
                                        )
                                    }
                                }
                                // ── Magnetic / True North toggle ──
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (useTrueNorth) colors.data.copy(alpha = 0.15f) else colors.info.copy(alpha = 0.08f),
                                    tonalElevation = 0.dp,
                                    modifier = Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { haptics.light(); useTrueNorth = !useTrueNorth }
                                    )
                                ) {
                                    Row(
                                        Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            MaterialSymbolIcon(if (useTrueNorth) "my_location" else "explore"),
                                            null,
                                            tint = if (useTrueNorth) colors.data else colors.info.copy(alpha = 0.6f),
                                            size = 16.dp
                                        )
                                        Text(
                                            if (useTrueNorth) "True" else "Mag",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (useTrueNorth) FontWeight.Bold else FontWeight.Medium,
                                            color = if (useTrueNorth) colors.data else colors.info.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Interference banner (animated) ──
            if (showInterferenceBanner) {
                CompassInterferenceCard(
                    label = interferenceLabel,
                    isStrong = magneticField > 200f,
                    bannerAlpha = bannerAlpha,
                    onDismiss = { dismissedInterference = true }
                )
            }

            // ── Sensor data row with mini gauges ──
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SensorMiniCard(
                    label = "Field",
                    value = "%.0f μT".format(magneticField),
                    progress = fieldRange,
                    progressColor = fieldStatus.second,
                    icon = when (fieldStatus.first) {
                        "Normal" -> MaterialSymbolIcon("magnet", filled = true)
                        else -> MaterialSymbolIcon("magnet")
                    },
                    modifier = Modifier.weight(1f)
                )
                SensorMiniCard(
                    label = "Status",
                    value = fieldStatus.first,
                    progress = 1f,
                    progressColor = fieldStatus.second,
                    icon = MaterialSymbolIcon(fieldStatus.third, filled = fieldStatus.first == "Normal"),
                    modifier = Modifier.weight(1f)
                )
                SensorMiniCard(
                    label = "Accuracy",
                    value = accuracy,
                    progress = when (accuracy) {
                        "High" -> 1f; "Medium" -> 0.66f; "Low" -> 0.33f; else -> 0f
                    },
                    progressColor = when (accuracy) {
                        "High" -> colors.positive
                        "Medium" -> colors.warning
                        else -> MaterialTheme.colorScheme.error
                    },
                    icon = when (accuracy) {
                        "High" -> MaterialSymbolIcon("check_circle", filled = true)
                        "Medium" -> MaterialSymbolIcon("radio_button_partial")
                        else -> MaterialSymbolIcon("warning")
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Magnetic field mini chart + Pitch/Roll ──
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Field chart
                Card(
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.weight(1.4f)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(MaterialSymbolIcon("show_chart"), null, tint = colors.data, size = 16.dp)
                            Text("Field (μT)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (fieldReadings.size >= 2) {
                            MagneticFieldChart(fieldReadings.toList(), fieldStatus.second)
                        } else {
                            Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                                Text("Collecting data…", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                // Pitch/Roll compact card with glassmorphic sparklines
                Card(
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(MaterialSymbolIcon("straighten"), null, tint = colors.data, size = 16.dp)
                            Text("Tilt", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TiltWithSparkline("Pitch", compassPitch, colors.info, pitchReadings.toList())
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                        TiltWithSparkline("Roll", compassRoll, colors.data, rollReadings.toList())
                    }
                }
            }

            // ── Calibration guide ──
            if (needsCalibration || showCalibrationGuide) {
                CompassCalibrationGuide(
                    accuracy = accuracy,
                    haptics = haptics,
                    onCalibrated = { showCalibrationGuide = false }
                )
            } else if (accuracy == "Medium" || accuracy == "High") {
                OutlinedButton(
                    onClick = { haptics.light(); showCalibrationGuide = true },
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(MaterialSymbolIcon("tune"), null, size = 16.dp)
                    Spacer(Modifier.size(6.dp))
                    Text("Recalibrate", fontWeight = FontWeight.SemiBold)
                }
            }
        }
        FieldMindSnackbarOverlay(hostState = snackbar, modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp, start = 16.dp, end = 16.dp))
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Premium Compass Rose Canvas
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun CompassRoseCanvas(
    azimuth: Float,
    isInterference: Boolean,
    magneticField: Float
) {
    val surfaceHighest = MaterialTheme.colorScheme.surfaceContainerHighest
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val colors = FieldMindTheme.colors

    // ── Pulsing glow for the outer ring ──
    val infiniteTransition = rememberInfiniteTransition(label = "compassGlow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "glowPulse"
    )
    val glowColor = if (isInterference) colors.warning.copy(alpha = glowPulse * 0.4f)
        else colors.info.copy(alpha = glowPulse * 0.3f)

    Box(modifier = Modifier.size(280.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = minOf(cx, cy) * 0.82f

            // ── Outer gradient glow ring ──
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        glowColor,
                        glowColor.copy(alpha = 0.05f),
                        glowColor,
                        glowColor.copy(alpha = 0.05f),
                        glowColor
                    )
                ),
                radius = radius + 18f,
                center = Offset(cx, cy)
            )

            // ── Outer ring ──
            drawCircle(color = surfaceHighest, radius = radius + 10f, center = Offset(cx, cy))
            drawCircle(
                color = outlineVariant.copy(alpha = 0.25f), radius = radius + 4f, center = Offset(cx, cy),
                style = Stroke(width = 2f)
            )

            // ── Rotating compass rose ──
            withTransform({ rotate(degrees = -azimuth, pivot = Offset(cx, cy)) }) {
                // Degree ticks
                for (deg in 0 until 360 step 2) {
                    val rad = Math.toRadians(deg.toDouble())
                    val isMajor = deg % 90 == 0
                    val isMinor = deg % 10 == 0
                    val tickLen = when { isMajor -> radius * 0.22f; isMinor -> radius * 0.12f; else -> radius * 0.06f }
                    val tickWidth = when { isMajor -> 3.5f; isMinor -> 2f; else -> 1f }
                    val tickColor = when {
                        isMajor -> onSurface
                        isMinor -> onSurfaceVariant.copy(alpha = 0.5f)
                        else -> outlineVariant
                    }
                    val innerR = radius - tickLen
                    val tx = cx + (innerR * sin(rad)).toFloat()
                    val ty = cy - (innerR * cos(rad)).toFloat()
                    val ex = cx + (radius * sin(rad)).toFloat()
                    val ey = cy - (radius * cos(rad)).toFloat()
                    drawLine(color = tickColor, start = Offset(tx, ty), end = Offset(ex, ey), strokeWidth = tickWidth, cap = StrokeCap.Round)
                }

                // ── Cardinal labels with glow ──
                val cardinals = listOf(
                    "N" to Color(0xFFE53935),
                    "E" to onSurface,
                    "S" to onSurface,
                    "W" to onSurface
                )
                val paint = android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.CENTER; isAntiAlias = true
                }
                cardinals.forEachIndexed { i, (label, color) ->
                    val angle = i * 90.0
                    val rad = Math.toRadians(angle)
                    val labelR = radius * 0.74f
                    val x = cx + (labelR * sin(rad)).toFloat()
                    val y = cy - (labelR * cos(rad)).toFloat()

                    // Glow behind N
                    if (label == "N") {
                        paint.color = Color(0xFFE53935).copy(alpha = 0.2f).toArgb()
                        paint.textSize = 52f
                        drawContext.canvas.nativeCanvas.drawText(label, x, y + paint.textSize / 3f, paint)
                    }

                    paint.color = color.toArgb()
                    paint.textSize = if (label == "N") 48f else 36f
                    paint.isFakeBoldText = label == "N"
                    drawContext.canvas.nativeCanvas.drawText(label, x, y + paint.textSize / 3f, paint)
                }

                // ── Intercardinal dots at 45° positions ──
                for (i in 0 until 4) {
                    val dotAngle = i * 90.0 + 45.0
                    val rad = Math.toRadians(dotAngle)
                    val dotR = radius * 0.78f
                    val dx = cx + (dotR * sin(rad)).toFloat()
                    val dy = cy - (dotR * cos(rad)).toFloat()
                    drawCircle(color = onSurfaceVariant.copy(alpha = 0.4f), radius = 3f, center = Offset(dx, dy))
                }
            }

            // ── Fixed heading indicator (red triangle at top) ──
            val indicatorLen = radius * 0.20f
            val indicatorWidth = radius * 0.065f
            val headingPath = Path().apply {
                moveTo(cx, cy - radius + 8f)
                lineTo(cx - indicatorWidth, cy - radius + 8f + indicatorLen)
                lineTo(cx + indicatorWidth, cy - radius + 8f + indicatorLen)
                close()
            }
            // Glow behind indicator
            drawPath(headingPath, color = Color(0xFFE53935).copy(alpha = 0.3f))
            drawPath(headingPath, color = Color(0xFFE53935))

            // South notch
            val southPath = Path().apply {
                moveTo(cx - indicatorWidth * 0.5f, cy + radius - 8f - indicatorLen * 0.4f)
                lineTo(cx, cy + radius - 8f)
                lineTo(cx + indicatorWidth * 0.5f, cy + radius - 8f - indicatorLen * 0.4f)
                close()
            }
            drawPath(southPath, color = Color(0xFF9E9E9E))

            // ── Center pivot ──
            drawCircle(color = onSurface.copy(alpha = 0.3f), radius = 7f, center = Offset(cx, cy))
            drawCircle(color = Color(0xFFE53935), radius = 4f, center = Offset(cx, cy))
            drawCircle(color = Color.White.copy(alpha = 0.5f), radius = 1.5f, center = Offset(cx - 1f, cy - 1f))
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Mini Sensor Data Card (compact gauge)
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun SensorMiniCard(
    label: String,
    value: String,
    progress: Float,
    progressColor: Color,
    icon: MaterialSymbolIcon,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(
            Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, tint = progressColor, size = 22.dp)
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = progressColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Box(
                modifier = Modifier.fillMaxWidth().height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(progress).fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(progressColor.copy(alpha = 0.7f), progressColor)
                            )
                        )
                )
            }
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Magnetic Field Chart — glassmorphic with animated axes
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun MagneticFieldChart(readings: List<Float>, lineColor: Color) {
    if (readings.size < 2) return
    val maxY = 200f
    val labels = listOf(200f, 150f, 100f, 50f, 0f)
    val onSurfaceV = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceHigh = MaterialTheme.colorScheme.surfaceContainerHigh

    Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
        val chartX = 38f  // space for Y-axis labels
        val chartW = size.width - chartX
        val chartH = size.height
        val stepX = chartW / (readings.size - 1).coerceAtLeast(1)

        // ── Glassmorphic background — translucent rounded surface ──
        drawRoundRect(
            color = surfaceHigh.copy(alpha = 0.25f),
            cornerRadius = CornerRadius(12f, 12f),
            topLeft = Offset(chartX, 0f),
            size = Size(chartW, chartH)
        )

        // ── Y-axis labels (rendered via nativeCanvas for pixel alignment) ──
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = onSurfaceV.copy(alpha = 0.4f).toArgb()
            textSize = 22f
            textAlign = android.graphics.Paint.Align.RIGHT
        }
        labels.forEach { value ->
            val y = chartH - (value / maxY * chartH)
            val text = "%.0f".format(value)
            drawContext.canvas.nativeCanvas.drawText(text, chartX - 6f, y + paint.textSize / 3f, paint)
        }

        // ── Horizontal grid lines ──
        val refColor = Color.Gray.copy(alpha = 0.08f)
        labels.forEach { refValue ->
            val y = chartH - (refValue / maxY * chartH)
            drawLine(refColor, Offset(chartX, y), Offset(size.width, y), strokeWidth = 1f)
        }

        // ── Gradient fill under the data line ──
        val fillPath = Path().apply {
            readings.forEachIndexed { i, value ->
                val x = chartX + i * stepX
                val y = chartH - (value.coerceIn(0f, maxY) / maxY * chartH)
                if (i == 0) { moveTo(x, chartH); lineTo(x, y) } else lineTo(x, y)
            }
            lineTo(chartX + (readings.size - 1) * stepX, chartH)
            close()
        }
        drawPath(fillPath, brush = Brush.verticalGradient(
            listOf(lineColor.copy(alpha = 0.25f), lineColor.copy(alpha = 0.03f))
        ))

        // ── Data line (2.5px for better visibility) ──
        val linePath = Path().apply {
            readings.forEachIndexed { i, value ->
                val x = chartX + i * stepX
                val y = chartH - (value.coerceIn(0f, maxY) / maxY * chartH)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(linePath, color = lineColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))

        // ── Fading trace dots at sampled positions for depth ──
        if (readings.size >= 3) {
            val traceStep = maxOf(1, readings.size / 8)
            for (i in 0 until readings.size - 1 step traceStep) {
                val tx = chartX + i * stepX
                val ty = chartH - (readings[i].coerceIn(0f, maxY) / maxY * chartH)
                val fade = (i.toFloat() / readings.size) * 0.3f
                drawCircle(lineColor.copy(alpha = fade * 0.3f), radius = 1.5f, center = Offset(tx, ty))
            }
        }

        // ── End dot with layered glow ──
        val lastX = chartX + (readings.size - 1) * stepX
        val lastY = chartH - (readings.last().coerceIn(0f, maxY) / maxY * chartH)
        drawCircle(lineColor.copy(alpha = 0.3f), radius = 8f, center = Offset(lastX, lastY))
        drawCircle(lineColor, radius = 3.5f, center = Offset(lastX, lastY))
        drawCircle(Color.White.copy(alpha = 0.25f), radius = 1.5f, center = Offset(lastX - 1f, lastY - 1f))

        // ── Y-axis border line ──
        drawLine(onSurfaceV.copy(alpha = 0.12f), Offset(chartX, 0f), Offset(chartX, chartH), strokeWidth = 1f)
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Tilt display with mini glassmorphic sparkline
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun TiltWithSparkline(
    label: String,
    degrees: Float,
    color: Color,
    readings: List<Float>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            "%.1f°".format(degrees),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp
            ),
            color = if (abs(degrees) < 2f) FieldMindTheme.colors.positive else color
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        if (readings.size >= 2) {
            MiniSparkline(readings, color)
        } else {
            Box(Modifier.fillMaxWidth().height(28.dp), contentAlignment = Alignment.Center) {
                Text("…", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            }
        }
    }
}

/** Mini glassmorphic sparkline chart — zero-centered with gradient fill */
@Composable
private fun MiniSparkline(readings: List<Float>, lineColor: Color) {
    if (readings.size < 2) return
    val maxY = 60f
    val surfaceHigh = MaterialTheme.colorScheme.surfaceContainerHigh

    Canvas(modifier = Modifier.fillMaxWidth().height(28.dp)) {
        val chartH = size.height
        val chartW = size.width
        val stepX = chartW / (readings.size - 1).coerceAtLeast(1)
        val midY = chartH / 2f

        // ── Glassmorphic background ──
        drawRoundRect(
            color = surfaceHigh.copy(alpha = 0.15f),
            cornerRadius = CornerRadius(6f, 6f),
            size = Size(chartW, chartH)
        )

        // ── Zero reference line ──
        drawLine(Color.Gray.copy(alpha = 0.08f), Offset(0f, midY), Offset(chartW, midY), strokeWidth = 1f)

        // ── Gradient fill (from data line to zero line for symmetric look) ──
        val fillPath = Path().apply {
            readings.forEachIndexed { i, value ->
                val x = i * stepX
                val y = midY - (value.coerceIn(-maxY, maxY) / maxY * midY)
                if (i == 0) { moveTo(x, midY); lineTo(x, y) } else lineTo(x, y)
            }
            lineTo((readings.size - 1) * stepX, midY)
            close()
        }
        drawPath(fillPath, brush = Brush.verticalGradient(
            listOf(lineColor.copy(alpha = 0.15f), lineColor.copy(alpha = 0.02f))
        ))

        // ── Data line ──
        val linePath = Path().apply {
            readings.forEachIndexed { i, value ->
                val x = i * stepX
                val y = midY - (value.coerceIn(-maxY, maxY) / maxY * midY)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(linePath, color = lineColor, style = Stroke(width = 1.5f, cap = StrokeCap.Round))

        // ── End dot ──
        val lastX = (readings.size - 1) * stepX
        val lastY = midY - (readings.last().coerceIn(-maxY, maxY) / maxY * midY)
        drawCircle(lineColor.copy(alpha = 0.2f), radius = 4f, center = Offset(lastX, lastY))
        drawCircle(lineColor, radius = 2f, center = Offset(lastX, lastY))
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Calibration Guide — glassmorphic with animated glow ring
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun CompassCalibrationGuide(
    accuracy: String,
    haptics: FieldMindHaptics,
    onCalibrated: () -> Unit = {}
) {
    val colors = FieldMindTheme.colors
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceV = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceHigh = MaterialTheme.colorScheme.surfaceContainerHigh

    val accuracyLevels = listOf("Unreliable", "Low", "Medium", "High")
    val currentLevel = accuracyLevels.indexOf(accuracy).coerceAtLeast(0)
    val progress = currentLevel.toFloat() / (accuracyLevels.size - 1)

    var wasCalibrated by remember { mutableStateOf(false) }
    val isCalibrated = currentLevel >= 2
    LaunchedEffect(isCalibrated) {
        if (isCalibrated && !wasCalibrated) { haptics.confirm(); onCalibrated() }
        wasCalibrated = isCalibrated
    }

    val infiniteTransition = rememberInfiniteTransition(label = "figure8")
    val figure8Progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "figure8Progress"
    )

    val progressColor by animateColorAsState(
        targetValue = when { progress >= 1f -> colors.positive; progress >= 0.66f -> colors.warning; else -> MaterialTheme.colorScheme.error },
        animationSpec = tween(400), label = "calProgress"
    )

    // ── Pulsing glow ring for the card (matching compass rose aesthetic) ──
    val glowTransition = rememberInfiniteTransition(label = "calGlow")
    val glowPulse by glowTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "calGlowPulse"
    )
    val glowColor = colors.info.copy(alpha = glowPulse * 0.25f)

    Box(modifier = Modifier.fillMaxWidth()) {
        // ── Animated gradient glow ring behind the card ──
        Canvas(modifier = Modifier.matchParentSize()) {
            val cx = size.width / 2f; val cy = size.height / 2f
            val outerR = maxOf(cx, cy) * 0.95f
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        glowColor, glowColor.copy(alpha = 0.03f),
                        glowColor, glowColor.copy(alpha = 0.03f),
                        glowColor
                    )
                ),
                radius = outerR + 14f, center = Offset(cx, cy)
            )
        }

        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier.size(36.dp).clip(RoundedCornerShape(18.dp))
                            .background(colors.info.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(MaterialSymbolIcon("tune"), null, tint = colors.info, size = 20.dp) }
                    Column(Modifier.weight(1f)) {
                        Text("Calibrate compass", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text("Figure-8 motion refines heading", style = MaterialTheme.typography.bodySmall, color = onSurfaceV)
                    }
                }

                // ── Figure-8 animation with glassmorphic canvas ──
                Box(modifier = Modifier.fillMaxWidth().height(90.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val cxa = size.width / 2f
                        val cya = size.height / 2f
                        val r = minOf(cxa, cya) * 0.35f

                        // Glassmorphic background
                        drawRoundRect(
                            color = surfaceHigh.copy(alpha = 0.2f),
                            cornerRadius = CornerRadius(14f, 14f),
                            size = size
                        )

                        // Figure-8 path
                        val path = Path().apply {
                            val steps = 60
                            for (i in 0..steps) {
                                val t = (i.toFloat() / steps) * (2f * kotlin.math.PI.toFloat())
                                val px = cxa + r * sin(t)
                                val py = cya + r * 0.5f * sin(2f * t)
                                if (i == 0) moveTo(px, py) else lineTo(px, py)
                            }
                            close()
                        }
                        drawPath(path, color = outlineVariant.copy(alpha = 0.25f), style = Stroke(width = 2f, cap = StrokeCap.Round))

                        // Animated dot with glow aura
                        val dotT = figure8Progress * 2f * kotlin.math.PI.toFloat()
                        val dotX = cxa + r * sin(dotT)
                        val dotY = cya + r * 0.5f * sin(2f * dotT)
                        // Outer glow ring
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    colors.info.copy(alpha = 0.1f), colors.info.copy(alpha = 0.02f),
                                    colors.info.copy(alpha = 0.1f), colors.info.copy(alpha = 0.02f),
                                    colors.info.copy(alpha = 0.1f)
                                )
                            ),
                            radius = 15f, center = Offset(dotX, dotY)
                        )
                        drawCircle(color = colors.info.copy(alpha = 0.12f), radius = 10f, center = Offset(dotX, dotY))
                        drawCircle(color = colors.info, radius = 5f, center = Offset(dotX, dotY))
                        drawCircle(color = Color.White.copy(alpha = 0.3f), radius = 2f,
                            center = Offset(dotX - 1.5f, dotY - 1.5f))
                    }
                }

                Text("Rotate your device in a figure-8 pattern until accuracy reaches Medium or High.",
                    style = MaterialTheme.typography.bodySmall, color = onSurfaceV)

                // ── Accuracy progress bar with gradient styling ──
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(MaterialSymbolIcon("speed"), null, tint = progressColor, size = 14.dp)
                            Text("Accuracy", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = onSurfaceV)
                        }
                        Text(accuracy, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = progressColor)
                    }
                    // Custom gradient progress indicator
                    Box(
                        modifier = Modifier.fillMaxWidth().height(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(progress).fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(progressColor.copy(alpha = 0.7f), progressColor)
                                    )
                                )
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        accuracyLevels.forEach { level ->
                            val idx = accuracyLevels.indexOf(level)
                            Text(
                                level,
                                style = MaterialTheme.typography.labelSmall, fontSize = 9.sp,
                                color = if (idx <= currentLevel) MaterialTheme.colorScheme.onSurface else onSurfaceV.copy(alpha = 0.35f),
                                fontWeight = if (idx == currentLevel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                if (accuracy == "High") {
                    Surface(shape = RoundedCornerShape(14.dp), color = colors.positive.copy(alpha = 0.1f), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(MaterialSymbolIcon("check_circle", filled = true), null, tint = colors.positive, size = 20.dp)
                            Text("Calibrated — heading is now accurate.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = colors.positive)
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Interference Banner
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun CompassInterferenceCard(
    label: String,
    isStrong: Boolean,
    bannerAlpha: Float,
    onDismiss: () -> Unit
) {
    val colors = FieldMindTheme.colors
    val bgColor = if (isStrong) MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
        else colors.warning.copy(alpha = 0.08f)
    val iconColor = if (isStrong) MaterialTheme.colorScheme.error else colors.warning
    val title = if (isStrong) "Strong interference" else "Magnetic interference"

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        modifier = Modifier.fillMaxWidth().alpha(bannerAlpha)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(MaterialSymbolIcon(if (isStrong) "gpp_bad" else "warning"), null, tint = iconColor, size = 22.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = iconColor, style = MaterialTheme.typography.labelMedium)
                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onDismiss, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp), modifier = Modifier.align(Alignment.Top)) {
                Icon(MaterialSymbolIcon("close"), contentDescription = "Dismiss", modifier = Modifier.size(16.dp), tint = iconColor.copy(alpha = 0.7f))
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Level Tool — 3D gravity projection (works in ALL orientations)
//  Features:
//    - Uses the full 3D gravity vector — no portrait/landscape distinction
//    - Flat mode: circular bubble level with pitch/roll
//    - Vertical mode: 2D tilt dot on crosshairs (orientation-independent)
//    - Smooth transition between modes (no hard cutoff at boundary)
//    - "Set reference" allows zeroing at any angle
// ══════════════════════════════════════════════════════════════════════

@Composable
fun LevelToolScreen(
    viewModel: FieldMindViewModel? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = FieldMindTheme.colors
    val snackbar = remember { SnackbarHostState() }

    // ── Raw gravity state (low-pass filtered) ──
    var gx by remember { mutableFloatStateOf(0f) }
    var gy by remember { mutableFloatStateOf(-SensorManager.GRAVITY_EARTH) }
    var gz by remember { mutableFloatStateOf(0f) }

    // ── Reference state ──
    var isReferenced by remember { mutableStateOf(false) }
    var refGravX by remember { mutableFloatStateOf(0f) }
    var refGravY by remember { mutableFloatStateOf(0f) }
    var refGravZ by remember { mutableFloatStateOf(0f) }

    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    DisposableEffect(Unit) {
        val gravity = FloatArray(3)
        var first = true
        val alpha = 0.12f
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    if (first) {
                        gravity[0] = event.values[0]; gravity[1] = event.values[1]; gravity[2] = event.values[2]
                        first = false
                    } else {
                        gravity[0] = gravity[0] * (1 - alpha) + event.values[0] * alpha
                        gravity[1] = gravity[1] * (1 - alpha) + event.values[1] * alpha
                        gravity[2] = gravity[2] * (1 - alpha) + event.values[2] * alpha
                    }
                    gx = gravity[0]; gy = gravity[1]; gz = gravity[2]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, acc: Int) {}
        }
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // ── Compute gravity magnitude and derived values ──
    val gravMag by remember(gx, gy, gz) {
        derivedStateOf { sqrt(gx * gx + gy * gy + gz * gz).coerceAtLeast(0.01f) }
    }

    // How flat is the phone? 0 = perfectly vertical, 1 = perfectly flat (face up/down)
    val flatness by remember(gz, gravMag) {
        derivedStateOf { abs(gz) / gravMag }
    }

    // ── Tilt values for flat mode (pitch = forward/back, roll = left/right) ──
    // Uses atan2 with gz as reference — works for both face-up and face-down
    val flatPitch: Float by remember(gx, gz, gravMag) {
        derivedStateOf {
            Math.toDegrees(atan2(gx.toDouble(), abs(gz).coerceAtLeast(0.01f).toDouble())).toFloat()
        }
    }
    val flatRoll: Float by remember(gy, gz, gravMag) {
        derivedStateOf {
            Math.toDegrees(atan2(gy.toDouble(), abs(gz).coerceAtLeast(0.01f).toDouble())).toFloat()
        }
    }

    // ── Tilt values for vertical mode (tilt angle + direction) ──
    // tiltFromVertical: 0° = perfectly plumb, 90° = perfectly horizontal
    val tiltFromVertical: Float by remember(gx, gy, gz, gravMag) {
        derivedStateOf {
            val horizMag = sqrt(gx * gx + gy * gy)
            Math.toDegrees(atan2(horizMag.toDouble(), abs(gz).coerceAtLeast(0.01f).toDouble())).toFloat()
        }
    }
    // tiltDirection: 0° = leaning right (+X), 90° = leaning down (+Y), 180° = leaning left (-X), etc.
    val tiltDirection: Float by remember(gx, gy) {
        derivedStateOf {
            Math.toDegrees(atan2(gy.toDouble(), gx.toDouble())).toFloat()
        }
    }
    // tiltMagnitudeXY: the magnitude of the XY gravity vector (0 = no lean, 1G = fully horizontal)
    val tiltMagnitudeXY by remember(gx, gy, gravMag) {
        derivedStateOf { sqrt(gx * gx + gy * gy) / gravMag }
    }

    // ── Smooth animation ──
    val smoothFlatPitch by animateFloatAsState(flatPitch, animationSpec = tween(100), label = "flatPitch")
    val smoothFlatRoll by animateFloatAsState(flatRoll, animationSpec = tween(100), label = "flatRoll")
    val smoothTiltFromVertical by animateFloatAsState(tiltFromVertical, animationSpec = tween(100), label = "tiltFromVert")
    val smoothTiltDirection by animateFloatAsState(tiltDirection, animationSpec = tween(100), label = "tiltDir")
    val smoothFlatness by animateFloatAsState(flatness, animationSpec = tween(200), label = "flatness")

    // ── Reference offset — store gravity vector at reference point ──
    val refApplied by remember(isReferenced, refGravX, refGravY, refGravZ, gx, gy, gz, gravMag) {
        derivedStateOf {
            if (isReferenced) {
                // The "effective" gravity is the difference from reference
                // This way 'level' means the phone is at the same orientation as when reference was set
                val dot = (gx * refGravX + gy * refGravY + gz * refGravZ) / (gravMag * sqrt(refGravX * refGravX + refGravY * refGravY + refGravZ * refGravZ))
                val angleFromRef = Math.toDegrees(acos(dot.coerceIn(-1f, 1f).toDouble())).toFloat()
                angleFromRef
            } else 0f
        }
    }

    // ── Mode transition: smoothly blend between flat and vertical display ──
    val isFlatMode by remember(smoothFlatness) {
        derivedStateOf { smoothFlatness > 0.35f }
    }

    val isLevel by remember(isFlatMode, smoothFlatPitch, smoothFlatRoll, smoothTiltFromVertical, isReferenced, refApplied) {
        derivedStateOf {
            if (isReferenced) refApplied < 2f
            else if (isFlatMode) abs(smoothFlatPitch) < 2f && abs(smoothFlatRoll) < 2f
            else smoothTiltFromVertical < 2f
        }
    }

    // ── Haptic feedback at ±1° ──
    val haptics = rememberFieldMindHaptics()
    val isHapticLevel by remember(isReferenced, refApplied, isFlatMode, smoothFlatPitch, smoothFlatRoll, smoothTiltFromVertical) {
        derivedStateOf {
            if (isReferenced) refApplied < 1f
            else if (isFlatMode) abs(smoothFlatPitch) < 1f && abs(smoothFlatRoll) < 1f
            else smoothTiltFromVertical < 1f
        }
    }
    var wasHapticLevel by remember { mutableStateOf(false) }
    LaunchedEffect(isHapticLevel) {
        if (isHapticLevel && !wasHapticLevel) haptics.confirm()
        wasHapticLevel = isHapticLevel
    }

    // ── Haptic on degree ring crossings (vertical mode only) ──
    // The degree rings in VerticalTiltIndicator are drawn at ratios 0.11/0.22/0.44 of outerRadius.
    // Dot position = tiltXY * maxRadius where maxRadius = outerRadius * 0.55f
    // So dot crosses a ring when tiltXY * 0.55 = ringRatio → tiltXY = ringRatio / 0.55
    val ringThresholds = remember { listOf(0.11f / 0.55f, 0.22f / 0.55f, 0.44f / 0.55f) }
    var prevTiltXY by remember { mutableFloatStateOf(0f) }
    var ringHapticFired by remember { mutableStateOf(setOf<Float>()) }

    // Reset tracking when switching between flat and vertical mode
    LaunchedEffect(isFlatMode) {
        if (!isFlatMode) {
            prevTiltXY = tiltMagnitudeXY
            ringHapticFired = emptySet()
        }
    }

    LaunchedEffect(tiltMagnitudeXY) {
        if (!isFlatMode) {
            ringThresholds.forEach { threshold ->
                val crossedOutward = prevTiltXY < threshold && tiltMagnitudeXY >= threshold
                val crossedInward = prevTiltXY > threshold && tiltMagnitudeXY <= threshold
                if ((crossedOutward || crossedInward) && threshold !in ringHapticFired) {
                    haptics.light()
                    ringHapticFired = ringHapticFired + threshold
                }
                // Re-arm the flag when the tilt moves far enough away from the threshold
                // Use prevTiltXY (pre-crossing value) so the distance is measured from the side
                // the tilt was on before crossing, avoiding a stuck flag on inward re-entry.
                if (abs(prevTiltXY - threshold) > 0.03f) {
                    ringHapticFired = ringHapticFired - threshold
                }
            }
            prevTiltXY = tiltMagnitudeXY
        }
    }

    val modeLabel = if (isFlatMode) "Surface level — place device flat"
        else "Plumb — check vertical alignment"

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

            // ── Level display card ──
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
                    if (isFlatMode) {
                        // ── Flat surface mode: circular bubble level ──
                        CircularBubbleLevel(
                            pitch = smoothFlatPitch,
                            roll = smoothFlatRoll,
                            isLevel = isLevel,
                            colors = colors
                        )
                    } else {
                        // ── Vertical/plumb mode: 2D tilt dot (works in ALL orientations) ──
                        VerticalTiltIndicator(
                            tiltXY = tiltMagnitudeXY.toDouble().coerceIn(0.0, 1.0),
                            tiltAngleDeg = smoothTiltDirection,
                            isLevel = isLevel,
                            colors = colors
                        )
                    }

                    if (isLevel) {
                        Surface(shape = RoundedCornerShape(24.dp), color = colors.positive.copy(alpha = 0.15f)) {
                            Row(Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(MaterialSymbolIcon("check_circle", filled = true), null, tint = colors.positive, size = 24.dp)
                                Text("Level!", fontWeight = FontWeight.Bold, color = colors.positive, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }

            // ── Tilt data + reference controls ──
            Card(
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (isFlatMode) {
                        Text("Tilt angles", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            TiltGauge("Pitch", smoothFlatPitch, "Forward/backward", colors.info, Modifier.weight(1f))
                            TiltGauge("Roll", smoothFlatRoll, "Left/right", colors.data, Modifier.weight(1f))
                        }
                    } else {
                        Text("Plumb data", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            TiltGauge("From vertical", smoothTiltFromVertical, "Tilt magnitude", colors.info, Modifier.weight(1f))
                            TiltGauge("Direction", smoothTiltDirection, "Lean direction °", colors.data, Modifier.weight(1f))
                        }
                    }

                    // Reference controls
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isReferenced) {
                            Surface(shape = RoundedCornerShape(12.dp), color = colors.info.copy(alpha = 0.08f), modifier = Modifier.weight(1f)) {
                                Column(Modifier.padding(10.dp)) {
                                    Text("Reference set", style = MaterialTheme.typography.labelSmall, color = colors.info)
                                    Text("Deviation: %.1f°".format(refApplied), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Button(
                                onClick = { isReferenced = false },
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
                                    refGravX = gx; refGravY = gy; refGravZ = gz
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
                        if (isReferenced) "Deviations shown relative to set reference orientation."
                        else "Place device on a surface or against a wall. Use 'Set reference' to zero at any angle.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        FieldMindSnackbarOverlay(hostState = snackbar, modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp, start = 16.dp, end = 16.dp))
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Level display composables
// ══════════════════════════════════════════════════════════════════════

/**
 * Circular bubble level for flat (horizontal) surface mode.
 * Shows tilt in X (forward/back) and Y (left/right) axes.
 */
@Composable
private fun CircularBubbleLevel(
    pitch: Float, roll: Float, isLevel: Boolean, colors: fieldmind.research.app.features.field.presentation.theme.FieldMindColors
) {
    Box(modifier = Modifier.size(260.dp), contentAlignment = Alignment.Center) {
        val sh = MaterialTheme.colorScheme.surfaceContainerHighest
        val ov = MaterialTheme.colorScheme.outlineVariant
        val osv = MaterialTheme.colorScheme.onSurfaceVariant
        val accent = if (isLevel) colors.positive else colors.info

        // ── Pulsing outer glow ring ──
        val infiniteTransition = rememberInfiniteTransition(label = "levelGlow")
        val glowPulse by infiniteTransition.animateFloat(
            initialValue = 0.6f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
            label = "levelGlowPulse"
        )
        val glowColor = accent.copy(alpha = glowPulse * 0.3f)

        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f; val cy = size.height / 2f
            val outerRadius = minOf(cx, cy) * 0.95f
            val bubbleRadius = outerRadius * 0.12f

            // ── Animated gradient glow ring ──
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        glowColor, glowColor.copy(alpha = 0.05f),
                        glowColor, glowColor.copy(alpha = 0.05f),
                        glowColor
                    )
                ),
                radius = outerRadius + 14f, center = Offset(cx, cy)
            )

            // ── Outer ring ──
            drawCircle(color = sh, radius = outerRadius, center = Offset(cx, cy))
            drawCircle(color = ov.copy(alpha = 0.25f), radius = outerRadius, center = Offset(cx, cy), style = Stroke(width = 2f))

            // ── Reference circles ──
            drawCircle(color = ov.copy(alpha = 0.10f), radius = outerRadius * 0.6f, center = Offset(cx, cy),
                style = Stroke(width = 1f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f))))

            // ── Crosshair with glow ──
            drawLine(osv.copy(alpha = 0.18f), Offset(cx, cy - outerRadius * 0.85f), Offset(cx, cy + outerRadius * 0.85f), 2f)
            drawLine(osv.copy(alpha = 0.18f), Offset(cx - outerRadius * 0.85f, cy), Offset(cx + outerRadius * 0.85f, cy), 2f)

            // ── Center reference ──
            drawCircle(color = osv.copy(alpha = 0.3f), radius = 3f, center = Offset(cx, cy))

            // ── Bubble ──
            val maxTilt = 45f; val sensitivity = 0.88f
            val bubbleX = cx + (roll.coerceIn(-maxTilt, maxTilt) / maxTilt * outerRadius * sensitivity)
            val bubbleY = cy + (pitch.coerceIn(-maxTilt, maxTilt) / maxTilt * outerRadius * sensitivity)

            val bubbleColor = accent
            // Glow aura
            drawCircle(color = bubbleColor.copy(alpha = 0.08f), radius = bubbleRadius * 2.5f, center = Offset(bubbleX, bubbleY))
            // Bubble body
            drawCircle(color = bubbleColor, radius = bubbleRadius, center = Offset(bubbleX, bubbleY))
            // Specular highlight
            drawCircle(color = Color.White.copy(alpha = 0.35f), radius = bubbleRadius * 0.4f,
                center = Offset(bubbleX - bubbleRadius * 0.2f, bubbleY - bubbleRadius * 0.2f))
        }
    }
}

/**
 * 2D tilt indicator for vertical/plumb mode.
 * Shows the direction and magnitude of tilt using a dot on crosshairs.
 * Works in ALL orientations — portrait, landscape, and everything between.
 * The dot moves away from center in the direction of the lean.
 */
@Composable
private fun VerticalTiltIndicator(
    tiltXY: Double,      // 0.0 = no lean, 1.0 = fully horizontal
    tiltAngleDeg: Float, // degrees, direction of lean in XY plane
    isLevel: Boolean,
    colors: fieldmind.research.app.features.field.presentation.theme.FieldMindColors
) {
    Box(modifier = Modifier.size(260.dp), contentAlignment = Alignment.Center) {
        val sh = MaterialTheme.colorScheme.surfaceContainerHighest
        val ov = MaterialTheme.colorScheme.outlineVariant
        val osv = MaterialTheme.colorScheme.onSurfaceVariant
        val accent = if (isLevel) colors.positive else colors.info

        // ── Pulsing outer glow ring ──
        val infiniteTransition = rememberInfiniteTransition(label = "tiltGlow")
        val glowPulse by infiniteTransition.animateFloat(
            initialValue = 0.6f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
            label = "tiltGlowPulse"
        )
        val glowColor = accent.copy(alpha = glowPulse * 0.3f)

        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f; val cy = size.height / 2f
            val outerRadius = minOf(cx, cy) * 0.95f
            val dotRadius = outerRadius * 0.10f

            // ── Animated gradient glow ring ──
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        glowColor, glowColor.copy(alpha = 0.05f),
                        glowColor, glowColor.copy(alpha = 0.05f),
                        glowColor
                    )
                ),
                radius = outerRadius + 14f, center = Offset(cx, cy)
            )

            // ── Outer ring ──
            drawCircle(color = sh, radius = outerRadius, center = Offset(cx, cy))
            drawCircle(color = ov.copy(alpha = 0.25f), radius = outerRadius, center = Offset(cx, cy), style = Stroke(width = 2f))

            // ── Reference circles (dashed at 50% tilt) ──
            drawCircle(color = ov.copy(alpha = 0.10f), radius = outerRadius * 0.6f, center = Offset(cx, cy),
                style = Stroke(width = 1f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f))))

            // ── Degree rings at 5°, 10°, 20° ──
            listOf(0.11, 0.22, 0.44).forEach { ratio ->
                drawCircle(color = ov.copy(alpha = 0.08f), radius = outerRadius * ratio.toFloat(), center = Offset(cx, cy), style = Stroke(width = 1f))
            }

            // ── Crosshair with glow ──
            drawLine(osv.copy(alpha = 0.18f), Offset(cx, cy - outerRadius * 0.85f), Offset(cx, cy + outerRadius * 0.85f), 2f)
            drawLine(osv.copy(alpha = 0.18f), Offset(cx - outerRadius * 0.85f, cy), Offset(cx + outerRadius * 0.85f, cy), 2f)

            // ── Directional tick marks (4-way) ──
            listOf(0f, 90f, 180f, 270f).forEach { angleDeg ->
                val rad = Math.toRadians(angleDeg.toDouble())
                val inner = outerRadius * 0.85f
                val outer = outerRadius * 0.75f
                drawLine(
                    color = osv.copy(alpha = 0.12f),
                    start = Offset(cx + inner * cos(rad).toFloat(), cy + inner * sin(rad).toFloat()),
                    end = Offset(cx + outer * cos(rad).toFloat(), cy + outer * sin(rad).toFloat()),
                    strokeWidth = 1.5f
                )
            }

            // ── Center reference ──
            drawCircle(color = osv.copy(alpha = 0.3f), radius = 2.5f, center = Offset(cx, cy))

            // ── Tilt dot ──
            val maxRadius = outerRadius * 0.55f
            val tiltRad = Math.toRadians(tiltAngleDeg.toDouble())
            val dotDist = (tiltXY * maxRadius).toFloat()
            // atan2(gy, gx): 0°=right, 90°=forward/up-in-phone=down-in-canvas
            val dotX = (cx + dotDist * cos(tiltRad)).toFloat()
            val dotY = (cy + dotDist * sin(tiltRad)).toFloat()

            val dotColor = accent
            // Glow aura
            drawCircle(color = dotColor.copy(alpha = 0.08f), radius = dotRadius * 2.5f, center = Offset(dotX, dotY))
            // Dot body
            drawCircle(color = dotColor, radius = dotRadius, center = Offset(dotX, dotY))
            // Specular highlight
            drawCircle(color = Color.White.copy(alpha = 0.35f), radius = dotRadius * 0.4f,
                center = Offset(dotX - dotRadius * 0.2f, dotY - dotRadius * 0.2f))
        }
    }
}

@Composable
private fun TiltGauge(label: String, degrees: Float, description: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("%.1f°".format(degrees), style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 32.sp), color = if (abs(degrees) < 2f) FieldMindTheme.colors.positive else color)
        Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


