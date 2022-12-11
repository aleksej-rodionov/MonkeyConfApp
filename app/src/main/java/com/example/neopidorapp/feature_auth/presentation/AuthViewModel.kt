package com.example.neopidorapp.feature_auth.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neopidorapp.feature_call.data.SocketRepo
import com.example.neopidorapp.feature_call.domain.model.MessageModel
import com.example.neopidorapp.util.Constants.TAG_AUTH
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
    private val socketRepo: SocketRepo
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
    var usernameSignup = ""
    var emailSignup = ""
    var passwordSignup = ""

    fun onSignupClick() {
        if (usernameSignup.isBlank()) {
            emitAuthEvent(AuthEvent.SnackbarMessage("Enter username"))
            return
        }

        if (emailSignup.isBlank()) {
            emitAuthEvent(AuthEvent.SnackbarMessage("Enter email"))
            return
        }

        if (passwordSignup.isBlank()) {
            emitAuthEvent(AuthEvent.SnackbarMessage("Enter password"))
            return
        }

        if (passwordSignup.length < 6) {
            emitAuthEvent(AuthEvent.SnackbarMessage("Password has to be not shorter tha 6 characters"))
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(emailSignup, passwordSignup)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    // todo get FCMToken from firebase
                    // todo make user(name, uid, fcmToken) and store_user at Server.js

                    _authState.value = authState.value.copy(isLoggedIn = true)
                } else {
                    emitAuthEvent(AuthEvent.SnackbarMessage(it.exception?.message ?: "Unknown exception"))
                    Log.d(TAG_AUTH, it.exception?.message ?: "Unknown exception")
                }
            }

//        sendMessageToSocket(
//            MessageModel(
//                "store_user",
//                username,
//                null,
//                null
//            )
//        )
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
                    emitAuthEvent(AuthEvent.SnackbarMessage(it.exception?.message ?: "Unknown exception"))
                    Log.d(TAG_AUTH, it.exception?.message ?: "Unknown exception")
                }
            }
    }

    fun authWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                _authState.value = authState.value.copy(isLoggedIn = true)
            } else {
                emitAuthEvent(AuthEvent.SnackbarMessage(it.exception?.message ?: "Unknown exception"))
                Log.d(TAG_AUTH, it.exception?.message ?: "Unknown exception")
            }
        }
    }

    fun onForgotPasswordClick() {
        emitAuthEvent(AuthEvent.NavigateToNewPasswordScreen)
    }
    //====================LOGIN SCREEN END====================



    //====================NEW PASSWORD SCREEN====================
    var emailNewPassword = ""

    fun onResetPasswordClick() {
        if (emailNewPassword.isBlank()) {
            emitAuthEvent(AuthEvent.SnackbarMessage("Enter email"))
            return
        }

        firebaseAuth.sendPasswordResetEmail(emailNewPassword)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    emitAuthEvent(AuthEvent.SnackbarMessage("Password reset link has been sent to your email"))
                } else {
                    emitAuthEvent(AuthEvent.SnackbarMessage(it.exception?.message ?: "Unknown exception"))
                    Log.d(TAG_AUTH, it.exception?.message ?: "Unknown exception")
                }
            }
    }
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