plugins {
    `kotlin-dsl`
}

group = "com.myapp.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "myapp.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "myapp.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("kotlinLibrary") {
            id = "myapp.kotlin.library"
            implementationClass = "KotlinLibraryConventionPlugin"
        }
        register("coreModule") {
            id = "myapp.core.module"
            implementationClass = "CoreModuleConventionPlugin"
        }
        register("navigationModule") {
            id = "myapp.navigation.module"
            implementationClass = "NavigationModuleConventionPlugin"
        }
        register("featureApiModule") {
            id = "myapp.feature.api"
            implementationClass = "FeatureApiModuleConventionPlugin"
        }
        register("featureImplModule") {
            id = "myapp.feature.impl"
            implementationClass = "FeatureImplModuleConventionPlugin"
        }
        register("androidHilt") {
            id = "myapp.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidRoom") {
            id = "myapp.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
    }
}
