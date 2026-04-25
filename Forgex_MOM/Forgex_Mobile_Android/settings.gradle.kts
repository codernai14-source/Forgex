pluginManagement {
    includeBuild("build-logic")
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
include(":core:architecture")
include(":core:designsystem")
include(":core:component")
include(":core:model")
include(":core:navigation")
include(":core:testing")
include(":core:device")
include(":feature:auth")
include(":feature:home")
include(":feature:workflow")
include(":feature:message")
include(":feature:profile")
include(":feature:basic")
include(":feature:report")
include(":feature:integration")
include(":feature:warehouse")
include(":feature:production")
include(":feature:quality")
include(":feature:equipment")
include(":feature:label")
