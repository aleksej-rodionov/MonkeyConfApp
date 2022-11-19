package com.example.neopidorapp.feature_call.presentation.call

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.neopidorapp.R
import com.example.neopidorapp.databinding.FragmentCallBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallFragment: Fragment(R.layout.fragment_call) {

    private val vm: CallViewModel by viewModels()

    private var _binding: FragmentCallBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCallBinding.bind(view)


    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}