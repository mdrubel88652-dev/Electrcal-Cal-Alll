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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ElectricalApp

enum class ToolCategory(val titleBn: String, val titleEn: String, val color: Color) {
    ALL("সকল টুলস (৫০)", "All Tools (50)", Color(0xFF082D63)),
    MEASURING("পরিমাপ ও টেস্টিং (১২)", "Measuring & Test (12)", Color(0xFF0284C7)),
    HAND_TOOLS("হ্যান্ড টুলস ও ড্রাইভার (১২)", "Hand Tools & Drivers (12)", Color(0xFF059669)),
    TERMINATION("কাটার ও ক্রিম্পিং (৮)", "Cutters & Crimpers (8)", Color(0xFFD97706)),
    POWER_TOOLS("পাওয়ার টুলস (৬)", "Power Tools (6)", Color(0xFFDC2626)),
    CONDUIT("পাইপ ও ওয়্যারিং (৮)", "Conduit & Wiring (8)", Color(0xFF7C3AED)),
    SAFETY("নিরাপত্তা ও সেফটি (৪)", "Safety & PPE (4)", Color(0xFFE11D48))
}

data class ElectricalTool(
    val id: Int,
    val nameBn: String,
    val nameEn: String,
    val category: ToolCategory,
    val icon: ImageVector,
    val accentColor: Color,
    val keyUseBn: String,
    val shortDescBn: String,
    val safetyTipBn: String
)

object ElectricalToolsCatalog {
    val tools: List<ElectricalTool> = listOf(
        // ==========================================
        // 1. MEASURING & TESTING INSTRUMENTS (1-12)
        // ==========================================
        ElectricalTool(
            id = 1,
            nameBn = "ডিজিটাল মাল্টিমিটার",
            nameEn = "Digital Multimeter (DMM)",
            category = ToolCategory.MEASURING,
            icon = Icons.Default.Speed,
            accentColor = Color(0xFF0284C7),
            keyUseBn = "AC/DC ভোল্টেজ, কারেন্ট, ওহম ও কন্টিনিউটি",
            shortDescBn = "ইলেকট্রিক্যাল সার্কিটের মূল ডায়াগনস্টিক মিটার। রেজিস্ট্যান্স, ক্যাপাসিট্যান্স, ডায়োড ও তারের ছেঁড়া/জোড়া পরীক্ষা করা হয়।",
            safetyTipBn = "ভোল্টেজ মাপার সময় প্রব যেন কখনই কারেন্ট (A) পোর্টে লাগানো না থাকে, অন্যথায় শর্ট সার্কিট হবে।"
        ),
        ElectricalTool(
            id = 2,
            nameBn = "ক্ল্যাম্প-অন মিটার",
            nameEn = "Clamp-on AC/DC Ammeter",
            category = ToolCategory.MEASURING,
            icon = Icons.Default.ElectricBolt,
            accentColor = Color(0xFF0284C7),
            keyUseBn = "চলতি লোডের এম্পিয়ার (A) মাপা",
            shortDescBn = "তার বিচ্ছিন্ন না করেই চলমান তারের চারপাশে চোয়াল (Jaw) আটকে তাৎক্ষণিক কারেন্ট পরিমাপ করা যায়।",
            safetyTipBn = "একসাথে ফেজ ও নিউট্রাল তার ক্ল্যাম্প করবেন না; সবসময় কেবল একটি নির্দিষ্ট তার ক্ল্যাম্প করুন।"
        ),
        ElectricalTool(
            id = 3,
            nameBn = "মেগার / ইনসুলেশন টেস্টার",
            nameEn = "Megger / Insulation Tester",
            category = ToolCategory.MEASURING,
            icon = Icons.Default.Memory,
            accentColor = Color(0xFF0284C7),
            keyUseBn = "কেবল ও মোটরের ইনসুলেশন রেজিস্ট্যান্স",
            shortDescBn = "উচ্চ ডিসি ভোল্টেজ (২৫০V, ৫০০V, ১০০০V) প্রয়োগ করে কেবল বা মোটরের ওয়াইন্ডিংয়ের লিকিজ কারেন্ট মেগা-ওহমে টেস্ট করা হয়।",
            safetyTipBn = "লাইভ লাইনে কখনো মেগার প্রয়োগ করবেন না। টেস্টের পর কেবল অবশ্যই গ্রাউন্ড ডিসচার্জ করতে হবে।"
        ),
        ElectricalTool(
            id = 4,
            nameBn = "নিয়ন ফেজ টেস্টার",
            nameEn = "Neon Phase / Test Pen",
            category = ToolCategory.MEASURING,
            icon = Icons.Default.Bolt,
            accentColor = Color(0xFF0284C7),
            keyUseBn = "লাইভ ফেজ তারে ভোল্টেজ উপস্থিতি যাচাই",
            shortDescBn = "পেন আকৃতির টেস্টার। অগ্রভাগ তারে ঠেকিয়ে পেছনের ধাতব ক্যাপে আঙুল রাখলে নিয়ন বাতি জ্বলে উঠে ফেজ শনাক্ত করে।",
            safetyTipBn = "ভেজা হাতে টেস্টার ব্যবহার করবেন না এবং ব্যবহারের পূর্বে পরিচিত লাইভ সকেটে টেস্টারটি কার্যকর কিনা যাচাই করুন।"
        ),
        ElectricalTool(
            id = 5,
            nameBn = "নন-কন্টাক্ট ভোল্টেজ ডিটেক্টর",
            nameEn = "Non-Contact Voltage (NCV) Pen",
            category = ToolCategory.MEASURING,
            icon = Icons.Default.Sensors,
            accentColor = Color(0xFF0284C7),
            keyUseBn = "তার স্পর্শ না করে দূর থেকে ভোল্টেজ ডিটেক্ট",
            shortDescBn = "ইলেক্ট্রোম্যাগনেটিক ফিল্ড সেন্স করে প্লাস্টিক ইনসুলেশনের বাইরে থেকেই অডিও বিপ ও এলইডি লাইটের মাধ্যমে ভোল্টেজ সতর্ক করে।",
            safetyTipBn = "শিল্ডেড কেবল বা মেটাল কনডুইটের ভেতরের তারে সিগন্যাল নাও পেতে পারে, সতর্ক থাকুন।"
        ),
        ElectricalTool(
            id = 6,
            nameBn = "আর্থ রেজিস্ট্যান্স টেস্টার",
            nameEn = "Earth Ground Resistance Tester",
            category = ToolCategory.MEASURING,
            icon = Icons.Default.Landslide,
            accentColor = Color(0xFF0284C7),
            keyUseBn = "আর্থিং রড ও মাটির রেজিস্ট্যান্স পরিমাপ",
            shortDescBn = "সাবস্টেশন ও আবাসিক বিল্ডিংয়ের আর্থিং ব্যবস্থা ৫ ওহমের নিচে (সাবস্টেশনে ১ ওহমের নিচে) নিশ্চিত করতে ৩ বা ৪ ওয়্যার পদ্ধতিতে টেস্ট হয়।",
            safetyTipBn = "বজ্রপাতের সময় বা বৃষ্টির ঠিক পরপরই সঠিক ফলাফলের জন্য আর্থিং টেস্ট এড়িয়ে চলুন।"
        ),
        ElectricalTool(
            id = 7,
            nameBn = "ফেজ সিকোয়েন্স মিটার",
            nameEn = "3-Phase Sequence Indicator",
            category = ToolCategory.MEASURING,
            icon = Icons.Default.Sync,
            accentColor = Color(0xFF0284C7),
            keyUseBn = "৩-ফেজ মোটরের ক্লকওয়াইজ রোটেশন চেক",
            shortDescBn = "R-Y-B ফেজের ক্রম সঠিক আছে কিনা তা নির্ধারণ করে, যাতে ৩-ফেজ মোটর উল্টো দিকে ঘুরে পাম্প বা মেশিন ক্ষতিগ্রস্ত না হয়।",
            safetyTipBn = "সঠিক ক্লিপ দিয়ে টার্মিনাল লক করুন যেন টেস্ট চলাকালে প্রব স্লিপ করে শর্ট না হয়।"
        ),
        ElectricalTool(
            id = 8,
            nameBn = "ডিজিটাল লাক্স মিটার",
            nameEn = "Digital Lux Light Meter",
            category = ToolCategory.MEASURING,
            icon = Icons.Default.WbSunny,
            accentColor = Color(0xFF0284C7),
            keyUseBn = "আলোর তীব্রতা (Lux/Foot-candle) পরিমাপ",
            shortDescBn = "অফিস, গার্মেন্টস, ক্লাসরুম ও ওয়ার্কশপে বিএলএনসি / আইইসি স্ট্যান্ডার্ড অনুযায়ী প্রয়োজনীয় আলোর মাত্রা পরিমাপ করা হয়।",
            safetyTipBn = "সেন্সরের উপর যাতে নিজের ছায়া না পড়ে সেভাবে সোজা রেখে রিডিং রেকর্ড করুন।"
        ),
        ElectricalTool(
            id = 9,
            nameBn = "ডিজিটাল ট্যাকোমিটার",
            nameEn = "Digital Optical RPM Tachometer",
            category = ToolCategory.MEASURING,
            icon = Icons.Default.Speed,
            accentColor = Color(0xFF0284C7),
            keyUseBn = "মোটর ও শ্যাফটের ঘূর্ণন গতি (RPM) মাপা",
            shortDescBn = "লেজার বা রিফ্লেক্টিভ টেপ ব্যবহার করে নন-কন্টাক্ট উপায়ে মোটরের প্রতি মিনিটে ঘূর্ণন সংখ্যা (RPM) নিখুঁতভাবে মাপা যায়।",
            safetyTipBn = "ঘূর্ণায়মান শ্যাফটের কাছাকাছি জামাকাপড় বা টাই এড়িয়ে চলুন।"
        ),
        ElectricalTool(
            id = 10,
            nameBn = "থার্মাল ইমেজার ক্যামেরা",
            nameEn = "Thermal Infrared Imager",
            category = ToolCategory.MEASURING,
            icon = Icons.Default.CameraAlt,
            accentColor = Color(0xFF0284C7),
            keyUseBn = "বাসবার ও ব্রেকারের অতিরিক্ত উত্তাপ হটস্পট নির্ণয়",
            shortDescBn = "ইনফ্রারেড হিট সিগনেচার দিয়ে প্যানেল বোর্ড ও মোটরের লুজ কানেকশন ও ওভারলোডেড কেবল অগ্নিকাণ্ডের আগেই চিহ্নিত করে।",
            safetyTipBn = "প্যানেল ডোর সাবধানে খুলে নিরাপদ দূরত্ব বজায় রেখে থার্মোগ্রাফি করুন।"
        ),
        ElectricalTool(
            id = 11,
            nameBn = "কেবল ফল্ট লোকেটার / টোনার",
            nameEn = "Wire Tracer & Cable Fault Locator",
            category = ToolCategory.MEASURING,
            icon = Icons.Default.Cable,
            accentColor = Color(0xFF0284C7),
            keyUseBn = "দেয়াল বা পাইপের ভেতর তারের রুট ও ব্রেক ট্রেস",
            shortDescBn = "টোন জেনারেটর সিগন্যাল পাঠায় এবং প্রোব রিসিভার দেয়ালের প্লাস্টারের মধ্য দিয়ে শব্দ শুনে তারের অবস্থান ও ছেঁড়া পয়েন্ট খুঁজে বের করে।",
            safetyTipBn = "শুধুমাত্র ডি-এনার্জাইজড (বন্ধ) লাইনে ট্র্যাকার ডিভাইস কানেক্ট করুন।"
        ),
        ElectricalTool(
            id = 12,
            nameBn = "সিরিজ টেস্ট ল্যাম্প",
            nameEn = "Series Test Lamp Assembly",
            category = ToolCategory.MEASURING,
            icon = Icons.Default.Lightbulb,
            accentColor = Color(0xFF0284C7),
            keyUseBn = "শর্ট সার্কিট ও কয়েল কন্টিনিউটি টেস্ট",
            shortDescBn = "১০০ ওয়াট ইনক্যানডেসেন্ট বাল্ব দিয়ে সিরিজ সার্কিট বানিয়ে মোটর ওয়াইন্ডিং বা অ্যাপ্লায়েন্সের ফল্ট নিরাপদভাবে টেস্ট করা হয়।",
            safetyTipBn = "টেস্ট চলাকালীন খোলা তারে হাত দেওয়া সম্পূর্ণ নিষিদ্ধ; সবসময় ইনসুলেটেড হোল্ডার ব্যবহার করুন।"
        ),

        // ==========================================
        // 2. HAND TOOLS & DRIVERS (13-24)
        // ==========================================
        ElectricalTool(
            id = 13,
            nameBn = "ইনসুলেটেড কম্বিনেশন প্লায়ার্স",
            nameEn = "Insulated Combination Pliers (1000V)",
            category = ToolCategory.HAND_TOOLS,
            icon = Icons.Default.Build,
            accentColor = Color(0xFF059669),
            keyUseBn = "তার কাটা, বাঁকানো ও শক্ত করে ধরা",
            shortDescBn = "ইলেকট্রিশিয়ানের সবচেয়ে ব্যবহৃত হ্যান্ড টুল। এর গ্রিপ ১০০০ ভোল্ট পর্যন্ত ইনসুলেটেড থাকে।",
            safetyTipBn = "গ্রিপে ফাটল বা কাট থাকলে তা লাইভ লাইনে কখনো ব্যবহার করবেন না।"
        ),
        ElectricalTool(
            id = 14,
            nameBn = "ডায়াগনাল সাইড কাটিং প্লায়ার্স",
            nameEn = "Diagonal Side Cutting Pliers",
            category = ToolCategory.HAND_TOOLS,
            icon = Icons.Default.ContentCut,
            accentColor = Color(0xFF059669),
            keyUseBn = "তারের বাড়তি অংশ ও কেবল টাই নিখুঁত কাটা",
            shortDescBn = "তীর্যক ব্লেড থাকায় সারফেসের সমান করে তার বা কেবল টাই কাটা যায় যাতে ধারালো প্রান্ত বের হয়ে না থাকে।",
            safetyTipBn = "শক্ত স্টিল তার কাটার জন্য ব্যবহার করবেন না, ব্লেড টোল খাবে।"
        ),
        ElectricalTool(
            id = 15,
            nameBn = "লং নোজ প্লায়ার্স",
            nameEn = "Long Nose / Needle Pliers",
            category = ToolCategory.HAND_TOOLS,
            icon = Icons.Default.Straighten,
            accentColor = Color(0xFF059669),
            keyUseBn = "চিপা জায়গা ও ডিবি বোর্ডে তার বাঁকানো ও ধরা",
            shortDescBn = "লম্বা চিকন মুখ থাকায় সুইচবোর্ড ও প্যানেলের গভীর কোণায় নাট ও তার সহজে নিয়ন্ত্রন করা যায়।",
            safetyTipBn = "অতিরিক্ত মোচড় দিয়ে লিভার হিসেবে ব্যবহার করবেন না, নোজ বেঁকে যেতে পারে।"
        ),
        ElectricalTool(
            id = 16,
            nameBn = "ইনসুলেটেড ফ্ল্যাট স্ক্রু ড্রাইভার",
            nameEn = "Insulated Slotted Screwdriver",
            category = ToolCategory.HAND_TOOLS,
            icon = Icons.Default.HorizontalRule,
            accentColor = Color(0xFF059669),
            keyUseBn = "ব্রেকার টার্মিনাল ও সকেটের স্লট স্ক্রু টাইট",
            shortDescBn = "শ্যাফটের অধিকাংশ জায়গা প্লাস্টিক ইনসুলেশনে ঢাকা থাকায় প্যানেলের পাশে ঠেকলেও স্পার্ক হয় না।",
            safetyTipBn = "স্ক্রুর মাপ অনুযায়ী সঠিক ব্লেডের সাইজ নির্বাচন করুন যাতে স্ক্রুর স্লট নষ্ট না হয়।"
        ),
        ElectricalTool(
            id = 17,
            nameBn = "ইনসুলেটেড ফিলিপস স্ক্রু ড্রাইভার",
            nameEn = "Insulated Phillips / Cross Screwdriver",
            category = ToolCategory.HAND_TOOLS,
            icon = Icons.Default.Close,
            accentColor = Color(0xFF059669),
            keyUseBn = "স্টার হেড স্ক্রু খোলা ও শক্তভাবে লাগানো",
            shortDescBn = "PH0, PH1, PH2 সাইজের ক্রসহেড স্ক্রু ড্রাইভার গ্যাং সুইচ, সিলিং ফ্যান ও কভার ফিক্সিংয়ে ব্যবহৃত হয়।",
            safetyTipBn = "হাতুড়ি দিয়ে স্ক্রু ড্রাইভারের মাথায় আঘাত করবেন না।"
        ),
        ElectricalTool(
            id = 18,
            nameBn = "সকেট পোলারিটি টেস্টার",
            nameEn = "3-Pin Socket Polarity Tester",
            category = ToolCategory.HAND_TOOLS,
            icon = Icons.Default.Power,
            accentColor = Color(0xFF059669),
            keyUseBn = "সকেটের ফেজ, নিউট্রাল ও আর্থিং সঠিকতা টেস্ট",
            shortDescBn = "প্লাগ ইন করলেই ৩টি এলইডির কম্বিনেশন দিয়ে লাইভ-নিউট্রাল রিভার্স, ওপেন আর্থিং বা মিসিং নিউট্রাল নির্দেশ করে।",
            safetyTipBn = "সব নতুন ওয়্যারিং ইন্সটলেশনে প্রতিটি সকেট এই টেস্টার দিয়ে চেক করা উচিত।"
        ),
        ElectricalTool(
            id = 19,
            nameBn = "অ্যাডজাস্টেবল স্লাই রেঞ্চ",
            nameEn = "Adjustable Shifting Wrench",
            category = ToolCategory.HAND_TOOLS,
            icon = Icons.Default.Build,
            accentColor = Color(0xFF059669),
            keyUseBn = "কেবল গ্ল্যান্ড ও বিভিন্ন মাপের নাট-বোল্ট ফিক্সিং",
            shortDescBn = "চাকা ঘুরিয়ে এর চোয়াল যেকোনো সাইজের নাটের মাপে ছোট-বড় করা যায়।",
            safetyTipBn = "টান সবসময় ফিক্সড জ’র দিকে দিন, মুভেবল জ’র দিকে নয়।"
        ),
        ElectricalTool(
            id = 20,
            nameBn = "কম্বিনেশন স্প্যানার সেট",
            nameEn = "Ring & Open Ended Spanner Set",
            category = ToolCategory.HAND_TOOLS,
            icon = Icons.Default.Construction,
            accentColor = Color(0xFF059669),
            keyUseBn = "সাবস্টেশন ও ট্রান্সফরমারের নাট-বোল্ট লক",
            shortDescBn = "৬ মিমি থেকে ৩২ মিমি পর্যন্ত একপাশে খোলা এবং অন্যপাশে রিং স্প্যানার নাট স্লিপ না করে শক্ত গ্রিপ দেয়।",
            safetyTipBn = "মরিচাযুক্ত নাটে প্রথমে ডব্লিউডি-৪০ স্প্রে করে স্প্যানার ব্যবহার করুন।"
        ),
        ElectricalTool(
            id = 21,
            nameBn = "অ্যালেন কি / হেক্স রেঞ্চ সেট",
            nameEn = "Hex Allen Key Wrench Set",
            category = ToolCategory.HAND_TOOLS,
            icon = Icons.Default.Key,
            accentColor = Color(0xFF059669),
            keyUseBn = "MCCB, ACB ও মোটর পুলির অভ্যন্তরীণ হেক্স স্ক্রু",
            shortDescBn = "এল-আকৃতির ষড়ভুজ রড কি। ১.৫ মিমি থেকে ১০ মিমি পর্যন্ত সাইজের সেট কন্ট্রোল প্যানেলে অপরিহার্য।",
            safetyTipBn = "সঠিক সাইজের হেক্স কি ব্যবহার করুন, সাইজ ছোট হলে স্ক্রুর হেক্সাগন স্লিপ করে নষ্ট হবে।"
        ),
        ElectricalTool(
            id = 22,
            nameBn = "ইনসুলেটেড টর্ক রেঞ্চ",
            nameEn = "Insulated Torque Wrench",
            category = ToolCategory.HAND_TOOLS,
            icon = Icons.Default.SquareFoot,
            accentColor = Color(0xFF059669),
            keyUseBn = "প্যানেল ও বাসবার নির্দিষ্ট Nm টর্কে টাইট",
            shortDescBn = "অতিরিক্ত বা কম টাইটের ঝুঁকি এড়াতে প্রস্তুতকারকের নির্দেশিত নিউটন-মিটার (Nm) টর্ক সেট করে ক্লিক সাউন্ডে লক করা হয়।",
            safetyTipBn = "ব্যবহার শেষে স্প্রিং টেনশন শূন্য বা সর্বনিম্ন মানে ফিরিয়ে সংরক্ষণ করুন।"
        ),
        ElectricalTool(
            id = 23,
            nameBn = "বল পিন ইলেকট্রিশিয়ান হ্যামার",
            nameEn = "Ball Peen Electrician Hammer",
            category = ToolCategory.HAND_TOOLS,
            icon = Icons.Default.Hardware,
            accentColor = Color(0xFF059669),
            keyUseBn = "ক্ল্যাম্প, রাওয়াল প্লাগ ও চ্যানেল ফিক্সিং",
            shortDescBn = "একপাশে চ্যাপ্টা ও অন্যপাশে গোলাকার মুখযুক্ত সুষম ওজনের হাতুড়ি ক্যাবল ক্ল্যাম্প ও চ্যানেল বসাতে নিখুঁত আঘাত হানে।",
            safetyTipBn = "হাতুড়ির হাতলে গ্রিপ শক্ত রাখুন এবং কাঠের বা ফাইবার হাতলে ফাটল থাকলে ব্যবহার করবেন না।"
        ),
        ElectricalTool(
            id = 24,
            nameBn = "অটোমেটিক সেন্টার পাঞ্চ",
            nameEn = "Automatic Center Punch",
            category = ToolCategory.HAND_TOOLS,
            icon = Icons.Default.FilterCenterFocus,
            accentColor = Color(0xFF059669),
            keyUseBn = "মেটাল প্যানেল বোর্ডে ড্রিলিং মার্কিং খাঁজ তৈরি",
            shortDescBn = "হাতুড়ি ছাড়াই শুধু চাপ দিলে ভেতরের স্প্রিং মেকানিজম মেটাল শিটে স্পষ্ট খাঁজ কাটে, যাতে ড্রিল বিট পিছলে না যায়।",
            safetyTipBn = "ড্রিলিং করার পূর্বে সবসময় সেন্টার পাঞ্চ দিয়ে মার্ক নিশ্চিত করুন।"
        ),

        // ==========================================
        // 3. STRIPPING, CUTTING & TERMINATION (25-32)
        // ==========================================
        ElectricalTool(
            id = 25,
            nameBn = "অটোমেটিক ওয়্যার স্ট্রিপার",
            nameEn = "Automatic Self-Adjusting Wire Stripper",
            category = ToolCategory.TERMINATION,
            icon = Icons.Default.ContentCut,
            accentColor = Color(0xFFD97706),
            keyUseBn = "তারের ভেতরের কপার অক্ষত রেখে কভার ছিলা",
            shortDescBn = "এক চাপে তারের বাইরের ইনসুলেশন কেটে টেনে খুলে দেয়। ০.২ থেকে ৬ আরএম পর্যন্ত তারে স্বয়ংক্রিয় অ্যাডজাস্ট হয়।",
            safetyTipBn = "খুব শক্ত ডাবল শিথড কেবলের ক্ষেত্রে কেবল নাইফ দিয়ে আগে বাইরের জ্যাকেট খুলে নিন।"
        ),
        ElectricalTool(
            id = 26,
            nameBn = "ম্যানুয়াল অ্যাডজাস্টেবল স্ট্রিপার",
            nameEn = "Adjustable Wire Stripper with Screw",
            category = ToolCategory.TERMINATION,
            icon = Icons.Default.Tune,
            accentColor = Color(0xFFD97706),
            keyUseBn = "নির্দিষ্ট গেজের তারের ইনসুলেশন নিখুঁত ছিলা",
            shortDescBn = "স্ক্রু দিয়ে গেজ লক করে রাখলে একই সাইজের শতাধিক তারের খোসা এক মাপ অনুযায়ী দ্রুত ছিলতে পারে।",
            safetyTipBn = "গেজ বেশি টাইট করবেন না যাতে তামার সূক্ষ্ম স্ট্র্যান্ড কেটে না যায়।"
        ),
        ElectricalTool(
            id = 27,
            nameBn = "র্যাচেট হেভি কেবল কাটার",
            nameEn = "Ratchet Heavy Cable Cutter",
            category = ToolCategory.TERMINATION,
            icon = Icons.Default.Hardware,
            accentColor = Color(0xFFD97706),
            keyUseBn = "মোটা কপার ও অ্যালুমিনিয়াম কেবল সহজে কাটা",
            shortDescBn = "র্যাচেট গিয়ার মেকানিজমে সামান্য হাতল চাপে ৫০ থেকে ৩০০ স্কয়ার মিলিমিটার কেবল চ্যাপ্টা না করে সুন্দর বৃত্তাকারে কাটে।",
            safetyTipBn = "স্টিল আর্মর বা এসিএসআর তার কাটার জন্য এই কাটার নিষিদ্ধ; ব্লেড ভেঙে যাবে।"
        ),
        ElectricalTool(
            id = 28,
            nameBn = "হাইড্রোলিক কেবল লাগ ক্রিম্পার",
            nameEn = "Hydraulic Cable Lug Crimper",
            category = ToolCategory.TERMINATION,
            icon = Icons.Default.PrecisionManufacturing,
            accentColor = Color(0xFFD97706),
            keyUseBn = "১৬ থেকে ৪০০ স্কয়ার মিমি কেবল লাগ পাঞ্চিং",
            shortDescBn = "হাইড্রোলিক তেল চাপে টন পরিমাণ বল তৈরি করে হেভি ডিউটি কপার ও অ্যালুমিনিয়াম লাগ কেবলের সাথে নিরেটভাবে লক করে।",
            safetyTipBn = "সঠিক সাইজের ডাই ব্যবহার করুন এবং পাঞ্চ শেষ হলে প্রেসার রিলিজ ভাল্ব ঘুরিয়ে ডাই খুলুন।"
        ),
        ElectricalTool(
            id = 29,
            nameBn = "হ্যান্ড র্যাচেট লাগ ক্রিম্পিং প্লায়ার্স",
            nameEn = "Hand Ratchet Terminal Crimper",
            category = ToolCategory.TERMINATION,
            icon = Icons.Default.Build,
            accentColor = Color(0xFFD97706),
            keyUseBn = "পিন, রিং ও ফর্ক লাগ ওয়্যারিংয়ে পাঞ্চ করা",
            shortDescBn = "১.৫ থেকে ১০ আরএম কন্ট্রোল তারে ইনসুলেটেড টার্মিনাল পাঞ্চিংয়ে ব্যবহৃত হয়। সম্পূর্ণ ক্রিম্প না হওয়া পর্যন্ত লক খোলে না।",
            safetyTipBn = "ক্রিম্প করার পর তার টেনে পরীক্ষা করুন যেন টার্মিনাল খুলে না আসে।"
        ),
        ElectricalTool(
            id = 30,
            nameBn = "ইলেকট্রিশিয়ান কেবল শিথ নাইফ",
            nameEn = "Cable Sheath Stripping Knife",
            category = ToolCategory.TERMINATION,
            icon = Icons.Default.Edit,
            accentColor = Color(0xFFD97706),
            keyUseBn = "ভারী কেবলের বাইরের শক্ত পিভিসি জ্যাকেট কাটা",
            shortDescBn = "হুক ব্লেড বা গাইড শু যুক্ত থাকায় ভেতরের তারের ইনসুলেশন ক্ষতিগ্রস্ত না করে কেবল বাইরের স্তর সোজা চিরে ফেলা যায়।",
            safetyTipBn = "কাটার সময় সবসময় ব্লেডের দিক শরীরের বিপরীত দিকে রাখুন।"
        ),
        ElectricalTool(
            id = 31,
            nameBn = "হিট গান / হট এয়ার ব্লোয়ার",
            nameEn = "Electric Heat Shrink Gun (2000W)",
            category = ToolCategory.TERMINATION,
            icon = Icons.Default.Whatshot,
            accentColor = Color(0xFFD97706),
            keyUseBn = "হিট শ্রিঙ্ক টিউব গরম করে কেবল জয়েন্টে সিল",
            shortDescBn = "৩০০-৬০০ ডিগ্রি সেলসিয়াসের উত্তপ্ত বাতাস দিয়ে তাপ-সংকোচনশীল পাইপ (Heat Shrink Sleeve) কেবল জয়েন্টে ওয়াটারপ্রুফ ইনসুলেশন বানায়।",
            safetyTipBn = "দাহ্য পদার্থ বা তারের পিভিসি বেশি পুড়িয়ে ফেলা থেকে নিরাপদ দূরত্ব বজায় রাখুন।"
        ),
        ElectricalTool(
            id = 32,
            nameBn = "কেবল টাই টেনশনার গান",
            nameEn = "Cable Tie Tensioner & Cut Tool",
            category = ToolCategory.TERMINATION,
            icon = Icons.Default.Tune,
            accentColor = Color(0xFFD97706),
            keyUseBn = "প্যানেল বোর্ডের ক্যাবল টাই এক চাপে টাইট ও কাটা",
            shortDescBn = "ট্রিগার টানলে কেবল টাই নির্দিষ্ট শক্তিতে টাইট হয়ে স্বয়ংক্রিয়ভাবে ধারালো ব্লেডে অতিরিক্ত অংশ সমানভাবে কেটে ফেলে।",
            safetyTipBn = "প্যানেলে প্রফেশনাল ও ঝকঝকে ড্রেসিংয়ের জন্য এই টুল অপরিহার্য।"
        ),

        // ==========================================
        // 4. POWER TOOLS & INSTALLATION (33-38)
        // ==========================================
        ElectricalTool(
            id = 33,
            nameBn = "রোটারি হ্যামার ড্রিল",
            nameEn = "Rotary SDS Plus Hammer Drill",
            category = ToolCategory.POWER_TOOLS,
            icon = Icons.Default.Hardware,
            accentColor = Color(0xFFDC2626),
            keyUseBn = "কংক্রিট দেয়াল ও ছাদে ড্রিল ও রয়্যাল প্লাগ বসানো",
            shortDescBn = "রোটারি হ্যামারিং অ্যাকশন দিয়ে শক্ত ঢালাই ও ইটের দেয়ালে দ্রুত ছিদ্র করা হয় পাইপ ক্ল্যাম্প ও বোর্ড লাগানোর জন্য।",
            safetyTipBn = "দেয়ালে ড্রিল করার আগে ভেতরের কনসিল্ড তার বা পানির পাইপ শনাক্তকারী মিটার দিয়ে চেক করুন।"
        ),
        ElectricalTool(
            id = 34,
            nameBn = "কর্ডলেস ইমপ্যাক্ট ড্রাইভার",
            nameEn = "Cordless 18V/20V Impact Driver",
            category = ToolCategory.POWER_TOOLS,
            icon = Icons.Default.BatteryChargingFull,
            accentColor = Color(0xFFDC2626),
            keyUseBn = "ব্যাটারি চালিত পোর্টেবল স্ক্রু ড্রাইভার",
            shortDescBn = "বৈদ্যুতিক সকেট বা লাইন ছাড়াই মইয়ের ওপর উঠে দ্রুত গ্যাং বক্স, সুইচবোর্ড ও ট্রের হাজারো স্ক্রু সহজে লাগানো যায়।",
            safetyTipBn = "প্লাস্টিক বোর্ডে স্ক্রু করার সময় টর্ক কম রাখুন যাতে প্লাস্টিক ভেঙে না যায়।"
        ),
        ElectricalTool(
            id = 35,
            nameBn = "ওয়াল চেজার মেশিন",
            nameEn = "Wall Chaser Grooving Machine",
            category = ToolCategory.POWER_TOOLS,
            icon = Icons.Default.ViewAgenda,
            accentColor = Color(0xFFDC2626),
            keyUseBn = "কনসিল্ড ওয়্যারিংয়ের জন্য দেয়ালে খাঁজ কাটা",
            shortDescBn = "দুটো সমান্তরাল ডায়মন্ড ব্লেড দিয়ে পাইপ বসানোর নির্দিষ্ট গভীরতায় দেয়াল কেটে দেয়, যা পরবর্তীতে ছেনি দিয়ে সহজেই ভাঙা যায়।",
            safetyTipBn = "প্রচুর ধুলাবালি তৈরি হওয়ায় ওয়াটার স্প্রে কানেক্ট করুন এবং এন৯৫ মাস্ক ও গগলস পরিধান করুন।"
        ),
        ElectricalTool(
            id = 36,
            nameBn = "অ্যাঙ্গেল গ্রাইন্ডার",
            nameEn = "Angle Grinder (4-inch / 5-inch)",
            category = ToolCategory.POWER_TOOLS,
            icon = Icons.Default.Engineering,
            accentColor = Color(0xFFDC2626),
            keyUseBn = "কেবল ট্রে, জিআই পাইপ ও মেটাল চ্যানেল কাটা",
            shortDescBn = "মেটাল কাটিং ডিস্ক ব্যবহার করে প্যানেল তৈরির সময় অ্যাঙ্গেল রড ও কেবল ট্রে দ্রুত সাইজ অনুযায়ী কাটা হয়।",
            safetyTipBn = "কখনো ব্লেড গার্ড ছাড়া গ্রাইন্ডার চালাবেন না এবং ফেস শিল্ড ব্যবহার করুন।"
        ),
        ElectricalTool(
            id = 37,
            nameBn = "হোল স / কোর কাটার সেট",
            nameEn = "Bi-Metal Hole Saw Set",
            category = ToolCategory.POWER_TOOLS,
            icon = Icons.Default.RadioButtonUnchecked,
            accentColor = Color(0xFFDC2626),
            keyUseBn = "প্যানেল বোর্ড ও ফলস সিলিংয়ে বড় গোল ছিদ্র",
            shortDescBn = "কেবল গ্ল্যান্ড বসানোর জন্য মেটাল এনক্লোজারে অথবা স্পটলাইট বসানোর জন্য জিপসাম সিলিংয়ে নিখুঁত গোল হোল তৈরি করে।",
            safetyTipBn = "মেটালে ড্রিলের সময় সামান্য কাটিং অয়েল ব্যবহার করলে ব্লেড ধারালো থাকে ও ঠান্ডা থাকে।"
        ),
        ElectricalTool(
            id = 38,
            nameBn = "ইলেকট্রিক এয়ার ব্লোয়ার",
            nameEn = "Electric Air Dust Blower",
            category = ToolCategory.POWER_TOOLS,
            icon = Icons.Default.Air,
            accentColor = Color(0xFFDC2626),
            keyUseBn = "সাবস্টেশন ও প্যানেল বোর্ডের ধুলাবালি পরিষ্কার",
            shortDescBn = "উচ্চগতির বাতাসের প্রবাহে ড্রাইভ, ইনভার্টার ও কন্ট্রোল কার্ডের ইলেকট্রনিক কম্পোনেন্ট পরিষ্কার করে আর্কিং রোধ করে।",
            safetyTipBn = "ব্লোয়ার ব্যবহারের পূর্বে মেইন পাওয়ার বন্ধ নিশ্চিত করুন এবং সেন্সিটিভ ফ্যানের পাখা আটকে রাখুন।"
        ),

        // ==========================================
        // 5. CONDUIT & WIRE PULLING TOOLS (39-46)
        // ==========================================
        ElectricalTool(
            id = 39,
            nameBn = "পিভিসি পাইপ বেন্ডিং স্প্রিং",
            nameEn = "PVC Conduit Bending Spring",
            category = ToolCategory.CONDUIT,
            icon = Icons.Default.Gesture,
            accentColor = Color(0xFF7C3AED),
            keyUseBn = "পাইপ বাঁকানোর সময় ভেতরে চ্যাপ্টা হওয়া রোধ",
            shortDescBn = "স্প্রিংটি পিভিসি পাইপের ভেতর ঢুকিয়ে হাঁটু বা হাত দিয়ে বাঁকালে পাইপ সুন্দর ৯০ ডিগ্রি বেন্ড হয় কিন্তু চ্যাপ্টা বা ভেঙে যায় না।",
            safetyTipBn = "ঠান্ডা আবহাওয়ায় পাইপ বেন্ড করার আগে হাত দিয়ে হালকা ঘষে উষ্ণ করে নিন।"
        ),
        ElectricalTool(
            id = 40,
            nameBn = "জিআই কনডুইট পাইপ বেন্ডার",
            nameEn = "GI Metal Conduit Bender with Foot Pedal",
            category = ToolCategory.CONDUIT,
            icon = Icons.Default.Timeline,
            accentColor = Color(0xFF7C3AED),
            keyUseBn = "মেটাল জিআই পাইপ নিখুঁত ডিগ্রিতে বাঁকানো",
            shortDescBn = "পায়ের প্যাডেলে ভর দিয়ে লিভার টেনে ৪৫°, ৯০° ও স্যাডল বেন্ড তৈরি করা হয় যাতে পাইপের ক্যালিবার অক্ষত থাকে।",
            safetyTipBn = "একটানা মসৃণ চাপে বেন্ড করুন, ঝাঁকুনি দিলে পাইপে ভাঁজ পড়বে।"
        ),
        ElectricalTool(
            id = 41,
            nameBn = "ফিশ টেপ / স্টিল কেবল পুলার",
            nameEn = "Nylon / Spring Steel Fish Tape (30M)",
            category = ToolCategory.CONDUIT,
            icon = Icons.Default.AllInclusive,
            accentColor = Color(0xFF7C3AED),
            keyUseBn = "পাইপের ভেতর দিয়ে তার একপ্রান্ত থেকে অন্যপ্রান্তে টানা",
            shortDescBn = "কনসিল্ড পাইপের ভেতর স্টিল বা নাইলন ফিশ টেপ ঠেলে দিয়ে বিপরীত দিক থেকে তার বেঁধে সহজে টেনে নেওয়া হয়।",
            safetyTipBn = "লাইভ লাইনের কাছাকাছি কাজের সময় কন্ডাক্টিভ স্টিলের বদলে নন-কন্ডাক্টিভ ফাইবারগ্লাস ফিশ টেপ ব্যবহার করুন।"
        ),
        ElectricalTool(
            id = 42,
            nameBn = "কেবল পুলিং সক্স / মেশ গ্রিপ",
            nameEn = "Cable Pulling Mesh Grip (Cable Sock)",
            category = ToolCategory.CONDUIT,
            icon = Icons.Default.Layers,
            accentColor = Color(0xFF7C3AED),
            keyUseBn = "ভারী আন্ডারগ্রাউন্ড কেবল নিরাপদে টানা",
            shortDescBn = "জালের মতো বোনা মেটাল গ্রিপ কেবলের মাথায় পরালে টানার সময় নিজে থেকেই কেবলের গায়ে টাইট হয়ে আটকে যায়।",
            safetyTipBn = "কেবলের বাইরের জ্যাকেটে অতিরিক্ত চাপ এড়াতে ক্যাবল পুলিং লুব্রিকেন্ট জেল ব্যবহার করুন।"
        ),
        ElectricalTool(
            id = 43,
            nameBn = "লেজার লেভেলার ৩৬০°",
            nameEn = "360-Degree Self-Leveling Cross Line Laser",
            category = ToolCategory.CONDUIT,
            icon = Icons.Default.ViewInAr,
            accentColor = Color(0xFF7C3AED),
            keyUseBn = "সুইচবোর্ড ও লাইটিং পয়েন্ট এক সরলরেখায় লেভেলিং",
            shortDescBn = "সবুজ লেজার রশ্মি পুরো রুমে অনুভূমিক ও উল্লম্ব রেখা ফেলে দেয়, ফলে সব সুইচবোর্ড মেপে মেপে এক উচ্চতায় বসানো যায়।",
            safetyTipBn = "লেজার রশ্মির দিকে সরাসরি খালি চোখে তাকাবেন না।"
        ),
        ElectricalTool(
            id = 44,
            nameBn = "ম্যাগনেটিক স্পিরিট লেভেল",
            nameEn = "Magnetic Torpedo Spirit Level",
            category = ToolCategory.CONDUIT,
            icon = Icons.Default.VerticalAlignBottom,
            accentColor = Color(0xFF7C3AED),
            keyUseBn = "ডিবি বোর্ড ও সুইচবোর্ড সোজা রাখা",
            shortDescBn = "বুদবুদ ভায়াল দেখে নিশ্চিত হওয়া যায় প্যানেল বোর্ড পুরোপুরি খাড়া ও সোজাভাবে দেয়ালে বসানো হয়েছে। চুম্বক থাকায় মেটালে আটকে থাকে।",
            safetyTipBn = "প্লাস্টারের আগে ও পরে দুইবারই স্পিরিট লেভেল দিয়ে বক্স পরীক্ষা করুন।"
        ),
        ElectricalTool(
            id = 45,
            nameBn = "স্টিল মেজারিং টেপ",
            nameEn = "Steel Measuring Tape (5M / 8M)",
            category = ToolCategory.CONDUIT,
            icon = Icons.Default.Straighten,
            accentColor = Color(0xFF7C3AED),
            keyUseBn = "তারের দৈর্ঘ্য, বোর্ড ও ড্রপ পাইপের পরিমাপ",
            shortDescBn = "বিল্ডিং ওয়্যারিংয়ের মালামালের হিসাব ও ড্রপ পাইপের সঠিক দৈর্ঘ্য মাপতে প্রতিটি ইলেকট্রিশিয়ানের পকেটে থাকা টুল।",
            safetyTipBn = "লাইভ প্যানেলের ভেতরে স্টিলের টেপ ঢোকাবেন না, শর্ট সার্কিট হতে পারে।"
        ),
        ElectricalTool(
            id = 46,
            nameBn = "হেক্সো ফ্রেম ও ব্লেড",
            nameEn = "Hacksaw Frame with 24TPI Bi-metal Blade",
            category = ToolCategory.CONDUIT,
            icon = Icons.Default.ContentCut,
            accentColor = Color(0xFF7C3AED),
            keyUseBn = "মেটাল রড, জিআই পাইপ ও প্লাস্টিক চ্যানেল কাটা",
            shortDescBn = "২৪ টিথ পার ইঞ্চি (TPI) ব্লেড পিভিসি পাইপ ও চ্যানেল কাটার জন্য সেরা। সোজা কাটার জন্য টেনশন নাট টাইট রাখতে হয়।",
            safetyTipBn = "ব্লেডের দাঁতের মুখ সবসময় সামনের দিকে মুখ করে ফিট করুন।"
        ),

        // ==========================================
        // 6. SAFETY & PPE EQUIPMENT (47-50)
        // ==========================================
        ElectricalTool(
            id = 47,
            nameBn = "হাই-ভোল্টেজ ইনসুলেটেড গ্লাভস",
            nameEn = "Class 0 / Class 1 Electrical Safety Gloves",
            category = ToolCategory.SAFETY,
            icon = Icons.Default.Shield,
            accentColor = Color(0xFFE11D48),
            keyUseBn = "১০০০ ভোল্ট পর্যন্ত লাইভ লাইনে শক নিরোধক সুরক্ষা",
            shortDescBn = "বিশেষ ডাই-ইলেকট্রিক রাবারে তৈরি। হাতের মাধ্যমে শরীরে বিদ্যুৎ প্রবেশের প্রাণঘাতী ঝুঁকি থেকে রক্ষা করে।",
            safetyTipBn = "প্রতিবার পরার আগে ফুঁ দিয়ে বাতাস ভরে পরীক্ষা করুন কোনো সূক্ষ্ম ছিদ্র বা লিক আছে কিনা।"
        ),
        ElectricalTool(
            id = 48,
            nameBn = "ইলেকট্রিশিয়ান সেফটি হেলমেট উইথ শিল্ড",
            nameEn = "Safety Helmet with Arc Flash Face Shield",
            category = ToolCategory.SAFETY,
            icon = Icons.Default.Security,
            accentColor = Color(0xFFE11D48),
            keyUseBn = "মাথায় আঘাত ও আর্ক ফ্ল্যাশের আগুন থেকে রক্ষা",
            shortDescBn = "ক্লাস ই (২০,০০০ ভোল্ট প্রুফ) হেলমেট এবং স্বচ্ছ পলিকার্বোনেট ফেস শিল্ড চোখের কর্নিয়া ও মুখের ত্বক রক্ষা করে।",
            safetyTipBn = "সাবস্টেশনে বা ৩-ফেজ ব্রেকার অন/অফ করার সময় ফেস শিল্ড নামিয়ে কাজ করুন।"
        ),
        ElectricalTool(
            id = 49,
            nameBn = "ডাই-ইলেকট্রিক অ্যান্টি-স্কিড সেফটি শু",
            nameEn = "Dielectric Anti-Skid Electrical Safety Boots",
            category = ToolCategory.SAFETY,
            icon = Icons.Default.DirectionsWalk,
            accentColor = Color(0xFFE11D48),
            keyUseBn = "পায়ের মাধ্যমে মাটিতে বিদ্যুৎ প্রবাহ বন্ধ রাখা",
            shortDescBn = "মোটা রাবার সোলযুক্ত ইনসুলেটেড বুট যা মাটির সাথে শরীরকে সম্পূর্ণ ডিসকানেক্ট রাখে এবং পায়ের পাতায় ভারী প্যানেল পড়া থেকে বাঁচায়।",
            safetyTipBn = "জুতার নিচে ভেজা বা মেটাল পেরেক বিদ্ধ থাকলে লাইভ লাইনে যাওয়া বিপজ্জনক।"
        ),
        ElectricalTool(
            id = 50,
            nameBn = "লকআউট-ট্যাগআউট (LOTO) কিট",
            nameEn = "Lockout-Tagout (LOTO) Safety Breaker Padlock",
            category = ToolCategory.SAFETY,
            icon = Icons.Default.Lock,
            accentColor = Color(0xFFE11D48),
            keyUseBn = "লাইনে কাজ করার সময় ব্রেকার অফ অবস্থায় লক করা",
            shortDescBn = "লাইনে ইলেকট্রিশিয়ান কাজ করার সময় যাতে অন্য কেউ ভুলবশত ব্রেকার অন না করে, সেজন্য ব্রেকারে লাল রঙের প্যাডলক ও বিপদ ট্যাগ ঝুলানো হয়।",
            safetyTipBn = "চাবি শুধুমাত্র যে কর্মী লাইনে কাজ করছেন তার ব্যক্তিগত পকেটে সংরক্ষিত থাকবে।"
        )
    )
}

@Composable
fun ElectricalToolsScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = remember { ElectricalApp.getApp(context) }
    val currentLanguage by app.settingsManager.languageFlow.collectAsState(initial = "BN")
    val isEn = currentLanguage == "EN"

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ToolCategory.ALL) }

    val filteredTools = remember(searchQuery, selectedCategory) {
        ElectricalToolsCatalog.tools.filter { tool ->
            val matchesCategory = selectedCategory == ToolCategory.ALL || tool.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                tool.nameBn.contains(searchQuery.trim(), ignoreCase = true) ||
                tool.nameEn.contains(searchQuery.trim(), ignoreCase = true) ||
                tool.keyUseBn.contains(searchQuery.trim(), ignoreCase = true) ||
                tool.shortDescBn.contains(searchQuery.trim(), ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("electrical_tools_screen_root")
    ) {
        // Main Scrollable List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Search Box
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = if (isEn) "Search 50 tools (e.g. Multimeter, Plier, Drill)..." else "৫০টি টুলস সার্চ করুন (যেমন: টেস্টার, প্লায়ার্স, ড্রিল)...",
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF64748B))
                            }
                        }
                    }
                }
            }

            // 2. Category Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ToolCategory.values().forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = {
                                Text(
                                    text = if (isEn) cat.titleEn else cat.titleBn,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF082D63),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // 3. Count Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "Total Tools Found: ${filteredTools.size}" else "টুলস সংখ্যা: ${filteredTools.size} টি",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = if (isEn) "Click to copy summary" else "কপি করতে কার্ডে ট্যাপ করুন",
                        fontSize = 11.sp,
                        color = Color(0xFF0284C7),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 4. Tools Cards
            items(filteredTools, key = { it.id }) { tool ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipText = "${tool.nameBn} (${tool.nameEn})\nকাজ: ${tool.keyUseBn}\nবিবরণ: ${tool.shortDescBn}\nটিপস: ${tool.safetyTipBn}"
                            val clip = ClipData.newPlainText("Tool Info", clipText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "${tool.nameBn} তথ্য কপি হয়েছে", Toast.LENGTH_SHORT).show()
                        },
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
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(tool.accentColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tool.icon,
                                    contentDescription = tool.nameEn,
                                    tint = tool.accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${tool.id}. ${tool.nameBn}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Surface(
                                        color = tool.category.color.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (isEn) tool.category.name else tool.category.titleBn.split(" ")[0],
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = tool.category.color,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = tool.nameEn,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Key use pill
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isEn) "Use: " else "মূল কাজ: ",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF082D63)
                                )
                                Text(
                                    text = tool.keyUseBn,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0284C7)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = tool.shortDescBn,
                            fontSize = 12.sp,
                            color = Color(0xFF334155),
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Safety Tip",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${if (isEn) "Safety: " else "সতর্কতা: "}${tool.safetyTipBn}",
                                fontSize = 11.sp,
                                color = Color(0xFF991B1B),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
