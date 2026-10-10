package com.example.data.model

enum class CalculatorLevel(val displayName: String, val routeName: String) {
    ADVANCE("Advance", "advance"),
    BASIC("Basic", "basic"),
    LOWER("Lower", "lower")
}

enum class CalculatorCategory(val displayName: String, val iconName: String) {
    FACTORY("Factory & Industrial", "factory"),
    TRANSFORMER("Transformer", "transformer"),
    GENERATOR("Generator", "generator"),
    MOTOR("Motor", "motor"),
    CABLE("Cable & Wire", "cable"),
    BUILDING("Building Electrical", "building"),
    POWER("Power System", "power"),
    BATTERY_UPS("Battery & UPS", "battery"),
    PFI("PFI & Capacitor", "pfi"),
    PROTECTION("Protection & Switchgear", "protection"),
    CONVERSION("Conversion", "conversion"),
    BASIC_ELEC("Basic Electrical", "basic_elec")
}

data class InputFieldConfig(
    val id: String,
    val label: String,
    val defaultValue: String = "",
    val unitOptions: List<String> = emptyList(),
    val defaultUnit: String = "",
    val hint: String = "",
    val isOptional: Boolean = false
)

data class CalculationStep(
    val stepNumber: Int,
    val title: String,
    val formula: String,
    val calculation: String,
    val result: String
) {
    val equation: String
        get() = calculation
}

data class CalculationResult(
    val primaryValue: String,
    val primaryUnit: String,
    val formulaUsed: String,
    val steps: List<CalculationStep> = emptyList(),
    val secondaryResults: List<Pair<String, String>> = emptyList(),
    val notes: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val standardBasis: String = "General Engineering / IEC Standard"
)

data class CalculatorDefinition(
    val id: Int,
    val name: String,
    val level: CalculatorLevel,
    val category: CalculatorCategory,
    val description: String,
    val formula: String,
    val standard: String = "IEC 60364 / IEEE Std",
    val keywords: List<String> = emptyList(),
    val inputs: List<InputFieldConfig>,
    val calculate: (Map<String, Double>, Map<String, String>) -> CalculationResult,
    val supportsPhaseSelection: Boolean = false,
    val defaultPhase: String = "3-Phase"
) {
    val route: String
        get() = "calc_$id"

    val isPhaseSelectable: Boolean
        get() = supportsPhaseSelection || inputs.any { it.id.equals("phase", ignoreCase = true) || it.id.equals("is3Phase", ignoreCase = true) } || id in PHASE_ENABLED_CALC_IDS

    companion object {
        val PHASE_ENABLED_CALC_IDS = setOf(
            1, 3, 5, 11, 13, 14, 16, 17, 18, 19, 20, 21, 22, 24, 28, 29, 36, 37,
            48, 49, 50, 51, 52, 53, 55, 57, 59, 60, 61, 62,
            69, 70, 74, 76, 80, 81, 82, 83, 84, 88,
            101, 102, 103, 106, 107, 110, 111, 125, 128,
            142, 143, 144, 145, 146, 160, 161, 162, 164, 165, 166
        )
    }
}
