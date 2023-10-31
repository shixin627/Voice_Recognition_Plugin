package com.skaiwalk.voice_recognition.service

import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.skaiwalk.voice_recognition.VoiceRecognitionCallback
import java.util.*

private const val LOG_TAG = "BTVoice"

class BluetoothVoiceRecognition {
    private var headset: BluetoothHeadset? = null
    private var device: BluetoothDevice? = null
    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var targetAddress: String = ""
    private var targetName: String = ""
    private var recognitionIntent: Boolean = false
    private lateinit var voiceRecognitionCallback: VoiceRecognitionCallback

    fun doInit(context: Context, callback: VoiceRecognitionCallback) {
        // Register a receiver to listen for Bluetooth headset events
        val filter = IntentFilter()
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
        filter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)

        context.registerReceiver(mReceiver, filter)
        voiceRecognitionCallback = callback
    }

    fun deInit(context: Context) {
        stopVoiceRecognition()
        context.unregisterReceiver(mReceiver)
    }

    private fun getBluetoothDeviceByAddress(address: String): BluetoothDevice? {
        val pairedDevices: Set<BluetoothDevice> = adapter.bondedDevices
        Log.d(LOG_TAG, "[getBluetoothDeviceByAddress] pairedDevices = $pairedDevices")
        for (device in pairedDevices) {
            if (device.address.equals(address)) {
                return device
            }
        }
        return null
    }

    private fun getBluetoothDeviceByName(name: String): BluetoothDevice? {
        val pairedDevices: Set<BluetoothDevice> = adapter.bondedDevices
        Log.d(LOG_TAG, "[getBluetoothDeviceByName] pairedDevices = $pairedDevices")
        for (device in pairedDevices) {
            if (device.name.equals(name)) {
                return device
            }
        }
        return null
    }

    fun pairBluetoothDeviceByName(name: String) {
        targetName = name
        val audioDevice = getBluetoothDeviceByName(name)
        if (audioDevice == null) {
            discoveryDevice()
        } else {
            targetAddress = audioDevice.address
            voiceRecognitionCallback.setTargetAddress(targetAddress)
        }
    }

    fun startVoiceRecognition(context: Context, deviceAddress: String): Boolean {
        if (deviceAddress.isEmpty()) {
            Log.d(LOG_TAG, "[startVoiceRecognition] No address")
            return false
        }
        checkAdapter()
        device = adapter.getRemoteDevice(deviceAddress)
        // Get the BluetoothHeadset proxy object
        adapter.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                headset = proxy as BluetoothHeadset
                // Start voice recognition on the Bluetooth device
                headset!!.startVoiceRecognition(device)
                setRecognitionState(true)
                Log.d(LOG_TAG, "Start voice recognition on the Bluetooth device(${device?.name})")
            }

            override fun onServiceDisconnected(profile: Int) {
                if (headset != null) {
                    headset!!.stopVoiceRecognition(device)
                    setRecognitionState(false)
                    Log.d(LOG_TAG, "Stop voice recognition")
                    headset = null
                }

            }
        }, BluetoothProfile.HEADSET)
        return true
    }

    fun stopVoiceRecognition() {
        headset?.stopVoiceRecognition(device)
        adapter.closeProfileProxy(BluetoothProfile.HEADSET, headset)
    }

    private fun checkAdapter() {
        // Check if Bluetooth is enabled
        if (!adapter.isEnabled) {
            return
        }
    }

    private fun discoveryDevice() {
        if (adapter.isDiscovering) {
            adapter.cancelDiscovery()
        }
        Log.d(LOG_TAG, "------- Start Discovery -------")
        adapter.startDiscovery()
    }

    private fun setRecognitionState(state: Boolean){
        voiceRecognitionCallback.setRecognitionState(state)
    }
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun pairDevice(device: BluetoothDevice): Boolean {
        return try {
            device.createBond()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            Log.d(LOG_TAG, "------- Pair Device -------")
        }
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.KITKAT)
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED == action) {
                val state = intent.getIntExtra(
                    BluetoothHeadset.EXTRA_STATE,
                    BluetoothHeadset.STATE_DISCONNECTED
                )
                if (state == BluetoothHeadset.STATE_CONNECTED) {
                    val connectedDevice =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                            ?: return
                    if (connectedDevice != device) {
//                        device = connectedDevice
                    } else {
                        // The target device has connected
                        Log.d(LOG_TAG, "The target device ${device?.name} has connected")
                    }
                }
            } else if (BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED == action) {
                val state = intent.getIntExtra(
                    BluetoothHeadset.EXTRA_STATE,
                    BluetoothHeadset.STATE_AUDIO_DISCONNECTED
                )
                if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                    // Voice recognition has started successfully
                    setRecognitionState(true)
                    Log.d(LOG_TAG, "Voice recognition has started successfully")
                } else if (state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
                    // Voice recognition has ended
                    setRecognitionState(false)
                    Log.d(LOG_TAG, "Voice recognition has ended")
                }
            } else if (BluetoothDevice.ACTION_FOUND == action) {
                val scannedDevice =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        ?: return
                val derp = scannedDevice.name + " - " + scannedDevice.address
                Log.d(LOG_TAG, derp)
                if (scannedDevice.name == targetName) {
                    Log.d(LOG_TAG, "------- Target Discovered -------")
                    targetAddress = scannedDevice.address
                    if (!adapter.bondedDevices.contains(scannedDevice)) {
                        val paired = pairDevice(scannedDevice)
                        if (paired) {
                            voiceRecognitionCallback.setTargetAddress(targetAddress)
                        }
                    }
                } else if (scannedDevice.address == targetAddress) {
                    if (!adapter.bondedDevices.contains(scannedDevice)) {
                        val paired = pairDevice(scannedDevice)
                    }
                }
            }

        }
    }


}
