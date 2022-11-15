package com.example.neopidorapp.util

import com.example.neopidorapp.models.MessageModel

interface NewMessageInterface {
    fun onNewMessage(message: MessageModel)
}