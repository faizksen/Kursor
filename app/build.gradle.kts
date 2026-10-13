plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kursor.compose)
    alias(libs.plugins.kursor.android.hilt)
}

android {
    namespace = "dev.faizksen.kursor"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "dev.faizksen.kursor"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":feature:watchlist"))
    implementation(project(":feature:converter"))
    implementation(project(":feature:alerts"))
    implementation(project(":feature:settings"))
    implementation(project(":core:sync"))
    implementation(project(":core:domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)

    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
