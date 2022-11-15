package com.example.neopidorapp

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.example.neopidorapp.databinding.ActivityCallBinding
import com.example.neopidorapp.models.MessageModel
import com.example.neopidorapp.util.NewMessageInterface
import com.example.neopidorapp.util.PeerConnectionObserver
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection

class CallActivity: AppCompatActivity(), NewMessageInterface {

    private val binding by lazy { ActivityCallBinding.inflate(layoutInflater) }
    private var userName: String? = null
    private var socketRepo: SocketRepo? = null
    private var rtcClient: RTCClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        userName = intent.getStringExtra("username")
        socketRepo = SocketRepo(this@CallActivity)
        userName?.let { socketRepo?.initSocket(it) }
        rtcClient = RTCClient(
            application, // we can just write "application" cause we're inside of the Activity
            userName!!,
            socketRepo!!,
            object : PeerConnectionObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                }
            }
        )

        rtcClient?.initializeSurfaceView(binding.localView)
        rtcClient?.startLocalVideo(binding.localView)
    }

    override fun onNewMessage(message: MessageModel) {
        // todo
    }
}