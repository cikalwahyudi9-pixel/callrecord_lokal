package com.example.callrecorder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

class PhoneStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("enabled", false)) return
        val screenOn = context.getSystemService(Context.POWER_SERVICE)
            ?.let { (it as android.os.PowerManager).isInteractive } ?: false
        if (!screenOn) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val serviceIntent = Intent(context, CallRecorderService::class.java).apply {
            action = when (state) {
                TelephonyManager.EXTRA_STATE_RINGING, TelephonyManager.EXTRA_STATE_OFFHOOK -> CallRecorderService.ACTION_CALL_STARTED
                TelephonyManager.EXTRA_STATE_IDLE -> CallRecorderService.ACTION_CALL_ENDED
                else -> CallRecorderService.ACTION_ARM
            }
        }
        context.startForegroundService(serviceIntent)
    }
}
