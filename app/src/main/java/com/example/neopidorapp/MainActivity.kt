package com.example.neopidorapp

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.neopidorapp.databinding.ActivityMainBinding
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.enterBtn.setOnClickListener {

            PermissionX.init(this)
                .permissions(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
                ).request { alLGranted, _, _ ->
                    if (alLGranted) {
                        startActivity(
                            Intent(
                                this@MainActivity,
                                CallActivity::class.java
                            ).putExtra("username", binding.username.text.toString())
                        )
                    } else {
                        Toast.makeText(
                            this,
                            "You shoulg accept all permissions",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}