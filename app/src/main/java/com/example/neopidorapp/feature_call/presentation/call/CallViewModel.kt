package com.example.neopidorapp.feature_call.presentation.call

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neopidorapp.R
import com.example.neopidorapp.feature_call.presentation.call.rtc.RTCAudioManager
import com.example.neopidorapp.feature_call.presentation.call.socket.SocketRepo
import com.example.neopidorapp.models.MessageModel
import dagger.Provides
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
class CallViewModel @Inject constructor(
    val socketRepo: SocketRepo,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private var username = savedStateHandle.get<String>("username")
    private var targetName: String? = null
    fun updateTargetName(name: String) { targetName = name }
    private val incomingMessage = socketRepo.incomingMessage

    //=====================SCREEN STATE=========================
    private val _callScreenState = MutableStateFlow(CallScreenState())
    val callScreenState: StateFlow<CallScreenState> = _callScreenState.asStateFlow()
    private fun updateIncomingCallReceived(received: Boolean) {
        _callScreenState.value = callScreenState.value.copy(incomingCallReceived = received)
    }
    private fun updateIsCallRunning(callRunning: Boolean) {
        _callScreenState.value = callScreenState.value.copy(isCallRunning = callRunning)
    }
    private fun updateIsMute(mute: Boolean) {
        _callScreenState.value = callScreenState.value.copy(isMute = mute)
    }
    private fun updateIsCameraPaused(mute: Boolean) {
        _callScreenState.value = callScreenState.value.copy(isCameraPaused = mute)
    }
    private fun updateIsSpeakerMode(mute: Boolean) {
        _callScreenState.value = callScreenState.value.copy(isSpeakerMode = mute)
    }



    //===========================METHODS==============================
    fun initSocket() {
        username?.let { u ->
            socketRepo.initSocket(u)
        }
    }

    //===================LISTENER METHODS======================
    fun onCallButtonClick() {
        socketRepo?.sendMessageToSocket(
            MessageModel(
                "start_call",
                username,
                targetName,
                null
            )
        )
    }

    fun onSwitchCameraButtonClick() {
        rtcClient?.switchCamera()
    }

    fun onMicButtonClick() {
        if (callScreenState.value.isMute){
            updateIsMute(false)
        }else{
            updateIsMute(true)
        }
        rtcClient?.toggleAudio(/*isMute*/ callScreenState.value.isMute)
    }

    fun onVideoButtonClick() {
        if (callScreenState.value.isCameraPaused) {
            updateIsCameraPaused(false)
        } else {
            updateIsCameraPaused(true)
        }
        rtcClient?.toggleCamera(/*isCameraPaused*/ callScreenState.value.isCameraPaused)
    }

    fun onAudioOutputButtonClick() {
        if (callScreenState.value.isSpeakerMode) {
            updateIsSpeakerMode(false)
            rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE)
        } else {
            updateIsSpeakerMode(true)
            rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
        }
    }

    fun onEndCallButtonClick() {
        updateIsCallRunning(false)
//        setCallLayoutGone()
//        setWhoToCallLayoutVisible()
//        setIncomingCallLayoutGone()
        rtcClient?.endCall()
    }
}

data class CallScreenState(
    val incomingCallReceived: Boolean = false,
    val isCallRunning: Boolean = false,
    val isMute: Boolean = false,
    val isCameraPaused: Boolean = false,
    val isSpeakerMode: Boolean = false
)