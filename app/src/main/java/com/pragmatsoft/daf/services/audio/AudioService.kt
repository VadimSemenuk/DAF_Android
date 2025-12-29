package com.pragmatsoft.daf.services.audio

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Binder
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.pragmatsoft.daf.ui.MainActivity
import com.pragmatsoft.daf.R
import com.pragmatsoft.daf.data.local.DataStoreRepository
import com.pragmatsoft.daf.services.audio.audioProcessor.AudioProcessor
import com.pragmatsoft.daf.utils.AudioHelper
import com.pragmatsoft.daf.utils.getInputDevices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

data class Devices(
    val inputDevices: List<AudioDeviceInfo> = listOf(),
    val outputDevices: List<AudioDeviceInfo> = listOf(),
    val inputDeviceId: Int? = null,
    val outputDeviceId: Int? = null,
)

@AndroidEntryPoint
class AudioService : Service() {

    @Inject lateinit var audioProcessor: AudioProcessor
    @Inject lateinit var audioHelper: AudioHelper
    @Inject lateinit var dataStoreRepository: DataStoreRepository
    @Inject lateinit var getPreferredInputDeviceUseCase: GetPreferredInputDeviceUseCase
    @Inject lateinit var getPreferredOutputDeviceUseCase: GetPreferredOutputDeviceUseCase

    val isActiveFlow = MutableStateFlow(false)
    var activatedTimestamp: Long? = null

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    // region binder
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): AudioService = this@AudioService
    }
    // endregion

    // region livecycle
    override fun onCreate() {
        super.onCreate()

        updateSampleRate()

        observeSelectedDevice()
        observeDelay()
        observeGain()
        observeNoiseCancellation()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (audioHelper.audioManager.getInputDevices().isEmpty()) {
            return START_NOT_STICKY
        }

        startAudioProcessing()

        startForeground(
            AudioServiceNotificationManager.NOTIFICATION_ID,
            AudioServiceNotificationManager(this)
                .createNotificationChanel()
                .createNotification()
        )

        isActiveFlow.value = true
        activatedTimestamp = System.currentTimeMillis()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
        isActiveFlow.value = false
        stopAudioProcessing()
    }
    // endregion

    private fun updateSampleRate() {
        val sampleRateStr = audioHelper.audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
        val sampleRate = sampleRateStr?.let { str -> Integer.parseInt(str).takeUnless { it == 0 }} ?: 44100
        audioProcessor.setSampleRate(sampleRate)

//        val nativeFramesPerBuffer = audioHelper.audioManager.getProperty(
//            AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER
//        )?.toInt() ?: 256
//        audioProcessor.framesPerBuffer = nativeFramesPerBuffer
    }

    @SuppressLint("MissingPermission")
    private fun observeSelectedDevice() {
        scope.launch {
            combine(
                dataStoreRepository.inputDeviceIdFlow,
                dataStoreRepository.outputDeviceIdFlow,
                audioHelper.inputDevicesFlow,
                audioHelper.outputDevicesFlow
            ) { inputDeviceId, outputDeviceId, inputDevices, outputDevices ->
                Devices(
                    inputDeviceId = inputDeviceId,
                    outputDeviceId = outputDeviceId,
                    inputDevices = inputDevices,
                    outputDevices = outputDevices
                )
            }
                .collect {
                    refreshInputDevice()
                    refreshOutputDevice()

                    if (!isActiveFlow.value) {
                        return@collect
                    }

                    if (it.outputDevices.isEmpty()) {
                        stop()
                        return@collect
                    }

                    refreshBluetoothSCO()
                    restartAudioProcessor()
                }
        }
    }

    private fun observeDelay() {
        scope.launch {
            dataStoreRepository.delayFlow.collect {
                audioProcessor.setDelay(it)
            }
        }
    }

    private fun observeGain() {
        scope.launch {
            dataStoreRepository.gainFlow.collect {
                audioProcessor.setGain(it)
            }
        }
    }

    private fun observeNoiseCancellation() {
        scope.launch {
            dataStoreRepository.isNoiseCancellationOn.collect {
            }
        }
    }

    private fun startAudioProcessing() {
        audioHelper.audioManager.mode = AudioManager.MODE_NORMAL
        refreshBluetoothSCO()
        startAudioProcessor()
    }

    private fun startAudioProcessor() {
        audioProcessor.start()
    }

    private fun stopAudioProcessing() {
        stopAudioProcessor()
        audioHelper.stopBluetoothSco()
    }

    private fun stopAudioProcessor() {
        audioProcessor.stop()
    }

    private val restartMutex = Mutex()

    @SuppressLint("MissingPermission")
    private suspend fun restartAudioProcessor() {
        if (!restartMutex.tryLock()) {
            return
        }

        try {
            stopAudioProcessor()
            delay(500)
            startAudioProcessor()
        } finally {
            restartMutex.unlock()
        }
    }

    private fun refreshInputDevice() {
        val preferredDevice = getPreferredInputDeviceUseCase()
        audioProcessor.setInputPreferredDevice(preferredDevice)
    }

    private fun refreshOutputDevice() {
        val preferredDevice = getPreferredOutputDeviceUseCase()
        audioProcessor.setOutputPreferredDevice(preferredDevice)
    }

    private fun refreshBluetoothSCO() {
        val preferredDevice = getPreferredOutputDeviceUseCase()

        if (preferredDevice?.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
            audioHelper.startBluetoothSco(preferredDevice)
        } else {
            audioHelper.stopBluetoothSco()
        }
    }

    fun start() {
        startForegroundService(getIntent(this))
    }

    fun stop() {
        if (!isActiveFlow.value) return
        isActiveFlow.value = false

        stopAudioProcessing()
        stopService()
    }

    private fun stopService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, AudioService::class.java)
        }

        fun bind(context: Context, connection: ServiceConnection) {
            context.bindService(getIntent(context), connection, BIND_AUTO_CREATE)
        }

        fun unbind(context: Context, connection: ServiceConnection) {
            context.unbindService(connection)
        }
    }
}

class AudioServiceNotificationManager(private val context: Context) {
    fun createNotificationChanel(): AudioServiceNotificationManager {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Audio Foreground Service Channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        return this
    }

    fun createNotification(): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java)
        openAppIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.resources.getString(R.string.speech_processing_is_active))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(openAppPendingIntent)
            .setSilent(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "AudioServiceNotificationChannel"
        const val NOTIFICATION_ID = 1
    }
}
