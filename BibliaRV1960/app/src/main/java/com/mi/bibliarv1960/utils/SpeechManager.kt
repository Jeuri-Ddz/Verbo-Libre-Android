package com.mi.bibliarv1960.utils

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeechManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var currentSpeed = 1.0f
    private var onVerseCompleteListener: ((Int) -> Unit)? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = Locale("es", "ES")
                tts?.language = locale
                tts?.setSpeechRate(currentSpeed)
                isInitialized = true
            }
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                val verseNum = utteranceId?.toIntOrNull() ?: -1
                onVerseCompleteListener?.invoke(verseNum)
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {}
        })
    }

    fun setOnVerseCompleteListener(listener: (Int) -> Unit) {
        onVerseCompleteListener = listener
    }

    fun setSpeed(speed: Float) {
        currentSpeed = speed
        tts?.setSpeechRate(speed)
    }

    fun speakVerse(verseNum: Int, text: String) {
        if (isInitialized) {
            val params = android.os.Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, verseNum.toString())
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, verseNum.toString())
        }
    }

    fun pause() {
        tts?.stop()
    }

    fun stop() {
        tts?.stop()
    }

    fun openSystemSettings() {
        val intent = Intent().apply {
            action = "com.android.settings.TTS_SETTINGS"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
