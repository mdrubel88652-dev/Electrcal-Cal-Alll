package com.example.data.calculator

import com.example.data.model.CalculationResult
import com.example.data.model.CalculationStep
import com.example.data.model.CalculatorCategory
import com.example.data.model.CalculatorDefinition
import com.example.data.model.CalculatorLevel
import com.example.data.model.InputFieldConfig

object CalcFactory {
    fun create(
        id: Int,
        name: String,
        level: CalculatorLevel,
        category: CalculatorCategory,
        description: String,
        formula: String,
        inputs: List<InputFieldConfig>,
        standard: String = "IEC 60364 / IEEE",
        calc: (vals: Map<String, Double>) -> Pair<String, String>, // primaryValue, primaryUnit
        stepsBuilder: ((vals: Map<String, Double>, res: String) -> List<CalculationStep>)? = null,
        secondaryBuilder: ((vals: Map<String, Double>) -> List<Pair<String, String>>)? = null,
        notes: List<String> = emptyList()
    ): CalculatorDefinition {
        return CalculatorDefinition(
            id = id,
            name = name,
            level = level,
            category = category,
            description = description,
            formula = formula,
            standard = standard,
            inputs = inputs,
            calculate = { vals, _ ->
                val (primaryVal, primaryUnit) = calc(vals)
                val steps = stepsBuilder?.invoke(vals, primaryVal) ?: listOf(
                    CalculationStep(1, "Calculation", formula, "Evaluated based on inputs", "$primaryVal $primaryUnit")
                )
                val sec = secondaryBuilder?.invoke(vals) ?: emptyList()
                CalculationResult(
                    primaryValue = primaryVal,
                    primaryUnit = primaryUnit,
                    formulaUsed = formula,
                    steps = steps,
                    secondaryResults = sec,
                    notes = notes,
                    standardBasis = standard
                )
            }
        )
    }

    fun input(id: String, label: String, def: String, unit: String = "", hint: String = ""): InputFieldConfig {
        return InputFieldConfig(
            id = id,
            label = label,
            defaultValue = def,
            unitOptions = if (unit.isNotEmpty()) listOf(unit) else emptyList(),
            defaultUnit = unit,
            hint = hint
        )
    }
}
