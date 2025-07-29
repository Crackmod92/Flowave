package com.crackmod.flowave.presentation.player

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import kotlin.random.Random

/**
 * Обертка над Player, которая реализует предсказуемое и управляемое поведение Shuffle.
 * Вместо того чтобы полагаться на внутренний шаффл ExoPlayer, эта обертка
 * вручную перемешивает очередь и отдает ее плееру как статичный плейлист,
 * сохраняя при этом оригинальный порядок для возможности возврата.
 */
@OptIn(UnstableApi::class)
class ShufflePlayer(player: Player) : ForwardingPlayer(player) {

    private var isShuffled = false
    private var originalQueue: List<MediaItem> = emptyList()

    private fun getCurrentPlayerQueue(): List<MediaItem> {
        return (0 until mediaItemCount).map { getMediaItemAt(it) }
    }

    override fun getShuffleModeEnabled(): Boolean {
        return isShuffled
    }

    override fun setShuffleModeEnabled(shuffleModeEnabled: Boolean) {
        if (isShuffled == shuffleModeEnabled) return

        isShuffled = shuffleModeEnabled
        val currentQueue = getCurrentPlayerQueue()
        val currentItem = currentMediaItem

        if (shuffleModeEnabled) {
            // Сохраняем текущий порядок как оригинальный и перемешиваем
            originalQueue = currentQueue
            if (originalQueue.isEmpty()) return

            val shuffledQueue = originalQueue.shuffled().toMutableList()

            // Перемещаем текущий трек в начало, чтобы воспроизведение не прерывалось
            if (currentItem != null) {
                shuffledQueue.remove(currentItem)
                shuffledQueue.add(0, currentItem)
            }

            val position = currentPosition
            super.setMediaItems(shuffledQueue, 0, position)

        } else {
            // Возвращаем оригинальный порядок
            if (originalQueue.isEmpty()) return

            val originalIndex = if (currentItem != null) {
                originalQueue.indexOf(currentItem).coerceAtLeast(0)
            } else {
                0
            }
            val position = currentPosition
            super.setMediaItems(originalQueue, originalIndex, position)
        }
        // Внутренний шаффл самого ExoPlayer ВСЕГДА выключен.
        super.setShuffleModeEnabled(false)
    }

    override fun setMediaItems(mediaItems: MutableList<MediaItem>, startIndex: Int, startPositionMs: Long) {
        originalQueue = mediaItems.toList()

        val itemsToSet = if (isShuffled) {
            val startItem = mediaItems[startIndex]
            val shuffled = mediaItems.shuffled().toMutableList()
            // Ставим стартовый трек на первое место в перемешанном списке
            shuffled.remove(startItem)
            shuffled.add(0, startItem)
            shuffled
        } else {
            mediaItems
        }

        val finalStartIndex = if (isShuffled) 0 else startIndex

        super.setMediaItems(itemsToSet, finalStartIndex, startPositionMs)
        super.setShuffleModeEnabled(false)
    }

    // Переопределяем остальные методы для консистентности
    override fun setMediaItems(mediaItems: MutableList<MediaItem>) {
        setMediaItems(mediaItems, 0, C.TIME_UNSET)
    }

    override fun setMediaItems(mediaItems: MutableList<MediaItem>, resetPosition: Boolean) {
        val startIndex = if(resetPosition) 0 else currentMediaItemIndex
        val startPosition = if(resetPosition) C.TIME_UNSET else currentPosition
        setMediaItems(mediaItems, startIndex, startPosition)
    }

    override fun addMediaItem(mediaItem: MediaItem) {
        originalQueue = originalQueue + mediaItem
        super.addMediaItem(mediaItem)
    }

    override fun addMediaItems(mediaItems: MutableList<MediaItem>) {
        originalQueue = originalQueue + mediaItems
        super.addMediaItems(mediaItems)
    }

    override fun removeMediaItem(index: Int) {
        val itemToRemove = getMediaItemAt(index)
        originalQueue = originalQueue.filter { it.mediaId != itemToRemove.mediaId }
        super.removeMediaItem(index)
    }

    override fun clearMediaItems() {
        originalQueue = emptyList()
        super.clearMediaItems()
    }
}