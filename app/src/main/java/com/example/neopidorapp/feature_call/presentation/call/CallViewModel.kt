package com.example.neopidorapp.feature_call.presentation.call

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.neopidorapp.feature_call.presentation.call.socket.SocketRepo
import com.example.neopidorapp.feature_call.presentation.rtc_service.RTCService
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
) : ViewModel() {

    var username = savedStateHandle.get<String>("username")

    var targetName: String? = null
    fun updateTargetName(name: String) {
        targetName = name
    }

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
    // somewhere here must be methods updating view state,
    // triggered by shit emited from RTCService.RTCClient.state

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



    //====================RTC SERVICE CONNECTION====================
    private val _rtcBinderState = MutableStateFlow<RTCService.RTCBinder?>(null)
    val rtcBinderState: StateFlow<RTCService.RTCBinder?> = _rtcBinderState.asStateFlow()

    private val rtcServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(service: ComponentName?, binder: IBinder?) {
            binder?.let {
                val rtcBinder = it as RTCService.RTCBinder
                _rtcBinderState.value = rtcBinder
            }
        }

        override fun onServiceDisconnected(service: ComponentName?) {
            _rtcBinderState.value = null
        }
    }

    fun getRTCServiceConnection() = rtcServiceConnection
    //====================RTC SERVICE CONNECTION END====================
}

data class CallScreenState(
    val isIncomingCall: Boolean = false,
    val isOngoingCall: Boolean = false,
    val isMute: Boolean = false,
    val isCameraPaused: Boolean = false,
    val isSpeakerMode: Boolean = false
)