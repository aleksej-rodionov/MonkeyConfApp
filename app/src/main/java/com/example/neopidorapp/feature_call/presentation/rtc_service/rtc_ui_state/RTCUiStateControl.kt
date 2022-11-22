package com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_ui_state

import kotlinx.coroutines.flow.StateFlow

interface RTCUiStateControl {

    val state: StateFlow<RTCUiState>

    fun currentState(): RTCUiState

//    fun updateIsIncomingCall(incomingCall: Boolean)
//    fun updateIsOngoingCall(ongoingCall: Boolean)
    fun updateIsMute(mute: Boolean)
    fun updateIsCameraPaused(cameraPaused: Boolean)
    fun updateIsSpeakerMode(speakerMode: Boolean)
}

data class RTCUiState(
//    val isIncomingCall: Boolean = false,
//    val isOngoingCall: Boolean = false,
    val isMute: Boolean = false,
    val isCameraPaused: Boolean = false,
    val isSpeakerMode: Boolean = false
)