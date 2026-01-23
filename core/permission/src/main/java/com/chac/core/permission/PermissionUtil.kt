package com.chac.core.permission

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat

abstract class PermissionUtil {
    abstract val permissions: Array<String>

    protected fun Permission.launch() {
        result.launch(permissions)
    }

    class Permission(
        val result: ActivityResultLauncher<Array<String>>,
    )

    sealed class PermissionState {
        data object Granted : PermissionState()

        data object PartiallyGranted : PermissionState()

        data object Denied : PermissionState()

        data object PermanentlyDenied : PermissionState()
    }

    fun getPermissionState(
        activity: Activity?,
        result: Map<String, Boolean>,
    ): PermissionState {
        val deniedList: List<String> = result.filter { !it.value }.map { it.key }
        val grantedCount = result.count { it.value }

        var state = when {
            deniedList.isEmpty() -> PermissionState.Granted
            grantedCount > 0 -> PermissionState.PartiallyGranted
            else -> PermissionState.Denied
        }

        if (state == PermissionState.Denied) {
            val permanentlyMappedList = deniedList.map {
                activity?.let { activity ->
                    shouldShowRequestPermissionRationale(activity, it)
                }
            }

            if (permanentlyMappedList.contains(false)) {
                // 사용자가 [다시 묻지 않음] 을 선택한 경우 -> 영구 거부로 판단함.
                state = PermissionState.PermanentlyDenied
            }
        }

        return state
    }

    fun getResultListener(
        onGranted: () -> Unit,
        onDenied: (() -> Unit)?,
        onPermanentlyDenied: (() -> Unit)?,
    ): (PermissionState) -> Unit = { state ->
        when (state) {
            PermissionState.PartiallyGranted,
            PermissionState.Granted,
            -> onGranted()

            PermissionState.Denied -> {
                if (onDenied != null) {
                    onDenied()
                }
            }

            PermissionState.PermanentlyDenied -> {
                if (onPermanentlyDenied != null) {
                    onPermanentlyDenied()
                }
            }
        }
    }

    fun checkPermissionGranted(context: Context): Boolean = permissions.all { permission ->
        ContextCompat.checkSelfPermission(
            context,
            permission,
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun shouldShowRequestPermissionRationale(activity: Activity?): Boolean {
        if (activity == null) return false

        return permissions.any { permission ->
            shouldShowRequestPermissionRationale(activity, permission)
        }
    }
}
