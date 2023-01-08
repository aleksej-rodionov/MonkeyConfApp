package com.example.monkeyconfapp.feature_call.presentation.rtc_service.rtc_client

import android.app.Application
import com.example.monkeyconfapp.feature_call.data.SocketRepo
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer

interface CallClientWrapper {
  fun initRtcClient(application: Application, observer: PeerConnectionObserver)



    fun initializeSurfaceView(surface: SurfaceViewRenderer)

    fun startLocalVideo(surface: SurfaceViewRenderer)

    fun call(targetName: String, username: String, socketRepo: SocketRepo)

    fun onRemoteSessionReceived(remoteSession: SessionDescription)

    fun answer(targetName: String, username: String, socketRepo: SocketRepo)

    fun addIceCandidate(p0: IceCandidate?)

    fun switchCamera()

    fun toggleAudio(mute: Boolean)

    fun toggleCamera(cameraPaused: Boolean)

    fun closePeerConnection()

    fun releaseSurfaceView(surface: SurfaceViewRenderer)

    fun nullizeRTCClient()
}