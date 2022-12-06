package com.example.neopidorapp.feature_call.presentation.rtc_service

import android.app.Application
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
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
import com.example.neopidorapp.util.Constants
import com.example.neopidorapp.util.Constants.TAG_DEBUG
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import org.webrtc.*
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

    var peerConnectionObserver: PeerConnectionObserver? = null

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

                        rtcState.updateIncomingOfferMessageData(socketMessage.data) // todo RTCdata

                        emitCallServiceEvent(CallServiceEvent.CallOfferReceived)

                        rtcState.updateRemoteViewLoading(false)
                    }
                    "answer_received" -> {
                        val remoteSession = SessionDescription(
                            SessionDescription.Type.ANSWER,
                            socketMessage.data.toString() // todo RTCdata
                        )
                        onRemoteSessionReceived(remoteSession)

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
        sendMessageToSocket( // not needed when implement pushes, replace with smth else.
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
        call(
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

    fun initPeerConnectionObserver() { // todo move to the Service?
        peerConnectionObserver = object : PeerConnectionObserver() {



            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                Log.d(Constants.TAG_PEER_CONNECTION_OUTPUT, "onIceConnectionChange: newState = $p0")
                super.onIceConnectionChange(p0)

                if (p0 == PeerConnection.IceConnectionState.DISCONNECTED) {
                    // todo OR/AND remove sll Sinks for video tracks? - hz.

                    // todo OR just call peerConnection.close() also.
//                    endCall()
                    rtcClientWrapper.killPeerConnection()
                }
            }

            override fun onIceCandidate(p0: IceCandidate?) {
                Log.d(Constants.TAG_PEER_CONNECTION_OUTPUT, "onIceCandidate: ${p0.toString()}")

                super.onIceCandidate(p0)
                addIceCandidate(p0)
                /**
                 * we add an ICE Candidate above...
                 * ... and it's time to send this ICE Candidate to our peer:
                 * SENDING ICE CANDIDATE:
                 */
                val candidate = hashMapOf(
                    "sdpMid" to p0?.sdpMid,
                    "sdpMLineIndex" to p0?.sdpMLineIndex,
                    "sdpCandidate" to p0?.sdp
                )
                sendMessageToSocket(
//                    MessageModel("ice_candidate", vm.username, vm.targetName, candidate)
                    MessageModel("ice_candidate", myUsername, _targetName, candidate)
                )
            }

            override fun onAddStream(p0: MediaStream?) {
                Log.d(Constants.TAG_PEER_CONNECTION_OUTPUT, "onAddStream: ${p0.toString()}")

                super.onAddStream(p0)
                p0?.videoTracks?.get(0)?.addSink(remoteView)
            }
        }
    }
    //====================METHODS END====================
}



sealed class CallServiceEvent {
    data class SnackbarMessage(val msg: String): CallServiceEvent()
    object TargetIsOnlineAndReadyToReceiveACall: CallServiceEvent()
    object CallOfferReceived: CallServiceEvent()
}