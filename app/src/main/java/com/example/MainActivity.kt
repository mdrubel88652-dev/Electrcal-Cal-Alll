package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.example.ui.screens.PrepaidMeterData
import com.example.ui.screens.ElectricalToolsCatalog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.ui.components.TechnicianHubSheet
import com.example.ui.navigation.Screen
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.BuildingMaterialScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.CalculatorsScreen
import com.example.ui.screens.DeveloperScreen
import com.example.ui.screens.ElectricalToolsScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InformationScreen
import com.example.ui.screens.LevelScreen
import com.example.ui.screens.MaterialEstimateScreen
import com.example.ui.screens.MaterialListScreen
import com.example.ui.screens.NotificationScreen
import com.example.ui.screens.PrepaidMeterScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.ProjectScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TechnicianProfileScreen
import com.example.ui.screens.ThemeScreen
import com.example.ui.theme.ElectricalCalculationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemNavigationBars()

        setContent {
            val app = ElectricalApp.getApp(this)
            val themeMode by app.settingsManager.themeModeFlow.collectAsState(initial = "LIGHT")
            val accentHex by app.settingsManager.accentColorFlow.collectAsState(initial = "#0284C7")
            val darkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> false
            }

            val accentColor = remember(accentHex) {
                try {
                    Color(android.graphics.Color.parseColor(accentHex))
                } catch (e: Exception) {
                    Color(0xFF0284C7)
                }
            }

            ElectricalCalculationTheme(
                darkTheme = darkTheme,
                accentColor = accentColor
            ) {
                MainAppNavigation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemNavigationBars()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemNavigationBars()
        }
    }

    private fun hideSystemNavigationBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
    }
}

@Composable
fun MainAppNavigation() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = ElectricalApp.getApp(context)
    val currentLanguage by app.settingsManager.languageFlow.collectAsState(initial = "EN")
    val isEn = currentLanguage == "EN"

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showThreeLineMenu by remember { mutableStateOf(false) }

    val isSplashScreen = currentRoute == Screen.Splash.route

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            CalculatorRegistry.allCalculators
            PrepaidMeterData.meterBrands
            ElectricalToolsCatalog.tools
        }
    }

    val isTopLevelRoute = currentRoute in listOf(
        Screen.Main.route,
        Screen.Calculators.route,
        Screen.Project.route,
        Screen.Favorites.route,
        Screen.Settings.route
    )

    val currentTitle = when {
        currentRoute == Screen.Main.route -> "Electrical Calculation All"
        currentRoute == Screen.Calculators.route -> if (isEn) "Calculators (167)" else "ক্যালকুলেটর (১৬৭)"
        currentRoute == Screen.Project.route -> if (isEn) "Contract Project Manager" else "কন্ট্রাক্ট প্রজেক্ট ম্যানেজার"
        currentRoute == Screen.Reports.route -> if (isEn) "Reports" else "রিপোর্ট"
        currentRoute == Screen.History.route -> if (isEn) "Calculation History" else "হিসাবের ইতিহাস"
        currentRoute == Screen.Favorites.route -> if (isEn) "Favorites" else "ফেভারিট"
        currentRoute == Screen.Theme.route -> if (isEn) "Theme & Appearance" else "থিম ও রূপরেখা"
        currentRoute == Screen.MaterialEstimate.route -> if (isEn) "Material Estimate" else "ম্যাটেরিয়াল এস্টিমেট"
        currentRoute == Screen.Advance.route -> if (isEn) "Advance Calculators (55)" else "অ্যাডভান্স ক্যালকুলেটর (৫৫)"
        currentRoute == Screen.Basic.route -> if (isEn) "Basic Calculators (61)" else "বেসিক ক্যালকুলেটর (৬১)"
        currentRoute == Screen.Lower.route -> if (isEn) "Lower Calculators (51)" else "লোয়ার ক্যালকুলেটর (৫১)"
        currentRoute == Screen.BuildingMaterialInput.route -> if (isEn) "Building Electrical Material" else "বিল্ডিং ইলেকট্রিক্যাল ম্যাটেরিয়াল"
        currentRoute == Screen.MaterialList.route -> if (isEn) "Material Requirement List" else "প্রয়োজনীয় মালামালের তালিকা"
        currentRoute == Screen.Settings.route -> if (isEn) "Settings" else "সেটিংস"
        currentRoute == Screen.About.route -> if (isEn) "About Application" else "অ্যাপ্লিকেশন তথ্য"
        currentRoute == Screen.Developer.route -> if (isEn) "Developer Information" else "ডেভেলপার তথ্য"
        currentRoute == Screen.PrivacyPolicy.route -> if (isEn) "Privacy Policy" else "প্রাইভেসি পলিসি"
        currentRoute == Screen.Notification.route -> if (isEn) "Notifications" else "নোটিফিকেশন"
        currentRoute == Screen.Information.route -> if (isEn) "Information Guide (167)" else "সহায়িকা • Information (167)"
        currentRoute == Screen.TechnicianProfile.route -> if (isEn) "Technician Report Profile" else "টেকনিশিয়ান রিপোর্ট প্রোফাইল"
        currentRoute == Screen.PrepaidMeter.route -> if (isEn) "Prepaid Meter Codes" else "প্রিপেইড মিটার কোড ও সহায়িকা"
        currentRoute == Screen.Tools.route -> if (isEn) "Electrical Tools Guide (50)" else "ইলেকট্রিক্যাল টুলস সহায়িকা (৫০)"
        currentRoute?.startsWith("calculator/") == true -> {
            val id = navBackStackEntry?.arguments?.getInt("calcId") ?: 0
            CalculatorRegistry.getById(id)?.name ?: "Calculator"
        }
        else -> "Electrical Calculation All"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (!isSplashScreen && currentRoute != Screen.Main.route) {
                AppTopBar(
                    title = currentTitle,
                    subtitle = "Electrical Engineering Calculation & Estimation",
                    canNavigateBack = !isTopLevelRoute,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToNotification = { navController.navigate(Screen.Notification.route) },
                    onNavigateToInformation = { navController.navigate(Screen.Information.route) },
                    onOpenMenu = { showThreeLineMenu = true }
                )
            }
        },
        bottomBar = {
            if (isTopLevelRoute && !isSplashScreen) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(Screen.Main.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
            modifier = if (isSplashScreen || currentRoute == Screen.Main.route) Modifier.fillMaxSize()
                       else Modifier.padding(innerPadding)
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

            composable(
                route = Screen.Main.route,
                exitTransition = { fadeOut(animationSpec = tween(0)) },
                popEnterTransition = { fadeIn(animationSpec = tween(0)) }
            ) {
                if (currentRoute == Screen.Main.route) {
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
                        onNavigateToNotification = {
                            navController.navigate(Screen.Notification.route)
                        },
                        onNavigateToAdvance = {
                            navController.navigate(Screen.Advance.route)
                        },
                        onNavigateToBasic = {
                            navController.navigate(Screen.Basic.route)
                        },
                        onNavigateToLower = {
                            navController.navigate(Screen.Lower.route)
                        },
                        onNavigateToPrepaidMeter = {
                            navController.navigate(Screen.PrepaidMeter.route)
                        },
                        onNavigateToTools = {
                            navController.navigate(Screen.Tools.route)
                        },
                        onOpenMenu = { showThreeLineMenu = true }
                    )
                }
            }

            composable(Screen.Advance.route) {
                LevelScreen(
                    level = CalculatorLevel.ADVANCE,
                    isEn = isEn,
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
                    isEn = isEn,
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
                    isEn = isEn,
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
                    onNavigateToReports = { navController.navigate(Screen.Reports.route) },
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

            composable(Screen.PrepaidMeter.route) {
                PrepaidMeterScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Tools.route) {
                ElectricalToolsScreen(
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

    if (showThreeLineMenu) {
        TechnicianHubSheet(
            onDismiss = { showThreeLineMenu = false },
            onNavigateToSettingsProfile = {
                navController.navigate(Screen.TechnicianProfile.route)
            },
            onNavigateToProject = {
                navController.navigate(Screen.Project.route) {
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
