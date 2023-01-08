package com.example.monkeyconfapp.feature_call.presentation.call

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.monkeyconfapp.feature_call.presentation.rtc_service.CallService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // todo MOVE THIS TO SERVICE!!!!!
    var username = savedStateHandle.get<String>("username")

    // todo MOVE THIS TO SERVICE!!!!!
    var targetName: String = ""
    fun updateTargetName(name: String) {
        targetName = name
    }

    //====================SCREEN STATE====================
    private val _callScreenState = MutableStateFlow(CallScreenState()) // todo must be mutable by Service state!
    val callScreenState: StateFlow<CallScreenState> = _callScreenState.asStateFlow()
    fun updateStateToDisplay(state: CallScreenState) {
        _callScreenState.value = state
    }

    //====================RTC SERVICE CONNECTION====================
    private val _callServiceBinderState = MutableStateFlow<CallService.CallServiceBinder?>(null)
    val callServiceBinderState: StateFlow<CallService.CallServiceBinder?> = _callServiceBinderState.asStateFlow()

    private val callrtcServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(service: ComponentName?, binder: IBinder?) {
            binder?.let {
                val callServiceBinder = it as CallService.CallServiceBinder
                _callServiceBinderState.value = callServiceBinder
            }
        }

        override fun onServiceDisconnected(service: ComponentName?) {
            _callServiceBinderState.value = null
        }
    }

    fun getCallServiceConnection() = callrtcServiceConnection
    //====================RTC SERVICE CONNECTION END====================
}

data class CallScreenState(
    val isIncomingCall: Boolean = false,
    val isOngoingCall: Boolean = false,
    val isMute: Boolean = false,
    val isCameraPaused: Boolean = false,
    val isSpeakerMode: Boolean = false,
    val remoteViewLoadingVisible: Boolean = true,
    val incomingCallSenderName: String? = null
)