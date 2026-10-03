package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.calculator.BuildingMaterialEngine
import com.example.data.model.BuildingProjectData
import com.example.ui.theme.ElectricBluePrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BuildingMaterialScreen(
    onNavigateToMaterialList: () -> Unit
) {
    val context = LocalContext.current

    var projectName by remember { mutableStateOf("Dream Residential Villa") }
    var ownerName by remember { mutableStateOf("Mr. Tariq Rahman") }
    var address by remember { mutableStateOf("Plot 42, Road 7, Sector 3, Dhaka") }
    var preparedBy by remember { mutableStateOf("Engr. M. Kabir (Lead Electrician)") }
    var floors by remember { mutableStateOf("2") }
    var rooms by remember { mutableStateOf("6") }

    val points = remember {
        mutableStateMapOf(
            "light" to 28,
            "fan" to 8,
            "socket" to 18,
            "ac" to 4,
            "geyser" to 2,
            "pump" to 1,
            "tv" to 3,
            "data" to 3,
            "bell" to 1,
            "exhaust" to 3,
            "other" to 2
        )
    }

    var wireBrand by remember { mutableStateOf("BRB / BBS Cables") }
    var pipeBrand by remember { mutableStateOf("Standard PVC Pipe") }
    var switchBrand by remember { mutableStateOf("Modular Switch (Super Star)") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("building_material_screen")
    ) {
        // Hero Card with architectural house rendering
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                Image(
                    painter = painterResource(id = R.drawable.img_building_house),
                    contentDescription = "Building Architecture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Building Electrical Material Calculation",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Estimate exact requirement of wires, conduits, switches, and panels for the building owner. Strictly no prices or selling.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Building & Project Information
        Text(
            text = "Project & Owner Information",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = projectName,
            onValueChange = { projectName = it },
            label = { Text("Project / Building Name") },
            modifier = Modifier.fillMaxWidth().testTag("input_project_name"),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = ownerName,
            onValueChange = { ownerName = it },
            label = { Text("Building Owner Name") },
            modifier = Modifier.fillMaxWidth().testTag("input_owner_name"),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Site Address / Location") },
            modifier = Modifier.fillMaxWidth().testTag("input_site_address"),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = floors,
                onValueChange = { floors = it },
                label = { Text("Floors") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).testTag("input_floors"),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = rooms,
                onValueChange = { rooms = it },
                label = { Text("Total Rooms") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).testTag("input_rooms"),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Electrical Points Counter
        Text(
            text = "Electrical Points Breakdown",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Enter points count across all rooms and common areas",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        val pointsList = listOf(
            "light" to "Light Point (LED / Panel)",
            "fan" to "Ceiling Fan Point",
            "socket" to "Power Socket 13A Outlet",
            "ac" to "Air Conditioner (AC) Point",
            "geyser" to "Water Heater (Geyser) Point",
            "pump" to "Water Pump Motor Point",
            "tv" to "TV / Cable Point",
            "data" to "Data / Internet RJ45 Point",
            "bell" to "Calling Bell Point",
            "exhaust" to "Exhaust Fan Point",
            "other" to "Other Points"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                pointsList.forEach { (key, label) ->
                    val count = points[key] ?: 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledIconButton(
                                onClick = {
                                    if (count > 0) points[key] = count - 1
                                },
                                modifier = Modifier.size(34.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                            }

                            Text(
                                text = count.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )

                            FilledIconButton(
                                onClick = { points[key] = count + 1 },
                                modifier = Modifier.size(34.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = ElectricBluePrimary
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Preferred Brands
        Text(
            text = "Preferred Specifications / Brands",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = wireBrand,
            onValueChange = { wireBrand = it },
            label = { Text("Wire & Cable Brand") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = pipeBrand,
            onValueChange = { pipeBrand = it },
            label = { Text("Conduit Pipe Brand") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = switchBrand,
            onValueChange = { switchBrand = it },
            label = { Text("Switch & Socket Brand") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Calculate Button
        Button(
            onClick = {
                val f = floors.toIntOrNull() ?: 1
                val r = rooms.toIntOrNull() ?: 1
                val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

                val proj = BuildingProjectData(
                    projectName = projectName.ifBlank { "Residential Electrical Project" },
                    ownerName = ownerName.ifBlank { "Building Owner" },
                    address = address.ifBlank { "Site Address" },
                    preparedBy = preparedBy,
                    date = dateStr,
                    floors = f,
                    rooms = r,
                    points = points.toMap(),
                    wireBrand = wireBrand,
                    pipeBrand = pipeBrand,
                    switchBrand = switchBrand
                )

                val generatedMaterials = BuildingMaterialEngine.calculateMaterials(proj)
                BuildingMaterialStateHolder.currentProject = proj.copy(materials = generatedMaterials)
                BuildingMaterialStateHolder.currentMaterials = generatedMaterials

                onNavigateToMaterialList()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_calculate_materials"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
        ) {
            Icon(Icons.Default.ListAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Calculate Material Requirement", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
