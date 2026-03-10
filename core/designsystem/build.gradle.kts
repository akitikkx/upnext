plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.theupnextapp.core.designsystem"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:common"))
    implementation(libs.appcompat)

    implementation(libs.android.ktx)
    implementation(libs.activity.compose)
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation(libs.android.material3)
    implementation(libs.material3.window.size.classes)
    implementation(libs.android.material3.adaptive)
    implementation(libs.android.material3.adaptive.layout)
    implementation(libs.android.material3.adaptive.navigation)
    implementation(libs.android.material3.adaptive.navigation.suite)

    implementation(libs.material.icons.extended)
    implementation(libs.animation)
    implementation(libs.compose.shimmer)
    implementation(libs.coil.compose)

    detektPlugins(libs.detekt.formatting)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
