package com.example.data.calculator

import com.example.data.model.CalculatorCategory
import com.example.data.model.CalculatorDefinition
import com.example.data.model.CalculatorLevel

data class CalculatorGuide(
    val id: Int,
    val name: String,
    val level: CalculatorLevel,
    val category: CalculatorCategory,
    val description: String,
    val formula: String,
    val standard: String,
    val isPhaseSelectable: Boolean,
    val inputListSummary: List<String>,
    val howToWorkBn: String,
    val howToCalculateBn: String,
    val phaseGuideBn: String,
    val practicalApplicationBn: String,
    val howToWorkEn: String,
    val howToCalculateEn: String,
    val phaseGuideEn: String,
    val practicalApplicationEn: String
)

object CalculatorGuideHelper {

    fun getGuide(calc: CalculatorDefinition): CalculatorGuide {
        val isPhase = calc.isPhaseSelectable
        val inputsDesc = calc.inputs.map { input ->
            val unitStr = if (input.unitOptions.isNotEmpty()) " (${input.unitOptions.joinToString("/")})" else if (input.defaultUnit.isNotBlank()) " (${input.defaultUnit})" else ""
            "${input.label}$unitStr"
        }

        val phaseGuideBn = if (isPhase) {
            "⚡ ফেজ নির্বাচন নির্দেশিকা: এই ক্যালকুলেটরে সিঙ্গেল ফেজ (1-Phase / 230V) ও থ্রি ফেজ (3-Phase / 400V) উভয়ের জন্য স্বয়ংক্রিয় ইঞ্জিন রয়েছে। আপনার লোডের ধরণ অনুযায়ী সঠিক ফেজ বেছে নিন। (কঠোর প্রকৌশল শর্ত: ১-ফেজ ও ৩-ফেজ লোড সর্বদা পৃথকভাবে হিসাব করতে হবে, কোনোভাবেই একসাথে মেশানো যাবে না)।"
        } else {
            "⚡ সাধারণ ফেজ নির্দেশিকা: এই ক্যালকুলেটরটি সার্বজনীন ইলেকট্রিক্যাল সমীকরণ ও স্ট্যান্ডার্ড প্যারামিটারের ভিত্তিতে কাজ করে।"
        }

        val phaseGuideEn = if (isPhase) {
            "⚡ Phase Selection Guide: This calculator features an automated dual-engine supporting Single Phase (1-Phase / 230V) and Three Phase (3-Phase / 400V). Select the appropriate phase configuration for your target electrical load. Strictly note: Single-phase and three-phase loads must be calculated separately according to standard electrical engineering codes."
        } else {
            "⚡ Standard Phase Guide: This calculator operates based on universal electrical engineering equations and reference standards."
        }

        val howToWorkBn = buildString {
            appendLine("১. প্রয়োজনীয় ইনপুট প্যারামিটার প্রদান করুন:")
            if (inputsDesc.isNotEmpty()) {
                appendLine("   • ইনপুট তালিকা: " + inputsDesc.joinToString(", "))
            } else {
                appendLine("   • নির্দিষ্ট প্যারামিটার ইনপুট বক্সে প্রবেশ করান।")
            }
            if (isPhase) {
                appendLine("২. ফেজ মোড সিলেক্ট করুন: স্ক্রিনের উপরে থাকা টগল থেকে 'Single Phase' অথবা 'Three Phase' নির্বাচন করুন।")
                appendLine("৩. ড্রপডাউন মেনু থেকে ইউনিট (V, A, kW, HP, kVA, meter, ইত্যাদি) যাচাই করে নিন।")
                appendLine("৪. 'CALCULATE' বাটনে ট্যাপ করে মুহূর্তের মধ্যে সমাধান ও ব্রেকার/ক্যাবল রিকমেন্ডেশন দেখুন।")
            } else {
                appendLine("২. ড্রপডাউন মেনু থেকে সঠিক পরিমাপ ইউনিট নিশ্চিত করুন।")
                appendLine("৩. 'CALCULATE' বাটনে ট্যাপ করে তাৎক্ষণিক ফলাফল ও স্টেপ-বাই-স্টেপ গাণিতিক ধাপ দেখুন।")
            }
            appendLine("৫. প্রয়োজনে 'PDF GENERATE' বা 'PRINT' অপশন ব্যবহার করে প্রফেশনাল রিপোর্ট তৈরি করুন।")
        }.trim()

        val howToWorkEn = buildString {
            appendLine("1. Enter the required engineering input parameters:")
            if (inputsDesc.isNotEmpty()) {
                appendLine("   • Input fields: " + inputsDesc.joinToString(", "))
            } else {
                appendLine("   • Enter design values into input fields.")
            }
            if (isPhase) {
                appendLine("2. Select Phase Configuration: Toggle between 'Single Phase' (230V) and 'Three Phase' (400V).")
                appendLine("3. Confirm units from unit selectors (V, A, kW, HP, kVA, meters, etc.).")
                appendLine("4. Tap 'CALCULATE' to instantly compute full engineering solutions, derating, and breaker/cable recommendations.")
            } else {
                appendLine("2. Confirm measurement units from dropdown pickers.")
                appendLine("3. Tap 'CALCULATE' for instantaneous results with step-by-step mathematical breakdown.")
            }
            appendLine("5. Generate professional PDF engineering calculation sheets via 'PDF GENERATE'.")
        }.trim()

        val howToCalculateBn = buildString {
            appendLine("📐 মূল প্রকৌশল সূত্র:")
            appendLine("   ${calc.formula}")
            appendLine("")
            appendLine("🔬 হিসাব প্রণালী ও নীতি:")
            if (isPhase) {
                appendLine("• Three Phase (৩-ফেজ): লাইন ভোল্টেজ VL = 400V এবং √3 (≈ 1.732) গুণক প্রযোজ্য। যেমন: P = √3 × VL × IL × cosφ।")
                appendLine("• Single Phase (১-ফেজ): ফেজ ভোল্টেজ Vph = 230V প্রযোজ্য। যেমন: P = Vph × I × cosφ।")
                appendLine("• পাওয়ার ফ্যাক্টর (cosφ): মোট পাওয়ার ও সক্রিয় পাওয়ারের ভারসাম্য রক্ষায় পাওয়ার ফ্যাক্টর ব্যবহৃত হয়।")
            } else {
                appendLine("• স্ট্যান্ডার্ড ইলেকট্রিক্যাল সমীকরণের মাধ্যমে ইনপুট মানগুলোকে আন্তর্জাতিক এসআই (SI) ইউনিটে রূপান্তর করে সমীকরণে প্রয়োগ করা হয়।")
                appendLine("• ওহমস ল, জুলস ল, ফ্যারাডের সূত্র বা সার্কিট নেটওয়ার্ক থিওরেম অনুযায়ী নিখুঁত গাণিতিক ফলাফল পাওয়া যায়।")
            }
            appendLine("• স্ট্যান্ডার্ড ভিত্তি: ${calc.standard} অনুসারে সেফটি মার্জিন ও সহনশীলতা হিসাবভুক্ত।")
        }.trim()

        val howToCalculateEn = buildString {
            appendLine("📐 Governing Engineering Formula:")
            appendLine("   ${calc.formula}")
            appendLine("")
            appendLine("🔬 Theory & Engineering Principles:")
            if (isPhase) {
                appendLine("• Three Phase (3-Phase): Uses line voltage VL = 400V and √3 (≈ 1.732) factor: P = √3 × VL × IL × cosφ.")
                appendLine("• Single Phase (1-Phase): Uses phase voltage Vph = 230V: P = Vph × I × cosφ.")
                appendLine("• Power Factor (cosφ): Compensates for reactive component to determine true real power and kVA.")
            } else {
                appendLine("• Inputs are normalized into base SI electrical units and applied to standard governing electrical theorems.")
                appendLine("• Solved precisely using Ohm's Law, Joule's heating laws, Kirchhoff's laws, or IEC/NEC lookup curves.")
            }
            appendLine("• Standard Foundation: Designed in full compliance with ${calc.standard} safety margins and tolerances.")
        }.trim()

        val practicalApplicationBn = when (calc.category) {
            CalculatorCategory.FACTORY -> "ইন্ডাস্ট্রিয়াল সাবস্টেশন, ডিস্ট্রিবিউশন প্যানেল, ফ্যাক্টরি মেশিনারিজ ও মোট পাওয়ার চাহিদা নিরূপণে ব্যবহৃত হয়।"
            CalculatorCategory.TRANSFORMER -> "ট্রান্সফরমার লোড নির্ধারণ, সাইজিং, পূর্ণ লোড কারেন্ট এবং সাবস্টেশন ডিজাইনে অপরিহার্য।"
            CalculatorCategory.GENERATOR -> "জেনারেটর সাইজিং, ব্যাকআপ পাওয়ার লোড এস্টিমেশন ও অল্টারনেটর ক্যালকুলেশনে ব্যবহৃত।"
            CalculatorCategory.MOTOR -> "ইন্ডাকশন মটরের ফুল লোড কারেন্ট (FLC), স্টার্টিং কারেন্ট, স্টার-ডেল্টা সুইচিং এবং প্রটেকশন ব্রেকার নির্ধারণে ব্যবহৃত।"
            CalculatorCategory.CABLE -> "ক্যাবল সাইজিং, ভোল্টেজ ড্রপ সীমা (৩% এর নিচে রাখা), তামার ও অ্যালুমিনিয়াম পরিবাহীর ক্যাপাসিটি নির্ধারণে।"
            CalculatorCategory.BUILDING -> "আবাসিক ও বাণিজ্যিক ভবনের ওয়্যারিং, সুইচগিয়ার, লোড শিডিউল এবং ইলেকট্রিক্যাল মালামাল এস্টিমেশনে ব্যবহৃত।"
            CalculatorCategory.POWER -> "পাওয়ার সিস্টেম অ্যানালাইসিস, ফল্ট কারেন্ট হিসাব, বাসবার সাইজিং ও এনার্জি ম্যানেজমেন্টে প্রয়োগ করা হয়।"
            CalculatorCategory.BATTERY_UPS -> "ইউপিএস ও আইপিএস ব্যাকআপ সময়, ব্যাটারি অ্যাম্পিয়ার-আওয়ার (Ah) রেটিং এবং ডিসচার্জ কারেন্ট হিসাবে।"
            CalculatorCategory.PFI -> "পাওয়ার ফ্যাক্টর উন্নয়ন (PFI), ক্যাপাসিটর ব্যাংক সাইজ (kVAR) এবং অতিরিক্ত বিদ্যুৎ বিল জরিমানা রোধে ব্যবহৃত।"
            CalculatorCategory.PROTECTION -> "সার্কিট ব্রেকার (MCB, MCCB, ACB), ফিউজ রেটিং ও আর্থিং রেজিস্ট্যান্স সেফটি ক্যালকুলেশনে।"
            CalculatorCategory.CONVERSION -> "কিলোওয়াট, হর্সপাওয়ার, kVA, অ্যাম্পিয়ার, ভোল্ট এবং এনার্জি ইউনিটের পারস্পরিক দ্রুত রূপান্তর।"
            CalculatorCategory.BASIC_ELEC -> "সার্কিট বিশ্লেষণ, রেজিসট্যান্স, ইন্ডাকট্যান্স, ক্যাপাসিট্যান্স ও বেসিক ওহমস ল ক্যালকুলেশনে।"
        }

        val practicalApplicationEn = when (calc.category) {
            CalculatorCategory.FACTORY -> "Used for industrial substations, main switchboards, distribution panels, factory load demands, and busbar trunking systems."
            CalculatorCategory.TRANSFORMER -> "Essential for transformer capacity sizing, full load currents, percentage impedance, short circuit MVA, and substation engineering."
            CalculatorCategory.GENERATOR -> "Utilized for diesel/gas generator sizing, starting kVA surges, alternator derating, and emergency standby power."
            CalculatorCategory.MOTOR -> "Calculates induction motor full load current (FLC), starting current, torque, star-delta switching, and thermal overload protection."
            CalculatorCategory.CABLE -> "Determines conductor cross-sectional area, current carrying capacity, continuous thermal ratings, and voltage drop compliance (<3%)."
            CalculatorCategory.BUILDING -> "Residential and commercial building conduit wiring, load distribution schedules, lighting lux, and complete bill of materials."
            CalculatorCategory.POWER -> "Power system stability, symmetrical fault currents, short circuit capacity, busbar sizing, and utility grid parameters."
            CalculatorCategory.BATTERY_UPS -> "Calculates true battery bank Ah ratings, discharge duration curves, UPS efficiency, and invertor sizing."
            CalculatorCategory.PFI -> "Power Factor Improvement (PFI), automatic capacitor bank sizing (kVAR), penalty avoidance, and harmonic resonance prevention."
            CalculatorCategory.PROTECTION -> "Sizing and coordination for MCBs, MCCBs, ACBs, VCBs, thermal-magnetic trip curves, and earthing pit safety resistance."
            CalculatorCategory.CONVERSION -> "Rapid engineering conversions between electrical power (kW, HP, kVA), current, voltage, frequency, and energy."
            CalculatorCategory.BASIC_ELEC -> "Fundamental DC/AC circuit network analysis, Ohm's law, impedance triangles, resonance, and capacitive/inductive reactance."
        }

        return CalculatorGuide(
            id = calc.id,
            name = calc.name,
            level = calc.level,
            category = calc.category,
            description = calc.description,
            formula = calc.formula,
            standard = calc.standard,
            isPhaseSelectable = isPhase,
            inputListSummary = inputsDesc,
            howToWorkBn = howToWorkBn,
            howToCalculateBn = howToCalculateBn,
            phaseGuideBn = phaseGuideBn,
            practicalApplicationBn = practicalApplicationBn,
            howToWorkEn = howToWorkEn,
            howToCalculateEn = howToCalculateEn,
            phaseGuideEn = phaseGuideEn,
            practicalApplicationEn = practicalApplicationEn
        )
    }
}
