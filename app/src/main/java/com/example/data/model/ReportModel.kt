package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateRangePreset(val displayName: String) {
    THIS_MONTH("This Month"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days"),
    LAST_3_MONTHS("Last 3 Months"),
    YEAR_TO_DATE("Year to Date"),
    ALL_TIME("All Time"),
    CUSTOM("Custom Range")
}

enum class TransactionTypeFilter(val displayName: String) {
    ALL("All Flow"),
    DEBIT_ONLY("Expenses Only"),
    CREDIT_ONLY("Income Only")
}

data class CustomReportFilter(
    val datePreset: DateRangePreset = DateRangePreset.THIS_MONTH,
    val customStartTimestamp: Long = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000),
    val customEndTimestamp: Long = System.currentTimeMillis(),
    val selectedCategories: Set<ExpenseCategory> = ExpenseCategory.values().toSet(),
    val selectedMerchants: Set<String> = emptySet(), // empty means all
    val typeFilter: TransactionTypeFilter = TransactionTypeFilter.DEBIT_ONLY,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val searchQuery: String = ""
)

data class CategoryReportItem(
    val category: ExpenseCategory,
    val totalAmount: Double,
    val percentage: Float, // 0.0 - 1.0
    val transactionCount: Int
)

data class MerchantReportItem(
    val merchant: String,
    val totalAmount: Double,
    val percentage: Float,
    val transactionCount: Int
)

data class DailySpendPoint(
    val label: String, // e.g. "08-Aug"
    val dayOfWeek: String, // e.g. "Fri"
    val timestamp: Long,
    val totalSpend: Double,
    val count: Int
)

data class CustomReportInsights(
    val filter: CustomReportFilter,
    val filteredTransactions: List<TransactionEntity>,
    val totalDebits: Double,
    val totalCredits: Double,
    val netFlow: Double,
    val transactionCount: Int,
    val averageSpendPerTx: Double,
    val medianSpend: Double,
    val highestTransaction: TransactionEntity?,
    val dailyAverageBurnRate: Double,
    val categoryBreakdown: List<CategoryReportItem>,
    val topMerchants: List<MerchantReportItem>,
    val dailyTrend: List<DailySpendPoint>,
    val weekendSpendPercent: Float,
    val weekdaySpendPercent: Float,
    val peakSpendingDayLabel: String,
    val daysCountInRange: Int
)

data class SavedReportPreset(
    val id: String,
    val name: String,
    val filter: CustomReportFilter,
    val createdAt: Long = System.currentTimeMillis()
)

object ReportEngine {

    fun generateInsights(
        allTransactions: List<TransactionEntity>,
        filter: CustomReportFilter
    ): CustomReportInsights {
        // Calculate date boundaries
        val (startTime, endTime) = getDateBounds(filter)

        // Filter transactions
        val filtered = allTransactions.filter { tx ->
            // Date bounds
            val inDate = tx.timestamp in startTime..endTime
            if (!inDate) return@filter false

            // Type filter
            val matchesType = when (filter.typeFilter) {
                TransactionTypeFilter.ALL -> true
                TransactionTypeFilter.DEBIT_ONLY -> tx.type == TransactionType.DEBIT
                TransactionTypeFilter.CREDIT_ONLY -> tx.type == TransactionType.CREDIT
            }
            if (!matchesType) return@filter false

            // Category filter
            if (filter.selectedCategories.isNotEmpty() && tx.category !in filter.selectedCategories) {
                return@filter false
            }

            // Merchant filter
            if (filter.selectedMerchants.isNotEmpty() && tx.merchant !in filter.selectedMerchants) {
                return@filter false
            }

            // Amount min/max
            if (filter.minAmount != null && tx.amount < filter.minAmount) return@filter false
            if (filter.maxAmount != null && tx.amount > filter.maxAmount) return@filter false

            // Search query
            if (filter.searchQuery.isNotBlank()) {
                val q = filter.searchQuery.lowercase(Locale.getDefault())
                val matchesQuery = tx.merchant.lowercase(Locale.getDefault()).contains(q) ||
                        tx.bankName.lowercase(Locale.getDefault()).contains(q) ||
                        tx.category.title.lowercase(Locale.getDefault()).contains(q) ||
                        tx.accountNumber.lowercase(Locale.getDefault()).contains(q)
                if (!matchesQuery) return@filter false
            }

            true
        }.sortedByDescending { it.timestamp }

        val debits = filtered.filter { it.type == TransactionType.DEBIT }
        val credits = filtered.filter { it.type == TransactionType.CREDIT }

        val totalDebits = debits.sumOf { it.amount }
        val totalCredits = credits.sumOf { it.amount }
        val netFlow = totalCredits - totalDebits
        val totalPrimaryAmount = if (filter.typeFilter == TransactionTypeFilter.CREDIT_ONLY) totalCredits else totalDebits

        val txCount = filtered.size
        val avgSpend = if (txCount > 0) (filtered.sumOf { it.amount } / txCount) else 0.0

        val sortedAmounts = filtered.map { it.amount }.sorted()
        val medianSpend = if (sortedAmounts.isNotEmpty()) {
            val mid = sortedAmounts.size / 2
            if (sortedAmounts.size % 2 == 0) (sortedAmounts[mid - 1] + sortedAmounts[mid]) / 2.0 else sortedAmounts[mid]
        } else 0.0

        val highestTx = filtered.maxByOrNull { it.amount }

        // Range days count
        val msDiff = (endTime - startTime).coerceAtLeast(1)
        val daysInRange = ((msDiff / (24L * 60 * 60 * 1000)) + 1).toInt().coerceAtLeast(1)
        val dailyBurnRate = totalDebits / daysInRange

        // Category breakdown
        val catMap = mutableMapOf<ExpenseCategory, MutableList<TransactionEntity>>()
        for (tx in filtered) {
            catMap.getOrPut(tx.category) { mutableListOf() }.add(tx)
        }

        val categoryBreakdown = catMap.map { (cat, list) ->
            val sum = list.sumOf { it.amount }
            val pct = if (totalPrimaryAmount > 0) (sum / totalPrimaryAmount).toFloat() else 0f
            CategoryReportItem(
                category = cat,
                totalAmount = sum,
                percentage = pct,
                transactionCount = list.size
            )
        }.sortedByDescending { it.totalAmount }

        // Top merchants
        val merchantMap = mutableMapOf<String, MutableList<TransactionEntity>>()
        for (tx in filtered) {
            merchantMap.getOrPut(tx.merchant) { mutableListOf() }.add(tx)
        }

        val topMerchants = merchantMap.map { (m, list) ->
            val sum = list.sumOf { it.amount }
            val pct = if (totalPrimaryAmount > 0) (sum / totalPrimaryAmount).toFloat() else 0f
            MerchantReportItem(
                merchant = m,
                totalAmount = sum,
                percentage = pct,
                transactionCount = list.size
            )
        }.sortedByDescending { it.totalAmount }.take(8)

        // Daily spend timeline & weekend/weekday analysis
        val dailySdf = SimpleDateFormat("dd-MMM", Locale.getDefault())
        val dayOfWeekSdf = SimpleDateFormat("EEE", Locale.getDefault())
        
        val dailyGroups = mutableMapOf<String, MutableList<TransactionEntity>>()
        var weekendTotal = 0.0
        var weekdayTotal = 0.0

        val cal = Calendar.getInstance()
        for (tx in filtered) {
            val key = dailySdf.format(Date(tx.timestamp))
            dailyGroups.getOrPut(key) { mutableListOf() }.add(tx)

            cal.timeInMillis = tx.timestamp
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            if (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) {
                weekendTotal += tx.amount
            } else {
                weekdayTotal += tx.amount
            }
        }

        val totalWeekendWeekday = (weekendTotal + weekdayTotal).coerceAtLeast(1e-6)
        val weekendPct = (weekendTotal / totalWeekendWeekday).toFloat()
        val weekdayPct = (weekdayTotal / totalWeekendWeekday).toFloat()

        val dailyTrend = dailyGroups.map { (dayStr, list) ->
            val first = list.first()
            val daySpend = list.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
            DailySpendPoint(
                label = dayStr,
                dayOfWeek = dayOfWeekSdf.format(Date(first.timestamp)),
                timestamp = first.timestamp,
                totalSpend = if (filter.typeFilter == TransactionTypeFilter.CREDIT_ONLY) list.sumOf { it.amount } else daySpend,
                count = list.size
            )
        }.sortedBy { it.timestamp }

        val peakDay = dailyTrend.maxByOrNull { it.totalSpend }
        val peakLabel = if (peakDay != null && peakDay.totalSpend > 0) {
            "${peakDay.label} (${peakDay.dayOfWeek}) - $${String.format(Locale.getDefault(), "%.2f", peakDay.totalSpend)}"
        } else "N/A"

        return CustomReportInsights(
            filter = filter,
            filteredTransactions = filtered,
            totalDebits = totalDebits,
            totalCredits = totalCredits,
            netFlow = netFlow,
            transactionCount = txCount,
            averageSpendPerTx = avgSpend,
            medianSpend = medianSpend,
            highestTransaction = highestTx,
            dailyAverageBurnRate = dailyBurnRate,
            categoryBreakdown = categoryBreakdown,
            topMerchants = topMerchants,
            dailyTrend = dailyTrend,
            weekendSpendPercent = weekendPct,
            weekdaySpendPercent = weekdayPct,
            peakSpendingDayLabel = peakLabel,
            daysCountInRange = daysInRange
        )
    }

    fun getDateBounds(filter: CustomReportFilter): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val now = System.currentTimeMillis()

        return when (filter.datePreset) {
            DateRangePreset.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                start to now
            }
            DateRangePreset.LAST_7_DAYS -> {
                (now - 7L * 24 * 60 * 60 * 1000) to now
            }
            DateRangePreset.LAST_30_DAYS -> {
                (now - 30L * 24 * 60 * 60 * 1000) to now
            }
            DateRangePreset.LAST_3_MONTHS -> {
                (now - 90L * 24 * 60 * 60 * 1000) to now
            }
            DateRangePreset.YEAR_TO_DATE -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis
                start to now
            }
            DateRangePreset.ALL_TIME -> {
                0L to Long.MAX_VALUE
            }
            DateRangePreset.CUSTOM -> {
                val start = filter.customStartTimestamp.coerceAtMost(filter.customEndTimestamp)
                val end = filter.customEndTimestamp.coerceAtLeast(filter.customStartTimestamp)
                start to end
            }
        }
    }
}
