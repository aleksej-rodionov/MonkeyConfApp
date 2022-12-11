package com.example.neopidorapp.feature_auth.domain.repository

interface AuthRepo {

    fun fetchFcmToken(): String?

    fun saveFcmToken(token: String)

    fun sendFcmToken(token: String)
}