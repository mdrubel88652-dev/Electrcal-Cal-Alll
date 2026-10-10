package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ElectricalApp
import com.example.R
import com.example.data.datastore.CalculationDefaults
import com.example.data.datastore.EngineerReportProfile
import com.example.data.datastore.PdfPrintSettings
import com.example.data.datastore.TechnicianReportProfile
import com.example.ui.theme.ElectricBluePrimary
import com.example.utils.ShareUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onNavigateToHistory: () -> Unit = {},
    onNavigateToNotification: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToDeveloper: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToTechnicianProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val app = ElectricalApp.getApp(context)

    val currentTheme by app.settingsManager.themeModeFlow.collectAsState(initial = "LIGHT")
    val pdfSettings by app.settingsManager.pdfPrintSettingsFlow.collectAsState(initial = PdfPrintSettings())
    val calcDefaults by app.settingsManager.calculationDefaultsFlow.collectAsState(initial = CalculationDefaults())
    val techProfile by app.settingsManager.technicianProfileFlow.collectAsState(initial = TechnicianReportProfile())

    var paperSize by remember(pdfSettings) { mutableStateOf(pdfSettings.paperSize) }
    var orientation by remember(pdfSettings) { mutableStateOf(pdfSettings.orientation) }
    var showLogo by remember(pdfSettings) { mutableStateOf(pdfSettings.showLogo) }
    var showDevInfo by remember(pdfSettings) { mutableStateOf(pdfSettings.showDeveloperInfo) }
    var showDateTime by remember(pdfSettings) { mutableStateOf(pdfSettings.showDateTime) }
    var showFormula by remember(pdfSettings) { mutableStateOf(pdfSettings.showFormula) }
    var showSteps by remember(pdfSettings) { mutableStateOf(pdfSettings.showSteps) }
    var showNotes by remember(pdfSettings) { mutableStateOf(pdfSettings.showNotes) }

    var selectedStandard by remember(calcDefaults) { mutableStateOf(calcDefaults.standard) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(14.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section 1: Technician Report Profile (Editable, Dedicated Screen)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTechnicianProfile() }
                    .testTag("setting_technician_profile"),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ElectricBluePrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = "Technician",
                            tint = ElectricBluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Technician Report Profile",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (techProfile.isNotEmpty()) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (techProfile.isNotEmpty()) "Configured" else "Empty",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (techProfile.isNotEmpty()) Color(0xFF2E7D32) else Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (techProfile.isNotEmpty()) {
                                "Technician: ${techProfile.name} • Appears at top of reports"
                            } else {
                                "Configure technician credentials for reports (Hidden if empty)"
                            },
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 15.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Open",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Section 2: Engineer & Report Profile (DEFAULT FIXED PROFILE - NON-EDITABLE)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setting_engineer_profile_fixed"),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = ElectricBluePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Engineer & Report Profile",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Surface(
                            color = Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Fixed",
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Fixed Profile",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }

                    Text(
                        text = "This default engineering profile appears at the bottom footer of printed and PDF reports.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Fixed Details Display
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = EngineerReportProfile.DEFAULT.name,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = EngineerReportProfile.DEFAULT.company,
                                fontSize = 11.5.sp,
                                color = ElectricBluePrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Phone: ${EngineerReportProfile.DEFAULT.contactPhone}",
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                            Text(
                                text = "Email: ${EngineerReportProfile.DEFAULT.email}",
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                            Text(
                                text = "Address: ${EngineerReportProfile.DEFAULT.officialAddress}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }

        // Section 3: App Features & Menu (All items from top menu)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Data & History",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsMenuItemRow(
                        title = "Calculation History",
                        subtitle = "View, search, or delete past calculation logs",
                        icon = Icons.Default.Timeline,
                        iconColor = Color(0xFF1565C0),
                        onClick = onNavigateToHistory
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    SettingsMenuItemRow(
                        title = "Notifications & Alerts",
                        subtitle = "View system notices and engineering updates",
                        icon = Icons.Default.Notifications,
                        iconColor = Color(0xFFE65100),
                        onClick = onNavigateToNotification
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    SettingsMenuItemRow(
                        title = "Developer Information",
                        subtitle = "Developer background, qualifications & contacts",
                        icon = Icons.Default.Person,
                        iconColor = Color(0xFF0D47A1),
                        onClick = onNavigateToDeveloper
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    SettingsMenuItemRow(
                        title = "About Application",
                        subtitle = "Version, purpose, electrical standards & overview",
                        icon = Icons.Default.Info,
                        iconColor = Color(0xFF00695C),
                        onClick = onNavigateToAbout
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    SettingsMenuItemRow(
                        title = "Privacy Policy",
                        subtitle = "Read data security, offline usage & policy terms",
                        icon = Icons.Default.Policy,
                        iconColor = Color(0xFF6A1B9A),
                        onClick = onNavigateToPrivacy
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    SettingsMenuItemRow(
                        title = "Share Application",
                        subtitle = "Share Electrical Cal/Pro with engineers and students",
                        icon = Icons.Default.Share,
                        iconColor = Color(0xFF2E7D32),
                        onClick = {
                            ShareUtils.shareText(
                                context,
                                "Electrical Cal/Pro",
                                "Download Electrical Cal/Pro - The all-in-one professional electrical engineering & building material calculation tool with 167 calculators!"
                            )
                        }
                    )
                }
            }
        }

        // Section 4: Report & PDF Settings
        item {
            SettingsCard(title = "Report & PDF Settings", icon = Icons.Default.PictureAsPdf) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Paper Size", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row {
                        listOf("A4", "A5").forEach { size ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = paperSize == size, onClick = { paperSize = size })
                                Text(size, fontSize = 12.sp)
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                SettingsToggleRow("Show App Logo in Header", showLogo) { showLogo = it }
                SettingsToggleRow("Show Engineer Profile in Footer", showDevInfo) { showDevInfo = it }
                SettingsToggleRow("Show Date & Time", showDateTime) { showDateTime = it }
                SettingsToggleRow("Show Formula", showFormula) { showFormula = it }
                SettingsToggleRow("Show Step-by-Step Solution", showSteps) { showSteps = it }
                SettingsToggleRow("Show Notes & Assumptions", showNotes) { showNotes = it }
            }
        }

        // Section 5: Electrical Standards
        item {
            SettingsCard(title = "Electrical Standards", icon = Icons.Default.Rule) {
                Text("Select Default Engineering Standard:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("IEC", "NEC", "BNBC", "General").forEach { std ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedStandard = std }.padding(end = 8.dp)
                        ) {
                            RadioButton(selected = selectedStandard == std, onClick = { selectedStandard = std })
                            Text(std, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section 6: Appearance & Theme
        item {
            SettingsCard(title = "Appearance & Theme", icon = Icons.Default.ColorLens) {
                Text("Select Theme Mode", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("LIGHT" to "Light (Default)", "DARK" to "Dark", "SYSTEM" to "System").forEach { (mode, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    coroutineScope.launch { app.settingsManager.setThemeMode(mode) }
                                }
                                .padding(end = 12.dp)
                        ) {
                            RadioButton(selected = currentTheme == mode, onClick = {
                                coroutineScope.launch { app.settingsManager.setThemeMode(mode) }
                            })
                            Text(label, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Save Settings Button
        item {
            Button(
                onClick = {
                    coroutineScope.launch {
                        app.settingsManager.updatePdfPrintSettings(
                            pdfSettings.copy(
                                paperSize = paperSize,
                                orientation = orientation,
                                showLogo = showLogo,
                                showDeveloperInfo = showDevInfo,
                                showDateTime = showDateTime,
                                showFormula = showFormula,
                                showSteps = showSteps,
                                showNotes = showNotes
                            )
                        )
                        app.settingsManager.updateCalculationDefaults(
                            calcDefaults.copy(standard = selectedStandard)
                        )
                        Toast.makeText(context, "Settings saved successfully", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_save_settings"),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Settings", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsMenuItemRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                lineHeight = 14.sp
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 12.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = ElectricBluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
            content()
        }
    }
}
