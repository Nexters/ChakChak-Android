package com.chac.core.permission

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MediaWithLocationPermissionUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun hasFullPermission(): Boolean = MediaWithLocationPermissionUtil.hasFullAccessPermission(context)

    fun needsPermissionRequest(): Boolean =
        !MediaWithLocationPermissionUtil.hasOnlyPartialAccessPhotosVideosPermission(context) && !hasFullPermission()
}
