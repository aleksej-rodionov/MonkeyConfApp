package com.example.neopidorapp.feature_call.presentation.call

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.neopidorapp.feature_call.presentation.call.socket.SocketRepo
import com.example.neopidorapp.models.MessageModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    val socketRepo: SocketRepo,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    var username = savedStateHandle.get<String>("username")

    var targetName: String? = null
    fun updateTargetName(name: String) { targetName = name }

    val incomingMessage = socketRepo.incomingMessage

    //====================SCREEN STATE====================
    private val _callScreenState = MutableStateFlow(CallScreenState())
    val callScreenState: StateFlow<CallScreenState> = _callScreenState.asStateFlow()
    fun updateIsIncomingCall(received: Boolean) {
        _callScreenState.value = callScreenState.value.copy(isIncomingCall = received)
    }
    fun updateIsOngoingCall(callRunning: Boolean) {
        _callScreenState.value = callScreenState.value.copy(isOngoingCall = callRunning)
    }
    fun updateIsMute(mute: Boolean) {
        _callScreenState.value = callScreenState.value.copy(isMute = mute)
    }
    fun updateIsCameraPaused(mute: Boolean) {
        _callScreenState.value = callScreenState.value.copy(isCameraPaused = mute)
    }
    fun updateIsSpeakerMode(mute: Boolean) {
        _callScreenState.value = callScreenState.value.copy(isSpeakerMode = mute)
    }
    //====================SCREEN STATE END====================



    //===========================METHODS==============================
    fun initSocket() {
        username?.let { u ->
            socketRepo.initSocket(u)
        }
    }

    //===================LISTENER METHODS======================
    fun onCallButtonClick() {
        socketRepo.sendMessageToSocket(
            MessageModel(
                "start_call",
                username,
                targetName,
                null
            )
        )
    }

    fun onSwitchCameraButtonClick() {
//        rtcClient?.switchCamera()
    }

    fun onMicButtonClick() {
        if (callScreenState.value.isMute) {
            updateIsMute(false)
        } else {
            updateIsMute(true)
        }
//        rtcClient?.toggleAudio(/*isMute*/ callScreenState.value.isMute)
    }

    fun onVideoButtonClick() {
        if (callScreenState.value.isCameraPaused) {
            updateIsCameraPaused(false)
        } else {
            updateIsCameraPaused(true)
        }
//        rtcClient?.toggleCamera(/*isCameraPaused*/ callScreenState.value.isCameraPaused)
    }

    fun onAudioOutputButtonClick() {
        if (callScreenState.value.isSpeakerMode) {
            updateIsSpeakerMode(false)
//            rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE)
        } else {
            updateIsSpeakerMode(true)
//            rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
        }
    }

    fun onEndCallButtonClick() {
        updateIsOngoingCall(false)
        updateIsIncomingCall(false) // todo why
//        rtcClient?.endCall()
    }
}

data class CallScreenState(
    val isIncomingCall: Boolean = false,
    val isOngoingCall: Boolean = false,
    val isMute: Boolean = false,
    val isCameraPaused: Boolean = false,
    val isSpeakerMode: Boolean = false
)