package com.skaiwalk.voice_recognition

interface VoiceRecognitionCallback {
    fun setRecognitionState(state: Boolean)
    fun setTargetAddress(address: String)
}
