package com.example.data.repository

import com.example.data.database.BuildingProjectDao
import com.example.data.database.BuildingProjectEntity
import com.example.data.database.HistoryDao
import com.example.data.database.HistoryEntity
import kotlinx.coroutines.flow.Flow

class HistoryRepository(
    private val historyDao: HistoryDao,
    private val buildingProjectDao: BuildingProjectDao
) {
    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()
    val allBuildingProjects: Flow<List<BuildingProjectEntity>> = buildingProjectDao.getAllProjects()

    fun searchHistory(query: String): Flow<List<HistoryEntity>> = historyDao.searchHistory(query)

    suspend fun insertHistory(item: HistoryEntity): Long = historyDao.insertHistory(item)

    suspend fun deleteHistory(id: Long) = historyDao.deleteHistory(id)

    suspend fun clearAllHistory() = historyDao.clearAll()

    suspend fun insertBuildingProject(project: BuildingProjectEntity): Long =
        buildingProjectDao.insertProject(project)

    suspend fun deleteBuildingProject(id: Long) = buildingProjectDao.deleteProject(id)
}
