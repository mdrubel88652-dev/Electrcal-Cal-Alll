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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay

/**
 * Modern High-Tech Cinematic Startup Duration
 */
const val SPLASH_DURATION_MS = 3600L

@Composable
fun SplashScreen(
    durationMs: Long = SPLASH_DURATION_MS,
    onLoadingFinished: () -> Unit
) {
    val progressAnim = remember { Animatable(0f) }

    // Infinite transitions for dynamic high-tech animations
    val infiniteTransition = rememberInfiniteTransition(label = "SplashAnimations")

    // Breathing pulse for emblem
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    // Outer cyber ring rotation (clockwise)
    val ringRotationClockwise by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ClockwiseRing"
    )

    // Inner cyber ring rotation (counter-clockwise)
    val ringRotationCounterClockwise by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CounterClockwiseRing"
    )

    // Glowing LED pulse
    val ledAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LedBlink"
    )

    // Ambient radial aura glow
    val ambientAlpha by infiniteTransition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AmbientAlpha"
    )

    LaunchedEffect(Unit) {
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = (durationMs - 350).toInt(), easing = LinearEasing)
        )
        delay(300)
        onLoadingFinished()
    }

    val progressVal = progressAnim.value
    val percentInt = (progressVal * 100).toInt().coerceIn(0, 100)

    val currentStepText = when {
        progressVal < 0.20f -> "INITIALIZING CORE • ১৬৭টি ইলেকট্রিক্যাল ইঞ্জিন সক্রিয় হচ্ছে..."
        progressVal < 0.45f -> "LOADING ENGINES • ইন্ডাস্ট্রিয়াল ও ডোমেস্টিক ক্যালকুলেটর লোড..."
        progressVal < 0.70f -> "CALIBRATING STANDARDS • IEC 60364 ও IEEE 1561 ক্যালিব্রেশন..."
        progressVal < 0.90f -> "PHASE MATRIX • ১-ফেজ ও ৩-ফেজ আইসোলেশন প্রোটোকল..."
        else -> "SYSTEM READY • সকল ১৬৭টি প্রকৌশল ক্যালকুলেটর প্রস্তুত!"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF060B18),
                        Color(0xFF040813),
                        Color(0xFF02040A)
                    )
                )
            )
            .testTag("splash_screen_root")
    ) {
        // High-Tech Cyber Circuit Canvas Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Horizontal & vertical circuit grid lines
            val lineColor = Color(0xFF00E5FF).copy(alpha = 0.05f)
            val strokeWidth = 1.5f

            // Circuit Grid Lines
            val spacing = 80f
            var x = spacing
            while (x < width) {
                drawLine(
                    color = lineColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = strokeWidth
                )
                x += spacing
            }

            var y = spacing
            while (y < height) {
                drawLine(
                    color = lineColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = strokeWidth
                )
                y += spacing
            }

            // Circuit corner tech lines
            val cyanGlow = Color(0xFF00E5FF).copy(alpha = 0.18f)
            drawLine(
                color = cyanGlow,
                start = Offset(40f, 100f),
                end = Offset(140f, 100f),
                strokeWidth = 2f
            )
            drawLine(
                color = cyanGlow,
                start = Offset(140f, 100f),
                end = Offset(180f, 140f),
                strokeWidth = 2f
            )
            drawCircle(color = Color(0xFF00E5FF).copy(alpha = 0.3f), radius = 4f, center = Offset(180f, 140f))

            drawLine(
                color = cyanGlow,
                start = Offset(width - 40f, height - 120f),
                end = Offset(width - 140f, height - 120f),
                strokeWidth = 2f
            )
            drawLine(
                color = cyanGlow,
                start = Offset(width - 140f, height - 120f),
                end = Offset(width - 180f, height - 160f),
                strokeWidth = 2f
            )
            drawCircle(color = Color(0xFFFFC107).copy(alpha = 0.35f), radius = 4f, center = Offset(width - 180f, height - 160f))
        }

        // Background Electrical Graphic Art
        Image(
            painter = painterResource(id = R.drawable.img_splash_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .scale(1.05f),
            contentScale = ContentScale.Crop,
            alpha = 0.30f
        )

        // Radial Cyan Aura Glow behind emblem
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(360.dp)
                .scale(pulseScale)
                .blur(95.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = ambientAlpha * 0.45f),
                            Color(0xFF0284C7).copy(alpha = ambientAlpha * 0.22f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Main Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top App Bar Branding Pill
            Box(
                modifier = Modifier
                    .padding(top = 22.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0B162E).copy(alpha = 0.88f))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF10B981).copy(alpha = ledAlpha), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ELECTRICAL PRO SUITE v1.0 • 167 CALCS ONLINE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                }
            }

            // Center: Rotating Cybernetic Rings + Luxury Emblem + Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                // Interactive Reactor Emblem Box
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Clockwise Rotating Cyber Ring
                    Canvas(
                        modifier = Modifier
                            .size(196.dp)
                            .rotate(ringRotationClockwise)
                    ) {
                        val stroke = 2.dp.toPx()
                        drawArc(
                            color = Color(0xFF00E5FF).copy(alpha = 0.85f),
                            startAngle = 0f,
                            sweepAngle = 70f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color(0xFF0284C7).copy(alpha = 0.45f),
                            startAngle = 90f,
                            sweepAngle = 60f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color(0xFF00E5FF).copy(alpha = 0.85f),
                            startAngle = 180f,
                            sweepAngle = 70f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color(0xFF38BDF8).copy(alpha = 0.45f),
                            startAngle = 270f,
                            sweepAngle = 60f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }

                    // Inner Counter-Clockwise Rotating Amber/Cyan Cyber Ring
                    Canvas(
                        modifier = Modifier
                            .size(168.dp)
                            .rotate(ringRotationCounterClockwise)
                    ) {
                        val stroke = 1.8.dp.toPx()
                        drawArc(
                            color = Color(0xFFFFC107).copy(alpha = 0.75f),
                            startAngle = 30f,
                            sweepAngle = 50f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color(0xFF38BDF8).copy(alpha = 0.50f),
                            startAngle = 120f,
                            sweepAngle = 80f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color(0xFFFFC107).copy(alpha = 0.75f),
                            startAngle = 230f,
                            sweepAngle = 60f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }

                    // Luxury App Emblem
                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .scale(pulseScale)
                            .shadow(
                                elevation = 28.dp,
                                shape = RoundedCornerShape(32.dp),
                                ambientColor = Color(0xFF00E5FF),
                                spotColor = Color(0xFF00E5FF)
                            )
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color(0xFF0A1123))
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        Color(0xFF00E5FF),
                                        Color(0xFF38BDF8),
                                        Color(0xFF0284C7),
                                        Color(0xFF0A1123)
                                    )
                                ),
                                shape = RoundedCornerShape(32.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "ELECTRICAL CALCULATION ALL",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(30.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Typography
                Text(
                    text = "ELECTRICAL",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "CALCULATION ALL",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = Color(0xFFFFC107),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Electrical Engineering Calculation & Estimation",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )
            }

            // Bottom Section: Digital Substation Telemetry & Progress
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                // High-Tech Telemetry HUD Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0B162E).copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(Color(0xFF00E5FF).copy(alpha = ledAlpha), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SYS-STATUS: 400V 3φ / 230V 1φ",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        }

                        Text(
                            text = "50Hz • PF 0.96",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFC107)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Percentage & Live Diagnostics Label
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DIAGNOSTICS INITIALIZATION",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "$percentInt%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // High-Tech Neon Linear Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1E293B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progressVal)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF00E5FF),
                                        Color(0xFF38BDF8),
                                        Color(0xFF0284C7),
                                        Color(0xFFFFC107)
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dynamic Status Readout in Bengali & English
                Text(
                    text = currentStepText,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.3.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Micro Standards Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("IEC 60364", "IEEE 1561", "1φ / 3φ ISOLATED", "OFFLINE 100%").forEachIndexed { idx, label ->
                        if (idx > 0) {
                            Text(
                                text = "•",
                                color = Color(0xFF334155),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                        }
                        Text(
                            text = label,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}
