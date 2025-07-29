package com.crackmod.flowave.data.audio

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioEffectsManager @Inject constructor() {

    var equalizer: Equalizer? = null
    var bassBoost: BassBoost? = null
    var virtualizer: Virtualizer? = null

    private var audioSessionId: Int = 0

    fun attach(audioSessionId: Int) {
        if (this.audioSessionId == audioSessionId) return

        this.audioSessionId = audioSessionId
        release() // Освобождаем старые инстансы, если были

        try {
            equalizer = Equalizer(0, audioSessionId)
            bassBoost = BassBoost(0, audioSessionId)
            virtualizer = Virtualizer(0, audioSessionId)
        } catch (e: Exception) {
            // Устройство может не поддерживать какой-то из эффектов
            e.printStackTrace()
        }
    }

    fun release() {
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        equalizer = null
        bassBoost = null
        virtualizer = null
    }
}