plugins {
    alias(libs.plugins.kursor.kmp.library)
    alias(libs.plugins.koin.compiler)
}

kotlin {
    android {
        namespace = "dev.faizksen.kursor.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))

            implementation(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.room3.runtime)
            implementation(libs.sqlite.bundled)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.sqlite.bundled.jvm)
        }
    }
}
