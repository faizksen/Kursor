plugins {
    alias(libs.plugins.kursor.feature)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "dev.faizksen.kursor.settings"
}

dependencies {
    implementation(libs.kotlinx.datetime)
}
