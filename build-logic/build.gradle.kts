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
    }
}
