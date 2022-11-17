package com.example.neopidorapp.util

import com.example.neopidorapp.models.MessageModel

// todo structure refactor:
// fun onNewMessage calls changes on the UI, => this interface stays in the presentation layer also.
interface NewMessageInterface {
    fun onNewMessage(message: MessageModel)
}