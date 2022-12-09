package com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_client

import android.app.Application
import android.util.Log
import com.example.neopidorapp.feature_call.data.SocketRepo
import com.example.neopidorapp.models.MessageModel
import com.example.neopidorapp.util.Constants.TAG_DEBUG
import com.example.neopidorapp.util.Constants.TAG_PEER_CONNECTION_BASE
import com.example.neopidorapp.util.Constants.TAG_PEER_CONNECTION_INPUT
import com.example.neopidorapp.util.Constants.TAG_PEER_CONNECTION_OUTPUT
import kotlinx.coroutines.CoroutineScope
import org.webrtc.*

private const val TAG = "RTCClient"

class RTCClient(
    private val application: Application?,
    private val observer: PeerConnection.Observer,
    private val socketRepo: SocketRepo,
    // This Observer object above will notify whenever there is an ICE Candidate.
    /**
     * (In previous video we've learned how to create an Offer or an Answer
     * and exchange their SessionDescription to each other.
     * But in this video we are going to learn how to exchange the ICE Candidates.
     * So, whenever a local SessionDescription is set, there will be some ICE Candidate.)
     */
    private val scope: CoroutineScope
) {

    private val eglContext = EglBase.create()
    private val peerConnectionFactory by lazy { createPeerConnectionFactory() }
    private val iceServer = listOf(
        /**
         * "ice" here means "Interactive Connectivity Establishment" (ICE)
         * I just googled "best iceservers",
         * opened "https://gist.github.com/sagivo/3a4b2f2c7ac6e1b5267c2f1f59ac6c6b"...
         * ...and chose the same server as Kael did (stun:iphone-stun.strato-iphone.de:3478),
         * but it works only if we are using local connection;
         * so if you want it to be a real calling app - type in google "best turn server"
         * (cause you need a TurnServer for that),
         * opened link "https://www.metered.ca/tools/openrelay/" and scrolled down to
         * the sample Javascript Code with recommended configuration to allow through most firewalls.
         * And copy-pasted that shit below the iphone-stun-server.
         * You can listen about it in the last (8th) video at the 8th minute.
         */
        PeerConnection.IceServer.builder("stun:iphone-stun.strato-iphone.de:3478").createIceServer(),
        PeerConnection.IceServer("stun:openrelay.metered.ca:80"), // turn server stuff starts here
        PeerConnection.IceServer("turn:openrelay.metered.ca:80","openrelayproject","openrelayproject"),
        PeerConnection.IceServer("turn:openrelay.metered.ca:443","openrelayproject","openrelayproject"),
        PeerConnection.IceServer("turn:openrelay.metered.ca:443?transport=tcp","openrelayproject","openrelayproject")
    )
    private val peerConnection by lazy { createPeerConnection(observer) }

    /**
     * A screencast is a digital recording of computer screen output,
     * also known as a video screen capture or a screen recording,
     * often containing audio narration.
     */
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
    private val localAudioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }
    private var videoCapturer: CameraVideoCapturer? = null
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null

    init {
        initPeerConnectionFactory(application!!)
        Log.d(TAG, "peerConnection = $peerConnection")
    }


    // todo review and understand functions below (they are for surface-view shit).
    // and all these functions below have to be triggered from the Fragment code,
    // but through the Service

    fun initializeSurfaceView(surface: SurfaceViewRenderer) {
        surface.run {
            setEnableHardwareScaler(true)
            setMirror(true)
            init(eglContext.eglBaseContext, null) // todo ...particularly this.
        }
    }

    fun releaseSurfaceView(surface: SurfaceViewRenderer) {
        surface.release()
    }

    fun startLocalVideo(surface: SurfaceViewRenderer) {
        val surfaceTextureHelper =
            SurfaceTextureHelper.create(Thread.currentThread().name, eglContext.eglBaseContext)
        videoCapturer = getVideoCapturer(application!!)
        videoCapturer?.initialize(
            surfaceTextureHelper,
            surface.context,
            localVideoSource.capturerObserver
        )
        videoCapturer?.startCapture(320, 240, 30)
        localVideoTrack =
            peerConnectionFactory.createVideoTrack("local_track", localVideoSource)
        localVideoTrack?.addSink(surface)
        localAudioTrack =
            peerConnectionFactory.createAudioTrack("local_track_audio", localAudioSource)
        val localStream = peerConnectionFactory.createLocalMediaStream("local_stream")
        localStream.addTrack(localAudioTrack)
        localStream.addTrack(localVideoTrack)

        Log.d(TAG_PEER_CONNECTION_INPUT, "addStream(local): ${localStream.toString()}")
        peerConnection?.addStream(localStream) // peerConnectionAction
    }

    fun call(targetName: String, username: String, socketRepo: SocketRepo) {
        Log.d(TAG_DEBUG, "call: CALLED")

        val mediaConstraints = MediaConstraints()
        mediaConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))

        Log.d(TAG_PEER_CONNECTION_INPUT, "createOffer")
        peerConnection?.createOffer( // peerConnectionAction
            object : SdpObserver {
                /**
                 * todo learn about this SdpObserver and what it does
                 * this is in Video 6 at 8th minute
                 */

                override fun onCreateSuccess(desc: SessionDescription?) {
                    // Whenever this Offer is created we also wand to add its Local Description to it.
                    Log.d(TAG_PEER_CONNECTION_INPUT, "setLocalDescription (to offer we just created)")
                    peerConnection?.setLocalDescription( // peerConnectionAction
                        object : SdpObserver {
                            override fun onCreateSuccess(p0: SessionDescription?) {}

                            override fun onSetSuccess() {

                                val offer = hashMapOf(
                                    "sdp" to desc?.description,
                                    "type" to desc?.type
                                )
                                socketRepo.sendMessageToSocket(
                                    MessageModel("create_offer", username, targetName, offer)
                                )
                            }

                            override fun onCreateFailure(p0: String?) {}
                            override fun onSetFailure(p0: String?) {}
                        },
                        desc
                    )

                }

                override fun onSetSuccess() {}
                override fun onCreateFailure(p0: String?) {}
                override fun onSetFailure(p0: String?) {}
            },
            mediaConstraints
        )
    }

    fun onRemoteSessionReceived(remoteSession: SessionDescription) {
        Log.d(TAG_PEER_CONNECTION_INPUT, "setRemoteDescription")
        peerConnection?.setRemoteDescription( // peerConnectionAction
            object : SdpObserver {
                override fun onCreateSuccess(p0: SessionDescription?) {}
                override fun onSetSuccess() {}
                override fun onCreateFailure(p0: String?) {}
                override fun onSetFailure(p0: String?) {}
            },
            remoteSession
        )
    }

    fun answer(targetName: String, username: String, socketRepo: SocketRepo) {
        val mediaConstraints = MediaConstraints()
        mediaConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))

        Log.d(TAG_PEER_CONNECTION_INPUT, "createAnswer")
        peerConnection?.createAnswer( // peerConnectionAction
            object : SdpObserver {
                override fun onCreateSuccess(desc: SessionDescription?) {

                    Log.d(TAG_PEER_CONNECTION_INPUT, "setLocalDescription (to answer we just created)")
                    peerConnection?.setLocalDescription(object : SdpObserver { // peerConnectionAction
                        override fun onCreateSuccess(p0: SessionDescription?) {}

                        override fun onSetSuccess() {
                            val answer = hashMapOf(
                                "sdp" to desc?.description,
                                "type" to desc?.type
                            )
                            socketRepo.sendMessageToSocket(
                                MessageModel("create_answer", username, targetName, answer)
                            )
                        }

                        override fun onCreateFailure(p0: String?) {}
                        override fun onSetFailure(p0: String?) {}
                    }, desc)
                }

                override fun onSetSuccess() {}
                override fun onCreateFailure(p0: String?) {}
                override fun onSetFailure(p0: String?) {}
            },
            mediaConstraints
        )
    }

    fun addIceCandidate(p0: IceCandidate?) {
        Log.d(TAG_PEER_CONNECTION_INPUT, "addIceCandidate(iceCandidate)")
        peerConnection?.addIceCandidate(p0) // peerConnectionAction
    }



//    fun killPeerConnection() {
//        peerConnection?.dispose()
////        peerConnection?.close()
//        peerConnectionFactory?.dispose()
//    }



    //====================CONTROL METHODS====================
    fun switchCamera() {
        videoCapturer?.switchCamera(null)
    }

    fun toggleAudio(mute: Boolean) {
        localAudioTrack?.setEnabled(!mute)
    }

    fun toggleCamera(cameraPaused: Boolean) {
        localVideoTrack?.setEnabled(!cameraPaused)
    }

    fun closePeerConnection() {
        peerConnection?.close()
    }
    //====================CONTROL METHODS END====================



    //====================PRIVATE FUNCTIONS====================
    private fun initPeerConnectionFactory(application: Application) {
        val peerConnectionOption = PeerConnectionFactory.InitializationOptions.builder(application)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(peerConnectionOption)
    }

    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory.builder()
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    eglContext.eglBaseContext,
                    true,
                    true
                )
            )
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglContext.eglBaseContext))
            .setOptions(PeerConnectionFactory.Options().apply {
                disableEncryption = true
                disableNetworkMonitor = true
            }).createPeerConnectionFactory()
    }

    private fun createPeerConnection(observer: PeerConnection.Observer): PeerConnection? {
        Log.d(TAG_PEER_CONNECTION_BASE, "createPeerConnection: ")
        return peerConnectionFactory.createPeerConnection(iceServer, observer)
    }

    private fun getVideoCapturer(application: Application): CameraVideoCapturer {
        return Camera2Enumerator(application).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }
    }
    //====================PRIVATE FUNCTIONS END====================
}





