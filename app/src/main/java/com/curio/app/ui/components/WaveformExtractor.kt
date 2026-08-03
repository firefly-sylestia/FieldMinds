package com.curio.app.ui.components

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs

/**
 * Extracts PCM amplitude samples from an audio file for waveform rendering.
 *
 * Uses [MediaExtractor] + [MediaCodec] to decode AAC/M4A files to PCM,
 * then downsamples to `barCount` normalized amplitude values (0.0–1.0).
 *
 * Usage:
 *   val samples = WaveformExtractor.extract(filePath, barCount = 120)
 *   // samples is a FloatArray of normalized amplitudes
 */
object WaveformExtractor {

    /** Number of amplitude bars in the rendered waveform. */
    private const val DEFAULT_BAR_COUNT = 120

    /**
     * Extract amplitude samples from the audio file at [filePath].
     *
     * @param filePath Absolute path to the audio file.
     * @param barCount Target number of waveform bars.
     * @return FloatArray of size [barCount] with normalized values 0.0–1.0,
     *         or null if extraction fails.
     */
    fun extract(filePath: String, barCount: Int = DEFAULT_BAR_COUNT): FloatArray? {
        val file = File(filePath)
        if (!file.exists() || file.length() == 0L) return null

        return try {
            val pcmSamples = decodeToPcm(filePath) ?: return null
            downsample(pcmSamples, barCount)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Decode the audio file to raw PCM 16-bit samples using MediaExtractor + MediaCodec.
     */
    private fun decodeToPcm(filePath: String): ShortArray? {
        val extractor = MediaExtractor().apply {
            try { setDataSource(filePath) } catch (_: Exception) { release(); return null }
        }

        val trackIndex = (0 until extractor.trackCount).firstOrNull { i ->
            extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
        } ?: run { extractor.release(); return null }

        val format = extractor.getTrackFormat(trackIndex)
        val mime = format.getString(MediaFormat.KEY_MIME) ?: run { extractor.release(); return null }

        extractor.selectTrack(trackIndex)

        val codec = try {
            MediaCodec.createDecoderByType(mime).apply {
                configure(format, null, null, 0)
                start()
            }
        } catch (_: Exception) {
            extractor.release()
            return null
        }

        val samples = mutableListOf<Short>()
        val bufferInfo = MediaCodec.BufferInfo()
        var done = false

        while (!done) {
            val inputIndex = codec.dequeueInputBuffer(10_000)
            if (inputIndex >= 0) {
                val inputBuffer = codec.getInputBuffer(inputIndex) ?: break
                val sampleSize = extractor.readSampleData(inputBuffer, 0)
                if (sampleSize < 0) {
                    codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                } else {
                    codec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, 0)
                    extractor.advance()
                }
            }

            val outputIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000)
            when {
                outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> { /* ignore */ }
                outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> { /* loop */ }
                outputIndex >= 0 -> {
                    val outputBuffer = codec.getOutputBuffer(outputIndex)
                    if (outputBuffer != null && bufferInfo.size > 0) {
                        val pcmData = extractPcmShorts(outputBuffer, bufferInfo)
                        samples.addAll(pcmData.toList())
                    }
                    codec.releaseOutputBuffer(outputIndex, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        done = true
                    }
                }
            }
        }

        codec.stop()
        codec.release()
        extractor.release()

        return if (samples.isEmpty()) null else samples.toShortArray()
    }

    /**
     * Read PCM 16-bit samples from a ByteBuffer.
     *
     * MediaCodec decoders emit PCM as **little-endian** shorts (per the
     * platform's canonical PCM layout), but a fresh ByteBuffer defaults to
     * BIG_ENDIAN — reading shorts without flipping the order byte-swaps every
     * sample, turning a clean waveform into noise (the "broken visualizer"
     * symptom). Pin the buffer to LITTLE_ENDIAN before reading.
     */
    private fun extractPcmShorts(buffer: ByteBuffer, info: MediaCodec.BufferInfo): ShortArray {
        val count = info.size / 2  // 2 bytes per 16-bit sample
        val result = ShortArray(count)
        val dup = buffer.duplicate()
        dup.order(ByteOrder.LITTLE_ENDIAN)
        dup.position(info.offset)
        dup.limit(info.offset + info.size)
        for (i in 0 until count) {
            result[i] = dup.short
        }
        return result
    }

    /**
     * Downsample raw PCM samples to [barCount] normalized amplitude bars.
     *
     * For each bar, computes the peak absolute amplitude across all samples
     * in that bar's window, then normalizes to 0.0–1.0.
     */
    private fun downsample(samples: ShortArray, barCount: Int): FloatArray {
        if (samples.isEmpty() || barCount <= 0) return FloatArray(barCount)
        val result = FloatArray(barCount)
        val windowSize = (samples.size / barCount).coerceAtLeast(1)

        for (bar in 0 until barCount) {
            val start = bar * windowSize
            val end = (start + windowSize).coerceAtMost(samples.size)
            var peak = 0
            for (i in start until end) {
                val a = abs(samples[i].toInt())
                if (a > peak) peak = a
            }
            // Normalize to 0.0–1.0 (16-bit max = 32767)
            result[bar] = (peak / 32767f).coerceIn(0f, 1f)
        }
        return result
    }
}
