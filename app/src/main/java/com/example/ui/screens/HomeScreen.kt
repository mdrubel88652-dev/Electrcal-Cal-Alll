package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.calculator.CalculatorRegistry
import com.example.data.model.CalculatorCategory
import com.example.ui.components.CalculatorCard
import com.example.ui.components.CategoryGrid
import com.example.ui.theme.ElectricBluePrimary

@Composable
fun HomeScreen(
    isSearchActive: Boolean = false,
    onCloseSearch: () -> Unit = {},
    onNavigateToCalculator: (Int) -> Unit,
    onNavigateToBuildingMaterial: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CalculatorCategory?>(null) }

    val filteredList = remember(searchQuery, selectedCategory) {
        var res = if (searchQuery.isNotBlank()) {
            CalculatorRegistry.search(searchQuery)
        } else {
            CalculatorRegistry.allCalculators
        }
        if (selectedCategory != null) {
            res = res.filter { it.category == selectedCategory }
        }
        res
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("home_screen_column"),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        // 1. Executive Search Bar (Modern Pill Design with Crisp Border)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 4.dp)
                    .height(42.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .background(Color.White)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFCBD5E1),
                        shape = RoundedCornerShape(21.dp)
                    )
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
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search 167 Calculators (Solar, Motor, Cable, Substation)...",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 12.5.sp,
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_search_bar")
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        // Search feedback banner when query is typed
        if (searchQuery.isNotBlank()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Results: ${filteredList.size} found for \"$searchQuery\"",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    TextButton(onClick = { searchQuery = "" }) {
                        Text("Clear", fontSize = 11.5.sp, color = ElectricBluePrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Executive Hero Banner (Shown when no search query is active)
        if (searchQuery.isBlank() && selectedCategory == null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF0A1123),
                                        Color(0xFF0F2042),
                                        Color(0xFF1E3A8A)
                                    )
                                )
                            )
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(Color(0xFF00E5FF), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PROFESSIONAL SUITE",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF00E5FF),
                                        letterSpacing = 1.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "ELECTRICAL CALCULATION ALL",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "Electrical Engineering Calculation & Estimation",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        color = Color(0xFF1E293B),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "167 Engines",
                                            color = Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                    Surface(
                                        color = Color(0xFF065F46),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "1φ & 3φ Isolated",
                                            color = Color(0xFF6EE7B7),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // New Minimalist Emblem Box with Cyan Border
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .shadow(8.dp, RoundedCornerShape(18.dp), ambientColor = Color(0xFF00E5FF), spotColor = Color(0xFF00E5FF))
                                    .clip(RoundedCornerShape(18.dp))
                                    .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(18.dp))
                                    .background(Color(0xFF0A1123)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_app_logo),
                                    contentDescription = "Official App Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            // 3. Quick Access Highlights (4 Iconic Workflows)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Featured Workflows",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        ),
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickLaunchCard(
                            modifier = Modifier.weight(1f),
                            title = "Solar PV All-In",
                            subtitle = "PV & Battery Design",
                            icon = Icons.Default.WbSunny,
                            badgeColor = Color(0xFFF59E0B),
                            onClick = { onNavigateToCalculator(4) }
                        )
                        QuickLaunchCard(
                            modifier = Modifier.weight(1f),
                            title = "Factory Load",
                            subtitle = "Demand & Substation",
                            icon = Icons.Default.Factory,
                            badgeColor = Color(0xFF2563EB),
                            onClick = { onNavigateToCalculator(1) }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickLaunchCard(
                            modifier = Modifier.weight(1f),
                            title = "House Wiring",
                            subtitle = "Connected & Service",
                            icon = Icons.Default.Home,
                            badgeColor = Color(0xFF10B981),
                            onClick = { onNavigateToCalculator(3) }
                        )
                        QuickLaunchCard(
                            modifier = Modifier.weight(1f),
                            title = "Building Mat.",
                            subtitle = "Takeoff (#167)",
                            icon = Icons.Default.Construction,
                            badgeColor = Color(0xFF8B5CF6),
                            onClick = { onNavigateToBuildingMaterial() }
                        )
                    }
                }
            }
        }

        // 4. Category Row
        if (searchQuery.isBlank()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Engineering Categories",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        ),
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Swipe to filter",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            item {
                CategoryGrid(
                    selectedCategory = selectedCategory,
                    onSelectCategory = { cat ->
                        selectedCategory = cat
                    }
                )
            }
        }

        // 5. Section Title: "Calculators" with count and filter state
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (selectedCategory != null) selectedCategory!!.displayName else "All Calculators",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${filteredList.size}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (selectedCategory != null || searchQuery.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            selectedCategory = null
                            searchQuery = ""
                        }
                    ) {
                        Text("Reset Filter", fontSize = 11.5.sp, color = ElectricBluePrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Empty state when search yields no results
        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No calculator matching \"$searchQuery\"",
                            fontSize = 13.5.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                searchQuery = ""
                                selectedCategory = null
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("View All 167 Calculators", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Calculators List
        items(filteredList, key = { it.id }) { calc ->
            CalculatorCard(
                calculator = calc,
                onClick = {
                    if (calc.id == 167) {
                        onNavigateToBuildingMaterial()
                    } else {
                        onNavigateToCalculator(calc.id)
                    }
                }
            )
        }
    }
}

@Composable
private fun QuickLaunchCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    fontSize = 9.5.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1
                )
            }
        }
    }
}
