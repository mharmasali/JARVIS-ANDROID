plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val aiBaseUrl = providers.gradleProperty("JARVIS_AI_BASE_URL")
    .orElse("")
    .get()

val aiApiKey = providers.gradleProperty("JARVIS_AI_API_KEY")
    .orElse("")
    .get()

val aiModel = providers.gradleProperty("JARVIS_AI_MODEL")
    .orElse("")
    .get()

fun escapeConfigValue(value: String): String {
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
}

android {
    namespace = "com.harmas.jarvis"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.harmas.jarvis"
        minSdk = 26
        targetSdk = 35

        versionCode = 1
        versionName = "1.0"

        buildConfigField(
            "String",
            "JARVIS_AI_BASE_URL",
            "\"${escapeConfigValue(aiBaseUrl)}\""
        )

        buildConfigField(
            "String",
            "JARVIS_AI_API_KEY",
            "\"${escapeConfigValue(aiApiKey)}\""
        )

        buildConfigField(
            "String",
            "JARVIS_AI_MODEL",
            "\"${escapeConfigValue(aiModel)}\""
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
}
