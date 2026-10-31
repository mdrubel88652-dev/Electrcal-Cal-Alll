package com.example

import com.example.data.calculator.BuildingMaterialEngine
import com.example.data.model.BuildingProjectData
import com.example.data.model.BuildingRoomData
import com.example.data.model.CircuitType
import com.example.data.model.RoomCircuitItem
import com.example.data.model.SupplyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.ceil

class BuildingMaterialCableCalculationTest {

    @Test
    fun testCoilCeilingFormula() {
        val coilLength = 100
        val requiredLength = 105.0
        val expectedCoils = ceil(requiredLength / coilLength).toInt()
        assertEquals(2, expectedCoils)

        val exactMultiple = 200.0
        assertEquals(2, ceil(exactMultiple / coilLength).toInt())

        val smallLength = 12.5
        assertEquals(1, ceil(smallLength / coilLength).toInt())
    }

    @Test
    fun testBuildingCableCalculationEngine() {
        val rooms = listOf(
            BuildingRoomData(
                id = "r1",
                name = "Living Room",
                floorNumber = 1,
                lights = 4,
                fans = 2,
                sockets13A = 3,
                acCount = 1,
                circuits = listOf(
                    RoomCircuitItem(name = "Lighting Circuit", circuitType = CircuitType.LIGHTING, loadPower = 160.0, routeLengthMeters = 15.0),
                    RoomCircuitItem(name = "13A Sockets", circuitType = CircuitType.POWER_SOCKET, loadPower = 1500.0, routeLengthMeters = 12.0),
                    RoomCircuitItem(name = "Living AC", circuitType = CircuitType.AC_DEDICATED, loadPower = 2200.0, routeLengthMeters = 18.0)
                )
            ),
            BuildingRoomData(
                id = "r2",
                name = "Kitchen",
                floorNumber = 1,
                lights = 2,
                fans = 1,
                sockets13A = 2,
                geyserCount = 1,
                circuits = listOf(
                    RoomCircuitItem(name = "Kitchen Heavy", circuitType = CircuitType.KITCHEN_HEAVY, loadPower = 2500.0, routeLengthMeters = 10.0),
                    RoomCircuitItem(name = "Kitchen Lights", circuitType = CircuitType.LIGHTING, loadPower = 80.0, routeLengthMeters = 8.0)
                )
            )
        )

        val project = BuildingProjectData(
            projectName = "Test Villa",
            ownerName = "Eng. Rahman",
            address = "Dhaka, Bangladesh",
            preparedBy = "Lead Electrician",
            date = "2026-10-30",
            floors = 1,
            rooms = 2,
            points = emptyMap(),
            roomList = rooms,
            coilLengthMeters = 100,
            supplyType = SupplyType.SINGLE_PHASE,
            mainDbLocation = "Ground Floor DB"
        )

        val (cableResult, materials) = BuildingMaterialEngine.calculateBuildingCablesAndMaterials(project)

        assertNotNull(cableResult)
        assertNotNull(materials)
        assertTrue(cableResult.totalConnectedLoadKw > 0)
        assertTrue(cableResult.cableSummaries.isNotEmpty())
        assertTrue(cableResult.conduitTotalMeters > 0)

        // Verify each coil summary item calculates coils with separate color coil ceil formula
        for (item in cableResult.cableSummaries) {
            val expectedPhaseCoils = if (item.phaseConductorMeters > 0) ceil(item.phaseConductorMeters / project.coilLengthMeters).toInt() else 0
            val expectedNeutralCoils = if (item.neutralConductorMeters > 0) ceil(item.neutralConductorMeters / project.coilLengthMeters).toInt() else 0
            val expectedEarthCoils = if (item.earthConductorMeters > 0) ceil(item.earthConductorMeters / project.coilLengthMeters).toInt() else 0

            assertEquals(expectedPhaseCoils, item.phaseCoilsRequired)
            assertEquals(expectedNeutralCoils, item.neutralCoilsRequired)
            assertEquals(expectedEarthCoils, item.earthCoilsRequired)
            assertEquals(expectedPhaseCoils + expectedNeutralCoils + expectedEarthCoils, item.totalCoilsRequired)
            assertTrue("Phase wire length must be non-negative", item.phaseConductorMeters >= 0)
            assertTrue("Neutral wire length must be non-negative", item.neutralConductorMeters >= 0)
            assertTrue("Earth wire length must be non-negative", item.earthConductorMeters >= 0)
        }

        // Verify floor summary exists
        assertEquals(1, cableResult.floorSummaries.size)
        assertEquals(1, cableResult.floorSummaries[0].floorNumber)
        assertEquals(2, cableResult.floorSummaries[0].roomCount)
    }

    @Test
    fun testFeederSizingAndStandardsReference() {
        val project = BuildingProjectData(
            projectName = "Commercial Building",
            ownerName = "Enterprise Ltd",
            address = "Banani, Dhaka",
            preparedBy = "Senior Design Engineer",
            date = "2026-10-30",
            floors = 3,
            rooms = 6,
            points = emptyMap(),
            supplyType = SupplyType.THREE_PHASE,
            ambientTemperatureC = 40
        )

        val (cableResult, _) = BuildingMaterialEngine.calculateBuildingCablesAndMaterials(project)
        assertNotNull(cableResult)
        assertTrue(cableResult.standardReferences.any { it.contains("BNBC") || it.contains("IEC 60364") })
        assertTrue("Main service feeder must be at least 10 mm2 for 3-phase building", cableResult.mainServiceFeederSizeMm2 >= 10.0)
        assertTrue("Main breaker must be sized appropriately", cableResult.mainMcbRatingA >= 32)
    }

    @Test
    fun testBackwardCompatibleMaterialCalculation() {
        val points = mapOf(
            "led_bulb" to 10,
            "fan" to 4,
            "ac_point" to 2,
            "refrigerator_point" to 1
        )
        val project = BuildingProjectData(
            projectName = "Legacy Estimate",
            ownerName = "Customer",
            address = "Mirpur",
            preparedBy = "Contractor",
            date = "2026-10-30",
            floors = 1,
            rooms = 3,
            points = points
        )

        val materials = BuildingMaterialEngine.calculateMaterials(project)
        assertTrue("Materials BOQ list must not be empty", materials.isNotEmpty())
        assertTrue("Should contain switch board items", materials.any { it.name.contains("Switch Board") || it.name.contains("Board") })
        assertTrue("Should contain conduit pipe", materials.any { it.name.contains("PVC Conduit") || it.name.contains("Conduit") })
        assertTrue("Should contain circuit breaker", materials.any { it.name.contains("MCB") || it.name.contains("Breaker") })
    }
}
