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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.calculator.CalculatorRegistry
import com.example.data.model.CalculatorCategory
import com.example.data.model.CalculatorDefinition

data class CategoryUiItem(
    val category: CalculatorCategory,
    val title: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun CalculatorsScreen(
    onNavigateToCalculator: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterPill by remember { mutableStateOf("All") }
    var expandedCategoryId by remember { mutableStateOf<CalculatorCategory?>(null) }

    val categoryList = remember {
        listOf(
            CategoryUiItem(CalculatorCategory.FACTORY, "Main / Factory", Icons.Default.Factory, Color(0xFF0284C7)),
            CategoryUiItem(CalculatorCategory.TRANSFORMER, "Transformer", Icons.Default.Memory, Color(0xFF0EA5E9)),
            CategoryUiItem(CalculatorCategory.GENERATOR, "Generator", Icons.Default.PrecisionManufacturing, Color(0xFF475569)),
            CategoryUiItem(CalculatorCategory.CABLE, "Cable / Wire", Icons.Default.Cable, Color(0xFFEA580C)),
            CategoryUiItem(CalculatorCategory.PROTECTION, "Breaker / Switchgear", Icons.Default.ElectricBolt, Color(0xFF1E3A8A)),
            CategoryUiItem(CalculatorCategory.BUILDING, "Control / Automation", Icons.Default.Engineering, Color(0xFF0D9488)),
            CategoryUiItem(CalculatorCategory.BATTERY_UPS, "Battery / Power Backup", Icons.Default.BatteryChargingFull, Color(0xFF16A34A)),
            CategoryUiItem(CalculatorCategory.PFI, "AC / Cooling & PFI", Icons.Default.AcUnit, Color(0xFF0284C7)),
            CategoryUiItem(CalculatorCategory.MOTOR, "Motor", Icons.Default.Bolt, Color(0xFFD97706)),
            CategoryUiItem(CalculatorCategory.CONVERSION, "Conversion", Icons.Default.SyncAlt, Color(0xFF7C3AED)),
            CategoryUiItem(CalculatorCategory.BASIC_ELEC, "Basic Electrical", Icons.Default.Construction, Color(0xFF2563EB)),
            CategoryUiItem(CalculatorCategory.POWER, "Solar & Renewable", Icons.Default.WbSunny, Color(0xFFF59E0B))
        )
    }

    val filterPills = listOf("All", "Main", "Motor", "Conversion", "Cable", "Transformer", "Solar")

    val allCalculators = CalculatorRegistry.allCalculators

    val searchResults = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else CalculatorRegistry.search(searchQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("calculators_screen_root")
    ) {
        // Search Bar (Exact Match Screen 2)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .height(42.dp)
                .clip(RoundedCornerShape(21.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(21.dp))
                .padding(horizontal = 12.dp),
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
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search calculators...",
                            fontSize = 12.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 13.sp,
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
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        // Filter Pills Row
        if (searchQuery.isBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterPills.forEach { pill ->
                    val isSelected = selectedFilterPill == pill
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedFilterPill = pill },
                        color = if (isSelected) Color(0xFF0284C7) else Color.White,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF0284C7) else Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = pill,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // List Content: Search results or Category Accordion Cards
        if (searchQuery.isNotBlank()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResults, key = { it.id }) { calc ->
                    CalculatorRowItem(calc = calc, onClick = { onNavigateToCalculator(calc.id) })
                }
            }
        } else {
            val displayedCategories = categoryList.filter { item ->
                when (selectedFilterPill) {
                    "All" -> true
                    "Main" -> item.category == CalculatorCategory.FACTORY
                    "Motor" -> item.category == CalculatorCategory.MOTOR
                    "Conversion" -> item.category == CalculatorCategory.CONVERSION
                    "Cable" -> item.category == CalculatorCategory.CABLE
                    "Transformer" -> item.category == CalculatorCategory.TRANSFORMER
                    "Solar" -> item.category == CalculatorCategory.POWER
                    else -> true
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayedCategories, key = { it.title }) { item ->
                    val calcsInCategory = allCalculators.filter { it.category == item.category }
                    val count = calcsInCategory.size
                    val isExpanded = expandedCategoryId == item.category

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedCategoryId = if (isExpanded) null else item.category
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category Icon
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(item.color.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = item.color,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$count calculators",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(if (isExpanded) 20.dp else 14.dp)
                                )
                            }

                            // Expanded Calculator Sub-list
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAFC))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    HorizontalDivider(color = Color(0xFFE2E8F0))
                                    calcsInCategory.forEach { calc ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onNavigateToCalculator(calc.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.White,
                                            border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFCBD5E1))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = calc.name,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                    Text(
                                                        text = "${calc.level.displayName} • ${calc.standard}",
                                                        fontSize = 10.5.sp,
                                                        color = Color(0xFF64748B)
                                                    )
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFFE0F2FE)
                                                ) {
                                                    Text(
                                                        text = "হিসাব করুন",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF0284C7),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
        }
    }
}

@Composable
private fun CalculatorRowItem(
    calc: CalculatorDefinition,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0F2FE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = calc.name,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "${calc.category.displayName} • ${calc.level.displayName}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFF1F5F9)
            ) {
                Text(
                    text = calc.standard,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
