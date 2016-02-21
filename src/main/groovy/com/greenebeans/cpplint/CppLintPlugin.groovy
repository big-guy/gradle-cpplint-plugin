/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.greenebeans.cpplint

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
            project.tasks["check"].dependsOn project.tasks.withType(RunCppLint)
        }
    }

    public static class Rules extends RuleSource {
        @Mutate
        void createRunLintTasks(ModelMap<Task> tasks, @Path("binaries") ModelMap<NativeBinarySpec> binaries) {
            for (NativeBinarySpec binary : binaries) {
                def taskName = buildRunLintTaskName(binary.component.name, binary.name)
                if (tasks.get(taskName)==null) {
                    // TODO: This shouldn't be necessary
                    tasks.create(buildRunLintTaskName(binary.component.name, binary.name), RunCppLint, new ConfigureRunLintTask(binary))
                }
            }
        }

        @Mutate
        void configureRunLintTask(@Each RunCppLint runTask,
                                  @Path("tasks.installCppLint") InstallCppLint installTask) {
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
            runCppLint.nativeBinarySpec = nativeBinarySpec
            runCppLint.source(CollectionUtils.collect(nativeBinarySpec.getInputs(), new Transformer<Iterable<File>, LanguageSourceSet>() {
                @Override
                Iterable<File> transform(LanguageSourceSet o) {
                    return o.source
                }
            }))
        }
    }
}
