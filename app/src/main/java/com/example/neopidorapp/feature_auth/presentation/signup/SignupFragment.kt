package com.example.neopidorapp.feature_auth.presentation.signup

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.neopidorapp.R
import com.example.neopidorapp.databinding.FragmentSignupBinding
import com.example.neopidorapp.feature_auth.presentation.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignupFragment: Fragment(R.layout.fragment_signup) {

    private val viewModel: AuthViewModel by activityViewModels()

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSignupBinding.bind(view)

        initListeners()
    }

    override fun onResume() {
        super.onResume()
        binding.name.setText(viewModel.usernameSignup)
        binding.email.setText(viewModel.emailSignup)
        binding.password.setText(viewModel.passwordSignup)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initListeners() {
        binding.apply {
            name.addTextChangedListener {
                viewModel.usernameSignup = it.toString()
            }

            email.addTextChangedListener {
                viewModel.emailSignup = it.toString()
            }

            password.addTextChangedListener {
                viewModel.passwordSignup = it.toString()
            }

            btnSignup.setOnClickListener {
                viewModel.onSignupClick()
            }

            btnAlreadyRegistered.setOnClickListener {
                viewModel.alreadyRegisteredClick()
            }
        }
    }
}