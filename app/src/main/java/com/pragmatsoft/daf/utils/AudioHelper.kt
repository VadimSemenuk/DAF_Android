package com.pragmatsoft.daf.utils

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.pragmatsoft.daf.R
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlin.collections.filter

class AudioHelper @Inject constructor(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager

    val devicesListUpdatedFlow = audioManager
        .getDevicesFlow()
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            replay = 1
        )

    val inputDevicesFlow = devicesListUpdatedFlow
        .map { audioManager.getInputDevices() }

    val outputDevicesFlow = devicesListUpdatedFlow
        .map { audioManager.getOutputDevices() }

    fun getInputDeviceById(id: Int): AudioDeviceInfo? {
        return audioManager.getInputDeviceById(id)
    }

    fun getOutputDeviceById(id: Int): AudioDeviceInfo? {
        return audioManager.getOutputDeviceById(id)
    }

    fun startBluetoothSco(device: AudioDeviceInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManager.setCommunicationDevice(device)
        } else {
            audioManager.isBluetoothScoOn = true
            audioManager.startBluetoothSco()
        }
    }

    fun stopBluetoothSco() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManager.clearCommunicationDevice()
        } else {
            audioManager.stopBluetoothSco()
            audioManager.isBluetoothScoOn = false
        }
    }

    fun toString(audioDeviceInfo: AudioDeviceInfo): String {
        val id = audioDeviceInfo.id
        val productName = audioDeviceInfo.productName

        return when (audioDeviceInfo.type) {
            AudioDeviceInfo.TYPE_BUILTIN_MIC -> {
                val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) audioDeviceInfo.address else ""
                "$id: ${context.getString(R.string.audio_device_type_builtin_mic)} (${address}) (${productName})"
            }
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> {
                "$id: ${context.getString(R.string.audio_device_type_bluetooth_ll)} (${productName})"
            }
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> {
                "$id: ${context.getString(R.string.audio_device_type_bluetooth_hq)} (${productName})"
            }
            AudioDeviceInfo.TYPE_WIRED_HEADSET -> {
                "$id: ${context.getString(R.string.audio_device_type_wiered_headset)} (${productName})"
            }
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> {
                "$id: ${context.getString(R.string.audio_device_type_wiered_headphones)} (${productName})"
            }
            AudioDeviceInfo.TYPE_TELEPHONY -> {
                "$id: ${context.getString(R.string.audio_device_type_telephony)}"
            }
            else -> {
                "$id: ${context.getString(R.string.audio_device_type)}"
            }
        }
    }
}

fun AudioManager.getDevicesFlow(): Flow<Unit> = callbackFlow {
    val callback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
            trySend(Unit)
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) {
            trySend(Unit)
        }
    }

    registerAudioDeviceCallback(callback, null)

    awaitClose {
        unregisterAudioDeviceCallback(callback)
    }
}

fun AudioManager.getInputDevices(): List<AudioDeviceInfo> {
    val targetTypes = listOf(
        AudioDeviceInfo.TYPE_BUILTIN_MIC,
        AudioDeviceInfo.TYPE_TELEPHONY,
        AudioDeviceInfo.TYPE_WIRED_HEADSET,
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
    )

    return getDevices(AudioManager.GET_DEVICES_INPUTS)
        .toList()
        .filter { targetTypes.contains(it.type) }
}

fun AudioManager.getOutputDevices(): List<AudioDeviceInfo> {
    val targetTypes = listOf(
        AudioDeviceInfo.TYPE_WIRED_HEADSET,
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO
    )

    return getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        .toList()
        .filter { targetTypes.contains(it.type) }
}


fun AudioManager.getInputDeviceById(id: Int): AudioDeviceInfo? {
    return getDevices(AudioManager.GET_DEVICES_INPUTS)
        .firstOrNull { it.id == id }
}

fun AudioManager.getOutputDeviceById(id: Int): AudioDeviceInfo? {
    return getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        .firstOrNull { it.id == id }
}
