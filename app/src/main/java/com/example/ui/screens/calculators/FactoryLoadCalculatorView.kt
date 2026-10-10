package com.example.ui.screens.calculators

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
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
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactoryLoadCalculatorView(
    calculator: CalculatorDefinition,
    onNavigateBack: () -> Unit,
    app: ElectricalApp,
    devSettings: DeveloperReportSettings,
    pdfSettings: PdfPrintSettings,
    techProfile: TechnicianReportProfile
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // System Phase state: "3 Phase" or "1 Phase"
    var systemPhase by remember { mutableStateOf("3 Phase") }

    // Pre-populated default factory loads list (Strict phase isolation: all matching systemPhase)
    val loads = remember {
        mutableStateListOf(
            FactoryLoadItem(name = "Pump Motor", power = 7.5, unit = "kW", quantity = 3, phase = "3 Phase", powerFactor = 0.85),
            FactoryLoadItem(name = "Industrial Blower", power = 3.0, unit = "kW", quantity = 4, phase = "3 Phase", powerFactor = 0.88),
            FactoryLoadItem(name = "Air Compressor", power = 15.0, unit = "kW", quantity = 2, phase = "3 Phase", powerFactor = 0.86)
        )
    }

    // Engineering parameters
    var demandFactorStr by remember { mutableStateOf("0.80") }
    var diversityFactorStr by remember { mutableStateOf("1.15") }
    var avgPfStr by remember { mutableStateOf("0.85") }
    var plantEffStr by remember { mutableStateOf("0.90") }
    var systemVoltageStr by remember { mutableStateOf("400") }

    // Dropdown / Selection states
    var selectedMachine by remember { mutableStateOf<PredefinedFactoryMachine?>(PredefinedFactoryLoadsCatalog.items.first()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var dropdownSearchQuery by remember { mutableStateOf("") }

    // Input state for selected machine
    var pendingPowerStr by remember { mutableStateOf("7.5") }
    var pendingUnit by remember { mutableStateOf("kW") }
    var pendingQtyStr by remember { mutableStateOf("1") }
    var pendingPhase by remember { mutableStateOf("3 Phase") }
    var pendingPfStr by remember { mutableStateOf("0.85") }

    // Custom Load Dialog state
    var showCustomDialog by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }
    var customPowerStr by remember { mutableStateOf("") }
    var customUnit by remember { mutableStateOf("kW") }
    var customQtyStr by remember { mutableStateOf("1") }
    var customPhase by remember { mutableStateOf("3 Phase") }
    var customPfStr by remember { mutableStateOf("0.85") }

    // Edit Load Dialog state
    var editingItem by remember { mutableStateOf<FactoryLoadItem?>(null) }
    var editName by remember { mutableStateOf("") }
    var editPowerStr by remember { mutableStateOf("") }
    var editUnit by remember { mutableStateOf("kW") }
    var editQtyStr by remember { mutableStateOf("1") }
    var editPhase by remember { mutableStateOf("3 Phase") }
    var editPfStr by remember { mutableStateOf("0.85") }

    // Calculation result state
    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var isSaved by remember { mutableStateOf(false) }

    fun runCalculation() {
        val totalKw = loads.sumOf { it.totalKw }
        val df = demandFactorStr.toDoubleOrNull() ?: 0.80
        val divF = diversityFactorStr.toDoubleOrNull() ?: 1.15
        val pf = (avgPfStr.toDoubleOrNull() ?: 0.85).coerceIn(0.1, 1.0)
        val eff = (plantEffStr.toDoubleOrNull() ?: 0.90).coerceIn(0.1, 1.0)
        val is3p = systemPhase.contains("3")
        val v = systemVoltageStr.toDoubleOrNull() ?: if (is3p) 400.0 else 230.0

        val maxDemandKw = totalKw * df
        val diversifiedDemandKw = maxDemandKw / divF
        val demandKva = diversifiedDemandKw / (pf * eff)
        val currentA = if (is3p) {
            (demandKva * 1000.0) / (sqrt(3.0) * v)
        } else {
            (demandKva * 1000.0) / v
        }

        // Select standard transformer with ~20% future margin
        val txKvaTarget = demandKva * 1.20
        val standardTxList = listOf(100, 160, 200, 250, 315, 400, 500, 630, 800, 1000, 1250, 1600, 2000, 2500, 3150)
        val recTx = standardTxList.firstOrNull { it >= txKvaTarget } ?: (ceil(txKvaTarget / 500.0) * 500).toInt()

        val steps = listOf(
            CalculationStep(1, "Total Connected Load", "Σ(Power_i × Qty_i)", "${loads.size} load categories summed ($systemPhase)", "${fmt(totalKw)} kW"),
            CalculationStep(2, "Maximum Demand (kW)", "Total kW × Demand Factor", "${fmt(totalKw)} × $df", "${fmt(maxDemandKw)} kW"),
            CalculationStep(3, "Diversified Demand (kW)", "Max Demand / Diversity Factor", "${fmt(maxDemandKw)} / $divF", "${fmt(diversifiedDemandKw)} kW"),
            CalculationStep(4, "Demand Apparent Power (kVA)", "Demand kW / (PF × Efficiency)", "${fmt(diversifiedDemandKw)} / ($pf × $eff)", "${fmt(demandKva)} kVA"),
            CalculationStep(
                5,
                "Full Load Current at ${v.roundToInt()}V (${if (is3p) "3-Phase" else "1-Phase"})",
                if (is3p) "kVA × 1000 / (√3 × V)" else "kVA × 1000 / V",
                if (is3p) "${fmt(demandKva)} × 1000 / (1.732 × ${v.roundToInt()})" else "${fmt(demandKva)} × 1000 / ${v.roundToInt()}",
                "${fmt(currentA)} A"
            ),
            CalculationStep(6, "Recommended Transformer", "Design kVA with 20% growth margin", "${fmt(txKvaTarget)} kVA", "$recTx kVA Standard Substation Unit")
        )

        val secondary = listOf(
            "Total Connected Load" to "${fmt(totalKw)} kW",
            "Max Demand Load" to "${fmt(maxDemandKw)} kW",
            "Diversified Demand" to "${fmt(diversifiedDemandKw)} kW",
            "Design Apparent Power" to "${fmt(demandKva)} kVA",
            "Operating Current (${if (is3p) "3-Phase" else "1-Phase"})" to "${fmt(currentA)} A",
            "Supply Phase System" to if (is3p) "3 Phase (400V Line-to-Line)" else "1 Phase (230V Line-to-Neutral)",
            "Recommended Transformer" to "$recTx kVA"
        )

        val notes = listOf(
            "Strict Phase Isolation: Single Phase ও Three Phase লোড একই ক্যালকুলেশনে মিশিয়ে হিসাব করা যাবে না।",
            "বর্তমানে সমস্ত লোড $systemPhase হিসেবে এককভাবে হিসাব করা হয়েছে।",
            "Demand Factor ($df) and Diversity Factor ($divF) are calculated per plant operational duty cycle.",
            "Substation transformer sized with 20% margin for motor inrush & future expansion.",
            "Design basis adheres to IEC 60364 & IEEE Industrial Plant Power Standards."
        )

        calculationResult = CalculationResult(
            primaryValue = fmt(demandKva),
            primaryUnit = "kVA (Design Maximum Demand - $systemPhase)",
            formulaUsed = if (is3p) {
                "kVA = (Total Connected kW × DF) / (Diversity × PF × Eff) | I = kVA×1000 / (√3×V)"
            } else {
                "kVA = (Total Connected kW × DF) / (Diversity × PF × Eff) | I = kVA×1000 / V"
            },
            steps = steps,
            secondaryResults = secondary,
            notes = notes,
            standardBasis = "IEC 60364 / IEEE Industrial Standards / BNBC"
        )
        isSaved = false
    }

    // Auto-calculate once
    LaunchedEffect(Unit) {
        runCalculation()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("factory_load_screen_container")
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
                            text = "#1 • BASIC",
                            color = LevelBasicColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Factory & Industrial",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All Factory Load Calculation",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Add factory machines from dropdown or custom entries. Accurately calculates total connected load (kW), maximum demand (kVA), line current, and recommended transformer sizing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Phase Selection Card (Strict phase isolation)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("factory_phase_selection_card"),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBluePrimary.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = ElectricBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Factory Phase System / ফেজ নির্বাচন",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ElectricBluePrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = systemPhase,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBluePrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val is3Sel = systemPhase == "3 Phase"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (is3Sel) ElectricBluePrimary else Color.Transparent)
                            .clickable {
                                systemPhase = "3 Phase"
                                systemVoltageStr = "400"
                                pendingPhase = "3 Phase"
                                customPhase = "3 Phase"
                                loads.forEachIndexed { idx, it -> loads[idx] = it.copy(phase = "3 Phase") }
                                runCalculation()
                            }
                            .testTag("factory_phase_three"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (is3Sel) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "Three Phase (3φ • 400V)",
                                fontSize = 12.sp,
                                fontWeight = if (is3Sel) FontWeight.Bold else FontWeight.Medium,
                                color = if (is3Sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val is1Sel = systemPhase == "1 Phase"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (is1Sel) ElectricBluePrimary else Color.Transparent)
                            .clickable {
                                systemPhase = "1 Phase"
                                systemVoltageStr = "230"
                                pendingPhase = "1 Phase"
                                customPhase = "1 Phase"
                                loads.forEachIndexed { idx, it -> loads[idx] = it.copy(phase = "1 Phase") }
                                runCalculation()
                            }
                            .testTag("factory_phase_single"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (is1Sel) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "Single Phase (1φ • 230V)",
                                fontSize = 12.sp,
                                fontWeight = if (is1Sel) FontWeight.Bold else FontWeight.Medium,
                                color = if (is1Sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(18.dp).padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "গুরুত্বপূর্ণ শর্ত: Single Phase ও Three Phase লোড একই ক্যালকুলেশনে মিশিয়ে হিসাব করা যাবে না।",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (systemPhase == "1 Phase") {
                                    "বর্তমানে সম্পূর্ণ ফ্যাক্টরি লোড Single Phase (1φ Line-to-Neutral 230V) হিসেবে এককভাবে হিসাব হচ্ছে। সূত্র: I = kVA × 1000 / V"
                                } else {
                                    "বর্তমানে সম্পূর্ণ ফ্যাক্টরি লোড Three Phase (3φ Line-to-Line 400V) হিসেবে এককভাবে হিসাব হচ্ছে। সূত্র: I = kVA × 1000 / (√3 × V)"
                                },
                                fontSize = 10.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. ADD FACTORY LOAD SECTION
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ADD FACTORY LOAD",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ElectricBluePrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select Machine / Motor / Load from the predefined catalog or add custom load.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Dropdown trigger
                OutlinedCard(
                    onClick = { isDropdownExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Select Machine / Motor / Load",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedMachine?.name ?: "Select Machine...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                    }
                }

                // Reference notification
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Default load value — editable",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Editable Fields for the selected machine
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pendingPowerStr,
                        onValueChange = { pendingPowerStr = it },
                        label = { Text("Power") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Unit selector (kW / W)
                    Column(modifier = Modifier.width(90.dp)) {
                        Text("Unit", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("kW", "W").forEach { u ->
                                val isSelected = pendingUnit.equals(u, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(if (isSelected) ElectricBluePrimary else Color.Transparent)
                                        .clickable { pendingUnit = u },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = u,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = pendingQtyStr,
                        onValueChange = { pendingQtyStr = it },
                        label = { Text("Qty") },
                        modifier = Modifier.weight(0.7f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Phase & Power Factor row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // System Phase indicator (strictly enforced - no mixing)
                    Surface(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                            Text(
                                text = "Phase: $systemPhase",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedTextField(
                        value = pendingPfStr,
                        onValueChange = { pendingPfStr = it },
                        label = { Text("Power Factor") },
                        modifier = Modifier.weight(0.9f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons for adding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val power = pendingPowerStr.toDoubleOrNull()
                            val qty = pendingQtyStr.toIntOrNull()
                            val pf = pendingPfStr.toDoubleOrNull() ?: 0.85
                            if (power == null || power <= 0.0) {
                                Toast.makeText(context, "Please enter valid power", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (qty == null || qty <= 0) {
                                Toast.makeText(context, "Quantity must be greater than zero", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val name = selectedMachine?.name ?: "Machine"
                            loads.add(
                                FactoryLoadItem(
                                    name = name,
                                    power = power,
                                    unit = pendingUnit,
                                    quantity = qty,
                                    phase = pendingPhase,
                                    powerFactor = pf,
                                    loadType = selectedMachine?.loadType ?: "Motor"
                                )
                            )
                            Toast.makeText(context, "Added $name ($qty)", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Machine", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            customName = ""
                            customPowerStr = ""
                            customUnit = "kW"
                            customQtyStr = "1"
                            customPhase = "3 Phase"
                            customPfStr = "0.85"
                            showCustomDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Custom Load", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. LOAD LIST TABLE / CARDS
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LOAD LIST (${loads.size} Items)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Total: %.2f kW".format(loads.sumOf { it.totalKw }),
                        fontWeight = FontWeight.ExtraBold,
                        color = ElectricBluePrimary,
                        fontSize = 15.sp
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                if (loads.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No loads added yet. Add factory machines from above.", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    loads.forEachIndexed { index, item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = ElectricBluePrimary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = ElectricBluePrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Text(
                                        text = "%.2f kW".format(item.totalKw),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ElectricBluePrimary,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.displayOriginal}  ×  Qty: ${item.quantity}  •  ${item.phase} (PF ${item.powerFactor})",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Action buttons: Duplicate, Edit, Delete
                                    Row {
                                        IconButton(
                                            onClick = {
                                                loads.add(index + 1, item.copy(id = UUID.randomUUID().toString()))
                                                Toast.makeText(context, "Duplicated ${item.name}", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", modifier = Modifier.size(16.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                editingItem = item
                                                editName = item.name
                                                editPowerStr = item.power.toString()
                                                editUnit = item.unit
                                                editQtyStr = item.quantity.toString()
                                                editPhase = item.phase
                                                editPfStr = item.powerFactor.toString()
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                loads.remove(item)
                                                Toast.makeText(context, "Deleted ${item.name}", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = ElectricBluePrimary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL CONNECTED LOAD:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ElectricBluePrimary
                            )
                            Text(
                                text = "%.2f kW".format(loads.sumOf { it.totalKw }),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = ElectricBluePrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. ENGINEERING PARAMETERS SECTION
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ENGINEERING PARAMETERS",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ElectricBluePrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Clearly defined industrial design factors per plant load profile.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = demandFactorStr,
                        onValueChange = { demandFactorStr = it },
                        label = { Text("Demand Factor") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = diversityFactorStr,
                        onValueChange = { diversityFactorStr = it },
                        label = { Text("Diversity Factor") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = avgPfStr,
                        onValueChange = { avgPfStr = it },
                        label = { Text("Plant Power Factor") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = plantEffStr,
                        onValueChange = { plantEffStr = it },
                        label = { Text("Plant Efficiency") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = systemVoltageStr,
                    onValueChange = { systemVoltageStr = it },
                    label = { Text("Supply Voltage (V) - $systemPhase") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (loads.isEmpty()) {
                                Toast.makeText(context, "Please add at least one load", Toast.LENGTH_SHORT).show()
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
                        Text("Calculate Factory Load", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            loads.clear()
                            loads.addAll(
                                if (systemPhase == "3 Phase") {
                                    listOf(
                                        FactoryLoadItem(name = "Pump Motor", power = 7.5, unit = "kW", quantity = 3, phase = "3 Phase", powerFactor = 0.85),
                                        FactoryLoadItem(name = "Industrial Blower", power = 3.0, unit = "kW", quantity = 4, phase = "3 Phase", powerFactor = 0.88),
                                        FactoryLoadItem(name = "Air Compressor", power = 15.0, unit = "kW", quantity = 2, phase = "3 Phase", powerFactor = 0.86)
                                    )
                                } else {
                                    listOf(
                                        FactoryLoadItem(name = "Single-Phase Motor", power = 2.2, unit = "kW", quantity = 3, phase = "1 Phase", powerFactor = 0.85),
                                        FactoryLoadItem(name = "Lighting Circuit", power = 1.5, unit = "kW", quantity = 4, phase = "1 Phase", powerFactor = 0.90),
                                        FactoryLoadItem(name = "Office Equipment", power = 3.0, unit = "kW", quantity = 2, phase = "1 Phase", powerFactor = 0.85)
                                    )
                                }
                            )
                            demandFactorStr = "0.80"
                            diversityFactorStr = "1.15"
                            avgPfStr = "0.85"
                            plantEffStr = "0.90"
                            systemVoltageStr = if (systemPhase == "3 Phase") "400" else "230"
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

        // 5. CALCULATION RESULT CARD
        calculationResult?.let { res ->
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("result_card"),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "FINAL RESULT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlueSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = res.primaryValue,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = res.primaryUnit,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    // Secondary results
                    if (res.secondaryResults.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        for ((label, value) in res.secondaryResults) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = value,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Formula
                    if (res.formulaUsed.isNotEmpty()) {
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
                                text = res.formulaUsed,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Step by step
                    if (res.steps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Step-by-Step Solution:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            res.steps.forEach { step ->
                                Text(
                                    text = "${step.stepNumber}. ${step.title}: ${step.equation} = ${step.result}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Standard & Notes
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Standard / Basis: ${res.standardBasis}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    res.notes.forEach { note ->
                        Text(
                            text = "• $note",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                                val summary = "Loads (${loads.size}): " + loads.joinToString(", ") { "${it.name} ${it.displayOriginal} (x${it.quantity})" } + " | Total: %.2f kW".format(loads.sumOf { it.totalKw })
                                val entity = HistoryEntity(
                                    calculatorId = calculator.id,
                                    calculatorName = calculator.name,
                                    level = calculator.level.displayName,
                                    category = calculator.category.displayName,
                                    formattedDate = dateStr,
                                    inputSummary = summary,
                                    primaryResult = res.primaryValue,
                                    resultUnit = res.primaryUnit,
                                    formula = res.formulaUsed,
                                    stepSummary = res.steps.joinToString(" | ") { it.result }
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
                            val copyText = "All Factory Load Calculation\nTotal Connected Load: %.2f kW\nDesign Demand: ${res.primaryValue} ${res.primaryUnit}\nFormula: ${res.formulaUsed}".format(loads.sumOf { it.totalKw })
                            ShareUtils.copyToClipboard(context, calculator.name, copyText)
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }

                    // Share
                    IconButton(
                        onClick = {
                            val shareText = "All Factory Load Calculation Report\nTotal Connected Load: %.2f kW\nDesign Demand: ${res.primaryValue} ${res.primaryUnit}\nCalculated with ELECTRICAL CALCULATION ALL".format(loads.sumOf { it.totalKw })
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
                                inputsMap["Total Connected Load"] = "%.2f kW".format(loads.sumOf { it.totalKw })
                                inputsMap["Demand Factor"] = demandFactorStr
                                inputsMap["Diversity Factor"] = diversityFactorStr
                                inputsMap["Power Factor"] = avgPfStr
                                inputsMap["Plant Efficiency"] = plantEffStr
                                inputsMap["Supply Voltage"] = "$systemVoltageStr V (3 Phase)"
                                loads.take(8).forEachIndexed { i, it ->
                                    inputsMap["Load #${i + 1}"] = "${it.name}: ${it.displayOriginal} × ${it.quantity} = %.2f kW".format(it.totalKw)
                                }
                                val pdf = PdfReportGenerator.generateCalculationPdf(
                                    context,
                                    calculator,
                                    inputsMap,
                                    res,
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
                                inputsMap["Total Connected Load"] = "%.2f kW".format(loads.sumOf { it.totalKw })
                                inputsMap["Demand Factor"] = demandFactorStr
                                inputsMap["Diversity Factor"] = diversityFactorStr
                                inputsMap["Power Factor"] = avgPfStr
                                inputsMap["Plant Efficiency"] = plantEffStr
                                inputsMap["Supply Voltage"] = "$systemVoltageStr V (3 Phase)"
                                loads.take(8).forEachIndexed { i, it ->
                                    inputsMap["Load #${i + 1}"] = "${it.name}: ${it.displayOriginal} × ${it.quantity} = %.2f kW".format(it.totalKw)
                                }
                                val pdf = PdfReportGenerator.generateCalculationPdf(
                                    context,
                                    calculator,
                                    inputsMap,
                                    res,
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

    // SEARCHABLE DROPDOWN DIALOG
    if (isDropdownExpanded) {
        Dialog(onDismissRequest = { isDropdownExpanded = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Machine / Motor / Load",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = dropdownSearchQuery,
                        onValueChange = { dropdownSearchQuery = it },
                        label = { Text("Search load (e.g. Pump, Motor, Fan...)") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val filteredList = PredefinedFactoryLoadsCatalog.items.filter {
                        dropdownSearchQuery.isBlank() ||
                            it.name.contains(dropdownSearchQuery, ignoreCase = true) ||
                            it.loadType.contains(dropdownSearchQuery, ignoreCase = true)
                    }

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(filteredList) { item ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedMachine = item
                                        pendingPowerStr = if (item.unit == "W") item.defaultPower.roundToInt().toString() else "%.2f".format(item.defaultPower)
                                        pendingUnit = item.unit
                                        pendingPhase = item.phase
                                        pendingPfStr = item.defaultPf.toString()
                                        isDropdownExpanded = false
                                    }
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text("${item.loadType} • ${item.phase}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        color = ElectricBluePrimary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (item.unit == "W") "${item.defaultPower.roundToInt()} W" else "${item.defaultPower} kW",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = ElectricBluePrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { isDropdownExpanded = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // CUSTOM LOAD DIALOG
    if (showCustomDialog) {
        Dialog(onDismissRequest = { showCustomDialog = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "+ ADD CUSTOM LOAD",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ElectricBluePrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Machine / Load Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customPowerStr,
                            onValueChange = { customPowerStr = it },
                            label = { Text("Power") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Unit selector
                        Column(modifier = Modifier.width(90.dp)) {
                            Text("Unit", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("kW", "W").forEach { u ->
                                    val isSel = customUnit.equals(u, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .background(if (isSel) ElectricBluePrimary else Color.Transparent)
                                            .clickable { customUnit = u },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = u,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = customQtyStr,
                            onValueChange = { customQtyStr = it },
                            label = { Text("Quantity") },
                            modifier = Modifier.weight(0.8f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Phase locked to systemPhase
                        Surface(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Text(
                                    text = "Phase: $systemPhase",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedTextField(
                            value = customPfStr,
                            onValueChange = { customPfStr = it },
                            label = { Text("Power Factor") },
                            modifier = Modifier.weight(0.9f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCustomDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (customName.isBlank()) {
                                    Toast.makeText(context, "Machine name required", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val p = customPowerStr.toDoubleOrNull()
                                val q = customQtyStr.toIntOrNull()
                                val pf = customPfStr.toDoubleOrNull() ?: 0.85
                                if (p == null || p <= 0.0) {
                                    Toast.makeText(context, "Valid power required", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (q == null || q <= 0) {
                                    Toast.makeText(context, "Quantity must be greater than zero", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                loads.add(
                                    FactoryLoadItem(
                                        name = customName.trim(),
                                        power = p,
                                        unit = customUnit,
                                        quantity = q,
                                        phase = systemPhase,
                                        powerFactor = pf,
                                        loadType = "Custom Load"
                                    )
                                )
                                showCustomDialog = false
                                Toast.makeText(context, "Added ${customName.trim()}", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
                        ) {
                            Text("Add Custom Load")
                        }
                    }
                }
            }
        }
    }

    // EDIT LOAD DIALOG
    editingItem?.let { item ->
        Dialog(onDismissRequest = { editingItem = null }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Edit Factory Load",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ElectricBluePrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Machine Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editPowerStr,
                            onValueChange = { editPowerStr = it },
                            label = { Text("Power") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Unit
                        Column(modifier = Modifier.width(90.dp)) {
                            Text("Unit", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("kW", "W").forEach { u ->
                                    val isSel = editUnit.equals(u, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .background(if (isSel) ElectricBluePrimary else Color.Transparent)
                                            .clickable { editUnit = u },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = u,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = editQtyStr,
                            onValueChange = { editQtyStr = it },
                            label = { Text("Quantity") },
                            modifier = Modifier.weight(0.8f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Text(
                                    text = "Phase: $systemPhase",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedTextField(
                            value = editPfStr,
                            onValueChange = { editPfStr = it },
                            label = { Text("Power Factor") },
                            modifier = Modifier.weight(0.9f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { editingItem = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val p = editPowerStr.toDoubleOrNull()
                                val q = editQtyStr.toIntOrNull()
                                val pf = editPfStr.toDoubleOrNull() ?: 0.85
                                if (p != null && p > 0 && q != null && q > 0) {
                                    val index = loads.indexOf(item)
                                    if (index != -1) {
                                        loads[index] = item.copy(
                                            name = editName.ifBlank { item.name },
                                            power = p,
                                            unit = editUnit,
                                            quantity = q,
                                            phase = systemPhase,
                                            powerFactor = pf
                                        )
                                    }
                                    editingItem = null
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }
}
