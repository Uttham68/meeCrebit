package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BudgetEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.ZenProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsSync(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE rawSmsBody = :rawSmsBody LIMIT 1")
    suspend fun findByRawSms(rawSmsBody: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE merchant = :merchant AND amount = :amount AND timestamp >= :minTime AND timestamp <= :maxTime LIMIT 1")
    suspend fun findDuplicate(merchant: String, amount: Double, minTime: Long, maxTime: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT' AND timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalExpenseBetween(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT' AND category = :category AND timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getCategoryExpenseBetweenSync(category: ExpenseCategory, startTime: Long, endTime: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT' AND timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getTotalExpenseBetweenSync(startTime: Long, endTime: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'CREDIT' AND timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalIncomeBetween(startTime: Long, endTime: Long): Flow<Double?>
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    fun getBudgetsForMonth(monthYear: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    suspend fun getBudgetsForMonthSync(monthYear: String): List<BudgetEntity>

    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE category = :category AND monthYear = :monthYear LIMIT 1")
    suspend fun getBudgetByCategoryAndMonth(category: ExpenseCategory, monthYear: String): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<BudgetEntity>)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()
}

@Dao
interface ZenProfileDao {
    @Query("SELECT * FROM zen_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<ZenProfileEntity?>

    @Query("SELECT * FROM zen_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): ZenProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: ZenProfileEntity)
}

@Dao
interface MerchantRuleDao {
    @Query("SELECT * FROM merchant_rules ORDER BY id DESC")
    fun getAllRules(): Flow<List<com.example.data.model.MerchantRuleEntity>>

    @Query("SELECT * FROM merchant_rules WHERE isEnabled = 1")
    suspend fun getActiveRulesSync(): List<com.example.data.model.MerchantRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRule(rule: com.example.data.model.MerchantRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<com.example.data.model.MerchantRuleEntity>)

    @Delete
    suspend fun deleteRule(rule: com.example.data.model.MerchantRuleEntity)

    @Query("DELETE FROM merchant_rules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM merchant_rules")
    suspend fun deleteAll()
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY isCompleted ASC, targetDate ASC")
    fun getAllGoals(): Flow<List<com.example.data.model.SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getGoalById(id: Long): com.example.data.model.SavingsGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGoal(goal: com.example.data.model.SavingsGoalEntity): Long

    @Delete
    suspend fun deleteGoal(goal: com.example.data.model.SavingsGoalEntity)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)

    @Query("UPDATE savings_goals SET currentAmount = currentAmount + :delta WHERE id = :goalId")
    suspend fun updateGoalAmount(goalId: Long, delta: Double)

    @Query("UPDATE savings_goals SET isCompleted = :isCompleted WHERE id = :goalId")
    suspend fun setGoalCompleted(goalId: Long, isCompleted: Boolean)
}

@Dao
interface GoalContributionDao {
    @Query("SELECT * FROM goal_contributions WHERE goalId = :goalId ORDER BY date DESC")
    fun getContributionsForGoal(goalId: Long): Flow<List<com.example.data.model.GoalContributionEntity>>

    @Query("SELECT * FROM goal_contributions ORDER BY date DESC")
    fun getAllContributions(): Flow<List<com.example.data.model.GoalContributionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: com.example.data.model.GoalContributionEntity): Long

    @Delete
    suspend fun deleteContribution(contribution: com.example.data.model.GoalContributionEntity)
}

@Dao
interface SplitExpenseDao {
    @Query("SELECT * FROM split_expenses ORDER BY date DESC")
    fun getAllSplitExpenses(): Flow<List<com.example.data.model.SplitExpenseEntity>>

    @Query("SELECT * FROM split_expenses WHERE id = :id")
    suspend fun getSplitExpenseById(id: Long): com.example.data.model.SplitExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplitExpense(expense: com.example.data.model.SplitExpenseEntity): Long

    @Update
    suspend fun updateSplitExpense(expense: com.example.data.model.SplitExpenseEntity)

    @Delete
    suspend fun deleteSplitExpense(expense: com.example.data.model.SplitExpenseEntity)

    @Query("DELETE FROM split_expenses WHERE id = :id")
    suspend fun deleteSplitExpenseById(id: Long)
}

@Dao
interface SplitParticipantDao {
    @Query("SELECT * FROM split_participants WHERE splitExpenseId = :splitExpenseId")
    fun getParticipantsForExpense(splitExpenseId: Long): Flow<List<com.example.data.model.SplitParticipantEntity>>

    @Query("SELECT * FROM split_participants WHERE splitExpenseId = :splitExpenseId")
    suspend fun getParticipantsForExpenseSync(splitExpenseId: Long): List<com.example.data.model.SplitParticipantEntity>

    @Query("SELECT * FROM split_participants")
    fun getAllParticipants(): Flow<List<com.example.data.model.SplitParticipantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(participants: List<com.example.data.model.SplitParticipantEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: com.example.data.model.SplitParticipantEntity): Long

    @Update
    suspend fun updateParticipant(participant: com.example.data.model.SplitParticipantEntity)

    @Query("UPDATE split_participants SET amountPaid = amountOwed, isSettled = 1, settledDate = :settledDate WHERE id = :participantId")
    suspend fun markParticipantSettled(participantId: Long, settledDate: Long)

    @Delete
    suspend fun deleteParticipant(participant: com.example.data.model.SplitParticipantEntity)
}

@Dao
interface BillReminderDao {
    @Query("SELECT * FROM bill_reminders ORDER BY isPaid ASC, dueDate ASC")
    fun getAllReminders(): Flow<List<com.example.data.model.BillReminderEntity>>

    @Query("SELECT * FROM bill_reminders WHERE isPaid = 0 ORDER BY dueDate ASC")
    fun getActiveReminders(): Flow<List<com.example.data.model.BillReminderEntity>>

    @Query("SELECT * FROM bill_reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): com.example.data.model.BillReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateReminder(reminder: com.example.data.model.BillReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: com.example.data.model.BillReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: com.example.data.model.BillReminderEntity)

    @Query("DELETE FROM bill_reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)
}


