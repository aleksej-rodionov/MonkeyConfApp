package com.example.neopidorapp.feature_auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(

): ViewModel() {

    val firebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _authEvent = MutableSharedFlow<AuthEvent>()
    val authEvent: SharedFlow<AuthEvent> = _authEvent.asSharedFlow()
    fun emitAuthEvent(event: AuthEvent) = viewModelScope.launch {
        _authEvent.emit(event)
    }



    //====================SIGNUP SCREEN====================
    var emailSignup = ""
    var passwordSignup = ""

    fun onSignupClick() {
        if (emailSignup.isBlank()) {
            emitAuthEvent(AuthEvent.SnackbarMessage("Enter email"))
            return
        }

        if (passwordSignup.isBlank()) {
            emitAuthEvent(AuthEvent.SnackbarMessage("Enter password"))
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(emailSignup, passwordSignup)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _authState.value = authState.value.copy(isLoggedIn = true)
                } else {
                    emitAuthEvent(AuthEvent.SnackbarMessage("Something went wrong O_o"))
                }
            }
    }

    fun alreadyRegisteredClick() {
        emitAuthEvent(AuthEvent.NavigateToLoginScreen)
    }
    //====================SIGNUP SCREEN END====================



    //====================LOGIN SCREEN====================
    var emailLogin = ""
    var passwordLogin = ""

    fun onLoginClick() {
        if (emailLogin.isBlank()) {
            emitAuthEvent(AuthEvent.SnackbarMessage("Enter email"))
            return
        }

        if (passwordLogin.isBlank()) {
            emitAuthEvent(AuthEvent.SnackbarMessage("Enter password"))
            return
        }

        firebaseAuth.signInWithEmailAndPassword(emailLogin, passwordLogin)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _authState.value = authState.value.copy(isLoggedIn = true)
                } else {
                    emitAuthEvent(AuthEvent.SnackbarMessage("Something went wrong O_o"))
                }
            }
    }

    fun authWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                _authState.value = authState.value.copy(isLoggedIn = true)
            } else {
                emitAuthEvent(AuthEvent.SnackbarMessage("Auth error"))
            }
        }
    }

    fun onForgotPasswordClick() {
        emitAuthEvent(AuthEvent.NavigateToNewPasswordScreen)
    }
    //====================LOGIN SCREEN END====================



    //====================NEW PASSWORD SCREEN====================

    //====================NEW PASSWORD SCREEN END====================


}

data class AuthState(
    val user: FirebaseUser? = null,
    val isLoggedIn: Boolean = false
)

sealed class AuthEvent() {
    data class SnackbarMessage(val msg: String): AuthEvent()
    object NavigateToLoginScreen: AuthEvent()
    object NavigateToNewPasswordScreen: AuthEvent()
}