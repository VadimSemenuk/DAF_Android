package com.pragmatsoft.daf.services.audio.audioProcessor

class DelayBuffer(
    private var sampleRate: Int,
    seconds: Int = 1
) {
    private var delay = 0
    private val maxSamples = sampleRate * seconds
    private val buffer = ShortArray(maxSamples)

    @Volatile private var readPos = 0
    @Volatile private var writePos = 0

    fun updateReadPos() {
        val delaySamples = ((delay * sampleRate) / 1000).coerceIn(1, maxSamples - 1)
        readPos = (writePos - delaySamples + maxSamples) % maxSamples
    }

    fun setDelay(value: Int) {
        this.delay = value
        updateReadPos()
    }

    fun setSampleRate(value: Int) {
        this.sampleRate = value
        updateReadPos()
    }

    fun push(value: Short) {
        buffer[writePos] = value
        writePos = (writePos + 1) % maxSamples
    }

    fun pop(): Short {
        val out = buffer[readPos]
        readPos = (readPos + 1) % maxSamples
        return out
    }
}
