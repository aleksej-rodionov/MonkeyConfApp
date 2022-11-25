package com.example.neopidorapp.feature_call.presentation.call

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.neopidorapp.MainActivity
import com.example.neopidorapp.R
import com.example.neopidorapp.databinding.FragmentCallBinding
import com.example.neopidorapp.feature_call.presentation.rtc_service.RTCService
import com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_client.PeerConnectionObserver
import com.example.neopidorapp.models.IceCandidateModel
import com.example.neopidorapp.models.MessageModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription

private const val TAG = "CallFragment"
private const val TAG_STATE_VM = "TAG_STATE_VM"
private const val TAG_STATE_SERVICE = "TAG_STATE_SERVICE"

@AndroidEntryPoint
class CallFragment: Fragment(R.layout.fragment_call) {

    private var rtcService: RTCService? = null
    private var peerConnectionObserver: PeerConnectionObserver? = null

    private val vm: CallViewModel by viewModels()

    private var _binding: FragmentCallBinding? = null
    private val binding get() = _binding!!

    // todo move to the Service?
//    private val rtcAudioManager by lazy { RTCAudioManager.create(requireContext()) }

    private val gson = Gson()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCallBinding.bind(view)

        initObservers()
        initListeners()

        vm.initSocket()

        // todo where to call it? in onViewCreated() or rtcBinderState.collectLatest {} ??
//        initPeerConnectionObserver()
//        initRtcClient()
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.callScreenState.collectLatest { state ->
                Log.d(TAG_STATE_VM, "$state")

                binding.apply {

                    //====================LAYOUT CONFIG====================
                    if (state.isIncomingCall && !state.isOngoingCall) {
                        setIncomingCallLayoutVisible()
                        setCallLayoutGone()
                        setWhoToCallLayoutGone()
                    } else if (!state.isIncomingCall && state.isOngoingCall) {
                        setIncomingCallLayoutGone()
                        setCallLayoutVisible()
                        setWhoToCallLayoutGone()
                    } else if (state.isIncomingCall && state.isOngoingCall) {
                        setIncomingCallLayoutVisible()
                        setCallLayoutVisible()
                        setWhoToCallLayoutGone()
                    } else if (!state.isIncomingCall && !state.isOngoingCall) {
                        setIncomingCallLayoutGone()
                        setCallLayoutGone()
                        setWhoToCallLayoutVisible()
                    }
                    //====================LAYOUT CONFIG END====================

                    micButton.setImageResource(if (state.isMute) R.drawable.ic_baseline_mic_24
                    else R.drawable.ic_baseline_mic_off_24)

                    videoButton.setImageResource(if (state.isCameraPaused) R.drawable.ic_baseline_videocam_off_24
                    else R.drawable.ic_baseline_videocam_24)

                    audioOutputButton.setImageResource(if (state.isSpeakerMode) R.drawable.ic_baseline_cameraswitch_24
                    else R.drawable.ic_baseline_hearing_24)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.incomingMessage.collectLatest { message ->
                Log.d(TAG, "onNewMessage: ${message.type}")
                when (message.type) {
                    "call_response" -> {
                        if (message.data == "user is not online") {
                            Toast.makeText( // todo handle through Service.state
                                requireContext(),
                                "user is not online",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            //====================LAYOUT CONFIG====================
//                                vm.updateIsOngoingCall(true) // todo handle through Service.state!!
                            rtcService?.updateIsOngoingCall(true)
                            //====================LAYOUT CONFIG END====================

                            binding.apply { // todo ERROR OCCURES SMWHERE IN THIS BLOCK
                                rtcService?.initializeSurfaceView(localView)
                                rtcService?.initializeSurfaceView(remoteView)
                                rtcService?.startLocalVideo(localView)
                                rtcService?.call(
                                    targetUserNameEt.text.toString(),
                                    vm.username!!,
                                    vm.socketRepo
                                )
                            }
                        }
                    }
                    "offer_received" -> {

                        //====================LAYOUT CONFIG====================
//                            vm.updateIsIncomingCall(true) // todo handle through Service.state!!
                        rtcService?.updateIsIncomingCall(true)
                        //====================LAYOUT CONFIG END====================

                        binding.apply {
                            incomingNameTV.text = "${message.name.toString()} is calling you"
                            acceptButton.setOnClickListener { // todo handle through Service.state

                                //====================LAYOUT CONFIG====================
                                setIncomingCallLayoutGone() // todo handle through Service.state
                                setCallLayoutVisible()  // todo handle through Service.state
                                setWhoToCallLayoutGone()  // todo handle through Service.state
//                                vm.updateIsIncomingCall(false) // todo handle through Service.state!!
//                                vm.updateIsOngoingCall(true) // todo handle through Service.state!!
                                rtcService?.updateIsIncomingCall(false)
                                rtcService?.updateIsOngoingCall(true)
                                //====================LAYOUT CONFIG END====================

                                rtcService?.initializeSurfaceView(localView)
                                rtcService?.initializeSurfaceView(remoteView)
                                rtcService?.startLocalVideo(localView)
                                val remoteSession = SessionDescription(
                                    SessionDescription.Type.OFFER,
                                    message.data.toString()
                                )
                                rtcService?.onRemoteSessionReceived(remoteSession)
                                rtcService?.answer(message.name!!, vm.username!!, vm.socketRepo)
                                vm.updateTargetName(message.name!!) // todo handle through Service.state
                            }
                            rejectButton.setOnClickListener {

                                //====================LAYOUT CONFIG====================
                                setIncomingCallLayoutGone() // todo handle through Service.state
//                                vm.updateIsIncomingCall(false) // todo handle through Service.state!!
                                rtcService?.updateIsIncomingCall(false)
                                //====================LAYOUT CONFIG END====================

                            }
                            remoteViewLoading.visibility = View.GONE
                        }
                    }
                    "answer_received" -> {
                        val session = SessionDescription(
                            SessionDescription.Type.ANSWER,
                            message.data.toString()
                        )
                        rtcService?.onRemoteSessionReceived(session)
                        binding.remoteViewLoading.visibility = View.GONE
                    }
                    "ice_candidate" -> {
                        // RECEIVING ICE CANDIDATE:
                        try {
                            val receivedIceCandidate = gson.fromJson(
                                gson.toJson(message.data),
                                IceCandidateModel::class.java
                            )
                            rtcService?.addIceCandidate(
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



        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.rtcBinderState.collectLatest {
                if (it != null) {
                    rtcService = it.service
                    initPeerConnectionObserver()
                    rtcService?.initRtcClient(
                        (activity as MainActivity).application,
                        peerConnectionObserver!!
                    )
                    initRTCStateCollector()
                } else {
                    rtcService = null
                }
            }
        }
    }

    private fun initRTCStateCollector() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            rtcService?.rtcState?.state?.collectLatest { state ->
                Log.d(TAG_STATE_SERVICE, "$state")
                vm.updateStateToDisplay(
                    CallScreenState(
                        state.isIncomingCall,
                        state.isOngoingCall,
                        state.isMute,
                        state.isCameraPaused,
                        state.isSpeakerMode
                    )
                )

//                binding.apply {
//
//                    //====================LAYOUT CONFIG====================
//                    if (state.isIncomingCall && !state.isOngoingCall) {
//                        setIncomingCallLayoutVisible()
//                        setCallLayoutGone()
//                        setWhoToCallLayoutGone()
//                    } else if (!state.isIncomingCall && state.isOngoingCall) {
//                        setIncomingCallLayoutGone()
//                        setCallLayoutVisible()
//                        setWhoToCallLayoutGone()
//                    } else if (state.isIncomingCall && state.isOngoingCall) {
//                        setIncomingCallLayoutVisible()
//                        setCallLayoutVisible()
//                        setWhoToCallLayoutGone()
//                    } else if (!state.isIncomingCall && !state.isOngoingCall) {
//                        setIncomingCallLayoutGone()
//                        setCallLayoutGone()
//                        setWhoToCallLayoutVisible()
//                    }
//                    //====================LAYOUT CONFIG END====================
//
//                    micButton.setImageResource(if (state.isMute) R.drawable.ic_baseline_mic_24
//                    else R.drawable.ic_baseline_mic_off_24)
//
//                    videoButton.setImageResource(if (state.isCameraPaused) R.drawable.ic_baseline_videocam_off_24
//                    else R.drawable.ic_baseline_videocam_24)
//
//                    audioOutputButton.setImageResource(if (state.isSpeakerMode) R.drawable.ic_baseline_cameraswitch_24
//                    else R.drawable.ic_baseline_hearing_24)
//                }
            }
        }
    }

    private fun initListeners() {
        binding.apply {

            targetUserNameEt.addTextChangedListener {
                vm.updateTargetName(it.toString())
            }



            callBtn.setOnClickListener {
                vm.onCallButtonClick()
            }



            //====================RTC VIEW CONTROL BUTTONS====================
            switchCameraButton.setOnClickListener {
                rtcService?.switchCamera()
            }

            micButton.setOnClickListener {
                rtcService?.toggleAudio(!vm.callScreenState.value.isMute)
            }

            videoButton.setOnClickListener {
                rtcService?.toggleCamera(!vm.callScreenState.value.isCameraPaused) // todo combine in Service
            }

            audioOutputButton.setOnClickListener {
                rtcService?.toggleAudioOutput()
            }

            endCallButton.setOnClickListener {
                rtcService?.endCall()
            }
            //====================RTC VIEW CONTROL BUTTONS END====================
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).bindRTCService(vm.getRTCServiceConnection())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    //====================PRIVATE METHODS====================
    private fun initPeerConnectionObserver() {
        peerConnectionObserver = object : PeerConnectionObserver() {
            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                rtcService?.addIceCandidate(p0)
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
                vm.socketRepo?.sendMessageToSocket(
//                        MessageModel("ice_candidate", username, vm.targetName, candidate)
                    MessageModel("ice_candidate", vm.username, vm.targetName, candidate)
                )
            }

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                p0?.videoTracks?.get(0)?.addSink(binding.remoteView)
            }
        }
    }



    private fun setIncomingCallLayoutGone() {
        binding.incomingCallLayout.visibility = View.GONE
    }

    private fun setIncomingCallLayoutVisible() {
        binding.incomingCallLayout.visibility = View.VISIBLE
    }

    private fun setWhoToCallLayoutGone() {
        binding.whoToCallLayout.visibility = View.GONE
    }

    private fun setWhoToCallLayoutVisible() {
        binding.whoToCallLayout.visibility = View.VISIBLE
    }

    private fun setCallLayoutGone() {
        binding.callLayout.visibility = View.GONE
    }

    private fun setCallLayoutVisible() {
        binding.callLayout.visibility = View.VISIBLE
    }
    //====================PRIVATE METHODS END====================
}