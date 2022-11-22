package com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_client

import com.example.neopidorapp.feature_call.presentation.call.socket.SocketRepo
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer

interface CallClientWrapper {



    // todo here have to be all call control functions,
    // and State + NotificationCallback + CoroutineScope will be parameters
    // in the constructor of the instance, inheriting this interface.

    fun initializeSurfaceView(surface: SurfaceViewRenderer)

    fun startLocalVideo(surface: SurfaceViewRenderer)

    fun call(targetName: String, usernameNew: String, socketRepoNew: SocketRepo)

    fun onRemoteSessionReceived(remoteSession: SessionDescription)

    fun answer(targetName: String, usernameNew: String, socketRepoNew: SocketRepo)

    fun addIceCandidate(p0: IceCandidate?)

    fun switchCamera()

    fun toggleAudio(mute: Boolean)

    fun toggleCamera(cameraPaused: Boolean)

    fun endCall()
}