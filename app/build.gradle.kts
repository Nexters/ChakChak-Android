import com.chakchak.convention.extensions.basePackage
import com.chakchak.convention.extensions.setNamespace

plugins {
    alias(libs.plugins.chakchak.android.application)
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
}
