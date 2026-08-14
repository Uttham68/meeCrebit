package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType(val displayName: String) {
    DEBIT("Debit / Spent"),
    CREDIT("Credit / Income")
}

enum class ExpenseCategory(
    val title: String,
    val hexColor: Long
) {
    FOOD_DINING("Food", 0xFFF97316),
    CASH("Cash", 0xFF10B981),
    GROCERIES("Groceries", 0xFF10B981),
    SHOPPING("Shopping", 0xFF8B5CF6),
    TRANSPORT("Travel", 0xFF3B82F6),
    BILLS_UTILITIES("Bills", 0xFFEF4444),
    ENTERTAINMENT("Entertainment", 0xFFEC4899),
    HEALTH_FITNESS("Health", 0xFF14B8A6),
    SALARY_INCOME("Salary", 0xFF22C55E),
    INVESTMENTS("Investments", 0xFFEAB308),
    OTHERS("Other", 0xFF64748B);

    fun getIcon(): ImageVector {
        return when (this) {
            FOOD_DINING -> Icons.Default.Fastfood
            CASH -> Icons.Default.LocalAtm
            GROCERIES -> Icons.Default.ShoppingCart
            SHOPPING -> Icons.Default.ShoppingBag
            TRANSPORT -> Icons.Default.DirectionsCar
            BILLS_UTILITIES -> Icons.Default.Receipt
            ENTERTAINMENT -> Icons.Default.Movie
            HEALTH_FITNESS -> Icons.Default.HealthAndSafety
            SALARY_INCOME -> Icons.Default.Payments
            INVESTMENTS -> Icons.Default.TrendingUp
            OTHERS -> Icons.Default.Category
        }
    }
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val merchant: String,
    val category: ExpenseCategory,
    val accountNumber: String,
    val bankName: String,
    val balanceAfter: Double? = null,
    val rawSmsBody: String? = null,
    val sender: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isManual: Boolean = false
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: ExpenseCategory,
    val monthlyLimit: Double,
    val monthYear: String // Format: "YYYY-MM"
)

@Entity(tableName = "zen_profile")
data class ZenProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val totalPoints: Int = 120,
    val streakDays: Int = 1,
    val lastActiveDate: String = "",
    val monthlySavingsGoal: Double = 5000.0
)
