package com.greenebeans.cpplint
import com.greenebeans.cpplint.tasks.InstallCppLint
import com.greenebeans.cpplint.tasks.RunCppLint
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.model.ModelMap
import org.gradle.nativeplatform.NativeBinarySpec
import org.gradle.nativeplatform.NativeComponentSpec
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

    def "creates a run-lint task for each binary"() {
        def rules = new CppLintPlugin.Rules()
        ModelMap<Task> tasks = Mock()
        ModelMap<NativeBinarySpec> binaries = Mock()

        NativeComponentSpec componentSpec = Mock()
        NativeBinarySpec binary1 = Mock()
        NativeBinarySpec binary2 = Mock()

        when:
        rules.createRunLintTasks(tasks, binaries)
        then:
        2 * componentSpec.getName() >> "component"
        1 * binary1.getComponent() >> componentSpec
        1 * binary1.getName() >> "binary1"
        1 * binary2.getComponent() >> componentSpec
        1 * binary2.getName() >> "binary2"

        1 * binaries.iterator() >> {
            [ binary1, binary2 ].iterator()
        }
        1 * tasks.create("runLintComponentBinary1", RunCppLint, _)
        1 * tasks.create("runLintComponentBinary2", RunCppLint, _)
        0 * _
    }
}
