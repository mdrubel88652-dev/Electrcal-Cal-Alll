package com.example.data.model

import java.util.UUID
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Data models and calculation engine for Solar PV System Design & Battery Backup.
 */
enum class SolarSystemType(val label: String, val description: String) {
    OFF_GRID("Off Grid", "Standalone solar and battery system independent of the utility grid"),
    HYBRID("Hybrid", "Bi-directional system combining solar PV, battery bank, and grid synchronization"),
    SOLAR_BATTERY_BACKUP("Solar + Battery Backup", "Grid-tied solar system with automatic battery backup transfer"),
    GRID_TIED_BACKUP("Grid-Tied + Backup", "Grid export system equipped with dedicated critical load subpanel battery backup")
}

enum class SolarBatteryType(val label: String, val typicalDod: Double, val description: String) {
    LEAD_ACID("Lead Acid (Flooded)", 50.0, "Deep cycle flooded lead acid (Max 50% DoD)"),
    AGM("AGM (Absorbent Glass Mat)", 50.0, "Sealed maintenance-free AGM (Max 50% DoD)"),
    GEL("Gel Electrolyte", 60.0, "Sealed gel VRLA battery (Max 60% DoD)"),
    TUBULAR("Tubular Deep Cycle", 60.0, "High endurance tubular plate battery (Max 60% DoD)"),
    LITHIUM_ION("Lithium-ion (NMC)", 80.0, "High density lithium-ion battery (Max 80% DoD)"),
    LIFEPO4("LiFePO4 (Lithium Iron Phosphate)", 90.0, "Premium long-cycle lithium iron phosphate (Max 90% DoD)"),
    CUSTOM("Custom Battery", 70.0, "User-defined battery chemistry and specifications")
}

data class SolarDesignInputs(
    val dailyEnergyKwh: Double = 10.0,
    val peakSunHours: Double = 4.5,
    val backupHours: Double = 6.0,
    val backupLoadKw: Double = 1.0, // If 0, derived from dailyEnergyKwh / 24
    val systemType: SolarSystemType = SolarSystemType.OFF_GRID,
    val systemVoltage: Double = 48.0, // 12, 24, 48, 96 V
    val panelWattage: Double = 550.0, // Watts
    val perfFactorPercent: Double = 80.0, // 80% default derate
    val batteryType: SolarBatteryType = SolarBatteryType.LIFEPO4,
    val batteryUnitVoltage: Double = 12.0, // 12V unit
    val batteryUnitAh: Double = 200.0, // 200Ah
    val dodPercent: Double = 90.0, // Depth of discharge
    val batteryMarginPercent: Double = 10.0, // Safety margin
    val peakLoadKw: Double = 3.5, // Maximum/peak connected AC load
    val powerFactor: Double = 0.90,
    val inverterEffPercent: Double = 90.0,
    val inverterMarginPercent: Double = 20.0,
    val outputPhase: String = "Single Phase 230V", // "Single Phase 230V" or "Three Phase 400V"
    val controllerType: String = "MPPT", // "MPPT" or "PWM"

    // Optional detailed electrical specs for PV Array configuration
    val panelVoc: Double? = null,
    val panelVmp: Double? = null,
    val panelIsc: Double? = null,
    val panelImp: Double? = null,
    val mpptMaxVoltage: Double? = null,
    val mpptMinVoltage: Double? = null,
    val mpptMaxCurrent: Double? = null
)

data class SolarPvStringConfig(
    val panelsInSeries: Int,
    val parallelStrings: Int,
    val totalPanels: Int,
    val stringVoc: Double,
    val stringVmp: Double,
    val arrayIsc: Double,
    val arrayImp: Double,
    val arrayPowerW: Double,
    val isVoltageValid: Boolean,
    val isCurrentValid: Boolean,
    val notes: String
)

data class SolarDesignResult(
    // 1. PV Array
    val requiredDailySolarEnergyKwh: Double,
    val requiredPvPowerKw: Double,
    val panelWattage: Double,
    val panelsCount: Int,
    val actualPvArrayPowerW: Double,
    val actualPvArrayPowerKw: Double,

    // 2. Battery Bank
    val effectiveBackupLoadKw: Double,
    val requiredBackupEnergyKwh: Double,
    val adjustedBatteryEnergyKwh: Double,
    val requiredBankCapacityAh: Double,
    val batteriesInSeries: Int,
    val parallelStrings: Int,
    val totalBatteries: Int,
    val actualBankVoltage: Double,
    val actualBankAh: Double,
    val actualBankEnergyKwh: Double,
    val usableBatteryEnergyKwh: Double,
    val estimatedBackupHours: Double,
    val estimatedBackupMinutes: Int,
    val backupRequirementSatisfied: Boolean,

    // 3. Inverter
    val calculatedInverterKva: Double,
    val recommendedInverterKva: Double,
    val recommendedInverterContinuousKw: Double,
    val inverterOutputVoltage: String,
    val inverterPhase: String,

    // 4. Charge Controller
    val controllerType: String,
    val calculatedControllerCurrentAmps: Double,
    val recommendedControllerAmps: Double,
    val controllerQuantity: Int,
    val perControllerAmps: Double,

    // 5. Array String Configuration (if detailed specs provided)
    val pvStringConfig: SolarPvStringConfig?,
    val pvConfigMissingMessage: String?,

    // 6. Documentation
    val steps: List<String>,
    val formulas: List<Pair<String, String>>,
    val assumptions: List<String>,
    val warnings: List<String>
)

object SolarSystemDesignEngine {

    // Standard inverter kVA sizes
    private val STANDARD_INVERTER_SIZES_KVA = listOf(
        1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 5.0, 6.0, 7.5, 8.0, 10.0, 12.0, 15.0, 20.0, 25.0, 30.0, 40.0, 50.0
    )

    // Standard charge controller current sizes
    private val STANDARD_CONTROLLER_AMPS = listOf(
        20.0, 30.0, 40.0, 50.0, 60.0, 80.0, 100.0
    )

    fun calculate(inputs: SolarDesignInputs): SolarDesignResult {
        val dailyEnergyKwh = max(0.1, inputs.dailyEnergyKwh)
        val peakSunHours = max(0.5, inputs.peakSunHours)
        val backupHours = max(0.1, inputs.backupHours)
        val perfFactor = (inputs.perfFactorPercent / 100.0).coerceIn(0.40, 1.00)
        val panelW = max(50.0, inputs.panelWattage)
        val sysVoltage = max(12.0, inputs.systemVoltage)
        val battUnitV = max(2.0, inputs.batteryUnitVoltage)
        val battUnitAh = max(10.0, inputs.batteryUnitAh)
        val dod = (inputs.dodPercent / 100.0).coerceIn(0.20, 1.00)
        val battMargin = (inputs.batteryMarginPercent / 100.0).coerceIn(0.0, 0.50)
        val invEff = (inputs.inverterEffPercent / 100.0).coerceIn(0.50, 1.00)
        val invMargin = (inputs.inverterMarginPercent / 100.0).coerceIn(0.0, 0.50)
        val pf = inputs.powerFactor.coerceIn(0.50, 1.00)

        // Effective backup load (kW)
        val effectiveBackupLoadKw = if (inputs.backupLoadKw > 0) {
            inputs.backupLoadKw
        } else {
            dailyEnergyKwh / 24.0
        }

        // --- 1. SOLAR PV CALCULATION ---
        // Required Daily Solar Energy = Daily Energy / Performance Factor
        val reqDailySolarKwh = dailyEnergyKwh / perfFactor

        // Required PV Power (kW) = Required Solar Energy / Peak Sun Hours
        val reqPvPowerKw = reqDailySolarKwh / peakSunHours
        val reqPvPowerW = reqPvPowerKw * 1000.0

        // Number of panels (Round UP - never down)
        val panelsCount = max(1, ceil(reqPvPowerW / panelW).toInt())
        val actualPvArrayPowerW = panelsCount * panelW
        val actualPvArrayPowerKw = actualPvArrayPowerW / 1000.0

        // --- 2. BATTERY BANK CALCULATION ---
        // Backup energy needed (kWh) = Backup Load × Backup Hours
        val reqBackupEnergyKwh = effectiveBackupLoadKw * backupHours

        // Adjusted Battery Energy = Backup Energy / (DoD × Inverter Efficiency × (1 - Battery Margin))
        val adjustedBatteryEnergyKwh = reqBackupEnergyKwh / (dod * invEff * (1.0 - battMargin))
        val adjustedBatteryEnergyWh = adjustedBatteryEnergyKwh * 1000.0

        // Required Bank Capacity (Ah) = Adjusted Energy (Wh) / System Voltage
        val reqBankCapacityAh = adjustedBatteryEnergyWh / sysVoltage

        // Batteries in Series (increases voltage to match system voltage)
        val batteriesInSeries = max(1, (sysVoltage / battUnitV).roundToInt())
        val actualBankVoltage = batteriesInSeries * battUnitV

        // Parallel strings (increases Ah capacity to meet required Ah)
        val parallelStrings = max(1, ceil(reqBankCapacityAh / battUnitAh).toInt())
        val totalBatteries = batteriesInSeries * parallelStrings
        val actualBankAh = parallelStrings * battUnitAh
        val actualBankEnergyKwh = (actualBankVoltage * actualBankAh) / 1000.0

        // Usable battery energy taking DoD and inverter efficiency into account
        val usableBatteryEnergyKwh = actualBankEnergyKwh * dod * invEff
        val estimatedBackupHours = if (effectiveBackupLoadKw > 0.001) {
            usableBatteryEnergyKwh / effectiveBackupLoadKw
        } else {
            backupHours
        }
        val estimatedBackupHoursInt = floor(estimatedBackupHours).toInt()
        val estimatedBackupMinutes = ((estimatedBackupHours - estimatedBackupHoursInt) * 60).roundToInt()
        val backupRequirementSatisfied = estimatedBackupHours >= (backupHours - 0.05)

        // --- 3. INVERTER SIZING ---
        val effectivePeakLoadKw = if (inputs.peakLoadKw > 0) {
            inputs.peakLoadKw
        } else {
            max(effectiveBackupLoadKw * 1.5, actualPvArrayPowerKw)
        }

        // Required Inverter kVA = (Peak Load kW / (PF × Inverter Efficiency)) × (1 + Margin)
        val calculatedInverterKva = (effectivePeakLoadKw / (pf * invEff)) * (1.0 + invMargin)
        val recommendedInverterKva = STANDARD_INVERTER_SIZES_KVA.firstOrNull { it >= calculatedInverterKva }
            ?: (ceil(calculatedInverterKva / 5.0) * 5.0)
        val recommendedInverterContinuousKw = recommendedInverterKva * pf

        // --- 4. CHARGE CONTROLLER SIZING ---
        // Controller Current = (PV Array Power (W) / System Voltage) × 1.25 (NEC/IEC design margin)
        val calculatedControllerCurrentAmps = (actualPvArrayPowerW / sysVoltage) * 1.25

        // Check if multiple controllers are needed (if > 80A standard controller)
        val (recommendedControllerAmps, controllerQuantity, perControllerAmps) = when {
            calculatedControllerCurrentAmps <= 80.0 -> {
                val std = STANDARD_CONTROLLER_AMPS.firstOrNull { it >= calculatedControllerCurrentAmps } ?: 80.0
                Triple(std, 1, std)
            }
            calculatedControllerCurrentAmps <= 100.0 -> {
                Triple(100.0, 1, 100.0)
            }
            else -> {
                // Split among 2 or more 60A/80A controllers
                val qty = ceil(calculatedControllerCurrentAmps / 60.0).toInt()
                val targetPerUnit = calculatedControllerCurrentAmps / qty
                val unitSize = STANDARD_CONTROLLER_AMPS.firstOrNull { it >= targetPerUnit } ?: 80.0
                Triple(unitSize * qty, qty, unitSize)
            }
        }

        // --- 5. DETAILED PV STRING CONFIGURATION (if specs provided) ---
        var pvStringConfig: SolarPvStringConfig? = null
        var pvConfigMissingMessage: String? = null

        val hasPanelSpecs = inputs.panelVoc != null && inputs.panelVmp != null &&
                inputs.panelIsc != null && inputs.panelImp != null &&
                inputs.panelVoc > 0 && inputs.panelVmp > 0
        val hasMpptSpecs = inputs.mpptMaxVoltage != null && inputs.mpptMinVoltage != null &&
                inputs.mpptMaxVoltage > 0 && inputs.mpptMinVoltage > 0

        if (hasPanelSpecs && hasMpptSpecs) {
            val voc = inputs.panelVoc!!
            val vmp = inputs.panelVmp!!
            val isc = inputs.panelIsc!!
            val imp = inputs.panelImp!!
            val mpptMaxV = inputs.mpptMaxVoltage!!
            val mpptMinV = inputs.mpptMinVoltage!!
            val mpptMaxI = inputs.mpptMaxCurrent ?: 999.0

            // Cold temperature Voc adjustment (approx +15% at minimum design temperature)
            val maxSeriesByVoc = floor(mpptMaxV / (voc * 1.15)).toInt()
            val minSeriesByVmp = ceil(mpptMinV / vmp).toInt()

            val seriesPanels = max(1, min(maxSeriesByVoc, panelsCount))
            val parallelStr = max(1, ceil(panelsCount.toDouble() / seriesPanels).toInt())
            val actualTotalPanels = seriesPanels * parallelStr

            val stringVoc = seriesPanels * voc
            val stringVmp = seriesPanels * vmp
            val arrayIsc = parallelStr * isc
            val arrayImp = parallelStr * imp
            val arrayPowerW = stringVmp * arrayImp

            val isVValid = stringVoc <= mpptMaxV && stringVmp >= mpptMinV
            val isIValid = arrayImp <= mpptMaxI

            val notes = buildString {
                append("Arrangement: $seriesPanels in Series × $parallelStr Strings in Parallel ($actualTotalPanels Panels total). ")
                if (!isVValid) append("⚠ String Voc ($stringVoc V) exceeds or Vmp is outside MPPT window ($mpptMinV - $mpptMaxV V)! ")
                if (!isIValid) append("⚠ Array Imp ($arrayImp A) exceeds MPPT max current ($mpptMaxI A)! ")
                if (isVValid && isIValid) append("✓ Configuration fully matches MPPT electrical window.")
            }

            pvStringConfig = SolarPvStringConfig(
                panelsInSeries = seriesPanels,
                parallelStrings = parallelStr,
                totalPanels = actualTotalPanels,
                stringVoc = stringVoc,
                stringVmp = stringVmp,
                arrayIsc = arrayIsc,
                arrayImp = arrayImp,
                arrayPowerW = arrayPowerW,
                isVoltageValid = isVValid,
                isCurrentValid = isIValid,
                notes = notes
            )
        } else {
            pvConfigMissingMessage = "Detailed PV string configuration requires panel electrical specifications (Voc, Vmp, Isc, Imp) and inverter MPPT input specifications (Max PV Voltage, MPPT Window, Max Current)."
        }

        // --- 6. STEPS & DOCUMENTATION ---
        val steps = listOf(
            "Step 1: Daily Solar Energy Requirement with Derating Factor (${inputs.perfFactorPercent}%)\n" +
                    "  Required Daily Solar Energy = ${fmt(dailyEnergyKwh)} kWh / ${fmt(perfFactor)} = ${fmt(reqDailySolarKwh)} kWh/day",

            "Step 2: Required Solar PV Peak Power (Peak Sun Hours: ${fmt(peakSunHours)} h/day)\n" +
                    "  Required PV Power = ${fmt(reqDailySolarKwh)} kWh / ${fmt(peakSunHours)} h = ${fmt(reqPvPowerKw)} kW (${fmt(reqPvPowerW)} W)",

            "Step 3: Solar Panel Count Selection (${panelW.roundToInt()}W Panel)\n" +
                    "  Panels Count = ceil(${fmt(reqPvPowerW)} W / ${panelW.roundToInt()} W) = ceil(${fmt(reqPvPowerW / panelW)}) = $panelsCount Panels\n" +
                    "  Actual Installed PV Array = $panelsCount × ${panelW.roundToInt()} W = ${fmt(actualPvArrayPowerW)} W (${fmt(actualPvArrayPowerKw)} kW)",

            "Step 4: Battery Energy & Bank Sizing (${inputs.batteryType.label})\n" +
                    "  Backup Energy Needed = ${fmt(effectiveBackupLoadKw)} kW × ${fmt(backupHours)} h = ${fmt(reqBackupEnergyKwh)} kWh\n" +
                    "  Adjusted Battery Energy = ${fmt(reqBackupEnergyKwh)} / (DoD ${inputs.dodPercent}% × Inv Eff ${inputs.inverterEffPercent}% × Margin ${(1.0 - battMargin) * 100}%)\n" +
                    "  Adjusted Battery Energy = ${fmt(adjustedBatteryEnergyKwh)} kWh (${fmt(adjustedBatteryEnergyWh)} Wh)\n" +
                    "  Required Bank Capacity = ${fmt(adjustedBatteryEnergyWh)} Wh / ${sysVoltage.roundToInt()} V = ${fmt(reqBankCapacityAh)} Ah",

            "Step 5: Battery Series / Parallel Configuration (${battUnitV.roundToInt()}V / ${battUnitAh.roundToInt()}Ah Units)\n" +
                    "  Batteries in Series = ${sysVoltage.roundToInt()} V / ${battUnitV.roundToInt()} V = $batteriesInSeries in Series (Multiplies Voltage)\n" +
                    "  Parallel Strings = ceil(${fmt(reqBankCapacityAh)} Ah / ${battUnitAh.roundToInt()} Ah) = $parallelStrings Strings (Multiplies Ah)\n" +
                    "  Total Batteries = $batteriesInSeries Series × $parallelStrings Parallel = $totalBatteries Units\n" +
                    "  Actual Battery Bank Rating = ${actualBankVoltage.roundToInt()} V / ${actualBankAh.roundToInt()} Ah (${fmt(actualBankEnergyKwh)} kWh total, ${fmt(usableBatteryEnergyKwh)} kWh usable)\n" +
                    "  Estimated Runtime = ${fmt(usableBatteryEnergyKwh)} kWh / ${fmt(effectiveBackupLoadKw)} kW = ${estimatedBackupHoursInt}h ${estimatedBackupMinutes}m",

            "Step 6: Inverter Sizing Calculation\n" +
                    "  Peak AC Load = ${fmt(effectivePeakLoadKw)} kW, Power Factor = ${fmt(pf)}, Inverter Efficiency = ${inputs.inverterEffPercent}%\n" +
                    "  Calculated Inverter Requirement = (${fmt(effectivePeakLoadKw)} / (${fmt(pf)} × ${fmt(invEff)})) × (1 + ${inputs.inverterMarginPercent}%) = ${fmt(calculatedInverterKva)} kVA\n" +
                    "  Recommended Standard Inverter Size = ${fmt(recommendedInverterKva)} kVA (${fmt(recommendedInverterContinuousKw)} kW continuous, ${inputs.outputPhase})",

            "Step 7: Charge Controller Sizing (${inputs.controllerType})\n" +
                    "  Design Charging Current = (${fmt(actualPvArrayPowerW)} W / ${sysVoltage.roundToInt()} V) × 1.25 = ${fmt(calculatedControllerCurrentAmps)} A\n" +
                    if (controllerQuantity > 1) {
                        "  Recommended Arrangement: $controllerQuantity × ${perControllerAmps.roundToInt()} A ${inputs.controllerType} Controllers (Total ${recommendedControllerAmps.roundToInt()} A rating)"
                    } else {
                        "  Recommended Controller: 1 × ${recommendedControllerAmps.roundToInt()} A ${inputs.controllerType} Charge Controller"
                    }
        )

        val formulas = listOf(
            "Required PV Power" to "P_pv (kW) = (Daily_Energy_kWh / Performance_Factor) / Peak_Sun_Hours",
            "Solar Panel Count" to "N_panels = ceil(P_pv_Watts / Panel_Wattage)",
            "Battery Bank Energy" to "E_batt (Wh) = (Backup_Load_kW × Backup_Hours × 1000) / (DoD × Inv_Eff × (1 - Margin))",
            "Battery Bank Capacity" to "Ah_bank = E_batt (Wh) / System_Voltage",
            "Battery Series Count" to "N_series = System_Voltage / Unit_Battery_Voltage",
            "Battery Parallel Strings" to "N_parallel = ceil(Ah_bank / Unit_Battery_Ah)",
            "Inverter Size" to "S_inv (kVA) = (Peak_Load_kW / (PF × Inverter_Efficiency)) × (1 + Margin)",
            "Charge Controller Rating" to "I_cc (A) = (Actual_PV_Power_W / System_Voltage) × 1.25"
        )

        val assumptions = listOf(
            "Solar performance/derating factor set to ${inputs.perfFactorPercent}% (accounts for dirt, dust, temperature coefficient, wiring losses, and mismatch).",
            "Battery Depth of Discharge (DoD) is capped at ${inputs.dodPercent}% based on ${inputs.batteryType.label} chemistry.",
            "Battery design safety margin is ${inputs.batteryMarginPercent}%.",
            "Inverter conversion efficiency assumed at ${inputs.inverterEffPercent}%, with a safety margin of ${inputs.inverterMarginPercent}%.",
            "Charge controller sized with 125% continuous duty factor in accordance with NEC Article 690 / IEC 62548 standards.",
            "System layout adapted for: ${inputs.systemType.label}."
        )

        val warnings = mutableListOf<String>()
        if (effectiveBackupLoadKw < 0.2) {
            warnings.add("Backup load is very low (< 200W). Ensure all essential loads (refrigeration, lights, router, fans) are accounted for.")
        }
        if (sysVoltage < 24.0 && actualPvArrayPowerKw > 1.5) {
            warnings.add("System voltage is 12V with a large PV array (${fmt(actualPvArrayPowerKw)} kW). A 24V or 48V bank is strongly recommended to prevent high DC current and thick cabling.")
        }
        if (inputs.dodPercent > 80.0 && inputs.batteryType != SolarBatteryType.LIFEPO4) {
            warnings.add("DoD exceeds 80% for lead-acid/tubular battery. Operating above 50-60% DoD will severely degrade cycle life.")
        }
        warnings.add("Panel string configuration requires actual Voc/Vmp specifications and seasonal temperature coefficients.")
        warnings.add("Verify inverter maximum DC input voltage and MPPT window before final installation.")
        warnings.add("Solar production fluctuates with weather, azimuth angle, tilt angle, and local shading.")
        warnings.add("Final electrical installation, earthing, DC disconnects, and surge protection must be verified by a licensed electrical engineer.")

        return SolarDesignResult(
            requiredDailySolarEnergyKwh = reqDailySolarKwh,
            requiredPvPowerKw = reqPvPowerKw,
            panelWattage = panelW,
            panelsCount = panelsCount,
            actualPvArrayPowerW = actualPvArrayPowerW,
            actualPvArrayPowerKw = actualPvArrayPowerKw,
            effectiveBackupLoadKw = effectiveBackupLoadKw,
            requiredBackupEnergyKwh = reqBackupEnergyKwh,
            adjustedBatteryEnergyKwh = adjustedBatteryEnergyKwh,
            requiredBankCapacityAh = reqBankCapacityAh,
            batteriesInSeries = batteriesInSeries,
            parallelStrings = parallelStrings,
            totalBatteries = totalBatteries,
            actualBankVoltage = actualBankVoltage,
            actualBankAh = actualBankAh,
            actualBankEnergyKwh = actualBankEnergyKwh,
            usableBatteryEnergyKwh = usableBatteryEnergyKwh,
            estimatedBackupHours = estimatedBackupHours,
            estimatedBackupMinutes = estimatedBackupMinutes,
            backupRequirementSatisfied = backupRequirementSatisfied,
            calculatedInverterKva = calculatedInverterKva,
            recommendedInverterKva = recommendedInverterKva,
            recommendedInverterContinuousKw = recommendedInverterContinuousKw,
            inverterOutputVoltage = inputs.outputPhase.substringAfterLast(" "),
            inverterPhase = inputs.outputPhase,
            controllerType = inputs.controllerType,
            calculatedControllerCurrentAmps = calculatedControllerCurrentAmps,
            recommendedControllerAmps = recommendedControllerAmps,
            controllerQuantity = controllerQuantity,
            perControllerAmps = perControllerAmps,
            pvStringConfig = pvStringConfig,
            pvConfigMissingMessage = pvConfigMissingMessage,
            steps = steps,
            formulas = formulas,
            assumptions = assumptions,
            warnings = warnings
        )
    }

    private fun fmt(v: Double): String = String.format("%.2f", v)
}
