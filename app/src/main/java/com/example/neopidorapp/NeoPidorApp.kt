package com.example.neopidorapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.neopidorapp.feature_auth.domain.repository.AuthRepo
import com.example.neopidorapp.feature_call.data.SocketRepo
import com.example.neopidorapp.util.Constants.TAG_FCM
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

private const val TAG = "NeoPidorApp"

@HiltAndroidApp
class NeoPidorApp: Application() {

    @Inject
    lateinit var notificationManager: NotificationManagerCompat

    @Inject
    lateinit var authRepo: AuthRepo

    @Inject
    lateinit var socketRepo: SocketRepo

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: App.hashcode = ${this.hashCode()}")

        createRtcNotificationChannel()

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    return@addOnCompleteListener
                }

                val token = it.result
                token?.let { t ->
                    val oldTokenInPrefStorage = authRepo.fetchFcmToken()
                    Log.d(TAG_FCM, "App.onCreate: \nmy token from FM.instance = $t")
                    oldTokenInPrefStorage?.let { ot ->
                        Log.d(TAG_FCM, "App.onCreate: \nold token from PrefStorage = $ot")
                    }
                    if (oldTokenInPrefStorage != t) {
                        authRepo.saveFcmToken(t)
                        authRepo.sendFcmToken(t) // todo handle onSuccess and onError
                    }
                }
            }

        socketRepo.initSocket()
    }



//====================PRIVATE METHODS====================
    private fun createRtcNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val rtcNotificationChannel = NotificationChannel(
                RTC_NOTIFICATION_CHANNEL_ID,
                "RTC Notification Channel",
                NotificationManager.IMPORTANCE_LOW
            )
//            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(rtcNotificationChannel)
        }
    }

    private fun createPushNotificationChannel() {
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pushNotificationChannel = NotificationChannel(
                PUSH_NOTIFICATION_CHANNEL_ID,
                "Push Notification Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                enableLights(true)
                setSound(sound, null)
                lightColor = Color.GREEN
            }

            notificationManager.createNotificationChannel(pushNotificationChannel)
        }
    }

    companion object {

        const val RTC_NOTIFICATION_CHANNEL_ID = "rtcNotificationChannel"
        const val PUSH_NOTIFICATION_CHANNEL_ID = "pushNotificationChannel"
    }
}