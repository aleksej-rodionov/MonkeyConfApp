package com.example.neopidorapp.feature_call.presentation.rtc_service.notification

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.neopidorapp.MainActivity
import com.example.neopidorapp.NeoPidorApp
import com.example.neopidorapp.R
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
        // todo
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

        val notificationBuilder = NotificationCompat.Builder(rtcService, NeoPidorApp.RTC_NOTIFICATION_CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_accept)

        if (state.isOngoingCall) {
            notificationBuilder.addAction(R.drawable.ic_reject, "End call", endCallPendingIntent)
        }

        notificationBuilder.setContentIntent(activityPendingIntent)

        val notification = notificationBuilder.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView())
//            .setContentTitle(state.playingItem?.title ?: "Unknown mediaItem")
//            .setContentText(if (state.playingItem?.isBuffering == true) state.playingItem.progress.let { convertTimer(it) } + " ...loading..." // for audioComments
//            else state.playingItem?.progress?.let { convertTimer(it) } ?: "Unknown progress") // for audioComments
            .setContentTitle("Ongoing call with ... somebody.")
            .setContentText(".. is in process") // for audioComments
            .build()

        rtcService.startForeground(1, notification)
    }
}