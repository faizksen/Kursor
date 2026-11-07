plugins {
    alias(libs.plugins.kursor.kmp.library)
    alias(libs.plugins.koin.compiler)
}

kotlin {
    android {
        namespace = "dev.faizksen.kursor.domain"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            implementation(project(":core:data"))

            api(libs.kotlinx.coroutines.core)

            implementation(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
