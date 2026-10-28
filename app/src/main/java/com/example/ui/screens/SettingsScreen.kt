package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    val isEn = selectedLanguage == "EN"

    // Dialog visibility states
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showStandardDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPdfSettingsDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }
    var showComplaintDialog by remember { mutableStateOf(false) }
    var showTranslationDialog by remember { mutableStateOf(false) }
    var showOtherAppsDialog by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }

    // PDF toggles
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
                Column {
                    Text(
                        text = if (isEn) "Select Language" else "ভাষা নির্বাচন করুন",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF082D63)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isEn) "Select app display language" else "অ্যাপ ডিসপ্লে ভাষা নির্বাচন করুন",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val languages = listOf("EN" to "English (Default)", "BN" to "বাংলা (Bengali)")
                    languages.forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedLanguage = code
                                    coroutineScope.launch {
                                        app.settingsManager.updateLanguage(code)
                                        Toast.makeText(
                                            context,
                                            if (code == "EN") "Language: English" else "ভাষা: বাংলা",
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
                                            if (code == "EN") "Language: English" else "ভাষা: বাংলা",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    showLanguageDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF082D63))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                fontSize = 14.5.sp,
                                fontWeight = if (selectedLanguage == code) FontWeight.Bold else FontWeight.Normal,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(if (isEn) "Close" else "বন্ধ করুন", color = Color(0xFF082D63), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // ==========================================
    // 2. DEFAULT STANDARD SELECTION MODAL DIALOG
    // ==========================================
    if (showStandardDialog) {
        AlertDialog(
            onDismissRequest = { showStandardDialog = false },
            title = {
                Column {
                    Text(
                        text = if (isEn) "Default Standard Setting" else "ডিফল্ট স্ট্যান্ডার্ড সেটিংস",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF082D63)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isEn) "International & national electrical engineering standards" else "আন্তর্জাতিক ও জাতীয় ইলেকট্রিক্যাল ইঞ্জিনিয়ারিং স্ট্যান্ডার্ড",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
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
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedStandard = std
                                    coroutineScope.launch {
                                        app.settingsManager.updateCalculationDefaults(calcDefaults.copy(standard = std))
                                        Toast.makeText(context, "Standard: $std", Toast.LENGTH_SHORT).show()
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
                                        Toast.makeText(context, "Standard: $std", Toast.LENGTH_SHORT).show()
                                    }
                                    showStandardDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF082D63))
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
                    Text(if (isEn) "Close" else "বন্ধ করুন", color = Color(0xFF082D63), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // ==========================================
    // 3. APPEARANCE THEME SELECTION MODAL DIALOG
    // ==========================================
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Column {
                    Text(
                        text = if (isEn) "Appearance Theme" else "থিম ও রূপরেখা",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF082D63)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isEn) "Choose light, dark, or system theme mode" else "লাইট, ডার্ক বা সিস্টেম থিম মোড নির্বাচন করুন",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val themes = listOf(
                        "LIGHT" to "Light (Clean Light Theme)",
                        "DARK" to "Dark (Deep Dark Theme)",
                        "SYSTEM" to "System Default (Follow Device)"
                    )
                    themes.forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedThemeMode = mode
                                    coroutineScope.launch {
                                        app.settingsManager.updateThemeMode(mode)
                                        Toast.makeText(context, "Theme: $label", Toast.LENGTH_SHORT).show()
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
                                        Toast.makeText(context, "Theme: $label", Toast.LENGTH_SHORT).show()
                                    }
                                    showThemeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF082D63))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                fontSize = 14.5.sp,
                                fontWeight = if (selectedThemeMode == mode) FontWeight.Bold else FontWeight.Normal,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(if (isEn) "Close" else "বন্ধ করুন", color = Color(0xFF082D63), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // ==========================================
    // 4. PDF REPORT EXPORT SETTINGS DIALOG
    // ==========================================
    if (showPdfSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showPdfSettingsDialog = false },
            title = {
                Column {
                    Text(
                        text = if (isEn) "PDF Report Export Settings" else "PDF রিপোর্ট এক্সপোর্ট সেটিংস",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF082D63)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isEn) "Configure elements to include in PDF calculation reports"
                               else "PDF রিপোর্ট ও প্রিন্ট ডকুমেন্টের উপাদানসমূহ কনফিগার করুন",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SettingsSwitchRow(
                        title = if (isEn) "Show App Logo in Header" else "হেডারে অ্যাপ লোগো দেখান",
                        subtitle = if (isEn) "Display app logo on top header" else "টপ হেডারে অ্যাপের লোগো প্রিন্ট করুন",
                        checked = showLogo,
                        onCheckedChange = {
                            showLogo = it
                            coroutineScope.launch {
                                app.settingsManager.updatePdfPrintSettings(pdfSettings.copy(showLogo = it))
                            }
                        }
                    )
                    SettingsSwitchRow(
                        title = if (isEn) "Show Technician Profile in Footer" else "ফুটার এ টেকনিশিয়ান প্রোফাইল দেখান",
                        subtitle = if (isEn) "Show technician profile in footer" else "রিপোর্ট ফুটারে টেকনিশিয়ানের তথ্য প্রদর্শন",
                        checked = showDevInfo,
                        onCheckedChange = {
                            showDevInfo = it
                            coroutineScope.launch {
                                app.settingsManager.updatePdfPrintSettings(pdfSettings.copy(showDeveloperInfo = it))
                            }
                        }
                    )
                    SettingsSwitchRow(
                        title = if (isEn) "Show Date & Time" else "তারিখ ও সময় অন্তর্ভুক্ত করুন",
                        subtitle = if (isEn) "Show current date and time on export" else "এক্সপোর্টে বর্তমান তারিখ ও সময় প্রদর্শন",
                        checked = showDateTime,
                        onCheckedChange = {
                            showDateTime = it
                            coroutineScope.launch {
                                app.settingsManager.updatePdfPrintSettings(pdfSettings.copy(showDateTime = it))
                            }
                        }
                    )
                    SettingsSwitchRow(
                        title = if (isEn) "Show Formula" else "হিসাবের সূত্র প্রদর্শন",
                        subtitle = if (isEn) "Display calculation formula in report" else "রিপোর্টে ব্যবহৃত সমীকরণ ও সূত্র প্রদর্শন",
                        checked = showFormula,
                        onCheckedChange = {
                            showFormula = it
                            coroutineScope.launch {
                                app.settingsManager.updatePdfPrintSettings(pdfSettings.copy(showFormula = it))
                            }
                        }
                    )
                    SettingsSwitchRow(
                        title = if (isEn) "Show Step-by-Step Solution" else "ধাপভিত্তিক সমাধান প্রদর্শন",
                        subtitle = if (isEn) "Show detailed step by step solution" else "বিস্তারিত সমীকরণ ও সমাধান ধাপ প্রদর্শন",
                        checked = showSteps,
                        onCheckedChange = {
                            showSteps = it
                            coroutineScope.launch {
                                app.settingsManager.updatePdfPrintSettings(pdfSettings.copy(showSteps = it))
                            }
                        }
                    )
                    SettingsSwitchRow(
                        title = if (isEn) "Show Engineering Notes & Standards" else "ইঞ্জিনিয়ারিং নোটস ও স্ট্যান্ডার্ড",
                        subtitle = if (isEn) "Display notes and engineering assumptions" else "ইঞ্জিনিয়ারিং নোট ও স্ট্যান্ডার্ড অনুমান প্রদর্শন",
                        checked = showNotes,
                        onCheckedChange = {
                            showNotes = it
                            coroutineScope.launch {
                                app.settingsManager.updatePdfPrintSettings(pdfSettings.copy(showNotes = it))
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPdfSettingsDialog = false }) {
                    Text(if (isEn) "Done" else "সম্পন্ন", color = Color(0xFF082D63), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // ==========================================
    // 5. CLEAR HISTORY CONFIRMATION DIALOG
    // ==========================================
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = {
                Text(
                    text = if (isEn) "Clear Calculation History" else "হিস্ট্রি পরিষ্কার করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFFDC2626)
                )
            },
            text = {
                Text(
                    text = if (isEn) "Are you sure you want to clear all calculation history records? This action cannot be undone."
                           else "আপনি কি সমস্ত হিসাবের ইতিহাস মুছে ফেলতে নিশ্চিত? এই হিস্ট্রি পুনরায় ফিরে পাওয়া যাবে না।",
                    fontSize = 13.5.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            app.historyRepository.clearAllHistory()
                            Toast.makeText(
                                context,
                                if (isEn) "History cleared successfully" else "হিস্ট্রি পরিষ্কার করা হয়েছে",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text(if (isEn) "Clear All" else "মুছুন", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(if (isEn) "Cancel" else "বাতিল", color = Color(0xFF64748B))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // ==========================================
    // 7. UPDATE PREMIUM VERSION DIALOG
    // ==========================================
    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Premium Edition",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF082D63)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "You have full lifetime access to Electrical Calculation All Pro:",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    val features = listOf(
                        "⚡ 167+ Electrical Engineering Calculators Unlocked",
                        "📁 Contract Project Manager & Small Job Tracking",
                        "📄 Watermark-Free Professional PDF Export",
                        "💼 Built-in Project Wallet & Expense Tracking",
                        "☁️ On-Device Offline High-Speed Database",
                        "🚫 100% Ad-Free Clean Interface"
                    )
                    features.forEach { feat ->
                        Text(
                            text = feat,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "All Pro features are fully unlocked for you!", Toast.LENGTH_SHORT).show()
                        showPremiumDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Enjoying Pro Features", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPremiumDialog = false }) {
                    Text("Close", color = Color(0xFF64748B))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // ==========================================
    // 8. APP COMPLAINT BOX / FEEDBACK DIALOG
    // ==========================================
    if (showComplaintDialog) {
        var feedbackText by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("Bug Report") }
        val categories = listOf("Bug Report", "Calculation Issue", "Feature Request", "Other")

        AlertDialog(
            onDismissRequest = { showComplaintDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Feedback,
                        contentDescription = null,
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "App Complaint & Feedback",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF082D63)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Report an issue, calculation discrepancy, or request new features directly:",
                        fontSize = 12.5.sp,
                        color = Color(0xFF64748B)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSel = selectedCategory == cat
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 10.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF082D63),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = Color(0xFF475569)
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        placeholder = { Text("Describe the issue or suggestion...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (feedbackText.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:tajnoakhali@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Electrical Calculation All — $selectedCategory")
                                putExtra(Intent.EXTRA_TEXT, feedbackText)
                            }
                            try {
                                context.startActivity(Intent.createChooser(intent, "Send Feedback"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Feedback recorded. Thank you!", Toast.LENGTH_SHORT).show()
                            }
                            showComplaintDialog = false
                        } else {
                            Toast.makeText(context, "Please enter your message first", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316))
                ) {
                    Text("Send Feedback", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showComplaintDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // ==========================================
    // 9. TRANSLATION TOOL DIALOG
    // ==========================================
    if (showTranslationDialog) {
        val dictionary = listOf(
            "Current" to "বিদ্যুৎ প্রবাহ (Amperes)",
            "Voltage" to "ভোল্টেজ / বিভব পার্থক্য (Volts)",
            "Resistance" to "রোধ / রেজিস্ট্যান্স (Ohms)",
            "Power Factor" to "পাওয়ার ফ্যাক্টর (cos θ)",
            "Transformer" to "বৈদ্যুতিক ট্রান্সফরমার",
            "Substation" to "বৈদ্যুতিক সাবস্টেশন",
            "Circuit Breaker" to "সার্কিট ব্রেকার (MCB / MCCB / ACB)",
            "Conduit Wiring" to "কনডুইট ওয়্যারিং পাইপ",
            "Earthing / Grounding" to "আর্থিং ও গ্রাউন্ডিং সিস্টেম",
            "Short Circuit" to "শর্ট সার্কিট ফল্ট",
            "Capacitor Bank" to "ক্যাপাসিটর ব্যাংক (PFI)",
            "Busbar Trunking" to "বাসবার ট্রাংকিং (BBT)"
        )
        var searchQuery by remember { mutableStateOf("") }
        val filteredList = dictionary.filter {
            it.first.contains(searchQuery, ignoreCase = true) || it.second.contains(searchQuery, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { showTranslationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Electrical Translation Tool",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF082D63)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "English ⟷ বাংলা Electrical Engineering Terminology",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search term (e.g. Current, Earthing...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredList.size) { idx ->
                            val item = filteredList[idx]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .clickable {
                                        ShareUtils.copyToClipboard(context, item.first, "${item.first} = ${item.second}")
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.first, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF082D63))
                                    Text(item.second, fontSize = 12.sp, color = Color(0xFF475569))
                                }
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTranslationDialog = false }) {
                    Text("Close", color = Color(0xFF082D63), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // ==========================================
    // 10. OTHER ALL APPS DIALOG
    // ==========================================
    if (showOtherAppsDialog) {
        val otherApps = listOf(
            "Solar System Designer Pro" to "Complete Off-Grid & Grid-Tie Solar PV Sizing",
            "Building Wiring BOQ Estimator" to "Residential & Commercial Conduit, Wire & Board BOQ",
            "Substation & HT Switchgear" to "Transformer, Breaker & Cable Sizing Guide",
            "Industrial Motor & Pump Calculator" to "HP, kW, Full-Load Current & Starter Sizing"
        )
        AlertDialog(
            onDismissRequest = { showOtherAppsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Other Useful Apps from Us",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF082D63)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    otherApps.forEach { (appName, desc) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps"))
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Opening Google Play Store...", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(appName, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFF0F172A))
                                    Text(desc, fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOtherAppsDialog = false }) {
                    Text("Close", color = Color(0xFF082D63), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // ==========================================
    // 11. RATE US APP DIALOG
    // ==========================================
    if (showRateDialog) {
        var userRating by remember { mutableIntStateOf(5) }
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Rate Electrical Calculation All",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF082D63),
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Your rating helps us keep adding new calculation tools and features for technicians and electrical engineers!",
                        fontSize = 12.5.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                    Row(horizontalArrangement = Arrangement.Center) {
                        for (i in 1..5) {
                            IconButton(onClick = { userRating = i }) {
                                Icon(
                                    imageVector = if (i <= userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$i Stars",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val packageName = context.packageName
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                            context.startActivity(webIntent)
                        }
                        showRateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                ) {
                    Text("Rate on Play Store", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRateDialog = false }) {
                    Text("Later", color = Color(0xFF64748B))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // ==========================================
    // MAIN SETTINGS LIST: 17 VERTICAL WHITE CARDS (MATCHES IMAGE 2)
    // ==========================================
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F6FB))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Technician Report Profile
        item {
            SettingsItemCard(
                icon = Icons.Default.Engineering,
                iconTint = Color(0xFF0284C7),
                iconBgColor = Color(0xFFE0F2FE),
                title = if (isEn) "Technician Report Profile" else "টেকনিশিয়ান রিপোর্ট প্রোফাইল",
                subtitle = if (isEn) "Manage technician profile and report information" else "টেকনিশিয়ান প্রোফাইল ও রিপোর্ট তথ্য পরিচালনা করুন",
                onClick = onNavigateToTechnicianProfile,
                testTag = "setting_item_technician_profile"
            )
        }

        // 2. Developer Profile Engineer
        item {
            SettingsItemCard(
                icon = Icons.Default.ManageAccounts,
                iconTint = Color(0xFF8B5CF6),
                iconBgColor = Color(0xFFF3E8FF),
                title = if (isEn) "Developer Profile Engineer" else "ডেভেলপার প্রোফাইল ইঞ্জিনিয়ার",
                subtitle = if (isEn) "Manage developer and engineer information" else "ডেভেলপার ও ইঞ্জিনিয়ার তথ্য পরিচালনা করুন",
                onClick = onNavigateToDeveloper,
                testTag = "setting_item_developer_profile"
            )
        }

        // 3. Update Premium Version
        item {
            SettingsItemCard(
                icon = Icons.Default.WorkspacePremium,
                iconTint = Color(0xFF10B981),
                iconBgColor = Color(0xFFD1FAE5),
                title = if (isEn) "Update Premium Version" else "আপডেট প্রিমিয়াম ভার্সন",
                subtitle = if (isEn) "Get premium features and benefits" else "প্রিমিয়াম ফিচার ও সুবিধাসমূহ উপভোগ করুন",
                onClick = { showPremiumDialog = true },
                testTag = "setting_item_update_premium"
            )
        }

        // 4. Report & Project Summary
        item {
            SettingsItemCard(
                icon = Icons.Default.Assessment,
                iconTint = Color(0xFF0284C7),
                iconBgColor = Color(0xFFE0F2FE),
                title = if (isEn) "Report & Project Summary" else "রিপোর্ট ও প্রজেক্ট সামারি",
                subtitle = if (isEn) "View saved reports and project summary" else "সংরক্ষিত রিপোর্ট ও প্রজেক্ট সারসংক্ষেপ দেখুন",
                onClick = onNavigateToReports,
                testTag = "setting_item_reports_summary"
            )
        }

        // 5. App Complaint Box
        item {
            SettingsItemCard(
                icon = Icons.Default.Feedback,
                iconTint = Color(0xFFF97316),
                iconBgColor = Color(0xFFFFEDD5),
                title = if (isEn) "App Complaint Box" else "অ্যাপ অভিযোগ বক্স • ফিডব্যাক",
                subtitle = if (isEn) "Send feedback or report an issue" else "ফিডব্যাক পাঠান বা যেকোনো সমস্যা রিপোর্ট করুন",
                onClick = { showComplaintDialog = true },
                testTag = "setting_item_complaint_box"
            )
        }

        // 6. Pdf Report Export Settings
        item {
            SettingsItemCard(
                icon = Icons.Default.PictureAsPdf,
                iconTint = Color(0xFFEF4444),
                iconBgColor = Color(0xFFFEE2E2),
                title = if (isEn) "Pdf Report Export Settings" else "PDF রিপোর্ট এক্সপোর্ট সেটিংস",
                subtitle = if (isEn) "Configure PDF export options" else "PDF এক্সপোর্টের উপাদানসমূহ কনফিগার করুন",
                onClick = { showPdfSettingsDialog = true },
                testTag = "setting_item_pdf_export_settings"
            )
        }

        // 7. Default Standard Setting
        item {
            SettingsItemCard(
                icon = Icons.Default.Tune,
                iconTint = Color(0xFF8B5CF6),
                iconBgColor = Color(0xFFF3E8FF),
                title = if (isEn) "Default Standard Setting" else "ডিফল্ট স্ট্যান্ডার্ড সেটিংস",
                subtitle = if (isEn) "Select default standard (IEC / NEC / BNBC / etc.)" else "স্ট্যান্ডার্ড নির্বাচন করুন (IEC / NEC / BNBC / ইত্যাদি)",
                onClick = { showStandardDialog = true },
                testTag = "setting_item_default_standard"
            )
        }

        // 8. Clear Calculation History
        item {
            SettingsItemCard(
                icon = Icons.Default.Delete,
                iconTint = Color(0xFFEF4444),
                iconBgColor = Color(0xFFFEE2E2),
                title = if (isEn) "Clear Calculation History" else "ক্যালকুলেশন হিস্ট্রি মুছুন",
                subtitle = if (isEn) "Remove all calculation history data" else "সকল সংরক্ষিত গণনার হিস্ট্রি ডেটা মুছে ফেলুন",
                onClick = { showClearHistoryDialog = true },
                testTag = "setting_item_clear_history"
            )
        }

        // 9. Appearance Times (Appearance Theme)
        item {
            SettingsItemCard(
                icon = Icons.Default.LightMode,
                iconTint = Color(0xFFF59E0B),
                iconBgColor = Color(0xFFFEF3C7),
                title = if (isEn) "Appearance Times" else "থিম ও রূপরেখা (Theme)",
                subtitle = if (isEn) "Light / Dark / System mode" else "লাইট / ডার্ক / সিস্টেম মোড",
                onClick = { showThemeDialog = true },
                testTag = "setting_item_appearance_theme"
            )
        }

        // 11. Language settings
        item {
            SettingsItemCard(
                icon = Icons.Default.Language,
                iconTint = Color(0xFF10B981),
                iconBgColor = Color(0xFFD1FAE5),
                title = if (isEn) "Language settings" else "ভাষা নির্বাচন (Language)",
                subtitle = if (isEn) "Choose your preferred language" else "আপনার পছন্দের ভাষা নির্বাচন করুন",
                onClick = { showLanguageDialog = true },
                testTag = "setting_item_language"
            )
        }

        // 12. Translation Tool
        item {
            SettingsItemCard(
                icon = Icons.Default.Translate,
                iconTint = Color(0xFF8B5CF6),
                iconBgColor = Color(0xFFF3E8FF),
                title = if (isEn) "Translation Tool" else "ইলেকট্রিক্যাল অনুবাদ টুল",
                subtitle = if (isEn) "Translate text or content" else "ইংরেজি ও বাংলা টেকনিক্যাল পরিভাষা অনুবাদ করুন",
                onClick = { showTranslationDialog = true },
                testTag = "setting_item_translation_tool"
            )
        }

        // 13. Share Application
        item {
            SettingsItemCard(
                icon = Icons.Default.Share,
                iconTint = Color(0xFF0284C7),
                iconBgColor = Color(0xFFE0F2FE),
                title = if (isEn) "Share Application" else "অ্যাপ্লিকেশন শেয়ার করুন",
                subtitle = if (isEn) "Share this app with your friends" else "বন্ধুদের সাথে এই দরকারী অ্যাপটি শেয়ার করুন",
                onClick = {
                    ShareUtils.shareText(
                        context,
                        "Electrical Calculation All",
                        "Download Electrical Calculation All - The professional electrical engineering calculation & estimation app with 167 calculators!"
                    )
                },
                testTag = "setting_item_share_app"
            )
        }

        // 14. About Application
        item {
            SettingsItemCard(
                icon = Icons.Default.Info,
                iconTint = Color(0xFF14B8A6),
                iconBgColor = Color(0xFFCCFBF1),
                title = if (isEn) "About Application" else "অ্যাপ্লিকেশন তথ্য • About",
                subtitle = if (isEn) "App version, developer and more" else "অ্যাপ ভার্সন, ডেভেলপার তথ্য ও বিস্তারিত",
                onClick = onNavigateToAbout,
                testTag = "setting_item_about_app"
            )
        }

        // 15. Other All Apps
        item {
            SettingsItemCard(
                icon = Icons.Default.Apps,
                iconTint = Color(0xFF10B981),
                iconBgColor = Color(0xFFD1FAE5),
                title = if (isEn) "Other All Apps" else "অন্যান্য দরকারী অ্যাপসমূহ",
                subtitle = if (isEn) "View more useful apps from us" else "আমাদের অন্যান্য ইঞ্জিনিয়ারিং অ্যাপসমূহ দেখুন",
                onClick = { showOtherAppsDialog = true },
                testTag = "setting_item_other_apps"
            )
        }

        // 16. Privacy Policy
        item {
            SettingsItemCard(
                icon = Icons.Default.Security,
                iconTint = Color(0xFF6366F1),
                iconBgColor = Color(0xFFE0E7FF),
                title = if (isEn) "Privacy Policy" else "প্রাইভেসি পলিসি • Privacy",
                subtitle = if (isEn) "Read our privacy policy" else "আমাদের গোপনীয়তা নীতি পড়ুন",
                onClick = onNavigateToPrivacy,
                testTag = "setting_item_privacy_policy"
            )
        }

        // 17. Rate Us App
        item {
            SettingsItemCard(
                icon = Icons.Default.Star,
                iconTint = Color(0xFFF59E0B),
                iconBgColor = Color(0xFFFEF3C7),
                title = if (isEn) "Rate Us App" else "অ্যাপে রেটিং দিন • Rate Us",
                subtitle = if (isEn) "Give your valuable rating" else "আপনার মূল্যবান রেটিং ও মতামত দিন",
                onClick = { showRateDialog = true },
                testTag = "setting_item_rate_us"
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SettingsItemCard(
    icon: ImageVector,
    iconTint: Color,
    iconBgColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF082D63),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF082D63),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
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
                checkedTrackColor = Color(0xFF082D63),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCBD5E1)
            )
        )
    }
}
