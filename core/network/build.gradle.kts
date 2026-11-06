plugins {
    alias(libs.plugins.kursor.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "dev.faizksen.kursor.network"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.datetime)

    implementation(platform(libs.koin.bom))
    api(libs.koin.android)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp.core)
    debugImplementation(libs.okhttp.interceptor.logging)

    testImplementation(platform(libs.okhttp.bom))
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
}
