plugins {
    alias(libs.plugins.kursor.kmp.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room3)
    alias(libs.plugins.koin.compiler)
}

kotlin {
    android {
        namespace = "dev.faizksen.kursor.database"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))

            implementation(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)

            implementation(libs.room3.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.sqlite.bundled.jvm)
        }
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.room3.compiler)
    add("kspIosArm64", libs.room3.compiler)
    add("kspIosSimulatorArm64", libs.room3.compiler)
}
