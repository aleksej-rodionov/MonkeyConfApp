package com.example.neopidorapp.feature_call.presentation.rtc_service

import android.app.Application
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.neopidorapp.feature_call.presentation.call.socket.SocketRepo
import com.example.neopidorapp.feature_call.presentation.rtc_service.notification.NotificationCallback
import com.example.neopidorapp.feature_call.presentation.rtc_service.notification.RTCNotification
import com.example.neopidorapp.feature_call.presentation.rtc_service.notification.RTCNotificationReceiver.Companion.ACTION_END_CALL
import com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_client.PeerConnectionObserver
import com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_client.RTCAudioManager
import com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_client.RTCClientWrapper
import com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_ui_state.RTCState
import com.example.neopidorapp.models.MessageModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer

@AndroidEntryPoint
class RTCService : Service(), NotificationCallback {

    private val rtcBinder = RTCBinder()

    // scope is needed for notification
    private val rtcServiceJob = SupervisorJob()
    private val rtcServiceScope = CoroutineScope(Dispatchers.Main + rtcServiceJob)

    private val rtcAudioManager by lazy { RTCAudioManager.create(this) }

    //====================RTCCLIENT AND ITS NOTIFICATION====================
    val rtcState = RTCState()

    // todo RTCClient with new parameters: state, notifCallback, coroutineScope
    val rtcClientWrapper = RTCClientWrapper(
        rtcState,
        this,
        rtcServiceScope
    )

    val notification = RTCNotification(rtcServiceScope, this)
    //====================RTCCLIENT AND ITS NOTIFICATION END====================


    //====================OVERRIDDEN SERVICE METHODS====================
    override fun onBind(p0: Intent?): IBinder? {
        return rtcBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let {
            when (it) {
                ACTION_END_CALL -> {
                    endCall()
                }
                else -> Unit
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        // todo stop and nullize rtc connection?
        rtcServiceScope.cancel()
    }
    //====================OVERRIDDEN SERVICE METHODS END====================

    inner class RTCBinder : Binder() {
        val service get() = this@RTCService
    }

    //====================OVERRIDDEN NOTIFICATION_CALLBACK METHODS====================
    override fun launchNotification() {
        notification.launchNotificationJob()
    }
    //====================OVERRIDDEN NOTIFICATION_CALLBACK METHODS END====================
    fun initRtcClient(application: Application, observer: PeerConnectionObserver) {
        rtcClientWrapper.initRtcClient(application, observer)
    }

    fun initializeSurfaceView(surface: SurfaceViewRenderer) {
        rtcClientWrapper.initializeSurfaceView(surface)
    }

    fun startLocalVideo(surface: SurfaceViewRenderer) {
        rtcClientWrapper.startLocalVideo(surface)
    }

    fun call(targetName: String, username: String, socketRepo: SocketRepo) {
        rtcClientWrapper.call(targetName, username, socketRepo)
    }

    fun onRemoteSessionReceived(remoteSession: SessionDescription) {
        rtcClientWrapper.onRemoteSessionReceived(remoteSession)
    }

    fun answer(targetName: String, username: String, socketRepo: SocketRepo) {
        rtcClientWrapper.answer(targetName, username, socketRepo)
    }

    fun addIceCandidate(p0: IceCandidate?) {
        rtcClientWrapper.addIceCandidate(p0)
    }


    //====================RTC WRAPPER METHODS====================
    // todo check if there are state observers in RTCClient later on

    fun switchCamera() {
        rtcClientWrapper.switchCamera()
    }

    fun toggleAudio(mute: Boolean) {
        rtcClientWrapper.toggleAudio(mute)
        rtcState.updateIsMute(mute)
    }

    fun toggleCamera(cameraPaused: Boolean) {
        rtcClientWrapper.toggleCamera(cameraPaused)
        rtcState.updateIsCameraPaused(cameraPaused)
    }

    fun toggleAudioOutput() {
        // todo toggle rtcAudioManager
        rtcAudioManager.setDefaultAudioDevice(
            if (!rtcState.currentState().isSpeakerMode) {
                RTCAudioManager.AudioDevice.SPEAKER_PHONE
            } else {
                RTCAudioManager.AudioDevice.EARPIECE
            }
        )

        rtcState.updateIsSpeakerMode(!rtcState.currentState().isSpeakerMode)
    }

    fun endCall() {
        rtcClientWrapper.endCall()
//        rtcState.updateIsOngoingCall(false)
//        rtcState.updateIsIncomingCall(false) // todo why
    }
    //====================RTC CONTROL METHODS END====================
    //====================RTC WRAPPER METHODS END====================
}