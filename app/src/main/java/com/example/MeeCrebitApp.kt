package com.example

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.data.db.MeeCrebitDatabase
import com.example.engine.SmsInboxObserver
import com.example.notification.NotificationHelper

class MeeCrebitApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Room DB
        MeeCrebitDatabase.getInstance(this)
        // Initialize Notification Channels for Transactions & Budget Alerts
        NotificationHelper.createNotificationChannels(this)

        // If READ_SMS permission is already granted, start observing inbox changes in real time
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            SmsInboxObserver.startObserving(this)
        }
    }
}
