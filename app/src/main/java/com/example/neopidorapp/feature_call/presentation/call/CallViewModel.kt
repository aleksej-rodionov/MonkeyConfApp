package com.example.neopidorapp.feature_call.presentation.call

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neopidorapp.feature_call.presentation.rtc_service.CallService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val firebaseAuth = FirebaseAuth.getInstance()

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

    //====================SCREEN EVENT====================
    private val _callScreenEvent = MutableSharedFlow<CallScreenEvent>()
    val callScreenEvent: SharedFlow<CallScreenEvent> = _callScreenEvent.asSharedFlow()
    fun emitCallScreenEvent(event: CallScreenEvent) = viewModelScope.launch {
        _callScreenEvent.emit(event)
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



    //====================AUTH METHODS====================
    fun onLogoutClick() {
        firebaseAuth.signOut()
        emitCallScreenEvent(CallScreenEvent.ToAuth(true))
    }

    fun onDeleteMeClick() {
        val user = firebaseAuth.currentUser
        user?.let { u ->
            u.delete()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        onUserDeleted()
                    }
                }
        }
    }

    fun onUserDeleted() = viewModelScope.launch {
        emitCallScreenEvent(CallScreenEvent.SnackMessage("User deleted"))
        delay(1000L)
        emitCallScreenEvent(CallScreenEvent.ToAuth(false))
    }
    //====================AUTH METHODS END====================
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

sealed class CallScreenEvent() {
    data class SnackMessage(val msg: String): CallScreenEvent()
    data class ToAuth(val straightToLoginScreen: Boolean): CallScreenEvent()
}