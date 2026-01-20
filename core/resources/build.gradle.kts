import com.chac.convention.extensions.setNamespace

plugins {
    alias(libs.plugins.chac.android.library)
}

android {
    setNamespace("core.resources")
}
