import org.gradle.api.Plugin
import org.gradle.api.Project

class TestingModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("myapp.android.library")
        }
    }
}
