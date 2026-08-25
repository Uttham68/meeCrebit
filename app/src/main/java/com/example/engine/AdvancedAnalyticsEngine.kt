package com.example.engine

import com.example.data.model.AdvancedAnalyticsState
import com.example.data.model.CategoryDeltaComparison
import com.example.data.model.CategoryForecast
import com.example.data.model.DeltaTrendStatus
import com.example.data.model.ExpenseCategory
import com.example.data.model.MonthOverMonthCategoryComparison
import com.example.data.model.MonthlyCategorySpend
import com.example.data.model.MonthlyOutflowDataPoint
import com.example.data.model.RecurringSubscription
import com.example.data.model.SixMonthSpendingTrend
import com.example.data.model.SpendingForecast
import com.example.data.model.SubscriptionFrequency
import com.example.data.model.SubscriptionStatus
import com.example.data.model.SubscriptionsSummary
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

object AdvancedAnalyticsEngine {

    private val KNOWN_SERVICES = mapOf(
        "netflix" to Pair(ExpenseCategory.ENTERTAINMENT, SubscriptionFrequency.MONTHLY),
        "spotify" to Pair(ExpenseCategory.ENTERTAINMENT, SubscriptionFrequency.MONTHLY),
        "prime" to Pair(ExpenseCategory.ENTERTAINMENT, SubscriptionFrequency.MONTHLY),
        "amazon prime" to Pair(ExpenseCategory.ENTERTAINMENT, SubscriptionFrequency.MONTHLY),
        "disney" to Pair(ExpenseCategory.ENTERTAINMENT, SubscriptionFrequency.MONTHLY),
        "hotstar" to Pair(ExpenseCategory.ENTERTAINMENT, SubscriptionFrequency.MONTHLY),
        "youtube" to Pair(ExpenseCategory.ENTERTAINMENT, SubscriptionFrequency.MONTHLY),
        "apple" to Pair(ExpenseCategory.BILLS_UTILITIES, SubscriptionFrequency.MONTHLY),
        "icloud" to Pair(ExpenseCategory.BILLS_UTILITIES, SubscriptionFrequency.MONTHLY),
        "google one" to Pair(ExpenseCategory.BILLS_UTILITIES, SubscriptionFrequency.MONTHLY),
        "jio" to Pair(ExpenseCategory.BILLS_UTILITIES, SubscriptionFrequency.MONTHLY),
        "airtel" to Pair(ExpenseCategory.BILLS_UTILITIES, SubscriptionFrequency.MONTHLY),
        "vi " to Pair(ExpenseCategory.BILLS_UTILITIES, SubscriptionFrequency.MONTHLY),
        "vodafone" to Pair(ExpenseCategory.BILLS_UTILITIES, SubscriptionFrequency.MONTHLY),
        "act fibernet" to Pair(ExpenseCategory.BILLS_UTILITIES, SubscriptionFrequency.MONTHLY),
        "broadband" to Pair(ExpenseCategory.BILLS_UTILITIES, SubscriptionFrequency.MONTHLY),
        "tataplay" to Pair(ExpenseCategory.ENTERTAINMENT, SubscriptionFrequency.MONTHLY),
        "tata play" to Pair(ExpenseCategory.ENTERTAINMENT, SubscriptionFrequency.MONTHLY),
        "swiggy one" to Pair(ExpenseCategory.FOOD_DINING, SubscriptionFrequency.MONTHLY),
        "zomato gold" to Pair(ExpenseCategory.FOOD_DINING, SubscriptionFrequency.MONTHLY),
        "cult" to Pair(ExpenseCategory.HEALTH_FITNESS, SubscriptionFrequency.MONTHLY),
        "gym" to Pair(ExpenseCategory.HEALTH_FITNESS, SubscriptionFrequency.MONTHLY),
        "fitso" to Pair(ExpenseCategory.HEALTH_FITNESS, SubscriptionFrequency.MONTHLY),
        "zerodha" to Pair(ExpenseCategory.INVESTMENTS, SubscriptionFrequency.MONTHLY),
        "groww" to Pair(ExpenseCategory.INVESTMENTS, SubscriptionFrequency.MONTHLY),
        "sip" to Pair(ExpenseCategory.INVESTMENTS, SubscriptionFrequency.MONTHLY),
        "lic" to Pair(ExpenseCategory.BILLS_UTILITIES, SubscriptionFrequency.ANNUAL),
        "insurance" to Pair(ExpenseCategory.BILLS_UTILITIES, SubscriptionFrequency.ANNUAL),
        "bescom" to Pair(ExpenseCategory.BILLS_UTILITIES, SubscriptionFrequency.MONTHLY),
        "electricity" to Pair(ExpenseCategory.BILLS_UTILITIES, SubscriptionFrequency.MONTHLY),
        "chatgpt" to Pair(ExpenseCategory.OTHERS, SubscriptionFrequency.MONTHLY),
        "openai" to Pair(ExpenseCategory.OTHERS, SubscriptionFrequency.MONTHLY),
        "github" to Pair(ExpenseCategory.OTHERS, SubscriptionFrequency.MONTHLY),
        "linkedin" to Pair(ExpenseCategory.OTHERS, SubscriptionFrequency.MONTHLY)
    )

    fun computeFullAnalytics(
        allTransactions: List<TransactionEntity>,
        currentMonthYear: String
    ): AdvancedAnalyticsState {
        val debitTxs = allTransactions.filter { it.type == TransactionType.DEBIT }
        val distinctMonths = getDistinctMonths(allTransactions)

        val subscriptionsSummary = detectRecurringSubscriptions(debitTxs)
        val spendingForecast = calculateSpendingForecast(
            allTransactions = debitTxs,
            subscriptions = subscriptionsSummary.subscriptions,
            currentMonthYear = currentMonthYear
        )
        val momComparison = calculateMonthOverMonthComparison(
            allTransactions = debitTxs,
            currentMonthYear = currentMonthYear
        )
        val sixMonthTrend = calculateSixMonthSpendingTrend(
            debitTransactions = debitTxs,
            currentMonthYear = currentMonthYear
        )

        return AdvancedAnalyticsState(
            subscriptionsSummary = subscriptionsSummary,
            spendingForecast = spendingForecast,
            momComparison = momComparison,
            sixMonthTrend = sixMonthTrend,
            totalHistoricalTransactions = allTransactions.size,
            monthsWithDataCount = distinctMonths.size
        )
    }

    /**
     * Calculates 6-Month historical spending trends across all categories
     */
    fun calculateSixMonthSpendingTrend(
        debitTransactions: List<TransactionEntity>,
        currentMonthYear: String
    ): SixMonthSpendingTrend {
        val sdfMonthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val sdfShort = SimpleDateFormat("MMM", Locale.getDefault())
        val sdfFull = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

        val baseDate = try {
            sdfMonthYear.parse(currentMonthYear) ?: Date()
        } catch (_: Exception) {
            Date()
        }

        // Generate last 6 consecutive months (ending at currentMonthYear)
        val monthKeys = mutableListOf<Triple<String, String, String>>() // monthYear, shortLabel, fullLabel
        val cal = Calendar.getInstance().apply {
            time = baseDate
            add(Calendar.MONTH, -5)
        }

        for (i in 0 until 6) {
            val d = cal.time
            monthKeys.add(Triple(sdfMonthYear.format(d), sdfShort.format(d), sdfFull.format(d)))
            cal.add(Calendar.MONTH, 1)
        }

        // Group debits by monthYear
        val debitsByMonth = debitTransactions.groupBy { sdfMonthYear.format(Date(it.timestamp)) }

        val monthlyDataPoints = monthKeys.map { (mKey, shortLbl, fullLbl) ->
            val txsInMonth = debitsByMonth[mKey] ?: emptyList()
            val totalOutflow = txsInMonth.sumOf { it.amount }

            val catMap = mutableMapOf<ExpenseCategory, Double>()
            for (tx in txsInMonth) {
                catMap[tx.category] = (catMap[tx.category] ?: 0.0) + tx.amount
            }

            val breakdownList = catMap.map { (cat, amt) ->
                val pct = if (totalOutflow > 0) (amt / totalOutflow).toFloat() else 0f
                MonthlyCategorySpend(category = cat, amount = amt, percentageOfMonthlyTotal = pct)
            }.sortedByDescending { it.amount }

            val topCat = breakdownList.firstOrNull()?.category

            MonthlyOutflowDataPoint(
                monthYear = mKey,
                shortLabel = shortLbl,
                fullLabel = fullLbl,
                totalOutflow = totalOutflow,
                categoryOutflows = catMap,
                categoryBreakdownList = breakdownList,
                topCategory = topCat,
                transactionCount = txsInMonth.size
            )
        }

        val totalSixMonthOutflow = monthlyDataPoints.sumOf { it.totalOutflow }
        val monthlyAverageOutflow = if (monthlyDataPoints.isNotEmpty()) totalSixMonthOutflow / monthlyDataPoints.size else 0.0
        val maxMonthlyOutflow = monthlyDataPoints.maxOfOrNull { it.totalOutflow } ?: 0.0
        val minMonthlyOutflow = monthlyDataPoints.minOfOrNull { it.totalOutflow } ?: 0.0

        val highestSpendMonth = monthlyDataPoints.maxByOrNull { it.totalOutflow }
        val lowestSpendMonth = monthlyDataPoints.filter { it.totalOutflow > 0 }.minByOrNull { it.totalOutflow }
            ?: monthlyDataPoints.minByOrNull { it.totalOutflow }

        // Category totals aggregated across all 6 months
        val overallCategoryTotals = mutableMapOf<ExpenseCategory, Double>()
        for (dp in monthlyDataPoints) {
            for ((cat, amt) in dp.categoryOutflows) {
                overallCategoryTotals[cat] = (overallCategoryTotals[cat] ?: 0.0) + amt
            }
        }
        val sortedOverallCategories = overallCategoryTotals.entries.sortedByDescending { it.value }
        val topCategoryOverall = sortedOverallCategories.firstOrNull()?.key
        val topCategoryTotalSpend = sortedOverallCategories.firstOrNull()?.value ?: 0.0
        val activeCategories = sortedOverallCategories.map { it.key }

        // MoM growth between the last month and previous month
        val latestMonth = monthlyDataPoints.lastOrNull()
        val prevMonth = monthlyDataPoints.getOrNull(monthlyDataPoints.size - 2)
        val momGrowthPercent = if (prevMonth != null && prevMonth.totalOutflow > 0 && latestMonth != null) {
            ((latestMonth.totalOutflow - prevMonth.totalOutflow) / prevMonth.totalOutflow) * 100.0
        } else 0.0

        return SixMonthSpendingTrend(
            months = monthlyDataPoints,
            totalSixMonthOutflow = totalSixMonthOutflow,
            monthlyAverageOutflow = monthlyAverageOutflow,
            maxMonthlyOutflow = maxMonthlyOutflow,
            minMonthlyOutflow = minMonthlyOutflow,
            highestSpendMonth = highestSpendMonth,
            lowestSpendMonth = lowestSpendMonth,
            topSpendingCategoryOverall = topCategoryOverall,
            topCategoryTotalSpend = topCategoryTotalSpend,
            activeCategories = activeCategories,
            momGrowthPercent = momGrowthPercent
        )
    }

    /**
     * Identifies recurring subscriptions and recurring commitments completely locally
     */
    fun detectRecurringSubscriptions(debitTransactions: List<TransactionEntity>): SubscriptionsSummary {
        val normalizedMerchantGroups = debitTransactions
            .groupBy { normalizeMerchantName(it.merchant) }
            .filter { it.key.isNotBlank() }

        val detectedList = mutableListOf<RecurringSubscription>()

        normalizedMerchantGroups.forEach { (normName, txList) ->
            val sorted = txList.sortedBy { it.timestamp }
            val count = sorted.size
            val avgAmount = sorted.map { it.amount }.average()
            val lastTx = sorted.last()
            val firstTx = sorted.first()
            val isKnown = isKnownService(normName)
            val knownData = getKnownServiceData(normName)

            // Cadence analysis
            val frequency = detectFrequency(sorted, knownData?.second)

            // Check if eligible:
            // Condition 1: Known recurring service with at least 1 transaction
            // Condition 2: 2 or more transactions with consistent amounts / rhythm
            val amountsAreConsistent = if (count >= 2) {
                val maxAmt = sorted.maxOf { it.amount }
                val minAmt = sorted.minOf { it.amount }
                (maxAmt - minAmt) / maxAmt <= 0.18 // within 18% variance
            } else false

            val hasDistinctMonths = if (count >= 2) {
                val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                sorted.map { sdf.format(Date(it.timestamp)) }.distinct().size >= 2
            } else false

            var isEligible = false
            var confidence = 50

            if (isKnown) {
                isEligible = true
                confidence = if (count >= 2) 95 else 80
            } else if (count >= 2 && (amountsAreConsistent || hasDistinctMonths)) {
                isEligible = true
                confidence = if (count >= 3 && amountsAreConsistent) 90 else 75
            } else if (count >= 4) {
                isEligible = true
                confidence = 70
            }

            if (isEligible) {
                val nextBilling = calculateNextBillingDate(lastTx.timestamp, frequency)
                val status = calculateSubscriptionStatus(nextBilling)
                val monthlyEq = avgAmount * frequency.monthlyCostMultiplier
                val annualCost = avgAmount * frequency.annualMultiplier

                val resolvedCategory = knownData?.first ?: lastTx.category

                detectedList.add(
                    RecurringSubscription(
                        id = normName,
                        merchant = formatDisplayMerchant(normName, lastTx.merchant),
                        averageAmount = avgAmount,
                        lastAmount = lastTx.amount,
                        category = resolvedCategory,
                        frequency = frequency,
                        occurrences = count,
                        firstBilledDate = firstTx.timestamp,
                        lastBilledDate = lastTx.timestamp,
                        nextExpectedBillingDate = nextBilling,
                        monthlyEquivalentCost = monthlyEq,
                        annualizedCost = annualCost,
                        status = status,
                        isKnownService = isKnown,
                        confidencePercent = confidence
                    )
                )
            }
        }

        val sortedSubs = detectedList.sortedByDescending { it.monthlyEquivalentCost }
        val totalMonthly = sortedSubs.sumOf { it.monthlyEquivalentCost }
        val totalAnnual = sortedSubs.sumOf { it.annualizedCost }
        val dueSoon = sortedSubs.count { it.status == SubscriptionStatus.DUE_SOON }

        val categoryBreakdown = sortedSubs
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.monthlyEquivalentCost } }

        return SubscriptionsSummary(
            subscriptions = sortedSubs,
            totalMonthlyBurden = totalMonthly,
            totalAnnualCost = totalAnnual,
            activeCount = sortedSubs.size,
            dueSoonCount = dueSoon,
            categoryBreakdown = categoryBreakdown,
            topCostlySubscription = sortedSubs.firstOrNull()
        )
    }

    /**
     * Forecasts spending for next month using weighted moving average and trend velocity
     */
    fun calculateSpendingForecast(
        allTransactions: List<TransactionEntity>,
        subscriptions: List<RecurringSubscription>,
        currentMonthYear: String
    ): SpendingForecast {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val currentCal = Calendar.getInstance()

        // Calculate next month formatted
        val parsedCurrent = try { sdf.parse(currentMonthYear) } catch (e: Exception) { null }
        val nextMonthCal = Calendar.getInstance().apply {
            if (parsedCurrent != null) time = parsedCurrent
            add(Calendar.MONTH, 1)
        }
        val nextMonthYear = sdf.format(nextMonthCal.time)
        val nextMonthFormatted = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(nextMonthCal.time)

        val monthGroups = allTransactions
            .groupBy { sdf.format(Date(it.timestamp)) }
            .toSortedMap()

        val fixedBaseline = subscriptions.sumOf { it.monthlyEquivalentCost }

        val monthTotals = monthGroups.mapValues { entry -> entry.value.sumOf { it.amount } }
        val allMonthlyAverages = if (monthTotals.isNotEmpty()) monthTotals.values.average() else 0.0

        val monthsList = monthTotals.keys.toList()
        val totalMonthsAnalyzed = monthsList.size

        // Variable spending calculations (Total - Fixed)
        val variableSpendPerMonth = monthTotals.mapValues { max(0.0, it.value - fixedBaseline) }

        val projectedDiscretionary: Double
        val trendSlope: Double
        val confidenceLabel: String
        val confidencePercent: Int

        when {
            totalMonthsAnalyzed >= 3 -> {
                val last3Months = monthsList.takeLast(3).map { variableSpendPerMonth[it] ?: 0.0 }
                // Weighted Moving Average (weights: 0.15, 0.30, 0.55)
                val wma = last3Months[0] * 0.15 + last3Months[1] * 0.30 + last3Months[2] * 0.55
                val momDelta = if (last3Months[1] > 0) (last3Months[2] - last3Months[1]) / last3Months[1] else 0.0
                trendSlope = (momDelta * 100.0).coerceIn(-40.0, 50.0)

                // Damped trend addition
                projectedDiscretionary = max(0.0, wma * (1.0 + (trendSlope / 100.0) * 0.25))
                confidenceLabel = if (totalMonthsAnalyzed >= 5) "High Confidence (${totalMonthsAnalyzed}mo data)" else "Moderate (3-4mo data)"
                confidencePercent = if (totalMonthsAnalyzed >= 5) 88 else 75
            }
            totalMonthsAnalyzed == 2 -> {
                val m1 = variableSpendPerMonth[monthsList[0]] ?: 0.0
                val m2 = variableSpendPerMonth[monthsList[1]] ?: 0.0
                val avg = (m1 * 0.35 + m2 * 0.65)
                val momDelta = if (m1 > 0) (m2 - m1) / m1 else 0.0
                trendSlope = (momDelta * 100.0).coerceIn(-30.0, 40.0)
                projectedDiscretionary = max(0.0, avg * (1.0 + (trendSlope / 100.0) * 0.2))
                confidenceLabel = "Initial Trend (2mo history)"
                confidencePercent = 65
            }
            totalMonthsAnalyzed == 1 -> {
                val currentSpend = variableSpendPerMonth[monthsList[0]] ?: 0.0
                // Run-rate projection based on current day of month
                val dayOfMonth = currentCal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
                val maxDaysInMonth = currentCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val runRate = (currentSpend / dayOfMonth) * maxDaysInMonth
                projectedDiscretionary = max(currentSpend, runRate)
                trendSlope = 0.0
                confidenceLabel = "Preliminary (Run-rate extrapolation)"
                confidencePercent = 50
            }
            else -> {
                projectedDiscretionary = 0.0
                trendSlope = 0.0
                confidenceLabel = "Awaiting transaction logs"
                confidencePercent = 20
            }
        }

        val totalProjected = fixedBaseline + projectedDiscretionary
        val lowerBound = (fixedBaseline + projectedDiscretionary * 0.88).coerceAtLeast(fixedBaseline)
        val upperBound = fixedBaseline + projectedDiscretionary * 1.15

        // Category Level Forecasts
        val activeCategories = ExpenseCategory.values().filter { it != ExpenseCategory.SALARY_INCOME }
        val categoryForecasts = activeCategories.map { category ->
            val catTxs = allTransactions.filter { it.category == category }
            val catMonthTotals = catTxs.groupBy { sdf.format(Date(it.timestamp)) }
                .mapValues { it.value.sumOf { tx -> tx.amount } }

            val catAvg = if (catMonthTotals.isNotEmpty()) catMonthTotals.values.average() else 0.0
            val lastMonthCat = catMonthTotals[currentMonthYear] ?: 0.0

            // Project based on historical category share
            val catProjected = if (allMonthlyAverages > 0 && totalProjected > 0) {
                val catShare = catAvg / allMonthlyAverages
                (totalProjected * catShare).coerceAtLeast(0.0)
            } else {
                catAvg
            }

            val catMom = if (lastMonthCat > 0) ((catProjected - lastMonthCat) / lastMonthCat) * 100.0 else 0.0
            val isIncreasing = catProjected > lastMonthCat

            val rationale = when {
                catMom > 15.0 -> "Expected to surge +${catMom.roundToInt()}% based on recent velocity"
                catMom < -15.0 -> "Expected to decrease ${abs(catMom).roundToInt()}%"
                catProjected > 0 -> "Projected to remain steady"
                else -> "No past activity recorded"
            }

            CategoryForecast(
                category = category,
                projectedAmount = catProjected,
                historicalAverage = catAvg,
                lastMonthAmount = lastMonthCat,
                momTrendPercent = catMom,
                isIncreasing = isIncreasing,
                rationale = rationale
            )
        }.sortedByDescending { it.projectedAmount }

        // Key drivers and Smart savings insights
        val drivers = mutableListOf<String>()
        val savings = mutableListOf<String>()

        if (fixedBaseline > 0) {
            val fixedPct = if (totalProjected > 0) ((fixedBaseline / totalProjected) * 100).roundToInt() else 0
            drivers.add("Fixed recurring subscriptions & bills make up $fixedPct% (₹${fixedBaseline.toInt()}) of projected spend.")
        }

        val topCategory = categoryForecasts.firstOrNull { it.projectedAmount > 0 }
        if (topCategory != null && totalProjected > 0) {
            val topPct = ((topCategory.projectedAmount / totalProjected) * 100).roundToInt()
            drivers.add("${topCategory.category.title} is predicted as your largest spending channel ($topPct% / ₹${topCategory.projectedAmount.toInt()}).")
        }

        if (trendSlope > 5.0) {
            drivers.add("Overall expenditure pace is trending upward (+${trendSlope.roundToInt()}%) vs historical baseline.")
            savings.add("Trimming 10% from discretionary ${topCategory?.category?.title ?: "dining/shopping"} can save ~₹${(totalProjected * 0.08).toInt()} next month.")
        } else if (trendSlope < -5.0) {
            drivers.add("Spending velocity is cooling down by ${abs(trendSlope).roundToInt()}% — positive frugality momentum.")
            savings.add("Keep your current spending pace to bank a projected ~₹${(upperBound - totalProjected).toInt()} in savings.")
        } else {
            drivers.add("Spending patterns are stable across recent billing cycles.")
            savings.add("Review active subscriptions to eliminate unused recurring services.")
        }

        return SpendingForecast(
            currentMonthYear = currentMonthYear,
            nextMonthYear = nextMonthYear,
            nextMonthFormatted = nextMonthFormatted,
            projectedTotal = totalProjected,
            projectedLowerBound = lowerBound,
            projectedUpperBound = upperBound,
            fixedRecurringBaseline = fixedBaseline,
            discretionaryProjected = projectedDiscretionary,
            historicalMonthlyAverage = allMonthlyAverages,
            momTrendSlopePercent = trendSlope,
            confidenceLabel = confidenceLabel,
            confidencePercent = confidencePercent,
            categoryForecasts = categoryForecasts,
            keyDrivers = drivers,
            savingsInsights = savings,
            monthsAnalyzed = totalMonthsAnalyzed
        )
    }

    /**
     * Compares category spending from one month to the next with visual deltas and statuses
     */
    fun calculateMonthOverMonthComparison(
        allTransactions: List<TransactionEntity>,
        currentMonthYear: String
    ): MonthOverMonthCategoryComparison {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val currentParsed = try { sdf.parse(currentMonthYear) } catch (e: Exception) { Date() }

        val prevCal = Calendar.getInstance().apply {
            if (currentParsed != null) time = currentParsed
            add(Calendar.MONTH, -1)
        }
        val previousMonthYear = sdf.format(prevCal.time)

        val currentMonthFormatted = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(currentParsed ?: Date())
        val previousMonthFormatted = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(prevCal.time)

        val currentMonthTxs = allTransactions.filter { sdf.format(Date(it.timestamp)) == currentMonthYear }
        val prevMonthTxs = allTransactions.filter { sdf.format(Date(it.timestamp)) == previousMonthYear }

        val currentTotal = currentMonthTxs.sumOf { it.amount }
        val prevTotal = prevMonthTxs.sumOf { it.amount }

        val totalDeltaAmount = currentTotal - prevTotal
        val totalDeltaPercent = if (prevTotal > 0) ((totalDeltaAmount) / prevTotal) * 100.0 else if (currentTotal > 0) 100.0 else 0.0

        val activeCategories = ExpenseCategory.values().filter { it != ExpenseCategory.SALARY_INCOME }

        val categoryComparisons = activeCategories.map { category ->
            val currCatTxs = currentMonthTxs.filter { it.category == category }
            val prevCatTxs = prevMonthTxs.filter { it.category == category }

            val currSpend = currCatTxs.sumOf { it.amount }
            val prevSpend = prevCatTxs.sumOf { it.amount }

            val deltaAmount = currSpend - prevSpend
            val deltaPct = when {
                prevSpend == 0.0 && currSpend > 0.0 -> 100.0
                prevSpend > 0.0 && currSpend == 0.0 -> -100.0
                prevSpend > 0.0 -> ((currSpend - prevSpend) / prevSpend) * 100.0
                else -> 0.0
            }

            val status = when {
                prevSpend == 0.0 && currSpend > 0.0 -> DeltaTrendStatus.NEW_SPEND
                prevSpend > 0.0 && currSpend == 0.0 -> DeltaTrendStatus.NO_SPEND
                deltaPct >= 30.0 -> DeltaTrendStatus.SURGED
                deltaPct > 5.0 -> DeltaTrendStatus.INCREASED
                deltaPct <= -5.0 -> DeltaTrendStatus.REDUCED
                else -> DeltaTrendStatus.STABLE
            }

            val currShare = if (currentTotal > 0) (currSpend / currentTotal).toFloat() else 0f
            val prevShare = if (prevTotal > 0) (prevSpend / prevTotal).toFloat() else 0f

            CategoryDeltaComparison(
                category = category,
                currentSpent = currSpend,
                previousSpent = prevSpend,
                deltaAmount = deltaAmount,
                deltaPercent = deltaPct,
                status = status,
                currentPercentageOfTotal = currShare,
                previousPercentageOfTotal = prevShare,
                txCountCurrent = currCatTxs.size,
                txCountPrevious = prevCatTxs.size
            )
        }.sortedWith(
            compareByDescending<CategoryDeltaComparison> { max(it.currentSpent, it.previousSpent) }
        )

        val topSurges = categoryComparisons
            .filter { it.deltaAmount > 0 }
            .sortedByDescending { it.deltaAmount }

        val topSavings = categoryComparisons
            .filter { it.deltaAmount < 0 }
            .sortedBy { it.deltaAmount }

        val primaryDriverText = when {
            totalDeltaAmount > 0 && topSurges.isNotEmpty() -> {
                val top = topSurges.first()
                "Total spend increased by ₹${totalDeltaAmount.roundToInt()} (${String.format(Locale.getDefault(), "%.1f", totalDeltaPercent)}%), primarily driven by ${top.category.title} (+₹${top.deltaAmount.roundToInt()})."
            }
            totalDeltaAmount < 0 && topSavings.isNotEmpty() -> {
                val top = topSavings.first()
                "Total spend decreased by ₹${abs(totalDeltaAmount).roundToInt()} (${String.format(Locale.getDefault(), "%.1f", abs(totalDeltaPercent))}%), led by savings in ${top.category.title} (-₹${abs(top.deltaAmount).roundToInt()})."
            }
            else -> "Spending across $currentMonthFormatted and $previousMonthFormatted is virtually balanced (±0%)."
        }

        return MonthOverMonthCategoryComparison(
            currentMonthYear = currentMonthYear,
            currentMonthFormatted = currentMonthFormatted,
            previousMonthYear = previousMonthYear,
            previousMonthFormatted = previousMonthFormatted,
            currentTotalSpent = currentTotal,
            previousTotalSpent = prevTotal,
            totalDeltaAmount = totalDeltaAmount,
            totalDeltaPercent = totalDeltaPercent,
            isTotalSpendingUp = totalDeltaAmount > 0,
            categories = categoryComparisons,
            topSurges = topSurges,
            topSavings = topSavings,
            primaryDriverText = primaryDriverText
        )
    }

    // Helper functions
    private fun normalizeMerchantName(raw: String): String {
        return raw.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .replace(Regex("\\b(pvt|ltd|limited|corp|upi|pos|ecom|vpa|txn|pay|payment)\\b"), "")
            .trim()
    }

    private fun isKnownService(normName: String): Boolean {
        return KNOWN_SERVICES.keys.any { normName.contains(it) }
    }

    private fun getKnownServiceData(normName: String): Pair<ExpenseCategory, SubscriptionFrequency>? {
        val entry = KNOWN_SERVICES.entries.firstOrNull { normName.contains(it.key) }
        return entry?.value
    }

    private fun detectFrequency(sortedTxs: List<TransactionEntity>, fallback: SubscriptionFrequency?): SubscriptionFrequency {
        if (sortedTxs.size < 2) return fallback ?: SubscriptionFrequency.MONTHLY

        val intervals = mutableListOf<Long>()
        for (i in 1 until sortedTxs.size) {
            val days = (sortedTxs[i].timestamp - sortedTxs[i - 1].timestamp) / (1000 * 60 * 60 * 24)
            if (days > 0) intervals.add(days)
        }

        if (intervals.isEmpty()) return fallback ?: SubscriptionFrequency.MONTHLY

        val medianDays = intervals.sorted()[intervals.size / 2]

        return when {
            medianDays in 5..10 -> SubscriptionFrequency.WEEKLY
            medianDays in 20..45 -> SubscriptionFrequency.MONTHLY
            medianDays in 75..110 -> SubscriptionFrequency.QUARTERLY
            medianDays > 300 -> SubscriptionFrequency.ANNUAL
            else -> fallback ?: SubscriptionFrequency.MONTHLY
        }
    }

    private fun calculateNextBillingDate(lastBilledTimestamp: Long, frequency: SubscriptionFrequency): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = lastBilledTimestamp
        }
        when (frequency) {
            SubscriptionFrequency.WEEKLY -> cal.add(Calendar.DAY_OF_YEAR, 7)
            SubscriptionFrequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
            SubscriptionFrequency.QUARTERLY -> cal.add(Calendar.MONTH, 3)
            SubscriptionFrequency.ANNUAL -> cal.add(Calendar.YEAR, 1)
        }
        return cal.timeInMillis
    }

    private fun calculateSubscriptionStatus(nextBillingTimestamp: Long): SubscriptionStatus {
        val now = System.currentTimeMillis()
        val diffDays = (nextBillingTimestamp - now) / (1000 * 60 * 60 * 24)

        return when {
            diffDays in 0..7 -> SubscriptionStatus.DUE_SOON
            diffDays < -3 -> SubscriptionStatus.OVERDUE
            else -> SubscriptionStatus.ACTIVE
        }
    }

    private fun formatDisplayMerchant(normName: String, originalMerchant: String): String {
        val known = KNOWN_SERVICES.keys.firstOrNull { normName.contains(it) }
        if (known != null) {
            return known.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
        }
        return originalMerchant.ifBlank { normName.capitalize(Locale.getDefault()) }
    }

    private fun getDistinctMonths(transactions: List<TransactionEntity>): List<String> {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return transactions.map { sdf.format(Date(it.timestamp)) }.distinct().sorted()
    }
}
