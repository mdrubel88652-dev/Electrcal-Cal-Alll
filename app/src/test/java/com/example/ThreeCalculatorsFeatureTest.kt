package com.example

import com.example.data.calculator.CalculatorRegistry
import com.example.data.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class ThreeCalculatorsFeatureTest {

    @Test
    fun testCalculator1_FactoryLoadCatalogAndMath() {
        val calc1 = CalculatorRegistry.getById(1)
        assertNotNull("Calculator 1 must exist", calc1)
        assertEquals("All Factory Load Calculation", calc1?.name)

        // Verify predefined machines catalog
        val catalog = PredefinedFactoryLoadsCatalog.items
        assertTrue("Factory catalog must have at least 35 items", catalog.size >= 35)

        val pump = catalog.firstOrNull { it.name == "Pump Motor" }
        assertNotNull("Pump Motor must exist in catalog", pump)
        assertEquals(7.5, pump!!.defaultPower, 0.01)
        assertEquals("kW", pump.unit)

        val compressor = catalog.firstOrNull { it.name == "Air Compressor" }
        assertNotNull("Air Compressor must exist", compressor)
        assertEquals(15.0, compressor!!.defaultPower, 0.01)

        val sewing = catalog.firstOrNull { it.name == "Sewing Machine" }
        assertNotNull("Sewing Machine must exist", sewing)
        assertEquals(550.0, sewing!!.defaultPower, 0.01)
        assertEquals("W", sewing.unit)

        // Verify W to kW conversion
        val itemW = FactoryLoadItem(name = "Lighting", power = 500.0, unit = "W", quantity = 20)
        assertEquals(0.500, itemW.powerInKw, 0.001)
        assertEquals(10.00, itemW.totalKw, 0.001)

        val itemKw = FactoryLoadItem(name = "Pump Motor", power = 7.5, unit = "kW", quantity = 3)
        assertEquals(7.5, itemKw.powerInKw, 0.001)
        assertEquals(22.5, itemKw.totalKw, 0.001)

        val itemCompressor = FactoryLoadItem(name = "Compressor", power = 15.0, unit = "kW", quantity = 2)
        assertEquals(30.0, itemCompressor.totalKw, 0.001)

        // Total connected load = 22.5 + 10.0 + 30.0 = 62.5 kW
        val totalKw = listOf(itemW, itemKw, itemCompressor).sumOf { it.totalKw }
        assertEquals(62.50, totalKw, 0.01)

        // Calculation with Demand factor 0.8, Diversity factor 1.15, PF 0.85, Eff 0.90
        val maxDemandKw = totalKw * 0.80
        assertEquals(50.0, maxDemandKw, 0.01)
        val divDemandKw = maxDemandKw / 1.15
        val demandKva = divDemandKw / (0.85 * 0.90)
        assertTrue("Demand kVA must be positive and realistic", demandKva > 50.0 && demandKva < 70.0)
    }

    @Test
    fun testCalculator2_HouseWiringCatalogAndMath() {
        val calc3 = CalculatorRegistry.getById(3)
        assertNotNull("Calculator 3 must exist", calc3)
        assertEquals("House Wiring Load Calculation", calc3?.name)

        // Verify household appliances catalog
        val catalog = PredefinedHouseholdLoadsCatalog.items
        assertTrue("Household catalog must have at least 30 items", catalog.size >= 30)

        val led = catalog.firstOrNull { it.name == "LED Light" }
        assertNotNull("LED Light must exist", led)
        assertEquals(15.0, led!!.defaultWattage, 0.01)

        val fan = catalog.firstOrNull { it.name == "Ceiling Fan" }
        assertNotNull("Ceiling Fan must exist", fan)
        assertEquals(80.0, fan!!.defaultWattage, 0.01)

        val tv = catalog.firstOrNull { it.name == "TV" }
        assertNotNull("TV must exist", tv)
        assertEquals(120.0, tv!!.defaultWattage, 0.01)

        val fridge = catalog.firstOrNull { it.name == "Refrigerator" }
        assertNotNull("Refrigerator must exist", fridge)
        assertEquals(200.0, fridge!!.defaultWattage, 0.01)

        // Test sample load list from prompt:
        // 1. LED Light (15 W, Qty 10) = 150 W
        // 2. Ceiling Fan (80 W, Qty 5) = 400 W
        // 3. TV (120 W, Qty 2) = 240 W
        // 4. Refrigerator (200 W, Qty 1) = 200 W
        // Total = 990 W -> 0.99 kW
        val itemLed = HouseholdLoadItem(name = "LED Light", power = 15.0, unit = "W", quantity = 10)
        val itemFan = HouseholdLoadItem(name = "Ceiling Fan", power = 80.0, unit = "W", quantity = 5)
        val itemTv = HouseholdLoadItem(name = "TV", power = 120.0, unit = "W", quantity = 2)
        val itemFridge = HouseholdLoadItem(name = "Refrigerator", power = 200.0, unit = "W", quantity = 1)

        val sampleLoads = listOf(itemLed, itemFan, itemTv, itemFridge)
        val totalWatts = sampleLoads.sumOf { it.totalWatts }
        val totalKw = sampleLoads.sumOf { it.totalKw }

        assertEquals(990.0, totalWatts, 0.01)
        assertEquals(0.99, totalKw, 0.001)

        // Check kW conversion if entered in kW
        val customKwItem = HouseholdLoadItem(name = "Heater", power = 1.5, unit = "kW", quantity = 1)
        assertEquals(1.5, customKwItem.powerInKw, 0.001)
        assertEquals(1500.0, customKwItem.totalWatts, 0.01)
    }

    @Test
    fun testCalculator3_PfiOptimizerAndCapacitorBankSizing() {
        val calc5 = CalculatorRegistry.getById(5)
        assertNotNull("Calculator 5 must exist", calc5)
        assertEquals("PFI Size Calculation / Capacitor Bank", calc5?.name)

        // Sizing test: 200 kW load, PF 0.72 -> 0.98 at 400V 50Hz 3-Phase
        val res = PfiBankOptimizer.calculate(
            loadValue = 200.0,
            isLoadInKva = false,
            existingPf = 0.72,
            targetPf = 0.98,
            systemVoltage = 400.0,
            frequency = 50,
            phase = "3 Phase"
        )

        // Analytical check:
        // phi1 = acos(0.72) = 0.7679 rad (43.95 deg) -> tan(phi1) = 0.9639
        // phi2 = acos(0.98) = 0.2003 rad (11.48 deg) -> tan(phi2) = 0.2031
        // tan(phi1) - tan(phi2) = 0.9639 - 0.2031 = 0.7608
        // Qc = 200 * 0.7608 = 152.17 kVAR
        assertTrue("Required kVAR must be around 152 kVAR", abs(res.requiredKvar - 152.17) < 3.0)
        assertTrue("Recommended bank must be >= required kVAR", res.recommendedBankKvar >= res.requiredKvar)
        assertTrue("Steps must be non-empty", res.steps.isNotEmpty())
        assertTrue("Controller steps must be >= steps count", res.recommendedControllerSteps >= res.totalStepsCount)
        assertTrue("Capacitor units must be positive", res.totalCapacitorUnits > 0)
        assertTrue("Harmonic warning must be present", res.harmonicWarning.contains("harmonic", ignoreCase = true))
        assertTrue("Capacitor voltage rating must be heavy duty", res.recommendedCapacitorVoltageRating.contains("Heavy-Duty"))
    }
}
