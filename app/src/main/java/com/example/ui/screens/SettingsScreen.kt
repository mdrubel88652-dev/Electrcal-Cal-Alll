package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ElectricalApp
import com.example.data.datastore.CalculationDefaults
import com.example.data.datastore.PdfPrintSettings
import com.example.data.datastore.TechnicianReportProfile
import com.example.ui.theme.ElectricBluePrimary
import com.example.utils.ShareUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onNavigateToHistory: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToNotification: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToDeveloper: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToTechnicianProfile: () -> Unit = {},
    onNavigateToTheme: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToMaterialEstimate: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val app = ElectricalApp.getApp(context)

    val currentTheme by app.settingsManager.themeModeFlow.collectAsState(initial = "LIGHT")
    val pdfSettings by app.settingsManager.pdfPrintSettingsFlow.collectAsState(initial = PdfPrintSettings())
    val calcDefaults by app.settingsManager.calculationDefaultsFlow.collectAsState(initial = CalculationDefaults())
    val techProfile by app.settingsManager.technicianProfileFlow.collectAsState(initial = TechnicianReportProfile())
    val currentLanguage by app.settingsManager.languageFlow.collectAsState(initial = "BN")

    // State for local editing before Save
    var selectedStandard by remember(calcDefaults.standard) { mutableStateOf(calcDefaults.standard) }
    var selectedThemeMode by remember(currentTheme) { mutableStateOf(currentTheme) }
    var selectedLanguage by remember(currentLanguage) { mutableStateOf(currentLanguage) }

    var showLogo by remember(pdfSettings.showLogo) { mutableStateOf(pdfSettings.showLogo) }
    var showDevInfo by remember(pdfSettings.showDeveloperInfo) { mutableStateOf(pdfSettings.showDeveloperInfo) }
    var showDateTime by remember(pdfSettings.showDateTime) { mutableStateOf(pdfSettings.showDateTime) }
    var showFormula by remember(pdfSettings.showFormula) { mutableStateOf(pdfSettings.showFormula) }
    var showSteps by remember(pdfSettings.showSteps) { mutableStateOf(pdfSettings.showSteps) }
    var showNotes by remember(pdfSettings.showNotes) { mutableStateOf(pdfSettings.showNotes) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(14.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // 1. Profile & Account Section (Blue theme)
        // ==========================================
        item {
            SectionContainer(
                borderColor = Color(0xFF3B82F6),
                icon = Icons.Default.Person,
                iconTint = Color(0xFF2563EB),
                iconBg = Color(0xFFEFF6FF),
                title = "Profile & Account",
                subtitle = "Manage your profile and account information"
            ) {
                // Developer Profile item (Engineer Rubel)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigateToDeveloper() }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Engineer Rubel",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFE0F2FE),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Developer",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Industrial Electrical & Power Engineering",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "+880 16094-92193 • mdrubel88652@gmail.com",
                            fontSize = 10.5.sp,
                            color = Color(0xFF0284C7)
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Open",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }

                HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 6.dp))

                // Technician Report Profile item
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigateToTechnicianProfile() }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Technician Report Profile",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (techProfile.isNotEmpty()) "Technician: ${techProfile.name}"
                                   else "Configure technician profile for reports",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Configured / Empty Badge
                    Surface(
                        color = if (techProfile.isNotEmpty()) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (techProfile.isNotEmpty()) "Configured" else "Empty",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (techProfile.isNotEmpty()) Color(0xFF16A34A) else Color(0xFFD97706),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Open",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // ==========================================
        // 2. Records & Reports Section (Purple/Teal theme)
        // ==========================================
        item {
            SectionContainer(
                borderColor = Color(0xFF8B5CF6),
                icon = Icons.Default.Description,
                iconTint = Color(0xFF7C3AED),
                iconBg = Color(0xFFF5F3FF),
                title = "Reports & History Records",
                subtitle = "Manage calculations history, site reports & PDF exports"
            ) {
                // Calculation History
                SettingsItemRow(
                    icon = Icons.Default.History,
                    iconTint = Color(0xFF0284C7),
                    iconBg = Color(0xFFE0F2FE),
                    title = "Calculation History (হিসাবের ইতিহাস)",
                    subtitle = "পূর্বে হিসাবকৃত সকল ক্যালকুলেশন রেকর্ড ও শেয়ারিং",
                    onClick = onNavigateToHistory
                )

                HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 4.dp))

                // Reports & Documents
                SettingsItemRow(
                    icon = Icons.Default.Description,
                    iconTint = Color(0xFF7C3AED),
                    iconBg = Color(0xFFF5F3FF),
                    title = "Reports & Project Summary (রিপোর্ট ও সামারি)",
                    subtitle = "প্রজেক্ট রিপোর্ট, ম্যাটেরিয়াল লিস্ট ও টেকনিশিয়ান শিট PDF",
                    onClick = onNavigateToReports
                )
            }
        }

        // ==========================================
        // 3. Language & Localization Section
        // ==========================================
        item {
            SectionContainer(
                borderColor = Color(0xFF0284C7),
                icon = Icons.Default.Language,
                iconTint = Color(0xFF0369A1),
                iconBg = Color(0xFFE0F2FE),
                title = "Language (ভাষা নির্বাচন)",
                subtitle = "Choose app language / অ্যাপের ভাষা নির্বাচন করুন"
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Option 1: বাংলা
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedLanguage = "BN"
                                coroutineScope.launch {
                                    app.settingsManager.updateLanguage("BN")
                                    Toast.makeText(context, "ভাষা নির্বাচন করা হয়েছে: বাংলা", Toast.LENGTH_SHORT).show()
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedLanguage == "BN") Color(0xFFE0F2FE) else Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(
                            1.2.dp,
                            if (selectedLanguage == "BN") Color(0xFF0284C7) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == "BN",
                                onClick = {
                                    selectedLanguage = "BN"
                                    coroutineScope.launch {
                                        app.settingsManager.updateLanguage("BN")
                                        Toast.makeText(context, "ভাষা নির্বাচন করা হয়েছে: বাংলা", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0284C7)),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "বাংলা",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedLanguage == "BN") Color(0xFF0369A1) else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "ডিফল্ট",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    // Option 2: English
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedLanguage = "EN"
                                coroutineScope.launch {
                                    app.settingsManager.updateLanguage("EN")
                                    Toast.makeText(context, "Language selected: English", Toast.LENGTH_SHORT).show()
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedLanguage == "EN") Color(0xFFE0F2FE) else Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(
                            1.2.dp,
                            if (selectedLanguage == "EN") Color(0xFF0284C7) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == "EN",
                                onClick = {
                                    selectedLanguage = "EN"
                                    coroutineScope.launch {
                                        app.settingsManager.updateLanguage("EN")
                                        Toast.makeText(context, "Language selected: English", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0284C7)),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "English",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedLanguage == "EN") Color(0xFF0369A1) else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Global",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. Application Settings Section (Green theme)
        // ==========================================
        item {
            SectionContainer(
                borderColor = Color(0xFF10B981),
                icon = Icons.Default.Apps,
                iconTint = Color(0xFF059669),
                iconBg = Color(0xFFECFDF5),
                title = "Application Settings",
                subtitle = "App information, standards, units and general settings"
            ) {
                // About Application
                SettingsItemRow(
                    icon = Icons.Default.Info,
                    iconTint = Color(0xFF059669),
                    iconBg = Color(0xFFECFDF5),
                    title = "About Application",
                    subtitle = "Version, purpose, electrical standards & overview",
                    onClick = onNavigateToAbout
                )

                HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 4.dp))

                // Privacy Policy
                SettingsItemRow(
                    icon = Icons.Default.Shield,
                    iconTint = Color(0xFF059669),
                    iconBg = Color(0xFFECFDF5),
                    title = "Privacy Policy",
                    subtitle = "Read data security, offline usage & policy terms",
                    onClick = onNavigateToPrivacy
                )

                HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 4.dp))

                // Share Application
                SettingsItemRow(
                    icon = Icons.Default.Share,
                    iconTint = Color(0xFF059669),
                    iconBg = Color(0xFFECFDF5),
                    title = "Share Application",
                    subtitle = "Share with engineers and students",
                    onClick = {
                        ShareUtils.shareText(
                            context,
                            "Electrical Cal/Pro",
                            "Download Electrical Cal/Pro - The professional electrical engineering calculation & estimation app with 167 calculators!"
                        )
                    }
                )
            }
        }

        // ==========================================
        // 3. Electrical Standards Section (Purple theme)
        // ==========================================
        item {
            SectionContainer(
                borderColor = Color(0xFF8B5CF6),
                icon = Icons.Default.Settings,
                iconTint = Color(0xFF7C3AED),
                iconBg = Color(0xFFF5F3FF),
                title = "Electrical Standards",
                subtitle = "Set your preferred engineering standard"
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F3FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Select Default Standard",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "IEC | NEC | BNBC | General",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Radio buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("IEC", "NEC", "BNBC", "General").forEach { std ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { selectedStandard = std }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                        ) {
                            RadioButton(
                                selected = selectedStandard == std,
                                onClick = { selectedStandard = std },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF7C3AED),
                                    unselectedColor = Color(0xFF94A3B8)
                                ),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = std,
                                fontSize = 12.sp,
                                fontWeight = if (selectedStandard == std) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedStandard == std) Color(0xFF7C3AED) else Color(0xFF334155)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. Appearance & Theme Section (Orange theme)
        // ==========================================
        item {
            SectionContainer(
                borderColor = Color(0xFFF97316),
                icon = Icons.Default.Palette,
                iconTint = Color(0xFFEA580C),
                iconBg = Color(0xFFFFF7ED),
                title = "Appearance & Theme",
                subtitle = "Customize app appearance"
            ) {
                // Theme Mode Row with Radios
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF7ED)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LightMode,
                            contentDescription = null,
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Theme Mode",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Light (Default) | Dark | System",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("LIGHT" to "Light", "DARK" to "Dark", "SYSTEM" to "System").forEach { (mode, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { selectedThemeMode = mode }
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedThemeMode == mode,
                                onClick = { selectedThemeMode = mode },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFFEA580C),
                                    unselectedColor = Color(0xFF94A3B8)
                                ),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (selectedThemeMode == mode) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedThemeMode == mode) Color(0xFFEA580C) else Color(0xFF334155)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Details",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onNavigateToTheme() }
                    )
                }

                HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 8.dp))

                // Customize Colors & Previews
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigateToTheme() }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Customize Colors & Previews",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Change app colors and preview screens",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Open",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // ==========================================
        // 5. App Behavior Section (Teal theme)
        // ==========================================
        item {
            SectionContainer(
                borderColor = Color(0xFF06B6D4),
                icon = Icons.Default.Tune,
                iconTint = Color(0xFF0891B2),
                iconBg = Color(0xFFECFEFF),
                title = "App Behavior",
                subtitle = "Control app features and behavior"
            ) {
                BehaviorSwitchRow(
                    title = "Show App Logo in Header",
                    subtitle = "Display app logo on top header",
                    checked = showLogo,
                    onCheckedChange = { showLogo = it }
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                BehaviorSwitchRow(
                    title = "Show Engineer Profile in Footer",
                    subtitle = "Show technician profile in footer",
                    checked = showDevInfo,
                    onCheckedChange = { showDevInfo = it }
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                BehaviorSwitchRow(
                    title = "Show Date & Time",
                    subtitle = "Show current date and time",
                    checked = showDateTime,
                    onCheckedChange = { showDateTime = it }
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                BehaviorSwitchRow(
                    title = "Show Formula",
                    subtitle = "Display calculation formula",
                    checked = showFormula,
                    onCheckedChange = { showFormula = it }
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                BehaviorSwitchRow(
                    title = "Show Step-by-Step Solution",
                    subtitle = "Show detailed step by step solution",
                    checked = showSteps,
                    onCheckedChange = { showSteps = it }
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                BehaviorSwitchRow(
                    title = "Show Notes & Assumptions",
                    subtitle = "Display notes and assumptions",
                    checked = showNotes,
                    onCheckedChange = { showNotes = it }
                )
            }
        }

        // ==========================================
        // 6. Save Settings Button (Full width Blue)
        // ==========================================
        item {
            Button(
                onClick = {
                    coroutineScope.launch {
                        // 1. Save standard
                        app.settingsManager.updateCalculationDefaults(
                            calcDefaults.copy(standard = selectedStandard)
                        )
                        // 2. Save theme
                        app.settingsManager.updateThemeMode(selectedThemeMode)
                        // 3. Save language
                        app.settingsManager.updateLanguage(selectedLanguage)
                        // 4. Save PDF & behavior toggles
                        app.settingsManager.updatePdfPrintSettings(
                            pdfSettings.copy(
                                showLogo = showLogo,
                                showDeveloperInfo = showDevInfo,
                                showDateTime = showDateTime,
                                showFormula = showFormula,
                                showSteps = showSteps,
                                showNotes = showNotes
                            )
                        )
                        Toast.makeText(context, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_settings_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Settings",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionContainer(
    borderColor: Color,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, borderColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(6.dp))

            content()
        }
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = Color(0xFF64748B)
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Open",
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun BehaviorSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0284C7),
                uncheckedThumbColor = Color(0xFFCBD5E1),
                uncheckedTrackColor = Color(0xFFE2E8F0)
            )
        )
    }
}
