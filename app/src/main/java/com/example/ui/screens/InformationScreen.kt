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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTier by remember { mutableStateOf("ALL") } // ALL, ADVANCE, BASIC, LOWER, PHASE
    var selectedCategory by remember { mutableStateOf<CalculatorCategory?>(null) }

    // Map to track expanded cards. By default, first 3 cards are expanded.
    val expandedStates = remember {
        mutableStateMapOf<Int, Boolean>().apply {
            put(1, true)
            put(3, true)
            put(5, true)
        }
    }
    var expandAllMode by remember { mutableStateOf(false) }

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
                    guide.howToCalculateBn.lowercase().contains(query)
                }

                matchesTier && matchesCategory && matchesSearch
            }
        }
    }

    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("information_screen_root")
    ) {
        // High-Tech Header Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0F172A),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF0284C7), Color(0xFF00E5FF))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Information • প্রকৌশল সহায়িকা",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "১৬৭টি ক্যালকুলেটরের হিসাব ও কাজের নিয়মাবলী",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Expand All / Collapse All Toggle Button
                    Surface(
                        onClick = {
                            val newMode = !expandAllMode
                            expandAllMode = newMode
                            allGuides.forEach { expandedStates[it.id] = newMode }
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (expandAllMode) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (expandAllMode) "সব বন্ধ" else "সব খুলুন",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats Metrics Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricBadge(label = "মোট ক্যালকুলেটর", value = "১৬৭টি", color = Color(0xFF38BDF8))
                    MetricBadge(label = "১φ ও ৩φ মোড", value = "৬০টি", color = Color(0xFFF59E0B))
                    MetricBadge(label = "মানক স্ট্যান্ডার্ড", value = "IEC / IEEE", color = Color(0xFF10B981))
                    MetricBadge(label = "অফলাইন রেডি", value = "১০০%", color = Color(0xFFA855F7))
                }
            }
        }

        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("information_search_input"),
                placeholder = {
                    Text(
                        "ক্যালকুলেটরের নাম, নম্বর (#1-167) বা সূত্র খুঁজুন...",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0284C7),
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TierChip(
                label = "সকল (১৬৭)",
                selected = selectedFilterTier == "ALL" && selectedCategory == null,
                onClick = {
                    selectedFilterTier = "ALL"
                    selectedCategory = null
                }
            )
            TierChip(
                label = "Advance (৫৫)",
                selected = selectedFilterTier == "ADVANCE",
                onClick = { selectedFilterTier = "ADVANCE" }
            )
            TierChip(
                label = "Basic (৬১)",
                selected = selectedFilterTier == "BASIC",
                onClick = { selectedFilterTier = "BASIC" }
            )
            TierChip(
                label = "Lower (৫১)",
                selected = selectedFilterTier == "LOWER",
                onClick = { selectedFilterTier = "LOWER" }
            )
            TierChip(
                label = "⚡ ১-ফেজ ও ৩-ফেজ (৬০)",
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

        Spacer(modifier = Modifier.height(6.dp))

        // Result Count Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "প্রদর্শিত হচ্ছে: ${filteredGuides.size} টি ক্যালকুলেটর সহায়িকা",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF475569)
            )

            if (searchQuery.isNotEmpty() || selectedFilterTier != "ALL" || selectedCategory != null) {
                Text(
                    text = "রিসেট ফিল্টার",
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

        // List of 167 Calculator Guide Cards
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("information_guide_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = filteredGuides,
                key = { it.id }
            ) { guide ->
                val isExpanded = expandedStates[guide.id] ?: expandAllMode

                CalculatorGuideCard(
                    guide = guide,
                    isExpanded = isExpanded,
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
                    color = Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
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
                            text = "সকল ১৬৭টি ক্যালকুলেটরের সূত্রাবলী আন্তর্জাতিক IEC 60364, IEEE Std ও NEC কোড অনুসারে পরীক্ষিত ও প্রমাণিত।",
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
    onToggleExpand: () -> Unit,
    onOpenCalculator: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("guide_card_${guide.id}")
            .animateContentSize(animationSpec = tween(250)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isExpanded) Color(0xFF38BDF8) else Color(0xFFE2E8F0)
        )
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

                    // SECTION 1: কিভাবে কাজ করতে হবে
                    SectionTitle(icon = Icons.Default.Settings, title = "১. কিভাবে কাজ করতে হবে (ব্যবহার প্রণালী)")

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = guide.howToWorkBn,
                                fontSize = 12.sp,
                                color = Color(0xFF1E293B),
                                lineHeight = 18.sp
                            )

                            if (guide.isPhaseSelectable) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFFFFFBEB),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
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
                                            text = guide.phaseGuideBn,
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

                    // SECTION 2: কিভাবে হিসাব করতে হবে (সূত্র ও মেথড)
                    SectionTitle(icon = Icons.Default.Functions, title = "২. কিভাবে হিসাব করতে হবে (সূত্র ও গাণিতিক নীতি)")

                    Spacer(modifier = Modifier.height(6.dp))

                    // Math Formula Box
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
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
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = guide.howToCalculateBn,
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
                            text = "ব্যবহারক্ষেত্র: ${guide.practicalApplicationBn}",
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
                            text = "#${guide.id} ${guide.name} গণনা করুন",
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
