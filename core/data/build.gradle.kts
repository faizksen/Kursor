plugins {
    alias(libs.plugins.kursor.android.library)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "dev.faizksen.kursor.data"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.datetime)
    implementation(project(":core:network"))
    implementation(project(":core:database"))

    implementation(platform(libs.koin.bom))
    api(libs.koin.core)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.room3.testing)
    androidTestImplementation(libs.sqlite.bundled)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
