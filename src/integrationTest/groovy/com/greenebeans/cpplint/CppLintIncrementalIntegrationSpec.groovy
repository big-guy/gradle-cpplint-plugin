package com.greenebeans.cpplint

import org.gradle.testkit.runner.TaskOutcome

class CppLintIncrementalIntegrationSpec extends AbstractIntegrationSpec {
    def setup() {
        buildFile << """
plugins {
    id 'com.greenebeans.cpplint'
}

model {
    components {
        main(NativeExecutableSpec)
    }
}
"""
        def srcDir = file("src/main/cpp")
        srcDir.mkdirs()
        50.times {
            file("src/main/cpp/src${it}.cpp") << """
// Copyright 2016 Example
int foo${it}() {
    return 0;
}
"""
        }
    }

    void assertLogContains(msg, count) {
        assert new File(temporaryFolder.root, "build/reports/cpplint/output.txt").text.count(msg) == count
    }

    def "runs cpplint"() {
        when:
        build("check")
        then:
        result.task(":runLintMainExecutable").outcome == TaskOutcome.SUCCESS
        assertLogContains("Done", 50)

        when:
        file("src/main/cpp/src0.cpp") << """
// a comment
"""
        and:
        build("check")
        then:
        result.task(":runLintMainExecutable").outcome == TaskOutcome.SUCCESS
        assertLogContains("Done", 1)

        when:
        build("check")
        then:
        result.task(":runLintMainExecutable").outcome == TaskOutcome.UP_TO_DATE
    }
}
