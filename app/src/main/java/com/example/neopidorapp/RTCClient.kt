package com.example.neopidorapp

import android.app.Application
import com.example.neopidorapp.models.MessageModel
import org.webrtc.*

class RTCClient(
    private val application: Application,
    private val username: String,
    private val socketRepo: SocketRepo,
    private val observer: PeerConnection.Observer
) {

    private val eglContext = EglBase.create()
    private val peerConnectionFactory by lazy { createPeerConnectionFactory() }
    private val iceServer = listOf(
        /**
         * "ice" here means "Interactive Connectivity Establishment" (ICE)
         * I just googled "best iceservers",
         * opened "https://gist.github.com/sagivo/3a4b2f2c7ac6e1b5267c2f1f59ac6c6b"
         * and chose the same server as Kael did.
         */
        PeerConnection.IceServer.builder("stun:iphone-stun.strato-iphone.de:3478").createIceServer()
    )
    private val peerConnection by lazy { createPeerConnection(observer) }

    /**
     * A screencast is a digital recording of computer screen output,
     * also known as a video screen capture or a screen recording,
     * often containing audio narration.
     */
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
    private val localAudioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }

    init {
        initPeerConnectionFactory(application)
    }

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
        return peerConnectionFactory.createPeerConnection(iceServer, observer)
    }


    // todo review and understand functions below (they are for surface-view shit).
    fun initializeSurfaceView(surface: SurfaceViewRenderer) {
        surface.run {
            setEnableHardwareScaler(true)
            setMirror(true)
            init(eglContext.eglBaseContext, null) // todo ...particularly this.
        }
    }

    fun startLocalVideo(surface: SurfaceViewRenderer) {
        val surfaceTextureHelper =
            SurfaceTextureHelper.create(Thread.currentThread().name, eglContext.eglBaseContext)
        val videoCapturer = getVideoCapturer(application)
        videoCapturer.initialize(
            surfaceTextureHelper,
            surface.context,
            localVideoSource.capturerObserver
        )
        videoCapturer.startCapture(320, 240, 30)
        val localVideoTrack =
            peerConnectionFactory.createVideoTrack("local_track", localVideoSource)
        localVideoTrack.addSink(surface)
        val localAudioTrack =
            peerConnectionFactory.createAudioTrack("local_track_audio", localAudioSource)
        val localStream = peerConnectionFactory.createLocalMediaStream("local_stream")
        localStream.addTrack(localAudioTrack)
        localStream.addTrack(localVideoTrack)

        peerConnection?.addStream(localStream)
    }

    private fun getVideoCapturer(application: Application): VideoCapturer {
        return Camera2Enumerator(application).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }
    }

    fun call(targetName: String) {

        val mediaConstraints = MediaConstraints()
        mediaConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))

        peerConnection?.createOffer(
            object : SdpObserver {
                /**
                 * todo learn about this SdpObserver and what it does
                 * this is in Video 6 at 8th minute
                 */

                override fun onCreateSuccess(desc: SessionDescription?) {
                    // Whenever this Offer is created we also wand to add its Local Description to it.
                    peerConnection?.setLocalDescription(
                        object : SdpObserver {
                            override fun onCreateSuccess(p0: SessionDescription?) {}

                            override fun onSetSuccess() {
                                val offer = hashMapOf(
                                    "sdp" to desc?.description,
                                    "type" to desc?.type
                                )
                                socketRepo?.sendMessageToSocket(
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
        peerConnection?.setRemoteDescription(
            object : SdpObserver {
                override fun onCreateSuccess(p0: SessionDescription?) {}
                override fun onSetSuccess() {}
                override fun onCreateFailure(p0: String?) {}
                override fun onSetFailure(p0: String?) {}
            },
            remoteSession
        )
    }

    fun answer(targetName: String) {
        val mediaConstraints = MediaConstraints()
        mediaConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))

        peerConnection?.createAnswer(
            object : SdpObserver {
                override fun onCreateSuccess(desc: SessionDescription?) {
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onCreateSuccess(p0: SessionDescription?) {}

                        override fun onSetSuccess() {
                            val answer = hashMapOf(
                                "sdp" to desc?.description,
                                "type" to desc?.type
                            )
                            socketRepo.sendMessageToSocket(
                                MessageModel("create_answeer", username, targetName, answer)
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
}





