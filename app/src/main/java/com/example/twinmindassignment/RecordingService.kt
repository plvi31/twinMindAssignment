package com.example.twinmindassignment

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import androidx.work.*

import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RecordingService : Service() {

    private var recorder: MediaRecorder? = null
    private lateinit var outputFile: String

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRecording()
            ACTION_STOP -> stopRecording()
        }
        return START_STICKY
    }

    private fun startRecording() {
        try {
            createNotificationChannel()
            startForeground(1, buildNotification("Recording..."))

            val dir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(dir, "REC_$timeStamp.m4a")
            outputFile = file.absolutePath

            recorder?.release()
            recorder = MediaRecorder()

            recorder?.apply {
                reset()
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile)
                prepare()
                start()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }


    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        startTranscription(outputFile)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TwinMind Recorder")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recording",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startTranscription(audioPath: String) {
        val data = workDataOf(
            TranscriptionWorker.KEY_AUDIO_PATH to audioPath
        )

        val request = OneTimeWorkRequestBuilder<TranscriptionWorker>()
            .setInputData(data)
            .build()

        WorkManager.getInstance(this).enqueue(request)
    }


    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "START_RECORDING"
        const val ACTION_STOP = "STOP_RECORDING"
        const val CHANNEL_ID = "recording_channel"
    }
}
