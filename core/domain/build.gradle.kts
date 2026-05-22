plugins {
    id("myapp.core.module")
}

android {
    namespace = "com.myapp.core.domain"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.bundles.coroutines)
}
