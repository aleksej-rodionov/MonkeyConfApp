package com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_client

import android.app.Application
import com.example.neopidorapp.MainActivity
import com.example.neopidorapp.feature_call.presentation.call.socket.SocketRepo
import com.example.neopidorapp.feature_call.presentation.rtc_service.notification.NotificationCallback
import com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_ui_state.RTCUiStateControl
import com.example.neopidorapp.models.MessageModel
import kotlinx.coroutines.CoroutineScope
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer

class RTCClientWrapper(
    private val rtcUiStateControl: RTCUiStateControl,
    private val notificationCallback: NotificationCallback,
    private val scope: CoroutineScope
): CallClientWrapper {

    var rtcClient: RTCClient? = null

    fun initRtcClient(application: Application, observer: PeerConnectionObserver) {
        rtcClient = RTCClient(application, observer)
    }

    // todo implement wrapper control methods
    override fun initializeSurfaceView(surface: SurfaceViewRenderer) {
        rtcClient?.initializeSurfaceView(surface)
    }

    override fun startLocalVideo(surface: SurfaceViewRenderer) {
        rtcClient?.startLocalVideo(surface)
    }

    override fun call(targetName: String, usernameNew: String, socketRepoNew: SocketRepo) {
        rtcClient?.call(targetName, usernameNew, socketRepoNew)
    }

    override fun onRemoteSessionReceived(remoteSession: SessionDescription) {
        rtcClient?.onRemoteSessionReceived(remoteSession)
    }

    override fun answer(targetName: String, usernameNew: String, socketRepoNew: SocketRepo) {
        rtcClient?.answer(targetName, usernameNew, socketRepoNew)
    }

    override fun addIceCandidate(p0: IceCandidate?) {
        addIceCandidate(p0)
    }



    //====================CONTROL METHODS====================
    override fun switchCamera() {
        rtcClient?.switchCamera()
    }

    override fun toggleAudio(mute: Boolean) {
        rtcClient?.toggleAudio(mute)
    }

    override fun toggleCamera(cameraPaused: Boolean) {
        rtcClient?.toggleCamera(cameraPaused)
    }

    override fun endCall() {
        rtcClient?.endCall()
    }
    //====================CONTROL METHODS END====================
}