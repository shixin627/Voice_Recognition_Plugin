package com.skaiwalk.voice_recognition

import android.Manifest
import android.app.Activity
import android.content.Context
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
import java.util.Observable
import java.util.Observer

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

/** VoiceRecognitionPlugin */
class VoiceRecognitionPlugin : FlutterPlugin, MethodCallHandler,
    PluginRegistry.RequestPermissionsResultListener, ActivityAware, Observer {
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private var currentActivity: Activity? = null

    private var audioPermissionGranted: Boolean = false

    private val recognitionIntent = Intent().putExtra(
        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
    )
    private lateinit var recognizer: SpeechRecognizer
    private lateinit var bluetoothVoiceRecognition: BluetoothVoiceRecognition
    private var intentMode: Int = 0

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        this.channel = MethodChannel(flutterPluginBinding.binaryMessenger, "voice_recognition")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
        recognizer =
            SpeechRecognizer.createSpeechRecognizer(context)
        recognizer.setRecognitionListener(VoiceRecognizer(recognizer, recognitionIntent))
        MyObservable.instance.addObserver(this)

        bluetoothVoiceRecognition = BluetoothVoiceRecognition()
        bluetoothVoiceRecognition.doInit(context, recognitionCallback)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        val arguments = call.arguments
        when (call.method) {
            "startVoiceRecognition" -> {
                val arguments = arguments as Map<*, *>
                val mode = arguments["mode"] as Int
                val bluetoothAddress = arguments["bluetoothAddress"] as String

                if (!bluetoothVoiceRecognition.startVoiceRecognition(context, bluetoothAddress)) {
                    intentMode = 0
                    result.success(false)
                    return
                }

                intentMode = when (mode) {
                    1 -> {
                        startRecognition()
                        result.success(true)
                        1
                    }

                    2 -> {
                        result.success(true)
                        2
                    }

                    else -> {
                        result.success(false)
                        0
                    }
                }

            }

            "stopVoiceRecognition" -> {
                if (intentMode == 1) {
                    bluetoothVoiceRecognition.stopVoiceRecognition()
                    stopRecognition()
//                    result.success("Stop Voice Recognition to Text")
                } else if (intentMode == 2) {
                    bluetoothVoiceRecognition.stopVoiceRecognition()
//                    result.success("Stop Voice Recognition to sound")
                }
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
        bluetoothVoiceRecognition.deInit(binding.applicationContext)
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
        if (MyObservable.instance.textResult == "/cmd/end") {
            bluetoothVoiceRecognition.stopVoiceRecognition()
            stopRecognition()
            channel.invokeMethod("onReturnCmd", "/cmd/end")
            return
        }
        channel.invokeMethod("onReturnResult", MyObservable.instance.textResult)
    }

    private var recognitionCallback: VoiceRecognitionCallback = object : VoiceRecognitionCallback {
        override fun setRecognitionState(state: Boolean) {
            channel.invokeMethod("setRecognitionState", state)
        }

        override fun setTargetAddress(address: String) {
            channel.invokeMethod("targetAddress", address)
        }
    }
}
