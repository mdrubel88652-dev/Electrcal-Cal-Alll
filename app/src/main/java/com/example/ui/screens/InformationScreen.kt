package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.calculator.CalculatorGuide
import com.example.data.calculator.CalculatorGuideHelper
import com.example.data.calculator.CalculatorRegistry
import com.example.data.model.CalculatorCategory
import com.example.data.model.CalculatorLevel

@Composable
fun InformationScreen(
    onNavigateToCalculator: (Int) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = com.example.ElectricalApp.getApp(context)
    val currentLanguage by app.settingsManager.languageFlow.collectAsState(initial = "EN")
    val isEn = currentLanguage == "EN"

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTier by remember { mutableStateOf("ALL") } // ALL, ADVANCE, BASIC, LOWER, PHASE
    var selectedCategory by remember { mutableStateOf<CalculatorCategory?>(null) }

    // Map to track expanded cards. All cards start closed by default as requested.
    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }

    val allCalculators = remember { CalculatorRegistry.allCalculators }

    // Guides cached mapped by ID
    val allGuides = remember(allCalculators) {
        allCalculators.map { CalculatorGuideHelper.getGuide(it) }
    }

    val filteredGuides by remember(searchQuery, selectedFilterTier, selectedCategory, allGuides) {
        derivedStateOf {
            allGuides.filter { guide ->
                val matchesTier = when (selectedFilterTier) {
                    "ADVANCE" -> guide.level == CalculatorLevel.ADVANCE
                    "BASIC" -> guide.level == CalculatorLevel.BASIC
                    "LOWER" -> guide.level == CalculatorLevel.LOWER
                    "PHASE" -> guide.isPhaseSelectable
                    else -> true
                }

                val matchesCategory = selectedCategory == null || guide.category == selectedCategory

                val query = searchQuery.trim().lowercase()
                val matchesSearch = if (query.isBlank()) {
                    true
                } else {
                    guide.id.toString() == query ||
                    guide.id.toString().contains(query) ||
                    guide.name.lowercase().contains(query) ||
                    guide.category.displayName.lowercase().contains(query) ||
                    guide.description.lowercase().contains(query) ||
                    guide.formula.lowercase().contains(query) ||
                    guide.howToWorkBn.lowercase().contains(query) ||
                    guide.howToCalculateBn.lowercase().contains(query) ||
                    guide.howToWorkEn.lowercase().contains(query) ||
                    guide.howToCalculateEn.lowercase().contains(query)
                }

                matchesTier && matchesCategory && matchesSearch
            }
        }
    }

    val listState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("information_screen_root")
    ) {
        // List of 167 Calculator Guide Cards with Scrollable Banner & Search
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("information_guide_list"),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ==========================================
            // 1. SCROLLABLE CLEAN HEADER BANNER (NO DARK BLUE)
            // ==========================================
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEFF4F9)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE0F2FE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = if (isEn) "Information Guide" else "Information • প্রকৌশল সহায়িকা",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = if (isEn) "Engineering calculation rules & operations for 167 tools" else "১৬৭টি ক্যালকুলেটরের হিসাব ও কাজের নিয়মাবলী",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stats Metrics Row (Light, readable, unified)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MetricBadge(label = if (isEn) "Total Tools" else "মোট ক্যালকুলেটর", value = if (isEn) "167 Tools" else "১৬৭টি", color = Color(0xFF0284C7))
                            MetricBadge(label = if (isEn) "1φ & 3φ" else "১φ ও ৩φ মোড", value = if (isEn) "60 Modes" else "৬০টি", color = Color(0xFFD97706))
                            MetricBadge(label = if (isEn) "Standards" else "মানক স্ট্যান্ডার্ড", value = "IEC / IEEE", color = Color(0xFF059669))
                            MetricBadge(label = if (isEn) "Offline Ready" else "অফলাইন রেডি", value = "100%", color = Color(0xFF7C3AED))
                        }
                    }
                }
            }

            // ==========================================
            // 2. SCROLLABLE SEARCH BAR
            // ==========================================
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(23.dp),
                    color = Color(0xFFEFF4F9)
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
                                    text = if (isEn) "Search calculator name, #id or formula..." else "ক্যালকুলেটরের নাম, নম্বর (#1-167) বা সূত্র খুঁজুন...",
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            androidx.compose.foundation.text.BasicTextField(
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
                                    .testTag("information_search_input")
                            )
                        }

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 3. SCROLLABLE CATEGORY & TIER FILTER PILLS
            // ==========================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TierChip(
                        label = if (isEn) "All (167)" else "সকল (১৬৭)",
                        selected = selectedFilterTier == "ALL" && selectedCategory == null,
                        onClick = {
                            selectedFilterTier = "ALL"
                            selectedCategory = null
                        }
                    )
                    TierChip(
                        label = if (isEn) "Advance (55)" else "Advance (৫৫)",
                        selected = selectedFilterTier == "ADVANCE",
                        onClick = { selectedFilterTier = "ADVANCE" }
                    )
                    TierChip(
                        label = if (isEn) "Basic (61)" else "Basic (৬১)",
                        selected = selectedFilterTier == "BASIC",
                        onClick = { selectedFilterTier = "BASIC" }
                    )
                    TierChip(
                        label = if (isEn) "Lower (51)" else "Lower (৫১)",
                        selected = selectedFilterTier == "LOWER",
                        onClick = { selectedFilterTier = "LOWER" }
                    )
                    TierChip(
                        label = if (isEn) "⚡ 1-Phase & 3-Phase (60)" else "⚡ ১-ফেজ ও ৩-ফেজ (৬০)",
                        selected = selectedFilterTier == "PHASE",
                        onClick = { selectedFilterTier = "PHASE" }
                    )

                    // Category Chips
                    CalculatorCategory.values().forEach { cat ->
                        TierChip(
                            label = cat.displayName,
                            selected = selectedCategory == cat,
                            onClick = {
                                selectedCategory = if (selectedCategory == cat) null else cat
                            }
                        )
                    }
                }
            }

            // ==========================================
            // 4. RESULT COUNT SUMMARY
            // ==========================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "Displaying: ${filteredGuides.size} Guides" else "প্রদর্শিত হচ্ছে: ${filteredGuides.size} টি সহায়িকা",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569)
                    )

                    if (searchQuery.isNotEmpty() || selectedFilterTier != "ALL" || selectedCategory != null) {
                        Text(
                            text = if (isEn) "Reset Filter" else "রিসেট ফিল্টার",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7),
                            modifier = Modifier.clickable {
                                searchQuery = ""
                                selectedFilterTier = "ALL"
                                selectedCategory = null
                            }
                        )
                    }
                }
            }
            items(
                items = filteredGuides,
                key = { it.id }
            ) { guide ->
                val isExpanded = expandedStates[guide.id] ?: false

                CalculatorGuideCard(
                    guide = guide,
                    isExpanded = isExpanded,
                    isEn = isEn,
                    onToggleExpand = {
                        expandedStates[guide.id] = !isExpanded
                    },
                    onOpenCalculator = {
                        onNavigateToCalculator(guide.id)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                // Bottom Engineering Footer
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF4F9),
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ELECTRICAL PRO SUITE • ENGINEERING GUIDE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isEn) "All 167 calculation formulas are verified and compliant with international IEC 60364, IEEE Std & NEC standards." else "সকল ১৬৭টি ক্যালকুলেটরের সূত্রাবলী আন্তর্জাতিক IEC 60364, IEEE Std ও NEC কোড অনুসারে পরীক্ষিত ও প্রমাণিত।",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun CalculatorGuideCard(
    guide: CalculatorGuide,
    isExpanded: Boolean,
    isEn: Boolean = true,
    onToggleExpand: () -> Unit,
    onOpenCalculator: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("guide_card_${guide.id}")
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onToggleExpand)
            .animateContentSize(animationSpec = tween(250)),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFEFF4F9),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header clickable row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    // ID Badge
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${guide.id}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00E5FF)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        // Title
                        Text(
                            text = guide.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Chips Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Tier Badge
                            val tierColor = when (guide.level) {
                                CalculatorLevel.ADVANCE -> Color(0xFF7C3AED)
                                CalculatorLevel.BASIC -> Color(0xFF0284C7)
                                CalculatorLevel.LOWER -> Color(0xFF0D9488)
                            }
                            SmallBadge(text = guide.level.displayName, bg = tierColor.copy(alpha = 0.12f), fg = tierColor)

                            // Category Badge
                            SmallBadge(text = guide.category.displayName, bg = Color(0xFFF1F5F9), fg = Color(0xFF475569))

                            // Phase Capable Badge
                            if (guide.isPhaseSelectable) {
                                SmallBadge(text = "1φ / 3φ", bg = Color(0xFFFEF3C7), fg = Color(0xFFD97706))
                            }
                        }
                    }
                }

                // Expand/Collapse Chevron
                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color(0xFF0284C7)
                    )
                }
            }

            // Description Preview
            Text(
                text = guide.description,
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 10.dp)
            )

            // Expanded Full Details
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(10.dp))

                    // SECTION 1: How it works
                    SectionTitle(
                        icon = Icons.Default.Settings,
                        title = if (isEn) "1. How It Works (Operating Procedure)" else "১. কিভাবে কাজ করতে হবে (ব্যবহার প্রণালী)"
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isEn) guide.howToWorkEn else guide.howToWorkBn,
                                fontSize = 12.sp,
                                color = Color(0xFF1E293B),
                                lineHeight = 18.sp
                            )

                            if (guide.isPhaseSelectable) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFFFFFBEB),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ElectricBolt,
                                            contentDescription = null,
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isEn) guide.phaseGuideEn else guide.phaseGuideBn,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF92400E),
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // SECTION 2: How to calculate
                    SectionTitle(
                        icon = Icons.Default.Functions,
                        title = if (isEn) "2. How to Calculate (Formula & Theory)" else "২. কিভাবে হিসাব করতে হবে (সূত্র ও গাণিতিক নীতি)"
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Math Formula Box
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "AUTHORITATIVE FORMULA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = guide.formula,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFFFC107)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Math Details Text
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isEn) guide.howToCalculateEn else guide.howToCalculateBn,
                                fontSize = 12.sp,
                                color = Color(0xFF1E293B),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Practical Application
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) "Field Application: ${guide.practicalApplicationEn}" else "ব্যবহারক্ষেত্র: ${guide.practicalApplicationBn}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF475569)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Button: Open Calculator
                    Button(
                        onClick = onOpenCalculator,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("open_calculator_btn_${guide.id}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEn) "Open #${guide.id} ${guide.name}" else "#${guide.id} ${guide.name} গণনা করুন",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF0284C7),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
    }
}

@Composable
private fun MetricBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(text = label, fontSize = 9.sp, color = Color(0xFF94A3B8))
    }
}

@Composable
private fun SmallBadge(text: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

@Composable
private fun TierChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF0284C7),
            selectedLabelColor = Color.White,
            containerColor = Color.White,
            labelColor = Color(0xFF334155)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Color(0xFFCBD5E1),
            selectedBorderColor = Color(0xFF0284C7)
        ),
        shape = RoundedCornerShape(16.dp)
    )
}
