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

@Entity(tableName = "contract_projects")
data class ContractProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectName: String,
    val clientName: String = "",
    val location: String = "",
    val contactPhone: String = "",
    val emailAddress: String = "",
    val contractAmount: Double,
    val startDate: String,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "project_payments")
data class ProjectPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val amount: Double,
    val paymentDate: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quick_tasks")
data class QuickTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskTitle: String, // কি কাজ করছে (e.g. Substation Maintenance, Board Wiring)
    val clientName: String = "", // ক্লায়েন্ট / কাস্টমার
    val contactPhone: String = "",
    val location: String = "",
    val amount: Double, // কত টাকা পেমেন্ট পেয়েছেন
    val taskDate: String, // কাজের তারিখ (e.g. "18 Oct 2026")
    val monthYear: String, // মাস ও বছর (e.g. "2026-10") for 50,000 monthly limit
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
