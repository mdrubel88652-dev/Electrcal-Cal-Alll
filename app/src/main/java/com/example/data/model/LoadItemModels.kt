package com.example.data.model

import java.util.UUID
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.tan

// ============================================================================
// 1. FACTORY LOAD MODELS
// ============================================================================

data class FactoryLoadItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val power: Double,
    val unit: String = "kW", // "kW" or "W"
    val quantity: Int = 1,
    val phase: String = "3 Phase", // "3 Phase" or "1 Phase"
    val powerFactor: Double = 0.85,
    val loadType: String = "Motor" // "Motor", "Heating", "Lighting", "HVAC", "Other"
) {
    // Internally converted to kW
    val powerInKw: Double
        get() = if (unit.equals("W", ignoreCase = true)) power / 1000.0 else power

    val totalKw: Double
        get() = powerInKw * quantity

    val displayOriginal: String
        get() = if (unit.equals("W", ignoreCase = true)) {
            val kw = power / 1000.0
            "${power.roundToInt()} W (%.3f kW)".format(kw)
        } else {
            "%.2f kW".format(power)
        }
}

data class PredefinedFactoryMachine(
    val name: String,
    val defaultPower: Double,
    val unit: String = "kW",
    val loadType: String = "3 Phase Motor",
    val phase: String = "3 Phase",
    val defaultPf: Double = 0.85
)

object PredefinedFactoryLoadsCatalog {
    val items: List<PredefinedFactoryMachine> = listOf(
        PredefinedFactoryMachine("3 Phase Motor", 7.5, "kW", "3 Phase Motor", "3 Phase", 0.85),
        PredefinedFactoryMachine("1 Phase Motor", 1.5, "kW", "1 Phase Motor", "1 Phase", 0.80),
        PredefinedFactoryMachine("Pump Motor", 7.5, "kW", "3 Phase Motor", "3 Phase", 0.85),
        PredefinedFactoryMachine("Air Compressor", 15.0, "kW", "3 Phase Motor", "3 Phase", 0.85),
        PredefinedFactoryMachine("Blower", 5.5, "kW", "3 Phase Motor", "3 Phase", 0.84),
        PredefinedFactoryMachine("Exhaust Fan", 1.5, "kW", "1 Phase / 3 Phase", "1 Phase", 0.80),
        PredefinedFactoryMachine("Conveyor Motor", 5.5, "kW", "3 Phase Motor", "3 Phase", 0.85),
        PredefinedFactoryMachine("Cooling Fan", 0.75, "kW", "1 Phase Motor", "1 Phase", 0.80),
        PredefinedFactoryMachine("Industrial Fan", 2.2, "kW", "3 Phase Motor", "3 Phase", 0.82),
        PredefinedFactoryMachine("Water Pump", 5.5, "kW", "3 Phase Motor", "3 Phase", 0.85),
        PredefinedFactoryMachine("Deep Tube Well Pump", 11.0, "kW", "3 Phase Motor", "3 Phase", 0.86),
        PredefinedFactoryMachine("Boiler Feed Pump", 15.0, "kW", "3 Phase Motor", "3 Phase", 0.86),
        PredefinedFactoryMachine("Boiler Fan", 7.5, "kW", "3 Phase Motor", "3 Phase", 0.84),
        PredefinedFactoryMachine("ID Fan", 11.0, "kW", "3 Phase Motor", "3 Phase", 0.85),
        PredefinedFactoryMachine("FD Fan", 7.5, "kW", "3 Phase Motor", "3 Phase", 0.85),
        PredefinedFactoryMachine("Cooling Tower", 7.5, "kW", "3 Phase Motor", "3 Phase", 0.84),
        PredefinedFactoryMachine("Chiller", 30.0, "kW", "3 Phase Motor", "3 Phase", 0.88),
        PredefinedFactoryMachine("Compressor", 18.5, "kW", "3 Phase Motor", "3 Phase", 0.86),
        PredefinedFactoryMachine("Refrigerator / Cold Storage", 10.0, "kW", "3 Phase Industrial", "3 Phase", 0.85),
        PredefinedFactoryMachine("Welding Machine", 8.0, "kW", "Industrial Load", "3 Phase", 0.70),
        PredefinedFactoryMachine("Lathe Machine", 5.5, "kW", "3 Phase Motor", "3 Phase", 0.83),
        PredefinedFactoryMachine("Drilling Machine", 2.2, "kW", "3 Phase Motor", "3 Phase", 0.82),
        PredefinedFactoryMachine("Milling Machine", 7.5, "kW", "3 Phase Motor", "3 Phase", 0.84),
        PredefinedFactoryMachine("Grinding Machine", 3.7, "kW", "3 Phase Motor", "3 Phase", 0.83),
        PredefinedFactoryMachine("Cutting Machine", 4.0, "kW", "3 Phase Motor", "3 Phase", 0.83),
        PredefinedFactoryMachine("CNC Machine", 15.0, "kW", "3 Phase Precision", "3 Phase", 0.87),
        PredefinedFactoryMachine("Packaging Machine", 5.5, "kW", "3 Phase Motor", "3 Phase", 0.84),
        PredefinedFactoryMachine("Sewing Machine", 550.0, "W", "1 Phase Motor", "1 Phase", 0.75),
        PredefinedFactoryMachine("Printing Machine", 7.5, "kW", "3 Phase Motor", "3 Phase", 0.85),
        PredefinedFactoryMachine("Heater", 12.0, "kW", "Resistive Heating", "3 Phase", 1.00),
        PredefinedFactoryMachine("Oven", 15.0, "kW", "Industrial Heating", "3 Phase", 1.00),
        PredefinedFactoryMachine("Dryer", 10.0, "kW", "Industrial Heating", "3 Phase", 0.98),
        PredefinedFactoryMachine("Lighting Load", 5000.0, "W", "Factory Lighting", "1 Phase", 0.90),
        PredefinedFactoryMachine("Air Conditioner", 7.5, "kW", "Industrial HVAC", "3 Phase", 0.86),
        PredefinedFactoryMachine("Office Equipment", 3.0, "kW", "1 Phase Equipment", "1 Phase", 0.90),
        PredefinedFactoryMachine("Computer", 250.0, "W", "IT Equipment", "1 Phase", 0.85),
        PredefinedFactoryMachine("Printer", 500.0, "W", "Office Equipment", "1 Phase", 0.85),
        PredefinedFactoryMachine("Other Load", 1.0, "kW", "General Load", "3 Phase", 0.85)
    )
}

// ============================================================================
// 2. HOUSEHOLD LOAD MODELS
// ============================================================================

data class HouseholdLoadItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val power: Double,
    val unit: String = "W", // "W" or "kW"
    val quantity: Int = 1,
    val phase: String = "1 Phase", // "1 Phase" or "3 Phase"
    val category: String = "General"
) {
    // Always convert to kW internally
    val powerInKw: Double
        get() = if (unit.equals("kW", ignoreCase = true)) power else power / 1000.0

    val powerInWatts: Double
        get() = if (unit.equals("kW", ignoreCase = true)) power * 1000.0 else power

    val totalWatts: Double
        get() = powerInWatts * quantity

    val totalKw: Double
        get() = totalWatts / 1000.0

    val displayOriginal: String
        get() = if (unit.equals("kW", ignoreCase = true)) {
            "%.2f kW".format(power)
        } else {
            "${power.roundToInt()} W"
        }
}

data class PredefinedAppliance(
    val name: String,
    val defaultWattage: Double,
    val category: String,
    val defaultPhase: String = "1 Phase"
)

object PredefinedHouseholdLoadsCatalog {
    val items: List<PredefinedAppliance> = listOf(
        // Lighting
        PredefinedAppliance("LED Light", 15.0, "Lighting"),
        PredefinedAppliance("LED Bulb 9W", 9.0, "Lighting"),
        PredefinedAppliance("LED Bulb 12W", 12.0, "Lighting"),
        PredefinedAppliance("LED Bulb 15W", 15.0, "Lighting"),
        PredefinedAppliance("LED Bulb 18W", 18.0, "Lighting"),
        PredefinedAppliance("Tube Light", 36.0, "Lighting"),
        PredefinedAppliance("Tube Light 20W LED", 20.0, "Lighting"),
        PredefinedAppliance("Tube Light 40W Fluo", 40.0, "Lighting"),
        PredefinedAppliance("Ceiling Light / Panel", 24.0, "Lighting"),
        PredefinedAppliance("Decorative Chandelier", 120.0, "Lighting"),
        PredefinedAppliance("Spot Light / COB", 12.0, "Lighting"),
        PredefinedAppliance("Outdoor / Security Light", 50.0, "Lighting"),

        // Fans & Ventilation
        PredefinedAppliance("Ceiling Fan", 80.0, "Fans"),
        PredefinedAppliance("Exhaust Fan", 45.0, "Fans"),
        PredefinedAppliance("Exhaust Fan (Kitchen/Bath)", 45.0, "Fans"),
        PredefinedAppliance("Wall Fan", 55.0, "Fans"),
        PredefinedAppliance("Table Fan", 45.0, "Fans"),
        PredefinedAppliance("Stand / Pedestal Fan", 65.0, "Fans"),
        PredefinedAppliance("Exhaust System (Central)", 150.0, "Fans"),

        // Entertainment & IT
        PredefinedAppliance("TV", 120.0, "Entertainment"),
        PredefinedAppliance("LED Television 43\"", 75.0, "Entertainment"),
        PredefinedAppliance("Smart Television 55\"-65\"", 120.0, "Entertainment"),
        PredefinedAppliance("Home Theater / Soundbar", 100.0, "Entertainment"),
        PredefinedAppliance("Desktop Computer (PC)", 250.0, "IT & Electronics"),
        PredefinedAppliance("Laptop Computer", 65.0, "IT & Electronics"),
        PredefinedAppliance("Printer / Scanner", 300.0, "IT & Electronics"),
        PredefinedAppliance("Wi-Fi Router & ONU", 15.0, "IT & Electronics"),
        PredefinedAppliance("CCTV System & DVR (4-8 Ch)", 60.0, "IT & Electronics"),
        PredefinedAppliance("Mobile / Tablet Charger", 20.0, "IT & Electronics"),
        PredefinedAppliance("Gaming Console", 180.0, "IT & Electronics"),

        // Refrigeration & Kitchen
        PredefinedAppliance("Refrigerator", 200.0, "Refrigeration"),
        PredefinedAppliance("Refrigerator (Direct Cool)", 150.0, "Refrigeration"),
        PredefinedAppliance("Refrigerator (Frost-Free)", 250.0, "Refrigeration"),
        PredefinedAppliance("Freezer", 300.0, "Refrigeration"),
        PredefinedAppliance("Deep Freezer", 250.0, "Refrigeration"),
        PredefinedAppliance("Microwave Oven", 1200.0, "Kitchen"),
        PredefinedAppliance("Electric Oven (OTG)", 2000.0, "Kitchen"),
        PredefinedAppliance("Rice Cooker", 700.0, "Kitchen"),
        PredefinedAppliance("Induction Cooker", 1800.0, "Kitchen"),
        PredefinedAppliance("Infrared Cooker", 2000.0, "Kitchen"),
        PredefinedAppliance("Electric Kettle", 1500.0, "Kitchen"),
        PredefinedAppliance("Blender / Grinder", 600.0, "Kitchen"),
        PredefinedAppliance("Kitchen Exhaust Chimney", 180.0, "Kitchen"),
        PredefinedAppliance("Toaster / Sandwich Maker", 800.0, "Kitchen"),
        PredefinedAppliance("Air Fryer", 1500.0, "Kitchen"),
        PredefinedAppliance("Water Purifier / Filter", 40.0, "Kitchen"),
        PredefinedAppliance("Dishwasher", 1500.0, "Kitchen"),

        // Heating, Cooling & Motors
        PredefinedAppliance("Air Conditioner 1.0 Ton", 1200.0, "Cooling"),
        PredefinedAppliance("Air Conditioner 1.5 Ton", 1800.0, "Cooling"),
        PredefinedAppliance("Air Conditioner 2.0 Ton", 2400.0, "Cooling"),
        PredefinedAppliance("3-Phase Air Conditioner 3.0 Ton", 3600.0, "Cooling", "3 Phase"),
        PredefinedAppliance("Water Heater / Geyser", 2000.0, "Heating"),
        PredefinedAppliance("Instant Water Heater", 3000.0, "Heating"),
        PredefinedAppliance("Room Heater (Convector/Halogen)", 1500.0, "Heating"),
        PredefinedAppliance("Electric Iron", 1000.0, "Laundry"),
        PredefinedAppliance("Washing Machine (Semi-Auto)", 400.0, "Laundry"),
        PredefinedAppliance("Washing Machine (Front Load)", 1800.0, "Laundry"),
        PredefinedAppliance("Clothes Dryer", 2000.0, "Laundry"),
        PredefinedAppliance("Water Pump 0.5 HP", 375.0, "Motors"),
        PredefinedAppliance("Water Pump 1.0 HP", 750.0, "Motors"),
        PredefinedAppliance("Water Pump 1.5 HP", 1120.0, "Motors"),
        PredefinedAppliance("Submersible Pump 2.0 HP (3-Phase)", 1500.0, "Motors", "3 Phase"),

        // Sockets & Power Backup
        PredefinedAppliance("Normal Socket Point (5A/6A)", 100.0, "Sockets"),
        PredefinedAppliance("Power Socket Point (15A/16A)", 1000.0, "Sockets"),
        PredefinedAppliance("IPS / Inverter Charger", 800.0, "Power Backup"),
        PredefinedAppliance("Online UPS 1kVA", 800.0, "Power Backup"),
        PredefinedAppliance("Hair Dryer", 1200.0, "Personal Care"),
        PredefinedAppliance("Vacuum Cleaner", 1200.0, "Cleaning"),
        PredefinedAppliance("Other Custom Appliance", 100.0, "General")
    )
}

// ============================================================================
// 3. PFI CAPACITOR BANK SIZING MODELS & OPTIMIZER
// ============================================================================

data class CapacitorStep(
    val stepNumber: Int,
    val kvar: Double,
    val unitsDescription: String
)

data class PfiBankSizingResult(
    val activePowerKw: Double,
    val existingPf: Double,
    val targetPf: Double,
    val phi1Deg: Double,
    val phi2Deg: Double,
    val tanPhi1: Double,
    val tanPhi2: Double,
    val requiredKvar: Double,
    val recommendedBankKvar: Double,
    val steps: List<CapacitorStep>,
    val totalStepsCount: Int,
    val totalCapacitorUnits: Int,
    val capacitorUnitBreakdown: String,
    val recommendedControllerSteps: Int,
    val systemVoltage: Double,
    val systemFrequency: Int,
    val phase: String,
    val recommendedCapacitorVoltageRating: String,
    val fullLoadCurrentA: Double,
    val capacitorBankRatedCurrentA: Double,
    val contactorSpec: String,
    val dischargeResistorSpec: String,
    val harmonicWarning: String,
    val standardBasis: String
)

object PfiBankOptimizer {

    // Standard available step ratings in kVAR
    val STANDARD_RATINGS = listOf(5.0, 10.0, 12.5, 15.0, 20.0, 25.0, 30.0, 40.0, 50.0)

    fun calculate(
        loadValue: Double,
        isLoadInKva: Boolean,
        existingPf: Double,
        targetPf: Double,
        systemVoltage: Double = 400.0,
        frequency: Int = 50,
        phase: String = "3 Phase",
        existingKvar: Double = 0.0,
        availableStepRatings: List<Double> = STANDARD_RATINGS
    ): PfiBankSizingResult {
        val clampedPf1 = existingPf.coerceIn(0.10, 0.99)
        val clampedPf2 = targetPf.coerceIn(clampedPf1 + 0.005, 1.0)

        val activePowerKw = if (isLoadInKva) {
            loadValue * clampedPf1
        } else {
            loadValue
        }

        val phi1Rad = acos(clampedPf1)
        val phi2Rad = acos(clampedPf2)
        val phi1Deg = Math.toDegrees(phi1Rad)
        val phi2Deg = Math.toDegrees(phi2Rad)

        val tanPhi1 = tan(phi1Rad)
        val tanPhi2 = tan(phi2Rad)

        // Theoretical Qc required
        val netKvarTheoretical = (activePowerKw * (tanPhi1 - tanPhi2) - existingKvar).coerceAtLeast(0.0)

        // Select practical capacitor bank steps
        val steps = mutableListOf<CapacitorStep>()
        val unitsMap = mutableMapOf<Double, Int>()

        // Sizing logic: practical combination matching standard step sizing
        // For small banks (< 30 kVAR): 5 or 10 kVAR steps
        // For medium banks (30 - 150 kVAR): 10, 15, 20, 25 kVAR steps
        // For large banks (> 150 kVAR): 25, 40, 50 kVAR steps
        val candidateSteps = availableStepRatings.sorted()

        var remainingKvar = netKvarTheoretical
        var currentBankKvar = 0.0

        if (netKvarTheoretical <= 15.0) {
            val stepSize = if (candidateSteps.contains(5.0)) 5.0 else candidateSteps.first()
            val count = ceil(netKvarTheoretical / stepSize).toInt().coerceAtLeast(1)
            for (i in 1..count) {
                steps.add(CapacitorStep(i, stepSize, "1 × ${stepSize.roundToInt()} kVAR"))
                unitsMap[stepSize] = (unitsMap[stepSize] ?: 0) + 1
            }
            currentBankKvar = count * stepSize
        } else if (netKvarTheoretical <= 50.0) {
            // E.g., 20 or 40 kVAR -> 10 or 12.5 kVAR steps
            val stepSize = if (candidateSteps.contains(10.0)) 10.0 else 12.5
            val count = ceil(netKvarTheoretical / stepSize).toInt().coerceAtLeast(2)
            for (i in 1..count) {
                steps.add(CapacitorStep(i, stepSize, "1 × ${stepSize.roundToInt()} kVAR"))
                unitsMap[stepSize] = (unitsMap[stepSize] ?: 0) + 1
            }
            currentBankKvar = count * stepSize
        } else {
            // Staged APFC arrangement: progressive steps, e.g., 10, 10, 20, 25, 25, 50
            // Choose base step (e.g. 10 or 15 or 25 kVAR)
            val baseStep = if (netKvarTheoretical <= 100.0) 10.0 else 25.0
            var accum = 0.0
            var stepNum = 1

            // 1st stage: base step
            steps.add(CapacitorStep(stepNum++, baseStep, "1 × ${baseStep.roundToInt()} kVAR"))
            unitsMap[baseStep] = (unitsMap[baseStep] ?: 0) + 1
            accum += baseStep

            // 2nd stage: base step
            if (accum < netKvarTheoretical) {
                steps.add(CapacitorStep(stepNum++, baseStep, "1 × ${baseStep.roundToInt()} kVAR"))
                unitsMap[baseStep] = (unitsMap[baseStep] ?: 0) + 1
                accum += baseStep
            }

            // Subsequent stages: larger steps (e.g. 20, 25, 40, or 50)
            val largerStep = if (netKvarTheoretical <= 150.0) {
                if (candidateSteps.contains(20.0)) 20.0 else 25.0
            } else {
                if (candidateSteps.contains(50.0)) 50.0 else 40.0
            }

            while (accum < netKvarTheoretical && stepNum <= 12) {
                val needed = netKvarTheoretical - accum
                val nextSize = candidateSteps.filter { it >= needed }.minOrNull() ?: largerStep
                steps.add(CapacitorStep(stepNum++, nextSize, "1 × ${nextSize.roundToInt()} kVAR"))
                unitsMap[nextSize] = (unitsMap[nextSize] ?: 0) + 1
                accum += nextSize
            }
            currentBankKvar = accum
        }

        val totalUnits = unitsMap.values.sum()
        val unitsSummary = unitsMap.entries.joinToString(", ") { "${it.value} × ${it.key.roundToInt()} kVAR" }

        // Determine APFC controller step rating (typically 4, 6, 8, 12, or 14 steps)
        val stepCount = steps.size
        val controllerSteps = when {
            stepCount <= 4 -> 4
            stepCount <= 6 -> 6
            stepCount <= 8 -> 8
            stepCount <= 12 -> 12
            else -> 14
        }

        // Current draw calculation
        val isThreePhase = phase.contains("3", ignoreCase = true)
        val ratedBankCurrent = if (isThreePhase) {
            (currentBankKvar * 1000.0) / (sqrt(3.0) * systemVoltage)
        } else {
            (currentBankKvar * 1000.0) / systemVoltage
        }

        val loadCurrent = if (isThreePhase) {
            (activePowerKw * 1000.0) / (sqrt(3.0) * systemVoltage * clampedPf1)
        } else {
            (activePowerKw * 1000.0) / (systemVoltage * clampedPf1)
        }

        // Capacitor voltage rating:
        // For 400V system -> 440V / 480V heavy-duty
        // For 415V system -> 440V / 480V / 525V heavy-duty
        val recommendedCapVolt = when {
            systemVoltage <= 240 -> "280 V / 300 V Heavy-Duty"
            systemVoltage <= 400 -> "440 V / 480 V Heavy-Duty"
            systemVoltage <= 415 -> "480 V / 525 V Heavy-Duty"
            else -> "525 V / 690 V Heavy-Duty"
        }

        return PfiBankSizingResult(
            activePowerKw = activePowerKw,
            existingPf = clampedPf1,
            targetPf = clampedPf2,
            phi1Deg = phi1Deg,
            phi2Deg = phi2Deg,
            tanPhi1 = tanPhi1,
            tanPhi2 = tanPhi2,
            requiredKvar = netKvarTheoretical,
            recommendedBankKvar = currentBankKvar,
            steps = steps,
            totalStepsCount = steps.size,
            totalCapacitorUnits = totalUnits,
            capacitorUnitBreakdown = unitsSummary,
            recommendedControllerSteps = controllerSteps,
            systemVoltage = systemVoltage,
            systemFrequency = frequency,
            phase = phase,
            recommendedCapacitorVoltageRating = recommendedCapVolt,
            fullLoadCurrentA = loadCurrent,
            capacitorBankRatedCurrentA = ratedBankCurrent,
            contactorSpec = "AC-6b Duty Capacitor Switching Contactor with pre-insertion damping resistors",
            dischargeResistorSpec = "Internal / External discharge resistors to discharge < 50V within 60s (IEC 60831)",
            harmonicWarning = "If significant harmonic distortion or nonlinear loads are present, capacitor bank design may require harmonic analysis and detuned reactors (e.g. 7% / 189Hz or 14% / 134Hz tuning).",
            standardBasis = "IEC 60831-1/2, IEEE Std 18, NEC Article 460 & BNBC Part 8"
        )
    }
}
