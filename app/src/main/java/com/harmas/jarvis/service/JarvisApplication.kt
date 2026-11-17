package com.harmas.jarvis.service

import android.app.Application
import com.harmas.jarvis.core.JarvisCore

class JarvisApplication : Application() {

    val jarvisCore: JarvisCore by lazy {
        JarvisCore.getInstance(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize the core when first accessed.
        // Heavy AI models will not load automatically.
    }
}
