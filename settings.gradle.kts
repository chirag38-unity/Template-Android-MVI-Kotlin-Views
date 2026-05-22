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
rootProject.name = "TemplateAndroidMVI"
include(":app")
include(":core:common")
include(":core:ui")
include(":core:navigation")
include(":core:network")
include(":core:database")
include(":core:domain")
include(":feature-feed:api")
include(":feature-feed:impl")
include(":feature-search:api")
include(":feature-search:impl")
include(":feature-player-details:api")
include(":feature-player-details:impl")
include(":core:cache:api")
include(":core:cache:impl")
