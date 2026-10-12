package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ElectricalApp
import com.example.data.calculator.CalculatorRegistry
import com.example.data.model.CalculatorCategory
import com.example.utils.ShareUtils
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    onNavigateToCalculator: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = remember { ElectricalApp.getApp(context) }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("favorites_screen_root")
    ) {
        // Search bar (Matching Screen 8 in design reference)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
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
                            text = "Search favorites...",
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
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        if (favoriteCalculators.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
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
                        text = if (searchQuery.isNotEmpty()) "কোনো ফলাফল পাওয়া যায়নি" else "কোনো ফেভারিট যুক্ত নেই",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ক্যালকুলেটরের তারা (★) আইকনে চাপ দিয়ে আপনার পছন্দের ইঞ্জিনগুলো এখানে যুক্ত করুন।",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(favoriteCalculators, key = { it.id }) { calc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToCalculator(calc.id) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rounded Category Badge
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(getCategoryColor(calc.category).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    tint = getCategoryColor(calc.category),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = calc.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${calc.category.displayName} • ${calc.level.displayName}",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFE0F2FE)
                                    ) {
                                        Text(
                                            text = calc.standard,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0284C7),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            // Star Action
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        app.settingsManager.toggleFavorite(calc.id)
                                        Toast.makeText(context, "ফেভারিট তালিকা আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Starred",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Share Action
                            IconButton(
                                onClick = {
                                    ShareUtils.shareText(
                                        context,
                                        calc.name,
                                        "⚡ Electrical Pro: ${calc.name}\nক্যাটাগরি: ${calc.category.displayName}\nসূত্র: ${calc.formula}\nস্ট্যান্ডার্ড: ${calc.standard}"
                                    )
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getCategoryColor(cat: CalculatorCategory): Color {
    return when (cat) {
        CalculatorCategory.TRANSFORMER -> Color(0xFF0284C7)
        CalculatorCategory.MOTOR -> Color(0xFF2563EB)
        CalculatorCategory.CABLE -> Color(0xFFEA580C)
        CalculatorCategory.GENERATOR -> Color(0xFF475569)
        CalculatorCategory.POWER -> Color(0xFFD97706)
        CalculatorCategory.PROTECTION -> Color(0xFF0D9488)
        CalculatorCategory.BUILDING -> Color(0xFF7C3AED)
        else -> Color(0xFF0284C7)
    }
}
