plugins {
    id("myapp.feature.api")
}

android {
    namespace = "com.myapp.feature.search.api"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.fragment.ktx)
}
