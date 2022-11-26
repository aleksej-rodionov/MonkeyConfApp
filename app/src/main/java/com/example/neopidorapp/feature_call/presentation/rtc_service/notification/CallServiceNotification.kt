package com.example.neopidorapp.feature_call.presentation.rtc_service.notification

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.neopidorapp.MainActivity
import com.example.neopidorapp.NeoPidorApp
import com.example.neopidorapp.R
import com.example.neopidorapp.feature_call.presentation.rtc_service.CallService
import com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_ui_state.RTCUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CallServiceNotification(
    private val scope: CoroutineScope,
    private val callService: CallService
): NotificationControl {

    private var rtcNotificationJob: Job? = null

    override fun launchNotificationJob() {
        rtcNotificationJob = scope.launch {
            callService.rtcState.state.collectLatest { state ->
                if (state.isOngoingCall) {
                    showRtcNotification(state)
                } else {
                    stopNotificationJob()
                    callService.stopForeground(true)
                }
            }
        }
    }

    override fun stopNotificationJob() {
        rtcNotificationJob?.cancel()
    }

    override fun showRtcNotification(state: RTCUiState) {

        val activityIntent = Intent(callService, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            callService, 1, activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val endCallIntent = Intent(callService, RTCNotificationReceiver::class.java)
        endCallIntent.action = RTCNotificationReceiver.ACTION_END_CALL
        val endCallPendingIntent = PendingIntent.getBroadcast(
            callService, 1, endCallIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(callService, NeoPidorApp.RTC_NOTIFICATION_CHANNEL_ID)
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

        callService.startForeground(1, notification)
    }
}