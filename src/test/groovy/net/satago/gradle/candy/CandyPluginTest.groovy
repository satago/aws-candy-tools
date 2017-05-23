package net.satago.gradle.candy

import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import java.nio.file.Files
import java.util.function.Predicate
import java.util.stream.Collectors

import static net.satago.gradle.candy.CandyPlugin.BIN_EXEC
import static net.satago.gradle.candy.CandyPlugin.BIN_SET_AWS_PROFILE
import static net.satago.gradle.candy.CandyPlugin.BIN_SET_BASTION_SSH
import static net.satago.gradle.candy.CandyPlugin.toRelativePath
import static org.apache.commons.io.filefilter.TrueFileFilter.INSTANCE
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class CandyPluginTest {
    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder()

    @Test
    void applyPluginToEmptyProject() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply CandyPlugin.PLUGIN_ID

        project.evaluate()

        assertTrue(project.tasks.createRevisions instanceof DefaultTask)
    }

    @Test
    void createRevisions() {
        def projectRoot = new File('src/test/resources/project-A')
        FileUtils.copyDirectory(
                projectRoot,
                testProjectDir.getRoot())

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withPluginClasspath()
                .withArguments('--stacktrace', '--info', 'createRevisions')
                .withDebug(true)
                .forwardOutput()
                .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(':createRevisions').outcome)
        assertEquals(
                ['revision-A/appspec.yml',
                 'revision-A/compose.bash',
                 'revision-A/compose.env',
                 'revision-A/data/config.properties',
                 'revision-A/data/service-a/Dockerfile',
                 'revision-A/data/service-b/Dockerfile',
                 'revision-A/decrypt',
                 'revision-A/decrypt-file.py',
                 'revision-A/decrypt-properties.bash',
                 'revision-A/docker-compose.service-A.yml',
                 'revision-A/elb/common_functions.sh',
                 'revision-A/elb/deregister_from_elb.sh',
                 'revision-A/elb/README.md',
                 'revision-A/elb/register_with_elb.sh',
                 'revision-A/elb/wait_for_elb.sh',
                 'revision-A/pre-hooks.bash',
                 'revision-A/pull.bash',
                 'revision-A/replace.py',
                 'revision-A/stop.bash',
                 'revision-A/symlink.bash',
                 'revision-A/up.bash',
                 'revision-A/validate.bash'
                ],
                listFiles(new File(testProjectDir.getRoot(), 'build/revisions'), { true }))

        def composeEnv = new File(testProjectDir.getRoot(), 'build/revisions/revision-A/compose.env').text

        println composeEnv
    }

    @Test
    void tarUntarRevisions() {
        def projectRoot = new File('src/test/resources/project-A')
        FileUtils.copyDirectory(
                projectRoot,
                testProjectDir.getRoot())

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withPluginClasspath()
                .withArguments('--stacktrace', '--info', 'tarRevisions', 'untarRevisions')
                .withDebug(true)
                .forwardOutput()
                .build()

        def tarName = "revision.tgz"
        def pathToContent = "${tarName.replace('.tgz', '')}/${testProjectDir.getRoot().name}".toString()

        assertEquals(TaskOutcome.SUCCESS, result.task(':tarRevisions').outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(':untarRevisions').outcome)
        assertEquals(
                [pathToContent + '/bin/decrypt',
                 pathToContent + '/bin/decrypt-file.py',
                 pathToContent + '/bin/deploy',
                 pathToContent + '/bin/encrypt',
                 pathToContent + '/bin/encrypt-file',
                 pathToContent + '/bin/encrypt-file.py',
                 pathToContent + '/bin/fleet',
                 pathToContent + '/bin/httping',
                 pathToContent + '/bin/replace.py',
                 pathToContent + '/bin/stack',
                 pathToContent + '/build/revisions/revision-A/appspec.yml',
                 pathToContent + '/build/revisions/revision-A/compose.bash',
                 pathToContent + '/build/revisions/revision-A/compose.env',
                 pathToContent + '/build/revisions/revision-A/data/config.properties',
                 pathToContent + '/build/revisions/revision-A/data/service-a/Dockerfile',
                 pathToContent + '/build/revisions/revision-A/data/service-b/Dockerfile',
                 pathToContent + '/build/revisions/revision-A/decrypt',
                 pathToContent + '/build/revisions/revision-A/decrypt-file.py',
                 pathToContent + '/build/revisions/revision-A/decrypt-properties.bash',
                 pathToContent + '/build/revisions/revision-A/docker-compose.service-A.yml',
                 pathToContent + '/build/revisions/revision-A/elb/common_functions.sh',
                 pathToContent + '/build/revisions/revision-A/elb/deregister_from_elb.sh',
                 pathToContent + '/build/revisions/revision-A/elb/README.md',
                 pathToContent + '/build/revisions/revision-A/elb/register_with_elb.sh',
                 pathToContent + '/build/revisions/revision-A/elb/wait_for_elb.sh',
                 pathToContent + '/build/revisions/revision-A/pre-hooks.bash',
                 pathToContent + '/build/revisions/revision-A/pull.bash',
                 pathToContent + '/build/revisions/revision-A/replace.py',
                 pathToContent + '/build/revisions/revision-A/stop.bash',
                 pathToContent + '/build/revisions/revision-A/symlink.bash',
                 pathToContent + '/build/revisions/revision-A/up.bash',
                 pathToContent + '/build/revisions/revision-A/validate.bash',
                 tarName
                ],
                listFiles(new File(testProjectDir.getRoot(), 'build/tar'), { true }))
    }

    private static List listFiles(File root, Predicate<String> filter) {
        FileUtils.listFiles(root, INSTANCE, INSTANCE)
                .stream()
                .map(toRelativePath(root))
                .filter(filter)
                .collect(Collectors.toList())
    }

    @Test
    void binInit() {
        def projectRoot = new File('src/test/resources/project-B')
        FileUtils.copyDirectory(
                projectRoot,
                testProjectDir.getRoot())

        runAndAssertBinInitSucceeded()
        // Second time shouldn't fail, i.e. should be idempotent
        runAndAssertBinInitSucceeded()
    }

    private void runAndAssertBinInitSucceeded() {
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withPluginClasspath()
                .withArguments('--stacktrace', '--info', 'binInit')
                .withDebug(true)
                .forwardOutput()
                .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(':binInit').outcome)

        def binDir = new File(testProjectDir.getRoot(), "bin")

        assertEquals(
                ['decrypt',
                 'decrypt-file.py',
                 'deploy',
                 'encrypt',
                 'encrypt-file',
                 'encrypt-file.py',
                 'exec',
                 'fleet',
                 'httping',
                 'replace.py',
                 'set-aws-profile',
                 'set-bastion-ssh',
                 'stack'],
                listFiles(binDir, { true }))

        FileUtils.listFiles(binDir, INSTANCE, INSTANCE).each {
            if ([BIN_EXEC, BIN_SET_AWS_PROFILE, BIN_SET_BASTION_SSH].contains(it.name)) {
                return
            }
            assertTrue("${it} must be symlink", Files.isSymbolicLink(it.toPath()))
            def link = Files.readSymbolicLink(it.toPath())
            assertEquals("Symlink must be relative: ${link}",
                    "../build/tmp/aws-candy-tools/bundle/bin/${it.name}",
                    "${link}")
        }
    }

    @Test
    void applyPluginWithCustomTaskPrefix() {
        def projectRoot = new File('src/test/resources/project-C')
        FileUtils.copyDirectory(
                projectRoot,
                testProjectDir.getRoot())

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withPluginClasspath()
                .withArguments('--stacktrace', '--info', 'candyRunTask', "-PcandyTaskName=binInit")
                .withDebug(true)
                .forwardOutput()
                .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(':candyRunTask').outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(':fooBinInit').outcome)
    }

}
