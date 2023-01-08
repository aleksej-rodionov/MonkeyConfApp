package com.example.monkeyconfapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

private const val TAG = "MonkeyConfApp"

@HiltAndroidApp
class MonkeyConfApp: Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: App.hashcode = ${this.hashCode()}")

        createRtcNotificationChannel()
    }



//====================PRIVATE METHODS====================
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