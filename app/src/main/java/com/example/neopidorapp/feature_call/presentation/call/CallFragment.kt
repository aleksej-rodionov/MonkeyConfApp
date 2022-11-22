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
import com.example.neopidorapp.feature_call.presentation.rtc_service.rtc_client.RTCAudioManager
import com.example.neopidorapp.models.IceCandidateModel
import com.example.neopidorapp.models.MessageModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription

private const val TAG = "CallFragment"

@AndroidEntryPoint
class CallFragment: Fragment(R.layout.fragment_call) {

    private var rtcService: RTCService? = null
    private var peerConnectionObserver: PeerConnectionObserver? = null

    private val vm: CallViewModel by viewModels()

    private var _binding: FragmentCallBinding? = null
    private val binding get() = _binding!!

    // todo move to the Service?
    private val rtcAudioManager by lazy { RTCAudioManager.create(requireContext()) }

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
//                    rtcClient?.toggleAudio(state.isMute)

                    videoButton.setImageResource(if (state.isCameraPaused) R.drawable.ic_baseline_videocam_off_24
                    else R.drawable.ic_baseline_videocam_24)
//                    rtcClient?.toggleCamera(state.isCameraPaused)

                    audioOutputButton.setImageResource(if (state.isSpeakerMode) R.drawable.ic_baseline_cameraswitch_24
                    else R.drawable.ic_baseline_hearing_24)

                    // todo move to the Service?
//                    rtcAudioManager.setDefaultAudioDevice(if (state.isSpeakerMode) RTCAudioManager.AudioDevice.SPEAKER_PHONE
//                    else RTCAudioManager.AudioDevice.EARPIECE)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.incomingMessage.collectLatest { message ->
                Log.d(TAG, "onNewMessage: $message")
                when (message.type) {
                    "call_response" -> {
                        if (message.data == "user is not online") {
//                            runOnUiThread { // we have to run it on the UI thread because we're on the Socket thread
                                Toast.makeText(requireContext(), "user is not online", Toast.LENGTH_LONG).show()
//                            }
                        } else {
//                            runOnUiThread { // we have to run it on the UI thread because we're on the Socket thread

                            //====================LAYOUT CONFIG====================
//                                setWhoToCallLayoutGone() // todo handle through state
//                                setCallLayoutVisible() // todo handle through state
                                vm.updateIsOngoingCall(true)
                            //====================LAYOUT CONFIG END====================

                                binding.apply {
                                    rtcService?.initializeSurfaceView(localView)
                                    rtcService?.initializeSurfaceView(remoteView)
                                    rtcService?.startLocalVideo(localView)
                                    rtcService?.call(targetUserNameEt.text.toString(), vm.username!!, vm.socketRepo)
                                }
//                            }
                        }
                    }
                    "offer_received" -> {
//                        runOnUiThread {

                        //====================LAYOUT CONFIG====================
//                            setIncomingCallLayoutVisible() // todo handle through state
                            vm.updateIsIncomingCall(true)
                        //====================LAYOUT CONFIG END====================

                            binding.apply {
                                incomingNameTV.text = "${message.name.toString()} is calling you"
                                acceptButton.setOnClickListener {

                                    //====================LAYOUT CONFIG====================
                                    setIncomingCallLayoutGone() // todo handle through state
                                    setCallLayoutVisible() // todo handle through state
                                    setWhoToCallLayoutGone() // todo handle through state
                                    vm.updateIsIncomingCall(false)
                                    vm.updateIsOngoingCall(true)
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
                                    vm.updateTargetName(message.name!!)
                                }
                                rejectButton.setOnClickListener {

                                    //====================LAYOUT CONFIG====================
                                    setIncomingCallLayoutGone() // todo handle through state
                                    vm.updateIsIncomingCall(false)
                                    //====================LAYOUT CONFIG END====================

                                }
                                remoteViewLoading.visibility = View.GONE
                            }
//                        }
                    }
                    "answer_received" -> {
                        val session = SessionDescription(
                            SessionDescription.Type.ANSWER,
                            message.data.toString()
                        )
                        rtcService?.onRemoteSessionReceived(session)
//                        runOnUiThread {
                            binding.remoteViewLoading.visibility = View.GONE
//                        }
                    }
                    "ice_candidate" -> {
                        // RECEIVING ICE CANDIDATE:
//                        runOnUiThread {
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
//                        }
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
            rtcService?.rtcState?.state?.collectLatest {
                Log.d(TAG, "new rtcState = $it")
                vm.updateState(it)
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

            switchCameraButton.setOnClickListener { // doesnt't kill CallFragment. Other listeners
                // (that call rtcClient in state.collect {} block) - kill it
                vm.onSwitchCameraButtonClick()
                rtcService?.switchCamera()
            }

            micButton.setOnClickListener {
                rtcService?.toggleAudio(!vm.callScreenState.value.isMute) // todo return to state collector?
                vm.onMicButtonClick()
            }

            videoButton.setOnClickListener {
                rtcService?.toggleCamera(!vm.callScreenState.value.isCameraPaused) // todo return to state collector?
                vm.onVideoButtonClick()
            }

            audioOutputButton.setOnClickListener {
                rtcAudioManager.setDefaultAudioDevice(if (!vm.callScreenState.value.isSpeakerMode) RTCAudioManager.AudioDevice.SPEAKER_PHONE // todo return to state collector?
                else RTCAudioManager.AudioDevice.EARPIECE) // todo return to state collector?
                vm.onAudioOutputButtonClick()
            }

            endCallButton.setOnClickListener { // doesnt't kill CallFragment. Other listeners
                // (that call rtcClient in state.collect {} block) - kill it
                vm.onEndCallButtonClick()
                rtcService?.endCall()

                //====================LAYOUT CONFIG====================
//                setCallLayoutGone() // todo return to state collector?
//                setWhoToCallLayoutVisible() // todo return to state collector?
//                setIncomingCallLayoutGone() // todo return to state collector?
                //====================LAYOUT CONFIG END====================
            }
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