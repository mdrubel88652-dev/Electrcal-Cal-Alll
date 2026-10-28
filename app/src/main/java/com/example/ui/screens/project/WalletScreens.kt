package com.example.ui.screens.project

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ContractProjectEntity
import com.example.data.database.ExpenseEntity
import com.example.data.database.ProjectPaymentEntity
import com.example.data.database.QuickTaskEntity

// --- 1. Wallet Screen (Matches Screen 8a) ---
@Composable
fun WalletScreen(
    walletBalance: Double,
    bigProjectIncome: Double,
    smallJobIncome: Double,
    totalExpenses: Double,
    transactions: List<UnifiedTransaction>,
    onBackClick: () -> Unit,
    onNavigateToTransactionHistory: () -> Unit,
    onAddExpenseClick: () -> Unit
) {
    Scaffold(
        topBar = {
            ProjectSubTopBar(
                title = "Wallet",
                onBackClick = onBackClick,
                actionIcon = Icons.Default.ReceiptLong,
                onActionClick = onNavigateToTransactionHistory
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Wallet Balance Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wallet_main_card"),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF0F3E7A), Color(0xFF092953))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color(0xFF0F3E7A),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Total Available Balance",
                                        color = Color(0xFFBAE6FD),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = formatCurrency(walletBalance),
                                        color = Color.White,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Breakdown Cards Row (Big Projects & Small Jobs)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Big Projects Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Big Projects",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatCurrency(bigProjectIncome),
                                color = Color(0xFF0F3E7A),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Small Jobs Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDCFCE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Small Jobs",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatCurrency(smallJobIncome),
                                color = Color(0xFF15803D),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Quick Actions: Add Expense & Transaction History
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onAddExpenseClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("wallet_add_expense_btn")
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Expense", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = onNavigateToTransactionHistory,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("wallet_view_history_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F3E7A))
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("History", fontSize = 13.sp)
                    }
                }
            }

            // Total Expenses Notice
            if (totalExpenses > 0) {
                item {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Total Expenses Deducted",
                                    fontSize = 13.sp,
                                    color = Color(0xFF991B1B)
                                )
                            }
                            Text(
                                text = "- ${formatCurrency(totalExpenses)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }

            // Recent Transactions Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    )
                    TextButton(
                        onClick = onNavigateToTransactionHistory,
                        modifier = Modifier.testTag("wallet_recent_tx_view_all")
                    ) {
                        Text(
                            text = "View All",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                    }
                }
            }

            // Recent Transactions (top 5)
            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recorded transactions yet.",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(transactions.take(5), key = { it.id }) { tx ->
                    TransactionItemCard(transaction = tx)
                }
            }
        }
    }
}

// --- 2. Transaction History Screen (Matches Screen 8b) ---
@Composable
fun TransactionHistoryScreen(
    transactions: List<UnifiedTransaction>,
    onBackClick: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filterTabs = listOf("All", "Income", "Expense")

    val filteredTransactions = remember(transactions, selectedFilter) {
        when (selectedFilter) {
            "Income" -> transactions.filter { it.isIncome }
            "Expense" -> transactions.filter { !it.isIncome }
            else -> transactions
        }
    }

    Scaffold(
        topBar = {
            ProjectSubTopBar(
                title = "Transaction History",
                onBackClick = onBackClick
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Tabs (All | Income | Expense)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterTabs.forEach { tab ->
                    val isSelected = selectedFilter == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = tab },
                        label = {
                            Text(
                                text = tab,
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
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            if (filteredTransactions.isEmpty()) {
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
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "No Transactions Found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        TransactionItemCard(transaction = tx)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemCard(transaction: UnifiedTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            // Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (transaction.isIncome) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        !transaction.isIncome -> Icons.Default.Receipt
                        transaction.type == "BIG_PROJECT" -> Icons.Default.Business
                        else -> Icons.Default.Build
                    },
                    contentDescription = null,
                    tint = if (transaction.isIncome) Color(0xFF15803D) else Color(0xFFDC2626),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (transaction.subtitle.isNotBlank()) {
                    Text(
                        text = transaction.subtitle,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = transaction.date,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = (if (transaction.isIncome) "+ " else "- ") + formatCurrency(transaction.amount),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (transaction.isIncome) Color(0xFF15803D) else Color(0xFFDC2626)
            )
        }
    }
}
