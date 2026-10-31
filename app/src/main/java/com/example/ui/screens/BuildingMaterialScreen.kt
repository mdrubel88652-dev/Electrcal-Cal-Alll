package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ElectricalApp
import com.example.data.calculator.BuildingMaterialEngine
import com.example.data.model.BuildingProjectData
import com.example.data.model.BuildingRoomData
import com.example.data.model.CableInsulationType
import com.example.data.model.CircuitType
import com.example.data.model.ConductorMaterial
import com.example.data.model.InstallationMethod
import com.example.data.model.RoomCircuitItem
import com.example.data.model.SupplyType
import com.example.ui.theme.ElectricBluePrimary
import com.example.ui.theme.LevelAdvanceColor
import com.example.ui.theme.LevelBasicColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun BuildingMaterialScreen(
    onNavigateToMaterialList: () -> Unit
) {
    val context = LocalContext.current
    val app = ElectricalApp.getApp(context)
    val language by app.settingsManager.languageFlow.collectAsState(initial = "EN")
    val isEn = language == "EN"

    // Project & Building Details
    var projectName by remember { mutableStateOf("Dream Residential Villa") }
    var ownerName by remember { mutableStateOf("Mr. Tariq Rahman") }
    var address by remember { mutableStateOf("Plot 42, Road 7, Sector 3, Dhaka") }
    var preparedBy by remember { mutableStateOf("Engr. M. Kabir (Lead Electrician)") }
    var floorsText by remember { mutableStateOf("2") }
    var layoutReference by remember { mutableStateOf("Architectural Floor Plan Rev-2") }
    var mainDbLocation by remember { mutableStateOf("Ground Floor Main Intake / Meter Room") }
    var supplyType by remember { mutableStateOf(SupplyType.SINGLE_PHASE) }

    // Routing & Environmental Details
    var ambientTempCText by remember { mutableStateOf("35") }
    var selectedConductorMaterial by remember { mutableStateOf(ConductorMaterial.COPPER) }
    var selectedInsulationType by remember { mutableStateOf(CableInsulationType.BYA_PVC) }
    var selectedInstallationMethod by remember { mutableStateOf(InstallationMethod.CONDUIT_IN_WALL) }
    var coilLength by remember { mutableIntStateOf(100) } // 90m or 100m or custom
    var customCoilLengthText by remember { mutableStateOf("100") }

    // Mode Toggle: Beginner Mode vs. Advanced Mode
    var isAdvancedMode by remember { mutableStateOf(BuildingMaterialStateHolder.isAdvancedMode) }

    // Filter & Search
    var roomSearchQuery by remember { mutableStateOf("") }
    var selectedFloorFilter by remember { mutableIntStateOf(0) } // 0 = All Floors

    // Brand preferences
    var wireBrand by remember { mutableStateOf("BRB / BBS Cables") }
    var pipeBrand by remember { mutableStateOf("Standard PVC Pipe") }
    var switchBrand by remember { mutableStateOf("Modular Switch (Super Star)") }

    // Dialog States
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var showBatchCreateDialog by remember { mutableStateOf(false) }
    var roomToEdit by remember { mutableStateOf<BuildingRoomData?>(null) }
    var roomForCustomCircuit by remember { mutableStateOf<BuildingRoomData?>(null) }

    // Initialize default rooms if empty
    LaunchedEffect(Unit) {
        val f = floorsText.toIntOrNull() ?: 2
        BuildingMaterialStateHolder.initializeWithDefaultsIfEmpty(floors = f, totalRooms = 6)
    }

    val roomsList = BuildingMaterialStateHolder.roomsList

    // Filtered rooms
    val filteredRooms = remember(roomsList.size, roomSearchQuery, selectedFloorFilter) {
        roomsList.filter { room ->
            val matchesSearch = roomSearchQuery.isBlank() || room.name.contains(roomSearchQuery, ignoreCase = true)
            val matchesFloor = selectedFloorFilter == 0 || room.floorNumber == selectedFloorFilter
            matchesSearch && matchesFloor
        }
    }

    val totalFloors = floorsText.toIntOrNull()?.coerceAtLeast(1) ?: 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("building_material_screen")
    ) {
        // ==========================================
        // 0. MODE SWITCHER HEADER (BEGINNER vs ADVANCED)
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cable,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isEn) "House Wiring Cable Sizing" else "হাউস ওয়্যারিং ক্যাবল ও কয়েল হিসাব",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = if (isEn) "Complete Building Wire & BOQ Engine (BNBC/IEC)" else "বিল্ডিং ওয়্যারিং ক্যাবল সাইজ ও কয়েল গণনা",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Beginner / Advanced Tab Switcher
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    isAdvancedMode = false
                                    BuildingMaterialStateHolder.isAdvancedMode = false
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (!isAdvancedMode) ElectricBluePrimary else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isEn) "🎓 Beginner Mode" else "🎓 সহজ গাইড মোড",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = if (!isAdvancedMode) Color.White else Color(0xFF475569)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    isAdvancedMode = true
                                    BuildingMaterialStateHolder.isAdvancedMode = true
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isAdvancedMode) LevelAdvanceColor else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isEn) "⚙️ Advanced Engineering" else "⚙️ অ্যাডভান্সড ইঞ্জিনিয়ারিং",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = if (isAdvancedMode) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // STEP 1 — BUILDING & PROJECT INFORMATION
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE0F2FE),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("1", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ElectricBluePrimary)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEn) "Building & Supply Information" else "বিল্ডিং ও বিদ্যুৎ সংযোগ তথ্য",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = Color(0xFF0F172A)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text(if (isEn) "Project / Building Name" else "প্রজেক্ট / ভবনের নাম") },
                    modifier = Modifier.fillMaxWidth().testTag("input_project_name"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text(if (isEn) "Owner Name" else "মালিকের নাম") },
                        modifier = Modifier.weight(1f).testTag("input_owner_name"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = floorsText,
                        onValueChange = {
                            floorsText = it
                        },
                        label = { Text(if (isEn) "Floors" else "মোট তলা") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.7f).testTag("input_floors"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(if (isEn) "Site Address / Location" else "সাইট লোকেশন / ঠিকানা") },
                    modifier = Modifier.fillMaxWidth().testTag("input_site_address"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Supply Type & DB Location
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { supplyType = SupplyType.SINGLE_PHASE },
                        shape = RoundedCornerShape(8.dp),
                        color = if (supplyType == SupplyType.SINGLE_PHASE) Color(0xFFE0F2FE) else Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (supplyType == SupplyType.SINGLE_PHASE) ElectricBluePrimary else Color(0xFFE2E8F0)
                        )
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (supplyType == SupplyType.SINGLE_PHASE) Icons.Default.Check else Icons.Default.Apartment,
                                contentDescription = null,
                                tint = if (supplyType == SupplyType.SINGLE_PHASE) ElectricBluePrimary else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "1-Phase (230V)",
                                fontSize = 12.sp,
                                fontWeight = if (supplyType == SupplyType.SINGLE_PHASE) FontWeight.Bold else FontWeight.Normal,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { supplyType = SupplyType.THREE_PHASE },
                        shape = RoundedCornerShape(8.dp),
                        color = if (supplyType == SupplyType.THREE_PHASE) Color(0xFFE0F2FE) else Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (supplyType == SupplyType.THREE_PHASE) ElectricBluePrimary else Color(0xFFE2E8F0)
                        )
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (supplyType == SupplyType.THREE_PHASE) Icons.Default.Check else Icons.Default.Apartment,
                                contentDescription = null,
                                tint = if (supplyType == SupplyType.THREE_PHASE) ElectricBluePrimary else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "3-Phase (400V)",
                                fontSize = 12.sp,
                                fontWeight = if (supplyType == SupplyType.THREE_PHASE) FontWeight.Bold else FontWeight.Normal,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = mainDbLocation,
                    onValueChange = { mainDbLocation = it },
                    label = { Text(if (isEn) "Main DB Location / Supply Intake" else "মেইন ডিস্ট্রিবিউশন বোর্ড (DB) অবস্থান") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                if (isAdvancedMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = layoutReference,
                        onValueChange = { layoutReference = it },
                        label = { Text(if (isEn) "Floor Plan / Drawing Reference" else "ফ্লোর প্ল্যান / ড্রয়িং রেফারেন্স") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // STEP 2 — COMPLETE BUILDING ROOM MANAGEMENT
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE0F2FE),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("2", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ElectricBluePrimary)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEn) "Building Rooms & Circuits" else "ভবনের রুম ও সার্কিট ব্যবস্থাপনা",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Text(
                        text = "${roomsList.size} Rooms",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBluePrimary
                    )
                }

                Text(
                    text = if (isEn) {
                        "Add, edit, duplicate, or batch-create rooms across any floor. Cable runs are calculated specifically for each room."
                    } else {
                        "প্রতিটি তলার রুম যোগ, এডিট বা ডুপ্লিকেট করুন। রুম প্রতি ক্যাবল রুট আলাদাভাবে হিসাব হবে।"
                    },
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                )

                // Search Bar & Floor Filter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = roomSearchQuery,
                        onValueChange = { roomSearchQuery = it },
                        placeholder = { Text(if (isEn) "Search room name..." else "রুম খুঁজুন...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Floor filter
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.clickable {
                            selectedFloorFilter = (selectedFloorFilter + 1) % (totalFloors + 1)
                        }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (selectedFloorFilter == 0) "All Floors" else "Floor $selectedFloorFilter",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBluePrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons: Add Room + Batch Create + 1-Click Quick Create 5/10 Rooms
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showAddRoomDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEn) "Add Room" else "+ রুম যোগ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showBatchCreateDialog = true },
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEn) "Batch Create" else "কাস্টম ব্যাচ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 1-Click Instant Room Creation (5 Rooms / 10 Rooms)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val cur = roomsList.size
                            val newRooms = (1..5).map { idx ->
                                val roomNum = cur + idx
                                val assignedFloor = if (totalFloors > 1) ((idx - 1) % totalFloors) + 1 else 1
                                BuildingRoomData(
                                    name = if (isEn) "Room #$roomNum" else "রুম #$roomNum",
                                    floorNumber = assignedFloor,
                                    lengthFt = 14.0,
                                    widthFt = 12.0,
                                    heightFt = 10.0,
                                    distanceToDbMeters = 6.0 + ((roomNum % 5) * 2.0),
                                    isDistanceVerified = false,
                                    lights = 4,
                                    fans = 2,
                                    sockets13A = 3,
                                    acCount = if (idx % 2 == 1) 1 else 0,
                                    acTonnage = 1.5,
                                    geyserCount = 0
                                )
                            }
                            roomsList.addAll(newRooms)
                            selectedFloorFilter = 0
                            Toast.makeText(context, if (isEn) "5 rooms created with 1 click!" else "এক ক্লিকেই ৫টি রুম সফলভাবে তৈরি হয়েছে!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEn) "+5 Rooms (1-Click)" else "+ ৫টি রুম (১-ক্লিক)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val cur = roomsList.size
                            val newRooms = (1..10).map { idx ->
                                val roomNum = cur + idx
                                val assignedFloor = if (totalFloors > 1) ((idx - 1) % totalFloors) + 1 else 1
                                BuildingRoomData(
                                    name = if (isEn) "Room #$roomNum" else "রুম #$roomNum",
                                    floorNumber = assignedFloor,
                                    lengthFt = 14.0,
                                    widthFt = 12.0,
                                    heightFt = 10.0,
                                    distanceToDbMeters = 6.0 + ((roomNum % 6) * 2.0),
                                    isDistanceVerified = false,
                                    lights = 4,
                                    fans = 2,
                                    sockets13A = 3,
                                    acCount = if (idx % 2 == 1) 1 else 0,
                                    acTonnage = 1.5,
                                    geyserCount = 0
                                )
                            }
                            roomsList.addAll(newRooms)
                            selectedFloorFilter = 0
                            Toast.makeText(context, if (isEn) "10 rooms created with 1 click!" else "এক ক্লিকেই ১০টি রুম সফলভাবে তৈরি হয়েছে!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEn) "+10 Rooms (1-Click)" else "+ ১০টি রুম (১-ক্লিক)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Room Cards List
                if (filteredRooms.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isEn) "No rooms match your filter. Click '+ Add Room' to start." else "কোনো রুম পাওয়া যায়নি। '+ রুম যোগ করুন' চাপুন।",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.5.sp
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredRooms.forEach { room ->
                            RoomCardItem(
                                room = room,
                                isAdvancedMode = isAdvancedMode,
                                isEn = isEn,
                                onEdit = { roomToEdit = room },
                                onDuplicate = {
                                    val newRoom = room.copy(
                                        id = UUID.randomUUID().toString(),
                                        name = "${room.name} (Copy)"
                                    )
                                    roomsList.add(newRoom)
                                    Toast.makeText(context, "Duplicated ${room.name}", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = {
                                    if (roomsList.size > 1) {
                                        roomsList.remove(room)
                                        Toast.makeText(context, "Removed ${room.name}", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "At least 1 room required in project", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onAddCustomCircuit = {
                                    roomForCustomCircuit = room
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // STEP 3 — CABLE ROUTING & ELECTRICAL INPUTS
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE0F2FE),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("3", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ElectricBluePrimary)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEn) "Cable Routing & Electrical Standards" else "ক্যাবল রাউটিং ও স্ট্যান্ডার্ড সেটিংস",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = Color(0xFF0F172A)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Coil Length Selector (90m, 100m, custom)
                Text(
                    text = if (isEn) "Cable Coil Length Setting (Supplier Roll Standard)" else "ক্যাবল কয়েল সাইজ (মার্কেট স্ট্যান্ডার্ড)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF475569)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(90, 100, 180).forEach { rollLength ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { coilLength = rollLength },
                            shape = RoundedCornerShape(8.dp),
                            color = if (coilLength == rollLength) Color(0xFFE0F2FE) else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (coilLength == rollLength) ElectricBluePrimary else Color(0xFFE2E8F0)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$rollLength m",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (coilLength == rollLength) ElectricBluePrimary else Color(0xFF0F172A)
                                )
                                Text(
                                    text = if (rollLength == 100) "Standard (BRB)" else "${(rollLength * 1.093).toInt()} yds",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                if (isAdvancedMode) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isEn) "Conductor & Installation Method (BNBC / IEC 60364)" else "কন্ডাক্টর ও ওয়্যারিং পদ্ধতি",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Material & Ambient Temp
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ambientTempCText,
                            onValueChange = { ambientTempCText = it },
                            label = { Text("Ambient Temp (°C)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        Surface(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(56.dp)
                                .clickable {
                                    selectedConductorMaterial = if (selectedConductorMaterial == ConductorMaterial.COPPER) {
                                        ConductorMaterial.ALUMINUM
                                    } else {
                                        ConductorMaterial.COPPER
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Conductor", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text(selectedConductorMaterial.label, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Installation method
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedInstallationMethod = when (selectedInstallationMethod) {
                                    InstallationMethod.CONDUIT_IN_WALL -> InstallationMethod.SURFACE_CONDUIT
                                    InstallationMethod.SURFACE_CONDUIT -> InstallationMethod.CABLE_TRAY
                                    InstallationMethod.CABLE_TRAY -> InstallationMethod.CONDUIT_IN_WALL
                                    else -> InstallationMethod.CONDUIT_IN_WALL
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Installation Method", fontSize = 10.5.sp, color = Color(0xFF64748B))
                                Text(selectedInstallationMethod.label, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("Change", fontSize = 11.5.sp, color = ElectricBluePrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // STEP 4 — SPECIFICATIONS & BRANDS
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isEn) "Cable Specifications & Brand" else "ক্যাবল ব্র্যান্ড ও স্পেসিফিকেশন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = wireBrand,
                    onValueChange = { wireBrand = it },
                    label = { Text(if (isEn) "Wire & Cable Brand (e.g. BRB / BBS / Eastern)" else "ক্যাবল ব্র্যান্ড (যেমনঃ BRB / BBS / Eastern)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // PRIMARY ACTION BUTTON: CALCULATE
        // ==========================================
        Button(
            onClick = {
                val f = floorsText.toIntOrNull() ?: 2
                val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                val ambient = ambientTempCText.toIntOrNull() ?: 35

                // Compile aggregated points map from all configured rooms for backward compatibility
                val pointsMap = mutableMapOf(
                    "light" to roomsList.sumOf { it.lights },
                    "fan" to roomsList.sumOf { it.fans },
                    "socket" to roomsList.sumOf { it.sockets13A },
                    "ac" to roomsList.sumOf { it.acCount },
                    "geyser" to roomsList.sumOf { it.geyserCount },
                    "pump" to (if (f > 1) 1 else 0),
                    "tv" to roomsList.size / 2,
                    "data" to roomsList.size / 2,
                    "bell" to 1,
                    "exhaust" to roomsList.size / 3,
                    "other" to 2
                )

                val proj = BuildingProjectData(
                    projectName = projectName.ifBlank { "Residential Electrical Project" },
                    ownerName = ownerName.ifBlank { "Building Owner" },
                    address = address.ifBlank { "Site Address" },
                    preparedBy = preparedBy,
                    date = dateStr,
                    floors = f,
                    rooms = roomsList.size,
                    points = pointsMap,
                    wireBrand = wireBrand,
                    pipeBrand = pipeBrand,
                    switchBrand = switchBrand,
                    coilLengthMeters = coilLength,
                    supplyType = supplyType,
                    mainDbLocation = mainDbLocation,
                    ambientTemperatureC = ambient,
                    calculationMode = if (isAdvancedMode) "ADVANCED" else "BEGINNER",
                    roomList = roomsList.toList()
                )

                // Run House Wiring Cable and Material Engine
                val (cableResult, materials) = BuildingMaterialEngine.calculateBuildingCablesAndMaterials(proj)

                BuildingMaterialStateHolder.currentProject = proj.copy(
                    materials = materials,
                    cableCalculationResult = cableResult
                )
                BuildingMaterialStateHolder.currentMaterials = materials
                BuildingMaterialStateHolder.currentCableResult = cableResult
                BuildingMaterialStateHolder.selectedCoilLength = coilLength

                onNavigateToMaterialList()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("btn_calculate_materials"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
        ) {
            Icon(Icons.Default.Cable, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isEn) "Calculate House Wiring Cable Sizing" else "হাউস ওয়্যারিং ক্যাবল সাইজিং হিসাব করুন",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ==========================================
    // DIALOG: ADD ROOM
    // ==========================================
    if (showAddRoomDialog) {
        RoomEditDialog(
            initialRoom = null,
            totalFloors = totalFloors,
            isEn = isEn,
            onDismiss = { showAddRoomDialog = false },
            onSave = { newRoom ->
                roomsList.add(newRoom)
                showAddRoomDialog = false
                Toast.makeText(context, "Added ${newRoom.name}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ==========================================
    // DIALOG: EDIT ROOM
    // ==========================================
    roomToEdit?.let { room ->
        RoomEditDialog(
            initialRoom = room,
            totalFloors = totalFloors,
            isEn = isEn,
            onDismiss = { roomToEdit = null },
            onSave = { updatedRoom ->
                val index = roomsList.indexOfFirst { it.id == room.id }
                if (index != -1) {
                    roomsList[index] = updatedRoom
                    Toast.makeText(context, "Updated ${updatedRoom.name}", Toast.LENGTH_SHORT).show()
                }
                roomToEdit = null
            }
        )
    }

    // ==========================================
    // DIALOG: BATCH CREATE SIMILAR ROOMS
    // ==========================================
    if (showBatchCreateDialog) {
        BatchCreateRoomsDialog(
            totalFloors = totalFloors,
            isEn = isEn,
            onDismiss = { showBatchCreateDialog = false },
            onGenerate = { createdRooms ->
                roomsList.addAll(createdRooms)
                showBatchCreateDialog = false
                Toast.makeText(context, "Created ${createdRooms.size} rooms successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ==========================================
    // DIALOG: ADD DEDICATED CIRCUIT
    // ==========================================
    roomForCustomCircuit?.let { targetRoom ->
        AddCustomCircuitDialog(
            roomName = targetRoom.name,
            isEn = isEn,
            onDismiss = { roomForCustomCircuit = null },
            onAddCircuit = { circuit ->
                val index = roomsList.indexOfFirst { it.id == targetRoom.id }
                if (index != -1) {
                    val updated = targetRoom.copy(circuits = targetRoom.circuits + circuit)
                    roomsList[index] = updated
                    Toast.makeText(context, "Added circuit to ${targetRoom.name}", Toast.LENGTH_SHORT).show()
                }
                roomForCustomCircuit = null
            }
        )
    }
}

@Composable
private fun RoomCardItem(
    room: BuildingRoomData,
    isAdvancedMode: Boolean,
    isEn: Boolean,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onAddCustomCircuit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ElectricBluePrimary.copy(alpha = 0.12f),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = "Floor ${room.floorNumber}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBluePrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = room.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Color(0xFF0F172A)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF475569), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Badges row: Dimensions & DB route
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${room.lengthFt.toInt()}′ × ${room.widthFt.toInt()}′ (${room.heightFt.toInt()}′ high)",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
                Text("•", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                Text(
                    text = "DB Route: ${room.distanceToDbMeters.toInt()}m ${if (room.isDistanceVerified) "✓ (Site Measured)" else "(Est.)"}",
                    fontSize = 11.sp,
                    color = if (room.isDistanceVerified) Color(0xFF059669) else Color(0xFF64748B),
                    fontWeight = if (room.isDistanceVerified) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Points summary chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PointChip(label = "Lights: ${room.lights}", color = Color(0xFF0284C7))
                PointChip(label = "Fans: ${room.fans}", color = Color(0xFF0D9488))
                PointChip(label = "Sockets: ${room.sockets13A}", color = Color(0xFFD97706))
                if (room.acCount > 0) {
                    PointChip(label = "AC: ${room.acCount}", color = Color(0xFFDC2626))
                }
                if (room.geyserCount > 0) {
                    PointChip(label = "Geyser: ${room.geyserCount}", color = Color(0xFF7C3AED))
                }
            }

            if (isAdvancedMode) {
                Spacer(modifier = Modifier.height(8.dp))
                if (room.circuits.isNotEmpty()) {
                    Text(
                        text = "Custom Circuits: ${room.circuits.joinToString { "${it.name} (${it.totalWatts.toInt()}W)" }}",
                        fontSize = 10.5.sp,
                        color = Color(0xFF475569)
                    )
                }
                TextButton(
                    onClick = onAddCustomCircuit,
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("+ Add Dedicated Circuit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricBluePrimary)
                }
            }
        }
    }
}

@Composable
private fun PointChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun RoomEditDialog(
    initialRoom: BuildingRoomData?,
    totalFloors: Int,
    isEn: Boolean,
    onDismiss: () -> Unit,
    onSave: (BuildingRoomData) -> Unit
) {
    var name by remember { mutableStateOf(initialRoom?.name ?: "Bed Room") }
    var floorNumber by remember { mutableIntStateOf(initialRoom?.floorNumber ?: 1) }
    var lengthText by remember { mutableStateOf((initialRoom?.lengthFt ?: 14.0).toInt().toString()) }
    var widthText by remember { mutableStateOf((initialRoom?.widthFt ?: 12.0).toInt().toString()) }
    var heightText by remember { mutableStateOf((initialRoom?.heightFt ?: 10.0).toInt().toString()) }
    var distanceText by remember { mutableStateOf((initialRoom?.distanceToDbMeters ?: 8.0).toInt().toString()) }
    var isDistanceVerified by remember { mutableStateOf(initialRoom?.isDistanceVerified ?: false) }

    var lights by remember { mutableIntStateOf(initialRoom?.lights ?: 4) }
    var fans by remember { mutableIntStateOf(initialRoom?.fans ?: 2) }
    var sockets by remember { mutableIntStateOf(initialRoom?.sockets13A ?: 3) }
    var acCount by remember { mutableIntStateOf(initialRoom?.acCount ?: 0) }
    var geyserCount by remember { mutableIntStateOf(initialRoom?.geyserCount ?: 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialRoom == null) (if (isEn) "Add Room" else "রুম যোগ করুন") else (if (isEn) "Edit Room" else "রুম পরিবর্তন করুন"),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Room Name / Type") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Floor selection
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Floor:", fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (f in 1..totalFloors.coerceAtMost(6)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (floorNumber == f) ElectricBluePrimary else Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { floorNumber = f }
                            ) {
                                Text(
                                    text = "F$f",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (floorNumber == f) Color.White else Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Dimensions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = lengthText,
                        onValueChange = { lengthText = it },
                        label = { Text("Length (ft)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = widthText,
                        onValueChange = { widthText = it },
                        label = { Text("Width (ft)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Height (ft)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Route from DB
                OutlinedTextField(
                    value = distanceText,
                    onValueChange = { distanceText = it },
                    label = { Text("Cable Route Length from DB (Meters)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isDistanceVerified,
                        onCheckedChange = { isDistanceVerified = it }
                    )
                    Text("Verified Site Measurement (Physical tape measured)", fontSize = 11.5.sp)
                }

                HorizontalDivider()

                // Points counters
                Text("Room Electrical Points:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                CounterRow(label = "Light Points", count = lights, onUpdate = { lights = it })
                CounterRow(label = "Ceiling Fans", count = fans, onUpdate = { fans = it })
                CounterRow(label = "13A Power Sockets", count = sockets, onUpdate = { sockets = it })
                CounterRow(label = "Air Conditioners (AC)", count = acCount, onUpdate = { acCount = it })
                CounterRow(label = "Water Geysers", count = geyserCount, onUpdate = { geyserCount = it })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val l = lengthText.toDoubleOrNull() ?: 14.0
                    val w = widthText.toDoubleOrNull() ?: 12.0
                    val h = heightText.toDoubleOrNull() ?: 10.0
                    val dist = distanceText.toDoubleOrNull() ?: 8.0

                    val room = initialRoom?.copy(
                        name = name.ifBlank { "Room" },
                        floorNumber = floorNumber,
                        lengthFt = l,
                        widthFt = w,
                        heightFt = h,
                        distanceToDbMeters = dist,
                        isDistanceVerified = isDistanceVerified,
                        lights = lights,
                        fans = fans,
                        sockets13A = sockets,
                        acCount = acCount,
                        geyserCount = geyserCount
                    ) ?: BuildingRoomData(
                        name = name.ifBlank { "Room" },
                        floorNumber = floorNumber,
                        lengthFt = l,
                        widthFt = w,
                        heightFt = h,
                        distanceToDbMeters = dist,
                        isDistanceVerified = isDistanceVerified,
                        lights = lights,
                        fans = fans,
                        sockets13A = sockets,
                        acCount = acCount,
                        geyserCount = geyserCount
                    )
                    onSave(room)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
            ) {
                Text("Save Room")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun CounterRow(label: String, count: Int, onUpdate: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledIconButton(
                onClick = { if (count > 0) onUpdate(count - 1) },
                modifier = Modifier.size(28.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
            }
            Text("$count", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 10.dp))
            FilledIconButton(
                onClick = { onUpdate(count + 1) },
                modifier = Modifier.size(28.dp),
                shape = RoundedCornerShape(6.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = ElectricBluePrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun BatchCreateRoomsDialog(
    totalFloors: Int,
    isEn: Boolean,
    onDismiss: () -> Unit,
    onGenerate: (List<BuildingRoomData>) -> Unit
) {
    var prefix by remember { mutableStateOf("Bed Room") }
    var quantityText by remember { mutableStateOf("5") }
    var floorText by remember { mutableStateOf("1") }
    var lightsPerRoom by remember { mutableIntStateOf(4) }
    var fansPerRoom by remember { mutableIntStateOf(2) }
    var socketsPerRoom by remember { mutableIntStateOf(3) }
    var acsPerRoom by remember { mutableIntStateOf(1) }

    fun makeRooms(count: Int, targetFloor: Int): List<BuildingRoomData> {
        val f = targetFloor.coerceIn(1, totalFloors)
        return (1..count).map { idx ->
            BuildingRoomData(
                name = "$prefix #$idx",
                floorNumber = f,
                lengthFt = 14.0,
                widthFt = 12.0,
                heightFt = 10.0,
                distanceToDbMeters = 6.0 + (idx * 2.0),
                isDistanceVerified = false,
                lights = lightsPerRoom,
                fans = fansPerRoom,
                sockets13A = socketsPerRoom,
                acCount = acsPerRoom,
                geyserCount = 0
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEn) "Batch Create Rooms" else "একসাথে একাধিক রুম তৈরি করুন", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick 1-Click Buttons
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (isEn) "1-Click Quick Presets:" else "১-ক্লিকে সরাসরি তৈরি করুন:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val f = floorText.toIntOrNull() ?: 1
                                    onGenerate(makeRooms(5, f))
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Text(if (isEn) "+5 Rooms" else "+ ৫টি রুম", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    val f = floorText.toIntOrNull() ?: 1
                                    onGenerate(makeRooms(10, f))
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                            ) {
                                Text(if (isEn) "+10 Rooms" else "+ ১০টি রুম", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Text(
                    text = if (isEn) "Or customize quantity and load settings below:" else "অথবা নিচে পছন্দমতো রুম সংখ্যা ও লোড সেট করুন:",
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B)
                )

                OutlinedTextField(
                    value = prefix,
                    onValueChange = { prefix = it },
                    label = { Text("Room Name Prefix (e.g. Bed Room / Office)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = floorText,
                        onValueChange = { floorText = it },
                        label = { Text("On Floor (1-$totalFloors)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                HorizontalDivider()

                CounterRow(label = "Lights per Room", count = lightsPerRoom, onUpdate = { lightsPerRoom = it })
                CounterRow(label = "Fans per Room", count = fansPerRoom, onUpdate = { fansPerRoom = it })
                CounterRow(label = "Sockets per Room", count = socketsPerRoom, onUpdate = { socketsPerRoom = it })
                CounterRow(label = "ACs per Room", count = acsPerRoom, onUpdate = { acsPerRoom = it })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = quantityText.toIntOrNull()?.coerceIn(1, 50) ?: 5
                    val f = floorText.toIntOrNull() ?: 1
                    onGenerate(makeRooms(count, f))
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
            ) {
                Text(if (isEn) "Generate Rooms" else "রুম তৈরি করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (isEn) "Cancel" else "বাতিল") }
        }
    )
}

@Composable
private fun AddCustomCircuitDialog(
    roomName: String,
    isEn: Boolean,
    onDismiss: () -> Unit,
    onAddCircuit: (RoomCircuitItem) -> Unit
) {
    var name by remember { mutableStateOf("Dedicated Kitchen Oven") }
    var powerText by remember { mutableStateOf("2000") }
    var unit by remember { mutableStateOf("W") } // "W" or "kW"
    var routeText by remember { mutableStateOf("18") }
    var isThreePhase by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Dedicated Circuit to $roomName", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Circuit Name (e.g. Microwave / Induction)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = powerText,
                        onValueChange = { powerText = it },
                        label = { Text("Rated Power") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Surface(
                        modifier = Modifier
                            .weight(0.6f)
                            .height(56.dp)
                            .clickable { unit = if (unit == "W") "kW" else "W" },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Unit: $unit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = routeText,
                    onValueChange = { routeText = it },
                    label = { Text("Route Run Length (Meters)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isThreePhase, onCheckedChange = { isThreePhase = it })
                    Text("3-Phase Supply (400V) Load", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = powerText.toDoubleOrNull() ?: 1500.0
                    val r = routeText.toDoubleOrNull() ?: 15.0
                    val item = RoomCircuitItem(
                        name = name.ifBlank { "Custom Circuit" },
                        circuitType = CircuitType.CUSTOM,
                        loadPower = p,
                        loadUnit = unit,
                        quantity = 1,
                        routeLengthMeters = r,
                        isThreePhase = isThreePhase
                    )
                    onAddCircuit(item)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
            ) {
                Text("Add Circuit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
