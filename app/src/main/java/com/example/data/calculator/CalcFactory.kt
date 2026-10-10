package com.example.data.calculator

import com.example.data.calculator.ElectricalFormulas.fmt
import com.example.data.model.CalculationResult
import com.example.data.model.CalculationStep
import com.example.data.model.CalculatorCategory
import com.example.data.model.CalculatorDefinition
import com.example.data.model.CalculatorLevel
import com.example.data.model.InputFieldConfig
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.sqrt
import kotlin.math.tan

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
        val isPhaseCapable = id in CalculatorDefinition.PHASE_ENABLED_CALC_IDS
        val defaultPhase = if (id == 3 || id == 18) "1-Phase" else "3-Phase"

        return CalculatorDefinition(
            id = id,
            name = name,
            level = level,
            category = category,
            description = description,
            formula = formula,
            standard = standard,
            inputs = inputs,
            supportsPhaseSelection = isPhaseCapable,
            defaultPhase = defaultPhase,
            calculate = { vals, units ->
                val phase = units["phase"] ?: units["is3Phase"] ?: defaultPhase
                val is3p = phase != "1-Phase"

                if (isPhaseCapable) {
                    val phaseRes = calculatePhaseAware(id, vals, is3p, formula, calc, stepsBuilder, secondaryBuilder)
                    CalculationResult(
                        primaryValue = phaseRes.primaryValue,
                        primaryUnit = phaseRes.primaryUnit,
                        formulaUsed = phaseRes.formulaUsed,
                        steps = phaseRes.steps,
                        secondaryResults = phaseRes.secondaryResults,
                        notes = notes + listOf("Phase Mode: ${if (is3p) "Three Phase (3-Phase / 400V)" else "Single Phase (1-Phase / 230V)"} (Single Phase & Three Phase loads strictly separated)"),
                        standardBasis = standard
                    )
                } else {
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
            }
        )
    }

    private data class PhaseCalculationData(
        val primaryValue: String,
        val primaryUnit: String,
        val formulaUsed: String,
        val steps: List<CalculationStep>,
        val secondaryResults: List<Pair<String, String>>
    )

    private fun calculatePhaseAware(
        id: Int,
        vals: Map<String, Double>,
        is3p: Boolean,
        defaultFormula: String,
        fallbackCalc: (Map<String, Double>) -> Pair<String, String>,
        fallbackSteps: ((Map<String, Double>, String) -> List<CalculationStep>)?,
        fallbackSec: ((Map<String, Double>) -> List<Pair<String, String>>)?
    ): PhaseCalculationData {
        return when (id) {
            11 -> { // Transformer Load Current
                val kva = vals["kva"] ?: 500.0
                val vlt = vals["vlt"] ?: if (is3p) 400.0 else 230.0
                val vht = vals["vht"] ?: 11000.0
                val ilt = if (is3p) (kva * 1000.0) / (sqrt(3.0) * vlt) else (kva * 1000.0) / vlt
                val iht = if (is3p) (kva * 1000.0) / (sqrt(3.0) * vht) else (kva * 1000.0) / vht
                val form = if (is3p) "I_LT = kVA × 1000 / (√3 × V_LT),  I_HT = kVA × 1000 / (√3 × V_HT)" else "I_LT = kVA × 1000 / V_LT,  I_HT = kVA × 1000 / V_HT"
                PhaseCalculationData(
                    fmt(ilt), "Amperes (LT ${if (is3p) "3-Phase" else "1-Phase"} FLC)", form,
                    listOf(
                        CalculationStep(1, "LT Current", form, "${kva * 1000} / ${if (is3p) "1.732 × $vlt" else "$vlt"}", "${fmt(ilt)} A"),
                        CalculationStep(2, "HT Current", "Primary FLC", "${kva * 1000} / ${if (is3p) "1.732 × $vht" else "$vht"}", "${fmt(iht)} A")
                    ),
                    listOf("HT Primary Current" to "${fmt(iht)} A", "Selected Phase" to if (is3p) "3-Phase (400V)" else "Single Phase (230V)")
                )
            }
            13 -> { // Transformer Rating Calculation
                val v = vals["v"] ?: if (is3p) 415.0 else 230.0
                val i = vals["i"] ?: 350.0
                val kva = if (is3p) (sqrt(3.0) * v * i) / 1000.0 else (v * i) / 1000.0
                val form = if (is3p) "kVA = (√3 × V × I) / 1000" else "kVA = (V × I) / 1000"
                PhaseCalculationData(
                    fmt(kva), "kVA (${if (is3p) "3-Phase" else "Single Phase"} Rating)", form,
                    listOf(CalculationStep(1, "Rating", form, "${if (is3p) "1.732 × $v × $i" else "$v × $i"} / 1000", "${fmt(kva)} kVA")),
                    listOf("Design Current" to "$i A", "Operating Voltage" to "$v V")
                )
            }
            14 -> { // Transformer Basic Calculation
                val kva = vals["kva"] ?: 100.0
                val v1 = vals["v1"] ?: 11000.0
                val v2 = vals["v2"] ?: if (is3p) 400.0 else 230.0
                val i1 = if (is3p) (kva * 1000.0) / (sqrt(3.0) * v1) else (kva * 1000.0) / v1
                val i2 = if (is3p) (kva * 1000.0) / (sqrt(3.0) * v2) else (kva * 1000.0) / v2
                val form = if (is3p) "I₂ = (kVA × 1000) / (√3 × V₂)" else "I₂ = (kVA × 1000) / V₂"
                PhaseCalculationData(
                    fmt(i2), "Amperes (Secondary Full Load)", form,
                    listOf(CalculationStep(1, "Secondary Current", form, "${kva * 1000} / ${if (is3p) "1.732 × $v2" else "$v2"}", "${fmt(i2)} A")),
                    listOf("Primary Current" to "${fmt(i1)} A", "Transformation Ratio" to fmt(v1 / v2, 2))
                )
            }
            16 -> { // Generator Load Calculation
                val kva = vals["kva"] ?: 250.0
                val v = vals["v"] ?: if (is3p) 400.0 else 230.0
                val pf = vals["pf"] ?: 0.80
                val i = if (is3p) (kva * 1000.0) / (sqrt(3.0) * v) else (kva * 1000.0) / v
                val kw = kva * pf
                val form = if (is3p) "I = (kVA × 1000) / (√3 × V)" else "I = (kVA × 1000) / V"
                PhaseCalculationData(
                    fmt(i), "Amperes (${if (is3p) "3-Phase" else "1-Phase"} Rated Current)", form,
                    listOf(CalculationStep(1, "Current", form, "${kva * 1000} / ${if (is3p) "1.732 × $v" else "$v"}", "${fmt(i)} A")),
                    listOf("Active Power Output" to "${fmt(kw)} kW", "System Voltage" to "$v V")
                )
            }
            17 -> { // Cable Size Calculation All In One
                val kw = vals["kw"] ?: 45.0
                val v = vals["v"] ?: if (is3p) 400.0 else 230.0
                val pf = vals["pf"] ?: 0.85
                val l = vals["length"] ?: 60.0
                val i = if (is3p) (kw * 1000.0) / (sqrt(3.0) * v * pf) else (kw * 1000.0) / (v * pf)
                val cable = ElectricalFormulas.selectCable(i * 1.25)
                val form = if (is3p) "I = kW × 1000 / (√3 × V × PF) [3-Phase]" else "I = kW × 1000 / (V × PF) [Single Phase]"
                PhaseCalculationData(
                    "$cable", "mm² (${if (is3p) "3/4-Core 3-Phase" else "2-Core 1-Phase"} Cable)", form,
                    listOf(
                        CalculationStep(1, "Line Current", form, "${kw * 1000} / ${if (is3p) "1.732 × $v × $pf" else "$v × $pf"}", "${fmt(i)} A"),
                        CalculationStep(2, "Recommended Cable Size", "Design Current with 25% margin = ${fmt(i * 1.25)} A", "Selected per IEC 60364", "$cable mm² Cu")
                    ),
                    listOf("Full Load Current" to "${fmt(i)} A", "Design Margin Current (125%)" to "${fmt(i * 1.25)} A", "Route Length" to "$l m")
                )
            }
            18 -> { // Wire Length Calculation All In One
                val vd = vals["vdrop"] ?: if (is3p) 12.0 else 6.9
                val i = vals["i"] ?: 20.0
                val a = vals["area"] ?: 4.0
                val rho = 0.0175
                val length = if (is3p) (vd * a) / (sqrt(3.0) * i * rho) else (vd * a) / (2.0 * i * rho)
                val form = if (is3p) "L_max = (ΔV × A) / (√3 × I × ρ) [3-Phase]" else "L_max = (ΔV × A) / (2 × I × ρ) [Single Phase]"
                PhaseCalculationData(
                    fmt(length), "Meters Maximum Route Length", form,
                    listOf(CalculationStep(1, "Length", form, "($vd × $a) / (${if (is3p) "1.732" else "2"} × $i × $rho)", "${fmt(length)} m")),
                    listOf("Allowable Drop" to "$vd V", "Current" to "$i A", "Phase Type" to if (is3p) "3-Phase" else "Single Phase")
                )
            }
            19 -> { // Cable Power Losses Calculation All In
                val i = vals["i"] ?: 80.0
                val r = vals["r"] ?: 0.12
                val loss = if (is3p) 3.0 * i * i * r else 2.0 * i * i * r
                val form = if (is3p) "P_loss = 3 × I² × R [3-Phase]" else "P_loss = 2 × I² × R [Single Phase]"
                PhaseCalculationData(
                    fmt(loss / 1000.0), "kW Power Loss (${if (is3p) "3 Cores" else "Phase + Neutral"})", form,
                    listOf(CalculationStep(1, "Joule Losses", form, "${if (is3p) "3" else "2"} × $i² × $r", "${fmt(loss)} W")),
                    listOf("Total Heat Dissipation" to "${fmt(loss)} Watts", "Conductor Current" to "$i A")
                )
            }
            20 -> { // Copper Losses Calculation All In
                val i = vals["i"] ?: 50.0
                val r = vals["r"] ?: 0.25
                val loss = if (is3p) 3.0 * i * i * r else 2.0 * i * i * r
                val form = if (is3p) "P_cu = 3 × I² × R [3-Phase]" else "P_cu = 2 × I² × R [Single Phase]"
                PhaseCalculationData(
                    fmt(loss), "Watts Copper Loss", form,
                    listOf(CalculationStep(1, "Copper Loss", form, "${if (is3p) "3" else "2"} × $i² × $r", "${fmt(loss)} W")),
                    listOf("Power Loss (kW)" to "${fmt(loss / 1000.0)} kW")
                )
            }
            21 -> { // Circuit Size Calculation Pro
                val i = vals["loadA"] ?: 36.0
                val b = ElectricalFormulas.selectBreaker(i)
                val cable = ElectricalFormulas.selectCable(i * 1.25)
                val form = if (is3p) "Breaker (3-Phase 3P/4P) >= 1.25 × I_load" else "Breaker (1-Phase 1P/2P) >= 1.25 × I_load"
                PhaseCalculationData(
                    "$b", "Amperes ${if (is3p) "3-Pole/4-Pole" else "Single-Pole/DP"} Breaker", form,
                    listOf(CalculationStep(1, "Breaker Rating", form, "1.25 × $i = ${fmt(i * 1.25)} A", "$b A Breaker")),
                    listOf("Recommended Cable" to "$cable mm²", "Design Current" to "${fmt(i * 1.25)} A")
                )
            }
            22 -> { // Voltage Drop Calculation
                val i = vals["current"] ?: 60.0
                val l = vals["length"] ?: 120.0
                val sysV = vals["v"] ?: if (is3p) 400.0 else 230.0
                val rm = (vals["r"] ?: 0.38) / 1000.0
                val vd = if (is3p) sqrt(3.0) * i * l * rm * 0.85 else 2.0 * i * l * rm * 0.85
                val pct = (vd / sysV) * 100.0
                val form = if (is3p) "ΔV = √3 × I × L × R / 1000 [3-Phase]" else "ΔV = 2 × I × L × R / 1000 [Single Phase]"
                PhaseCalculationData(
                    fmt(vd), "Volts (${fmt(pct)}% at ${sysV.toInt()}V)", form,
                    listOf(CalculationStep(1, "Voltage Drop", form, "${if (is3p) "1.732" else "2"} × $i × $l × $rm × 0.85", "${fmt(vd)} V (${fmt(pct)}%)")),
                    listOf("Voltage Drop (V)" to "${fmt(vd)} V", "Percentage Drop" to "${fmt(pct)}%", "System Voltage" to "$sysV V")
                )
            }
            24 -> { // Conductor Size Calculation
                val i = vals["i"] ?: 45.0
                val l = vals["l"] ?: 80.0
                val vd = vals["vd"] ?: if (is3p) 12.0 else 6.9
                val rho = 0.0175
                val area = if (is3p) (sqrt(3.0) * rho * l * i) / vd else (2.0 * rho * l * i) / vd
                val form = if (is3p) "A = (√3 × ρ × L × I) / ΔV [3-Phase]" else "A = (2 × ρ × L × I) / ΔV [Single Phase]"
                PhaseCalculationData(
                    fmt(area), "mm² Minimum Cross-Section", form,
                    listOf(CalculationStep(1, "Conductor Area", form, "(${if (is3p) "1.732" else "2"} × $rho × $l × $i) / $vd", "${fmt(area)} mm²")),
                    listOf("Nearest Standard Cable" to "${ElectricalFormulas.selectCable(area)} mm²")
                )
            }
            28 -> { // LBS Size Calculation
                val kva = vals["kva"] ?: 630.0
                val kv = vals["kv"] ?: 11.0
                val flc = if (is3p) kva / (sqrt(3.0) * kv) else kva / kv
                val rating = if (flc <= 400) 630 else 800
                val form = if (is3p) "I = kVA / (√3 × kV) [3-Phase]" else "I = kVA / kV [Single Phase]"
                PhaseCalculationData(
                    "$rating", "Amperes LBS Continuous Rating (FLC: ${fmt(flc)} A)", form,
                    listOf(CalculationStep(1, "Full Load Current", form, "$kva / ${if (is3p) "1.732 × $kv" else "$kv"}", "${fmt(flc)} A")),
                    listOf("Rated Switch Current" to "$rating A", "Short Circuit Breaking" to "20 kA / 1s")
                )
            }
            29 -> { // ATS Size Calculation
                val kva = vals["kva"] ?: 350.0
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val flc = if (is3p) (kva * 1000.0) / (sqrt(3.0) * volt) else (kva * 1000.0) / volt
                val b = ElectricalFormulas.selectBreaker(flc * 1.25)
                val form = if (is3p) "ATS Rating >= 1.25 × [kVA × 1000 / (√3 × V)] (4-Pole 3-Phase)" else "ATS Rating >= 1.25 × [kVA × 1000 / V] (2-Pole Single Phase)"
                PhaseCalculationData(
                    "$b", "Amperes ${if (is3p) "4-Pole 3-Phase" else "2-Pole 1-Phase"} ATS", form,
                    listOf(CalculationStep(1, "Rated Current", form, "${kva * 1000} / ${if (is3p) "1.732 × $volt" else "$volt"}", "${fmt(flc)} A")),
                    listOf("Continuous Rating (125%)" to "$b A", "Full Load Current" to "${fmt(flc)} A")
                )
            }
            36 -> { // Drop Out Fuse Size Calculation
                val kva = vals["kva"] ?: 200.0
                val kv = vals["kv"] ?: 11.0
                val flc = if (is3p) kva / (sqrt(3.0) * kv) else kva / kv
                val fuse = flc * 1.5
                val form = if (is3p) "Fuse Link = 1.5 × [kVA / (√3 × kV)] [3-Phase]" else "Fuse Link = 1.5 × (kVA / kV) [Single Phase]"
                PhaseCalculationData(
                    "${fmt(fuse)} A", "Drop Out Fuse Link (${if (is3p) "3-Phase" else "Single Phase"})", form,
                    listOf(CalculationStep(1, "Fuse Rating", form, "1.5 × ($kva / ${if (is3p) "1.732 × $kv" else "$kv"})", "${fmt(fuse)} A")),
                    listOf("Transformer FLC" to "${fmt(flc)} A")
                )
            }
            37 -> { // HRC Fuse Size Calculation
                val kva = vals["kva"] ?: 250.0
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val flc = if (is3p) (kva * 1000.0) / (sqrt(3.0) * volt) else (kva * 1000.0) / volt
                val fuse = ElectricalFormulas.selectBreaker(flc * 1.25)
                val form = if (is3p) "Fuse >= 1.25 × [kVA × 1000 / (√3 × V)]" else "Fuse >= 1.25 × [kVA × 1000 / V]"
                PhaseCalculationData(
                    "$fuse A", "HRC Fuse Rating (${if (is3p) "3-Phase 400V" else "Single Phase 230V"})", form,
                    listOf(CalculationStep(1, "HRC Fuse", form, "1.25 × ${fmt(flc)} A", "$fuse A")),
                    listOf("Full Load Current" to "${fmt(flc)} A")
                )
            }
            48 -> { // AC Size Calculation Pro
                val sqft = vals["sqft"] ?: 250.0
                val people = vals["people"] ?: 4.0
                val btu = (sqft * 25.0) + (people * 600.0)
                val tons = btu / 12000.0
                val form = if (is3p) "Tons = Total BTU / 12000 (3-Phase 400V Commercial/VRF)" else "Tons = Total BTU / 12000 (Single Phase 230V Split AC)"
                PhaseCalculationData(
                    "${fmt(tons, 1)} Tons", if (is3p) "Commercial/VRF 3-Phase AC" else "Residential Split AC (1-Phase)", form,
                    listOf(CalculationStep(1, "Cooling Capacity", form, "($sqft × 25 + $people × 600) / 12000", "${fmt(tons, 1)} Tons (${fmt(btu)} BTU/hr)")),
                    listOf("Total Cooling Load" to "${fmt(btu)} BTU/h", "Power Equivalent" to "${fmt(tons * 3.517, 1)} kW")
                )
            }
            49 -> { // AC Load and Protection Calculation
                val tons = vals["ton"] ?: 2.5
                val eer = vals["eer"] ?: 10.5
                val volt = if (is3p) 400.0 else 230.0
                val watts = (tons * 12000.0) / eer
                val flc = if (is3p) watts / (sqrt(3.0) * volt * 0.90) else watts / (volt * 0.90)
                val mcb = ElectricalFormulas.selectBreaker(flc * 1.5)
                val form = if (is3p) "I = Watts / (√3 × V × PF) [3-Phase 400V]" else "I = Watts / (V × PF) [Single Phase 230V]"
                PhaseCalculationData(
                    "$mcb A", "${if (is3p) "3-Pole" else "Single-Pole/DP"} Type-C MCB", form,
                    listOf(CalculationStep(1, "Operating Current", form, "${fmt(watts)} / (${if (is3p) "1.732 × $volt" else "$volt"} × 0.90)", "${fmt(flc)} A")),
                    listOf("Running Current" to "${fmt(flc)} A", "Power Input" to "${fmt(watts / 1000.0)} kW")
                )
            }
            50 -> { // Magnetic Contactor Calculation
                val kw = vals["kw"] ?: 18.5
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val pf = vals["pf"] ?: 0.85
                val flc = if (is3p) (kw * 1000.0) / (sqrt(3.0) * volt * pf) else (kw * 1000.0) / (volt * pf)
                val contactor = ElectricalFormulas.selectBreaker(flc * 1.25)
                val form = if (is3p) "Contactor >= 1.25 × [kW × 1000 / (√3 × V × PF)] (3-Pole AC-3)" else "Contactor >= 1.25 × [kW × 1000 / (V × PF)] (Single-Phase AC-1/AC-3)"
                PhaseCalculationData(
                    "$contactor A", "${if (is3p) "3-Pole" else "1-Pole/2-Pole"} AC-3 Contactor (FLC: ${fmt(flc)} A)", form,
                    listOf(CalculationStep(1, "Contactor Size", form, "1.25 × ${fmt(flc)}", "$contactor A")),
                    listOf("Motor Full Load Current" to "${fmt(flc)} A", "Operating Voltage" to "$volt V")
                )
            }
            51 -> { // Overload Relay Calculation
                val kw = vals["kw"] ?: 15.0
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val pf = vals["pf"] ?: 0.86
                val eff = vals["eff"] ?: 0.90
                val flc = if (is3p) (kw * 1000.0) / (sqrt(3.0) * volt * pf * eff) else (kw * 1000.0) / (volt * pf * eff)
                val form = if (is3p) "Relay = 1.05 × [kW × 1000 / (√3 × V × PF × η)]" else "Relay = 1.05 × [kW × 1000 / (V × PF × η)]"
                PhaseCalculationData(
                    "${fmt(flc * 1.05)} A", "${if (is3p) "3-Phase" else "Single Phase"} Overload Setting (FLC: ${fmt(flc)} A)", form,
                    listOf(CalculationStep(1, "Relay Setting", form, "1.05 × ${fmt(flc)}", "${fmt(flc * 1.05)} A")),
                    listOf("Motor FLC" to "${fmt(flc)} A", "Setting Range" to "${fmt(flc * 0.95)} - ${fmt(flc * 1.15)} A")
                )
            }
            52 -> { // Fuse Size Calculation
                val kw = vals["kw"] ?: 11.0
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val pf = vals["pf"] ?: 0.85
                val flc = if (is3p) (kw * 1000.0) / (sqrt(3.0) * volt * pf) else (kw * 1000.0) / (volt * pf)
                val fuse = ElectricalFormulas.selectBreaker(flc * 1.5)
                val form = if (is3p) "Fuse = 1.5 × [kW × 1000 / (√3 × V × PF)]" else "Fuse = 1.5 × [kW × 1000 / (V × PF)]"
                PhaseCalculationData(
                    "$fuse A", "${if (is3p) "3-Phase" else "Single Phase"} Back-up Fuse (FLC: ${fmt(flc)} A)", form,
                    listOf(CalculationStep(1, "Fuse Rating", form, "1.5 × ${fmt(flc)}", "$fuse A")),
                    listOf("Motor FLC" to "${fmt(flc)} A")
                )
            }
            53 -> { // Direct Starter Calculation (DOL)
                val kw = vals["kw"] ?: 7.5
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val pf = vals["pf"] ?: 0.85
                val eff = vals["eff"] ?: 0.88
                val flc = if (is3p) (kw * 1000.0) / (sqrt(3.0) * volt * pf * eff) else (kw * 1000.0) / (volt * pf * eff)
                val contactor = ElectricalFormulas.selectBreaker(flc * 1.25)
                val form = if (is3p) "FLC = (kW × 1000) / (√3 × V × PF × η) [3-Phase DOL]" else "FLC = (kW × 1000) / (V × PF × η) [Single Phase DOL]"
                PhaseCalculationData(
                    "${fmt(flc)} A", "DOL Starter (${if (is3p) "3-Phase 400V" else "Single Phase 230V"})", form,
                    listOf(
                        CalculationStep(1, "Motor FLC", form, "${kw * 1000} / (${if (is3p) "1.732" else "1"} × $volt × $pf × $eff)", "${fmt(flc)} A"),
                        CalculationStep(2, "Contactor Rating", "1.25 × FLC", "1.25 × ${fmt(flc)}", "$contactor A AC-3"),
                        CalculationStep(3, "Overload Relay", "1.0 × FLC", "${fmt(flc)} A", "${fmt(flc)} A Thermal Relay")
                    ),
                    listOf("Contactor Rating" to "$contactor A", "Thermal Overload" to "${fmt(flc)} A", "Back-up MCB" to "${ElectricalFormulas.selectBreaker(flc * 2.0)} A")
                )
            }
            55 -> { // Motor Size Calculation
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val i = vals["i"] ?: 22.0
                val pf = vals["pf"] ?: 0.85
                val eff = vals["eff"] ?: 0.88
                val kw = if (is3p) (sqrt(3.0) * volt * i * pf * eff) / 1000.0 else (volt * i * pf * eff) / 1000.0
                val hp = kw / 0.746
                val form = if (is3p) "kW = (√3 × V × I × PF × η) / 1000" else "kW = (V × I × PF × η) / 1000"
                PhaseCalculationData(
                    "${fmt(kw)} kW", "${fmt(hp, 1)} HP Output (${if (is3p) "3-Phase" else "Single Phase"})", form,
                    listOf(CalculationStep(1, "Output Power", form, "${if (is3p) "1.732 × $volt × $i" else "$volt × $i"} × $pf × $eff / 1000", "${fmt(kw)} kW")),
                    listOf("Horsepower" to "${fmt(hp, 1)} HP", "Electrical Input" to "${fmt(kw / eff)} kW")
                )
            }
            57 -> { // Motor Load Protection
                val kw = vals["kw"] ?: 15.0
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val pf = vals["pf"] ?: 0.85
                val flc = if (is3p) (kw * 1000.0) / (sqrt(3.0) * volt * pf * 0.90) else (kw * 1000.0) / (volt * pf * 0.90)
                val mpcb = ElectricalFormulas.selectBreaker(flc * 1.15)
                val form = if (is3p) "MPCB = 1.15 × [kW × 1000 / (√3 × V × PF × η)]" else "MPCB = 1.15 × [kW × 1000 / (V × PF × η)]"
                PhaseCalculationData(
                    "$mpcb A", "${if (is3p) "3-Pole" else "Single-Pole/DP"} MPCB Frame", form,
                    listOf(CalculationStep(1, "Protection Setting", form, "1.15 × ${fmt(flc)}", "$mpcb A")),
                    listOf("Motor Full Load Current" to "${fmt(flc)} A")
                )
            }
            59 -> { // Motor Starting Current
                val kw = vals["kw"] ?: 22.0
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val pf = vals["pf"] ?: 0.85
                val eff = vals["eff"] ?: 0.90
                val flc = if (is3p) (kw * 1000.0) / (sqrt(3.0) * volt * pf * eff) else (kw * 1000.0) / (volt * pf * eff)
                val mult = if (is3p) 6.0 else 5.0
                val startI = flc * mult
                val form = if (is3p) "I_start = 6.0 × [kW × 1000 / (√3 × V × PF × η)] [3-Phase DOL]" else "I_start = 5.0 × [kW × 1000 / (V × PF × η)] [Single Phase DOL]"
                PhaseCalculationData(
                    "${fmt(startI)} A", "Direct Starting Current ($mult × ${fmt(flc)} A FLC)", form,
                    listOf(CalculationStep(1, "Starting Inrush", form, "$mult × ${fmt(flc)}", "${fmt(startI)} A")),
                    listOf("Full Load Current" to "${fmt(flc)} A", "Starting Multiplier" to "${mult.toInt()}× FLC")
                )
            }
            60 -> { // Motor FLC Calculation
                val kw = vals["kw"] ?: 30.0
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val pf = vals["pf"] ?: 0.86
                val eff = vals["eff"] ?: 0.91
                val flc = if (is3p) (kw * 1000.0) / (sqrt(3.0) * volt * pf * eff) else (kw * 1000.0) / (volt * pf * eff)
                val form = if (is3p) "I_FLC = (kW × 1000) / (√3 × V × PF × η) [3-Phase]" else "I_FLC = (kW × 1000) / (V × PF × η) [Single Phase]"
                PhaseCalculationData(
                    fmt(flc), "Amperes (${if (is3p) "3-Phase 400V" else "Single Phase 230V"} FLC)", form,
                    listOf(CalculationStep(1, "FLC", form, "${kw * 1000} / (${if (is3p) "1.732" else "1"} × $volt × $pf × $eff)", "${fmt(flc)} A")),
                    listOf("Motor Active Power" to "$kw kW", "Selected Phase" to if (is3p) "3-Phase" else "Single Phase")
                )
            }
            61 -> { // Motor Current Calculation
                val hp = vals["hp"] ?: 10.0
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val pf = vals["pf"] ?: 0.85
                val eff = vals["eff"] ?: 0.88
                val i = if (is3p) (hp * 746.0) / (sqrt(3.0) * volt * pf * eff) else (hp * 746.0) / (volt * pf * eff)
                val form = if (is3p) "I = (HP × 746) / (√3 × V × PF × η)" else "I = (HP × 746) / (V × PF × η)"
                PhaseCalculationData(
                    fmt(i), "Amperes (${if (is3p) "3-Phase" else "Single Phase"} Current)", form,
                    listOf(CalculationStep(1, "Current", form, "${hp * 746} / (${if (is3p) "1.732" else "1"} × $volt × $pf × $eff)", "${fmt(i)} A")),
                    listOf("Equivalent Power" to "${fmt(hp * 0.746)} kW")
                )
            }
            62 -> { // Motor Voltage Calculation
                val kw = vals["kw"] ?: 15.0
                val i = vals["i"] ?: 28.0
                val pf = vals["pf"] ?: 0.85
                val eff = vals["eff"] ?: 0.89
                val v = if (is3p) (kw * 1000.0) / (sqrt(3.0) * i * pf * eff) else (kw * 1000.0) / (i * pf * eff)
                val form = if (is3p) "V = (kW × 1000) / (√3 × I × PF × η) [3-Phase Line-to-Line]" else "V = (kW × 1000) / (I × PF × η) [Single Phase Line-to-Neutral]"
                PhaseCalculationData(
                    fmt(v), "Volts (${if (is3p) "3-Phase Line-to-Line" else "Single Phase Line-to-Neutral"})", form,
                    listOf(CalculationStep(1, "Voltage", form, "${kw * 1000} / (${if (is3p) "1.732" else "1"} × $i × $pf × $eff)", "${fmt(v)} V")),
                    listOf("Line Current" to "$i A")
                )
            }
            5, 106, 107, 110, 111 -> { // PFI & Capacitor Bank calculators
                val kw = vals["kw"] ?: 150.0
                val pf1 = (vals["pf1"] ?: 0.75).coerceIn(0.1, 0.99)
                val pf2 = (vals["pf2"] ?: 0.98).coerceIn(pf1, 1.0)
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val phi1 = acos(pf1); val phi2 = acos(pf2)
                val kvar = kw * (tan(phi1) - tan(phi2))
                val ic = if (is3p) (kvar * 1000.0) / (sqrt(3.0) * volt) else (kvar * 1000.0) / volt
                val form = if (is3p) "Q = P × [tan(φ₁) - tan(φ₂)],  I_c = Q × 1000 / (√3 × V) [3-Phase]" else "Q = P × [tan(φ₁) - tan(φ₂)],  I_c = Q × 1000 / V [Single Phase]"
                PhaseCalculationData(
                    "${fmt(kvar)} kVAR", "Required Bank (${fmt(ic)} A at ${volt.toInt()}V ${if (is3p) "3-Phase" else "Single Phase"})", form,
                    listOf(
                        CalculationStep(1, "Required kVAR", form, "$kw × [tan(${fmt(phi1, 3)}) - tan(${fmt(phi2, 3)})]", "${fmt(kvar)} kVAR"),
                        CalculationStep(2, "Capacitor Current", "Q / (${if (is3p) "√3 × " else ""}V)", "${fmt(kvar * 1000)} / ${if (is3p) "1.732 × $volt" else "$volt"}", "${fmt(ic)} A")
                    ),
                    listOf("Capacitor Current" to "${fmt(ic)} A", "Target PF" to "$pf2", "Bank System" to if (is3p) "3-Phase Delta Bank" else "Single-Phase Bank")
                )
            }
            125 -> { // Cable Ampacity Calculation
                val a = vals["ampacity"] ?: 85.0
                val c = vals["cables"] ?: 1.0
                val derated = if (is3p) a * 0.70 * c else a * 0.80 * c
                val form = if (is3p) "I_z = I_tab × 0.70 (3 Loaded Phase Conductors)" else "I_z = I_tab × 0.80 (2 Loaded Phase+Neutral Conductors)"
                PhaseCalculationData(
                    "${fmt(derated)} A", "Derated Ampacity (${if (is3p) "3-Phase 3-Core" else "Single Phase 2-Core"})", form,
                    listOf(CalculationStep(1, "Ampacity", form, "$a × ${if (is3p) "0.70" else "0.80"} × $c", "${fmt(derated)} A")),
                    listOf("Base Tabular Ampacity" to "$a A", "Circuits" to "$c")
                )
            }
            128 -> { // Maximum Cable Length Calculation
                val vd = vals["vdrop"] ?: if (is3p) 12.0 else 6.9
                val i = vals["i"] ?: 40.0
                val rm = (vals["rm"] ?: 0.45) / 1000.0
                val l = if (is3p) vd / (sqrt(3.0) * i * rm) else vd / (2.0 * i * rm)
                val form = if (is3p) "L_max = ΔV / (√3 × I × R) [3-Phase]" else "L_max = ΔV / (2 × I × R) [Single Phase]"
                PhaseCalculationData(
                    "${fmt(l)} m", "Meters Maximum Route Length", form,
                    listOf(CalculationStep(1, "Max Length", form, "$vd / (${if (is3p) "1.732" else "2"} × $i × $rm)", "${fmt(l)} m")),
                    listOf("Allowable Drop" to "$vd V", "Operating Current" to "$i A")
                )
            }
            142 -> { // Motor Power Calculation
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val i = vals["i"] ?: 45.0
                val pf = vals["pf"] ?: 0.86
                val eff = vals["eff"] ?: 0.91
                val pIn = if (is3p) (sqrt(3.0) * volt * i * pf) / 1000.0 else (volt * i * pf) / 1000.0
                val pOut = pIn * eff
                val form = if (is3p) "P_in = √3 × V × I × cos φ,  P_out = P_in × η [3-Phase]" else "P_in = V × I × cos φ,  P_out = P_in × η [Single Phase]"
                PhaseCalculationData(
                    "${fmt(pOut)} kW", "Mechanical Shaft Power (${if (is3p) "3-Phase" else "Single Phase"})", form,
                    listOf(
                        CalculationStep(1, "Electrical Input", if (is3p) "√3 × V × I × PF" else "V × I × PF", "${if (is3p) "1.732 × $volt × $i" else "$volt × $i"} × $pf / 1000", "${fmt(pIn)} kW"),
                        CalculationStep(2, "Mechanical Output", "P_in × η", "${fmt(pIn)} × $eff", "${fmt(pOut)} kW")
                    ),
                    listOf("Electrical Input" to "${fmt(pIn)} kW", "Horsepower" to "${fmt(pOut / 0.746, 1)} HP")
                )
            }
            143 -> { // Motor Protection Setting Calculation
                val kw = vals["kw"] ?: 37.0
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val pf = vals["pf"] ?: 0.87
                val flc = if (is3p) (kw * 1000.0) / (sqrt(3.0) * volt * pf * 0.92) else (kw * 1000.0) / (volt * pf * 0.92)
                val form = if (is3p) "I_FLC = kW × 1000 / (√3 × V × PF × η),  Relay = 1.05 × I_FLC" else "I_FLC = kW × 1000 / (V × PF × η),  Relay = 1.05 × I_FLC"
                PhaseCalculationData(
                    "${fmt(flc * 1.05)} A", "${if (is3p) "3-Pole" else "Single Pole"} Overload Setting (FLC: ${fmt(flc)} A)", form,
                    listOf(CalculationStep(1, "Relay Setting", form, "1.05 × ${fmt(flc)}", "${fmt(flc * 1.05)} A")),
                    listOf("Motor FLC" to "${fmt(flc)} A")
                )
            }
            144 -> { // Motor Cable Size Calculation
                val kw = vals["kw"] ?: 45.0
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val flc = if (is3p) (kw * 1000.0) / (sqrt(3.0) * volt * 0.86 * 0.92) else (kw * 1000.0) / (volt * 0.86 * 0.92)
                val cable = ElectricalFormulas.selectCable(flc * 1.25)
                val form = if (is3p) "Cable >= 1.25 × I_FLC (3-Phase 3/4 Core)" else "Cable >= 1.25 × I_FLC (Single Phase 2-Core)"
                PhaseCalculationData(
                    "$cable mm²", "${if (is3p) "3/4-Core Cu/XLPE" else "2-Core Cu"} Feeder Cable", form,
                    listOf(CalculationStep(1, "Cable Selection", form, "1.25 × ${fmt(flc)} = ${fmt(flc * 1.25)} A", "$cable mm²")),
                    listOf("Motor FLC" to "${fmt(flc)} A")
                )
            }
            145 -> { // Motor Voltage Drop Calculation
                val kw = vals["kw"] ?: 30.0
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val l = vals["length"] ?: 80.0
                val a = vals["area"] ?: 16.0
                val rho = 0.0175
                val flc = if (is3p) (kw * 1000.0) / (sqrt(3.0) * volt * 0.86 * 0.91) else (kw * 1000.0) / (volt * 0.86 * 0.91)
                val startI = flc * (if (is3p) 6.0 else 5.0)
                val vd = if (is3p) (sqrt(3.0) * startI * l * (rho / a)) else (2.0 * startI * l * (rho / a))
                val pct = (vd / volt) * 100.0
                val form = if (is3p) "ΔV_start = √3 × I_start × L × ρ / A [3-Phase]" else "ΔV_start = 2 × I_start × L × ρ / A [Single Phase]"
                PhaseCalculationData(
                    "${fmt(vd)} V (${fmt(pct)}%)", "Starting Voltage Drop at ${volt.toInt()}V", form,
                    listOf(CalculationStep(1, "Transient Drop", form, "${if (is3p) "1.732" else "2"} × ${fmt(startI)} × $l × $rho / $a", "${fmt(vd)} V (${fmt(pct)}%)")),
                    listOf("Starting Current" to "${fmt(startI)} A", "Percentage Drop" to "${fmt(pct)}%")
                )
            }
            146 -> { // Motor Locked Rotor Current Calculation
                val hp = vals["hp"] ?: 50.0
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val kvaHp = 6.0
                val lrc = if (is3p) (kvaHp * hp * 1000.0) / (sqrt(3.0) * volt) else (kvaHp * hp * 1000.0) / volt
                val form = if (is3p) "I_LRC = (kVA/HP × HP × 1000) / (√3 × V) [3-Phase]" else "I_LRC = (kVA/HP × HP × 1000) / V [Single Phase]"
                PhaseCalculationData(
                    "${fmt(lrc)} A", "Locked Rotor Current (Code G ${if (is3p) "3-Phase" else "Single Phase"})", form,
                    listOf(CalculationStep(1, "Locked Rotor Current", form, "6.0 × $hp × 1000 / ${if (is3p) "1.732 × $volt" else "$volt"}", "${fmt(lrc)} A")),
                    listOf("NEMA Code Letter" to "Code G (6.0 kVA/HP)")
                )
            }
            160 -> { // Busbar Voltage Drop Calculation
                val i = vals["i"] ?: 1200.0
                val l = vals["length"] ?: 25.0
                val r = (vals["r"] ?: 0.035) / 1000.0
                val vd = if (is3p) sqrt(3.0) * i * l * r else 2.0 * i * l * r
                val form = if (is3p) "ΔV = √3 × I × L × R_ac [3-Phase]" else "ΔV = 2 × I × L × R_ac [Single Phase]"
                PhaseCalculationData(
                    "${fmt(vd)} V", "Busbar Voltage Drop (${if (is3p) "3-Phase 400V" else "Single Phase 230V"})", form,
                    listOf(CalculationStep(1, "Busbar Drop", form, "${if (is3p) "1.732" else "2"} × $i × $l × $r", "${fmt(vd)} V")),
                    listOf("Busbar Current" to "$i A")
                )
            }
            161 -> { // Panel Load Calculation
                val kw = vals["kw"] ?: 120.0
                val pf = vals["pf"] ?: 0.85
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val kva = kw / pf
                val i = if (is3p) (kva * 1000.0) / (sqrt(3.0) * volt) else (kva * 1000.0) / volt
                val form = if (is3p) "kVA = kW / PF,  I = kVA × 1000 / (√3 × V) [TPN 3-Phase Main Panel]" else "kVA = kW / PF,  I = kVA × 1000 / V [SPN 1-Phase Sub-Panel]"
                PhaseCalculationData(
                    "${fmt(kva)} kVA", "${if (is3p) "TPN 3-Phase Main Panel" else "SPN Single Phase Sub-Panel"} (${fmt(i)} A at ${volt.toInt()}V)", form,
                    listOf(
                        CalculationStep(1, "Demand kVA", "kW / PF", "$kw / $pf", "${fmt(kva)} kVA"),
                        CalculationStep(2, "Incomer Current", if (is3p) "kVA × 1000 / (√3 × V)" else "kVA × 1000 / V", "${fmt(kva * 1000)} / ${if (is3p) "1.732 × $volt" else "$volt"}", "${fmt(i)} A")
                    ),
                    listOf("Total Incomer Current" to "${fmt(i)} A", "Operating Voltage" to "$volt V", "Panel Standard" to if (is3p) "TPN (400V 3-Phase)" else "SPN (230V 1-Phase)")
                )
            }
            162 -> { // Panel Main Breaker Size Calculation
                val kw = vals["kw"] ?: 160.0
                val pf = vals["pf"] ?: 0.85
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val kva = kw / pf
                val flc = if (is3p) (kva * 1000.0) / (sqrt(3.0) * volt) else (kva * 1000.0) / volt
                val b = ElectricalFormulas.selectBreaker(flc * 1.25)
                val form = if (is3p) "Breaker >= 1.25 × [kVA × 1000 / (√3 × V)] (3-Pole/4-Pole)" else "Breaker >= 1.25 × [kVA × 1000 / V] (2-Pole DP)"
                PhaseCalculationData(
                    "$b A", "${if (is3p) "3-Pole / 4-Pole" else "2-Pole DP"} Main Incomer Breaker", form,
                    listOf(CalculationStep(1, "Main Breaker", form, "1.25 × ${fmt(flc)}", "$b A Breaker")),
                    listOf("Incomer Full Load Current" to "${fmt(flc)} A", "Phase Type" to if (is3p) "3-Phase 400V" else "Single Phase 230V")
                )
            }
            164 -> { // DB Size Calculation
                val ways = (vals["circuits"] ?: 12.0).toInt()
                val std = listOf(4, 6, 8, 12, 16, 24).firstOrNull { it >= ways } ?: 24
                val form = if (is3p) "Standard TPN DB Enclosure (400V 3-Phase & Neutral)" else "Standard SPN DB Enclosure (230V Single Phase & Neutral)"
                PhaseCalculationData(
                    "$std-Way ${if (is3p) "TPN" else "SPN"}", "${if (is3p) "Three Phase (TPN)" else "Single Phase (SPN)"} Distribution Board", form,
                    listOf(CalculationStep(1, "DB Capacity", form, "$ways circuits required -> Standard $std-way selected", "$std-Way ${if (is3p) "TPN" else "SPN"}")),
                    listOf("Required Outgoing Circuits" to "$ways", "Spare Ways" to "${std - ways}")
                )
            }
            165 -> { // MCCB Size Calculation
                val kw = vals["kw"] ?: 75.0
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val pf = vals["pf"] ?: 0.85
                val flc = if (is3p) (kw * 1000.0) / (sqrt(3.0) * volt * pf) else (kw * 1000.0) / (volt * pf)
                val b = ElectricalFormulas.selectBreaker(flc * 1.25)
                val form = if (is3p) "MCCB >= 1.25 × [kW × 1000 / (√3 × V × PF)] (3-Pole/4-Pole Frame)" else "MCCB >= 1.25 × [kW × 1000 / (V × PF)] (2-Pole Frame)"
                PhaseCalculationData(
                    "$b A", "${if (is3p) "3-Pole/4-Pole" else "2-Pole"} MCCB Frame (FLC: ${fmt(flc)} A)", form,
                    listOf(CalculationStep(1, "MCCB Rating", form, "1.25 × ${fmt(flc)}", "$b A")),
                    listOf("Full Load Current" to "${fmt(flc)} A")
                )
            }
            166 -> { // MCB Size Calculation
                val w = vals["loadW"] ?: 3500.0
                val volt = vals["v"] ?: if (is3p) 400.0 else 230.0
                val flc = if (is3p) w / (sqrt(3.0) * volt * 0.90) else w / (volt * 0.90)
                val b = ElectricalFormulas.selectBreaker(flc * 1.25)
                val form = if (is3p) "MCB >= 1.25 × [W / (√3 × V × PF)] (3P/4P 10kA)" else "MCB >= 1.25 × [W / (V × PF)] (1P/2P 6kA)"
                PhaseCalculationData(
                    "$b A", "${if (is3p) "3-Pole/4-Pole (10kA)" else "Single Pole/DP (6kA)"} MCB (FLC: ${fmt(flc)} A)", form,
                    listOf(CalculationStep(1, "MCB Sizing", form, "1.25 × ${fmt(flc)}", "$b A MCB")),
                    listOf("Load Current" to "${fmt(flc)} A")
                )
            }
            else -> {
                val (primaryVal, primaryUnit) = fallbackCalc(vals)
                val steps = fallbackSteps?.invoke(vals, primaryVal) ?: listOf(
                    CalculationStep(1, "Calculation", defaultFormula, "Evaluated based on inputs", "$primaryVal $primaryUnit")
                )
                val sec = fallbackSec?.invoke(vals) ?: emptyList()
                PhaseCalculationData(primaryVal, primaryUnit, defaultFormula, steps, sec)
            }
        }
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
