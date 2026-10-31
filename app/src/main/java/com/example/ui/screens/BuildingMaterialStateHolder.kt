package com.example.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.model.BuildingCableCalculationResult
import com.example.data.model.BuildingProjectData
import com.example.data.model.BuildingRoomData
import com.example.data.model.MaterialItem

object BuildingMaterialStateHolder {
    var currentProject by mutableStateOf<BuildingProjectData?>(null)
    var currentMaterials by mutableStateOf<List<MaterialItem>>(emptyList())
    var currentCableResult by mutableStateOf<BuildingCableCalculationResult?>(null)

    // Dynamic Rooms list persisted across screen navigation
    val roomsList = mutableStateListOf<BuildingRoomData>()

    var isAdvancedMode by mutableStateOf(false)
    var selectedCoilLength by mutableStateOf(100)
    var selectedFloorFilter by mutableStateOf(0) // 0 means All Floors

    fun initializeWithDefaultsIfEmpty(floors: Int = 2, totalRooms: Int = 6) {
        if (roomsList.isEmpty()) {
            val defaultNames = listOf(
                "Master Bed Room", "Bed Room 2", "Living Room",
                "Dining Room", "Kitchen", "Guest Room"
            )
            val count = maxOf(1, totalRooms)
            for (i in 0 until count) {
                val floor = (i / maxOf(1, count / floors)) + 1
                val name = defaultNames.getOrElse(i) { "Room #${i + 1}" }
                roomsList.add(
                    BuildingRoomData(
                        name = name,
                        floorNumber = floor.coerceAtMost(floors),
                        lengthFt = 14.0,
                        widthFt = 12.0,
                        heightFt = 10.0,
                        distanceToDbMeters = 6.0 + (i * 2.0),
                        isDistanceVerified = false,
                        lights = 4,
                        fans = 2,
                        sockets13A = 3,
                        acCount = if (i < 2) 1 else 0,
                        acTonnage = 1.5,
                        geyserCount = if (i == 0 || i == 4) 1 else 0
                    )
                )
            }
        }
    }
}
