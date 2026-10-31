package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ElectricalApp
import com.example.data.database.ContractProjectEntity
import com.example.data.database.ExpenseEntity
import com.example.data.database.ProjectPaymentEntity
import com.example.data.database.QuickTaskEntity
import com.example.ui.screens.project.*
import kotlinx.coroutines.launch

sealed class ProjectSubScreen {
    object Main : ProjectSubScreen()
    object BigProjectList : ProjectSubScreen()
    object AddBigProject : ProjectSubScreen()
    data class BigProjectDetail(val projectId: Long) : ProjectSubScreen()
    data class ProjectPayments(val projectId: Long) : ProjectSubScreen()
    object SmallJobList : ProjectSubScreen()
    object AddSmallJob : ProjectSubScreen()
    object Wallet : ProjectSubScreen()
    object TransactionHistory : ProjectSubScreen()
}

@Composable
fun ProjectScreen() {
    val context = LocalContext.current
    val app = ElectricalApp.getApp(context)
    val coroutineScope = rememberCoroutineScope()

    // Navigation Stack
    var screenStack by remember { mutableStateOf(listOf<ProjectSubScreen>(ProjectSubScreen.Main)) }
    val currentScreen = screenStack.lastOrNull() ?: ProjectSubScreen.Main

    fun navigateTo(screen: ProjectSubScreen) {
        screenStack = screenStack + screen
    }

    fun navigateBack(): Boolean {
        if (screenStack.size > 1) {
            screenStack = screenStack.dropLast(1)
            return true
        }
        return false
    }

    // System back navigation handles the sub-screen stack
    BackHandler(enabled = screenStack.size > 1) {
        navigateBack()
    }

    // Collect Room Data Flows
    val projects by app.projectRepository.allProjects.collectAsState(initial = emptyList())
    val allPayments by app.projectRepository.allPayments.collectAsState(initial = emptyList())
    val quickTasks by app.projectRepository.allQuickTasks.collectAsState(initial = emptyList())
    val allExpenses by app.projectRepository.allExpenses.collectAsState(initial = emptyList())

    // Financial Computations according to specifications
    val bigProjectIncome = remember(allPayments) { allPayments.sumOf { it.amount } }
    val smallJobIncome = remember(quickTasks) { quickTasks.sumOf { it.amount } }
    val totalExpenses = remember(allExpenses) { allExpenses.sumOf { it.amount } }
    val walletBalance = remember(bigProjectIncome, smallJobIncome, totalExpenses) {
        (bigProjectIncome + smallJobIncome) - totalExpenses
    }

    // Unified Transaction Ledger
    val unifiedTransactions = remember(allPayments, projects, quickTasks, allExpenses) {
        val list = mutableListOf<UnifiedTransaction>()

        allPayments.forEach { p ->
            val projName = projects.find { it.id == p.projectId }?.projectName ?: "Contract Project"
            list.add(
                UnifiedTransaction(
                    id = "payment_${p.id}",
                    title = "Payment from Customer",
                    subtitle = projName,
                    date = p.paymentDate,
                    amount = p.amount,
                    isIncome = true,
                    type = "BIG_PROJECT",
                    timestamp = p.timestamp
                )
            )
        }

        quickTasks.forEach { task ->
            if (task.amount > 0) {
                list.add(
                    UnifiedTransaction(
                        id = "job_${task.id}",
                        title = "Small Job Income",
                        subtitle = task.taskTitle,
                        date = task.taskDate,
                        amount = task.amount,
                        isIncome = true,
                        type = "SMALL_JOB",
                        timestamp = task.timestamp
                    )
                )
            }
        }

        allExpenses.forEach { exp ->
            list.add(
                UnifiedTransaction(
                    id = "expense_${exp.id}",
                    title = exp.category,
                    subtitle = exp.description.ifBlank { exp.category },
                    date = exp.expenseDate,
                    amount = exp.amount,
                    isIncome = false,
                    type = "EXPENSE",
                    timestamp = exp.timestamp
                )
            )
        }

        list.sortedByDescending { it.timestamp }
    }

    // Global Add Expense Modal
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ProjectScreenTransition"
    ) { screen ->
        when (screen) {
            is ProjectSubScreen.Main -> {
                ProjectMainScreen(
                    walletBalance = walletBalance,
                    bigProjectsCount = projects.size,
                    bigProjectIncome = bigProjectIncome,
                    smallJobsCount = quickTasks.size,
                    smallJobIncome = smallJobIncome,
                    onNavigateToBigProjects = { navigateTo(ProjectSubScreen.BigProjectList) },
                    onNavigateToSmallJobs = { navigateTo(ProjectSubScreen.SmallJobList) },
                    onNavigateToWallet = { navigateTo(ProjectSubScreen.Wallet) }
                )
            }

            is ProjectSubScreen.BigProjectList -> {
                BigProjectListScreen(
                    projects = projects,
                    allPayments = allPayments,
                    onBackClick = { navigateBack() },
                    onAddProjectClick = { navigateTo(ProjectSubScreen.AddBigProject) },
                    onProjectClick = { projectId ->
                        navigateTo(ProjectSubScreen.BigProjectDetail(projectId))
                    }
                )
            }

            is ProjectSubScreen.AddBigProject -> {
                AddBigProjectScreen(
                    onBackClick = { navigateBack() },
                    onSaveProject = { newProject ->
                        coroutineScope.launch {
                            app.projectRepository.saveProject(newProject)
                            Toast.makeText(context, "Big Project Saved!", Toast.LENGTH_SHORT).show()
                            navigateBack()
                        }
                    }
                )
            }

            is ProjectSubScreen.BigProjectDetail -> {
                val project = projects.find { it.id == screen.projectId }
                if (project != null) {
                    val projectPayments = allPayments.filter { it.projectId == project.id }
                    val projectExpenses = allExpenses.filter { it.linkedProjectId == project.id }
                    BigProjectDetailScreen(
                        project = project,
                        payments = projectPayments,
                        expenses = projectExpenses,
                        onBackClick = { navigateBack() },
                        onNavigateToPayments = {
                            navigateTo(ProjectSubScreen.ProjectPayments(project.id))
                        },
                        onDeleteProject = {
                            coroutineScope.launch {
                                app.projectRepository.deleteProject(project.id)
                                Toast.makeText(context, "Project Deleted", Toast.LENGTH_SHORT).show()
                                navigateBack()
                            }
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Project not found")
                    }
                }
            }

            is ProjectSubScreen.ProjectPayments -> {
                val project = projects.find { it.id == screen.projectId }
                if (project != null) {
                    val projectPayments = allPayments.filter { it.projectId == project.id }
                    val projectExpenses = allExpenses.filter { it.linkedProjectId == project.id }
                    ProjectPaymentsScreen(
                        project = project,
                        payments = projectPayments,
                        expenses = projectExpenses,
                        onBackClick = { navigateBack() },
                        onSavePayment = { newPayment ->
                            coroutineScope.launch {
                                app.projectRepository.addPayment(newPayment)
                                Toast.makeText(context, "Payment Recorded & Added to Wallet Income!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDeletePayment = { paymentId ->
                            coroutineScope.launch {
                                app.projectRepository.deletePayment(paymentId)
                                Toast.makeText(context, "Payment Removed", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onAddExpenseClick = { showAddExpenseDialog = true }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Project not found")
                    }
                }
            }

            is ProjectSubScreen.SmallJobList -> {
                SmallJobListScreen(
                    jobs = quickTasks,
                    onBackClick = { navigateBack() },
                    onAddJobClick = { navigateTo(ProjectSubScreen.AddSmallJob) },
                    onUpdateJob = { updatedJob ->
                        coroutineScope.launch {
                            app.projectRepository.saveQuickTask(updatedJob)
                            Toast.makeText(context, "Job Updated!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDeleteJob = { jobId ->
                        coroutineScope.launch {
                            app.projectRepository.deleteQuickTask(jobId)
                            Toast.makeText(context, "Job Deleted", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            is ProjectSubScreen.AddSmallJob -> {
                AddSmallJobScreen(
                    onBackClick = { navigateBack() },
                    onSaveJob = { newJob ->
                        coroutineScope.launch {
                            app.projectRepository.addQuickTask(newJob)
                            Toast.makeText(context, "Small Job Saved!", Toast.LENGTH_SHORT).show()
                            navigateBack()
                        }
                    }
                )
            }

            is ProjectSubScreen.Wallet -> {
                WalletScreen(
                    walletBalance = walletBalance,
                    bigProjectIncome = bigProjectIncome,
                    smallJobIncome = smallJobIncome,
                    totalExpenses = totalExpenses,
                    transactions = unifiedTransactions,
                    onBackClick = { navigateBack() },
                    onNavigateToTransactionHistory = {
                        navigateTo(ProjectSubScreen.TransactionHistory)
                    },
                    onAddExpenseClick = { showAddExpenseDialog = true }
                )
            }

            is ProjectSubScreen.TransactionHistory -> {
                TransactionHistoryScreen(
                    transactions = unifiedTransactions,
                    onBackClick = { navigateBack() }
                )
            }
        }
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            projectList = projects.map { it.id to it.projectName },
            onDismiss = { showAddExpenseDialog = false },
            onSaveExpense = { newExpense ->
                coroutineScope.launch {
                    app.projectRepository.addExpense(newExpense)
                    showAddExpenseDialog = false
                    Toast.makeText(context, "Expense Saved & Wallet Deducted", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun ProjectMainScreen(
    walletBalance: Double,
    bigProjectsCount: Int,
    bigProjectIncome: Double,
    smallJobsCount: Int,
    smallJobIncome: Double,
    onNavigateToBigProjects: () -> Unit,
    onNavigateToSmallJobs: () -> Unit,
    onNavigateToWallet: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            // A. Dark Blue Gradient Wallet Summary Card (Matches Screen 1 Top Card)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("project_main_wallet_card"),
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
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Top Row: Wallet Icon + Total Available Balance + View Wallet Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
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
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Total Wallet Balance",
                                            color = Color(0xFFBAE6FD),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = formatCurrency(walletBalance),
                                            color = Color.White,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Button(
                                    onClick = onNavigateToWallet,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF0284C7),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(18.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("view_wallet_btn")
                                ) {
                                    Text(
                                        text = "View Wallet",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Sub-Cards Row: Big Projects & Small Jobs
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Big Projects Sub-card
                                Surface(
                                    color = Color.White.copy(alpha = 0.95f),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(onClick = onNavigateToBigProjects)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE0F2FE)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Business,
                                                contentDescription = null,
                                                tint = Color(0xFF0284C7),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Big Projects",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "$bigProjectsCount Projects",
                                                fontSize = 10.sp,
                                                color = Color(0xFF64748B)
                                            )
                                            Text(
                                                text = formatCurrency(bigProjectIncome),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0284C7)
                                            )
                                        }
                                    }
                                }

                                // Small Jobs Sub-card
                                Surface(
                                    color = Color.White.copy(alpha = 0.95f),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(onClick = onNavigateToSmallJobs)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFDCFCE7)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Build,
                                                contentDescription = null,
                                                tint = Color(0xFF15803D),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Small Jobs",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "$smallJobsCount Jobs",
                                                fontSize = 10.sp,
                                                color = Color(0xFF64748B)
                                            )
                                            Text(
                                                text = formatCurrency(smallJobIncome),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF15803D)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // B. Three Quick Navigation Round Buttons (Matches Screen 1 middle)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickNavButton(
                        icon = Icons.Default.Assignment,
                        bgColor = Color(0xFF0284C7),
                        title = "Contract\nProject Manager",
                        onClick = onNavigateToBigProjects,
                        tag = "quick_nav_contract_manager"
                    )
                    QuickNavButton(
                        icon = Icons.Default.Build,
                        bgColor = Color(0xFF10B981),
                        title = "Small Job\nManager",
                        onClick = onNavigateToSmallJobs,
                        tag = "quick_nav_small_job_manager"
                    )
                    QuickNavButton(
                        icon = Icons.Default.AccountBalanceWallet,
                        bgColor = Color(0xFF8B5CF6),
                        title = "Wallet",
                        onClick = onNavigateToWallet,
                        tag = "quick_nav_wallet"
                    )
                }
            }

            // C. "Your Projects" Section (Matches Screen 1 bottom)
            item {
                Text(
                    text = "Your Projects",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 1. Big Contract Projects Navigation Card
            item {
                MainSectionNavCard(
                    icon = Icons.Default.Business,
                    iconBgColor = Color(0xFF0284C7),
                    title = "Big Contract Projects ($bigProjectsCount)",
                    subtitle = "Manage your contract projects",
                    onClick = onNavigateToBigProjects,
                    tag = "main_nav_big_projects"
                )
            }

            // 2. Small Job Projects Navigation Card
            item {
                MainSectionNavCard(
                    icon = Icons.Default.Build,
                    iconBgColor = Color(0xFF10B981),
                    title = "Small Job Projects ($smallJobsCount)",
                    subtitle = "Daily service & small contracts",
                    onClick = onNavigateToSmallJobs,
                    tag = "main_nav_small_jobs"
                )
            }
        }
    }

@Composable
fun QuickNavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    title: String,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(tag)
            .width(105.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1E293B),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 15.sp
        )
    }
}

@Composable
fun MainSectionNavCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
