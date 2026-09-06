plugins {
    alias(libs.plugins.kursor.android.library)
    alias(libs.plugins.kursor.android.hilt)
    alias(libs.plugins.room)
}

android {
    namespace = "dev.faizksen.kursor.database"
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
