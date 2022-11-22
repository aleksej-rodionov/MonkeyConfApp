package com.example.neopidorapp.feature_call.presentation.rtc_service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.neopidorapp.feature_call.presentation.rtc_service.notification.NotificationCallback
import com.example.neopidorapp.feature_call.presentation.rtc_service.notification.RTCNotification
import com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_client.RTCClientWrapper
import com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_ui_state.RTCState
import com.example.neopidorapp.models.MessageModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream

@AndroidEntryPoint
class RTCService : Service(), NotificationCallback {

    private val rtcBinder = RTCBinder()

    // scope is needed for notification
    private val rtcServiceJob = SupervisorJob()
    private val rtcServiceScope = CoroutineScope(Dispatchers.Main + rtcServiceJob)


    //====================RTCCLIENT AND ITS NOTIFICATION====================
    val rtcState = RTCState()

    // todo RTCClient with new parameters: state, notifCallback, coroutineScope
    val rtcClientWrapper = RTCClientWrapper(
        rtcState,
        this,
        rtcServiceScope
    )

    val notification = RTCNotification(rtcServiceScope, this)
    //====================RTCCLIENT AND ITS NOTIFICATION END====================


    //====================OVERRIDDEN SERVICE METHODS====================
    override fun onBind(p0: Intent?): IBinder? {
        return rtcBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let {
            when (it) {
                // todo
                else -> Unit
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        // todo stop and nullize rtc connection?
        rtcServiceScope.cancel()
    }
    //====================OVERRIDDEN SERVICE METHODS END====================

    inner class RTCBinder : Binder() {
        val service get() = this@RTCService
    }

    //====================OVERRIDDEN NOTIFICATION_CALLBACK METHODS====================
    override fun launchNotification() {
        notification.launchNotificationJob()
    }
    //====================OVERRIDDEN NOTIFICATION_CALLBACK METHODS END====================



    //====================RTC_CLIENT CONTROL METHODS====================
    // initRtcClient() = rtcClientWrapper.initRtcClient() // todo
    //====================RTC_CLIENT CONTROL METHODS END====================
}