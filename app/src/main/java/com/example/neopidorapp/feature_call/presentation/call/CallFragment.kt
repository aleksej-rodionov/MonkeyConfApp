package com.example.neopidorapp.feature_call.presentation.call

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.neopidorapp.MainActivity
import com.example.neopidorapp.R
import com.example.neopidorapp.databinding.FragmentCallBinding
import com.example.neopidorapp.feature_call.presentation.call.rtc.PeerConnectionObserver
import com.example.neopidorapp.feature_call.presentation.call.rtc.RTCAudioManager
import com.example.neopidorapp.feature_call.presentation.call.rtc.RTCClient
import com.example.neopidorapp.models.MessageModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import org.webrtc.IceCandidate
import org.webrtc.MediaStream

@AndroidEntryPoint
class CallFragment: Fragment(R.layout.fragment_call) {

    private val vm: CallViewModel by viewModels()

    private var _binding: FragmentCallBinding? = null
    private val binding get() = _binding!!

    // todo move to the Service?
    private val rtcAudioManager by lazy { RTCAudioManager.create(requireContext()) }
    // todo move to the Service?
    private var rtcClient: RTCClient? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCallBinding.bind(view)

        initObservers()
        initListeners()

        vm.initSocket()
        initRtcClient()
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.callScreenState.collectLatest { state ->

                binding.apply {

                    if (state.incomingCallReceived) {
                        setIncomingCallLayoutVisible()
                        setWhoToCallLayoutGone()
                    } else {
                        setIncomingCallLayoutGone()
                    }

                    if (state.isCallRunning) {
                        setCallLayoutVisible()
                        setWhoToCallLayoutGone()
                    } else {
                        setCallLayoutGone()
                    }

                    if (!state.incomingCallReceived && !state.isCallRunning) {
                        setWhoToCallLayoutVisible()
                    }

                    micButton.setImageResource(if (state.isMute) R.drawable.ic_baseline_mic_24
                    else R.drawable.ic_baseline_mic_off_24)
                    rtcClient?.toggleAudio(state.isMute)

                    videoButton.setImageResource(if (state.isCameraPaused) R.drawable.ic_baseline_videocam_off_24
                    else R.drawable.ic_baseline_videocam_24)
                    rtcClient?.toggleCamera(state.isCameraPaused)

                    audioOutputButton.setImageResource(if (state.isSpeakerMode) R.drawable.ic_baseline_speaker_up_24
                    else R.drawable.ic_baseline_hearing_24)

                    // todo move to the Service?
                    rtcAudioManager.setDefaultAudioDevice(if (state.isSpeakerMode) RTCAudioManager.AudioDevice.SPEAKER_PHONE
                    else RTCAudioManager.AudioDevice.EARPIECE)
                }
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

            switchCameraButton.setOnClickListener {
                vm.onSwitchCameraButtonClick()
                rtcClient?.switchCamera()
            }

            micButton.setOnClickListener {
                vm.onMicButtonClick()
            }

            videoButton.setOnClickListener {
                vm.onVideoButtonClick()
            }

            audioOutputButton.setOnClickListener {
                vm.onAudioOutputButtonClick()
            }

            endCallButton.setOnClickListener {
                vm.onEndCallButtonClick()
                rtcClient?.endCall()
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    //====================RTCCLIENT METHODS====================
    private fun initRtcClient() {
        val username = vm.username
        val socketRepo = vm.socketRepo
        rtcClient = RTCClient(
            (activity as? MainActivity)?.application, // we can just write "application" cause we're inside of the Activity
            username!!,
            socketRepo,
            object : PeerConnectionObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    rtcClient?.addIceCandidate(p0) // we add an ICE Candidate...
                    // ... and it's time to send this ICE Candidate to our peer:
                    // SENDING ICE CANDIDATE:
                    val candidate = hashMapOf(
                        "sdpMid" to p0?.sdpMid,
                        "sdpMLineIndex" to p0?.sdpMLineIndex,
                        "sdpCandidate" to p0?.sdp
                    )
                    socketRepo?.sendMessageToSocket(
                        MessageModel("ice_candidate", username, vm.targetName, candidate)
                    )
                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    p0?.videoTracks?.get(0)?.addSink(binding.remoteView)
                }
            }
        )
    }
    //====================RTCCLIENT METHODS END====================



//====================PRIVATE METHODS====================
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