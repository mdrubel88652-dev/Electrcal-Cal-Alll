package com.example

import com.example.data.calculator.CalculatorRegistry
import com.example.data.model.CalculatorLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorRegistryTest {

    @Test
    fun testCalculatorCounts() {
        val all = CalculatorRegistry.allCalculators
        val advance = CalculatorRegistry.advanceCalculators
        val basic = CalculatorRegistry.basicCalculators
        val lower = CalculatorRegistry.lowerCalculators

        assertEquals("Total calculators must be exactly 167", 167, all.size)
        assertEquals("Advance calculators must be exactly 55", 55, advance.size)
        assertEquals("Basic calculators must be exactly 61", 61, basic.size)
        assertEquals("Lower calculators must be exactly 51", 51, lower.size)
        assertEquals("Sum must match total", 167, advance.size + basic.size + lower.size)
    }

    @Test
    fun testUniqueIds() {
        val all = CalculatorRegistry.allCalculators
        val ids = all.map { it.id }
        val uniqueIds = ids.toSet()

        assertEquals("All 167 calculators must have unique IDs", 167, uniqueIds.size)
        for (i in 1..167) {
            assertTrue("Calculator ID $i must exist", uniqueIds.contains(i))
        }
    }

    @Test
    fun testBuildingElectricalMaterialCalculator() {
        val calc167 = CalculatorRegistry.getById(167)
        assertNotNull("Calculator 167 must exist", calc167)
        assertEquals("Building Electrical Material Calculation", calc167?.name)
        assertEquals(CalculatorLevel.ADVANCE, calc167?.level)
    }
}
