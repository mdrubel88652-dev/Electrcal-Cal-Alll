package com.example.ui.navigation

sealed class Screen(val route: String) {
    // Startup Destination
    object Splash : Screen("splash")

    // 4 Bottom Nav Destinations
    object Main : Screen("main")
    object Advance : Screen("advance")
    object Basic : Screen("basic")
    object Lower : Screen("lower")
    object Project : Screen("project")

    // Sub-screens
    object CalculatorDetail : Screen("calculator/{calcId}") {
        fun createRoute(calcId: Int) = "calculator/$calcId"
    }
    object BuildingMaterialInput : Screen("building_material_input")
    object MaterialList : Screen("material_list")
    object History : Screen("history")
    object Settings : Screen("settings")
    object About : Screen("about")
    object Developer : Screen("developer")
    object PrivacyPolicy : Screen("privacy_policy")
    object Notification : Screen("notification")
    object Information : Screen("information")
    object TechnicianProfile : Screen("technician_profile")
    object Calculators : Screen("calculators")
    object Reports : Screen("reports")
    object Favorites : Screen("favorites")
    object Theme : Screen("theme")
    object MaterialEstimate : Screen("material_estimate")
    object PrepaidMeter : Screen("prepaid_meter")
    object Tools : Screen("tools")
}
