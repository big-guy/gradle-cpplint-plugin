package com.greenebeans.cpplint

import com.greenebeans.cpplint.CppLintPlugin.Rules
import com.greenebeans.cpplint.tasks.InstallCppLint
import com.greenebeans.cpplint.tasks.RunCppLint
import org.gradle.api.*
import org.gradle.language.base.LanguageSourceSet
import org.gradle.model.*
import org.gradle.nativeplatform.NativeBinarySpec
import org.gradle.util.CollectionUtils

class CppLintPlugin implements Plugin<Project> {
    private static final String INSTALL_TASK_NAME = "installCppLint"

    @Override
    void apply(Project project) {
        project.pluginManager.withPlugin("org.gradle.cpp") {
            project.tasks.create(INSTALL_TASK_NAME, InstallCppLint)
            project.apply(plugin: Rules)
        }
    }

    public static class Rules extends RuleSource {
        @Mutate
        void createRunLintTasks(ModelMap<Task> tasks, @Path("binaries") ModelMap<NativeBinarySpec> binaries) {
            println "createRunLintTasks"
            for (NativeBinarySpec binary : binaries) {
                def taskName = buildRunLintTaskName(binary.component.name, binary.name)
                if (tasks.get(taskName)==null) {
                    tasks.create(buildRunLintTaskName(binary.component.name, binary.name), RunCppLint, new ConfigureRunLintTask(binary))
                }
            }
        }

        @Finalize
        void configureRunLintTask(@Each RunCppLint runTask, @Path("tasks.installCppLint") InstallCppLint installTask) {
            runTask.with {
                description = "Runs cpplint.py"
                group = "verification"
                executablePath = installTask.installPath.absolutePath
                dependsOn installTask
            }
        }

        private String buildRunLintTaskName(String componentName, String binaryName) {
            return "runLint" + componentName.capitalize() + binaryName.capitalize()
        }
    }

    private static class ConfigureRunLintTask implements Action<RunCppLint> {
        NativeBinarySpec nativeBinarySpec
        ConfigureRunLintTask(NativeBinarySpec nativeBinarySpec) {
            this.nativeBinarySpec = nativeBinarySpec
        }

        @Override
        void execute(RunCppLint runCppLint) {
            println "sources? " + nativeBinarySpec.sources.values()
            runCppLint.source(CollectionUtils.collect(nativeBinarySpec.getInputs(), new Transformer<Iterable<File>, LanguageSourceSet>() {
                @Override
                Iterable<File> transform(LanguageSourceSet o) {
                    println "Adding " + o.source.name
                    println "contents " + o.source.files
                    return o.source
                }
            }))
        }
    }
}
