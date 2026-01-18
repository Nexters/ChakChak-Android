package com.chac.convention

import com.chac.convention.extensions.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureKotlin() {
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_21.majorVersion
        targetCompatibility = JavaVersion.VERSION_21.majorVersion
    }

    dependencies {
        val libs = project.extensions.libs
        "implementation"(libs.findLibrary("kotlinx.coroutines.core").get())
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.addAll(
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-XXLanguage:+PropertyParamAnnotationDefaultTargetMode",
            )
        }
    }
}
