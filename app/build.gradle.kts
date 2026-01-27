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
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("CHAC_KEYSTORE_PATH"))
            storePassword = System.getenv("CHAC_KEYSTORE_PASSWORD")
            keyAlias = System.getenv("CHAC_KEY_ALIAS")
            keyPassword = System.getenv("CHAC_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.data.album)
    implementation(projects.feature.album)

    // Core Navigation 3 라이브러리
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.timber)

    // Hilt work
    implementation(libs.hilt.ext.work)
    ksp(libs.hilt.ext.compiler)
}
