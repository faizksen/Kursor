plugins {
    alias(libs.plugins.kursor.android.library)
}

android {
    namespace = "dev.faizksen.kursor.sync"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:model"))

    implementation(platform(libs.koin.bom))
    api(libs.koin.android)
    implementation(libs.koin.workmanager)

    implementation(libs.androidx.work.runtime)
}
