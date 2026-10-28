package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ElectricalApp
import com.example.data.database.BuildingProjectEntity
import com.example.data.datastore.DeveloperReportSettings
import com.example.data.datastore.TechnicianReportProfile
import com.example.data.model.MaterialEstimateRow
import com.example.pdf.PdfReportGenerator
import com.example.print.PrintManagerHelper
import com.example.utils.ShareUtils
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MaterialEstimateScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = remember { ElectricalApp.getApp(context) }

    val devSettings by app.settingsManager.developerSettingsFlow.collectAsState(initial = DeveloperReportSettings())
    val techProfile by app.settingsManager.technicianProfileFlow.collectAsState(initial = TechnicianReportProfile())

    // Project Info State
    var estimateTitle by remember { mutableStateOf("Electrical Material Requirement") }
    var projectName by remember { mutableStateOf("Factory Electrical Setup") }
    var clientName by remember { mutableStateOf("Standard Power Ltd.") }
    var location by remember { mutableStateOf("Industrial Area, Dhaka") }
    val dateStr = remember { SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date()) }

    var showEditProjectDialog by remember { mutableStateOf(false) }

    // Materials Table State (Simple list: Material Name, Size/Spec, Quantity, Unit)
    val materials = remember {
        mutableStateListOf(
            MaterialEstimateRow(1, "Copper Wire 1C x 2.5 mm²", "BYA Standard", 4.0, "Roll"),
            MaterialEstimateRow(2, "Copper Wire 1C x 4.0 mm²", "BYA Heavy Duty", 2.0, "Roll"),
            MaterialEstimateRow(3, "PVC Conduit Pipe 25mm (1\")", "Class D Rigid", 30.0, "Length"),
            MaterialEstimateRow(4, "Single Pole MCB 16A", "10kA C-Curve", 12.0, "PCS"),
            MaterialEstimateRow(5, "Double Pole MCB 32A", "Main Isolator", 2.0, "PCS"),
            MaterialEstimateRow(6, "Distribution Board 8-Way", "Surface Mounting Metal", 1.0, "Box")
        )
    }

    // Dialog state for Add / Edit
    var showMaterialDialog by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var inputName by remember { mutableStateOf("") }
    var inputSpec by remember { mutableStateOf("") }
    var inputQty by remember { mutableStateOf("1") }
    var inputUnit by remember { mutableStateOf("PCS") }

    // Re-index materials
    fun reindexMaterials() {
        for (i in materials.indices) {
            materials[i] = materials[i].copy(sl = i + 1)
        }
    }

    // Function to generate the universal PDF
    fun createPdf(): File {
        return PdfReportGenerator.generateMaterialEstimatePdf(
            context = context,
            estimateTitle = estimateTitle,
            projectName = projectName,
            clientName = clientName,
            location = location,
            dateStr = dateStr,
            materials = materials.toList(),
            techProfile = techProfile,
            devSettings = devSettings
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("material_estimate_screen_root"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Project Information Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Project Information",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TextButton(
                        onClick = { showEditProjectDialog = true },
                        modifier = Modifier.testTag("btn_edit_project_info")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF0284C7))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Info", fontSize = 12.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                InfoFieldRow("Estimate Title", estimateTitle)
                InfoFieldRow("Project Name", projectName)
                InfoFieldRow("Client Name", clientName)
                InfoFieldRow("Location", location)
                InfoFieldRow("Date", dateStr)
            }
        }

        // Materials Table Section (Simple Material List)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Material List (${materials.size} Items)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Simple material list without price or financial calculations",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Button(
                        onClick = {
                            editingIndex = null
                            inputName = ""
                            inputSpec = ""
                            inputQty = "1"
                            inputUnit = "PCS"
                            showMaterialDialog = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("btn_add_material_item")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Material", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Table
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Column {
                        // Table Header
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                    .width(560.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCell("SL", 35.dp, isHeader = true)
                                TableCell("Material Name", 190.dp, isHeader = true)
                                TableCell("Size / Specification", 150.dp, isHeader = true)
                                TableCell("Quantity", 65.dp, isHeader = true)
                                TableCell("Unit", 50.dp, isHeader = true)
                                TableCell("Actions", 70.dp, isHeader = true)
                            }
                        }

                        // Table Rows
                        if (materials.isEmpty()) {
                            Surface(
                                color = Color.White,
                                modifier = Modifier.width(560.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No materials added yet. Tap '+ Add Material' above.",
                                        fontSize = 12.5.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        } else {
                            materials.forEachIndexed { index, item ->
                                val bg = if (index % 2 == 0) Color(0xFFF8FAFC) else Color.White
                                Surface(
                                    color = bg,
                                    modifier = Modifier.border(0.5.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                            .width(560.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TableCell("${index + 1}", 35.dp)
                                        TableCell(item.name, 190.dp, isBold = true)
                                        TableCell(if (item.spec.isNotBlank()) item.spec else "-", 150.dp)
                                        val qtyDisplay = if (item.qty % 1.0 == 0.0) item.qty.toInt().toString() else String.format(Locale.US, "%.1f", item.qty)
                                        TableCell(qtyDisplay, 65.dp, isBold = true)
                                        TableCell(item.unit, 50.dp)

                                        Row(
                                            modifier = Modifier.width(70.dp),
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    editingIndex = index
                                                    inputName = item.name
                                                    inputSpec = item.spec
                                                    inputQty = if (item.qty % 1.0 == 0.0) item.qty.toInt().toString() else item.qty.toString()
                                                    inputUnit = item.unit
                                                    showMaterialDialog = true
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF0284C7), modifier = Modifier.size(15.dp))
                                            }
                                            IconButton(
                                                onClick = {
                                                    materials.removeAt(index)
                                                    reindexMaterials()
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action Buttons (Save, PDF, Print, Share)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Save
            Surface(
                onClick = {
                    scope.launch {
                        app.database.buildingProjectDao().insertProject(
                            BuildingProjectEntity(
                                projectName = projectName,
                                ownerName = clientName,
                                address = location,
                                preparedBy = techProfile.name.ifBlank { "Technician" },
                                date = dateStr,
                                totalPoints = materials.size,
                                floors = 1,
                                rooms = 1,
                                materialsJson = "Materials: ${materials.size} items"
                            )
                        )
                        Toast.makeText(context, "এস্টিমেট সফলভাবে সেভ হয়েছে", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0284C7),
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_save_material_estimate")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 11.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // PDF (Universal PDF Generation)
            Surface(
                onClick = {
                    try {
                        val pdfFile = createPdf()
                        Toast.makeText(context, "PDF Report তৈরি হয়েছে: ${pdfFile.name}", Toast.LENGTH_SHORT).show()
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Open Material Estimate PDF"))
                    } catch (e: Exception) {
                        Toast.makeText(context, "PDF Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                },
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFDC2626),
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_pdf_material_estimate")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 11.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Print (Native Android PrintManager)
            Surface(
                onClick = {
                    try {
                        val pdfFile = createPdf()
                        PrintManagerHelper.printPdfFile(context, pdfFile, "$estimateTitle - Print")
                    } catch (e: Exception) {
                        Toast.makeText(context, "Print Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2563EB),
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_print_material_estimate")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 11.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Share (Universal Share PDF)
            Surface(
                onClick = {
                    try {
                        val pdfFile = createPdf()
                        ShareUtils.sharePdf(context, pdfFile, "$estimateTitle - $projectName")
                    } catch (e: Exception) {
                        // Fallback text share
                        val shareText = buildString {
                            appendLine("📋 $estimateTitle")
                            appendLine("Project: $projectName | Client: $clientName")
                            appendLine("Date: $dateStr")
                            appendLine("Total Materials: ${materials.size} Items")
                            materials.forEach { m ->
                                appendLine("• ${m.name} (${m.spec}) - ${m.qty} ${m.unit}")
                            }
                        }
                        ShareUtils.shareText(context, estimateTitle, shareText)
                    }
                },
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_share_material_estimate")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 11.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Add / Edit Material Item Dialog
    if (showMaterialDialog) {
        val unitOptions = listOf("PCS", "Dozen", "Meter", "Feet", "Roll", "Box", "Coil", "Length", "Set")
        var unitDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showMaterialDialog = false },
            title = {
                Text(
                    text = if (editingIndex != null) "Edit Material Item" else "Add Material Item",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Material Name
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Material Name") },
                        placeholder = { Text("e.g. Copper Cable 1C x 2.5 mm²") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_material_name"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0284C7),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    // 2. Size / Specification
                    OutlinedTextField(
                        value = inputSpec,
                        onValueChange = { inputSpec = it },
                        label = { Text("Size / Specification") },
                        placeholder = { Text("e.g. BYA / NYY / Class D") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_material_spec"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0284C7),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    // 3. Quantity & Unit Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quantity
                        OutlinedTextField(
                            value = inputQty,
                            onValueChange = { inputQty = it },
                            label = { Text("Quantity") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_material_qty"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        // Unit Select Option / Dropdown with visible outline border
                        Box(modifier = Modifier.weight(1f)) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clickable { unitDropdownExpanded = true }
                                    .testTag("select_material_unit"),
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = BorderStroke(1.2.dp, Color(0xFFCBD5E1))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "Unit", fontSize = 10.sp, color = Color(0xFF64748B))
                                        Text(text = inputUnit, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    }
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color(0xFF0284C7))
                                }
                            }

                            DropdownMenu(
                                expanded = unitDropdownExpanded,
                                onDismissRequest = { unitDropdownExpanded = false }
                            ) {
                                unitOptions.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u, fontWeight = if (u == inputUnit) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            inputUnit = u
                                            unitDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputName.isNotBlank()) {
                            val parsedQty = inputQty.toDoubleOrNull() ?: 1.0
                            val finalSpec = if (inputSpec.isBlank()) "-" else inputSpec.trim()

                            val curIdx = editingIndex
                            if (curIdx != null && curIdx in materials.indices) {
                                materials[curIdx] = materials[curIdx].copy(
                                    name = inputName.trim(),
                                    spec = finalSpec,
                                    qty = parsedQty,
                                    unit = inputUnit
                                )
                            } else {
                                materials.add(
                                    MaterialEstimateRow(
                                        sl = materials.size + 1,
                                        name = inputName.trim(),
                                        spec = finalSpec,
                                        qty = parsedQty,
                                        unit = inputUnit
                                    )
                                )
                            }
                            reindexMaterials()
                            showMaterialDialog = false
                        } else {
                            Toast.makeText(context, "Please enter Material Name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text(if (editingIndex != null) "Update" else "Add Item", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMaterialDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    // Edit Project Information Dialog
    if (showEditProjectDialog) {
        var tempTitle by remember { mutableStateOf(estimateTitle) }
        var tempProject by remember { mutableStateOf(projectName) }
        var tempClient by remember { mutableStateOf(clientName) }
        var tempLocation by remember { mutableStateOf(location) }

        AlertDialog(
            onDismissRequest = { showEditProjectDialog = false },
            title = { Text("Edit Project Info", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = tempTitle,
                        onValueChange = { tempTitle = it },
                        label = { Text("Estimate Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0284C7),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                    OutlinedTextField(
                        value = tempProject,
                        onValueChange = { tempProject = it },
                        label = { Text("Project Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0284C7),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                    OutlinedTextField(
                        value = tempClient,
                        onValueChange = { tempClient = it },
                        label = { Text("Client Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0284C7),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                    OutlinedTextField(
                        value = tempLocation,
                        onValueChange = { tempLocation = it },
                        label = { Text("Location") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0284C7),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        estimateTitle = tempTitle.trim()
                        projectName = tempProject.trim()
                        clientName = tempClient.trim()
                        location = tempLocation.trim()
                        showEditProjectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProjectDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

@Composable
private fun InfoFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$label :", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.width(110.dp))
        Text(
            text = value.ifBlank { "-" },
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TableCell(text: String, width: androidx.compose.ui.unit.Dp, isHeader: Boolean = false, isBold: Boolean = false) {
    Text(
        text = text,
        fontSize = if (isHeader) 11.sp else 11.5.sp,
        fontWeight = if (isHeader || isBold) FontWeight.Bold else FontWeight.Normal,
        color = if (isHeader) Color.White else Color(0xFF0F172A),
        modifier = Modifier.width(width)
    )
}
