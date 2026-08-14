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
}
