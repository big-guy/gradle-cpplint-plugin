package com.greenebeans.cpplint;

import com.greenebeans.cpplint.tasks.InstallCppLint;
import com.greenebeans.cpplint.tasks.RunCppLint;
import org.gradle.api.*;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.language.base.LanguageSourceSet;
import org.gradle.model.*;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.util.CollectionUtils;

import java.io.File;
import java.util.concurrent.Callable;

public class CppLintPlugin implements Plugin<Project> {
    @Override
    public void apply(final Project project) {
        project.getPluginManager().apply("org.gradle.cpp");
    }

    @SuppressWarnings("unused")
    public static class Rules extends RuleSource {
        private static final String INSTALL_TASK_NAME = "installCppLint";

        @Defaults
        public void addLintInstallTask(ModelMap<Task> tasks,
                                       @Path("buildDir") final File buildDir) {
            tasks.create(INSTALL_TASK_NAME, InstallCppLint.class, new Action<InstallCppLint>() {
                @Override
                public void execute(InstallCppLint installCppLint) {
                    installCppLint.setDescription("Installs cpplint.py");
                    installCppLint.setDistUrl("https://raw.githubusercontent.com/google/styleguide/b43afc71a5ae4a2585a583333b45ce664cd2c3c6/cpplint/cpplint.py");
                    installCppLint.setInstallPath(new File(buildDir, "cpplint/cpplint.py"));
                    installCppLint.setSkipInstall(false);
                }
            });
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
        public void configureRunLintTask(@Path("tasks") ModelMap<RunCppLint> runTasks,
                                         @Path("tasks.installCppLint") final InstallCppLint installTask) {
            runTasks.afterEach(new Action<RunCppLint>() {
                @Override
                public void execute(RunCppLint runTask) {
                    runTask.setDescription("Runs cpplint.py");
                    runTask.setGroup("verification");
                    runTask.setExecutablePath(installTask.getInstallPath().getAbsolutePath());
                    runTask.dependsOn(installTask);
                }
            });
        }

        @Mutate
        public void makeCheckDependentOnRunLintTasks(@Path("tasks.check") final Task check) {
            check.dependsOn(new Callable<TaskCollection<RunCppLint>>() {
                @Override
                public TaskCollection<RunCppLint> call() throws Exception {
                    return check.getProject().getTasks().withType(RunCppLint.class);
                }
            });
        }

        private String buildRunLintTaskName(String componentName, String binaryName) {
            StringBuilder sb = new StringBuilder("runLint");
            sb.append(Character.toTitleCase(componentName.charAt(0))).append(componentName.substring(1));
            sb.append(Character.toTitleCase(binaryName.charAt(0))).append(binaryName.substring(1));
            return sb.toString();
        }
    }
}
