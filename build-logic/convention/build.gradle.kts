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
            id = libs.plugins.chakchak.android.application.get().pluginId
            implementationClass = "com.chakchak.AndroidApplicationConventionPlugin"
        }
    }

    plugins {
        register("androidLibrary") {
            id = libs.plugins.chakchak.android.library.asProvider().get().pluginId
            implementationClass = "com.chakchak.AndroidLibraryConventionPlugin"
        }
    }

    plugins {
        register("androidLibraryFeature") {
            id = libs.plugins.chakchak.android.library.feature.get().pluginId
            implementationClass = "com.chakchak.AndroidLibraryFeatureConventionPlugin"
        }
    }

    plugins {
        register("androidCompose") {
            id = libs.plugins.chakchak.android.compose.get().pluginId
            implementationClass = "com.chakchak.AndroidComposeConventionPlugin"
        }
    }

    plugins {
        register("androidRoom") {
            id = libs.plugins.chakchak.android.room.get().pluginId
            implementationClass = "com.chakchak.AndroidRoomConventionPlugin"
        }
    }

    plugins {
        register("kotlinLibraryDomain") {
            id = libs.plugins.chakchak.kotlin.library.domain.get().pluginId
            implementationClass = "com.chakchak.KotlinLibraryDomainConventionPlugin"
        }
    }
}
