import com.chac.convention.extensions.setNamespace

plugins {
    alias(libs.plugins.chac.android.library)
    alias(libs.plugins.chac.android.room)
    alias(libs.plugins.chac.android.hilt)
}

android {
    setNamespace("data.album")
}

dependencies {
    implementation(projects.domain.album)
    implementation(projects.core.resources)
    implementation(libs.commons.math3)
    implementation(libs.androidx.work.ktx)
    implementation(libs.mlkit.image.labeling)
    implementation(libs.hilt.ext.common)
    implementation(libs.hilt.ext.work)
    ksp(libs.hilt.ext.compiler)

    testImplementation(libs.junit)
}
