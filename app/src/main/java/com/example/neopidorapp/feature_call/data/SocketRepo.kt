package com.example.neopidorapp.feature_call.data

import android.util.Log
import com.example.neopidorapp.feature_call.domain.model.MessageModel
import com.example.neopidorapp.util.Constants.TAG_DEBUG
import com.example.neopidorapp.util.Constants.TAG_SOCKET
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import kotlin.Exception

class SocketRepo(
    private val scope: CoroutineScope
) {
    private val _incomingMessage: MutableSharedFlow<MessageModel> = MutableSharedFlow()
    val incomingMessage: SharedFlow<MessageModel> = _incomingMessage.asSharedFlow()
    private fun emitNewMessage(message: MessageModel) = scope.launch {
        Log.d(TAG_DEBUG, "emitNewMessage: $message")
        _incomingMessage.emit(message)
    }

    private var webSocket: WebSocketClient? = null
    private val gson = Gson()

    fun initSocket() {

        // for emulators: 10.0.2.2:3000
        // for real devices: IP of your internet connection + :3000
        // but if your ws is deployed - add it's IP + :3000
        // (192.168.43.215 was of my phone)
        // (192.168.42.201 was of my phone)
        // (10.124.1.226 was of in coworking)
        // (192.168.1.136 was of in borsok)
        // (thawing-everglades-71111.herokuapp.com/ is heroku)
        // 192.168.16.100 at the flat
//        webSocket = object : WebSocketClient(URI("ws://thawing-everglades-71111.herokuapp.com/:3000")) {
        webSocket = object : WebSocketClient(URI("ws://192.168.16.103:3000")) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(TAG_SOCKET, "onOpen: ")
            }

            override fun onMessage(message: String?) {
                try {
                    Log.d(TAG_SOCKET, "onMessage: $message")
                    // todo emit some SharedFlow new value instead of triggering the interface method
//                    newMessageInterface.onNewMessage(gson.fromJson(message, MessageModel::class.java))
                    val messageModel = gson.fromJson(message, MessageModel::class.java)
                    emitNewMessage(messageModel)
                } catch (e: Exception) {
                    Log.d(TAG_SOCKET, "onMessage: ${e.localizedMessage}")
                    e.printStackTrace()
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(TAG_SOCKET, "onClose: $reason")
            }

            override fun onError(ex: Exception?) {
                Log.d(TAG_SOCKET, "onError: $ex")
            }
        }

        webSocket?.connect() // here we connect our client to our webSocket
    }

    fun sendMessageToSocket(message: MessageModel)/* = scope.launch(Dispatchers.IO)*/ {
        try {
            webSocket?.send(Gson().toJson(message))
        } catch (e: Exception) {
            Log.d(TAG_DEBUG, "sendMessageToSocket: e = ${e.localizedMessage}")
            e.printStackTrace()
        }
    }
}