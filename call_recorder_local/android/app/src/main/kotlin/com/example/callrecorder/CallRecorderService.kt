package com.example.callrecorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CallRecorderService : Service() {
    companion object {
        const val ACTION_ARM = "ARM"
        const val ACTION_CALL_STARTED = "CALL_STARTED"
        const val ACTION_CALL_ENDED = "CALL_ENDED"
        const val ACTION_TEST = "TEST"
        private const val CHANNEL_ID = "call_recorder"
        private const val NOTIFICATION_ID = 4107

        fun recordingDirectory(context: Context): File {
            val base = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.filesDir
            return File(base, "CallRecorder").apply { mkdirs() }
        }
    }

    private var recorder: MediaRecorder? = null
    private var testMode = false

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, notification("Siap menunggu panggilan"))
        when (intent?.action) {
            ACTION_CALL_STARTED -> startRecording(false)
            ACTION_CALL_ENDED -> stopRecording()
            ACTION_TEST -> startRecording(true)
            ACTION_ARM -> Unit
        }
        return START_NOT_STICKY
    }

    private fun startRecording(isTest: Boolean) {
        if (recorder != null) return
        testMode = isTest
        val dir = recordingDirectory(this)
        val stamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
        val file = File(dir, "${if (isTest) "test_" else "call_"}$stamp.m4a")

        try {
            val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(this) else MediaRecorder()
            r.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioEncodingBitRate(96000)
            r.setAudioSamplingRate(44100)
            r.setOutputFile(file.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            updateNotification("Sedang merekam")

            if (isTest) {
                android.os.Handler(mainLooper).postDelayed({ stopRecording() }, 5000)
            }
        } catch (e: Exception) {
            try { recorder?.release() } catch (_: Exception) {}
            recorder = null
            updateNotification("Tidak dapat memulai recorder: ${e.javaClass.simpleName}")
        }
    }

    private fun stopRecording() {
        val r = recorder ?: return
        try { r.stop() } catch (_: Exception) {}
        try { r.reset() } catch (_: Exception) {}
        try { r.release() } catch (_: Exception) {}
        recorder = null
        updateNotification("Siap menunggu panggilan")
        testMode = false
    }

    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Call Recorder", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun notification(text: String): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Local Call Recorder")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setOngoing(true)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("Local Call Recorder")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setOngoing(true)
                .build()
        }
    }

    private fun updateNotification(text: String) {
        getSystemService(NotificationManager::class.java)?.notify(NOTIFICATION_ID, notification(text))
    }
}
