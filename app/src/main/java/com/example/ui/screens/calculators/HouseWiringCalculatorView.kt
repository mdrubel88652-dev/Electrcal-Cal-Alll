package com.example.ui.screens.calculators

import android.widget.Toast
import androidx.compose.foundation.background
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
import com.example.data.calculator.ElectricalFormulas
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
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseWiringCalculatorView(
    calculator: CalculatorDefinition,
    onNavigateBack: () -> Unit,
    app: ElectricalApp,
    devSettings: DeveloperReportSettings,
    pdfSettings: PdfPrintSettings,
    techProfile: TechnicianReportProfile
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Predefined default loads matching exact prompt example
    val loads = remember {
        mutableStateListOf(
            HouseholdLoadItem(name = "LED Light", power = 15.0, unit = "W", quantity = 10, category = "Lighting"),
            HouseholdLoadItem(name = "Ceiling Fan", power = 80.0, unit = "W", quantity = 5, category = "Fans"),
            HouseholdLoadItem(name = "TV", power = 120.0, unit = "W", quantity = 2, category = "Entertainment"),
            HouseholdLoadItem(name = "Refrigerator", power = 200.0, unit = "W", quantity = 1, category = "Refrigeration")
        )
    }

    // Engineering parameters
    var demandFactorStr by remember { mutableStateOf("0.75") }
    var supplyPhase by remember { mutableStateOf("1 Phase") } // "1 Phase" or "3 Phase"
    var supplyVoltageStr by remember { mutableStateOf("230") }
    var powerFactorStr by remember { mutableStateOf("0.90") }

    // Dropdown states
    var selectedAppliance by remember { mutableStateOf<PredefinedAppliance?>(PredefinedHouseholdLoadsCatalog.items.first()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var dropdownSearchQuery by remember { mutableStateOf("") }

    // Input state for pending selection
    var pendingPowerStr by remember { mutableStateOf("15") }
    var pendingUnit by remember { mutableStateOf("W") }
    var pendingQtyStr by remember { mutableStateOf("1") }

    // Custom Load Dialog
    var showCustomDialog by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }
    var customPowerStr by remember { mutableStateOf("") }
    var customUnit by remember { mutableStateOf("W") }
    var customQtyStr by remember { mutableStateOf("1") }

    // Edit Load Dialog
    var editingItem by remember { mutableStateOf<HouseholdLoadItem?>(null) }
    var editName by remember { mutableStateOf("") }
    var editPowerStr by remember { mutableStateOf("") }
    var editUnit by remember { mutableStateOf("W") }
    var editQtyStr by remember { mutableStateOf("1") }

    // Result
    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var isSaved by remember { mutableStateOf(false) }

    fun runCalculation() {
        val totalWatts = loads.sumOf { it.totalWatts }
        val totalKw = totalWatts / 1000.0
        val df = demandFactorStr.toDoubleOrNull() ?: 0.75
        val pf = (powerFactorStr.toDoubleOrNull() ?: 0.90).coerceIn(0.1, 1.0)
        val v = supplyVoltageStr.toDoubleOrNull() ?: if (supplyPhase == "3 Phase") 400.0 else 230.0

        val demandKw = totalKw * df
        val demandKva = demandKw / pf
        val isThreePhase = supplyPhase.contains("3", ignoreCase = true)
        val designCurrent = if (isThreePhase) {
            (demandKw * 1000.0) / (sqrt(3.0) * v * pf)
        } else {
            (demandKw * 1000.0) / (v * pf)
        }

        // Breaker selection & Cable sizing recommendation
        val breakerRating = ElectricalFormulas.selectBreaker(designCurrent)
        val cableSize = when {
            breakerRating <= 16 -> "2.5 mm² Cu (Twin Core with ECC)"
            breakerRating <= 20 -> "4.0 mm² Cu (Twin Core with ECC)"
            breakerRating <= 32 -> "6.0 mm² Cu (Single / Twin Core)"
            breakerRating <= 50 -> "10.0 mm² Cu (Meter Feeder)"
            breakerRating <= 63 -> "16.0 mm² Cu (Sub-main Feeder)"
            breakerRating <= 80 -> "25.0 mm² Cu Main Service Cable"
            else -> "35.0 mm² Cu Main Service Cable"
        }

        val steps = listOf(
            CalculationStep(1, "Total Connected Load (Watts)", "Σ(Appliance Power × Qty)", "${loads.size} appliances summed", "${totalWatts.roundToInt()} W"),
            CalculationStep(2, "Convert to Kilowatts (kW)", "W / 1000", "${totalWatts.roundToInt()} / 1000", "${fmt(totalKw)} kW"),
            CalculationStep(3, "Estimated Maximum Demand", "Total kW × Demand Factor", "${fmt(totalKw)} × $df", "${fmt(demandKw)} kW"),
            CalculationStep(4, "Apparent Power (kVA)", "Demand kW / PF", "${fmt(demandKw)} / $pf", "${fmt(demandKva)} kVA"),
            CalculationStep(5, "Design Service Current", if (isThreePhase) "Demand W / (√3 × V × PF)" else "Demand W / (V × PF)", if (isThreePhase) "${fmt(demandKw * 1000)} / (1.732 × ${v.roundToInt()} × $pf)" else "${fmt(demandKw * 1000)} / (${v.roundToInt()} × $pf)", "${fmt(designCurrent)} A"),
            CalculationStep(6, "Main Service Breaker (MCB)", "Standard breaker rating > $designCurrent A", "Selected per IEC 60898", "$breakerRating A MCB ($supplyPhase)")
        )

        val secondary = listOf(
            "Total Connected Watts" to "${totalWatts.roundToInt()} W",
            "Total Connected Load" to "${fmt(totalKw)} kW",
            "Estimated Demand Load" to "${fmt(demandKw)} kW",
            "Estimated Apparent Power" to "${fmt(demandKva)} kVA",
            "Design Operating Current" to "${fmt(designCurrent)} A",
            "Main Distribution Breaker" to "$breakerRating A MCB",
            "Recommended Service Cable" to cableSize
        )

        val notes = listOf(
            "All wattage entries automatically converted to kW (W / 1000); kW inputs preserved without duplicate division.",
            "Demand factor ($df) applied per residential diversity standard (IEC 60364-5-52 / BNBC Part 8).",
            "Main service cable & protective breaker selected to prevent thermal overload and nuisance tripping.",
            "Standard compliance: IEC 60364 Building Electrical Installations, NEC Article 220 & BNBC."
        )

        calculationResult = CalculationResult(
            primaryValue = fmt(totalKw),
            primaryUnit = "kW Total Connected Load (${totalWatts.roundToInt()} W)",
            formulaUsed = "Total kW = Σ(Power in W / 1000); Demand kW = Total kW × DF; I = P / (V × PF)",
            steps = steps,
            secondaryResults = secondary,
            notes = notes,
            standardBasis = "IEC 60364-5-52 / NEC Article 220 / BNBC Part 8"
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
            .testTag("house_wiring_screen_container")
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
                            text = "#3 • BASIC",
                            color = LevelBasicColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Building Electrical",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "House Wiring Load Calculation",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Add household electrical loads from the predefined appliance catalog or custom items. Automatically converts W to kW, calculates diversified demand, design current, and main circuit breaker rating.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. ADD HOUSEHOLD LOAD SECTION
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ADD HOUSEHOLD LOAD",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ElectricBluePrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select Appliance / Load from the dropdown or add custom appliances.",
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
                                text = "Select Appliance / Load",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedAppliance?.name ?: "Select Appliance...",
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
                            text = "Default Wattage — Editable",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Editable Fields for selected appliance
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

                    // Unit selector (W / kW)
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
                            listOf("W", "kW").forEach { u ->
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

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val power = pendingPowerStr.toDoubleOrNull()
                            val qty = pendingQtyStr.toIntOrNull()
                            if (power == null || power <= 0.0) {
                                Toast.makeText(context, "Please enter valid power", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (qty == null || qty <= 0) {
                                Toast.makeText(context, "Quantity must be greater than zero", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val name = selectedAppliance?.name ?: "Appliance"
                            loads.add(
                                HouseholdLoadItem(
                                    name = name,
                                    power = power,
                                    unit = pendingUnit,
                                    quantity = qty,
                                    category = selectedAppliance?.category ?: "General"
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
                        Text("Add Appliance", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            customName = ""
                            customPowerStr = ""
                            customUnit = "W"
                            customQtyStr = "1"
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

        // 3. HOUSE LOAD TABLE
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
                        text = "HOUSEHOLD LOAD TABLE (${loads.size} Items)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${loads.sumOf { it.totalWatts }.roundToInt()} W",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "%.2f kW".format(loads.sumOf { it.totalKw }),
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricBluePrimary,
                            fontSize = 15.sp
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                if (loads.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No appliances added yet. Select appliances above.", color = Color.Gray, fontSize = 13.sp)
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

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${item.totalWatts.roundToInt()} W",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "(%.3f kW)".format(item.totalKw),
                                            fontSize = 11.sp,
                                            color = ElectricBluePrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.displayOriginal}  ×  Qty: ${item.quantity}  •  ${item.category}",
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
                            Column {
                                Text(
                                    text = "TOTAL CONNECTED LOAD:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ElectricBluePrimary
                                )
                                Text(
                                    text = "${loads.sumOf { it.totalWatts }.roundToInt()} Watts",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "%.2f kW".format(loads.sumOf { it.totalKw }),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = ElectricBluePrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. HOUSE WIRING CALCULATION PARAMETERS
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SERVICE & DEMAND PARAMETERS",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ElectricBluePrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Configure diversity factor, service voltage, and operating power factor.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Supply Phase selector
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Supply Phase", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("1 Phase", "3 Phase").forEach { ph ->
                                val isSel = supplyPhase == ph
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(if (isSel) ElectricBluePrimary else Color.Transparent)
                                        .clickable {
                                            supplyPhase = ph
                                            supplyVoltageStr = if (ph == "3 Phase") "400" else "230"
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ph,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = demandFactorStr,
                        onValueChange = { demandFactorStr = it },
                        label = { Text("Demand Factor") },
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
                        value = supplyVoltageStr,
                        onValueChange = { supplyVoltageStr = it },
                        label = { Text("Voltage (V)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = powerFactorStr,
                        onValueChange = { powerFactorStr = it },
                        label = { Text("Power Factor") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Strict Phase Isolation Notice
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
                                text = if (supplyPhase == "1 Phase") {
                                    "বর্তমানে সম্পূর্ণ হিসাব Single Phase (1φ 230V) সূত্রে সম্পন্ন হচ্ছে: I = P / (V × PF)।"
                                } else {
                                    "বর্তমানে সম্পূর্ণ হিসাব Three Phase (3φ 400V) সূত্রে সম্পন্ন হচ্ছে: I = P / (√3 × V × PF)।"
                                },
                                fontSize = 10.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

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
                        Text("Calculate House Load", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            loads.clear()
                            loads.addAll(
                                listOf(
                                    HouseholdLoadItem(name = "LED Light", power = 15.0, unit = "W", quantity = 10, category = "Lighting"),
                                    HouseholdLoadItem(name = "Ceiling Fan", power = 80.0, unit = "W", quantity = 5, category = "Fans"),
                                    HouseholdLoadItem(name = "TV", power = 120.0, unit = "W", quantity = 2, category = "Entertainment"),
                                    HouseholdLoadItem(name = "Refrigerator", power = 200.0, unit = "W", quantity = 1, category = "Refrigeration")
                                )
                            )
                            demandFactorStr = "0.75"
                            supplyPhase = "1 Phase"
                            supplyVoltageStr = "230"
                            powerFactorStr = "0.90"
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

        // 5. HOUSE WIRING RESULT
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
                                val summary = "Loads (${loads.size}): " + loads.joinToString(", ") { "${it.name} ${it.displayOriginal} (x${it.quantity})" } + " | Total: %.2f kW (${loads.sumOf { it.totalWatts }.roundToInt()} W)".format(loads.sumOf { it.totalKw })
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
                            val copyText = "House Wiring Load Calculation\nTotal Connected Load: %.2f kW (${loads.sumOf { it.totalWatts }.roundToInt()} W)\nFormula: ${res.formulaUsed}\nStandard: ${res.standardBasis}".format(loads.sumOf { it.totalKw })
                            ShareUtils.copyToClipboard(context, calculator.name, copyText)
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }

                    // Share
                    IconButton(
                        onClick = {
                            val shareText = "House Wiring Load Calculation Report\nTotal Connected Load: %.2f kW (${loads.sumOf { it.totalWatts }.roundToInt()} W)\nDemand: ${res.secondaryResults.firstOrNull { it.first == "Estimated Demand Load" }?.second ?: ""}\nCalculated with ELECTRICAL CALCULATION ALL".format(loads.sumOf { it.totalKw })
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
                                inputsMap["Total Connected Load"] = "%.2f kW (${loads.sumOf { it.totalWatts }.roundToInt()} W)".format(loads.sumOf { it.totalKw })
                                inputsMap["Demand Factor"] = demandFactorStr
                                inputsMap["Supply Phase"] = supplyPhase
                                inputsMap["Supply Voltage"] = "$supplyVoltageStr V"
                                inputsMap["Power Factor"] = powerFactorStr
                                loads.take(8).forEachIndexed { i, it ->
                                    inputsMap["Appliance #${i + 1}"] = "${it.name}: ${it.displayOriginal} × ${it.quantity} = ${it.totalWatts.roundToInt()} W (%.3f kW)".format(it.totalKw)
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
                                inputsMap["Total Connected Load"] = "%.2f kW (${loads.sumOf { it.totalWatts }.roundToInt()} W)".format(loads.sumOf { it.totalKw })
                                inputsMap["Demand Factor"] = demandFactorStr
                                inputsMap["Supply Phase"] = supplyPhase
                                inputsMap["Supply Voltage"] = "$supplyVoltageStr V"
                                inputsMap["Power Factor"] = powerFactorStr
                                loads.take(8).forEachIndexed { i, it ->
                                    inputsMap["Appliance #${i + 1}"] = "${it.name}: ${it.displayOriginal} × ${it.quantity} = ${it.totalWatts.roundToInt()} W (%.3f kW)".format(it.totalKw)
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
                        text = "Select Appliance / Load",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = dropdownSearchQuery,
                        onValueChange = { dropdownSearchQuery = it },
                        label = { Text("Search appliance (e.g. Light, Fan, Socket...)") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val filteredList = PredefinedHouseholdLoadsCatalog.items.filter {
                        dropdownSearchQuery.isBlank() ||
                            it.name.contains(dropdownSearchQuery, ignoreCase = true) ||
                            it.category.contains(dropdownSearchQuery, ignoreCase = true)
                    }

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(filteredList) { item ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedAppliance = item
                                        pendingPowerStr = item.defaultWattage.roundToInt().toString()
                                        pendingUnit = "W"
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
                                        Text(item.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        color = ElectricBluePrimary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "${item.defaultWattage.roundToInt()} W",
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
                        label = { Text("Appliance / Load Name") },
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
                                listOf("W", "kW").forEach { u ->
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
                                    Toast.makeText(context, "Appliance name required", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val p = customPowerStr.toDoubleOrNull()
                                val q = customQtyStr.toIntOrNull()
                                if (p == null || p <= 0.0) {
                                    Toast.makeText(context, "Valid power required", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (q == null || q <= 0) {
                                    Toast.makeText(context, "Quantity must be greater than zero", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                loads.add(
                                    HouseholdLoadItem(
                                        name = customName.trim(),
                                        power = p,
                                        unit = customUnit,
                                        quantity = q,
                                        category = "Custom Load"
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
                        text = "Edit Household Load",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ElectricBluePrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Appliance Name") },
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
                                listOf("W", "kW").forEach { u ->
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
                                if (p != null && p > 0 && q != null && q > 0) {
                                    val index = loads.indexOf(item)
                                    if (index != -1) {
                                        loads[index] = item.copy(
                                            name = editName.ifBlank { item.name },
                                            power = p,
                                            unit = editUnit,
                                            quantity = q
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
