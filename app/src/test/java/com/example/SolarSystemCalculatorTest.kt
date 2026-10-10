package com.example

import com.example.data.calculator.CalculatorRegistry
import com.example.data.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class SolarSystemCalculatorTest {

    @Test
    fun testCalculator4_ExistsInRegistry() {
        val calc4 = CalculatorRegistry.getById(4)
        assertNotNull("Calculator 4 must exist in registry", calc4)
        assertEquals("Solar System Size Calculation All In", calc4?.name)
        assertTrue(calc4?.description?.contains("Solar", ignoreCase = true) == true)
    }

    @Test
    fun testSolarPvCalculation_ExampleFromPrompt() {
        // Prompt Example:
        // Daily Energy = 10 kWh/day
        // Performance Factor = 80% (0.80)
        // Peak Sun Hours = 4 h/day
        // Required PV Energy = 10 / 0.80 = 12.5 kWh/day
        // Required PV Power = 12.5 / 4 = 3.125 kW
        // Panel: 550 W
        // Panel count: 3125 / 550 = 5.68 -> Round UP to 6 Panels
        // Actual PV Array = 6 × 550 W = 3300 W = 3.30 kW
        val inputs = SolarDesignInputs(
            dailyEnergyKwh = 10.0,
            peakSunHours = 4.0,
            backupHours = 6.0,
            perfFactorPercent = 80.0,
            panelWattage = 550.0
        )
        val res = SolarSystemDesignEngine.calculate(inputs)

        assertEquals(12.50, res.requiredDailySolarEnergyKwh, 0.01)
        assertEquals(3.125, res.requiredPvPowerKw, 0.01)
        assertEquals(6, res.panelsCount)
        assertEquals(3300.0, res.actualPvArrayPowerW, 0.01)
        assertEquals(3.30, res.actualPvArrayPowerKw, 0.01)
    }

    @Test
    fun testSolarPanelRoundUp_NeverRoundsDown() {
        // Required PV Power = 3.01 kW = 3010 W
        // Panel: 600 W
        // 3010 / 600 = 5.016 -> MUST round UP to 6 Panels (never 5)
        val inputs = SolarDesignInputs(
            dailyEnergyKwh = 10.0,
            peakSunHours = 4.1528, // yields ~3010 W
            perfFactorPercent = 80.0,
            panelWattage = 600.0
        )
        val res = SolarSystemDesignEngine.calculate(inputs)
        assertTrue("Panels count must be rounded UP", res.panelsCount >= 6)
        assertTrue("Actual PV array must be >= required PV power", res.actualPvArrayPowerKw >= res.requiredPvPowerKw)
    }

    @Test
    fun testBatteryBankSizing_SeriesParallelConfiguration() {
        // System Voltage: 48V, Unit Battery: 12V 200Ah
        // Series = 48 / 12 = 4 Batteries in Series
        val inputs = SolarDesignInputs(
            dailyEnergyKwh = 24.0, // average 1 kW load
            peakSunHours = 4.5,
            backupHours = 6.0,
            backupLoadKw = 1.0,
            systemVoltage = 48.0,
            batteryUnitVoltage = 12.0,
            batteryUnitAh = 200.0,
            batteryType = SolarBatteryType.LIFEPO4,
            dodPercent = 90.0,
            batteryMarginPercent = 10.0,
            inverterEffPercent = 90.0
        )
        val res = SolarSystemDesignEngine.calculate(inputs)

        assertEquals(4, res.batteriesInSeries)
        assertEquals(48.0, res.actualBankVoltage, 0.01)
        assertTrue("Parallel strings must be >= 1", res.parallelStrings >= 1)
        assertEquals(res.batteriesInSeries * res.parallelStrings, res.totalBatteries)
        assertTrue("Actual Bank Ah must be multiple of unit battery Ah", res.actualBankAh % 200.0 == 0.0)
        assertTrue("Usable battery energy must be positive", res.usableBatteryEnergyKwh > 0)
        assertTrue("Estimated backup hours must satisfy or approximate 6h", res.estimatedBackupHours >= 5.9)
    }

    @Test
    fun testInverterSizing_StandardRatings() {
        val inputs = SolarDesignInputs(
            peakLoadKw = 4.2,
            powerFactor = 0.90,
            inverterEffPercent = 90.0,
            inverterMarginPercent = 20.0
        )
        val res = SolarSystemDesignEngine.calculate(inputs)

        // Calculated = (4.2 / (0.9 * 0.9)) * 1.20 = 5.185 * 1.2 = 6.22 kVA
        // Recommended standard inverter = 7.5 kVA
        assertTrue("Calculated kVA must be around 6.2 kVA", abs(res.calculatedInverterKva - 6.22) < 0.5)
        assertTrue("Recommended inverter must be >= calculated", res.recommendedInverterKva >= res.calculatedInverterKva)
        assertTrue("Recommended continuous kW must equal kVA * PF", abs(res.recommendedInverterContinuousKw - (res.recommendedInverterKva * 0.90)) < 0.01)
    }

    @Test
    fun testChargeController_CurrentAndMultipleUnits() {
        // Standard system
        val inputs = SolarDesignInputs(
            dailyEnergyKwh = 10.0,
            peakSunHours = 4.0,
            systemVoltage = 48.0,
            panelWattage = 550.0 // 6 panels = 3300 W -> 3300 / 48 * 1.25 = 85.9 A
        )
        val res = SolarSystemDesignEngine.calculate(inputs)

        assertTrue("Calculated controller current must be around 85.9 A", abs(res.calculatedControllerCurrentAmps - 85.9) < 2.0)
        assertTrue("Recommended controller current must be >= calculated", res.recommendedControllerAmps >= res.calculatedControllerCurrentAmps)
        assertTrue("Controller quantity must be >= 1", res.controllerQuantity >= 1)
    }

    @Test
    fun testPvArrayStringValidation_WhenSpecsProvided() {
        // Detailed panel electrical specs provided:
        // Voc = 49.8V, Vmp = 41.5V, Isc = 14.0A, Imp = 13.25A
        // MPPT Max V = 500V, MPPT Min V = 120V, Max I = 30A
        val inputs = SolarDesignInputs(
            dailyEnergyKwh = 10.0,
            peakSunHours = 4.0,
            panelWattage = 550.0,
            panelVoc = 49.8,
            panelVmp = 41.5,
            panelIsc = 14.0,
            panelImp = 13.25,
            mpptMaxVoltage = 500.0,
            mpptMinVoltage = 120.0,
            mpptMaxCurrent = 30.0
        )
        val res = SolarSystemDesignEngine.calculate(inputs)

        assertNotNull("PV String Config must be calculated when specs provided", res.pvStringConfig)
        val cfg = res.pvStringConfig!!
        assertTrue("Panels in series must be > 0", cfg.panelsInSeries > 0)
        assertTrue("String Voc must be within MPPT limits", cfg.stringVoc <= 500.0)
        assertTrue("String Vmp must be >= MPPT Min V", cfg.stringVmp >= 120.0)
        assertTrue("String Voc must be positive", cfg.stringVoc > 0)
        assertTrue("Configuration notes must be non-empty", cfg.notes.isNotEmpty())
    }

    @Test
    fun testPvArrayStringValidation_WhenSpecsMissing() {
        // Specs missing -> do NOT invent values
        val inputs = SolarDesignInputs(
            panelVoc = null,
            panelVmp = null
        )
        val res = SolarSystemDesignEngine.calculate(inputs)

        assertNull("PV String Config must be null when specs missing", res.pvStringConfig)
        assertNotNull("Missing message must be present", res.pvConfigMissingMessage)
        assertTrue(res.pvConfigMissingMessage!!.contains("requires panel electrical specifications"))
    }
}
