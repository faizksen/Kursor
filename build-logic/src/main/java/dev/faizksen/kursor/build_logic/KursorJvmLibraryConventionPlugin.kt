package dev.faizksen.kursor.build_logic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class KursorJvmLibraryConventionPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("java-library")
        target.pluginManager.apply("org.jetbrains.kotlin.jvm")
        val libs = target.extensions.getByType<VersionCatalogsExtension>().named("libs")
        target.dependencies {
            add("testImplementation", libs.findLibrary("junit").get())
            add("testImplementation", libs.findLibrary("truth").get())
        }
    }
}