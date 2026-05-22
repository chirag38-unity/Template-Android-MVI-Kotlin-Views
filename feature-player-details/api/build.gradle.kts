plugins {
    id("myapp.feature.api")
}

android {
    namespace = "com.myapp.feature.player.details.api"
}

dependencies {
    // Player model lives in feature-feed:api; expose it transitively
    api(project(":feature-feed:api"))
    implementation(libs.androidx.fragment.ktx)
}
