package com.example.ui.screens.project

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.QuickTaskEntity

// --- 1. Small Job List Screen (Matches Screen 4) ---
@Composable
fun SmallJobListScreen(
    jobs: List<QuickTaskEntity>,
    onBackClick: () -> Unit,
    onAddJobClick: () -> Unit,
    onUpdateJob: (QuickTaskEntity) -> Unit,
    onDeleteJob: (Long) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filterTabs = listOf("All", "Pending", "Paid", "Complete")

    var selectedJobForAction by remember { mutableStateOf<QuickTaskEntity?>(null) }

    val filteredJobs = remember(jobs, searchQuery, selectedFilter) {
        jobs.filter { job ->
            val matchesFilter = when (selectedFilter) {
                "Pending" -> job.paymentStatus.equals("Pending", ignoreCase = true) || job.paymentStatus.equals("Unpaid", ignoreCase = true)
                "Paid" -> job.paymentStatus.equals("Paid", ignoreCase = true)
                "Complete" -> job.paymentStatus.equals("Complete", ignoreCase = true) || job.paymentStatus.equals("Paid", ignoreCase = true)
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                job.taskTitle.contains(searchQuery, ignoreCase = true) ||
                job.clientName.contains(searchQuery, ignoreCase = true) ||
                job.contactPhone.contains(searchQuery, ignoreCase = true) ||
                job.location.contains(searchQuery, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            ProjectSubTopBar(
                title = "Small Job List",
                onBackClick = onBackClick,
                actionText = "+ Add Job",
                onActionClick = onAddJobClick
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search customer, job or contact...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF64748B)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedBorderColor = Color(0xFF0F3E7A)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("small_job_search_input")
            )

            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterTabs.forEach { tab ->
                    val count = when (tab) {
                        "All" -> jobs.size
                        "Pending" -> jobs.count { it.paymentStatus.equals("Pending", ignoreCase = true) || it.paymentStatus.equals("Unpaid", ignoreCase = true) }
                        "Paid" -> jobs.count { it.paymentStatus.equals("Paid", ignoreCase = true) }
                        "Complete" -> jobs.count { it.paymentStatus.equals("Paid", ignoreCase = true) }
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

            if (filteredJobs.isEmpty()) {
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
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(60.dp)
                        )
                        Text(
                            text = "No Small Jobs Found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = "Record fan repair, board wiring, troubleshooting & daily service calls.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = onAddJobClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Small Job")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredJobs, key = { it.id }) { job ->
                        SmallJobItemCard(
                            job = job,
                            onClick = { selectedJobForAction = job }
                        )
                    }
                }
            }
        }
    }

    // Job Action Dialog (View/Toggle Payment/Call/Delete)
    selectedJobForAction?.let { job ->
        AlertDialog(
            onDismissRequest = { selectedJobForAction = null },
            title = {
                Text(
                    text = job.taskTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Customer: ${job.clientName}", fontWeight = FontWeight.Medium)
                    if (job.contactPhone.isNotBlank()) {
                        Text("Phone: ${job.contactPhone}")
                    }
                    if (job.location.isNotBlank()) {
                        Text("Location: ${job.location}")
                    }
                    if (job.workDescription.isNotBlank()) {
                        Text("Work: ${job.workDescription}", color = Color(0xFF475569))
                    }
                    Text(
                        text = "Total Amount: ${formatCurrency(if (job.totalAmount > 0) job.totalAmount else job.amount)}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    Text("Date: ${job.taskDate} • Status: ${job.paymentStatus}")
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (job.paymentStatus != "Paid") {
                        Button(
                            onClick = {
                                onUpdateJob(job.copy(paymentStatus = "Paid", amount = if (job.totalAmount > 0) job.totalAmount else job.amount))
                                selectedJobForAction = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
                        ) {
                            Text("Mark as Paid")
                        }
                    }
                    if (job.contactPhone.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${job.contactPhone}"))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDeleteJob(job.id)
                        selectedJobForAction = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            }
        )
    }
}

@Composable
fun SmallJobItemCard(
    job: QuickTaskEntity,
    onClick: () -> Unit
) {
    val displayAmount = if (job.totalAmount > 0) job.totalAmount else job.amount
    val isPaid = job.paymentStatus.equals("Paid", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("small_job_item_${job.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isPaid) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = if (isPaid) Color(0xFF15803D) else Color(0xFFD97706),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = job.taskTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (job.clientName.isNotBlank()) {
                    Text(
                        text = job.clientName,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (job.contactPhone.isNotBlank()) {
                    Text(
                        text = job.contactPhone,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = job.taskDate,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(displayAmount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isPaid) Color(0xFF15803D) else Color(0xFFD97706)
                )
                Spacer(modifier = Modifier.height(3.dp))
                PaymentStatusBadge(status = job.paymentStatus)
            }

            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// --- 2. Add Small Job Form (Matches Screen 3) ---
@Composable
fun AddSmallJobScreen(
    onBackClick: () -> Unit,
    onSaveJob: (QuickTaskEntity) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var jobCategory by remember { mutableStateOf("Installation") }
    var location by remember { mutableStateOf("") }
    var workDescription by remember { mutableStateOf("") }
    var totalAmountText by remember { mutableStateOf("") }
    var paymentStatus by remember { mutableStateOf("Paid") }
    var dateText by remember { mutableStateOf(getCurrentDateFormatted()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var showJobTitleDropdown by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showPaymentStatusDropdown by remember { mutableStateOf(false) }

    val presetJobTitles = listOf(
        "Fan Installation",
        "LED Light Installation",
        "Switch Board Change",
        "House Wiring (Small Work)",
        "Ceiling Fan Repair",
        "Socket & Plug Replacement",
        "Circuit Breaker / MCB Replacement",
        "Short Circuit Troubleshooting",
        "Substation / Panel Inspection",
        "Water Pump Motor Repair"
    )

    val categoryOptions = listOf(
        "Installation",
        "Repair",
        "Wiring",
        "Troubleshooting",
        "Maintenance",
        "Other"
    )

    val paymentStatusOptions = listOf("Paid", "Pending", "Partially Paid")

    Scaffold(
        topBar = {
            ProjectSubTopBar(
                title = "Add Small Job",
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
                // Customer Name
                item {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name *") },
                        placeholder = { Text("e.g. Karim Uddin") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF0F3E7A))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("small_job_customer_name_input")
                    )
                }

                // Contact Number
                item {
                    OutlinedTextField(
                        value = contactNumber,
                        onValueChange = { contactNumber = it },
                        label = { Text("Contact Number *") },
                        placeholder = { Text("e.g. 01798765432") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF0F3E7A))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Job Title
                item {
                    Box {
                        OutlinedTextField(
                            value = jobTitle,
                            onValueChange = { jobTitle = it },
                            label = { Text("Job Title *") },
                            placeholder = { Text("e.g. Fan Installation") },
                            leadingIcon = {
                                Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF0F3E7A))
                            },
                            trailingIcon = {
                                IconButton(onClick = { showJobTitleDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("small_job_title_input")
                        )
                        DropdownMenu(
                            expanded = showJobTitleDropdown,
                            onDismissRequest = { showJobTitleDropdown = false }
                        ) {
                            presetJobTitles.forEach { title ->
                                DropdownMenuItem(
                                    text = { Text(title) },
                                    onClick = {
                                        jobTitle = title
                                        showJobTitleDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Job Category
                item {
                    Box {
                        OutlinedTextField(
                            value = jobCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Job Category *") },
                            leadingIcon = {
                                Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFF0F3E7A))
                            },
                            trailingIcon = {
                                IconButton(onClick = { showCategoryDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCategoryDropdown = true }
                        )
                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false }
                        ) {
                            categoryOptions.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        jobCategory = cat
                                        showCategoryDropdown = false
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
                        label = { Text("Location") },
                        placeholder = { Text("e.g. Mohammadpur, Dhaka") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF0F3E7A))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Work Description
                item {
                    OutlinedTextField(
                        value = workDescription,
                        onValueChange = { workDescription = it },
                        label = { Text("Work Description") },
                        placeholder = { Text("e.g. 3 ceiling fan installation with wiring") },
                        leadingIcon = {
                            Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF0F3E7A))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                // Total Amount
                item {
                    OutlinedTextField(
                        value = totalAmountText,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() || char == '.' }) {
                                totalAmountText = it
                            }
                        },
                        label = { Text("Total Amount (৳) *") },
                        placeholder = { Text("e.g. 3000") },
                        leadingIcon = {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF0F3E7A))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("small_job_amount_input")
                    )
                }

                // Payment Status
                item {
                    Box {
                        OutlinedTextField(
                            value = paymentStatus,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Payment Status *") },
                            trailingIcon = {
                                IconButton(onClick = { showPaymentStatusDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPaymentStatusDropdown = true }
                        )
                        DropdownMenu(
                            expanded = showPaymentStatusDropdown,
                            onDismissRequest = { showPaymentStatusDropdown = false }
                        ) {
                            paymentStatusOptions.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status) },
                                    onClick = {
                                        paymentStatus = status
                                        showPaymentStatusDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Date
                item {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text("Date *") },
                        trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
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
                            val amount = totalAmountText.toDoubleOrNull()
                            if (customerName.isBlank()) {
                                errorMsg = "Please enter customer name"
                                return@Button
                            }
                            if (jobTitle.isBlank()) {
                                errorMsg = "Please enter job title"
                                return@Button
                            }
                            if (amount == null || amount <= 0) {
                                errorMsg = "Please enter a valid amount"
                                return@Button
                            }

                            val receivedCash = if (paymentStatus == "Paid") amount else 0.0

                            val newJob = QuickTaskEntity(
                                taskTitle = jobTitle.trim(),
                                clientName = customerName.trim(),
                                contactPhone = contactNumber.trim(),
                                location = location.trim(),
                                amount = receivedCash,
                                totalAmount = amount,
                                jobCategory = jobCategory,
                                workDescription = workDescription.trim(),
                                taskDate = dateText.ifBlank { getCurrentDateFormatted() },
                                paymentStatus = paymentStatus
                            )
                            onSaveJob(newJob)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3E7A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_small_job_submit_btn")
                    ) {
                        Text(
                            text = "Save Job",
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
