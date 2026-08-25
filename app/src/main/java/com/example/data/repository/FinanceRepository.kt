package com.example.data.repository

import android.content.Context
import com.example.data.db.MeeCrebitDatabase
import com.example.data.model.BudgetEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.MerchantRuleEntity
import com.example.data.model.RuleMatchType
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.ZenProfileEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FinanceRepository(private val db: MeeCrebitDatabase) {

    val allTransactions: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactions()
    val allBudgets: Flow<List<BudgetEntity>> = db.budgetDao().getAllBudgets()
    val allRules: Flow<List<MerchantRuleEntity>> = db.merchantRuleDao().getAllRules()
    val zenProfile: Flow<ZenProfileEntity?> = db.zenProfileDao().getProfile()

    fun getBudgetsForMonth(monthYear: String): Flow<List<BudgetEntity>> {
        return db.budgetDao().getBudgetsForMonth(monthYear)
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return db.transactionDao().insertTransaction(transaction)
    }

    suspend fun insertTransactionDeduplicated(transaction: TransactionEntity): Boolean {
        if (!transaction.rawSmsBody.isNullOrBlank()) {
            val existing = db.transactionDao().findByRawSms(transaction.rawSmsBody)
            if (existing != null) return false
        }
        val margin = 60000L // 60 seconds margin for duplicate detection
        val duplicate = db.transactionDao().findDuplicate(
            merchant = transaction.merchant,
            amount = transaction.amount,
            minTime = transaction.timestamp - margin,
            maxTime = transaction.timestamp + margin
        )
        if (duplicate != null) return false

        db.transactionDao().insertTransaction(transaction)
        return true
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        db.transactionDao().updateTransaction(transaction)
    }

    suspend fun deleteTransaction(id: Long) {
        db.transactionDao().deleteById(id)
    }

    suspend fun insertOrUpdateBudget(budget: BudgetEntity) {
        db.budgetDao().insertOrUpdateBudget(budget)
    }

    suspend fun insertOrUpdateBudgets(budgets: List<BudgetEntity>) {
        db.budgetDao().insertAll(budgets)
    }

    suspend fun deleteBudget(id: Long) {
        db.budgetDao().deleteById(id)
    }

    suspend fun insertOrUpdateRule(rule: MerchantRuleEntity): Long {
        return db.merchantRuleDao().insertOrUpdateRule(rule)
    }

    suspend fun deleteRule(id: Long) {
        db.merchantRuleDao().deleteById(id)
    }

    suspend fun applyRulesToTransaction(rawBody: String, candidateMerchant: String): Pair<ExpenseCategory?, String?> {
        val rules = db.merchantRuleDao().getActiveRulesSync()
        for (rule in rules) {
            if (rule.matches(rawBody, candidateMerchant)) {
                return Pair(rule.targetCategory, rule.overrideMerchantName)
            }
        }
        return Pair(null, null)
    }

    suspend fun updateZenProfile(profile: ZenProfileEntity) {
        db.zenProfileDao().insertOrUpdateProfile(profile)
    }

    suspend fun addZenPoints(pointsToAdd: Int) {
        val current = db.zenProfileDao().getProfileSync()
        val currentPoints = current?.totalPoints ?: 120
        db.zenProfileDao().insertOrUpdateProfile(
            current?.copy(totalPoints = (currentPoints + pointsToAdd).coerceAtLeast(0))
                ?: ZenProfileEntity(totalPoints = (120 + pointsToAdd).coerceAtLeast(0))
        )
    }

    suspend fun clearAllData() {
        db.transactionDao().deleteAll()
        db.budgetDao().deleteAll()
        db.merchantRuleDao().deleteAll()
        db.zenProfileDao().insertOrUpdateProfile(ZenProfileEntity(totalPoints = 100))
    }

    fun getUserCustomMonthlyBudget(context: Context): Double? {
        val prefs = context.getSharedPreferences("meecrebit_budget_prefs", Context.MODE_PRIVATE)
        return if (prefs.contains("user_custom_monthly_budget")) {
            val v = prefs.getFloat("user_custom_monthly_budget", -1f)
            if (v > 0) v.toDouble() else null
        } else {
            null
        }
    }

    fun setUserCustomMonthlyBudget(context: Context, limit: Double?) {
        val prefs = context.getSharedPreferences("meecrebit_budget_prefs", Context.MODE_PRIVATE)
        if (limit != null && limit > 0) {
            prefs.edit().putFloat("user_custom_monthly_budget", limit.toFloat()).apply()
        } else {
            prefs.edit().remove("user_custom_monthly_budget").apply()
        }
    }

    suspend fun repairExistingTransactionDates(context: Context? = null): Int {
        val allTxns = db.transactionDao().getAllTransactionsSync()
        if (allTxns.isEmpty()) return 0

        val rules = db.merchantRuleDao().getActiveRulesSync()

        // Query device SMS inbox if permission is granted to get true message timestamps
        val smsDateMap = mutableMapOf<String, Long>()
        if (context != null && androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_SMS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val uri = android.provider.Telephony.Sms.CONTENT_URI
                val projection = arrayOf(android.provider.Telephony.Sms.BODY, android.provider.Telephony.Sms.DATE)
                val cursor = context.contentResolver.query(
                    uri,
                    projection,
                    null,
                    null,
                    "${android.provider.Telephony.Sms.DATE} DESC LIMIT 500"
                )
                cursor?.use { c ->
                    val bodyIdx = c.getColumnIndex(android.provider.Telephony.Sms.BODY)
                    val dateIdx = c.getColumnIndex(android.provider.Telephony.Sms.DATE)
                    while (c.moveToNext()) {
                        val body = if (bodyIdx >= 0) c.getString(bodyIdx) else null
                        val date = if (dateIdx >= 0) c.getLong(dateIdx) else 0L
                        if (!body.isNullOrBlank() && date > 0) {
                            val cleanKey = body.trim().replace("\\s+".toRegex(), " ")
                            if (!smsDateMap.containsKey(cleanKey)) {
                                smsDateMap[cleanKey] = date
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }

        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        var repairedCount = 0
        for (tx in allTxns) {
            val raw = tx.rawSmsBody
            if (!raw.isNullOrBlank()) {
                val cleanKey = raw.trim().replace("\\s+".toRegex(), " ")
                val inboxSmsDate = smsDateMap[cleanKey] ?: tx.timestamp
                val parsed = com.example.engine.SmsParserEngine.parse(raw, tx.sender ?: "", rules)

                if (!parsed.isValidTransaction) {
                    // Remove non-transactional artifacts (OTPs, balance inquiries, promotional limit SMS)
                    db.transactionDao().deleteById(tx.id)
                    repairedCount++
                    continue
                }

                var targetDate = com.example.engine.SmsParserEngine.extractTransactionDate(raw, inboxSmsDate)
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = targetDate }
                if (cal.get(java.util.Calendar.YEAR) in 2000..(currentYear - 1)) {
                    cal.set(java.util.Calendar.YEAR, currentYear)
                    targetDate = cal.timeInMillis
                }

                val updatedTx = tx.copy(
                    amount = parsed.amount,
                    type = parsed.type,
                    merchant = parsed.merchant,
                    category = if (tx.category != ExpenseCategory.OTHERS && tx.category != parsed.category) tx.category else parsed.category,
                    accountNumber = parsed.accountNumber,
                    bankName = parsed.bankName,
                    balanceAfter = parsed.balanceAfter,
                    timestamp = targetDate
                )

                if (updatedTx != tx) {
                    db.transactionDao().updateTransaction(updatedTx)
                    repairedCount++
                }
            } else {
                // Manual transaction - ensure timestamp year is plausible
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                if (cal.get(java.util.Calendar.YEAR) in 2000..(currentYear - 1)) {
                    cal.set(java.util.Calendar.YEAR, currentYear)
                    db.transactionDao().updateTransaction(tx.copy(timestamp = cal.timeInMillis))
                    repairedCount++
                }
            }
        }
        return repairedCount
    }

    suspend fun seedDefaultRulesIfEmpty() {
        val existing = db.merchantRuleDao().getActiveRulesSync()
        if (existing.isNotEmpty()) return

        val defaultRules = listOf(
            MerchantRuleEntity(pattern = "swiggy", matchType = RuleMatchType.CONTAINS, targetCategory = ExpenseCategory.FOOD_DINING, overrideMerchantName = "Swiggy"),
            MerchantRuleEntity(pattern = "zomato", matchType = RuleMatchType.CONTAINS, targetCategory = ExpenseCategory.FOOD_DINING, overrideMerchantName = "Zomato"),
            MerchantRuleEntity(pattern = "blinkit", matchType = RuleMatchType.CONTAINS, targetCategory = ExpenseCategory.GROCERIES, overrideMerchantName = "Blinkit"),
            MerchantRuleEntity(pattern = "zepto", matchType = RuleMatchType.CONTAINS, targetCategory = ExpenseCategory.GROCERIES, overrideMerchantName = "Zepto"),
            MerchantRuleEntity(pattern = "instamart", matchType = RuleMatchType.CONTAINS, targetCategory = ExpenseCategory.GROCERIES, overrideMerchantName = "Swiggy Instamart"),
            MerchantRuleEntity(pattern = "uber", matchType = RuleMatchType.CONTAINS, targetCategory = ExpenseCategory.TRANSPORT, overrideMerchantName = "Uber"),
            MerchantRuleEntity(pattern = "ola", matchType = RuleMatchType.CONTAINS, targetCategory = ExpenseCategory.TRANSPORT, overrideMerchantName = "Ola Cabs"),
            MerchantRuleEntity(pattern = "netflix", matchType = RuleMatchType.CONTAINS, targetCategory = ExpenseCategory.ENTERTAINMENT, overrideMerchantName = "Netflix"),
            MerchantRuleEntity(pattern = "spotify", matchType = RuleMatchType.CONTAINS, targetCategory = ExpenseCategory.ENTERTAINMENT, overrideMerchantName = "Spotify"),
            MerchantRuleEntity(pattern = "cult", matchType = RuleMatchType.CONTAINS, targetCategory = ExpenseCategory.HEALTH_FITNESS, overrideMerchantName = "Cult.fit"),
            MerchantRuleEntity(pattern = "amazon", matchType = RuleMatchType.CONTAINS, targetCategory = ExpenseCategory.SHOPPING, overrideMerchantName = "Amazon"),
            MerchantRuleEntity(pattern = "flipkart", matchType = RuleMatchType.CONTAINS, targetCategory = ExpenseCategory.SHOPPING, overrideMerchantName = "Flipkart"),
            MerchantRuleEntity(pattern = "zerodha", matchType = RuleMatchType.CONTAINS, targetCategory = ExpenseCategory.INVESTMENTS, overrideMerchantName = "Zerodha"),
            MerchantRuleEntity(pattern = "groww", matchType = RuleMatchType.CONTAINS, targetCategory = ExpenseCategory.INVESTMENTS, overrideMerchantName = "Groww"),
            MerchantRuleEntity(pattern = "atm", matchType = RuleMatchType.CONTAINS, targetCategory = ExpenseCategory.CASH, overrideMerchantName = "ATM Cash Withdrawal")
        )
        db.merchantRuleDao().insertAll(defaultRules)
    }

    suspend fun seedDefaultBudgetsIfEmpty(monthYear: String) {
        val defaultBudgets = listOf(
            BudgetEntity(category = ExpenseCategory.FOOD_DINING, monthlyLimit = 8000.0, monthYear = monthYear),
            BudgetEntity(category = ExpenseCategory.GROCERIES, monthlyLimit = 10000.0, monthYear = monthYear),
            BudgetEntity(category = ExpenseCategory.SHOPPING, monthlyLimit = 6000.0, monthYear = monthYear),
            BudgetEntity(category = ExpenseCategory.TRANSPORT, monthlyLimit = 4000.0, monthYear = monthYear),
            BudgetEntity(category = ExpenseCategory.BILLS_UTILITIES, monthlyLimit = 5000.0, monthYear = monthYear),
            BudgetEntity(category = ExpenseCategory.ENTERTAINMENT, monthlyLimit = 2500.0, monthYear = monthYear),
            BudgetEntity(category = ExpenseCategory.HEALTH_FITNESS, monthlyLimit = 3000.0, monthYear = monthYear)
        )
        db.budgetDao().insertAll(defaultBudgets)
    }

    suspend fun seedRealisticDemoData() {
        val now = System.currentTimeMillis()
        val dayMs = 24L * 60 * 60 * 1000
        val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

        seedDefaultBudgetsIfEmpty(monthYear)

        val sampleTransactions = listOf(
            TransactionEntity(
                amount = 450.00,
                type = TransactionType.DEBIT,
                merchant = "Zomato",
                category = ExpenseCategory.FOOD_DINING,
                accountNumber = "XX9123",
                bankName = "HDFC Bank",
                balanceAfter = 28450.00,
                rawSmsBody = "HDFC Bank: Rs 450.00 debited from A/C XX9123 to ZOMATO on 14-Aug at 1:45 PM. Avl Bal: Rs 28,450.00",
                sender = "HDFCBK",
                timestamp = now - (2 * 60 * 60 * 1000), // Today
                isManual = false
            ),
            TransactionEntity(
                amount = 5000.00,
                type = TransactionType.DEBIT,
                merchant = "HDFC Bank ATM",
                category = ExpenseCategory.CASH,
                accountNumber = "XX9123",
                bankName = "HDFC Bank",
                balanceAfter = 28900.00,
                rawSmsBody = "HDFC Bank: Rs 5,000.00 withdrawn from ATM using Debit Card 9123 on 13-Aug. Avl Bal: Rs 28,900.00",
                sender = "HDFCBK",
                timestamp = now - (26 * 60 * 60 * 1000), // Yesterday
                isManual = false
            ),
            TransactionEntity(
                amount = 320.00,
                type = TransactionType.DEBIT,
                merchant = "Swiggy",
                category = ExpenseCategory.FOOD_DINING,
                accountNumber = "XX9123",
                bankName = "HDFC Bank",
                balanceAfter = 33900.00,
                rawSmsBody = "HDFC Bank: Rs 320.00 debited from A/C XX9123 to SWIGGY BANGALORE on 13-Aug. Avl Bal: Rs 33,900.00",
                sender = "HDFCBK",
                timestamp = now - (29 * 60 * 60 * 1000),
                isManual = false
            ),
            TransactionEntity(
                amount = 420.00,
                type = TransactionType.DEBIT,
                merchant = "Uber India",
                category = ExpenseCategory.TRANSPORT,
                accountNumber = "XX9123",
                bankName = "HDFC Bank",
                balanceAfter = 34220.00,
                rawSmsBody = "HDFC Bank: Rs 420.00 debited from A/C XX9123 for UBER RIDE. Bal: Rs 34,220.00",
                sender = "HDFCBK",
                timestamp = now - (2 * dayMs),
                isManual = false
            ),
            TransactionEntity(
                amount = 3499.00,
                type = TransactionType.DEBIT,
                merchant = "Amazon India",
                category = ExpenseCategory.SHOPPING,
                accountNumber = "XX1104",
                bankName = "ICICI Bank",
                balanceAfter = 34640.00,
                rawSmsBody = "ICICI Bank: Alert! Rs 3,499.00 spent on Card XX1104 at AMAZON INDIA. Avail Limit: Rs 95,000.00",
                sender = "ICICIB",
                timestamp = now - (3 * dayMs),
                isManual = false
            ),
            TransactionEntity(
                amount = 85000.0,
                type = TransactionType.CREDIT,
                merchant = "Infosys Technologies Ltd",
                category = ExpenseCategory.SALARY_INCOME,
                accountNumber = "XX9123",
                bankName = "HDFC Bank",
                balanceAfter = 119640.00,
                rawSmsBody = "HDFC Bank: Salary of Rs 85,000.00 credited to A/C XX9123 on 01-Aug from INFOSYS TECHNOLOGIES. Avl Bal: Rs 1,19,640.00",
                sender = "HDFCBK",
                timestamp = now - (13 * dayMs),
                isManual = false
            ),
            TransactionEntity(
                amount = 649.00,
                type = TransactionType.DEBIT,
                merchant = "Netflix India",
                category = ExpenseCategory.ENTERTAINMENT,
                accountNumber = "XX1104",
                bankName = "ICICI Bank",
                balanceAfter = 36151.00,
                rawSmsBody = "ICICI Bank: INR 649.00 debited from Card ending 1104 for NETFLIX INDIA on 08-Aug.",
                sender = "ICICIB",
                timestamp = now - (6 * dayMs),
                isManual = false
            ),
            TransactionEntity(
                amount = 2500.00,
                type = TransactionType.DEBIT,
                merchant = "Indian Oil Petrol",
                category = ExpenseCategory.TRANSPORT,
                accountNumber = "XX4829",
                bankName = "State Bank of India",
                balanceAfter = 45750.00,
                rawSmsBody = "SBI: Rs 2,500.00 debited from A/C 4829 on 10-Aug at INDIAN OIL PUMP. Bal: Rs 45,750.00",
                sender = "SBIBNK",
                timestamp = now - (4 * dayMs),
                isManual = false
            ),
            TransactionEntity(
                amount = 1499.00,
                type = TransactionType.DEBIT,
                merchant = "Cult.fit Fitness",
                category = ExpenseCategory.HEALTH_FITNESS,
                accountNumber = "XX9123",
                bankName = "HDFC Bank",
                balanceAfter = 112151.00,
                rawSmsBody = "HDFC Bank: Rs 1,499.00 paid to CULT FIT on 11-Aug. Bal: Rs 1,12,151.00",
                sender = "HDFCBK",
                timestamp = now - (3 * dayMs),
                isManual = false
            ),
            TransactionEntity(
                amount = 340.00,
                type = TransactionType.DEBIT,
                merchant = "Zomato",
                category = ExpenseCategory.FOOD_DINING,
                accountNumber = "XX9123",
                bankName = "HDFC Bank",
                balanceAfter = 111811.00,
                rawSmsBody = "HDFC Bank: Rs 340.00 debited from A/C XX9123 to ZOMATO on 13-Aug. Bal: Rs 1,11,811.00",
                sender = "HDFCBK",
                timestamp = now - (1 * dayMs),
                isManual = false
            ),
            TransactionEntity(
                amount = 1250.00,
                type = TransactionType.CREDIT,
                merchant = "Dividend / Interest",
                category = ExpenseCategory.INVESTMENTS,
                accountNumber = "XX4829",
                bankName = "State Bank of India",
                balanceAfter = 47000.00,
                rawSmsBody = "SBI: Rs 1,250.00 credited to A/C 4829 towards MUTUAL FUND DIVIDEND on 14-Aug. Bal: Rs 47,000.00",
                sender = "SBIBNK",
                timestamp = now - (4 * 60 * 60 * 1000),
                isManual = false
            )
        )

        db.transactionDao().insertAll(sampleTransactions)
        db.zenProfileDao().insertOrUpdateProfile(
            ZenProfileEntity(
                totalPoints = 285,
                streakDays = 5,
                lastActiveDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                monthlySavingsGoal = 25000.0
            )
        )
    }

    suspend fun restoreLedger(restored: com.example.security.RestoredLedgerData) {
        if (restored.transactions.isNotEmpty()) {
            db.transactionDao().insertAll(restored.transactions)
        }
        if (restored.budgets.isNotEmpty()) {
            db.budgetDao().insertAll(restored.budgets)
        }
        if (restored.rules.isNotEmpty()) {
            db.merchantRuleDao().insertAll(restored.rules)
        }
        if (restored.zenProfile != null) {
            db.zenProfileDao().insertOrUpdateProfile(restored.zenProfile)
        }
    }
}

