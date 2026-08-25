package com.example.engine

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.util.Log
import com.example.data.db.MeeCrebitDatabase
import com.example.engine.ml.LocalCategorizationModel
import com.example.notification.NotificationHelper
import com.example.widget.MeeCrebitBudgetWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Real-time ContentObserver for SMS Inbox.
 * Automatically triggers whenever a new SMS arrives on the device,
 * parsing and saving financial transactions instantly to Room DB without manual rescan.
 */
class SmsInboxObserver(
    private val context: Context,
    handler: Handler = Handler(Looper.getMainLooper())
) : ContentObserver(handler) {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var lastProcessedTimestamp = 0L

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        Log.d("SmsInboxObserver", "SMS ContentObserver triggered: $uri")
        processLatestSms()
    }

    fun processLatestSms() {
        scope.launch {
            try {
                // Give OS SMS Provider 300ms to finish writing body and address
                delay(300)

                val projection = arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.TYPE
                )

                val cursor = context.contentResolver.query(
                    Telephony.Sms.CONTENT_URI,
                    projection,
                    "${Telephony.Sms.TYPE} = ?",
                    arrayOf(Telephony.Sms.MESSAGE_TYPE_INBOX.toString()),
                    "${Telephony.Sms.DATE} DESC LIMIT 5"
                )

                cursor?.use {
                    val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
                    val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                    val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)

                    if (addressIdx == -1 || bodyIdx == -1 || dateIdx == -1) return@use

                    val db = MeeCrebitDatabase.getInstance(context)
                    val activeRules = db.merchantRuleDao().getActiveRulesSync()
                    var newlyAddedCount = 0

                    while (it.moveToNext()) {
                        val address = it.getString(addressIdx) ?: ""
                        val body = it.getString(bodyIdx) ?: ""
                        val date = it.getLong(dateIdx)

                        if (body.isBlank()) continue

                        // Check if already in database
                        val existing = db.transactionDao().findByRawSms(body)
                        if (existing != null) {
                            continue
                        }

                        val parsed = SmsParserEngine.parse(body, address, activeRules)
                        if (parsed.isValidTransaction) {
                            val txnDate = SmsParserEngine.extractTransactionDate(body, date)
                            val entity = SmsParserEngine.toEntity(parsed, customTimestamp = txnDate)

                            // Check duplicate within 60s window
                            val duplicate = db.transactionDao().findDuplicate(
                                merchant = entity.merchant,
                                amount = entity.amount,
                                minTime = entity.timestamp - 60000L,
                                maxTime = entity.timestamp + 60000L
                            )

                            if (duplicate == null) {
                                val insertedId = db.transactionDao().insertTransaction(entity)
                                newlyAddedCount++

                                // Train ML model locally
                                LocalCategorizationModel.getInstance().trainSample(body, entity.category, entity.merchant)

                                // Award zen points
                                val currentProfile = db.zenProfileDao().getProfileSync()
                                val newPoints = (currentProfile?.totalPoints ?: 120) + 10
                                db.zenProfileDao().insertOrUpdateProfile(
                                    currentProfile?.copy(totalPoints = newPoints)
                                        ?: com.example.data.model.ZenProfileEntity(totalPoints = newPoints)
                                )

                                // Notify user and check budget limits
                                NotificationHelper.notifyTransactionLogged(context, entity.copy(id = insertedId), insertedId.toInt())
                                NotificationHelper.checkAndNotifyBudgetExceeded(context, entity.copy(id = insertedId), db)

                                Log.d("SmsInboxObserver", "Automatically captured new SMS transaction: ${entity.merchant} - ₹${entity.amount}")
                            }
                        }
                    }

                    if (newlyAddedCount > 0) {
                        MeeCrebitBudgetWidgetProvider.triggerUpdate(context)
                    }
                }
            } catch (e: Exception) {
                Log.e("SmsInboxObserver", "Error processing incoming SMS: ${e.message}", e)
            }
        }
    }

    companion object {
        private var instance: SmsInboxObserver? = null
        private var isRegistered = false

        fun startObserving(context: Context) {
            if (isRegistered) return
            try {
                if (instance == null) {
                    instance = SmsInboxObserver(context.applicationContext)
                }
                context.contentResolver.registerContentObserver(
                    Telephony.Sms.CONTENT_URI,
                    true,
                    instance!!
                )
                isRegistered = true
                Log.d("SmsInboxObserver", "SmsInboxObserver successfully registered on Telephony.Sms.CONTENT_URI")
            } catch (e: Exception) {
                Log.e("SmsInboxObserver", "Failed to register SmsInboxObserver: ${e.message}")
            }
        }

        fun stopObserving(context: Context) {
            if (!isRegistered || instance == null) return
            try {
                context.contentResolver.unregisterContentObserver(instance!!)
                isRegistered = false
                Log.d("SmsInboxObserver", "SmsInboxObserver stopped")
            } catch (e: Exception) {
                Log.e("SmsInboxObserver", "Failed to stop SmsInboxObserver: ${e.message}")
            }
        }
    }
}
