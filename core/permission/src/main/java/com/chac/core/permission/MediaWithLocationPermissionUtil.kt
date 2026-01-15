package com.chac.core.permission
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

object MediaWithLocationPermissionUtil : PermissionUtil() {
    override val permissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.ACCESS_MEDIA_LOCATION,
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.ACCESS_MEDIA_LOCATION,
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_MEDIA_LOCATION,
            )
        }

    fun Permission.launchMediaWithLocationPermission() {
        launch()
    }

    fun checkPermission(context: Context): Boolean =
        permissions.all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission,
            ) == PackageManager.PERMISSION_GRANTED
        }

    fun hasFullAccessPermission(context: Context): Boolean =
        hasFullAccessPhotosVideosPermission(context) && hasMediaAccessPermission(context)

    fun hasFullAccessPhotosVideosPermission(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasReadMediaImagesPermission(context) && hasReadMediaVideoPermission(context)
        } else {
            hasReadStoragePermission(context)
        }

    fun hasOnlyPartialAccessPhotosVideosPermission(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            !hasFullAccessPhotosVideosPermission(context) &&
                hasReadMediaVisualUserSelectedPermission(context)
        } else {
            false
        }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun hasReadMediaVisualUserSelectedPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
        ) == PackageManager.PERMISSION_GRANTED

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun hasReadMediaImagesPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES,
        ) == PackageManager.PERMISSION_GRANTED

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun hasReadMediaVideoPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_VIDEO,
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasReadStoragePermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasMediaAccessPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_MEDIA_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    fun getPermissionResultListener(
        onGranted: () -> Unit,
        onDenied: (() -> Unit)?,
        onPermanentlyDenied: (() -> Unit)?,
    ): (PermissionState) -> Unit =
        { state ->
            when (state) {
                PermissionState.Granted -> onGranted()
                PermissionState.Denied, PermissionState.PartiallyGranted -> {
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
}
