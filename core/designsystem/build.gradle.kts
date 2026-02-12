import com.chac.core.designsystem.tasks.registerDesignTokensTasks
import com.chac.convention.extensions.setNamespace

plugins {
    alias(libs.plugins.chac.android.library)
    alias(libs.plugins.chac.android.compose)
}

android {
    setNamespace("core.designsystem")
}

dependencies {
    implementation(libs.coil.compose)
    implementation(projects.core.resources)
}

registerDesignTokensTasks()
