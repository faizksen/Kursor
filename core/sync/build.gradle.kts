plugins {
    alias(libs.plugins.kursor.android.library)
    alias(libs.plugins.kursor.android.hilt)
}

android {
    namespace = "dev.faizksen.kursor.sync"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:model"))

    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
}
