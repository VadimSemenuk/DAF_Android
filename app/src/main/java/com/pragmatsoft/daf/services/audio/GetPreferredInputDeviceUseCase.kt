package com.pragmatsoft.daf.services.audio

import android.media.AudioDeviceInfo
import com.pragmatsoft.daf.data.local.DataStoreRepository
import com.pragmatsoft.daf.utils.AudioHelper
import com.pragmatsoft.daf.utils.getInputDevices
import javax.inject.Inject

class GetPreferredInputDeviceUseCase @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private var audioHelper: AudioHelper
) {
    operator fun invoke(): AudioDeviceInfo? {
        var device: AudioDeviceInfo? = null

        val deviceId = dataStoreRepository.getInputDeviceId()
        if (deviceId != null) {
            device = audioHelper.getInputDeviceById(deviceId)
        }

        // if device is not selected or selected device is not connected try to find preferred device
        if (device == null) {
            val devices = audioHelper.audioManager.getInputDevices()
            val preferredDeviceTypes = listOf(
                AudioDeviceInfo.TYPE_BUILTIN_MIC,
                AudioDeviceInfo.TYPE_TELEPHONY
            )
            for (preferredDeviceType in preferredDeviceTypes) {
                val d = devices.find { it.type == preferredDeviceType }
                if (d != null) {
                    device = d
                    break
                }
            }
        }

        return device
    }
}