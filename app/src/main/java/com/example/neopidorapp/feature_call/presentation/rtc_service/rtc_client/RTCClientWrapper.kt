package com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_client

import android.app.Application
import com.example.neopidorapp.feature_call.presentation.call.socket.SocketRepo
import com.example.neopidorapp.feature_call.presentation.rtc_service.notification.NotificationCallback
import com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_ui_state.RTCUiStateControl
import kotlinx.coroutines.CoroutineScope
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer

class RTCClientWrapper(
    private val rtcUiStateControl: RTCUiStateControl,
    private val notificationCallback: NotificationCallback,
    private val scope: CoroutineScope
): CallClientWrapper {

    var rtcClient: RTCClient? = null



    //====================RTC WRAPPER METHODS====================
    override fun initRtcClient(application: Application, observer: PeerConnectionObserver) {
        rtcClient = RTCClient(application, observer)
    }

    override fun initializeSurfaceView(surface: SurfaceViewRenderer) {
        rtcClient?.initializeSurfaceView(surface)
    }

    override fun startLocalVideo(surface: SurfaceViewRenderer) {
        rtcClient?.startLocalVideo(surface)
    }

    override fun call(targetName: String, username: String, socketRepo: SocketRepo) {
        rtcClient?.call(targetName, username, socketRepo)
    }

    override fun onRemoteSessionReceived(remoteSession: SessionDescription) {
        rtcClient?.onRemoteSessionReceived(remoteSession)
    }

    override fun answer(targetName: String, username: String, socketRepo: SocketRepo) {
        rtcClient?.answer(targetName, username, socketRepo)
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
    //====================RTC WRAPPER METHODS END====================
}