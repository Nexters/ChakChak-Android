package com.chac

import com.chac.convention.configureAndroid
import com.chac.convention.configureCompose
import com.chac.convention.configureHiltAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class AndroidLibraryFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.library")

            configureAndroid()
            configureCompose()
            configureHiltAndroid()
        }
    }
}
