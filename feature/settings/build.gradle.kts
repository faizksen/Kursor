plugins {
    alias(libs.plugins.kursor.feature)
}

android {
    namespace = "dev.faizksen.kursor.settings"
}

dependencies {
    implementation(libs.kotlinx.datetime)
}
