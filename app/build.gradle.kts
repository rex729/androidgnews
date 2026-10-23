import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
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
    }

    installation {
        // Grant runtime permissions on install (debug's ACCESS_LOCAL_NETWORK) so there's no prompt.
        installOptions += "-g"
    }

    // The app's own backend (../AndroidGNewsBackend). Both can be overridden in local.properties.
    buildTypes {
        debug {
            // 10.0.2.2 is the emulator's alias for this PC; reaching it needs ACCESS_LOCAL_NETWORK,
            // which only the debug manifest requests.
            buildConfigField(
                "String",
                "BACKEND_BASE_URL",
                "\"${localProperties.getProperty("BACKEND_BASE_URL", "http://10.0.2.2:8080/")}\"",
            )
        }
        release {
            // Must be the deployed HTTPS backend: release builds don't allow plain HTTP. The
            // placeholder fails fast (the .invalid domain never resolves) until a real URL is set.
            buildConfigField(
                "String",
                "BACKEND_BASE_URL",
                "\"${localProperties.getProperty("RELEASE_BACKEND_BASE_URL", "https://backend.invalid/")}\"",
            )
            isMinifyEnabled = false
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

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
