package com.example.neopidorapp.feature_auth.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.neopidorapp.CallActivity
import com.example.neopidorapp.NavGraphAuthDirections
import com.example.neopidorapp.NavGraphDirections
import com.example.neopidorapp.R
import com.example.neopidorapp.databinding.ActivityAuthBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AuthActivity: AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    private val binding by lazy { ActivityAuthBinding.inflate(layoutInflater) }
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val navHost = supportFragmentManager.findFragmentById(R.id.fragmentContainerAuth) as NavHostFragment
        navController = navHost.navController

        initObservers()

        val straightToLogin = intent.getBooleanExtra("straightToLogin", false)
        if (straightToLogin) {
            viewModel.alreadyRegisteredClick()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun initObservers() {
        this.lifecycleScope.launchWhenStarted {
            viewModel.authState.collectLatest {
                if (it.user != null || it.isLoggedIn) {
                    toCallActivity()
                }
            }
        }

        this.lifecycleScope.launchWhenStarted {
            viewModel.authEvent.collectLatest { event ->
                when (event) {
                    is AuthEvent.SnackbarMessage -> {
                        Snackbar.make(binding.root, event.msg, Snackbar.LENGTH_SHORT).show()
                    }
                    is AuthEvent.NavigateToLoginScreen -> {
                        val action = NavGraphAuthDirections.actionGlobalLoginFragment()
                        navController.navigate(action)
                    }
                    is AuthEvent.NavigateToNewPasswordScreen -> {
                        val action = NavGraphAuthDirections.actionGlobalNewPasswordFragment()
                        navController.navigate(action)
                    }
                }
            }
        }
    }

    private fun toCallActivity() {
        val intent = Intent(this, CallActivity::class.java)
        startActivity(intent)
        finish()
    }
}

const val RC_SIGN_IN = 9001