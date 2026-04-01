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
        maven(url = "https://maven.google.com")
        google()
        maven(url = "https://repo1.maven.org/maven2")
        mavenCentral()
    }
}

rootProject.name = "Zenchat"
include(":app")
 