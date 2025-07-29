// FloWaveApplication.kt
package com.crackmod.flowave

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FloWaveApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        println("FloWave: Application created")
    }
}
