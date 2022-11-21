package com.example.neopidorapp.feature_call.presentation.rtc_service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@AndroidEntryPoint
class RTCService : Service() {

    private val rtcBinder = RTCBinder()

    // scope is needed for notification
    private val rtcServiceJob = SupervisorJob()
    private val rtcServiceScope = CoroutineScope(Dispatchers.Main + rtcServiceJob)

    // todo RTCClient and its Notification stuff to be declared here

    //====================OVERRIDDEN SERVICE METHODS====================
    override fun onBind(p0: Intent?): IBinder? {
        return rtcBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let {
            when (it) {
                // todo
                else -> Unit
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        // todo stop and nullize rtc connection?
        rtcServiceScope.cancel()
    }
    //====================OVERRIDDEN SERVICE METHODS END====================

    inner class RTCBinder : Binder() {
        val service get() = this@RTCService
    }
}