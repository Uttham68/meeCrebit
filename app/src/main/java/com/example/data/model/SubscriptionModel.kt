package com.example.data.model

import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

enum class BillingCycle(val displayName: String, val approxDays: Int) {
    WEEKLY("Weekly", 7),
    MONTHLY("Monthly", 30),
    QUARTERLY("Quarterly", 90),
    ANNUAL("Annual", 365)
}

data class SubscriptionItem(
    val id: String,
    val merchant: String,
    val amount: Double,
    val category: ExpenseCategory,
    val cycle: BillingCycle,
    val lastChargedDate: Long,
    val nextDueDate: Long,
    val daysUntilDue: Int,
    val occurrencesCount: Int,
    val confidence: Float,
    val isAutoDetected: Boolean = true,
    val isActive: Boolean = true
) {
    val monthlyBurn: Double
        get() = when (cycle) {
            BillingCycle.WEEKLY -> amount * 4.33
            BillingCycle.MONTHLY -> amount
            BillingCycle.QUARTERLY -> amount / 3.0
            BillingCycle.ANNUAL -> amount / 12.0
        }

    val annualBurn: Double
        get() = monthlyBurn * 12.0
}

object SubscriptionDetectorEngine {

    /**
     * Smart recurring cycle detector:
     * Analyzes historical debit transactions and identifies repeating amounts / merchants
     * across 7-day, 28-32 day (monthly), or 90-day intervals.
     */
    fun detectSubscriptions(transactions: List<TransactionEntity>): List<SubscriptionItem> {
        val debits = transactions.filter { it.type == TransactionType.DEBIT && it.amount > 0 }
        val groupedByMerchant = debits.groupBy { normalizeMerchantKey(it.merchant) }
        val detectedList = mutableListOf<SubscriptionItem>()

        val now = System.currentTimeMillis()

        for ((merchantKey, txList) in groupedByMerchant) {
            if (txList.isEmpty()) continue

            // Sort ascending by timestamp
            val sorted = txList.sortedBy { it.timestamp }
            val latest = sorted.last()

            // Check known subscriptions (Netflix, Spotify, AWS, WiFi, Rent, SIP, etc.)
            val isKnownRecurringService = isKnownRecurringMerchant(merchantKey)

            if (sorted.size >= 2) {
                // Check recurring intervals between successive transactions
                var isMonthly = false
                var isWeekly = false
                var totalIntervalDays = 0L
                var intervalCount = 0

                for (i in 0 until sorted.size - 1) {
                    val diffMs = sorted[i + 1].timestamp - sorted[i].timestamp
                    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs)
                    if (diffDays in 5..9) {
                        isWeekly = true
                    }
                    if (diffDays in 25..35) {
                        isMonthly = true
                    }
                    totalIntervalDays += diffDays
                    intervalCount++
                }

                val avgInterval = if (intervalCount > 0) totalIntervalDays / intervalCount else 30
                val cycle = when {
                    isWeekly || avgInterval in 6..8 -> BillingCycle.WEEKLY
                    avgInterval in 25..35 || isMonthly -> BillingCycle.MONTHLY
                    avgInterval in 80..100 -> BillingCycle.QUARTERLY
                    avgInterval in 340..380 -> BillingCycle.ANNUAL
                    else -> BillingCycle.MONTHLY
                }

                val nextDue = calculateNextDueDate(latest.timestamp, cycle)
                val daysUntil = TimeUnit.MILLISECONDS.toDays(nextDue - now).toInt().coerceAtLeast(0)

                detectedList.add(
                    SubscriptionItem(
                        id = "sub_${merchantKey.hashCode()}",
                        merchant = latest.merchant,
                        amount = latest.amount,
                        category = latest.category,
                        cycle = cycle,
                        lastChargedDate = latest.timestamp,
                        nextDueDate = nextDue,
                        daysUntilDue = daysUntil,
                        occurrencesCount = sorted.size,
                        confidence = if (isKnownRecurringService) 0.95f else 0.85f,
                        isAutoDetected = true,
                        isActive = true
                    )
                )
            } else if (isKnownRecurringService) {
                // Even if 1 transaction exists for Netflix/Spotify/Jio/AWS/SIP, recognize as subscription
                val cycle = BillingCycle.MONTHLY
                val nextDue = calculateNextDueDate(latest.timestamp, cycle)
                val daysUntil = TimeUnit.MILLISECONDS.toDays(nextDue - now).toInt().coerceAtLeast(0)

                detectedList.add(
                    SubscriptionItem(
                        id = "sub_${merchantKey.hashCode()}",
                        merchant = latest.merchant,
                        amount = latest.amount,
                        category = latest.category,
                        cycle = cycle,
                        lastChargedDate = latest.timestamp,
                        nextDueDate = nextDue,
                        daysUntilDue = daysUntil,
                        occurrencesCount = 1,
                        confidence = 0.90f,
                        isAutoDetected = true,
                        isActive = true
                    )
                )
            }
        }

        // Return sorted by nearest due date
        return detectedList.sortedBy { it.daysUntilDue }
    }

    private fun normalizeMerchantKey(name: String): String {
        return name.lowercase(Locale.getDefault())
            .replace(Regex("""[^a-z0-9]"""), "")
            .trim()
    }

    private fun isKnownRecurringMerchant(key: String): Boolean {
        val recurringSignatures = listOf(
            "netflix", "spotify", "youtube", "amazonprime", "prime",
            "hotstar", "disney", "apple", "icloud", "googleone",
            "aws", "digitalocean", "github", "chatgpt", "openai",
            "airtel", "jio", "vi", "vodafone", "actfibernet", "hathway",
            "tataplay", "cultfit", "cult", "gym", "sip", "zerodha",
            "groww", "lic", "insurance", "rent", "broadband", "electricity"
        )
        return recurringSignatures.any { key.contains(it) }
    }

    private fun calculateNextDueDate(lastDate: Long, cycle: BillingCycle): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = lastDate }
        val now = System.currentTimeMillis()

        while (cal.timeInMillis <= now) {
            when (cycle) {
                BillingCycle.WEEKLY -> cal.add(Calendar.DAY_OF_YEAR, 7)
                BillingCycle.MONTHLY -> cal.add(Calendar.MONTH, 1)
                BillingCycle.QUARTERLY -> cal.add(Calendar.MONTH, 3)
                BillingCycle.ANNUAL -> cal.add(Calendar.YEAR, 1)
            }
        }
        return cal.timeInMillis
    }
}
