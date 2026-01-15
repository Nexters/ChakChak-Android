package com.chac

import com.android.build.api.dsl.ApplicationExtension
import com.chac.convention.configureAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.application")

            configureAndroid()

            extensions.configure<ApplicationExtension> {
                defaultConfig.targetSdk = 36
            }
        }
    }
}
