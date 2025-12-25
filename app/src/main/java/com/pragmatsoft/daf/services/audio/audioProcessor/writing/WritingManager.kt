package com.pragmatsoft.daf.services.audio.audioProcessor.writing

import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.ArrayBlockingQueue

class WritingManager(private var sampleRate: Int) {

    val isActiveFlow = MutableStateFlow(false)
    var activatedTimestamp: Long? = null

    private val writingQueue = ArrayBlockingQueue<ByteArray>(200)

    fun start(filePath: String) {
        val encoder = AACEncoder(
            sampleRate = sampleRate,
            channelCount = 1,
            bitrate = 64000,
            outputPath = filePath
        )
        encoder.start()


        Thread {
            try {
                while (isActiveFlow.value || !writingQueue.isEmpty()) {
                    val pcm = writingQueue.poll()
                    if (pcm != null) {
                        encoder.encode(pcm)
                    } else {
                        Thread.sleep(2)
                    }
                }
            } finally {
                encoder.stop()
                stop()
            }
        }.start()

        isActiveFlow.value = true
        activatedTimestamp = System.currentTimeMillis()
    }

    fun stop() {
        isActiveFlow.value = false
        activatedTimestamp = null
        writingQueue.clear()
    }

    fun putIfActive(value: ByteArray) {
        if (isActiveFlow.value) {
            writingQueue.offer(value)
        }
    }

    fun setSampleRate(value: Int) {
        this.sampleRate = value
    }
}