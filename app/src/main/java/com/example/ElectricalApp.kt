package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.datastore.SettingsManager
import com.example.data.repository.HistoryRepository

class ElectricalApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var historyRepository: HistoryRepository
        private set

    lateinit var settingsManager: SettingsManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getDatabase(this)
        historyRepository = HistoryRepository(database.historyDao(), database.buildingProjectDao())
        settingsManager = SettingsManager(this)
    }

    companion object {
        lateinit var instance: ElectricalApp
            private set
    }
}
