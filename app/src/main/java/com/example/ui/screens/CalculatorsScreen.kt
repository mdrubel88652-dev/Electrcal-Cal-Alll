package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ElectricalApp
import com.example.data.calculator.CalculatorRegistry
import com.example.data.model.CalculatorCategory
import com.example.data.model.CalculatorDefinition
import com.example.data.model.CalculatorLevel
import kotlinx.coroutines.launch

private data class CategoryFilterItem(
    val category: CalculatorCategory?,
    val title: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun CalculatorsScreen(
    onNavigateToCalculator: (Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val app = remember { ElectricalApp.getApp(context) }
    val currentLanguage by app.settingsManager.languageFlow.collectAsState(initial = "EN")
    val isEn = currentLanguage == "EN"
    val favoriteIds by app.settingsManager.favoritesFlow.collectAsState(initial = emptySet())

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CalculatorCategory?>(null) }
    var selectedLevel by remember { mutableStateOf<CalculatorLevel?>(null) }

    val categoryFilters = remember(isEn) {
        listOf(
            CategoryFilterItem(null, if (isEn) "All (167)" else "সব ক্যালকুলেটর (১৬৭)", Icons.Default.ElectricBolt, Color(0xFF0284C7)),
            CategoryFilterItem(CalculatorCategory.FACTORY, if (isEn) "Factory & Industry" else "ফ্যাক্টরি ও ইন্ডাস্ট্রি", Icons.Default.Factory, Color(0xFF0284C7)),
            CategoryFilterItem(CalculatorCategory.TRANSFORMER, if (isEn) "Transformer" else "ট্রান্সফরমার", Icons.Default.Memory, Color(0xFF0EA5E9)),
            CategoryFilterItem(CalculatorCategory.MOTOR, if (isEn) "Motor & Pump" else "মোটর ও পাম্প", Icons.Default.Bolt, Color(0xFFD97706)),
            CategoryFilterItem(CalculatorCategory.CABLE, if (isEn) "Cable & Wire Sizing" else "ক্যাবল ও তার সাইজিং", Icons.Default.Cable, Color(0xFFEA580C)),
            CategoryFilterItem(CalculatorCategory.PROTECTION, if (isEn) "Breaker & Switchgear" else "ব্রেকার ও সুইচগিয়ার", Icons.Default.Shield, Color(0xFF1E3A8A)),
            CategoryFilterItem(CalculatorCategory.POWER, if (isEn) "Solar & Power" else "সোলার ও পাওয়ার", Icons.Default.WbSunny, Color(0xFFF59E0B)),
            CategoryFilterItem(CalculatorCategory.BATTERY_UPS, if (isEn) "Battery & UPS" else "ব্যাটারি ও UPS", Icons.Default.BatteryChargingFull, Color(0xFF16A34A)),
            CategoryFilterItem(CalculatorCategory.BUILDING, if (isEn) "Building Wiring & Control" else "বিল্ডিং ওয়্যারিং ও কন্ট্রোল", Icons.Default.Engineering, Color(0xFF0D9488)),
            CategoryFilterItem(CalculatorCategory.PFI, if (isEn) "PFI & Capacitor" else "PFI ও ক্যাপাসিটর", Icons.Default.AcUnit, Color(0xFF0284C7)),
            CategoryFilterItem(CalculatorCategory.GENERATOR, if (isEn) "Generator" else "জেনারেটর", Icons.Default.PrecisionManufacturing, Color(0xFF475569)),
            CategoryFilterItem(CalculatorCategory.CONVERSION, if (isEn) "Unit Conversion" else "একক রূপান্তর", Icons.Default.SyncAlt, Color(0xFF7C3AED)),
            CategoryFilterItem(CalculatorCategory.BASIC_ELEC, if (isEn) "Basic Electrical" else "বেসিক ইলেকট্রিক্যাল", Icons.Default.Construction, Color(0xFF2563EB))
        )
    }

    val filteredCalculators = remember(searchQuery, selectedCategory, selectedLevel) {
        var list = if (searchQuery.isNotBlank()) CalculatorRegistry.search(searchQuery)
        else CalculatorRegistry.allCalculators

        if (selectedCategory != null) {
            list = list.filter { it.category == selectedCategory }
        }
        if (selectedLevel != null) {
            list = list.filter { it.level == selectedLevel }
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("calculators_screen_root")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ==========================================
            // 1. TOP SEARCH BAR (SCROLLABLE WITH LIST)
            // ==========================================
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
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
                                    text = if (isEn) "Search any of 167 calculators..." else "১৬৭টি ক্যালকুলেটরের যেকোনোটি সার্চ করুন...",
                                    fontSize = 12.5.sp,
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
                                    .testTag("search_calculators_input")
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
            }

            // ==========================================
            // 2. CATEGORY PILLS (SCROLLABLE WITH LIST)
            // ==========================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryFilters.forEach { filterItem ->
                        val isSelected = selectedCategory == filterItem.category
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFF0284C7) else Color(0xFFEFF4F9),
                            modifier = Modifier.clickable {
                                selectedCategory = if (isSelected && filterItem.category != null) null
                                else filterItem.category
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = filterItem.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else filterItem.color,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = filterItem.title,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF334155)
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 3. LEVEL FILTER CHIPS & LIVE COUNT ROW
            // ==========================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Level Pills
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        LevelChip(
                            title = if (isEn) "All" else "সকল",
                            isSelected = selectedLevel == null,
                            onClick = { selectedLevel = null }
                        )
                        LevelChip(
                            title = if (isEn) "Advance (55)" else "Advance (৫৫)",
                            isSelected = selectedLevel == CalculatorLevel.ADVANCE,
                            onClick = {
                                selectedLevel = if (selectedLevel == CalculatorLevel.ADVANCE) null else CalculatorLevel.ADVANCE
                            }
                        )
                        LevelChip(
                            title = if (isEn) "Basic (61)" else "Basic (৬১)",
                            isSelected = selectedLevel == CalculatorLevel.BASIC,
                            onClick = {
                                selectedLevel = if (selectedLevel == CalculatorLevel.BASIC) null else CalculatorLevel.BASIC
                            }
                        )
                        LevelChip(
                            title = if (isEn) "Lower (51)" else "Lower (৫১)",
                            isSelected = selectedLevel == CalculatorLevel.LOWER,
                            onClick = {
                                selectedLevel = if (selectedLevel == CalculatorLevel.LOWER) null else CalculatorLevel.LOWER
                            }
                        )
                    }

                    // Count Indicator
                    Text(
                        text = if (isEn) "${filteredCalculators.size} Tools" else "${filteredCalculators.size} টি টুল",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                }
            }

            // ==========================================
            // 4. CALCULATORS LIST ITEMS
            // ==========================================
            if (filteredCalculators.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp, bottom = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isEn) "No calculators found" else "কোনো ক্যালকুলেটর পাওয়া যায়নি",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isEn) "Try a different keyword or reset filters" else "সার্চ কীওয়ার্ড পরিবর্তন করুন বা ফিল্টার রিসেট করুন",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            } else {
                items(
                    items = filteredCalculators,
                    key = { it.id }
                ) { calc ->
                    Box(modifier = Modifier.padding(horizontal = 14.dp)) {
                        val isFavorite = favoriteIds.contains(calc.id)
                        CalculatorCatalogCard(
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
    }
}

@Composable
private fun LevelChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF0F172A) else Color(0xFFEFF4F9),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            fontSize = 10.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF475569),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun CalculatorCatalogCard(
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
            // Left: Icon + Text
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
                            CalculatorCategory.MOTOR -> Icons.Default.Bolt
                            CalculatorCategory.CABLE -> Icons.Default.Cable
                            CalculatorCategory.BUILDING -> Icons.Default.Engineering
                            CalculatorCategory.FACTORY -> Icons.Default.Factory
                            CalculatorCategory.POWER -> Icons.Default.WbSunny
                            CalculatorCategory.BATTERY_UPS -> Icons.Default.BatteryChargingFull
                            CalculatorCategory.PROTECTION -> Icons.Default.Shield
                            CalculatorCategory.GENERATOR -> Icons.Default.PrecisionManufacturing
                            CalculatorCategory.PFI -> Icons.Default.AcUnit
                            CalculatorCategory.CONVERSION -> Icons.Default.SyncAlt
                            CalculatorCategory.BASIC_ELEC -> Icons.Default.Construction
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

            // Right: Favorite toggle button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
