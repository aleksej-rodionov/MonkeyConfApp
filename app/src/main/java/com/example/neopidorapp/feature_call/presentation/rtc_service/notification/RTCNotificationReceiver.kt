package com.example.neopidorapp.feature_call.presentation.rtc_service.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.neopidorapp.feature_call.presentation.rtc_service.CallService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RTCNotificationReceiver: BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        when (p1?.action) {
            ACTION_END_CALL -> {
                val serviceIntent = Intent(p0, CallService::class.java)
                serviceIntent.action = ACTION_END_CALL
                p0?.startService(serviceIntent)
            }
        }
    }

    companion object {
        const val ACTION_END_CALL = "ACTION_END_CALL"
    }
}