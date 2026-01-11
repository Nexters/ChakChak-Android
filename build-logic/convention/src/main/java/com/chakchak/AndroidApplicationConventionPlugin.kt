package com.chakchak

import com.chakchak.convention.configureAndroid
import com.chakchak.convention.configureCompose
import com.chakchak.convention.configureHiltAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.application")

            configureAndroid()
            configureCompose()
            configureHiltAndroid()
        }
    }
}
