package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.MeeCrebitDatabase
import com.example.data.model.BudgetEntity
import com.example.data.model.CustomReportFilter
import com.example.data.model.CustomReportInsights
import com.example.data.model.DateRangePreset
import com.example.data.model.ExpenseCategory
import com.example.data.model.MerchantRuleEntity
import com.example.data.model.MultiBankBalanceCalculator
import com.example.data.model.MultiBankSummary
import com.example.data.model.ReportEngine
import com.example.data.model.RuleMatchType
import com.example.data.model.SavedReportPreset
import com.example.data.model.SubscriptionDetectorEngine
import com.example.data.model.SubscriptionItem
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.TransactionTypeFilter
import com.example.data.model.ZenProfileEntity
import com.example.data.repository.FinanceRepository
import com.example.engine.ParsedSmsResult
import com.example.engine.SmsInboxScanner
import com.example.engine.SmsParserEngine
import com.example.engine.ml.LocalCategorizationModel
import com.example.engine.ml.ModelMetrics
import com.example.notification.NotificationHelper
import com.example.security.BiometricAuthManager
import com.example.security.EncryptedBackupManager
import com.example.widget.MeeCrebitBudgetWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class CategorySpendProgress(
    val category: ExpenseCategory,
    val spent: Double,
    val limit: Double?,
    val percentage: Float, // 0.0 to 1.0+
    val isOverBudget: Boolean,
    val isNearBudget: Boolean
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    private val mlModel = LocalCategorizationModel.getInstance()
    private val biometricAuthManager = BiometricAuthManager.getInstance(application)

    // -------------------------------------------------------------
    // Biometric Security & Lock State
    // -------------------------------------------------------------
    private val _isBiometricEnabled = MutableStateFlow(biometricAuthManager.isBiometricEnabled)
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _lockTimeoutSeconds = MutableStateFlow(biometricAuthManager.autoLockTimeoutMillis / 1000)
    val lockTimeoutSeconds: StateFlow<Long> = _lockTimeoutSeconds.asStateFlow()

    private val _isAppLocked = MutableStateFlow(biometricAuthManager.isBiometricEnabled)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _biometricErrorMessage = MutableStateFlow<String?>(null)
    val biometricErrorMessage: StateFlow<String?> = _biometricErrorMessage.asStateFlow()

    init {
        val db = MeeCrebitDatabase.getInstance(application)
        repository = FinanceRepository(db)
        viewModelScope.launch {
            val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
            repository.seedDefaultBudgetsIfEmpty(currentMonth)
            repository.seedDefaultRulesIfEmpty()
        }
    }

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val merchantRules: StateFlow<List<MerchantRuleEntity>> = repository.allRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val zenProfile: StateFlow<ZenProfileEntity?> = repository.zenProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // -------------------------------------------------------------
    // Smart Subscription & Recurring Bill Detector State
    // -------------------------------------------------------------
    val subscriptions: StateFlow<List<SubscriptionItem>> = transactions.combine(transactions) { txList, _ ->
        SubscriptionDetectorEngine.detectSubscriptions(txList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMonthlyRecurringBurn: StateFlow<Double> = subscriptions.combine(subscriptions) { subs, _ ->
        subs.filter { it.isActive }.sumOf { it.monthlyBurn }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalAnnualRecurringBurn: StateFlow<Double> = subscriptions.combine(subscriptions) { subs, _ ->
        subs.filter { it.isActive }.sumOf { it.annualBurn }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // -------------------------------------------------------------
    // Multi-Bank Live Balance Tracking State
    // -------------------------------------------------------------
    val multiBankSummary: StateFlow<MultiBankSummary> = transactions.combine(transactions) { txList, _ ->
        MultiBankBalanceCalculator.calculateBalances(txList)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MultiBankSummary(emptyList(), 0.0, 0.0, 0.0, System.currentTimeMillis())
    )

    // Current selected statement month (e.g., "2026-08")
    private val _selectedMonthYear = MutableStateFlow(
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    )
    val selectedMonthYear: StateFlow<String> = _selectedMonthYear.asStateFlow()

    // Test Sandbox state
    private val _testSmsResult = MutableStateFlow<ParsedSmsResult?>(null)
    val testSmsResult: StateFlow<ParsedSmsResult?> = _testSmsResult.asStateFlow()

    private val _isScanningInbox = MutableStateFlow(false)
    val isScanningInbox: StateFlow<Boolean> = _isScanningInbox.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // -------------------------------------------------------------
    // Machine Learning State & Local Model Diagnostics
    // -------------------------------------------------------------
    private val _modelMetrics = MutableStateFlow(mlModel.getModelMetrics())
    val modelMetrics: StateFlow<ModelMetrics> = _modelMetrics.asStateFlow()

    private val _isTrainingModel = MutableStateFlow(false)
    val isTrainingModel: StateFlow<Boolean> = _isTrainingModel.asStateFlow()

    // -------------------------------------------------------------
    // Custom Spending Reports State
    // -------------------------------------------------------------
    private val _reportFilter = MutableStateFlow(CustomReportFilter())
    val reportFilter: StateFlow<CustomReportFilter> = _reportFilter.asStateFlow()

    val reportInsights: StateFlow<CustomReportInsights> = combine(
        transactions,
        _reportFilter
    ) { txList, filter ->
        ReportEngine.generateInsights(txList, filter)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ReportEngine.generateInsights(emptyList(), CustomReportFilter())
    )

    private val _savedPresets = MutableStateFlow<List<SavedReportPreset>>(
        listOf(
            SavedReportPreset(
                id = "preset_dining",
                name = "Food & Dining Audit",
                filter = CustomReportFilter(
                    datePreset = DateRangePreset.LAST_30_DAYS,
                    selectedCategories = setOf(ExpenseCategory.FOOD_DINING, ExpenseCategory.GROCERIES),
                    typeFilter = TransactionTypeFilter.DEBIT_ONLY
                )
            ),
            SavedReportPreset(
                id = "preset_major",
                name = "High Spend (Over $50)",
                filter = CustomReportFilter(
                    datePreset = DateRangePreset.THIS_MONTH,
                    minAmount = 50.0,
                    typeFilter = TransactionTypeFilter.DEBIT_ONLY
                )
            ),
            SavedReportPreset(
                id = "preset_transport",
                name = "Commute & Gas Analysis",
                filter = CustomReportFilter(
                    datePreset = DateRangePreset.LAST_3_MONTHS,
                    selectedCategories = setOf(ExpenseCategory.TRANSPORT),
                    typeFilter = TransactionTypeFilter.DEBIT_ONLY
                )
            )
        )
    )
    val savedPresets: StateFlow<List<SavedReportPreset>> = _savedPresets.asStateFlow()

    fun selectMonthYear(monthYear: String) {
        _selectedMonthYear.value = monthYear
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // Calculations for dynamic dashboard & statement
    val currentMonthTransactions: StateFlow<List<TransactionEntity>> = combine(
        transactions,
        _selectedMonthYear
    ) { txList, monthYear ->
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        txList.filter { sdf.format(Date(it.timestamp)) == monthYear }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoryProgressList: StateFlow<List<CategorySpendProgress>> = combine(
        currentMonthTransactions,
        budgets,
        _selectedMonthYear
    ) { txList, budgetList, monthYear ->
        val monthlyBudgets = budgetList.filter { it.monthYear == monthYear }.associateBy { it.category }
        val categorySpends = mutableMapOf<ExpenseCategory, Double>()

        for (tx in txList) {
            if (tx.type == TransactionType.DEBIT) {
                categorySpends[tx.category] = (categorySpends[tx.category] ?: 0.0) + tx.amount
            }
        }

        ExpenseCategory.values().filter { it != ExpenseCategory.SALARY_INCOME }.map { cat ->
            val spent = categorySpends[cat] ?: 0.0
            val limit = monthlyBudgets[cat]?.monthlyLimit
            val progress = if (limit != null && limit > 0) (spent / limit).toFloat() else 0f
            CategorySpendProgress(
                category = cat,
                spent = spent,
                limit = limit,
                percentage = progress,
                isOverBudget = limit != null && spent > limit,
                isNearBudget = limit != null && spent >= (limit * 0.8) && spent <= limit
            )
        }.sortedByDescending { it.spent }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // -------------------------------------------------------------
    // Machine Learning Actions
    // -------------------------------------------------------------

    fun trainLocalMlModel() {
        _isTrainingModel.value = true
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                // Collect existing historical transactions from DB to reinforce model
                val txList = transactions.value
                for (tx in txList) {
                    val text = tx.rawSmsBody ?: "${tx.type.name} at ${tx.merchant} amount ${tx.amount}"
                    mlModel.trainSample(text, tx.category, tx.merchant)
                }
                delay(600) // Visual progress simulation for training computation
            }
            _modelMetrics.value = mlModel.getModelMetrics()
            _isTrainingModel.value = false
            _statusMessage.value = "Local ML Model trained successfully on device! Accuracy: ${String.format(Locale.getDefault(), "%.1f%%", _modelMetrics.value.accuracy)}"
        }
    }

    fun reinforceMlFeedback(smsBody: String, correctCategory: ExpenseCategory, merchant: String? = null) {
        mlModel.reinforceFeedback(smsBody, correctCategory, merchant)
        _modelMetrics.value = mlModel.getModelMetrics()
        _statusMessage.value = "Learned pattern! Category updated to ${correctCategory.title}."
    }

    // -------------------------------------------------------------
    // Custom Spending Report Actions
    // -------------------------------------------------------------

    fun updateReportFilter(newFilter: CustomReportFilter) {
        _reportFilter.value = newFilter
    }

    fun setReportDatePreset(preset: DateRangePreset) {
        _reportFilter.update { it.copy(datePreset = preset) }
    }

    fun setReportCustomDates(start: Long, end: Long) {
        _reportFilter.update {
            it.copy(
                datePreset = DateRangePreset.CUSTOM,
                customStartTimestamp = start,
                customEndTimestamp = end
            )
        }
    }

    fun toggleReportCategory(category: ExpenseCategory) {
        _reportFilter.update { current ->
            val updated = current.selectedCategories.toMutableSet()
            if (category in updated) {
                updated.remove(category)
            } else {
                updated.add(category)
            }
            current.copy(selectedCategories = updated)
        }
    }

    fun selectAllReportCategories(selectAll: Boolean) {
        _reportFilter.update { current ->
            current.copy(
                selectedCategories = if (selectAll) ExpenseCategory.values().toSet() else emptySet()
            )
        }
    }

    fun toggleReportMerchant(merchant: String) {
        _reportFilter.update { current ->
            val updated = current.selectedMerchants.toMutableSet()
            if (merchant in updated) {
                updated.remove(merchant)
            } else {
                updated.add(merchant)
            }
            current.copy(selectedMerchants = updated)
        }
    }

    fun clearReportMerchantFilter() {
        _reportFilter.update { it.copy(selectedMerchants = emptySet()) }
    }

    fun setReportTypeFilter(typeFilter: TransactionTypeFilter) {
        _reportFilter.update { it.copy(typeFilter = typeFilter) }
    }

    fun setReportAmountRange(min: Double?, max: Double?) {
        _reportFilter.update { it.copy(minAmount = min, maxAmount = max) }
    }

    fun setReportSearchQuery(query: String) {
        _reportFilter.update { it.copy(searchQuery = query) }
    }

    fun saveCurrentFilterPreset(name: String) {
        if (name.isBlank()) return
        val newPreset = SavedReportPreset(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            filter = _reportFilter.value
        )
        _savedPresets.update { listOf(newPreset) + it }
        _statusMessage.value = "Preset \"$name\" saved!"
    }

    fun applySavedPreset(preset: SavedReportPreset) {
        _reportFilter.value = preset.filter
        _statusMessage.value = "Applied preset \"${preset.name}\""
    }

    fun deleteSavedPreset(id: String) {
        _savedPresets.update { list -> list.filter { it.id != id } }
        _statusMessage.value = "Preset removed."
    }

    fun exportCustomReportCsv(context: Context) {
        viewModelScope.launch {
            val insights = reportInsights.value
            val txList = insights.filteredTransactions
            if (txList.isEmpty()) {
                Toast.makeText(context, "No transactions match current report filter", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val filter = insights.filter
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dateLabel = filter.datePreset.displayName

            val csvContent = buildString {
                append("meeCrebit Custom Financial Spending Report\n")
                append("Generated 100% On-Device: ${SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
                append("Filter Scope: $dateLabel | Type: ${filter.typeFilter.displayName}\n\n")

                append("Transaction ID,Date & Time,Type,Merchant / Payee,Category,Amount (INR),Bank,Account,Balance After\n")
                for (tx in txList) {
                    val dateStr = sdf.format(Date(tx.timestamp))
                    append("\"${tx.id}\",\"$dateStr\",\"${tx.type.name}\",\"${tx.merchant.replace("\"", "\"\"")}\",\"${tx.category.title}\",\"${tx.amount}\",\"${tx.bankName}\",\"${tx.accountNumber}\",\"${tx.balanceAfter ?: "N/A"}\"\n")
                }

                append("\n--- EXECUTIVE SUMMARY ---\n")
                append("Total Expenses / Debits,${insights.totalDebits}\n")
                append("Total Income / Credits,${insights.totalCredits}\n")
                append("Net Flow,${insights.netFlow}\n")
                append("Transactions Analyzed,${insights.transactionCount}\n")
                append("Average Transaction Amount,${String.format(Locale.getDefault(), "%.2f", insights.averageSpendPerTx)}\n")
                append("Daily Burn Rate,${String.format(Locale.getDefault(), "%.2f", insights.dailyAverageBurnRate)} / day\n")
                append("Peak Spending Day,${insights.peakSpendingDayLabel}\n")
                append("Weekend vs Weekday Split,Weekend: ${String.format(Locale.getDefault(), "%.1f", insights.weekendSpendPercent * 100)}% | Weekday: ${String.format(Locale.getDefault(), "%.1f", insights.weekdaySpendPercent * 100)}%\n\n")

                append("--- TOP CATEGORIES ---\n")
                append("Category,Amount (INR),Percentage,Count\n")
                for (cat in insights.categoryBreakdown) {
                    append("\"${cat.category.title}\",${cat.totalAmount},${String.format(Locale.getDefault(), "%.1f", cat.percentage * 100)}%,${cat.transactionCount}\n")
                }

                append("\n--- TOP MERCHANTS ---\n")
                append("Merchant,Amount (INR),Percentage,Count\n")
                for (m in insights.topMerchants) {
                    append("\"${m.merchant.replace("\"", "\"\"")}\",${m.totalAmount},${String.format(Locale.getDefault(), "%.1f", m.percentage * 100)}%,${m.transactionCount}\n")
                }
            }

            try {
                val fileName = "meeCrebit_CustomReport_${System.currentTimeMillis()}.csv"
                val file = File(context.cacheDir, fileName)
                FileOutputStream(file).use { it.write(csvContent.toByteArray()) }

                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_SUBJECT, "meeCrebit Custom Spending Report - $dateLabel")
                    putExtra(Intent.EXTRA_TEXT, "Here is my custom spending analysis generated offline with meeCrebit.")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(shareIntent, "Share Custom Spending Report CSV")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to share report: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun shareCustomReportSummary(context: Context) {
        val insights = reportInsights.value
        val summaryText = buildString {
            append("📊 meeCrebit Spending Intelligence Report\n")
            append("📅 Scope: ${insights.filter.datePreset.displayName} (${insights.daysCountInRange} days)\n\n")
            append("• Total Spent: $${String.format(Locale.getDefault(), "%.2f", insights.totalDebits)}\n")
            append("• Total Inflow: $${String.format(Locale.getDefault(), "%.2f", insights.totalCredits)}\n")
            append("• Transactions Analyzed: ${insights.transactionCount}\n")
            append("• Daily Average Burn: $${String.format(Locale.getDefault(), "%.2f", insights.dailyAverageBurnRate)}/day\n")
            append("• Peak Spending Day: ${insights.peakSpendingDayLabel}\n\n")

            if (insights.categoryBreakdown.isNotEmpty()) {
                append("Top Categories:\n")
                insights.categoryBreakdown.take(3).forEach {
                    append("  - ${it.category.title}: $${String.format(Locale.getDefault(), "%.2f", it.totalAmount)} (${String.format(Locale.getDefault(), "%.1f", it.percentage * 100)}%)\n")
                }
                append("\n")
            }

            if (insights.topMerchants.isNotEmpty()) {
                append("Top Merchants:\n")
                insights.topMerchants.take(3).forEach {
                    append("  - ${it.merchant}: $${String.format(Locale.getDefault(), "%.2f", it.totalAmount)}\n")
                }
                append("\n")
            }

            append("🔒 Generated 100% offline & privacy-safe with meeCrebit.")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "meeCrebit Spending Summary")
            putExtra(Intent.EXTRA_TEXT, summaryText)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Spending Summary")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    // -------------------------------------------------------------
    // Biometric Security Controls
    // -------------------------------------------------------------

    fun setBiometricEnabled(enabled: Boolean) {
        biometricAuthManager.isBiometricEnabled = enabled
        _isBiometricEnabled.value = enabled
        if (!enabled) {
            _isAppLocked.value = false
            _statusMessage.value = "Biometric Lock disabled."
        } else {
            _statusMessage.value = "Biometric Lock enabled. Confidential ledger secured."
        }
    }

    fun setLockTimeout(seconds: Long) {
        biometricAuthManager.autoLockTimeoutMillis = seconds * 1000L
        _lockTimeoutSeconds.value = seconds
        _statusMessage.value = "Auto-lock timeout updated."
    }

    fun unlockApp() {
        _isAppLocked.value = false
        _biometricErrorMessage.value = null
    }

    fun lockApp() {
        if (_isBiometricEnabled.value) {
            _isAppLocked.value = true
        }
    }

    fun setBiometricErrorMessage(msg: String?) {
        _biometricErrorMessage.value = msg
    }

    fun recordAppPaused() {
        biometricAuthManager.recordAppPaused()
    }

    fun checkAppResumeLock() {
        if (biometricAuthManager.shouldLockOnResume()) {
            _isAppLocked.value = true
        }
    }

    fun getBiometricStatus() = biometricAuthManager.checkBiometricSupport()

    fun triggerWidgetUpdate() {
        MeeCrebitBudgetWidgetProvider.triggerUpdate(getApplication())
    }

    // -------------------------------------------------------------
    // General Actions
    // -------------------------------------------------------------

    fun testParseSms(smsText: String, sender: String = "BANK-ALERT") {
        if (smsText.isBlank()) {
            _testSmsResult.value = null
            return
        }
        val result = SmsParserEngine.parse(smsText, sender, merchantRules.value)
        _testSmsResult.value = result
    }

    fun saveParsedSmsToDb(result: ParsedSmsResult) {
        if (!result.isValidTransaction) return
        viewModelScope.launch {
            val entity = SmsParserEngine.toEntity(result)
            val id = repository.insertTransaction(entity)
            repository.addZenPoints(15)
            // Feed into ML model for on-device continuous learning
            mlModel.trainSample(result.rawBody, result.category, result.merchant)
            _modelMetrics.value = mlModel.getModelMetrics()
            _statusMessage.value = "Transaction saved offline! +15 Zen Points earned."
            triggerWidgetUpdate()

            // Trigger Notifications
            val context = getApplication<Application>()
            val db = MeeCrebitDatabase.getInstance(context)
            NotificationHelper.notifyTransactionLogged(context, entity.copy(id = id))
            NotificationHelper.checkAndNotifyBudgetExceeded(context, entity, db)
        }
    }

    fun addManualTransaction(
        amount: Double,
        type: TransactionType,
        merchant: String,
        category: ExpenseCategory,
        account: String,
        bank: String
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                amount = amount,
                type = type,
                merchant = merchant.ifBlank { if (type == TransactionType.DEBIT) "Expense" else "Income" },
                category = category,
                accountNumber = account.ifBlank { "XX1001" },
                bankName = bank.ifBlank { "Offline Bank" },
                timestamp = System.currentTimeMillis(),
                isManual = true
            )
            val id = repository.insertTransaction(entity)
            repository.addZenPoints(5)
            // Reinforce ML model with merchant & category pairing
            mlModel.trainSample("Manual entry for $merchant", category, merchant)
            _modelMetrics.value = mlModel.getModelMetrics()
            _statusMessage.value = "Transaction logged! +5 Zen Points."
            triggerWidgetUpdate()

            // Trigger Notifications
            val context = getApplication<Application>()
            val db = MeeCrebitDatabase.getInstance(context)
            NotificationHelper.notifyTransactionLogged(context, entity.copy(id = id))
            NotificationHelper.checkAndNotifyBudgetExceeded(context, entity, db)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            // Reinforce ML model
            mlModel.trainSample(transaction.rawSmsBody ?: "Tx ${transaction.merchant}", transaction.category, transaction.merchant)
            _modelMetrics.value = mlModel.getModelMetrics()
            _statusMessage.value = "Transaction updated & ML model refined."
            triggerWidgetUpdate()
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            _statusMessage.value = "Transaction deleted."
            triggerWidgetUpdate()
        }
    }

    fun setBudgetLimit(category: ExpenseCategory, limit: Double, monthYear: String = _selectedMonthYear.value) {
        viewModelScope.launch {
            val budget = BudgetEntity(
                category = category,
                monthlyLimit = limit,
                monthYear = monthYear
            )
            repository.insertOrUpdateBudget(budget)
            _statusMessage.value = "Budget limit for ${category.title} set to ₹${String.format(Locale("en", "IN"), "%,.2f", limit)}."
            triggerWidgetUpdate()

            // Check if current spending already exceeds the newly configured limit
            val context = getApplication<Application>()
            val db = MeeCrebitDatabase.getInstance(context)
            val checkTx = TransactionEntity(
                amount = 0.0,
                type = TransactionType.DEBIT,
                merchant = category.title,
                category = category,
                accountNumber = "",
                bankName = "",
                timestamp = System.currentTimeMillis()
            )
            NotificationHelper.checkAndNotifyBudgetExceeded(context, checkTx, db)
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteBudget(id)
            _statusMessage.value = "Budget limit removed."
            triggerWidgetUpdate()
        }
    }

    fun testSendTransactionNotification() {
        val context = getApplication<Application>()
        val testTx = TransactionEntity(
            amount = 1499.00,
            type = TransactionType.DEBIT,
            merchant = "Amazon India",
            category = ExpenseCategory.SHOPPING,
            accountNumber = "XX9123",
            bankName = "HDFC Bank",
            timestamp = System.currentTimeMillis(),
            isManual = false
        )
        NotificationHelper.notifyTransactionLogged(context, testTx)
        _statusMessage.value = "Transaction alert sent to notification shade!"
    }

    fun testSendBudgetExceededNotification() {
        val context = getApplication<Application>()
        val monthDisplay = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        NotificationHelper.notifyBudgetExceeded(
            context = context,
            title = "⚠️ Monthly Budget Exceeded: Food & Dining",
            message = "You have spent ₹8,450.00 in Food & Dining for $monthDisplay, exceeding your monthly limit of ₹8,000.00 (5% over limit)."
        )
        _statusMessage.value = "Budget limit alert sent to notification shade!"
    }

    fun scanExistingInbox() {
        _isScanningInbox.value = true
        viewModelScope.launch {
            val context = getApplication<Application>()
            val foundTxns = SmsInboxScanner.scanInbox(context, limit = 150)
            if (foundTxns.isNotEmpty()) {
                var newCount = 0
                var skippedDuplicates = 0
                for (tx in foundTxns) {
                    val inserted = repository.insertTransactionDeduplicated(tx)
                    if (inserted) {
                        newCount++
                        if (tx.rawSmsBody != null) {
                            mlModel.trainSample(tx.rawSmsBody, tx.category, tx.merchant)
                        }
                    } else {
                        skippedDuplicates++
                    }
                }
                if (newCount > 0) {
                    repository.addZenPoints(newCount * 5)
                    _modelMetrics.value = mlModel.getModelMetrics()
                    _statusMessage.value = "Scanned inbox: Added $newCount new transactions ($skippedDuplicates duplicates skipped)."
                } else {
                    _statusMessage.value = "Inbox up to date: All $skippedDuplicates detected SMS transactions are already recorded."
                }
                triggerWidgetUpdate()
            } else {
                _statusMessage.value = "No standard bank transaction SMS found in device inbox."
            }
            _isScanningInbox.value = false
        }
    }

    fun loadRealisticDemoData() {
        viewModelScope.launch {
            repository.seedRealisticDemoData()
            trainLocalMlModel()
            _statusMessage.value = "Realistic financial demo dataset loaded offline! ML model trained."
            triggerWidgetUpdate()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _statusMessage.value = "All offline data cleared."
            triggerWidgetUpdate()
        }
    }

    fun exportStatementCsv(context: Context) {
        viewModelScope.launch {
            val month = _selectedMonthYear.value
            val txList = currentMonthTransactions.value
            if (txList.isEmpty()) {
                Toast.makeText(context, "No transactions to export for $month", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val csvContent = buildString {
                append("meeCrebit Offline Financial Statement - $month\n")
                append("Generated 100% on-device on ${SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault()).format(Date())}\n\n")
                append("Transaction ID,Date & Time,Type,Merchant / Payee,Category,Amount (INR),Bank,Account,Balance After,Auto Logged Via\n")

                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                for (tx in txList) {
                    val dateStr = sdf.format(Date(tx.timestamp))
                    val autoStr = if (tx.isManual) "Manual" else "Offline SMS (${tx.sender ?: "SMS"})"
                    append("\"${tx.id}\",\"$dateStr\",\"${tx.type.name}\",\"${tx.merchant.replace("\"", "\"\"")}\",\"${tx.category.title}\",\"${tx.amount}\",\"${tx.bankName}\",\"${tx.accountNumber}\",\"${tx.balanceAfter ?: "N/A"}\",\"$autoStr\"\n")
                }

                val totalIncome = txList.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount }
                val totalExpense = txList.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
                val netSavings = totalIncome - totalExpense

                append("\nSUMMARY\n")
                append("Total Inflow / Income,$totalIncome\n")
                append("Total Outflow / Spent,$totalExpense\n")
                append("Net Monthly Savings,$netSavings\n")
                append("Savings Rate,${if (totalIncome > 0) String.format("%.1f", (netSavings / totalIncome) * 100) else "0.0"}%\n")
            }

            try {
                val fileName = "meeCrebit_Statement_$month.csv"
                val cacheDir = context.cacheDir
                val file = File(cacheDir, fileName)
                FileOutputStream(file).use { it.write(csvContent.toByteArray()) }

                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_SUBJECT, "meeCrebit Statement - $month")
                    putExtra(Intent.EXTRA_TEXT, "Here is my privacy-first monthly financial statement for $month generated offline with meeCrebit.")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(shareIntent, "Share Monthly Statement CSV")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to share CSV: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // -------------------------------------------------------------
    // Merchant Auto-Tagging & Rule Engine Actions
    // -------------------------------------------------------------

    fun saveMerchantRule(
        pattern: String,
        matchType: RuleMatchType,
        category: ExpenseCategory,
        overrideName: String? = null,
        id: Long = 0
    ) {
        if (pattern.isBlank()) return
        viewModelScope.launch {
            val rule = MerchantRuleEntity(
                id = id,
                pattern = pattern.trim(),
                matchType = matchType,
                targetCategory = category,
                overrideMerchantName = overrideName?.trim()?.ifBlank { null }
            )
            repository.insertOrUpdateRule(rule)
            _statusMessage.value = "Rule saved: '${rule.pattern}' -> ${category.title}"
        }
    }

    fun toggleMerchantRule(rule: MerchantRuleEntity) {
        viewModelScope.launch {
            repository.insertOrUpdateRule(rule.copy(isEnabled = !rule.isEnabled))
            _statusMessage.value = "Rule ${if (!rule.isEnabled) "enabled" else "disabled"}."
        }
    }

    fun deleteMerchantRule(id: Long) {
        viewModelScope.launch {
            repository.deleteRule(id)
            _statusMessage.value = "Rule removed."
        }
    }

    // -------------------------------------------------------------
    // AES-256 Encrypted Offline Backup & Restore Actions
    // -------------------------------------------------------------

    fun createEncryptedBackup(context: Context, password: CharSequence) {
        if (password.length < 4) {
            Toast.makeText(context, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show()
            return
        }
        viewModelScope.launch {
            try {
                val txList = transactions.value
                val bList = budgets.value
                val rList = merchantRules.value
                val zProf = zenProfile.value

                val backupFile = EncryptedBackupManager.createEncryptedBackup(
                    context = context,
                    password = password.toString(),
                    transactions = txList,
                    budgets = bList,
                    rules = rList,
                    zenProfile = zProf
                )

                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    backupFile
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_SUBJECT, "meeCrebit Encrypted Backup")
                    putExtra(Intent.EXTRA_TEXT, "Encrypted offline financial ledger backup. Keep your password safe.")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(shareIntent, "Save Encrypted Backup (.meecrebit)")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                _statusMessage.value = "Encrypted backup created with AES-256 GCM!"
            } catch (e: Exception) {
                Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun restoreEncryptedBackup(
        backupContent: String,
        password: CharSequence,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val restored = EncryptedBackupManager.restoreFromEncryptedBackup(
                    backupFileContent = backupContent,
                    password = password.toString()
                )
                repository.restoreLedger(restored)
                trainLocalMlModel()
                _statusMessage.value = "Successfully restored ${restored.transactions.size} transactions & ${restored.rules.size} rules!"
                onSuccess(restored.transactions.size)
            } catch (e: Exception) {
                val msg = e.message ?: "Invalid password or corrupted backup file."
                onError(msg)
            }
        }
    }
}

