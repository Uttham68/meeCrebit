package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BillFrequency
import com.example.data.model.BillReminderEntity
import com.example.data.model.BillReminderType
import com.example.data.model.BillStatus
import com.example.notification.NotificationHelper
import com.example.ui.components.formatCurrency
import com.example.ui.theme.AccentGold
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SlateSurfaceVariant
import com.example.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class BillFilterTab(val title: String) {
    ALL("All Bills"),
    UPCOMING("Upcoming"),
    RECURRING("Recurring"),
    PAID("Paid History")
}

@Composable
fun BillRemindersScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reminders by viewModel.billReminders.collectAsStateWithLifecycle()
    val upcomingCount by viewModel.upcomingBillsCount.collectAsStateWithLifecycle()
    val totalUpcomingAmount by viewModel.totalUpcomingBillsAmount.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedFilterTab by remember { mutableStateOf(BillFilterTab.ALL) }
    var showAddBillDialog by remember { mutableStateOf(false) }
    var editingBill by remember { mutableStateOf<BillReminderEntity?>(null) }

    val filteredReminders = remember(reminders, selectedFilterTab) {
        when (selectedFilterTab) {
            BillFilterTab.ALL -> reminders
            BillFilterTab.UPCOMING -> reminders.filter { !it.isPaid }
            BillFilterTab.RECURRING -> reminders.filter { it.frequency != BillFrequency.ONE_TIME }
            BillFilterTab.PAID -> reminders.filter { it.isPaid }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingBill = null
                    showAddBillDialog = true
                },
                containerColor = EmeraldPrimary,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_bill_reminder")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Bill")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Reminder", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_bill_reminders")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Bill & Subscription Reminders",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Due dates, recurring cycle schedules & alarms",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }
            }

            // Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("bill_summary_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate700)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "UPCOMING BILL OBLIGATIONS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp,
                                    color = EmeraldLight
                                )
                                Text(
                                    text = formatCurrency(totalUpcomingAmount),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "$upcomingCount bills due this cycle",
                                    fontSize = 12.sp,
                                    color = Slate400
                                )
                            }

                            Surface(
                                color = EmeraldPrimary.copy(alpha = 0.15f),
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .size(28.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Filter Tabs
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(BillFilterTab.values()) { tab ->
                        val isSelected = selectedFilterTab == tab
                        Surface(
                            modifier = Modifier.clickable { selectedFilterTab = tab },
                            color = if (isSelected) EmeraldPrimary else Slate900,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else Slate800)
                        ) {
                            Text(
                                text = tab.title,
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            if (filteredReminders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        border = BorderStroke(1.dp, Slate800)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No bills found in this view",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+ Add Reminder' to schedule bills, credit cards or OTT subscriptions",
                                fontSize = 12.sp,
                                color = Slate400,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredReminders, key = { it.id }) { reminder ->
                    BillReminderItemCard(
                        reminder = reminder,
                        onMarkPaid = { viewModel.markBillAsPaid(reminder, logAsTransaction = true) },
                        onEdit = {
                            editingBill = reminder
                            showAddBillDialog = true
                        },
                        onDelete = { viewModel.deleteBillReminder(reminder.id) },
                        onTestAlert = { NotificationHelper.notifyBillDue(context, reminder) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    if (showAddBillDialog) {
        AddEditBillReminderDialog(
            existingBill = editingBill,
            onDismiss = {
                showAddBillDialog = false
                editingBill = null
            },
            onSave = { title, amount, dueDate, frequency, reminderType, biller, autoPay, notes ->
                viewModel.addOrUpdateBillReminder(
                    id = editingBill?.id ?: 0L,
                    title = title,
                    amount = amount,
                    dueDate = dueDate,
                    frequency = frequency,
                    reminderType = reminderType,
                    billerOrBank = biller,
                    autoPayEnabled = autoPay,
                    notes = notes
                )
                showAddBillDialog = false
                editingBill = null
            }
        )
    }
}

@Composable
fun BillReminderItemCard(
    reminder: BillReminderEntity,
    onMarkPaid: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTestAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typeColor = Color(reminder.reminderType.hexColor)
    val dueDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(reminder.dueDate))

    val statusBadgeColor: Color
    val statusText: String
    when (reminder.status) {
        BillStatus.OVERDUE -> {
            statusBadgeColor = ExpenseRed
            statusText = "OVERDUE"
        }
        BillStatus.DUE_TODAY -> {
            statusBadgeColor = AccentGold
            statusText = "DUE TODAY"
        }
        BillStatus.DUE_SOON -> {
            statusBadgeColor = AccentGold
            statusText = "DUE IN ${reminder.daysUntilDue} DAYS"
        }
        BillStatus.UPCOMING -> {
            statusBadgeColor = EmeraldLight
            statusText = "DUE IN ${reminder.daysUntilDue} DAYS"
        }
        BillStatus.PAID -> {
            statusBadgeColor = IncomeGreen
            statusText = "PAID"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("bill_card_${reminder.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = BorderStroke(1.dp, if (reminder.status == BillStatus.OVERDUE) ExpenseRed.copy(alpha = 0.6f) else Slate800)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(typeColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = reminder.reminderType.getIcon(),
                            contentDescription = reminder.reminderType.title,
                            tint = typeColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = reminder.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${reminder.billerOrBank.ifBlank { reminder.reminderType.title }} • ${reminder.frequency.title}",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                }

                Surface(
                    color = statusBadgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusBadgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Amount Due",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                    Text(
                        text = formatCurrency(reminder.amount),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Due Date",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                    Text(
                        text = dueDateFormat,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    IconButton(onClick = onTestAlert, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = "Test Alert", tint = Slate400, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Slate400, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Slate400, modifier = Modifier.size(16.dp))
                    }
                }

                if (!reminder.isPaid) {
                    Button(
                        onClick = onMarkPaid,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(34.dp).testTag("btn_mark_paid_${reminder.id}")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark as Paid", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBillReminderDialog(
    existingBill: BillReminderEntity?,
    onDismiss: () -> Unit,
    onSave: (String, Double, Long, BillFrequency, BillReminderType, String, Boolean, String) -> Unit
) {
    var title by remember { mutableStateOf(existingBill?.title ?: "") }
    var amountStr by remember { mutableStateOf(existingBill?.amount?.toString() ?: "") }
    var daysUntilDue by remember { mutableStateOf(existingBill?.daysUntilDue?.toInt()?.coerceAtLeast(1) ?: 5) }
    var selectedFrequency by remember { mutableStateOf(existingBill?.frequency ?: BillFrequency.MONTHLY) }
    var selectedType by remember { mutableStateOf(existingBill?.reminderType ?: BillReminderType.CREDIT_CARD) }
    var biller by remember { mutableStateOf(existingBill?.billerOrBank ?: "") }
    var autoPay by remember { mutableStateOf(existingBill?.autoPayEnabled ?: false) }
    var notes by remember { mutableStateOf(existingBill?.notes ?: "") }

    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var freqDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingBill == null) "Schedule Bill Reminder" else "Edit Bill Reminder",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill Name (e.g. HDFC Card, Rent)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Slate700
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Amount Due (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Slate700
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = biller,
                        onValueChange = { biller = it },
                        label = { Text("Provider/Bank") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Slate700
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Bill Type Selector
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType.title,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Bill Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Slate700
                        ),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        BillReminderType.values().forEach { bType ->
                            DropdownMenuItem(
                                text = { Text(bType.title) },
                                onClick = {
                                    selectedType = bType
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Frequency Selector
                ExposedDropdownMenuBox(
                    expanded = freqDropdownExpanded,
                    onExpandedChange = { freqDropdownExpanded = !freqDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedFrequency.title,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Billing Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = freqDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Slate700
                        ),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = freqDropdownExpanded,
                        onDismissRequest = { freqDropdownExpanded = false }
                    ) {
                        BillFrequency.values().forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq.title) },
                                onClick = {
                                    selectedFrequency = freq
                                    freqDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Due in Days Slider
                Column {
                    Text(
                        text = "Due In: $daysUntilDue Day${if (daysUntilDue > 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = EmeraldLight,
                        fontWeight = FontWeight.Bold
                    )
                    androidx.compose.material3.Slider(
                        value = daysUntilDue.toFloat(),
                        onValueChange = { daysUntilDue = it.toInt() },
                        valueRange = 1f..31f,
                        steps = 30
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto-Debit Enabled", fontSize = 12.sp, color = Color.White)
                    Switch(
                        checked = autoPay,
                        onCheckedChange = { autoPay = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0) {
                        val cal = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, daysUntilDue)
                        }
                        onSave(title, amt, cal.timeInMillis, selectedFrequency, selectedType, biller, autoPay, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Schedule Bill", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Slate400) }
        },
        containerColor = Slate900
    )
}
