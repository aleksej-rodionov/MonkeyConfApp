package com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_client

import com.example.neopidorapp.feature_call.presentation.rtc_service.notification.NotificationCallback
import com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_ui_state.RTCUiStateControl
import kotlinx.coroutines.CoroutineScope

class RTCClientWrapper(
    private val rtcUiStateControl: RTCUiStateControl,
    private val notificationCallback: NotificationCallback,
    private val scope: CoroutineScope
): CallClientWrapper {


}