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
import com.example.neopidorapp.feature_call.presentation.rtc_service.CallService
import com.example.neopidorapp.feature_call.presentation.rtc_service.CallServiceEvent
import com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_client.PeerConnectionObserver
import com.example.neopidorapp.models.IceCandidateModel
import com.example.neopidorapp.models.MessageModel
import com.example.neopidorapp.util.Constants.TAG_DEBUG
import com.example.neopidorapp.util.Constants.TAG_PEER_CONNECTION
import com.example.neopidorapp.util.currentThreadName
import com.example.neopidorapp.util.isCurrentThreadMain
import com.google.android.material.snackbar.Snackbar
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

    private var callService: CallService? = null
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

        // todo where to call it? in onViewCreated() or rtcBinderState.collectLatest {} ??
//        callService?.initSocket()
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

                    if (state.remoteViewLoadingVisible) {
                        remoteViewLoading.visibility = View.VISIBLE
                    } else {
                        remoteViewLoading.visibility = View.GONE
                    }

                    incomingNameTV.text = "${state.incomingCallSenderName} is calling you"

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
            vm.callServiceBinderState.collectLatest {
                if (it != null) {
                    callService = it.service
                    callService?.initUsername(vm.username)
                    callService?.initSocket(vm.username)
                    initPeerConnectionObserver()
                    callService?.initRtcClient(
                        (activity as MainActivity).application,
                        peerConnectionObserver!!
                    )
                    initRTCStateCollector()
                    initCallServiceEventCollector()
                } else {
                    callService = null
                }
            }
        }
    }

    private fun initRTCStateCollector() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            callService?.rtcState?.state?.collectLatest { state ->
                Log.d(TAG_STATE_SERVICE, "$state")
                vm.updateStateToDisplay(
                    CallScreenState(
                        state.isIncomingCall,
                        state.isOngoingCall,
                        state.isMute,
                        state.isCameraPaused,
                        state.isSpeakerMode,
                        state.remoteViewLoadingVisible,
                        state.incomingCallSenderName
                    )
                )
            }
        }
    }

    private fun initCallServiceEventCollector() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            callService?.callServiceEvent?.collectLatest { event ->
                when (event) {
                    is CallServiceEvent.SnackbarMessage -> {
                        Snackbar.make(
                            binding.root,
                            "user is not online",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    is CallServiceEvent.TargetIsOnlineAndReadyToReceiveACall -> {
                        callService?.initializeSurfaceViewsAndStartLocalVideo(
                            binding.localView,
                            binding.remoteView
                        )
                        callService?.callAfterInitializingSurfaceViews()
                    }
                    is CallServiceEvent.CallOfferReceived -> {
                        setAcceptClickListener()
                        setRejectClickListener()
                    }
                }
            }
        }
    }

    private fun initListeners() {
        binding.apply {

            targetUserNameEt.addTextChangedListener {
                vm.updateTargetName(it.toString())
                callService?.updateTargetName(it.toString())
            }



            callBtn.setOnClickListener {
                callService?.onCallButtonClick(vm.username, vm.targetName)
            }



            //====================RTC VIEW CONTROL BUTTONS====================
            switchCameraButton.setOnClickListener {
                callService?.switchCamera()
            }

            micButton.setOnClickListener {
                callService?.toggleAudio(!vm.callScreenState.value.isMute)
            }

            videoButton.setOnClickListener {
                callService?.toggleCamera(!vm.callScreenState.value.isCameraPaused) // todo combine in Service
            }

            audioOutputButton.setOnClickListener {
                callService?.toggleAudioOutput()
            }

            endCallButton.setOnClickListener {
                callService?.endCall()
            }
            //====================RTC VIEW CONTROL BUTTONS END====================
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).bindRTCService(vm.getCallServiceConnection())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    //====================PRIVATE METHODS====================
    private fun initPeerConnectionObserver() {
        peerConnectionObserver = object : PeerConnectionObserver() {
            override fun onIceCandidate(p0: IceCandidate?) {
                Log.d(TAG_PEER_CONNECTION, "onIceCandidate: ${p0.toString()}")

                super.onIceCandidate(p0)
                callService?.addIceCandidate(p0)
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
                callService?.sendMessageToSocket(
//                        MessageModel("ice_candidate", username, vm.targetName, candidate)
                    MessageModel("ice_candidate", vm.username, vm.targetName, candidate)
                )
            }

            override fun onAddStream(p0: MediaStream?) {
                Log.d(TAG_PEER_CONNECTION, "onAddStream: ${p0.toString()}")

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



    private fun setAcceptClickListener() {
        binding.acceptButton.setOnClickListener { // todo handle through Service.state
//            setIncomingCallLayoutGone() // todo handle through Service.state
//            setCallLayoutVisible()  // todo handle through Service.state
//            setWhoToCallLayoutGone()  // todo handle through Service.state
            callService?.updateIsIncomingCall(false)
            callService?.updateIsOngoingCall(true)

            binding.apply {
                callService?.initializeSurfaceViewsAndStartLocalVideo(
                    localView,
                    remoteView
                )
            }

            callService?.setReceivedSessionDescriptionToPeerConnection()

            callService?.answerAfterInitViewsAndReceivingSession()
        }
    }
    private fun setRejectClickListener() {
        binding.rejectButton.setOnClickListener {

            //====================LAYOUT CONFIG====================
            setIncomingCallLayoutGone() // todo handle through Service.state
            callService?.updateIsIncomingCall(false)
            //====================LAYOUT CONFIG END====================

        }
    }
    //====================PRIVATE METHODS END====================
}