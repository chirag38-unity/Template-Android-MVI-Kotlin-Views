import org.gradle.api.Plugin
import org.gradle.api.Project

class NavigationModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("myapp.android.library")
                apply("com.google.dagger.hilt.android")
                apply("com.google.devtools.ksp")
            }
        }
    }
}
