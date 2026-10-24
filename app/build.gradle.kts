import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    // Reads app/google-services.json (from your Firebase project; not in git).
    alias(libs.plugins.google.services)
    // Uploads R8 mapping files on release builds so crash reports show original names and lines.
    alias(libs.plugins.firebase.crashlytics)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use(::load)
}

android {
    namespace = "com.well.androidgnews"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.well.androidgnews"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"

        // This branch calls GNews directly, so the key ships inside the APK (anyone can extract it,
        // and every install shares its daily quota). Kept out of git in local.properties.
        buildConfigField(
            "String",
            "GNEWS_API_KEY",
            "\"${localProperties.getProperty("GNEWS_API_KEY", "")}\"",
        )
    }

    // Release signing key, from local.properties (never in git; the repo is public). Use the Play
    // upload key here; Google Play re-signs with its own app signing key.
    val releaseStoreFile = localProperties.getProperty("RELEASE_STORE_FILE")
    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = file(releaseStoreFile)
                storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
                keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            // Until there's a real release key, sign with the debug key so the optimized APK still
            // installs and Google sign-in works (the debug SHA-1 is registered in Firebase). Not
            // for Play uploads.
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
            // R8: drop unused code and resources, shorten names.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        // Plain JVM unit tests only have stub android.* classes. OkHttp calls android.util.Log while
        // starting up, so let the stubs return defaults (false/0/null) instead of throwing.
        unitTests.isReturnDefaultValues = true
    }
}

// A release without a key would install fine and then fail every request; refuse to build it.
tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    doFirst {
        check(!localProperties.getProperty("GNEWS_API_KEY").isNullOrBlank()) {
            "Set GNEWS_API_KEY in local.properties before building release."
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.browser)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
