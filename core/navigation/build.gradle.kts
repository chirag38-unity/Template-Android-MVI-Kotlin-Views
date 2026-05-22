plugins {
    id("myapp.navigation.module")
}

android {
    namespace = "com.myapp.core.navigation"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.material)
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.compiler)
}
