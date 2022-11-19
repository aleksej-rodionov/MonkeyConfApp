package com.example.neopidorapp.util

import com.example.neopidorapp.models.MessageModel

// todo should be removed and replaced with Hot SharedFlow
interface NewMessageInterface {
    fun onNewMessage(message: MessageModel)
}