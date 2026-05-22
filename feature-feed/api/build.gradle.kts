plugins {
    id("myapp.feature.api")
    id("kotlin-parcelize")
}

android {
    namespace = "com.myapp.feature.feed.api"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.fragment.ktx)
}
