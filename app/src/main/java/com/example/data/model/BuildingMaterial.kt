package com.example.data.model

data class MaterialItem(
    val no: Int,
    val name: String,
    val specification: String,
    val brand: String = "",
    val unit: String,
    val quantity: Int,
    val remarks: String = ""
)

data class ElectricalPointCounter(
    val id: String,
    val name: String,
    val defaultCount: Int,
    val category: String
)

data class BuildingProjectData(
    val id: Long = 0,
    val projectName: String,
    val ownerName: String,
    val address: String,
    val preparedBy: String,
    val date: String,
    val floors: Int,
    val rooms: Int,
    val points: Map<String, Int>,
    val wireBrand: String = "BRB / BBS / Eastern",
    val pipeBrand: String = "Standard PVC",
    val switchBrand: String = "Modular Super Star",
    val materials: List<MaterialItem> = emptyList()
)
