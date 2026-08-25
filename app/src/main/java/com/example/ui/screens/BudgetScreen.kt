package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExpenseCategory
import com.example.ui.components.CategoryIconBox
import com.example.ui.components.EditBudgetDialog
import com.example.ui.components.PrivacyShieldBadge
import com.example.ui.components.SleekProgressBar
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
import com.example.viewmodel.CategorySpendProgress
import com.example.viewmodel.FinanceViewModel
import java.util.Locale

@Composable
fun BudgetScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val categoryProgressList by viewModel.categoryProgressList.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonthYear.collectAsStateWithLifecycle()

    var editingCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var currentEditingLimit by remember { mutableStateOf<Double?>(null) }
    var showCategoryBudgetPlannerDialog by remember { mutableStateOf(false) }

    val sumCategoryLimits = categoryProgressList.mapNotNull { it.limit }.sum()
    val effectiveBudgetCap = sumCategoryLimits
    val totalSpentMonth = categoryProgressList.sumOf { it.spent }
    val overallProgress = if (effectiveBudgetCap > 0) (totalSpentMonth / effectiveBudgetCap).toFloat() else 0f

    if (showCategoryBudgetPlannerDialog) {
        com.example.ui.components.CategoryMonthlyBudgetPlannerDialog(
            initialCategoryLimits = categoryProgressList.filter { it.limit != null }.associate { it.category to it.limit!! },
            categoryCurrentSpent = categoryProgressList.associate { it.category to it.spent },
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
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
                        text = "Dynamic Budget Planner",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Set category limits & track sliding progress ($selectedMonth)",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldLight
                    )
                }

                androidx.compose.material3.FilledTonalButton(
                    onClick = { showCategoryBudgetPlannerDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                        containerColor = SlateSurfaceVariant,
                        contentColor = EmeraldLight
                    ),
                    modifier = Modifier.testTag("set_budget_header_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Plan Monthly Budgets",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Plan Budgets", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Month Selector Bar
        item {
            val formattedMonth = remember(selectedMonth) {
                try {
                    val sdfInput = java.text.SimpleDateFormat("yyyy-MM", Locale.getDefault())
                    val sdfOutput = java.text.SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    val parsed = sdfInput.parse(selectedMonth)
                    if (parsed != null) sdfOutput.format(parsed) else selectedMonth
                } catch (_: Exception) {
                    selectedMonth
                }
            }
            Surface(
                color = Slate900,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BUDGET PERIOD",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.2.sp
                    )

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
                                .size(28.dp)
                                .testTag("budget_prev_month_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Month",
                                tint = Slate400,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Text(
                            text = formattedMonth,
                            color = EmeraldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        IconButton(
                            onClick = { viewModel.goToNextMonth() },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("budget_next_month_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Month",
                                tint = Slate400,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Overall Budget Health Meter (Sleek Dark Hero)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Slate800),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategoryBudgetPlannerDialog = true }
                    .testTag("overall_budget_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL MONTHLY BUDGET (CATEGORY SUM)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate400,
                            letterSpacing = 1.5.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Categories",
                                tint = EmeraldLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Plan All",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("Total Spent ($selectedMonth)", style = MaterialTheme.typography.labelSmall, color = Slate400)
                            Text(
                                text = formatCurrency(totalSpentMonth),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (totalSpentMonth > effectiveBudgetCap && effectiveBudgetCap > 0) ExpenseRed else Color.White
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Monthly Budget", style = MaterialTheme.typography.labelSmall, color = Slate400)
                            Text(
                                text = if (effectiveBudgetCap > 0) formatCurrency(effectiveBudgetCap) else "Tap to Set",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    SleekProgressBar(
                        progress = overallProgress.coerceIn(0f, 1f),
                        isOverBudget = totalSpentMonth > effectiveBudgetCap && effectiveBudgetCap > 0
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (effectiveBudgetCap > 0) {
                            val remaining = effectiveBudgetCap - totalSpentMonth
                            Text(
                                text = if (remaining >= 0) "Remaining: ${formatCurrency(remaining)}" else "Overspent: ${formatCurrency(-remaining)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (remaining >= 0) IncomeGreen else ExpenseRed
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f%% utilized", (totalSpentMonth / effectiveBudgetCap) * 100),
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                        } else {
                            Text(
                                text = "No budget configured yet",
                                style = MaterialTheme.typography.labelMedium,
                                color = Slate400
                            )
                            Text(
                                text = "Tap here to configure",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Notification Alerts Guard
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notification_guard_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EmeraldPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Notification Guard",
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Real-Time Notification Guard",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Instant notifications on every logged transaction & high-priority alert when monthly limits are exceeded.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.testSendTransactionNotification() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_txn_notification_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Test Txn Alert",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldLight
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.testSendBudgetExceededNotification() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_budget_notification_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Test Limit Alert",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ExpenseRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category Budgets Header
        item {
            Text(
                text = "CATEGORY LIMITS & LIVE GAUGES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        items(categoryProgressList) { item ->
            SleekBudgetCategoryCard(
                progressItem = item,
                onEditClick = {
                    editingCategory = item.category
                    currentEditingLimit = item.limit
                }
            )
        }
    }

    // Edit Budget Dialog
    editingCategory?.let { cat ->
        EditBudgetDialog(
            category = cat,
            currentLimit = currentEditingLimit,
            onDismiss = { editingCategory = null },
            onSaveLimit = { newLimit ->
                viewModel.setBudgetLimit(cat, newLimit)
                editingCategory = null
            }
        )
    }
}

@Composable
fun SleekBudgetCategoryCard(
    progressItem: CategorySpendProgress,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            if (progressItem.isOverBudget) ExpenseRed.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("budget_card_${progressItem.category.name}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBox(category = progressItem.category, size = 36)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = progressItem.category.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (progressItem.limit != null) "Limit: ${formatCurrency(progressItem.limit)}" else "No limit configured",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (progressItem.isOverBudget) {
                        Surface(
                            color = ExpenseRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "OVERBUDGET",
                                style = MaterialTheme.typography.labelSmall,
                                color = ExpenseRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (progressItem.isNearBudget) {
                        Surface(
                            color = AccentGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "NEAR LIMIT",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Budget",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SleekProgressBar(
                progress = progressItem.percentage,
                isOverBudget = progressItem.isOverBudget
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: ${formatCurrency(progressItem.spent)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (progressItem.isOverBudget) ExpenseRed else MaterialTheme.colorScheme.onSurface
                )
                if (progressItem.limit != null) {
                    val remaining = progressItem.limit - progressItem.spent
                    Text(
                        text = if (remaining >= 0) "Left: ${formatCurrency(remaining)}" else "+${formatCurrency(-remaining)} over",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (remaining >= 0) IncomeGreen else ExpenseRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
