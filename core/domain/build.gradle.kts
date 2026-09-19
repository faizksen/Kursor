plugins {
    alias(libs.plugins.kursor.android.library)
    alias(libs.plugins.kursor.android.hilt)
}

android {
    namespace = "dev.faizksen.kursor.domain"
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:data"))

    api(libs.kotlinx.coroutines.core)
}
