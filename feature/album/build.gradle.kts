import com.chac.convention.extensions.setNamespace

plugins {
    alias(libs.plugins.chac.android.library.feature)
}

android {
    setNamespace("feature.album")
}
