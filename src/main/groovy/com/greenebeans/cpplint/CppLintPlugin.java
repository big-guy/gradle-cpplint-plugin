package com.greenebeans.cpplint;

import com.greenebeans.cpplint.tasks.RunCppLint;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.AppliedPlugin;

public class CppLintPlugin implements Plugin<Project> {
    @Override
    public void apply(final Project project) {
        project.getPluginManager().withPlugin("org.gradle.cpp", new Action<AppliedPlugin>() {
            @Override
            public void execute(AppliedPlugin appliedPlugin) {
                project.getTasks().getByName("check").dependsOn(project.getTasks().withType(RunCppLint.class));
                project.getPluginManager().apply(Rules.class);
            }
        });
    }
}
