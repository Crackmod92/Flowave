package com.crackmod.flowave.data.player

import android.content.Context
import androidx.media3.common.audio.SonicAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.SilenceSkippingAudioProcessor
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@UnstableApi
class RenderersFactory @Inject constructor(
    @ApplicationContext private val context: Context
) : DefaultRenderersFactory(context) {

    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean
    ): DefaultAudioSink {
        val silenceSkippingAudioProcessor = SilenceSkippingAudioProcessor(
            2_000_000L,
            500_000L,
            150_000L.toShort()
        )

        // Процессор для изменения скорости и тона
        val sonicAudioProcessor = SonicAudioProcessor()

        return DefaultAudioSink.Builder(context)
            .setEnableFloatOutput(enableFloatOutput)
            .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
            .setAudioProcessors(
                arrayOf(
                    silenceSkippingAudioProcessor,
                    sonicAudioProcessor
                )
            )
            .build()
    }
}