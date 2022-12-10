package com.example.neopidorapp.feature_auth.presentation.new_password

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.neopidorapp.R
import com.example.neopidorapp.databinding.FragmentNewPasswordBinding
import com.example.neopidorapp.feature_auth.presentation.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewPasswordFragment: Fragment(R.layout.fragment_new_password) {

    private val viewModel: AuthViewModel by activityViewModels()

    private var _binding: FragmentNewPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNewPasswordBinding.bind(view)

        initListeners()
    }



    private fun initListeners() {
        binding.apply {
            etEmail.addTextChangedListener {
                viewModel.emailNewPassword = it.toString()
            }

            btnSubmitNewPassword.setOnClickListener {
                viewModel.onResetPasswordClick()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.etEmail.setText(viewModel.emailNewPassword)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}