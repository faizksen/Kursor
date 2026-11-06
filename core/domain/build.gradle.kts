plugins {
    alias(libs.plugins.kursor.android.library)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "dev.faizksen.kursor.domain"
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:data"))

    api(libs.kotlinx.coroutines.core)

    implementation(platform(libs.koin.bom))
    api(libs.koin.core)

    testImplementation(project(":core:testing"))
}
