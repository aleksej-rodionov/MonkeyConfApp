package com.example.neopidorapp.util

import org.webrtc.*

/**
 * this open class is created in order to avoid implementing all these numerous functions
 * when instantiating RTCClient in CallActivity.
 * We just use this class and implement only 2 functions we really need instead.
 */
open class PeerConnectionObserver : PeerConnection.Observer {

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        // empty
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        // empty
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        // empty
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        // empty
    }

    override fun onIceCandidate(p0: IceCandidate?) {
        // empty
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        // empty
    }

    override fun onAddStream(p0: MediaStream?) {
        // empty
    }

    override fun onRemoveStream(p0: MediaStream?) {
        // empty
    }

    override fun onDataChannel(p0: DataChannel?) {
        // empty
    }

    override fun onRenegotiationNeeded() {
        // empty
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        // empty
    }
}