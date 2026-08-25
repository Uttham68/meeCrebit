package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.max

enum class SavingsGoalCategory(
    val title: String,
    val hexColor: Long
) {
    EMERGENCY_FUND("Emergency Fund", 0xFF10B981), // Emerald
    VACATION("Vacation & Travel", 0xFF3B82F6),     // Blue
    GADGETS("Gadget / Tech", 0xFF8B5CF6),        // Purple
    VEHICLE("Car / Bike", 0xFFF59E0B),           // Amber
    HOME("Home & Living", 0xFFEC4899),           // Pink
    EDUCATION("Education / Course", 0xFF14B8A6), // Teal
    WEDDING_GIFT("Celebration / Gift", 0xFFF97316),// Orange
    CUSTOM("Custom Sinking Fund", 0xFF64748B);   // Slate

    fun getIcon(): ImageVector {
        return when (this) {
            EMERGENCY_FUND -> Icons.Default.Shield
            VACATION -> Icons.Default.Flight
            GADGETS -> Icons.Default.Laptop
            VEHICLE -> Icons.Default.DirectionsCar
            HOME -> Icons.Default.Home
            EDUCATION -> Icons.Default.School
            WEDDING_GIFT -> Icons.Default.CardGiftcard
            CUSTOM -> Icons.Default.Savings
        }
    }
}

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: Long, // timestamp in ms
    val category: SavingsGoalCategory = SavingsGoalCategory.CUSTOM,
    val colorHex: Long = category.hexColor,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val progressFraction: Float
        get() = if (targetAmount > 0) (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()

    val remainingAmount: Double
        get() = max(0.0, targetAmount - currentAmount)

    val daysRemaining: Long
        get() {
            val diffMs = targetDate - System.currentTimeMillis()
            return if (diffMs <= 0) 0 else TimeUnit.MILLISECONDS.toDays(diffMs)
        }

    val monthsRemaining: Double
        get() = max(0.1, daysRemaining / 30.4375)

    val requiredMonthlySavings: Double
        get() {
            if (isCompleted || remainingAmount <= 0) return 0.0
            return remainingAmount / monthsRemaining
        }

    val requiredDailySavings: Double
        get() {
            if (isCompleted || remainingAmount <= 0) return 0.0
            val days = max(1L, daysRemaining)
            return remainingAmount / days.toDouble()
        }
}

@Entity(
    tableName = "goal_contributions",
    foreignKeys = [
        ForeignKey(
            entity = SavingsGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["goalId"])]
)
data class GoalContributionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val note: String = "Manual Contribution",
    val linkedTransactionId: Long? = null
)
