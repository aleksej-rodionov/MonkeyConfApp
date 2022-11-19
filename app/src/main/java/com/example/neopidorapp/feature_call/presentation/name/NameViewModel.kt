package com.example.neopidorapp.feature_call.presentation.name

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NameViewModel @Inject constructor(

): ViewModel() {

    var username = ""
    fun updateName(newName: String) {
        username = newName
    }

    private val _nameEvent = MutableSharedFlow<NameEvent>()
    val nameEvent: SharedFlow<NameEvent> = _nameEvent.asSharedFlow()
    fun onEnterClick() = viewModelScope.launch {
        _nameEvent.emit(NameEvent.EnterClick(username))
    }
}

sealed class NameEvent {
    data class EnterClick(val username: String): NameEvent()
}