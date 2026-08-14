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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
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
import com.example.ui.components.MerchantAvatar
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

@Composable
fun HomeScreen(
    viewModel: FinanceViewModel,
    onNavigateToSmsStudio: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToReports: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val categoryProgress by viewModel.categoryProgressList.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonthYear.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val isScanningInbox by viewModel.isScanningInbox.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showPermissionGuideDialog by remember { mutableStateOf(false) }
    var selectedTransactionForDetail by remember { mutableStateOf<TransactionEntity?>(null) }
    var activeFilter by remember { mutableStateOf(TransactionListFilter.ALL) }

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
            // Automatically trigger inbox scan when granted
            viewModel.scanExistingInbox()
        }
    }

    val currentMonthTxns = transactions.filter {
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.timestamp)) == selectedMonth
    }

    val totalExpense = currentMonthTxns.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
    val totalIncome = currentMonthTxns.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount }
    val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
    val dailyAvg = totalExpense / dayOfMonth

    // Budget goal calculation
    val totalBudgetCap = categoryProgress.mapNotNull { it.limit }.sum().let { if (it > 0) it else 38500.0 }
    val budgetProgressRatio = (totalExpense / totalBudgetCap).toFloat().coerceIn(0f, 1f)

    // Filter transactions for list view
    val filteredTransactions = when (activeFilter) {
        TransactionListFilter.ALL -> transactions
        TransactionListFilter.EXPENSES -> transactions.filter { it.type == TransactionType.DEBIT }
        TransactionListFilter.INCOME -> transactions.filter { it.type == TransactionType.CREDIT }
    }

    // Animated dynamic total expense counter for liveliness
    val animatedTotalExpense by animateFloatAsState(
        targetValue = totalExpense.toFloat(),
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "animated_total_expense"
    )

    LazyColumn(
        modifier = modifier
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MEECREBIT VAULT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldLight,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Financial Soul",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
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

                    Surface(
                        onClick = onNavigateToSettings,
                        shape = CircleShape,
                        color = Slate800,
                        border = BorderStroke(1.dp, Slate700),
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("home_settings_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Slate200,
                                modifier = Modifier.size(18.dp)
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
                // Active SMS Interceptor Status Card with 1-tap Inbox Sync
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
                                    text = "Real bank SMS auto-saved to local Room DB",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.scanExistingInbox() },
                            enabled = !isScanningInbox,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF047857),
                                disabledContainerColor = Slate800
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("scan_device_inbox_button")
                        ) {
                            if (isScanningInbox) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scanning...", fontSize = 11.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Scan SMS Inbox",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan Inbox", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Slate800),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("overview_balance_card")
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Ambient radial glow decorative accent
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .align(Alignment.TopEnd)
                            .background(
                                color = EmeraldPrimary.copy(alpha = 0.08f),
                                shape = CircleShape
                            )
                    )

                    Column(modifier = Modifier.padding(22.dp)) {
                        Text(
                            text = "Total Spent in Rupees ($selectedMonth)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(animatedTotalExpense.toDouble()),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

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
                                    .height(30.dp)
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
                                Text(
                                    text = "${currentMonthTxns.size} Records",
                                    style = MaterialTheme.typography.titleMedium,
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
                // Monthly Goal Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToBudgets() }
                        .testTag("monthly_goal_card")
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .height(95.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "MONTHLY BUDGET",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate400,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                        Column {
                            Text(
                                text = formatCurrency(totalBudgetCap),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { budgetProgressRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = EmeraldPrimary,
                                trackColor = Slate100,
                                strokeCap = StrokeCap.Round
                            )
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

        // Filter Chips Row (All, Expenses, Income) matching Screenshot 1 with spring animations
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = filter.label,
                                color = chipTextColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Section Header: "Recent Transactions" & "View All" matching Screenshot 1
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )

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

        // Composable List View: Parsed Transactions List or Empty State
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
                            text = "No Real Transactions Yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Scan your device SMS inbox to import real bank transactions into Room database, or load realistic Indian demo data.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

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
                    }
                }
            }
        } else {
            items(
                items = filteredTransactions,
                key = { it.id }
            ) { tx ->
                ParsedSmsTransactionCard(
                    transaction = tx,
                    onClick = { selectedTransactionForDetail = tx }
                )
            }
        }
    }

    // Dialogs
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

/**
 * Composable List View Item for Transactions matching Screenshot 1 exactly
 * Displays Avatar with First Letter, Merchant Name, Category Pill, Relative Time,
 * Rupee Amount (Debit/Credit color-coded), and Balance After.
 */
@Composable
fun ParsedSmsTransactionCard(
    transaction: TransactionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDebit = transaction.type == TransactionType.DEBIT
    val initial = transaction.merchant.trim().firstOrNull()?.uppercaseChar() ?: 'M'

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

                        Text(
                            text = formatRelativeTimestamp(transaction.timestamp),
                            color = Color(0xFF717D8D),
                            fontSize = 12.sp,
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
