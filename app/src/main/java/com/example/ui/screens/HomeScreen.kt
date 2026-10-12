package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ElectricalApp
import com.example.R
import com.example.data.calculator.CalculatorRegistry
import com.example.data.model.CalculatorCategory
import com.example.data.model.CalculatorDefinition
import com.example.data.model.CalculatorLevel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToCalculator: (Int) -> Unit,
    onNavigateToBuildingMaterial: () -> Unit,
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToCalculatorsList: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProject: () -> Unit = {},
    onNavigateToInformation: () -> Unit = {},
    onNavigateToAdvance: () -> Unit = {},
    onNavigateToBasic: () -> Unit = {},
    onNavigateToLower: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val app = remember { ElectricalApp.getApp(context) }
    val historyList by app.database.historyDao().getAllHistory().collectAsState(initial = emptyList())
    val favoriteIds by app.settingsManager.favoritesFlow.collectAsState(initial = emptySet())

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<CalculatorCategory?>(null) }

    val filteredList = remember(searchQuery, selectedCategoryFilter) {
        var res = if (searchQuery.isNotBlank()) CalculatorRegistry.search(searchQuery)
        else CalculatorRegistry.allCalculators

        if (selectedCategoryFilter != null) {
            res = res.filter { it.category == selectedCategoryFilter }
        }
        res
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("home_screen_column"),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // 1. HERO HEADER AREA with High-Definition Electrical Control Room Wallpaper & Glassmorphism
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF061426),
                                Color(0xFF0A1E38),
                                Color(0xFF0F2E59)
                            )
                        )
                    )
            ) {
                // Background Hero Banner
                Image(
                    painter = painterResource(id = R.drawable.img_electrical_hero),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(265.dp),
                    contentScale = ContentScale.Crop,
                    alpha = 0.32f
                )

                // Gradient scrim overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(265.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xCC050E1D),
                                    Color(0xAA0A1E38),
                                    Color(0xF00F172A)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Actions Row: Information Guide on Left, Settings on Right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Information Guide Chip
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { onNavigateToInformation() },
                            color = Color(0x3300E5FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6600E5FF))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Information",
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "সহায়িকা (১৬৭)",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE0F7FA)
                                )
                            }
                        }

                        // Settings Pill
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { onNavigateToSettings() },
                            color = Color(0x22FFFFFF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "সেটিংস",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Center App Identity: Glowing Emblem + Brand Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(14.dp, CircleShape, spotColor = Color(0xFF00E5FF), ambientColor = Color(0xFF00E5FF))
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFF00E5FF), CircleShape)
                                .background(Color(0xFF0B192C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo),
                                contentDescription = "App Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Electrical Cal/Pro",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFF59E0B),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "167 PRO",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1E1B4B),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "পেশাদার ইলেকট্রিক্যাল ইঞ্জিনিয়ারিং ক্যালকুলেশন ও এস্টিমেশন",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFBAE6FD)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Rounded Capsule Search Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .shadow(6.dp, RoundedCornerShape(23.dp))
                            .clip(RoundedCornerShape(23.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(23.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "ক্যালকুলেটর খুঁজুন (নাম বা বিষয়)...",
                                        fontSize = 13.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 13.5.sp,
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("home_search_input")
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal Quick Category Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CategoryPill(
                            title = "সবগুলো (১৬৭)",
                            isSelected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null }
                        )
                        CategoryPill(
                            title = "ফ্যাক্টরি লোড",
                            isSelected = selectedCategoryFilter == CalculatorCategory.FACTORY,
                            onClick = { selectedCategoryFilter = CalculatorCategory.FACTORY }
                        )
                        CategoryPill(
                            title = "মোটর ও পাম্প",
                            isSelected = selectedCategoryFilter == CalculatorCategory.MOTOR,
                            onClick = { selectedCategoryFilter = CalculatorCategory.MOTOR }
                        )
                        CategoryPill(
                            title = "ট্রান্সফরমার",
                            isSelected = selectedCategoryFilter == CalculatorCategory.TRANSFORMER,
                            onClick = { selectedCategoryFilter = CalculatorCategory.TRANSFORMER }
                        )
                        CategoryPill(
                            title = "ক্যাবল সাইজ",
                            isSelected = selectedCategoryFilter == CalculatorCategory.CABLE,
                            onClick = { selectedCategoryFilter = CalculatorCategory.CABLE }
                        )
                        CategoryPill(
                            title = "সোলার পাওয়ার",
                            isSelected = selectedCategoryFilter == CalculatorCategory.POWER,
                            onClick = { selectedCategoryFilter = CalculatorCategory.POWER }
                        )
                        CategoryPill(
                            title = "ব্রেকার ও সুইচগিয়ার",
                            isSelected = selectedCategoryFilter == CalculatorCategory.PROTECTION,
                            onClick = { selectedCategoryFilter = CalculatorCategory.PROTECTION }
                        )
                        CategoryPill(
                            title = "বিল্ডিং ওয়্যারিং",
                            isSelected = selectedCategoryFilter == CalculatorCategory.BUILDING,
                            onClick = { selectedCategoryFilter = CalculatorCategory.BUILDING }
                        )
                    }
                }
            }
        }

        // 2. QUICK STAT CARDS ROW (Favorites | History | Material Estimate)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Favorites Card
                StatDashboardCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Star,
                    iconBgColor = Color(0xFFFEF3C7),
                    iconTint = Color(0xFFD97706),
                    title = "পছন্দের হিসাব",
                    subtitle = "${favoriteIds.size} টি সেভ করা",
                    onClick = onNavigateToFavorites
                )

                // History Card
                StatDashboardCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.History,
                    iconBgColor = Color(0xFFE0F2FE),
                    iconTint = Color(0xFF0284C7),
                    title = "হিসাবের ইতিহাস",
                    subtitle = "${historyList.size} টি সম্পন্ন",
                    onClick = onNavigateToHistory
                )

                // Material Estimate Card
                StatDashboardCard(
                    modifier = Modifier.weight(1.1f),
                    icon = Icons.Default.Description,
                    iconBgColor = Color(0xFFDCFCE7),
                    iconTint = Color(0xFF16A34A),
                    title = "মালামাল এস্টিমেট",
                    subtitle = "বিল্ডিং ওয়্যারিং বিওকিউ",
                    onClick = onNavigateToBuildingMaterial
                )
            }
        }

        // 3. LEVEL SELECTION TIERS (Advance 55, Basic 61, Lower 51, Building Material)
        item {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ক্যালকুলেটর লেভেল ও ক্যাটাগরি",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "সব ক্যালকুলেটর (১৬৭) ➔",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.clickable { onNavigateToCalculatorsList() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LevelBadgeCard(
                        modifier = Modifier.weight(1f),
                        title = "Advance",
                        count = "৫৫ টি",
                        subtitle = "পাওয়ার ও ফ্যাক্টরি",
                        gradient = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED)),
                        icon = Icons.Default.ElectricBolt,
                        onClick = onNavigateToAdvance
                    )
                    LevelBadgeCard(
                        modifier = Modifier.weight(1f),
                        title = "Basic",
                        count = "৬১ টি",
                        subtitle = "ওহম ও সার্কিট",
                        gradient = listOf(Color(0xFF0284C7), Color(0xFF0EA5E9)),
                        icon = Icons.Default.Speed,
                        onClick = onNavigateToBasic
                    )
                    LevelBadgeCard(
                        modifier = Modifier.weight(1f),
                        title = "Lower",
                        count = "৫১ টি",
                        subtitle = "একক রূপান্তর",
                        gradient = listOf(Color(0xFFD97706), Color(0xFFF59E0B)),
                        icon = Icons.Default.Cable,
                        onClick = onNavigateToLower
                    )
                }
            }
        }

        // 4. FEATURED CONTRACT PROJECT MANAGER CARD (Technician 5-Project System)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .clickable(onClick = onNavigateToProject),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    // Futuristic Blueprint Project Artwork
                    Image(
                        painter = painterResource(id = R.drawable.img_project_tech),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Dark gradient scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xF50A192F),
                                        Color(0xDD0F2547),
                                        Color(0x991E3A8A)
                                    )
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF10B981)
                            ) {
                                Text(
                                    text = "★ টেকনিশিয়ান স্পেশাল",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "কন্ট্রাক্ট প্রজেক্ট ও কিস্তির হিসাব",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "৫টি প্রজেক্ট খতিয়ান • কিস্তির জমা-খরচ ট্র্যাকিং • অফিশিয়াল PDF ভাউচার ও প্রিন্ট",
                                fontSize = 11.sp,
                                color = Color(0xFFBAE6FD),
                                maxLines = 2,
                                lineHeight = 15.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF00E5FF),
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ম্যানেজ ➔",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0A192F)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. QUICK ACCESS POPULAR TOOLS (Transformer, Motor, Cable, Generator)
        item {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                Text(
                    text = "জনপ্রিয় ও প্রয়োজনীয় ক্যালকুলেটর",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickAccessCard(
                        modifier = Modifier.weight(1f),
                        title = "ট্রান্সফরমার",
                        subtitle = "লোড ও কারেন্ট",
                        icon = Icons.Default.Memory,
                        bgColor = Color(0xFFE0F2FE),
                        iconColor = Color(0xFF0284C7),
                        onClick = { onNavigateToCalculator(2) }
                    )
                    QuickAccessCard(
                        modifier = Modifier.weight(1f),
                        title = "মোটর FLC",
                        subtitle = "ক্ষমতা ও কারেন্ট",
                        icon = Icons.Default.Bolt,
                        bgColor = Color(0xFFDBEAFE),
                        iconColor = Color(0xFF2563EB),
                        onClick = { onNavigateToCalculator(3) }
                    )
                    QuickAccessCard(
                        modifier = Modifier.weight(1f),
                        title = "ক্যাবল সাইজ",
                        subtitle = "ড্রপ ও সাইজিং",
                        icon = Icons.Default.Cable,
                        bgColor = Color(0xFFFFEDD5),
                        iconColor = Color(0xFFEA580C),
                        onClick = { onNavigateToCalculator(1) }
                    )
                    QuickAccessCard(
                        modifier = Modifier.weight(1f),
                        title = "জেনারেটর",
                        subtitle = "kVA রেটিং",
                        icon = Icons.Default.PrecisionManufacturing,
                        bgColor = Color(0xFFF1F5F9),
                        iconColor = Color(0xFF475569),
                        onClick = { onNavigateToCalculator(13) }
                    )
                }
            }
        }

        // 6. MAIN CATEGORY TILES (8 Categories Grid)
        item {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text(
                    text = "প্রধান বিষয়ভিত্তিক ক্যাটাগরি",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryIconCard(
                        modifier = Modifier.weight(1f),
                        title = "ফ্যাক্টরি লোড",
                        subtitle = "Factory & Plant",
                        icon = Icons.Default.Factory,
                        color = Color(0xFF0284C7),
                        isSelected = selectedCategoryFilter == CalculatorCategory.FACTORY,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == CalculatorCategory.FACTORY) null else CalculatorCategory.FACTORY
                        }
                    )
                    CategoryIconCard(
                        modifier = Modifier.weight(1f),
                        title = "পাওয়ার সিস্টেম",
                        subtitle = "Solar & Grid",
                        icon = Icons.Default.ElectricBolt,
                        color = Color(0xFF0EA5E9),
                        isSelected = selectedCategoryFilter == CalculatorCategory.POWER,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == CalculatorCategory.POWER) null else CalculatorCategory.POWER
                        }
                    )
                    CategoryIconCard(
                        modifier = Modifier.weight(1f),
                        title = "ট্রান্সফরমার",
                        subtitle = "Substation",
                        icon = Icons.Default.Memory,
                        color = Color(0xFF2563EB),
                        isSelected = selectedCategoryFilter == CalculatorCategory.TRANSFORMER,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == CalculatorCategory.TRANSFORMER) null else CalculatorCategory.TRANSFORMER
                        }
                    )
                    CategoryIconCard(
                        modifier = Modifier.weight(1f),
                        title = "জেনারেটর",
                        subtitle = "DG & Backup",
                        icon = Icons.Default.PrecisionManufacturing,
                        color = Color(0xFF0D9488),
                        isSelected = selectedCategoryFilter == CalculatorCategory.GENERATOR,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == CalculatorCategory.GENERATOR) null else CalculatorCategory.GENERATOR
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryIconCard(
                        modifier = Modifier.weight(1f),
                        title = "ব্যাটারি / UPS",
                        subtitle = "Power Backup",
                        icon = Icons.Default.BatteryChargingFull,
                        color = Color(0xFF16A34A),
                        isSelected = selectedCategoryFilter == CalculatorCategory.BATTERY_UPS,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == CalculatorCategory.BATTERY_UPS) null else CalculatorCategory.BATTERY_UPS
                        }
                    )
                    CategoryIconCard(
                        modifier = Modifier.weight(1f),
                        title = "বিল্ডিং ওয়্যারিং",
                        subtitle = "House & DB",
                        icon = Icons.Default.Engineering,
                        color = Color(0xFF7C3AED),
                        isSelected = selectedCategoryFilter == CalculatorCategory.BUILDING,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == CalculatorCategory.BUILDING) null else CalculatorCategory.BUILDING
                        }
                    )
                    CategoryIconCard(
                        modifier = Modifier.weight(1f),
                        title = "ব্রেকার / ফিউজ",
                        subtitle = "Protection",
                        icon = Icons.Default.Cable,
                        color = Color(0xFFEA580C),
                        isSelected = selectedCategoryFilter == CalculatorCategory.PROTECTION,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == CalculatorCategory.PROTECTION) null else CalculatorCategory.PROTECTION
                        }
                    )
                    CategoryIconCard(
                        modifier = Modifier.weight(1f),
                        title = "PFI / ক্যাপাসিটর",
                        subtitle = "Power Factor",
                        icon = Icons.Default.Bolt,
                        color = Color(0xFF0284C7),
                        isSelected = selectedCategoryFilter == CalculatorCategory.PFI,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == CalculatorCategory.PFI) null else CalculatorCategory.PFI
                        }
                    )
                }
            }
        }

        // 7. CALCULATORS LIST HEADER
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedCategoryFilter != null) "${selectedCategoryFilter?.displayName} (${filteredList.size})"
                           else "সকল ইলেকট্রিক্যাল ক্যালকুলেটর (${filteredList.size} টি)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                if (selectedCategoryFilter != null || searchQuery.isNotEmpty()) {
                    Text(
                        text = "ফিল্টার রিসেট ✕",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.clickable {
                            selectedCategoryFilter = null
                            searchQuery = ""
                        }
                    )
                }
            }
        }

        // 8. RICH CALCULATORS LIST ITEMS
        items(
            items = filteredList,
            key = { it.id }
        ) { calc ->
            val isFavorite = favoriteIds.contains(calc.id)
            Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                PolishedCalculatorCard(
                    calc = calc,
                    isFavorite = isFavorite,
                    onToggleFavorite = {
                        coroutineScope.launch {
                            app.settingsManager.toggleFavorite(calc.id)
                        }
                    },
                    onClick = { onNavigateToCalculator(calc.id) }
                )
            }
        }
    }
}

@Composable
private fun CategoryPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) Color(0xFF00E5FF) else Color(0x33FFFFFF),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF00E5FF) else Color(0x33FFFFFF)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color(0xFF0A192F) else Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StatDashboardCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LevelBadgeCard(
    modifier: Modifier = Modifier,
    title: String,
    count: String,
    subtitle: String,
    gradient: List<Color>,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradient))
                .padding(10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xCCFFFFFF),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = count,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = subtitle,
                    fontSize = 9.5.sp,
                    color = Color(0xEEFFFFFF),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun QuickAccessCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(88.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = Color(0xFF64748B),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CategoryIconCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.12f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) color else Color(0xFFE2E8F0)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) color else Color(0xFF1E293B),
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 8.5.sp,
                color = Color(0xFF64748B),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PolishedCalculatorCard(
    calc: CalculatorDefinition,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    val levelColor = when (calc.level) {
        CalculatorLevel.ADVANCE -> Color(0xFF7C3AED)
        CalculatorLevel.BASIC -> Color(0xFF0284C7)
        CalculatorLevel.LOWER -> Color(0xFFD97706)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(levelColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    tint = levelColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = calc.name,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = levelColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = calc.level.displayName,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = levelColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = calc.category.displayName,
                        fontSize = 10.5.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = calc.standard,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Favorite button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                    modifier = Modifier.size(19.dp)
                )
            }

            // Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
