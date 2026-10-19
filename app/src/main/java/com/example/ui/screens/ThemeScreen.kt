package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ElectricalApp
import kotlinx.coroutines.launch

@Composable
fun ThemeScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = remember { ElectricalApp.getApp(context) }
    val currentMode by app.settingsManager.themeModeFlow.collectAsState(initial = "LIGHT")
    val currentAccent by app.settingsManager.accentColorFlow.collectAsState(initial = "#0284C7")

    var selectedMode by remember(currentMode) { mutableStateOf(currentMode) }
    var selectedAccent by remember(currentAccent) { mutableStateOf(currentAccent) }

    val accentColors = listOf(
        "#0284C7" to Color(0xFF0284C7), // Blue
        "#00E5FF" to Color(0xFF00E5FF), // Cyan
        "#10B981" to Color(0xFF10B981), // Green
        "#F59E0B" to Color(0xFFF59E0B), // Orange
        "#EC4899" to Color(0xFFEC4899), // Pink
        "#8B5CF6" to Color(0xFF8B5CF6)  // Purple
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("theme_screen_root"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Theme Mode Selection (Screen 11)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Option 1: System Default
                ThemeOptionRow(
                    title = "System Default",
                    subtitle = "Follow device theme",
                    isSelected = selectedMode == "SYSTEM",
                    onClick = { selectedMode = "SYSTEM" }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: Light / White
                ThemeOptionRow(
                    title = "Light / White",
                    subtitle = "Always light mode",
                    isSelected = selectedMode == "LIGHT",
                    onClick = { selectedMode = "LIGHT" }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Option 3: Dark
                ThemeOptionRow(
                    title = "Dark",
                    subtitle = "Always dark mode",
                    isSelected = selectedMode == "DARK",
                    onClick = { selectedMode = "DARK" }
                )
            }
        }

        // Section Accent Color
        Column {
            Text(
                text = "Accent Color",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                accentColors.forEach { (hex, color) ->
                    val isChosen = selectedAccent == hex
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { selectedAccent = hex }
                            .then(
                                if (isChosen) Modifier.border(3.dp, Color.White, CircleShape).border(4.dp, color, CircleShape)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChosen) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // Section Theme Previews (Screen 11 preview thumbnails)
        Column {
            Text(
                text = "Preview",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Light preview
                ThemePreviewCard(
                    modifier = Modifier.weight(1f),
                    title = "Light",
                    bgColor = Color(0xFFF8FAFC),
                    cardColor = Color.White,
                    headerColor = Color(0xFF0284C7),
                    textColor = Color(0xFF0F172A),
                    isSelected = selectedMode == "LIGHT",
                    onClick = { selectedMode = "LIGHT" }
                )

                // System preview
                ThemePreviewCard(
                    modifier = Modifier.weight(1f),
                    title = "System",
                    bgColor = Color(0xFFE2E8F0),
                    cardColor = Color.White,
                    headerColor = Color(0xFF1E3A8A),
                    textColor = Color(0xFF0F172A),
                    isSelected = selectedMode == "SYSTEM",
                    onClick = { selectedMode = "SYSTEM" }
                )

                // Dark preview
                ThemePreviewCard(
                    modifier = Modifier.weight(1f),
                    title = "Dark",
                    bgColor = Color(0xFF0F172A),
                    cardColor = Color(0xFF1E293B),
                    headerColor = Color(0xFF0A1123),
                    textColor = Color.White,
                    isSelected = selectedMode == "DARK",
                    onClick = { selectedMode = "DARK" }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Apply Button
        Button(
            onClick = {
                scope.launch {
                    app.settingsManager.updateThemeMode(selectedMode)
                    app.settingsManager.updateAccentColor(selectedAccent)
                    Toast.makeText(context, "থিম সফলভাবে প্রয়োগ করা হয়েছে", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("apply_theme_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
        ) {
            Text(text = "Apply", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(title, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0284C7))
        )
    }
}

@Composable
private fun ThemePreviewCard(
    modifier: Modifier = Modifier,
    title: String,
    bgColor: Color,
    cardColor: Color,
    headerColor: Color,
    textColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = RoundedCornerShape(10.dp),
            color = bgColor,
            border = androidx.compose.foundation.BorderStroke(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) Color(0xFF0284C7) else Color(0xFFCBD5E1)
            )
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                // Header block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(headerColor)
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Mini cards
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(cardColor)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(cardColor)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color(0xFF0284C7) else Color(0xFF64748B)
        )
    }
}
