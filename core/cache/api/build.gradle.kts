plugins {
    id("myapp.core.module")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.myapp.core.cache.api"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.serialization.json)
}
