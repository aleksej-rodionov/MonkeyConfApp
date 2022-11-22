package com.example.neopidorapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.neopidorapp.databinding.ActivityMainBinding
import com.example.neopidorapp.feature_call.presentation.rtc_service.RTCService
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val navHost = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHost.navController
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
    
    
    
    //====================RTC SERVICE====================
    private fun startService() {
        val serviceIntent = Intent(this, RTCService::class.java)
        startService(serviceIntent)
    }

    fun bindRTCService(conn: ServiceConnection) {
        val serviceIntent = Intent(this, RTCService::class.java)
        bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE)
    }


    //====================RTC SERVICE END====================
}