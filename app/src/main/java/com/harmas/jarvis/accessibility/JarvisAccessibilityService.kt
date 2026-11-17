package com.harmas.jarvis.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class JarvisAccessibilityService : AccessibilityService() {

    companion object {

        @Volatile
        var instance: JarvisAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {
        // Screen events are handled only when requested.
        // No continuous screen recording.
    }

    override fun onInterrupt() {
        // Accessibility interruption.
    }

    override fun onDestroy() {
        if (instance === this) {
            instance = null
        }
        super.onDestroy()
    }

    fun performBack(): Boolean {
        return performGlobalAction(
            GLOBAL_ACTION_BACK
        )
    }

    fun performHome(): Boolean {
        return performGlobalAction(
            GLOBAL_ACTION_HOME
        )
    }

    fun openRecentApps(): Boolean {
        return performGlobalAction(
            GLOBAL_ACTION_RECENTS
        )
    }

    fun getVisibleScreenText(): String {

        val root = rootInActiveWindow ?: return ""

        return try {
            val output = StringBuilder()
            collectText(root, output, 0)

            output.toString().take(8000)

        } finally {
            root.recycle()
        }
    }

    private fun collectText(
        node: AccessibilityNodeInfo,
        output: StringBuilder,
        depth: Int
    ) {

        if (depth > 20 || output.length >= 8000) {
            return
        }

        val text = node.text?.toString()
        val description = node.contentDescription?.toString()

        if (!text.isNullOrBlank()) {
            output.append(text).append('\n')
        }

        if (!description.isNullOrBlank()) {
            output.append(description).append('\n')
        }

        for (index in 0 until node.childCount) {

            val child = node.getChild(index) ?: continue

            try {
                collectText(child, output, depth + 1)
            } finally {
                child.recycle()
            }
        }
    }
}
