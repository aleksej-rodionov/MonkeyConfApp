package com.example.neopidorapp.feature_call.presentation.call

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.neopidorapp.feature_call.presentation.call.socket.SocketRepo
import dagger.Provides
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
class CallViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
): ViewModel() {
    // savedStateHandle.get<String>("username")?.let {

    }