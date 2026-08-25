package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExpenseCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.AddEditTransactionDialog
import com.example.ui.components.CategoryIconBox
import com.example.ui.components.MeeCrebitBrandHeader
import com.example.ui.components.MerchantAvatar
import com.example.ui.components.MonthlySpendingTrendChartCard
import com.example.ui.components.PrivacyShieldBadge
import com.example.ui.components.SecuritySettingsDialog
import com.example.ui.components.SmsPermissionGuideDialog
import com.example.ui.components.TransactionDetailDialog
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatRelativeTimestamp
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.TextMuted
import com.example.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TransactionListFilter(val label: String) {
    ALL("All"),
    EXPENSES("Expenses"),
    INCOME("Income")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FinanceViewModel,
    onNavigateToSmsStudio: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToReports: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSavings: () -> Unit = {},
    onNavigateToSplits: () -> Unit = {},
    onNavigateToBills: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val categoryProgress by viewModel.categoryProgressList.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonthYear.collectAsStateWithLifecycle()
    val userCustomMonthlyBudget by viewModel.userCustomMonthlyBudget.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val isScanningInbox by viewModel.isScanningInbox.collectAsStateWithLifecycle()
    val analyticsState by viewModel.advancedAnalyticsState.collectAsStateWithLifecycle()
    val savingsGoals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    val totalSaved by viewModel.totalSavingsAccumulated.collectAsStateWithLifecycle()
    val totalOwedToMe by viewModel.totalOwedToMe.collectAsStateWithLifecycle()
    val upcomingBillsCount by viewModel.upcomingBillsCount.collectAsStateWithLifecycle()
    val upcomingBillsAmount by viewModel.totalUpcomingBillsAmount.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showPermissionGuideDialog by remember { mutableStateOf(false) }
    var showCategoryBudgetPlannerDialog by remember { mutableStateOf(false) }
    var selectedTransactionForDetail by remember { mutableStateOf<TransactionEntity?>(null) }
    var activeFilter by remember { mutableStateOf(TransactionListFilter.ALL) }
    var activeMonthFilter by remember(selectedMonth) { mutableStateOf(selectedMonth) }
    var activeCategoryFilter by remember { mutableStateOf<ExpenseCategory?>(null) }
    var showMonthPickerMenu by remember { mutableStateOf(false) }

    // Check SMS permissions dynamically
    var hasReadSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasReceiveSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val hasFullSmsPermissions = hasReadSmsPermission && hasReceiveSmsPermission

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasReadSmsPermission = permissions[Manifest.permission.READ_SMS] ?: hasReadSmsPermission
        hasReceiveSmsPermission = permissions[Manifest.permission.RECEIVE_SMS] ?: hasReceiveSmsPermission
        if (hasReadSmsPermission) {
            com.example.engine.SmsInboxObserver.startObserving(context)
            viewModel.scanExistingInbox()
        }
    }

    val availableMonths = remember(transactions) {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val months = linkedSetOf<String>()
        val curMonth = sdf.format(Date())
        months.add(curMonth)
        transactions.forEach { tx ->
            months.add(sdf.format(Date(tx.timestamp)))
        }
        months.toList().sortedDescending()
    }

    val formattedSelectedMonth = remember(selectedMonth) {
        try {
            val sdfInput = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val sdfOutput = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val parsed = sdfInput.parse(selectedMonth)
            if (parsed != null) sdfOutput.format(parsed) else selectedMonth
        } catch (_: Exception) {
            selectedMonth
        }
    }

    val currentMonthTxns = remember(transactions, selectedMonth) {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        transactions.filter {
            sdf.format(Date(it.timestamp)) == selectedMonth
        }
    }

    val totalExpense = remember(currentMonthTxns) {
        currentMonthTxns.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
    }
    val totalIncome = remember(currentMonthTxns) {
        currentMonthTxns.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount }
    }
    val dayOfMonth = remember {
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
    }
    val dailyAvg = remember(totalExpense, dayOfMonth) {
        totalExpense / dayOfMonth
    }

    // Budget goal calculation: Total monthly budget is the sum of category budgets
    val sumCategoryLimits = remember(categoryProgress) {
        categoryProgress.mapNotNull { it.limit }.sum()
    }
    val totalBudgetCap = remember(sumCategoryLimits) {
        if (sumCategoryLimits > 0) sumCategoryLimits else null
    }
    val budgetProgressRatio = remember(totalExpense, totalBudgetCap) {
        if (totalBudgetCap != null && totalBudgetCap > 0) {
            (totalExpense / totalBudgetCap).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    // Filter transactions for list view by type, month, and category
    val filteredTransactions = remember(transactions, activeFilter, activeMonthFilter, activeCategoryFilter) {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        transactions.filter { tx ->
            val matchesType = when (activeFilter) {
                TransactionListFilter.ALL -> true
                TransactionListFilter.EXPENSES -> tx.type == TransactionType.DEBIT
                TransactionListFilter.INCOME -> tx.type == TransactionType.CREDIT
            }
            val matchesMonth = if (activeMonthFilter == "ALL") true else {
                sdf.format(Date(tx.timestamp)) == activeMonthFilter
            }
            val matchesCategory = activeCategoryFilter == null || tx.category == activeCategoryFilter
            matchesType && matchesMonth && matchesCategory
        }
    }

    // Chronologically group transactions by Day (yyyy-MM-dd) precomputing summaries and time strings
    val groupedDayItems = remember(filteredTransactions) {
        val sorted = filteredTransactions.sortedByDescending { it.timestamp }
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val map = linkedMapOf<String, MutableList<TxDisplayItem>>()
        for (tx in sorted) {
            val dateKey = dayFormat.format(Date(tx.timestamp))
            val timeStr = timeFormat.format(Date(tx.timestamp))
            map.getOrPut(dateKey) { mutableListOf() }.add(TxDisplayItem(tx, timeStr))
        }
        map.map { (dateKey, items) ->
            val dayDebits = items.filter { it.transaction.type == TransactionType.DEBIT }.sumOf { it.transaction.amount }
            val dayCredits = items.filter { it.transaction.type == TransactionType.CREDIT }.sumOf { it.transaction.amount }
            DayGroupItem(
                dateKey = dateKey,
                dateHeaderTitle = formatDateHeader(dateKey),
                dayDebits = dayDebits,
                dayCredits = dayCredits,
                items = items
            )
        }
    }

    // Animated dynamic total expense counter for liveliness
    val animatedTotalExpense by animateFloatAsState(
        targetValue = totalExpense.toFloat(),
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "animated_total_expense"
    )

    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isScanningInbox,
        onRefresh = { viewModel.scanExistingInbox() },
        state = pullToRefreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isScanningInbox,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = Slate900,
                color = EmeraldLight
            )
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("home_pull_to_refresh")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Top Bar: Brand Subtitle + Title + Biometric Vault Button & Settings Icon
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MeeCrebitBrandHeader(
                    modifier = Modifier.weight(1f),
                    tagline = "100% On-Device SMS Ledger"
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Biometric Security Lock / Settings Pill
                    Surface(
                        onClick = { viewModel.lockApp() },
                        shape = RoundedCornerShape(50.dp),
                        color = if (isBiometricEnabled) Color(0xFF064E3B) else Slate800,
                        border = BorderStroke(1.dp, if (isBiometricEnabled) Color(0xFF059669) else Slate700),
                        modifier = Modifier.testTag("biometric_vault_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = if (isBiometricEnabled) Icons.Default.Lock else Icons.Default.Fingerprint,
                                contentDescription = "Security Vault",
                                tint = if (isBiometricEnabled) EmeraldLight else Slate400,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBiometricEnabled) "LOCKED" else "VAULT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isBiometricEnabled) Color.White else Slate400,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // SMS Permission Status Banner & Flow
        item {
            if (!hasFullSmsPermissions) {
                // Permission Request Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D2818)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sms_permission_request_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF047857),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Sms,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Enable Offline SMS Tracking",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Intercept real bank & UPI SMS 100% locally",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EmeraldLight,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Grant SMS permissions so meeCrebit can automatically capture your real transaction alerts (HDFC, SBI, ICICI, UPI, etc.) into your secure on-device Room database with ZERO cloud uploads.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate200,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.READ_SMS,
                                            Manifest.permission.RECEIVE_SMS
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("request_sms_permission_button")
                            ) {
                                Text(
                                    "Allow SMS Access",
                                    color = Color(0xFF04201A),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            OutlinedButton(
                                onClick = { showPermissionGuideDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sms_guide_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Guide", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            } else {
                // Active SMS Interceptor Status Card (Pull-to-Refresh Enabled)
                Surface(
                    color = Color(0xFF062018),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldLight)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "SMS Interceptor Active (Offline)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight
                                )
                                Text(
                                    text = if (isScanningInbox) "Scanning SMS inbox..." else "Pull down to scan new SMS messages",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        if (isScanningInbox) {
                            CircularProgressIndicator(
                                color = EmeraldLight,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Surface(
                                color = Color(0xFF064E3B).copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.4f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Pull to Refresh",
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Pull to Scan",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldLight
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Hero Dark Slate Card displaying Rupee Currency
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Slate800),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("overview_balance_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Top Row: Category Badge + Month Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFF062A22),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFF047857).copy(alpha = 0.6f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(EmeraldLight, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Monthly Spends",
                                    color = EmeraldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Month Switcher Controls
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF131B26))
                                    .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.goToPreviousMonth() },
                                    modifier = Modifier
                                        .size(26.dp)
                                        .testTag("prev_month_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Previous Month",
                                        tint = Slate400,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Text(
                                    text = formattedSelectedMonth,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .clickable { showMonthPickerMenu = true }
                                        .padding(horizontal = 6.dp)
                                        .testTag("selected_month_picker_btn")
                                )

                                IconButton(
                                    onClick = { viewModel.goToNextMonth() },
                                    modifier = Modifier
                                        .size(26.dp)
                                        .testTag("next_month_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Next Month",
                                        tint = Slate400,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showMonthPickerMenu,
                                onDismissRequest = { showMonthPickerMenu = false }
                            ) {
                                availableMonths.forEach { mKey ->
                                    val mLabel = try {
                                        val p = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(mKey)
                                        if (p != null) SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(p) else mKey
                                    } catch (_: Exception) {
                                        mKey
                                    }
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = mLabel,
                                                fontWeight = if (mKey == selectedMonth) FontWeight.Bold else FontWeight.Normal,
                                                color = if (mKey == selectedMonth) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            viewModel.selectMonthYear(mKey)
                                            showMonthPickerMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = formatCurrency(animatedTotalExpense.toDouble()),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )

                    if (totalIncome > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "+${formatCurrency(totalIncome)} Income received",
                                color = IncomeGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(
                        color = Color(0xFF1E2633),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DAILY AVG SPEND",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate400,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatCurrency(dailyAvg),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(Slate700)
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "THIS MONTH TXNS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate400,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = Color(0xFF062A22),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF047857).copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "${currentMonthTxns.size} Records",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2-Column Grid: Monthly Goal & Data Integrity
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Monthly Goal Card (Clickable to set/edit budget directly)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showCategoryBudgetPlannerDialog = true }
                        .testTag("monthly_goal_card")
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .height(100.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MONTHLY BUDGET",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate400,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Category Budgets",
                                tint = EmeraldLight,
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (totalBudgetCap != null) formatCurrency(totalBudgetCap) else "Tap to Set",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (totalBudgetCap != null) MaterialTheme.colorScheme.onSurface else EmeraldLight
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            if (totalBudgetCap != null) {
                                LinearProgressIndicator(
                                    progress = { budgetProgressRatio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (budgetProgressRatio >= 1f) ExpenseRed else EmeraldPrimary,
                                    trackColor = Slate100,
                                    strokeCap = StrokeCap.Round
                                )
                            } else {
                                Text(
                                    text = "Plan category budgets",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Data Integrity Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("data_integrity_card")
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .height(95.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ROOM DB PERSISTENCE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate400,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                        Column {
                            Text(
                                text = "100% On-Device",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${transactions.size} records in SQLite",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Smart Analytics & Forecast Spotlight Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToReports() }
                    .testTag("home_analytics_spotlight_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF064E3B),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoGraph,
                                        contentDescription = null,
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Smart Analytics & Forecast",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Next-month prediction & subscription audits",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Analytics",
                            tint = EmeraldLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Forecast Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "NEXT MONTH",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate400,
                                    fontSize = 9.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formatCurrency(analyticsState.spendingForecast.projectedTotal),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight
                                )
                                val slope = analyticsState.spendingForecast.momTrendSlopePercent
                                val slopeSign = if (slope > 0) "+" else ""
                                Text(
                                    text = "$slopeSign${String.format(Locale.getDefault(), "%.1f", slope)}% pace",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (slope > 0) ExpenseRed else IncomeGreen,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Subscriptions Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "RECURRING",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate400,
                                    fontSize = 9.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formatCurrency(analyticsState.subscriptionsSummary.totalMonthlyBurden),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${analyticsState.subscriptionsSummary.subscriptions.size} recurring items",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate400,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6-Month Visual Spending Trend Chart (Recharts-Inspired Outflow Comparison)
        item {
            MonthlySpendingTrendChartCard(
                trendData = analyticsState.sixMonthTrend,
                onNavigateToAnalytics = onNavigateToReports
            )
        }

        // Financial Hub Cards: Savings Pots, Split Bills & Bill Reminders
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Financial Tools & Modules",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Savings Goals Pot Card
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToSavings() }
                            .testTag("hub_card_savings"),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = EmeraldPrimary.copy(alpha = 0.2f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Savings, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text("${savingsGoals.size} Pots", fontSize = 10.sp, color = EmeraldLight, fontWeight = FontWeight.Bold)
                            }
                            Text("Savings Goals", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(formatCurrency(totalSaved), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldLight)
                        }
                    }

                    // Split & IOU Ledger Card
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToSplits() }
                            .testTag("hub_card_splits"),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text("IOU", fontSize = 10.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                            }
                            Text("Split Bills", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(if (totalOwedToMe > 0) "+${formatCurrency(totalOwedToMe)}" else "Settled", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = if (totalOwedToMe > 0) IncomeGreen else Slate400)
                        }
                    }

                    // Bill Reminders Card
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToBills() }
                            .testTag("hub_card_bills"),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text("$upcomingBillsCount Due", fontSize = 10.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                            }
                            Text("Bill Alarms", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(formatCurrency(upcomingBillsAmount), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFF59E0B))
                        }
                    }
                }
            }
        }

        // Quick Actions (SMS Playground & Manual Entry)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToSmsStudio() }
                        .testTag("action_sms_studio"),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFD1FAE5),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = "SMS Interceptor",
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SMS Studio & ML",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showAddDialog = true }
                        .testTag("action_manual_entry"),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Entry",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Manual Entry",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Filter Row 1: Type (All, Expenses, Income)
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TransactionListFilter.values().forEach { filter ->
                    val isSelected = activeFilter == filter
                    val chipBgColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else Color(0xFF1E2228),
                        animationSpec = tween(200),
                        label = "chip_bg_${filter.name}"
                    )
                    val chipTextColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF0F172A) else Color(0xFF8E9BAE),
                        animationSpec = tween(200),
                        label = "chip_text_${filter.name}"
                    )
                    val chipScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.02f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.6f),
                        label = "chip_scale_${filter.name}"
                    )

                    Surface(
                        onClick = { activeFilter = filter },
                        shape = RoundedCornerShape(50.dp),
                        color = chipBgColor,
                        modifier = Modifier
                            .scale(chipScale)
                            .testTag("filter_chip_${filter.name.lowercase()}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = filter.label,
                                color = chipTextColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Filter Row 2: Month / Period Selector Chips
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "TIME PERIOD",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // All Time Option
                    item {
                        val isSelected = activeMonthFilter == "ALL"
                        Surface(
                            onClick = { activeMonthFilter = "ALL" },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else Color(0xFF161A20),
                            border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else Color(0xFF262D37)),
                            modifier = Modifier.testTag("month_filter_all")
                        ) {
                            Text(
                                text = "All Time",
                                color = if (isSelected) EmeraldLight else Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    items(availableMonths) { monthKey ->
                        val isSelected = activeMonthFilter == monthKey
                        val formattedLabel = try {
                            val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                            val date = sdf.parse(monthKey)
                            SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(date!!)
                        } catch (_: Exception) {
                            monthKey
                        }

                        Surface(
                            onClick = {
                                activeMonthFilter = monthKey
                                viewModel.selectMonthYear(monthKey)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else Color(0xFF161A20),
                            border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else Color(0xFF262D37)),
                            modifier = Modifier.testTag("month_filter_$monthKey")
                        ) {
                            Text(
                                text = formattedLabel,
                                color = if (isSelected) EmeraldLight else Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Filter Row 3: Category Filter Carousel
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "CATEGORIES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        val isSelected = activeCategoryFilter == null
                        Surface(
                            onClick = { activeCategoryFilter = null },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF334155) else Color(0xFF161A20),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF64748B) else Color(0xFF262D37)),
                            modifier = Modifier.testTag("category_filter_all")
                        ) {
                            Text(
                                text = "All Categories",
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    items(ExpenseCategory.values()) { cat ->
                        val isSelected = activeCategoryFilter == cat
                        Surface(
                            onClick = { activeCategoryFilter = if (isSelected) null else cat },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF334155) else Color(0xFF161A20),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF64748B) else Color(0xFF262D37)),
                            modifier = Modifier.testTag("category_filter_${cat.name.lowercase()}")
                        ) {
                            Text(
                                text = cat.title,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section Header: "Transactions by Date" & Count
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFF1E2228),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${filteredTransactions.size} records",
                            color = Color(0xFF8E9BAE),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "View All",
                    color = EmeraldLight,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { onNavigateToReports() }
                        .testTag("view_all_transactions_btn")
                )
            }
        }

        // Composable List View: Date-Grouped Parsed Transactions List or Empty State
        if (filteredTransactions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFD1FAE5),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Sms,
                                    contentDescription = "No Transactions",
                                    tint = Color(0xFF047857),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Matching Transactions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (transactions.isEmpty()) {
                                "Scan your device SMS inbox to import real bank transactions into Room database, or load realistic sample data."
                            } else {
                                "No transactions matched the selected date, month, or category filters. Try clearing your filters."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (transactions.isEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        if (hasReadSmsPermission) {
                                            viewModel.scanExistingInbox()
                                        } else {
                                            permissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.READ_SMS,
                                                    Manifest.permission.RECEIVE_SMS
                                                )
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("scan_inbox_empty_state_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        tint = Color(0xFF04201A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Scan SMS Inbox", color = Color(0xFF04201A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.loadRealisticDemoData() },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("load_demo_data_button")
                                ) {
                                    Text("Load Sample Data", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    activeFilter = TransactionListFilter.ALL
                                    activeMonthFilter = "ALL"
                                    activeCategoryFilter = null
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Reset All Filters", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        } else {
            // Group transactions by date with headers
            groupedDayItems.forEach { dayGroup ->
                item(key = "header_${dayGroup.dateKey}") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dayGroup.dateHeaderTitle,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (dayGroup.dayDebits > 0) {
                                Text(
                                    text = "-${formatCurrency(dayGroup.dayDebits)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFEF4444)
                                )
                            }
                            if (dayGroup.dayCredits > 0) {
                                Text(
                                    text = "+${formatCurrency(dayGroup.dayCredits)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldLight
                                )
                            }
                        }
                    }
                }

                items(
                    items = dayGroup.items,
                    key = { it.transaction.id }
                ) { displayItem ->
                    ParsedSmsTransactionCard(
                        transaction = displayItem.transaction,
                        formattedTime = displayItem.formattedTime,
                        onClick = { selectedTransactionForDetail = displayItem.transaction }
                    )
                }
            }
        }
    }
}

    // Dialogs
    if (showCategoryBudgetPlannerDialog) {
        com.example.ui.components.CategoryMonthlyBudgetPlannerDialog(
            initialCategoryLimits = categoryProgress.filter { it.limit != null }.associate { it.category to it.limit!! },
            categoryCurrentSpent = categoryProgress.associate { it.category to it.spent },
            selectedMonthYear = selectedMonth,
            onDismiss = { showCategoryBudgetPlannerDialog = false },
            onSaveCategoryBudgets = { newCategoryBudgets ->
                viewModel.setBatchBudgetLimits(newCategoryBudgets, selectedMonth)
                showCategoryBudgetPlannerDialog = false
            },
            onCopyFromPreviousMonth = {
                viewModel.copyBudgetsFromPreviousMonth()
            }
        )
    }

    if (showAddDialog) {
        AddEditTransactionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { amount, type, merchant, category, account, bank ->
                viewModel.addManualTransaction(amount, type, merchant, category, account, bank)
            }
        )
    }

    if (showPermissionGuideDialog) {
        SmsPermissionGuideDialog(
            onDismiss = { showPermissionGuideDialog = false },
            onOpenAppSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        )
    }

    if (showSecurityDialog) {
        SecuritySettingsDialog(
            isBiometricEnabled = isBiometricEnabled,
            onToggleBiometric = { enabled ->
                viewModel.setBiometricEnabled(enabled)
            },
            onLockNow = {
                viewModel.lockApp()
            },
            biometricStatus = viewModel.getBiometricStatus(),
            onDismiss = { showSecurityDialog = false }
        )
    }

    selectedTransactionForDetail?.let { tx ->
        TransactionDetailDialog(
            transaction = tx,
            onDismiss = { selectedTransactionForDetail = null },
            onDelete = {
                viewModel.deleteTransaction(tx.id)
                selectedTransactionForDetail = null
            },
            onUpdateCategory = { newCategory ->
                viewModel.updateTransaction(tx.copy(category = newCategory))
                selectedTransactionForDetail = null
            }
        )
    }
}

data class DayGroupItem(
    val dateKey: String,
    val dateHeaderTitle: String,
    val dayDebits: Double,
    val dayCredits: Double,
    val items: List<TxDisplayItem>
)

data class TxDisplayItem(
    val transaction: TransactionEntity,
    val formattedTime: String
)

/**
 * Composable List View Item for Transactions matching Screenshot 1 exactly
 * Displays Avatar with First Letter, Merchant Name, Category Pill, Relative Time,
 * Rupee Amount (Debit/Credit color-coded), and Balance After.
 */
@Composable
fun ParsedSmsTransactionCard(
    transaction: TransactionEntity,
    formattedTime: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDebit = transaction.type == TransactionType.DEBIT
    val initial = transaction.merchant.trim().firstOrNull()?.uppercaseChar() ?: 'M'
    val displayTime = if (formattedTime.isNotBlank()) formattedTime else {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(transaction.timestamp))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
            .testTag("tx_item_${transaction.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Merchant Circular Avatar (48dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = Color(0xFF1E2228),
                    border = BorderStroke(1.dp, Color(0xFF282F39))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initial.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Middle: Merchant Name & (Category Pill + Relative Date)
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = transaction.merchant,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Subtle Category Pill
                        Surface(
                            color = Color(0xFF1E2228),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = transaction.category.title,
                                color = Color(0xFFB0BDD0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }

                        if (transaction.bankName.isNotBlank() && transaction.bankName != "Bank") {
                            Text(
                                text = "•  ${transaction.bankName}",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = "•  $displayTime",
                            color = Color(0xFF717D8D),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right: Amount and Balance After
            Column(horizontalAlignment = Alignment.End) {
                val sign = if (isDebit) "-" else "+"
                val amountColor = if (isDebit) Color.White else EmeraldLight

                Text(
                    text = "$sign${formatCurrency(transaction.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = amountColor
                )

                Spacer(modifier = Modifier.height(3.dp))

                val bal = transaction.balanceAfter ?: 0.0
                Text(
                    text = "Bal: ${formatCurrency(bal)}",
                    color = Color(0xFF717D8D),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(
            color = Color(0xFF181D24),
            thickness = 1.dp
        )
    }
}

fun formatDateHeader(dateKey: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(dateKey) ?: return dateKey
        val todayStr = sdf.format(Date())
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayStr = sdf.format(cal.time)

        val displayFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val formattedDate = displayFmt.format(date)

        when (dateKey) {
            todayStr -> "Today • $formattedDate"
            yesterdayStr -> "Yesterday • $formattedDate"
            else -> {
                val dayOfWeekFmt = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
                dayOfWeekFmt.format(date)
            }
        }
    } catch (_: Exception) {
        dateKey
    }
}

