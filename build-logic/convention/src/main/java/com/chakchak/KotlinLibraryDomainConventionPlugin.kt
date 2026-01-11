package com.chakchak

import com.chakchak.convention.configureHiltKotlin
import com.chakchak.convention.configureKotlin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class KotlinLibraryDomainConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.jetbrains.kotlin.jvm")

            configureKotlin()
            configureHiltKotlin()
        }
    }
}
