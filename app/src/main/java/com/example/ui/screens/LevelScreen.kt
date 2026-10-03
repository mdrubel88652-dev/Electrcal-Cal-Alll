package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.calculator.CalculatorRegistry
import com.example.data.model.CalculatorLevel
import com.example.ui.components.CalculatorCard
import com.example.ui.theme.LevelAdvanceBg
import com.example.ui.theme.LevelAdvanceColor
import com.example.ui.theme.LevelBasicBg
import com.example.ui.theme.LevelBasicColor
import com.example.ui.theme.LevelLowerBg
import com.example.ui.theme.LevelLowerColor

@Composable
fun LevelScreen(
    level: CalculatorLevel,
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

    val (levelColor, levelBg, gradColors) = when (level) {
        CalculatorLevel.ADVANCE -> Triple(
            LevelAdvanceColor,
            LevelAdvanceBg,
            listOf(Color(0xFF1565C0), Color(0xFF0D47A1))
        )
        CalculatorLevel.BASIC -> Triple(
            LevelBasicColor,
            LevelBasicBg,
            listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))
        )
        CalculatorLevel.LOWER -> Triple(
            LevelLowerColor,
            LevelLowerBg,
            listOf(Color(0xFF7B1FA2), Color(0xFF4A148C))
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("level_screen_${level.routeName}"),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Level Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(gradColors))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${level.displayName} Level",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = Color(0x33FFFFFF),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "${calculators.size} Calculators",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (level) {
                                CalculatorLevel.ADVANCE -> "Substation, fault analysis, grounding grids, HV protection & building materials"
                                CalculatorLevel.BASIC -> "Transformers, motors, generators, cables, starters, batteries & PFI"
                                CalculatorLevel.LOWER -> "Unit conversions, Ohm's law, energy tariffs & fundamental electrical equations"
                            },
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search ${level.displayName} Calculators...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("level_search_bar"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${level.displayName} Calculators (${filteredList.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

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
