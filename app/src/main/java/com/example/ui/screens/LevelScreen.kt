package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.calculator.CalculatorRegistry
import com.example.data.model.CalculatorLevel
import com.example.ui.components.CalculatorCard
import com.example.ui.theme.LevelAdvanceColor
import com.example.ui.theme.LevelBasicColor
import com.example.ui.theme.LevelLowerColor

@Composable
fun LevelScreen(
    level: CalculatorLevel,
    isEn: Boolean = true,
    onNavigateToCalculator: (Int) -> Unit,
    onNavigateToBuildingMaterial: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val calculators = remember(level) {
        CalculatorRegistry.getByLevel(level)
    }

    val filteredList = remember(calculators, searchQuery) {
        if (searchQuery.isBlank()) {
            calculators
        } else {
            val q = searchQuery.trim().lowercase()
            calculators.filter {
                it.name.lowercase().contains(q) ||
                    it.category.displayName.lowercase().contains(q) ||
                    it.description.lowercase().contains(q) ||
                    it.id.toString() == q
            }
        }
    }

    // Modern color palette tailored per level with seamless unified tones
    val (levelAccentColor, levelBgTint, levelIcon, levelSubtitleEn, levelSubtitleBn, featureTags) = when (level) {
        CalculatorLevel.ADVANCE -> LevelMeta(
            accentColor = LevelAdvanceColor,
            bgTint = Color(0xFFF0F4FF),
            icon = Icons.Default.ElectricBolt,
            subtitleEn = "High-voltage substations, fault current analysis, earthing grid design, protection relays & building materials",
            subtitleBn = "হাই-ভোল্টেজ সাবস্টেশন, শর্ট সার্কিট ফল্ট অ্যানালাইসিস, আর্থিং গ্রিড ডিজাইন, প্রোটেকশন রিলে ও ওয়্যারিং ম্যাটেরিয়াল",
            tags = listOf("Substation", "Fault Analysis", "HV Protection", "Grid & Panels", "Building Material")
        )
        CalculatorLevel.BASIC -> LevelMeta(
            accentColor = LevelBasicColor,
            bgTint = Color(0xFFF0FDF4),
            icon = Icons.Default.Tune,
            subtitleEn = "Transformers, 3-phase induction motors, diesel generators, power cables, starters, batteries & PFI capacitors",
            subtitleBn = "ট্রান্সফরমার, থ্রি-ফেজ ইনডাকশন মোটর, ডিজেল জেনারেটর, পাওয়ার ক্যাবল, স্টার্টার, ব্যাটারি ব্যাংক ও পিএফআই",
            tags = listOf("Transformers", "Motors & Starters", "Generators", "Cables", "PFI & UPS")
        )
        CalculatorLevel.LOWER -> LevelMeta(
            accentColor = LevelLowerColor,
            bgTint = Color(0xFFFFFBEB),
            icon = Icons.Default.Calculate,
            subtitleEn = "Engineering unit conversions, fundamental Ohm's law, energy tariffs, residential billing & basic electrical formulas",
            subtitleBn = "ইলেকট্রিক্যাল ইউনিট কনভার্সন, ওহমের সূত্র, বিদ্যুৎ বিল ট্যারিফ ও প্রাথমিক ইলেকট্রিক্যাল সূত্রাবলী",
            tags = listOf("Ohm's Law", "Unit Conversion", "Electricity Bill", "Formulas", "Tariffs")
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("level_screen_${level.routeName}"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Modern Unified Banner (Seamless Background, Smooth Scrolling)
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header Row: Level Icon + Title + Pill Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(levelAccentColor.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = levelIcon,
                                    contentDescription = null,
                                    tint = levelAccentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = if (isEn) "${level.displayName} Level Engineering" else "${level.displayName} লেভেল ইঞ্জিনিয়ারিং",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isEn) "Professional Suite" else "প্রফেশনাল ইঞ্জিনিয়ারিং সুইট",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = levelAccentColor
                                )
                            }
                        }

                        // Count Badge
                        Surface(
                            color = levelAccentColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = if (isEn) "${calculators.size} Calculators" else "${calculators.size} টি ক্যালকুলেটর",
                                color = levelAccentColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Description text
                    Text(
                        text = if (isEn) levelSubtitleEn else levelSubtitleBn,
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Feature Highlight Tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        featureTags.take(3).forEach { tag ->
                            Surface(
                                color = Color.White.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Seamless Search Bar (Matching HomeScreen & CalculatorsScreen)
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEFF4F9),
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(19.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = if (isEn) "Search ${level.displayName} Calculators..." else "${level.displayName} ক্যালকুলেটর খুঁজুন...",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF0F172A)
                            ),
                            cursorBrush = SolidColor(levelAccentColor),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("level_search_input")
                        )
                    }

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(22.dp)
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

        // Section Title & Result Counter
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEn) "${level.displayName} Calculators (${filteredList.size})" else "${level.displayName} ক্যালকুলেটর (${filteredList.size})",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                if (searchQuery.isNotEmpty()) {
                    Text(
                        text = if (isEn) "Reset" else "রিসেট",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = levelAccentColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { searchQuery = "" }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Empty State or List
        if (filteredList.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF4F9),
                    shadowElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isEn) "No calculators match '$searchQuery'" else "'$searchQuery' এর জন্য কোনো ক্যালকুলেটর মেলেনি",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isEn) "Try searching for breaker, cable, fault, etc." else "ব্রেকার, ক্যাবল, ফল্ট ইত্যাদি লিখে খুঁজুন",
                            fontSize = 11.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        } else {
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
}

private data class LevelMeta(
    val accentColor: Color,
    val bgTint: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val subtitleEn: String,
    val subtitleBn: String,
    val tags: List<String>
)
