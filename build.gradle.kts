// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.ktfmt) apply false
    alias(libs.plugins.gms) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.sonar)
}

sonar {
    properties {
        property("sonar.projectKey", "SwEnt-Fall-2024-Group-22_Cyrcle")
        property("sonar.organization", "swent-fall-2024-group-22")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}