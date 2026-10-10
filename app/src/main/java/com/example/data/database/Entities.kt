package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val calculatorId: Int,
    val calculatorName: String,
    val level: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDate: String,
    val inputSummary: String,
    val primaryResult: String,
    val resultUnit: String,
    val formula: String,
    val stepSummary: String,
    val notes: String = "",
    val technicianName: String = "",
    val technicianCompany: String = "",
    val technicianPhone: String = "",
    val technicianEmail: String = "",
    val technicianAddress: String = ""
)

@Entity(tableName = "building_projects")
data class BuildingProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectName: String,
    val ownerName: String,
    val address: String,
    val preparedBy: String,
    val date: String,
    val totalPoints: Int,
    val floors: Int,
    val rooms: Int,
    val materialsJson: String,
    val timestamp: Long = System.currentTimeMillis()
)
