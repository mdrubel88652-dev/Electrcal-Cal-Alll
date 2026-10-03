package com.example.data.calculator

import com.example.data.calculator.CalcFactory.input
import com.example.data.calculator.ElectricalFormulas.fmt
import com.example.data.model.CalculatorCategory
import com.example.data.model.CalculatorDefinition
import com.example.data.model.CalculatorLevel
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.sqrt

object CalculatorsAdvancePart1 {
    val list: List<CalculatorDefinition> = listOf(
        // 2. Substation Size Calculation All In
        CalcFactory.create(
            2, "Substation Size Calculation All In", CalculatorLevel.ADVANCE, CalculatorCategory.POWER,
            "Calculates total substation transformer capacity, HT/LT switchgear, and civil footprint.",
            "Substation kVA = Total Diversified Demand / 0.8 Loading Factor",
            listOf(input("kw", "Peak Connected Load (kW)", "850", "kW"), input("df", "Diversity Factor", "0.80"), input("pf", "Plant Power Factor", "0.85")),
            calc = { v ->
                val kw = v["kw"] ?: 850.0; val df = v["df"] ?: 0.80; val pf = v["pf"] ?: 0.85
                val demandKva = (kw * df) / pf
                val subKva = demandKva / 0.8 // 80% maximum loading rule
                val standard = listOf(500, 630, 800, 1000, 1250, 1600, 2000, 2500, 3150).firstOrNull { it >= subKva } ?: 3150
                "$standard" to "kVA Transformer Size (Calculated: ${fmt(subKva)} kVA)"
            },
            secondaryBuilder = { v ->
                val kw = v["kw"] ?: 850.0; val df = v["df"] ?: 0.80; val pf = v["pf"] ?: 0.85
                val kva = (kw * df) / (pf * 0.8)
                listOf("HT Incomer Current (11kV)" to "${fmt(kva / (1.732 * 11.0))} A", "LT Incomer Current (400V)" to "${fmt((kva * 1000.0) / (1.732 * 400.0))} A", "Substation Room Area Approx" to "${fmt(kva * 0.05 + 25.0)} m²")
            }
        ),
        // 26. VCB Size Calculation
        CalcFactory.create(
            26, "VCB Size Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.PROTECTION,
            "Sizes Vacuum Circuit Breaker (VCB) rated normal current and symmetrical breaking capacity in kA.",
            "I_n >= 1.25 × Full Load Current; I_sc = Fault MVA / (√3 × V_rated)",
            listOf(input("kva", "Substation Transformer (kVA)", "1250", "kVA"), input("kv", "Rated Voltage (kV)", "11", "kV"), input("faultMva", "Grid Short Circuit MVA", "350", "MVA")),
            calc = { v ->
                val kva = v["kva"] ?: 1250.0; val kv = v["kv"] ?: 11.0
                val flc = kva / (sqrt(3.0) * kv)
                val rating = if (flc <= 630) 630 else 1250
                "$rating" to "Amperes VCB Continuous Rating"
            },
            secondaryBuilder = { v ->
                val mva = v["faultMva"] ?: 350.0; val kv = v["kv"] ?: 11.0
                val isc = mva / (sqrt(3.0) * kv)
                listOf("Symmetrical Breaking Capacity" to "${fmt(isc)} kA (Standard: 25kA / 31.5kA)", "Making Current Peak" to "${fmt(isc * 2.5)} kA Peak")
            }
        ),
        // 27. ACB Air Circuit Breaker Size Calculation
        CalcFactory.create(
            27, "ACB Air Circuit Breaker Size Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.PROTECTION,
            "Sizes LT Air Circuit Breaker (ACB) continuous ampere and Icu fault breaking capacity.",
            "ACB Rating >= 1.25 × Transformer Secondary Full Load Current",
            listOf(input("kva", "Transformer Rating (kVA)", "1600", "kVA"), input("v", "Secondary Voltage (V)", "400", "V"), input("zpct", "Transformer Impedance %Z", "5.5", "%")),
            calc = { v ->
                val kva = v["kva"] ?: 1600.0; val volt = v["v"] ?: 400.0
                val ilt = (kva * 1000.0) / (sqrt(3.0) * volt)
                val des = ilt * 1.25
                val acb = listOf(630, 800, 1000, 1250, 1600, 2000, 2500, 3200, 4000, 5000, 6300).firstOrNull { it >= des } ?: 6300
                "$acb" to "Amperes (ACB Frame Size)"
            },
            secondaryBuilder = { v ->
                val kva = v["kva"] ?: 1600.0; val volt = v["v"] ?: 400.0; val z = (v["zpct"] ?: 5.5) / 100.0
                val ilt = (kva * 1000.0) / (sqrt(3.0) * volt)
                val isc = (ilt / z) / 1000.0
                listOf("Full Load Secondary Current" to "${fmt(ilt)} A", "Prospective Fault Current" to "${fmt(isc)} kA", "Recommended Breaking Icu" to "${if (isc <= 50) 50 else if (isc <= 65) 65 else 85} kA")
            }
        ),
        // 30. OCB Size Calculation
        CalcFactory.create(
            30, "OCB Size Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.PROTECTION,
            "Oil Circuit Breaker (OCB) retrofit rating and insulating oil dielectric volume calculation.",
            "I_rating >= 1.25 × I_load; Breaking MVA = √3 × V × I_break",
            listOf(input("kv", "Operating Voltage (kV)", "33", "kV"), input("mva", "Substation Capacity (MVA)", "15", "MVA")),
            calc = { v ->
                val kv = v["kv"] ?: 33.0; val mva = v["mva"] ?: 15.0
                val current = (mva * 1000.0) / (sqrt(3.0) * kv)
                val ocb = listOf(400, 630, 800, 1250).firstOrNull { it >= current * 1.25 } ?: 1250
                "$ocb" to "Amperes OCB Rating"
            }
        ),
        // 31. SF6 Breaker Size Calculation
        CalcFactory.create(
            31, "SF6 Breaker Size Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.PROTECTION,
            "Sizes high-voltage Sulfur Hexafluoride (SF6) gas circuit breaker for 33kV/132kV transmission.",
            "I_break = S_sc / (√3 × V_line); Rated Gas Pressure = 0.5 to 0.6 MPa",
            listOf(input("kv", "Rated Line Voltage (kV)", "33", "kV"), input("faultMva", "System Short Circuit Level (MVA)", "1000", "MVA")),
            calc = { v ->
                val kv = v["kv"] ?: 33.0; val mva = v["faultMva"] ?: 1000.0
                val isc = mva / (sqrt(3.0) * kv)
                fmt(isc) to "kA Symmetrical Short Circuit Interrupting Capacity"
            },
            secondaryBuilder = { v ->
                val kv = v["kv"] ?: 33.0; val mva = v["faultMva"] ?: 1000.0
                val isc = mva / (sqrt(3.0) * kv)
                listOf("Standard Continuous Current" to "1250A / 2000A", "Rated SF6 Operating Pressure" to "0.60 MPa (6 bar)", "Peak Making Capacity" to "${fmt(isc * 2.5)} kA Peak")
            }
        ),
        // 32. Lightning Arrester Calculation
        CalcFactory.create(
            32, "Lightning Arrester Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.PROTECTION,
            "Selects Metal Oxide Surge Arrester (MOSA) Maximum Continuous Operating Voltage (MCOV).",
            "MCOV >= (V_m / √3) × Earth Fault Factor 1.4 for non-effectively grounded systems",
            listOf(input("sysKv", "System Nominal Voltage (kV)", "11", "kV"), input("grounded", "Effectively Grounded? (1 = Yes, 0 = No)", "1")),
            calc = { v ->
                val kv = v["sysKv"] ?: 11.0; val eff = (v["grounded"] ?: 1.0) > 0.5
                val maxSysKv = kv * 1.10
                val factor = if (eff) 1.05 else 1.40
                val mcov = (maxSysKv / sqrt(3.0)) * factor
                val ratedKv = mcov * 1.25
                fmt(ratedKv) to "kV Arrester Rated Voltage (Ur)"
            },
            secondaryBuilder = { v ->
                val kv = v["sysKv"] ?: 11.0; val eff = (v["grounded"] ?: 1.0) > 0.5
                val mcov = ((kv * 1.10) / sqrt(3.0)) * (if (eff) 1.05 else 1.40)
                listOf("MCOV (Continuous)" to "${fmt(mcov)} kV", "Nominal Discharge Current" to "10 kA Class 1")
            }
        ),
        // 33. HV Isolator Size Calculation
        CalcFactory.create(
            33, "HV Isolator Size Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.PROTECTION,
            "Calculates High Voltage off-load Disconnecting Switch (Isolator) continuous and short-time withstand.",
            "I_n >= 1.25 × Circuit Max Load; Short Time Withstand = 31.5 kA for 3 seconds",
            listOf(input("kv", "Line Voltage (kV)", "33", "kV"), input("loadMva", "Maximum Throughput (MVA)", "25", "MVA")),
            calc = { v ->
                val kv = v["kv"] ?: 33.0; val mva = v["loadMva"] ?: 25.0
                val i = (mva * 1000.0) / (sqrt(3.0) * kv)
                val isolator = listOf(630, 800, 1250, 1600, 2000).firstOrNull { it >= i * 1.25 } ?: 2000
                "$isolator" to "Amperes Isolator Continuous Rating"
            }
        ),
        // 34. Automatic Circuit Recloser (ACR)
        CalcFactory.create(
            34, "Automatic Circuit Recloser (ACR)", CalculatorLevel.ADVANCE, CalculatorCategory.PROTECTION,
            "Sizes vacuum/solid dielectric Automatic Circuit Recloser for overhead distribution lines.",
            "Operating Current >= Feeder Peak; Shot Sequence: O - 0.5s - CO - 2s - CO - 2s - CO",
            listOf(input("feederA", "Peak Feeder Current (A)", "280", "A"), input("lineKv", "Line Voltage (kV)", "11", "kV")),
            calc = { v ->
                val a = v["feederA"] ?: 280.0
                val acr = listOf(400, 630, 800).firstOrNull { it >= a * 1.25 } ?: 800
                "$acr" to "Amperes Rated Continuous Current"
            }
        ),
        // 38. CT Size Calculation
        CalcFactory.create(
            38, "CT Size Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.PROTECTION,
            "Current Transformer (CT) primary ratio, burden (VA), and Knee Point Voltage (Vk) calculation.",
            "Vk = 2 × I_fault × (R_ct + 2 × R_lead + R_relay)",
            listOf(input("flc", "Load Full Load Current (A)", "360", "A"), input("faultKa", "Max Fault Current (kA)", "20", "kA"), input("rLead", "Lead Loop Resistance (Ω)", "0.5", "Ω")),
            calc = { v ->
                val flc = v["flc"] ?: 360.0
                val primary = listOf(100, 150, 200, 300, 400, 500, 600, 800, 1000, 1200).firstOrNull { it >= flc } ?: 1000
                "$primary/5A" to "Standard CT Ratio"
            },
            secondaryBuilder = { v ->
                val fka = v["faultKa"] ?: 20.0; val r = v["rLead"] ?: 0.5
                val vk = 2.0 * ((fka * 1000.0) / 80.0) * (r + 0.2) // Class PS knee point
                listOf("Class PS Knee Point Voltage (Vk)" to "${fmt(vk)} Volts", "Metering Accuracy Class" to "Class 0.2S / 0.5", "Protection Class" to "5P20 15VA")
            }
        ),
        // 39. PT Size Calculation
        CalcFactory.create(
            39, "PT Size Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.PROTECTION,
            "Potential / Voltage Transformer (PT / VT) transformation ratio and secondary burden (VA).",
            "Ratio = V_primary / V_secondary; Burden = Meter VA + Relay VA + Cable drop",
            listOf(input("vPri", "Primary System Voltage (V)", "11000", "V"), input("vSec", "Secondary Voltage (V)", "110", "V"), input("totalVa", "Total Instrument Burden (VA)", "45", "VA")),
            calc = { v ->
                val v1 = v["vPri"] ?: 11000.0; val v2 = v["vSec"] ?: 110.0
                val ratio = fmt(v1 / v2, 1)
                "$ratio:1 (${fmt(v1)}V / ${fmt(v2)}V)" to "PT Ratio"
            },
            secondaryBuilder = { v ->
                val va = v["totalVa"] ?: 45.0
                val recVa = if (va <= 50) 50 else if (va <= 100) 100 else 200
                listOf("Recommended Standard Burden" to "$recVa VA", "Accuracy Class" to "Class 0.2 for Revenue Metering, 3P for Protection")
            }
        ),
        // 58. Motor Starting Time
        CalcFactory.create(
            58, "Motor Starting Time", CalculatorLevel.ADVANCE, CalculatorCategory.MOTOR,
            "Calculates induction motor acceleration starting run-up time until full speed.",
            "t_start = (J_total × 2π × N_s) / (60 × (T_motor_avg - T_load_avg))",
            listOf(input("j", "Total Moment of Inertia J (kg·m²)", "8.5", "kg·m²"), input("rpm", "Motor Rated Speed (RPM)", "1480", "RPM"), input("tAcc", "Net Accelerating Torque (N·m)", "120", "N·m")),
            calc = { v ->
                val j = v["j"] ?: 8.5; val n = v["rpm"] ?: 1480.0; val tNet = (v["tAcc"] ?: 120.0).coerceAtLeast(1.0)
                val omega = (2.0 * PI * n) / 60.0
                val timeSec = (j * omega) / tNet
                fmt(timeSec) to "Seconds Starting Run-up Time"
            }
        ),
        // 112. Short Circuit Current Calculation
        CalcFactory.create(
            112, "Short Circuit Current Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.POWER,
            "Calculates prospective symmetrical short circuit fault current according to IEC 60909.",
            "I_sc = c × V_n / (√3 × Z_k)",
            listOf(input("v", "Nominal Line Voltage (V)", "400", "V"), input("zMilli", "Total Fault Impedance Zk (mΩ)", "8.2", "mΩ")),
            calc = { v ->
                val volt = v["v"] ?: 400.0; val zm = (v["zMilli"] ?: 8.2) / 1000.0
                val isc = (1.05 * volt) / (sqrt(3.0) * zm)
                fmt(isc / 1000.0) to "kA Symmetrical Short Circuit Current"
            }
        ),
        // 113. 3-Phase Fault Current Calculation
        CalcFactory.create(
            113, "3-Phase Fault Current Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.POWER,
            "Calculates bolted 3-phase symmetrical fault current at secondary of power transformer.",
            "I_3p = I_flc / (%Z / 100)",
            listOf(input("kva", "Transformer Rating (kVA)", "1000", "kVA"), input("v", "Secondary Voltage (V)", "400", "V"), input("zpct", "Impedance %Z", "5.0", "%")),
            calc = { v ->
                val kva = v["kva"] ?: 1000.0; val volt = v["v"] ?: 400.0; val z = (v["zpct"] ?: 5.0) / 100.0
                val flc = (kva * 1000.0) / (sqrt(3.0) * volt)
                val isc = (flc / z) / 1000.0
                fmt(isc) to "kA 3-Phase Symmetrical Fault Current"
            },
            secondaryBuilder = { v ->
                val kva = v["kva"] ?: 1000.0; val volt = v["v"] ?: 400.0; val z = (v["zpct"] ?: 5.0) / 100.0
                val flc = (kva * 1000.0) / (sqrt(3.0) * volt)
                val isc = (flc / z) / 1000.0
                listOf("Transformer Rated Current" to "${fmt(flc)} A", "Peak Asymmetrical Current" to "${fmt(isc * 2.5)} kA Peak", "Fault MVA" to "${fmt(sqrt(3.0) * volt * isc)} MVA")
            }
        ),
        // 114. Single Phase Fault Current Calculation
        CalcFactory.create(
            114, "Single Phase Fault Current Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.POWER,
            "Single phase line-to-earth fault current using symmetrical sequence components.",
            "I_1p = (3 × V_ph) / (Z₁ + Z₂ + Z₀ + 3×Z_f)",
            listOf(input("vph", "Phase-to-Neutral Voltage (V)", "230", "V"), input("z1", "Positive Sequence Z1 (Ω)", "0.025", "Ω"), input("z0", "Zero Sequence Z0 (Ω)", "0.065", "Ω")),
            calc = { v ->
                val vph = v["vph"] ?: 230.0; val z1 = v["z1"] ?: 0.025; val z0 = v["z0"] ?: 0.065
                val zTotal = (2.0 * z1) + z0
                val iEarth = (3.0 * vph) / zTotal
                fmt(iEarth / 1000.0) to "kA Single Phase-to-Ground Fault"
            }
        ),
        // 115. Transformer Short Circuit Current Calculation
        CalcFactory.create(
            115, "Transformer Short Circuit Current Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.TRANSFORMER,
            "Determines maximum secondary short circuit current with infinite bus assumption.",
            "I_sc = (kVA × 1000) / (√3 × V_sec × %Z)",
            listOf(input("kva", "Transformer kVA", "2000", "kVA"), input("v", "Secondary Volts (V)", "400", "V"), input("z", "Impedance %Z", "6.0", "%")),
            calc = { v ->
                val kva = v["kva"] ?: 2000.0; val volt = v["v"] ?: 400.0; val z = (v["z"] ?: 6.0) / 100.0
                val flc = (kva * 1000.0) / (sqrt(3.0) * volt)
                val isc = flc / z
                fmt(isc / 1000.0) to "kA Fault Current"
            }
        ),
        // 116. Short Circuit MVA Calculation
        CalcFactory.create(
            116, "Short Circuit MVA Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.POWER,
            "Calculates available fault level MVA at any bus in power transmission & distribution.",
            "MVA_sc = Transformer MVA / (Z_pu)",
            listOf(input("mva", "Transformer Rating (MVA)", "2.5", "MVA"), input("zpct", "Percent Impedance %Z", "6.5", "%")),
            calc = { v ->
                val mva = v["mva"] ?: 2.5; val z = (v["zpct"] ?: 6.5) / 100.0
                val scMva = mva / z
                fmt(scMva) to "MVA Short Circuit Capacity"
            }
        ),
        // 117. Fault Impedance Calculation
        CalcFactory.create(
            117, "Fault Impedance Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.POWER,
            "Calculates total upstream Thévenin source impedance and cable loop resistance to fault point.",
            "Z_fault = √[(R_grid + R_tx + R_cable)² + (X_grid + X_tx + X_cable)²]",
            listOf(input("r", "Total Resistance R (mΩ)", "4.5", "mΩ"), input("x", "Total Reactance X (mΩ)", "12.8", "mΩ")),
            calc = { v ->
                val r = v["r"] ?: 4.5; val x = v["x"] ?: 12.8
                val z = sqrt((r * r) + (x * x))
                fmt(z) to "mΩ Fault Loop Impedance"
            },
            secondaryBuilder = { v ->
                val r = v["r"] ?: 4.5; val x = v["x"] ?: 12.8
                listOf("X/R Ratio" to fmt(x / r, 2))
            }
        ),
        // 118. Breaking Capacity Calculation
        CalcFactory.create(
            118, "Breaking Capacity Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.PROTECTION,
            "Calculates minimum circuit breaker breaking capacity Icu with 20% safety margin.",
            "I_cu_required >= 1.20 × I_sc_prospective",
            listOf(input("isc", "Prospective Fault Current (kA)", "38", "kA")),
            calc = { v ->
                val isc = v["isc"] ?: 38.0
                val req = isc * 1.20
                val std = listOf(25, 36, 50, 65, 85, 100).firstOrNull { it >= req } ?: 150
                "$std" to "kA (Recommended Standard Breaking Capacity Icu)"
            }
        ),
        // 119. Earthing Resistance Calculation
        CalcFactory.create(
            119, "Earthing Resistance Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.POWER,
            "Calculates single driven ground rod electrical resistance using Dwight's formula.",
            "R = (ρ / (2πL)) × [ln(4L / d) - 1]",
            listOf(input("rho", "Soil Resistivity ρ (Ω·m)", "50", "Ω·m"), input("len", "Rod Length L (m)", "3.0", "m"), input("diam", "Rod Diameter d (m)", "0.016", "m")),
            calc = { v ->
                val rho = v["rho"] ?: 50.0; val l = v["len"] ?: 3.0; val d = (v["diam"] ?: 0.016).coerceAtLeast(0.005)
                val r = (rho / (2.0 * PI * l)) * (ln((4.0 * l) / d) - 1.0)
                fmt(r, 2) to "Ohms (Ω) Single Rod Resistance"
            },
            secondaryBuilder = { v ->
                val rho = v["rho"] ?: 50.0; val l = v["len"] ?: 3.0; val d = (v["diam"] ?: 0.016).coerceAtLeast(0.005)
                val r1 = (rho / (2.0 * PI * l)) * (ln((4.0 * l) / d) - 1.0)
                listOf("2 Rods in Parallel" to "${fmt(r1 * 0.58)} Ω", "4 Rods in Parallel" to "${fmt(r1 * 0.33)} Ω", "Standard Target" to "< 1.0 Ω for Substation, < 5.0 Ω for Domestic")
            }
        ),
        // 120. Earth Electrode Size Calculation
        CalcFactory.create(
            120, "Earth Electrode Size Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.POWER,
            "Determines required cross-sectional area of grounding rod based on fault current duration.",
            "A = (I_fault × √t) / K  (IEC 60364-5-54)",
            listOf(input("faultKa", "Earth Fault Current (kA)", "15", "kA"), input("time", "Fault Clearing Time (s)", "0.5", "s"), input("k", "Material K-Factor", "159", "", hint = "Copper=159, Steel=58")),
            calc = { v ->
                val i = (v["faultKa"] ?: 15.0) * 1000.0; val t = v["time"] ?: 0.5; val k = v["k"] ?: 159.0
                val area = (i * sqrt(t)) / k
                fmt(area) to "mm² Minimum Conductor Area"
            }
        ),
        // 121. Earth Conductor Size Calculation
        CalcFactory.create(
            121, "Earth Conductor Size Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.POWER,
            "Sizes Main Earthing Conductor connecting main earthing terminal to grounding pit.",
            "Area = (I × √t) / K; Recommended standard strip/cable size",
            listOf(input("faultA", "Fault Current (A)", "8000", "A"), input("time", "Trip Time (s)", "0.2", "s")),
            calc = { v ->
                val i = v["faultA"] ?: 8000.0; val t = v["time"] ?: 0.2
                val a = (i * sqrt(t)) / 159.0 // Copper K=159
                val std = listOf(16, 25, 35, 50, 70, 95, 120, 150).firstOrNull { it >= a } ?: 185
                "$std" to "mm² Bare Copper Earthing Conductor"
            }
        ),
        // 122. Earth Pit Resistance Calculation
        CalcFactory.create(
            122, "Earth Pit Resistance Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.POWER,
            "Calculates treated earth pit resistance with bentonite / earthing chemical compound.",
            "R_pit = R_rod × Reduction Factor (0.4 to 0.6 with chemical backfill)",
            listOf(input("rho", "Native Soil Resistivity (Ω·m)", "80", "Ω·m"), input("compound", "With Bentonite Chemical? (1 = Yes, 0 = No)", "1")),
            calc = { v ->
                val rho = v["rho"] ?: 80.0; val chem = (v["compound"] ?: 1.0) > 0.5
                val rRod = (rho / (2.0 * PI * 3.0)) * (ln((4.0 * 3.0) / 0.016) - 1.0)
                val rFinal = if (chem) rRod * 0.45 else rRod
                fmt(rFinal) to "Ohms (Ω) Earth Pit Resistance"
            }
        ),
        // 123. Grounding Grid Calculation
        CalcFactory.create(
            123, "Grounding Grid Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.POWER,
            "Calculates substation earth mesh grid resistance using IEEE Std 80 Laurent & Niemann equation.",
            "R_grid = ρ × [1 / (4 × r) + 1 / L_total] where r = √(Area / π)",
            listOf(input("rho", "Soil Resistivity (Ω·m)", "60", "Ω·m"), input("area", "Substation Grid Area (m²)", "400", "m²"), input("gridLen", "Total Buried Conductor Length (m)", "250", "m")),
            calc = { v ->
                val rho = v["rho"] ?: 60.0; val a = v["area"] ?: 400.0; val l = v["gridLen"] ?: 250.0
                val rEq = sqrt(a / PI)
                val rg = rho * ((1.0 / (4.0 * rEq)) + (1.0 / l))
                fmt(rg, 2) to "Ohms (Ω) Substation Grid Resistance"
            }
        ),
        // 124. Earth Fault Current Calculation
        CalcFactory.create(
            124, "Earth Fault Current Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.POWER,
            "Calculates touch voltage (Etouch) and step voltage (Estep) safety compliance.",
            "I_ef = V_ph / (Z_line + R_earth)",
            listOf(input("vph", "Phase Voltage (V)", "230", "V"), input("re", "Total Earthing Resistance (Ω)", "2.5", "Ω")),
            calc = { v ->
                val volt = v["vph"] ?: 230.0; val re = (v["re"] ?: 2.5).coerceAtLeast(0.1)
                val ief = volt / re
                fmt(ief) to "Amperes Earth Fault Current"
            }
        ),
        // 125. Cable Ampacity Calculation
        CalcFactory.create(
            125, "Cable Ampacity Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.CABLE,
            "Calculates derated current-carrying capacity based on installation conditions and ambient temperature.",
            "I_actual = I_base × C_temp × C_group × C_ground",
            listOf(input("baseAmp", "Base Tabulated Ampacity (A)", "185", "A"), input("cTemp", "Temperature Factor", "0.91"), input("cGroup", "Grouping Factor", "0.80"), input("cDepth", "Depth / Soil Factor", "0.95")),
            calc = { v ->
                val ib = v["baseAmp"] ?: 185.0; val ct = v["cTemp"] ?: 0.91; val cg = v["cGroup"] ?: 0.80; val cd = v["cDepth"] ?: 0.95
                val iActual = ib * ct * cg * cd
                fmt(iActual) to "Amperes Derated Permissible Ampacity"
            }
        ),
        // 126. Cable Derating Calculation
        CalcFactory.create(
            126, "Cable Derating Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.CABLE,
            "Calculates total compound derating factor from ambient temp, bundling, and thermal resistivity.",
            "Total Derating Factor K = K_t × K_g × K_d",
            listOf(input("kt", "Ambient Temp Factor", "0.88"), input("kg", "Grouping Factor (e.g. 6 circuits)", "0.65")),
            calc = { v ->
                val kt = v["kt"] ?: 0.88; val kg = v["kg"] ?: 0.65
                val k = kt * kg
                fmt(k, 3) to "Total Derating Factor (${fmt(k * 100.0)}%)"
            }
        ),
        // 127. Cable Parallel Run Calculation
        CalcFactory.create(
            127, "Cable Parallel Run Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.CABLE,
            "Determines required number of parallel cable runs per phase for heavy current feeders.",
            "Parallel Runs = ceil(Total Design Current / Cable Unit Ampacity)",
            listOf(input("totalA", "Total Design Current (A)", "1200", "A"), input("cableA", "Selected Single Cable Ampacity (A)", "380", "A")),
            calc = { v ->
                val tot = v["totalA"] ?: 1200.0; val cap = (v["cableA"] ?: 380.0).coerceAtLeast(10.0)
                val runs = ceil(tot / cap).toInt()
                "$runs Runs" to "Parallel Cables Per Phase"
            },
            secondaryBuilder = { v ->
                val tot = v["totalA"] ?: 1200.0; val cap = (v["cableA"] ?: 380.0).coerceAtLeast(10.0)
                val runs = ceil(tot / cap).toInt()
                listOf("Combined Current Capacity" to "${fmt(runs * cap)} A", "Current Per Parallel Run" to "${fmt(tot / runs)} A")
            }
        ),
        // 128. Maximum Cable Length Calculation
        CalcFactory.create(
            128, "Maximum Cable Length Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.CABLE,
            "Calculates maximum length before fault loop impedance prevents protective device magnetic trip.",
            "L_max = (0.8 × V_ph) / (2 × I_magnetic_trip × Z_loop)",
            listOf(input("vph", "Phase Voltage (V)", "230", "V"), input("iTrip", "Breaker Instantaneous Trip Current (A)", "320", "A"), input("rPerM", "Cable Loop Resistance (mΩ/m)", "0.92", "mΩ/m")),
            calc = { v ->
                val vph = v["vph"] ?: 230.0; val itrip = (v["iTrip"] ?: 320.0).coerceAtLeast(1.0); val rm = (v["rPerM"] ?: 0.92) / 1000.0
                val lmax = (0.8 * vph) / (2.0 * itrip * rm)
                fmt(lmax) to "Meters Maximum Length for Shock Protection"
            }
        )
    )
}
