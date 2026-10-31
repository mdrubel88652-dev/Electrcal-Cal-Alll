package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ripple
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import com.example.R
import com.example.ElectricalApp
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
    onNavigateToNotification: () -> Unit = {},
    onNavigateToAdvance: () -> Unit = {},
    onNavigateToBasic: () -> Unit = {},
    onNavigateToLower: () -> Unit = {},
    onNavigateToPrepaidMeter: () -> Unit = {},
    onNavigateToTools: () -> Unit = {},
    onOpenMenu: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val app = remember { ElectricalApp.getApp(context) }
    val currentLanguage by app.settingsManager.languageFlow.collectAsState(initial = "EN")
    val isEn = currentLanguage == "EN"
    val historyList by app.database.historyDao().getAllHistory().collectAsState(initial = emptyList())
    val favoriteIds by app.settingsManager.favoritesFlow.collectAsState(initial = emptySet())

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<CalculatorCategory?>(null) }
    var selectedLevelFilter by remember { mutableStateOf<CalculatorLevel?>(null) }

    val isSearching = searchQuery.isNotBlank()

    val filteredList = remember(searchQuery, selectedCategoryFilter, selectedLevelFilter) {
        var res = if (searchQuery.isNotBlank()) CalculatorRegistry.search(searchQuery)
        else CalculatorRegistry.allCalculators

        if (selectedCategoryFilter != null) {
            res = res.filter { it.category == selectedCategoryFilter }
        }
        if (selectedLevelFilter != null) {
            res = res.filter { it.level == selectedLevelFilter }
        }
        res
    }

    val displayList = remember(selectedCategoryFilter) {
        if (selectedCategoryFilter != null) {
            CalculatorRegistry.allCalculators.filter { it.category == selectedCategoryFilter }
        } else {
            listOfNotNull(
                CalculatorRegistry.getById(1),  // Factory Load
                CalculatorRegistry.getById(6),  // Motor FLC
                CalculatorRegistry.getById(2),  // Transformer
                CalculatorRegistry.getById(3),  // House Wiring
                CalculatorRegistry.getById(5),  // PFI Size
                CalculatorRegistry.getById(4),  // Solar
                CalculatorRegistry.getById(13), // Generator
                CalculatorRegistry.getById(8),  // Earthing
                CalculatorRegistry.getById(14), // Substation
                CalculatorRegistry.getById(7),  // Voltage Drop
                CalculatorRegistry.getById(9),  // Conduit Sizing
                CalculatorRegistry.getById(12)  // Power Factor Improvement
            )
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF082D63))
    ) {
        val cornerRadius = 24.dp
        // Banner image aspect ratio is 2172 x 724 = 3 : 1
        val bannerHeight = maxWidth * (724f / 2172f)

        // 1. Full-width electrical calculation dashboard header banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bannerHeight)
                .background(Color(0xFF082D63))
        ) {
            Image(
                painter = painterResource(id = R.drawable.electrical_header_bg),
                contentDescription = "Electrical Calculation All Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillWidth
            )

            // Clickable logo area on top-left (preserves existing click to Information & testTag)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 12.dp, top = 6.dp)
                    .size(42.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onNavigateToInformation
                    )
                    .testTag("top_bar_user_picture")
            )

            // Top action buttons: Information, Notification, Hamburger Menu
            // Solid electric blue border, solid electrical navy background, no glass/translucent effect
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 6.dp, end = 12.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF063B82),
                border = BorderStroke(1.5.dp, Color(0xFF00BFFF)),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onNavigateToInformation,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("top_bar_information_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Information",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(18.dp)
                            .background(Color(0xFF00BFFF).copy(alpha = 0.35f))
                    )

                    IconButton(
                        onClick = onNavigateToNotification,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("top_bar_notification_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notification",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(18.dp)
                            .background(Color(0xFF00BFFF).copy(alpha = 0.35f))
                    )

                    IconButton(
                        onClick = onOpenMenu,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("top_bar_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Technician Hub & Wallet Menu",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 2. Main White Dashboard Content Panel with rounded top corners
        // Sits directly over the bottom of the banner by cornerRadius (24.dp)
        // so the rounded corners reveal the exact banner background underneath
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = (bannerHeight - cornerRadius).coerceAtLeast(0.dp))
                .fillMaxHeight(),
            shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("home_screen_column"),
                    contentPadding = PaddingValues(bottom = 36.dp)
                ) {
        // ==========================================
        // 1. FLOATING MODERN SEARCH & METRICS DOCK
        // ==========================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Sleek Search Bar directly on background
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.2.dp, Color(0xFFCBD5E1))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = if (isEn) "Search any of 167 electrical calculators..." else "১৬৭টি ক্যালকুলেটরের যেকোনোটি সার্চ করুন...",
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

                // Quick Metric Bar directly on background
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE0F2FE)
                        ) {
                            Text(
                                text = if (isEn) "167 Tools" else "১৬৭ ক্যালকুলেটর",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.clickable { onNavigateToFavorites() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isEn) "${favoriteIds.size} Favorites" else "${favoriteIds.size} প্রিয়",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF4F9),
                            modifier = Modifier.clickable { onNavigateToHistory() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isEn) "${historyList.size} History" else "${historyList.size} হিস্টোরি",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isEn) "View All ➔" else "সব দেখুন ➔",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.clickable { onNavigateToCalculatorsList() }
                    )
                }
            }
        }

        // ==========================================
        // 2. LIVE SEARCH RESULTS (IF SEARCHING)
        // ==========================================
        if (isSearching) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "Search Results (${filteredList.size} Tools)" else "সার্চ রেজাল্ট (${filteredList.size} টি ক্যালকুলেটর)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = if (isEn) "Reset ✕" else "রিসেট করুন ✕",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.clickable { searchQuery = "" }
                    )
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isEn) "No calculators found" else "কোনো ক্যালকুলেটর পাওয়া যায়নি",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isEn) "Try a different keyword or calculator ID" else "অন্য কীওয়ার্ড বা বাংলায় নাম লিখে চেষ্টা করুন",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            } else {
                items(
                    items = filteredList,
                    key = { it.id }
                ) { calc ->
                    val isFavorite = favoriteIds.contains(calc.id)
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        ModernCalculatorRowCard(
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
        } else {
            // ==========================================
            // 3. MAIN DASHBOARD: 6 APP DOCK PORTALS
            // ==========================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = if (isEn) "CALCULATOR TIERS & SPECIAL TOOLS" else "ক্যালকুলেটর লেভেল ও বিশেষ ফিচার",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFEAB308))
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 1: Advance, Basic, Lower, Prepaid Meter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModernDockButton(
                            modifier = Modifier.weight(1f),
                            title = if (isEn) "Advance" else "অ্যাডভান্স",
                            count = if (isEn) "55 Tools" else "৫৫ টি",
                            badge = if (isEn) "Industrial" else "ইন্ডাস্ট্রিয়াল",
                            icon = Icons.Default.ElectricBolt,
                            iconBg = Color(0xFF4338CA),
                            cardBg = Color(0xFFEEF2FF),
                            border = Color(0xFFC7D2FE),
                            onClick = onNavigateToAdvance
                        )

                        ModernDockButton(
                            modifier = Modifier.weight(1f),
                            title = if (isEn) "Basic" else "বেসিক",
                            count = if (isEn) "61 Tools" else "৬১ টি",
                            badge = if (isEn) "Circuit & Load" else "সার্কিট ও লোড",
                            icon = Icons.Default.Speed,
                            iconBg = Color(0xFF0284C7),
                            cardBg = Color(0xFFF0F9FF),
                            border = Color(0xFFBAE6FD),
                            onClick = onNavigateToBasic
                        )

                        ModernDockButton(
                            modifier = Modifier.weight(1f),
                            title = if (isEn) "Lower" else "লোয়ার",
                            count = if (isEn) "51 Tools" else "৫১ টি",
                            badge = if (isEn) "Units & Laws" else "একক ও সূত্র",
                            icon = Icons.Default.Cable,
                            iconBg = Color(0xFFD97706),
                            cardBg = Color(0xFFFFFBEB),
                            border = Color(0xFFFDE68A),
                            onClick = onNavigateToLower
                        )

                        ModernDockButton(
                            modifier = Modifier.weight(1f),
                            title = if (isEn) "Prepaid Meter" else "প্রিপেইড মিটার",
                            count = if (isEn) "All Codes" else "মিটার কোড",
                            badge = if (isEn) "BD Meters" else "বিডি মিটার",
                            icon = Icons.Default.Bolt,
                            iconBg = Color(0xFF0D9488),
                            cardBg = Color(0xFFF0FDFA),
                            border = Color(0xFF99F6E4),
                            onClick = onNavigateToPrepaidMeter
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Building BOQ, Project Hub, History, Tools (Empty as requested)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModernDockButton(
                            modifier = Modifier.weight(1f),
                            title = if (isEn) "House Wiring Cable Sizing" else "হাউস ওয়্যারিং ক্যাবল সাইজিং",
                            count = if (isEn) "Cable & Coil" else "ক্যাবল ও কয়েল হিসাব",
                            badge = if (isEn) "Cable Sizing" else "ক্যাবল সাইজিং",
                            icon = Icons.Default.Cable,
                            iconBg = Color(0xFF059669),
                            cardBg = Color(0xFFECFDF5),
                            border = Color(0xFFA7F3D0),
                            onClick = onNavigateToBuildingMaterial
                        )

                        ModernDockButton(
                            modifier = Modifier.weight(1f),
                            title = if (isEn) "Project Hub" else "প্রজেক্ট খতিয়ান",
                            count = if (isEn) "5 Sites" else "৫ টি সাইট",
                            badge = if (isEn) "PDF Report" else "রিপোর্ট PDF",
                            icon = Icons.Default.Assignment,
                            iconBg = Color(0xFF0F172A),
                            cardBg = Color(0xFFF1F5F9),
                            border = Color(0xFFCBD5E1),
                            onClick = onNavigateToProject
                        )

                        ModernDockButton(
                            modifier = Modifier.weight(1f),
                            title = if (isEn) "History" else "হিস্টোরি রেকর্ড",
                            count = if (isEn) "${historyList.size} Saved" else "${historyList.size} টি সেভড",
                            badge = if (isEn) "Logs" else "লগ ও রিপোর্ট",
                            icon = Icons.Default.History,
                            iconBg = Color(0xFF475569),
                            cardBg = Color(0xFFF1F5F9),
                            border = Color(0xFFCBD5E1),
                            onClick = onNavigateToHistory
                        )

                        // 4th slot: 50 Electrical Tools Guide
                        ModernDockButton(
                            modifier = Modifier.weight(1f),
                            title = if (isEn) "Tools" else "টুলস",
                            count = if (isEn) "50 Tools" else "৫০ টি টুলস",
                            badge = if (isEn) "Guide" else "সহায়িকা",
                            icon = Icons.Default.Engineering,
                            iconBg = Color(0xFF0284C7),
                            cardBg = Color(0xFFF0F9FF),
                            border = Color(0xFFBAE6FD),
                            onClick = onNavigateToTools
                        )
                    }
                }
            }

            // ==========================================
            // 3.5. ANIMATED AUTO-SLIDING BANNER CAROUSEL (600x180)
            // ==========================================
            item {
                HomeBannerCarousel(
                    banners = listOf(
                        HomeBannerItem(
                            imageRes = R.drawable.banner_all_calculators,
                            contentDescription = if (isEn) "All 167+ Calculators" else "সকল ১৬৭+ ক্যালকুলেটর",
                            onClick = onNavigateToCalculatorsList
                        ),
                        HomeBannerItem(
                            imageRes = R.drawable.banner_advance,
                            contentDescription = if (isEn) "Advance Calculators" else "অ্যাডভান্স ক্যালকুলেটর",
                            onClick = onNavigateToAdvance
                        ),
                        HomeBannerItem(
                            imageRes = R.drawable.banner_basic,
                            contentDescription = if (isEn) "Basic Calculators" else "বেসিক ক্যালকুলেটর",
                            onClick = onNavigateToBasic
                        ),
                        HomeBannerItem(
                            imageRes = R.drawable.banner_lower,
                            contentDescription = if (isEn) "Lower Calculators" else "লোয়ার ক্যালকুলেটর",
                            onClick = onNavigateToLower
                        ),
                        HomeBannerItem(
                            imageRes = R.drawable.banner_project,
                            contentDescription = if (isEn) "Project Management System" else "প্রজেক্ট ম্যানেজমেন্ট",
                            onClick = onNavigateToProject
                        ),
                        HomeBannerItem(
                            imageRes = R.drawable.banner_settings,
                            contentDescription = if (isEn) "Settings & Customization" else "সেটিংস",
                            onClick = onNavigateToSettings
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            // ==========================================
            // 4. TOP ESSENTIAL TOOLS (HORIZONTAL HERO CARDS)
            // ==========================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isEn) "🔥 Most Used Calculators" else "🔥 সর্বাধিক ব্যবহৃত ক্যালকুলেটর",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Text(
                            text = if (isEn) "All Calculators ➔" else "সব ক্যালকুলেটর ➔",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7),
                            modifier = Modifier.clickable { onNavigateToCalculatorsList() }
                        )
                    }

                    val featuredList = listOfNotNull(
                        CalculatorRegistry.getById(1), // Factory Load
                        CalculatorRegistry.getById(6), // Motor FLC
                        CalculatorRegistry.getById(2), // Transformer Sizing
                        CalculatorRegistry.getById(3), // House Wiring Material
                        CalculatorRegistry.getById(5), // PFI Sizing
                        CalculatorRegistry.getById(4)  // Solar PV
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(featuredList, key = { it.id }) { calc ->
                            FeaturedToolCard(
                                calc = calc,
                                isFavorite = favoriteIds.contains(calc.id),
                                isEn = isEn,
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

            // ==========================================
            // 5. SUBJECT FILTER CHIPS ROW
            // ==========================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) "CATEGORIES & SPECIALIZED TOOLS" else "বিষয়ভিত্তিক ক্যালকুলেটর সমূহ",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        if (selectedCategoryFilter != null) {
                            Text(
                                text = if (isEn) "Reset Filter ✕" else "ফিল্টার রিসেট ✕",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7),
                                modifier = Modifier.clickable { selectedCategoryFilter = null }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryPill(
                            title = if (isEn) "All Tools" else "সব ক্যালকুলেটর",
                            icon = Icons.Default.Bolt,
                            isSelected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null }
                        )
                        CategoryPill(
                            title = if (isEn) "Transformer" else "ট্রান্সফরমার",
                            icon = Icons.Default.Memory,
                            isSelected = selectedCategoryFilter == CalculatorCategory.TRANSFORMER,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == CalculatorCategory.TRANSFORMER) null else CalculatorCategory.TRANSFORMER
                            }
                        )
                        CategoryPill(
                            title = if (isEn) "Motor & Pump" else "মোটর ও পাম্প",
                            icon = Icons.Default.Speed,
                            isSelected = selectedCategoryFilter == CalculatorCategory.MOTOR,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == CalculatorCategory.MOTOR) null else CalculatorCategory.MOTOR
                            }
                        )
                        CategoryPill(
                            title = if (isEn) "Cable & Wiring" else "ক্যাবল ও ওয়্যারিং",
                            icon = Icons.Default.Cable,
                            isSelected = selectedCategoryFilter == CalculatorCategory.CABLE,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == CalculatorCategory.CABLE) null else CalculatorCategory.CABLE
                            }
                        )
                        CategoryPill(
                            title = if (isEn) "Factory & Power" else "ফ্যাক্টরি ও পাওয়ার",
                            icon = Icons.Default.Factory,
                            isSelected = selectedCategoryFilter == CalculatorCategory.FACTORY,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == CalculatorCategory.FACTORY) null else CalculatorCategory.FACTORY
                            }
                        )
                        CategoryPill(
                            title = if (isEn) "Solar & Grid" else "সোলার ও গ্রিড",
                            icon = Icons.Default.SolarPower,
                            isSelected = selectedCategoryFilter == CalculatorCategory.POWER,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == CalculatorCategory.POWER) null else CalculatorCategory.POWER
                            }
                        )
                        CategoryPill(
                            title = if (isEn) "Battery & UPS" else "ব্যাটারি ও UPS",
                            icon = Icons.Default.BatteryChargingFull,
                            isSelected = selectedCategoryFilter == CalculatorCategory.BATTERY_UPS,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == CalculatorCategory.BATTERY_UPS) null else CalculatorCategory.BATTERY_UPS
                            }
                        )
                        CategoryPill(
                            title = if (isEn) "Breaker & Switchgear" else "ব্রেকার ও সুইচগিয়ার",
                            icon = Icons.Default.Shield,
                            isSelected = selectedCategoryFilter == CalculatorCategory.PROTECTION,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == CalculatorCategory.PROTECTION) null else CalculatorCategory.PROTECTION
                            }
                        )
                    }
                }
            }

            // ==========================================
            // 6. STREAMLINED CALCULATORS LIST
            // ==========================================
            items(
                items = displayList,
                key = { it.id }
            ) { calc ->
                val isFavorite = favoriteIds.contains(calc.id)
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    ModernCalculatorRowCard(
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

            // ==========================================
            // 7. CONTRACT PROJECT PRO BANNER (BOTTOM)
            // ==========================================
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clickable { onNavigateToProject() }
                        .testTag("home_contract_project_card"),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFEFF4F9)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isEn) "Contract Project & Billing" else "কন্ট্রাক্ট প্রজেক্ট ও হিসাব",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFF0284C7),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "PRO",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (isEn) "Work orders, site estimation & professional PDF reports" else "ওয়ার্ক অর্ডার, সাইট হিসাব ও প্রফেশনাল রিপোর্ট PDF",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF1F5F9)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(16.dp)
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

// ========================================================
// REUSABLE MODERN COMPONENTS FOR HOMESCREEN
// ========================================================

@Composable
private fun ModernDockButton(
    modifier: Modifier = Modifier,
    title: String,
    count: String,
    badge: String,
    icon: ImageVector,
    iconBg: Color,
    cardBg: Color,
    border: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFEFF4F9)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = count,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FeaturedToolCard(
    calc: CalculatorDefinition,
    isFavorite: Boolean,
    isEn: Boolean = true,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFEFF4F9)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Category & Favorite Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (calc.level) {
                        CalculatorLevel.ADVANCE -> Color(0xFFEEF2FF)
                        CalculatorLevel.BASIC -> Color(0xFFF0F9FF)
                        CalculatorLevel.LOWER -> Color(0xFFFFFBEB)
                    }
                ) {
                    Text(
                        text = calc.level.displayName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (calc.level) {
                            CalculatorLevel.ADVANCE -> Color(0xFF4338CA)
                            CalculatorLevel.BASIC -> Color(0xFF0284C7)
                            CalculatorLevel.LOWER -> Color(0xFFD97706)
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = calc.name,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = calc.standard,
                fontSize = 10.5.sp,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0284C7)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "Calculate" else "গণনা করুন",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryPill(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF0284C7) else Color(0xFFEFF4F9),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color(0xFF64748B),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFF334155)
            )
        }
    }
}

@Composable
private fun ModernCalculatorRowCard(
    calc: CalculatorDefinition,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFEFF4F9)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Icon + Titles
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (calc.level) {
                                CalculatorLevel.ADVANCE -> Color(0xFFEEF2FF)
                                CalculatorLevel.BASIC -> Color(0xFFF0F9FF)
                                CalculatorLevel.LOWER -> Color(0xFFFFFBEB)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (calc.category) {
                            CalculatorCategory.TRANSFORMER -> Icons.Default.Memory
                            CalculatorCategory.MOTOR -> Icons.Default.Speed
                            CalculatorCategory.CABLE -> Icons.Default.Cable
                            CalculatorCategory.BUILDING -> Icons.Default.Home
                            CalculatorCategory.FACTORY -> Icons.Default.Factory
                            CalculatorCategory.POWER -> Icons.Default.SolarPower
                            CalculatorCategory.BATTERY_UPS -> Icons.Default.BatteryChargingFull
                            CalculatorCategory.PROTECTION -> Icons.Default.Shield
                            else -> Icons.Default.ElectricBolt
                        },
                        contentDescription = null,
                        tint = when (calc.level) {
                            CalculatorLevel.ADVANCE -> Color(0xFF4338CA)
                            CalculatorLevel.BASIC -> Color(0xFF0284C7)
                            CalculatorLevel.LOWER -> Color(0xFFD97706)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = calc.name,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (calc.level) {
                                CalculatorLevel.ADVANCE -> Color(0xFFEEF2FF)
                                CalculatorLevel.BASIC -> Color(0xFFF0F9FF)
                                CalculatorLevel.LOWER -> Color(0xFFFFFBEB)
                            }
                        ) {
                            Text(
                                text = calc.level.displayName,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (calc.level) {
                                    CalculatorLevel.ADVANCE -> Color(0xFF4338CA)
                                    CalculatorLevel.BASIC -> Color(0xFF0284C7)
                                    CalculatorLevel.LOWER -> Color(0xFFD97706)
                                },
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${calc.category.displayName} • ${calc.standard}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right: Favorite Icon
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

data class HomeBannerItem(
    val imageRes: Int,
    val contentDescription: String,
    val onClick: () -> Unit
)

@Composable
fun HomeBannerCarousel(
    banners: List<HomeBannerItem>,
    modifier: Modifier = Modifier
) {
    if (banners.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Auto-scroll banner smoothly
    LaunchedEffect(pagerState) {
        while (true) {
            delay(5000L)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(
                page = nextPage,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF082D63)),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(600f / 130f)
                .background(Color(0xFF082D63), RoundedCornerShape(12.dp))
                .testTag("dashboard_banner_carousel")
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF082D63))
            ) { page ->
                val banner = banners[page]
                Image(
                    painter = painterResource(id = banner.imageRes),
                    contentDescription = banner.contentDescription,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF082D63))
                        .clickable(onClick = banner.onClick)
                )
            }
        }

        // Animated Page Indicator Dots
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            banners.indices.forEach { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(4.dp)
                        .width(if (isSelected) 16.dp else 5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (isSelected) Color(0xFF0284C7)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                )
            }
        }
    }
}

