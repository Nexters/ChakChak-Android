package com.chac.core.permission.compose

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.chac.core.permission.MediaWithLocationPermissionUtil.getPermissionResultListener
import com.chac.core.permission.MediaWithLocationPermissionUtil.getPermissionState
import com.chac.core.permission.NotificationPermissionUtil
import com.chac.core.permission.NotificationPermissionUtil.launchNotificationPermission
import com.chac.core.permission.PermissionUtil.Permission
import com.chac.core.permission.PermissionUtil.PermissionState
import kotlinx.coroutines.CompletableDeferred

@Composable
internal fun rememberRegisterPermission(onPermissionResult: (PermissionState) -> Unit): Permission {
    val activity = LocalActivity.current
    val register = rememberLauncherForActivityResult(
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
): Permission = rememberRegisterPermission(
    onPermissionResult = getPermissionResultListener(
        onGranted = onGranted,
        onDenied = onDenied,
        onPermanentlyDenied = onPermanentlyDenied,
    ),
)

@Composable
fun rememberRegisterNotificationPermission(
    onGranted: () -> Unit = {},
    onDenied: (() -> Unit)? = null,
    onPermanentlyDenied: (() -> Unit)? = null,
): Permission = rememberRegisterPermission(
    onPermissionResult = NotificationPermissionUtil.getResultListener(
        onGranted = onGranted,
        onDenied = onDenied,
        onPermanentlyDenied = onPermanentlyDenied,
    ),
)

/**
 * 알림 권한 요청 결과를 코루틴에서 순차적으로 기다릴 수 있는 suspend 함수를 반환한다.
 *
 * - Android 13 미만 또는 이미 권한이 허용된 경우 [PermissionState.Granted]를 즉시 반환한다.
 * - 권한이 필요한 경우 시스템 권한 다이얼로그를 띄우고 사용자 응답 결과를 반환한다.
 *
 * 주로 하나의 `LaunchedEffect` 안에서
 * "알림 권한 요청 완료 -> 후속 권한/작업 실행" 흐름을 구성할 때 사용한다.
 */
@Composable
fun rememberAwaitNotificationPermissionResult(): suspend () -> PermissionState {
    val context = LocalContext.current
    var pendingResult by remember { mutableStateOf<CompletableDeferred<PermissionState>?>(null) }
    val permission = rememberRegisterNotificationPermission(
        onGranted = {
            pendingResult?.complete(PermissionState.Granted)
            pendingResult = null
        },
        onDenied = {
            pendingResult?.complete(PermissionState.Denied)
            pendingResult = null
        },
        onPermanentlyDenied = {
            pendingResult?.complete(PermissionState.PermanentlyDenied)
            pendingResult = null
        },
    )

    return remember(permission, context) {
        suspend {
            if (NotificationPermissionUtil.checkPermission(context)) {
                PermissionState.Granted
            } else {
                val deferred = pendingResult ?: CompletableDeferred<PermissionState>().also {
                    pendingResult = it
                    permission.launchNotificationPermission()
                }

                try {
                    deferred.await()
                } finally {
                    if (pendingResult === deferred) {
                        pendingResult = null
                    }
                }
            }
        }
    }
}

@Composable
fun rememberWriteRequestLauncher(
    onGranted: () -> Unit,
    onDenied: (() -> Unit)? = null,
): (IntentSender) -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                onGranted()
            } else {
                onDenied?.invoke()
            }
        },
    )

    return { intentSender ->
        launcher.launch(IntentSenderRequest.Builder(intentSender).build())
    }
}

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
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData("package:${context.packageName}".toUri())
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
        context.startActivity(intent)
    }
}
