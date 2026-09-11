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
        // Eynora LMS SDK artifacts (com.dn:lms-sdk, com.dn:shared), published as AARs to the
        // GitLab Maven registry of eynorix/eynorix-mobile-app (project 4).
        // Requires gitlabToken (PAT/project token with read_api) in ~/.gradle/gradle.properties.
        maven {
            name = "GitLab"
            url = uri("https://gitlab.eynora.tech/api/v4/projects/4/packages/maven")
            credentials(HttpHeaderCredentials::class) {
                name = "Private-Token"
                value = providers.gradleProperty("gitlabToken").orElse("").get()
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
            content {
                includeGroup("com.dn")
                // Vendored eSewa payment SDK, pulled in transitively by com.dn:lms-sdk
                includeGroup("com.dn.thirdparty")
            }
        }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        // Convay Meet SDK — transitive dependency of the LMS SDK (live classes)
        maven { url = uri("https://raw.githubusercontent.com/Synesis-IT-PLC/convay-maven-repository/master/releases") }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "tenant_lms_sdk_test"
include(":app")
