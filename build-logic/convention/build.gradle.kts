plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.chac.android.application.get().pluginId
            implementationClass = "com.chac.AndroidApplicationConventionPlugin"
        }
    }

    plugins {
        register("androidLibrary") {
            id = libs.plugins.chac.android.library.asProvider().get().pluginId
            implementationClass = "com.chac.AndroidLibraryConventionPlugin"
        }
    }

    plugins {
        register("androidLibraryFeature") {
            id = libs.plugins.chac.android.library.feature.get().pluginId
            implementationClass = "com.chac.AndroidLibraryFeatureConventionPlugin"
        }
    }

    plugins {
        register("androidCompose") {
            id = libs.plugins.chac.android.compose.get().pluginId
            implementationClass = "com.chac.AndroidComposeConventionPlugin"
        }
    }

    plugins {
        register("androidHilt") {
            id = libs.plugins.chac.android.hilt.get().pluginId
            implementationClass = "com.chac.AndroidHiltConventionPlugin"
        }
    }

    plugins {
        register("androidRoom") {
            id = libs.plugins.chac.android.room.get().pluginId
            implementationClass = "com.chac.AndroidRoomConventionPlugin"
        }
    }

    plugins {
        register("kotlinLibraryDomain") {
            id = libs.plugins.chac.kotlin.library.domain.get().pluginId
            implementationClass = "com.chac.KotlinLibraryDomainConventionPlugin"
        }
    }

    plugins {
        register("kotlinHilt") {
            id = libs.plugins.chac.kotlin.hilt.get().pluginId
            implementationClass = "com.chac.KotlinHiltConventionPlugin"
        }
    }
}
