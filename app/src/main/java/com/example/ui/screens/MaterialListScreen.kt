package com.example.ui.screens

import android.widget.Toast
import java.util.Locale
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ElectricalApp
import com.example.data.model.CableCoilSummaryItem
import com.example.data.model.FloorCableSummary
import com.example.data.model.RoomCableResult
import com.example.pdf.PdfReportGenerator
import com.example.print.PrintManagerHelper
import com.example.ui.theme.ElectricBluePrimary
import com.example.ui.theme.LevelAdvanceColor
import com.example.ui.theme.LevelBasicColor
import com.example.ui.theme.LevelLowerColor
import com.example.utils.ShareUtils

@Composable
fun MaterialListScreen() {
    val context = LocalContext.current
    val app = ElectricalApp.getApp(context)
    val devSettings by app.settingsManager.developerSettingsFlow.collectAsState(initial = com.example.data.datastore.DeveloperReportSettings())
    val language by app.settingsManager.languageFlow.collectAsState(initial = "EN")
    val isEn = language == "EN"

    val project = BuildingMaterialStateHolder.currentProject
    val materials = BuildingMaterialStateHolder.currentMaterials
    val cableResult = BuildingMaterialStateHolder.currentCableResult ?: project?.cableCalculationResult

    if (project == null || cableResult == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (isEn) "No building cable calculation data available. Please run calculation first."
                else "কোনো ক্যাবল সাইজিং হিসাবের তথ্য পাওয়া যায়নি। অনুগ্রহ করে প্রথমে হিসাব সম্পন্ন করুন।",
                color = Color(0xFF64748B),
                fontSize = 14.sp
            )
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
        // ==========================================
        // 1. PROJECT & REPORT HEADER CARD
        // ==========================================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEn) "HOUSE WIRING CABLE SIZING & COIL SCHEDULE" else "হাউস ওয়্যারিং ক্যাবল সাইজিং ও কয়েল হিসাব",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ElectricBluePrimary
                            )
                            Text(
                                text = "BNBC 2020 / IEC 60364-5-52 Verified House Wiring Cable Schedule",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text("Project: ${project.projectName}", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            Text("Owner: ${project.ownerName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Site: ${project.address}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("DB Location: ${project.mainDbLocation}", fontSize = 11.5.sp, color = Color(0xFF0369A1))
                        }
                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(0.9f)) {
                            Text("Date: ${project.date}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("Floors: ${project.floors} | Rooms: ${project.rooms}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Supply: ${project.supplyType.label.take(13)}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = ElectricBluePrimary)
                            Text("Coil Length: ${project.coilLengthMeters}m / roll", fontSize = 11.5.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ==========================================
        // 2. ACTION BUTTONS (PRINT, SAVE PDF, SHARE)
        // ==========================================
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
                    Text(if (isEn) "Print" else "প্রিন্ট", fontSize = 13.sp)
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
                    Text(if (isEn) "Save PDF" else "সেভ PDF", fontSize = 13.sp)
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
                    Text(if (isEn) "Share" else "শেয়ার", fontSize = 13.sp)
                }
            }
        }

        // ==========================================
        // CABLES, COILS, FEEDERS & ROOM AUDIT SCHEDULE
        // ==========================================
        // Summary KPI Cards (Load, Feeder, Sub-main, Coils)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiSummaryCard(
                        title = if (isEn) "Connected Load" else "মোট লোড",
                        value = "${String.format(Locale.US, "%.1f", cableResult.totalConnectedLoadKw)} kW",
                        subtitle = "${cableResult.totalFloors} Floors, ${cableResult.totalRooms} Rooms",
                        accentColor = ElectricBluePrimary,
                        modifier = Modifier.weight(1f)
                    )
                    KpiSummaryCard(
                        title = if (isEn) "Main Feeder" else "মেইন ফিডার",
                        value = "${cableResult.mainServiceFeederSizeMm2} mm²",
                        subtitle = "${cableResult.mainFeederLengthMeters.toInt()}m (${cableResult.mainMcbRatingA}A MCB)",
                        accentColor = Color(0xFF0D9488),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val totalCoilsCount = cableResult.cableSummaries.sumOf { it.totalCoilsRequired }
                    KpiSummaryCard(
                        title = if (isEn) "Total Cable Coils" else "মোট ক্যাবল কয়েল",
                        value = "$totalCoilsCount Coils",
                        subtitle = "Roll length: ${cableResult.configuredCoilLengthMeters}m",
                        accentColor = Color(0xFFD97706),
                        modifier = Modifier.weight(1f)
                    )
                    KpiSummaryCard(
                        title = if (isEn) "Sub-Main Feeder" else "সাব-মেইন রাইজার",
                        value = "${cableResult.subMainFeederSizeMm2} mm²",
                        subtitle = "${cableResult.subMainTotalMeters.toInt()}m vertical riser",
                        accentColor = Color(0xFF4338CA),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Cable Sizing & Coils Table Header
            item {
                Text(
                    text = if (isEn) "House Wiring Cable Sizing & Coil Requirements" else "ক্যাবল সাইজ ও কয়েল হিসাব (BNBC / IEC 60364)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Cards for every Cable Size (1.5mm², 2.5mm², 4.0mm², 6.0mm², 10.0mm², 16.0mm²)
            items(cableResult.cableSummaries, key = { it.cableSizeMm2 }) { cable ->
                CableCoilSummaryCard(cable = cable, isEn = isEn)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Floor-by-Floor & Room Breakdown Section
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isEn) "Floor & Room Cable Audit Breakdown" else "ফ্লোর ও রুম অনুযায়ী বিস্তারিত ক্যাবল হিসাব",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            items(cableResult.floorSummaries, key = { it.floorNumber }) { floor ->
                FloorSummaryCard(
                    floor = floor,
                    rooms = cableResult.roomResults.filter { it.floorNumber == floor.floorNumber },
                    isEn = isEn
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Engineering Notes & Standards Card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = ElectricBluePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Engineering Formulas & Compliance Notes" else "ইঞ্জিনিয়ারিং সূত্র ও স্ট্যান্ডার্ড রেফারেন্স",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        cableResult.calculationNotes.forEach { note ->
                            Text(
                                text = "• $note",
                                fontSize = 11.sp,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = Color(0xFFCBD5E1))
                        Spacer(modifier = Modifier.height(6.dp))

                        cableResult.standardReferences.forEach { ref ->
                            Text(
                                text = "✓ $ref",
                                fontSize = 10.5.sp,
                                color = Color(0xFF0369A1),
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Report Signatures & Verification Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Prepared By: ${project.preparedBy}", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Owner Signature: _______________________", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Note: Calculated strictly per BNBC 2020 Part 8 and IEC 60364-5-52. All coil requirements represent full single-conductor rolls (Red Phase, Black Neutral, Green Earth) with ceiling formula rounding to prevent on-site shortages.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
}

@Composable
private fun KpiSummaryCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.5.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun CableCoilSummaryCard(
    cable: CableCoilSummaryItem,
    isEn: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                        color = ElectricBluePrimary,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "${cable.cableSizeMm2} mm²",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.5.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Column {
                        Text(cable.purpose, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                        Text(cable.insulationType, fontSize = 10.5.sp, color = Color(0xFF64748B))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = "${cable.totalCoilsRequired} Coils",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(6.dp))

            // Breakdown of Phase, Neutral, Earth
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Phase (Red)", fontSize = 10.5.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                    Text("${cable.phaseConductorMeters.toInt()}m (${cable.phaseCoilsRequired} coils)", fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Neutral (Black)", fontSize = 10.5.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
                    Text("${cable.neutralConductorMeters.toInt()}m (${cable.neutralCoilsRequired} coils)", fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Earth (Green)", fontSize = 10.5.sp, color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                    Text("${cable.earthConductorMeters.toInt()}m (${cable.earthCoilsRequired} coils)", fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Total Conductor Wire: ${cable.totalConductorMeters.toInt()} meters • ${cable.formulaExplanation}",
                fontSize = 10.5.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun FloorSummaryCard(
    floor: FloorCableSummary,
    rooms: List<RoomCableResult>,
    isEn: Boolean
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE0F2FE),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Floor ${floor.floorNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = ElectricBluePrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Column {
                        Text("${floor.roomCount} Rooms • ${(floor.totalConnectedWatts / 1000.0).format(1)} kW", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Sub-Main: ${floor.subMainFeederSizeMm2} mm² Feeder (${floor.subMainFeederMeters.toInt()}m)", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${floor.totalCoils} Coils", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(6.dp))

                    rooms.forEach { room ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(room.roomName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("Points: ${room.totalPoints} (${room.connectedWatts.toInt()}W)", fontSize = 10.5.sp, color = Color(0xFF64748B))
                            }
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1.2f)) {
                                Text("1.5mm²: ${room.cable1_5Mm2Meters.toInt()}m | 2.5mm²: ${room.cable2_5Mm2Meters.toInt()}m", fontSize = 10.5.sp)
                                if (room.cable4_0Mm2Meters > 0) {
                                    Text("4.0mm² (AC): ${room.cable4_0Mm2Meters.toInt()}m", fontSize = 10.5.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF8FAFC))
                    }
                }
            }
        }
    }
}

private fun Double.format(digits: Int): String {
    return String.format(Locale.US, "%.${digits}f", this)
}
