package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    var currentStepText by remember { mutableStateOf("INITIALIZING ELECTRICAL COMPUTATION CORE...") }

    // Breathing pulse transition for the emblem
    val infiniteTransition = rememberInfiniteTransition(label = "EmblemGlowTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val ambientAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AmbientAlpha"
    )

    LaunchedEffect(Unit) {
        // Animate progress smoothly from 0 to 1
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = (durationMs - 400).toInt(), easing = LinearEasing)
        )
        delay(300)
        onLoadingFinished()
    }

    // Dynamic system telemetry stage based on progress
    val progressVal = progressAnim.value
    val percentInt = (progressVal * 100).toInt().coerceIn(0, 100)

    currentStepText = when {
        progressVal < 0.20f -> "INITIALIZING HIGH-VOLTAGE CALCULATION ENGINE..."
        progressVal < 0.45f -> "LOADING 167 INDUSTRIAL & DOMESTIC FORMULA ENGINES..."
        progressVal < 0.70f -> "CALIBRATING IEC 60364 & IEEE 1561 STANDARDS MATRIX..."
        progressVal < 0.90f -> "CONFIGURING SINGLE-PHASE & THREE-PHASE SEPARATION..."
        else -> "SYSTEM CALIBRATED • READY FOR ENGINEERING CALCULATIONS"
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
        // Subtle Background Electrical Art if available
        Image(
            painter = painterResource(id = R.drawable.img_splash_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .scale(1.05f),
            contentScale = ContentScale.Crop,
            alpha = 0.40f
        )

        // Radial Cyan Glow behind emblem
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(340.dp)
                .scale(pulseScale)
                .blur(90.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = ambientAlpha * 0.45f),
                            Color(0xFF0284C7).copy(alpha = ambientAlpha * 0.20f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Central Content Column
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
                    .padding(top = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0B162E).copy(alpha = 0.85f))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "ELECTRICAL PRO SUITE v1.0 • 167 CALCS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                }
            }

            // Center: Minimalist Luxury Emblem + Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 20.dp)
            ) {
                // High-End Emblem Container
                Box(
                    modifier = Modifier
                        .size(144.dp)
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

                Spacer(modifier = Modifier.height(28.dp))

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

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Electrical Engineering Calculation & Estimation",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )
            }

            // Bottom Section: High-Tech Progress & Live Telemetry
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp)
            ) {
                // Percentage and Live Status Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INITIALIZING SYSTEM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF),
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "$percentInt%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
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
                                        Color(0xFF3B82F6),
                                        Color(0xFFFFC107)
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dynamic Status Readout
                Text(
                    text = currentStepText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Micro Standards Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("IEC 60364", "IEEE 1561", "1φ / 3φ ISOLATED", "OFFLINE READY").forEachIndexed { idx, label ->
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
