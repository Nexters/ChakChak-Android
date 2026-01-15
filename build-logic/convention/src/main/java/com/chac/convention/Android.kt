package com.chac.convention

import com.chac.convention.extensions.androidExtension
import com.chac.convention.extensions.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroid() {
    with(pluginManager) {
        apply("org.jetbrains.kotlin.android")
    }

    // Android settings
    androidExtension.apply {
        compileSdk = 36

        defaultConfig {
            minSdk = 31
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }

        configureKotlin()

        buildTypes {
            getByName("release") {
                isMinifyEnabled = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro",
                )
            }
        }
    }

    dependencies {
        val libs = project.extensions.libs
        "coreLibraryDesugaring"(libs.findLibrary("android.desugarJdkLibs").get())
        "implementation"(libs.findLibrary("androidx.core.ktx").get())
        "implementation"(libs.findLibrary("androidx.lifecycle.runtime.ktx").get())
        "implementation"(libs.findLibrary("timber").get())
    }
}
