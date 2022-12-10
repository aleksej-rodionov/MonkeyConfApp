package com.example.neopidorapp

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.neopidorapp.databinding.ActivityMainBinding
import com.example.neopidorapp.feature_call.presentation.name.NameFragmentDirections
import com.example.neopidorapp.feature_call.presentation.rtc_service.CallService
import com.example.neopidorapp.util.Constants.TAG_DEBUG
import com.example.neopidorapp.util.Constants.TAG_HASHCODE
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG_HASHCODE, "onCreate: activity.hashcode = ${this.hashCode()}")
        setContentView(binding.root)

        val navHost = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHost.navController

        val myUsername = intent.getStringExtra("myUsername")
//        myUsername?.let {
//            navController.navigate(R.))
//        }
        val openFragment = intent.getStringExtra("openFragment")
        openFragment?.let {
            val direction = NavGraphDirections.actionGlobalCallFragment(myUsername ?: "no_name")
            navController.navigate(direction)
        }
    }

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        Log.d(TAG_DEBUG, "onNewIntent: ")
//        val username = intent?.getStringExtra("myUsername")
//        username?.let {
//            Log.d(TAG_DEBUG, "onNewIntent: username = $it")
//        }
//        val openFragment = intent?.getStringExtra("openFragment")
//        openFragment?.let {
//            Log.d(TAG_DEBUG, "onNewIntent: openFragment = $it")
//        }
//    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG_HASHCODE, "onResume: activity.hashcode = ${this.hashCode()}")
        startCallService()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG_HASHCODE, "onDestroy: activity.hashcode = ${this.hashCode()}")
        stopCallService()
    }

    
    
    //====================RTC SERVICE====================
    fun startCallService() {
        val serviceIntent = Intent(this, CallService::class.java)
        startService(serviceIntent)
    }

    fun bindCallService(conn: ServiceConnection) {
        val serviceIntent = Intent(this, CallService::class.java)
        bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE)
    }

    fun stopCallService() {
        stopService(Intent(this, CallService::class.java))
    }
    //====================RTC SERVICE END====================
}