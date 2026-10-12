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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ElectricalApp
import com.example.data.database.BuildingProjectEntity
import com.example.data.datastore.DeveloperReportSettings
import com.example.print.PrintManagerHelper
import com.example.utils.ShareUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MaterialEstimateRow(
    val sl: Int,
    var name: String,
    var spec: String,
    var brand: String,
    var unit: String,
    var qty: Double,
    var unitPrice: Double
) {
    val total: Double get() = qty * unitPrice
}

@Composable
fun MaterialEstimateScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = remember { ElectricalApp.getApp(context) }
    val devSettings by app.settingsManager.developerSettingsFlow.collectAsState(initial = DeveloperReportSettings())

    // Project Info State (Screen 6)
    var estimateTitle by remember { mutableStateOf("Factory Electrical Work") }
    var projectName by remember { mutableStateOf("KSM Poultry & Feed Ltd.") }
    var clientName by remember { mutableStateOf("KSM Group") }
    var location by remember { mutableStateOf("Dhaka, Bangladesh") }
    var preparedBy by remember { mutableStateOf(devSettings.developerName.ifEmpty { "Mohammad Rubel" }) }
    var contact by remember { mutableStateOf(devSettings.contactNumber.ifEmpty { "09876543210" }) }
    var notes by remember { mutableStateOf("High Quality Standards with NYY cable and Schneider switchgear") }
    val dateStr = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }

    // Materials Table State
    val materials = remember {
        mutableStateListOf(
            MaterialEstimateRow(1, "Cable 4C x 35 mm²", "NYY", "BRB", "m", 100.0, 450.0),
            MaterialEstimateRow(2, "MCCB 3P 63A", "-", "Schneider", "pcs", 2.0, 8500.0),
            MaterialEstimateRow(3, "DB Box 2", "-", "Hager", "pcs", 2.0, 3000.0)
        )
    }

    var showAddItemDialog by remember { mutableStateOf(false) }

    // Financial calculations
    val subtotal = materials.sumOf { it.total }
    val discountPercent = 5.0
    val discountAmount = subtotal * (discountPercent / 100.0)
    val vatPercent = 15.0
    val vatAmount = (subtotal - discountAmount) * (vatPercent / 100.0)
    val otherCost = 1000.0
    val grandTotal = (subtotal - discountAmount) + vatAmount + otherCost

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("material_estimate_screen_root"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Project Information Card (Screen 6)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Project Information",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(10.dp))

                InfoFieldRow("Estimate Title", estimateTitle) { estimateTitle = it }
                InfoFieldRow("Project Name", projectName) { projectName = it }
                InfoFieldRow("Client Name", clientName) { clientName = it }
                InfoFieldRow("Location", location) { location = it }
                InfoFieldRow("Date", dateStr, isReadOnly = true) {}
                InfoFieldRow("Prepared By", preparedBy) { preparedBy = it }
                InfoFieldRow("Contact", contact) { contact = it }
            }
        }

        // Materials Table Section (Screen 6)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Materials (${materials.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Button(
                        onClick = { showAddItemDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Item", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

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
                                    .width(620.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCell("SL", 30.dp, isHeader = true)
                                TableCell("Material Name", 150.dp, isHeader = true)
                                TableCell("Spec", 60.dp, isHeader = true)
                                TableCell("Brand", 80.dp, isHeader = true)
                                TableCell("Unit", 45.dp, isHeader = true)
                                TableCell("Qty", 55.dp, isHeader = true)
                                TableCell("Unit Price", 80.dp, isHeader = true)
                                TableCell("Total (৳)", 80.dp, isHeader = true)
                                TableCell("Del", 40.dp, isHeader = true)
                            }
                        }

                        // Table Rows
                        materials.forEachIndexed { index, item ->
                            val bg = if (index % 2 == 0) Color(0xFFF8FAFC) else Color.White
                            Surface(color = bg) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                        .width(620.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TableCell("${index + 1}", 30.dp)
                                    TableCell(item.name, 150.dp, isBold = true)
                                    TableCell(item.spec, 60.dp)
                                    TableCell(item.brand, 80.dp)
                                    TableCell(item.unit, 45.dp)
                                    TableCell(String.format(Locale.US, "%.0f", item.qty), 55.dp)
                                    TableCell("৳ ${String.format(Locale.US, "%,.0f", item.unitPrice)}", 80.dp)
                                    TableCell("৳ ${String.format(Locale.US, "%,.0f", item.total)}", 80.dp, isBold = true)
                                    IconButton(
                                        onClick = { materials.removeAt(index) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Financial Breakdown Card (Screen 6)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                FinancialRow("Subtotal", "৳ ${String.format(Locale.US, "%,.0f", subtotal)}")
                FinancialRow("Discount (5%)", "৳ ${String.format(Locale.US, "%,.0f", discountAmount)}")
                FinancialRow("VAT (15%)", "৳ ${String.format(Locale.US, "%,.0f", vatAmount)}")
                FinancialRow("Other Cost", "৳ ${String.format(Locale.US, "%,.0f", otherCost)}")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFCBD5E1))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Grand Total", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                    Text(
                        "৳ ${String.format(Locale.US, "%,.0f", grandTotal)}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0284C7)
                    )
                }
            }
        }

        // Bottom Action Buttons (Screen 6: Save, PDF, Print, Share)
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
                                preparedBy = preparedBy,
                                date = dateStr,
                                totalPoints = materials.size,
                                floors = 1,
                                rooms = 1,
                                materialsJson = "Estimate: ৳ ${String.format(Locale.US, "%,.0f", grandTotal)}"
                            )
                        )
                        Toast.makeText(context, "এস্টিমেট সফলভাবে সেভ হয়েছে", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0284C7),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // PDF
            Surface(
                onClick = {
                    Toast.makeText(context, "Generating PDF...", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFDC2626),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Print
            Surface(
                onClick = {
                    Toast.makeText(context, "Connecting Printer...", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2563EB),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Share
            Surface(
                onClick = {
                    val shareText = buildString {
                        appendLine("📋 $estimateTitle")
                        appendLine("Project: $projectName | Client: $clientName")
                        appendLine("Location: $location | Date: $dateStr")
                        appendLine("Total Estimated: ৳ ${String.format(Locale.US, "%,.0f", grandTotal)}")
                    }
                    ShareUtils.shareText(context, estimateTitle, shareText)
                },
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
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

    // Add Item Dialog
    if (showAddItemDialog) {
        var mName by remember { mutableStateOf("") }
        var mSpec by remember { mutableStateOf("-") }
        var mBrand by remember { mutableStateOf("") }
        var mUnit by remember { mutableStateOf("pcs") }
        var mQty by remember { mutableStateOf("1") }
        var mPrice by remember { mutableStateOf("0") }

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text("নতুন মালামাল যোগ করুন", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = mName, onValueChange = { mName = it }, label = { Text("Material Name") }, singleLine = true)
                    OutlinedTextField(value = mSpec, onValueChange = { mSpec = it }, label = { Text("Specification") }, singleLine = true)
                    OutlinedTextField(value = mBrand, onValueChange = { mBrand = it }, label = { Text("Brand") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = mQty, onValueChange = { mQty = it }, label = { Text("Qty") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = mUnit, onValueChange = { mUnit = it }, label = { Text("Unit") }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = mPrice, onValueChange = { mPrice = it }, label = { Text("Unit Price (৳)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (mName.isNotBlank()) {
                            materials.add(
                                MaterialEstimateRow(
                                    sl = materials.size + 1,
                                    name = mName,
                                    spec = mSpec,
                                    brand = mBrand,
                                    unit = mUnit,
                                    qty = mQty.toDoubleOrNull() ?: 1.0,
                                    unitPrice = mPrice.toDoubleOrNull() ?: 0.0
                                )
                            )
                            showAddItemDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("যোগ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun InfoFieldRow(label: String, value: String, isReadOnly: Boolean = false, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$label :", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.width(100.dp))
        Text(
            text = value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0F172A),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TableCell(text: String, width: androidx.compose.ui.unit.Dp, isHeader: Boolean = false, isBold: Boolean = false) {
    Text(
        text = text,
        fontSize = if (isHeader) 10.5.sp else 11.sp,
        fontWeight = if (isHeader || isBold) FontWeight.Bold else FontWeight.Normal,
        color = if (isHeader) Color.White else Color(0xFF0F172A),
        modifier = Modifier.width(width)
    )
}

@Composable
private fun FinancialRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color(0xFF64748B))
        Text(value, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
    }
}
