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
    private val incomingMessage = socketRepo.incomingMessage

    //=====================SCREEN STATE=========================
    private val _callScreenState = MutableStateFlow(CallScreenState())
    val callScreenState: StateFlow<CallScreenState> = _callScreenState.asStateFlow()
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
                targetUserNameEt.text.toString(),
                null
            )
        )
        targetName = targetUserNameEt.text.toString()
    }

    fun onSwitchCameraButtonClick() {
        rtcClient?.switchCamera()
    }

    fun onMicButtonClick() {
        if (callScreenState.value.isMute){
            updateIsMute(false)
            micButton.setImageResource(R.drawable.ic_baseline_mic_off_24)
        }else{
            updateIsMute(true)
            micButton.setImageResource(R.drawable.ic_baseline_mic_24)
        }
        rtcClient?.toggleAudio(isMute)
    }

    fun onVideoButtonClick() {
        if (callScreenState.value.isCameraPaused) {
            updateIsCameraPaused(false)
            videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24)
        } else {
            updateIsCameraPaused(true)
            videoButton.setImageResource(R.drawable.ic_baseline_videocam_24)
        }
        rtcClient?.toggleCamera(isCameraPaused)
    }

    fun onAudioOutputButtonClick() {
        if (callScreenState.value.isSpeakerMode) {
            updateIsSpeakerMode(false)
            audioOutputButton.setImageResource(R.drawable.ic_baseline_hearing_24)
            rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE)
        } else {
            updateIsSpeakerMode(true)
            audioOutputButton.setImageResource(R.drawable.ic_baseline_speaker_up_24)
            rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
        }
    }

    fun onEndCallButtonClick() {
        setCallLayoutGone()
        setWhoToCallLayoutVisible()
        setIncomingCallLayoutGone()
        rtcClient?.endCall()
    }
}

data class CallScreenState(
    val isMute: Boolean = false,
    val isCameraPaused: Boolean = false,
    val isSpeakerMode: Boolean = false
)