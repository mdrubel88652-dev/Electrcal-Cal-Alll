package com.example.data.model

import java.util.UUID

data class MaterialItem(
    val no: Int,
    val name: String,
    val specification: String,
    val brand: String = "",
    val unit: String,
    val quantity: Int,
    val remarks: String = ""
)

data class MaterialEstimateRow(
    val sl: Int = 1,
    var name: String = "",
    var spec: String = "-",
    var qty: Double = 1.0,
    var unit: String = "PCS"
)

data class ElectricalPointCounter(
    val id: String,
    val name: String,
    val defaultCount: Int,
    val category: String
)

enum class ConductorMaterial(val label: String, val resistivity: Double) {
    COPPER("Copper (Cu)", 0.0178),
    ALUMINUM("Aluminum (Al)", 0.0285)
}

enum class CableInsulationType(val label: String, val maxOperatingTempC: Int) {
    BYA_PVC("BYA (Single Core PVC 70°C)", 70),
    BYM_PVC("BYM (Multi-Core Flat PVC 70°C)", 70),
    NYY_PVC("NYY (Heavy Duty PVC/PVC 70°C)", 70),
    XLPE("XLPE Insulated (90°C)", 90)
}

enum class InstallationMethod(val label: String, val code: String) {
    CONDUIT_IN_WALL("Concealed in Wall/Slab Conduit", "B2"),
    SURFACE_CONDUIT("Surface Conduit on Wall/Ceiling", "B1"),
    CABLE_TRAY("Perforated Cable Tray / Trunking", "C"),
    DIRECT_BURIED("Direct in Earth / Under-floor Duct", "D")
}

enum class SupplyType(val label: String, val voltageVolts: Double, val phases: Int) {
    SINGLE_PHASE("Single Phase (230 V, 50 Hz)", 230.0, 1),
    THREE_PHASE("Three Phase (400 V, 50 Hz)", 400.0, 3)
}

enum class CircuitType(
    val labelEn: String,
    val labelBn: String,
    val defaultWatts: Double,
    val standardSizeMm2: Double,
    val defaultRouteMeters: Double
) {
    LIGHTING("Lighting & Ceiling Fans", "লাইট ও ফ্যান সার্কিট", 350.0, 1.5, 16.0),
    POWER_SOCKET("13A Power Sockets", "১৩এ পাওয়ার সকেট সার্কিট", 1500.0, 2.5, 18.0),
    AC_DEDICATED("Air Conditioner (AC)", "এয়ার কন্ডিশনার (AC) লাইন", 2000.0, 4.0, 22.0),
    KITCHEN_HEAVY("Kitchen & Geyser Heavy Load", "কিচেন ও গিজার ডেডিকেটেড লোড", 2500.0, 4.0, 20.0),
    WATER_PUMP("Water Pump Motor", "পানির পাম্প মোটর সার্কিট", 1500.0, 4.0, 28.0),
    SUB_MAIN_FEEDER("Floor Sub-Main Feeder", "ফ্লোর সাব-মেইন ফিডার ক্যাবল", 8000.0, 10.0, 22.0),
    MAIN_SERVICE_FEEDER("Main Service Feeder", "মেইন সার্ভিস ফিডার ক্যাবল", 18000.0, 16.0, 35.0),
    EARTHING_ECC("Protective Earth (ECC)", "আর্থিং কন্ডাক্টর (ECC)", 0.0, 2.5, 18.0),
    CUSTOM("Custom Dedicated Circuit", "কাস্টম ডেডিকেটেড সার্কিট", 1200.0, 2.5, 16.0)
}

data class CableRouteSegment(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val lengthMeters: Double,
    val isVerifiedMeasurement: Boolean = false,
    val description: String = ""
)

data class RoomCircuitItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val circuitType: CircuitType,
    val loadPower: Double,
    val loadUnit: String = "W", // "W" or "kW"
    val quantity: Int = 1,
    val routeLengthMeters: Double,
    val verticalRunMeters: Double = 0.0,
    val isVerifiedSiteMeasurement: Boolean = false,
    val conductorMaterial: ConductorMaterial = ConductorMaterial.COPPER,
    val insulationType: CableInsulationType = CableInsulationType.BYA_PVC,
    val installationMethod: InstallationMethod = InstallationMethod.CONDUIT_IN_WALL,
    val powerFactor: Double = 0.85,
    val efficiency: Double = 0.90,
    val startingCurrentMultiplier: Double = 1.0, // e.g. 2.5x to 5x for compressor/motor
    val ambientTempC: Int = 35,
    val groupingCount: Int = 2,
    val maxVoltageDropPct: Double = 3.0,
    val isThreePhase: Boolean = false
) {
    val totalWatts: Double
        get() {
            val base = if (loadUnit.equals("kW", ignoreCase = true)) loadPower * 1000.0 else loadPower
            return base * quantity.coerceAtLeast(1)
        }

    val totalRouteMeters: Double
        get() = (routeLengthMeters + verticalRunMeters).coerceAtLeast(1.0)
}

data class BuildingRoomData(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val floorNumber: Int = 1,
    val lengthFt: Double = 14.0,
    val widthFt: Double = 12.0,
    val heightFt: Double = 10.0,
    val distanceToDbMeters: Double = 8.0,
    val isDistanceVerified: Boolean = false,
    val lights: Int = 4,
    val fans: Int = 2,
    val sockets13A: Int = 3,
    val acCount: Int = 1,
    val acTonnage: Double = 1.5,
    val geyserCount: Int = 0,
    val otherLoadsWatts: Double = 0.0,
    val circuits: List<RoomCircuitItem> = emptyList()
) {
    val totalPoints: Int
        get() = lights + fans + sockets13A + acCount + geyserCount + (if (otherLoadsWatts > 0) 1 else 0)

    val estimatedWatts: Double
        get() = (lights * 25.0) + (fans * 75.0) + (sockets13A * 250.0) +
                (acCount * (acTonnage * 1200.0)) + (geyserCount * 1500.0) + otherLoadsWatts
}

data class CableSizingResult(
    val recommendedSizeMm2: Double,
    val designCurrentIb: Double,
    val nominalMcbRatingA: Int,
    val cableCapacityIz: Double,
    val voltageDropVolts: Double,
    val voltageDropPercent: Double,
    val isVoltageDropOk: Boolean,
    val tempDeratingCa: Double,
    val groupingDeratingCg: Double,
    val minSizeShortCircuitMm2: Double,
    val protectiveEarthSizeMm2: Double,
    val standardReference: String,
    val missingInputsWarning: String? = null
)

data class CableCoilSummaryItem(
    val cableSizeMm2: Double,
    val conductorMaterial: String,
    val insulationType: String,
    val purpose: String,
    val phaseConductorMeters: Double,
    val neutralConductorMeters: Double,
    val earthConductorMeters: Double,
    val totalConductorMeters: Double,
    val coilLengthMeters: Int,
    val phaseCoilsRequired: Int,
    val neutralCoilsRequired: Int,
    val earthCoilsRequired: Int,
    val totalCoilsRequired: Int,
    val colorBreakdown: String,
    val formulaExplanation: String
)

data class RoomCableResult(
    val roomName: String,
    val floorNumber: Int,
    val totalPoints: Int,
    val connectedWatts: Double,
    val cable1_5Mm2Meters: Double,
    val cable2_5Mm2Meters: Double,
    val cable4_0Mm2Meters: Double,
    val cable6_0Mm2Meters: Double,
    val earthEccMeters: Double,
    val recommendedCircuitsCount: Int,
    val circuitBreakdown: List<String>
)

data class FloorCableSummary(
    val floorNumber: Int,
    val roomCount: Int,
    val totalConnectedWatts: Double,
    val subMainFeederSizeMm2: Double,
    val subMainFeederMeters: Double,
    val totalWireMeters: Double,
    val totalCoils: Int
)

data class BuildingCableCalculationResult(
    val totalFloors: Int,
    val totalRooms: Int,
    val totalConnectedLoadWatts: Double,
    val totalConnectedLoadKw: Double,
    val supplyType: SupplyType,
    val mainServiceFeederSizeMm2: Double,
    val mainFeederLengthMeters: Double,
    val mainMcbRatingA: Int,
    val subMainFeederSizeMm2: Double,
    val subMainTotalMeters: Double,
    val configuredCoilLengthMeters: Int,
    val cableSummaries: List<CableCoilSummaryItem>,
    val roomResults: List<RoomCableResult>,
    val floorSummaries: List<FloorCableSummary>,
    val conduitPipeLengths3m: Int,
    val conduitTotalMeters: Double,
    val circularBoxesCount: Int,
    val switchBoardsCount: Int,
    val distributionBoardsCount: Int,
    val circuitBreakersTotal: Int,
    val calculationNotes: List<String>,
    val standardReferences: List<String>,
    val missingInputsWarnings: List<String>
)

data class BuildingProjectData(
    val id: Long = 0,
    val projectName: String,
    val ownerName: String,
    val address: String,
    val preparedBy: String,
    val date: String,
    val floors: Int,
    val rooms: Int,
    val points: Map<String, Int>,
    val wireBrand: String = "BRB / BBS / Eastern",
    val pipeBrand: String = "Standard PVC",
    val switchBrand: String = "Modular Super Star",
    val materials: List<MaterialItem> = emptyList(),
    // Extended House Wiring Cable & Complete Building Room Management Fields
    val coilLengthMeters: Int = 100,
    val supplyType: SupplyType = SupplyType.SINGLE_PHASE,
    val mainDbLocation: String = "Ground Floor Intake / Meter Room",
    val ambientTemperatureC: Int = 35,
    val calculationMode: String = "BEGINNER", // "BEGINNER" or "ADVANCED"
    val roomList: List<BuildingRoomData> = emptyList(),
    val cableCalculationResult: BuildingCableCalculationResult? = null
)
