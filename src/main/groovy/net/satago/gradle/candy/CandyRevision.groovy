package net.satago.gradle.candy

import groovy.transform.ToString
import org.gradle.api.Project
import org.gradle.api.file.CopySpec

import static java.util.Collections.emptyList

@ToString
class CandyRevision {
    String namespace
    String localName
    Project project
    String name
    List<String> composeFiles
    List<String> services
    CopySpec resources

    void composeFiles(String... composeFiles) {
        this.composeFiles = new ArrayList<>()
        this.composeFiles.addAll composeFiles
    }

    List<String> getComposeFiles() {
        return composeFiles ?: emptyList()
    }

    void services(String... services) {
        this.services = new ArrayList<>()
        this.services.addAll services
    }

    List<String> getServices() {
        return services ?: emptyList()
    }

    void resources(CopySpec resources) {
        this.resources = resources
    }

    CopySpec getResources() {
        return resources ?: project.copySpec()
    }

    String getSafeFQName() {
        return name.replaceAll('/', '-')
    }
}
