package com.example.neopidorapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NeoPidorApp: Application() {

    override fun onCreate() {
        super.onCreate()

        createRtcNotificationChannel()
    }

    private fun createRtcNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val rtcNotificationChannel = NotificationChannel(
                RTC_NOTIFICATION_CHANNEL_ID,
                "RTC Notification Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(rtcNotificationChannel)
        }
    }

    companion object {

        const val RTC_NOTIFICATION_CHANNEL_ID = "rtcNotificationChannel"
    }
}