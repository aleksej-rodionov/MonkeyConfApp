package com.example.neopidorapp

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.example.neopidorapp.databinding.ActivityCallBinding
import com.example.neopidorapp.models.MessageModel
import com.example.neopidorapp.util.NewMessageInterface

class CallActivity: AppCompatActivity(), NewMessageInterface {

    private val binding by lazy { ActivityCallBinding.inflate(layoutInflater) }
    private var userName: String? = null
    private var socketRepo: SocketRepo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        userName = intent.getStringExtra("username")
        socketRepo = SocketRepo(this@CallActivity)
        userName?.let { socketRepo?.initSocket(it) }
    }

    override fun onNewMessage(message: MessageModel) {
        // todo
    }
}