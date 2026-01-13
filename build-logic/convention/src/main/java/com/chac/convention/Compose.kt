package com.chac.convention

import com.chac.convention.extensions.androidExtension
import com.chac.convention.extensions.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureCompose() {
    with(pluginManager) {
        apply("org.jetbrains.kotlin.plugin.compose")
    }

    androidExtension.apply {
        buildFeatures {
            compose = true
        }

        composeOptions {
            kotlinCompilerExtensionVersion = "1.5.1"
        }

        dependencies {
            val libs = project.extensions.libs
            "implementation"(libs.findLibrary("androidx.activity.compose").get())

            val bom = libs.findLibrary("compose-bom").get()
            "implementation"(platform(bom))

            "implementation"(libs.findLibrary("compose.material3").get())
            "implementation"(libs.findLibrary("compose.material.icons").get())
            "implementation"(libs.findLibrary("compose.ui").get())
            "implementation"(libs.findLibrary("compose.ui.tooling.preview").get())
            "debugImplementation"(libs.findLibrary("compose.ui.tooling").get())
        }
    }
}
