package com.chac

import com.chac.convention.configureHiltAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureHiltAndroid()
        }
    }
}
