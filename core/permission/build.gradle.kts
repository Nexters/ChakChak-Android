import com.chac.convention.extensions.setNamespace

plugins {
    alias(libs.plugins.chac.android.library)
    alias(libs.plugins.chac.android.hilt)
    alias(libs.plugins.chac.android.compose)
}

android {
    setNamespace("core.permission")
}
