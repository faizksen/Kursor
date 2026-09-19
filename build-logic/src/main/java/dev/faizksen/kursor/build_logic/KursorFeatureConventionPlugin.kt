package dev.faizksen.kursor.build_logic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.project

class KursorFeatureConventionPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("kursor.android.library")
        target.pluginManager.apply("kursor.compose")
        target.pluginManager.apply("kursor.android.hilt")
        target.pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

        val libs = target.extensions.getByType<VersionCatalogsExtension>().named("libs")
        target.dependencies {
            add("implementation", project(":core:domain"))
            add("implementation", project(":core:ui"))
            add("implementation", project(":core:common"))
            add("implementation", project(":core:designsystem"))

            add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
            add("implementation", libs.findLibrary("androidx-hilt-navigation-compose").get())
            add("implementation", libs.findLibrary("androidx-navigation-compose").get())

            add("implementation", libs.findLibrary("kotlinx-coroutines-core").get())
            add("implementation", libs.findLibrary("kotlinx-coroutines-android").get())
        }
    }
}