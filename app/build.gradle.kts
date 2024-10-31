import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.ktfmt)
    alias(libs.plugins.gms)
    alias(libs.plugins.sonar)
    id("jacoco")

    id("kotlin-kapt")
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.github.se.cyrcle"
    compileSdk = 34


    buildFeatures {
        viewBinding = true
    }


    initLocalProps()

    defaultConfig {
        applicationId = "com.github.se.cyrcle"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.github.se.cyrcle.di.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }

        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    testCoverage {
        jacocoVersion = "0.8.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    // Robolectric needs to be run only in debug. But its tests are placed in the shared source set (test)
    // The next lines transfers the src/test/* from shared to the testDebug one
    //
    // This prevent errors from occurring during unit tests
    sourceSets.getByName("testDebug") {
        val test = sourceSets.getByName("test")

        java.setSrcDirs(test.java.srcDirs)
        res.setSrcDirs(test.res.srcDirs)
        resources.setSrcDirs(test.resources.srcDirs)
    }

    sourceSets.getByName("test") {
        java.setSrcDirs(emptyList<File>())
        res.setSrcDirs(emptyList<File>())
        resources.setSrcDirs(emptyList<File>())
    }
}

sonar {}

// When a library is used both by robolectric and connected tests, use this function
fun DependencyHandlerScope.globalTestImplementation(dep: Any) {
    androidTestImplementation(dep)
    testImplementation(dep)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.material.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // ------------- Jetpack Compose ------------------
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    // Material Design 3
    implementation(libs.compose.material3)
    // Integration with activities
    implementation(libs.compose.activity)
    implementation(libs.compose.navigation)
    // Integration with ViewModels
    implementation(libs.compose.viewmodel)
    // Android Studio Preview support
    implementation(libs.compose.preview)
    debugImplementation(libs.compose.tooling)


    // ---------------------------------------------------
    // -------------------  PLUGINS  ---------------------
    // ---------------------------------------------------

    // ---------------- Firebase  ------------------
    implementation(libs.google.services)
    implementation(libs.play.services.auth)

    val firebaseBom = platform(libs.firebase.bom)
    implementation(firebaseBom)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage.ktx)

    // ---------------- OkHTTP ------------------
    implementation(libs.okhttp)

    // ---------------- GSON ------------------
    implementation(libs.gson)

    // ---------------- COIL ------------------
    implementation(libs.coil.compose)

    // ------------     Hilt      --------------
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.android)

    // ---------------- MapBox ------------------
    implementation(libs.mapbox.compose)
    implementation(libs.mapbox.android)
    implementation(libs.mapbox.sdk.turf)

    // ---------------------------------------------------
    // -------------------  TESTING  ---------------------
    // ---------------------------------------------------
    globalTestImplementation(composeBom)
    globalTestImplementation(libs.androidx.junit)
    globalTestImplementation(libs.androidx.espresso.core)

    globalTestImplementation(libs.hilt.android.testing)
    globalTestImplementation(libs.mockito.kotlin)

    globalTestImplementation(libs.compose.test.junit)
    debugImplementation(libs.compose.test.manifest)

    // ----------       UI Testing    ------------
    kaptAndroidTest(libs.hilt.android.compiler)
    androidTestImplementation(libs.androidx.espresso.intents)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.mockito.android)

    // ----------       Unit Tests    ------------
    kaptTest(libs.hilt.android.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.agent)
    testImplementation(libs.mockk.android)
    testImplementation(libs.mockito.inline)

}

tasks.withType<Test> {
    // Configure Jacoco for each tests
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register("jacocoTestReport", JacocoReport::class) {
    mustRunAfter("testDebugUnitTest", "connectedDebugAndroidTest")

    reports {
        xml.required = true
        html.required = true
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
    )

    val debugTree = fileTree("${project.layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.layout.projectDirectory}/src/main/java"
    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(project.layout.buildDirectory.get()) {
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        include("outputs/code_coverage/debugAndroidTest/connected/*/coverage.ec")
    })

    doLast {
        val jacocoReport = reports.xml.outputLocation.asFile.get()
        val newReport = jacocoReport.readText().replace("<line[^>]+nr=\"65535\"[^>]*>".toRegex(), "")
        jacocoReport.writeText(newReport)
    }

}

fun initLocalProps(): Properties {

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists())
        localProperties.load(localPropertiesFile.inputStream())

    val mapboxAccessToken = localProperties.getProperty("MAPBOX_ACCESS_TOKEN")
    if (mapboxAccessToken != null) {
        val mapboxAccessTokenXmlFile = file("src/main/res/values/mapbox_access_token.xml")
        val mapboxAccessTokenXmlContent = """
            <?xml version="1.0" encoding="utf-8"?>
            <resources xmlns:tools="http://schemas.android.com/tools">
                <string name="mapbox_access_token" translatable="false" tools:ignore="UnusedResources">$mapboxAccessToken</string>
            </resources>
        """.trimIndent()

        mapboxAccessTokenXmlFile.writeText(mapboxAccessTokenXmlContent)
    } else {
        throw GradleException("mapbox_access_token not found in local.properties")
    }
    return localProperties
}
