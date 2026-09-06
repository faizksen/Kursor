plugins {
    alias(libs.plugins.kursor.android.library)
    alias(libs.plugins.kursor.android.hilt)
}

android {
    namespace = "dev.faizksen.kursor.data"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))

    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
}
