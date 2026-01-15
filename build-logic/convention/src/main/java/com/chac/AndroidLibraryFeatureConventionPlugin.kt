package com.chac

import com.chac.convention.configureAndroid
import com.chac.convention.configureCompose
import com.chac.convention.configureHiltAndroid
import com.chac.convention.extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.library")

            configureAndroid()
            configureCompose()
            configureHiltAndroid()

            dependencies {
                val libs = project.extensions.libs
                "implementation"(libs.findLibrary("hilt.navigation.compose").get())
            }
        }
    }
}
