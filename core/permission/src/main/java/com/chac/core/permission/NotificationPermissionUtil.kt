package com.chac.core.permission

import android.Manifest
import android.content.Context
import android.os.Build

object NotificationPermissionUtil : PermissionUtil() {
    override val permissions: Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }

    fun Permission.launchNotificationPermission() {
        launch()
    }

    fun checkPermission(context: Context): Boolean = checkPermissionGranted(context)
}
