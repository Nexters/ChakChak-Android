import com.chac.convention.extensions.setNamespace

plugins {
    alias(libs.plugins.chac.android.library.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    setNamespace("feature.album")
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.permission)
    implementation(projects.core.resources)
    implementation(projects.domain.album)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.serialization.json)
}
