package com.greenebeans.cpplint;

import com.greenebeans.cpplint.tasks.InstallCppLint;
import com.greenebeans.cpplint.tasks.RunCppLint;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.language.base.LanguageSourceSet;
import org.gradle.model.*;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.util.CollectionUtils;

import java.io.File;

@SuppressWarnings("unused")
public class Rules extends RuleSource {
    private static final String INSTALL_TASK_NAME = "installCppLint";

    @Defaults
    public void addLintInstallTask(ModelMap<Task> tasks) {
        tasks.create(INSTALL_TASK_NAME, InstallCppLint.class);
    }

    @Mutate
    public void createRunLintTasks(ModelMap<Task> tasks, @Path("binaries") ModelMap<NativeBinarySpec> binaries) {
        for (final NativeBinarySpec binary : binaries) {
            String taskName = buildRunLintTaskName(binary.getComponent().getName(), binary.getName());
            tasks.create(buildRunLintTaskName(binary.getComponent().getName(), binary.getName()), RunCppLint.class, new Action<RunCppLint>() {
                public void execute(RunCppLint task) {
                    task.setNativeBinarySpec(binary);
                    task.source(CollectionUtils.collect(binary.getInputs(), new Transformer<Iterable<File>, LanguageSourceSet>() {
                        @Override
                        public Iterable<File> transform(LanguageSourceSet o) {
                            return o.getSource();
                        }
                    }));
                }
            });
        }
    }

    @Mutate
    public void configureRunLintTask(@Each RunCppLint runTask,
                                     @Path("tasks.installCppLint") final InstallCppLint installTask) {
        runTask.setDescription("Runs cpplint.py");
        runTask.setGroup("verification");
        runTask.setExecutablePath(installTask.getInstallPath().getAbsolutePath());
        runTask.dependsOn(installTask);
    }

    private String buildRunLintTaskName(String componentName, String binaryName) {
        StringBuilder sb = new StringBuilder("runLint");
        sb.append(Character.toTitleCase(componentName.charAt(0))).append(componentName.substring(1));
        sb.append(Character.toTitleCase(binaryName.charAt(0))).append(binaryName.substring(1));
        return sb.toString();
    }
}
