package com.example.data.calculator

import com.example.data.calculator.CalcFactory.input
import com.example.data.calculator.ElectricalFormulas.fmt
import com.example.data.model.CalculationStep
import com.example.data.model.CalculatorCategory
import com.example.data.model.CalculatorDefinition
import com.example.data.model.CalculatorLevel
import kotlin.math.ceil
import kotlin.math.sqrt

object CalculatorsBasicPart1 {
    val list: List<CalculatorDefinition> = listOf(
        // 1. All Factory Load Calculation
        CalcFactory.create(
            1, "All Factory Load Calculation", CalculatorLevel.BASIC, CalculatorCategory.FACTORY,
            "Estimates total industrial plant electrical connected load, diversified maximum demand, and transformer sizing.",
            "Demand (kVA) = (Total Connected kW × Diversity Factor) / (PF × Efficiency)",
            listOf(input("kw", "Total Connected Load (kW)", "500", "kW"), input("df", "Diversity Factor", "0.75"), input("pf", "Plant Power Factor", "0.85"), input("eff", "Average Plant Efficiency", "0.90")),
            calc = { v ->
                val kw = v["kw"] ?: 500.0; val df = v["df"] ?: 0.75; val pf = v["pf"] ?: 0.85; val eff = v["eff"] ?: 0.90
                val demandKva = (kw * df) / (pf * eff)
                fmt(demandKva) to "kVA (Maximum Demand)"
            },
            secondaryBuilder = { v ->
                val kw = v["kw"] ?: 500.0; val df = v["df"] ?: 0.75; val pf = v["pf"] ?: 0.85; val eff = v["eff"] ?: 0.90
                val kva = (kw * df) / (pf * eff)
                listOf("Recommended Transformer" to "${ceil(kva * 1.25 / 100) * 100} kVA", "Substation Current at 400V" to "${fmt(kva * 1000 / (1.732 * 400))} A")
            }
        ),
        // 3. House Wiring Load Calculation
        CalcFactory.create(
            3, "House Wiring Load Calculation", CalculatorLevel.BASIC, CalculatorCategory.BUILDING,
            "Calculates total residential electrical connected wattage and design service feeder capacity.",
            "Total Load (W) = Sum of Lighting + Sockets + Appliances; Feeder A = Load / (V × PF)",
            listOf(input("lights", "Lighting Watts Total", "600", "W"), input("fans", "Ceiling Fans Watts", "400", "W"), input("sockets", "General Sockets Watts", "1500", "W"), input("heavy", "AC & Heavy Loads Watts", "3500", "W"), input("v", "Supply Voltage (V)", "230", "V")),
            calc = { v ->
                val tot = (v["lights"] ?: 600.0) + (v["fans"] ?: 400.0) + (v["sockets"] ?: 1500.0) + (v["heavy"] ?: 3500.0)
                val volt = v["v"] ?: 230.0; val amps = tot / (volt * 0.9)
                fmt(tot / 1000.0) to "kW Total Connected"
            },
            secondaryBuilder = { v ->
                val tot = (v["lights"] ?: 600.0) + (v["fans"] ?: 400.0) + (v["sockets"] ?: 1500.0) + (v["heavy"] ?: 3500.0)
                val volt = v["v"] ?: 230.0; val amps = tot / (volt * 0.9)
                listOf("Design Current" to "${fmt(amps)} A", "Main MCB Rating" to "${ElectricalFormulas.selectBreaker(amps)} A")
            }
        ),
        // 4. Solar System Size Calculation All In
        CalcFactory.create(
            4, "Solar System Size Calculation All In", CalculatorLevel.BASIC, CalculatorCategory.POWER,
            "Solar PV System Design & Backup Calculation. Sizing for solar panels, inverter, battery bank, and charge controller.",
            "P_pv (kW) = (Daily_kWh / Derate_0.80) / Peak_Sun_Hours | Battery_Ah = Backup_Wh / (V_sys × DoD × η_inv)",
            listOf(
                input("dailyKwh", "Daily Energy Consumption (kWh)", "10", "kWh/day"),
                input("sunHours", "Average Peak Sun Hours (h)", "4.5", "Hours"),
                input("backupHours", "Backup System Hours (h)", "6", "Hours"),
                input("panelW", "Solar Panel Wattage (W)", "550", "W")
            ),
            calc = { v ->
                val kwh = v["dailyKwh"] ?: 10.0
                val psh = v["sunHours"] ?: 4.5
                val panelW = v["panelW"] ?: 550.0
                val reqKw = (kwh / 0.80) / psh
                val panels = ceil((reqKw * 1000.0) / panelW).toInt()
                val actualKw = (panels * panelW) / 1000.0
                fmt(actualKw) to "kW PV Array ($panels × ${panelW.toInt()}W)"
            },
            standard = "IEC 62548 / NEC Article 690 / IEEE 1561 Solar PV Standards",
            secondaryBuilder = { v ->
                val kwh = v["dailyKwh"] ?: 10.0
                val psh = v["sunHours"] ?: 4.5
                val backupH = v["backupHours"] ?: 6.0
                val panelW = v["panelW"] ?: 550.0
                val reqKw = (kwh / 0.80) / psh
                val panels = ceil((reqKw * 1000.0) / panelW).toInt()
                val actualKw = (panels * panelW) / 1000.0
                val backupLoadKw = kwh / 24.0
                val battKwh = (backupLoadKw * backupH) / (0.90 * 0.90 * 0.90)
                val bankAh = (battKwh * 1000.0) / 48.0
                val strings = ceil(bankAh / 200.0).toInt()
                val totalBatt = 4 * strings
                listOf(
                    "Recommended PV Array" to "${fmt(actualKw)} kW ($panels Panels)",
                    "Inverter Size" to "${fmt(ceil(actualKw * 1.25))} kVA Inverter",
                    "Battery Bank" to "48V / ${strings * 200}Ah ($totalBatt × 12V 200Ah Units)",
                    "Backup Runtime" to "${backupH.toInt()} Hours Expected",
                    "Charge Controller" to "${ceil((actualKw * 1000.0 / 48.0) * 1.25).toInt()}A MPPT"
                )
            }
        ),
        // 5. PFI Size Calculation / Capacitor Bank
        CalcFactory.create(
            5, "PFI Size Calculation / Capacitor Bank", CalculatorLevel.BASIC, CalculatorCategory.PFI,
            "Calculates required Power Factor Improvement capacitor bank kVAR rating to achieve target PF.",
            "Q (kVAR) = P (kW) × [tan(cos⁻¹ PF₁) - tan(cos⁻¹ PF₂)]",
            listOf(input("kw", "Active Load P (kW)", "200", "kW"), input("pf1", "Initial Power Factor", "0.72"), input("pf2", "Target Power Factor", "0.98")),
            standard = "IEC 60831 / IEEE 18 / NEC 460 / BNBC",
            calc = { v ->
                val p = v["kw"] ?: 200.0; val pf1 = (v["pf1"] ?: 0.72).coerceIn(0.1, 0.99); val pf2 = (v["pf2"] ?: 0.98).coerceIn(pf1, 1.0)
                val phi1 = kotlin.math.acos(pf1); val phi2 = kotlin.math.acos(pf2)
                val kvar = p * (kotlin.math.tan(phi1) - kotlin.math.tan(phi2))
                fmt(kvar) to "kVAR Required"
            },
            secondaryBuilder = { v ->
                val p = v["kw"] ?: 200.0; val pf1 = (v["pf1"] ?: 0.72).coerceIn(0.1, 0.99); val pf2 = (v["pf2"] ?: 0.98).coerceIn(pf1, 1.0)
                val phi1 = kotlin.math.acos(pf1); val phi2 = kotlin.math.acos(pf2)
                val kvar = p * (kotlin.math.tan(phi1) - kotlin.math.tan(phi2))
                val bank = kotlin.math.ceil(kvar / 25.0) * 25.0
                listOf(
                    "Recommended Bank" to "$bank kVAR",
                    "APFC Stages" to "6-Step Automatic Controller",
                    "Capacitor Voltage Rating" to "440V / 480V Heavy Duty"
                )
            }
        ),
        // 10. Transformer Size Calculation
        CalcFactory.create(
            10, "Transformer Size Calculation", CalculatorLevel.BASIC, CalculatorCategory.TRANSFORMER,
            "Calculates required standard transformer rating in kVA with safety margin.",
            "kVA = (Total kW / PF) × Future Expansion Factor",
            listOf(input("kw", "Peak Load (kW)", "320", "kW"), input("pf", "Power Factor", "0.85"), input("future", "Growth Margin %", "20", "%")),
            calc = { v ->
                val kw = v["kw"] ?: 320.0; val pf = v["pf"] ?: 0.85; val g = (v["future"] ?: 20.0) / 100.0
                val kva = (kw / pf) * (1.0 + g)
                fmt(kva) to "kVA Required"
            },
            secondaryBuilder = { v ->
                val kw = v["kw"] ?: 320.0; val pf = v["pf"] ?: 0.85; val g = (v["future"] ?: 20.0) / 100.0
                val kva = (kw / pf) * (1.0 + g)
                val standard = listOf(100, 160, 200, 250, 315, 400, 500, 630, 800, 1000, 1250, 1600, 2000, 2500).firstOrNull { it >= kva } ?: 3150
                listOf("Recommended Standard Rating" to "$standard kVA")
            }
        ),
        // 11. Transformer Load Current Calculation
        CalcFactory.create(
            11, "Transformer Load Current Calculation", CalculatorLevel.BASIC, CalculatorCategory.TRANSFORMER,
            "Calculates primary (HT) and secondary (LT) full load rated currents.",
            "I_LT = kVA × 1000 / (√3 × V_LT),  I_HT = kVA × 1000 / (√3 × V_HT)",
            listOf(input("kva", "Transformer Rating (kVA)", "500", "kVA"), input("vlt", "Secondary Voltage (V)", "400", "V"), input("vht", "Primary Voltage (V)", "11000", "V")),
            calc = { v ->
                val kva = v["kva"] ?: 500.0; val vlt = v["vlt"] ?: 400.0
                val ilt = (kva * 1000.0) / (sqrt(3.0) * vlt)
                fmt(ilt) to "Amperes (LT Full Load Current)"
            },
            secondaryBuilder = { v ->
                val kva = v["kva"] ?: 500.0; val vht = v["vht"] ?: 11000.0
                val iht = (kva * 1000.0) / (sqrt(3.0) * vht)
                listOf("HT Side Current (11kV)" to "${fmt(iht)} A")
            }
        ),
        // 12. Transformer Efficiency Calculation
        CalcFactory.create(
            12, "Transformer Efficiency Calculation", CalculatorLevel.BASIC, CalculatorCategory.TRANSFORMER,
            "Calculates transformer percentage efficiency at any load fraction.",
            "η = (x × kVA × PF) / [(x × kVA × PF) + Pi + x² × Pc] × 100%",
            listOf(input("kva", "Rating (kVA)", "1000", "kVA"), input("load", "Loading Fraction (e.g. 0.8)", "0.8"), input("pf", "Power Factor", "0.85"), input("iron", "Iron Loss Pi (Watts)", "1800", "W"), input("cu", "Full Load Copper Loss Pc (Watts)", "8500", "W")),
            calc = { v ->
                val kva = v["kva"] ?: 1000.0; val x = v["load"] ?: 0.8; val pf = v["pf"] ?: 0.85
                val pi = (v["iron"] ?: 1800.0) / 1000.0; val pc = (v["cu"] ?: 8500.0) / 1000.0
                val output = x * kva * pf
                val losses = pi + (x * x * pc)
                val eff = (output / (output + losses)) * 100.0
                fmt(eff, 3) to "% Efficiency"
            }
        ),
        // 13. Transformer Rating Calculation
        CalcFactory.create(
            13, "Transformer Rating Calculation", CalculatorLevel.BASIC, CalculatorCategory.TRANSFORMER,
            "Computes transformer kVA rating from line voltage and full-load amperes.",
            "kVA = (√3 × V × I) / 1000",
            listOf(input("v", "Secondary Line Voltage (V)", "415", "V"), input("i", "Current Demand (A)", "350", "A")),
            calc = { v ->
                val volt = v["v"] ?: 415.0; val amp = v["i"] ?: 350.0
                val kva = (sqrt(3.0) * volt * amp) / 1000.0
                fmt(kva) to "kVA"
            }
        ),
        // 14. Transformer Basic Calculation
        CalcFactory.create(
            14, "Transformer Basic Calculation", CalculatorLevel.BASIC, CalculatorCategory.TRANSFORMER,
            "Turns ratio, primary and secondary voltages and currents relationship.",
            "V1 / V2 = N1 / N2 = I2 / I1",
            listOf(input("v1", "Primary Voltage (V)", "11000", "V"), input("v2", "Secondary Voltage (V)", "400", "V"), input("n1", "Primary Turns N1", "5500", "Turns")),
            calc = { v ->
                val v1 = v["v1"] ?: 11000.0; val v2 = v["v2"] ?: 400.0; val n1 = v["n1"] ?: 5500.0
                val n2 = (v2 * n1) / v1
                fmt(n2, 1) to "Secondary Turns (N2)"
            },
            secondaryBuilder = { v ->
                val v1 = v["v1"] ?: 11000.0; val v2 = v["v2"] ?: 400.0
                listOf("Transformation Ratio (K = V2/V1)" to fmt(v2 / v1, 4))
            }
        ),
        // 15. Generator Size Calculation
        CalcFactory.create(
            15, "Generator Size Calculation", CalculatorLevel.BASIC, CalculatorCategory.GENERATOR,
            "Calculates standby diesel generator kVA size based on continuous loads and motor surge.",
            "kVA = (Running kW / PF) + (Largest Motor Starting kVA × 0.4)",
            listOf(input("runKw", "Continuous Running Load (kW)", "180", "kW"), input("motorKw", "Largest Motor (kW)", "30", "kW"), input("pf", "Generator PF", "0.8")),
            calc = { v ->
                val r = v["runKw"] ?: 180.0; val m = v["motorKw"] ?: 30.0; val pf = v["pf"] ?: 0.8
                val kva = (r / pf) + (m * 2.5)
                fmt(kva) to "kVA Generator Size"
            }
        ),
        // 16. Generator Load Calculation
        CalcFactory.create(
            16, "Generator Load Calculation", CalculatorLevel.BASIC, CalculatorCategory.GENERATOR,
            "Determines generator percentage loading and available spare capacity.",
            "Loading % = (Operating kW / (Generator kVA × PF)) × 100%",
            listOf(input("genKva", "Generator Rated (kVA)", "250", "kVA"), input("loadKw", "Actual Load (kW)", "160", "kW"), input("pf", "Power Factor", "0.8")),
            calc = { v ->
                val kva = v["genKva"] ?: 250.0; val kw = v["loadKw"] ?: 160.0; val pf = v["pf"] ?: 0.8
                val capKw = kva * pf
                val pct = (kw / capKw) * 100.0
                fmt(pct) to "% Generator Loading"
            },
            secondaryBuilder = { v ->
                val kva = v["genKva"] ?: 250.0; val kw = v["loadKw"] ?: 160.0; val pf = v["pf"] ?: 0.8
                val spare = (kva * pf) - kw
                listOf("Spare Power Capacity" to "${fmt(spare)} kW", "Status" to if (kw / (kva * pf) > 0.8) "High Load" else "Optimal")
            }
        ),
        // 17. Cable Size Calculation All In One
        CalcFactory.create(
            17, "Cable Size Calculation All In One", CalculatorLevel.BASIC, CalculatorCategory.CABLE,
            "Comprehensive cable sizing considering full-load ampacity and maximum 3% voltage drop limit.",
            "Design Current I = P / (√3 × V × PF) × 1.25",
            listOf(input("kw", "Load Power (kW)", "45", "kW"), input("v", "Voltage (V)", "400", "V"), input("len", "Run Length (m)", "80", "m"), input("pf", "Power Factor", "0.85")),
            calc = { v ->
                val kw = v["kw"] ?: 45.0; val volt = v["v"] ?: 400.0; val pf = v["pf"] ?: 0.85
                val current = (kw * 1000.0) / (sqrt(3.0) * volt * pf)
                val cable = ElectricalFormulas.selectCable(current * 1.25)
                "$cable" to "mm² Copper Cable"
            },
            secondaryBuilder = { v ->
                val kw = v["kw"] ?: 45.0; val volt = v["v"] ?: 400.0; val pf = v["pf"] ?: 0.85
                val current = (kw * 1000.0) / (sqrt(3.0) * volt * pf)
                listOf("Full Load Current" to "${fmt(current)} A", "Design Current (125%)" to "${fmt(current * 1.25)} A")
            }
        ),
        // 18. Wire Length Calculation All In One
        CalcFactory.create(
            18, "Wire Length Calculation All In One", CalculatorLevel.BASIC, CalculatorCategory.CABLE,
            "Calculates maximum permissible circuit route length before exceeding allowable voltage drop.",
            "L_max = (ΔV × A) / (2 × I × ρ)  [Single Phase]",
            listOf(input("vdrop", "Allowable Voltage Drop (V)", "6.9", "V"), input("i", "Current (A)", "20", "A"), input("area", "Wire Size (mm²)", "4.0", "mm²")),
            calc = { v ->
                val vd = v["vdrop"] ?: 6.9; val i = v["i"] ?: 20.0; val a = v["area"] ?: 4.0
                val rho = 0.0175 // Copper ohm*mm2/m
                val length = (vd * a) / (2.0 * i * rho)
                fmt(length) to "Meters Maximum Length"
            }
        ),
        // 19. Cable Power Losses Calculation All In
        CalcFactory.create(
            19, "Cable Power Losses Calculation All In", CalculatorLevel.BASIC, CalculatorCategory.CABLE,
            "Calculates total Joule heating power loss (Watts) along electrical feeder runs.",
            "P_loss = 3 × I² × R_cable  (3-Phase)",
            listOf(input("i", "Line Current (A)", "80", "A"), input("r", "Cable Resistance per Core (Ω)", "0.12", "Ω")),
            calc = { v ->
                val i = v["i"] ?: 80.0; val r = v["r"] ?: 0.12
                val loss = 3.0 * i * i * r
                fmt(loss / 1000.0) to "kW Power Loss"
            },
            secondaryBuilder = { v ->
                val i = v["i"] ?: 80.0; val r = v["r"] ?: 0.12
                listOf("Total Heat Dissipated" to "${fmt(3.0 * i * i * r)} Watts")
            }
        ),
        // 20. Copper Losses Calculation All In
        CalcFactory.create(
            20, "Copper Losses Calculation All In", CalculatorLevel.BASIC, CalculatorCategory.CABLE,
            "Calculates conductor I²R copper power loss and thermal dissipation.",
            "P_cu = I² × R",
            listOf(input("current", "Current (A)", "50", "A"), input("resistance", "Total Resistance (Ω)", "0.08", "Ω")),
            calc = { v ->
                val i = v["current"] ?: 50.0; val r = v["resistance"] ?: 0.08
                val p = i * i * r
                fmt(p) to "Watts Copper Loss"
            }
        ),
        // 21. Circuit Size Calculation Pro
        CalcFactory.create(
            21, "Circuit Size Calculation Pro", CalculatorLevel.BASIC, CalculatorCategory.PROTECTION,
            "Determines circuit breaker rating and matching minimum conductor ampacity.",
            "Breaker = Standard rating >= 1.25 × Continuous Load Current",
            listOf(input("loadA", "Continuous Current (A)", "36", "A")),
            calc = { v ->
                val i = v["loadA"] ?: 36.0
                val b = ElectricalFormulas.selectBreaker(i)
                "$b" to "Amperes Breaker Rating"
            },
            secondaryBuilder = { v ->
                val i = v["loadA"] ?: 36.0
                val cable = ElectricalFormulas.selectCable(i * 1.25)
                listOf("Recommended Cable Size" to "$cable mm²", "Design Current (125%)" to "${fmt(i * 1.25)} A")
            }
        ),
        // 22. Voltage Drop Calculation
        CalcFactory.create(
            22, "Voltage Drop Calculation", CalculatorLevel.BASIC, CalculatorCategory.CABLE,
            "Computes line-to-line voltage drop (V and %) over cable runs according to IEC 60364.",
            "ΔV = √3 × I × L × (R·cos φ + X·sin φ) / 1000",
            listOf(input("current", "Load Current (A)", "60", "A"), input("length", "Length (m)", "120", "m"), input("v", "System Voltage (V)", "400", "V"), input("r", "Resistance (mΩ/m)", "0.38", "mΩ/m")),
            calc = { v ->
                val i = v["current"] ?: 60.0; val l = v["length"] ?: 120.0; val sysV = v["v"] ?: 400.0; val rm = (v["r"] ?: 0.38) / 1000.0
                val vd = sqrt(3.0) * i * l * rm * 0.85
                val pct = (vd / sysV) * 100.0
                fmt(vd) to "Volts (${fmt(pct)}%)"
            },
            secondaryBuilder = { v ->
                val i = v["current"] ?: 60.0; val l = v["length"] ?: 120.0; val sysV = v["v"] ?: 400.0; val rm = (v["r"] ?: 0.38) / 1000.0
                val vd = sqrt(3.0) * i * l * rm * 0.85
                val pct = (vd / sysV) * 100.0
                listOf("Percentage Drop" to "${fmt(pct)} %", "Status" to if (pct <= 3.0) "Pass (< 3%)" else "Exceeds 3% limit")
            }
        ),
        // 24. Conductor Size Calculation
        CalcFactory.create(
            24, "Conductor Size Calculation", CalculatorLevel.BASIC, CalculatorCategory.CABLE,
            "Determines minimal conductor cross-sectional area based on thermal current capacity.",
            "A = I_design / Current Density (J)",
            listOf(input("i", "Design Current (A)", "120", "A"), input("j", "Current Density (A/mm²)", "3.5", "A/mm²")),
            calc = { v ->
                val i = v["i"] ?: 120.0; val j = (v["j"] ?: 3.5).coerceAtLeast(0.5)
                val reqA = i / j
                val standard = ElectricalFormulas.standardCableSizes.firstOrNull { it >= reqA } ?: 300.0
                "$standard" to "mm² Standard Size (Calculated: ${fmt(reqA)} mm²)"
            }
        ),
        // 25. Conductor Resistance Calculation
        CalcFactory.create(
            25, "Conductor Resistance Calculation", CalculatorLevel.BASIC, CalculatorCategory.CABLE,
            "Calculates temperature-adjusted conductor resistance R_T = R_20 × [1 + α × (T - 20)].",
            "R_T = (ρ_20 × L / A) × [1 + α(T - 20)]",
            listOf(input("len", "Conductor Length (m)", "100", "m"), input("area", "Area (mm²)", "16", "mm²"), input("temp", "Operating Temp (°C)", "70", "°C")),
            calc = { v ->
                val l = v["len"] ?: 100.0; val a = v["area"] ?: 16.0; val t = v["temp"] ?: 70.0
                val r20 = (0.0175 * l) / a
                val rt = r20 * (1.0 + 0.00393 * (t - 20.0))
                fmt(rt, 4) to "Ohms (Ω)"
            },
            secondaryBuilder = { v ->
                val l = v["len"] ?: 100.0; val a = v["area"] ?: 16.0
                listOf("Resistance at 20°C" to "${fmt((0.0175 * l) / a, 4)} Ω")
            }
        ),
        // 28. LBS Size Calculation
        CalcFactory.create(
            28, "LBS Size Calculation", CalculatorLevel.BASIC, CalculatorCategory.PROTECTION,
            "Sizes 11kV Load Break Switch (LBS) for transformer HT primary protection.",
            "I_HT = kVA / (√3 × 11kV) × 1.5 Safety Margin",
            listOf(input("kva", "Transformer Rating (kVA)", "630", "kVA"), input("kv", "Line Voltage (kV)", "11", "kV")),
            calc = { v ->
                val kva = v["kva"] ?: 630.0; val kv = v["kv"] ?: 11.0
                val i = kva / (sqrt(3.0) * kv)
                val lbsRating = listOf(400, 630).firstOrNull { it >= i } ?: 630
                "$lbsRating" to "Amperes (Standard LBS 12kV)"
            },
            secondaryBuilder = { v ->
                val kva = v["kva"] ?: 630.0; val kv = v["kv"] ?: 11.0
                val i = kva / (sqrt(3.0) * kv)
                listOf("Rated HT Load Current" to "${fmt(i)} A")
            }
        ),
        // 29. ATS Size Calculation
        CalcFactory.create(
            29, "ATS Size Calculation", CalculatorLevel.BASIC, CalculatorCategory.PROTECTION,
            "Calculates Automatic Transfer Switch (ATS) continuous ampere rating for generator/mains changeover.",
            "ATS Rating >= 1.25 × Max(Mains Current, Generator Current)",
            listOf(input("mainsA", "Mains Full Load (A)", "400", "A"), input("genA", "Generator Full Load (A)", "350", "A")),
            calc = { v ->
                val m = v["mainsA"] ?: 400.0; val g = v["genA"] ?: 350.0
                val maxA = maxOf(m, g) * 1.25
                val ats = ElectricalFormulas.selectBreaker(maxA)
                "$ats" to "Amperes ATS Rating"
            }
        ),
        // 35. ACR Load Current Calculation
        CalcFactory.create(
            35, "ACR Load Current Calculation", CalculatorLevel.BASIC, CalculatorCategory.PROTECTION,
            "Calculates primary continuous current for distribution Automatic Circuit Recloser (ACR).",
            "I = Total Feeder kVA / (√3 × V_line)",
            listOf(input("feederKva", "Feeder Connected Load (kVA)", "4000", "kVA"), input("kv", "System Voltage (kV)", "33", "kV")),
            calc = { v ->
                val kva = v["feederKva"] ?: 4000.0; val kv = v["kv"] ?: 33.0
                val i = kva / (sqrt(3.0) * kv)
                fmt(i) to "Amperes Feeder Current"
            }
        ),
        // 36. Drop Out Fuse Size Calculation
        CalcFactory.create(
            36, "Drop Out Fuse Size Calculation", CalculatorLevel.BASIC, CalculatorCategory.PROTECTION,
            "Calculates HT Drop Out (DO) fuse barrel link ampere rating for distribution transformer protection.",
            "Fuse Link Rating = 1.5 to 2.0 × Full Load Primary Current",
            listOf(input("kva", "Transformer kVA", "200", "kVA"), input("kv", "Primary Voltage (kV)", "11", "kV")),
            calc = { v ->
                val kva = v["kva"] ?: 200.0; val kv = v["kv"] ?: 11.0
                val i = kva / (sqrt(3.0) * kv)
                val fuse = fmt(i * 1.5, 1)
                "$fuse" to "Amperes Fuse Link"
            },
            secondaryBuilder = { v ->
                val kva = v["kva"] ?: 200.0; val kv = v["kv"] ?: 11.0
                listOf("Primary Rated Current" to "${fmt(kva / (sqrt(3.0) * kv))} A")
            }
        ),
        // 37. HRC Fuse Size Calculation
        CalcFactory.create(
            37, "HRC Fuse Size Calculation", CalculatorLevel.BASIC, CalculatorCategory.PROTECTION,
            "High Rupturing Capacity (HRC) fuse sizing for motor and feeder backup short circuit protection.",
            "I_fuse >= 1.5 × Motor FLC (for DOL) or 1.25 × Feeder Load",
            listOf(input("flc", "Load Full Load Current (A)", "48", "A")),
            calc = { v ->
                val flc = v["flc"] ?: 48.0
                val f = ElectricalFormulas.selectBreaker(flc * 1.5)
                "$f" to "Amperes HRC Fuse"
            }
        ),
        // 40. HMI Size Calculation
        CalcFactory.create(
            40, "HMI Size Calculation", CalculatorLevel.BASIC, CalculatorCategory.FACTORY,
            "Calculates industrial Human Machine Interface (HMI) DC power consumption and screen resolution requirements.",
            "Power (W) = V_DC × I_DC; Sizing = Screen diagonal + 20% margin",
            listOf(input("screenInch", "Screen Size (Inches)", "7", "Inches"), input("current", "Current Draw at 24VDC (mA)", "650", "mA")),
            calc = { v ->
                val ma = v["current"] ?: 650.0
                val w = (24.0 * ma) / 1000.0
                fmt(w) to "Watts Power Consumption (24V DC)"
            }
        ),
        // 41. VFD Size Calculation
        CalcFactory.create(
            41, "VFD Size Calculation", CalculatorLevel.BASIC, CalculatorCategory.MOTOR,
            "Sizes Variable Frequency Drive (VFD / Inverter) with 120% heavy-duty overload margin.",
            "VFD Rated Current >= 1.20 × Motor Full Load Current",
            listOf(input("motorKw", "Motor Power (kW)", "22", "kW"), input("flc", "Motor FLC (A)", "42", "A")),
            calc = { v ->
                val flc = v["flc"] ?: 42.0
                val reqA = flc * 1.20
                fmt(reqA) to "Amperes VFD Output Rating"
            },
            secondaryBuilder = { v ->
                val kw = v["motorKw"] ?: 22.0
                listOf("Drive Power Rating" to "$kw kW (${fmt(kw / 0.7457)} HP)")
            }
        ),
        // 42. PLC Size Calculation
        CalcFactory.create(
            42, "PLC Size Calculation", CalculatorLevel.BASIC, CalculatorCategory.FACTORY,
            "Estimates Programmable Logic Controller (PLC) I/O module rack count and 24VDC power supply size.",
            "Power Supply (W) = [Digital I/O × 0.1W + Analog I/O × 0.5W + CPU 15W] × 1.3",
            listOf(input("di", "Digital Inputs (DI)", "32", "Points"), input("do", "Digital Outputs (DO)", "24", "Points"), input("ai", "Analog Inputs (AI)", "8", "Points")),
            calc = { v ->
                val di = v["di"] ?: 32.0; val d_o = v["do"] ?: 24.0; val ai = v["ai"] ?: 8.0
                val load = (di * 0.1) + (d_o * 0.15) + (ai * 0.6) + 15.0
                val ps = load * 1.3
                fmt(ps) to "Watts 24V DC Power Supply"
            }
        ),
        // 43. Battery Size Calculation
        CalcFactory.create(
            43, "Battery Size Calculation", CalculatorLevel.BASIC, CalculatorCategory.BATTERY_UPS,
            "Calculates battery bank Ampere-hour (Ah) capacity based on load and desired autonomy time.",
            "Ah = (Load in Watts × Backup Hours) / (System Voltage × Depth of Discharge × Efficiency)",
            listOf(input("watts", "Load Power (W)", "600", "W"), input("hours", "Backup Time (Hours)", "4", "Hours"), input("v", "Battery Bank Voltage (V)", "24", "V"), input("dod", "Depth of Discharge (DOD)", "0.80")),
            calc = { v ->
                val w = v["watts"] ?: 600.0; val h = v["hours"] ?: 4.0; val volt = v["v"] ?: 24.0; val dod = v["dod"] ?: 0.8
                val ah = (w * h) / (volt * dod * 0.9)
                fmt(ah) to "Ah Battery Bank Capacity"
            }
        ),
        // 44. Battery Backup Time Calculation
        CalcFactory.create(
            44, "Battery Backup Time Calculation", CalculatorLevel.BASIC, CalculatorCategory.BATTERY_UPS,
            "Estimates continuous operating hours from battery capacity and load consumption.",
            "Time (Hours) = (Battery Ah × V × Efficiency 0.9 × DOD) / Load Watts",
            listOf(input("ah", "Battery Rating (Ah)", "200", "Ah"), input("v", "Battery Voltage (V)", "12", "V"), input("load", "Inverter Load (Watts)", "350", "W")),
            calc = { v ->
                val ah = v["ah"] ?: 200.0; val volt = v["v"] ?: 12.0; val w = (v["load"] ?: 350.0).coerceAtLeast(1.0)
                val hours = (ah * volt * 0.9 * 0.8) / w
                fmt(hours) to "Hours Backup Time"
            }
        ),
        // 45. Inverter Size Calculation
        CalcFactory.create(
            45, "Inverter Size Calculation", CalculatorLevel.BASIC, CalculatorCategory.BATTERY_UPS,
            "Sizes pure sine wave inverter in VA with 25% continuous safety factor.",
            "Inverter VA = Total Running Watts / (PF 0.8 × 0.80 Inverter Load Factor)",
            listOf(input("watts", "Total Running Appliance Watts (W)", "950", "W")),
            calc = { v ->
                val w = v["watts"] ?: 950.0
                val va = w / (0.8 * 0.8)
                fmt(va) to "VA Minimum Inverter Size"
            }
        ),
        // 46. IPS Size Calculation
        CalcFactory.create(
            46, "IPS Size Calculation", CalculatorLevel.BASIC, CalculatorCategory.BATTERY_UPS,
            "Instant Power Supply (IPS) home sizing based on lighting, ceiling fans, and computer loads.",
            "IPS (VA) = Total Load (Watts) / 0.8 Power Factor",
            listOf(input("fans", "Ceiling Fans (80W each)", "4", "pcs"), input("lights", "LED Lights (20W each)", "6", "pcs"), input("tv", "TV & Router Watts", "150", "W")),
            calc = { v ->
                val f = v["fans"] ?: 4.0; val l = v["lights"] ?: 6.0; val tv = v["tv"] ?: 150.0
                val totW = (f * 80.0) + (l * 20.0) + tv
                val ipsVa = totW / 0.8
                fmt(ipsVa) to "VA Recommended IPS"
            },
            secondaryBuilder = { v ->
                val f = v["fans"] ?: 4.0; val l = v["lights"] ?: 6.0; val tv = v["tv"] ?: 150.0
                val totW = (f * 80.0) + (l * 20.0) + tv
                listOf("Total Load Watts" to "$totW Watts", "Battery Recommendation" to "12V 150Ah - 200Ah")
            }
        ),
        // 47. AVR Size Calculation
        CalcFactory.create(
            47, "AVR Size Calculation", CalculatorLevel.BASIC, CalculatorCategory.PROTECTION,
            "Automatic Voltage Regulator / Servo Stabilizer kVA sizing with surge factor.",
            "AVR (kVA) = (Total Load kW / PF) × 1.30",
            listOf(input("loadKw", "Connected Load (kW)", "8", "kW"), input("pf", "Power Factor", "0.85")),
            calc = { v ->
                val kw = v["loadKw"] ?: 8.0; val pf = v["pf"] ?: 0.85
                val avrKva = (kw / pf) * 1.30
                fmt(avrKva) to "kVA Servo AVR Rating"
            }
        ),
        // 48. AC Size Calculation Pro
        CalcFactory.create(
            48, "AC Size Calculation Pro", CalculatorLevel.BASIC, CalculatorCategory.BUILDING,
            "Calculates required cooling capacity in Tons for rooms based on area and heat load factors.",
            "Tons = (Length ft × Width ft × 25 BTU/hr) / 12000 BTU/Ton",
            listOf(input("len", "Room Length (Feet)", "15", "ft"), input("wid", "Room Width (Feet)", "12", "ft"), input("persons", "Occupants Count", "3", "Persons")),
            calc = { v ->
                val l = v["len"] ?: 15.0; val w = v["wid"] ?: 12.0; val p = v["persons"] ?: 3.0
                val btu = (l * w * 25.0) + (p * 500.0) + 1000.0
                val tons = btu / 12000.0
                fmt(tons) to "Tons Cooling Capacity"
            },
            secondaryBuilder = { v ->
                val l = v["len"] ?: 15.0; val w = v["wid"] ?: 12.0; val p = v["persons"] ?: 3.0
                val btu = (l * w * 25.0) + (p * 500.0) + 1000.0
                val rec = if (btu <= 12500) "1.0 Ton (12,000 BTU)" else if (btu <= 18500) "1.5 Ton (18,000 BTU)" else "2.0 Ton (24,000 BTU)"
                listOf("Commercial Recommendation" to rec, "Total Heat Load" to "${fmt(btu)} BTU/hr")
            }
        ),
        // 49. AC Load and Protection Calculation
        CalcFactory.create(
            49, "AC Load and Protection Calculation", CalculatorLevel.BASIC, CalculatorCategory.PROTECTION,
            "Air conditioner electrical running amps, wire size, and circuit breaker selection.",
            "I_run = AC Tons × 3.517 kW / (V × PF × EER)",
            listOf(input("tons", "AC Capacity (Tons)", "1.5", "Tons"), input("v", "Operating Voltage (V)", "230", "V")),
            calc = { v ->
                val tons = v["tons"] ?: 1.5; val volt = v["v"] ?: 230.0
                val powerW = tons * 1250.0 // approx 1250W per ton
                val amps = powerW / (volt * 0.9)
                fmt(amps) to "Amperes Running Current"
            },
            secondaryBuilder = { v ->
                val tons = v["tons"] ?: 1.5; val volt = v["v"] ?: 230.0
                val amps = (tons * 1250.0) / (volt * 0.9)
                listOf("Breaker MCB" to "${ElectricalFormulas.selectBreaker(amps * 1.5)} A Type-C", "Cable Size" to if (tons <= 1.5) "2.5 mm²" else "4.0 mm²")
            }
        ),
        // 50. Magnetic Contactor Calculation
        CalcFactory.create(
            50, "Magnetic Contactor Calculation", CalculatorLevel.BASIC, CalculatorCategory.MOTOR,
            "Selects AC-3 duty magnetic contactor continuous rating for electric motor control.",
            "Contactor AC-3 Amps >= Motor Full Load Current (FLC)",
            listOf(input("flc", "Motor FLC (Amperes)", "30", "A")),
            calc = { v ->
                val flc = v["flc"] ?: 30.0
                val contactor = listOf(9, 12, 18, 25, 32, 40, 50, 65, 80, 95, 115, 150, 185, 225, 265, 330, 400).firstOrNull { it >= flc } ?: 500
                "$contactor" to "Amperes (AC-3 Contactor Rating)"
            }
        ),
        // 51. Overload Relay Calculation
        CalcFactory.create(
            51, "Overload Relay Calculation", CalculatorLevel.BASIC, CalculatorCategory.MOTOR,
            "Thermal overload relay (TOR) tripping current setting range.",
            "Setting = 1.0 × Motor FLC (DOL) | Setting = 0.58 × Motor FLC (Star-Delta)",
            listOf(input("flc", "Motor Full Load Current (A)", "28", "A"), input("isStarDelta", "Star Delta Starter? (1 = Yes, 0 = No)", "0")),
            calc = { v ->
                val flc = v["flc"] ?: 28.0; val sd = (v["isStarDelta"] ?: 0.0) > 0.5
                val setA = if (sd) flc * 0.58 else flc
                fmt(setA) to "Amperes Relay Trip Setting"
            }
        ),
        // 52. Fuse Size Calculation
        CalcFactory.create(
            52, "Fuse Size Calculation", CalculatorLevel.BASIC, CalculatorCategory.PROTECTION,
            "Calculates fuse rating for general resistive and inductive motor circuits.",
            "Fuse Rating = 1.25 × Continuous Load or 1.75 × Motor Load",
            listOf(input("current", "Circuit Load Current (A)", "22", "A")),
            calc = { v ->
                val i = v["current"] ?: 22.0
                val f = ElectricalFormulas.selectBreaker(i * 1.3)
                "$f" to "Amperes Fuse Rating"
            }
        )
    )
}
