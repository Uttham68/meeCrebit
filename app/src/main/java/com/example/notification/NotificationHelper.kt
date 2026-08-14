package com.example.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.db.MeeCrebitDatabase
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object NotificationHelper {

    const val CHANNEL_TXNS = "meecrebit_txns"
    const val CHANNEL_BUDGET_ALERTS = "meecrebit_budget_alerts"

    private const val NOTIFICATION_ID_BUDGET_BASE = 9000

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            // Channel 1: Transaction Logged Alerts
            val txnChannel = NotificationChannel(
                CHANNEL_TXNS,
                "Transaction Logged Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when a new financial transaction is parsed or logged offline"
                enableVibration(true)
            }

            // Channel 2: Monthly Budget Limit Exceeded Alerts
            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGET_ALERTS,
                "Budget & Limit Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority warning when your spending exceeds your monthly budget limit"
                enableVibration(true)
                enableLights(true)
            }

            notificationManager.createNotificationChannel(txnChannel)
            notificationManager.createNotificationChannel(budgetChannel)
        }
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun notifyTransactionLogged(
        context: Context,
        transaction: TransactionEntity,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
    ) {
        if (!hasNotificationPermission(context)) {
            Log.w("NotificationHelper", "POST_NOTIFICATIONS permission not granted. Skipping notification.")
            return
        }

        createNotificationChannels(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isDebit = transaction.type == TransactionType.DEBIT
        val sign = if (isDebit) "-" else "+"
        val formattedAmt = String.format(Locale("en", "IN"), "₹%,.2f", transaction.amount)
        val title = "Transaction Logged: $sign$formattedAmt"
        val content = "${transaction.merchant} • ${transaction.category.title} (${transaction.bankName} • ${transaction.accountNumber}). Logged 100% offline."

        val notification = NotificationCompat.Builder(context, CHANNEL_TXNS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun notifyBudgetExceeded(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = NOTIFICATION_ID_BUDGET_BASE
    ) {
        if (!hasNotificationPermission(context)) {
            Log.w("NotificationHelper", "POST_NOTIFICATIONS permission not granted for budget alert.")
            return
        }

        createNotificationChannels(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    suspend fun checkAndNotifyBudgetExceeded(
        context: Context,
        transaction: TransactionEntity,
        db: MeeCrebitDatabase
    ) {
        if (transaction.type != TransactionType.DEBIT) return

        try {
            val cal = Calendar.getInstance().apply {
                timeInMillis = transaction.timestamp
            }
            val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val monthYear = monthYearFormat.format(cal.time)
            val monthDisplayFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val monthDisplay = monthDisplayFormat.format(cal.time)

            // Calculate start and end of month timestamp
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfMonth = cal.timeInMillis

            cal.add(Calendar.MONTH, 1)
            cal.add(Calendar.MILLISECOND, -1)
            val endOfMonth = cal.timeInMillis

            // 1. Check specific Category Budget Limit
            val categoryBudget = db.budgetDao().getBudgetByCategoryAndMonth(transaction.category, monthYear)
            if (categoryBudget != null && categoryBudget.monthlyLimit > 0) {
                val categorySpent = db.transactionDao().getCategoryExpenseBetweenSync(
                    transaction.category,
                    startOfMonth,
                    endOfMonth
                ) ?: 0.0

                if (categorySpent > categoryBudget.monthlyLimit) {
                    val formattedSpent = String.format(Locale("en", "IN"), "₹%,.2f", categorySpent)
                    val formattedLimit = String.format(Locale("en", "IN"), "₹%,.2f", categoryBudget.monthlyLimit)
                    val percentOver = (((categorySpent - categoryBudget.monthlyLimit) / categoryBudget.monthlyLimit) * 100).toInt()

                    val title = "⚠️ Monthly Budget Exceeded: ${transaction.category.title}"
                    val message = "You have spent $formattedSpent in ${transaction.category.title} for $monthDisplay, exceeding your limit of $formattedLimit ($percentOver% over limit)."

                    notifyBudgetExceeded(
                        context = context,
                        title = title,
                        message = message,
                        notificationId = NOTIFICATION_ID_BUDGET_BASE + transaction.category.ordinal
                    )
                }
            }

            // 2. Check Overall Monthly Budget Limit (sum of all category budgets for this month)
            val allMonthlyBudgets = db.budgetDao().getBudgetsForMonthSync(monthYear)
            val totalLimit = allMonthlyBudgets.sumOf { it.monthlyLimit }
            if (totalLimit > 0) {
                val totalSpent = db.transactionDao().getTotalExpenseBetweenSync(startOfMonth, endOfMonth) ?: 0.0
                if (totalSpent > totalLimit) {
                    val formattedTotalSpent = String.format(Locale("en", "IN"), "₹%,.2f", totalSpent)
                    val formattedTotalLimit = String.format(Locale("en", "IN"), "₹%,.2f", totalLimit)
                    val percentOverTotal = (((totalSpent - totalLimit) / totalLimit) * 100).toInt()

                    val title = "🚨 Overall Monthly Budget Exceeded!"
                    val message = "Total monthly expenses reached $formattedTotalSpent, exceeding your overall limit of $formattedTotalLimit for $monthDisplay ($percentOverTotal% over budget)."

                    notifyBudgetExceeded(
                        context = context,
                        title = title,
                        message = message,
                        notificationId = NOTIFICATION_ID_BUDGET_BASE + 999
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Failed to check budget threshold: ${e.message}")
        }
    }
}
