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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExpenseCategory
import com.example.data.model.PersonIouSummary
import com.example.data.model.SplitExpenseWithParticipants
import com.example.data.model.SplitParticipantEntity
import com.example.ui.components.CategoryIconBox
import com.example.ui.components.formatCurrency
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
import java.util.Date
import java.util.Locale

@Composable
fun SplitExpensesScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val splitsWithParticipants by viewModel.splitExpensesWithParticipants.collectAsStateWithLifecycle()
    val personSummaries by viewModel.personIouSummaries.collectAsStateWithLifecycle()
    val totalOwedToMe by viewModel.totalOwedToMe.collectAsStateWithLifecycle()

    var showCreateSplitDialog by remember { mutableStateOf(false) }
    var settlingParticipant by remember { mutableStateOf<SplitParticipantEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateSplitDialog = true },
                containerColor = EmeraldPrimary,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_create_split_expense")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Split")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Split Bill", fontWeight = FontWeight.Bold)
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
                        modifier = Modifier.testTag("btn_back_split_expenses")
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
                            text = "Split & IOU Ledger",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Track shared bills, group spends & repayments",
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
                        .testTag("split_summary_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate700)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL YOU ARE OWED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                                color = EmeraldLight
                            )
                            Text(
                                text = formatCurrency(totalOwedToMe),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = IncomeGreen
                            )
                            Text(
                                text = "Across ${personSummaries.count { it.netBalance > 0 }} friends",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }

                        Surface(
                            color = EmeraldPrimary.copy(alpha = 0.15f),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
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

            // Friends Balances Strip
            if (personSummaries.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Text(
                            text = "People Owing You",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(personSummaries, key = { it.personName }) { person ->
                                PersonBalanceChip(
                                    person = person,
                                    onSettleClick = {
                                        // Find first unsettled participant for this person
                                        val part = splitsWithParticipants
                                            .flatMap { it.participants }
                                            .firstOrNull { it.personName.equals(person.personName, ignoreCase = true) && !it.isSettled }
                                        if (part != null) settlingParticipant = part
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Split Expenses List Header
            item {
                Text(
                    text = "Shared Expenses (${splitsWithParticipants.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)
                )
            }

            if (splitsWithParticipants.isEmpty()) {
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
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No split expenses logged yet",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Split restaurant bills, rent, or vacation costs with friends easily",
                                fontSize = 12.sp,
                                color = Slate400,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(splitsWithParticipants, key = { it.expense.id }) { item ->
                    SplitExpenseItemCard(
                        splitItem = item,
                        onSettleParticipant = { part -> settlingParticipant = part },
                        onDeleteExpense = { viewModel.deleteSplitExpense(item.expense.id) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    // Dialog: Create Split
    if (showCreateSplitDialog) {
        CreateSplitExpenseDialog(
            onDismiss = { showCreateSplitDialog = false },
            onConfirm = { title, totalAmt, category, friendsList, notes ->
                viewModel.createSplitExpense(
                    title = title,
                    totalAmount = totalAmt,
                    category = category,
                    participantNames = friendsList,
                    notes = notes
                )
                showCreateSplitDialog = false
            }
        )
    }

    // Dialog: Settle Up Participant
    settlingParticipant?.let { participant ->
        SettleParticipantDialog(
            participant = participant,
            onDismiss = { settlingParticipant = null },
            onFullSettle = {
                viewModel.settleIouForParticipant(participant.id)
                settlingParticipant = null
            },
            onPartialSettle = { amount ->
                viewModel.recordPartialSettlement(participant, amount)
                settlingParticipant = null
            }
        )
    }
}

@Composable
fun PersonBalanceChip(
    person: PersonIouSummary,
    onSettleClick: () -> Unit
) {
    Surface(
        color = Slate900,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (person.netBalance > 0) EmeraldPrimary.copy(alpha = 0.4f) else Slate800)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .clickable { if (person.netBalance > 0) onSettleClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(EmeraldPrimary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = person.personName.take(1).uppercase(),
                    color = EmeraldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = person.personName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
                Text(
                    text = if (person.netBalance > 0) "owes ${formatCurrency(person.netBalance)}" else "All Settled",
                    color = if (person.netBalance > 0) IncomeGreen else Slate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SplitExpenseItemCard(
    splitItem: SplitExpenseWithParticipants,
    onSettleParticipant: (SplitParticipantEntity) -> Unit,
    onDeleteExpense: () -> Unit,
    modifier: Modifier = Modifier
) {
    val expense = splitItem.expense
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(expense.date))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("split_card_${expense.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = BorderStroke(1.dp, Slate800)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBox(category = expense.category, size = 40)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = expense.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "$dateFormat • Total ${formatCurrency(expense.totalAmount)}",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                }

                IconButton(onClick = onDeleteExpense, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Slate400, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Participants Breakdown
            Text(
                text = "Participants & Status:",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate400
            )

            Spacer(modifier = Modifier.height(6.dp))

            splitItem.participants.forEach { part ->
                Surface(
                    color = SlateSurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (part.isSettled) EmeraldLight else Slate400,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = part.personName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Share: ${formatCurrency(part.amountOwed)}",
                                    fontSize = 10.sp,
                                    color = Slate400
                                )
                            }
                        }

                        if (part.isSettled) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Paid", color = EmeraldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Owes ${formatCurrency(part.remainingToSettle)}",
                                    color = ExpenseRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    modifier = Modifier.clickable { onSettleParticipant(part) },
                                    color = EmeraldPrimary,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Settle",
                                        color = Color.Black,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateSplitExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, ExpenseCategory, List<String>, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var totalAmountStr by remember { mutableStateOf("") }
    var friendsInput by remember { mutableStateOf("Rahul, Priya, Alex") }
    var notes by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategory.FOOD_DINING) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("New Split Bill", fontWeight = FontWeight.Bold, color = Color.White)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title (e.g. Goa Trip, Dinner)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Slate700
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = totalAmountStr,
                    onValueChange = { totalAmountStr = it },
                    label = { Text("Total Bill Paid by You (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Slate700
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = friendsInput,
                    onValueChange = { friendsInput = it },
                    label = { Text("Friend Names (comma-separated)") },
                    supportingText = { Text("Will split equally including you", fontSize = 10.sp, color = Slate400) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Slate700
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Venue (optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Slate700
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = totalAmountStr.toDoubleOrNull() ?: 0.0
                    val friends = friendsInput.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }

                    if (title.isNotBlank() && amt > 0 && friends.isNotEmpty()) {
                        onConfirm(title, amt, category, friends, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Create Split", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Slate400) }
        },
        containerColor = Slate900
    )
}

@Composable
fun SettleParticipantDialog(
    participant: SplitParticipantEntity,
    onDismiss: () -> Unit,
    onFullSettle: () -> Unit,
    onPartialSettle: (Double) -> Unit
) {
    var partialAmtStr by remember { mutableStateOf(participant.remainingToSettle.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Settle with ${participant.personName}", fontWeight = FontWeight.Bold, color = Color.White)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Total Remaining Owed: ${formatCurrency(participant.remainingToSettle)}",
                    fontSize = 13.sp,
                    color = EmeraldLight,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = partialAmtStr,
                    onValueChange = { partialAmtStr = it },
                    label = { Text("Payment Received (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Slate700
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pAmt = partialAmtStr.toDoubleOrNull() ?: 0.0
                    if (pAmt >= participant.remainingToSettle) {
                        onFullSettle()
                    } else if (pAmt > 0) {
                        onPartialSettle(pAmt)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Confirm Received", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Slate400) }
        },
        containerColor = Slate900
    )
}
