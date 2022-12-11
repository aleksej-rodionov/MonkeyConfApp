package com.example.neopidorapp.feature_auth

import android.util.Log
import com.example.neopidorapp.feature_auth.domain.repository.AuthRepo
import com.example.neopidorapp.util.Constants.TAG_FCM
import com.google.firebase.messaging.FirebaseMessagingService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CloudMessagingService: FirebaseMessagingService() {

    @Inject
    lateinit var authRepo: AuthRepo

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG_FCM, "onNewToken: $token")

        authRepo.sendFcmToken(token) // todo remove?
    }
}