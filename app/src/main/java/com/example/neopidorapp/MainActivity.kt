package com.example.neopidorapp

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.neopidorapp.databinding.ActivityMainBinding
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

//        binding.enterBtn.setOnClickListener {
//
//            PermissionX.init(this)
//                .permissions(
//                    Manifest.permission.RECORD_AUDIO,
//                    Manifest.permission.CAMERA
//                ).request { alLGranted, _, _ ->
//                    if (alLGranted) {
//                        startActivity(
//                            Intent(
//                                this@MainActivity,
//                                CallActivity::class.java
//                            ).putExtra("username", binding.username.text.toString())
//                        )
//                    } else {
//                        Toast.makeText(
//                            this,
//                            "You shoulg accept all permissions",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
//                }
//        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }




}