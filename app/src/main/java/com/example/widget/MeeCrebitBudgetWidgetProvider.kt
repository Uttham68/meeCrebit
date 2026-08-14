package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.db.MeeCrebitDatabase
import com.example.data.model.TransactionType
import com.example.ui.components.formatCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MeeCrebitBudgetWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateAllWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_MANUAL_REFRESH || intent.action == ACTION_DATA_UPDATED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, MeeCrebitBudgetWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)
            if (ids.isNotEmpty()) {
                updateAllWidgets(context, appWidgetManager, ids)
            }
        }
    }

    private fun updateAllWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = MeeCrebitDatabase.getInstance(context)
                val txDao = db.transactionDao()
                val budgetDao = db.budgetDao()

                // Calculate current month's start & end timestamps
                val cal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfMonth = cal.timeInMillis
                val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()).uppercase()

                // Query DB
                val allTx = txDao.getAllTransactions().first()
                val monthDebits = allTx
                    .filter { it.timestamp >= startOfMonth && it.type == TransactionType.DEBIT }
                    .sumOf { it.amount }

                val budgets = budgetDao.getAllBudgets().first()
                val totalBudget = if (budgets.isNotEmpty()) budgets.sumOf { it.monthlyLimit } else 2000.0

                val remaining = totalBudget - monthDebits
                val percentUsed = if (totalBudget > 0) ((monthDebits / totalBudget) * 100).toInt().coerceIn(0, 100) else 0

                val statusText: String
                val remainingTextColor: Int

                when {
                    monthDebits > totalBudget -> {
                        statusText = "OVER BUDGET"
                        remainingTextColor = 0xFFEF4444.toInt() // Red
                    }
                    percentUsed >= 80 -> {
                        statusText = "CAUTION"
                        remainingTextColor = 0xFFF59E0B.toInt() // Amber
                    }
                    else -> {
                        statusText = "ON TRACK"
                        remainingTextColor = 0xFF10B981.toInt() // Emerald
                    }
                }

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_meecrebit_budget)

                    // Set texts
                    views.setTextViewText(R.id.widget_month_label, "$monthName SPENDING")
                    views.setTextViewText(R.id.widget_spent_amount, formatCurrency(monthDebits))
                    views.setTextViewText(R.id.widget_budget_total, "/ ${formatCurrency(totalBudget)}")
                    views.setTextViewText(R.id.widget_status_badge, statusText)

                    if (monthDebits > totalBudget) {
                        val overAmount = monthDebits - totalBudget
                        views.setTextViewText(R.id.widget_remaining_text, "${formatCurrency(overAmount)} over budget")
                    } else {
                        views.setTextViewText(R.id.widget_remaining_text, "${formatCurrency(remaining)} remaining")
                    }
                    views.setTextColor(R.id.widget_remaining_text, remainingTextColor)
                    views.setTextViewText(R.id.widget_percent_text, "$percentUsed% used")

                    // Progress bar
                    views.setProgressBar(R.id.widget_progress_bar, 100, percentUsed, false)

                    // Click intent: Open App
                    val openAppIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val openAppPendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        openAppIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

                    // Click intent: Refresh button
                    val refreshIntent = Intent(context, MeeCrebitBudgetWidgetProvider::class.java).apply {
                        action = ACTION_MANUAL_REFRESH
                    }
                    val refreshPendingIntent = PendingIntent.getBroadcast(
                        context,
                        1,
                        refreshIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_MANUAL_REFRESH = "com.example.widget.ACTION_MANUAL_REFRESH"
        const val ACTION_DATA_UPDATED = "com.example.widget.ACTION_DATA_UPDATED"

        fun triggerUpdate(context: Context) {
            val intent = Intent(context, MeeCrebitBudgetWidgetProvider::class.java).apply {
                action = ACTION_DATA_UPDATED
            }
            context.sendBroadcast(intent)
        }
    }
}
