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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Pangle SDK (pulled in by the Pangle mediation adapter) is only published here
        maven("https://artifact.bytedance.com/repository/pangle/")
    }
}

rootProject.name = "juooy-ads"
include(":ads")
