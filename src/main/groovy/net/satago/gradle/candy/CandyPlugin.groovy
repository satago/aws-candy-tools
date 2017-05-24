package net.satago.gradle.candy

import net.satago.gradle.common.AppendFiles
import net.satago.gradle.common.DeferredReplaceTokens
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.function.Function

class CandyPlugin implements Plugin<Project> {

    //  This name also is a part of the META-INF/gradle-plugins/<PLUGIN_ID>.properties
    public static final String PLUGIN_ID = 'net.satago.gradle.aws-candy-tools'
    public static final String GRADLE_TASKS_GROUP = "AWS Candy Tools"
    public static final String BIN_EXEC = "exec"
    public static final String BIN_SET_AWS_PROFILE = "set-aws-profile"
    public static final String BIN_SET_BASTION_SSH = "set-bastion-ssh"

    private static final String GENERATED_FILE_MARKER = "# This is a generated file!  Do not edit."

    static class ImageInfo {
        Closure<String> getImageId = { '' }
        Closure<String> getImageName = { '' }
        Closure<String> getImageTag = { getImageName().split(':').last() }
        Closure<String> getEcrStackName = { '' }
    }

    static Function<File, String> toRelativePath(File root) {
        { file ->
            return root.toPath().relativize(file.toPath()).toString()
        }
    }

    @Override
    void apply(Project project) {

        project.configure(project) {
            project.logger.info("Creating extension")
            project.extensions.create("candy", CandyExtension, project)
        }

        project.afterEvaluate {
            project.logger.info("Creating tasks")
            createTasks(project, project.extensions.getByType(CandyExtension))
        }
    }

    private static void createTasks(Project project, CandyExtension extension) {
        def createRevisionsTask = project.task(extension.getTaskName('createRevisions')) {
            Task task ->
                task.group GRADLE_TASKS_GROUP
                task.description "Creates CodeDeploy revision folders for all configured revisions"
        }

        def index = extension.services.collectEntries(new HashMap<String, ImageInfo>(), { [it.name, new ImageInfo()] })

        //  Assuming the plugin was loaded from JAR file
        def bundleDirInsideJar = 'META-INF/aws-candy-tools'
        def bundlePathInsideJar = "/${bundleDirInsideJar}/bundle.tgz"
        def bundleURL = CandyPlugin.class.getResource(bundlePathInsideJar)
        def pluginJarLocation = bundleURL.toString().replaceAll('.*file:(/[^!]+).*', '$1')

        def tempDir = "${project.buildDir}/tmp/aws-candy-tools"
        def extractedBundleDir = "${tempDir}/bundle"

        def copyBundleTask = project.task(extension.getTaskName("copyBundle"), type: Copy) {
            Copy copy ->
                copy.group GRADLE_TASKS_GROUP
                copy.description "Copies plugin's resource bundle out from the plugin's JAR"

                copy.from project.zipTree(pluginJarLocation)
                copy.include bundlePathInsideJar
                copy.into tempDir
                copy.eachFile { fileCopyDetails ->
                    fileCopyDetails.path = fileCopyDetails.path.replaceAll(bundleDirInsideJar, '')
                }
                copy.doLast {
                    // https://issues.gradle.org/browse/GRADLE-1830
                    project.file("${tempDir}/META-INF").deleteDir()
                    outputs.getFiles().asFileTree.forEach {
                        project.logger.info "{}", it
                    }
                }
        }

        def untarBundleTask = project.task(extension.getTaskName("untarBundle"), type: Sync) {
            Sync sync ->
                sync.group GRADLE_TASKS_GROUP
                sync.description "Unpacks the plugin's resource bundle into a working directory"

                sync.dependsOn copyBundleTask

                sync.from project.tarTree("${tempDir}/bundle.tgz")
                sync.into extractedBundleDir
                sync.doLast {
                    outputs.getFiles().asFileTree.forEach {
                        project.logger.info "{}", it
                    }
                }
        }

        //  `createRevisionsTask` should be executed by this moment followed by `bin/deploy docker-tag-and-push ...`
        //  The latter is required to update docker image IDs in `compose.env` with ECR image URLs
        def tarRevisionsTask = project.task(extension.getTaskName("tarRevisions"), type: Tar) {
            tar ->
                tar.group GRADLE_TASKS_GROUP
                tar.description "Packages previously created CodeDeploy revisions into a self-contained redistributable archive"

                tar.baseName "revision"
                tar.into project.name
                tar.destinationDir project.file("${project.buildDir}/tar")
                tar.from(extractedBundleDir) {
                    include 'bin/'
                    exclude "bin/${BIN_EXEC}"
                }
                tar.from(project.projectDir) {
                    include 'bin/'
                    exclude "bin/${BIN_EXEC}"
                    //  Ignore symlinks
                    eachFile { FileCopyDetails fileCopyDetails ->
                        if (Files.isSymbolicLink(new File(fileCopyDetails.path).toPath())) {
                            fileCopyDetails.exclude()
                        }
                    }
                }
                tar.from(project.buildDir) {
                    into 'build'    // `bin/deploy` expects revisions to be in `../build/revisions/`
                    include 'revisions/'
                    exclude 'tar/'
                }
                tar.compression Compression.GZIP
                tar.doLast {
                    outputs.getFiles().asFileTree.forEach {
                        project.logger.info "{}", it
                    }
                }
        }

        project.task(extension.getTaskName("untarRevisions"), type: Sync) {
            Sync sync ->
                sync.group GRADLE_TASKS_GROUP
                sync.description "Extracts previously packaged revisions archive (for debugging purposes)"

                def file = project.file("${project.buildDir}/tar/${tarRevisionsTask.archiveName}")
                def targetDir = new File(file.absolutePath.substring(0, file.absolutePath.lastIndexOf('.')))
                if (!Objects.equals(file.parentFile, targetDir.parentFile)) {
                    throw new IllegalArgumentException("Unable to build a path for tar extraction from '${file.absolutePath}'." +
                            " File name doesn't have extension?")
                }

                sync.from project.tarTree(file)
                sync.into targetDir
                sync.doFirst {
                    project.logger.info "Untarring archive ${file.absolutePath}..."
                }
        }

        project.task(extension.getTaskName("binInit")) {
            Task task ->
                task.group GRADLE_TASKS_GROUP
                task.description "Initialises bin/ directory using plugin's resource bundle"

                task.dependsOn untarBundleTask

                task.doFirst {
                    //  Make './bin/' directory if doesn't exist
                    def binDir = new File(project.projectDir, 'bin')
                    binDir.mkdir()
                    new File(extractedBundleDir, 'bin').listFiles().each {
                        def sourceFile = it
                        def relativeSource = new File(toRelativePath(binDir).apply(it)).toPath()
                        def linkFile = new File(binDir, "${it.name}")
                        def link = linkFile.toPath()
                        if (linkFile.exists()) {
                            if (Files.isSymbolicLink(link)) {
                                if (Files.readSymbolicLink(link) == relativeSource) {
                                    return
                                }
                                throw new RuntimeException("Can't create a symbolic link to target '${relativeSource}'" +
                                        " as file '${linkFile.absolutePath}' already exists. Delete the file manually and rerun the task.")
                            }
                            if (linkFile.name == BIN_EXEC) {
                                def fileCopied = linkFile.withReader {
                                    def line = it.readLine()
                                    if (line == GENERATED_FILE_MARKER) {
                                        //  It's OK to rewrite the file
                                        Files.copy(sourceFile.toPath(), link,
                                                StandardCopyOption.COPY_ATTRIBUTES,
                                                StandardCopyOption.REPLACE_EXISTING
                                        )
                                        return true
                                    }
                                    return false
                                }

                                if (fileCopied) {
                                    return
                                }
                            }
                            if ([BIN_SET_AWS_PROFILE, BIN_SET_BASTION_SSH].contains(linkFile.name)) {
                                project.logger.info "Skipping ${linkFile.absolutePath} as the file already exists"
                                return
                            }
                            throw new RuntimeException("Can't overwrite the file '${linkFile.absolutePath}'." +
                                    " Delete the file manually and rerun the task.")
                        }
                        if ([BIN_EXEC, BIN_SET_AWS_PROFILE, BIN_SET_BASTION_SSH].contains(linkFile.name)) {
                            Files.copy(sourceFile.toPath(), link, StandardCopyOption.COPY_ATTRIBUTES)
                        } else {
                            Files.createSymbolicLink(link, relativeSource)
                        }
                    }
                }
        }

        extension.revisions.each { revision ->

            def copyTask = project.task(extension.getTaskName("copy-${revision.name}"), type: Sync) {
                Sync sync ->
                    sync.group GRADLE_TASKS_GROUP
                    sync.description "Copies files for CodeDeploy revision ${revision.name}"

                    sync.dependsOn untarBundleTask

                    sync.into "${project.buildDir}/revisions/${revision.name}"
                    sync.from("${extractedBundleDir}/codedeploy-common-files/") { it ->
                        it.exclude 'compose.env'
                    }
                    sync.from project.file("${extractedBundleDir}/bin/decrypt")
                    sync.from project.file("${extractedBundleDir}/bin/decrypt-file.py")
                    sync.from project.file("${extractedBundleDir}/bin/replace.py")
                    sync.from("${extractedBundleDir}/awslabs") { it ->
                        it.include 'elb/'
                    }
                    sync.from("${extractedBundleDir}/codedeploy-common-files/compose.env") { it ->
                        it.filter(DeferredReplaceTokens, tokenGenerator: {
                            [
                                    composeFiles  : revision.composeFiles.join(' '),
                                    buildTimestamp: new Date().format("yyyy-MM-dd HH:mm:ss ZZZ")
                            ]
                        })
                    }
                    revision.composeFiles.each { it ->
                        def file = project.file("apps/${it}")
                        if (!file.exists()) {
                            throw new InvalidUserDataException('docker-compose file not found: ' + file)
                        }
                        sync.from file
                    }
                    sync.with project.copySpec() {
                        it.into 'data'
                        it.from("apps/${revision.name}/") { it2 ->
                            //  `compose.env` file will be handled separately,
                            //  sync will be merged into single `compose.env`
                            it2.exclude 'compose.env'
                        }
                    }
                    sync.with revision.resources
            }

            def concatTask = project.task(extension.getTaskName("concat-${revision.name}"), type: AppendFiles) {
                AppendFiles append ->
                    append.group GRADLE_TASKS_GROUP
                    append.description "Concatenates `compose.env` files for CodeDeploy revision ${revision.name}"

                    append.dependsOn copyTask

                    append.files = project.fileTree("apps/${revision.name}") {
                        it.include 'compose.env'
                    }
                    append.target = project.file("${project.buildDir}/revisions/${revision.name}/compose.env")
                    append.doFirst {
                        append.target.append("\nDEPLOYABLE_NAME=${revision.name}")
                    }
                    append.doLast {
                        append.target.append("\nSERVICES=(${revision.services.join(' ').toUpperCase()})")

                        revision.services.each {
                            append.target.append("\n")
                            def prefix = it.toUpperCase()
                            append.target.append("\n${prefix}_IMAGE=${index[it].getImageName()}")
                            append.target.append("\n${prefix}_IMAGE_TAG=${index[it].getImageTag()}")
                            append.target.append("\n${prefix}_IMAGE_ID=${index[it].getImageId()}")
                            append.target.append("\n${prefix}_ECR_STACK_NAME=${index[it].getEcrStackName()}")
                        }
                    }
            }

            def createRevisionTask = project.task(extension.getTaskName("createRevision-${revision.name}")) {
                Task task ->
                    task.group GRADLE_TASKS_GROUP
                    task.description "Creates CodeDeploy revision folder for ${revision.name}"

                    task.dependsOn concatTask
            }

            createRevisionsTask.dependsOn createRevisionTask

            //  Not all revisions might need to build an image
            revision.services.each { service ->

                //  We could also support literal images, but we don't have such yet
                def imageBuilder = extension.services[service].imageBuilder

                index[service].getEcrStackName = { extension.services[service].ecrStackName }

                if (imageBuilder instanceof DockerComposeBuilder) {
                    //  Build image with docker-compose

                    def imageNamePrefix = "${revision.name}-${imageBuilder.serviceName}"
                    def imageTag = "${project.version}"

                    index[service].getImageName = { "${imageNamePrefix}:${imageTag}" }
                    index[service].getImageTag = { imageTag }
                    //  We won't know real image ID, but we don't really need it, full name of the image will work fine here as well
                    index[service].getImageId = index[service].getImageName

                    def composeBuildTask = project.task(
                            extension.getTaskName("composeBuild-${revision.name}-${imageBuilder.serviceName}"), type: Exec) {
                        it.workingDir = "${project.buildDir}/revisions/${revision.name}"
                        it.executable = './compose.bash'
                        it.args = ['build', imageBuilder.serviceName]
                    }

                    composeBuildTask.dependsOn concatTask

                    createRevisionTask.dependsOn composeBuildTask
                } else if (imageBuilder instanceof DockerJavaBuilder) {
                    //  Build image using DockerBuildImage Gradle task
                    def wireDockerBuildImageTask = { Task buildImage ->
                        copyTask.dependsOn buildImage
                        index[service].getImageId = { buildImage.getImageId() }
                        index[service].getImageName = { buildImage.getTag() }
                    }

                    def dockerBuildProject = imageBuilder.project as Project

                    if (dockerBuildProject == null) {
                        throw new RuntimeException("Error resolving docker image for revision '" + revision.name
                                + "': property 'dockerJava.project' is 'null' for service '" + service + "'")
                    }

                    def builderTaskName = imageBuilder.taskName
                    def dockerBuildImageTask = dockerBuildProject.tasks.findByName(builderTaskName)
                    if (dockerBuildImageTask == null) {
                        dockerBuildProject.afterEvaluate { evaluatedProject ->
                            def task = evaluatedProject.tasks.findByName(builderTaskName)
                            if (task != null) {
                                wireDockerBuildImageTask(task)
                            } else {
                                project.logger.warn "!!! ${revision.name}:" +
                                        " task '${builderTaskName}' not found in project '${evaluatedProject.name}'"
                                //  Cleanup
                                createRevisionsTask.dependsOn.remove createRevisionTask
                            }
                        }
                    } else {
                        wireDockerBuildImageTask(dockerBuildImageTask)
                    }
                } else {
                    throw new RuntimeException("Unsupported image builder (" + imageBuilder + ") for service " + service)
                }
            }
        }

        project.task("candyRunTask") {
            Task task ->
                task.group GRADLE_TASKS_GROUP
                task.description "Runs a task by its default name specified as `candyTaskName` project property. Default name must not include `taskPrefix`, i.e. `-PcandyTaskName=binInit`"

                def candyTaskNameProperty = "candyTaskName"
                if (project.hasProperty(candyTaskNameProperty)) {
                    def candyTaskName = (String) project.property(candyTaskNameProperty)
                    task.dependsOn project.getTasksByName(extension.getTaskName(candyTaskName), false)
                }
        }
    }
}