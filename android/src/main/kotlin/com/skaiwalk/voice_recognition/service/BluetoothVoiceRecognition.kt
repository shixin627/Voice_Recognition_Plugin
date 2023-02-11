package com.skaiwalk.voice_recognition.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log

private const val LOG_TAG = "BTVoice"
class BluetoothVoiceRecognition(private val context: Context) {
    private var headset: BluetoothHeadset? = null
    private var device: BluetoothDevice? = null
    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    fun startVoiceRecognition(deviceAddress: String) {
        // Get the target Bluetooth device
        device = adapter.getRemoteDevice(deviceAddress)
        // Register a receiver to listen for Bluetooth headset events
        val filter = IntentFilter()
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
        filter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
        context.registerReceiver(mReceiver, filter)

        // Get the BluetoothHeadset proxy object
        adapter.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                headset = proxy as BluetoothHeadset

                // Start voice recognition on the Bluetooth device
                headset!!.startVoiceRecognition(device)
            }

            override fun onServiceDisconnected(profile: Int) {
                headset = null
            }
        }, BluetoothProfile.HEADSET)
    }

    fun stopVoiceRecognition() {
        if (headset != null) {
            headset!!.stopVoiceRecognition(device)
            context.unregisterReceiver(mReceiver)
            adapter.closeProfileProxy(BluetoothProfile.HEADSET, headset)
            headset = null
        }
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
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
                    if (connectedDevice == device) {
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
                    Log.d(LOG_TAG, "Voice recognition has started successfully")
                }
            }
        }
    }

}
