package com.example.engine

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SmsInboxScanner {

    suspend fun scanInbox(context: Context, limit: Int = 100): List<TransactionEntity> = withContext(Dispatchers.IO) {
        val transactions = mutableListOf<TransactionEntity>()
        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        try {
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

                    val parsed = SmsParserEngine.parse(body, address)
                    if (parsed.isValidTransaction) {
                        val entity = SmsParserEngine.toEntity(parsed, customTimestamp = date)
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
