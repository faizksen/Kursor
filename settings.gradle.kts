pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Kursor"
include(":app")
include(":core:model")
include(":core:common")
include(":core:database")
include(":core:datastore")
include(":core:network")
include(":core:data")
include(":core:domain")
include(":core:sync")
include(":core:designsystem")
include(":core:testing")
include(":feature:watchlist")
include(":feature:pairdetail")
include(":feature:converter")
include(":feature:alerts")
include(":feature:settings")
include(":widget")
