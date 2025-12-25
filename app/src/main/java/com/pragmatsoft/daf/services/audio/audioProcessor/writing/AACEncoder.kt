package com.pragmatsoft.daf.services.audio.audioProcessor.writing

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer

class AACEncoder(
    private val sampleRate: Int = 44100,
    private val channelCount: Int = 1,
    private val bitrate: Int = 64000,
    private val outputPath: String
) {
    private lateinit var codec: MediaCodec
    private lateinit var muxer: MediaMuxer
    private var trackIndex = -1
    private var muxerStarted = false

    fun start() {
        codec = MediaCodec.createEncoderByType("audio/mp4a-latm")

        val format = MediaFormat.createAudioFormat(
            MediaFormat.MIMETYPE_AUDIO_AAC,
            sampleRate,
            channelCount
        )
        format.setInteger(
            MediaFormat.KEY_AAC_PROFILE,
            MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)

        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.start()

        muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    }

    fun encode(pcm: ByteArray) {
        val inputBufferIndex = codec.dequeueInputBuffer(10000)
        if (inputBufferIndex >= 0) {
            val buffer = codec.getInputBuffer(inputBufferIndex)
            buffer?.clear()
            buffer?.put(pcm)

            codec.queueInputBuffer(
                inputBufferIndex,
                0,
                pcm.size,
                System.nanoTime() / 1000,
                0
            )
        }

        drainEncoder()
    }

    private fun drainEncoder() {
        val bufferInfo = MediaCodec.BufferInfo()

        while (true) {
            val outputIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
            if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break
            } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (muxerStarted) throw RuntimeException("format changed twice")
                trackIndex = muxer.addTrack(codec.outputFormat)
                muxer.start()
                muxerStarted = true
            } else if (outputIndex >= 0) {
                val encodedData = codec.getOutputBuffer(outputIndex)
                    ?: throw RuntimeException("encoderOutputBuffer $outputIndex was null")

                if (bufferInfo.size != 0) {
                    if (!muxerStarted) {
                        throw RuntimeException("muxer hasn't started")
                    }
                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)

                    muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                }

                codec.releaseOutputBuffer(outputIndex, false)
            }
        }
    }

    fun stop() {
        val inputBufferIndex = codec.dequeueInputBuffer(10000)
        if (inputBufferIndex >= 0) {
            codec.queueInputBuffer(
                inputBufferIndex,
                0,
                0,
                System.nanoTime() / 1000,
                MediaCodec.BUFFER_FLAG_END_OF_STREAM
            )
        }
        drainEncoder()

        codec.stop()
        codec.release()

        if (muxerStarted) {
            muxer.stop()
            muxer.release()
        }
    }
}