package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ElectricalApp
import com.example.data.database.QuickTaskEntity
import com.example.data.datastore.TechnicianReportProfile
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicianHubSheet(
    onDismiss: () -> Unit,
    onNavigateToSettingsProfile: () -> Unit,
    onNavigateToProject: () -> Unit
) {
    val context = LocalContext.current
    val app = remember { ElectricalApp.getApp(context) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentLanguage by app.settingsManager.languageFlow.collectAsState(initial = "EN")
    val isEn = currentLanguage == "EN"

    val techProfile by app.settingsManager.technicianProfileFlow.collectAsState(initial = TechnicianReportProfile())
    val quickTasks by app.projectRepository.allQuickTasks.collectAsState(initial = emptyList())
    val contractPayments by app.projectRepository.allPayments.collectAsState(initial = emptyList())
    val contractProjects by app.projectRepository.allProjects.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Profile, 1: Wallet
    var taskToDelete by remember { mutableStateOf<QuickTaskEntity?>(null) }

    // Financial calculations
    val smallTasksTotal = quickTasks.sumOf { it.amount }
    val bigProjectsTotal = contractPayments.sumOf { it.amount }
    val grandTotal = smallTasksTotal + bigProjectsTotal

    val currentMonthKey = remember {
        SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
    }
    val currentMonthQuickTasks = quickTasks.filter { it.monthYear == currentMonthKey }
    val currentMonthSmallTotal = currentMonthQuickTasks.sumOf { it.amount }
    val monthlyLimit = 50000.0
    val remainingMonthlyLimit = (monthlyLimit - currentMonthSmallTotal).coerceAtLeast(0.0)

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF8FAFC),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(width = 36.dp, height = 4.dp),
                shape = CircleShape,
                color = Color(0xFFCBD5E1)
            ) {}
        },
        modifier = Modifier.testTag("technician_hub_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.Badge else Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isEn) "Technician Hub" else "টেকনিশিয়ান হাব ও শর্টকাট",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = if (isEn) "Quick Profile & Real-time Wallet" else "প্রোফাইল ভিউ এবং রিয়েল-টাইম ওয়ালেট",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                    },
                    modifier = Modifier.testTag("close_hub_sheet_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF64748B)
                    )
                }
            }

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFFF1F5F9),
                contentColor = Color(0xFF0284C7),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF0284C7),
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 0) Color(0xFF0284C7) else Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Profile" else "প্রোফাইল",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 0) Color(0xFF0284C7) else Color(0xFF64748B)
                            )
                        }
                    },
                    modifier = Modifier.testTag("hub_tab_profile")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 1) Color(0xFF0284C7) else Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Wallet (৳)" else "ওয়ালেট (৳)",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 1) Color(0xFF0284C7) else Color(0xFF64748B)
                            )
                        }
                    },
                    modifier = Modifier.testTag("hub_tab_wallet")
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Profile View (READ ONLY)
                    TechnicianProfileReadOnlyTab(
                        profile = techProfile,
                        isEn = isEn,
                        onNavigateToEdit = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                onDismiss()
                                onNavigateToSettingsProfile()
                            }
                        }
                    )
                }
                1 -> {
                    // Wallet View (Real-time live updates & separated amounts)
                    TechnicianWalletTab(
                        grandTotal = grandTotal,
                        smallTasksTotal = smallTasksTotal,
                        bigProjectsTotal = bigProjectsTotal,
                        currentMonthSmallTotal = currentMonthSmallTotal,
                        monthlyLimit = monthlyLimit,
                        remainingMonthlyLimit = remainingMonthlyLimit,
                        quickTasks = quickTasks,
                        contractPayments = contractPayments,
                        contractProjects = contractProjects,
                        isEn = isEn,
                        numberFormat = numberFormat,
                        onDeleteQuickTask = { task ->
                            taskToDelete = task
                        },
                        onNavigateToProject = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                onDismiss()
                                onNavigateToProject()
                            }
                        }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFDC2626)
                )
            },
            title = {
                Text(
                    text = if (isEn) "Delete Task & Deduct from Wallet?" else "কাজ ডিলিট ও ওয়ালেট থেকে মাইনাস করবেন?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = if (isEn) {
                        "Are you sure you want to delete '${task.taskTitle}'? ৳${numberFormat.format(task.amount)} will be immediately removed and cancelled from your Wallet."
                    } else {
                        "আপনি কি নিশ্চিত যে '${task.taskTitle}' কাজটি ডিলিট করতে চান? ৳${numberFormat.format(task.amount)} সাথে সাথে আপনার ওয়ালেট থেকে মাইনাস ও বাতিল হয়ে যাবে।"
                    },
                    fontSize = 13.5.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val taskId = task.id
                        taskToDelete = null
                        scope.launch {
                            app.projectRepository.deleteQuickTask(taskId)
                            Toast.makeText(
                                context,
                                if (isEn) "Task deleted and amount deducted from Wallet!" else "কাজ ডিলিট করা হয়েছে এবং ওয়ালেট থেকে অ্যামাউন্ট মাইনাস হয়েছে!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text(if (isEn) "Delete & Deduct" else "ডিলিট ও মাইনাস করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text(if (isEn) "Cancel" else "বাতিল")
                }
            }
        )
    }
}

@Composable
private fun TechnicianProfileReadOnlyTab(
    profile: TechnicianReportProfile,
    isEn: Boolean,
    onNavigateToEdit: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        // Read-only advisory notice
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFEF3C7),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isEn) "Read-Only Profile View" else "শুধুমাত্র দেখার জন্য প্রোফাইল",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = if (isEn) {
                                "You cannot edit anything here. To edit or submit your technician profile, please go to Settings."
                            } else {
                                "এইখানে ইউজার কোনো কিছু এডিট করতে পারবে না। শুধুমাত্র সেটিংস অপশনে গিয়ে টেকনিশিয়ান প্রোফাইল এডিট বা সাবমিট করতে পারবেন।"
                            },
                            fontSize = 11.5.sp,
                            color = Color(0xFFB45309),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Profile Badge Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.name.take(1).uppercase().ifBlank { "T" },
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = profile.name.ifBlank { if (isEn) "No Name Submitted" else "কোনো নাম সাবমিট করা হয়নি" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = profile.company.ifBlank { if (isEn) "Electrical Technician / Engineer" else "ইলেকট্রিক্যাল টেকনিশিয়ান / ইঞ্জিনিয়ার" },
                        fontSize = 12.5.sp,
                        color = Color(0xFF0284C7),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEn) "Registered in Settings" else "সেটিংস থেকে সংরক্ষিত",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }
        }

        // Details List (Read Only)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReadOnlyInfoRow(
                        icon = Icons.Default.Phone,
                        label = if (isEn) "Contact Phone" else "যোগাযোগের ফোন",
                        value = profile.contactPhone.ifBlank { if (isEn) "Not provided" else "দেওয়া হয়নি" }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    ReadOnlyInfoRow(
                        icon = Icons.Default.Business,
                        label = if (isEn) "Company / Organization" else "প্রতিষ্ঠান / কোম্পানি",
                        value = profile.company.ifBlank { if (isEn) "Not provided" else "দেওয়া হয়নি" }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    ReadOnlyInfoRow(
                        icon = Icons.Default.Email,
                        label = if (isEn) "Email Address" else "ইমেইল এড্রেস",
                        value = profile.email.ifBlank { if (isEn) "Not provided" else "দেওয়া হয়নি" }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    ReadOnlyInfoRow(
                        icon = Icons.Default.LocationOn,
                        label = if (isEn) "Official Address" else "অফিসিয়াল ঠিকানা",
                        value = profile.officialAddress.ifBlank { if (isEn) "Not provided" else "দেওয়া হয়নি" }
                    )
                }
            }
        }

        // Action button to go to Settings
        item {
            Button(
                onClick = onNavigateToEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("hub_go_to_settings_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEn) "Go to Settings to Edit Profile" else "সেটিংস থেকে প্রোফাইল এডিট করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ReadOnlyInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF0284C7),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 13.5.sp,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TechnicianWalletTab(
    grandTotal: Double,
    smallTasksTotal: Double,
    bigProjectsTotal: Double,
    currentMonthSmallTotal: Double,
    monthlyLimit: Double,
    remainingMonthlyLimit: Double,
    quickTasks: List<QuickTaskEntity>,
    contractPayments: List<com.example.data.database.ProjectPaymentEntity>,
    contractProjects: List<com.example.data.database.ContractProjectEntity>,
    isEn: Boolean,
    numberFormat: NumberFormat,
    onDeleteQuickTask: (QuickTaskEntity) -> Unit,
    onNavigateToProject: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        // Hero Combined Wallet Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0369A1))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isEn) "TECHNICIAN WALLET" else "টেকনিশিয়ান ওয়ালেট",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x3310B981)
                            ) {
                                Text(
                                    text = if (isEn) "Live Synced" else "লাইভ সিঙ্ক",
                                    color = Color(0xFF34D399),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isEn) "Total Wallet Balance" else "সর্বমোট ওয়ালেট ব্যালেন্স (আয়)",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "৳ ${numberFormat.format(grandTotal)}",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isEn) {
                                "Combined: Small Tasks (৳${numberFormat.format(smallTasksTotal)}) + Major Projects (৳${numberFormat.format(bigProjectsTotal)})"
                            } else {
                                "সংযুক্ত: ছোট কাজ (৳${numberFormat.format(smallTasksTotal)}) + বড় প্রজেক্ট (৳${numberFormat.format(bigProjectsTotal)})"
                            },
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Two Distinct Breakdown Cards (Small Projects vs Big Projects)
        item {
            Text(
                text = if (isEn) "Wallet Amount Breakdown" else "আলাদা আলাদা ওয়ালেট হিসাব",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        // Card 1: Small Projects / Quick Tasks
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Work,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isEn) "Small Projects / Quick Tasks" else "ছোট প্রজেক্ট ও কাজের অ্যামাউন্ট",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = if (isEn) "${quickTasks.size} tasks recorded" else "${quickTasks.size} টি ছোট কাজ যুক্ত আছে",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Text(
                            text = "৳ ${numberFormat.format(smallTasksTotal)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Monthly 50,000 Quota Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEn) "This Month: ৳${numberFormat.format(currentMonthSmallTotal)} / ৳50,000" else "চলতি মাসে: ৳${numberFormat.format(currentMonthSmallTotal)} / ৳৫০,০০০",
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isEn) "Limit Left: ৳${numberFormat.format(remainingMonthlyLimit)}" else "বাকি সীমা: ৳${numberFormat.format(remainingMonthlyLimit)}",
                            fontSize = 11.sp,
                            color = if (remainingMonthlyLimit < 5000) Color(0xFFDC2626) else Color(0xFF16A34A),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val quotaFraction = (currentMonthSmallTotal / monthlyLimit).toFloat().coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { quotaFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (quotaFraction > 0.9f) Color(0xFFDC2626) else Color(0xFFD97706),
                        trackColor = Color(0xFFF1F5F9)
                    )
                }
            }
        }

        // Card 2: Big Projects (5 Contract Projects)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isEn) "Major 5 Contract Projects" else "বড় ৫টি প্রজেক্টের অ্যামাউন্ট",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = if (isEn) "${contractProjects.size}/5 projects • ${contractPayments.size} customer payments" else "${contractProjects.size}/৫টি প্রজেক্ট • ${contractPayments.size}টি কিস্তি রসিদ",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Text(
                            text = "৳ ${numberFormat.format(bigProjectsTotal)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                    }
                }
            }
        }

        // Quick Tasks List with Delete Option
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEn) "Quick Tasks History (${quickTasks.size})" else "ছোট কাজের হিস্ট্রি (${quickTasks.size})",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                TextButton(
                    onClick = onNavigateToProject,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = if (isEn) "Add Task in Projects" else "+ প্রজেক্টে কাজ যোগ করুন",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                }
            }
        }

        if (quickTasks.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isEn) "No quick tasks added yet" else "এখনো কোনো ছোট কাজ যুক্ত করা হয়নি",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = if (isEn) "Add small electrical jobs in the Projects screen to track payments here up to ৳50,000/month." else "প্রজেক্ট স্ক্রিনে ছোট ছোট কাজ যুক্ত করলে সাথে সাথে এই ওয়ালেটে যোগ হবে (মাসে সর্বোচ্চ ৫০,০০০ টাকা)।",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(quickTasks, key = { it.id }) { task ->
                QuickTaskWalletItem(
                    task = task,
                    isEn = isEn,
                    numberFormat = numberFormat,
                    onDelete = { onDeleteQuickTask(task) }
                )
            }
        }

        // Action Button: Open Projects
        item {
            OutlinedButton(
                onClick = onNavigateToProject,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("hub_open_projects_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0284C7))
            ) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEn) "Go to Projects Manager" else "প্রজেক্ট অপশনে যান (কাজ ও কিস্তি যুক্ত করতে)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun QuickTaskWalletItem(
    task: QuickTaskEntity,
    isEn: Boolean,
    numberFormat: NumberFormat,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = task.taskTitle,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (task.clientName.isNotBlank()) {
                            Text(
                                text = "${task.clientName} • ",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Text(
                            text = task.taskDate,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "+ ৳${numberFormat.format(task.amount)}",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16A34A)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_quick_task_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete task",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
