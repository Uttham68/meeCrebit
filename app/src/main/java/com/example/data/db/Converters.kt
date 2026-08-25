package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.ExpenseCategory
import com.example.data.model.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return try {
            TransactionType.valueOf(value)
        } catch (e: Exception) {
            TransactionType.DEBIT
        }
    }

    @TypeConverter
    fun fromExpenseCategory(value: ExpenseCategory): String {
        return value.name
    }

    @TypeConverter
    fun toExpenseCategory(value: String): ExpenseCategory {
        return try {
            ExpenseCategory.valueOf(value)
        } catch (e: Exception) {
            ExpenseCategory.OTHERS
        }
    }

    @TypeConverter
    fun fromSavingsGoalCategory(value: com.example.data.model.SavingsGoalCategory): String {
        return value.name
    }

    @TypeConverter
    fun toSavingsGoalCategory(value: String): com.example.data.model.SavingsGoalCategory {
        return try {
            com.example.data.model.SavingsGoalCategory.valueOf(value)
        } catch (e: Exception) {
            com.example.data.model.SavingsGoalCategory.CUSTOM
        }
    }

    @TypeConverter
    fun fromBillFrequency(value: com.example.data.model.BillFrequency): String {
        return value.name
    }

    @TypeConverter
    fun toBillFrequency(value: String): com.example.data.model.BillFrequency {
        return try {
            com.example.data.model.BillFrequency.valueOf(value)
        } catch (e: Exception) {
            com.example.data.model.BillFrequency.MONTHLY
        }
    }

    @TypeConverter
    fun fromBillReminderType(value: com.example.data.model.BillReminderType): String {
        return value.name
    }

    @TypeConverter
    fun toBillReminderType(value: String): com.example.data.model.BillReminderType {
        return try {
            com.example.data.model.BillReminderType.valueOf(value)
        } catch (e: Exception) {
            com.example.data.model.BillReminderType.OTHER
        }
    }
}
