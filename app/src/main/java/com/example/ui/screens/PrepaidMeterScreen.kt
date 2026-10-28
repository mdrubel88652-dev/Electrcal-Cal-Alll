package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ElectricalApp

data class MeterCodeItem(
    val code: String,
    val titleBn: String,
    val titleEn: String,
    val description: String,
    val category: String
)

data class PrepaidMeterModel(
    val id: String,
    val brandName: String,
    val brandNameBn: String,
    val utilities: List<String>,
    val descriptionBn: String,
    val accentColor: Color,
    val codes: List<MeterCodeItem>
)

data class MeterErrorGuide(
    val errorDisplay: String,
    val meaningBn: String,
    val solutionBn: String
)

object PrepaidMeterData {
    val meterBrands: List<PrepaidMeterModel> = listOf(
        PrepaidMeterModel(
            id = "INHEMETER",
            brandName = "Inhemeter",
            brandNameBn = "ইনহিমিটার (Inhemeter)",
            utilities = listOf("BREB (পল্লী বিদ্যুৎ)", "BPDB", "NESCO", "WZPDCL"),
            descriptionBn = "পল্লী বিদ্যুৎ ও বিপিডিবির সর্বাধিক ব্যবহৃত স্মার্ট কিপ্যাড মিটার।",
            accentColor = Color(0xFF0284C7),
            codes = listOf(
                MeterCodeItem("801", "অবশিষ্ট ব্যালেন্স টাকা", "Remaining Balance (Tk)", "মিটারে কত টাকা অবশিষ্ট আছে তা দেখা যায়। (কিছু মডেলে 01 কোড কাজ করে)", "balance"),
                MeterCodeItem("802", "মোট বিদ্যুৎ ব্যবহার (ইউনিট)", "Total Consumed kWh", "মিটার চালুর পর থেকে মোট কত ইউনিট ব্যবহার হয়েছে।", "unit"),
                MeterCodeItem("803", "বর্তমান লাইভ ভোল্টেজ", "Instantaneous Voltage (V)", "লাইনের বর্তমান ভোল্টেজ (স্বাভাবিক ২২০-২৩০ ভোল্ট)।", "power"),
                MeterCodeItem("804", "চলতি বিদ্যুৎ লোড (কিলোওয়াট)", "Running Load (kW)", "বর্তমানে বাড়িতে কত কিলোওয়াট বিদ্যুৎ খরচ হচ্ছে।", "power"),
                MeterCodeItem("805", "ফ্রিকোয়েন্সি (Hz)", "Frequency (Hz)", "বিদ্যুৎ লাইনের স্বাভাবিক ৫০ হার্টজ ফ্রিকোয়েন্সি যাচাই।", "power"),
                MeterCodeItem("806", "পাওয়ার ফ্যাক্টর (PF)", "Power Factor (PF)", "আপনার বিদ্যুৎ ব্যবহারের পাওয়ার ফ্যাক্টর (০.৯৫ - ১.০ কাঙ্ক্ষিত)।", "power"),
                MeterCodeItem("807", "মিটার ক্রমিক নম্বর", "Meter Serial Number", "মিটারের ইউনিক সিরিয়াল নম্বর ডিসপ্লেতে দেখতে।", "system"),
                MeterCodeItem("814", "ইমার্জেন্সি ব্যালেন্সের পরিমাণ", "Emergency Credit Balance", "ইমার্জেন্সি ব্যালেন্স কত টাকা অবশিষ্ট আছে।", "emergency"),
                MeterCodeItem("800", "সতর্কীকরণ অ্যালার্ম বাজার মিউট", "Mute Buzzer Alarm", "টাকা শেষ হওয়ার সংকেত বিপ সাউন্ড সাময়িকভাবে বন্ধ করতে।", "system"),
                MeterCodeItem("886", "মিটার রিলে টেস্ট / সংযোগ স্ট্যাটাস", "Relay Status Check", "মিটারের ভেতর মেইন সুইচের রিলে অন নাকি অফ আছে তা চেক করতে।", "system"),
                MeterCodeItem("809", "বর্তমান ট্যারিফ রেট", "Current Tariff Rate", "প্রতি ইউনিট বিদ্যুতের জন্য কত টাকা কর্তন হচ্ছে।", "system")
            )
        ),
        PrepaidMeterModel(
            id = "HEXING",
            brandName = "Hexing",
            brandNameBn = "হেক্সিং মিটার (Hexing Single/Three Phase)",
            utilities = listOf("DPDC", "DESCO", "WZPDCL", "BREB"),
            descriptionBn = "ডেসকো ও ডিপিডিসির বহুল প্রচলিত প্রিপেইড মিটার ব্র্যান্ড।",
            accentColor = Color(0xFF059669),
            codes = listOf(
                MeterCodeItem("801", "অবশিষ্ট ব্যালেন্স", "Remaining Balance", "বর্তমানে মিটারে কত টাকা জমা রয়েছে।", "balance"),
                MeterCodeItem("802", "মোট ব্যবহৃত কিলোওয়াট-আওয়ার", "Total Consumed kWh", "চালু হওয়ার পর থেকে মোট ইউনিট খরচ।", "unit"),
                MeterCodeItem("804", "চলতি ফেজ কারেন্ট (A)", "Instant Current (Amps)", "লোড লাইনে কত এম্পিয়ার বিদ্যুৎ প্রবাহিত হচ্ছে।", "power"),
                MeterCodeItem("805", "বর্তমান গ্রিড ভোল্টেজ (V)", "Instant Voltage (Volts)", "সাপ্লাই লাইনের লাইভ ভোল্টেজ রিডিং।", "power"),
                MeterCodeItem("806", "চলমান লোড (কিলোওয়াট)", "Running Load kW", "বর্তমান লোড ডিমান্ড।", "power"),
                MeterCodeItem("807", "মিটার আইডি / গ্রাহক কোড", "Meter Identification Number", "টোকেন কেনার সময় ব্যবহৃত মিটার নম্বর।", "system"),
                MeterCodeItem("813", "সর্বশেষ রিচার্জ টোকেন", "Last Entered Token", "সর্বশেষ প্রবেশ করানো ২০ ডিজিটের কোড।", "emergency"),
                MeterCodeItem("817", "ওভারলোড লিমিট সীমা", "Overload Limit (kW)", "সর্বোচ্চ কত লোড পর্যন্ত ট্রিপ না করে চলবে।", "system"),
                MeterCodeItem("888", "বাজার অ্যালার্ম বন্ধ", "Silence Buzzer Alarm", "লো ব্যালেন্স বিপ বন্ধ করতে।", "system")
            )
        ),
        PrepaidMeterModel(
            id = "SHENZHEN_STAR",
            brandName = "Shenzhen Star",
            brandNameBn = "শেনজেন স্টার (Shenzhen Star Instruments)",
            utilities = listOf("BREB (পল্লী বিদ্যুৎ)", "BPDB"),
            descriptionBn = "পল্লী বিদ্যুতের গ্রামীণ এলাকার জন্য ব্যাপকভাবে স্থাপিত কিপ্যাড মিটার।",
            accentColor = Color(0xFFD97706),
            codes = listOf(
                MeterCodeItem("01", "অবশিষ্ট ব্যালেন্স টাকা", "Remaining Balance", "অবশিষ্ট টাকার পরিমাণ দেখতে (অথবা 801 ডায়াল করুন)।", "balance"),
                MeterCodeItem("02", "মোট ইউনিট খরচ (kWh)", "Total kWh Consumed", "মোট বিদ্যুৎ ব্যবহার ইউনিট।", "unit"),
                MeterCodeItem("03", "রানিং ভোল্টেজ (V)", "Grid Voltage", "বর্তমান ভোল্টেজ মান।", "power"),
                MeterCodeItem("04", "রানিং কারেন্ট (A)", "Load Current", "মোট অ্যাম্পিয়ার বিদ্যুৎ প্রবাহ।", "power"),
                MeterCodeItem("05", "রানিং পাওয়ার লোড (kW)", "Active Power kW", "চলতি ওয়াট/কিলোওয়াট লোড।", "power"),
                MeterCodeItem("06", "পাওয়ার ফ্যাক্টর (PF)", "Operating Power Factor", "পাওয়ার ফ্যাক্টর মান।", "power"),
                MeterCodeItem("07", "মিটার সিরিয়াল নম্বর", "Meter Serial Number", "মিটারের পরিচিতি নম্বর।", "system"),
                MeterCodeItem("08", "তারিখ ও বর্তমান সময়", "Date & Time", "মিটারের অভ্যন্তরীণ ঘড়ির সময়।", "system"),
                MeterCodeItem("00", "লো-ক্রেডিট বিপ শব্দ মিউট", "Mute Buzzer", "বিপ শব্দ বন্ধ করতে 00 চেপে এন্টার চাপুন।", "system")
            )
        ),
        PrepaidMeterModel(
            id = "WASION",
            brandName = "Wasion",
            brandNameBn = "ওয়াসিওন মিটার (Wasion Meter)",
            utilities = listOf("DESCO", "DPDC", "NESCO"),
            descriptionBn = "ঢাকা ও রাজশাহী/রংপুর অঞ্চলে ব্যবহৃত আধুনিক স্মার্ট মিটার।",
            accentColor = Color(0xFF7C3AED),
            codes = listOf(
                MeterCodeItem("801", "অবশিষ্ট ব্যালেন্স", "Remaining Balance", "বর্তমানে মিটারে কত টাকা জমা রয়েছে।", "balance"),
                MeterCodeItem("802", "মোট বিদ্যুৎ খরচ", "Total Cumulative kWh", "মোট ইউনিট খরচ।", "unit"),
                MeterCodeItem("803", "ভোল্টেজ ও ফ্রিকোয়েন্সি", "Voltage & Frequency", "বর্তমান ভোল্টেজ ও ৫০Hz স্ট্যাটাস।", "power"),
                MeterCodeItem("804", "রানিং এম্পিয়ার কারেন্ট", "Instant Amps", "বর্তমান লাইভ এম্পিয়ার।", "power"),
                MeterCodeItem("807", "মিটার নম্বর", "Meter ID", "মিটার আইডি প্রদর্শন।", "system"),
                MeterCodeItem("888", "অ্যালার্ম সাউন্ড মিউট", "Mute Alarm", "জরুরি বিপ শব্দ সাময়িক মিউট করতে।", "system")
            )
        ),
        PrepaidMeterModel(
            id = "HOLLEY",
            brandName = "Holley Technology",
            brandNameBn = "হলি মিটার (Holley Single Phase)",
            utilities = listOf("BREB", "BPDB", "WZPDCL"),
            descriptionBn = "বিপিডিবি ও পল্লী বিদ্যুতের অন্যতম নির্ভরযোগ্য স্মার্ট মিটার।",
            accentColor = Color(0xFFDC2626),
            codes = listOf(
                MeterCodeItem("801", "বর্তমান অবশিষ্ট টাকা", "Available Balance", "কত টাকা জমা আছে।", "balance"),
                MeterCodeItem("802", "মোট ইউনিট রিডিং", "Active Energy kWh", "সর্বমোট বিদ্যুৎ ইউনিট।", "unit"),
                MeterCodeItem("803", "লাইন ভোল্টেজ", "Voltage (V)", "বর্তমান লাইভ ভোল্টেজ।", "power"),
                MeterCodeItem("804", "চলতি কারেন্ট লোড", "Current (A)", "মোট রানিং কারেন্ট।", "power"),
                MeterCodeItem("807", "মিটার সিরিয়াল নম্বর", "Meter No", "মিটারের পরিচিতি নাম্বার।", "system"),
                MeterCodeItem("812", "শেষ রিচার্জের টাকার পরিমাণ", "Last Recharge Amount", "সর্বশেষ কত টাকা রিচার্জ করা হয়েছিল।", "emergency"),
                MeterCodeItem("800", "বিপ বাজার সাউন্ড বন্ধ", "Stop Buzzer", "টাকা শেষের অ্যালার্ম অফ করতে।", "system")
            )
        ),
        PrepaidMeterModel(
            id = "SANXING",
            brandName = "Sanxing Electric",
            brandNameBn = "সানক্সিং মিটার (Sanxing Smart Meter)",
            utilities = listOf("DPDC", "DESCO"),
            descriptionBn = "ডিপিডিসি ও ডেসকোর আবাসিক গ্রাহকদের জন্য স্থাপিত মিটার।",
            accentColor = Color(0xFF0D9488),
            codes = listOf(
                MeterCodeItem("801", "অবশিষ্ট ব্যালেন্স", "Remaining Credit", "বর্তমান জমার পরিমাণ।", "balance"),
                MeterCodeItem("802", "মোট ইউনিট খরচ", "Total Consumption", "মোট ব্যবহৃত কিলোওয়াট-আওয়ার।", "unit"),
                MeterCodeItem("803", "বর্তমান ভোল্টেজ", "Voltage", "লাইভ লাইন ভোল্টেজ।", "power"),
                MeterCodeItem("804", "বর্তমান কারেন্ট", "Current", "লাইভ লোড কারেন্ট।", "power"),
                MeterCodeItem("806", "পাওয়ার ফ্যাক্টর", "Power Factor", "চলমান পাওয়ার ফ্যাক্টর।", "power"),
                MeterCodeItem("807", "মিটার নম্বর", "Meter Serial", "মিটারের অনন্য ক্রমিক নম্বর।", "system")
            )
        ),
        PrepaidMeterModel(
            id = "SECURE",
            brandName = "Secure Meters",
            brandNameBn = "সিকিউর মিটার (Secure Three-Phase)",
            utilities = listOf("BPDB", "DESCO", "DPDC", "BREB"),
            descriptionBn = "কমার্শিয়াল, থ্রি-ফেজ ও আবাসিক প্যানেলের জন্য ব্যবহৃত সিকিউর মিটার।",
            accentColor = Color(0xFF334155),
            codes = listOf(
                MeterCodeItem("1", "ফেজ ভোল্টেজ (Phase Voltage)", "Phase Voltage", "তিন ফেজের ভোল্টেজ ভ্যালু দেখতে।", "power"),
                MeterCodeItem("2", "লাইন কারেন্ট (Line Current)", "Line Current (A)", "প্রতি ফেজে মোট অ্যাম্পিয়ার কারেন্ট।", "power"),
                MeterCodeItem("3", "অ্যাক্টিভ লোড (kW)", "Active Power Load", "মোট চলতি কিলোওয়াট লোড।", "power"),
                MeterCodeItem("4", "পাওয়ার ফ্যাক্টর (PF)", "Power Factor", "চলমান গড় পাওয়ার ফ্যাক্টর।", "power"),
                MeterCodeItem("5", "অবশিষ্ট ব্যালেন্স", "Remaining Balance", "বর্তমান অবশিষ্ট জমার পরিমাণ।", "balance"),
                MeterCodeItem("6", "মোট ব্যবহৃত কিলোওয়াট-আওয়ার", "Total Active kWh", "মোট ইউনিট বিদ্যুৎ ব্যবহার।", "unit"),
                MeterCodeItem("7", "ম্যাক্সিমাম ডিমান্ড (MD in kW)", "Maximum Demand", "মাসের সর্বোচ্চ লোড রেকর্ড।", "power")
            )
        )
    )

    val errorGuides: List<MeterErrorGuide> = listOf(
        MeterErrorGuide("Err 01 / Reject", "ভুল টোকেন বা পূর্বেই ব্যবহৃত টোকেন", "রিচার্জ টোকেন টাইপ করার সময় কোনো একটি সংখ্যা ভুল হয়েছে অথবা এই টোকেনটি আগে রিচার্জ করা হয়েছে। এসএমএসের ২০ ডিজিটের কোডটি পুনরায় সতর্কতার সাথে টাইপ করুন।"),
        MeterErrorGuide("Err 02 / Old Token", "পুরাতন বা ক্রমচ্যুত টোকেন", "আপনি একের অধিক রিচার্জ টোকেনের মধ্যে পূর্ববর্তী টোকেনটি ডায়াল করছেন। তারিখ ও সময় মিলিয়ে সর্বশেষ কেনা টোকেনটি আগে প্রবেশ করান।"),
        MeterErrorGuide("Err 03 / Used Token", "ইতিপূর্বে সফলভাবে ব্যবহৃত টোকেন", "এই রিচার্জ কোডটি ইতোমধ্যে আপনার মিটারে সফলভাবে যোগ করা হয়েছে। ব্যালেন্স দেখতে ৮০১ বা ০১ ডায়াল করুন।"),
        MeterErrorGuide("Err 04 / Overload Trip", "অনুমোদিত লোডের বেশি ব্যবহার (ওভারলোড)", "আপনার অনুমোদিত লোডের চেয়ে বেশি বিদ্যুৎ সরঞ্জাম এক সাথে চালু করায় মিটার স্বয়ংক্রিয়ভাবে লাইন ট্রিপ করেছে। কিছু ভারি লোড (এসি, গিজার, মোটর) বন্ধ করে ৫ মিনিট অপেক্ষা করুন, লাইন অটো চালু হবে।"),
        MeterErrorGuide("Open / Rel Open", "মিটার কাভার খোলা বা টেম্পারিং প্রোটেকশন", "মিটারের টার্মিনাল কাভার বা সুরক্ষা সীল খোলা হয়েছে অথবা শর্ট সার্কিট হয়েছে। এক্ষেত্রে গ্রাহক নিজে অন করতে পারবেন না, নিকটস্থ বিদ্যুৎ অফিসের অভিযোগ কেন্দ্রে বা হেল্পলাইনে কল দিন।"),
        MeterErrorGuide("Low Credit Beep", "ব্যালেন্স ১০০ টাকার নিচে আসায় সতর্কীকরণ", "মিটারে টাকা কম থাকায় গ্রাহককে সতর্ক করতে বাজার শব্দ করে। ৮০১ বা ৮০৭ বা ৮১৭ অথবা স্টার মিটারে ০০ চেপে এন্টার দিলে অ্যালার্ম মিউট হয়ে যাবে।"),
        MeterErrorGuide("Battery Low / Bat", "মিটারের অভ্যন্তরীণ ইন্টারনাল ব্যাটারি দুর্বল", "মিটারের ডিসপ্লে মেমোরি ও ঘড়ির ব্যাটারি শেষ হয়ে আসছে। বিদ্যুৎ অফিসকে অবহিত করলে তারা ফ্রিতে ব্যাটারি পরিবর্তন করে দিবে।"),
        MeterErrorGuide("Comm Fail", "কিপ্যাড ও মিটার কার্ডের যোগাযোগ ত্রুটি", "কিপ্যাড ইউনিট ও মিটারের মধ্যকার তারে কোনো ফল্ট হয়েছে। কানেকশন তার পরীক্ষা করুন অথবা বিদ্যুৎ অফিসে জানান।")
    )

    val utilityHelplines: List<Pair<String, String>> = listOf(
        Pair("পল্লী বিদ্যুৎ (BREB)", "16262 অথবা 01769-400400"),
        Pair("ডেসকো (DESCO)", "16120 অথবা 09612316120"),
        Pair("ডিপিডিসি (DPDC)", "16116 অথবা 09612316116"),
        Pair("বিপিডিবি (BPDB)", "16200"),
        Pair("নেসকো (NESCO)", "16603"),
        Pair("ওজোপাডিকো (WZPDCL)", "16117")
    )
}

@Composable
fun PrepaidMeterScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = remember { ElectricalApp.getApp(context) }
    val currentLanguage by app.settingsManager.languageFlow.collectAsState(initial = "BN")
    val isEn = currentLanguage == "EN"

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = if (isEn) listOf("Meter Codes", "Error Guide", "Helplines") else listOf("মিটার কোড", "এরর সমাধান", "হেল্পলাইন")

    var selectedBrandId by remember { mutableStateOf("INHEMETER") }

    val meterBrands = PrepaidMeterData.meterBrands
    val errorGuides = PrepaidMeterData.errorGuides
    val utilityHelplines = PrepaidMeterData.utilityHelplines

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("prepaid_meter_screen_root")
    ) {
        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = Color(0xFF082D63)
        ) {
            tabs.forEachIndexed { index, tabName ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = tabName,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.5.sp
                        )
                    }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> {
                // Meter Codes View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Meter Brand Selector Pills directly at the top
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedBrandId == "ALL",
                                onClick = { selectedBrandId = "ALL" },
                                label = { Text(if (isEn) "All Brands" else "সব মিটার", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF082D63),
                                    selectedLabelColor = Color.White
                                )
                            )
                            meterBrands.forEach { brand ->
                                val isSelected = selectedBrandId == brand.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedBrandId = brand.id },
                                    label = { Text(brand.brandName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF082D63),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // Display Selected Brands
                    val displayedBrands = if (selectedBrandId == "ALL") meterBrands else meterBrands.filter { it.id == selectedBrandId }

                    displayedBrands.forEach { brand ->
                        val matchedCodes = brand.codes

                        if (matchedCodes.isNotEmpty()) {
                            item(key = brand.id) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(brand.accentColor),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Speed,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = brand.brandNameBn,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                    Text(
                                                        text = brand.utilities.joinToString(", "),
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF0284C7),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = brand.descriptionBn,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF64748B),
                                            modifier = Modifier.padding(top = 6.dp, bottom = 10.dp)
                                        )

                                        HorizontalDivider(color = Color(0xFFF1F5F9))
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Code Table
                                        matchedCodes.forEach { codeItem ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFF8FAFC))
                                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Surface(
                                                        color = brand.accentColor,
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text(
                                                            text = codeItem.code,
                                                            fontSize = 13.5.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color.White,
                                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text(
                                                            text = codeItem.titleBn,
                                                            fontSize = 12.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF0F172A)
                                                        )
                                                        Text(
                                                            text = codeItem.description,
                                                            fontSize = 11.sp,
                                                            color = Color(0xFF64748B),
                                                            lineHeight = 15.sp
                                                        )
                                                    }
                                                }

                                                IconButton(
                                                    onClick = {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        val clip = ClipData.newPlainText("Meter Code", codeItem.code)
                                                        clipboard.setPrimaryClip(clip)
                                                        Toast.makeText(context, "কোড ${codeItem.code} কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = "Copy",
                                                        tint = Color(0xFF0284C7),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Error Guide View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = if (isEn) "COMMON METER ERROR CODES & SOLUTIONS" else "মিটারের ডিসপ্লে এরর কোড ও তাৎক্ষণিক সমাধান:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                    }

                    items(errorGuides) { guide ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        color = Color(0xFFFEE2E2),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = guide.errorDisplay,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDC2626),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = guide.meaningBn,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "সমাধান: ${guide.solutionBn}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155),
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // Helplines View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = if (isEn) "OFFICIAL UTILITY COMPLAINT & HELPLINES" else "বিদ্যুৎ সংস্থাগুলোর হটলাইন ও জরুরি অভিযোগ নম্বর:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                    }

                    items(utilityHelplines) { (utility, helpline) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE0F2FE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = "Phone",
                                            tint = Color(0xFF0284C7),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = utility,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = helpline,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF0284C7)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        val numberOnly = helpline.split(" ")[0].trim()
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Helpline", numberOnly)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "$numberOnly কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Helpline",
                                        tint = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
