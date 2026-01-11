import com.chakchak.convention.extensions.setNamespace

plugins {
    alias(libs.plugins.chakchak.android.library.feature)
}

android {
    setNamespace("feature.album")
}
