// di/MediaScannerModule.kt
package com.crackmod.flowave.di

import android.content.Context
import com.crackmod.flowave.data.scanner.MediaScanner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaScannerModule {

    @Provides
    @Singleton
    fun provideMediaScanner(
        @ApplicationContext context: Context
    ): MediaScanner {
        return MediaScanner(context)
    }
}
