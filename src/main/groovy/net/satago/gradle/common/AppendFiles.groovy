package net.satago.gradle.common

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class AppendFiles extends DefaultTask {
    @InputFiles
    FileCollection files
    @OutputFile
    File target

    @TaskAction
    void append() {
        target.withWriterAppend { writer ->
            files.each { file ->
                def fileName = project.rootDir.toPath().relativize(file.toPath())
                file.withReader { reader ->
                    writer << "\n########"
                    writer << "\n## Begin $fileName"
                    writer << "\n##\n"
                    writer << reader
                    writer << "\n##"
                    writer << "\n## End $fileName"
                    writer << "\n########\n"
                }
            }
        }
    }
}
