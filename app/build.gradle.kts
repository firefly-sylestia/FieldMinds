plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.curio.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.curio.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0-curio"

        // Only include English locale — saves ~5-8 MB of APK size.
        // Curio ships as a single-language app. Add others as needed.
        androidResources.localeFilters.clear()
        androidResources.localeFilters.add("en")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
            )
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Icons are rendered via Material Symbols font ligatures (CurioIcon), NOT
    // the bundled M2 vector set, so androidx.compose.material.icons.core is
    // intentionally absent. Re-add only if a screen needs an M2 vector icon.
    implementation(libs.androidx.compose.animation)
    implementation(libs.org.jetbrains.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
