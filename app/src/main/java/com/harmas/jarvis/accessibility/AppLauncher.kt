package com.harmas.jarvis.accessibility

import android.content.Context
import android.content.Intent

data class AppLaunchResult(
    val success: Boolean,
    val message: String
)

class AppLauncher(context: Context) {

    private val appContext = context.applicationContext

    private val knownApps = mapOf(
        "youtube" to "com.google.android.youtube",
        "instagram" to "com.instagram.android",
        "whatsapp" to "com.whatsapp",
        "chrome" to "com.android.chrome",
        "settings" to "com.android.settings",
        "play store" to "com.android.vending"
    )

    fun launchApp(appName: String): AppLaunchResult {

        val packageName = knownApps[
            appName.trim().lowercase()
        ] ?: return AppLaunchResult(
            false,
            "App is not in the supported app list."
        )

        return try {

            val intent = appContext.packageManager
                .getLaunchIntentForPackage(packageName)

            if (intent == null) {
                return AppLaunchResult(
                    false,
                    "App is not installed or cannot be launched."
                )
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            appContext.startActivity(intent)

            AppLaunchResult(
                true,
                "Launching $appName."
            )

        } catch (error: Exception) {

            AppLaunchResult(
                false,
                error.message ?: "Unable to launch app."
            )
        }
    }
}
