package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    val currentLanguage by app.settingsManager.languageFlow.collectAsState(initial = "EN")

    // State for local editing
    var selectedStandard by remember(calcDefaults.standard) { mutableStateOf(calcDefaults.standard) }
    var selectedThemeMode by remember(currentTheme) { mutableStateOf(currentTheme) }
    var selectedLanguage by remember(currentLanguage) { mutableStateOf(currentLanguage) }

    // Dialog visibility states for user-requested interactive pickers
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showStandardDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    var showLogo by remember(pdfSettings.showLogo) { mutableStateOf(pdfSettings.showLogo) }
    var showDevInfo by remember(pdfSettings.showDeveloperInfo) { mutableStateOf(pdfSettings.showDeveloperInfo) }
    var showDateTime by remember(pdfSettings.showDateTime) { mutableStateOf(pdfSettings.showDateTime) }
    var showFormula by remember(pdfSettings.showFormula) { mutableStateOf(pdfSettings.showFormula) }
    var showSteps by remember(pdfSettings.showSteps) { mutableStateOf(pdfSettings.showSteps) }
    var showNotes by remember(pdfSettings.showNotes) { mutableStateOf(pdfSettings.showNotes) }

    // ==========================================
    // 1. LANGUAGE SELECTION MODAL DIALOG
    // ==========================================
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = if (selectedLanguage == "EN") "Select Language" else "ভাষা নির্বাচন করুন (Select Language)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val languages = listOf("EN" to "English (Default)", "BN" to "বাংলা (Bengali)")
                    languages.forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedLanguage = code
                                    coroutineScope.launch {
                                        app.settingsManager.updateLanguage(code)
                                        Toast.makeText(
                                            context,
                                            if (code == "EN") "Language changed: English" else "ভাষা পরিবর্তিত হয়েছে: বাংলা",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == code,
                                onClick = {
                                    selectedLanguage = code
                                    coroutineScope.launch {
                                        app.settingsManager.updateLanguage(code)
                                        Toast.makeText(
                                            context,
                                            if (code == "EN") "Language changed: English" else "ভাষা পরিবর্তিত হয়েছে: বাংলা",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    showLanguageDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0284C7))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = if (selectedLanguage == code) FontWeight.Bold else FontWeight.Normal,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(if (selectedLanguage == "EN") "Close" else "বন্ধ করুন", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ==========================================
    // 2. DEFAULT STANDARD SELECTION MODAL DIALOG
    // ==========================================
    if (showStandardDialog) {
        AlertDialog(
            onDismissRequest = { showStandardDialog = false },
            title = {
                Text(
                    text = "Selected Default Standard Man IEC, NEC, BNBC",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val standards = listOf(
                        "IEC" to "IEC (International Electrotechnical Commission - IEC 60364)",
                        "NEC" to "NEC (National Electrical Code - NFPA 70)",
                        "BNBC" to "BNBC (Bangladesh National Building Code)",
                        "General" to "General Engineering Standard"
                    )
                    standards.forEach { (std, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedStandard = std
                                    coroutineScope.launch {
                                        app.settingsManager.updateCalculationDefaults(calcDefaults.copy(standard = std))
                                        Toast.makeText(context, "স্ট্যান্ডার্ড নির্বাচন: $std", Toast.LENGTH_SHORT).show()
                                    }
                                    showStandardDialog = false
                                }
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedStandard == std,
                                onClick = {
                                    selectedStandard = std
                                    coroutineScope.launch {
                                        app.settingsManager.updateCalculationDefaults(calcDefaults.copy(standard = std))
                                        Toast.makeText(context, "স্ট্যান্ডার্ড নির্বাচন: $std", Toast.LENGTH_SHORT).show()
                                    }
                                    showStandardDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0284C7))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = std,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = desc,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStandardDialog = false }) {
                    Text("বন্ধ করুন", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ==========================================
    // 3. APPEARANCE THEME SELECTION MODAL DIALOG
    // ==========================================
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Text(
                    text = "Appearance Theme নির্বাচন করুন",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val themes = listOf(
                        "LIGHT" to "Light (হালকা থিম • ডিফল্ট)",
                        "DARK" to "Dark (গাঢ় ব্ল্যাক থিম)",
                        "SYSTEM" to "System Default (ডিভাইসের নিয়ম অনুযায়ী)"
                    )
                    themes.forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedThemeMode = mode
                                    coroutineScope.launch {
                                        app.settingsManager.updateThemeMode(mode)
                                        Toast.makeText(context, "থিম মোড: $label", Toast.LENGTH_SHORT).show()
                                    }
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedThemeMode == mode,
                                onClick = {
                                    selectedThemeMode = mode
                                    coroutineScope.launch {
                                        app.settingsManager.updateThemeMode(mode)
                                        Toast.makeText(context, "থিম মোড: $label", Toast.LENGTH_SHORT).show()
                                    }
                                    showThemeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0284C7))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = if (selectedThemeMode == mode) FontWeight.Bold else FontWeight.Normal,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("বন্ধ করুন", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ==========================================
    // MAIN SETTINGS LIST DIRECTLY ON ROOT BACKGROUND
    // ==========================================
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // ------------------------------------------
        // SECTION 1: CORE PREFERENCES (REQUESTED)
        // ------------------------------------------
        item {
            SettingsCategoryHeader(title = "মূল পছন্দসমূহ • PREFERENCES")
        }

        item {
            BorderlessSettingItem(
                icon = Icons.Default.Language,
                iconTint = Color(0xFF0284C7),
                iconBg = Color(0xFFE0F2FE),
                title = "Language",
                subtitle = "অ্যাপের ভাষা নির্বাচন করুন / Select language",
                valueBadge = if (selectedLanguage == "BN") "বাংলা" else "English",
                onClick = { showLanguageDialog = true }
            )
        }

        item {
            BorderlessSettingItem(
                icon = Icons.Default.FormatListNumbered,
                iconTint = Color(0xFF7C3AED),
                iconBg = Color(0xFFF5F3FF),
                title = "Selected Default Standard Man IEC,NEC, BNBC",
                subtitle = "আন্তর্জাতিক ও জাতীয় ইলেকট্রিক্যাল স্ট্যান্ডার্ড",
                valueBadge = selectedStandard,
                onClick = { showStandardDialog = true }
            )
        }

        item {
            BorderlessSettingItem(
                icon = Icons.Default.LightMode,
                iconTint = Color(0xFFEA580C),
                iconBg = Color(0xFFFFF7ED),
                title = "Appearance Theme",
                subtitle = "লাইট, ডার্ক বা সিস্টেম থিম মোড নির্বাচন করুন",
                valueBadge = when (selectedThemeMode) {
                    "DARK" -> "Dark"
                    "SYSTEM" -> "System"
                    else -> "Light"
                },
                onClick = { showThemeDialog = true }
            )
        }

        // ------------------------------------------
        // SECTION 2: PROFILES & ACCOUNTS
        // ------------------------------------------
        item {
            SettingsCategoryHeader(title = "প্রোফাইল ও রিপোর্ট খতিয়ান")
        }

        item {
            BorderlessSettingItem(
                icon = Icons.Default.Person,
                iconTint = Color(0xFF0284C7),
                iconBg = Color(0xFFE0F2FE),
                title = "Engineer Rubel (Developer Profile)",
                subtitle = "Industrial Electrical & Power Engineering • mdrubel88652@gmail.com",
                valueBadge = "Developer",
                onClick = onNavigateToDeveloper
            )
        }

        item {
            BorderlessSettingItem(
                icon = Icons.Default.Person,
                iconTint = Color(0xFF2563EB),
                iconBg = Color(0xFFEFF6FF),
                title = "Technician Report Profile",
                subtitle = if (techProfile.isNotEmpty()) "Technician: ${techProfile.name} • ${techProfile.company}"
                           else "Configure technician profile for PDF reports",
                valueBadge = if (techProfile.isNotEmpty()) "Configured" else "Empty",
                onClick = onNavigateToTechnicianProfile
            )
        }

        // ------------------------------------------
        // SECTION 3: RECORDS & REPORTS
        // ------------------------------------------
        item {
            SettingsCategoryHeader(title = "হিসাবের ইতিহাস ও রিপোর্ট")
        }

        item {
            BorderlessSettingItem(
                icon = Icons.Default.History,
                iconTint = Color(0xFF0284C7),
                iconBg = Color(0xFFE0F2FE),
                title = "Calculation History (হিসাবের ইতিহাস)",
                subtitle = "পূর্বে সংরক্ষিত সকল ক্যালকুলেশন রেকর্ড ও শেয়ারিং",
                onClick = onNavigateToHistory
            )
        }

        item {
            BorderlessSettingItem(
                icon = Icons.Default.Description,
                iconTint = Color(0xFF7C3AED),
                iconBg = Color(0xFFF5F3FF),
                title = "Reports & Project Summary (রিপোর্ট ও সামারি)",
                subtitle = "প্রজেক্ট রিপোর্ট, ম্যাটেরিয়াল লিস্ট ও টেকনিশিয়ান শিট PDF",
                onClick = onNavigateToReports
            )
        }

        // ------------------------------------------
        // SECTION 4: APPLICATION INFO
        // ------------------------------------------
        item {
            SettingsCategoryHeader(title = "অ্যাপ্লিকেশন তথ্য ও নিরাপত্তা")
        }

        item {
            BorderlessSettingItem(
                icon = Icons.Default.Info,
                iconTint = Color(0xFF059669),
                iconBg = Color(0xFFECFDF5),
                title = "About Application",
                subtitle = "ভার্সন, ১৬৭টি ক্যালকুলেটরের উদ্দেশ্য ও বিবরণ",
                onClick = onNavigateToAbout
            )
        }

        item {
            BorderlessSettingItem(
                icon = Icons.Default.Shield,
                iconTint = Color(0xFF059669),
                iconBg = Color(0xFFECFDF5),
                title = "Privacy Policy",
                subtitle = "ডাটা নিরাপত্তা, অফলাইন সুবিধা ও পলিসি",
                onClick = onNavigateToPrivacy
            )
        }

        item {
            BorderlessSettingItem(
                icon = Icons.Default.Share,
                iconTint = Color(0xFF059669),
                iconBg = Color(0xFFECFDF5),
                title = "Share Application",
                subtitle = "প্রকৌশলী, ইলেকট্রিশিয়ান ও শিক্ষার্থীদের সাথে শেয়ার করুন",
                onClick = {
                    ShareUtils.shareText(
                        context,
                        "Electrical Cal/Pro",
                        "Download Electrical Cal/Pro - The professional electrical engineering calculation & estimation app with 167 calculators!"
                    )
                }
            )
        }

        // ------------------------------------------
        // SECTION 5: PDF REPORT CONTROLS
        // ------------------------------------------
        item {
            SettingsCategoryHeader(title = "PDF রিপোর্ট এক্সপোর্ট সেটিংস")
        }

        item {
            BorderlessSwitchItem(
                title = "Show App Logo in Header",
                subtitle = "Display app logo on top header",
                checked = showLogo,
                onCheckedChange = {
                    showLogo = it
                    coroutineScope.launch {
                        app.settingsManager.updatePdfPrintSettings(pdfSettings.copy(showLogo = it))
                    }
                }
            )
        }

        item {
            BorderlessSwitchItem(
                title = "Show Technician Profile in Footer",
                subtitle = "Show technician profile in footer",
                checked = showDevInfo,
                onCheckedChange = {
                    showDevInfo = it
                    coroutineScope.launch {
                        app.settingsManager.updatePdfPrintSettings(pdfSettings.copy(showDeveloperInfo = it))
                    }
                }
            )
        }

        item {
            BorderlessSwitchItem(
                title = "Show Date & Time",
                subtitle = "Show current date and time on export",
                checked = showDateTime,
                onCheckedChange = {
                    showDateTime = it
                    coroutineScope.launch {
                        app.settingsManager.updatePdfPrintSettings(pdfSettings.copy(showDateTime = it))
                    }
                }
            )
        }

        item {
            BorderlessSwitchItem(
                title = "Show Formula",
                subtitle = "Display calculation formula in report",
                checked = showFormula,
                onCheckedChange = {
                    showFormula = it
                    coroutineScope.launch {
                        app.settingsManager.updatePdfPrintSettings(pdfSettings.copy(showFormula = it))
                    }
                }
            )
        }

        item {
            BorderlessSwitchItem(
                title = "Show Step-by-Step Solution",
                subtitle = "Show detailed step by step solution",
                checked = showSteps,
                onCheckedChange = {
                    showSteps = it
                    coroutineScope.launch {
                        app.settingsManager.updatePdfPrintSettings(pdfSettings.copy(showSteps = it))
                    }
                }
            )
        }

        item {
            BorderlessSwitchItem(
                title = "Show Notes & Assumptions",
                subtitle = "Display notes and engineering assumptions",
                checked = showNotes,
                onCheckedChange = {
                    showNotes = it
                    coroutineScope.launch {
                        app.settingsManager.updatePdfPrintSettings(pdfSettings.copy(showNotes = it))
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0284C7),
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 6.dp)
    )
}

@Composable
private fun BorderlessSettingItem(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    valueBadge: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
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
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            if (valueBadge != null) {
                Surface(
                    color = Color(0xFFE0F2FE),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = valueBadge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun BorderlessSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(2.dp))
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
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCBD5E1)
            )
        )
    }
}
