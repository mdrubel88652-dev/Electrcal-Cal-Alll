package com.example.ui.screens.calculators

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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

// ============================================================================
// CABLE & PROTECTION SIZING CONSTANTS PER BNBC 2020 / IEC 60364-5-52
// ============================================================================

private data class CableSpec(
    val sizeMm2: Double,
    val ampacityCuConduit: Double, // In conduit concealed in wall/ceiling
    val ampacityAlConduit: Double,
    val resistancePerKmCu: Double, // ohm/km
    val resistancePerKmAl: Double  // ohm/km
)

private val STANDARD_CABLE_SPECS = listOf(
    CableSpec(1.5, 14.5, 11.0, 12.1, 19.5),
    CableSpec(2.5, 19.5, 15.0, 7.41, 11.9),
    CableSpec(4.0, 26.0, 20.0, 4.61, 7.41),
    CableSpec(6.0, 34.0, 26.0, 3.08, 4.95),
    CableSpec(10.0, 46.0, 36.0, 1.83, 2.94),
    CableSpec(16.0, 61.0, 48.0, 1.15, 1.85),
    CableSpec(25.0, 80.0, 63.0, 0.727, 1.17),
    CableSpec(35.0, 99.0, 77.0, 0.524, 0.841),
    CableSpec(50.0, 119.0, 93.0, 0.387, 0.621),
    CableSpec(70.0, 151.0, 118.0, 0.268, 0.431),
    CableSpec(95.0, 182.0, 142.0, 0.193, 0.310),
    CableSpec(120.0, 210.0, 164.0, 0.153, 0.246)
)

private val STANDARD_BREAKER_RATINGS = listOf(
    6, 10, 16, 20, 25, 32, 40, 50, 63, 80, 100, 125, 160, 200, 250
)

private val STANDARD_FUSE_RATINGS = listOf(
    10, 16, 20, 25, 32, 40, 50, 63, 80, 100, 125, 160, 200, 250
)

private fun getRealisticSampleLoads(): List<HouseholdLoadItem> = listOf(
    HouseholdLoadItem(name = "LED Bulb 15W", power = 15.0, unit = "W", quantity = 10, phase = "1 Phase", category = "Lighting"),
    HouseholdLoadItem(name = "Tube Light 20W LED", power = 20.0, unit = "W", quantity = 4, phase = "1 Phase", category = "Lighting"),
    HouseholdLoadItem(name = "Ceiling Fan", power = 75.0, unit = "W", quantity = 5, phase = "1 Phase", category = "Fans"),
    HouseholdLoadItem(name = "Exhaust Fan", power = 45.0, unit = "W", quantity = 2, phase = "1 Phase", category = "Fans"),
    HouseholdLoadItem(name = "Smart Television 55\"", power = 120.0, unit = "W", quantity = 2, phase = "1 Phase", category = "Entertainment"),
    HouseholdLoadItem(name = "Refrigerator (Frost-Free)", power = 250.0, unit = "W", quantity = 1, phase = "1 Phase", category = "Refrigeration"),
    HouseholdLoadItem(name = "Microwave Oven", power = 1200.0, unit = "W", quantity = 1, phase = "1 Phase", category = "Kitchen"),
    HouseholdLoadItem(name = "Rice Cooker", power = 700.0, unit = "W", quantity = 1, phase = "1 Phase", category = "Kitchen"),
    HouseholdLoadItem(name = "Electric Iron", power = 1000.0, unit = "W", quantity = 1, phase = "1 Phase", category = "Laundry"),
    HouseholdLoadItem(name = "Washing Machine", power = 500.0, unit = "W", quantity = 1, phase = "1 Phase", category = "Laundry"),
    HouseholdLoadItem(name = "Water Pump 1.0 HP", power = 750.0, unit = "W", quantity = 1, phase = "1 Phase", category = "Motors"),
    HouseholdLoadItem(name = "Air Conditioner 1.5 Ton", power = 1.8, unit = "kW", quantity = 1, phase = "1 Phase", category = "Cooling"),
    HouseholdLoadItem(name = "Wi-Fi Router & ONU", power = 15.0, unit = "W", quantity = 1, phase = "1 Phase", category = "IT & Electronics")
)

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

    // Household loads state
    val loads = remember {
        mutableStateListOf<HouseholdLoadItem>().apply {
            addAll(getRealisticSampleLoads())
        }
    }

    var isSampleDataActive by remember { mutableStateOf(true) }

    // Service & Engineering parameters
    var supplyPhase by remember { mutableStateOf("1 Phase") } // "1 Phase" or "3 Phase"
    var supplyVoltageStr by remember { mutableStateOf("230") }
    var powerFactorStr by remember { mutableStateOf("0.85") }
    var cableLengthStr by remember { mutableStateOf("15") } // Meters
    var conductorMaterial by remember { mutableStateOf("Copper") } // "Copper" or "Aluminum"

    // Searchable Dropdown state
    var selectedCatalogItem by remember { mutableStateOf<PredefinedAppliance?>(PredefinedHouseholdLoadsCatalog.items.first()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var dropdownSearchQuery by remember { mutableStateOf("") }

    // Editable pending load row state (allows editing name, wattage, unit, qty, phase before adding)
    var pendingName by remember { mutableStateOf(PredefinedHouseholdLoadsCatalog.items.first().name) }
    var pendingPowerStr by remember { mutableStateOf(PredefinedHouseholdLoadsCatalog.items.first().defaultWattage.roundToInt().toString()) }
    var pendingUnit by remember { mutableStateOf("W") }
    var pendingQtyStr by remember { mutableStateOf("1") }
    var pendingPhase by remember { mutableStateOf("1 Phase") }

    // Custom Load Dialog state
    var showCustomDialog by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }
    var customPowerStr by remember { mutableStateOf("") }
    var customUnit by remember { mutableStateOf("W") }
    var customQtyStr by remember { mutableStateOf("1") }
    var customPhase by remember { mutableStateOf("1 Phase") }

    // Edit Load Dialog state
    var editingItem by remember { mutableStateOf<HouseholdLoadItem?>(null) }
    var editName by remember { mutableStateOf("") }
    var editPowerStr by remember { mutableStateOf("") }
    var editUnit by remember { mutableStateOf("W") }
    var editQtyStr by remember { mutableStateOf("1") }
    var editPhase by remember { mutableStateOf("1 Phase") }

    // Calculation Result
    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var isSaved by remember { mutableStateOf(false) }
    var showFormulaSteps by remember { mutableStateOf(false) }

    // Sizing computation function
    fun runCalculation() {
        if (loads.isEmpty()) {
            calculationResult = null
            return
        }

        // 1. Total Connected Load
        val totalConnectedWatts = loads.sumOf { it.totalWatts }
        val totalConnectedKw = totalConnectedWatts / 1000.0

        // 2. Mandatory +25% Future Load Allowance (BNBC Part 8 / IEC 60364)
        val allowanceWatts = totalConnectedWatts * 0.25
        val allowanceKw = totalConnectedKw * 0.25

        // 3. Design Load (applied exactly once)
        val designWatts = totalConnectedWatts * 1.25
        val designKw = totalConnectedKw * 1.25

        // Electrical inputs validation
        val v = supplyVoltageStr.toDoubleOrNull() ?: if (supplyPhase == "3 Phase") 400.0 else 230.0
        val pf = (powerFactorStr.toDoubleOrNull() ?: 0.85).coerceIn(0.10, 1.0)
        val lengthM = (cableLengthStr.toDoubleOrNull() ?: 15.0).coerceAtLeast(1.0)
        val isThreePhase = supplyPhase.contains("3", ignoreCase = true)
        val isCopper = conductorMaterial.equals("Copper", ignoreCase = true)

        if (v <= 0.0 || pf <= 0.0) {
            Toast.makeText(context, "Invalid Voltage or Power Factor", Toast.LENGTH_SHORT).show()
            return
        }

        // 4. Operating Currents
        val connectedCurrent = if (isThreePhase) {
            totalConnectedWatts / (sqrt(3.0) * v * pf)
        } else {
            totalConnectedWatts / (v * pf)
        }

        val designCurrent = if (isThreePhase) {
            designWatts / (sqrt(3.0) * v * pf)
        } else {
            designWatts / (v * pf)
        }

        val designKva = designKw / pf

        // 5. Main Circuit Breaker (MCB/MCCB) Selection
        // Smallest standard rating >= designCurrent
        val breakerRating = STANDARD_BREAKER_RATINGS.firstOrNull { it >= designCurrent }
            ?: ((designCurrent / 50).toInt() + 1) * 50

        val breakerType = if (isThreePhase) {
            if (breakerRating <= 100) "$breakerRating A TPN / 4P MCB (Curve C, 10kA)" else "$breakerRating A 4P MCCB (Adj. Trip, 25kA)"
        } else {
            "$breakerRating A Double Pole (DP) MCB (Curve C, 6kA/10kA)"
        }

        // 6. Main Service Cutout / Kit-Kat Fuse Sizing (Rewirable Fuse for Houses)
        // Sized equal to or next standard size above breaker rating for selectivity
        val cutoutFuseRating = STANDARD_FUSE_RATINGS.firstOrNull { it >= breakerRating } ?: breakerRating
        val cutoutSpec = "$cutoutFuseRating A Kit-Kat Fuse (Rewirable Service Cutout)"

        // 7. Main Service Cable Sizing (mm²) & Voltage Drop Verification
        // Sized so cable ampacity >= breakerRating >= designCurrent
        var selectedCable = STANDARD_CABLE_SPECS.firstOrNull { spec ->
            val ampacity = if (isCopper) spec.ampacityCuConduit else spec.ampacityAlConduit
            ampacity >= breakerRating
        } ?: STANDARD_CABLE_SPECS.last()

        // Check Voltage Drop: limit <= 3.0% for service sub-main run per BNBC/IEC
        fun calcVdrop(spec: CableSpec): Pair<Double, Double> {
            val rPerKm = if (isCopper) spec.resistancePerKmCu else spec.resistancePerKmAl
            val totalR = (rPerKm * lengthM) / 1000.0
            val dropV = if (isThreePhase) {
                sqrt(3.0) * designCurrent * totalR * pf
            } else {
                2.0 * designCurrent * totalR * pf
            }
            val dropPct = (dropV / v) * 100.0
            return Pair(dropV, dropPct)
        }

        var (vDropVolts, vDropPct) = calcVdrop(selectedCable)

        // Step up cable size if voltage drop exceeds 3.0%
        val currentIndex = STANDARD_CABLE_SPECS.indexOf(selectedCable)
        for (i in currentIndex until STANDARD_CABLE_SPECS.size) {
            val candidate = STANDARD_CABLE_SPECS[i]
            val (dv, dp) = calcVdrop(candidate)
            if (dp <= 3.0 || i == STANDARD_CABLE_SPECS.size - 1) {
                selectedCable = candidate
                vDropVolts = dv
                vDropPct = dp
                break
            }
        }

        val condLabel = if (isCopper) "Cu (Copper)" else "Al (Aluminum)"
        val eccSize = when {
            selectedCable.sizeMm2 <= 2.5 -> 2.5
            selectedCable.sizeMm2 <= 6.0 -> 4.0
            selectedCable.sizeMm2 <= 16.0 -> 6.0
            selectedCable.sizeMm2 <= 35.0 -> 10.0
            else -> 16.0
        }

        val cableDesc = if (isThreePhase) {
            "4 × ${selectedCable.sizeMm2} mm² $condLabel + 1 × $eccSize mm² ECC (NYY/PVC in Conduit)"
        } else {
            "2 × ${selectedCable.sizeMm2} mm² $condLabel + 1 × $eccSize mm² ECC (BYA/NYY in Conduit)"
        }

        // 8. Electricity Meter & Service Capacity
        val isThreePhaseMeterRequired = isThreePhase || totalConnectedKw > 7.5
        val meterType = if (isThreePhaseMeterRequired) {
            if (designCurrent > 60.0) {
                "3-Phase 4-Wire CT-Operated Meter (Low Voltage CT Class 0.5S, 400V, 50Hz)"
            } else {
                "3-Phase 4-Wire Whole Current Energy Meter (10-60A / 10-100A, 400V, 50Hz)"
            }
        } else {
            "1-Phase 2-Wire Whole Current Energy Meter (10-40A / 10-60A, 230V, 50Hz)"
        }

        val supplyUpgradeNotice = if (!isThreePhase && totalConnectedKw > 7.5) {
            "⚠️ Notice: Connected load (${fmt(totalConnectedKw)} kW) exceeds 7.5 kW single-phase limit. Utility rules (BNBC / BREB / DPDC / DESCO) require upgrading to a 3-Phase 400V supply connection."
        } else {
            "Complies with utility service connection standards."
        }

        // Calculation Steps
        val steps = listOf(
            CalculationStep(
                1,
                "Total Connected Load (Watts)",
                "Σ(Device Unit Power × Quantity)",
                "${loads.size} load items summed across premises",
                "${totalConnectedWatts.roundToInt()} W (${fmt(totalConnectedKw)} kW)"
            ),
            CalculationStep(
                2,
                "Mandatory +25% Future Load Allowance",
                "Total Connected Load × 0.25",
                "${fmt(totalConnectedKw)} kW × 0.25 (BNBC Part 8 / IEC 60364)",
                "${allowanceWatts.roundToInt()} W (${fmt(allowanceKw)} kW)"
            ),
            CalculationStep(
                3,
                "Continuous Design Service Load",
                "Total Connected Load × 1.25",
                "${fmt(totalConnectedKw)} kW × 1.25 (Applied strictly once)",
                "${designWatts.roundToInt()} W (${fmt(designKw)} kW / ${fmt(designKva)} kVA)"
            ),
            CalculationStep(
                4,
                "Operating & Design Service Current",
                if (isThreePhase) "I = P / (√3 × V × PF)" else "I = P / (V × PF)",
                if (isThreePhase)
                    "I_conn = ${totalConnectedWatts.roundToInt()} / (1.732 × ${v.roundToInt()} × $pf); I_design = ${designWatts.roundToInt()} / (1.732 × ${v.roundToInt()} × $pf)"
                else
                    "I_conn = ${totalConnectedWatts.roundToInt()} / (${v.roundToInt()} × $pf); I_design = ${designWatts.roundToInt()} / (${v.roundToInt()} × $pf)",
                "Connected: ${fmt(connectedCurrent)} A | Design: ${fmt(designCurrent)} A"
            ),
            CalculationStep(
                5,
                "Main Service Breaker (MCB/MCCB)",
                "Standard breaker rating In ≥ I_design",
                "Selected $breakerRating A per IEC 60898-1 / BNBC with Curve C motor inrush withstand",
                breakerType
            ),
            CalculationStep(
                6,
                "Main Service Cutout (Kit-Kat Fuse)",
                "Rewirable cutout fuse upstream protection",
                "Standard Kit-Kat Fuse matching residential service entrance",
                cutoutSpec
            ),
            CalculationStep(
                7,
                "Main Service Cable & Voltage Drop",
                "Iz ≥ In ≥ I_design with ΔV% ≤ 3.0% over ${lengthM.roundToInt()}m",
                "Current rating: ${(if (isCopper) selectedCable.ampacityCuConduit else selectedCable.ampacityAlConduit).roundToInt()} A; ΔV = ${fmt(vDropVolts)} V (${fmt(vDropPct)}%)",
                "$cableDesc — ΔV: ${fmt(vDropPct)}%"
            ),
            CalculationStep(
                8,
                "Electricity Meter & Service Capacity",
                if (isThreePhaseMeterRequired) "3-Phase 4-Wire Metering" else "1-Phase 2-Wire Metering",
                "Connected: ${fmt(totalConnectedKw)} kW | Design: ${fmt(designKw)} kW",
                meterType
            )
        )

        val secondary = listOf(
            "Total Connected Load" to "${totalConnectedWatts.roundToInt()} W (${fmt(totalConnectedKw)} kW)",
            "Mandatory 25% Allowance" to "+${allowanceWatts.roundToInt()} W (+${fmt(allowanceKw)} kW)",
            "Continuous Design Load" to "${designWatts.roundToInt()} W (${fmt(designKw)} kW / ${fmt(designKva)} kVA)",
            "Operating Current (Connected)" to "${fmt(connectedCurrent)} A",
            "Design Operating Current (1.25×)" to "${fmt(designCurrent)} A",
            "Main Distribution Breaker" to breakerType,
            "Main Service Cutout (Kit-Kat Fuse)" to cutoutSpec,
            "Main Service Cable" to cableDesc,
            "Calculated Voltage Drop" to "${fmt(vDropVolts)} V (${fmt(vDropPct)}% at ${lengthM.roundToInt()}m)",
            "Electricity Metering" to meterType,
            "Supply Capacity Status" to supplyUpgradeNotice
        )

        val notes = listOf(
            "Dual Unit Handling: Watts (W) and Kilowatts (kW) converted to common base without duplicate multiplication.",
            "Mandatory 25% Load Allowance applied strictly once per BNBC 2020 Part 8 and IEC 60364 continuous load standard.",
            "MCB Curve C specified to handle inrush starting currents of inductive loads (AC compressors, water pump motors).",
            "Cable selected for both thermal ampacity (in conduit) and maximum permissible 3.0% voltage drop.",
            "Utility Disclaimer: Final sanction load, meter type, and service drop specifications are subject to local power distribution authority (DPDC/DESCO/BPDB/BREB/NESCO/WZPDCL) approval."
        )

        calculationResult = CalculationResult(
            primaryValue = fmt(designKw),
            primaryUnit = "kW Continuous Design Load (Connected: ${fmt(totalConnectedKw)} kW)",
            formulaUsed = if (isThreePhase)
                "P_design = 1.25 × Σ(P); I_design = P_design / (√3 × V_L × cos φ); In ≥ I_design; ΔV% ≤ 3%"
            else
                "P_design = 1.25 × Σ(P); I_design = P_design / (V × cos φ); In ≥ I_design; ΔV% ≤ 3%",
            steps = steps,
            secondaryResults = secondary,
            notes = notes,
            standardBasis = "BNBC 2020 Part 8 / BDS IEC 60364-5-52 / IEC 60898-1"
        )
        isSaved = false
    }

    // Automatically compute initial sample load
    LaunchedEffect(Unit) {
        runCalculation()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("house_wiring_screen_container")
    ) {
        // 1. Header Info Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = LevelBasicBg, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = "#3 • BASIC",
                        color = LevelBasicColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(color = Color(0xFFE2E8F0), shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = "Building Electrical",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFEFF6FF)) {
                Text(
                    text = "BNBC / IEC 60364",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0284C7),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Sample Load and Reset Buttons System Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSampleDataActive)
                            "নমুনা লোড সক্রিয় (Sample Demo Data Active)। নিজের মান বসাতে রিসেট করুন।"
                        else
                            "প্রয়োজন অনুযায়ী যেকোনো ডিভাইস যোগ বা সম্পাদন করুন।",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Reset Sample Button (Clears form without affecting history)
                    Surface(
                        onClick = {
                            loads.clear()
                            isSampleDataActive = false
                            calculationResult = null
                            Toast.makeText(context, "সকল লোড তালিকা খালি করা হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reset_sample_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "নমুনা রিসেট / খালি করুন",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }

                    // Load Sample Button (Predefined realistic residential load)
                    Surface(
                        onClick = {
                            loads.clear()
                            loads.addAll(getRealisticSampleLoads())
                            isSampleDataActive = true
                            runCalculation()
                            Toast.makeText(context, "বাস্তবধর্মী নমুনা লোড সফলভাবে লোড হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("load_sample_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "নমুনা লোড (Load Sample)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                        }
                    }
                }
            }
        }

        // 3. ADD HOUSEHOLD LOAD CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ADD ELECTRICAL DEVICE",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ElectricBluePrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select from common residential appliances or add custom devices.",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Searchable Dropdown Trigger
                OutlinedCard(
                    onClick = {
                        dropdownSearchQuery = ""
                        isDropdownExpanded = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("device_dropdown_trigger"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Search & Select Preset Device",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedCatalogItem?.name ?: "Select from 50+ Household Devices...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Editable Device Name Field
                OutlinedTextField(
                    value = pendingName,
                    onValueChange = { pendingName = it },
                    label = { Text("Device Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("device_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Power Value, Unit Selector (W/kW), and Quantity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Rated Power
                    OutlinedTextField(
                        value = pendingPowerStr,
                        onValueChange = { pendingPowerStr = it },
                        label = { Text("Power") },
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("device_power_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Unit selector (W / kW)
                    Column(modifier = Modifier.width(96.dp)) {
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
                                        .clickable { pendingUnit = u }
                                        .testTag("unit_selector_$u"),
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

                    // Quantity
                    OutlinedTextField(
                        value = pendingQtyStr,
                        onValueChange = { pendingQtyStr = it },
                        label = { Text("Qty") },
                        modifier = Modifier
                            .weight(0.7f)
                            .testTag("device_qty_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons: Add Appliance & Custom Load
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

                            val name = pendingName.trim().ifBlank { selectedCatalogItem?.name ?: "Device" }
                            loads.add(
                                HouseholdLoadItem(
                                    name = name,
                                    power = power,
                                    unit = pendingUnit,
                                    quantity = qty,
                                    phase = pendingPhase,
                                    category = selectedCatalogItem?.category ?: "General"
                                )
                            )
                            isSampleDataActive = false
                            runCalculation()
                            Toast.makeText(context, "Added $name ($qty)", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(46.dp)
                            .testTag("add_device_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Device", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            customName = ""
                            customPowerStr = ""
                            customUnit = "W"
                            customQtyStr = "1"
                            customPhase = "1 Phase"
                            showCustomDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("custom_device_btn"),
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

        // 4. HOUSEHOLD LOAD TABLE
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("household_load_table"),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val totalWatts = loads.sumOf { it.totalWatts }
                val totalKw = loads.sumOf { it.totalKw }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HOUSEHOLD LOAD TABLE",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${loads.size} Devices Listed",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${totalWatts.roundToInt()} W",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "%.2f kW".format(totalKw),
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricBluePrimary,
                            fontSize = 16.sp
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                if (loads.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Inbox,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "No electrical devices added yet.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Select appliances above or click 'Load Sample'.",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    loads.forEachIndexed { index, item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
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
                                        Column {
                                            Text(
                                                text = item.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    color = Color(0xFFE0F2FE),
                                                    shape = RoundedCornerShape(3.dp)
                                                ) {
                                                    Text(
                                                        text = item.phase,
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF0369A1),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = item.category,
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
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

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.displayOriginal}  ×  Qty: ${item.quantity}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Action buttons: Duplicate, Edit, Delete
                                    Row {
                                        IconButton(
                                            onClick = {
                                                loads.add(index + 1, item.copy(id = UUID.randomUUID().toString()))
                                                runCalculation()
                                                Toast.makeText(context, "Duplicated ${item.name}", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", modifier = Modifier.size(15.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                editingItem = item
                                                editName = item.name
                                                editPowerStr = item.power.toString()
                                                editUnit = item.unit
                                                editQtyStr = item.quantity.toString()
                                                editPhase = item.phase
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(15.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                loads.remove(item)
                                                runCalculation()
                                                Toast.makeText(context, "Deleted ${item.name}", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(15.dp))
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
                                    fontSize = 12.5.sp,
                                    color = ElectricBluePrimary
                                )
                                Text(
                                    text = "${totalWatts.roundToInt()} Watts Total",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "%.2f kW".format(totalKw),
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

        // 5. SERVICE & ELECTRICAL ENGINEERING PARAMETERS CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SERVICE & CABLE PARAMETERS",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ElectricBluePrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Supply phase, nominal voltage, power factor, and service cable run.",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phase Selector
                com.example.ui.components.ModernPhaseSelector(
                    selectedPhase = supplyPhase,
                    onPhaseSelected = { ph ->
                        supplyPhase = if (ph.contains("3")) "3 Phase" else "1 Phase"
                        supplyVoltageStr = if (ph.contains("3")) "400" else "230"
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                )

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
                        label = { Text("Power Factor (cos φ)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cableLengthStr,
                        onValueChange = { cableLengthStr = it },
                        label = { Text("Cable Run (Meters)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Conductor Material Selector (Copper / Aluminum)
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text("Conductor", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("Copper", "Aluminum").forEach { m ->
                                val isSelected = conductorMaterial.equals(m, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(if (isSelected) ElectricBluePrimary else Color.Transparent)
                                        .clickable { conductorMaterial = m },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (m == "Copper") "Cu (Copper)" else "Al (Alum)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (loads.isEmpty()) {
                            Toast.makeText(context, "Please add at least one device", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        runCalculation()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("calculate_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Calculate House Load & Sizing", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 6. CALCULATION RESULTS CARD
        calculationResult?.let { res ->
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("result_card"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "FINAL RESULT & SIZING RECOMMENDATIONS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlueSecondary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

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
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // MANDATORY 3-STAGE LOAD BREAKDOWN BOX (+25% Allowance)
                    val totalConnectedW = loads.sumOf { it.totalWatts }
                    val totalConnectedK = totalConnectedW / 1000.0
                    val allowW = totalConnectedW * 0.25
                    val allowK = totalConnectedK * 0.25
                    val designW = totalConnectedW * 1.25
                    val designK = totalConnectedK * 1.25

                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Mandatory Continuous Load Standard (BNBC Part 8 / IEC 60364)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("1. Total Connected Load:", fontSize = 12.sp, color = Color(0xFF374151))
                                Text("${totalConnectedW.roundToInt()} W (%.2f kW)".format(totalConnectedK), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF111827))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("2. Mandatory +25% Allowance:", fontSize = 12.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Medium)
                                Text("+${allowW.roundToInt()} W (+%.2f kW)".format(allowK), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF15803D))
                            }
                            HorizontalDivider(color = Color(0xFF86EFAC), modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("3. Continuous Design Load (1.25×):", fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF166534))
                                Text("${designW.roundToInt()} W (%.2f kW)".format(designK), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFF166534))
                            }
                        }
                    }

                    // SIZING METRICS LIST
                    if (res.secondaryResults.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        for ((label, value) in res.secondaryResults) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = value,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }

                    // Formula and Step-by-Step Toggle
                    if (res.formulaUsed.isNotEmpty() || res.steps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { showFormulaSteps = !showFormulaSteps },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                if (showFormulaSteps) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (showFormulaSteps) "Hide Engineering Steps" else "Show Step-by-Step Engineering Solution",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Collapsible Engineering Solution
                    AnimatedVisibility(visible = showFormulaSteps) {
                        Column {
                            if (res.formulaUsed.isNotEmpty()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                                Text(
                                    text = "Formulas Used:",
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
                                        fontSize = 11.5.sp,
                                        modifier = Modifier.padding(10.dp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            if (res.steps.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Step-by-Step Engineering Breakdown:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Column(modifier = Modifier.padding(top = 4.dp)) {
                                    res.steps.forEach { step ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "${step.stepNumber}. ${step.title}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.5.sp,
                                                    color = ElectricBluePrimary
                                                )
                                                Text(
                                                    text = "Formula: ${step.equation}",
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Result: ${step.result}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF15803D)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (res.standardBasis.isNotBlank() || res.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(8.dp))
                                if (res.standardBasis.isNotBlank()) {
                                    Text(
                                        text = "Standard Reference: ${res.standardBasis}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                res.notes.forEach { note ->
                                    Text(
                                        text = "• $note",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Save, Copy, Share, PDF Report, Print
            com.example.ui.components.CalculatorActionButtons(
                isSaved = isSaved,
                onSave = {
                    coroutineScope.launch {
                        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
                        val summary = "Loads (${loads.size}): " + loads.take(5).joinToString(", ") { "${it.name} ${it.displayOriginal} (x${it.quantity})" } + " | Design: %.2f kW (Connected: %.2f kW)".format(loads.sumOf { it.totalKw } * 1.25, loads.sumOf { it.totalKw })
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
                },
                onCopy = {
                    val totalW = loads.sumOf { it.totalWatts }
                    val totalK = totalW / 1000.0
                    val designK = totalK * 1.25
                    val copyText = "House Wiring Load Calculation\n" +
                        "Total Connected Load: %.2f kW (${totalW.roundToInt()} W)\n" +
                        "Mandatory 25% Allowance: +%.2f kW (+${(totalW * 0.25).roundToInt()} W)\n" +
                        "Continuous Design Load: %.2f kW (${(totalW * 1.25).roundToInt()} W)\n" +
                        "Main Breaker: ${res.secondaryResults.firstOrNull { it.first == "Main Distribution Breaker" }?.second ?: ""}\n" +
                        "Service Cable: ${res.secondaryResults.firstOrNull { it.first == "Main Service Cable" }?.second ?: ""}\n" +
                        "Standards: ${res.standardBasis}"
                    ShareUtils.copyToClipboard(context, calculator.name, copyText)
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                onShare = {
                    val totalW = loads.sumOf { it.totalWatts }
                    val shareText = "House Wiring Load Calculation Report\n" +
                        "Connected Load: %.2f kW (${totalW.roundToInt()} W)\n" +
                        "Design Load (with +25% allowance): %.2f kW\n" +
                        "Main Breaker: ${res.secondaryResults.firstOrNull { it.first == "Main Distribution Breaker" }?.second ?: ""}\n" +
                        "Service Cable: ${res.secondaryResults.firstOrNull { it.first == "Main Service Cable" }?.second ?: ""}\n" +
                        "Calculated with ELECTRICAL CALCULATION ALL"
                    ShareUtils.shareText(context, calculator.name, shareText)
                },
                onPdf = {
                    try {
                        val inputsMap = mutableMapOf<String, String>()
                        val totalW = loads.sumOf { it.totalWatts }
                        inputsMap["Total Connected Load"] = "%.2f kW (${totalW.roundToInt()} W)".format(totalW / 1000.0)
                        inputsMap["Mandatory 25% Allowance"] = "+%.2f kW (+${(totalW * 0.25).roundToInt()} W)".format(totalW * 0.25 / 1000.0)
                        inputsMap["Continuous Design Load"] = "%.2f kW (${(totalW * 1.25).roundToInt()} W)".format(totalW * 1.25 / 1000.0)
                        inputsMap["Supply Phase"] = supplyPhase
                        inputsMap["Nominal Voltage"] = "$supplyVoltageStr V"
                        inputsMap["Power Factor"] = powerFactorStr
                        inputsMap["Cable Conductor"] = conductorMaterial
                        inputsMap["Cable Run Length"] = "$cableLengthStr m"

                        loads.take(10).forEachIndexed { i, it ->
                            inputsMap["Device #${i + 1}"] = "${it.name} [${it.phase}]: ${it.displayOriginal} × ${it.quantity} = ${it.totalWatts.roundToInt()} W (%.3f kW)".format(it.totalKw)
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
                },
                onPrint = {
                    try {
                        val inputsMap = mutableMapOf<String, String>()
                        val totalW = loads.sumOf { it.totalWatts }
                        inputsMap["Total Connected Load"] = "%.2f kW (${totalW.roundToInt()} W)".format(totalW / 1000.0)
                        inputsMap["Mandatory 25% Allowance"] = "+%.2f kW (+${(totalW * 0.25).roundToInt()} W)".format(totalW * 0.25 / 1000.0)
                        inputsMap["Continuous Design Load"] = "%.2f kW (${(totalW * 1.25).roundToInt()} W)".format(totalW * 1.25 / 1000.0)
                        inputsMap["Supply Phase"] = supplyPhase
                        inputsMap["Nominal Voltage"] = "$supplyVoltageStr V"
                        inputsMap["Power Factor"] = powerFactorStr
                        inputsMap["Cable Conductor"] = conductorMaterial
                        inputsMap["Cable Run Length"] = "$cableLengthStr m"

                        loads.take(10).forEachIndexed { i, it ->
                            inputsMap["Device #${i + 1}"] = "${it.name} [${it.phase}]: ${it.displayOriginal} × ${it.quantity} = ${it.totalWatts.roundToInt()} W (%.3f kW)".format(it.totalKw)
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
            )
        }
    }

    // 7. SEARCHABLE DEVICE DROPDOWN DIALOG
    if (isDropdownExpanded) {
        Dialog(onDismissRequest = { isDropdownExpanded = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 540.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Electrical Appliance",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ElectricBluePrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = dropdownSearchQuery,
                        onValueChange = { dropdownSearchQuery = it },
                        label = { Text("Search (Light, Fan, AC, Pump, TV...)") },
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
                                        selectedCatalogItem = item
                                        pendingName = item.name
                                        pendingPowerStr = item.defaultWattage.roundToInt().toString()
                                        pendingUnit = "W"
                                        pendingPhase = item.defaultPhase
                                        isDropdownExpanded = false
                                    }
                                    .padding(vertical = 3.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(item.category, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            if (item.defaultPhase.contains("3")) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(3.dp)) {
                                                    Text(
                                                        text = "3-Phase",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFB45309),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
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
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
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

    // 8. CUSTOM LOAD DIALOG
    if (showCustomDialog) {
        Dialog(onDismissRequest = { showCustomDialog = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "+ ADD CUSTOM DEVICE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ElectricBluePrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Device Name") },
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

                        // Unit Selector (W / kW)
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
                            label = { Text("Qty") },
                            modifier = Modifier.weight(0.8f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Phase Selector for Custom Device
                    Text("Phase", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("1 Phase", "3 Phase").forEach { ph ->
                            val isSel = customPhase == ph
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(if (isSel) ElectricBluePrimary else Color.Transparent)
                                    .clickable { customPhase = ph },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ph,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
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
                                    Toast.makeText(context, "Device name is required", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val p = customPowerStr.toDoubleOrNull()
                                val q = customQtyStr.toIntOrNull()
                                if (p == null || p <= 0.0) {
                                    Toast.makeText(context, "Valid power value is required", Toast.LENGTH_SHORT).show()
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
                                        phase = customPhase,
                                        category = "Custom Device"
                                    )
                                )
                                isSampleDataActive = false
                                showCustomDialog = false
                                runCalculation()
                                Toast.makeText(context, "Added ${customName.trim()}", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
                        ) {
                            Text("Add Custom Device")
                        }
                    }
                }
            }
        }
    }

    // 9. EDIT LOAD DIALOG
    editingItem?.let { item ->
        Dialog(onDismissRequest = { editingItem = null }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Edit Electrical Device",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ElectricBluePrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Device Name") },
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

                        // Unit Selector (W / kW)
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
                            label = { Text("Qty") },
                            modifier = Modifier.weight(0.8f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Phase Selector for Edit Device
                    Text("Phase", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("1 Phase", "3 Phase").forEach { ph ->
                            val isSel = editPhase == ph
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(if (isSel) ElectricBluePrimary else Color.Transparent)
                                    .clickable { editPhase = ph },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ph,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
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
                                            quantity = q,
                                            phase = editPhase
                                        )
                                        runCalculation()
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
