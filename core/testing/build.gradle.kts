plugins {
    alias(libs.plugins.kursor.android.library)
}

android {
    namespace = "dev.faizksen.kursor.testing"
}

dependencies {
    api(libs.junit)
    api(libs.truth)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
}
