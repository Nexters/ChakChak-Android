package com.chac.convention.extensions

import org.gradle.api.Project

fun Project.versionName(): String {
    val versionProps = loadVersionProperties()
    val major = versionProps["VERSION_MAJOR"]
    val minor = versionProps["VERSION_MINOR"]
    val patch = versionProps["VERSION_PATCH"]
    return "$major.$minor.$patch"
}

fun Project.versionCode(): Int {
    val versionProps = loadVersionProperties()
    val major = versionProps["VERSION_MAJOR"]?.toInt()
    val minor = versionProps["VERSION_MINOR"]?.toInt()
    val patch = versionProps["VERSION_PATCH"]?.toInt()
    val build = versionProps["VERSION_BUILD"]?.toInt()

    if (major == null || minor == null || patch == null || build == null) {
        throw IllegalStateException("Invalid version properties")
    }

    // MAJOR * 1000000 + MINOR * 10000 + PATCH * 100 + BUILD
    return major * 1000000 + minor * 10000 + patch * 100 + build
}

private fun Project.loadVersionProperties(): Map<String, String> {
    val versionPropsFile = rootProject.file("version.properties")
    val versionProps = mutableMapOf<String, String>()

    if (versionPropsFile.exists()) {
        versionPropsFile.readLines().forEach { line ->
            if (line.contains("=") && !line.startsWith("#")) {
                val (key, value) = line.split("=", limit = 2)
                versionProps[key.trim()] = value.trim()
            }
        }
    }
    return versionProps
}
