package net.satago.gradle.candy

import groovy.transform.ToString
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

@ToString
class CandyExtension {
    Project project
    String taskPrefix
    NamedDomainObjectContainer<CandyService> services
    NamedDomainObjectContainer<CandyRevision> revisions

    CandyExtension(Project project) {
        this.project = project
        services = project.container(CandyService,
                { String name ->
                    new CandyService(name: name, project: project)
                })
        revisions = project.container(CandyRevision,
                { String name ->
                    new CandyRevision(name: name, project: project)
                })
    }

    void services(Closure closure) {
        project.logger.info("Configuring services")
        services.configure(closure)
    }

    void revisions(Closure closure) {
        project.logger.info("Configuring revisions")
        revisions.configure(closure)
    }

    String getTaskName(String taskName) {
        if (taskPrefix == null || taskPrefix.trim().length() == 0) {
            return taskName
        }
        return taskPrefix + Character.toUpperCase(taskName.charAt(0)) + taskName.substring(1)
    }
}
