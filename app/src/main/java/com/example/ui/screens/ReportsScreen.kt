package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ElectricalApp
import com.example.data.calculator.CalculatorRegistry
import com.example.data.database.ContractProjectEntity
import com.example.data.database.HistoryEntity
import com.example.pdf.PdfReportGenerator
import com.example.utils.ShareUtils
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ReportsScreen(
    onNavigateToCalculator: (Int) -> Unit,
    onNavigateToMaterialEstimate: () -> Unit,
    onNavigateToProject: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = remember { ElectricalApp.getApp(context) }
    val historyList by app.database.historyDao().getAllHistory().collectAsState(initial = emptyList())
    val buildingList by app.database.buildingProjectDao().getAllProjects().collectAsState(initial = emptyList())
    val projectList by app.projectRepository.allProjects.collectAsState(initial = emptyList())
    val techProfile by app.settingsManager.technicianProfileFlow.collectAsState(initial = com.example.data.datastore.TechnicianReportProfile())

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Calculations, 1 = Material Estimates, 2 = Projects
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("reports_screen_root")
    ) {
        // Tab Row (Calculations | Material Estimates | Project Contracts)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Calculations", "Material Estimates", "Project Contracts").forEachIndexed { index, tabTitle ->
                    val isSelected = selectedTab == index
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedTab = index },
                        color = if (isSelected) Color(0xFF0284C7) else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = tabTitle,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Search reports bar (Screen 5)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .height(42.dp)
                .clip(RoundedCornerShape(21.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(21.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search reports...",
                            fontSize = 12.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_reports_input")
                    )
                }
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        // List Content based on Selected Tab
        when (selectedTab) {
            0 -> {
                // Calculations Tab
                val filteredHistory = remember(historyList, searchQuery) {
                    if (searchQuery.isBlank()) historyList
                    else historyList.filter {
                        it.calculatorName.contains(searchQuery, ignoreCase = true) ||
                        it.primaryResult.contains(searchQuery, ignoreCase = true)
                    }
                }

                if (filteredHistory.isEmpty()) {
                    EmptyReportsPlaceholder(message = "এখনও কোনো ক্যালকুলেশন রিপোর্ট সংরক্ষিত নেই")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredHistory, key = { it.id }) { item ->
                            CalculationReportCard(
                                item = item,
                                onView = { onNavigateToCalculator(item.calculatorId) },
                                onPdf = {
                                    scope.launch {
                                        try {
                                            val calc = CalculatorRegistry.getById(item.calculatorId)
                                            if (calc != null) {
                                                val dummyResult = com.example.data.model.CalculationResult(
                                                    primaryValue = item.primaryResult,
                                                    primaryUnit = item.resultUnit,
                                                    formulaUsed = item.formula,
                                                    notes = if (item.notes.isNotBlank()) listOf(item.notes) else emptyList()
                                                )
                                                val file = PdfReportGenerator.generateCalculationPdf(
                                                    context = context,
                                                    calculator = calc,
                                                    inputs = mapOf("Summary" to item.inputSummary),
                                                    result = dummyResult,
                                                    techProfile = techProfile
                                                )
                                                ShareUtils.sharePdf(context, file, "Report - ${item.calculatorName}")
                                            } else {
                                                Toast.makeText(context, "Calculator not found", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "PDF Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onShare = {
                                    ShareUtils.shareText(
                                        context,
                                        item.calculatorName,
                                        "⚡ Electrical Report: ${item.calculatorName}\nফলাফল: ${item.primaryResult} ${item.resultUnit}\nতারিখ: ${item.formattedDate}\nসূত্র: ${item.formula}"
                                    )
                                }
                            )
                        }
                    }
                }
            }

            1 -> {
                // Material Estimates Tab
                val filteredBuildings = remember(buildingList, searchQuery) {
                    if (searchQuery.isBlank()) buildingList
                    else buildingList.filter {
                        it.projectName.contains(searchQuery, ignoreCase = true) ||
                        it.ownerName.contains(searchQuery, ignoreCase = true)
                    }
                }

                if (filteredBuildings.isEmpty()) {
                    EmptyReportsPlaceholder(message = "এখনও কোনো মালামাল এস্টিমেট রিপোর্ট তৈরি করা হয়নি")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredBuildings, key = { it.id }) { bProj ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFE0F2FE)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(bProj.projectName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                            Text("Owner: ${bProj.ownerName} • ${bProj.date}", fontSize = 11.5.sp, color = Color(0xFF64748B))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            onClick = onNavigateToMaterialEstimate,
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFF1F5F9),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(vertical = 6.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF0284C7))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("View", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                            }
                                        }
                                        Surface(
                                            onClick = {
                                                Toast.makeText(context, "Opening Material Estimate...", Toast.LENGTH_SHORT).show()
                                                onNavigateToMaterialEstimate()
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFEE2E2),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(vertical = 6.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFDC2626))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("PDF", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Project Contracts Tab
                if (projectList.isEmpty()) {
                    EmptyReportsPlaceholder(message = "এখনও কোনো প্রজেক্ট চুক্তি যুক্ত করা হয়নি")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(projectList, key = { it.id }) { proj ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = onNavigateToProject),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFE0F2FE)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(proj.projectName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                            Text("বাড়িওয়ালা: ${proj.clientName} • চুক্তি: ৳ ${String.format(Locale.US, "%,.0f", proj.contractAmount)}", fontSize = 11.5.sp, color = Color(0xFF64748B))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Surface(
                                            onClick = onNavigateToProject,
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFE0F2FE)
                                        ) {
                                            Text(
                                                text = "প্রজেক্টে যান ➔",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0284C7),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
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
}

@Composable
private fun CalculationReportCard(
    item: HistoryEntity,
    onView: () -> Unit,
    onPdf: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Icon, Title, Result, Standard Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE0F2FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.calculatorName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.primaryResult} ${item.resultUnit} • ${item.formattedDate}",
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFE0F2FE)
                ) {
                    Text(
                        text = "IEC",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons: View, PDF, Share (Exact Screen 5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // View Button
                Surface(
                    onClick = onView,
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF0284C7))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                    }
                }

                // PDF Button
                Surface(
                    onClick = onPdf,
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFEE2E2),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFDC2626))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    }
                }

                // Share Button
                Surface(
                    onClick = onShare,
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF475569))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyReportsPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0F2FE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "যেকোনো ক্যালকুলেশন সম্পন্ন করার পর Save বাটনে চাপ দিলে তা এখানে রিপোর্ট আকারে দেখতে পাবেন।",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
