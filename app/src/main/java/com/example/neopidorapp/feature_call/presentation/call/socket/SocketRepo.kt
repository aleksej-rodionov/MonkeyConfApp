package com.example.neopidorapp.feature_call.presentation.call.socket

import android.util.Log
import com.example.neopidorapp.models.MessageModel
import com.example.neopidorapp.util.NewMessageInterface
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

private const val TAG = "SocketRepo"

class SocketRepo(
    private val scope: CoroutineScope
//    private val newMessageInterface: NewMessageInterface
) {
    private val _incomingMessage: MutableSharedFlow<MessageModel> = MutableSharedFlow()
    val incomingMessage: SharedFlow<MessageModel> = _incomingMessage.asSharedFlow()
    private fun emitNewMessage(message: MessageModel) = scope.launch {
        _incomingMessage.emit(message)
    }

    private var webSocket: WebSocketClient? = null
    private var userName: String? = null
    private val gson = Gson()

    fun initSocket(username: String) {
        userName = username

        // for emulators: 10.0.2.2:3000
        // for real devices: IP of your internet connection + :3000
        // but if your ws is deployed - add it's IP + :3000
        // (192.168.43.215 was of my phone)
        // (192.168.42.201 was of my phone)
        // (10.124.1.226 was of in coworking)
        // (192.168.1.136 was of in borsok)
        webSocket = object : WebSocketClient(URI("ws://192.168.1.136:3000")) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                sendMessageToSocket(
                    MessageModel(
                        "store_user",
                        username,
                        null,
                        null
                    )
                )
            }

            override fun onMessage(message: String?) {
                try {
                    // todo emit some SharedFlow new value instead of triggering the interface method
//                    newMessageInterface.onNewMessage(gson.fromJson(message, MessageModel::class.java))
                    val messageModel = gson.fromJson(message, MessageModel::class.java)
                    emitNewMessage(messageModel)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(TAG, "onClose: $reason")
            }

            override fun onError(ex: Exception?) {
                Log.d(TAG, "onError: $ex")
            }
        }

        webSocket?.connect() // here we connect our client to our webSocket
    }

    fun sendMessageToSocket(message: MessageModel) {
        try {
            Log.d(TAG, "sendMessageToSocket: try = call")
            webSocket?.send(Gson().toJson(message))
        } catch (e: Exception) {
            Log.d(TAG, "sendMessageToSocket: e = ${e.localizedMessage}")
            e.printStackTrace()
        }
    }
}