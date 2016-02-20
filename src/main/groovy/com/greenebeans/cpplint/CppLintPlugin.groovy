package com.greenebeans.cpplint

import com.greenebeans.cpplint.tasks.InstallCppLint
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.model.RuleSource

class CppLintPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.withPlugin("org.gradle.cpp") {
            project.tasks.create("installCppLint", InstallCppLint)
            project.apply(plugin: Rules)
        }
    }

    public static class Rules extends RuleSource {

    }
}
