plugins {
    alias(libs.plugins.kursor.android.library)
}

android {
    namespace = "dev.faizksen.kursor.ui"
}

dependencies {
    api(libs.androidx.lifecycle.viewmodel)
}