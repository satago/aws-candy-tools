package net.satago.gradle.candy

import groovy.transform.ToString
import org.gradle.api.Project

@ToString
class CandyService {
    Project project
    String name
    String ecrStackName
    DockerImageBuilder imageBuilder

    void ecrStackName(String ecrStackName) {
        this.ecrStackName = ecrStackName
    }

    void dockerJava(Closure closure) {
        imageBuilder = project.configure(new DockerJavaBuilder(), closure) as DockerImageBuilder
    }

    void composeBuild(Closure closure) {
        imageBuilder = project.configure(new DockerComposeBuilder(), closure) as DockerImageBuilder
    }
}

trait DockerImageBuilder {
}

@ToString
class DockerJavaBuilder implements DockerImageBuilder {
    Project project
    String taskName

    void project(Project project) {
        this.project = project
    }

    void taskName(String taskName) {
        this.taskName = taskName
    }
}

@ToString
class DockerComposeBuilder implements DockerImageBuilder {
    String serviceName

    void serviceName(String serviceName) {
        this.serviceName = serviceName
    }
}