package com.pragmatsoft.daf.services.audio.audioProcessor

import android.Manifest
import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.PlaybackParams
import android.os.Process
import androidx.annotation.RequiresPermission
import com.pragmatsoft.daf.services.audio.audioProcessor.writing.WritingManager
import com.pragmatsoft.daf.utils.toByteArray
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

//    val periods = 2
//    val bytesPerFrame = 2
//    val trackFrames = framesPerBuffer * periods
//    val recordBufferSize = trackFrames * bytesPerFrame
//    val trackBufferSize = recordBufferSize * 2
//    val bytesPerTick = framesPerBuffer * bytesPerFrame

class AudioProcessor @Inject constructor() {
    var isActiveFlow = MutableStateFlow(false)

    private var sampleRate = 44100

    private var inputPreferredDevice: AudioDeviceInfo? = null
    private var outputPreferredDevice: AudioDeviceInfo? = null

    private var gain = 0

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
                    val n = recorder.read(inBuf, 0, inBuf.size)
                    if (n <= 0) continue

                    for (i in 0 until n) {
                        val sample = (inBuf[i] * gain).toShort()
//                        inBuf[i] = sample
                        delayBuffer.push(sample)
                        outBuf[i] = delayBuffer.pop()
                    }

                    player.write(outBuf, 0, n)

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

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
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

//        val playbackParams = PlaybackParams()
//        playbackParams.pitch = 0.9F
//        player.playbackParams = playbackParams

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