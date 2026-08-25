package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.BillReminderEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.GoalContributionEntity
import com.example.data.model.MerchantRuleEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.SplitExpenseEntity
import com.example.data.model.SplitParticipantEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.ZenProfileEntity

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        ZenProfileEntity::class,
        MerchantRuleEntity::class,
        SavingsGoalEntity::class,
        GoalContributionEntity::class,
        SplitExpenseEntity::class,
        SplitParticipantEntity::class,
        BillReminderEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MeeCrebitDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun zenProfileDao(): ZenProfileDao
    abstract fun merchantRuleDao(): MerchantRuleDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun goalContributionDao(): GoalContributionDao
    abstract fun splitExpenseDao(): SplitExpenseDao
    abstract fun splitParticipantDao(): SplitParticipantDao
    abstract fun billReminderDao(): BillReminderDao

    companion object {
        @Volatile
        private var INSTANCE: MeeCrebitDatabase? = null

        fun getInstance(context: Context): MeeCrebitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeeCrebitDatabase::class.java,
                    "meecrebit_offline.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

