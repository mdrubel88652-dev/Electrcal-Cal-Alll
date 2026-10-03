package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ElectricalApp
import com.example.data.datastore.CalculationDefaults
import com.example.data.datastore.DeveloperReportSettings
import com.example.data.datastore.PdfPrintSettings
import com.example.ui.theme.ElectricBluePrimary
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val app = ElectricalApp.instance

    val currentTheme by app.settingsManager.themeModeFlow.collectAsState(initial = "LIGHT")
    val devSettings by app.settingsManager.developerSettingsFlow.collectAsState(initial = DeveloperReportSettings())
    val pdfSettings by app.settingsManager.pdfPrintSettingsFlow.collectAsState(initial = PdfPrintSettings())
    val calcDefaults by app.settingsManager.calculationDefaultsFlow.collectAsState(initial = CalculationDefaults())

    // Local form states
    var devName by remember(devSettings) { mutableStateOf(devSettings.developerName) }
    var compName by remember(devSettings) { mutableStateOf(devSettings.companyName) }
    var devPhone by remember(devSettings) { mutableStateOf(devSettings.contactNumber) }
    var devEmail by remember(devSettings) { mutableStateOf(devSettings.email) }
    var devAddress by remember(devSettings) { mutableStateOf(devSettings.address) }

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
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Appearance Section
        item {
            SettingsCard(title = "1. Appearance", icon = Icons.Default.ColorLens) {
                Text("Theme Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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

        // 2. Developer & Report Information
        item {
            SettingsCard(title = "2. Developer & Report Information", icon = Icons.Default.Person) {
                OutlinedTextField(
                    value = devName,
                    onValueChange = { devName = it },
                    label = { Text("Developer / Engineer Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = compName,
                    onValueChange = { compName = it },
                    label = { Text("Company / Organization") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = devPhone,
                    onValueChange = { devPhone = it },
                    label = { Text("Contact Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = devEmail,
                    onValueChange = { devEmail = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = devAddress,
                    onValueChange = { devAddress = it },
                    label = { Text("Official Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // 3. PDF & Print Settings
        item {
            SettingsCard(title = "3. PDF & Print Report Settings", icon = Icons.Default.PictureAsPdf) {
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

                SettingsToggleRow("Show App Logo", showLogo) { showLogo = it }
                SettingsToggleRow("Show Developer / Company Info", showDevInfo) { showDevInfo = it }
                SettingsToggleRow("Show Date & Time", showDateTime) { showDateTime = it }
                SettingsToggleRow("Show Formula", showFormula) { showFormula = it }
                SettingsToggleRow("Show Step-by-Step Solution", showSteps) { showSteps = it }
                SettingsToggleRow("Show Notes & Assumptions", showNotes) { showNotes = it }
            }
        }

        // 4. Standards & Calculations
        item {
            SettingsCard(title = "4. Standards & Calculation Basis", icon = Icons.Default.Rule) {
                Text("Select Default Engineering Standard:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

        // Save Settings Button
        item {
            Button(
                onClick = {
                    coroutineScope.launch {
                        app.settingsManager.updateDeveloperSettings(
                            DeveloperReportSettings(
                                developerName = devName,
                                companyName = compName,
                                contactNumber = devPhone,
                                email = devEmail,
                                address = devAddress
                            )
                        )
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
                    .height(50.dp)
                    .testTag("btn_save_settings"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Settings", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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

@Composable
fun SettingsToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
