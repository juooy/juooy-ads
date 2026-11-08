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
        // Pangle SDK, pulled in by juooy-ads' Pangle mediation adapter.
        // exclusiveContent: only com.pangle.global resolves here, and only from here
        exclusiveContent {
            forRepository { maven("https://artifact.bytedance.com/repository/pangle/") }
            filter { includeGroup("com.pangle.global") }
        }
    }
}

rootProject.name = "juooy-ads"
include(":ads")
