package com.chac.core.permission.compose

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import com.chac.core.permission.MediaWithLocationPermissionUtil.getPermissionResultListener
import com.chac.core.permission.MediaWithLocationPermissionUtil.getPermissionState
import com.chac.core.permission.PermissionUtil.Permission
import com.chac.core.permission.PermissionUtil.PermissionState

@Composable
internal fun rememberRegisterPermission(onPermissionResult: (PermissionState) -> Unit): Permission {
    val activity = LocalActivity.current
    val register =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { onPermissionResult(getPermissionState(activity = activity, result = it)) },
        )
    return Permission(register)
}

@Composable
fun rememberRegisterMediaWithLocationPermission(
    onGranted: () -> Unit,
    onDenied: (() -> Unit)? = null,
    onPermanentlyDenied: (() -> Unit)? = null,
): Permission =
    rememberRegisterPermission(
        onPermissionResult =
            getPermissionResultListener(
                onGranted = onGranted,
                onDenied = onDenied,
                onPermanentlyDenied = onPermanentlyDenied,
            ),
    )

@Composable
fun PermissionDeniedDialog(
    title: String,
    message: String,
    onDismissRequest: () -> Unit,
    onNegativeClick: (() -> Unit) = onDismissRequest,
    onPositiveClick: (() -> Unit) = onDismissRequest,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            Text("설정으로", modifier = Modifier.clickable(onClick = onPositiveClick))
        },
        dismissButton = {
            Text("취소", modifier = Modifier.clickable(onClick = onNegativeClick))
        },
    )
}

fun moveToPermissionSetting(context: Context) {
    try {
        val intent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData("package:${context.packageName}".toUri())
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
        context.startActivity(intent)
    }
}
