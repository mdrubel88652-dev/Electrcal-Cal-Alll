package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.mutableIntStateOf
import com.example.ElectricalApp
import com.example.data.database.ContractProjectEntity
import com.example.data.database.ProjectPaymentEntity
import com.example.data.database.QuickTaskEntity
import com.example.pdf.PdfReportGenerator
import com.example.print.PrintManagerHelper
import com.example.utils.ShareUtils
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MAX_PROJECTS = 5

@Composable
fun ProjectScreen() {
    val context = LocalContext.current
    val app = remember { ElectricalApp.getApp(context) }
    val currentLanguage by app.settingsManager.languageFlow.collectAsState(initial = "EN")
    val isEn = currentLanguage == "EN"
    val repository = remember { app.projectRepository }
    val scope = rememberCoroutineScope()

    val projects by repository.allProjects.collectAsState(initial = emptyList())
    val techProfile by app.settingsManager.technicianProfileFlow.collectAsState(initial = com.example.data.datastore.TechnicianReportProfile())

    var showAddProjectDialog by remember { mutableStateOf(false) }
    var selectedProjectForPayment by remember { mutableStateOf<ContractProjectEntity?>(null) }
    var projectToDelete by remember { mutableStateOf<ContractProjectEntity?>(null) }

    val projectCount = projects.size
    val isSlotFull = projectCount >= MAX_PROJECTS

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("project_screen_root")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("projects_list"),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ==========================================
            // 1. SCROLLABLE PROJECT HEADER CARD (CLEAN APP THEME)
            // ==========================================
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF4F9),
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE0F2FE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Assignment,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "Contract Project Manager",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = if (isEn) "Site Wiring, Quotations & Payment Receipts" else "সাইট ওয়্যারিং, কোটেশন ও কিস্তি রসিদ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            // Project Slot Counter Pill
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSlotFull) Color(0xFFFEE2E2) else Color(0xFFE0F2FE)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                if (isSlotFull) Color(0xFFEF4444) else Color(0xFF10B981),
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "$projectCount/$MAX_PROJECTS ${if (isEn) "Slots" else "স্লট"}",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSlotFull) Color(0xFFDC2626) else Color(0xFF0284C7)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar of Slots
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isSlotFull) {
                                        if (isEn) "Max 5 project slots full" else "সর্বোচ্চ ৫টি প্রজেক্ট স্লট পূর্ণ"
                                    } else {
                                        if (isEn) "Available slots: ${MAX_PROJECTS - projectCount} remaining" else "উপলব্ধ স্লট: ${MAX_PROJECTS - projectCount} টি খালি"
                                    },
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = if (isEn) "Capacity: 5 Projects" else "ধারণক্ষমতা: ৫টি প্রজেক্ট",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LinearProgressIndicator(
                                progress = { projectCount.toFloat() / MAX_PROJECTS.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (isSlotFull) Color(0xFFEF4444) else Color(0xFF0284C7),
                                trackColor = Color(0xFFF1F5F9)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 2. ACTION ROW (SCROLLABLE)
            // ==========================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "My Projects ($projectCount)" else "আমার প্রজেক্টসমূহ ($projectCount)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Button(
                        onClick = {
                            if (isSlotFull) {
                                Toast.makeText(
                                    context,
                                    if (isEn) "Maximum 5 projects allowed. Delete an existing project to add a new one."
                                    else "সর্বোচ্চ ৫টি প্রজেক্ট সংরক্ষণ করা যাবে। নতুন প্রজেক্ট যোগ করতে পূর্বের কোনো প্রজেক্ট ডিলিট করুন।",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                showAddProjectDialog = true
                            }
                        },
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("add_project_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSlotFull) Color(0xFF94A3B8) else Color(0xFF0284C7),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEn) "New Project" else "নতুন প্রজেক্ট",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ==========================================
            // 3. PROJECT ITEMS OR EMPTY STATE
            // ==========================================
            if (projects.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = if (isEn) "No projects added yet" else "এখনও কোনো প্রজেক্ট যুক্ত করা হয়নি",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (isEn) "As an electrical technician/contractor, manage your site projects, total contracts, and installment payments by tapping 'New Project' (up to 5 projects)."
                                else "একজন টেকনিশিয়ান হিসেবে বাড়িওয়ালার সাথে আপনার চুক্তিকৃত প্রজেক্ট, মোট টাকার পরিমাণ এবং কিস্তিতে নেওয়া টাকার হিসাব পরিচালনা করতে 'নতুন প্রজেক্ট' বাটনে চাপ দিন (সর্বোচ্চ ৫টি প্রজেক্ট)।",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }
            } else {
                items(
                    items = projects,
                    key = { it.id }
                ) { project ->
                    val paymentsFlow = remember(project.id) { repository.getPaymentsForProject(project.id) }
                    val payments by paymentsFlow.collectAsState(initial = emptyList())

                    ProjectCard(
                        project = project,
                        payments = payments,
                        onAddPayment = {
                            selectedProjectForPayment = project
                        },
                        onDeleteProject = {
                            projectToDelete = project
                        },
                        onDeletePayment = { paymentId ->
                            scope.launch {
                                repository.deletePayment(paymentId)
                                Toast.makeText(context, "কিস্তির রেকর্ড মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onGeneratePdf = {
                            scope.launch {
                                try {
                                    val pdfFile = PdfReportGenerator.generateProjectContractPdf(
                                        context = context,
                                        project = project,
                                        payments = payments,
                                        techProfile = techProfile
                                    )
                                    Toast.makeText(context, "PDF সফলভাবে তৈরি হয়েছে: ${pdfFile.name}", Toast.LENGTH_SHORT).show()
                                    ShareUtils.sharePdf(context, pdfFile, "Project Contract - ${project.projectName}")
                                } catch (e: Exception) {
                                    Toast.makeText(context, "PDF তৈরিতে সমস্যা: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onPrint = {
                            scope.launch {
                                try {
                                    val pdfFile = PdfReportGenerator.generateProjectContractPdf(
                                        context = context,
                                        project = project,
                                        payments = payments,
                                        techProfile = techProfile
                                    )
                                    PrintManagerHelper.printPdfFile(context, pdfFile, "Project_${project.id}")
                                } catch (e: Exception) {
                                    Toast.makeText(context, "প্রিন্ট এরর: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onShare = {
                            val totalReceived = payments.sumOf { it.amount }
                            val due = project.contractAmount - totalReceived
                            val summary = buildString {
                                appendLine("📋 প্রজেক্ট স্টেটমেন্ট: ${project.projectName}")
                                if (project.clientName.isNotBlank()) appendLine("বাড়িওয়ালা: ${project.clientName}")
                                if (project.location.isNotBlank()) appendLine("লোকেশন: ${project.location}")
                                if (project.contactPhone.isNotBlank()) appendLine("মোবাইল: ${project.contactPhone}")
                                appendLine("মোট চুক্তি: ৳ ${String.format(Locale.US, "%,.2f", project.contractAmount)}")
                                appendLine("মোট আদায়: ৳ ${String.format(Locale.US, "%,.2f", totalReceived)}")
                                appendLine("বকেয়া টাকা: ৳ ${String.format(Locale.US, "%,.2f", maxOf(0.0, due))}")
                                appendLine("অবস্থা: ${if (due <= 0) "পরিশোধিত ও সমাপ্ত (COMPLETED)" else "চলমান প্রজেক্ট (RUNNING)"}")
                                appendLine("")
                                appendLine("আদায়কৃত কিস্তিসমূহ:")
                                if (payments.isEmpty()) {
                                    appendLine("এখনও কোনো কিস্তি জমা হয়নি।")
                                } else {
                                    payments.sortedBy { it.timestamp }.forEachIndexed { idx, p ->
                                        appendLine("${idx + 1}. ${p.paymentDate}: ৳ ${String.format(Locale.US, "%,.2f", p.amount)} (${p.note})")
                                    }
                                }
                                appendLine("")
                                appendLine("Electrical Pro Suite থেকে প্রেরিত।")
                            }
                            ShareUtils.shareText(context, "Project Contract - ${project.projectName}", summary)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }

    // Add Project Dialog
    if (showAddProjectDialog) {
        AddProjectDialog(
            onDismiss = { showAddProjectDialog = false },
            onSave = { newProject ->
                scope.launch {
                    repository.saveProject(newProject)
                    showAddProjectDialog = false
                    Toast.makeText(context, "প্রজেক্ট সফলভাবে যুক্ত করা হয়েছে", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Add Payment Dialog
    selectedProjectForPayment?.let { proj ->
        AddPaymentDialog(
            project = proj,
            onDismiss = { selectedProjectForPayment = null },
            onSave = { payment ->
                scope.launch {
                    repository.addPayment(payment)
                    selectedProjectForPayment = null
                    Toast.makeText(context, "টাকা জমার কিস্তি সফলভাবে যুক্ত হয়েছে", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Delete Confirmation Dialog
    projectToDelete?.let { proj ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = {
                Text("প্রজেক্ট মুছে ফেলবেন?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("আপনি কি নিশ্চিত যে '${proj.projectName}' প্রজেক্ট এবং এর সাথে যুক্ত সকল পেমেন্ট হিস্ট্রি মুছে ফেলতে চান? এতে একটি প্রজেক্ট স্লট খালি হবে।")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.deleteProject(proj.id)
                            projectToDelete = null
                            Toast.makeText(context, "প্রজেক্ট সফলভাবে মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("মুছে ফেলুন", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun ProjectCard(
    project: ContractProjectEntity,
    payments: List<ProjectPaymentEntity>,
    onAddPayment: () -> Unit,
    onDeleteProject: () -> Unit,
    onDeletePayment: (Long) -> Unit,
    onGeneratePdf: () -> Unit,
    onPrint: () -> Unit,
    onShare: () -> Unit
) {
    val totalReceived = payments.sumOf { it.amount }
    val remainingDue = project.contractAmount - totalReceived
    val isPaidInFull = remainingDue <= 0.0
    val progressFraction = if (project.contractAmount > 0) {
        (totalReceived / project.contractAmount).coerceIn(0.0, 1.0).toFloat()
    } else 0f
    val percentInt = (progressFraction * 100).toInt()

    var showHistory by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_card_${project.id}")
            .clip(RoundedCornerShape(12.dp))
            .animateContentSize(animationSpec = tween(200)),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFEFF4F9),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Project Title & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.projectName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (project.clientName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "বাড়িওয়ালা: ${project.clientName}",
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }

                // Completed or Running Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isPaidInFull) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPaidInFull) Icons.Default.CheckCircle else Icons.Default.Payments,
                            contentDescription = null,
                            tint = if (isPaidInFull) Color(0xFF059669) else Color(0xFFD97706),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPaidInFull) "প্রজেক্ট সমাপ্ত (Paid)" else "চলমান ($percentInt%)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPaidInFull) Color(0xFF065F46) else Color(0xFF92400E)
                        )
                    }
                }
            }

            // Location, Phone, Email details
            Spacer(modifier = Modifier.height(6.dp))
            if (project.location.isNotBlank() || project.contactPhone.isNotBlank() || project.emailAddress.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (project.location.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = project.location, fontSize = 11.5.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    if (project.contactPhone.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = project.contactPhone, fontSize = 11.5.sp, color = Color(0xFF64748B))
                        }
                    }
                    if (project.emailAddress.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = project.emailAddress, fontSize = 11.5.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial Metrics Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("মোট চুক্তি", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text(
                            text = "৳ ${String.format(Locale.US, "%,.0f", project.contractAmount)}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("মোট আদায়", fontSize = 10.sp, color = Color(0xFF059669))
                        Text(
                            text = "৳ ${String.format(Locale.US, "%,.0f", totalReceived)}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF059669)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(if (isPaidInFull) "অবস্থা" else "বকেয়া বাকি", fontSize = 10.sp, color = if (isPaidInFull) Color(0xFF059669) else Color(0xFFDC2626))
                        Text(
                            text = if (isPaidInFull) "০ (সম্পূর্ণ)" else "৳ ${String.format(Locale.US, "%,.0f", maxOf(0.0, remainingDue))}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPaidInFull) Color(0xFF059669) else Color(0xFFDC2626)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Payment Progress Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isPaidInFull) Color(0xFF10B981) else Color(0xFF0284C7),
                    trackColor = Color(0xFFE2E8F0)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isPaidInFull) "🎉 প্রজেক্টের সম্পূর্ণ টাকা পরিশোধিত ও চুক্তি শেষ!" else "মূল টাকা থেকে কিস্তি সমন্বয় চলছে",
                        fontSize = 10.sp,
                        fontWeight = if (isPaidInFull) FontWeight.Bold else FontWeight.Normal,
                        color = if (isPaidInFull) Color(0xFF059669) else Color(0xFF64748B)
                    )
                    Text(
                        text = "$percentInt% পরিশোধিত",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaidInFull) Color(0xFF059669) else Color(0xFF0284C7)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: Add Payment & Toggle History
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onAddPayment,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0284C7),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "টাকা জমা নিন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showHistory = !showHistory },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (showHistory) "হিস্ট্রি লুকান" else "কিস্তি হিস্ট্রি (${payments.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = if (showHistory) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Expanded Payment History List
            AnimatedVisibility(visible = showHistory) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ধারাবাহিক কিস্তি আদায়ের তালিকা:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (payments.isEmpty()) {
                        Text(
                            text = "এখনও কোনো কিস্তির টাকা জমা দেওয়া হয়নি। 'টাকা জমা নিন' বাটনে চাপ দিয়ে কিস্তি রেকর্ড করুন।",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            payments.sortedByDescending { it.timestamp }.forEachIndexed { idx, pmt ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "৳ ${String.format(Locale.US, "%,.0f", pmt.amount)}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF059669)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = pmt.paymentDate,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                            if (pmt.note.isNotBlank()) {
                                                Text(
                                                    text = pmt.note,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF475569),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { onDeletePayment(pmt.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Payment",
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Row: PDF, Print, Share, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // PDF Button
                    Surface(
                        onClick = onGeneratePdf,
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF0FDF4)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF ভাউচার", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                        }
                    }

                    // Print Button
                    Surface(
                        onClick = onPrint,
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFEFF6FF)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("প্রিন্ট", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                        }
                    }

                    // Share Button
                    Surface(
                        onClick = onShare,
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("শেয়ার", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                        }
                    }
                }

                // Delete Project Button
                IconButton(
                    onClick = onDeleteProject,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Project",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddProjectDialog(
    onDismiss: () -> Unit,
    onSave: (ContractProjectEntity) -> Unit
) {
    var projectName by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var contractAmountStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val currentDateStr = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x990A192F))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // Top Drag Handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(44.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFCBD5E1))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PostAdd,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "নতুন প্রজেক্ট কন্ট্রাক্ট যোগ করুন",
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "প্রয়োজনীয় তথ্য পূরণ করুন",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. প্রজেক্টের নাম *
                    ProjectFieldInputCard(
                        icon = Icons.Default.Business,
                        label = "প্রজেক্টের নাম",
                        isRequired = true,
                        placeholder = "যেমন: ৩ তলা ভবন ওয়্যারিং",
                        value = projectName,
                        onValueChange = { projectName = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. বাড়িওয়ালা / ক্লায়েন্টের নাম
                    ProjectFieldInputCard(
                        icon = Icons.Default.Person,
                        label = "বাড়িওয়ালা / ক্লায়েন্টের নাম",
                        isRequired = false,
                        placeholder = "বাড়িওয়ালা বা ক্লায়েন্টের নাম লিখুন",
                        value = clientName,
                        onValueChange = { clientName = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. লোকেশন / সাইট ঠিকানা
                    ProjectFieldInputCard(
                        icon = Icons.Default.LocationOn,
                        label = "লোকেশন / সাইট ঠিকানা",
                        isRequired = false,
                        placeholder = "সাইটের ঠিকানা লিখুন",
                        value = location,
                        onValueChange = { location = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. মোবাইল নম্বর & চুক্তির টাকা (৳) *
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ProjectFieldInputCard(
                                icon = Icons.Default.Phone,
                                label = "মোবাইল নম্বর",
                                isRequired = false,
                                placeholder = "০১XXXXXXXXX",
                                value = contactPhone,
                                onValueChange = { contactPhone = it },
                                keyboardType = KeyboardType.Phone
                            )
                        }

                        Box(modifier = Modifier.weight(1.05f)) {
                            ProjectFieldInputCard(
                                icon = Icons.Default.Payments,
                                label = "চুক্তির টাকা (৳)",
                                isRequired = true,
                                placeholder = "টাকার পরিমাণ লিখুন",
                                value = contractAmountStr,
                                onValueChange = { contractAmountStr = it },
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5. ইমেইল অ্যাড্রেস (ঐচ্ছিক)
                    ProjectFieldInputCard(
                        icon = Icons.Default.Email,
                        label = "ইমেইল অ্যাড্রেস (ঐচ্ছিক)",
                        isRequired = false,
                        placeholder = "example@domain.com",
                        value = emailAddress,
                        onValueChange = { emailAddress = it },
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 6. বিশেষ নোট বা শর্তাবলী (ঐচ্ছিক)
                    ProjectFieldInputCard(
                        icon = Icons.Default.Description,
                        label = "বিশেষ নোট বা শর্তাবলী (ঐচ্ছিক)",
                        isRequired = false,
                        placeholder = "যেকোনো অতিরিক্ত তথ্য লিখুন...",
                        value = notes,
                        onValueChange = { notes = it }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Bottom Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "বাতিল",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                        }

                        Button(
                            onClick = {
                                val amount = contractAmountStr.toDoubleOrNull() ?: 0.0
                                if (projectName.isBlank() || amount <= 0.0) {
                                    return@Button
                                }
                                val project = ContractProjectEntity(
                                    projectName = projectName.trim(),
                                    clientName = clientName.trim(),
                                    location = location.trim(),
                                    contactPhone = contactPhone.trim(),
                                    emailAddress = emailAddress.trim(),
                                    contractAmount = amount,
                                    startDate = currentDateStr,
                                    notes = notes.trim()
                                )
                                onSave(project)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "সংরক্ষণ করুন",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ProjectFieldInputCard(
    icon: ImageVector,
    label: String,
    isRequired: Boolean,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFEFF4F9)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    if (isRequired) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "*",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 11.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                        textStyle = TextStyle(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun AddPaymentDialog(
    project: ContractProjectEntity,
    onDismiss: () -> Unit,
    onSave: (ProjectPaymentEntity) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val currentDateStr = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("টাকা জমার কিস্তি যুক্ত করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "প্রজেক্ট: ${project.projectName}",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0284C7)
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("আদায়কৃত টাকার পরিমাণ (৳) *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("কিসের বাবদ টাকা নিলেন (যেমন: ১ম কিস্তি পাইপ টানা)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "তারিখ: $currentDateStr",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (amount <= 0.0) return@Button
                    val payment = ProjectPaymentEntity(
                        projectId = project.id,
                        amount = amount,
                        paymentDate = currentDateStr,
                        note = if (note.isBlank()) "কিস্তি পরিশোধ" else note.trim()
                    )
                    onSave(payment)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
            ) {
                Text("জমা দিন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
