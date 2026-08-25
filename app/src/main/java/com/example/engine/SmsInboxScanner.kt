package com.example.engine

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import com.example.data.db.MeeCrebitDatabase
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SmsInboxScanner {

    suspend fun scanInbox(context: Context, limit: Int = 150): List<TransactionEntity> = withContext(Dispatchers.IO) {
        val transactions = mutableListOf<TransactionEntity>()
        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        try {
            val db = MeeCrebitDatabase.getInstance(context)
            val rules = db.merchantRuleDao().getActiveRulesSync()

            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC LIMIT $limit"
            )

            cursor?.use {
                val addressIdx = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIdx = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

                while (it.moveToNext()) {
                    val address = it.getString(addressIdx) ?: ""
                    val body = it.getString(bodyIdx) ?: ""
                    val date = it.getLong(dateIdx)

                    if (body.isBlank()) continue

                    val parsed = SmsParserEngine.parse(body, address, rules)
                    if (parsed.isValidTransaction) {
                        val txnDate = SmsParserEngine.extractTransactionDate(body, date)
                        val entity = SmsParserEngine.toEntity(parsed, customTimestamp = txnDate)
                        transactions.add(entity)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SmsInboxScanner", "Failed to query SMS inbox: ${e.message}")
        }

        return@withContext transactions
    }
}
