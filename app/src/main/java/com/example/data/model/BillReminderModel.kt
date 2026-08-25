package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.max

enum class BillFrequency(val title: String, val daysInCycle: Int) {
    ONE_TIME("One Time", 0),
    MONTHLY("Monthly", 30),
    QUARTERLY("Quarterly (3 Months)", 90),
    HALF_YEARLY("Half-Yearly (6 Months)", 180),
    YEARLY("Yearly", 365)
}

enum class BillReminderType(val title: String, val hexColor: Long) {
    CREDIT_CARD("Credit Card Bill", 0xFF8B5CF6),
    ELECTRICITY("Electricity Bill", 0xFFF59E0B),
    RENT("House Rent", 0xFF3B82F6),
    INTERNET("Wi-Fi / Broadband", 0xFF06B6D4),
    SUBSCRIPTION("OTT / Subscription", 0xFFEC4899),
    INSURANCE("Insurance Premium", 0xFF10B981),
    LOAN_EMI("Loan / EMI", 0xFFEF4444),
    OTHER("Other Utility Bill", 0xFF64748B);

    fun getIcon(): ImageVector {
        return when (this) {
            CREDIT_CARD -> Icons.Default.CreditCard
            ELECTRICITY -> Icons.Default.ElectricBolt
            RENT -> Icons.Default.Home
            INTERNET -> Icons.Default.Wifi
            SUBSCRIPTION -> Icons.Default.Tv
            INSURANCE -> Icons.Default.Shield
            LOAN_EMI -> Icons.Default.LocalAtm
            OTHER -> Icons.Default.Receipt
        }
    }
}

enum class BillStatus {
    OVERDUE,
    DUE_TODAY,
    DUE_SOON,
    UPCOMING,
    PAID
}

@Entity(tableName = "bill_reminders")
data class BillReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val dueDate: Long, // timestamp in ms for current due date
    val frequency: BillFrequency = BillFrequency.MONTHLY,
    val reminderType: BillReminderType = BillReminderType.OTHER,
    val reminderDaysBefore: Int = 2, // notify 2 days before due date
    val isPaid: Boolean = false,
    val lastPaidDate: Long? = null,
    val billerOrBank: String = "",
    val autoPayEnabled: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val status: BillStatus
        get() {
            if (isPaid) return BillStatus.PAID
            val now = System.currentTimeMillis()
            val calNow = Calendar.getInstance().apply { timeInMillis = now }
            val calDue = Calendar.getInstance().apply { timeInMillis = dueDate }

            val isSameDay = calNow.get(Calendar.YEAR) == calDue.get(Calendar.YEAR) &&
                    calNow.get(Calendar.DAY_OF_YEAR) == calDue.get(Calendar.DAY_OF_YEAR)

            if (isSameDay) return BillStatus.DUE_TODAY
            if (dueDate < now) return BillStatus.OVERDUE

            val diffDays = TimeUnit.MILLISECONDS.toDays(dueDate - now)
            return if (diffDays <= 3) BillStatus.DUE_SOON else BillStatus.UPCOMING
        }

    val daysUntilDue: Long
        get() {
            val diffMs = dueDate - System.currentTimeMillis()
            return TimeUnit.MILLISECONDS.toDays(diffMs)
        }
}
