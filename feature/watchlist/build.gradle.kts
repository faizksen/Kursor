plugins {
    alias(libs.plugins.kursor.feature)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "dev.faizksen.kursor.watchlist"
}

dependencies {
    implementation(libs.kotlinx.datetime)
}
