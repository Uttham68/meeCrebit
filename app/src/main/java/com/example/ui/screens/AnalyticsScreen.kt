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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExpenseCategory
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryIconBox
import com.example.ui.components.PrivacyShieldBadge
import com.example.ui.components.formatCurrency
import com.example.ui.theme.AccentGold
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.viewmodel.FinanceViewModel
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: FinanceViewModel,
    onNavigateToCustomReports: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.currentMonthTransactions.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonthYear.collectAsStateWithLifecycle()

    val debits = transactions.filter { it.type == TransactionType.DEBIT }
    val credits = transactions.filter { it.type == TransactionType.CREDIT }

    val totalExpense = debits.sumOf { it.amount }
    val totalIncome = credits.sumOf { it.amount }
    val netSavings = totalIncome - totalExpense
    val savingsRate = if (totalIncome > 0) (netSavings / totalIncome) * 100 else 0.0

    val categoryTotals = mutableMapOf<ExpenseCategory, Double>()
    for (tx in debits) {
        categoryTotals[tx.category] = (categoryTotals[tx.category] ?: 0.0) + tx.amount
    }
    val sortedCategories = categoryTotals.toList().sortedByDescending { it.second }

    val merchantTotals = mutableMapOf<String, Double>()
    for (tx in debits) {
        merchantTotals[tx.merchant] = (merchantTotals[tx.merchant] ?: 0.0) + tx.amount
    }
    val topMerchants = merchantTotals.toList().sortedByDescending { it.second }.take(5)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Title
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Intelligent Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Calculated completely on-device for $selectedMonth",
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldLight
                )
            }
        }

        // Custom Report Builder Shortcut Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCustomReports() }
                    .testTag("open_custom_reports_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldPrimary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFF04201A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Custom Reports & Deep Slicing",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Filter by date range, merchant, & custom category splits",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldLight
                            )
                        }
                    }

                    Text(
                        text = "Launch →",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldLight
                    )
                }
            }
        }

        // Sleek Cash Flow Ratio Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Slate800),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cashflow_analytics_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "SAVINGS EFFICIENCY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Savings Rate", style = MaterialTheme.typography.labelSmall, color = Slate400)
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f%%", savingsRate),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (savingsRate >= 20.0) EmeraldLight else AccentGold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Net Surplus", style = MaterialTheme.typography.labelSmall, color = Slate400)
                            Text(
                                text = formatCurrency(netSavings),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (netSavings >= 0) IncomeGreen else ExpenseRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val outflowRatio = if (totalIncome > 0) (totalExpense / totalIncome).toFloat().coerceIn(0f, 1f) else 1f
                    LinearProgressIndicator(
                        progress = { outflowRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = ExpenseRed,
                        trackColor = IncomeGreen,
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Spent: ${formatCurrency(totalExpense)}", style = MaterialTheme.typography.labelSmall, color = ExpenseRed)
                        Text("Earned: ${formatCurrency(totalIncome)}", style = MaterialTheme.typography.labelSmall, color = IncomeGreen)
                    }
                }
            }
        }

        // Category Spending Breakdown
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Category Spending Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Real-time categorization from SMS metadata",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (sortedCategories.isEmpty()) {
                        Text(
                            text = "No debit transactions recorded for this month.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        sortedCategories.forEach { (cat, amount) ->
                            val pct = if (totalExpense > 0) (amount / totalExpense) else 0.0
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CategoryIconBox(category = cat, size = 30)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = cat.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = formatCurrency(amount),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = String.format(Locale.getDefault(), "%.1f%%", pct * 100),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate400
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { pct.toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(cat.hexColor),
                                    trackColor = Slate100,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top 5 Merchants Leaderboard
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = "Top Merchants",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Top Payees & Merchants",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (topMerchants.isEmpty()) {
                        Text(
                            text = "No merchants parsed yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    } else {
                        topMerchants.forEachIndexed { index, (merchant, spent) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = Slate100,
                                        shape = CircleShape,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF047857)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = merchant,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = formatCurrency(spent),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // Offline Intelligence Insight
        item {
            Surface(
                color = Color(0xFFD1FAE5),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Tip",
                        tint = Color(0xFF047857),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Offline Intelligence Insight",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val topCategory = sortedCategories.firstOrNull()?.first
                        val insightText = if (topCategory != null) {
                            "${topCategory.title} represents your biggest expense category this month. Setting a monthly budget limit will help you optimize savings."
                        } else {
                            "Keep SMS permissions enabled so meeCrebit can automatically build rich financial trend intelligence without sending any data to the cloud."
                        }
                        Text(
                            text = insightText,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF047857),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
