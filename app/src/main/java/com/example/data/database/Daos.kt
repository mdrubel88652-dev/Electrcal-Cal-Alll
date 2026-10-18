package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM calculation_history WHERE calculatorName LIKE '%' || :query || '%' OR primaryResult LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchHistory(query: String): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryEntity): Long

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteHistory(id: Long)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAll()
}

@Dao
interface BuildingProjectDao {
    @Query("SELECT * FROM building_projects ORDER BY timestamp DESC")
    fun getAllProjects(): Flow<List<BuildingProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: BuildingProjectEntity): Long

    @Query("DELETE FROM building_projects WHERE id = :id")
    suspend fun deleteProject(id: Long)

    @Query("DELETE FROM building_projects")
    suspend fun clearAll()
}

@Dao
interface ContractProjectDao {
    @Query("SELECT * FROM contract_projects ORDER BY timestamp DESC")
    fun getAllProjects(): Flow<List<ContractProjectEntity>>

    @Query("SELECT * FROM contract_projects WHERE id = :id")
    suspend fun getProjectById(id: Long): ContractProjectEntity?

    @Query("SELECT COUNT(*) FROM contract_projects")
    fun getProjectCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProject(project: ContractProjectEntity): Long

    @Query("DELETE FROM contract_projects WHERE id = :id")
    suspend fun deleteProject(id: Long)

    @Query("DELETE FROM contract_projects")
    suspend fun clearAll()
}

@Dao
interface ProjectPaymentDao {
    @Query("SELECT * FROM project_payments WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getPaymentsForProject(projectId: Long): Flow<List<ProjectPaymentEntity>>

    @Query("SELECT * FROM project_payments WHERE projectId = :projectId ORDER BY timestamp ASC")
    suspend fun getPaymentsListForProject(projectId: Long): List<ProjectPaymentEntity>

    @Query("SELECT * FROM project_payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<ProjectPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: ProjectPaymentEntity): Long

    @Query("DELETE FROM project_payments WHERE id = :id")
    suspend fun deletePayment(id: Long)

    @Query("DELETE FROM project_payments WHERE projectId = :projectId")
    suspend fun deletePaymentsForProject(projectId: Long)
}

@Dao
interface QuickTaskDao {
    @Query("SELECT * FROM quick_tasks ORDER BY timestamp DESC")
    fun getAllQuickTasks(): Flow<List<QuickTaskEntity>>

    @Query("SELECT * FROM quick_tasks WHERE monthYear = :monthYear ORDER BY timestamp DESC")
    fun getQuickTasksForMonth(monthYear: String): Flow<List<QuickTaskEntity>>

    @Query("SELECT SUM(amount) FROM quick_tasks WHERE monthYear = :monthYear")
    fun getMonthlyTotal(monthYear: String): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuickTask(task: QuickTaskEntity): Long

    @Query("DELETE FROM quick_tasks WHERE id = :id")
    suspend fun deleteQuickTask(id: Long)

    @Query("DELETE FROM quick_tasks")
    suspend fun clearAll()
}
