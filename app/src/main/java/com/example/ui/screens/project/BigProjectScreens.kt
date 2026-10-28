package com.example.ui.screens.project

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.database.ContractProjectEntity
import com.example.data.database.ExpenseEntity
import com.example.data.database.ProjectPaymentEntity
import com.example.pdf.PdfReportGenerator

// --- 1. Big Project List Screen (Matches Screen 5) ---
@Composable
fun BigProjectListScreen(
    projects: List<ContractProjectEntity>,
    allPayments: List<ProjectPaymentEntity>,
    onBackClick: () -> Unit,
    onAddProjectClick: () -> Unit,
    onProjectClick: (Long) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filterTabs = listOf("All", "Ongoing", "Completed", "Pending")

    val filteredProjects = remember(projects, selectedFilter) {
        when (selectedFilter) {
            "Ongoing" -> projects.filter { it.status.equals("Ongoing", ignoreCase = true) }
            "Completed" -> projects.filter { it.isCompleted || it.status.equals("Completed", ignoreCase = true) }
            "Pending" -> projects.filter { it.status.equals("Pending", ignoreCase = true) || it.status.equals("Planned", ignoreCase = true) }
            else -> projects
        }
    }

    Scaffold(
        topBar = {
            ProjectSubTopBar(
                title = "Big Project List",
                onBackClick = onBackClick,
                actionText = "+ Add Project",
                onActionClick = onAddProjectClick
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterTabs.forEach { tab ->
                    val count = when (tab) {
                        "All" -> projects.size
                        "Ongoing" -> projects.count { it.status.equals("Ongoing", ignoreCase = true) }
                        "Completed" -> projects.count { it.isCompleted || it.status.equals("Completed", ignoreCase = true) }
                        "Pending" -> projects.count { it.status.equals("Pending", ignoreCase = true) || it.status.equals("Planned", ignoreCase = true) }
                        else -> 0
                    }
                    val isSelected = selectedFilter == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = tab },
                        label = {
                            Text(
                                text = "$tab ($count)",
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0F3E7A),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color(0xFF475569)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color(0xFF0F3E7A) else Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            if (filteredProjects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No Big Contract Projects",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = "Tap '+ Add Project' to create your first electrical contract project.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = onAddProjectClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3E7A)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Big Project")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProjects, key = { it.id }) { project ->
                        val projectPayments = allPayments.filter { it.projectId == project.id }
                        val totalReceived = projectPayments.sumOf { it.amount }
                        val progress = if (project.contractAmount > 0) {
                            ((totalReceived / project.contractAmount) * 100).toInt().coerceIn(0, 100)
                        } else 0

                        BigProjectItemCard(
                            project = project,
                            progress = progress,
                            onClick = { onProjectClick(project.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BigProjectItemCard(
    project: ContractProjectEntity,
    progress: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("project_item_${project.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail / Icon
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0F3E7A), Color(0xFF0284C7))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.projectName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    StatusBadge(status = project.status)
                }

                if (project.clientName.isNotBlank()) {
                    Text(
                        text = project.clientName,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (project.location.isNotBlank()) {
                    Text(
                        text = project.location,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatCurrency(project.contractAmount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0284C7)
                    )
                    Text(
                        text = "$progress%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (progress >= 100) Color(0xFF15803D) else Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Details",
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// --- 2. Add Big Project Form (Matches Screen 2) ---
@Composable
fun AddBigProjectScreen(
    onBackClick: () -> Unit,
    onSaveProject: (ContractProjectEntity) -> Unit
) {
    var projectName by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var floorsText by remember { mutableStateOf("5 (5 Floors)") }
    var location by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var contractAmountText by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(getCurrentDateFormatted()) }
    var endDate by remember { mutableStateOf("31-12-2026") }
    var status by remember { mutableStateOf("Ongoing") }
    var notes by remember { mutableStateOf("") }
    var showFloorsDropdown by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val floorOptions = listOf(
        "1 (1 Floor)",
        "2 (2 Floors)",
        "3 (3 Floors)",
        "4 (4 Floors)",
        "5 (5 Floors)",
        "6 (6 Floors)",
        "7 (7 Floors)",
        "8 (8 Floors)",
        "10 (10 Floors)",
        "12 (12 Floors)",
        "15 (15 Floors)",
        "20 (20 Floors)"
    )

    val statusOptions = listOf("Ongoing", "Planned", "Completed", "On Hold")

    Scaffold(
        topBar = {
            ProjectSubTopBar(
                title = "Add Big Project",
                onBackClick = onBackClick
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 60.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Project Name
                item {
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        label = { Text("Project Name *") },
                        placeholder = { Text("e.g. 5 Stored Building Wiring") },
                        leadingIcon = {
                            Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFF0F3E7A))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("project_name_input")
                    )
                }

                // Customer Name
                item {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name *") },
                        placeholder = { Text("e.g. Rahman Construction Ltd.") },
                        leadingIcon = {
                            Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF0F3E7A))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_name_input")
                    )
                }

                // Building Floors
                item {
                    Box {
                        OutlinedTextField(
                            value = floorsText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Building Floors *") },
                            leadingIcon = {
                                Icon(Icons.Default.Apartment, contentDescription = null, tint = Color(0xFF0F3E7A))
                            },
                            trailingIcon = {
                                IconButton(onClick = { showFloorsDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showFloorsDropdown = true }
                        )
                        DropdownMenu(
                            expanded = showFloorsDropdown,
                            onDismissRequest = { showFloorsDropdown = false }
                        ) {
                            floorOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        floorsText = opt
                                        showFloorsDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Location
                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location *") },
                        placeholder = { Text("e.g. Uttara, Dhaka") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF0F3E7A))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Contact Name
                item {
                    OutlinedTextField(
                        value = contactName,
                        onValueChange = { contactName = it },
                        label = { Text("Contact Name") },
                        placeholder = { Text("e.g. Mr. Rahman") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF0F3E7A))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Contact Number
                item {
                    OutlinedTextField(
                        value = contactNumber,
                        onValueChange = { contactNumber = it },
                        label = { Text("Contact Number *") },
                        placeholder = { Text("e.g. 01712345678") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF0F3E7A))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Contract Amount
                item {
                    OutlinedTextField(
                        value = contractAmountText,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() || char == '.' }) {
                                contractAmountText = it
                            }
                        },
                        label = { Text("Contract Amount (৳) *") },
                        placeholder = { Text("e.g. 500000") },
                        leadingIcon = {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF0F3E7A))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contract_amount_input")
                    )
                }

                // Dates Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Start Date") },
                            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("End Date") },
                            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Status
                item {
                    Box {
                        OutlinedTextField(
                            value = status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Project Status") },
                            trailingIcon = {
                                IconButton(onClick = { showStatusDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showStatusDropdown = true }
                        )
                        DropdownMenu(
                            expanded = showStatusDropdown,
                            onDismissRequest = { showStatusDropdown = false }
                        ) {
                            statusOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        status = opt
                                        showStatusDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Notes
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Project Description / Notes") },
                        placeholder = { Text("Special requirements or instructions...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                if (errorMsg != null) {
                    item {
                        Text(
                            text = errorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val amount = contractAmountText.toDoubleOrNull()
                            if (projectName.isBlank()) {
                                errorMsg = "Please enter project name"
                                return@Button
                            }
                            if (customerName.isBlank()) {
                                errorMsg = "Please enter customer or company name"
                                return@Button
                            }
                            if (amount == null || amount <= 0) {
                                errorMsg = "Please enter a valid contract amount"
                                return@Button
                            }

                            val floorCount = floorsText.filter { it.isDigit() }.toIntOrNull() ?: 1

                            val newProject = ContractProjectEntity(
                                projectName = projectName.trim(),
                                clientName = customerName.trim(),
                                location = location.trim(),
                                contactPhone = contactNumber.trim(),
                                contactPerson = contactName.trim(),
                                contractAmount = amount,
                                startDate = startDate.ifBlank { getCurrentDateFormatted() },
                                endDate = endDate.trim(),
                                notes = notes.trim(),
                                floors = floorCount,
                                status = status
                            )
                            onSaveProject(newProject)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3E7A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_project_submit_btn")
                    ) {
                        Text(
                            text = "Save Project",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// --- 3. Project Details Screen (Matches Screen 6) ---
@Composable
fun BigProjectDetailScreen(
    project: ContractProjectEntity,
    payments: List<ProjectPaymentEntity>,
    expenses: List<ExpenseEntity>,
    onBackClick: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onDeleteProject: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val totalReceived = payments.sumOf { it.amount }
    val remainingAmount = (project.contractAmount - totalReceived).coerceAtLeast(0.0)
    val progress = if (project.contractAmount > 0) {
        ((totalReceived / project.contractAmount) * 100).toInt().coerceIn(0, 100)
    } else 0

    Scaffold(
        topBar = {
            ProjectSubTopBar(
                title = "Project Details",
                onBackClick = onBackClick,
                actionIcon = Icons.Default.Delete,
                onActionClick = { showDeleteConfirm = true }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Hero Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0F3E7A), Color(0xFF0284C7), Color(0xFF38BDF8))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.BottomStart)
                        ) {
                            Text(
                                text = "${project.floors} Stored Building Wiring",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Contract Amount: ${formatCurrency(project.contractAmount)}",
                                color = Color(0xFFE0F2FE),
                                fontSize = 13.sp
                            )
                        }

                        Box(
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            StatusBadge(status = project.status)
                        }
                    }
                }

                // Info Section Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = project.projectName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color(0xFF0F172A)
                            )
                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            DetailInfoRow(
                                icon = Icons.Default.Business,
                                label = "Customer",
                                value = project.clientName.ifBlank { "Not Specified" }
                            )
                            DetailInfoRow(
                                icon = Icons.Default.LocationOn,
                                label = "Location",
                                value = project.location.ifBlank { "Not Specified" }
                            )
                            DetailInfoRow(
                                icon = Icons.Default.Phone,
                                label = "Contact",
                                value = "${project.contactPerson} (${project.contactPhone})".trim()
                            )
                            DetailInfoRow(
                                icon = Icons.Default.AttachMoney,
                                label = "Contract Amount",
                                value = formatCurrency(project.contractAmount),
                                valueColor = Color(0xFF0284C7)
                            )
                            DetailInfoRow(
                                icon = Icons.Default.CalendarToday,
                                label = "Start Date",
                                value = project.startDate
                            )
                            if (project.endDate.isNotBlank()) {
                                DetailInfoRow(
                                    icon = Icons.Default.CalendarMonth,
                                    label = "End Date",
                                    value = project.endDate
                                )
                            }
                        }
                    }
                }

                // Payment Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Payment Summary",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Received Box
                                Surface(
                                    color = Color(0xFFF0FDF4),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF15803D),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Received (৳)",
                                                fontSize = 11.sp,
                                                color = Color(0xFF166534)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = formatCurrency(totalReceived),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                }

                                // Remaining Box
                                Surface(
                                    color = Color(0xFFF0F9FF),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = Color(0xFF0284C7),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Remaining (৳)",
                                                fontSize = 11.sp,
                                                color = Color(0xFF075985)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = formatCurrency(remainingAmount),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF0284C7)
                                        )
                                    }
                                }
                            }

                            // Progress Bar
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                LinearProgressIndicator(
                                    progress = { progress / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = Color(0xFF0284C7),
                                    trackColor = Color(0xFFE2E8F0)
                                )
                                Text(
                                    text = "$progress% Received",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom 3 Action Buttons (Payments, Expenses, Reports)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToPayments,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F3E7A))
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Payments", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onAddExpenseClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Expenses", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        try {
                            val pdfFile = PdfReportGenerator.generateProjectContractPdf(
                                context = context,
                                project = project,
                                payments = payments
                            )
                            val uri: Uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                pdfFile
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Project Summary PDF"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3E7A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reports", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Project") },
            text = { Text("Are you sure you want to delete '${project.projectName}' and all its recorded payments?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteProject()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color(0xFF0F172A)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF0F3E7A),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            modifier = Modifier.weight(1f)
        )
    }
}

// --- 4. Project Payments Screen (Matches Screen 7) ---
@Composable
fun ProjectPaymentsScreen(
    project: ContractProjectEntity,
    payments: List<ProjectPaymentEntity>,
    expenses: List<ExpenseEntity>,
    onBackClick: () -> Unit,
    onSavePayment: (ProjectPaymentEntity) -> Unit,
    onDeletePayment: (Long) -> Unit,
    onAddExpenseClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Payments", "Expenses", "Balance")
    var showRecordPaymentDialog by remember { mutableStateOf(false) }

    val totalReceived = payments.sumOf { it.amount }
    val remainingAmount = (project.contractAmount - totalReceived).coerceAtLeast(0.0)
    val totalExpenses = expenses.sumOf { it.amount }
    val netProfit = totalReceived - totalExpenses

    Scaffold(
        topBar = {
            ProjectSubTopBar(
                title = "Project Payments",
                onBackClick = onBackClick,
                actionIcon = Icons.Default.Add,
                onActionClick = { showRecordPaymentDialog = true }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Project Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F3E7A)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = project.projectName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Total Amount: ${formatCurrency(project.contractAmount)}",
                            color = Color(0xFFBAE6FD),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Tabs: Payments | Expenses | Balance
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF0F3E7A)
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Payments List
                    if (payments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "No payments recorded yet",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF475569)
                                )
                                Button(
                                    onClick = { showRecordPaymentDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3E7A))
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Record 1st Payment")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(payments, key = { it.id }) { p ->
                                PaymentItemCard(
                                    payment = p,
                                    onDelete = { onDeletePayment(p.id) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Expenses for this project
                    if (expenses.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "No expenses linked to this project",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF475569)
                                )
                                Button(
                                    onClick = onAddExpenseClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Project Expense")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(expenses, key = { it.id }) { exp ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = exp.category,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            if (exp.description.isNotBlank()) {
                                                Text(
                                                    text = exp.description,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                            Text(
                                                text = exp.expenseDate,
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                        Text(
                                            text = "- ${formatCurrency(exp.amount)}",
                                            color = Color(0xFFDC2626),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Balance breakdown
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Financial Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                DetailInfoRow(Icons.Default.AttachMoney, "Agreed Contract", formatCurrency(project.contractAmount))
                                DetailInfoRow(Icons.Default.CheckCircle, "Total Received", formatCurrency(totalReceived), Color(0xFF15803D))
                                DetailInfoRow(Icons.Default.Schedule, "Remaining Due", formatCurrency(remainingAmount), Color(0xFF0284C7))
                                DetailInfoRow(Icons.Default.Receipt, "Project Expenses", formatCurrency(totalExpenses), Color(0xFFDC2626))
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                DetailInfoRow(
                                    Icons.Default.AccountBalanceWallet,
                                    "Net Margin / Profit",
                                    formatCurrency(netProfit),
                                    if (netProfit >= 0) Color(0xFF15803D) else Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRecordPaymentDialog) {
        RecordPaymentDialog(
            projectTitle = project.projectName,
            remainingAmount = remainingAmount,
            onDismiss = { showRecordPaymentDialog = false },
            onSavePayment = { newPayment ->
                showRecordPaymentDialog = false
                onSavePayment(newPayment.copy(projectId = project.id))
            }
        )
    }
}

@Composable
fun PaymentItemCard(
    payment: ProjectPaymentEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCFCE7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = Color(0xFF15803D),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = payment.installmentName.ifBlank { "Installment Payment" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "${payment.paymentDate} • ${payment.paymentMethod}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                if (payment.receiptNo.isNotBlank()) {
                    Text(
                        text = "Receipt: ${payment.receiptNo}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(payment.amount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF15803D)
                )
                Spacer(modifier = Modifier.height(2.dp))
                StatusBadge(status = "Received")
            }

            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
