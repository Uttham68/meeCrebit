package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.Slate400
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    onDismiss: () -> Unit,
    onSave: (amount: Double, type: TransactionType, merchant: String, category: ExpenseCategory, account: String, bank: String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var merchantStr by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.DEBIT) }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD_DINING) }
    var accountStr by remember { mutableStateOf("XX9123") }
    var bankStr by remember { mutableStateOf("HDFC Bank") }
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Manual Offline Entry",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type Selector (Debit vs Credit)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedType = TransactionType.DEBIT }
                            .testTag("type_debit_tab"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedType == TransactionType.DEBIT) ExpenseRed.copy(alpha = 0.2f) else SlateSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selectedType == TransactionType.DEBIT) ExpenseRed else SlateBorder
                        )
                    ) {
                        Text(
                            text = "Debit (Expense)",
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedType == TransactionType.DEBIT) ExpenseRed else TextSecondary
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedType = TransactionType.CREDIT }
                            .testTag("type_credit_tab"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedType == TransactionType.CREDIT) IncomeGreen.copy(alpha = 0.2f) else SlateSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selectedType == TransactionType.CREDIT) IncomeGreen else SlateBorder
                        )
                    ) {
                        Text(
                            text = "Credit (Income)",
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedType == TransactionType.CREDIT) IncomeGreen else TextSecondary
                        )
                    }
                }

                // Amount Field
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amount_input"),
                    singleLine = true
                )

                // Merchant / Payee
                OutlinedTextField(
                    value = merchantStr,
                    onValueChange = { merchantStr = it },
                    label = { Text(if (selectedType == TransactionType.DEBIT) "Merchant / Payee" else "Source / Employer") },
                    placeholder = { Text("e.g. Swiggy, Starbucks, Amazon") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("merchant_input"),
                    singleLine = true
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.title,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expense Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        ExpenseCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CategoryIconBox(category = cat, size = 28)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(cat.title)
                                    }
                                },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Bank & Account Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = bankStr,
                        onValueChange = { bankStr = it },
                        label = { Text("Bank") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = accountStr,
                        onValueChange = { accountStr = it },
                        label = { Text("A/C Ending") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onSave(amt, selectedType, merchantStr, selectedCategory, accountStr, bankStr)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("save_transaction_button")
            ) {
                Text("Save Offline", color = Color(0xFF04201A), fontWeight = FontWeight.Bold)
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
fun TransactionDetailDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onUpdateCategory: (ExpenseCategory) -> Unit
) {
    var isCategoryDropdownOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Transaction",
                        tint = ExpenseRed
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with amount and type
                Surface(
                    color = SlateSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (transaction.type == TransactionType.DEBIT) "-${formatCurrency(transaction.amount)}" else "+${formatCurrency(transaction.amount)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (transaction.type == TransactionType.DEBIT) ExpenseRed else IncomeGreen
                        )
                        Text(
                            text = transaction.merchant,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatTimestamp(transaction.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                // Category & Reassign
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlateSurfaceVariant)
                        .border(1.dp, SlateBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryIconBox(category = transaction.category, size = 32)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = transaction.category.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Bank & Account Details
                Text(
                    text = "Account & Origin",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = SlateSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Bank", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(transaction.bankName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(
                        color = SlateSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Account", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(transaction.accountNumber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Raw SMS audit (Offline Proof)
                if (!transaction.rawSmsBody.isNullOrBlank()) {
                    Text(
                        text = "Raw Intercepted SMS (Offline Evidence)",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Surface(
                        color = Color(0xFF061410),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sms,
                                contentDescription = "SMS",
                                tint = EmeraldLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = transaction.rawSmsBody,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = EmeraldLight,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Close", color = Color(0xFF04201A), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun CategoryMonthlyBudgetPlannerDialog(
    initialCategoryLimits: Map<ExpenseCategory, Double>,
    categoryCurrentSpent: Map<ExpenseCategory, Double> = emptyMap(),
    selectedMonthYear: String,
    onDismiss: () -> Unit,
    onSaveCategoryBudgets: (Map<ExpenseCategory, Double>) -> Unit,
    onCopyFromPreviousMonth: (() -> Unit)? = null
) {
    val activeCategories = remember {
        ExpenseCategory.values().filter { it != ExpenseCategory.SALARY_INCOME }
    }

    val categoryInputs = remember {
        androidx.compose.runtime.mutableStateMapOf<ExpenseCategory, String>().apply {
            activeCategories.forEach { cat ->
                val current = initialCategoryLimits[cat]
                put(cat, if (current != null && current > 0) current.toInt().toString() else "")
            }
        }
    }

    val totalSum = categoryInputs.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
    val activeCount = categoryInputs.values.count { (it.toDoubleOrNull() ?: 0.0) > 0 }

    val formattedMonth = remember(selectedMonthYear) {
        try {
            val sdfInput = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
            val sdfOutput = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
            val parsed = sdfInput.parse(selectedMonthYear)
            if (parsed != null) sdfOutput.format(parsed) else selectedMonthYear
        } catch (_: Exception) {
            selectedMonthYear
        }
    }

    // Helper to apply preset distribution
    fun applyPreset(presetMap: Map<ExpenseCategory, Double>) {
        activeCategories.forEach { cat ->
            val amount = presetMap[cat] ?: 0.0
            categoryInputs[cat] = if (amount > 0) amount.toInt().toString() else ""
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF047857),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Monthly Budget Planner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Set category limits → Sums to Monthly Budget",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldLight,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Total Summary Card
                Surface(
                    color = com.example.ui.theme.Slate900,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL MONTHLY BUDGET",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.Slate400,
                                letterSpacing = 1.2.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldPrimary.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = formattedMonth,
                                    color = EmeraldLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = formatCurrency(totalSum),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (totalSum > 0) EmeraldLight else com.example.ui.theme.Slate400
                            )
                            Text(
                                text = if (activeCount > 0) "$activeCount categories budgeted" else "Enter category limits below",
                                style = MaterialTheme.typography.labelSmall,
                                color = com.example.ui.theme.Slate400
                            )
                        }
                    }
                }

                // Quick Presets Section
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick Presets / Auto-allocate:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.Slate400
                        )
                        if (onCopyFromPreviousMonth != null) {
                            Text(
                                text = "Copy Last Month",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldLight,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    onCopyFromPreviousMonth()
                                    onDismiss()
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("₹25k", "Essential", mapOf(
                                ExpenseCategory.FOOD_DINING to 6000.0,
                                ExpenseCategory.GROCERIES to 5000.0,
                                ExpenseCategory.TRANSPORT to 3000.0,
                                ExpenseCategory.BILLS_UTILITIES to 4000.0,
                                ExpenseCategory.SHOPPING to 3000.0,
                                ExpenseCategory.HEALTH_FITNESS to 2000.0,
                                ExpenseCategory.OTHERS to 2000.0
                            )),
                            Triple("₹50k", "Balanced", mapOf(
                                ExpenseCategory.FOOD_DINING to 12000.0,
                                ExpenseCategory.GROCERIES to 9000.0,
                                ExpenseCategory.TRANSPORT to 5000.0,
                                ExpenseCategory.BILLS_UTILITIES to 7000.0,
                                ExpenseCategory.SHOPPING to 6000.0,
                                ExpenseCategory.ENTERTAINMENT to 4000.0,
                                ExpenseCategory.HEALTH_FITNESS to 3000.0,
                                ExpenseCategory.INVESTMENTS to 3000.0,
                                ExpenseCategory.OTHERS to 1000.0
                            )),
                            Triple("₹75k", "Comfort", mapOf(
                                ExpenseCategory.FOOD_DINING to 18000.0,
                                ExpenseCategory.GROCERIES to 12000.0,
                                ExpenseCategory.TRANSPORT to 8000.0,
                                ExpenseCategory.BILLS_UTILITIES to 10000.0,
                                ExpenseCategory.SHOPPING to 10000.0,
                                ExpenseCategory.ENTERTAINMENT to 6000.0,
                                ExpenseCategory.HEALTH_FITNESS to 5000.0,
                                ExpenseCategory.INVESTMENTS to 5000.0,
                                ExpenseCategory.OTHERS to 1000.0
                            )),
                            Triple("₹1L", "Premium", mapOf(
                                ExpenseCategory.FOOD_DINING to 25000.0,
                                ExpenseCategory.GROCERIES to 16000.0,
                                ExpenseCategory.TRANSPORT to 12000.0,
                                ExpenseCategory.BILLS_UTILITIES to 14000.0,
                                ExpenseCategory.SHOPPING to 12000.0,
                                ExpenseCategory.ENTERTAINMENT to 8000.0,
                                ExpenseCategory.HEALTH_FITNESS to 6000.0,
                                ExpenseCategory.INVESTMENTS to 5000.0,
                                ExpenseCategory.OTHERS to 2000.0
                            ))
                        ).forEach { (label, sub, preset) ->
                            Surface(
                                onClick = { applyPreset(preset) },
                                shape = RoundedCornerShape(10.dp),
                                color = com.example.ui.theme.SlateSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.SlateBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldLight
                                    )
                                    Text(
                                        text = sub,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = com.example.ui.theme.Slate400,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Category List
                Text(
                    text = "Category Limits:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = com.example.ui.theme.Slate400,
                    letterSpacing = 1.sp
                )

                activeCategories.forEach { category ->
                    val currentVal = categoryInputs[category] ?: ""
                    val spentThisMonth = categoryCurrentSpent[category] ?: 0.0

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CategoryIconBox(category = category, size = 28)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = category.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (spentThisMonth > 0) {
                                    Text(
                                        text = "Spent: ${formatCurrency(spentThisMonth)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = com.example.ui.theme.Slate400,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = currentVal,
                                    onValueChange = { input ->
                                        categoryInputs[category] = input.filter { it.isDigit() }
                                    },
                                    placeholder = { Text("0", color = com.example.ui.theme.Slate400) },
                                    prefix = { Text("₹ ", color = EmeraldLight, fontWeight = FontWeight.Bold) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("budget_input_${category.name}"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = EmeraldLight,
                                        cursorColor = EmeraldLight
                                    )
                                )

                                // Quick increment chips
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(1000, 5000).forEach { inc ->
                                        Surface(
                                            onClick = {
                                                val existing = categoryInputs[category]?.toDoubleOrNull() ?: 0.0
                                                categoryInputs[category] = (existing + inc).toInt().toString()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            color = com.example.ui.theme.SlateSurfaceVariant,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.SlateBorder)
                                        ) {
                                            Text(
                                                text = "+₹${inc / 1000}k",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = EmeraldLight,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    if (currentVal.isNotEmpty()) {
                                        Surface(
                                            onClick = { categoryInputs[category] = "" },
                                            shape = RoundedCornerShape(8.dp),
                                            color = com.example.ui.theme.ExpenseRed.copy(alpha = 0.1f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.ExpenseRed.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = "✕",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = com.example.ui.theme.ExpenseRed,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val resultMap = mutableMapOf<ExpenseCategory, Double>()
                    activeCategories.forEach { cat ->
                        val amount = categoryInputs[cat]?.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            resultMap[cat] = amount
                        }
                    }
                    onSaveCategoryBudgets(resultMap)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_category_budgets_button")
            ) {
                Text(
                    text = "Save Monthly Budget (${formatCurrency(totalSum)})",
                    color = Color(0xFF04201A),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = com.example.ui.theme.Slate400)
            }
        }
    )
}

@Composable
fun SetOverallMonthlyBudgetDialog(
    currentBudget: Double?,
    onDismiss: () -> Unit,
    onSaveBudget: (Double?) -> Unit
) {
    // Legacy fallback redirecting to default preset if invoked directly
    val defaultCategoryMap = mapOf(
        ExpenseCategory.FOOD_DINING to ((currentBudget ?: 30000.0) * 0.25),
        ExpenseCategory.GROCERIES to ((currentBudget ?: 30000.0) * 0.20),
        ExpenseCategory.TRANSPORT to ((currentBudget ?: 30000.0) * 0.15),
        ExpenseCategory.BILLS_UTILITIES to ((currentBudget ?: 30000.0) * 0.15),
        ExpenseCategory.SHOPPING to ((currentBudget ?: 30000.0) * 0.15),
        ExpenseCategory.OTHERS to ((currentBudget ?: 30000.0) * 0.10)
    )
    CategoryMonthlyBudgetPlannerDialog(
        initialCategoryLimits = defaultCategoryMap,
        selectedMonthYear = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date()),
        onDismiss = onDismiss,
        onSaveCategoryBudgets = { map ->
            onSaveBudget(map.values.sum())
        }
    )
}

@Composable
fun EditBudgetDialog(
    category: ExpenseCategory,
    currentLimit: Double?,
    onDismiss: () -> Unit,
    onSaveLimit: (Double) -> Unit
) {
    var limitStr by remember { mutableStateOf(if (currentLimit != null && currentLimit > 0) currentLimit.toString() else "300.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryIconBox(category = category, size = 32)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Set Budget: ${category.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Set a monthly spending threshold. meeCrebit will alert you when approaching 80% and 100% of this limit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { limitStr = it },
                    label = { Text("Monthly Limit (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_limit_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitStr.toDoubleOrNull() ?: 0.0
                    if (limit > 0) {
                        onSaveLimit(limit)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("save_budget_button")
            ) {
                Text("Update Limit", color = Color(0xFF04201A), fontWeight = FontWeight.Bold)
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
fun SecuritySettingsDialog(
    isBiometricEnabled: Boolean,
    onToggleBiometric: (Boolean) -> Unit,
    onLockNow: () -> Unit,
    biometricStatus: com.example.security.BiometricStatus,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF064E3B),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Security & Biometrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ledger Protection",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldLight
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "meeCrebit protects your private financial transactions and account balances directly on device with hardware-backed Android biometric security.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Surface(
                    color = SlateSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = EmeraldLight,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Biometric Lock",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Require fingerprint/PIN on launch",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = onToggleBiometric,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF04201A),
                                checkedTrackColor = EmeraldPrimary
                            ),
                            modifier = Modifier.testTag("toggle_biometric_switch")
                        )
                    }
                }

                // Sensor Status Chip
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "DEVICE BIOMETRICS STATUS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (biometricStatus) {
                                is com.example.security.BiometricStatus.Available -> "✓ Hardware Biometric Ready (Fingerprint/Face/PIN enrolled)"
                                is com.example.security.BiometricStatus.NotEnrolled -> "⚠️ No biometrics enrolled. Device PIN/Password will be used."
                                is com.example.security.BiometricStatus.Unsupported -> "⚠️ No biometric sensor detected. Device Credentials active."
                                is com.example.security.BiometricStatus.HardwareUnavailable -> "⚠️ Biometric sensor busy or unavailable."
                                is com.example.security.BiometricStatus.Unknown -> "Device credential protection active."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (biometricStatus is com.example.security.BiometricStatus.Available) EmeraldLight else Color(0xFFFBBF24),
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }

                // Instant Lock Button
                if (isBiometricEnabled) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onLockNow()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lock_app_now_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lock Vault Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("close_security_dialog_button")
            ) {
                Text("Done", color = Color(0xFF04201A), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun SmsPermissionGuideDialog(
    onDismiss: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF064E3B),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Android SMS Setup Guide",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "How to enable SMS permissions",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldLight
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "meeCrebit requires SMS permissions to intercept incoming bank transaction alerts and scan your inbox for offline financial tracking.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                // 100% Offline Guarantee card
                Surface(
                    color = Color(0xFF061410),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "100% Offline: This app has NO Internet permission. Your SMS messages never leave your device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "Step-by-step Android Instructions:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val steps = listOf(
                    "1. Tap the 'Open Android Settings' button below.",
                    "2. On the App Info page, tap 'Permissions'.",
                    "3. Select 'SMS' (or 'Messages').",
                    "4. Choose 'Allow' or 'Allow all the time'.",
                    "5. Return to meeCrebit to automatically start offline transaction logging."
                )

                steps.forEach { stepText ->
                    Surface(
                        color = SlateSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stepText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onOpenAppSettings()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("open_app_settings_button")
            ) {
                Text("Open Android Settings", color = Color(0xFF04201A), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

