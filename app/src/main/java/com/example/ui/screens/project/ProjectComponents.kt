package com.example.ui.screens.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ExpenseEntity
import com.example.data.database.ProjectPaymentEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return "৳ " + formatter.format(amount.toLong())
}

fun getCurrentDateFormatted(): String {
    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    return sdf.format(Date())
}

fun getDisplayDateFormatted(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
    return sdf.format(Date())
}

data class UnifiedTransaction(
    val id: String,
    val title: String,
    val subtitle: String,
    val date: String,
    val amount: Double,
    val isIncome: Boolean,
    val type: String, // "BIG_PROJECT", "SMALL_JOB", "EXPENSE"
    val timestamp: Long
)

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status.lowercase()) {
        "ongoing", "চলমান" -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), "Ongoing")
        "completed", "সম্পন্ন" -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), "Completed")
        "pending", "অপেক্ষমান", "planned" -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), "Pending")
        "on hold", "স্থগিত" -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), "On Hold")
        else -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), status)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PaymentStatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status.lowercase()) {
        "paid", "পরিশোধিত" -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), "Paid")
        "partially paid", "আংশিক" -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), "Partial")
        "pending", "অপেক্ষমান" -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "Pending")
        else -> Triple(Color(0xFFFEE2E2), Color(0xFFB91C1C), "Unpaid")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ProjectSubTopBar(
    title: String,
    onBackClick: () -> Unit,
    actionIcon: ImageVector? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Surface(
        color = Color.White,
        shadowElevation = 1.5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("sub_top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0F3E7A)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F3E7A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (actionText != null && onActionClick != null) {
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F3E7A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .padding(end = 4.dp)
                        .testTag("sub_top_bar_action_text_btn")
                ) {
                    Text(
                        text = actionText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (actionIcon != null && onActionClick != null) {
                IconButton(
                    onClick = onActionClick,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("sub_top_bar_action_icon_btn")
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = "Action",
                        tint = Color(0xFF0F3E7A)
                    )
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    projectList: List<Pair<Long, String>>,
    onDismiss: () -> Unit,
    onSaveExpense: (ExpenseEntity) -> Unit
) {
    var category by remember { mutableStateOf("Material Purchase") }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(getCurrentDateFormatted()) }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var selectedProjectId by remember { mutableStateOf<Long?>(null) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showProjectDropdown by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val categories = listOf(
        "Material Purchase",
        "Tools & Equipment",
        "Transport / Fuel",
        "Labor / Helper",
        "Food & Snacks",
        "Subcontractor",
        "Other"
    )

    val paymentMethods = listOf("Cash", "Bank Transfer", "MFS (bKash/Nagad)", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Expense",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Category Picker
                Box {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expense Category *") },
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
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() || char == '.' }) {
                            amountText = it
                        }
                    },
                    label = { Text("Expense Amount (৳) *") },
                    leadingIcon = { Text("৳", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input")
                )

                // Date
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Expense Date") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Notes") },
                    placeholder = { Text("e.g. 2.5 RM cables, circuit breakers") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Linked Project (Optional)
                if (projectList.isNotEmpty()) {
                    Box {
                        val currentProjectName = projectList.find { it.first == selectedProjectId }?.second ?: "None (General Expense)"
                        OutlinedTextField(
                            value = currentProjectName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Link to Big Project (Optional)") },
                            trailingIcon = {
                                IconButton(onClick = { showProjectDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showProjectDropdown = true }
                        )
                        DropdownMenu(
                            expanded = showProjectDropdown,
                            onDismissRequest = { showProjectDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("None (General Expense)") },
                                onClick = {
                                    selectedProjectId = null
                                    showProjectDropdown = false
                                }
                            )
                            projectList.forEach { (projId, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        selectedProjectId = projId
                                        showProjectDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMsg = "Please enter a valid expense amount"
                        return@Button
                    }
                    onSaveExpense(
                        ExpenseEntity(
                            category = category,
                            amount = amount,
                            expenseDate = dateText.ifBlank { getCurrentDateFormatted() },
                            description = description,
                            linkedProjectId = selectedProjectId,
                            paymentMethod = paymentMethod
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                modifier = Modifier.testTag("save_expense_button")
            ) {
                Text("Save Expense", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RecordPaymentDialog(
    projectTitle: String,
    remainingAmount: Double,
    onDismiss: () -> Unit,
    onSavePayment: (ProjectPaymentEntity) -> Unit
) {
    var installmentName by remember { mutableStateOf("1st Installment") }
    var amountText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(getCurrentDateFormatted()) }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var receiptNo by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var showOverpaymentWarning by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showInstallmentDropdown by remember { mutableStateOf(false) }

    val presetInstallments = listOf(
        "Advance Payment",
        "1st Installment",
        "2nd Installment",
        "3rd Installment",
        "Conduit Completion",
        "Wiring Completion",
        "Final Handover Payment"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = Color(0xFF15803D),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Record Customer Payment",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Project: $projectTitle",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color(0xFF0F3E7A)
                )
                Text(
                    text = "Remaining Due: ${formatCurrency(remainingAmount)}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = if (remainingAmount > 0) Color(0xFFB45309) else Color(0xFF15803D)
                )

                // Installment Name
                Box {
                    OutlinedTextField(
                        value = installmentName,
                        onValueChange = { installmentName = it },
                        label = { Text("Payment / Installment Title *") },
                        trailingIcon = {
                            IconButton(onClick = { showInstallmentDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = showInstallmentDropdown,
                        onDismissRequest = { showInstallmentDropdown = false }
                    ) {
                        presetInstallments.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    installmentName = name
                                    showInstallmentDropdown = false
                                }
                            )
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() || char == '.' }) {
                            amountText = it
                        }
                    },
                    label = { Text("Amount Received (৳) *") },
                    leadingIcon = { Text("৳", fontWeight = FontWeight.Bold, color = Color(0xFF15803D)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input")
                )

                // Date
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Payment Date") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Receipt No
                OutlinedTextField(
                    value = receiptNo,
                    onValueChange = { receiptNo = it },
                    label = { Text("Receipt / Voucher No. (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (showOverpaymentWarning) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Warning: Amount entered exceeds remaining contract balance (${formatCurrency(remainingAmount)}). Tap confirm again to proceed.",
                                color = Color(0xFF92400E),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMsg = "Please enter a valid received amount"
                        return@Button
                    }
                    if (amount > remainingAmount && !showOverpaymentWarning) {
                        showOverpaymentWarning = true
                        return@Button
                    }
                    onSavePayment(
                        ProjectPaymentEntity(
                            projectId = 0L,
                            amount = amount,
                            paymentDate = dateText.ifBlank { getCurrentDateFormatted() },
                            installmentName = installmentName.ifBlank { "Installment" },
                            paymentMethod = paymentMethod,
                            receiptNo = receiptNo,
                            note = note
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                modifier = Modifier.testTag("save_payment_confirm_button")
            ) {
                Text("Confirm Payment", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
