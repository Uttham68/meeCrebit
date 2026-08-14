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

