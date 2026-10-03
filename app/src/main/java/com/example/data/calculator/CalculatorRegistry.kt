package com.example.data.calculator

import com.example.data.model.CalculatorCategory
import com.example.data.model.CalculatorDefinition
import com.example.data.model.CalculatorLevel

object CalculatorRegistry {

    val allCalculators: List<CalculatorDefinition> by lazy {
        val fullList = mutableListOf<CalculatorDefinition>()
        fullList.addAll(CalculatorsLower.list)
        fullList.addAll(CalculatorsBasicPart1.list)
        fullList.addAll(CalculatorsBasicPart2.list)
        fullList.addAll(CalculatorsAdvancePart1.list)
        fullList.addAll(CalculatorsAdvancePart2.list)

        // Sort by ID to ensure consistent ordering
        fullList.sortedBy { it.id }
    }

    val lowerCalculators: List<CalculatorDefinition> by lazy {
        allCalculators.filter { it.level == CalculatorLevel.LOWER }
    }

    val basicCalculators: List<CalculatorDefinition> by lazy {
        allCalculators.filter { it.level == CalculatorLevel.BASIC }
    }

    val advanceCalculators: List<CalculatorDefinition> by lazy {
        allCalculators.filter { it.level == CalculatorLevel.ADVANCE }
    }

    fun getById(id: Int): CalculatorDefinition? {
        return allCalculators.firstOrNull { it.id == id }
    }

    fun getByLevel(level: CalculatorLevel): List<CalculatorDefinition> {
        return when (level) {
            CalculatorLevel.LOWER -> lowerCalculators
            CalculatorLevel.BASIC -> basicCalculators
            CalculatorLevel.ADVANCE -> advanceCalculators
        }
    }

    fun getByCategory(category: CalculatorCategory): List<CalculatorDefinition> {
        return allCalculators.filter { it.category == category }
    }

    fun search(query: String): List<CalculatorDefinition> {
        if (query.isBlank()) return allCalculators
        val q = query.trim().lowercase()
        return allCalculators.filter { calc ->
            calc.name.lowercase().contains(q) ||
                calc.category.displayName.lowercase().contains(q) ||
                calc.description.lowercase().contains(q) ||
                calc.id.toString() == q ||
                calc.keywords.any { it.lowercase().contains(q) }
        }
    }

    fun verifyIntegrity(): String {
        val total = allCalculators.size
        val lower = lowerCalculators.size
        val basic = basicCalculators.size
        val advance = advanceCalculators.size
        val uniqueIds = allCalculators.map { it.id }.toSet().size
        return "Total: $total (Lower: $lower, Basic: $basic, Advance: $advance), Unique IDs: $uniqueIds"
    }
}
