package com.example.neopidorapp.feature_splashscreen.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomSplashViewModel @Inject constructor(

): ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _isLogged = MutableSharedFlow<Boolean>()
    val isLogged: SharedFlow<Boolean> = _isLogged.asSharedFlow()

    init {
        viewModelScope.launch {
            delay(1000L)
            val isLoggedIn = firebaseAuth.currentUser != null
            _isLogged.emit(isLoggedIn)
        }
    }
}