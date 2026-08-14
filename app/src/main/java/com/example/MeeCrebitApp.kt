package com.example

import android.app.Application
import com.example.data.db.MeeCrebitDatabase
import com.example.notification.NotificationHelper

class MeeCrebitApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Room DB
        MeeCrebitDatabase.getInstance(this)
        // Initialize Notification Channels for Transactions & Budget Alerts
        NotificationHelper.createNotificationChannels(this)
    }
}
