package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.db.MeeCrebitDatabase
import com.example.data.model.TransactionType
import com.example.engine.SmsParserEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.util.Locale

class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        val fullBody = buildString {
            for (msg in messages) {
                append(msg.displayMessageBody)
            }
        }
        val sender = messages[0].displayOriginatingAddress ?: ""
        val timestamp = messages[0].timestampMillis

        Log.d("MeeCrebitReceiver", "SMS received from $sender: $fullBody")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = MeeCrebitDatabase.getInstance(context)
                val existing = db.transactionDao().findByRawSms(fullBody)
                if (existing != null) {
                    return@launch
                }

                val activeRules = db.merchantRuleDao().getActiveRulesSync()
                val parsed = SmsParserEngine.parse(fullBody, sender, activeRules)
                if (!parsed.isValidTransaction) {
                    return@launch
                }

                val txnDate = SmsParserEngine.extractTransactionDate(fullBody, timestamp)
                val entity = SmsParserEngine.toEntity(parsed, txnDate)

                // Check 60-second duplicate window
                val duplicate = db.transactionDao().findDuplicate(
                    merchant = entity.merchant,
                    amount = entity.amount,
                    minTime = entity.timestamp - 60000L,
                    maxTime = entity.timestamp + 60000L
                )
                if (duplicate != null) {
                    return@launch
                }

                val id = db.transactionDao().insertTransaction(entity)

                // Train ML model locally
                com.example.engine.ml.LocalCategorizationModel.getInstance().trainSample(fullBody, entity.category, entity.merchant)

                // Award Zen Points for auto-logging
                val currentProfile = db.zenProfileDao().getProfileSync()
                val newPoints = (currentProfile?.totalPoints ?: 120) + 10
                db.zenProfileDao().insertOrUpdateProfile(
                    currentProfile?.copy(totalPoints = newPoints)
                        ?: com.example.data.model.ZenProfileEntity(totalPoints = newPoints)
                )

                // Update Home Widget
                com.example.widget.MeeCrebitBudgetWidgetProvider.triggerUpdate(context)

                // Post Transaction Logged Alert
                com.example.notification.NotificationHelper.notifyTransactionLogged(context, entity.copy(id = id), id.toInt())

                // Check and Alert if Monthly Budget Limit is Exceeded
                com.example.notification.NotificationHelper.checkAndNotifyBudgetExceeded(context, entity.copy(id = id), db)
            } catch (e: Exception) {
                Log.e("MeeCrebitReceiver", "Failed to process SMS transaction: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
