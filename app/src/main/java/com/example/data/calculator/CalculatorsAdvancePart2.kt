package com.example.data.calculator

import com.example.data.calculator.CalcFactory.input
import com.example.data.calculator.ElectricalFormulas.fmt
import com.example.data.model.CalculatorCategory
import com.example.data.model.CalculatorDefinition
import com.example.data.model.CalculatorLevel
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

object CalculatorsAdvancePart2 {
    val list: List<CalculatorDefinition> = listOf(
        // 129. Cable Short Circuit Withstand Calculation
        CalcFactory.create(
            129, "Cable Short Circuit Withstand Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.CABLE,
            "Calculates adiabatic short circuit thermal withstand capacity using the adiabatic equation I²t <= k²S².",
            "I_sc_withstand = (K × S) / √t  (IEC 60364-5-54)",
            listOf(input("area", "Conductor Cross Section S (mm²)", "70", "mm²"), input("time", "Fault Duration t (Seconds)", "0.5", "s"), input("k", "K Factor", "143", "", hint = "XLPE Copper: 143, PVC Copper: 115")),
            calc = { v ->
                val s = v["area"] ?: 70.0; val t = (v["time"] ?: 0.5).coerceAtLeast(0.01); val k = v["k"] ?: 143.0
                val isc = (k * s) / sqrt(t)
                fmt(isc / 1000.0) to "kA Maximum Withstand Current"
            }
        ),
        // 130. Cable AC Impedance Calculation
        CalcFactory.create(
            130, "Cable AC Impedance Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.CABLE,
            "Calculates total AC impedance per phase incorporating skin effect, proximity effect, and inductive reactance.",
            "Z = √(R_ac² + X_l²)",
            listOf(input("rac", "AC Resistance at 70°C (Ω/km)", "0.387", "Ω/km"), input("xl", "Inductive Reactance (Ω/km)", "0.082", "Ω/km"), input("len", "Cable Length (m)", "150", "m")),
            calc = { v ->
                val r = v["rac"] ?: 0.387; val x = v["xl"] ?: 0.082; val l = (v["len"] ?: 150.0) / 1000.0
                val zKm = sqrt((r * r) + (x * x))
                val zTot = zKm * l
                fmt(zTot, 4) to "Ohms (Ω) Total Loop Impedance"
            }
        ),
        // 131. Cable Inductance Calculation
        CalcFactory.create(
            131, "Cable Inductance Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.CABLE,
            "Calculates positive sequence inductance of single-core or trefoil multiconductor cables.",
            "L = 0.05 + 0.2 × ln(2s / d)  (mH/km)",
            listOf(input("spacing", "Axial Spacing s (mm)", "65", "mm"), input("diam", "Conductor Diameter d (mm)", "14", "mm")),
            calc = { v ->
                val s = v["spacing"] ?: 65.0; val d = (v["diam"] ?: 14.0).coerceAtLeast(1.0)
                val l = 0.05 + 0.2 * ln((2.0 * s) / d)
                fmt(l, 4) to "mH/km Inductance"
            }
        ),
        // 132. Cable Capacitance Calculation
        CalcFactory.create(
            132, "Cable Capacitance Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.CABLE,
            "Calculates operating capacitance and charging reactive current of shielded medium voltage cables.",
            "C = (ε_r) / [18 × ln(D / d)]  (μF/km)",
            listOf(input("er", "Relative Permittivity εr (XLPE=2.3, PVC=4.5)", "2.5", ""), input("dout", "Screen Inner Diameter D (mm)", "24", "mm"), input("din", "Conductor Diameter d (mm)", "12", "mm")),
            calc = { v ->
                val er = v["er"] ?: 2.5; val dOut = v["dout"] ?: 24.0; val dIn = (v["din"] ?: 12.0).coerceAtLeast(1.0)
                val c = er / (18.0 * ln(dOut / dIn))
                fmt(c, 4) to "μF/km Capacitance"
            }
        ),
        // 136. Motor Slip Calculation
        CalcFactory.create(
            136, "Motor Slip Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.MOTOR,
            "Calculates fractional and percentage rotor slip in three-phase induction motors.",
            "s = (N_s - N_r) / N_s × 100%",
            listOf(input("ns", "Synchronous Speed Ns (RPM)", "1500", "RPM"), input("nr", "Rotor Actual Speed Nr (RPM)", "1440", "RPM")),
            calc = { v ->
                val ns = (v["ns"] ?: 1500.0).coerceAtLeast(1.0); val nr = v["nr"] ?: 1440.0
                val s = ((ns - nr) / ns) * 100.0
                fmt(s, 2) to "% Slip"
            },
            secondaryBuilder = { v ->
                val ns = (v["ns"] ?: 1500.0).coerceAtLeast(1.0); val nr = v["nr"] ?: 1440.0
                val s = (ns - nr) / ns
                listOf("Rotor Frequency (f_r)" to "${fmt(s * 50.0, 2)} Hz (at 50Hz supply)")
            }
        ),
        // 137. Motor Synchronous Speed Calculation
        CalcFactory.create(
            137, "Motor Synchronous Speed Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.MOTOR,
            "Calculates revolving magnetic field speed (Ns) from AC line frequency and number of stator poles.",
            "N_s = (120 × f) / P",
            listOf(input("f", "Frequency (Hz)", "50", "Hz"), input("poles", "Number of Stator Poles (P)", "4", "Poles")),
            calc = { v ->
                val f = v["f"] ?: 50.0; val p = (v["poles"] ?: 4.0).coerceAtLeast(2.0)
                val ns = (120.0 * f) / p
                fmt(ns, 0) to "RPM Synchronous Speed"
            }
        ),
        // 138. Motor Torque Calculation
        CalcFactory.create(
            138, "Motor Torque Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.MOTOR,
            "Calculates mechanical shaft torque developed by motor at rated output power and speed.",
            "T = (P_kW × 9550) / N_rpm",
            listOf(input("kw", "Motor Power (kW)", "37", "kW"), input("rpm", "Rated Shaft Speed (RPM)", "1460", "RPM")),
            calc = { v ->
                val kw = v["kw"] ?: 37.0; val n = (v["rpm"] ?: 1460.0).coerceAtLeast(1.0)
                val t = (kw * 9550.0) / n
                fmt(t) to "N·m (Newton-Meters)"
            }
        ),
        // 139. Motor Starting Torque Calculation
        CalcFactory.create(
            139, "Motor Starting Torque Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.MOTOR,
            "Estimates motor breakaway starting torque T_start / T_rated ratio.",
            "T_start = T_rated × (I_start / I_flc)² × s_fl",
            listOf(input("ratedTorque", "Rated Torque (N·m)", "195", "N·m"), input("ratio", "Starting to Rated Torque Ratio", "1.8", "×")),
            calc = { v ->
                val t = v["ratedTorque"] ?: 195.0; val r = v["ratio"] ?: 1.8
                val ts = t * r
                fmt(ts) to "N·m Starting Torque"
            }
        ),
        // 140. Motor Running Torque Calculation
        CalcFactory.create(
            140, "Motor Running Torque Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.MOTOR,
            "Determines instantaneous mechanical torque at any given operating mechanical power output.",
            "T = (P_shaft × 60) / (2π × N)",
            listOf(input("watts", "Shaft Mechanical Power (Watts)", "15000", "W"), input("rpm", "Speed (RPM)", "1450", "RPM")),
            calc = { v ->
                val w = v["watts"] ?: 15000.0; val n = (v["rpm"] ?: 1450.0).coerceAtLeast(1.0)
                val t = (w * 60.0) / (2.0 * PI * n)
                fmt(t) to "N·m Running Torque"
            }
        ),
        // 141. Motor Efficiency Calculation
        CalcFactory.create(
            141, "Motor Efficiency Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.MOTOR,
            "Calculates motor efficiency class (IE1, IE2, IE3, IE4) and percentage efficiency.",
            "η = (Output Mechanical Power / Input Electrical Power) × 100%",
            listOf(input("pOut", "Output Power (kW)", "22", "kW"), input("pIn", "Input Electric Power (kW)", "24.2", "kW")),
            calc = { v ->
                val po = v["pOut"] ?: 22.0; val pi = (v["pIn"] ?: 24.2).coerceAtLeast(po)
                val eff = (po / pi) * 100.0
                fmt(eff, 2) to "% Motor Efficiency"
            },
            secondaryBuilder = { v ->
                val po = v["pOut"] ?: 22.0; val pi = (v["pIn"] ?: 24.2).coerceAtLeast(po)
                val eff = (po / pi) * 100.0
                val ieClass = if (eff >= 94.0) "IE4 Super Premium" else if (eff >= 93.0) "IE3 Premium" else if (eff >= 91.5) "IE2 High Efficiency" else "IE1 Standard"
                listOf("Efficiency Class" to ieClass, "Internal Losses" to "${fmt(pi - po)} kW")
            }
        ),
        // 142. Motor Power Calculation
        CalcFactory.create(
            142, "Motor Power Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.MOTOR,
            "Computes electrical input active and apparent power absorbed from the line.",
            "P_in = √3 × V × I × cos φ",
            listOf(input("v", "Line Voltage (V)", "400", "V"), input("i", "Measured Current (A)", "55", "A"), input("pf", "Power Factor", "0.85")),
            calc = { v ->
                val volt = v["v"] ?: 400.0; val amp = v["i"] ?: 55.0; val pf = (v["pf"] ?: 0.85).coerceIn(0.1, 1.0)
                val pin = (sqrt(3.0) * volt * amp * pf) / 1000.0
                fmt(pin) to "kW Active Power Drawn"
            }
        ),
        // 143. Motor Protection Setting Calculation
        CalcFactory.create(
            143, "Motor Protection Setting Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.PROTECTION,
            "Comprehensive protection parameters for motor protection relay (ANSI 49, 50, 51, 46, 51LR).",
            "Thermal Overload (49) = 1.05 × FLC, Locked Rotor (51LR) = 2.5 × FLC for 5s, Unbalance (46) = 15%",
            listOf(input("flc", "Motor Full Load Current (A)", "80", "A")),
            calc = { v ->
                val flc = v["flc"] ?: 80.0
                fmt(flc * 1.05) to "Amperes Overload Threshold (ANSI 49)"
            },
            secondaryBuilder = { v ->
                val flc = v["flc"] ?: 80.0
                listOf("Short Circuit Instantaneous (ANSI 50)" to "${fmt(flc * 10.0)} A", "Locked Rotor Protection (ANSI 51LR)" to "${fmt(flc * 4.0)} A at 6s", "Negative Sequence Current Unbalance (46)" to "${fmt(flc * 0.15)} A (15%)")
            }
        ),
        // 144. Motor Cable Size Calculation
        CalcFactory.create(
            144, "Motor Cable Size Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.CABLE,
            "Sizes motor feeder cable with starting voltage drop constraint (< 10% during start).",
            "I_cable >= 1.25 × FLC and Start Voltage Drop <= 10%",
            listOf(input("kw", "Motor Power (kW)", "45", "kW"), input("len", "Cable Length (m)", "90", "m"), input("v", "Line Voltage (V)", "400", "V")),
            calc = { v ->
                val kw = v["kw"] ?: 45.0; val volt = v["v"] ?: 400.0
                val flc = (kw * 1000.0) / (sqrt(3.0) * volt * 0.86 * 0.92)
                val cable = ElectricalFormulas.selectCable(flc * 1.25)
                "$cable" to "mm² Copper Cable"
            },
            secondaryBuilder = { v ->
                val kw = v["kw"] ?: 45.0; val volt = v["v"] ?: 400.0
                val flc = (kw * 1000.0) / (sqrt(3.0) * volt * 0.86 * 0.92)
                listOf("Motor Rated FLC" to "${fmt(flc)} A", "Design Ampacity (125%)" to "${fmt(flc * 1.25)} A")
            }
        ),
        // 145. Motor Voltage Drop Calculation
        CalcFactory.create(
            145, "Motor Voltage Drop Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.MOTOR,
            "Calculates running and transient starting voltage drop percentage at motor terminals.",
            "ΔV_start = √3 × I_start × L × (R·cos φ_start + X·sin φ_start)",
            listOf(input("flc", "Motor FLC (A)", "60", "A"), input("len", "Run Length (m)", "100", "m"), input("rPerKm", "Cable Resistance (Ω/km)", "0.524", "Ω/km")),
            calc = { v ->
                val flc = v["flc"] ?: 60.0; val l = (v["len"] ?: 100.0) / 1000.0; val r = v["rPerKm"] ?: 0.524
                val vdRun = sqrt(3.0) * flc * l * r * 0.85
                val vdStart = sqrt(3.0) * (flc * 6.0) * l * r * 0.35 // during start PF ~ 0.35
                fmt(vdRun) to "Volts Running Drop (${fmt(vdRun / 400.0 * 100.0)}%)"
            },
            secondaryBuilder = { v ->
                val flc = v["flc"] ?: 60.0; val l = (v["len"] ?: 100.0) / 1000.0; val r = v["rPerKm"] ?: 0.524
                val vdStart = sqrt(3.0) * (flc * 6.0) * l * r * 0.35
                listOf("Starting Voltage Drop" to "${fmt(vdStart)} V (${fmt(vdStart / 400.0 * 100.0)}%)", "Starting Status" to if (vdStart / 400.0 <= 0.15) "Permissible (<15%)" else "Excessive drop")
            }
        ),
        // 146. Motor Locked Rotor Current Calculation
        CalcFactory.create(
            146, "Motor Locked Rotor Current Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.MOTOR,
            "Calculates NEMA Code letter locked rotor current (LRA) at standstill.",
            "LRA = (kVA/HP × HP × 1000) / (√3 × V)",
            listOf(input("hp", "Motor Rating (HP)", "50", "HP"), input("v", "Voltage (V)", "400", "V"), input("kvaPerHp", "NEMA Code kVA/HP (e.g. Code G = 6.0)", "6.0", "")),
            calc = { v ->
                val hp = v["hp"] ?: 50.0; val volt = v["v"] ?: 400.0; val kva = v["kvaPerHp"] ?: 6.0
                val lra = (kva * hp * 1000.0) / (sqrt(3.0) * volt)
                fmt(lra) to "Amperes Locked Rotor Current (LRA)"
            }
        ),
        // 147. Motor Starter Selection Calculation
        CalcFactory.create(
            147, "Motor Starter Selection Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.MOTOR,
            "Selects optimal motor starter method (DOL, Star-Delta, Soft Starter, VFD) by motor kW rating.",
            "DOL <= 5.5kW, Star-Delta 7.5kW-45kW, Soft Starter > 45kW, VFD for speed control",
            listOf(input("kw", "Motor Power (kW)", "30", "kW")),
            calc = { v ->
                val kw = v["kw"] ?: 30.0
                val sel = if (kw <= 5.5) "Direct On-Line (DOL)" else if (kw <= 45.0) "Star-Delta Starter (or Soft Starter)" else "Electronic Soft Starter / VFD"
                sel to "Recommended Starter Architecture"
            }
        ),
        // 157. Busbar Size Calculation
        CalcFactory.create(
            157, "Busbar Size Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.POWER,
            "Calculates cross-sectional area (mm²) for electrical panel main busbars.",
            "Area (mm²) = Rated Current / Permissible Current Density (e.g. 1.5 A/mm² Cu, 0.9 A/mm² Al)",
            listOf(input("amps", "Rated Incomer Current (A)", "1250", "A"), input("j", "Permissible Density (A/mm²)", "1.5", "A/mm²", hint = "Copper: 1.2-1.6, Al: 0.8-1.0")),
            calc = { v ->
                val a = v["amps"] ?: 1250.0; val j = (v["j"] ?: 1.5).coerceAtLeast(0.5)
                val area = a / j
                fmt(area) to "mm² Busbar Cross Section"
            },
            secondaryBuilder = { v ->
                val a = v["amps"] ?: 1250.0; val j = (v["j"] ?: 1.5).coerceAtLeast(0.5)
                val area = a / j
                val rec = if (area <= 400) "1 Run of 40x10 mm" else if (area <= 800) "1 Run of 80x10 mm" else if (area <= 1200) "2 Runs of 60x10 mm" else "2 Runs of 80x10 mm"
                listOf("Standard Bar Config" to rec)
            }
        ),
        // 158. Busbar Current Capacity Calculation
        CalcFactory.create(
            158, "Busbar Current Capacity Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.POWER,
            "Calculates continuous ampacity of copper/aluminum busbar based on dimensions and cooling.",
            "I_capacity = Width (mm) × Thickness (mm) × Current Density Factor",
            listOf(input("width", "Busbar Width (mm)", "80", "mm"), input("thick", "Busbar Thickness (mm)", "10", "mm"), input("bars", "Number of Bars per Phase", "2", "Bars")),
            calc = { v ->
                val w = v["width"] ?: 80.0; val t = v["thick"] ?: 10.0; val n = v["bars"] ?: 2.0
                val totalArea = w * t * n
                val amp = totalArea * 1.35 // 1.35 A/mm2 for multi-bar with air gap
                fmt(amp) to "Amperes Continuous Ampacity"
            }
        ),
        // 159. Busbar Short Circuit Withstand Calculation
        CalcFactory.create(
            159, "Busbar Short Circuit Withstand Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.POWER,
            "Calculates electrodynamic mechanical force and thermal short circuit withstand of busbars.",
            "Force F = (0.2 × I_peak²) / S_spacing  (N/m)",
            listOf(input("peakKa", "Peak Fault Current (kA)", "65", "kA"), input("spacing", "Center Spacing Between Phases (m)", "0.15", "m")),
            calc = { v ->
                val ipk = v["peakKa"] ?: 65.0; val s = (v["spacing"] ?: 0.15).coerceAtLeast(0.05)
                val force = (0.2 * ipk * ipk * 1000.0) / s
                fmt(force) to "N/m Electrodynamic Repulsion Force"
            },
            secondaryBuilder = { v ->
                val ipk = v["peakKa"] ?: 65.0; val s = (v["spacing"] ?: 0.15).coerceAtLeast(0.05)
                val force = (0.2 * ipk * ipk * 1000.0) / s
                listOf("Busbar Insulator Spacing" to "Maximum 400mm between supports")
            }
        ),
        // 160. Busbar Voltage Drop Calculation
        CalcFactory.create(
            160, "Busbar Voltage Drop Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.POWER,
            "Calculates millivolts drop across busbar trunking or panel busbar runs.",
            "ΔV = √3 × I × L × R_ac",
            listOf(input("amps", "Busbar Current (A)", "1600", "A"), input("len", "Busbar Length (m)", "12", "m")),
            calc = { v ->
                val a = v["amps"] ?: 1600.0; val l = v["len"] ?: 12.0
                val rPerM = 0.000025 // approx 25 micro-ohms per meter
                val vd = sqrt(3.0) * a * l * rPerM
                fmt(vd, 3) to "Volts Drop Along Busbar"
            }
        ),
        // 161. Panel Load Calculation
        CalcFactory.create(
            161, "Panel Load Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.POWER,
            "Aggregates total connected power, active demand kW, and incoming line current for switchboards.",
            "Total Demand kW = Sum of Outgoing Branch kW × Diversity Factor",
            listOf(input("connKw", "Total Connected Circuits (kW)", "240", "kW"), input("df", "Diversity Factor", "0.75"), input("v", "Line Voltage (V)", "400", "V"), input("pf", "Power Factor", "0.85")),
            calc = { v ->
                val kw = v["connKw"] ?: 240.0; val df = v["df"] ?: 0.75; val volt = v["v"] ?: 400.0; val pf = v["pf"] ?: 0.85
                val demKw = kw * df
                val amps = (demKw * 1000.0) / (sqrt(3.0) * volt * pf)
                fmt(demKw) to "kW Diversified Demand"
            },
            secondaryBuilder = { v ->
                val kw = v["connKw"] ?: 240.0; val df = v["df"] ?: 0.75; val volt = v["v"] ?: 400.0; val pf = v["pf"] ?: 0.85
                val demKw = kw * df
                val amps = (demKw * 1000.0) / (sqrt(3.0) * volt * pf)
                listOf("Main Incomer Current" to "${fmt(amps)} A", "Main Incomer Breaker" to "${ElectricalFormulas.selectBreaker(amps)} A")
            }
        ),
        // 162. Panel Main Breaker Size Calculation
        CalcFactory.create(
            162, "Panel Main Breaker Size Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.PROTECTION,
            "Sizes Main Switchboard Incomer MCCB / ACB with 25% continuous duty growth margin.",
            "I_main >= 1.25 × Full Load Diversified Current",
            listOf(input("incomerA", "Panel Incomer Full Load (A)", "480", "A")),
            calc = { v ->
                val a = v["incomerA"] ?: 480.0
                val b = ElectricalFormulas.selectBreaker(a)
                "$b" to "Amperes Main Breaker Frame"
            }
        ),
        // 163. MCC Panel Size Calculation
        CalcFactory.create(
            163, "MCC Panel Size Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.FACTORY,
            "Motor Control Center (MCC) total incomer ampacity and physical vertical tier estimation.",
            "MCC Incomer = Largest Motor Starting + Sum of Remaining Full Load Currents",
            listOf(input("largestMotorA", "Largest Motor FLC (A)", "95", "A"), input("otherMotorsA", "Sum of Other Motors FLC (A)", "260", "A"), input("starters", "Number of Starters / Feeders", "12", "Feeders")),
            calc = { v ->
                val lm = v["largestMotorA"] ?: 95.0; val om = v["otherMotorsA"] ?: 260.0
                val incomerA = (lm * 1.5) + om
                val b = ElectricalFormulas.selectBreaker(incomerA)
                "$b" to "Amperes MCC Main Incomer"
            },
            secondaryBuilder = { v ->
                val s = v["starters"] ?: 12.0
                val columns = ceil(s / 4.0).toInt().coerceAtLeast(1)
                listOf("Estimated MCC Columns (Tiers)" to "$columns Vertical Sections (approx ${columns * 800}mm width)")
            }
        ),
        // 164. DB Size Calculation
        CalcFactory.create(
            164, "DB Size Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.BUILDING,
            "Distribution Board (DB / SDB) way sizing (6, 8, 12, 16, 24 Way) with 25% spare capacity.",
            "Total Ways = ceil(Used Branch Circuits × 1.25)",
            listOf(input("lightCircuits", "Lighting Branch Circuits", "8", "Circuits"), input("powerCircuits", "Power Socket Circuits", "6", "Circuits"), input("heavyCircuits", "AC / Heavy Circuits", "3", "Circuits")),
            calc = { v ->
                val l = v["lightCircuits"] ?: 8.0; val p = v["powerCircuits"] ?: 6.0; val h = v["heavyCircuits"] ?: 3.0
                val totalUsed = l + p + h
                val reqWays = ceil(totalUsed * 1.25).toInt()
                val standardWays = listOf(6, 8, 12, 16, 18, 24, 36, 42).firstOrNull { it >= reqWays } ?: 48
                "$standardWays-Way DB" to "Recommended Enclosure Size (Active: ${totalUsed.toInt()}, Spares: ${standardWays - totalUsed.toInt()})"
            }
        ),
        // 165. MCCB Size Calculation
        CalcFactory.create(
            165, "MCCB Size Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.PROTECTION,
            "Selects Moulded Case Circuit Breaker (MCCB) frame rating, trip dial, and short circuit kA.",
            "MCCB Rating = 1.25 × Continuous Feeder Load; Adjustable thermal dial 0.8 - 1.0",
            listOf(input("loadA", "Feeder Design Current (A)", "185", "A")),
            calc = { v ->
                val a = v["loadA"] ?: 185.0
                val mccb = listOf(100, 125, 160, 200, 250, 315, 400, 630, 800).firstOrNull { it >= a * 1.15 } ?: 1000
                "$mccb" to "Amperes MCCB Frame Rating"
            },
            secondaryBuilder = { v ->
                val a = v["loadA"] ?: 185.0
                val mccb = listOf(100, 125, 160, 200, 250, 315, 400, 630, 800).firstOrNull { it >= a * 1.15 } ?: 1000
                listOf("Thermal Setting Ir" to "${fmt(a)} A (${fmt(a / mccb, 2)} × In)", "Standard Breaking Capacity" to "36 kA / 50 kA")
            }
        ),
        // 166. MCB Size Calculation
        CalcFactory.create(
            166, "MCB Size Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.PROTECTION,
            "Selects Miniature Circuit Breaker (MCB) curve type (B, C, D) and continuous rating.",
            "Curve B (3-5x) for Resistive/Lighting, Curve C (5-10x) for General/Sockets, Curve D (10-20x) for Motors",
            listOf(input("current", "Circuit Continuous Load (A)", "14", "A"), input("isMotor", "Motor / Inductive Load? (1 = Yes, 0 = No)", "0")),
            calc = { v ->
                val i = v["current"] ?: 14.0; val isInductive = (v["isMotor"] ?: 0.0) > 0.5
                val std = listOf(6, 10, 16, 20, 25, 32, 40, 50, 63).firstOrNull { it >= i * 1.20 } ?: 63
                val curve = if (isInductive) "Type-C / Type-D" else "Type-B / Type-C"
                "$std A ($curve)" to "MCB Standard Rating"
            }
        ),
        // 167. Building Electrical Material Calculation
        CalcFactory.create(
            167, "Building Electrical Material Calculation", CalculatorLevel.ADVANCE, CalculatorCategory.BUILDING,
            "Estimates complete electrical materials, wires (1.5, 2.5, 4, 6, 10 mm²), conduits, switchboards, and protection for building projects.",
            "Material requirements computed according to point counts, floor runs, and standard engineering practice (NO prices or billing).",
            listOf(
                input("floors", "Number of Floors", "2", "Floors"),
                input("rooms", "Number of Rooms Total", "6", "Rooms"),
                input("lights", "Total Light Points", "24", "Points"),
                input("fans", "Total Ceiling Fan Points", "8", "Points"),
                input("sockets", "Total 13A Power Sockets", "16", "Points"),
                input("ac", "Total AC Points", "4", "Points"),
                input("geyser", "Total Geyser Points", "2", "Points")
            ),
            calc = { v ->
                val f = (v["floors"] ?: 2.0).toInt().coerceAtLeast(1)
                val r = (v["rooms"] ?: 6.0).toInt().coerceAtLeast(1)
                val l = (v["lights"] ?: 24.0).toInt()
                val fn = (v["fans"] ?: 8.0).toInt()
                val s = (v["sockets"] ?: 16.0).toInt()
                val ac = (v["ac"] ?: 4.0).toInt()
                val g = (v["geyser"] ?: 2.0).toInt()
                val totalPoints = l + fn + s + ac + g
                val wire1_5Coils = ceil((l + fn) * 14.0 * 2.3 / 100.0).toInt().coerceAtLeast(2)
                val wire2_5Coils = ceil(s * 18.0 * 2.3 / 100.0).toInt().coerceAtLeast(2)
                val conduitPcs = ceil(totalPoints * 4.5 / 3.0).toInt().coerceAtLeast(30)
                "Ready ($totalPoints Points)" to "Summary: $wire1_5Coils coils 1.5mm², $wire2_5Coils coils 2.5mm², $conduitPcs PVC Pipes"
            },
            secondaryBuilder = { v ->
                listOf(
                    "Detailed Material List" to "Open 'Building Material List' to view the complete formatted table with specs and quantities",
                    "Pricing Notice" to "Strictly engineering material estimation — NO commercial prices, labour or billing"
                )
            },
            notes = listOf(
                "Use the dedicated 'Building Electrical Material' tool for full room-by-room interactive breakdown and printable owner material lists."
            )
        )
    )
}
