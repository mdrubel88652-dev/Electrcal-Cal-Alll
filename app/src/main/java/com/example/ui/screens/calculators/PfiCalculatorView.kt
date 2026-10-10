package com.example.ui.screens.calculators

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.calculator.ElectricalFormulas.fmt
import com.example.data.database.HistoryEntity
import com.example.data.datastore.DeveloperReportSettings
import com.example.data.datastore.PdfPrintSettings
import com.example.data.datastore.TechnicianReportProfile
import com.example.data.model.*
import com.example.ElectricalApp
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
fun PfiCalculatorView(
    calculator: CalculatorDefinition,
    onNavigateBack: () -> Unit,
    app: ElectricalApp,
    devSettings: DeveloperReportSettings,
    pdfSettings: PdfPrintSettings,
    techProfile: TechnicianReportProfile
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Inputs
    var loadValueStr by remember { mutableStateOf("200") }
    var loadUnit by remember { mutableStateOf("kW") } // "kW" or "kVA"
    var existingPfStr by remember { mutableStateOf("0.72") }
    var targetPfStr by remember { mutableStateOf("0.98") }
    var systemVoltageStr by remember { mutableStateOf("400") }
    var phaseSelection by remember { mutableStateOf("3 Phase") }
    var frequencySelection by remember { mutableStateOf("50 Hz") }
    var existingKvarStr by remember { mutableStateOf("0") }

    // Configurable capacitor step sizes
    val availableRatings = remember {
        mutableStateListOf(5.0, 10.0, 12.5, 15.0, 20.0, 25.0, 30.0, 40.0, 50.0)
    }

    var sizingResult by remember { mutableStateOf<PfiBankSizingResult?>(null) }
    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var isSaved by remember { mutableStateOf(false) }

    fun runCalculation() {
        val load = loadValueStr.toDoubleOrNull() ?: 200.0
        val isKva = loadUnit.equals("kVA", ignoreCase = true)
        val pf1 = (existingPfStr.toDoubleOrNull() ?: 0.72).coerceIn(0.10, 0.99)
        val pf2Raw = targetPfStr.toDoubleOrNull() ?: 0.98
        val pf2 = pf2Raw.coerceIn(pf1 + 0.005, 1.0)
        val volt = systemVoltageStr.toDoubleOrNull() ?: 400.0
        val freq = if (frequencySelection.contains("60")) 60 else 50
        val existingKvar = existingKvarStr.toDoubleOrNull() ?: 0.0

        val res = PfiBankOptimizer.calculate(
            loadValue = load,
            isLoadInKva = isKva,
            existingPf = pf1,
            targetPf = pf2,
            systemVoltage = volt,
            frequency = freq,
            phase = phaseSelection,
            existingKvar = existingKvar,
            availableStepRatings = availableRatings.toList()
        )
        sizingResult = res

        val steps = listOf(
            CalculationStep(1, "Existing Power Factor (PF₁)", "cos φ₁", "Direct Input", "%.2f (Lagging)".format(pf1)),
            CalculationStep(2, "Target Power Factor (PF₂)", "cos φ₂", "Design Target", "%.2f (Lagging)".format(pf2)),
            CalculationStep(3, "Calculate Phase Angle φ₁", "cos⁻¹(PF₁)", "cos⁻¹(%.2f)".format(pf1), "%.2f° (%.4f rad)".format(res.phi1Deg, Math.toRadians(res.phi1Deg))),
            CalculationStep(4, "Calculate Phase Angle φ₂", "cos⁻¹(PF₂)", "cos⁻¹(%.2f)".format(pf2), "%.2f° (%.4f rad)".format(res.phi2Deg, Math.toRadians(res.phi2Deg))),
            CalculationStep(5, "Calculate tan φ₁", "tan(%.2f°)".format(res.phi1Deg), "sin φ₁ / cos φ₁", "%.4f".format(res.tanPhi1)),
            CalculationStep(6, "Calculate tan φ₂", "tan(%.2f°)".format(res.phi2Deg), "sin φ₂ / cos φ₂", "%.4f".format(res.tanPhi2)),
            CalculationStep(7, "Required Compensation Qc", "P × (tan φ₁ - tan φ₂)", "%.1f kW × (%.4f - %.4f)".format(res.activePowerKw, res.tanPhi1, res.tanPhi2), "${fmt(res.requiredKvar)} kVAR"),
            CalculationStep(8, "Practical APFC Bank Configuration", "${res.totalStepsCount} Automatic Steps", res.steps.joinToString(" + ") { "${it.kvar.roundToInt()}kVAR" }, "${fmt(res.recommendedBankKvar)} kVAR Total (${res.recommendedControllerSteps}-Step APFC Controller)")
        )

        val secondary = mutableListOf(
            "Active Load (P)" to "${fmt(res.activePowerKw)} kW",
            "Initial PF / Target PF" to "%.2f / %.2f".format(res.existingPf, res.targetPf),
            "Theoretical Required kVAR" to "${fmt(res.requiredKvar)} kVAR",
            "Recommended Installed Bank" to "${fmt(res.recommendedBankKvar)} kVAR",
            "Capacitor Steps Count" to "${res.totalStepsCount} Switching Stages",
            "APFC Controller Specification" to "${res.recommendedControllerSteps}-Step Automatic Relay",
            "Capacitor Units" to "${res.totalCapacitorUnits} Units (${res.capacitorUnitBreakdown})",
            "Capacitor Voltage Rating" to res.recommendedCapacitorVoltageRating,
            "Capacitor Rated Bank Current" to "${fmt(res.capacitorBankRatedCurrentA)} A",
            "Switching Contactor Type" to "AC-6b Capacitor Duty Contactor",
            "Discharge Resistor Duty" to "< 50V within 60 seconds (IEC 60831)",
            "Harmonic Detuned Reactor" to "Not specified / Required based on harmonic study"
        )

        val notes = listOf(
            "Capacitor bank rating selected to achieve target PF (${fmt(pf2)}) without causing leading power factor during light loads.",
            "APFC controller step count (${res.recommendedControllerSteps}-step) matches the actual switching stages for optimal step resolution.",
            "Heavy-duty capacitor voltage rating (${res.recommendedCapacitorVoltageRating}) recommended to withstand voltage rise from prospective detuned reactors.",
            res.harmonicWarning
        )

        calculationResult = CalculationResult(
            primaryValue = fmt(res.recommendedBankKvar),
            primaryUnit = "kVAR (Recommended APFC Bank)",
            formulaUsed = "Qc = P × (tan φ₁ - tan φ₂),  where φ₁ = cos⁻¹(PF₁) and φ₂ = cos⁻¹(PF₂)",
            steps = steps,
            secondaryResults = secondary,
            notes = notes,
            standardBasis = res.standardBasis
        )
        isSaved = false
    }

    LaunchedEffect(Unit) {
        runCalculation()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("pfi_calculator_screen_container")
    ) {
        // 1. Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = LevelBasicBg, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text = "#5 • BASIC",
                            color = LevelBasicColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PFI & Capacitor",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PFI Size Calculation / Capacitor Bank",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Complete APFC Capacitor Bank sizing assistant. Calculates required kVAR, recommends practical step configurations, determines APFC controller stages, capacitor voltage rating, and contactor requirements.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. INPUT PARAMETERS CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PFI INPUT PARAMETERS",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ElectricBluePrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enter electrical load demand and power factor correction targets.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Load value & unit row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = loadValueStr,
                        onValueChange = { loadValueStr = it },
                        label = { Text("Existing Load / Demand") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Unit selector (kW / kVA)
                    Column(modifier = Modifier.width(100.dp)) {
                        Text("Unit", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("kW", "kVA").forEach { u ->
                                val isSel = loadUnit.equals(u, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(if (isSel) ElectricBluePrimary else Color.Transparent)
                                        .clickable { loadUnit = u },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = u,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Existing PF & Target PF
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = existingPfStr,
                        onValueChange = { existingPfStr = it },
                        label = { Text("Existing Power Factor") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = targetPfStr,
                        onValueChange = { targetPfStr = it },
                        label = { Text("Target Power Factor") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Voltage & Frequency & Phase
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = systemVoltageStr,
                        onValueChange = { systemVoltageStr = it },
                        label = { Text("Voltage (V)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Phase selector
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Phase", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("3 Phase", "1 Phase").forEach { ph ->
                                val isSel = phaseSelection == ph
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(if (isSel) ElectricBluePrimary else Color.Transparent)
                                        .clickable { phaseSelection = ph },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ph,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Frequency selector
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Frequency", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("50 Hz", "60 Hz").forEach { f ->
                                val isSel = frequencySelection == f
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(if (isSel) ElectricBluePrimary else Color.Transparent)
                                        .clickable { frequencySelection = f },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = f,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = existingKvarStr,
                    onValueChange = { existingKvarStr = it },
                    label = { Text("Existing Capacitor Bank (kVAR) - Optional") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons: Calculate & Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val l = loadValueStr.toDoubleOrNull()
                            val p1 = existingPfStr.toDoubleOrNull()
                            val p2 = targetPfStr.toDoubleOrNull()
                            if (l == null || l <= 0) {
                                Toast.makeText(context, "Valid load value required", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (p1 == null || p1 <= 0 || p1 > 1.0) {
                                Toast.makeText(context, "Existing PF must be between 0 and 1.0", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (p2 == null || p2 <= p1 || p2 > 1.0) {
                                Toast.makeText(context, "Target PF must be greater than existing PF and <= 1.0", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            runCalculation()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("calculate_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Calculate Capacitor Bank", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            loadValueStr = "200"
                            loadUnit = "kW"
                            existingPfStr = "0.72"
                            targetPfStr = "0.98"
                            systemVoltageStr = "400"
                            phaseSelection = "3 Phase"
                            frequencySelection = "50 Hz"
                            existingKvarStr = "0"
                            runCalculation()
                        },
                        modifier = Modifier
                            .weight(0.5f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }
                }
            }
        }

        // 3. PFI / CAPACITOR BANK RESULT SCREEN
        sizingResult?.let { res ->
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pfi_result_card"),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "PFI / CAPACITOR BANK RESULT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlueSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Summary comparison row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Existing PF:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("%.2f".format(res.existingPf), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Target PF:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("%.2f".format(res.targetPf), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Required Compensation:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${fmt(res.requiredKvar)} kVAR", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Recommended Bank Big Card
                    Surface(
                        color = ElectricBluePrimary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "RECOMMENDED CAPACITOR BANK:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBluePrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = fmt(res.recommendedBankKvar),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ElectricBluePrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "kVAR Installed",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

                    // CAPACITOR STEP CONFIGURATION
                    Text(
                        text = "CAPACITOR STEP CONFIGURATION",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    res.steps.forEach { step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Step ${step.stepNumber}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${step.kvar.roundToInt()} kVAR",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Bank Capacity",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBluePrimary
                        )
                        Text(
                            text = "${fmt(res.recommendedBankKvar)} kVAR",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricBluePrimary
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // CAPACITOR COUNT
                    Text(
                        text = "CAPACITOR COUNT",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${res.totalCapacitorUnits} Capacitors (${res.capacitorUnitBreakdown})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // APFC CONTROLLER
                    Text(
                        text = "APFC CONTROLLER",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${res.recommendedControllerSteps} Step Controller (${res.totalStepsCount} active stages, ${res.recommendedControllerSteps - res.totalStepsCount} spare)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // SYSTEM SPECIFICATIONS
                    Text(
                        text = "SYSTEM & CAPACITOR SPECIFICATIONS",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("• System Voltage: ${res.systemVoltage.roundToInt()} V, ${res.systemFrequency} Hz (${res.phase})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Recommended Capacitor Voltage Rating: ${res.recommendedCapacitorVoltageRating}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Text("• Capacitor Bank Rated Current: ${fmt(res.capacitorBankRatedCurrentA)} A", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Switching Contactor: ${res.contactorSpec}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Discharge Resistor Duty: ${res.dischargeResistorSpec}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // HARMONIC WARNING BOX
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFFFFF7ED),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDBA74)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp)) {
                            Icon(Icons.Default.WarningAmber, contentDescription = "Warning", tint = Color(0xFFEA580C), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Reactor / Harmonic Note:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFC2410C))
                                Text(res.harmonicWarning, fontSize = 11.sp, color = Color(0xFF7C2D12))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Reactor: Not specified / Required based on harmonic study", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFFC2410C))
                            }
                        }
                    }

                    // FORMULA
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Text(
                        text = "Formula:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Text(
                            text = "Qc = P × (tan φ₁ - tan φ₂)\nwhere φ₁ = cos⁻¹(PF₁) and φ₂ = cos⁻¹(PF₂)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 8-STEP SOLUTION
                    calculationResult?.let { cRes ->
                        if (cRes.steps.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Step-by-Step Solution:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Column(modifier = Modifier.padding(top = 4.dp)) {
                                cRes.steps.forEach { step ->
                                    Text(
                                        text = "${step.stepNumber}. ${step.title}: ${step.equation} = ${step.result}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Standard Basis
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Standard / Basis: ${res.standardBasis}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Save
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
                                val summary = "Load: ${loadValueStr} $loadUnit, PF: $existingPfStr -> $targetPfStr, V: ${systemVoltageStr}V | Req: ${fmt(res.requiredKvar)} kVAR"
                                val notesStr = "Steps: ${res.totalStepsCount} (${res.steps.joinToString { "${it.kvar.roundToInt()}kVAR" }}) | Controller: ${res.recommendedControllerSteps}-Step | Bank: ${fmt(res.recommendedBankKvar)} kVAR"
                                val entity = HistoryEntity(
                                    calculatorId = calculator.id,
                                    calculatorName = calculator.name,
                                    level = calculator.level.displayName,
                                    category = calculator.category.displayName,
                                    formattedDate = dateStr,
                                    inputSummary = summary,
                                    primaryResult = fmt(res.recommendedBankKvar),
                                    resultUnit = "kVAR (Recommended Bank)",
                                    formula = "Qc = P × (tan φ₁ - tan φ₂)",
                                    stepSummary = "Steps: ${res.totalStepsCount} (${res.recommendedControllerSteps}-Step Controller) | Required: ${fmt(res.requiredKvar)} kVAR",
                                    notes = notesStr
                                )
                                app.historyRepository.insertHistory(entity)
                                isSaved = true
                                Toast.makeText(context, "Saved to history", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Check else Icons.Default.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (isSaved) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Copy
                    IconButton(
                        onClick = {
                            val copyText = "PFI / Capacitor Bank Sizing\nLoad: ${loadValueStr} $loadUnit\nPF: $existingPfStr -> $targetPfStr\nRequired Compensation: ${fmt(res.requiredKvar)} kVAR\nRecommended Bank: ${fmt(res.recommendedBankKvar)} kVAR\nConfiguration: ${res.steps.joinToString { "Step ${it.stepNumber}: ${it.kvar.roundToInt()} kVAR" }}\nAPFC Controller: ${res.recommendedControllerSteps} Step"
                            ShareUtils.copyToClipboard(context, calculator.name, copyText)
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }

                    // Share
                    IconButton(
                        onClick = {
                            val shareText = "PFI / Capacitor Bank Report\nLoad: ${loadValueStr} $loadUnit\nPF: $existingPfStr -> $targetPfStr\nRequired: ${fmt(res.requiredKvar)} kVAR\nRecommended Bank: ${fmt(res.recommendedBankKvar)} kVAR\nController: ${res.recommendedControllerSteps}-Step APFC\nCalculated with ELECTRICAL CALCULATION ALL"
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
                                inputsMap["Existing Load"] = "${loadValueStr} $loadUnit"
                                inputsMap["Existing PF"] = existingPfStr
                                inputsMap["Target PF"] = targetPfStr
                                inputsMap["System Voltage"] = "${systemVoltageStr} V (${phaseSelection})"
                                inputsMap["Frequency"] = frequencySelection
                                inputsMap["Required kVAR"] = "${fmt(res.requiredKvar)} kVAR"
                                inputsMap["Recommended Bank"] = "${fmt(res.recommendedBankKvar)} kVAR"
                                inputsMap["Capacitor Steps"] = res.steps.joinToString(", ") { "S${it.stepNumber}: ${it.kvar.roundToInt()}kVAR" }
                                inputsMap["APFC Controller"] = "${res.recommendedControllerSteps}-Step Controller"
                                inputsMap["Capacitor Units"] = "${res.totalCapacitorUnits} Units (${res.capacitorUnitBreakdown})"
                                inputsMap["Capacitor Voltage"] = res.recommendedCapacitorVoltageRating

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
                                inputsMap["Existing Load"] = "${loadValueStr} $loadUnit"
                                inputsMap["Existing PF"] = existingPfStr
                                inputsMap["Target PF"] = targetPfStr
                                inputsMap["System Voltage"] = "${systemVoltageStr} V (${phaseSelection})"
                                inputsMap["Frequency"] = frequencySelection
                                inputsMap["Required kVAR"] = "${fmt(res.requiredKvar)} kVAR"
                                inputsMap["Recommended Bank"] = "${fmt(res.recommendedBankKvar)} kVAR"
                                inputsMap["Capacitor Steps"] = res.steps.joinToString(", ") { "S${it.stepNumber}: ${it.kvar.roundToInt()}kVAR" }
                                inputsMap["APFC Controller"] = "${res.recommendedControllerSteps}-Step Controller"
                                inputsMap["Capacitor Units"] = "${res.totalCapacitorUnits} Units (${res.capacitorUnitBreakdown})"
                                inputsMap["Capacitor Voltage"] = res.recommendedCapacitorVoltageRating

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
