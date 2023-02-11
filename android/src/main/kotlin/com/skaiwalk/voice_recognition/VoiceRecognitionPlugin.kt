package com.skaiwalk.voice_recognition

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.skaiwalk.voice_recognition.service.VoiceRecognizer
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import android.content.Intent
import android.util.Log
import com.skaiwalk.voice_recognition.service.BluetoothVoiceRecognition
import com.skaiwalk.voice_recognition.service.MyObservable
import java.util.*

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
private const val TRAGET_ADDRESS = "09:53:D2:D2:78:F8"

/** VoiceRecognitionPlugin */
class VoiceRecognitionPlugin : FlutterPlugin, MethodCallHandler,
    PluginRegistry.RequestPermissionsResultListener, ActivityAware, Observer {
    private lateinit var channel: MethodChannel

    private var currentActivity: Activity? = null

    private var audioPermissionGranted: Boolean = false

    private val recognitionIntent = Intent().putExtra(
        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
    )
    private lateinit var recognizer: SpeechRecognizer

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        this.channel = MethodChannel(flutterPluginBinding.binaryMessenger, "voice_recognition")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "startVoiceRecognition" -> {
                currentActivity?.let {
                    BluetoothVoiceRecognition(it).startVoiceRecognition(
                        TRAGET_ADDRESS
                    )
                }
                startRecognition()
                result.success("Start Voice Recognition")
            }
            "stopVoiceRecognition" -> {
                stopRecognition()
                result.success("Stop Voice Recognition")
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> {
                if (null != grantResults) {
                    audioPermissionGranted = grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED
                }
                return true
            }
        }
        return false
    }

    override fun onDetachedFromActivity() {
        currentActivity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        currentActivity = binding.activity
        binding.addRequestPermissionsResultListener(this)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        currentActivity = binding.activity
        binding.addRequestPermissionsResultListener(this)
        requestAudioPermission()

        recognizer = SpeechRecognizer.createSpeechRecognizer(currentActivity)
        recognizer?.setRecognitionListener(VoiceRecognizer(recognizer, recognitionIntent))

        MyObservable.instance.addObserver(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        currentActivity = null
    }

    private fun requestAudioPermission() {
        currentActivity?.let {
            ActivityCompat.requestPermissions(
                it,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        }
    }

    private fun checkAudioPermission() {
        audioPermissionGranted = currentActivity?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } ==
                PackageManager.PERMISSION_GRANTED
        if (!audioPermissionGranted) {
            requestAudioPermission()
        }
    }

    private fun startRecognition() {
        checkAudioPermission()
        recognizer?.startListening(recognitionIntent)
    }

    private fun stopRecognition() {
        recognizer?.stopListening()
    }

    override fun update(o: Observable?, arg: Any?) {
        Log.d("VoiceRecognitionPlugin", "MyObservable產生變化~${MyObservable.instance.textResult}")
        channel.invokeMethod("onReturnResult", MyObservable.instance.textResult)
    }
}
