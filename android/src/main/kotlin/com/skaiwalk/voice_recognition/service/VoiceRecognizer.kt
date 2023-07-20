package com.skaiwalk.voice_recognition.service

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log

class VoiceRecognizer(private var recognizer: SpeechRecognizer, var intent: Intent) :
    RecognitionListener {
    var resultText = ""
    private val handler = Handler()
    private var idleCount = 0
    override fun onResults(results: Bundle) {
        val resList: List<String>? =
            results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val sb = StringBuffer()
        for (res in resList!!) {
            sb.append(res)
            break
        }
        Log.d("RECOGNIZER", "onResults: $sb")
        resultText = sb.toString()
        if (resultText.isNotEmpty()) {
            MyObservable.instance.setData(resultText)
            idleCount = 0
        }

        if (sb.toString() == "下一步") {
            Log.d("RECOGNIZER", "那我就下一步")
        }
    }

    override fun onError(error: Int) {
        Log.d("RECOGNIZER", "Error Code: $error")
        idleCount++;
    }

    override fun onReadyForSpeech(params: Bundle) {
        Log.d("RECOGNIZER", "ready")
    }

    override fun onBeginningOfSpeech() {
        Log.d("RECOGNIZER", "beginning")
    }

    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray) {
        Log.d("RECOGNIZER", "onBufferReceived")
    }

    override fun onEndOfSpeech() {
        Log.d("RECOGNIZER", "onEndOfSpeech $idleCount")
        if (idleCount < 10) {
            handler.postDelayed({ // Do something after 1s = 1000ms
                Log.d("RECOGNIZER", "done")
                recognizer.startListening(intent)
            }, 1000)
        }
        else {
            MyObservable.instance.setData("/cmd/end")
            idleCount = 0
        }
    }

    override fun onPartialResults(partialResults: Bundle) {
        Log.d("RECOGNIZER", "onPartialResults$partialResults")
    }

    override fun onEvent(eventType: Int, params: Bundle) {
        Log.d("RECOGNIZER", "onPartialResults$params")
    }
}