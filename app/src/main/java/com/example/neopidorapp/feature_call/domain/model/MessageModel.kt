package com.example.neopidorapp.feature_call.domain.model

data class MessageModel(
    val type: String,
    val name: String? = null,
    val target: String? = null,
    val data: Any? = null
)
