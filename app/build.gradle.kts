plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// ── Release signing wiring ───────────────────────────────────────────────────
//
// Curio consumes the existing KEYSTORE_* secrets that were carried over from
// the legacy fieldmind build's android.yml CI pipeline (KEYSTORE_BASE64 +
// KEYSTORE_PASSWORD + KEY_ALIAS + KEY_PASSWORD). The CI workflow decodes the
// base64-encoded keystore to ./release.keystore and exports KEYSTORE_PATH etc.
// as env vars at build time, which we read here.
//
// Local dev (no env vars set): falls back to the default debug signing config,
// so `gradlew assembleRelease` still produces an installable-but-debug-keyed
// APK. CI: produces a properly-signed release APK.
val keyStorePath: String? = System.getenv("KEYSTORE_PATH")
val keyStorePassword: String? = System.getenv("KEYSTORE_PASSWORD")
val keyAlias: String? = System.getenv("KEY_ALIAS")
val keyPassword: String? = System.getenv("KEY_PASSWORD")
val hasReleaseSigningMaterial: Boolean =
    keyStorePath != null && keyStorePassword != null &&
    keyAlias != null && keyPassword != null

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

    signingConfigs {
        // Only create the release signing config when ALL four env vars are
        // present. When any are missing (e.g. local dev), we skip — the
        // release buildType falls back to the default debug signing below so
        // local `gradlew assembleRelease` still works for testing.
        if (hasReleaseSigningMaterial) {
            create("release") {
                storeFile = file(keyStorePath!!)
                storePassword = keyStorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = if (hasReleaseSigningMaterial) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
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
