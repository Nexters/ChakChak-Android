import com.chac.convention.extensions.basePackage
import com.chac.convention.extensions.setNamespace

plugins {
    alias(libs.plugins.chac.android.application)
    alias(libs.plugins.chac.android.compose)
    alias(libs.plugins.chac.android.hilt)
}

android {
    setNamespace("")

    defaultConfig {
        applicationId = "$basePackage.app"
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.data.photo)
    implementation(projects.feature.album)

    // Core Navigation 3 라이브러리
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
}
