package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
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

@Composable
fun FavoritesScreen(
    onNavigateToCalculator: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = remember { ElectricalApp.getApp(context) }
    val currentLanguage by app.settingsManager.languageFlow.collectAsState(initial = "EN")
    val isEn = currentLanguage == "EN"
    val favoriteIds by app.settingsManager.favoritesFlow.collectAsState(initial = emptySet())

    var searchQuery by remember { mutableStateOf("") }

    val favoriteCalculators = remember(favoriteIds, searchQuery) {
        val list = favoriteIds.mapNotNull { CalculatorRegistry.getById(it) }
        if (searchQuery.isBlank()) list
        else list.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.category.displayName.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("favorites_screen_root"),
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
                                text = if (isEn) "Search favorite calculators..." else "ফেভারিট ক্যালকুলেটর খুঁজুন...",
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
                                .testTag("search_favorites_input")
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
        // 2. SUMMARY HEADER (SCROLLABLE)
        // ==========================================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEn) "Favorite Calculators" else "পছন্দের ক্যালকুলেটর সমূহ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = if (isEn) "${favoriteCalculators.size} Favorites" else "${favoriteCalculators.size} টি ফেভারিট",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // ==========================================
        // 3. FAVORITES LIST (UNBREAKABLE CARD DESIGN)
        // ==========================================
        if (favoriteCalculators.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, bottom = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                if (isEn) "No results found" else "কোনো ফলাফল পাওয়া যায়নি"
                            } else {
                                if (isEn) "No favorites added yet" else "কোনো ফেভারিট যুক্ত নেই"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isEn) "Tap the star (★) icon on any calculator to add it to your quick access list."
                            else "ক্যালকুলেটরের তারা (★) আইকনে চাপ দিয়ে আপনার পছন্দের ইঞ্জিনগুলো এখানে যুক্ত করুন।",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }
        } else {
            items(
                items = favoriteCalculators,
                key = { it.id }
            ) { calc ->
                Box(modifier = Modifier.padding(horizontal = 14.dp)) {
                    FavoriteCatalogCard(
                        calc = calc,
                        onToggleFavorite = {
                            scope.launch {
                                app.settingsManager.toggleFavorite(calc.id)
                                Toast.makeText(context, "ফেভারিট তালিকা আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onClick = { onNavigateToCalculator(calc.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteCatalogCard(
    calc: CalculatorDefinition,
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

            // Right: Star Action Button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Starred",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
