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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.screens.CalculatorsScreen
import com.example.ui.screens.DeveloperScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InformationScreen
import com.example.ui.screens.LevelScreen
import com.example.ui.screens.MaterialEstimateScreen
import com.example.ui.screens.MaterialListScreen
import com.example.ui.screens.NotificationScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.ProjectScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TechnicianProfileScreen
import com.example.ui.screens.ThemeScreen
import com.example.ui.theme.ElectricalCalculationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val app = ElectricalApp.getApp(this)
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

    val isSplashScreen = currentRoute == Screen.Splash.route

    val isTopLevelRoute = currentRoute in listOf(
        Screen.Main.route,
        Screen.Calculators.route,
        Screen.Project.route,
        Screen.Reports.route,
        Screen.History.route,
        Screen.Settings.route
    )

    val currentTitle = when {
        currentRoute == Screen.Main.route -> "Electrical Calculation All"
        currentRoute == Screen.Calculators.route -> "Calculators (167)"
        currentRoute == Screen.Project.route -> "Contract Project Manager"
        currentRoute == Screen.Reports.route -> "Reports"
        currentRoute == Screen.History.route -> "Calculation History"
        currentRoute == Screen.Favorites.route -> "Favorites"
        currentRoute == Screen.Theme.route -> "Theme & Appearance"
        currentRoute == Screen.MaterialEstimate.route -> "Material Estimate"
        currentRoute == Screen.Advance.route -> "Advance Calculators (55)"
        currentRoute == Screen.Basic.route -> "Basic Calculators (61)"
        currentRoute == Screen.Lower.route -> "Lower Calculators (51)"
        currentRoute == Screen.BuildingMaterialInput.route -> "Building Electrical Material"
        currentRoute == Screen.MaterialList.route -> "Material Requirement List"
        currentRoute == Screen.Settings.route -> "Settings"
        currentRoute == Screen.About.route -> "About Application"
        currentRoute == Screen.Developer.route -> "Developer Information"
        currentRoute == Screen.PrivacyPolicy.route -> "Privacy Policy"
        currentRoute == Screen.Notification.route -> "Notifications"
        currentRoute == Screen.Information.route -> "Information • সহায়িকা (167)"
        currentRoute == Screen.TechnicianProfile.route -> "Technician Report Profile"
        currentRoute?.startsWith("calculator/") == true -> {
            val id = navBackStackEntry?.arguments?.getInt("calcId") ?: 0
            CalculatorRegistry.getById(id)?.name ?: "Calculator"
        }
        else -> "Electrical Calculation All"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!isSplashScreen) {
                AppTopBar(
                    title = currentTitle,
                    canNavigateBack = !isTopLevelRoute,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToNotification = { navController.navigate(Screen.Notification.route) },
                    onNavigateToInformation = { navController.navigate(Screen.Information.route) }
                )
            }
        },
        bottomBar = {
            if (isTopLevelRoute && !isSplashScreen) {
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
            startDestination = Screen.Splash.route,
            modifier = if (isSplashScreen) Modifier.fillMaxSize() else Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onLoadingFinished = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Splash.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Main.route) {
                HomeScreen(
                    onNavigateToCalculator = { id ->
                        navController.navigate(Screen.CalculatorDetail.createRoute(id))
                    },
                    onNavigateToBuildingMaterial = {
                        navController.navigate(Screen.MaterialEstimate.route)
                    },
                    onNavigateToFavorites = {
                        navController.navigate(Screen.Favorites.route)
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route)
                    },
                    onNavigateToCalculatorsList = {
                        navController.navigate(Screen.Calculators.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToProject = {
                        navController.navigate(Screen.Project.route)
                    },
                    onNavigateToInformation = {
                        navController.navigate(Screen.Information.route)
                    },
                    onNavigateToAdvance = {
                        navController.navigate(Screen.Advance.route)
                    },
                    onNavigateToBasic = {
                        navController.navigate(Screen.Basic.route)
                    },
                    onNavigateToLower = {
                        navController.navigate(Screen.Lower.route)
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

            composable(Screen.Project.route) {
                ProjectScreen()
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

            composable(Screen.Calculators.route) {
                CalculatorsScreen(
                    onNavigateToCalculator = { id ->
                        navController.navigate(Screen.CalculatorDetail.createRoute(id))
                    }
                )
            }

            composable(Screen.Reports.route) {
                ReportsScreen(
                    onNavigateToCalculator = { id ->
                        navController.navigate(Screen.CalculatorDetail.createRoute(id))
                    },
                    onNavigateToMaterialEstimate = {
                        navController.navigate(Screen.MaterialEstimate.route)
                    },
                    onNavigateToProject = {
                        navController.navigate(Screen.Project.route)
                    }
                )
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onNavigateToCalculator = { id ->
                        navController.navigate(Screen.CalculatorDetail.createRoute(id))
                    }
                )
            }

            composable(Screen.Theme.route) {
                ThemeScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.MaterialEstimate.route) {
                MaterialEstimateScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToNotification = { navController.navigate(Screen.Notification.route) },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) },
                    onNavigateToDeveloper = { navController.navigate(Screen.Developer.route) },
                    onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route) },
                    onNavigateToTechnicianProfile = { navController.navigate(Screen.TechnicianProfile.route) },
                    onNavigateToTheme = { navController.navigate(Screen.Theme.route) },
                    onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                    onNavigateToMaterialEstimate = { navController.navigate(Screen.MaterialEstimate.route) }
                )
            }

            composable(Screen.TechnicianProfile.route) {
                TechnicianProfileScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
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

            composable(Screen.Information.route) {
                InformationScreen(
                    onNavigateToCalculator = { id ->
                        navController.navigate(Screen.CalculatorDetail.createRoute(id))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
