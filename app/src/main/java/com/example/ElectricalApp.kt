package com.example

import android.app.Application
import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.datastore.SettingsManager
import com.example.data.repository.HistoryRepository

class ElectricalApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    val historyRepository: HistoryRepository by lazy {
        HistoryRepository(database.historyDao(), database.buildingProjectDao())
    }

    val projectRepository: com.example.data.repository.ProjectRepository by lazy {
        com.example.data.repository.ProjectRepository(
            database.contractProjectDao(),
            database.projectPaymentDao(),
            database.quickTaskDao(),
            database.expenseDao()
        )
    }

    val settingsManager: SettingsManager by lazy {
        SettingsManager(this)
    }

    override fun onCreate() {
        super.onCreate()
        _instance = this
    }

    companion object {
        private var _instance: ElectricalApp? = null

        val instance: ElectricalApp
            get() = _instance ?: synchronized(this) {
                _instance ?: ElectricalApp().also { _instance = it }
            }

        fun getApp(context: Context): ElectricalApp {
            return (context.applicationContext as? ElectricalApp) ?: instance
        }
    }
}
