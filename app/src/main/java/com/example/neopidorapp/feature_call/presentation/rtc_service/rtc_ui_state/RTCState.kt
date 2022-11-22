package com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_ui_state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RTCState: RTCUiStateControl {

    private val _state = MutableStateFlow(RTCUiState())
    override val state: StateFlow<RTCUiState> = _state.asStateFlow()

    override fun currentState() = state.value

//    override fun updateIsIncomingCall(incomingCall: Boolean) {
//        _state.value = state.value.copy(isIncomingCall = incomingCall)
//    }
//
//    override fun updateIsOngoingCall(ongoingCall: Boolean) {
//        _state.value = state.value.copy(isOngoingCall = ongoingCall)
//    }

    override fun updateIsMute(mute: Boolean) {
        _state.value = state.value.copy(isMute = mute)
    }

    override fun updateIsCameraPaused(cameraPaused: Boolean) {
        _state.value = state.value.copy(isCameraPaused = cameraPaused)
    }

    override fun updateIsSpeakerMode(speakerMode: Boolean) {
        _state.value = state.value.copy(isSpeakerMode = speakerMode)
    }
}