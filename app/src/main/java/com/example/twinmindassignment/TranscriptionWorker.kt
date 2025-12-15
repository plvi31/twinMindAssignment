package com.example.twinmindassignment

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import java.io.File

class TranscriptionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        // Audio file path
        val audioPath = inputData.getString(KEY_AUDIO_PATH)
            ?: return Result.failure()

        val audioFile = File(audioPath)
        if (!audioFile.exists()) return Result.failure()

        // Simulate API call (Whisper / Gemini)
        delay(3000)

        val transcript = """
            This is a mock transcription.
            The meeting discussed project updates,
            deadlines, and action items.
        """.trimIndent()

        // Save transcript locally (SharedPreferences for now)
        val prefs = applicationContext
            .getSharedPreferences("transcripts", Context.MODE_PRIVATE)

        prefs.edit()
            .putString("latest_transcript", transcript)
            .apply()

        return Result.success()
    }

    companion object {
        const val KEY_AUDIO_PATH = "audio_path"
    }
}
