package org.bonitasoft.engine.gradle.docker

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension

/**
 * @author Emmanuel Duchastenier
 */
class DockerDatabasePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.configurations {
            drivers
        }

        project.dependencies {
            // the following jdbc drivers are available for integration tests
            drivers(project.extensions.getByType(VersionCatalogsExtension.class).named("libs")
                    .findLibrary("mysql").get())
            drivers(project.extensions.getByType(VersionCatalogsExtension.class).named("libs")
                    .findLibrary("oracle").get())
            drivers(project.extensions.getByType(VersionCatalogsExtension.class).named("libs")
                    .findLibrary("postgresql").get())
            drivers(project.extensions.getByType(VersionCatalogsExtension.class).named("libs")
                    .findLibrary("msSqlServer").get())
        }

        def databaseIntegrationTest = project.extensions.create("databaseIntegrationTest", DatabasePluginExtension)

        DockerDatabaseContainerTasksCreator.createTasks(project, databaseIntegrationTest)

        project.afterEvaluate {
            if (!databaseIntegrationTest.includes) {
                println "No databaseIntegrationTest.include found. No tests to run!"
            }
        }
    }

}
