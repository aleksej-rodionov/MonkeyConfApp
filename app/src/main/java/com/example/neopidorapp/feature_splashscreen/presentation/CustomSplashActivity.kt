package com.example.neopidorapp.feature_splashscreen.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.neopidorapp.CallActivity
import com.example.neopidorapp.databinding.ActivitySplashBinding
import com.example.neopidorapp.feature_auth.presentation.AuthActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CustomSplashActivity: AppCompatActivity() {

    private val binding by lazy { ActivitySplashBinding.inflate(layoutInflater) }

    private val viewModel: CustomSplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initObservers()
    }

    private fun initObservers() {
        this.lifecycleScope.launch {
            viewModel.isLogged.collectLatest {
                if (it) {
                    toCallActivity()
                } else {
                    toAuthActivity()
                }
            }
        }
    }

    private fun toCallActivity() {
        val intent = Intent(this, CallActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun toAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }
}