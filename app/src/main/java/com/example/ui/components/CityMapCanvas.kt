package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CityLocation
import com.example.data.models.Driver
import com.example.data.models.DriverStatus
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun CityMapCanvas(
    modifier: Modifier = Modifier,
    pickupCoords: Pair<Float, Float>? = null,
    dropoffCoords: Pair<Float, Float>? = null,
    driverProgress: Float = 0f,
    activeDrivers: List<Driver> = emptyList(),
    locations: List<CityLocation> = emptyList(),
    selectedLocation: CityLocation? = null,
    onLocationTap: ((CityLocation) -> Unit)? = null,
    showAllDrivers: Boolean = false,
    mapLabel: String = "Metro Live Radar"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
            .testTag("city_map_canvas_container")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(locations) {
                    detectTapGestures { offset ->
                        if (onLocationTap != null && locations.isNotEmpty()) {
                            val w = size.width
                            val h = size.height
                            // Find nearest location within tap threshold
                            val tapped = locations.minByOrNull { loc ->
                                val lx = loc.coords.first * w
                                val ly = loc.coords.second * h
                                val dx = lx - offset.x
                                val dy = ly - offset.y
                                dx * dx + dy * dy
                            }
                            if (tapped != null) {
                                val lx = tapped.coords.first * w
                                val ly = tapped.coords.second * h
                                val dist = kotlin.math.sqrt(((lx - offset.x) * (lx - offset.x) + (ly - offset.y) * (ly - offset.y)).toDouble())
                                if (dist < 80.0) {
                                    onLocationTap(tapped)
                                }
                            }
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. Water Bay feature on the top-left/side
            val waterPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(w * 0.22f, 0f)
                cubicTo(w * 0.20f, h * 0.25f, w * 0.12f, h * 0.40f, 0f, h * 0.50f)
                close()
            }
            drawPath(waterPath, color = Color(0xFF0C4A6E))

            // 2. City Park feature on bottom right
            drawRoundRect(
                color = Color(0xFF064E3B),
                topLeft = Offset(w * 0.55f, h * 0.58f),
                size = Size(w * 0.35f, h * 0.30f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )

            // 3. Grid road network
            val roadColor = Color(0xFF1E293B)
            val majorAvenueColor = Color(0xFF334155)

            // Secondary Streets
            for (i in 1..8) {
                val y = h * (i / 9f)
                drawLine(
                    color = roadColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 3f
                )
                val x = w * (i / 9f)
                drawLine(
                    color = roadColor,
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 3f
                )
            }

            // Primary Arteries / Highways (High contrast)
            // Grand Avenue (horizontal diagonal)
            drawLine(
                color = majorAvenueColor,
                start = Offset(0f, h * 0.45f),
                end = Offset(w, h * 0.52f),
                strokeWidth = 7f
            )
            // Transit Express (vertical curve)
            val transitPath = Path().apply {
                moveTo(w * 0.48f, 0f)
                quadraticBezierTo(w * 0.52f, h * 0.5f, w * 0.65f, h)
            }
            drawPath(
                path = transitPath,
                color = majorAvenueColor,
                style = Stroke(width = 8f)
            )

            // 4. City Location landmarks (dots + subtle labels)
            locations.forEach { loc ->
                val lx = loc.coords.first * w
                val ly = loc.coords.second * h
                val isSelected = selectedLocation?.name == loc.name

                drawCircle(
                    color = if (isSelected) AmberPrimary else Color(0xFF64748B),
                    radius = if (isSelected) 7f else 4.5f,
                    center = Offset(lx, ly)
                )

                // Draw tiny label for landmark
                val textLayout = textMeasurer.measure(
                    text = loc.name.take(14),
                    style = TextStyle(
                        color = if (isSelected) Color(0xFFFDE68A) else Color(0xFF94A3B8),
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                )
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(lx + 7f, ly - 10f)
                )
            }

            // 5. Active Route Polyline (if pickup & dropoff provided)
            if (pickupCoords != null && dropoffCoords != null) {
                val px = pickupCoords.first * w
                val py = pickupCoords.second * h
                val dx = dropoffCoords.first * w
                val dy = dropoffCoords.second * h

                // Waypoint intermediate curve
                val midX = (px + dx) / 2f + (if (px < dx) 25f else -25f)
                val midY = (py + dy) / 2f

                val routePath = Path().apply {
                    moveTo(px, py)
                    quadraticBezierTo(midX, midY, dx, dy)
                }

                // Route halo glow
                drawPath(
                    path = routePath,
                    color = CyanAccent.copy(alpha = 0.25f),
                    style = Stroke(width = 12f)
                )

                // Route main line (dashed)
                drawPath(
                    path = routePath,
                    color = CyanAccent,
                    style = Stroke(
                        width = 4.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f), 0f)
                    )
                )

                // Draw Pickup Marker (Emerald)
                drawCircle(
                    color = EmeraldSuccess.copy(alpha = pulseAlpha),
                    radius = pulseRadius,
                    center = Offset(px, py)
                )
                drawCircle(color = EmeraldSuccess, radius = 7f, center = Offset(px, py))
                drawCircle(color = Color.White, radius = 3.5f, center = Offset(px, py))

                // Draw Dropoff Marker (Amber/Red)
                drawCircle(color = AmberDark, radius = 7f, center = Offset(dx, dy))
                drawCircle(color = Color.White, radius = 3.5f, center = Offset(dx, dy))

                // Draw Moving Rider / Bike Icon along progress
                val riderProg = driverProgress.coerceIn(0f, 1f)
                // Quadratic bezier formula: B(t) = (1-t)^2 P0 + 2(1-t)t P1 + t^2 P2
                val t = riderProg
                val u = 1f - t
                val riderX = u * u * px + 2 * u * t * midX + t * t * dx
                val riderY = u * u * py + 2 * u * t * midY + t * t * dy

                // Rider outer ring
                drawCircle(
                    color = AmberPrimary,
                    radius = 9f,
                    center = Offset(riderX, riderY)
                )
                drawCircle(
                    color = Color.Black,
                    radius = 5.5f,
                    center = Offset(riderX, riderY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5f,
                    center = Offset(riderX, riderY)
                )
            }

            // 6. Manager Fleet Overview (All active drivers on the map)
            if (showAllDrivers) {
                activeDrivers.forEach { driver ->
                    val bx = driver.coords.first * w
                    val by = driver.coords.second * h
                    val driverColor = when {
                        !driver.isOnline -> Color(0xFF64748B)
                        driver.currentStatus == DriverStatus.AVAILABLE -> EmeraldSuccess
                        driver.currentStatus == DriverStatus.ON_TRIP -> AmberPrimary
                        else -> CyanAccent
                    }

                    // Pulsing ring if available
                    if (driver.isOnline && driver.currentStatus == DriverStatus.AVAILABLE) {
                        drawCircle(
                            color = driverColor.copy(alpha = pulseAlpha * 0.6f),
                            radius = pulseRadius * 0.8f,
                            center = Offset(bx, by)
                        )
                    }

                    // Driver indicator badge
                    drawCircle(color = driverColor, radius = 6.5f, center = Offset(bx, by))
                    drawCircle(color = Color.White, radius = 2.5f, center = Offset(bx, by))

                    // Small text plate
                    val plateLayout = textMeasurer.measure(
                        text = "${driver.name.take(5)} (${driver.licensePlate})",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    drawText(plateLayout, topLeft = Offset(bx + 8f, by - 6f))
                }
            }
        }

        // Overlay status tag & GPS pill
        Surface(
            color = Slate900.copy(alpha = 0.85f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.TopStart)
        ) {
            Text(
                text = "📍 $mapLabel",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        if (locations.isNotEmpty() && onLocationTap != null) {
            Surface(
                color = Slate800.copy(alpha = 0.90f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Text(
                    text = "Tap any landmark to set",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
