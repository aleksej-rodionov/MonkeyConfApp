package com.example.neopidorapp.feature_auth.data.repository

import android.content.SharedPreferences
import com.example.neopidorapp.feature_auth.domain.repository.AuthRepo
import com.example.neopidorapp.util.Constants.FCM_TOKEN_KEY

class AuthRepoImpl(
    private val sharedPref: SharedPreferences
): AuthRepo {

    override fun fetchFcmToken(): String? {
        return sharedPref.getString(FCM_TOKEN_KEY, null)
    }

    override fun saveFcmToken(token: String) {
        sharedPref.edit().putString(FCM_TOKEN_KEY, token).apply()
    }

    override fun sendFcmToken(token: String) {
        // todo | send token to server so that Firebase could send Push to this particular user
        // todo | when there is an incoming call to him
    }
}