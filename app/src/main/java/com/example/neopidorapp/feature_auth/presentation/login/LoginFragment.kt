package com.example.neopidorapp.feature_auth.presentation.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.neopidorapp.R
import com.example.neopidorapp.databinding.FragmentLoginBinding
import com.example.neopidorapp.feature_auth.presentation.AuthActivity
import com.example.neopidorapp.feature_auth.presentation.AuthEvent
import com.example.neopidorapp.feature_auth.presentation.AuthViewModel
import com.example.neopidorapp.feature_auth.presentation.RC_SIGN_IN
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginFragment: Fragment(R.layout.fragment_login), GoogleApiClient.OnConnectionFailedListener {

    private val viewModel: AuthViewModel by activityViewModels()

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val gso by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    private val googleApiClient by lazy {
        GoogleApiClient.Builder(requireContext())
            .enableAutoManage((activity as AuthActivity), this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        initListeners()
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            email.setText(viewModel.emailLogin)
            password.setText(viewModel.passwordLogin)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun initListeners() {
        binding.apply {
            email.addTextChangedListener {
                viewModel.emailLogin = it.toString()
            }

            password.addTextChangedListener {
                viewModel.passwordLogin = it.toString()
            }

            btnLogin.setOnClickListener {
                viewModel.onLoginClick()
            }

            btnSignup.setOnClickListener {
                viewModel.onSignupClick()
            }

            btnForgotPassword.setOnClickListener {
                viewModel.onForgotPasswordClick()
            }

            btnGoogleSignin.setOnClickListener {
                onGoogleSignInClick()
            }
        }
    }

    private fun onGoogleSignInClick() {
        val signIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result?.isSuccess == true) {
                val account = result.signInAccount
                account?.let {
                    viewModel.authWithGoogle(it)
                }
            }
        }
    }



    //====================GOOGLE API CLIENT METHODS====================
    override fun onConnectionFailed(p0: ConnectionResult) {
        viewModel.emitAuthEvent(AuthEvent.SnackbarMessage("Google connection failed: ${p0.errorMessage}"))
    }
}