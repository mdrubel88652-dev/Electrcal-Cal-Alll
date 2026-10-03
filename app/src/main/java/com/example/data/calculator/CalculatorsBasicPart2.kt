package com.example.data.calculator

import com.example.data.calculator.CalcFactory.input
import com.example.data.calculator.ElectricalFormulas.fmt
import com.example.data.model.CalculatorCategory
import com.example.data.model.CalculatorDefinition
import com.example.data.model.CalculatorLevel
import kotlin.math.acos
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlin.math.tan

object CalculatorsBasicPart2 {
    val list: List<CalculatorDefinition> = listOf(
        // 53. Direct Starter Calculation
        CalcFactory.create(
            53, "Direct Starter Calculation", CalculatorLevel.BASIC, CalculatorCategory.MOTOR,
            "Direct Online (DOL) Starter contactor, thermal overload relay, and backup fuse ratings.",
            "Contactor >= FLC, Overload Relay Setting = 1.0 × FLC, Fuse = 2.0 × FLC",
            listOf(input("kw", "Motor Power (kW)", "7.5", "kW"), input("v", "Voltage (V)", "400", "V"), input("pf", "Power Factor", "0.85"), input("eff", "Efficiency", "0.88")),
            calc = { v ->
                val kw = v["kw"] ?: 7.5; val volt = v["v"] ?: 400.0; val pf = v["pf"] ?: 0.85; val eff = v["eff"] ?: 0.88
                val flc = (kw * 1000.0) / (sqrt(3.0) * volt * pf * eff)
                fmt(flc) to "Amperes Full Load Current (FLC)"
            },
            secondaryBuilder = { v ->
                val kw = v["kw"] ?: 7.5; val volt = v["v"] ?: 400.0; val pf = v["pf"] ?: 0.85; val eff = v["eff"] ?: 0.88
                val flc = (kw * 1000.0) / (sqrt(3.0) * volt * pf * eff)
                listOf("Main Contactor" to "${ElectricalFormulas.selectBreaker(flc)} A", "Relay Setting Range" to "${fmt(flc * 0.9)} - ${fmt(flc * 1.1)} A", "Backup Fuse" to "${ElectricalFormulas.selectBreaker(flc * 2.0)} A")
            }
        ),
        // 54. Star Delta Starter Calculation
        CalcFactory.create(
            54, "Star Delta Starter Calculation", CalculatorLevel.BASIC, CalculatorCategory.MOTOR,
            "Calculates Main, Delta, and Star contactor current ratings (0.58 × FLC, 0.33 × FLC).",
            "I_phase = 0.58 × FLC, I_star = 0.33 × FLC",
            listOf(input("kw", "Motor Power (kW)", "30", "kW"), input("v", "Line Voltage (V)", "400", "V")),
            calc = { v ->
                val kw = v["kw"] ?: 30.0; val volt = v["v"] ?: 400.0
                val flc = (kw * 1000.0) / (sqrt(3.0) * volt * 0.85 * 0.9)
                val mainContactor = flc * 0.58
                fmt(mainContactor) to "Amperes (Main & Delta Contactor)"
            },
            secondaryBuilder = { v ->
                val kw = v["kw"] ?: 30.0; val volt = v["v"] ?: 400.0
                val flc = (kw * 1000.0) / (sqrt(3.0) * volt * 0.85 * 0.9)
                listOf("Motor Line FLC" to "${fmt(flc)} A", "Star Contactor Rating" to "${fmt(flc * 0.33)} A", "Overload Relay Setting" to "${fmt(flc * 0.58)} A")
            }
        ),
        // 55. Motor Size Calculation
        CalcFactory.create(
            55, "Motor Size Calculation", CalculatorLevel.BASIC, CalculatorCategory.MOTOR,
            "Calculates required electric motor power rating from mechanical load torque and speed.",
            "Power (kW) = (Torque in N·m × Speed in RPM) / 9550",
            listOf(input("torque", "Required Torque (N·m)", "150", "N·m"), input("rpm", "Shaft Speed (RPM)", "1440", "RPM")),
            calc = { v ->
                val t = v["torque"] ?: 150.0; val n = v["rpm"] ?: 1440.0
                val kw = (t * n) / 9550.0
                fmt(kw) to "kW Motor Power (${fmt(kw / 0.7457)} HP)"
            }
        ),
        // 56. Motor Speed Calculation
        CalcFactory.create(
            56, "Motor Speed Calculation", CalculatorLevel.BASIC, CalculatorCategory.MOTOR,
            "Calculates rotor operational speed from synchronous speed and percentage slip.",
            "N_r = N_s × (1 - s),  N_s = (120 × f) / P",
            listOf(input("freq", "Frequency (Hz)", "50", "Hz"), input("poles", "Number of Poles", "4", "Poles"), input("slip", "Rotor Slip %", "3.5", "%")),
            calc = { v ->
                val f = v["freq"] ?: 50.0; val p = (v["poles"] ?: 4.0).coerceAtLeast(2.0); val s = (v["slip"] ?: 3.5) / 100.0
                val ns = (120.0 * f) / p
                val nr = ns * (1.0 - s)
                fmt(nr, 1) to "RPM Rotor Speed"
            },
            secondaryBuilder = { v ->
                val f = v["freq"] ?: 50.0; val p = (v["poles"] ?: 4.0).coerceAtLeast(2.0)
                listOf("Synchronous Speed Ns" to "${fmt((120.0 * f) / p, 0)} RPM")
            }
        ),
        // 57. Motor Load Protection
        CalcFactory.create(
            57, "Motor Load Protection", CalculatorLevel.BASIC, CalculatorCategory.PROTECTION,
            "Configures motor protection circuit breaker (MPCB) thermal and magnetic trip thresholds.",
            "Thermal Trip = 1.0 × FLC, Magnetic Short Circuit = 10 to 14 × FLC",
            listOf(input("flc", "Motor Full Load Amperes", "45", "A")),
            calc = { v ->
                val flc = v["flc"] ?: 45.0
                fmt(flc) to "Amperes Overload Continuous Setting"
            },
            secondaryBuilder = { v ->
                val flc = v["flc"] ?: 45.0
                listOf("Instantaneous Magnetic Trip" to "${fmt(flc * 12.0)} A", "Locked Rotor Protection" to "Trip within 10s at ${fmt(flc * 6.0)} A")
            }
        ),
        // 59. Motor Starting Current
        CalcFactory.create(
            59, "Motor Starting Current", CalculatorLevel.BASIC, CalculatorCategory.MOTOR,
            "Calculates inrush starting current for DOL, Star-Delta, and Soft Starter methods.",
            "DOL = 6 to 8 × FLC, Star-Delta = 2 to 2.5 × FLC, Soft Starter = 3 to 3.5 × FLC",
            listOf(input("flc", "Motor Full Load Current (A)", "35", "A")),
            calc = { v ->
                val flc = v["flc"] ?: 35.0
                val dol = flc * 6.5
                fmt(dol) to "Amperes (DOL Inrush Starting Current)"
            },
            secondaryBuilder = { v ->
                val flc = v["flc"] ?: 35.0
                listOf("Star-Delta Starting Current" to "${fmt(flc * 2.2)} A", "Soft Starter Inrush" to "${fmt(flc * 3.0)} A", "VFD Inrush (Soft ramp)" to "${fmt(flc * 1.1)} A")
            }
        ),
        // 60. Motor FLC Calculation
        CalcFactory.create(
            60, "Motor FLC Calculation", CalculatorLevel.BASIC, CalculatorCategory.MOTOR,
            "Calculates 3-phase induction motor Full Load Current (FLC) according to IEC 60034.",
            "FLC = (kW × 1000) / (√3 × V × PF × η)",
            listOf(input("kw", "Motor Power (kW)", "18.5", "kW"), input("v", "Line Voltage (V)", "400", "V"), input("pf", "Power Factor", "0.86"), input("eff", "Motor Efficiency (η)", "0.91")),
            calc = { v ->
                val kw = v["kw"] ?: 18.5; val volt = v["v"] ?: 400.0; val pf = v["pf"] ?: 0.86; val eff = v["eff"] ?: 0.91
                val flc = (kw * 1000.0) / (sqrt(3.0) * volt * pf * eff)
                fmt(flc) to "Amperes Full Load Current"
            }
        ),
        // 61. Motor Current Calculation
        CalcFactory.create(
            61, "Motor Current Calculation", CalculatorLevel.BASIC, CalculatorCategory.MOTOR,
            "Calculates actual operating current at partial loading conditions.",
            "I_load = FLC × Load Fraction × (PF_rated / PF_actual)",
            listOf(input("flc", "Rated FLC (A)", "50", "A"), input("pct", "Actual Load %", "75", "%")),
            calc = { v ->
                val flc = v["flc"] ?: 50.0; val pct = (v["pct"] ?: 75.0) / 100.0
                val i = flc * pct * 1.05
                fmt(i) to "Amperes Operating Current"
            }
        ),
        // 62. Motor Voltage Calculation
        CalcFactory.create(
            62, "Motor Voltage Calculation", CalculatorLevel.BASIC, CalculatorCategory.MOTOR,
            "Calculates motor terminal voltage from power, current draw, and power factor.",
            "V = (kW × 1000) / (√3 × I × PF × η)",
            listOf(input("kw", "Motor Power (kW)", "15", "kW"), input("i", "Current (A)", "28", "A"), input("pf", "Power Factor", "0.85"), input("eff", "Efficiency", "0.90")),
            calc = { v ->
                val kw = v["kw"] ?: 15.0; val i = (v["i"] ?: 28.0).coerceAtLeast(0.1); val pf = v["pf"] ?: 0.85; val eff = v["eff"] ?: 0.90
                val volt = (kw * 1000.0) / (sqrt(3.0) * i * pf * eff)
                fmt(volt) to "Volts (Line Voltage)"
            }
        ),
        // 106. Capacitor Bank Size Calculation
        CalcFactory.create(
            106, "Capacitor Bank Size Calculation", CalculatorLevel.BASIC, CalculatorCategory.PFI,
            "Computes total kVAR required for automatic power factor correction (APFC) bank.",
            "kVAR = P × (tan φ1 - tan φ2)",
            listOf(input("kw", "Plant Active Load (kW)", "450", "kW"), input("pf1", "Initial PF", "0.75"), input("pf2", "Target PF", "0.98")),
            calc = { v ->
                val p = v["kw"] ?: 450.0; val pf1 = (v["pf1"] ?: 0.75).coerceIn(0.1, 0.99); val pf2 = (v["pf2"] ?: 0.98).coerceIn(pf1, 1.0)
                val kvar = p * (tan(acos(pf1)) - tan(acos(pf2)))
                fmt(kvar) to "kVAR Total Bank Size"
            },
            secondaryBuilder = { v ->
                val p = v["kw"] ?: 450.0; val pf1 = (v["pf1"] ?: 0.75).coerceIn(0.1, 0.99); val pf2 = (v["pf2"] ?: 0.98).coerceIn(pf1, 1.0)
                val kvar = p * (tan(acos(pf1)) - tan(acos(pf2)))
                val steps = ceil(kvar / 25.0).toInt()
                listOf("Suggested APFC Steps" to "$steps × 25 kVAR Steps", "Harmonic Filter Reactor" to "7% Detuned Reactor Recommended")
            }
        ),
        // 107. Required kVAR Calculation
        CalcFactory.create(
            107, "Required kVAR Calculation", CalculatorLevel.BASIC, CalculatorCategory.PFI,
            "Direct calculation of reactive power compensation (kVAR).",
            "kVAR = P × [√(1 - PF1²)/PF1 - √(1 - PF2²)/PF2]",
            listOf(input("kw", "Active Power (kW)", "120", "kW"), input("pf1", "Present PF", "0.78"), input("pf2", "Desired PF", "0.96")),
            calc = { v ->
                val p = v["kw"] ?: 120.0; val pf1 = (v["pf1"] ?: 0.78).coerceIn(0.1, 0.99); val pf2 = (v["pf2"] ?: 0.96).coerceIn(pf1, 1.0)
                val kvar = p * (tan(acos(pf1)) - tan(acos(pf2)))
                fmt(kvar) to "kVAR"
            }
        ),
        // 108. Existing PF Calculation
        CalcFactory.create(
            108, "Existing PF Calculation", CalculatorLevel.BASIC, CalculatorCategory.PFI,
            "Calculates prevailing plant power factor from active power (kW) and apparent power (kVA).",
            "PF = kW / kVA",
            listOf(input("kw", "Active Power (kW)", "210", "kW"), input("kva", "Apparent Power (kVA)", "280", "kVA")),
            calc = { v ->
                val kw = v["kw"] ?: 210.0; val kva = (v["kva"] ?: 280.0).coerceAtLeast(0.1)
                val pf = (kw / kva).coerceIn(0.0, 1.0)
                fmt(pf, 3) to "Power Factor (cos φ)"
            }
        ),
        // 109. Target PF Calculation
        CalcFactory.create(
            109, "Target PF Calculation", CalculatorLevel.BASIC, CalculatorCategory.PFI,
            "Calculates resulting power factor after adding a known capacitor rating (kVAR).",
            "PF_new = P / √[P² + (Q_old - Q_cap)²]",
            listOf(input("kw", "Active Load (kW)", "200", "kW"), input("pf1", "Initial PF", "0.75"), input("kvar", "Added Capacitor (kVAR)", "110", "kVAR")),
            calc = { v ->
                val p = v["kw"] ?: 200.0; val pf1 = (v["pf1"] ?: 0.75).coerceIn(0.1, 0.99); val qCap = v["kvar"] ?: 110.0
                val q1 = p * tan(acos(pf1))
                val q2 = maxOf(0.0, q1 - qCap)
                val s2 = sqrt((p * p) + (q2 * q2))
                val newPf = if (s2 > 0) p / s2 else 1.0
                fmt(newPf, 3) to "New Power Factor (cos φ)"
            }
        ),
        // 110. Capacitor kVAR Calculation
        CalcFactory.create(
            110, "Capacitor kVAR Calculation", CalculatorLevel.BASIC, CalculatorCategory.PFI,
            "Calculates reactive power generation of a 3-phase capacitor from capacitance (μF) and voltage.",
            "kVAR = (2 × π × f × C_μF × V²) / 10⁹",
            listOf(input("c", "Total Capacitance (μF)", "150", "μF"), input("v", "Voltage (V)", "400", "V"), input("f", "Frequency (Hz)", "50", "Hz")),
            calc = { v ->
                val c = v["c"] ?: 150.0; val volt = v["v"] ?: 400.0; val f = v["f"] ?: 50.0
                val kvar = (2.0 * kotlin.math.PI * f * c * volt * volt) / 1e9
                fmt(kvar) to "kVAR Rating"
            }
        ),
        // 111. Power Factor Correction Calculation
        CalcFactory.create(
            111, "Power Factor Correction Calculation", CalculatorLevel.BASIC, CalculatorCategory.PFI,
            "Calculates line current reduction and utility penalty savings after PFC.",
            "Current Reduction ΔI = I_old - I_new",
            listOf(input("kw", "Active Load (kW)", "300", "kW"), input("v", "Voltage (V)", "400", "V"), input("pf1", "Initial PF", "0.72"), input("pf2", "Target PF", "0.98")),
            calc = { v ->
                val kw = v["kw"] ?: 300.0; val volt = v["v"] ?: 400.0; val pf1 = v["pf1"] ?: 0.72; val pf2 = v["pf2"] ?: 0.98
                val i1 = (kw * 1000.0) / (sqrt(3.0) * volt * pf1)
                val i2 = (kw * 1000.0) / (sqrt(3.0) * volt * pf2)
                val savedAmps = i1 - i2
                fmt(savedAmps) to "Amperes Line Current Saved"
            },
            secondaryBuilder = { v ->
                val kw = v["kw"] ?: 300.0; val volt = v["v"] ?: 400.0; val pf1 = v["pf1"] ?: 0.72; val pf2 = v["pf2"] ?: 0.98
                val i1 = (kw * 1000.0) / (sqrt(3.0) * volt * pf1)
                val i2 = (kw * 1000.0) / (sqrt(3.0) * volt * pf2)
                listOf("Before PFC Current" to "${fmt(i1)} A", "After PFC Current" to "${fmt(i2)} A", "Line Losses Reduction" to "${fmt((1.0 - (i2*i2)/(i1*i1)) * 100.0)} %")
            }
        ),
        // 148. UPS Size Calculation
        CalcFactory.create(
            148, "UPS Size Calculation", CalculatorLevel.BASIC, CalculatorCategory.BATTERY_UPS,
            "Sizes Uninterruptible Power Supply (UPS) with 30% headroom for IT & data center loads.",
            "UPS kVA = (Total Critical kW / PF 0.8) × 1.30",
            listOf(input("kw", "Connected IT Load (kW)", "14", "kW"), input("pf", "Load Power Factor", "0.85")),
            calc = { v ->
                val kw = v["kw"] ?: 14.0; val pf = v["pf"] ?: 0.85
                val upsKva = (kw / pf) * 1.30
                fmt(upsKva) to "kVA Online UPS Size"
            }
        ),
        // 149. UPS Battery Size Calculation
        CalcFactory.create(
            149, "UPS Battery Size Calculation", CalculatorLevel.BASIC, CalculatorCategory.BATTERY_UPS,
            "Calculates battery bank Ah rating for desired backup duration on online UPS.",
            "Ah = (UPS VA × PF × Backup Minutes) / (DC Bus Voltage × Inverter Eff 0.92 × 60)",
            listOf(input("kva", "UPS Rating (kVA)", "20", "kVA"), input("mins", "Backup Time (Minutes)", "30", "Mins"), input("vdc", "DC Bus Voltage (V)", "192", "V")),
            calc = { v ->
                val kva = v["kva"] ?: 20.0; val mins = v["mins"] ?: 30.0; val vdc = v["vdc"] ?: 192.0
                val ah = (kva * 1000.0 * 0.8 * (mins / 60.0)) / (vdc * 0.92)
                fmt(ah) to "Ah Battery Capacity"
            }
        ),
        // 150. Battery Charging Time Calculation
        CalcFactory.create(
            150, "Battery Charging Time Calculation", CalculatorLevel.BASIC, CalculatorCategory.BATTERY_UPS,
            "Estimates full recharge hours based on charger current and battery Coulombic efficiency.",
            "Time (h) = (Battery Ah / Charging Amps) × 1.25 Efficiency Factor",
            listOf(input("ah", "Battery Rating (Ah)", "150", "Ah"), input("amps", "Charger Current (A)", "15", "A")),
            calc = { v ->
                val ah = v["ah"] ?: 150.0; val a = (v["amps"] ?: 15.0).coerceAtLeast(0.5)
                val t = (ah / a) * 1.25
                fmt(t) to "Hours Full Charging Time"
            }
        ),
        // 151. Battery Charging Current Calculation
        CalcFactory.create(
            151, "Battery Charging Current Calculation", CalculatorLevel.BASIC, CalculatorCategory.BATTERY_UPS,
            "Calculates recommended charging current (typically 10% to 15% of battery Ah rating).",
            "I_charge = Battery Ah × 0.10 to 0.15",
            listOf(input("ah", "Battery Capacity (Ah)", "200", "Ah")),
            calc = { v ->
                val ah = v["ah"] ?: 200.0
                val rec = ah * 0.10
                fmt(rec) to "Amperes Recommended (10% C-Rate)"
            },
            secondaryBuilder = { v ->
                val ah = v["ah"] ?: 200.0
                listOf("Max Safe Charging Current (15%)" to "${fmt(ah * 0.15)} A")
            }
        ),
        // 152. Battery Energy Calculation
        CalcFactory.create(
            152, "Battery Energy Calculation", CalculatorLevel.BASIC, CalculatorCategory.BATTERY_UPS,
            "Calculates total stored electrical energy in Watt-hours (Wh) and Joules.",
            "Energy (Wh) = Voltage (V) × Capacity (Ah)",
            listOf(input("v", "Nominal Voltage (V)", "12", "V"), input("ah", "Capacity (Ah)", "120", "Ah")),
            calc = { v ->
                val volt = v["v"] ?: 12.0; val ah = v["ah"] ?: 120.0
                val wh = volt * ah
                fmt(wh) to "Watt-hours (Wh)"
            },
            secondaryBuilder = { v ->
                val volt = v["v"] ?: 12.0; val ah = v["ah"] ?: 120.0
                listOf("Energy in kWh" to "${fmt((volt * ah) / 1000.0)} kWh", "Energy in Megajoules" to "${fmt((volt * ah * 3600.0) / 1e6)} MJ")
            }
        ),
        // 153. Battery Ah to kWh Calculation
        CalcFactory.create(
            153, "Battery Ah to kWh Calculation", CalculatorLevel.BASIC, CalculatorCategory.BATTERY_UPS,
            "Converts battery Ampere-hours to Kilowatt-hours (kWh).",
            "kWh = (Ah × V) / 1000",
            listOf(input("ah", "Battery Ah", "200", "Ah"), input("v", "Battery Voltage (V)", "48", "V")),
            calc = { v ->
                val ah = v["ah"] ?: 200.0; val volt = v["v"] ?: 48.0
                val kwh = (ah * volt) / 1000.0
                fmt(kwh) to "kWh"
            }
        ),
        // 154. Battery Series Connection Calculation
        CalcFactory.create(
            154, "Battery Series Connection Calculation", CalculatorLevel.BASIC, CalculatorCategory.BATTERY_UPS,
            "Calculates bank total voltage and capacity for series-connected batteries.",
            "V_total = N × V_unit,  Ah_total = Ah_unit",
            listOf(input("qty", "Number of Batteries in Series", "4", "pcs"), input("v", "Individual Battery Voltage (V)", "12", "V"), input("ah", "Individual Battery Ah", "100", "Ah")),
            calc = { v ->
                val n = v["qty"] ?: 4.0; val volt = v["v"] ?: 12.0; val ah = v["ah"] ?: 100.0
                val totV = n * volt
                fmt(totV) to "Volts Total (at ${fmt(ah)} Ah)"
            },
            secondaryBuilder = { v ->
                val n = v["qty"] ?: 4.0; val volt = v["v"] ?: 12.0; val ah = v["ah"] ?: 100.0
                listOf("Total Bank Energy" to "${fmt((n * volt * ah) / 1000.0)} kWh")
            }
        ),
        // 155. Battery Parallel Connection Calculation
        CalcFactory.create(
            155, "Battery Parallel Connection Calculation", CalculatorLevel.BASIC, CalculatorCategory.BATTERY_UPS,
            "Calculates bank total capacity and runtime for parallel-connected batteries.",
            "Ah_total = N × Ah_unit,  V_total = V_unit",
            listOf(input("qty", "Number of Batteries in Parallel", "3", "pcs"), input("v", "Individual Voltage (V)", "12", "V"), input("ah", "Individual Ah", "150", "Ah")),
            calc = { v ->
                val n = v["qty"] ?: 3.0; val volt = v["v"] ?: 12.0; val ah = v["ah"] ?: 150.0
                val totAh = n * ah
                fmt(totAh) to "Ah Total (at ${fmt(volt)} V)"
            },
            secondaryBuilder = { v ->
                val n = v["qty"] ?: 3.0; val volt = v["v"] ?: 12.0; val ah = v["ah"] ?: 150.0
                listOf("Total Bank Energy" to "${fmt((n * volt * ah) / 1000.0)} kWh")
            }
        ),
        // 156. DC Load Current Calculation
        CalcFactory.create(
            156, "DC Load Current Calculation", CalculatorLevel.BASIC, CalculatorCategory.BATTERY_UPS,
            "Calculates DC amperes draw from battery bank for given DC or AC inverter loads.",
            "I_DC = Power (W) / (V_DC × Inverter Efficiency)",
            listOf(input("watts", "Inverter Output Load (W)", "1200", "W"), input("vdc", "DC System Voltage (V)", "24", "V"), input("eff", "Inverter Efficiency", "0.90")),
            calc = { v ->
                val w = v["watts"] ?: 1200.0; val volt = (v["vdc"] ?: 24.0).coerceAtLeast(1.0); val eff = v["eff"] ?: 0.90
                val idc = w / (volt * eff)
                fmt(idc) to "Amperes DC Current Draw"
            },
            secondaryBuilder = { v ->
                val w = v["watts"] ?: 1200.0; val volt = (v["vdc"] ?: 24.0).coerceAtLeast(1.0); val eff = v["eff"] ?: 0.90
                val idc = w / (volt * eff)
                val cable = ElectricalFormulas.selectCable(idc * 1.25)
                listOf("DC Cable Recommendation" to "$cable mm² Copper", "DC Fuse / Breaker" to "${ElectricalFormulas.selectBreaker(idc * 1.25)} A")
            }
        )
    )
}
