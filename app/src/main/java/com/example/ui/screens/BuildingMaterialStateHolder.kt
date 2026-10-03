package com.example.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.model.BuildingProjectData
import com.example.data.model.MaterialItem

object BuildingMaterialStateHolder {
    var currentProject by mutableStateOf<BuildingProjectData?>(null)
    var currentMaterials by mutableStateOf<List<MaterialItem>>(emptyList())
}
