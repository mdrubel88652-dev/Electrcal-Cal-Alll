package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ElectricalApp
import com.example.data.database.BuildingProjectEntity
import com.example.pdf.PdfReportGenerator
import com.example.print.PrintManagerHelper
import com.example.ui.theme.ElectricBluePrimary
import com.example.ui.theme.LevelAdvanceColor
import com.example.ui.theme.LevelBasicColor
import com.example.ui.theme.LevelLowerColor
import com.example.utils.ShareUtils
import kotlinx.coroutines.launch

@Composable
fun MaterialListScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val app = ElectricalApp.getApp(context)
    val devSettings by app.settingsManager.developerSettingsFlow.collectAsState(initial = com.example.data.datastore.DeveloperReportSettings())

    val project = BuildingMaterialStateHolder.currentProject
    val materials = BuildingMaterialStateHolder.currentMaterials

    if (project == null || materials.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No building project material data available. Please run calculation first.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
            .testTag("material_list_screen")
    ) {
        // Report Title & Project Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ELECTRICAL MATERIAL REQUIREMENT LIST",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ElectricBluePrimary
                    )
                    Text(
                        text = "Electrical Engineering Calculation & Estimation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Project: ${project.projectName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Owner: ${project.ownerName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Address: ${project.address}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Date: ${project.date}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("Floors: ${project.floors} | Rooms: ${project.rooms}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${materials.size} Items Total", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElectricBluePrimary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Action Buttons Row (Print, Save PDF, Share PDF)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Print Button (Blue)
                Button(
                    onClick = {
                        try {
                            val pdf = PdfReportGenerator.generateMaterialListPdf(context, project, materials, devSettings)
                            PrintManagerHelper.printPdfFile(context, pdf, "Material List - ${project.projectName}")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Print error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LevelAdvanceColor)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print", fontSize = 13.sp)
                }

                // Save PDF (Green)
                Button(
                    onClick = {
                        try {
                            val pdf = PdfReportGenerator.generateMaterialListPdf(context, project, materials, devSettings)
                            Toast.makeText(context, "PDF saved: ${pdf.name}", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "PDF error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LevelBasicColor)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save PDF", fontSize = 13.sp)
                }

                // Share PDF (Purple)
                Button(
                    onClick = {
                        try {
                            val pdf = PdfReportGenerator.generateMaterialListPdf(context, project, materials, devSettings)
                            ShareUtils.sharePdf(context, pdf, "Electrical Material Requirement List - ${project.projectName}")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LevelLowerColor)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 13.sp)
                }
            }
        }

        // Table Header
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ElectricBluePrimary,
                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("No.", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(28.dp))
                    Text("Material", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.4f))
                    Text("Specification", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.5f))
                    Text("Unit", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.9f))
                    Text("Qty", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(36.dp))
                }
            }
        }

        // Table Rows
        items(materials, key = { it.no }) { item ->
            val isEven = item.no % 2 == 0
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (isEven) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${item.no}", fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(28.dp))
                    Column(modifier = Modifier.weight(1.4f)) {
                        Text(item.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        if (item.brand.isNotEmpty()) {
                            Text(item.brand, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(item.specification, fontSize = 11.sp, modifier = Modifier.weight(1.5f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(item.unit, fontSize = 11.sp, modifier = Modifier.weight(0.9f))
                    Text("${item.quantity}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = ElectricBluePrimary, modifier = Modifier.width(36.dp))
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        }

        // Table Footer & Signatures
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Prepared By: ${project.preparedBy}", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Owner Signature: _______________________", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Note: This is an engineering material requirement list for the building owner to purchase. No material prices, labour charges, or billing are included.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
