package com.example.neopidorapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.neopidorapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.enterBtn.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    CallActivity::class.java
                ).putExtra("username", binding.username.text.toString())
            )
        }
    }
}