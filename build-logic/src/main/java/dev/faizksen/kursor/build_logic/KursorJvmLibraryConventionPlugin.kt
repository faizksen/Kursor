package dev.faizksen.kursor.build_logic

import org.gradle.api.Plugin
import org.gradle.api.Project

class KursorJvmLibraryConventionPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("java-library")
        target.pluginManager.apply("org.jetbrains.kotlin.jvm")
    }
}