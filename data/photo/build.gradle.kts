import com.chac.convention.extensions.setNamespace

plugins {
    alias(libs.plugins.chac.android.library)
    alias(libs.plugins.chac.android.room)
}

android {
    setNamespace("data.photo")
}
