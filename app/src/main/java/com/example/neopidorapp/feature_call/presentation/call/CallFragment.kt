package com.example.neopidorapp.feature_call.presentation.call

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.neopidorapp.R
import com.example.neopidorapp.databinding.FragmentCallBinding
import com.example.neopidorapp.feature_call.presentation.call.rtc.RTCAudioManager
import com.example.neopidorapp.models.MessageModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallFragment: Fragment(R.layout.fragment_call) {

    private val vm: CallViewModel by viewModels()

    private var _binding: FragmentCallBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCallBinding.bind(view)

        vm.initSocket()
        initListeners()
    }

    private fun initListeners() {
        binding.apply {
            callBtn.setOnClickListener {
                vm.onCallButtonClick()
            }

            switchCameraButton.setOnClickListener {
                vm.onSwitchCameraButtonClick()
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
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setIncomingCallLayoutGone() {
        binding.incomingCallLayout.visibility = View.GONE
    }

    private fun setIncomingCallLayoutVisible() {
        binding.incomingCallLayout.visibility = View.VISIBLE
    }

    private fun setCallLayoutGone() {
        binding.callLayout.visibility = View.GONE
    }

    private fun setCallLayoutVisible() {
        binding.callLayout.visibility = View.VISIBLE
    }

    private fun setWhoToCallLayoutGone() {
        binding.whoToCallLayout.visibility = View.GONE
    }

    private fun setWhoToCallLayoutVisible() {
        binding.whoToCallLayout.visibility = View.VISIBLE
    }
}