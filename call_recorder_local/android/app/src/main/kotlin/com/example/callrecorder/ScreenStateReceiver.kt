package com.example.callrecorder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScreenStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("enabled", false)) return
        when (intent.action) {
            Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_UNLOCKED -> {
                val serviceIntent = Intent(context, CallRecorderService::class.java).setAction(CallRecorderService.ACTION_ARM)
                context.startForegroundService(serviceIntent)
            }
            Intent.ACTION_SCREEN_OFF -> {
                context.stopService(Intent(context, CallRecorderService::class.java))
            }
        }
    }
}
