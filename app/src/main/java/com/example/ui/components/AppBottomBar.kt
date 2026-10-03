package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.navigation.Screen
import com.example.ui.theme.LevelAdvanceColor
import com.example.ui.theme.LevelBasicColor
import com.example.ui.theme.LevelLowerColor

@Composable
fun AppBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        tonalElevation = 8.dp,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        // 1. Main
        val isMain = currentRoute == Screen.Main.route
        NavigationBarItem(
            selected = isMain,
            onClick = { onNavigate(Screen.Main.route) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Main"
                )
            },
            label = {
                Text(
                    "Main",
                    fontWeight = if (isMain) FontWeight.Bold else FontWeight.Normal
                )
            },
            modifier = Modifier.testTag("nav_item_main")
        )

        // 2. Advance
        val isAdvance = currentRoute == Screen.Advance.route
        NavigationBarItem(
            selected = isAdvance,
            onClick = { onNavigate(Screen.Advance.route) },
            icon = {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = "Advance"
                )
            },
            label = {
                Text(
                    "Advance",
                    color = if (isAdvance) LevelAdvanceColor else Color.Unspecified,
                    fontWeight = if (isAdvance) FontWeight.Bold else FontWeight.Normal
                )
            },
            modifier = Modifier.testTag("nav_item_advance")
        )

        // 3. Basic
        val isBasic = currentRoute == Screen.Basic.route
        NavigationBarItem(
            selected = isBasic,
            onClick = { onNavigate(Screen.Basic.route) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Basic"
                )
            },
            label = {
                Text(
                    "Basic",
                    color = if (isBasic) LevelBasicColor else Color.Unspecified,
                    fontWeight = if (isBasic) FontWeight.Bold else FontWeight.Normal
                )
            },
            modifier = Modifier.testTag("nav_item_basic")
        )

        // 4. Lower
        val isLower = currentRoute == Screen.Lower.route
        NavigationBarItem(
            selected = isLower,
            onClick = { onNavigate(Screen.Lower.route) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = "Lower"
                )
            },
            label = {
                Text(
                    "Lower",
                    color = if (isLower) LevelLowerColor else Color.Unspecified,
                    fontWeight = if (isLower) FontWeight.Bold else FontWeight.Normal
                )
            },
            modifier = Modifier.testTag("nav_item_lower")
        )
    }
}
