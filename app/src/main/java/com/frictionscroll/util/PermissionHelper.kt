package com.frictionscroll.util

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils

object PermissionHelper {

    fun hasOverlayPermission(context: Context): Boolean =
        Settings.canDrawOverlays(context)

    fun hasAccessibilityPermission(context: Context): Boolean {
        val expectedComponent = ComponentName(
            context,
            "com.frictionscroll.service.ScrollFrictionAccessibilityService"
        )
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentName = ComponentName.unflattenFromString(colonSplitter.next())
            if (componentName == expectedComponent) return true
        }
        return false
    }
}
