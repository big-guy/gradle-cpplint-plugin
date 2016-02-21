package com.greenebeans.cpplint

import org.gradle.testkit.runner.TaskOutcome

class CppLintIntegrationSpec extends AbstractIntegrationSpec {
    def setup() {
        buildFile << """
    apply plugin: 'com.greenebeans.cpplint'
    apply plugin: 'org.gradle.cpp'

    model {
       components {
          main(NativeExecutableSpec)
       }
    }
"""
        def srcDir = file("src/main/cpp")
        srcDir.mkdirs()
        def srcFile = file("src/main/cpp/main.cpp")
        srcFile << """
// Copyright 2016 Example

    int main(int argc, char** argv) {
       return 0;
    }
"""
    }

    def "installs cpplint.py"() {
        when:
        build("installCppLint")
        then:
        result.task(":installCppLint").outcome == TaskOutcome.SUCCESS
        file("build/cpplint/cpplint.py").exists()
        file("build/cpplint/cpplint.py").canExecute()

        when:
        build("installCppLint")
        then:
        result.task(":installCppLint").outcome == TaskOutcome.UP_TO_DATE
    }

    def "adds a cpplint task for component"() {
        expect:
        build("tasks")
        result.output.contains("runLintMainExecutable - Runs cpplint.py")
    }

    def "runs cpplint"() {
        when:
        build("check")
        then:
        result.task(":runLintMainExecutable").outcome == TaskOutcome.SUCCESS

        when:
        build("check")
        then:
        result.task(":runLintMainExecutable").outcome == TaskOutcome.UP_TO_DATE
    }
}
