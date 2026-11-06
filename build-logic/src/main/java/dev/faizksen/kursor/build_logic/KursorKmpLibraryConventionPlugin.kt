package dev.faizksen.kursor.build_logic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KursorKmpLibraryConventionPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        target.pluginManager.apply("com.android.kotlin.multiplatform.library")

        val libs = target.extensions.getByType<VersionCatalogsExtension>().named("libs")

        target.extensions.configure<KotlinMultiplatformExtension> {
            (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
                compileSdk = 37
                minSdk = 28

                withHostTest {
                    isIncludeAndroidResources = false
                }

                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }

            iosArm64()
            iosSimulatorArm64()

            sourceSets.getByName("androidHostTest").dependencies {
                implementation(libs.findLibrary("junit").get())
                implementation(libs.findLibrary("truth").get())
            }
        }

        target.tasks.register("test") {
            dependsOn("allTests")
        }
    }
}
