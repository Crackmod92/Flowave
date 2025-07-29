// di/RepositoryModule.kt
package com.crackmod.flowave.di

import com.crackmod.flowave.data.repository.MusicRepositoryImpl
import com.crackmod.flowave.data.repository.SettingsRepositoryImpl
import com.crackmod.flowave.domain.repository.MusicRepository
import com.crackmod.flowave.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMusicRepository(
        musicRepositoryImpl: MusicRepositoryImpl
    ): MusicRepository

    // !!! НОВЫЙ МЕТОД !!!
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
}