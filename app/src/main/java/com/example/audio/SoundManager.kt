package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.game.NoOpSoundPlayer
import com.example.game.SoundEffect
import com.example.game.SoundPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * CompositionLocal providing global [SoundManager] access across all Compose screens
 * so every button click, tab switch, and navigation action produces consistent audio feedback.
 */
val LocalSoundManager = staticCompositionLocalOf<SoundPlayer> { NoOpSoundPlayer }

/**
 * Global SoundManager class handling synthesized sound effects for:
 * - Menu navigation ([SoundEffect.MENU_NAVIGATE])
 * - Button clicks & UI interactions ([SoundEffect.BUTTON_CLICK], [SoundEffect.UNLOCK_SUCCESS], [SoundEffect.ACTION_DENIED])
 * - In-game platformer events ([SoundEffect.JUMP], [SoundEffect.COIN], [SoundEffect.DASH], [SoundEffect.SPRING], [SoundEffect.HIT_OBSTACLE], [SoundEffect.STAGE_CLEAR])
 *
 * Works 100% offline with zero network dependencies, backed by Android [SoundPool]
 * and an [AudioTrack] fallback so sounds never silently drop while preloading.
 */
class SoundManager(context: Context) : SoundPlayer {

    private val appContext = context.applicationContext
    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var enabled: Boolean = true

    @Volatile
    private var masterVolume: Float = 1.0f

    @Volatile
    private var released: Boolean = false

    private val soundIds = ConcurrentHashMap<SoundEffect, Int>()
    private val loadedSounds = ConcurrentHashMap<Int, Boolean>()
    private val pcmCache = ConcurrentHashMap<SoundEffect, ShortArray>()

    private val gameAudioAttributes: AudioAttributes by lazy {
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    }

    @Volatile
    private var soundPool: SoundPool? = null

    init {
        initializeAudioBackend()
    }

    @Synchronized
    private fun initializeAudioBackend() {
        released = false
        if (soundPool == null) {
            soundPool = runCatching {
                SoundPool.Builder()
                    .setMaxStreams(8)
                    .setAudioAttributes(gameAudioAttributes)
                    .build()
                    .apply {
                        setOnLoadCompleteListener { _, sampleId, status ->
                            if (status == 0) {
                                loadedSounds[sampleId] = true
                            }
                        }
                    }
            }.getOrNull()
        }
        scope.launch {
            prepareAllEffects()
        }
    }

    private fun prepareAllEffects() {
        val pool = soundPool
        val cacheDir = File(appContext.cacheDir, "volt_dash_sfx").apply { mkdirs() }

        val generators: Map<SoundEffect, () -> ShortArray> = mapOf(
            SoundEffect.JUMP to ::synthJump,
            SoundEffect.COIN to ::synthCoin,
            SoundEffect.HIT_OBSTACLE to ::synthHitObstacle,
            SoundEffect.DASH to ::synthDash,
            SoundEffect.SPRING to ::synthSpring,
            SoundEffect.STAGE_CLEAR to ::synthStageClear,
            SoundEffect.BUTTON_CLICK to ::synthButtonClick,
            SoundEffect.MENU_NAVIGATE to ::synthMenuNavigate,
            SoundEffect.UNLOCK_SUCCESS to ::synthUnlockSuccess,
            SoundEffect.ACTION_DENIED to ::synthActionDenied
        )

        generators.forEach { (effect, synth) ->
            if (released) return@forEach
            runCatching {
                val pcm = synth()
                pcmCache[effect] = pcm
                if (pool != null) {
                    val outFile = File(cacheDir, "sfx_v3_${effect.name.lowercase()}.wav")
                    if (!outFile.exists() || outFile.length() < 128L) {
                        writeWavFile(outFile, pcm, SAMPLE_RATE)
                    }
                    val id = pool.load(outFile.absolutePath, 1)
                    if (id > 0) {
                        soundIds[effect] = id
                    }
                }
            }
        }
    }

    override fun play(effect: SoundEffect) {
        if (!enabled) return
        if (released) {
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            initializeAudioBackend()
        }

        val volume = (volumeFor(effect) * masterVolume).coerceIn(0f, 1f)
        if (volume <= 0.001f) return

        val pool = soundPool
        val sampleId = soundIds[effect]
        if (pool != null && sampleId != null && loadedSounds[sampleId] == true) {
            val rate = when (effect) {
                SoundEffect.COIN, SoundEffect.BUTTON_CLICK -> 0.98f + Random.nextFloat() * 0.04f
                else -> 1.0f
            }
            val streamId = runCatching {
                pool.play(sampleId, volume, volume, 1, 0, rate)
            }.getOrDefault(0)
            if (streamId != 0) return
        }

        // Fallback to direct AudioTrack playback if SoundPool hasn't finished preloading
        val pcm = pcmCache[effect] ?: return
        scope.launch {
            if (!enabled || released) return@launch
            playViaAudioTrack(pcm, volume)
        }
    }

    private fun playViaAudioTrack(pcm: ShortArray, volume: Float) {
        runCatching {
            val rawBytes = pcm.size * 2
            val minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(rawBytes)
            val bufferBytes = maxOf(rawBytes, minBuf)
            val paddedSamples = if (bufferBytes > rawBytes) {
                ShortArray(bufferBytes / 2).also { pcm.copyInto(it) }
            } else {
                pcm
            }
            val track = AudioTrack.Builder()
                .setAudioAttributes(gameAudioAttributes)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(paddedSamples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.setVolume(volume)
            track.write(paddedSamples, 0, paddedSamples.size)
            track.play()
            val durationMs = ((pcm.size.toFloat() / SAMPLE_RATE) * 1000f).toLong() + 45L
            Thread.sleep(durationMs)
            track.stop()
            track.release()
        }
    }

    private fun volumeFor(effect: SoundEffect): Float = when (effect) {
        SoundEffect.BUTTON_CLICK -> 0.38f
        SoundEffect.MENU_NAVIGATE -> 0.44f
        SoundEffect.UNLOCK_SUCCESS -> 0.60f
        SoundEffect.ACTION_DENIED -> 0.48f
        SoundEffect.COIN -> 0.56f
        SoundEffect.JUMP -> 0.50f
        SoundEffect.DASH -> 0.48f
        SoundEffect.SPRING -> 0.54f
        SoundEffect.HIT_OBSTACLE -> 0.68f
        SoundEffect.STAGE_CLEAR -> 0.66f
    }

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        if (!enabled) {
            runCatching { soundPool?.autoPause() }
        }
    }

    override fun isEnabled(): Boolean = enabled

    override fun setMasterVolume(volume: Float) {
        this.masterVolume = volume.coerceIn(0f, 1f)
    }

    override fun release() {
        released = true
        scope.cancel()
        runCatching { soundPool?.release() }
        soundPool = null
        soundIds.clear()
        loadedSounds.clear()
        pcmCache.clear()
    }

    private fun envelope(index: Int, total: Int): Float {
        val attackSamples = (SAMPLE_RATE * 0.004f).coerceAtLeast(1f)
        val attack = (index / attackSamples).coerceIn(0f, 1f)
        val releaseSamples = (SAMPLE_RATE * 0.012f).coerceAtLeast(1f)
        val release = ((total - 1 - index) / releaseSamples).coerceIn(0f, 1f)
        return attack * release
    }

    private fun synthButtonClick(): ShortArray {
        val durationSec = 0.042f
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val freq = 720.0 + 340.0 * t
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val wave = 0.72 * sin(phase) + 0.28 * (if (sin(phase) >= 0.0) 1.0 else -1.0)
            val env = exp(-t * 8.5f) * envelope(i, count)
            samples[i] = (wave * env * 0.52 * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    private fun synthMenuNavigate(): ShortArray {
        val durationSec = 0.082f
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val freq = if (t < 0.45f) 523.25 else 783.99
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val triangle = (2.0 / PI) * asin(sin(phase))
            val env = exp(-t * 5.0f) * envelope(i, count)
            samples[i] = ((0.6 * sin(phase) + 0.4 * triangle) * env * 0.55 * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    private fun synthUnlockSuccess(): ShortArray {
        val durationSec = 0.30f
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        val notes = doubleArrayOf(587.33, 739.99, 880.00, 1174.66) // D5, F#5, A5, D6
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val noteIdx = ((t * notes.size).toInt()).coerceIn(0, notes.lastIndex)
            val localT = (t * notes.size) - noteIdx
            phase += 2.0 * PI * notes[noteIdx] / SAMPLE_RATE
            val wave = 0.7 * sin(phase) + 0.3 * (if (sin(phase * 2.0) >= 0.0) 0.5 else -0.5)
            val env = exp(-localT * 3.4f) * (1f - t * 0.25f) * envelope(i, count)
            samples[i] = (wave * env * 0.62 * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    private fun synthActionDenied(): ShortArray {
        val durationSec = 0.12f
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val pulse = if (t < 0.42f || t > 0.56f) 1.0f else 0.15f
            val freq = 185.0 - 45.0 * t
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val square = if (sin(phase) >= 0.0) 1.0 else -1.0
            val env = pulse * (1f - t * 0.45f) * envelope(i, count)
            samples[i] = ((0.55 * square + 0.45 * sin(phase)) * env * 0.50 * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    private fun synthJump(): ShortArray {
        val durationSec = 0.12f
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val freq = 250.0 + 440.0 * t * t
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val square = if (sin(phase) >= 0.0) 1.0 else -1.0
            val triangle = (2.0 / PI) * asin(sin(phase))
            val env = (1f - t) * exp(-t * 2.2f) * envelope(i, count)
            samples[i] = ((0.55 * square + 0.45 * triangle) * env * 0.55 * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    private fun synthCoin(): ShortArray {
        val durationSec = 0.19f
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val freq = if (t < 0.28f) 987.77 else 1318.51
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val wave = 0.72 * sin(phase) + 0.28 * if (sin(phase * 2.0) >= 0.0) 0.5 else -0.5
            val env = exp(-(t - (if (t < 0.28f) 0f else 0.28f)) * 7.0f) * (1f - t * 0.35f) * envelope(i, count)
            samples[i] = (wave * env * 0.6 * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    private fun synthHitObstacle(): ShortArray {
        val durationSec = 0.25f
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        var phase = 0.0
        val rng = Random(42)
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val freq = 260.0 - 195.0 * t
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val saw = ((phase / PI) % 2.0) - 1.0
            val noise = (rng.nextDouble() * 2.0 - 1.0) * (1.0 - t)
            val env = exp(-t * 5.2f) * envelope(i, count)
            val mix = 0.62 * saw + 0.38 * noise
            samples[i] = (mix * env * 0.72 * Short.MAX_VALUE).toInt().coerceIn(
                Short.MIN_VALUE.toInt(),
                Short.MAX_VALUE.toInt()
            ).toShort()
        }
        return samples
    }

    private fun synthDash(): ShortArray {
        val durationSec = 0.12f
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        var phase = 0.0
        val rng = Random(99)
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val freq = 620.0 - 380.0 * t
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val tone = sin(phase)
            val whoosh = rng.nextDouble() * 2.0 - 1.0
            val env = exp(-t * 7.5f) * envelope(i, count)
            samples[i] = ((0.55 * tone + 0.45 * whoosh) * env * 0.55 * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    private fun synthSpring(): ShortArray {
        val durationSec = 0.18f
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val wobble = sin(t * 26f) * 35.0
            val freq = 260.0 + 680.0 * t + wobble
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val env = (1f - t) * envelope(i, count)
            samples[i] = (sin(phase) * env * 0.62 * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    private fun synthStageClear(): ShortArray {
        val durationSec = 0.42f
        val count = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(count)
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val noteIdx = ((t * notes.size).toInt()).coerceIn(0, notes.lastIndex)
            val localT = (t * notes.size) - noteIdx
            phase += 2.0 * PI * notes[noteIdx] / SAMPLE_RATE
            val square = if (sin(phase) >= 0.0) 1.0 else -1.0
            val mix = 0.65 * sin(phase) + 0.35 * square
            val env = exp(-localT * 3.0f) * (1f - t * 0.25f) * envelope(i, count)
            samples[i] = (mix * env * 0.62 * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    private fun writeWavFile(file: File, pcm: ShortArray, sampleRate: Int) {
        val dataSize = pcm.size * 2
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray())
            putInt(36 + dataSize)
            put("WAVE".toByteArray())
            put("fmt ".toByteArray())
            putInt(16)
            putShort(1)
            putShort(1)
            putInt(sampleRate)
            putInt(sampleRate * 2)
            putShort(2)
            putShort(16)
            put("data".toByteArray())
            putInt(dataSize)
        }
        val body = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
        pcm.forEach { body.putShort(it) }

        FileOutputStream(file).use { fos ->
            fos.write(header.array())
            fos.write(body.array())
        }
    }

    companion object {
        private const val SAMPLE_RATE = 22050

        @Volatile
        private var instance: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return instance ?: synchronized(this) {
                instance ?: SoundManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
