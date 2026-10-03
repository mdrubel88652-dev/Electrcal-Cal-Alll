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
    val calculate: (Map<String, Double>, Map<String, String>) -> CalculationResult
) {
    val route: String
        get() = "calc_$id"
}
