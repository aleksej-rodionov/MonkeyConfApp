package com.example.neopidorapp.feature_calls.presentation.call

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neopidorapp.feature_calls.data.remote.SocketRepo
import com.example.neopidorapp.feature_calls.domain.model.MessageModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val socketRepo: SocketRepo,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _event: MutableSharedFlow<CallScreenEvent> = MutableSharedFlow()
    val event: SharedFlow<CallScreenEvent> = _event.asSharedFlow()
    private fun emitEvent(event: CallScreenEvent) = viewModelScope.launch {
        _event.emit(event)
    }

    fun initSocket() {
        val username = savedStateHandle.get<String>("username")
        username?.let {

        }
    }
}

data class CallScreenState(

)

sealed class CallScreenEvent() {
    data class SocketMessageReceived(val msg: MessageModel): CallScreenEvent()
}