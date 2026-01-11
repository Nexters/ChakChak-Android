package com.chakchak

import com.chakchak.convention.configureCompose
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureCompose()
        }
    }
}
