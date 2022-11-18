package com.example.neopidorapp.feature_calls.data.remote

import com.example.neopidorapp.feature_calls.domain.model.MessageModel
import com.example.neopidorapp.shared.Resource
import kotlinx.coroutines.flow.Flow


interface SocketRepo {
    suspend fun initSocket(username: String): Resource<Unit>
    fun incomingMessageFlow(): Flow<MessageModel>
    fun sendMessageToSocket(message: MessageModel)
}