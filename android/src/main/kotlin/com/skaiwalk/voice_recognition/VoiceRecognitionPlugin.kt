package com.skaiwalk.voice_recognition

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.skaiwalk.voice_recognition.service.BluetoothVoiceRecognition
import com.skaiwalk.voice_recognition.service.MyObservable
import com.skaiwalk.voice_recognition.service.VoiceRecognizer
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import java.util.*

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

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
    private lateinit var bluetoothVoiceRecognition: BluetoothVoiceRecognition

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        this.channel = MethodChannel(flutterPluginBinding.binaryMessenger, "voice_recognition")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        val arguments = call.arguments
        when (call.method) {
            "startVoiceRecognition" -> {
                val bluetoothAddress = (arguments as Map<*, *>)["bluetoothAddress"] as String
                val started = bluetoothVoiceRecognition.startVoiceRecognition(
                    bluetoothAddress
                )
                if (started) {
                    startRecognition()
                    result.success("[onMethodCall]Start Voice Recognition")
                } else {
                    result.success("[onMethodCall]Bluetooth audio device has not prepared to start voice recognition")
                }
            }
            "stopVoiceRecognition" -> {
                bluetoothVoiceRecognition.stopVoiceRecognition()
                stopRecognition()
                result.success("[onMethodCall]Stop Voice Recognition")
            }
            "pairBluetoothDeviceByName" -> {
                val bluetoothName = (arguments as Map<*, *>)["bluetoothName"] as String
                bluetoothVoiceRecognition.pairBluetoothDeviceByName(bluetoothName)
                result.success("[onMethodCall]Pair Bluetooth Device By Name:$bluetoothName")
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> {
                audioPermissionGranted = grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                return true
            }
        }
        return false
    }

    override fun onDetachedFromActivity() {
        bluetoothVoiceRecognition.deInit()
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
        recognizer.setRecognitionListener(VoiceRecognizer(recognizer, recognitionIntent))

        MyObservable.instance.addObserver(this)

        bluetoothVoiceRecognition = BluetoothVoiceRecognition(currentActivity!!, channel)
        bluetoothVoiceRecognition.init()
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
        recognizer.startListening(recognitionIntent)
    }

    private fun stopRecognition() {
        recognizer.stopListening()
    }

    override fun update(o: Observable?, arg: Any?) {
        Log.d("VoiceRecognitionPlugin", "MyObservable changed~${MyObservable.instance.textResult}")
        channel.invokeMethod("onReturnResult", MyObservable.instance.textResult)
    }
}
