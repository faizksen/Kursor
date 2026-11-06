plugins {
    alias(libs.plugins.kursor.kmp.library)
}

kotlin {
    android {
        namespace = "dev.faizksen.kursor.common"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
        }
    }
}
