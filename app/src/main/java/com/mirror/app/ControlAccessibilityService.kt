package com.mirror.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class ControlAccessibilityService : AccessibilityService() {

    companion object {
        var instance: ControlAccessibilityService? = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        instance = this
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }
}
