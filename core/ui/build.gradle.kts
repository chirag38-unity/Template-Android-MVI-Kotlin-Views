plugins {
    id("myapp.core.module")
}

android {
    namespace = "com.myapp.core.ui"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.bundles.coroutines)
}
