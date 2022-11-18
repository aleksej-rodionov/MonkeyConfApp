package com.example.neopidorapp.feature_calls.data.remote

import android.util.Log
import com.example.neopidorapp.feature_calls.data.remote.model.MessageDto
import com.example.neopidorapp.feature_calls.domain.model.MessageModel
import com.example.neopidorapp.shared.Resource
import com.example.neopidorapp.util.NewMessageInterface
import com.google.gson.Gson
import kotlinx.coroutines.flow.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

private const val TAG = "SocketRepoImpl"

class SocketRepoImpl(
//    private val newMessageInterface: NewMessageInterface
): SocketRepo {

//    private val _incomingMessage: MutableSharedFlow<String> = MutableSharedFlow()
//    private val incomingMessage: SharedFlow<String> = _incomingMessage.asSharedFlow()
//    private suspend fun emitNewMessage(message: String) {
//        _incomingMessage.emit(message)
//    }

    private var webSocket: WebSocketClient? = null
    private var userName: String? = null
    private val gson = Gson()

    override suspend fun initSocket(username: String): Resource<Unit> {
        userName = username

        // for emulators: 10.0.2.2:3000
        // for real devices: IP of your internet connection + :3000
        // but if your ws is deployed - add it's IP + :3000
        // (192.168.43.215 was of my phone)
        // (192.168.42.201 was of my phone)
        // (10.124.1.226 was of in coworking)
        webSocket = object : WebSocketClient(URI("ws://10.124.1.226:3000")) {
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
                // todo here I want to emit new MessageModel through some new Flow<MessageModel>
                try {
//                    newMessageInterface.onNewMessage(message)
//                    incomingMessageFlow(message)

                    incomingMessageFlow().
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

        return try {
            webSocket?.connect() // here we connect our client to our webSocket

            Resource.Success(Unit) // todo uncomment this below
//            if (webSocket?.isOpen == true) {
//                Resource.Success(Unit) // cause nothing to return
//            } else {
//                Resource.Error("Couldn't establish a connection")
//            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    override fun incomingMessageFlow(): Flow<MessageModel> = emptyFlow()

/*    override fun incomingMessageFlow(messageJson: String?): Flow<MessageModel> = flow {
        if (messageJson.isNullOrBlank()) {
            // emit nothing
            // todo emit Resource.Error after implementing Resource util Sealed Class
        } else {
            try {
                val messageDto = gson.fromJson(messageJson, MessageDto::class.java)
                val message = messageDto.toMessageModel()
                emit(message)
            } catch (e: Exception) {
                e.printStackTrace()
                // emit nothing
                // todo emit Resource.Error after implementing Resource util Sealed Class
            }
        }
    }*/

    override fun sendMessageToSocket(message: MessageModel) {
        try {
            Log.d(TAG, "sendMessageToSocket: try = call")
            webSocket?.send(Gson().toJson(message))
        } catch (e: Exception) {
            Log.d(TAG, "sendMessageToSocket: e = ${e.localizedMessage}")
            e.printStackTrace()
        }
    }
}