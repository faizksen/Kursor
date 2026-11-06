plugins {
    alias(libs.plugins.kursor.kmp.library)
}

kotlin {
    android {
        namespace = "dev.faizksen.kursor.model"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
