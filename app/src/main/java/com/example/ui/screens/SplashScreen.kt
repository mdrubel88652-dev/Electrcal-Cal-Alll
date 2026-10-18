package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay

const val SPLASH_DURATION_MS = 2800L

@Composable
fun SplashScreen(
    durationMs: Long = SPLASH_DURATION_MS,
    onLoadingFinished: () -> Unit
) {
    val progressAnim = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "BulbGlow")
    val bulbPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BulbPulse"
    )

    LaunchedEffect(Unit) {
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = (durationMs - 200).toInt(), easing = LinearEasing)
        )
        delay(150)
        onLoadingFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF6FAFF),
                        Color(0xFFEBF3FE),
                        Color(0xFFF3F8FF),
                        Color(0xFFF8FAFC)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("splash_screen_root")
    ) {
        // Subtle decorative background cloud/wave accents
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Top-right soft ambient aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFDCEAFE).copy(alpha = 0.45f), Color.Transparent),
                    center = Offset(w * 0.9f, h * 0.12f),
                    radius = w * 0.45f
                ),
                radius = w * 0.45f,
                center = Offset(w * 0.9f, h * 0.12f)
            )

            // Bottom-left soft ambient aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE0EDFD).copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(w * 0.15f, h * 0.88f),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f,
                center = Offset(w * 0.15f, h * 0.88f)
            )
        }

        // Main Splash Content Column (faithful reproduction of uploaded image)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. TOP SECTION: App Icon + Typography
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top App Logo Icon (Dark blue rounded square with glowing lightning)
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .shadow(8.dp, RoundedCornerShape(22.dp), spotColor = Color(0x330284C7))
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0F2B5C), Color(0xFF081836))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "App Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ELECTRICAL
                Text(
                    text = "ELECTRICAL",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    color = Color(0xFF0F265C),
                    textAlign = TextAlign.Center
                )

                // CALCULATION ALL
                Text(
                    text = "CALCULATION ALL",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp,
                    color = Color(0xFF1366D9),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle: Electrical Engineering Calculation & Estimation
                Text(
                    text = "Electrical Engineering\nCalculation & Estimation",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E3A8A),
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )
            }

            // 2. CENTER SECTION: Transmission Tower + Glowing Light Bulb + Calculator + Waves
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(230.dp)
                ) {
                    val cw = size.width
                    val ch = size.height

                    // ----------------------------------------------------
                    // Background Cityscape / Grid Silhouette (subtle translucent blue)
                    // ----------------------------------------------------
                    val cityColor = Color(0xFFBAE6FD).copy(alpha = 0.40f)
                    val cityPath = Path().apply {
                        moveTo(0f, ch * 0.62f)
                        lineTo(cw * 0.08f, ch * 0.62f)
                        lineTo(cw * 0.08f, ch * 0.52f)
                        lineTo(cw * 0.14f, ch * 0.52f)
                        lineTo(cw * 0.14f, ch * 0.58f)
                        lineTo(cw * 0.22f, ch * 0.58f)
                        lineTo(cw * 0.22f, ch * 0.48f)
                        lineTo(cw * 0.28f, ch * 0.48f)
                        lineTo(cw * 0.28f, ch * 0.62f)
                        lineTo(cw * 0.70f, ch * 0.62f)
                        lineTo(cw * 0.70f, ch * 0.53f)
                        lineTo(cw * 0.78f, ch * 0.53f)
                        lineTo(cw * 0.78f, ch * 0.59f)
                        lineTo(cw * 0.88f, ch * 0.59f)
                        lineTo(cw * 0.88f, ch * 0.50f)
                        lineTo(cw * 0.95f, ch * 0.50f)
                        lineTo(cw * 0.95f, ch * 0.62f)
                        lineTo(cw, ch * 0.62f)
                        lineTo(cw, ch)
                        lineTo(0f, ch)
                        close()
                    }
                    drawPath(cityPath, cityColor)

                    // ----------------------------------------------------
                    // A. Transmission Tower / Power Pylon (Left)
                    // ----------------------------------------------------
                    val pylonX = cw * 0.28f
                    val pylonTopY = ch * 0.08f
                    val pylonBaseY = ch * 0.68f
                    val pylonColor = Color(0xFF1E3A8A)
                    val pylonStroke = 2.4f

                    // Legs
                    drawLine(pylonColor, Offset(pylonX, pylonTopY), Offset(pylonX - 42f, pylonBaseY), strokeWidth = pylonStroke)
                    drawLine(pylonColor, Offset(pylonX, pylonTopY), Offset(pylonX + 42f, pylonBaseY), strokeWidth = pylonStroke)

                    // Cross-arm 1 (Top)
                    drawLine(pylonColor, Offset(pylonX - 32f, ch * 0.20f), Offset(pylonX + 32f, ch * 0.20f), strokeWidth = pylonStroke)
                    // Cross-arm 2 (Middle)
                    drawLine(pylonColor, Offset(pylonX - 44f, ch * 0.32f), Offset(pylonX + 44f, ch * 0.32f), strokeWidth = pylonStroke)
                    // Cross-arm 3 (Lower)
                    drawLine(pylonColor, Offset(pylonX - 36f, ch * 0.44f), Offset(pylonX + 36f, ch * 0.44f), strokeWidth = pylonStroke)

                    // Lattice trusses (X bracing)
                    drawLine(pylonColor, Offset(pylonX - 12f, ch * 0.20f), Offset(pylonX + 18f, ch * 0.32f), strokeWidth = 1.5f)
                    drawLine(pylonColor, Offset(pylonX + 12f, ch * 0.20f), Offset(pylonX - 18f, ch * 0.32f), strokeWidth = 1.5f)
                    drawLine(pylonColor, Offset(pylonX - 18f, ch * 0.32f), Offset(pylonX + 24f, ch * 0.44f), strokeWidth = 1.5f)
                    drawLine(pylonColor, Offset(pylonX + 18f, ch * 0.32f), Offset(pylonX - 24f, ch * 0.44f), strokeWidth = 1.5f)
                    drawLine(pylonColor, Offset(pylonX - 24f, ch * 0.44f), Offset(pylonX + 32f, ch * 0.56f), strokeWidth = 1.5f)
                    drawLine(pylonColor, Offset(pylonX + 24f, ch * 0.44f), Offset(pylonX - 32f, ch * 0.56f), strokeWidth = 1.5f)

                    // Top spire & insulators
                    drawLine(pylonColor, Offset(pylonX, pylonTopY - 14f), Offset(pylonX, pylonTopY), strokeWidth = pylonStroke)
                    drawCircle(Color(0xFF0284C7), radius = 3.5f, center = Offset(pylonX - 44f, ch * 0.32f))
                    drawCircle(Color(0xFF0284C7), radius = 3.5f, center = Offset(pylonX + 44f, ch * 0.32f))

                    // ----------------------------------------------------
                    // B. Glowing Incandescent Light Bulb (Center)
                    // ----------------------------------------------------
                    val bulbCenterX = cw * 0.54f
                    val bulbCenterY = ch * 0.24f
                    val bulbRadius = 38f * bulbPulse

                    // Glowing Aura
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD54F).copy(alpha = 0.55f),
                                Color(0xFFFFEE58).copy(alpha = 0.28f),
                                Color.Transparent
                            ),
                            center = Offset(bulbCenterX, bulbCenterY),
                            radius = bulbRadius * 1.7f
                        ),
                        radius = bulbRadius * 1.7f,
                        center = Offset(bulbCenterX, bulbCenterY)
                    )

                    // Glass Bulb Body (Radiant Amber-Yellow)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFCA28), Color(0xFFFFA000)),
                            center = Offset(bulbCenterX - 8f, bulbCenterY - 8f),
                            radius = bulbRadius
                        ),
                        radius = bulbRadius,
                        center = Offset(bulbCenterX, bulbCenterY)
                    )

                    // Filament (Warm highlight)
                    val filamentPath = Path().apply {
                        moveTo(bulbCenterX - 10f, bulbCenterY + 12f)
                        lineTo(bulbCenterX - 6f, bulbCenterY - 10f)
                        lineTo(bulbCenterX, bulbCenterY - 4f)
                        lineTo(bulbCenterX + 6f, bulbCenterY - 10f)
                        lineTo(bulbCenterX + 10f, bulbCenterY + 12f)
                    }
                    drawPath(
                        path = filamentPath,
                        color = Color.White,
                        style = Stroke(width = 2.4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Bulb Neck & Metallic Screw Base
                    val neckY = bulbCenterY + bulbRadius * 0.78f
                    drawRoundRect(
                        color = Color(0xFF64748B),
                        topLeft = Offset(bulbCenterX - 14f, neckY),
                        size = Size(28f, 24f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    // Base contact
                    drawRoundRect(
                        color = Color(0xFF334155),
                        topLeft = Offset(bulbCenterX - 11f, neckY + 18f),
                        size = Size(22f, 10f),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                    // Thread ridges
                    drawLine(Color(0xFF94A3B8), Offset(bulbCenterX - 14f, neckY + 6f), Offset(bulbCenterX + 14f, neckY + 6f), strokeWidth = 1.8f)
                    drawLine(Color(0xFF94A3B8), Offset(bulbCenterX - 14f, neckY + 12f), Offset(bulbCenterX + 14f, neckY + 12f), strokeWidth = 1.8f)

                    // ----------------------------------------------------
                    // C. Modern Engineering Calculator (Right)
                    // ----------------------------------------------------
                    val calcLeft = cw * 0.69f
                    val calcTop = ch * 0.28f
                    val calcW = 62f
                    val calcH = 88f

                    // Calculator Body (Dark Slate Blue)
                    drawRoundRect(
                        color = Color(0xFF1E3A8A),
                        topLeft = Offset(calcLeft, calcTop),
                        size = Size(calcW, calcH),
                        cornerRadius = CornerRadius(10f, 10f)
                    )

                    // Display Screen (Light Sky Blue / Cyan)
                    drawRoundRect(
                        color = Color(0xFFE0F2FE),
                        topLeft = Offset(calcLeft + 7f, calcTop + 8f),
                        size = Size(calcW - 14f, 18f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    // Display readout mock
                    drawLine(
                        color = Color(0xFF0369A1),
                        start = Offset(calcLeft + 12f, calcTop + 17f),
                        end = Offset(calcLeft + calcW - 14f, calcTop + 17f),
                        strokeWidth = 2.4f
                    )

                    // Button grid (3 cols x 4 rows)
                    val btnW = 11f
                    val btnH = 9f
                    val btnGapX = 4f
                    val btnGapY = 4.5f
                    val btnStartX = calcLeft + 8f
                    val btnStartY = calcTop + 32f

                    for (r in 0..3) {
                        for (c in 0..2) {
                            val bx = btnStartX + c * (btnW + btnGapX)
                            val by = btnStartY + r * (btnH + btnGapY)
                            val btnColor = if (r == 3 && c == 2) Color(0xFF0284C7) else Color(0xFF3B82F6)
                            drawRoundRect(
                                color = btnColor,
                                topLeft = Offset(bx, by),
                                size = Size(btnW, btnH),
                                cornerRadius = CornerRadius(2.5f, 2.5f)
                            )
                        }
                    }

                    // ----------------------------------------------------
                    // D. Flowing Cyan-Blue & Golden Energy Wave Swooshes
                    // ----------------------------------------------------
                    // Upper Electric Blue Swoosh
                    val blueWavePath = Path().apply {
                        moveTo(cw * 0.08f, ch * 0.68f)
                        cubicTo(
                            cw * 0.28f, ch * 0.60f,
                            cw * 0.45f, ch * 0.72f,
                            cw * 0.78f, ch * 0.62f
                        )
                        cubicTo(
                            cw * 0.60f, ch * 0.76f,
                            cw * 0.35f, ch * 0.72f,
                            cw * 0.08f, ch * 0.68f
                        )
                        close()
                    }
                    drawPath(
                        path = blueWavePath,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF0284C7), Color(0xFF0EA5E9), Color(0xFF38BDF8))
                        )
                    )

                    // Lower Golden Yellow Sweeping Wave
                    val yellowWavePath = Path().apply {
                        moveTo(cw * 0.22f, ch * 0.69f)
                        cubicTo(
                            cw * 0.50f, ch * 0.68f,
                            cw * 0.76f, ch * 0.64f,
                            cw * 0.94f, ch * 0.56f
                        )
                        cubicTo(
                            cw * 0.88f, ch * 0.74f,
                            cw * 0.58f, ch * 0.78f,
                            cw * 0.22f, ch * 0.69f
                        )
                        close()
                    }
                    drawPath(
                        path = yellowWavePath,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFF59E0B), Color(0xFFFBBF24), Color(0xFFFDE047))
                        )
                    )
                }
            }

            // 3. BOTTOM SECTION: Smooth Loading Bar + Slogans
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                // Sleek Pill-shaped Progress Bar (faithful to screenshot)
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFD6E4F9))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressAnim.value)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF1366D9), Color(0xFF0284C7))
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Smart Calculation • Better Design
                Text(
                    text = "Smart Calculation  •  Better Design",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF3B5998),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Safer Tomorrow
                Text(
                    text = "Safer Tomorrow",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF47639E),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
