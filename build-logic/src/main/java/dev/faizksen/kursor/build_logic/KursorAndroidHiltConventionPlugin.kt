package dev.faizksen.kursor.build_logic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class KursorAndroidHiltConventionPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("com.google.devtools.ksp")
        target.pluginManager.apply("com.google.dagger.hilt.android")

        val libs = target.extensions.getByType<VersionCatalogsExtension>().named("libs")
        target.dependencies {
            add("implementation", libs.findLibrary("hilt-android").get())
            add("ksp", libs.findLibrary("hilt-compiler").get())
        }
    }
}