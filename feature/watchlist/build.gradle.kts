plugins {
    alias(libs.plugins.kursor.feature)
}

android {
    namespace = "dev.faizksen.kursor.watchlist"
}

dependencies {
    implementation(libs.kotlinx.datetime)
}
