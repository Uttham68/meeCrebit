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

        val parsed = SmsParserEngine.parse(fullBody, sender)
        if (parsed.isValidTransaction) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = MeeCrebitDatabase.getInstance(context)
                    val existing = db.transactionDao().findByRawSms(fullBody)
                    if (existing != null) {
                        return@launch
                    }
                    val entity = SmsParserEngine.toEntity(parsed, timestamp)
                    val id = db.transactionDao().insertTransaction(entity)

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
                    com.example.notification.NotificationHelper.notifyTransactionLogged(context, entity, id.toInt())

                    // Check and Alert if Monthly Budget Limit is Exceeded
                    com.example.notification.NotificationHelper.checkAndNotifyBudgetExceeded(context, entity, db)
                } catch (e: Exception) {
                    Log.e("MeeCrebitReceiver", "Failed to process SMS transaction: ${e.message}")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
