plugins {
    alias(libs.plugins.kursor.jvm.library)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(platform(libs.koin.bom))
    api(libs.koin.core)
}
