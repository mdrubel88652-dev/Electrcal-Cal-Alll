package com.example.ui.screens.calculators

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ElectricalApp
import com.example.data.calculator.ElectricalFormulas.fmt
import com.example.data.database.HistoryEntity
import com.example.data.datastore.DeveloperReportSettings
import com.example.data.datastore.PdfPrintSettings
import com.example.data.datastore.TechnicianReportProfile
import com.example.data.model.*
import com.example.pdf.PdfReportGenerator
import com.example.print.PrintManagerHelper
import com.example.ui.theme.*
import com.example.utils.ShareUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolarSystemCalculatorView(
    calculator: CalculatorDefinition,
    onNavigateBack: () -> Unit,
    app: ElectricalApp,
    devSettings: DeveloperReportSettings,
    pdfSettings: PdfPrintSettings,
    techProfile: TechnicianReportProfile
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 1. Core Energy Inputs
    var dailyEnergyStr by remember { mutableStateOf("10") } // kWh/day
    var peakSunHoursStr by remember { mutableStateOf("4.5") } // h/day
    var backupHoursStr by remember { mutableStateOf("6") } // hours
    var backupLoadStr by remember { mutableStateOf("") } // kW or empty (derived)
    var backupLoadUnit by remember { mutableStateOf("kW") } // "kW" or "W"

    // 2. System Type & Voltage
    var selectedSystemType by remember { mutableStateOf(SolarSystemType.OFF_GRID) }
    var selectedSystemVoltage by remember { mutableStateOf(48.0) }
    var customVoltageStr by remember { mutableStateOf("48") }
    var isCustomVoltage by remember { mutableStateOf(false) }

    // 3. Solar Panel Selection
    var selectedPanelWattage by remember { mutableStateOf(550.0) }
    var customPanelWattageStr by remember { mutableStateOf("550") }
    var isCustomPanelWattage by remember { mutableStateOf(false) }
    var perfFactorStr by remember { mutableStateOf("80") } // 80%

    // 4. Battery Inputs
    var selectedBatteryType by remember { mutableStateOf(SolarBatteryType.LIFEPO4) }
    var batteryUnitVoltageStr by remember { mutableStateOf("12") }
    var batteryUnitAhStr by remember { mutableStateOf("200") }
    var dodPercentStr by remember { mutableStateOf("90") }
    var batteryMarginStr by remember { mutableStateOf("10") }

    // 5. Inverter Inputs
    var peakLoadStr by remember { mutableStateOf("3.5") } // kW
    var powerFactorStr by remember { mutableStateOf("0.90") }
    var inverterEfficiencyStr by remember { mutableStateOf("90") }
    var inverterMarginStr by remember { mutableStateOf("20") }
    var selectedOutputPhase by remember { mutableStateOf("Single Phase 230V") }

    // 6. Charge Controller
    var selectedControllerType by remember { mutableStateOf("MPPT") }

    // 7. Optional PV electrical specs for string configuration
    var showDetailedPvSpecs by remember { mutableStateOf(false) }
    var panelVocStr by remember { mutableStateOf("") }
    var panelVmpStr by remember { mutableStateOf("") }
    var panelIscStr by remember { mutableStateOf("") }
    var panelImpStr by remember { mutableStateOf("") }
    var mpptMaxVoltStr by remember { mutableStateOf("") }
    var mpptMinVoltStr by remember { mutableStateOf("") }
    var mpptMaxCurrentStr by remember { mutableStateOf("") }

    // Result States
    var designResult by remember { mutableStateOf<SolarDesignResult?>(null) }
    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var isSaved by remember { mutableStateOf(false) }

    fun runCalculation() {
        val dailyEnergy = dailyEnergyStr.toDoubleOrNull() ?: 10.0
        val psh = peakSunHoursStr.toDoubleOrNull() ?: 4.5
        val backupH = backupHoursStr.toDoubleOrNull() ?: 6.0

        val backupLoadRaw = backupLoadStr.toDoubleOrNull() ?: 0.0
        val backupLoadKw = if (backupLoadRaw > 0) {
            if (backupLoadUnit == "W") backupLoadRaw / 1000.0 else backupLoadRaw
        } else {
            0.0 // auto derived from dailyEnergy / 24
        }

        val sysVolt = if (isCustomVoltage) {
            customVoltageStr.toDoubleOrNull() ?: 48.0
        } else {
            selectedSystemVoltage
        }

        val panelW = if (isCustomPanelWattage) {
            customPanelWattageStr.toDoubleOrNull() ?: 550.0
        } else {
            selectedPanelWattage
        }

        val perfFactor = perfFactorStr.toDoubleOrNull() ?: 80.0
        val battUnitV = batteryUnitVoltageStr.toDoubleOrNull() ?: 12.0
        val battUnitAh = batteryUnitAhStr.toDoubleOrNull() ?: 200.0
        val dod = dodPercentStr.toDoubleOrNull() ?: selectedBatteryType.typicalDod
        val battMargin = batteryMarginStr.toDoubleOrNull() ?: 10.0

        val peakLoad = peakLoadStr.toDoubleOrNull() ?: 3.5
        val pf = powerFactorStr.toDoubleOrNull() ?: 0.90
        val invEff = inverterEfficiencyStr.toDoubleOrNull() ?: 90.0
        val invMargin = inverterMarginStr.toDoubleOrNull() ?: 20.0

        val voc = panelVocStr.toDoubleOrNull()
        val vmp = panelVmpStr.toDoubleOrNull()
        val isc = panelIscStr.toDoubleOrNull()
        val imp = panelImpStr.toDoubleOrNull()
        val mpptMaxV = mpptMaxVoltStr.toDoubleOrNull()
        val mpptMinV = mpptMinVoltStr.toDoubleOrNull()
        val mpptMaxI = mpptMaxCurrentStr.toDoubleOrNull()

        val inputs = SolarDesignInputs(
            dailyEnergyKwh = dailyEnergy,
            peakSunHours = psh,
            backupHours = backupH,
            backupLoadKw = backupLoadKw,
            systemType = selectedSystemType,
            systemVoltage = sysVolt,
            panelWattage = panelW,
            perfFactorPercent = perfFactor,
            batteryType = selectedBatteryType,
            batteryUnitVoltage = battUnitV,
            batteryUnitAh = battUnitAh,
            dodPercent = dod,
            batteryMarginPercent = battMargin,
            peakLoadKw = peakLoad,
            powerFactor = pf,
            inverterEffPercent = invEff,
            inverterMarginPercent = invMargin,
            outputPhase = selectedOutputPhase,
            controllerType = selectedControllerType,
            panelVoc = voc,
            panelVmp = vmp,
            panelIsc = isc,
            panelImp = imp,
            mpptMaxVoltage = mpptMaxV,
            mpptMinVoltage = mpptMinV,
            mpptMaxCurrent = mpptMaxI
        )

        val res = SolarSystemDesignEngine.calculate(inputs)
        designResult = res

        val calcSteps = res.steps.mapIndexed { idx, st ->
            CalculationStep(idx + 1, "Engineering Stage ${idx + 1}", "", "", st)
        }

        val secondaryList = listOf(
            "Recommended PV Array" to "${fmt(res.actualPvArrayPowerKw)} kW (${res.panelsCount} × ${res.panelWattage.roundToInt()}W)",
            "Inverter Size" to "${fmt(res.recommendedInverterKva)} kVA (${fmt(res.recommendedInverterContinuousKw)} kW continuous)",
            "Battery Bank" to "${res.actualBankVoltage.roundToInt()}V / ${res.actualBankAh.roundToInt()}Ah (${res.totalBatteries} Units: ${res.batteriesInSeries}S × ${res.parallelStrings}P)",
            "Estimated Backup" to "${res.estimatedBackupHours.roundToInt()}h ${res.estimatedBackupMinutes}m Runtime",
            "Charge Controller" to if (res.controllerQuantity > 1) {
                "${res.controllerQuantity} × ${res.perControllerAmps.roundToInt()}A ${res.controllerType}"
            } else {
                "${res.recommendedControllerAmps.roundToInt()}A ${res.controllerType}"
            }
        )

        calculationResult = CalculationResult(
            primaryValue = fmt(res.actualPvArrayPowerKw),
            primaryUnit = "kW (Recommended PV Array)",
            formulaUsed = "P_pv = (Daily_kWh / Derate_0.80) / Peak_Sun_Hours | Bank_Ah = Backup_Wh / (V_sys × DoD × η_inv)",
            steps = calcSteps,
            secondaryResults = secondaryList,
            notes = res.assumptions,
            standardBasis = "IEC 62548 / NEC Article 690 / IEEE 1561 Solar PV Standards"
        )
        isSaved = false
    }

    // Run initial calculation
    LaunchedEffect(Unit) {
        runCalculation()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Solar System Size Calculation All In",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        Text(
                            text = "Solar PV System Design & Backup Calculation",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        dailyEnergyStr = "10"
                        peakSunHoursStr = "4.5"
                        backupHoursStr = "6"
                        backupLoadStr = ""
                        selectedSystemType = SolarSystemType.OFF_GRID
                        selectedSystemVoltage = 48.0
                        isCustomVoltage = false
                        selectedPanelWattage = 550.0
                        isCustomPanelWattage = false
                        perfFactorStr = "80"
                        selectedBatteryType = SolarBatteryType.LIFEPO4
                        batteryUnitVoltageStr = "12"
                        batteryUnitAhStr = "200"
                        dodPercentStr = "90"
                        batteryMarginStr = "10"
                        peakLoadStr = "3.5"
                        powerFactorStr = "0.90"
                        inverterEfficiencyStr = "90"
                        inverterMarginStr = "20"
                        selectedOutputPhase = "Single Phase 230V"
                        selectedControllerType = "MPPT"
                        runCalculation()
                        Toast.makeText(context, "Reset to standard engineering values", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Inputs")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ==========================================
            // 1. SOLAR ENERGY INPUT SECTION
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = ElectricAmber, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SOLAR ENERGY INPUT",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 1. Daily Energy Consumption
                    OutlinedTextField(
                        value = dailyEnergyStr,
                        onValueChange = {
                            dailyEnergyStr = it
                            runCalculation()
                        },
                        label = { Text("Daily Energy Consumption (kWh/day)") },
                        trailingIcon = { Text("kWh/day", modifier = Modifier.padding(end = 12.dp), style = MaterialTheme.typography.bodySmall) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("solar_daily_energy_input"),
                        singleLine = true
                    )

                    // 2. Average Peak Sun Hours
                    OutlinedTextField(
                        value = peakSunHoursStr,
                        onValueChange = {
                            peakSunHoursStr = it
                            runCalculation()
                        },
                        label = { Text("Average Peak Sun Hours (h/day)") },
                        trailingIcon = { Text("h/day", modifier = Modifier.padding(end = 12.dp), style = MaterialTheme.typography.bodySmall) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("solar_psh_input"),
                        singleLine = true
                    )

                    // 3. Backup System Hours
                    OutlinedTextField(
                        value = backupHoursStr,
                        onValueChange = {
                            backupHoursStr = it
                            runCalculation()
                        },
                        label = { Text("Backup System Hours (hours)") },
                        trailingIcon = { Text("Hours", modifier = Modifier.padding(end = 12.dp), style = MaterialTheme.typography.bodySmall) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("solar_backup_hours_input"),
                        singleLine = true
                    )

                    // 4. Backup Load (kW or W)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = backupLoadStr,
                            onValueChange = {
                                backupLoadStr = it
                                runCalculation()
                            },
                            label = { Text("Backup Load (Optional)") },
                            placeholder = { Text("e.g. 1.0 (derived if blank)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("solar_backup_load_input"),
                            singleLine = true
                        )
                        Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface)) {
                            FilterChip(
                                selected = backupLoadUnit == "kW",
                                onClick = { backupLoadUnit = "kW"; runCalculation() },
                                label = { Text("kW") }
                            )
                            FilterChip(
                                selected = backupLoadUnit == "W",
                                onClick = { backupLoadUnit = "W"; runCalculation() },
                                label = { Text("W") }
                            )
                        }
                    }
                    Text(
                        text = if (backupLoadStr.isBlank()) {
                            "ℹ If left blank, backup load is automatically calculated from average hourly load: ${(dailyEnergyStr.toDoubleOrNull() ?: 10.0) / 24.0} kW"
                        } else {
                            "✓ Using explicit dedicated backup load in calculation."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ==========================================
            // 2. SYSTEM TYPE & VOLTAGE
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "SYSTEM TYPE & BATTERY BANK VOLTAGE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    // System Type Dropdown/Selector
                    var typeExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSystemType.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("System Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false }
                        ) {
                            SolarSystemType.values().forEach { st ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(st.label, fontWeight = FontWeight.Bold)
                                            Text(st.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        selectedSystemType = st
                                        typeExpanded = false
                                        runCalculation()
                                    }
                                )
                            }
                        }
                    }

                    // System Voltage Chips
                    Text(
                        text = "System / Battery Bank Voltage:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(12.0, 24.0, 48.0, 96.0).forEach { volt ->
                            FilterChip(
                                selected = !isCustomVoltage && selectedSystemVoltage == volt,
                                onClick = {
                                    isCustomVoltage = false
                                    selectedSystemVoltage = volt
                                    runCalculation()
                                },
                                label = { Text("${volt.roundToInt()}V") }
                            )
                        }
                        FilterChip(
                            selected = isCustomVoltage,
                            onClick = {
                                isCustomVoltage = true
                                runCalculation()
                            },
                            label = { Text("Custom") }
                        )
                    }

                    if (isCustomVoltage) {
                        OutlinedTextField(
                            value = customVoltageStr,
                            onValueChange = {
                                customVoltageStr = it
                                runCalculation()
                            },
                            label = { Text("Custom Bank Voltage (V)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // ==========================================
            // 3. SOLAR PANEL SELECTION
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "SOLAR PANEL SPECIFICATION",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    var panelExpanded by remember { mutableStateOf(false) }
                    val standardWattages = listOf(100.0, 150.0, 200.0, 250.0, 300.0, 330.0, 350.0, 400.0, 450.0, 500.0, 550.0, 580.0, 600.0)

                    ExposedDropdownMenuBox(
                        expanded = panelExpanded,
                        onExpandedChange = { panelExpanded = !panelExpanded }
                    ) {
                        OutlinedTextField(
                            value = if (isCustomPanelWattage) "Custom (${customPanelWattageStr} W)" else "${selectedPanelWattage.roundToInt()} W",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Panel Wattage (W)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = panelExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = panelExpanded,
                            onDismissRequest = { panelExpanded = false }
                        ) {
                            standardWattages.forEach { w ->
                                DropdownMenuItem(
                                    text = { Text("${w.roundToInt()} W (Standard PV Module)") },
                                    onClick = {
                                        isCustomPanelWattage = false
                                        selectedPanelWattage = w
                                        panelExpanded = false
                                        runCalculation()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Custom Wattage...") },
                                onClick = {
                                    isCustomPanelWattage = true
                                    panelExpanded = false
                                }
                            )
                        }
                    }

                    if (isCustomPanelWattage) {
                        OutlinedTextField(
                            value = customPanelWattageStr,
                            onValueChange = {
                                customPanelWattageStr = it
                                runCalculation()
                            },
                            label = { Text("Enter Custom Panel Wattage (W)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // System Performance Factor
                    OutlinedTextField(
                        value = perfFactorStr,
                        onValueChange = {
                            perfFactorStr = it
                            runCalculation()
                        },
                        label = { Text("System Performance / Derating Factor (%)") },
                        trailingIcon = { Text("%", modifier = Modifier.padding(end = 12.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text(
                        text = "Engineering standard default is 80%. Default value is editable. Shows in report.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ==========================================
            // 4. BATTERY SELECTION & SAFETY MARGIN
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "BATTERY STORAGE SPECIFICATION",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    // Battery Type Dropdown
                    var battTypeExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = battTypeExpanded,
                        onExpandedChange = { battTypeExpanded = !battTypeExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedBatteryType.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Battery Chemistry / Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = battTypeExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = battTypeExpanded,
                            onDismissRequest = { battTypeExpanded = false }
                        ) {
                            SolarBatteryType.values().forEach { bt ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(bt.label, fontWeight = FontWeight.Bold)
                                            Text("Recommended DoD: ${bt.typicalDod.roundToInt()}% • ${bt.description}", fontSize = 11.sp)
                                        }
                                    },
                                    onClick = {
                                        selectedBatteryType = bt
                                        dodPercentStr = bt.typicalDod.roundToInt().toString()
                                        battTypeExpanded = false
                                        runCalculation()
                                    }
                                )
                            }
                        }
                    }

                    // Unit Battery Voltage & Capacity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = batteryUnitVoltageStr,
                            onValueChange = {
                                batteryUnitVoltageStr = it
                                runCalculation()
                            },
                            label = { Text("Unit Battery Voltage") },
                            trailingIcon = { Text("V") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = batteryUnitAhStr,
                            onValueChange = {
                                batteryUnitAhStr = it
                                runCalculation()
                            },
                            label = { Text("Unit Capacity") },
                            trailingIcon = { Text("Ah") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Depth of Discharge & Battery Design Margin
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = dodPercentStr,
                            onValueChange = {
                                dodPercentStr = it
                                runCalculation()
                            },
                            label = { Text("Depth of Discharge") },
                            trailingIcon = { Text("%") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = batteryMarginStr,
                            onValueChange = {
                                batteryMarginStr = it
                                runCalculation()
                            },
                            label = { Text("Design Margin") },
                            trailingIcon = { Text("%") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Text(
                        text = "Maximum recommended DoD used in calculation: $dodPercentStr%. Battery design margin: $batteryMarginStr%.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ==========================================
            // 5. INVERTER & CHARGE CONTROLLER INPUTS
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "INVERTER & CHARGE CONTROLLER",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = peakLoadStr,
                            onValueChange = {
                                peakLoadStr = it
                                runCalculation()
                            },
                            label = { Text("Peak / Max AC Load") },
                            trailingIcon = { Text("kW") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = powerFactorStr,
                            onValueChange = {
                                powerFactorStr = it
                                runCalculation()
                            },
                            label = { Text("Power Factor") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = inverterEfficiencyStr,
                            onValueChange = {
                                inverterEfficiencyStr = it
                                runCalculation()
                            },
                            label = { Text("Inverter Efficiency") },
                            trailingIcon = { Text("%") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = inverterMarginStr,
                            onValueChange = {
                                inverterMarginStr = it
                                runCalculation()
                            },
                            label = { Text("Inverter Margin") },
                            trailingIcon = { Text("%") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Phase & Output Voltage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedOutputPhase == "Single Phase 230V",
                            onClick = { selectedOutputPhase = "Single Phase 230V"; runCalculation() },
                            label = { Text("1-Phase 230V") }
                        )
                        FilterChip(
                            selected = selectedOutputPhase == "Three Phase 400V",
                            onClick = { selectedOutputPhase = "Three Phase 400V"; runCalculation() },
                            label = { Text("3-Phase 400V") }
                        )
                    }

                    // Charge Controller Type
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Charge Controller:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                        FilterChip(
                            selected = selectedControllerType == "MPPT",
                            onClick = { selectedControllerType = "MPPT"; runCalculation() },
                            label = { Text("MPPT") }
                        )
                        FilterChip(
                            selected = selectedControllerType == "PWM",
                            onClick = { selectedControllerType = "PWM"; runCalculation() },
                            label = { Text("PWM") }
                        )
                    }
                    Text(
                        text = "MPPT is recommended for higher efficiency solar system designs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElectricBluePrimary
                    )
                }
            }

            // ==========================================
            // 6. DETAILED PV SPECS (OPTIONAL ACCORDION)
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDetailedPvSpecs = !showDetailedPvSpecs },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Detailed PV String & Inverter MPPT Specs (Optional)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Required only for series/parallel string voltage validation",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            if (showDetailedPvSpecs) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }

                    AnimatedVisibility(visible = showDetailedPvSpecs) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Panel Electrical Specs (STC):", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = panelVocStr,
                                    onValueChange = { panelVocStr = it; runCalculation() },
                                    label = { Text("Voc (V)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = panelVmpStr,
                                    onValueChange = { panelVmpStr = it; runCalculation() },
                                    label = { Text("Vmp (V)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = panelIscStr,
                                    onValueChange = { panelIscStr = it; runCalculation() },
                                    label = { Text("Isc (A)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = panelImpStr,
                                    onValueChange = { panelImpStr = it; runCalculation() },
                                    label = { Text("Imp (A)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Inverter MPPT Specs:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = mpptMaxVoltStr,
                                    onValueChange = { mpptMaxVoltStr = it; runCalculation() },
                                    label = { Text("Max PV V (V)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = mpptMinVoltStr,
                                    onValueChange = { mpptMinVoltStr = it; runCalculation() },
                                    label = { Text("MPPT Min (V)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = mpptMaxCurrentStr,
                                    onValueChange = { mpptMaxCurrentStr = it; runCalculation() },
                                    label = { Text("Max I (A)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }

            // Calculate Button
            Button(
                onClick = { runCalculation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("solar_calculate_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CALCULATE COMPLETE SOLAR SYSTEM DESIGN", fontWeight = FontWeight.Bold)
            }

            // ==========================================
            // 7. RESULTS DISPLAY SECTION
            // ==========================================
            val res = designResult
            if (res != null) {

                // MAIN RESULT CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        // System Type Badge & Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SOLAR SYSTEM DESIGN",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = ElectricBluePrimary
                                )
                                Text(
                                    text = "${selectedSystemType.label} • ${selectedOutputPhase}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = "${res.actualPvArrayPowerKw} kW PV",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = ElectricAmber, modifier = Modifier.size(16.dp))
                                }
                            )
                        }

                        // Backup Runtime Alert Banner
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = if (res.backupRequirementSatisfied) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (res.backupRequirementSatisfied) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (res.backupRequirementSatisfied) Color(0xFF2E7D32) else Color(0xFFE65100),
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Estimated Backup Runtime: ${res.estimatedBackupHours.toInt()}h ${res.estimatedBackupMinutes}m",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (res.backupRequirementSatisfied) Color(0xFF1B5E20) else Color(0xFFBF360C)
                                    )
                                    Text(
                                        text = if (res.backupRequirementSatisfied) {
                                            "✓ Requirement satisfied (Requested: ${backupHoursStr} hours at ${fmt(res.effectiveBackupLoadKw)} kW load)"
                                        } else {
                                            "⚠ Additional battery capacity required to satisfy full ${backupHoursStr} hours"
                                        },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        HorizontalDivider()

                        // 4 Major Highlights Grid
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                            // Row 1: PV Array & Inverter
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Solar Panels Card
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("SOLAR PANELS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricBluePrimary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${fmt(res.actualPvArrayPowerKw)} kW", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                        Text("${res.panelsCount} × ${res.panelWattage.roundToInt()}W Panels", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Req: ${fmt(res.requiredPvPowerKw)} kW", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                // Inverter Card
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("INVERTER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricBluePrimary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${fmt(res.recommendedInverterKva)} kVA", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                        Text("${fmt(res.recommendedInverterContinuousKw)} kW continuous", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Calc: ${fmt(res.calculatedInverterKva)} kVA", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            // Row 2: Battery Bank & Charge Controller
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Battery Bank Card
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("BATTERY BANK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricBluePrimary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${res.actualBankVoltage.roundToInt()}V / ${res.actualBankAh.roundToInt()}Ah", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                                        Text("${res.totalBatteries} Batteries (${res.batteriesInSeries}S × ${res.parallelStrings}P)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Usable: ${fmt(res.usableBatteryEnergyKwh)} kWh", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                // Charge Controller Card
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("CHARGE CONTROLLER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricBluePrimary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            if (res.controllerQuantity > 1) {
                                                "${res.controllerQuantity} × ${res.perControllerAmps.roundToInt()}A"
                                            } else {
                                                "${res.recommendedControllerAmps.roundToInt()}A"
                                            },
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text("${res.controllerType} Controller", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Calculated: ${fmt(res.calculatedControllerCurrentAmps)}A", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        HorizontalDivider()

                        // Detailed Battery Series / Parallel Logic Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("BATTERY BANK CONFIGURATION EXPLANATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(
                                "• Series Arrangement: ${res.batteriesInSeries} in series multiplies voltage (${res.batteriesInSeries} × ${batteryUnitVoltageStr}V = ${res.actualBankVoltage.roundToInt()}V System).",
                                fontSize = 12.sp
                            )
                            Text(
                                "• Parallel Arrangement: ${res.parallelStrings} strings in parallel multiplies capacity (${res.parallelStrings} × ${batteryUnitAhStr}Ah = ${res.actualBankAh.roundToInt()}Ah Bank).",
                                fontSize = 12.sp
                            )
                            Text(
                                "• Total Units: ${res.batteriesInSeries} Series × ${res.parallelStrings} Parallel = ${res.totalBatteries} Batteries total.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // ==========================================
                // 8. SYSTEM DESIGN DIAGRAM
                // ==========================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountTree, contentDescription = null, tint = ElectricBluePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SYSTEM DESIGN SCHEMATIC DIAGRAM",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Text(
                            text = "Adapted for: ${selectedSystemType.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Diagram blocks based on selected system type
                        when (selectedSystemType) {
                            SolarSystemType.OFF_GRID -> {
                                OffGridDiagram(res)
                            }
                            SolarSystemType.HYBRID -> {
                                HybridDiagram(res)
                            }
                            SolarSystemType.SOLAR_BATTERY_BACKUP, SolarSystemType.GRID_TIED_BACKUP -> {
                                GridTiedBackupDiagram(res)
                            }
                        }
                    }
                }

                // ==========================================
                // 9. PV STRING CONFIGURATION
                // ==========================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "PV ARRAY STRING CONFIGURATION",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        val pvCfg = res.pvStringConfig
                        if (pvCfg != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Panels in Series: ${pvCfg.panelsInSeries}", fontWeight = FontWeight.Bold)
                                    Text("Parallel Strings: ${pvCfg.parallelStrings}")
                                    Text("Total Panels: ${pvCfg.totalPanels}")
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("String Voc: ${fmt(pvCfg.stringVoc)} V", fontWeight = FontWeight.Bold)
                                    Text("String Vmp: ${fmt(pvCfg.stringVmp)} V")
                                    Text("Array Imp: ${fmt(pvCfg.arrayImp)} A")
                                }
                            }
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = if (pvCfg.isVoltageValid && pvCfg.isCurrentValid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            ) {
                                Text(
                                    text = pvCfg.notes,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(8.dp),
                                    color = if (pvCfg.isVoltageValid && pvCfg.isCurrentValid) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                                )
                            }
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = ElectricBluePrimary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = res.pvConfigMissingMessage ?: "Detailed PV string configuration requires panel electrical specifications and inverter MPPT input specifications.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 10. STEP-BY-STEP CALCULATION BREAKDOWN
                // ==========================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "STEP-BY-STEP ENGINEERING SOLUTION",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        res.steps.forEach { stepText ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = stepText,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(10.dp),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // 11. FORMULAS & ASSUMPTIONS
                // ==========================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "FORMULAS & ENGINEERING STANDARDS",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        res.formulas.forEach { (name, formula) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                formula,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Text("DESIGN ASSUMPTIONS:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        res.assumptions.forEach { ass ->
                            Text("• $ass", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (res.warnings.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Text("ENGINEERING WARNINGS:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                            res.warnings.forEach { w ->
                                Text("⚠ $w", fontSize = 11.sp, color = Color(0xFFC62828))
                            }
                        }
                    }
                }

                // ==========================================
                // 12. ACTIONS TOOLBAR (SAVE, COPY, SHARE, PDF, PRINT)
                // ==========================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Save History
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())
                                    val entity = HistoryEntity(
                                        calculatorId = calculator.id,
                                        calculatorName = calculator.name,
                                        level = calculator.level.displayName,
                                        category = calculator.category.displayName,
                                        formattedDate = dateStr,
                                        inputSummary = "Daily: ${dailyEnergyStr} kWh | PSH: ${peakSunHoursStr}h | Backup: ${backupHoursStr}h | Load: ${fmt(res.effectiveBackupLoadKw)} kW",
                                        primaryResult = fmt(res.actualPvArrayPowerKw),
                                        resultUnit = "kW (${res.panelsCount} × ${res.panelWattage.roundToInt()}W Panels)",
                                        formula = "P_pv = (Daily_kWh / Derate_0.80) / PSH",
                                        stepSummary = "${res.panelsCount} Panels | ${fmt(res.recommendedInverterKva)} kVA Inverter | ${res.actualBankVoltage.roundToInt()}V/${res.actualBankAh.roundToInt()}Ah Batt Bank",
                                        notes = "Solar PV System Design: ${fmt(res.actualPvArrayPowerKw)} kW PV array, ${fmt(res.recommendedInverterKva)} kVA inverter, ${res.actualBankVoltage.roundToInt()}V/${res.actualBankAh.roundToInt()}Ah battery bank (${res.totalBatteries} batteries: ${res.batteriesInSeries}S × ${res.parallelStrings}P)."
                                    )
                                    app.historyRepository.insertHistory(entity)
                                    isSaved = true
                                    Toast.makeText(context, "Solar system design saved to history!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                if (isSaved) Icons.Default.BookmarkAdded else Icons.Default.BookmarkBorder,
                                contentDescription = "Save to History",
                                tint = if (isSaved) ElectricBluePrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Copy
                        IconButton(
                            onClick = {
                                val copyText = buildString {
                                    appendLine("=== SOLAR SYSTEM DESIGN REPORT ===")
                                    appendLine("Daily Energy: ${dailyEnergyStr} kWh/day | Peak Sun Hours: ${peakSunHoursStr} h | Backup: ${backupHoursStr} h")
                                    appendLine("Solar PV Array: ${fmt(res.actualPvArrayPowerKw)} kW (${res.panelsCount} × ${res.panelWattage.roundToInt()}W Panels)")
                                    appendLine("Inverter Size: ${fmt(res.recommendedInverterKva)} kVA (${fmt(res.recommendedInverterContinuousKw)} kW continuous, ${selectedOutputPhase})")
                                    appendLine("Battery Bank: ${res.actualBankVoltage.roundToInt()}V / ${res.actualBankAh.roundToInt()}Ah (${res.totalBatteries} Batteries: ${res.batteriesInSeries}S × ${res.parallelStrings}P)")
                                    appendLine("Charge Controller: ${if (res.controllerQuantity > 1) "${res.controllerQuantity} × ${res.perControllerAmps.roundToInt()}A" else "${res.recommendedControllerAmps.roundToInt()}A"} ${res.controllerType}")
                                    appendLine("Estimated Backup: ${res.estimatedBackupHours.toInt()}h ${res.estimatedBackupMinutes}m")
                                    appendLine("Generated by ELECTRICAL CALCULATION ALL")
                                }
                                ShareUtils.copyToClipboard(context, calculator.name, copyText)
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }

                        // Share
                        IconButton(
                            onClick = {
                                val shareText = buildString {
                                    appendLine("SOLAR PV SYSTEM DESIGN")
                                    appendLine("PV Array: ${fmt(res.actualPvArrayPowerKw)} kW (${res.panelsCount} × ${res.panelWattage.roundToInt()}W)")
                                    appendLine("Inverter: ${fmt(res.recommendedInverterKva)} kVA")
                                    appendLine("Battery Bank: ${res.actualBankVoltage.roundToInt()}V / ${res.actualBankAh.roundToInt()}Ah (${res.totalBatteries} Units)")
                                    appendLine("Estimated Runtime: ${res.estimatedBackupHours.toInt()}h ${res.estimatedBackupMinutes}m")
                                    appendLine("ELECTRICAL CALCULATION ALL")
                                }
                                ShareUtils.shareText(context, calculator.name, shareText)
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }

                        // PDF
                        IconButton(
                            onClick = {
                                try {
                                    val inputsMap = mutableMapOf<String, String>()
                                    inputsMap["Daily Energy"] = "${dailyEnergyStr} kWh/day"
                                    inputsMap["Peak Sun Hours"] = "${peakSunHoursStr} h/day"
                                    inputsMap["Backup Duration"] = "${backupHoursStr} Hours"
                                    inputsMap["Backup Load"] = "${fmt(res.effectiveBackupLoadKw)} kW"
                                    inputsMap["System Type"] = selectedSystemType.label
                                    inputsMap["System Voltage"] = "${res.actualBankVoltage.roundToInt()} V"
                                    inputsMap["Panel Rating"] = "${res.panelWattage.roundToInt()} W"
                                    inputsMap["Battery Chemistry"] = selectedBatteryType.label
                                    inputsMap["Unit Battery"] = "${batteryUnitVoltageStr}V / ${batteryUnitAhStr}Ah"
                                    inputsMap["Battery DoD"] = "${dodPercentStr}%"
                                    inputsMap["Inverter Efficiency"] = "${inverterEfficiencyStr}%"
                                    inputsMap["Output Phase"] = selectedOutputPhase
                                    inputsMap["Controller Type"] = selectedControllerType

                                    val cRes = calculationResult ?: return@IconButton
                                    val pdf = PdfReportGenerator.generateCalculationPdf(
                                        context,
                                        calculator,
                                        inputsMap,
                                        cRes,
                                        devSettings,
                                        pdfSettings,
                                        techProfile
                                    )
                                    ShareUtils.sharePdf(context, pdf, "${calculator.name} Report")
                                } catch (e: Exception) {
                                    Toast.makeText(context, "PDF error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Report", tint = Color(0xFFC62828))
                        }

                        // Print
                        IconButton(
                            onClick = {
                                try {
                                    val inputsMap = mutableMapOf<String, String>()
                                    inputsMap["Daily Energy"] = "${dailyEnergyStr} kWh/day"
                                    inputsMap["Peak Sun Hours"] = "${peakSunHoursStr} h/day"
                                    inputsMap["Backup Duration"] = "${backupHoursStr} Hours"
                                    inputsMap["Backup Load"] = "${fmt(res.effectiveBackupLoadKw)} kW"
                                    inputsMap["System Type"] = selectedSystemType.label
                                    inputsMap["System Voltage"] = "${res.actualBankVoltage.roundToInt()} V"
                                    inputsMap["Panel Rating"] = "${res.panelWattage.roundToInt()} W"
                                    inputsMap["Battery Chemistry"] = selectedBatteryType.label
                                    inputsMap["Unit Battery"] = "${batteryUnitVoltageStr}V / ${batteryUnitAhStr}Ah"
                                    inputsMap["Battery DoD"] = "${dodPercentStr}%"
                                    inputsMap["Inverter Efficiency"] = "${inverterEfficiencyStr}%"
                                    inputsMap["Output Phase"] = selectedOutputPhase
                                    inputsMap["Controller Type"] = selectedControllerType

                                    val cRes = calculationResult ?: return@IconButton
                                    val pdf = PdfReportGenerator.generateCalculationPdf(
                                        context,
                                        calculator,
                                        inputsMap,
                                        cRes,
                                        devSettings,
                                        pdfSettings,
                                        techProfile
                                    )
                                    PrintManagerHelper.printPdfFile(context, pdf, calculator.name)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Print error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Print, contentDescription = "Print", tint = ElectricBluePrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OffGridDiagram(res: SolarDesignResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        DiagramNode("SOLAR PANELS (${res.panelsCount} × ${res.panelWattage.roundToInt()}W = ${fmt(res.actualPvArrayPowerKw)} kW)", ElectricAmber)
        DiagramArrow("DC Power")
        DiagramNode("PV COMBINER BOX / DC SPD & ISOLATOR", Color(0xFF546E7A))
        DiagramArrow("DC String Voltage")
        DiagramNode(
            if (res.controllerQuantity > 1) {
                "${res.controllerQuantity} × ${res.perControllerAmps.roundToInt()}A ${res.controllerType} CHARGE CONTROLLERS"
            } else {
                "${res.recommendedControllerAmps.roundToInt()}A ${res.controllerType} CHARGE CONTROLLER"
            },
            ElectricBluePrimary
        )
        DiagramArrow("DC Charging Current")
        DiagramNode("BATTERY BANK (${res.actualBankVoltage.roundToInt()}V / ${res.actualBankAh.roundToInt()}Ah • ${res.totalBatteries} Batt)", Color(0xFF2E7D32))
        DiagramArrow("DC Power Output")
        DiagramNode("INVERTER (${fmt(res.recommendedInverterKva)} kVA / ${fmt(res.recommendedInverterContinuousKw)} kW)", Color(0xFF6A1B9A))
        DiagramArrow("AC Power (${res.inverterPhase})")
        DiagramNode("AC DISTRIBUTION BOARD & ELECTRICAL LOADS", Color(0xFF37474F))
    }
}

@Composable
private fun HybridDiagram(res: SolarDesignResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DiagramNode("SOLAR PV ARRAY\n${fmt(res.actualPvArrayPowerKw)} kW", ElectricAmber)
                DiagramArrow("Solar DC")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DiagramNode("UTILITY GRID\n(Net Metering)", Color(0xFF1565C0))
                DiagramArrow("AC In/Out")
            }
        }

        DiagramNode(
            "HYBRID BI-DIRECTIONAL INVERTER\n(${fmt(res.recommendedInverterKva)} kVA / ${fmt(res.recommendedInverterContinuousKw)} kW)",
            ElectricBluePrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DiagramArrow("Charge / Discharge")
                DiagramNode("BATTERY BANK\n${res.actualBankVoltage.roundToInt()}V / ${res.actualBankAh.roundToInt()}Ah", Color(0xFF2E7D32))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DiagramArrow("AC Supply")
                DiagramNode("CRITICAL BACKUP\n& NORMAL LOADS", Color(0xFF37474F))
            }
        }
    }
}

@Composable
private fun GridTiedBackupDiagram(res: SolarDesignResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        DiagramNode("SOLAR ARRAY (${fmt(res.actualPvArrayPowerKw)} kW)", ElectricAmber)
        DiagramArrow("DC")
        DiagramNode("GRID-TIED INVERTER WITH ESS BACKUP (${fmt(res.recommendedInverterKva)} kVA)", ElectricBluePrimary)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DiagramArrow("DC Bus")
                DiagramNode("BATTERY ESS\n${res.actualBankVoltage.roundToInt()}V / ${res.actualBankAh.roundToInt()}Ah", Color(0xFF2E7D32))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DiagramArrow("Auto-Transfer")
                DiagramNode("CRITICAL LOAD\nSUBPANEL", Color(0xFF6A1B9A))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DiagramArrow("Grid Export")
                DiagramNode("MAIN METER\n& GRID", Color(0xFF1565C0))
            }
        }
    }
}

@Composable
private fun DiagramNode(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.6f)),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun DiagramArrow(label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
    }
}
