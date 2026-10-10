package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalculatorCategory

data class CategoryVisualItem(
    val category: CalculatorCategory?,
    val title: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val solidColor: Color
)

val visualCategoryList = listOf(
    // 0. All Categories Option
    CategoryVisualItem(
        category = null,
        title = "All Calculators",
        icon = Icons.Default.Apps,
        gradientColors = listOf(Color(0xFF334155), Color(0xFF0F172A)),
        solidColor = Color(0xFF0F172A)
    ),
    // 1. Factory & Industrial (Orange)
    CategoryVisualItem(
        category = CalculatorCategory.FACTORY,
        title = "Factory & Industrial",
        icon = Icons.Default.Build,
        gradientColors = listOf(Color(0xFFFF9800), Color(0xFFE65100)),
        solidColor = Color(0xFFE65100)
    ),
    // 2. Transformer (Purple)
    CategoryVisualItem(
        category = CalculatorCategory.TRANSFORMER,
        title = "Transformer Power",
        icon = Icons.Default.Bolt,
        gradientColors = listOf(Color(0xFF8E24AA), Color(0xFF6A1B9A)),
        solidColor = Color(0xFF6A1B9A)
    ),
    // 3. Generator (Green)
    CategoryVisualItem(
        category = CalculatorCategory.GENERATOR,
        title = "Generator Sets",
        icon = Icons.Default.Power,
        gradientColors = listOf(Color(0xFF43A047), Color(0xFF2E7D32)),
        solidColor = Color(0xFF2E7D32)
    ),
    // 4. Motor (Blue)
    CategoryVisualItem(
        category = CalculatorCategory.MOTOR,
        title = "Electric Motor",
        icon = Icons.Default.RotateRight,
        gradientColors = listOf(Color(0xFF1E88E5), Color(0xFF1565C0)),
        solidColor = Color(0xFF1565C0)
    ),
    // 5. Cable & Wire (Red)
    CategoryVisualItem(
        category = CalculatorCategory.CABLE,
        title = "Cable & Wire Sizing",
        icon = Icons.Default.Cable,
        gradientColors = listOf(Color(0xFFE53935), Color(0xFFC62828)),
        solidColor = Color(0xFFC62828)
    ),
    // 6. Building Electrical (Teal)
    CategoryVisualItem(
        category = CalculatorCategory.BUILDING,
        title = "Building Electrical",
        icon = Icons.Default.Apartment,
        gradientColors = listOf(Color(0xFF00897B), Color(0xFF00695C)),
        solidColor = Color(0xFF00695C)
    ),
    // 7. Power System (Navy/Indigo)
    CategoryVisualItem(
        category = CalculatorCategory.POWER,
        title = "Power System",
        icon = Icons.Default.FlashOn,
        gradientColors = listOf(Color(0xFF3949AB), Color(0xFF1A237E)),
        solidColor = Color(0xFF1A237E)
    ),
    // 8. Battery & UPS (Pink/Magenta)
    CategoryVisualItem(
        category = CalculatorCategory.BATTERY_UPS,
        title = "Battery & UPS",
        icon = Icons.Default.BatteryChargingFull,
        gradientColors = listOf(Color(0xFFD81B60), Color(0xFFAD1457)),
        solidColor = Color(0xFFAD1457)
    ),
    // 9. PFI (Slate/Charcoal)
    CategoryVisualItem(
        category = CalculatorCategory.PFI,
        title = "PFI & Capacitor",
        icon = Icons.Default.Speed,
        gradientColors = listOf(Color(0xFF546E7A), Color(0xFF37474F)),
        solidColor = Color(0xFF37474F)
    )
)

@Composable
fun CategoryGrid(
    selectedCategory: CalculatorCategory?,
    onSelectCategory: (CalculatorCategory?) -> Unit
) {
    // Slim & Wide Horizontal Category Cards (উপরে নিচে চিকন এবং পাশাপাশি লম্বা)
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("category_grid_container"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(visualCategoryList) { item ->
            val isSelected = (item.category == null && selectedCategory == null) || (item.category != null && selectedCategory == item.category)

            Card(
                modifier = Modifier
                    .width(156.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        onSelectCategory(item.category)
                    }
                    .then(
                        if (isSelected) {
                            Modifier.border(2.dp, Color.White, RoundedCornerShape(4.dp))
                        } else Modifier
                    )
                    .testTag(if (item.category != null) "category_card_${item.category.name}" else "category_card_ALL"),
                shape = RoundedCornerShape(4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(Brush.horizontalGradient(item.gradientColors))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Slim round icon circle with translucent white backdrop
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0x35FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Category Title
                        Text(
                            text = item.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Selected Checkmark Badge
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
