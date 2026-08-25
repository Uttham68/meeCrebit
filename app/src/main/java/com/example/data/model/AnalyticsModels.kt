package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.Slate400

enum class SubscriptionFrequency(val displayName: String, val annualMultiplier: Double) {
    WEEKLY("Weekly", 52.0),
    MONTHLY("Monthly", 12.0),
    QUARTERLY("Quarterly", 4.0),
    ANNUAL("Annual", 1.0);

    val monthlyCostMultiplier: Double
        get() = when (this) {
            WEEKLY -> 4.333
            MONTHLY -> 1.0
            QUARTERLY -> 1.0 / 3.0
            ANNUAL -> 1.0 / 12.0
        }
}

enum class SubscriptionStatus(val displayName: String) {
    ACTIVE("Active"),
    DUE_SOON("Due in < 7 days"),
    OVERDUE("Overdue / Expected"),
    IRREGULAR("Irregular Cadence")
}

data class RecurringSubscription(
    val id: String,
    val merchant: String,
    val averageAmount: Double,
    val lastAmount: Double,
    val category: ExpenseCategory,
    val frequency: SubscriptionFrequency,
    val occurrences: Int,
    val firstBilledDate: Long,
    val lastBilledDate: Long,
    val nextExpectedBillingDate: Long,
    val monthlyEquivalentCost: Double,
    val annualizedCost: Double,
    val status: SubscriptionStatus,
    val isKnownService: Boolean,
    val confidencePercent: Int,
    val isUserCustomized: Boolean = false
)

data class SubscriptionsSummary(
    val subscriptions: List<RecurringSubscription>,
    val totalMonthlyBurden: Double,
    val totalAnnualCost: Double,
    val activeCount: Int,
    val dueSoonCount: Int,
    val categoryBreakdown: Map<ExpenseCategory, Double>,
    val topCostlySubscription: RecurringSubscription?
)

data class CategoryForecast(
    val category: ExpenseCategory,
    val projectedAmount: Double,
    val historicalAverage: Double,
    val lastMonthAmount: Double,
    val momTrendPercent: Double,
    val isIncreasing: Boolean,
    val rationale: String
)

data class SpendingForecast(
    val currentMonthYear: String,
    val nextMonthYear: String,
    val nextMonthFormatted: String,
    val projectedTotal: Double,
    val projectedLowerBound: Double,
    val projectedUpperBound: Double,
    val fixedRecurringBaseline: Double,
    val discretionaryProjected: Double,
    val historicalMonthlyAverage: Double,
    val momTrendSlopePercent: Double,
    val confidenceLabel: String,
    val confidencePercent: Int,
    val categoryForecasts: List<CategoryForecast>,
    val keyDrivers: List<String>,
    val savingsInsights: List<String>,
    val monthsAnalyzed: Int
)

enum class DeltaTrendStatus(val title: String, val badgeColor: Color) {
    SURGED("Surged (+30%+)", ExpenseRed),
    INCREASED("Increased", Color(0xFFF97316)),
    STABLE("Stable (±5%)", Slate400),
    REDUCED("Reduced", EmeraldLight),
    NEW_SPEND("New Category", Color(0xFF8B5CF6)),
    NO_SPEND("Zero Spend", EmeraldLight)
}

data class CategoryDeltaComparison(
    val category: ExpenseCategory,
    val currentSpent: Double,
    val previousSpent: Double,
    val deltaAmount: Double,
    val deltaPercent: Double,
    val status: DeltaTrendStatus,
    val currentPercentageOfTotal: Float,
    val previousPercentageOfTotal: Float,
    val txCountCurrent: Int,
    val txCountPrevious: Int
)

data class MonthOverMonthCategoryComparison(
    val currentMonthYear: String,
    val currentMonthFormatted: String,
    val previousMonthYear: String,
    val previousMonthFormatted: String,
    val currentTotalSpent: Double,
    val previousTotalSpent: Double,
    val totalDeltaAmount: Double,
    val totalDeltaPercent: Double,
    val isTotalSpendingUp: Boolean,
    val categories: List<CategoryDeltaComparison>,
    val topSurges: List<CategoryDeltaComparison>,
    val topSavings: List<CategoryDeltaComparison>,
    val primaryDriverText: String
)

data class MonthlyCategorySpend(
    val category: ExpenseCategory,
    val amount: Double,
    val percentageOfMonthlyTotal: Float
)

data class MonthlyOutflowDataPoint(
    val monthYear: String, // e.g. "2026-08"
    val shortLabel: String, // e.g. "Aug"
    val fullLabel: String, // e.g. "August 2026"
    val totalOutflow: Double,
    val categoryOutflows: Map<ExpenseCategory, Double>,
    val categoryBreakdownList: List<MonthlyCategorySpend>,
    val topCategory: ExpenseCategory?,
    val transactionCount: Int
)

data class SixMonthSpendingTrend(
    val months: List<MonthlyOutflowDataPoint>,
    val totalSixMonthOutflow: Double,
    val monthlyAverageOutflow: Double,
    val maxMonthlyOutflow: Double,
    val minMonthlyOutflow: Double,
    val highestSpendMonth: MonthlyOutflowDataPoint?,
    val lowestSpendMonth: MonthlyOutflowDataPoint?,
    val topSpendingCategoryOverall: ExpenseCategory?,
    val topCategoryTotalSpend: Double,
    val activeCategories: List<ExpenseCategory>,
    val momGrowthPercent: Double
)

data class AdvancedAnalyticsState(
    val subscriptionsSummary: SubscriptionsSummary,
    val spendingForecast: SpendingForecast,
    val momComparison: MonthOverMonthCategoryComparison,
    val sixMonthTrend: SixMonthSpendingTrend,
    val totalHistoricalTransactions: Int,
    val monthsWithDataCount: Int,
    val lastCalculatedTimestamp: Long = System.currentTimeMillis()
)
