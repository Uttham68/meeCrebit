package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Locale

@Entity(tableName = "split_expenses")
data class SplitExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val totalAmount: Double,
    val paidByMe: Boolean = true,
    val date: Long = System.currentTimeMillis(),
    val category: ExpenseCategory = ExpenseCategory.FOOD_DINING,
    val notes: String = "",
    val linkedTransactionId: Long? = null,
    val isFullySettled: Boolean = false
)

@Entity(
    tableName = "split_participants",
    foreignKeys = [
        ForeignKey(
            entity = SplitExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["splitExpenseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["splitExpenseId"])]
)
data class SplitParticipantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val splitExpenseId: Long,
    val personName: String,
    val amountOwed: Double,
    val amountPaid: Double = 0.0,
    val isSettled: Boolean = false,
    val settledDate: Long? = null,
    val phoneOrUpi: String = ""
) {
    val remainingToSettle: Double
        get() = (amountOwed - amountPaid).coerceAtLeast(0.0)
}

data class SplitExpenseWithParticipants(
    val expense: SplitExpenseEntity,
    val participants: List<SplitParticipantEntity>
) {
    val totalOwedToMe: Double
        get() = if (expense.paidByMe) {
            participants.filter { !it.isSettled }.sumOf { it.remainingToSettle }
        } else {
            0.0
        }

    val totalSettledByOthers: Double
        get() = participants.sumOf { it.amountPaid }
}

data class PersonIouSummary(
    val personName: String,
    val netBalance: Double, // Positive = they owe you; Negative = you owe them
    val pendingExpensesCount: Int,
    val phoneOrUpi: String = "",
    val latestActivityDate: Long = 0
)
