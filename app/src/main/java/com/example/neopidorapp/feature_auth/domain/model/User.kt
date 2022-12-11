package com.example.neopidorapp.feature_auth.domain.model

data class User(
    val name: String,
    val uid: String,
    val fcmToken: String
)
