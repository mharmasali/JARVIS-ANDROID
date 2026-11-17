package com.harmas.jarvis.service

import android.app.Application
import com.harmas.jarvis.core.JarvisCore
import com.harmas.jarvis.engine.EngineBootstrap

class JarvisApplication : Application() {

    val jarvisCore: JarvisCore by lazy {
        JarvisCore.getInstance(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize the central JARVIS system.
        jarvisCore

        // Register the configured AI engine.
        EngineBootstrap.initialize()
    }
}
