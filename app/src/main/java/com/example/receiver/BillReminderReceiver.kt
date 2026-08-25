package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.MeeCrebitDatabase
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BillReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val billId = intent.getLongExtra(EXTRA_BILL_ID, -1L)
        if (billId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = MeeCrebitDatabase.getInstance(context)
                val bill = db.billReminderDao().getReminderById(billId)
                if (bill != null && !bill.isPaid) {
                    NotificationHelper.notifyBillDue(context, bill)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_BILL_ID = "com.example.receiver.EXTRA_BILL_ID"
    }
}
