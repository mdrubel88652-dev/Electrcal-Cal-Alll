package com.example.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "electrical_settings")

data class DeveloperReportSettings(
    val developerName: String = "Rubel Electrical Engineer & Developer",
    val companyName: String = "Industrial Electrical & Power Engineering",
    val contactNumber: String = "+880 16094-92193",
    val email: String = "mdrubel88652@gmail.com",
    val address: String = "Power Systems Engineering Division",
    val website: String = "www.electricalcalculationall.com"
)

data class PdfPrintSettings(
    val enablePdfReport: Boolean = true,
    val printPreview: Boolean = true,
    val paperSize: String = "A4", // A4, A5
    val orientation: String = "PORTRAIT", // PORTRAIT, LANDSCAPE
    val showLogo: Boolean = true,
    val showDeveloperInfo: Boolean = true,
    val showDateTime: Boolean = true,
    val showCalculationId: Boolean = true,
    val showFormula: Boolean = true,
    val showSteps: Boolean = true,
    val showNotes: Boolean = true
)

data class CalculationDefaults(
    val default3PhaseVoltage: Double = 400.0,
    val default1PhaseVoltage: Double = 230.0,
    val defaultFrequency: Double = 50.0,
    val defaultPowerFactor: Double = 0.85,
    val decimalPlaces: Int = 2,
    val standard: String = "IEC" // IEC, NEC, BNBC, General
)

data class BuildingMaterialSettings(
    val wireUnit: String = "Coil (100m)",
    val conduitUnit: String = "Length (3m / 10ft)",
    val showSpecification: Boolean = true,
    val showBrand: Boolean = true,
    val showQuantity: Boolean = true
)

class SettingsManager(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode") // LIGHT, DARK, SYSTEM
        
        // Developer
        val DEV_NAME = stringPreferencesKey("dev_name")
        val DEV_COMPANY = stringPreferencesKey("dev_company")
        val DEV_PHONE = stringPreferencesKey("dev_phone")
        val DEV_EMAIL = stringPreferencesKey("dev_email")
        val DEV_ADDRESS = stringPreferencesKey("dev_address")
        val DEV_WEBSITE = stringPreferencesKey("dev_website")
        
        // PDF & Print
        val PDF_ENABLED = booleanPreferencesKey("pdf_enabled")
        val PRINT_PREVIEW = booleanPreferencesKey("print_preview")
        val PAPER_SIZE = stringPreferencesKey("paper_size")
        val ORIENTATION = stringPreferencesKey("orientation")
        val SHOW_LOGO = booleanPreferencesKey("show_logo")
        val SHOW_DEV_INFO = booleanPreferencesKey("show_dev_info")
        val SHOW_DATE_TIME = booleanPreferencesKey("show_date_time")
        val SHOW_CALC_ID = booleanPreferencesKey("show_calc_id")
        val SHOW_FORMULA = booleanPreferencesKey("show_formula")
        val SHOW_STEPS = booleanPreferencesKey("show_steps")
        val SHOW_NOTES = booleanPreferencesKey("show_notes")
        
        // Calculation
        val DEF_3P_VOLT = doublePreferencesKey("def_3p_volt")
        val DEF_1P_VOLT = doublePreferencesKey("def_1p_volt")
        val DEF_FREQ = doublePreferencesKey("def_freq")
        val DEF_PF = doublePreferencesKey("def_pf")
        val DECIMAL_PLACES = intPreferencesKey("decimal_places")
        val STANDARD = stringPreferencesKey("standard")
        
        // Building Material
        val WIRE_UNIT = stringPreferencesKey("wire_unit")
        val CONDUIT_UNIT = stringPreferencesKey("conduit_unit")
        val SHOW_SPEC = booleanPreferencesKey("show_spec")
        val SHOW_BRAND = booleanPreferencesKey("show_brand")
        val SHOW_QTY = booleanPreferencesKey("show_qty")

        // Technician Report Profile
        val TECH_NAME = stringPreferencesKey("tech_name")
        val TECH_COMPANY = stringPreferencesKey("tech_company")
        val TECH_PHONE = stringPreferencesKey("tech_phone")
        val TECH_EMAIL = stringPreferencesKey("tech_email")
        val TECH_ADDRESS = stringPreferencesKey("tech_address")
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE] ?: "LIGHT"
    }

    val developerSettingsFlow: Flow<DeveloperReportSettings> = context.dataStore.data.map { prefs ->
        DeveloperReportSettings(
            developerName = prefs[Keys.DEV_NAME] ?: "Lead Electrical Engineer",
            companyName = prefs[Keys.DEV_COMPANY] ?: "Industrial Electrical & Power Engineering",
            contactNumber = prefs[Keys.DEV_PHONE] ?: "+880 1700-123456",
            email = prefs[Keys.DEV_EMAIL] ?: "electrical.engineer@domain.com",
            address = prefs[Keys.DEV_ADDRESS] ?: "Power Systems Engineering Division",
            website = prefs[Keys.DEV_WEBSITE] ?: "www.electricalcalculationall.com"
        )
    }

    val pdfPrintSettingsFlow: Flow<PdfPrintSettings> = context.dataStore.data.map { prefs ->
        PdfPrintSettings(
            enablePdfReport = prefs[Keys.PDF_ENABLED] ?: true,
            printPreview = prefs[Keys.PRINT_PREVIEW] ?: true,
            paperSize = prefs[Keys.PAPER_SIZE] ?: "A4",
            orientation = prefs[Keys.ORIENTATION] ?: "PORTRAIT",
            showLogo = prefs[Keys.SHOW_LOGO] ?: true,
            showDeveloperInfo = prefs[Keys.SHOW_DEV_INFO] ?: true,
            showDateTime = prefs[Keys.SHOW_DATE_TIME] ?: true,
            showCalculationId = prefs[Keys.SHOW_CALC_ID] ?: true,
            showFormula = prefs[Keys.SHOW_FORMULA] ?: true,
            showSteps = prefs[Keys.SHOW_STEPS] ?: true,
            showNotes = prefs[Keys.SHOW_NOTES] ?: true
        )
    }

    val calculationDefaultsFlow: Flow<CalculationDefaults> = context.dataStore.data.map { prefs ->
        CalculationDefaults(
            default3PhaseVoltage = prefs[Keys.DEF_3P_VOLT] ?: 400.0,
            default1PhaseVoltage = prefs[Keys.DEF_1P_VOLT] ?: 230.0,
            defaultFrequency = prefs[Keys.DEF_FREQ] ?: 50.0,
            defaultPowerFactor = prefs[Keys.DEF_PF] ?: 0.85,
            decimalPlaces = prefs[Keys.DECIMAL_PLACES] ?: 2,
            standard = prefs[Keys.STANDARD] ?: "IEC"
        )
    }

    val buildingMaterialSettingsFlow: Flow<BuildingMaterialSettings> = context.dataStore.data.map { prefs ->
        BuildingMaterialSettings(
            wireUnit = prefs[Keys.WIRE_UNIT] ?: "Coil (100m)",
            conduitUnit = prefs[Keys.CONDUIT_UNIT] ?: "Length (3m / 10ft)",
            showSpecification = prefs[Keys.SHOW_SPEC] ?: true,
            showBrand = prefs[Keys.SHOW_BRAND] ?: true,
            showQuantity = prefs[Keys.SHOW_QTY] ?: true
        )
    }

    val technicianProfileFlow: Flow<TechnicianReportProfile> = context.dataStore.data.map { prefs ->
        TechnicianReportProfile(
            name = prefs[Keys.TECH_NAME] ?: "",
            company = prefs[Keys.TECH_COMPANY] ?: "",
            contactPhone = prefs[Keys.TECH_PHONE] ?: "",
            email = prefs[Keys.TECH_EMAIL] ?: "",
            officialAddress = prefs[Keys.TECH_ADDRESS] ?: ""
        )
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun updateDeveloperSettings(settings: DeveloperReportSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEV_NAME] = settings.developerName
            prefs[Keys.DEV_COMPANY] = settings.companyName
            prefs[Keys.DEV_PHONE] = settings.contactNumber
            prefs[Keys.DEV_EMAIL] = settings.email
            prefs[Keys.DEV_ADDRESS] = settings.address
            prefs[Keys.DEV_WEBSITE] = settings.website
        }
    }

    suspend fun updatePdfPrintSettings(settings: PdfPrintSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PDF_ENABLED] = settings.enablePdfReport
            prefs[Keys.PRINT_PREVIEW] = settings.printPreview
            prefs[Keys.PAPER_SIZE] = settings.paperSize
            prefs[Keys.ORIENTATION] = settings.orientation
            prefs[Keys.SHOW_LOGO] = settings.showLogo
            prefs[Keys.SHOW_DEV_INFO] = settings.showDeveloperInfo
            prefs[Keys.SHOW_DATE_TIME] = settings.showDateTime
            prefs[Keys.SHOW_CALC_ID] = settings.showCalculationId
            prefs[Keys.SHOW_FORMULA] = settings.showFormula
            prefs[Keys.SHOW_STEPS] = settings.showSteps
            prefs[Keys.SHOW_NOTES] = settings.showNotes
        }
    }

    suspend fun updateCalculationDefaults(defaults: CalculationDefaults) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEF_3P_VOLT] = defaults.default3PhaseVoltage
            prefs[Keys.DEF_1P_VOLT] = defaults.default1PhaseVoltage
            prefs[Keys.DEF_FREQ] = defaults.defaultFrequency
            prefs[Keys.DEF_PF] = defaults.defaultPowerFactor
            prefs[Keys.DECIMAL_PLACES] = defaults.decimalPlaces
            prefs[Keys.STANDARD] = defaults.standard
        }
    }

    suspend fun updateBuildingMaterialSettings(settings: BuildingMaterialSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WIRE_UNIT] = settings.wireUnit
            prefs[Keys.CONDUIT_UNIT] = settings.conduitUnit
            prefs[Keys.SHOW_SPEC] = settings.showSpecification
            prefs[Keys.SHOW_BRAND] = settings.showBrand
            prefs[Keys.SHOW_QTY] = settings.showQuantity
        }
    }

    suspend fun updateTechnicianProfile(profile: TechnicianReportProfile) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TECH_NAME] = profile.name.trim()
            prefs[Keys.TECH_COMPANY] = profile.company.trim()
            prefs[Keys.TECH_PHONE] = profile.contactPhone.trim()
            prefs[Keys.TECH_EMAIL] = profile.email.trim()
            prefs[Keys.TECH_ADDRESS] = profile.officialAddress.trim()
        }
    }

    suspend fun resetTechnicianProfile() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.TECH_NAME)
            prefs.remove(Keys.TECH_COMPANY)
            prefs.remove(Keys.TECH_PHONE)
            prefs.remove(Keys.TECH_EMAIL)
            prefs.remove(Keys.TECH_ADDRESS)
        }
    }
}
