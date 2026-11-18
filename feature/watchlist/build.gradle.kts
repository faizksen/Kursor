plugins {
    alias(libs.plugins.kursor.feature)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "dev.faizksen.kursor.watchlist"
}

dependencies {
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.reorderable)
}
