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

// ── Topic data validation (CURIO_DATA_PLAN.md §5.2 step 3) ─────────────────
//
// Validates every JSON file under app/src/main/assets/topics/*.json against
// the §2 schema. Asserts:
//   - root has categoryId, version, curatedDate, topics
//   - topics array non-empty
//   - all `id`s unique within the file
//   - every topic has id/subtype/name/teaser/imageUrl/actionPrompt
//   - every actionPrompt has verb/targetName/durationMinutes/instruction
//   - every instruction <= 280 chars
//
// When assets/topics/ contains any JSON files, this task is wired into
// preBuild so a malformed entry fails the assemble. When the directory is
// empty (placeholder UI ships), the task is a no-op and preBuild is not
// affected.
val topicsDir = file("src/main/assets/topics")
val hasTopicFiles: Boolean = topicsDir.exists() &&
    topicsDir.listFiles { f -> f.extension == "json" }?.isNotEmpty() == true

tasks.register("validateTopics") {
    group = "verification"
    description = "Validates assets/topics/*.json against the CurioTopic schema (CURIO_DATA_PLAN.md §2)."
    doLast {
        if (!topicsDir.exists()) {
            logger.warn("topics/ directory missing — nothing to validate (OK for placeholder UI ships).")
            return@doLast
        }
        val jsonFiles = topicsDir.listFiles { f -> f.extension == "json" } ?: emptyArray()
        if (jsonFiles.isEmpty()) {
            logger.warn("topics/ has no JSON files — nothing to validate.")
            return@doLast
        }
        val parser = groovy.json.JsonSlurper()
        // Collect every id across all files first so we can assert global
        // uniqueness (cross-file collisions would break the Room FK on `id`).
        val seenIds = mutableMapOf<String, String>()  // id -> first filename
        jsonFiles.forEach { json ->
            @Suppress("UNCHECKED_CAST")
            val root = parser.parse(json) as Map<String, Any?>
            val categoryId = root["categoryId"] as? String
                ?: throw GradleException("${json.name}: missing or non-string `categoryId`")
            require(categoryId == json.nameWithoutExtension.uppercase()) {
                "${json.name}: categoryId '$categoryId' does not match filename '${json.nameWithoutExtension.uppercase()}'"
            }
            @Suppress("UNCHECKED_CAST")
            val topics = root["topics"] as? List<Map<String, Any?>>
                ?: throw GradleException("${json.name}: missing or non-array `topics`")
            require(topics.isNotEmpty()) { "${json.name}: `topics` array is empty" }
            topics.forEachIndexed { idx, t ->
                val id = t["id"] as? String
                    ?: throw GradleException("${json.name}: topic #$idx missing or non-string `id`")
                val previousFile = seenIds[id]
                if (previousFile != null) {
                    throw GradleException(
                        "duplicate topic id '$id' across files: first seen in $previousFile, also in ${json.name}"
                    )
                }
                seenIds[id] = json.name
                listOf("subtype", "name", "teaser", "imageUrl", "actionPrompt").forEach { f ->
                    require(t.containsKey(f)) {
                        throw GradleException("${json.name}: topic '$id' missing required field `$f`")
                    }
                }
                @Suppress("UNCHECKED_CAST")
                val prompt = t["actionPrompt"] as Map<String, Any?>
                listOf("verb", "targetName", "durationMinutes", "instruction").forEach { f ->
                    require(prompt.containsKey(f)) {
                        throw GradleException("${json.name}: topic '$id' actionPrompt missing required field `$f`")
                    }
                }
                val instruction = prompt["instruction"] as? String
                    ?: throw GradleException("${json.name}: topic '$id' actionPrompt.instruction missing or non-string")
                require(instruction.length <= 280) {
                    throw GradleException("${json.name}: topic '$id' instruction is ${instruction.length} chars (max 280)")
                }
            }
            logger.lifecycle("✓ ${json.name}: $categoryId, ${topics.size} topics validated")
        }
    }
}

// Only hook validateTopics into preBuild when there's actually JSON to check.
// Keeps placeholder-UI builds (no topics yet) friction-free.
if (hasTopicFiles) {
    tasks.named("preBuild") {
        dependsOn("validateTopics")
    }
}
