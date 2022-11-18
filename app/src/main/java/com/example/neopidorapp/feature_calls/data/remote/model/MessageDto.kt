package com.example.neopidorapp.feature_calls.data.remote.model

import com.example.neopidorapp.feature_calls.domain.model.MessageModel

data class MessageDto(
    val type: String,
    val name: String? = null,
    val target: String? = null,
    val data: Any? = null
) {

    fun toMessageModel(): MessageModel = MessageModel(type, name, target, data)
}
