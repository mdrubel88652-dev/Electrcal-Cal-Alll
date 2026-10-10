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
        val cleanQuery = query.trim().lowercase()
        val tokens = cleanQuery.split("\\s+".toRegex()).filter { it.isNotBlank() }
        return allCalculators.filter { calc ->
            val idStr = calc.id.toString()
            val nameLower = calc.name.lowercase()
            val catLower = calc.category.displayName.lowercase()
            val descLower = calc.description.lowercase()
            val keywordsLower = calc.keywords.map { it.lowercase() }

            cleanQuery in nameLower ||
                cleanQuery in catLower ||
                cleanQuery in descLower ||
                idStr.contains(cleanQuery) ||
                keywordsLower.any { it.contains(cleanQuery) } ||
                tokens.all { t ->
                    t in nameLower || t in catLower || t in descLower || idStr.contains(t) || keywordsLower.any { it.contains(t) }
                }
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
