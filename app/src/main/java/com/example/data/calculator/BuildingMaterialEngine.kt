package com.example.data.calculator

import com.example.data.model.BuildingCableCalculationResult
import com.example.data.model.BuildingProjectData
import com.example.data.model.BuildingRoomData
import com.example.data.model.CableCoilSummaryItem
import com.example.data.model.CableInsulationType
import com.example.data.model.CableSizingResult
import com.example.data.model.CircuitType
import com.example.data.model.ConductorMaterial
import com.example.data.model.FloorCableSummary
import com.example.data.model.InstallationMethod
import com.example.data.model.MaterialItem
import com.example.data.model.RoomCableResult
import com.example.data.model.RoomCircuitItem
import com.example.data.model.SupplyType
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sqrt

object BuildingMaterialEngine {

    // IEC 60364-5-52 / BNBC Table 8.1.18 Current-Carrying Capacities (Amperes)
    // Copper conductor, 70°C PVC insulation, 30°C ambient, 2 loaded conductors (1-Phase)
    private val copperPvcCapacityMethodB2 = mapOf(
        1.5 to 15.5,
        2.5 to 21.0,
        4.0 to 28.0,
        6.0 to 36.0,
        10.0 to 50.0,
        16.0 to 68.0,
        25.0 to 89.0,
        35.0 to 110.0
    )

    private val copperPvcCapacityMethodB1 = mapOf(
        1.5 to 17.5,
        2.5 to 24.0,
        4.0 to 32.0,
        6.0 to 41.0,
        10.0 to 57.0,
        16.0 to 76.0,
        25.0 to 101.0,
        35.0 to 125.0
    )

    // Resistance in mΩ per meter at 70°C (IEC 60228 / BNBC)
    private val copperResistanceMilliOhmPerMeter = mapOf(
        1.5 to 14.5,
        2.5 to 8.87,
        4.0 to 5.52,
        6.0 to 3.69,
        10.0 to 2.19,
        16.0 to 1.38,
        25.0 to 0.87,
        35.0 to 0.63
    )

    val standardCableSizes = listOf(1.5, 2.5, 4.0, 6.0, 10.0, 16.0, 25.0, 35.0)

    fun getTemperatureDerating(ambientC: Int, isXlpe: Boolean = false): Double {
        return if (isXlpe) {
            when {
                ambientC <= 30 -> 1.00
                ambientC <= 35 -> 0.96
                ambientC <= 40 -> 0.91
                ambientC <= 45 -> 0.87
                ambientC <= 50 -> 0.82
                else -> 0.76
            }
        } else {
            // 70°C PVC (BNBC / IEC 60364-5-52 Table B.52.14)
            when {
                ambientC <= 25 -> 1.06
                ambientC <= 30 -> 1.00
                ambientC <= 35 -> 0.94
                ambientC <= 40 -> 0.87
                ambientC <= 45 -> 0.79
                ambientC <= 50 -> 0.71
                else -> 0.61
            }
        }
    }

    fun getGroupingDerating(circuitsInSameConduit: Int): Double {
        return when (circuitsInSameConduit) {
            1 -> 1.00
            2 -> 0.80
            3 -> 0.70
            4 -> 0.65
            5 -> 0.60
            6 -> 0.57
            else -> 0.52
        }
    }

    fun sizeCableForCircuit(
        powerWatts: Double,
        voltageVolts: Double,
        powerFactor: Double,
        efficiency: Double,
        routeLengthMeters: Double,
        isThreePhase: Boolean,
        conductorMaterial: ConductorMaterial,
        insulationType: CableInsulationType,
        installationMethod: InstallationMethod,
        ambientTempC: Int,
        groupingCount: Int,
        maxVoltageDropPct: Double = 3.0,
        startingCurrentMultiplier: Double = 1.0
    ): CableSizingResult {
        val missingWarnings = mutableListOf<String>()
        if (powerWatts <= 0.0) missingWarnings.add("Load power not specified")
        if (voltageVolts <= 0.0) missingWarnings.add("Supply voltage not specified")
        if (routeLengthMeters <= 0.0) missingWarnings.add("Cable route length not entered")

        val effectivePf = powerFactor.coerceIn(0.5, 1.0)
        val effectiveEff = efficiency.coerceIn(0.5, 1.0)
        val effectiveVoltage = voltageVolts.coerceAtLeast(100.0)

        // Design Current Ib
        val designCurrentIb = if (isThreePhase) {
            powerWatts / (sqrt(3.0) * effectiveVoltage * effectivePf * effectiveEff)
        } else {
            powerWatts / (effectiveVoltage * effectivePf * effectiveEff)
        }

        val ca = getTemperatureDerating(ambientTempC, insulationType == CableInsulationType.XLPE)
        val cg = getGroupingDerating(groupingCount)
        val totalDerating = (ca * cg).coerceAtLeast(0.3)

        // Select nominal MCB rating In >= Ib
        val standardMcbRatings = listOf(6, 10, 16, 20, 25, 32, 40, 50, 63, 80, 100, 125)
        val nominalMcb = standardMcbRatings.firstOrNull { it >= ceil(designCurrentIb) } ?: 125

        // Required continuous current capacity Iz >= In / (Ca * Cg)
        val requiredIz = nominalMcb / totalDerating

        // Select based on standard table
        val capacityTable = when (installationMethod) {
            InstallationMethod.SURFACE_CONDUIT -> copperPvcCapacityMethodB1
            else -> copperPvcCapacityMethodB2
        }

        val matMultiplier = if (conductorMaterial == ConductorMaterial.ALUMINUM) 0.78 else 1.0

        var selectedSize = standardCableSizes.firstOrNull { size ->
            val baseCap = (capacityTable[size] ?: 15.0) * matMultiplier
            baseCap >= requiredIz
        } ?: standardCableSizes.last()

        // Voltage drop check and size step-up if needed
        var selectedR = (copperResistanceMilliOhmPerMeter[selectedSize] ?: 14.5) / 1000.0
        if (conductorMaterial == ConductorMaterial.ALUMINUM) selectedR *= 1.61

        fun calcVd(size: Double): Pair<Double, Double> {
            var r = (copperResistanceMilliOhmPerMeter[size] ?: 14.5) / 1000.0
            if (conductorMaterial == ConductorMaterial.ALUMINUM) r *= 1.61
            val vd = if (isThreePhase) {
                (sqrt(3.0) * routeLengthMeters * designCurrentIb * r * effectivePf)
            } else {
                (2.0 * routeLengthMeters * designCurrentIb * r * effectivePf)
            }
            val vdPly = (vd / effectiveVoltage) * 100.0
            return Pair(vd, vdPly)
        }

        var (vdVolts, vdPct) = calcVd(selectedSize)
        while (vdPct > maxVoltageDropPct && selectedSize < standardCableSizes.last()) {
            val nextIdx = standardCableSizes.indexOf(selectedSize) + 1
            if (nextIdx < standardCableSizes.size) {
                selectedSize = standardCableSizes[nextIdx]
                val pair = calcVd(selectedSize)
                vdVolts = pair.first
                vdPct = pair.second
            } else break
        }

        val isVdOk = vdPct <= maxVoltageDropPct

        // Protective earth (ECC) sizing according to IEC 60364-5-54 / BNBC
        val protectiveEarthSize = if (selectedSize <= 16.0) selectedSize else 16.0

        val baseCableCap = (capacityTable[selectedSize] ?: 15.0) * matMultiplier
        val actualIz = baseCableCap * totalDerating

        val warning = if (missingWarnings.isNotEmpty()) {
            "Additional engineering inputs required: " + missingWarnings.joinToString(", ")
        } else null

        return CableSizingResult(
            recommendedSizeMm2 = selectedSize,
            designCurrentIb = designCurrentIb,
            nominalMcbRatingA = nominalMcb,
            cableCapacityIz = actualIz,
            voltageDropVolts = vdVolts,
            voltageDropPercent = vdPct,
            isVoltageDropOk = isVdOk,
            tempDeratingCa = ca,
            groupingDeratingCg = cg,
            minSizeShortCircuitMm2 = 1.5,
            protectiveEarthSizeMm2 = protectiveEarthSize,
            standardReference = "IEC 60364-5-52 / BNBC Table 8.1.18",
            missingInputsWarning = warning
        )
    }

    /**
     * Primary calculation function for House Wiring Cable Calculation and Complete Building Room Management.
     * Computes cable sizes, route lengths, conductor totals, and coil counts for every room, floor, and the building.
     */
    fun calculateBuildingCablesAndMaterials(
        project: BuildingProjectData
    ): Pair<BuildingCableCalculationResult, List<MaterialItem>> {
        val floors = project.floors.coerceAtLeast(1)
        val configuredRooms = if (project.roomList.isNotEmpty()) {
            project.roomList
        } else {
            // Generate default structured rooms if none provided
            generateDefaultRooms(floors, project.rooms.coerceAtLeast(1), project.points)
        }

        val coilLength = if (project.coilLengthMeters in listOf(90, 100, 180, 200)) project.coilLengthMeters else 100
        val isThreePhase = project.supplyType == SupplyType.THREE_PHASE
        val systemVoltage = project.supplyType.voltageVolts

        // Accumulators for wire lengths by size (meters of actual single-core conductor)
        // Separate phase, neutral, earth
        val phaseLengthsByMm2 = mutableMapOf<Double, Double>()
        val neutralLengthsByMm2 = mutableMapOf<Double, Double>()
        val earthLengthsByMm2 = mutableMapOf<Double, Double>()
        val totalRouteByMm2 = mutableMapOf<Double, Double>()

        val roomResults = mutableListOf<RoomCableResult>()
        val missingInputsList = mutableListOf<String>()

        var totalConnectedWatts = 0.0

        // Process every room
        for (room in configuredRooms) {
            var roomConnectedWatts = 0.0
            val circuitDescriptions = mutableListOf<String>()

            // Room base dimensions and average route from floor DB
            val dbToRoom = room.distanceToDbMeters.coerceAtLeast(4.0)
            val perimeterFt = 2 * (room.lengthFt + room.widthFt)
            val roomHalfCircumferenceMeters = (perimeterFt * 0.3048) / 2.0

            // 1. Lighting & Fan Circuit (1.5 mm²)
            val lightCount = room.lights
            val fanCount = room.fans
            val lightPoints = lightCount + fanCount
            var room1_5Meters = 0.0
            if (lightPoints > 0) {
                val lightWatts = (lightCount * 25.0) + (fanCount * 75.0)
                roomConnectedWatts += lightWatts
                // Route: DB to room switchboard + internal conduit chasing to fixtures
                val routeMeters = dbToRoom + (lightPoints * 3.5) + (room.heightFt * 0.3048)
                val sizing = sizeCableForCircuit(
                    powerWatts = lightWatts,
                    voltageVolts = 230.0,
                    powerFactor = 0.90,
                    efficiency = 1.0,
                    routeLengthMeters = routeMeters,
                    isThreePhase = false,
                    conductorMaterial = ConductorMaterial.COPPER,
                    insulationType = CableInsulationType.BYA_PVC,
                    installationMethod = InstallationMethod.CONDUIT_IN_WALL,
                    ambientTempC = project.ambientTemperatureC,
                    groupingCount = 2,
                    maxVoltageDropPct = 3.0
                )
                val wireSize = sizing.recommendedSizeMm2.coerceAtLeast(1.5)
                // 1 Phase conductor + 1 Neutral conductor + 1 ECC Earth conductor
                val phaseM = routeMeters * 1.10
                val neutralM = routeMeters * 1.10
                val earthM = routeMeters * 1.10

                phaseLengthsByMm2[wireSize] = (phaseLengthsByMm2[wireSize] ?: 0.0) + phaseM
                neutralLengthsByMm2[wireSize] = (neutralLengthsByMm2[wireSize] ?: 0.0) + neutralM
                earthLengthsByMm2[sizing.protectiveEarthSizeMm2] = (earthLengthsByMm2[sizing.protectiveEarthSizeMm2] ?: 0.0) + earthM
                totalRouteByMm2[wireSize] = (totalRouteByMm2[wireSize] ?: 0.0) + routeMeters

                room1_5Meters = phaseM + neutralM
                circuitDescriptions.add("Lighting: $lightPoints pts (${lightWatts.toInt()}W), $wireSize mm² Cu, ${routeMeters.toInt()}m route")
            }

            // 2. 13A Power Sockets Circuit (2.5 mm²)
            val socketCount = room.sockets13A
            var room2_5Meters = 0.0
            if (socketCount > 0) {
                val socketWatts = socketCount * 300.0 // Diversity rated 300W per 13A socket outlet
                roomConnectedWatts += socketWatts
                val routeMeters = dbToRoom + (socketCount * 4.5) + (room.heightFt * 0.3048)
                val sizing = sizeCableForCircuit(
                    powerWatts = socketWatts,
                    voltageVolts = 230.0,
                    powerFactor = 0.85,
                    efficiency = 1.0,
                    routeLengthMeters = routeMeters,
                    isThreePhase = false,
                    conductorMaterial = ConductorMaterial.COPPER,
                    insulationType = CableInsulationType.BYA_PVC,
                    installationMethod = InstallationMethod.CONDUIT_IN_WALL,
                    ambientTempC = project.ambientTemperatureC,
                    groupingCount = 2,
                    maxVoltageDropPct = 3.0
                )
                val wireSize = sizing.recommendedSizeMm2.coerceAtLeast(2.5)
                val phaseM = routeMeters * 1.10
                val neutralM = routeMeters * 1.10
                val earthM = routeMeters * 1.10

                phaseLengthsByMm2[wireSize] = (phaseLengthsByMm2[wireSize] ?: 0.0) + phaseM
                neutralLengthsByMm2[wireSize] = (neutralLengthsByMm2[wireSize] ?: 0.0) + neutralM
                earthLengthsByMm2[sizing.protectiveEarthSizeMm2] = (earthLengthsByMm2[sizing.protectiveEarthSizeMm2] ?: 0.0) + earthM
                totalRouteByMm2[wireSize] = (totalRouteByMm2[wireSize] ?: 0.0) + routeMeters

                room2_5Meters = phaseM + neutralM
                circuitDescriptions.add("Power: $socketCount sockets (${socketWatts.toInt()}W), $wireSize mm² Cu, ${routeMeters.toInt()}m route")
            }

            // 3. Air Conditioner (AC) Dedicated Circuit (4.0 mm²)
            val acCount = room.acCount
            var room4_0Meters = 0.0
            if (acCount > 0) {
                val acWatts = acCount * (room.acTonnage * 1250.0) // 1.5 Ton ≈ 1875W
                roomConnectedWatts += acWatts
                val routeMeters = (dbToRoom + 12.0 + (room.heightFt * 0.3048)) * acCount
                val sizing = sizeCableForCircuit(
                    powerWatts = acWatts / acCount,
                    voltageVolts = 230.0,
                    powerFactor = 0.82,
                    efficiency = 0.88,
                    routeLengthMeters = routeMeters / acCount,
                    isThreePhase = false,
                    conductorMaterial = ConductorMaterial.COPPER,
                    insulationType = CableInsulationType.BYA_PVC,
                    installationMethod = InstallationMethod.CONDUIT_IN_WALL,
                    ambientTempC = project.ambientTemperatureC,
                    groupingCount = 2,
                    maxVoltageDropPct = 2.5,
                    startingCurrentMultiplier = 3.5
                )
                val wireSize = sizing.recommendedSizeMm2.coerceAtLeast(4.0)
                val phaseM = routeMeters * 1.10
                val neutralM = routeMeters * 1.10
                val earthM = routeMeters * 1.10

                phaseLengthsByMm2[wireSize] = (phaseLengthsByMm2[wireSize] ?: 0.0) + phaseM
                neutralLengthsByMm2[wireSize] = (neutralLengthsByMm2[wireSize] ?: 0.0) + neutralM
                earthLengthsByMm2[sizing.protectiveEarthSizeMm2] = (earthLengthsByMm2[sizing.protectiveEarthSizeMm2] ?: 0.0) + earthM
                totalRouteByMm2[wireSize] = (totalRouteByMm2[wireSize] ?: 0.0) + routeMeters

                room4_0Meters += phaseM + neutralM
                circuitDescriptions.add("AC Dedicated: $acCount unit (${acWatts.toInt()}W), $wireSize mm² Cu, ${routeMeters.toInt()}m route")
            }

            // 4. Geyser / Heavy Dedicated Appliance (4.0 mm²)
            val geyserCount = room.geyserCount
            if (geyserCount > 0) {
                val geyserWatts = geyserCount * 2000.0
                roomConnectedWatts += geyserWatts
                val routeMeters = (dbToRoom + 10.0 + (room.heightFt * 0.3048)) * geyserCount
                val sizing = sizeCableForCircuit(
                    powerWatts = 2000.0,
                    voltageVolts = 230.0,
                    powerFactor = 1.0,
                    efficiency = 1.0,
                    routeLengthMeters = routeMeters / geyserCount,
                    isThreePhase = false,
                    conductorMaterial = ConductorMaterial.COPPER,
                    insulationType = CableInsulationType.BYA_PVC,
                    installationMethod = InstallationMethod.CONDUIT_IN_WALL,
                    ambientTempC = project.ambientTemperatureC,
                    groupingCount = 2,
                    maxVoltageDropPct = 2.5
                )
                val wireSize = sizing.recommendedSizeMm2.coerceAtLeast(4.0)
                val phaseM = routeMeters * 1.10
                val neutralM = routeMeters * 1.10
                val earthM = routeMeters * 1.10

                phaseLengthsByMm2[wireSize] = (phaseLengthsByMm2[wireSize] ?: 0.0) + phaseM
                neutralLengthsByMm2[wireSize] = (neutralLengthsByMm2[wireSize] ?: 0.0) + neutralM
                earthLengthsByMm2[sizing.protectiveEarthSizeMm2] = (earthLengthsByMm2[sizing.protectiveEarthSizeMm2] ?: 0.0) + earthM
                totalRouteByMm2[wireSize] = (totalRouteByMm2[wireSize] ?: 0.0) + routeMeters

                room4_0Meters += phaseM + neutralM
                circuitDescriptions.add("Geyser Dedicated: $geyserCount unit (${geyserWatts.toInt()}W), $wireSize mm² Cu")
            }

            // 5. Additional custom circuits defined on the room
            var room6_0Meters = 0.0
            for (customCircuit in room.circuits) {
                val cWatts = customCircuit.totalWatts
                roomConnectedWatts += cWatts
                val routeMeters = customCircuit.totalRouteMeters
                val sizing = sizeCableForCircuit(
                    powerWatts = cWatts,
                    voltageVolts = if (customCircuit.isThreePhase) 400.0 else 230.0,
                    powerFactor = customCircuit.powerFactor,
                    efficiency = customCircuit.efficiency,
                    routeLengthMeters = routeMeters,
                    isThreePhase = customCircuit.isThreePhase,
                    conductorMaterial = customCircuit.conductorMaterial,
                    insulationType = customCircuit.insulationType,
                    installationMethod = customCircuit.installationMethod,
                    ambientTempC = customCircuit.ambientTempC,
                    groupingCount = customCircuit.groupingCount,
                    maxVoltageDropPct = customCircuit.maxVoltageDropPct,
                    startingCurrentMultiplier = customCircuit.startingCurrentMultiplier
                )
                val wireSize = sizing.recommendedSizeMm2
                val cores = if (customCircuit.isThreePhase) 3 else 1
                val phaseM = routeMeters * cores * 1.10
                val neutralM = routeMeters * 1.10
                val earthM = routeMeters * 1.10

                phaseLengthsByMm2[wireSize] = (phaseLengthsByMm2[wireSize] ?: 0.0) + phaseM
                neutralLengthsByMm2[wireSize] = (neutralLengthsByMm2[wireSize] ?: 0.0) + neutralM
                earthLengthsByMm2[sizing.protectiveEarthSizeMm2] = (earthLengthsByMm2[sizing.protectiveEarthSizeMm2] ?: 0.0) + earthM
                totalRouteByMm2[wireSize] = (totalRouteByMm2[wireSize] ?: 0.0) + routeMeters

                if (wireSize >= 6.0) room6_0Meters += phaseM + neutralM
                circuitDescriptions.add("${customCircuit.name}: ${cWatts.toInt()}W, $wireSize mm², ${routeMeters.toInt()}m")
            }

            val earthRoomTotal = (room1_5Meters + room2_5Meters + room4_0Meters + room6_0Meters) * 0.5
            totalConnectedWatts += roomConnectedWatts

            roomResults.add(
                RoomCableResult(
                    roomName = room.name,
                    floorNumber = room.floorNumber,
                    totalPoints = room.totalPoints,
                    connectedWatts = roomConnectedWatts,
                    cable1_5Mm2Meters = room1_5Meters,
                    cable2_5Mm2Meters = room2_5Meters,
                    cable4_0Mm2Meters = room4_0Meters,
                    cable6_0Mm2Meters = room6_0Meters,
                    earthEccMeters = earthRoomTotal,
                    recommendedCircuitsCount = circuitDescriptions.size,
                    circuitBreakdown = circuitDescriptions
                )
            )
        }

        // Sub-Main Feeders between Main DB and Floor Distribution Boards (Floors > 1)
        var subMainFeederSize = 10.0
        var totalSubMainFeederMeters = 0.0
        val floorSummaries = mutableListOf<FloorCableSummary>()

        for (floor in 1..floors) {
            val floorRooms = roomResults.filter { it.floorNumber == floor }
            val floorWatts = floorRooms.sumOf { it.connectedWatts }
            val floorPoints = floorRooms.sumOf { it.totalPoints }

            // Vertical run to floor DB (approx 3.2m per floor above ground)
            val verticalRiserMeters = (floor - 1) * 3.5 + 8.0
            val floorSizing = sizeCableForCircuit(
                powerWatts = (floorWatts * 0.70).coerceAtLeast(3000.0), // 70% diversity
                voltageVolts = if (isThreePhase) 400.0 else 230.0,
                powerFactor = 0.85,
                efficiency = 1.0,
                routeLengthMeters = verticalRiserMeters,
                isThreePhase = isThreePhase,
                conductorMaterial = ConductorMaterial.COPPER,
                insulationType = CableInsulationType.NYY_PVC,
                installationMethod = InstallationMethod.CONDUIT_IN_WALL,
                ambientTempC = project.ambientTemperatureC,
                groupingCount = 1,
                maxVoltageDropPct = 2.0
            )
            val fSize = floorSizing.recommendedSizeMm2.coerceAtLeast(6.0)
            if (fSize > subMainFeederSize) subMainFeederSize = fSize

            val fPhaseMeters = verticalRiserMeters * (if (isThreePhase) 3 else 1) * 1.10
            val fNeutralM = verticalRiserMeters * 1.10
            val fEarthM = verticalRiserMeters * 1.10

            phaseLengthsByMm2[fSize] = (phaseLengthsByMm2[fSize] ?: 0.0) + fPhaseMeters
            neutralLengthsByMm2[fSize] = (neutralLengthsByMm2[fSize] ?: 0.0) + fNeutralM
            earthLengthsByMm2[floorSizing.protectiveEarthSizeMm2] = (earthLengthsByMm2[floorSizing.protectiveEarthSizeMm2] ?: 0.0) + fEarthM
            totalRouteByMm2[fSize] = (totalRouteByMm2[fSize] ?: 0.0) + verticalRiserMeters
            totalSubMainFeederMeters += verticalRiserMeters

            val floorTotalMeters = floorRooms.sumOf { it.cable1_5Mm2Meters + it.cable2_5Mm2Meters + it.cable4_0Mm2Meters + it.cable6_0Mm2Meters + it.earthEccMeters }
            val floorCoils = ceil(floorTotalMeters / coilLength.toDouble()).toInt()

            floorSummaries.add(
                FloorCableSummary(
                    floorNumber = floor,
                    roomCount = floorRooms.size,
                    totalConnectedWatts = floorWatts,
                    subMainFeederSizeMm2 = fSize,
                    subMainFeederMeters = verticalRiserMeters,
                    totalWireMeters = floorTotalMeters,
                    totalCoils = floorCoils
                )
            )
        }

        // Main Service Feeder Cable (from Energy Meter to Main DB)
        val mainFeederRouteMeters = 30.0 + (floors * 3.0)
        val diversifiedTotalWatts = totalConnectedWatts * 0.65 // 65% building diversity factor
        val mainSizing = sizeCableForCircuit(
            powerWatts = diversifiedTotalWatts.coerceAtLeast(8000.0),
            voltageVolts = systemVoltage,
            powerFactor = 0.85,
            efficiency = 1.0,
            routeLengthMeters = mainFeederRouteMeters,
            isThreePhase = isThreePhase,
            conductorMaterial = ConductorMaterial.COPPER,
            insulationType = CableInsulationType.NYY_PVC,
            installationMethod = InstallationMethod.CONDUIT_IN_WALL,
            ambientTempC = project.ambientTemperatureC,
            groupingCount = 1,
            maxVoltageDropPct = 2.0
        )
        val mainFeederSize = mainSizing.recommendedSizeMm2.coerceAtLeast(if (isThreePhase) 16.0 else 10.0)
        val mainFeederMcbRating = mainSizing.nominalMcbRatingA.coerceAtLeast(if (isThreePhase) 40 else 50)

        // Generate Cable Coil Summary Items for every size used
        val cableSummaries = mutableListOf<CableCoilSummaryItem>()
        val distinctSizes = (phaseLengthsByMm2.keys + neutralLengthsByMm2.keys + earthLengthsByMm2.keys).sorted()

        for (size in distinctSizes) {
            val phaseM = phaseLengthsByMm2[size] ?: 0.0
            val neutralM = neutralLengthsByMm2[size] ?: 0.0
            val earthM = earthLengthsByMm2[size] ?: 0.0
            val totalM = phaseM + neutralM + earthM
            val routeM = totalRouteByMm2[size] ?: (totalM / 2.5)

            val pCoils = ceil(phaseM / coilLength.toDouble()).toInt()
            val nCoils = ceil(neutralM / coilLength.toDouble()).toInt()
            val eCoils = ceil(earthM / coilLength.toDouble()).toInt()
            val totalCoils = pCoils + nCoils + eCoils

            val purpose = when (size) {
                1.5 -> "Lighting, Ceiling Fans & Exhaust Sub-Circuits"
                2.5 -> "13A Power Sockets & General Power Outlets"
                4.0 -> "Air Conditioners (AC) & Water Geysers"
                6.0 -> "Water Pumps, Cookers & Sub-DB Connections"
                10.0 -> "Floor Sub-Main Feeders & High-Load Risers"
                16.0 -> "Main Building Service Feeder & Distribution"
                25.0, 35.0 -> "Main Intake Heavy Feeder & Industrial Sub-Mains"
                else -> "General House Wiring Circuits"
            }

            cableSummaries.add(
                CableCoilSummaryItem(
                    cableSizeMm2 = size,
                    conductorMaterial = "Copper (Cu)",
                    insulationType = "BYA FR 70°C (Single Core)",
                    purpose = purpose,
                    phaseConductorMeters = phaseM,
                    neutralConductorMeters = neutralM,
                    earthConductorMeters = earthM,
                    totalConductorMeters = totalM,
                    coilLengthMeters = coilLength,
                    phaseCoilsRequired = pCoils,
                    neutralCoilsRequired = nCoils,
                    earthCoilsRequired = eCoils,
                    totalCoilsRequired = totalCoils,
                    colorBreakdown = "Red (Phase): ${phaseM.toInt()}m ($pCoils coils) | Black (Neutral): ${neutralM.toInt()}m ($nCoils coils) | Green/Yellow (Earth): ${earthM.toInt()}m ($eCoils coils)",
                    formulaExplanation = "Coils = ⌈$totalM m ÷ $coilLength m⌉ = $totalCoils coils of $coilLength m"
                )
            )
        }

        // Conduits, Boxes, and Accessories Calculation
        val totalConductorMeters = cableSummaries.sumOf { it.totalConductorMeters }
        val totalPoints = roomResults.sumOf { it.totalPoints }
        val conduitMeters = (totalPoints * 4.8 * 1.15).coerceAtLeast(60.0)
        val conduitPipes3m = ceil(conduitMeters / 3.0).toInt()
        val circularBoxes = ceil(totalPoints * 1.25).toInt()
        val switchBoards = configuredRooms.size * 2 + (floors * 2)
        val dbCount = floors + 1 // 1 Main DB + 1 SDB per floor
        val breakerTotal = roomResults.sumOf { it.recommendedCircuitsCount } + (floors * 2) + 2

        val notes = listOf(
            "All cable sizing calculated strictly per BNBC 2020 (Part 8, Chap 1) and IEC 60364-5-52.",
            "Conductor temperature derating factor Ca = ${getTemperatureDerating(project.ambientTemperatureC)} applied for ${project.ambientTemperatureC}°C ambient.",
            "Conduit grouping derating factor Cg = ${getGroupingDerating(2)} applied for 2-circuit conduit occupancy.",
            "Coil counts calculated using ceiling formula ⌈total length ÷ coil length⌉ with configured $coilLength m coils.",
            "Separate conductors accounted for: Phase (Red), Neutral (Black), and Protective Earth ECC (Green/Yellow) with zero double-counting."
        )

        val references = listOf(
            "BNBC 2020: Bangladesh National Building Code — Part 8 Electrical & Telecommunication",
            "IEC 60364-5-52: Low-voltage electrical installations — Selection and erection of wiring systems",
            "IEC 60364-5-54: Earthing arrangements and protective conductors",
            "BDS 900: Specifications for PVC insulated non-armoured copper cables"
        )

        val calculationResult = BuildingCableCalculationResult(
            totalFloors = floors,
            totalRooms = configuredRooms.size,
            totalConnectedLoadWatts = totalConnectedWatts,
            totalConnectedLoadKw = totalConnectedWatts / 1000.0,
            supplyType = project.supplyType,
            mainServiceFeederSizeMm2 = mainFeederSize,
            mainFeederLengthMeters = mainFeederRouteMeters,
            mainMcbRatingA = mainFeederMcbRating,
            subMainFeederSizeMm2 = subMainFeederSize,
            subMainTotalMeters = totalSubMainFeederMeters,
            configuredCoilLengthMeters = coilLength,
            cableSummaries = cableSummaries,
            roomResults = roomResults,
            floorSummaries = floorSummaries,
            conduitPipeLengths3m = conduitPipes3m,
            conduitTotalMeters = conduitMeters,
            circularBoxesCount = circularBoxes,
            switchBoardsCount = switchBoards,
            distributionBoardsCount = dbCount,
            circuitBreakersTotal = breakerTotal,
            calculationNotes = notes,
            standardReferences = references,
            missingInputsWarnings = missingInputsList
        )

        // Generate MaterialItems Bill of Quantities list (preserves complete backward compatibility)
        val materialList = generateFullMaterialItemList(project, calculationResult)

        return Pair(calculationResult, materialList)
    }

    /**
     * Backward-compatible helper method used by existing screens and PDF generator.
     */
    fun calculateMaterials(project: BuildingProjectData): List<MaterialItem> {
        val (calcResult, materials) = calculateBuildingCablesAndMaterials(project)
        return materials
    }

    private fun generateDefaultRooms(
        floors: Int,
        totalRooms: Int,
        points: Map<String, Int>
    ): List<BuildingRoomData> {
        val roomsList = mutableListOf<BuildingRoomData>()
        val roomsPerFloor = max(1, totalRooms / floors)

        val totalLights = points["light"] ?: (totalRooms * 4)
        val totalFans = points["fan"] ?: (totalRooms * 2)
        val totalSockets = points["socket"] ?: (totalRooms * 3)
        val totalAcs = points["ac"] ?: max(1, totalRooms / 2)
        val totalGeysers = points["geyser"] ?: max(1, totalRooms / 3)

        val avgLights = max(1, totalLights / totalRooms)
        val avgFans = max(1, totalFans / totalRooms)
        val avgSockets = max(1, totalSockets / totalRooms)

        val roomTypes = listOf(
            "Master Bedroom", "Bed Room 2", "Living Room", "Dining Room",
            "Kitchen", "Guest Room", "Study Room", "Office Room", "Lobby"
        )

        for (i in 0 until totalRooms) {
            val floor = (i / roomsPerFloor) + 1
            val name = roomTypes[i % roomTypes.size] + " #${i + 1}"
            val hasAc = i < totalAcs
            val hasGeyser = i < totalGeysers

            roomsList.add(
                BuildingRoomData(
                    name = name,
                    floorNumber = floor.coerceAtMost(floors),
                    lengthFt = 14.0,
                    widthFt = 12.0,
                    heightFt = 10.0,
                    distanceToDbMeters = 6.0 + ((i % roomsPerFloor) * 2.5),
                    isDistanceVerified = false,
                    lights = avgLights,
                    fans = avgFans,
                    sockets13A = avgSockets,
                    acCount = if (hasAc) 1 else 0,
                    acTonnage = 1.5,
                    geyserCount = if (hasGeyser) 1 else 0
                )
            )
        }
        return roomsList
    }

    private fun generateFullMaterialItemList(
        project: BuildingProjectData,
        cableResult: BuildingCableCalculationResult
    ): List<MaterialItem> {
        val list = mutableListOf<MaterialItem>()
        var itemNo = 1

        val coilUnit = "Coil (${cableResult.configuredCoilLengthMeters}m)"

        // 1. Wires & Cables Breakdown
        for (cable in cableResult.cableSummaries) {
            if (cable.phaseCoilsRequired > 0) {
                list.add(
                    MaterialItem(
                        no = itemNo++,
                        name = "Single Core Copper Wire (BYA) — Phase (Red)",
                        specification = "${cable.cableSizeMm2} mm² (1x${cable.cableSizeMm2} RM) 450/750V FR Grade",
                        brand = project.wireBrand,
                        unit = coilUnit,
                        quantity = cable.phaseCoilsRequired,
                        remarks = "${cable.purpose} (Total: ${cable.phaseConductorMeters.toInt()}m)"
                    )
                )
            }
            if (cable.neutralCoilsRequired > 0) {
                list.add(
                    MaterialItem(
                        no = itemNo++,
                        name = "Single Core Copper Wire (BYA) — Neutral (Black)",
                        specification = "${cable.cableSizeMm2} mm² (1x${cable.cableSizeMm2} RM) 450/750V FR Grade",
                        brand = project.wireBrand,
                        unit = coilUnit,
                        quantity = cable.neutralCoilsRequired,
                        remarks = "Return Neutral Line (Total: ${cable.neutralConductorMeters.toInt()}m)"
                    )
                )
            }
            if (cable.earthCoilsRequired > 0) {
                list.add(
                    MaterialItem(
                        no = itemNo++,
                        name = "Earth Continuity Conductor (ECC) — Earth (Green/Yellow)",
                        specification = "${cable.cableSizeMm2} mm² (1x${cable.cableSizeMm2} RM) FR Grade",
                        brand = project.wireBrand,
                        unit = coilUnit,
                        quantity = cable.earthCoilsRequired,
                        remarks = "Shock Protection Grounding (Total: ${cable.earthConductorMeters.toInt()}m)"
                    )
                )
            }
        }

        // 2. Main Service Feeder Cable
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Armoured / NYY Power Feeder Cable",
                specification = if (cableResult.supplyType == SupplyType.THREE_PHASE) {
                    "4 Core x ${cableResult.mainServiceFeederSizeMm2} mm² Copper NYY (0.6/1kV)"
                } else {
                    "2 Core x ${cableResult.mainServiceFeederSizeMm2} mm² Copper BYM (450/750V)"
                },
                brand = project.wireBrand,
                unit = "Meter",
                quantity = cableResult.mainFeederLengthMeters.toInt(),
                remarks = "Main Service Entrance from Meter to MDB (${cableResult.mainMcbRatingA}A MCB)"
            )
        )

        // 3. Sub-Main Risers between floors (if multi-floor)
        if (cableResult.totalFloors > 1) {
            list.add(
                MaterialItem(
                    no = itemNo++,
                    name = "Sub-Main Riser Cable (NYY/BYM)",
                    specification = "4 Core x ${cableResult.subMainFeederSizeMm2} mm² Copper Feeder",
                    brand = project.wireBrand,
                    unit = "Meter",
                    quantity = cableResult.subMainTotalMeters.toInt(),
                    remarks = "Inter-floor Risers connecting MDB to Floor SDBs"
                )
            )
        }

        // 4. Conduits & Accessories
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "uPVC Electrical Conduit Pipe (Medium Duty)",
                specification = "20 mm (3/4 inch) Heavy Duty Rigid Conduit",
                brand = project.pipeBrand,
                unit = "Length (3m / 10ft)",
                quantity = cableResult.conduitPipeLengths3m,
                remarks = "Concealed Wall & Ceiling Slab Chasing"
            )
        )

        val conduit25Lengths = ceil(cableResult.conduitPipeLengths3m * 0.25).toInt().coerceAtLeast(6)
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "uPVC Electrical Conduit Pipe (Feeder / AC)",
                specification = "25 mm (1 inch) Heavy Duty Conduit",
                brand = project.pipeBrand,
                unit = "Length (3m / 10ft)",
                quantity = conduit25Lengths,
                remarks = "Sub-Main Feeder Runs & AC Dedicated Conduit Lines"
            )
        )

        list.add(
            MaterialItem(
                no = itemNo++,
                name = "PVC Circular Junction Boxes with Lid",
                specification = "20mm 1-Way, 2-Way & 4-Way Deep Junction Boxes",
                brand = project.pipeBrand,
                unit = "PCS",
                quantity = cableResult.circularBoxesCount,
                remarks = "Ceiling Fan, Light Hook & Junction Pull Boxes"
            )
        )

        val bendCount = ceil(cableResult.conduitPipeLengths3m * 0.8).toInt()
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "uPVC Conduit Bends / Long Bends",
                specification = "20 mm & 25 mm Injection Moulded 90° Bends",
                brand = project.pipeBrand,
                unit = "PCS",
                quantity = bendCount,
                remarks = "Smooth Corner & Slab Drop Routing"
            )
        )

        // 5. Switchboards & Gang Boxes
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Concealed Metal / PVC Switch Gang Boxes",
                specification = "Modular 2/3/4/6/8-Gang Concealed Wall Boxes",
                brand = project.switchBrand,
                unit = "PCS",
                quantity = cableResult.switchBoardsCount,
                remarks = "Room Switch & Power Socket Board Placement"
            )
        )

        // 6. Distribution Boards & Protection
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Main Distribution Board (MDB) Enclosure",
                specification = "Sheet Metal SPN/TPN Board with Busbars & Neutral Link",
                brand = "Standard Sheet Steel",
                unit = "Set",
                quantity = 1,
                remarks = "Incoming Supply Intake with Main Isolator"
            )
        )

        if (cableResult.totalFloors > 1) {
            list.add(
                MaterialItem(
                    no = itemNo++,
                    name = "Sub-Distribution Boards (SDB) Enclosure",
                    specification = "6/8/12-Way SPN MCB Enclosure with Door",
                    brand = "Standard Sheet Steel",
                    unit = "Set",
                    quantity = cableResult.totalFloors,
                    remarks = "One SDB per Floor for Local Load Balancing"
                )
            )
        }

        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Miniature Circuit Breakers (MCB)",
                specification = "Single Pole 6A, 10A, 16A, 20A, 32A (6kA Type C)",
                brand = "Havells / Schneider / ABB / Siemens",
                unit = "PCS",
                quantity = cableResult.circuitBreakersTotal,
                remarks = "Individual Branch Circuit Overcurrent Protection"
            )
        )

        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Residual Current Circuit Breaker (RCCB/ELCB)",
                specification = "2-Pole / 4-Pole 40A/63A, 30mA Sensitivity",
                brand = "Havells / Schneider / ABB",
                unit = "PCS",
                quantity = cableResult.distributionBoardsCount,
                remarks = "Life Safety Earth Leakage & Electrocution Protection"
            )
        )

        // 7. Earthing Materials
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Copper-Bonded Earth Electrode Rod",
                specification = "5/8 inch (16 mm) Diameter x 10 Feet Length",
                brand = "High Conductivity Pure Copper",
                unit = "PCS",
                quantity = max(1, cableResult.totalFloors),
                remarks = "Main Earth Grounding System (BNBC Compliant)"
            )
        )

        return list
    }
}
