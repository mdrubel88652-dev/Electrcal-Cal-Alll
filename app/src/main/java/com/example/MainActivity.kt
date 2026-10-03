package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.calculator.CalculatorRegistry
import com.example.data.model.CalculatorLevel
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.BuildingMaterialScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.DeveloperScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LevelScreen
import com.example.ui.screens.MaterialListScreen
import com.example.ui.screens.NotificationScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ElectricalCalculationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val app = ElectricalApp.instance
            val themeMode by app.settingsManager.themeModeFlow.collectAsState(initial = "LIGHT")
            val darkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            ElectricalCalculationTheme(darkTheme = darkTheme) {
                MainAppNavigation()
            }
        }
    }
}

@Composable
fun MainAppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelRoute = currentRoute in listOf(
        Screen.Main.route,
        Screen.Advance.route,
        Screen.Basic.route,
        Screen.Lower.route
    )

    val currentTitle = when {
        currentRoute == Screen.Main.route -> "ELECTRICAL CALCULATION ALL"
        currentRoute == Screen.Advance.route -> "Advance Calculators (55)"
        currentRoute == Screen.Basic.route -> "Basic Calculators (61)"
        currentRoute == Screen.Lower.route -> "Lower Calculators (51)"
        currentRoute == Screen.BuildingMaterialInput.route -> "Building Electrical Material"
        currentRoute == Screen.MaterialList.route -> "Material Requirement List"
        currentRoute == Screen.History.route -> "Calculation History"
        currentRoute == Screen.Settings.route -> "Settings"
        currentRoute == Screen.About.route -> "About Application"
        currentRoute == Screen.Developer.route -> "Developer Information"
        currentRoute == Screen.PrivacyPolicy.route -> "Privacy Policy"
        currentRoute == Screen.Notification.route -> "Notifications"
        currentRoute?.startsWith("calculator/") == true -> {
            val id = navBackStackEntry?.arguments?.getInt("calcId") ?: 0
            CalculatorRegistry.getById(id)?.name ?: "Calculator"
        }
        else -> "ELECTRICAL CALCULATION ALL"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = currentTitle,
                canNavigateBack = !isTopLevelRoute,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onNavigateToDeveloper = { navController.navigate(Screen.Developer.route) },
                onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route) },
                onNavigateToNotification = { navController.navigate(Screen.Notification.route) }
            )
        },
        bottomBar = {
            if (isTopLevelRoute) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Main.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Main.route) {
                HomeScreen(
                    onNavigateToCalculator = { id ->
                        navController.navigate(Screen.CalculatorDetail.createRoute(id))
                    },
                    onNavigateToBuildingMaterial = {
                        navController.navigate(Screen.BuildingMaterialInput.route)
                    }
                )
            }

            composable(Screen.Advance.route) {
                LevelScreen(
                    level = CalculatorLevel.ADVANCE,
                    onNavigateToCalculator = { id ->
                        navController.navigate(Screen.CalculatorDetail.createRoute(id))
                    },
                    onNavigateToBuildingMaterial = {
                        navController.navigate(Screen.BuildingMaterialInput.route)
                    }
                )
            }

            composable(Screen.Basic.route) {
                LevelScreen(
                    level = CalculatorLevel.BASIC,
                    onNavigateToCalculator = { id ->
                        navController.navigate(Screen.CalculatorDetail.createRoute(id))
                    },
                    onNavigateToBuildingMaterial = {
                        navController.navigate(Screen.BuildingMaterialInput.route)
                    }
                )
            }

            composable(Screen.Lower.route) {
                LevelScreen(
                    level = CalculatorLevel.LOWER,
                    onNavigateToCalculator = { id ->
                        navController.navigate(Screen.CalculatorDetail.createRoute(id))
                    },
                    onNavigateToBuildingMaterial = {
                        navController.navigate(Screen.BuildingMaterialInput.route)
                    }
                )
            }

            composable(
                route = Screen.CalculatorDetail.route,
                arguments = listOf(navArgument("calcId") { type = NavType.IntType })
            ) { backStackEntry ->
                val calcId = backStackEntry.arguments?.getInt("calcId") ?: 1
                CalculatorScreen(
                    calcId = calcId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.BuildingMaterialInput.route) {
                BuildingMaterialScreen(
                    onNavigateToMaterialList = {
                        navController.navigate(Screen.MaterialList.route)
                    }
                )
            }

            composable(Screen.MaterialList.route) {
                MaterialListScreen()
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    onNavigateToCalculator = { id ->
                        navController.navigate(Screen.CalculatorDetail.createRoute(id))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            composable(Screen.About.route) {
                AboutScreen()
            }

            composable(Screen.Developer.route) {
                DeveloperScreen()
            }

            composable(Screen.PrivacyPolicy.route) {
                PrivacyPolicyScreen()
            }

            composable(Screen.Notification.route) {
                NotificationScreen()
            }
        }
    }
}
