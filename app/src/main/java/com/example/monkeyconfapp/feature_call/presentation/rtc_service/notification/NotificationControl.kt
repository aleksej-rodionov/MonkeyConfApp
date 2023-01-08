package com.example.monkeyconfapp.feature_call.presentation.rtc_service.notification

import com.example.monkeyconfapp.feature_call.presentation.rtc_service.rtc_ui_state.RTCUiState

interface NotificationControl {
    fun launchNotificationJob()
    fun stopNotificationJob()
    fun showRtcNotification(state: RTCUiState)
}