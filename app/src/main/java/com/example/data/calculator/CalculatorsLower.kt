package com.example.data.calculator

import com.example.data.model.CalculationResult
import com.example.data.model.CalculationStep
import com.example.data.model.CalculatorCategory
import com.example.data.model.CalculatorDefinition
import com.example.data.model.CalculatorLevel
import com.example.data.model.InputFieldConfig
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

object CalculatorsLower {

    val list: List<CalculatorDefinition> = listOf(
        // 6. Electrical Basic Calculation
        CalculatorDefinition(
            id = 6,
            name = "Electrical Basic Calculation",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.BASIC_ELEC,
            description = "Calculates Power, Current, Voltage and Resistance using fundamental AC/DC relationships.",
            formula = "P = V × I × PF,  V = I × R,  S = V × I",
            inputs = listOf(
                InputFieldConfig("voltage", "Voltage (V)", "230", listOf("V", "kV"), "V"),
                InputFieldConfig("current", "Current (A)", "10", listOf("A", "mA"), "A"),
                InputFieldConfig("pf", "Power Factor (cos φ)", "0.85", emptyList(), "", hint = "0.1 to 1.0")
            ),
            calculate = { vals, units ->
                var v = vals["voltage"] ?: 230.0
                if (units["voltage"] == "kV") v *= 1000.0
                var i = vals["current"] ?: 10.0
                if (units["current"] == "mA") i /= 1000.0
                val pf = (vals["pf"] ?: 0.85).coerceIn(0.1, 1.0)
                val p = v * i * pf
                val s = v * i
                val r = if (i > 0) v / i else 0.0
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(p),
                    primaryUnit = "Watts (W)",
                    formulaUsed = "P = V × I × cos φ",
                    steps = listOf(
                        CalculationStep(1, "Active Power", "P = V × I × PF", "$v × $i × $pf", "${ElectricalFormulas.fmt(p)} W"),
                        CalculationStep(2, "Apparent Power", "S = V × I", "$v × $i", "${ElectricalFormulas.fmt(s)} VA"),
                        CalculationStep(3, "Equivalent Impedance", "Z = V / I", "$v / $i", "${ElectricalFormulas.fmt(r)} Ω")
                    ),
                    secondaryResults = listOf(
                        "Active Power (kW)" to "${ElectricalFormulas.fmt(p / 1000.0)} kW",
                        "Apparent Power (kVA)" to "${ElectricalFormulas.fmt(s / 1000.0)} kVA",
                        "Equivalent Resistance" to "${ElectricalFormulas.fmt(r)} Ω"
                    ),
                    notes = listOf("Single-phase calculation basis. For 3-phase, multiply by √3 (1.732).")
                )
            }
        ),

        // 7. Electricity Cost Calculator
        CalculatorDefinition(
            id = 7,
            name = "Electricity Cost Calculator",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.BASIC_ELEC,
            description = "Calculates total electrical energy consumption in kWh and financial cost over time.",
            formula = "Energy (kWh) = (Power in Watts × Hours × Days) / 1000, Cost = Energy × Tariff",
            inputs = listOf(
                InputFieldConfig("power", "Power Rating", "1500", listOf("Watts", "kW"), "Watts"),
                InputFieldConfig("hours", "Usage Hours Per Day", "6", listOf("Hours"), "Hours"),
                InputFieldConfig("days", "Number of Days", "30", listOf("Days"), "Days"),
                InputFieldConfig("tariff", "Electricity Rate (Cost per Unit/kWh)", "8.50", listOf("Currency/kWh"), "Currency/kWh")
            ),
            calculate = { vals, units ->
                var p = vals["power"] ?: 1500.0
                if (units["power"] == "kW") p *= 1000.0
                val h = vals["hours"] ?: 6.0
                val d = vals["days"] ?: 30.0
                val rate = vals["tariff"] ?: 8.5
                val kwh = (p * h * d) / 1000.0
                val cost = kwh * rate
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(cost),
                    primaryUnit = "Total Cost",
                    formulaUsed = "Cost = Energy (kWh) × Rate per Unit",
                    steps = listOf(
                        CalculationStep(1, "Total Energy", "(P × Hours × Days) / 1000", "($p × $h × $d) / 1000", "${ElectricalFormulas.fmt(kwh)} kWh"),
                        CalculationStep(2, "Total Expense", "Energy × Rate", "$kwh × $rate", "${ElectricalFormulas.fmt(cost)}")
                    ),
                    secondaryResults = listOf(
                        "Energy Consumption" to "${ElectricalFormulas.fmt(kwh)} kWh (Units)",
                        "Daily Energy" to "${ElectricalFormulas.fmt((p * h) / 1000.0)} kWh/day",
                        "Daily Cost" to "${ElectricalFormulas.fmt(((p * h) / 1000.0) * rate)}"
                    )
                )
            }
        ),

        // 8. Current Bill Calculator
        CalculatorDefinition(
            id = 8,
            name = "Current Bill Calculator",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.BASIC_ELEC,
            description = "Computes consumer electricity utility bill with energy slabs, demand charge, and meter rent.",
            formula = "Total Bill = (Consumed Units × Average Rate) + Demand Charge + Meter Rent",
            inputs = listOf(
                InputFieldConfig("units", "Units Consumed (kWh)", "280", listOf("Units"), "Units"),
                InputFieldConfig("rate", "Rate per Unit", "7.50", listOf("Rate/Unit"), "Rate/Unit"),
                InputFieldConfig("demand", "Demand Charge / Fixed Charge", "75.00", listOf("Fixed"), "Fixed"),
                InputFieldConfig("meterRent", "Meter Rent & Taxes", "40.00", listOf("Fixed"), "Fixed")
            ),
            calculate = { vals, _ ->
                val u = vals["units"] ?: 280.0
                val r = vals["rate"] ?: 7.5
                val d = vals["demand"] ?: 75.0
                val m = vals["meterRent"] ?: 40.0
                val energyCharge = u * r
                val total = energyCharge + d + m
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(total),
                    primaryUnit = "Total Bill Amount",
                    formulaUsed = "Bill = (Units × Rate) + Demand Charge + Meter Charge",
                    steps = listOf(
                        CalculationStep(1, "Energy Charge", "Units × Rate", "$u × $r", "${ElectricalFormulas.fmt(energyCharge)}"),
                        CalculationStep(2, "Fixed Charges", "Demand + Meter", "$d + $m", "${ElectricalFormulas.fmt(d + m)}"),
                        CalculationStep(3, "Net Bill", "Energy Charge + Fixed", "$energyCharge + ${d + m}", "${ElectricalFormulas.fmt(total)}")
                    ),
                    secondaryResults = listOf(
                        "Energy Charge" to ElectricalFormulas.fmt(energyCharge),
                        "Fixed Surcharges" to ElectricalFormulas.fmt(d + m)
                    )
                )
            }
        ),

        // 9. Hysteresis Loss Calculation
        CalculatorDefinition(
            id = 9,
            name = "Hysteresis Loss Calculation",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.TRANSFORMER,
            description = "Calculates ferromagnetic core hysteresis power loss using the Steinmetz equation.",
            formula = "Ph = η × Bmax^1.6 × f × V",
            inputs = listOf(
                InputFieldConfig("eta", "Steinmetz Hysteresis Coeff (η)", "0.001", emptyList(), "", hint = "Silicon steel: 0.001"),
                InputFieldConfig("bmax", "Max Flux Density Bmax (Tesla)", "1.2", listOf("Tesla", "Gauss"), "Tesla"),
                InputFieldConfig("freq", "Frequency f (Hz)", "50", listOf("Hz", "kHz"), "Hz"),
                InputFieldConfig("volume", "Core Volume (m³)", "0.05", listOf("m³"), "m³")
            ),
            calculate = { vals, units ->
                val eta = vals["eta"] ?: 0.001
                var b = vals["bmax"] ?: 1.2
                if (units["bmax"] == "Gauss") b /= 10000.0
                var f = vals["freq"] ?: 50.0
                if (units["freq"] == "kHz") f *= 1000.0
                val v = vals["volume"] ?: 0.05
                val ph = eta * b.pow(1.6) * f * v
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(ph),
                    primaryUnit = "Watts (W)",
                    formulaUsed = "Ph = η × Bmax^1.6 × f × V",
                    steps = listOf(
                        CalculationStep(1, "Flux Density Term", "Bmax^1.6", "$b^1.6", ElectricalFormulas.fmt(b.pow(1.6), 4)),
                        CalculationStep(2, "Total Hysteresis Loss", "η × Bmax^1.6 × f × V", "$eta × ${ElectricalFormulas.fmt(b.pow(1.6), 3)} × $f × $v", "${ElectricalFormulas.fmt(ph)} W")
                    ),
                    notes = listOf("Steinmetz exponent of 1.6 applies for silicon iron sheet cores up to 1.5 Tesla.")
                )
            }
        ),

        // 23. Resistance Calculation
        CalculatorDefinition(
            id = 23,
            name = "Resistance Calculation",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.BASIC_ELEC,
            description = "Calculates electrical resistance of a conductor based on resistivity, length, and cross-sectional area.",
            formula = "R = ρ × (L / A)",
            inputs = listOf(
                InputFieldConfig("rho", "Resistivity ρ (Ω·m)", "0.0000000172", emptyList(), "", hint = "Copper: 1.72e-8, Al: 2.82e-8"),
                InputFieldConfig("length", "Length L", "100", listOf("Meters", "Feet"), "Meters"),
                InputFieldConfig("area", "Cross-Section Area A", "2.5", listOf("mm²", "cm²"), "mm²")
            ),
            calculate = { vals, units ->
                val rho = vals["rho"] ?: 1.72e-8
                var l = vals["length"] ?: 100.0
                if (units["length"] == "Feet") l *= 0.3048
                var a = vals["area"] ?: 2.5
                val aM2 = if (units["area"] == "cm²") a * 1e-4 else a * 1e-6
                val r = if (aM2 > 0) rho * (l / aM2) else 0.0
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(r, 4),
                    primaryUnit = "Ohms (Ω)",
                    formulaUsed = "R = ρ × (L / A)",
                    steps = listOf(
                        CalculationStep(1, "Area in m²", "A × 10^-6", "$a mm²", "$aM2 m²"),
                        CalculationStep(2, "Resistance", "ρ × (L / A)", "$rho × ($l / $aM2)", "${ElectricalFormulas.fmt(r, 4)} Ω")
                    )
                )
            }
        ),

        // 63. kW to HP
        CalculatorDefinition(
            id = 63,
            name = "kW to HP",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Converts active electric power in Kilowatts to Mechanical Horsepower (HP).",
            formula = "HP = kW / 0.745699872",
            inputs = listOf(InputFieldConfig("kw", "Power (kW)", "7.5", listOf("kW"), "kW")),
            calculate = { vals, _ ->
                val kw = vals["kw"] ?: 7.5
                val hp = kw / 0.745699872
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(hp),
                    primaryUnit = "HP (Horsepower)",
                    formulaUsed = "HP = kW / 0.7457",
                    steps = listOf(CalculationStep(1, "Conversion", "kW / 0.7457", "$kw / 0.7457", "${ElectricalFormulas.fmt(hp)} HP"))
                )
            }
        ),

        // 64. HP to kW
        CalculatorDefinition(
            id = 64,
            name = "HP to kW",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Converts Horsepower (HP) to electric power in Kilowatts (kW).",
            formula = "kW = HP × 0.745699872",
            inputs = listOf(InputFieldConfig("hp", "Power (HP)", "10", listOf("HP"), "HP")),
            calculate = { vals, _ ->
                val hp = vals["hp"] ?: 10.0
                val kw = hp * 0.745699872
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(kw),
                    primaryUnit = "kW (Kilowatts)",
                    formulaUsed = "kW = HP × 0.7457",
                    steps = listOf(CalculationStep(1, "Conversion", "HP × 0.7457", "$hp × 0.7457", "${ElectricalFormulas.fmt(kw)} kW"))
                )
            }
        ),

        // 65. kW to kWh
        CalculatorDefinition(
            id = 65,
            name = "kW to kWh",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates accumulated energy in Kilowatt-hours (kWh) from electric power and operating duration.",
            formula = "Energy (kWh) = Power (kW) × Time (Hours)",
            inputs = listOf(
                InputFieldConfig("kw", "Power (kW)", "5", listOf("kW"), "kW"),
                InputFieldConfig("hours", "Operating Time (Hours)", "8", listOf("Hours"), "Hours")
            ),
            calculate = { vals, _ ->
                val kw = vals["kw"] ?: 5.0
                val h = vals["hours"] ?: 8.0
                val kwh = kw * h
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(kwh),
                    primaryUnit = "kWh (Units)",
                    formulaUsed = "kWh = kW × Hours",
                    steps = listOf(CalculationStep(1, "Energy", "kW × Hours", "$kw × $h", "${ElectricalFormulas.fmt(kwh)} kWh"))
                )
            }
        ),

        // 66. kVA to kW
        CalculatorDefinition(
            id = 66,
            name = "kVA to kW",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Converts apparent power in kVA to active real power in kW using the load Power Factor.",
            formula = "kW = kVA × Power Factor (cos φ)",
            inputs = listOf(
                InputFieldConfig("kva", "Apparent Power (kVA)", "100", listOf("kVA"), "kVA"),
                InputFieldConfig("pf", "Power Factor (cos φ)", "0.8", emptyList(), "", hint = "0.1 to 1.0")
            ),
            calculate = { vals, _ ->
                val kva = vals["kva"] ?: 100.0
                val pf = (vals["pf"] ?: 0.8).coerceIn(0.1, 1.0)
                val kw = kva * pf
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(kw),
                    primaryUnit = "kW (Active Power)",
                    formulaUsed = "kW = kVA × PF",
                    steps = listOf(CalculationStep(1, "Active Power", "kVA × PF", "$kva × $pf", "${ElectricalFormulas.fmt(kw)} kW")),
                    secondaryResults = listOf("Reactive Power (kVAR)" to "${ElectricalFormulas.fmt(sqrt((kva * kva) - (kw * kw)))} kVAR")
                )
            }
        ),

        // 67. kVA to HP
        CalculatorDefinition(
            id = 67,
            name = "kVA to HP",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates mechanical output horsepower from electrical kVA rating and efficiency/PF.",
            formula = "HP = (kVA × PF) / 0.7457",
            inputs = listOf(
                InputFieldConfig("kva", "Apparent Power (kVA)", "50", listOf("kVA"), "kVA"),
                InputFieldConfig("pf", "Power Factor", "0.85", emptyList(), "", hint = "0.85 typical")
            ),
            calculate = { vals, _ ->
                val kva = vals["kva"] ?: 50.0
                val pf = (vals["pf"] ?: 0.85).coerceIn(0.1, 1.0)
                val hp = (kva * pf) / 0.7457
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(hp),
                    primaryUnit = "HP",
                    formulaUsed = "HP = (kVA × PF) / 0.7457",
                    steps = listOf(CalculationStep(1, "Result", "(kVA × PF) / 0.7457", "($kva × $pf) / 0.7457", "${ElectricalFormulas.fmt(hp)} HP"))
                )
            }
        ),

        // 68. kW/HP to kVA
        CalculatorDefinition(
            id = 68,
            name = "kW/HP to kVA",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates apparent power (kVA) from electric kW or motor mechanical HP rating.",
            formula = "kVA = kW / PF  or  kVA = (HP × 0.7457) / PF",
            inputs = listOf(
                InputFieldConfig("power", "Input Power", "30", listOf("kW", "HP"), "kW"),
                InputFieldConfig("pf", "Power Factor", "0.85", emptyList(), "")
            ),
            calculate = { vals, units ->
                var p = vals["power"] ?: 30.0
                if (units["power"] == "HP") p *= 0.7457
                val pf = (vals["pf"] ?: 0.85).coerceIn(0.1, 1.0)
                val kva = p / pf
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(kva),
                    primaryUnit = "kVA",
                    formulaUsed = "kVA = kW / PF",
                    steps = listOf(CalculationStep(1, "kVA calculation", "kW / PF", "$p / $pf", "${ElectricalFormulas.fmt(kva)} kVA"))
                )
            }
        ),

        // 69. kW to Volts
        CalculatorDefinition(
            id = 69,
            name = "kW to Volts",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates required circuit voltage from electric power, current draw, and power factor for 1-Phase or 3-Phase systems.",
            formula = "1φ: V = (kW × 1000) / (I × PF) | 3φ: V = (kW × 1000) / (√3 × I × PF)",
            supportsPhaseSelection = true,
            defaultPhase = "3-Phase",
            inputs = listOf(
                InputFieldConfig("kw", "Power (kW)", "11", listOf("kW"), "kW"),
                InputFieldConfig("current", "Current (Amperes)", "20", listOf("A"), "A"),
                InputFieldConfig("pf", "Power Factor", "0.85", emptyList(), "")
            ),
            calculate = { vals, units ->
                val kw = vals["kw"] ?: 11.0
                val i = (vals["current"] ?: 20.0).coerceAtLeast(0.1)
                val pf = (vals["pf"] ?: 0.85).coerceIn(0.1, 1.0)
                val is3p = (units["phase"] ?: units["is3Phase"] ?: "3-Phase") != "1-Phase"
                val v = if (is3p) (kw * 1000.0) / (sqrt(3.0) * i * pf) else (kw * 1000.0) / (i * pf)
                val form = if (is3p) "V = (kW × 1000) / (√3 × I × PF)" else "V = (kW × 1000) / (I × PF)"
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(v),
                    primaryUnit = "Volts (${if (is3p) "3-Phase Line-to-Line" else "Single Phase Line-to-Neutral"})",
                    formulaUsed = form,
                    steps = listOf(CalculationStep(1, "Voltage", form, "(${kw * 1000}) / (${if (is3p) "1.732 × " else ""}$i × $pf)", "${ElectricalFormulas.fmt(v)} V")),
                    notes = listOf("Strict Phase Isolation: Single Phase ও Three Phase লোড পৃথকভাবে হিসাব করা হয়েছে।")
                )
            }
        ),

        // 70. kW to Amps
        CalculatorDefinition(
            id = 70,
            name = "kW to Amps",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates current in Amperes for single-phase or three-phase systems.",
            formula = "1φ: I = (kW × 1000) / (V × PF) | 3φ: I = (kW × 1000) / (√3 × V × PF)",
            supportsPhaseSelection = true,
            defaultPhase = "3-Phase",
            inputs = listOf(
                InputFieldConfig("kw", "Power (kW)", "15", listOf("kW"), "kW"),
                InputFieldConfig("voltage", "Voltage (V)", "400", listOf("V"), "V"),
                InputFieldConfig("pf", "Power Factor", "0.85", emptyList(), "")
            ),
            calculate = { vals, units ->
                val kw = vals["kw"] ?: 15.0
                val v = (vals["voltage"] ?: 400.0).coerceAtLeast(1.0)
                val pf = (vals["pf"] ?: 0.85).coerceIn(0.1, 1.0)
                val is3p = (units["phase"] ?: units["is3Phase"] ?: "3-Phase") != "1-Phase"
                val i = if (is3p) (kw * 1000.0) / (sqrt(3.0) * v * pf) else (kw * 1000.0) / (v * pf)
                val form = if (is3p) "I = (kW × 1000) / (√3 × V × PF)" else "I = (kW × 1000) / (V × PF)"
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(i),
                    primaryUnit = "Amperes (${if (is3p) "3-Phase" else "1-Phase"})",
                    formulaUsed = form,
                    steps = listOf(CalculationStep(1, "Current", form, "${kw * 1000} / (${if (is3p) "1.732 × " else ""}$v × $pf)", "${ElectricalFormulas.fmt(i)} A")),
                    notes = listOf("Strict Phase Isolation: Single Phase ও Three Phase লোড পৃথকভাবে হিসাব করা হয়েছে।")
                )
            }
        ),

        // 71. kW to VA
        CalculatorDefinition(
            id = 71,
            name = "kW to VA",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates Volt-Amperes (VA) from real power in kW and power factor.",
            formula = "VA = (kW × 1000) / PF",
            inputs = listOf(
                InputFieldConfig("kw", "Power (kW)", "5", listOf("kW"), "kW"),
                InputFieldConfig("pf", "Power Factor", "0.8", emptyList(), "")
            ),
            calculate = { vals, _ ->
                val kw = vals["kw"] ?: 5.0
                val pf = (vals["pf"] ?: 0.8).coerceIn(0.1, 1.0)
                val va = (kw * 1000.0) / pf
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(va),
                    primaryUnit = "Volt-Amperes (VA)",
                    formulaUsed = "VA = (kW × 1000) / PF",
                    steps = listOf(CalculationStep(1, "Result", "(5 × 1000) / 0.8", "${kw * 1000} / $pf", "${ElectricalFormulas.fmt(va)} VA"))
                )
            }
        ),

        // 72. kWh to kW
        CalculatorDefinition(
            id = 72,
            name = "kWh to kW",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Computes average electrical power demand in kW from total energy in kWh and elapsed hours.",
            formula = "kW = kWh / Hours",
            inputs = listOf(
                InputFieldConfig("kwh", "Energy (kWh)", "120", listOf("kWh"), "kWh"),
                InputFieldConfig("hours", "Time (Hours)", "24", listOf("Hours"), "Hours")
            ),
            calculate = { vals, _ ->
                val kwh = vals["kwh"] ?: 120.0
                val h = (vals["hours"] ?: 24.0).coerceAtLeast(0.01)
                val kw = kwh / h
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(kw),
                    primaryUnit = "kW (Power)",
                    formulaUsed = "kW = kWh / Hours",
                    steps = listOf(CalculationStep(1, "Power", "kWh / Hours", "$kwh / $h", "${ElectricalFormulas.fmt(kw)} kW"))
                )
            }
        ),

        // 73. kWh to Watts
        CalculatorDefinition(
            id = 73,
            name = "kWh to Watts",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates average power in Watts from total energy in kilowatt-hours and time.",
            formula = "Watts = (kWh × 1000) / Hours",
            inputs = listOf(
                InputFieldConfig("kwh", "Energy (kWh)", "15", listOf("kWh"), "kWh"),
                InputFieldConfig("hours", "Duration (Hours)", "10", listOf("Hours"), "Hours")
            ),
            calculate = { vals, _ ->
                val kwh = vals["kwh"] ?: 15.0
                val h = (vals["hours"] ?: 10.0).coerceAtLeast(0.01)
                val w = (kwh * 1000.0) / h
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(w),
                    primaryUnit = "Watts (W)",
                    formulaUsed = "Watts = (kWh × 1000) / Hours",
                    steps = listOf(CalculationStep(1, "Result", "(kWh × 1000) / Hours", "(${kwh * 1000}) / $h", "${ElectricalFormulas.fmt(w)} W"))
                )
            }
        ),

        // 74. kVA to Amps
        CalculatorDefinition(
            id = 74,
            name = "kVA to Amps",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates full load current in Amperes from apparent power (kVA) and voltage rating.",
            formula = "3φ: I = (kVA × 1000) / (√3 × V)  |  1φ: I = (kVA × 1000) / V",
            supportsPhaseSelection = true,
            defaultPhase = "3-Phase",
            inputs = listOf(
                InputFieldConfig("kva", "Apparent Power (kVA)", "250", listOf("kVA"), "kVA"),
                InputFieldConfig("voltage", "Voltage (V)", "400", listOf("V"), "V")
            ),
            calculate = { vals, units ->
                val kva = vals["kva"] ?: 250.0
                val v = (vals["voltage"] ?: 400.0).coerceAtLeast(1.0)
                val is3p = (units["phase"] ?: units["is3Phase"] ?: "3-Phase") != "1-Phase"
                val i = if (is3p) (kva * 1000.0) / (sqrt(3.0) * v) else (kva * 1000.0) / v
                val form = if (is3p) "I = (kVA × 1000) / (√3 × V)" else "I = (kVA × 1000) / V"
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(i),
                    primaryUnit = "Amperes (${if (is3p) "3-Phase" else "1-Phase"})",
                    formulaUsed = form,
                    steps = listOf(CalculationStep(1, "Current", form, "${kva * 1000} / ${if (is3p) "1.732 × $v" else "$v"}", "${ElectricalFormulas.fmt(i)} A")),
                    notes = listOf("Strict Phase Isolation: Single Phase ও Three Phase লোড পৃথকভাবে হিসাব করা হয়েছে।")
                )
            }
        ),

        // 75. kVA to VA
        CalculatorDefinition(
            id = 75,
            name = "kVA to VA",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Simple conversion of kilovolt-amperes to volt-amperes.",
            formula = "VA = kVA × 1000",
            inputs = listOf(InputFieldConfig("kva", "Power (kVA)", "75", listOf("kVA"), "kVA")),
            calculate = { vals, _ ->
                val kva = vals["kva"] ?: 75.0
                val va = kva * 1000.0
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(va),
                    primaryUnit = "VA",
                    formulaUsed = "VA = kVA × 1000",
                    steps = listOf(CalculationStep(1, "Calculation", "kVA × 1000", "$kva × 1000", "${ElectricalFormulas.fmt(va)} VA"))
                )
            }
        ),

        // 76. Watts to Amps
        CalculatorDefinition(
            id = 76,
            name = "Watts to Amps",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates electric current in Amperes from active power in Watts for Single Phase or Three Phase.",
            formula = "1φ: I = Watts / (V × PF) | 3φ: I = Watts / (√3 × V × PF)",
            supportsPhaseSelection = true,
            defaultPhase = "1-Phase",
            inputs = listOf(
                InputFieldConfig("watts", "Power (Watts)", "2200", listOf("W"), "W"),
                InputFieldConfig("voltage", "Voltage (V)", "230", listOf("V"), "V"),
                InputFieldConfig("pf", "Power Factor", "0.95", emptyList(), "")
            ),
            calculate = { vals, units ->
                val w = vals["watts"] ?: 2200.0
                val v = (vals["voltage"] ?: 230.0).coerceAtLeast(1.0)
                val pf = (vals["pf"] ?: 0.95).coerceIn(0.1, 1.0)
                val is3p = (units["phase"] ?: units["is3Phase"] ?: "1-Phase") != "1-Phase"
                val i = if (is3p) w / (sqrt(3.0) * v * pf) else w / (v * pf)
                val form = if (is3p) "I = Watts / (√3 × V × PF)" else "I = Watts / (V × PF)"
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(i),
                    primaryUnit = "Amperes (${if (is3p) "3-Phase" else "1-Phase"})",
                    formulaUsed = form,
                    steps = listOf(CalculationStep(1, "Current", form, "$w / (${if (is3p) "1.732 × " else ""}$v × $pf)", "${ElectricalFormulas.fmt(i)} A")),
                    notes = listOf("Strict Phase Isolation: Single Phase ও Three Phase লোড পৃথকভাবে হিসাব করা হয়েছে।")
                )
            }
        ),

        // 77. Watts to Volts
        CalculatorDefinition(
            id = 77,
            name = "Watts to Volts",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Computes potential difference in Volts from power, current and power factor.",
            formula = "V = Watts / (Amps × Power Factor)",
            inputs = listOf(
                InputFieldConfig("watts", "Power (Watts)", "1500", listOf("W"), "W"),
                InputFieldConfig("amps", "Current (Amps)", "6.5", listOf("A"), "A"),
                InputFieldConfig("pf", "Power Factor", "1.0", emptyList(), "")
            ),
            calculate = { vals, _ ->
                val w = vals["watts"] ?: 1500.0
                val a = (vals["amps"] ?: 6.5).coerceAtLeast(0.01)
                val pf = (vals["pf"] ?: 1.0).coerceIn(0.1, 1.0)
                val v = w / (a * pf)
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(v),
                    primaryUnit = "Volts (V)",
                    formulaUsed = "V = W / (I × PF)",
                    steps = listOf(CalculationStep(1, "Voltage", "W / (I × PF)", "$w / ($a × $pf)", "${ElectricalFormulas.fmt(v)} V"))
                )
            }
        ),

        // 78. Watts to kWh
        CalculatorDefinition(
            id = 78,
            name = "Watts to kWh",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates energy consumed in kilowatt-hours from device wattage and run time.",
            formula = "kWh = (Watts × Hours) / 1000",
            inputs = listOf(
                InputFieldConfig("watts", "Power (Watts)", "800", listOf("W"), "W"),
                InputFieldConfig("hours", "Duration (Hours)", "5", listOf("Hours"), "Hours")
            ),
            calculate = { vals, _ ->
                val w = vals["watts"] ?: 800.0
                val h = vals["hours"] ?: 5.0
                val kwh = (w * h) / 1000.0
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(kwh, 3),
                    primaryUnit = "kWh",
                    formulaUsed = "kWh = (W × h) / 1000",
                    steps = listOf(CalculationStep(1, "Energy", "(W × Hours) / 1000", "($w × $h) / 1000", "${ElectricalFormulas.fmt(kwh, 3)} kWh"))
                )
            }
        ),

        // 79. Watts to VA
        CalculatorDefinition(
            id = 79,
            name = "Watts to VA",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates apparent power in Volt-Amperes from real power in Watts.",
            formula = "VA = Watts / Power Factor",
            inputs = listOf(
                InputFieldConfig("watts", "Active Power (Watts)", "1200", listOf("W"), "W"),
                InputFieldConfig("pf", "Power Factor", "0.85", emptyList(), "")
            ),
            calculate = { vals, _ ->
                val w = vals["watts"] ?: 1200.0
                val pf = (vals["pf"] ?: 0.85).coerceIn(0.1, 1.0)
                val va = w / pf
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(va),
                    primaryUnit = "VA",
                    formulaUsed = "VA = W / PF",
                    steps = listOf(CalculationStep(1, "Apparent Power", "W / PF", "$w / $pf", "${ElectricalFormulas.fmt(va)} VA"))
                )
            }
        ),

        // 80. W-V-A Calculator
        CalculatorDefinition(
            id = 80,
            name = "W-V-A Calculator",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.BASIC_ELEC,
            description = "Comprehensive multi-variable solver for Watts, Volts, Amperes, and Power Factor.",
            formula = "1φ: P = V × I × PF | 3φ: P = √3 × V × I × PF",
            supportsPhaseSelection = true,
            defaultPhase = "1-Phase",
            inputs = listOf(
                InputFieldConfig("v", "Voltage (V)", "230", listOf("V"), "V"),
                InputFieldConfig("i", "Current (A)", "10", listOf("A"), "A"),
                InputFieldConfig("pf", "Power Factor", "0.9", emptyList(), "")
            ),
            calculate = { vals, units ->
                val v = vals["v"] ?: 230.0
                val i = vals["i"] ?: 10.0
                val pf = (vals["pf"] ?: 0.9).coerceIn(0.1, 1.0)
                val is3p = (units["phase"] ?: units["is3Phase"] ?: "1-Phase") != "1-Phase"
                val w = if (is3p) sqrt(3.0) * v * i * pf else v * i * pf
                val va = if (is3p) sqrt(3.0) * v * i else v * i
                val form = if (is3p) "P = √3 × V × I × PF" else "P = V × I × PF"
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(w),
                    primaryUnit = "Watts (${if (is3p) "3-Phase" else "1-Phase"})",
                    formulaUsed = form,
                    steps = listOf(CalculationStep(1, "Power", form, "${if (is3p) "1.732 × " else ""}$v × $i × $pf", "${ElectricalFormulas.fmt(w)} W")),
                    secondaryResults = listOf(
                        "Apparent Power" to "${ElectricalFormulas.fmt(va)} VA",
                        "Apparent Power (kVA)" to "${ElectricalFormulas.fmt(va / 1000.0)} kVA",
                        "Active Power (kW)" to "${ElectricalFormulas.fmt(w / 1000.0)} kW"
                    ),
                    notes = listOf("Strict Phase Isolation: Single Phase ও Three Phase লোড পৃথকভাবে হিসাব করা হয়েছে।")
                )
            }
        ),

        // 81. Amps to Watts
        CalculatorDefinition(
            id = 81,
            name = "Amps to Watts",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Converts current in Amperes to active power in Watts for 1-Phase or 3-Phase.",
            formula = "1φ: W = V × I × PF | 3φ: W = √3 × V × I × PF",
            supportsPhaseSelection = true,
            defaultPhase = "1-Phase",
            inputs = listOf(
                InputFieldConfig("amps", "Current (A)", "16", listOf("A"), "A"),
                InputFieldConfig("voltage", "Voltage (V)", "230", listOf("V"), "V"),
                InputFieldConfig("pf", "Power Factor", "0.9", emptyList(), "")
            ),
            calculate = { vals, units ->
                val a = vals["amps"] ?: 16.0
                val v = vals["voltage"] ?: 230.0
                val pf = (vals["pf"] ?: 0.9).coerceIn(0.1, 1.0)
                val is3p = (units["phase"] ?: units["is3Phase"] ?: "1-Phase") != "1-Phase"
                val w = if (is3p) sqrt(3.0) * v * a * pf else v * a * pf
                val form = if (is3p) "W = √3 × V × I × PF" else "W = V × I × PF"
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(w),
                    primaryUnit = "Watts (${if (is3p) "3-Phase" else "1-Phase"})",
                    formulaUsed = form,
                    steps = listOf(CalculationStep(1, "Power", form, "${if (is3p) "1.732 × " else ""}$v × $a × $pf", "${ElectricalFormulas.fmt(w)} W")),
                    notes = listOf("Strict Phase Isolation: Single Phase ও Three Phase লোড পৃথকভাবে হিসাব করা হয়েছে।")
                )
            }
        ),

        // 82. Amps to kW
        CalculatorDefinition(
            id = 82,
            name = "Amps to kW",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates power in kilowatts from current in Amperes and operating voltage.",
            formula = "1φ: kW = (V × I × PF) / 1000 | 3φ: kW = (√3 × V × I × PF) / 1000",
            supportsPhaseSelection = true,
            defaultPhase = "3-Phase",
            inputs = listOf(
                InputFieldConfig("amps", "Current (A)", "40", listOf("A"), "A"),
                InputFieldConfig("voltage", "Voltage (V)", "400", listOf("V"), "V"),
                InputFieldConfig("pf", "Power Factor", "0.85", emptyList(), "")
            ),
            calculate = { vals, units ->
                val a = vals["amps"] ?: 40.0
                val v = vals["voltage"] ?: 400.0
                val pf = (vals["pf"] ?: 0.85).coerceIn(0.1, 1.0)
                val is3p = (units["phase"] ?: units["is3Phase"] ?: "3-Phase") != "1-Phase"
                val kw = if (is3p) (sqrt(3.0) * v * a * pf) / 1000.0 else (v * a * pf) / 1000.0
                val form = if (is3p) "kW = (√3 × V × I × PF) / 1000" else "kW = (V × I × PF) / 1000"
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(kw),
                    primaryUnit = "kW (${if (is3p) "3-Phase" else "1-Phase"})",
                    formulaUsed = form,
                    steps = listOf(CalculationStep(1, "Power", form, "${if (is3p) "1.732 × $v × $a × $pf" else "$v × $a × $pf"} / 1000", "${ElectricalFormulas.fmt(kw)} kW")),
                    notes = listOf("Strict Phase Isolation: Single Phase ও Three Phase লোড পৃথকভাবে হিসাব করা হয়েছে।")
                )
            }
        ),

        // 83. Amps to kVA
        CalculatorDefinition(
            id = 83,
            name = "Amps to kVA",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Computes apparent power in kVA from line current and voltage.",
            formula = "3φ: kVA = (√3 × V × I) / 1000 | 1φ: kVA = (V × I) / 1000",
            supportsPhaseSelection = true,
            defaultPhase = "3-Phase",
            inputs = listOf(
                InputFieldConfig("amps", "Current (A)", "144", listOf("A"), "A"),
                InputFieldConfig("voltage", "Voltage (V)", "400", listOf("V"), "V")
            ),
            calculate = { vals, units ->
                val a = vals["amps"] ?: 144.0
                val v = vals["voltage"] ?: 400.0
                val is3p = (units["phase"] ?: units["is3Phase"] ?: "3-Phase") != "1-Phase"
                val kva = if (is3p) (sqrt(3.0) * v * a) / 1000.0 else (v * a) / 1000.0
                val form = if (is3p) "S = (√3 × V × I) / 1000" else "S = (V × I) / 1000"
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(kva),
                    primaryUnit = "kVA (${if (is3p) "3-Phase" else "1-Phase"})",
                    formulaUsed = form,
                    steps = listOf(CalculationStep(1, "Apparent Power", form, "${if (is3p) "1.732 × $v × $a" else "$v × $a"} / 1000", "${ElectricalFormulas.fmt(kva)} kVA")),
                    notes = listOf("Strict Phase Isolation: Single Phase ও Three Phase লোড পৃথকভাবে হিসাব করা হয়েছে।")
                )
            }
        ),

        // 84. Amps to VA
        CalculatorDefinition(
            id = 84,
            name = "Amps to VA",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Converts current in Amperes and voltage in Volts to Volt-Amperes.",
            formula = "1φ: VA = V × I | 3φ: VA = √3 × V × I",
            supportsPhaseSelection = true,
            defaultPhase = "1-Phase",
            inputs = listOf(
                InputFieldConfig("amps", "Current (A)", "10", listOf("A"), "A"),
                InputFieldConfig("voltage", "Voltage (V)", "230", listOf("V"), "V")
            ),
            calculate = { vals, units ->
                val a = vals["amps"] ?: 10.0
                val v = vals["voltage"] ?: 230.0
                val is3p = (units["phase"] ?: units["is3Phase"] ?: "1-Phase") != "1-Phase"
                val va = if (is3p) sqrt(3.0) * v * a else v * a
                val form = if (is3p) "VA = √3 × V × I" else "VA = V × I"
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(va),
                    primaryUnit = "VA (${if (is3p) "3-Phase" else "1-Phase"})",
                    formulaUsed = form,
                    steps = listOf(CalculationStep(1, "VA", form, "${if (is3p) "1.732 × " else ""}$v × $a", "${ElectricalFormulas.fmt(va)} VA")),
                    notes = listOf("Strict Phase Isolation: Single Phase ও Three Phase লোড পৃথকভাবে হিসাব করা হয়েছে।")
                )
            }
        ),

        // 85. Amps to Volts
        CalculatorDefinition(
            id = 85,
            name = "Amps to Volts",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates voltage from current and resistance (Ohm's Law).",
            formula = "V = I × R",
            inputs = listOf(
                InputFieldConfig("amps", "Current (A)", "5", listOf("A"), "A"),
                InputFieldConfig("ohms", "Resistance (Ω)", "46", listOf("Ω"), "Ω")
            ),
            calculate = { vals, _ ->
                val a = vals["amps"] ?: 5.0
                val r = vals["ohms"] ?: 46.0
                val v = a * r
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(v),
                    primaryUnit = "Volts (V)",
                    formulaUsed = "V = I × R",
                    steps = listOf(CalculationStep(1, "Voltage", "I × R", "$a × $r", "${ElectricalFormulas.fmt(v)} V"))
                )
            }
        ),

        // 86. Volts to Amps
        CalculatorDefinition(
            id = 86,
            name = "Volts to Amps",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates current flowing through a circuit given voltage and resistance.",
            formula = "I = V / R",
            inputs = listOf(
                InputFieldConfig("volts", "Voltage (V)", "230", listOf("V"), "V"),
                InputFieldConfig("ohms", "Resistance (Ω)", "23", listOf("Ω"), "Ω")
            ),
            calculate = { vals, _ ->
                val v = vals["volts"] ?: 230.0
                val r = (vals["ohms"] ?: 23.0).coerceAtLeast(0.001)
                val i = v / r
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(i),
                    primaryUnit = "Amperes (A)",
                    formulaUsed = "I = V / R",
                    steps = listOf(CalculationStep(1, "Current", "V / R", "$v / $r", "${ElectricalFormulas.fmt(i)} A"))
                )
            }
        ),

        // 87. Volts to Watts
        CalculatorDefinition(
            id = 87,
            name = "Volts to Watts",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Computes active power dissipated from voltage and circuit resistance or current.",
            formula = "P = V² / R",
            inputs = listOf(
                InputFieldConfig("volts", "Voltage (V)", "230", listOf("V"), "V"),
                InputFieldConfig("ohms", "Resistance (Ω)", "50", listOf("Ω"), "Ω")
            ),
            calculate = { vals, _ ->
                val v = vals["volts"] ?: 230.0
                val r = (vals["ohms"] ?: 50.0).coerceAtLeast(0.01)
                val w = (v * v) / r
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(w),
                    primaryUnit = "Watts (W)",
                    formulaUsed = "P = V² / R",
                    steps = listOf(CalculationStep(1, "Power", "V² / R", "$v² / $r", "${ElectricalFormulas.fmt(w)} W"))
                )
            }
        ),

        // 88. Volts to kW
        CalculatorDefinition(
            id = 88,
            name = "Volts to kW",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates electric power in kW from voltage, current, and power factor for 1-Phase or 3-Phase.",
            formula = "1φ: kW = (V × I × PF) / 1000 | 3φ: kW = (√3 × V × I × PF) / 1000",
            supportsPhaseSelection = true,
            defaultPhase = "3-Phase",
            inputs = listOf(
                InputFieldConfig("volts", "Voltage (V)", "400", listOf("V"), "V"),
                InputFieldConfig("current", "Current (A)", "25", listOf("A"), "A"),
                InputFieldConfig("pf", "Power Factor", "0.9", emptyList(), "")
            ),
            calculate = { vals, units ->
                val v = vals["volts"] ?: 400.0
                val i = vals["current"] ?: 25.0
                val pf = (vals["pf"] ?: 0.9).coerceIn(0.1, 1.0)
                val is3p = (units["phase"] ?: units["is3Phase"] ?: "3-Phase") != "1-Phase"
                val kw = if (is3p) (sqrt(3.0) * v * i * pf) / 1000.0 else (v * i * pf) / 1000.0
                val form = if (is3p) "kW = (√3 × V × I × PF) / 1000" else "kW = (V × I × PF) / 1000"
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(kw),
                    primaryUnit = "kW (${if (is3p) "3-Phase" else "1-Phase"})",
                    formulaUsed = form,
                    steps = listOf(CalculationStep(1, "Power", form, "(${if (is3p) "1.732 × " else ""}$v × $i × $pf) / 1000", "${ElectricalFormulas.fmt(kw)} kW")),
                    notes = listOf("Strict Phase Isolation: Single Phase ও Three Phase লোড পৃথকভাবে হিসাব করা হয়েছে।")
                )
            }
        ),

        // 89. Volts to Joules
        CalculatorDefinition(
            id = 89,
            name = "Volts to Joules",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates energy in Joules from voltage and electric charge in Coulombs.",
            formula = "E (Joules) = V (Volts) × Q (Coulombs)",
            inputs = listOf(
                InputFieldConfig("volts", "Voltage (V)", "12", listOf("V"), "V"),
                InputFieldConfig("coulombs", "Electric Charge Q (Coulombs)", "500", listOf("Coulombs"), "Coulombs")
            ),
            calculate = { vals, _ ->
                val v = vals["volts"] ?: 12.0
                val q = vals["coulombs"] ?: 500.0
                val j = v * q
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(j),
                    primaryUnit = "Joules (J)",
                    formulaUsed = "E = V × Q",
                    steps = listOf(CalculationStep(1, "Energy", "V × Q", "$v × $q", "${ElectricalFormulas.fmt(j)} J")),
                    secondaryResults = listOf("Energy in Watt-hours" to "${ElectricalFormulas.fmt(j / 3600.0, 4)} Wh")
                )
            }
        ),

        // 90. Volts to eV
        CalculatorDefinition(
            id = 90,
            name = "Volts to eV",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates electron-volts (eV) gained by an elementary charge through electric potential.",
            formula = "E (eV) = q × V  where q = 1 elementary charge (e)",
            inputs = listOf(InputFieldConfig("volts", "Potential (Volts)", "1000", listOf("V", "kV"), "V")),
            calculate = { vals, units ->
                var v = vals["volts"] ?: 1000.0
                if (units["volts"] == "kV") v *= 1000.0
                val ev = v // for 1 electron
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(ev),
                    primaryUnit = "eV (Electron-Volts)",
                    formulaUsed = "E = e × V",
                    steps = listOf(CalculationStep(1, "Energy", "1e × V", "1 × $v", "${ElectricalFormulas.fmt(ev)} eV")),
                    secondaryResults = listOf("Energy in Joules" to "${ElectricalFormulas.fmt(v * 1.602176634e-19, 6)} × 10^-19 J")
                )
            }
        ),

        // 91. VA to Amps
        CalculatorDefinition(
            id = 91,
            name = "VA to Amps",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates line current in Amperes from apparent power in Volt-Amperes.",
            formula = "I = VA / V",
            inputs = listOf(
                InputFieldConfig("va", "Apparent Power (VA)", "2300", listOf("VA"), "VA"),
                InputFieldConfig("volts", "Voltage (V)", "230", listOf("V"), "V")
            ),
            calculate = { vals, _ ->
                val va = vals["va"] ?: 2300.0
                val v = (vals["volts"] ?: 230.0).coerceAtLeast(1.0)
                val i = va / v
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(i),
                    primaryUnit = "Amperes (A)",
                    formulaUsed = "I = VA / V",
                    steps = listOf(CalculationStep(1, "Current", "VA / V", "$va / $v", "${ElectricalFormulas.fmt(i)} A"))
                )
            }
        ),

        // 92. VA to Watts
        CalculatorDefinition(
            id = 92,
            name = "VA to Watts",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Converts apparent power (VA) to active real power (Watts).",
            formula = "Watts = VA × Power Factor",
            inputs = listOf(
                InputFieldConfig("va", "Apparent Power (VA)", "1000", listOf("VA"), "VA"),
                InputFieldConfig("pf", "Power Factor", "0.85", emptyList(), "")
            ),
            calculate = { vals, _ ->
                val va = vals["va"] ?: 1000.0
                val pf = (vals["pf"] ?: 0.85).coerceIn(0.1, 1.0)
                val w = va * pf
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(w),
                    primaryUnit = "Watts (W)",
                    formulaUsed = "W = VA × PF",
                    steps = listOf(CalculationStep(1, "Watts", "VA × PF", "$va × $pf", "${ElectricalFormulas.fmt(w)} W"))
                )
            }
        ),

        // 93. VA to kW
        CalculatorDefinition(
            id = 93,
            name = "VA to kW",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Converts Volt-Amperes directly to Kilowatts.",
            formula = "kW = (VA × Power Factor) / 1000",
            inputs = listOf(
                InputFieldConfig("va", "Apparent Power (VA)", "5000", listOf("VA"), "VA"),
                InputFieldConfig("pf", "Power Factor", "0.8", emptyList(), "")
            ),
            calculate = { vals, _ ->
                val va = vals["va"] ?: 5000.0
                val pf = (vals["pf"] ?: 0.8).coerceIn(0.1, 1.0)
                val kw = (va * pf) / 1000.0
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(kw),
                    primaryUnit = "kW",
                    formulaUsed = "kW = (VA × PF) / 1000",
                    steps = listOf(CalculationStep(1, "Power", "(VA × PF) / 1000", "($va × $pf) / 1000", "${ElectricalFormulas.fmt(kw)} kW"))
                )
            }
        ),

        // 94. VA to kVA
        CalculatorDefinition(
            id = 94,
            name = "VA to kVA",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Divides Volt-Amperes by 1000 to obtain Kilovolt-Amperes.",
            formula = "kVA = VA / 1000",
            inputs = listOf(InputFieldConfig("va", "Power (VA)", "12500", listOf("VA"), "VA")),
            calculate = { vals, _ ->
                val va = vals["va"] ?: 12500.0
                val kva = va / 1000.0
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(kva),
                    primaryUnit = "kVA",
                    formulaUsed = "kVA = VA / 1000",
                    steps = listOf(CalculationStep(1, "Calculation", "VA / 1000", "$va / 1000", "${ElectricalFormulas.fmt(kva)} kVA"))
                )
            }
        ),

        // 95. eV to Volts
        CalculatorDefinition(
            id = 95,
            name = "eV to Volts",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates electric potential corresponding to an energy of electron-volts.",
            formula = "V = E (eV) / e",
            inputs = listOf(InputFieldConfig("ev", "Energy (eV)", "500", listOf("eV"), "eV")),
            calculate = { vals, _ ->
                val ev = vals["ev"] ?: 500.0
                val v = ev
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(v),
                    primaryUnit = "Volts (V)",
                    formulaUsed = "V = eV / 1e",
                    steps = listOf(CalculationStep(1, "Result", "eV / 1e", "$ev / 1", "${ElectricalFormulas.fmt(v)} V"))
                )
            }
        ),

        // 96. Joules to Watts
        CalculatorDefinition(
            id = 96,
            name = "Joules to Watts",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Computes rate of energy transfer (Power in Watts) from total Joules and seconds.",
            formula = "P (Watts) = Energy (Joules) / Time (Seconds)",
            inputs = listOf(
                InputFieldConfig("joules", "Energy (Joules)", "36000", listOf("Joules"), "Joules"),
                InputFieldConfig("seconds", "Time (Seconds)", "60", listOf("Seconds"), "Seconds")
            ),
            calculate = { vals, _ ->
                val j = vals["joules"] ?: 36000.0
                val s = (vals["seconds"] ?: 60.0).coerceAtLeast(0.001)
                val w = j / s
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(w),
                    primaryUnit = "Watts (W)",
                    formulaUsed = "P = J / s",
                    steps = listOf(CalculationStep(1, "Power", "Joules / Seconds", "$j / $s", "${ElectricalFormulas.fmt(w)} W"))
                )
            }
        ),

        // 97. Joules to Volts
        CalculatorDefinition(
            id = 97,
            name = "Joules to Volts",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Calculates electric potential from energy in Joules and charge in Coulombs.",
            formula = "V = Energy (Joules) / Charge (Coulombs)",
            inputs = listOf(
                InputFieldConfig("joules", "Energy (Joules)", "1200", listOf("Joules"), "Joules"),
                InputFieldConfig("coulombs", "Charge (Coulombs)", "100", listOf("Coulombs"), "Coulombs")
            ),
            calculate = { vals, _ ->
                val j = vals["joules"] ?: 1200.0
                val q = (vals["coulombs"] ?: 100.0).coerceAtLeast(0.001)
                val v = j / q
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(v),
                    primaryUnit = "Volts (V)",
                    formulaUsed = "V = J / Q",
                    steps = listOf(CalculationStep(1, "Voltage", "Joules / Coulombs", "$j / $q", "${ElectricalFormulas.fmt(v)} V"))
                )
            }
        ),

        // 98. Voltage Calculation
        CalculatorDefinition(
            id = 98,
            name = "Voltage Calculation",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.BASIC_ELEC,
            description = "Determines voltage across a component using Ohm's Law and Power equations.",
            formula = "V = I × R  or  V = P / I",
            inputs = listOf(
                InputFieldConfig("current", "Current (A)", "12", listOf("A"), "A"),
                InputFieldConfig("resistance", "Resistance (Ω)", "19.16", listOf("Ω"), "Ω")
            ),
            calculate = { vals, _ ->
                val i = vals["current"] ?: 12.0
                val r = vals["resistance"] ?: 19.16
                val v = i * r
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(v),
                    primaryUnit = "Volts (V)",
                    formulaUsed = "V = I × R",
                    steps = listOf(CalculationStep(1, "Voltage", "I × R", "$i × $r", "${ElectricalFormulas.fmt(v)} V")),
                    secondaryResults = listOf("Power Dissipated" to "${ElectricalFormulas.fmt(v * i)} Watts")
                )
            }
        ),

        // 99. Current Calculation
        CalculatorDefinition(
            id = 99,
            name = "Current Calculation",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.BASIC_ELEC,
            description = "Calculates electric current flowing through a circuit path.",
            formula = "I = V / R  or  I = P / V",
            inputs = listOf(
                InputFieldConfig("voltage", "Voltage (V)", "230", listOf("V"), "V"),
                InputFieldConfig("resistance", "Resistance (Ω)", "15", listOf("Ω"), "Ω")
            ),
            calculate = { vals, _ ->
                val v = vals["voltage"] ?: 230.0
                val r = (vals["resistance"] ?: 15.0).coerceAtLeast(0.01)
                val i = v / r
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(i),
                    primaryUnit = "Amperes (A)",
                    formulaUsed = "I = V / R",
                    steps = listOf(CalculationStep(1, "Current", "V / R", "$v / $r", "${ElectricalFormulas.fmt(i)} A"))
                )
            }
        ),

        // 100. Frequency Calculation
        CalculatorDefinition(
            id = 100,
            name = "Frequency Calculation",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.BASIC_ELEC,
            description = "Calculates AC electrical frequency from generator speed (RPM) and number of poles, or waveform period.",
            formula = "f = (P × N) / 120  or  f = 1 / T",
            inputs = listOf(
                InputFieldConfig("poles", "Number of Machine Poles (P)", "4", listOf("Poles"), "Poles"),
                InputFieldConfig("rpm", "Rotational Speed N (RPM)", "1500", listOf("RPM"), "RPM")
            ),
            calculate = { vals, _ ->
                val p = (vals["poles"] ?: 4.0).coerceAtLeast(2.0)
                val n = vals["rpm"] ?: 1500.0
                val f = (p * n) / 120.0
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(f),
                    primaryUnit = "Hertz (Hz)",
                    formulaUsed = "f = (P × N) / 120",
                    steps = listOf(CalculationStep(1, "Frequency", "(P × N) / 120", "($p × $n) / 120", "${ElectricalFormulas.fmt(f)} Hz")),
                    secondaryResults = listOf(
                        "Angular Frequency (ω)" to "${ElectricalFormulas.fmt(2 * PI * f)} rad/s",
                        "Wave Period (T)" to "${ElectricalFormulas.fmt(1000.0 / f, 3)} ms"
                    )
                )
            }
        ),

        // 101. Active Power Calculation
        CalculatorDefinition(
            id = 101,
            name = "Active Power Calculation",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.BASIC_ELEC,
            description = "Calculates true active power (kW) consumed by single or three phase AC loads.",
            formula = "1φ: P = V × I × cos φ | 3φ: P = √3 × V × I × cos φ",
            supportsPhaseSelection = true,
            defaultPhase = "3-Phase",
            inputs = listOf(
                InputFieldConfig("voltage", "Voltage (V)", "400", listOf("V"), "V"),
                InputFieldConfig("current", "Line Current (A)", "65", listOf("A"), "A"),
                InputFieldConfig("pf", "Power Factor (cos φ)", "0.85", emptyList(), "")
            ),
            calculate = { vals, units ->
                val v = vals["voltage"] ?: 400.0
                val i = vals["current"] ?: 65.0
                val pf = (vals["pf"] ?: 0.85).coerceIn(0.1, 1.0)
                val is3p = (units["phase"] ?: units["is3Phase"] ?: "3-Phase") != "1-Phase"
                val pWatts = if (is3p) sqrt(3.0) * v * i * pf else v * i * pf
                val form = if (is3p) "P = √3 × V × I × cos φ" else "P = V × I × cos φ"
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(pWatts / 1000.0),
                    primaryUnit = "kW (${if (is3p) "3-Phase" else "1-Phase"})",
                    formulaUsed = form,
                    steps = listOf(CalculationStep(1, "Active Power", form, "${if (is3p) "1.732 × " else ""}$v × $i × $pf", "${ElectricalFormulas.fmt(pWatts)} W")),
                    secondaryResults = listOf("Active Power in Watts" to "${ElectricalFormulas.fmt(pWatts)} W"),
                    notes = listOf("Strict Phase Isolation: Single Phase ও Three Phase লোড পৃথকভাবে হিসাব করা হয়েছে।")
                )
            }
        ),

        // 102. Apparent Power Calculation
        CalculatorDefinition(
            id = 102,
            name = "Apparent Power Calculation",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.BASIC_ELEC,
            description = "Calculates total apparent power (kVA) supplied to an AC electrical system.",
            formula = "1φ: S = V × I | 3φ: S = √3 × V × I",
            supportsPhaseSelection = true,
            defaultPhase = "3-Phase",
            inputs = listOf(
                InputFieldConfig("voltage", "Voltage (V)", "400", listOf("V"), "V"),
                InputFieldConfig("current", "Current (A)", "120", listOf("A"), "A")
            ),
            calculate = { vals, units ->
                val v = vals["voltage"] ?: 400.0
                val i = vals["current"] ?: 120.0
                val is3p = (units["phase"] ?: units["is3Phase"] ?: "3-Phase") != "1-Phase"
                val sVa = if (is3p) sqrt(3.0) * v * i else v * i
                val form = if (is3p) "S = √3 × V × I" else "S = V × I"
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(sVa / 1000.0),
                    primaryUnit = "kVA (${if (is3p) "3-Phase" else "1-Phase"})",
                    formulaUsed = form,
                    steps = listOf(CalculationStep(1, "Apparent Power", form, "${if (is3p) "1.732 × " else ""}$v × $i", "${ElectricalFormulas.fmt(sVa)} VA")),
                    notes = listOf("Strict Phase Isolation: Single Phase ও Three Phase লোড পৃথকভাবে হিসাব করা হয়েছে।")
                )
            }
        ),

        // 103. Reactive Power Calculation
        CalculatorDefinition(
            id = 103,
            name = "Reactive Power Calculation",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.BASIC_ELEC,
            description = "Calculates imaginary reactive magnetizing power in kVAR for Single or Three Phase.",
            formula = "1φ: Q = V × I × sin φ | 3φ: Q = √3 × V × I × sin φ",
            supportsPhaseSelection = true,
            defaultPhase = "3-Phase",
            inputs = listOf(
                InputFieldConfig("voltage", "Voltage (V)", "400", listOf("V"), "V"),
                InputFieldConfig("current", "Current (A)", "80", listOf("A"), "A"),
                InputFieldConfig("pf", "Power Factor (cos φ)", "0.80", emptyList(), "")
            ),
            calculate = { vals, units ->
                val v = vals["voltage"] ?: 400.0
                val i = vals["current"] ?: 80.0
                val pf = (vals["pf"] ?: 0.80).coerceIn(0.1, 1.0)
                val is3p = (units["phase"] ?: units["is3Phase"] ?: "3-Phase") != "1-Phase"
                val sinPhi = sqrt(1.0 - (pf * pf))
                val qVar = if (is3p) sqrt(3.0) * v * i * sinPhi else v * i * sinPhi
                val form = if (is3p) "Q = √3 × V × I × sin φ" else "Q = V × I × sin φ"
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(qVar / 1000.0),
                    primaryUnit = "kVAR (${if (is3p) "3-Phase" else "1-Phase"})",
                    formulaUsed = form,
                    steps = listOf(
                        CalculationStep(1, "sin φ", "√(1 - cos² φ)", "√(1 - $pf²)", ElectricalFormulas.fmt(sinPhi, 4)),
                        CalculationStep(2, "Reactive Power", form, "${if (is3p) "1.732 × " else ""}$v × $i × ${ElectricalFormulas.fmt(sinPhi, 3)}", "${ElectricalFormulas.fmt(qVar)} VAR")
                    ),
                    secondaryResults = listOf("sin φ (Reactive Factor)" to ElectricalFormulas.fmt(sinPhi, 4)),
                    notes = listOf("Strict Phase Isolation: Single Phase ও Three Phase লোড পৃথকভাবে হিসাব করা হয়েছে।")
                )
            }
        ),

        // 104. Ohm's Law AC
        CalculatorDefinition(
            id = 104,
            name = "Ohm's Law AC",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.BASIC_ELEC,
            description = "Calculates AC circuit relationship between Voltage, Current and Complex Impedance Z.",
            formula = "V = I × Z,  Z = √(R² + X²)",
            inputs = listOf(
                InputFieldConfig("r", "Resistance R (Ω)", "12", listOf("Ω"), "Ω"),
                InputFieldConfig("x", "Net Reactance X (Ω)", "9", listOf("Ω"), "Ω"),
                InputFieldConfig("v", "Voltage V (V)", "230", listOf("V"), "V")
            ),
            calculate = { vals, _ ->
                val r = vals["r"] ?: 12.0
                val x = vals["x"] ?: 9.0
                val v = vals["v"] ?: 230.0
                val z = sqrt((r * r) + (x * x))
                val i = if (z > 0) v / z else 0.0
                val pf = if (z > 0) r / z else 1.0
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(z),
                    primaryUnit = "Impedance Z (Ω)",
                    formulaUsed = "Z = √(R² + X²),  I = V / Z",
                    steps = listOf(
                        CalculationStep(1, "Impedance", "√(R² + X²)", "√($r² + $x²)", "${ElectricalFormulas.fmt(z)} Ω"),
                        CalculationStep(2, "AC Current", "V / Z", "$v / ${ElectricalFormulas.fmt(z)}", "${ElectricalFormulas.fmt(i)} A")
                    ),
                    secondaryResults = listOf(
                        "Circuit Current" to "${ElectricalFormulas.fmt(i)} A",
                        "Power Factor (cos φ)" to ElectricalFormulas.fmt(pf, 3),
                        "Phase Angle" to "${ElectricalFormulas.fmt(Math.toDegrees(kotlin.math.acos(pf)))}°"
                    )
                )
            }
        ),

        // 105. Ohm's Law DC
        CalculatorDefinition(
            id = 105,
            name = "Ohm's Law DC",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.BASIC_ELEC,
            description = "Calculates DC Voltage, Current, Resistance, and Power.",
            formula = "V = I × R,  P = V × I = I² × R",
            inputs = listOf(
                InputFieldConfig("voltage", "Voltage (V)", "24", listOf("V"), "V"),
                InputFieldConfig("current", "Current (A)", "3", listOf("A"), "A")
            ),
            calculate = { vals, _ ->
                val v = vals["voltage"] ?: 24.0
                val i = vals["current"] ?: 3.0
                val r = if (i > 0) v / i else 0.0
                val p = v * i
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(r),
                    primaryUnit = "Ohms (Ω)",
                    formulaUsed = "R = V / I,  P = V × I",
                    steps = listOf(
                        CalculationStep(1, "Resistance", "V / I", "$v / $i", "${ElectricalFormulas.fmt(r)} Ω"),
                        CalculationStep(2, "Power Dissipated", "V × I", "$v × $i", "${ElectricalFormulas.fmt(p)} W")
                    ),
                    secondaryResults = listOf("Power Dissipation" to "${ElectricalFormulas.fmt(p)} Watts")
                )
            }
        ),

        // 133. AWG to mm² Conversion
        CalculatorDefinition(
            id = 133,
            name = "AWG to mm² Conversion",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Converts American Wire Gauge (AWG) size to cross-sectional area in square millimeters (mm²).",
            formula = "A (mm²) = (π/4) × (0.127 × 92^((36-n)/39))²",
            inputs = listOf(InputFieldConfig("awg", "AWG Size (e.g. 10, 12, 14, 1/0 as 0)", "12", listOf("AWG"), "AWG")),
            calculate = { vals, _ ->
                val awg = vals["awg"] ?: 12.0
                val mm2 = ElectricalFormulas.awgToMm2(awg)
                val diameter = 0.127 * 92.0.pow((36.0 - awg) / 39.0)
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(mm2, 3),
                    primaryUnit = "mm²",
                    formulaUsed = "d = 0.127 × 92^((36-n)/39), A = π(d/2)²",
                    steps = listOf(
                        CalculationStep(1, "Diameter", "d in mm", "0.127 × 92^((36-$awg)/39)", "${ElectricalFormulas.fmt(diameter, 3)} mm"),
                        CalculationStep(2, "Area", "π × (d/2)²", "π × (${ElectricalFormulas.fmt(diameter/2.0, 3)})²", "${ElectricalFormulas.fmt(mm2, 3)} mm²")
                    ),
                    secondaryResults = listOf(
                        "Conductor Diameter" to "${ElectricalFormulas.fmt(diameter, 3)} mm",
                        "Diameter in Inches" to "${ElectricalFormulas.fmt(diameter / 25.4, 4)} in"
                    ),
                    standardBasis = "ASTM B258 Standard Specification"
                )
            }
        ),

        // 134. mm² to AWG Conversion
        CalculatorDefinition(
            id = 134,
            name = "mm² to AWG Conversion",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Converts conductor cross-sectional area in mm² to the closest American Wire Gauge (AWG).",
            formula = "n = -39 × log92(d / 0.127) + 36",
            inputs = listOf(InputFieldConfig("mm2", "Conductor Area (mm²)", "4.0", listOf("mm²"), "mm²")),
            calculate = { vals, _ ->
                val area = vals["mm2"] ?: 4.0
                val awgExact = ElectricalFormulas.mm2ToAwg(area)
                val awgRound = kotlin.math.round(awgExact).toInt()
                CalculationResult(
                    primaryValue = "$awgRound AWG",
                    primaryUnit = "AWG (Gauge)",
                    formulaUsed = "AWG Exact = ${ElectricalFormulas.fmt(awgExact, 2)}",
                    steps = listOf(
                        CalculationStep(1, "Equivalent AWG", "Logarithmic gauge formula", "d = 2√(A/π) = ${ElectricalFormulas.fmt(2 * sqrt(area / PI), 3)} mm", "Exact: ${ElectricalFormulas.fmt(awgExact, 2)} AWG"),
                        CalculationStep(2, "Closest Commercial Gauge", "Nearest integer AWG", "${ElectricalFormulas.fmt(awgExact, 1)}", "$awgRound AWG")
                    ),
                    secondaryResults = listOf("Exact Decimal AWG" to ElectricalFormulas.fmt(awgExact, 2))
                )
            }
        ),

        // 135. SWG to mm Conversion
        CalculatorDefinition(
            id = 135,
            name = "SWG to mm Conversion",
            level = CalculatorLevel.LOWER,
            category = CalculatorCategory.CONVERSION,
            description = "Converts Standard British Wire Gauge (SWG) to diameter in mm and area in mm².",
            formula = "BS 3737 Standard Wire Gauge Table",
            inputs = listOf(InputFieldConfig("swg", "SWG Number (0 to 40)", "18", listOf("SWG"), "SWG")),
            calculate = { vals, _ ->
                val swg = (vals["swg"] ?: 18.0).toInt().coerceIn(0, 40)
                val diam = ElectricalFormulas.swgTable[swg] ?: 1.219
                val area = PI * (diam / 2.0).pow(2.0)
                CalculationResult(
                    primaryValue = ElectricalFormulas.fmt(diam, 3),
                    primaryUnit = "mm (Diameter)",
                    formulaUsed = "Standard BS 3737 SWG Specification",
                    steps = listOf(
                        CalculationStep(1, "Diameter Lookup", "SWG $swg", "Standard reference table", "${ElectricalFormulas.fmt(diam, 3)} mm"),
                        CalculationStep(2, "Cross-Section Area", "π × (d/2)²", "π × (${ElectricalFormulas.fmt(diam/2.0, 3)})²", "${ElectricalFormulas.fmt(area, 4)} mm²")
                    ),
                    secondaryResults = listOf(
                        "Conductor Area" to "${ElectricalFormulas.fmt(area, 4)} mm²",
                        "Diameter in Mils (thou)" to "${ElectricalFormulas.fmt(diam * 39.3701, 1)} mils"
                    ),
                    standardBasis = "British Standard BS 3737"
                )
            }
        )
    )
}
