package com.example.neopidorapp.feature_call.presentation.rtc_service

import android.app.Application
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.example.neopidorapp.feature_call.data.SocketRepo
import com.example.neopidorapp.feature_call.presentation.rtc_service.notification.NotificationCallback
import com.example.neopidorapp.feature_call.presentation.rtc_service.notification.CallServiceNotification
import com.example.neopidorapp.feature_call.presentation.rtc_service.notification.RTCNotificationReceiver.Companion.ACTION_END_CALL
import com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_client.PeerConnectionObserver
import com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_client.RTCAudioManager
import com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_client.RTCClientWrapper
import com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_ui_state.RTCState
import com.example.neopidorapp.models.IceCandidateModel
import com.example.neopidorapp.models.MessageModel
import com.example.neopidorapp.util.Constants.TAG_DEBUG
import com.example.neopidorapp.util.currentThreadName
import com.example.neopidorapp.util.isCurrentThreadMain
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

@AndroidEntryPoint
class CallService : Service(), NotificationCallback {

    var myUsername: String = ""

    var _targetName: String = ""
    fun updateTargetName(name: String) {
        _targetName = name
    }

    var _callerName: String = ""
    fun updateCallerName(name: String) {
        _callerName = name
    }

    var localView: SurfaceViewRenderer? = null // todo store them here in case of background
    var remoteView: SurfaceViewRenderer? = null

    private val callServiceBinder = CallServiceBinder()

    // scope is needed for notification
    private val callServiceJob = SupervisorJob()
    private val callServiceScope = CoroutineScope(Dispatchers.Main + callServiceJob)

    private val rtcAudioManager by lazy { RTCAudioManager.create(this) }

    @Inject
    lateinit var socketRepo: SocketRepo

    private val gson = Gson()

    private val _callServiceEvent = MutableSharedFlow<CallServiceEvent>()
    val callServiceEvent: SharedFlow<CallServiceEvent> = _callServiceEvent.asSharedFlow()
    fun emitCallServiceEvent(event: CallServiceEvent) = callServiceScope.launch {
        _callServiceEvent.emit(event)
    }

    //====================RTCCLIENT AND ITS NOTIFICATION====================
    val rtcState = RTCState()

    val rtcClientWrapper by lazy {
        RTCClientWrapper(
            rtcState,
            this,
            socketRepo,
            callServiceScope
        )
    }

    val notification = CallServiceNotification(callServiceScope, this)
    //====================RTCCLIENT AND ITS NOTIFICATION END====================


    //====================OVERRIDDEN SERVICE METHODS====================
    override fun onCreate() {
        super.onCreate()
        initIncomingSocketMessageObserver()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return callServiceBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let {
            when (it) {
                ACTION_END_CALL -> {
                    endCall()
                    localView?.let { lv ->
                        remoteView?.let { rv ->
                            releaseSurfaceViews(lv, rv)
                        }
                    }
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
        callServiceScope.cancel()
    }
    //====================OVERRIDDEN SERVICE METHODS END====================



    //====================OBSERVER METHODS====================
    private fun initIncomingSocketMessageObserver() {
        callServiceScope.launch {
            socketRepo.incomingMessage.collectLatest { socketMessage ->
                Log.d(TAG_DEBUG, "Service.incomingMessage: \n${socketMessage.type} \n${socketMessage.data}")

                when (socketMessage.type) {
                    "call_response" -> {
                        if (socketMessage.data == "user is not online") {
                            emitCallServiceEvent(CallServiceEvent.SnackbarMessage("user is not online"))
                        } else {
                            updateIsOngoingCall(true)

                            emitCallServiceEvent(CallServiceEvent.TargetIsOnlineAndReadyToReceiveACall)
                        }
                    }
                    "offer_received" -> {
                        updateIsIncomingCall(true) // STATE

                        updateCallerName(socketMessage.name ?: "")

                        rtcState.updateIncomingOfferMessageData(socketMessage.data)

                        emitCallServiceEvent(CallServiceEvent.CallOfferReceived)

                        rtcState.updateRemoteViewLoading(false)
                    }
                    "answer_received" -> {
                        val session = SessionDescription(
                            SessionDescription.Type.ANSWER,
                            socketMessage.data.toString()
                        )
                        onRemoteSessionReceived(session)

                        rtcState.updateRemoteViewLoading(false) // STATE
                    }
                    "ice_candidate" -> {
                        try {
                            val receivedIceCandidate = gson.fromJson(
                                gson.toJson(socketMessage.data),
                                IceCandidateModel::class.java
                            )
                            addIceCandidate(
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
    }
    //====================OBSERVER METHODS END====================



    inner class CallServiceBinder : Binder() {
        val service get() = this@CallService
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

    fun call(targetName: String, username: String) {
        rtcClientWrapper.call(targetName, username, socketRepo)
    }

    fun onRemoteSessionReceived(remoteSession: SessionDescription) {
        rtcClientWrapper.onRemoteSessionReceived(remoteSession)
    }

    fun answer(targetName: String, username: String) {
        rtcClientWrapper.answer(targetName, username, socketRepo)
    }

    fun addIceCandidate(p0: IceCandidate?) {
        rtcClientWrapper.addIceCandidate(p0)
    }


    //====================RTC WRAPPER METHODS====================
    // todo check if there are state observers in RTCClient later on
    fun updateIsIncomingCall(incoming: Boolean) {
        rtcState.updateIsIncomingCall(incoming)
    }

    fun updateIsOngoingCall(ongoing: Boolean) {
        rtcState.updateIsOngoingCall(ongoing)
    }

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
        rtcState.updateIsOngoingCall(false)
        rtcState.updateIsIncomingCall(false) // todo why
    }

    fun releaseSurfaceViews(
        localView: SurfaceViewRenderer,
        remoteView: SurfaceViewRenderer
    ) {
        Log.d(TAG_DEBUG, "releaseSurfaceViews: CALLED")
        rtcClientWrapper.releaseSurfaceView(localView)
        rtcClientWrapper.releaseSurfaceView(remoteView)
    }
    //====================RTC CONTROL METHODS END====================
    //====================RTC WRAPPER METHODS END====================



    //====================METHODS====================
    fun initUsername(username: String?) {
        username?.let { myUsername = it }
    }

    fun initSocket(username: String?) {
        username?.let { u ->
            socketRepo.initSocket(u)
        }
    }

    fun onCallButtonClick(username: String?, targetName: String?) {
        sendMessageToSocket(
            MessageModel(
                "start_call",
                username,
                targetName,
                null
            )
        )
    }

    fun sendMessageToSocket(messageModel: MessageModel) {
        socketRepo.sendMessageToSocket(messageModel)
    }

    fun initializeSurfaceViewsAndStartLocalVideo(
        localView: SurfaceViewRenderer,
        remoteView: SurfaceViewRenderer
    ) {
        this.localView = localView
        this.remoteView = remoteView

        initializeSurfaceView(localView)
        initializeSurfaceView(remoteView)
        startLocalVideo(localView)
    }

    fun callAfterInitializingSurfaceViews() {
        call( // todo ERROR OCCURS SMWHERE IN THIS BLOCK
            _targetName ?: "",
            myUsername!!
        )
    }

    fun setReceivedSessionDescriptionToPeerConnection() {
        val remoteSession = SessionDescription(
            SessionDescription.Type.OFFER,
            rtcState.state.value.incomingOfferMessageData.toString()
        )

        onRemoteSessionReceived(remoteSession)
    }

    fun answerAfterInitViewsAndReceivingSession() {
        answer(_callerName, myUsername)
        updateTargetName(_callerName)
    }
    //====================METHODS END====================
}



sealed class CallServiceEvent {
    data class SnackbarMessage(val msg: String): CallServiceEvent()
    object TargetIsOnlineAndReadyToReceiveACall: CallServiceEvent()
    object CallOfferReceived: CallServiceEvent()
}