package com.example.neopidorapp.feature_call.presentation.rtc_service.notification

import android.app.PendingIntent
import android.content.Intent
import com.example.neopidorapp.MainActivity
import com.example.neopidorapp.feature_call.presentation.rtc_service.RTCService
import com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_ui_state.RTCUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

class RTCNotification(
    private val scope: CoroutineScope,
    private val rtcService: RTCService
): NotificationControl {

    private var rtcNotificationJob: Job? = null

    override fun launchNotificationJob() {
        TODO("Not yet implemented")
    }

    override fun stopNotificationJob() {
        rtcNotificationJob?.cancel()
    }

    override fun showRtcNotification(state: RTCUiState) {

        val activityIntent = Intent(rtcService, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            rtcService, 1, activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val endCallIntent = Intent(rtcService, RTCNotificationReceiver::class.java)
        endCallIntent.action = RTCNotificationReceiver.ACTION_END_CALL
        val endCallPendingIntent = PendingIntent.getBroadcast(
            rtcService, 1, endCallIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder =
    }
}