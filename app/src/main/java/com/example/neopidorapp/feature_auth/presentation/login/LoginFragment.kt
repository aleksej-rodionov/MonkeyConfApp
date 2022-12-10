package com.example.neopidorapp.feature_auth.presentation.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.neopidorapp.R
import com.example.neopidorapp.databinding.FragmentLoginBinding
import com.example.neopidorapp.feature_auth.presentation.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment: Fragment(R.layout.fragment_login) {

    private val viewModel: AuthViewModel by activityViewModels()

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        initObserver()
        initListeners()
    }

    private fun initObserver() {
        // todo
    }

    private fun initListeners() {
        // todo
    }

    override fun onResume() {
        super.onResume()
//        binding // todo set values from vm to editText-s
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}