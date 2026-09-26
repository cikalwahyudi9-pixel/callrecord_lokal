package com.example.callrecorder

import android.content.Intent
import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val channel = "call_recorder_local/native"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channel).setMethodCallHandler { call, result ->
            when (call.method) {
                "enableAutomation" -> {
                    getSharedPreferences("settings", MODE_PRIVATE).edit().putBoolean("enabled", true).apply()
                    result.success(null)
                }
                "disableAutomation" -> {
                    getSharedPreferences("settings", MODE_PRIVATE).edit().putBoolean("enabled", false).apply()
                    stopService(Intent(this, CallRecorderService::class.java))
                    result.success(null)
                }
                "recordingDirectory" -> result.success(CallRecorderService.recordingDirectory(this).absolutePath)
                "testRecording" -> {
                    val intent = Intent(this, CallRecorderService::class.java).setAction(CallRecorderService.ACTION_TEST)
                    startForegroundService(intent)
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }
    }
}
