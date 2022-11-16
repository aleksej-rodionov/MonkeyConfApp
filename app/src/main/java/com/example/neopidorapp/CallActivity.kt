package com.example.neopidorapp

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.neopidorapp.databinding.ActivityCallBinding
import com.example.neopidorapp.models.IceCandidateModel
import com.example.neopidorapp.models.MessageModel
import com.example.neopidorapp.util.NewMessageInterface
import com.example.neopidorapp.util.PeerConnectionObserver
import com.example.neopidorapp.util.RTCAudioManager
import com.google.gson.Gson
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription

private const val TAG = "CallActivity"

class CallActivity : AppCompatActivity(), NewMessageInterface {

    private val binding by lazy { ActivityCallBinding.inflate(layoutInflater) }
    private var userName: String? = null
    private var socketRepo: SocketRepo? = null
    private var rtcClient: RTCClient? = null
    private var target: String = ""
    private val gson = Gson()
    private var isMute = false
    private var isCameraPaused = false
    private val rtcAudioManager by lazy { RTCAudioManager.create(this) } // todo look over this class
    private var isSpeakerMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        userName = intent.getStringExtra("username")
        socketRepo = SocketRepo(this@CallActivity)
        userName?.let {
            socketRepo?.initSocket(it)
        }
        rtcClient = RTCClient(
            application, // we can just write "application" cause we're inside of the Activity
            userName!!,
            socketRepo!!,
            object : PeerConnectionObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    rtcClient?.addIceCandidate(p0) // we add an ICE Candidate...
                    // ... and it's time to send this ICE Candidate to our peer:
                    // SENDING ICE CANDIDATE:
                    val candidate = hashMapOf(
                        "sdpMid" to p0?.sdpMid,
                        "sdpMLineIndex" to p0?.sdpMLineIndex,
                        "sdpCandidate" to p0?.sdp
                    )
                    socketRepo?.sendMessageToSocket(
                        MessageModel("ice_candidate", userName, target, candidate)
                    )
                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    p0?.videoTracks?.get(0)?.addSink(binding.remoteView)
                }
            }
        )

        rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)

        binding.apply {
            callBtn.setOnClickListener {
                socketRepo?.sendMessageToSocket(
                    MessageModel(
                        "start_call",
                        userName,
                        targetUserNameEt.text.toString(),
                        null
                    )
                )
                target = targetUserNameEt.text.toString()
            }

            switchCameraButton.setOnClickListener {
                rtcClient?.switchCamera()
            }

            micButton.setOnClickListener {
                if (isMute){
                    isMute = false
                    micButton.setImageResource(R.drawable.ic_baseline_mic_off_24)
                }else{
                    isMute = true
                    micButton.setImageResource(R.drawable.ic_baseline_mic_24)
                }
                rtcClient?.toggleAudio(isMute)
            }

            videoButton.setOnClickListener {
                if (isCameraPaused) {
                    isCameraPaused = false
                    videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24)
                } else {
                    isCameraPaused = true
                    videoButton.setImageResource(R.drawable.ic_baseline_videocam_24)
                }
                rtcClient?.toggleCamera(isCameraPaused)
            }

            audioOutputButton.setOnClickListener {
                if (isSpeakerMode) {
                    isSpeakerMode = false
                    audioOutputButton.setImageResource(R.drawable.ic_baseline_hearing_24)
                    rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE)
                } else {
                    isSpeakerMode = true
                    audioOutputButton.setImageResource(R.drawable.ic_baseline_speaker_up_24)
                    rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
                }
            }

            endCallButton.setOnClickListener {
                setCallLayoutGone()
                setWhoToCallLayoutVisible()
                setIncomingCallLayoutGone()
                rtcClient?.endCall()
            }
        }
    }

    override fun onNewMessage(message: MessageModel) {
        Log.d(TAG, "onNewMessage: $message")
        when (message.type) {
            "call_response" -> {
                if (message.data == "user is not online") {
                    runOnUiThread { // we have to run it on the UI thread because we're on the Socket thread
                        Toast.makeText(this, "user is not reachable", Toast.LENGTH_LONG).show()
                    }
                } else {
                    runOnUiThread { // we have to run it on the UI thread because we're on the Socket thread
                        setWhoToCallLayoutGone()
                        setCallLayoutVisible()
                        binding.apply {
                            rtcClient?.initializeSurfaceView(localView)
                            rtcClient?.initializeSurfaceView(remoteView)
                            rtcClient?.startLocalVideo(localView)
                            rtcClient?.call(targetUserNameEt.text.toString())
                        }
                    }
                }
            }
            "offer_received" -> {
                runOnUiThread {
                    setIncomingCallLayoutVisible()
                    binding.apply {
                        incomingNameTV.text = "${message.name.toString()} is calling you"
                        acceptButton.setOnClickListener {
                            setIncomingCallLayoutGone()
                            setCallLayoutVisible()
                            setWhoToCallLayoutGone()

                            rtcClient?.initializeSurfaceView(localView)
                            rtcClient?.initializeSurfaceView(remoteView)
                            rtcClient?.startLocalVideo(localView)
                            val remoteSession = SessionDescription(
                                SessionDescription.Type.OFFER,
                                message.data.toString()
                            )
                            rtcClient?.onRemoteSessionReceived(remoteSession)
                            rtcClient?.answer(message.name!!)
                            target = message.name!!
                        }
                        rejectButton.setOnClickListener {
                            setIncomingCallLayoutGone()
                        }
                        remoteViewLoading.visibility = View.GONE
                    }
                }
            }
            "answer_received" -> {
                val session = SessionDescription(
                    SessionDescription.Type.ANSWER,
                    message.data.toString()
                )
                rtcClient?.onRemoteSessionReceived(session)
                runOnUiThread {
                    binding.remoteViewLoading.visibility = View.GONE
                }
            }
            "ice_candidate" -> {
                // RECEIVING ICE CANDIDATE:
                runOnUiThread {
                    try {
                        val receivedIceCandidate = gson.fromJson(
                            gson.toJson(message.data),
                            IceCandidateModel::class.java
                        )
                        rtcClient?.addIceCandidate(
                            IceCandidate(
                                receivedIceCandidate.sdpMid,
                                Math.toIntExact(receivedIceCandidate.sdpMLineIndex.toLong()),
                                receivedIceCandidate.sdpCandidate
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun setIncomingCallLayoutGone() {
        binding.incomingCallLayout.visibility = View.GONE
    }

    private fun setIncomingCallLayoutVisible() {
        binding.incomingCallLayout.visibility = View.VISIBLE
    }

    private fun setCallLayoutGone() {
        binding.callLayout.visibility = View.GONE
    }

    private fun setCallLayoutVisible() {
        binding.callLayout.visibility = View.VISIBLE
    }

    private fun setWhoToCallLayoutGone() {
        binding.whoToCallLayout.visibility = View.GONE
    }

    private fun setWhoToCallLayoutVisible() {
        binding.whoToCallLayout.visibility = View.VISIBLE
    }
}