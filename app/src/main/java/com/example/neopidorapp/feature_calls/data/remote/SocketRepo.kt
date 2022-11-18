package com.example.neopidorapp.feature_calls.data.remote

import com.example.neopidorapp.feature_calls.domain.model.MessageModel
import kotlinx.coroutines.flow.Flow


interface SocketRepo {
    fun initSocket(username: String)
    fun incomingMessageFlow(messageJson: String?): Flow<MessageModel>
    fun sendMessageToSocket(message: MessageModel)
}