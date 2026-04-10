pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Forgex_Mobile_Android"

include(":app")
include(":core:common")
include(":core:network")
include(":core:datastore")
include(":core:ui")
include(":feature:auth")
include(":feature:home")
include(":feature:workflow")
include(":feature:message")
include(":feature:profile")
