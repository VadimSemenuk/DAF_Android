package com.pragmatsoft.daf.services.audio.audioProcessor

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Process
import android.util.Log
import com.pragmatsoft.daf.services.audio.audioProcessor.writing.WritingManager
import com.pragmatsoft.daf.utils.toByteArray
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class AudioProcessor @Inject constructor() {
    var isActiveFlow = MutableStateFlow(false)

    @Volatile
    private var sampleRate = 44100

    private var inputPreferredDevice: AudioDeviceInfo? = null
    private var outputPreferredDevice: AudioDeviceInfo? = null

    @Volatile
    private var gain = 1

    private val delayBuffer = DelayBuffer(sampleRate)
    val writingManager = WritingManager(sampleRate)

    @SuppressLint("MissingPermission")
    fun start() {
        if (isActiveFlow.value) return
        isActiveFlow.value = true

        val recorder = initAudioRecord()
        val player = initAudioPlayer()

        Thread {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)

            val inBuf = ShortArray(1024)
            val outBuf = ShortArray(1024)

            recorder.startRecording()
            player.play()

            try {
                while (isActiveFlow.value) {
                    val readCount = recorder.read(inBuf, 0, inBuf.size)
                    if (readCount <= 0) continue

                    for (i in 0 until readCount) {
                        val sample = (inBuf[i] * gain)
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                            .toShort()
                        delayBuffer.push(sample)
                        outBuf[i] = delayBuffer.pop()
                    }

                    player.write(outBuf, 0, readCount)

                    writingManager.putIfActive(outBuf.toByteArray())
                }
            } finally {
                releaseAudioRecorder(recorder)
                releaseAudioPlayer(player)
                stop()
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    private fun initAudioRecord(): AudioRecord {
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val recorder = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .build()

        recorder.preferredDevice = inputPreferredDevice

        return recorder
    }

    private fun releaseAudioRecorder(recorder: AudioRecord) {
        recorder.stop()
        recorder.release()
    }

    private fun initAudioPlayer(): AudioTrack {
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val player = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
//                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        player.preferredDevice = outputPreferredDevice

        return player
    }

    private fun releaseAudioPlayer(player: AudioTrack) {
        player.stop()
        player.release()
    }

    fun stop() {
        writingManager.stop()
        isActiveFlow.value = false
    }

    fun setSampleRate(value: Int) {
        sampleRate = value
        delayBuffer.setSampleRate(value)
        writingManager.setSampleRate(value)
    }

    fun setDelay(ms: Int) {
        delayBuffer.setDelay(ms)
    }

    fun setGain(value: Int) {
        gain = value
    }

    fun setInputPreferredDevice(device: AudioDeviceInfo?) {
        inputPreferredDevice = device
    }

    fun setOutputPreferredDevice(device: AudioDeviceInfo?) {
        outputPreferredDevice = device
    }
}