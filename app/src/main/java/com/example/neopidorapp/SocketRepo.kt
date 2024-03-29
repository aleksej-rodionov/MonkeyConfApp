package com.example.neopidorapp

import android.util.Log
import com.example.neopidorapp.models.MessageModel
import com.example.neopidorapp.util.NewMessageInterface
import com.google.gson.Gson
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import kotlin.Exception

private const val TAG = "SocketRepo"

class SocketRepo(
    private val newMessageInterface: NewMessageInterface
) {
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
        webSocket = object : WebSocketClient(URI("ws://10.124.1.65:3000")) {
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
                    newMessageInterface.onNewMessage(gson.fromJson(message, MessageModel::class.java))
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