import org.gradle.kotlin.dsl.compileOnly

plugins {
    `kotlin-dsl`
}
dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kursorJvmLibrary") {
            id = "kursor.jvm.library"
            implementationClass = "dev.faizksen.kursor.build_logic.KursorJvmLibraryConventionPlugin"
        }
        register("kursorAndroidLibrary") {
            id = "kursor.android.library"
            implementationClass = "dev.faizksen.kursor.build_logic.KursorAndroidLibraryConventionPlugin"
        }
        register("kursorAndroidHilt") {
            id = "kursor.android.hilt"
            implementationClass = "dev.faizksen.kursor.build_logic.KursorAndroidHiltConventionPlugin"
        }
        register("kursorCompose") {
            id = "kursor.compose"
            implementationClass = "dev.faizksen.kursor.build_logic.KursorJetpackComposeConventionPlugin"
        }
        register("kursorFeature") {
            id = "kursor.feature"
            implementationClass = "dev.faizksen.kursor.build_logic.KursorFeatureConventionPlugin"
        }
    }
}
