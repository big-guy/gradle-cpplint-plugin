package com.greenebeans.cpplint
import com.greenebeans.cpplint.tasks.InstallCppLint
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.model.ModelMap
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class CppLintPluginTest extends Specification {
    @Rule TemporaryFolder temporaryFolder = new TemporaryFolder()

    def "cpp lint plugin requires cpp plugin"() {
        given:
        def project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()
        when:
        project.apply(plugin: CppLintPlugin)
        then:
        project.pluginManager.hasPlugin("org.gradle.cpp")
    }

    def "creates a install task"() {
        def rules = new CppLintPlugin.Rules()
        ModelMap<Task> tasks = Mock()
        InstallCppLint installTask = Mock()
        File buildDir = temporaryFolder.newFolder("build")
        when:
        rules.addLintInstallTask(tasks, buildDir)
        then:
        1 * tasks.create("installCppLint", InstallCppLint, _) >> { args ->
            Action<InstallCppLint> action = args[2]
            action.execute(installTask)
        }

        1 * installTask.setDescription(_)
        1 * installTask.setDistUrl(_)
        1 * installTask.setInstallPath(_) >> { args ->
            File installPath = (File)args[0]
            assert buildDir == installPath.parentFile.parentFile
        }
        1 * installTask.setSkipInstall(false)

        0 * _
    }
}
