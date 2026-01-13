package com.chac.convention

import com.chac.convention.extensions.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureHiltAndroid() {
    with(pluginManager) {
        apply("com.google.dagger.hilt.android")
        apply("com.google.devtools.ksp")
    }

    dependencies {
        val libs = project.extensions.libs
        "implementation"(libs.findLibrary("hilt.android").get())
        "ksp"(libs.findLibrary("hilt.android.compiler").get())
    }
}
