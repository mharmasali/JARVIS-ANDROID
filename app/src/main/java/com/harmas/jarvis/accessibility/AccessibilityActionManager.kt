package com.harmas.jarvis.accessibility

class AccessibilityActionManager {

    fun goBack(): Boolean {
        return JarvisAccessibilityService.instance
            ?.performBack() ?: false
    }

    fun goHome(): Boolean {
        return JarvisAccessibilityService.instance
            ?.performHome() ?: false
    }

    fun showRecentApps(): Boolean {
        return JarvisAccessibilityService.instance
            ?.openRecentApps() ?: false
    }

    fun readVisibleScreen(): String {
        return JarvisAccessibilityService.instance
            ?.getVisibleScreenText()
            ?: "Accessibility service is not enabled."
    }

    fun isServiceConnected(): Boolean {
        return JarvisAccessibilityService.instance != null
    }
}
