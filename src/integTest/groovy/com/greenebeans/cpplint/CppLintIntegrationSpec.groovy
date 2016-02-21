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

    def "runs cpplint - good code"() {
        when:
        build("check")
        then:
        result.task(":runLintMainExecutable").outcome == TaskOutcome.SUCCESS

        when:
        build("check")
        then:
        result.task(":runLintMainExecutable").outcome == TaskOutcome.UP_TO_DATE
    }


    def "runs cpplint - bad code"() {
        given:
        def srcFile = file("src/main/cpp/bad.cpp")
        srcFile << """
int foo() { return 42; }
"""
        when:
        buildAndFail("check")
        then:
        result.task(":runLintMainExecutable").outcome == TaskOutcome.FAILED

        when:
        srcFile << """
// Copyright 2016 Example
"""
        and:
        build("check")
        then:
        result.task(":runLintMainExecutable").outcome == TaskOutcome.SUCCESS
    }

    def "works for multiple components"() {
        given:
        buildFile << """
model {
    components {
        lib(NativeLibrarySpec)
    }
}
"""
        def srcDir = file("src/lib/cpp")
        srcDir.mkdirs()
        def srcFile = file("src/lib/cpp/main.cpp")
        srcFile << """
// Copyright 2016 Example

    int main(int argc, char** argv) {
        return 0;
    }
"""
        when:
        build("check")
        then:
        result.task(":runLintMainExecutable").outcome == TaskOutcome.SUCCESS
        result.task(":runLintLibSharedLibrary").outcome == TaskOutcome.SUCCESS
        result.task(":runLintLibStaticLibrary").outcome == TaskOutcome.SUCCESS
    }

    def "works for multiple binaries"() {
        given:
        buildFile << """
model {
    flavors {
        free
        paid
        professional
    }
}
"""
        when:
        build("check")
        then:
        result.task(":runLintMainFreeExecutable").outcome == TaskOutcome.SUCCESS
        result.task(":runLintMainPaidExecutable").outcome == TaskOutcome.SUCCESS
        result.task(":runLintMainProfessionalExecutable").outcome == TaskOutcome.SUCCESS
    }

    def "can restrict to only some binaries"() {
        given:
        buildFile << """
model {
    flavors {
        free
        paid
        professional
    }

    tasks {
        withType(com.greenebeans.cpplint.tasks.RunCppLint) {
            onlyIf {
                nativeBinarySpec.flavor == flavors.professional
            }
        }
    }
}
"""
        when:
        build("check")
        then:
        result.task(":runLintMainFreeExecutable").outcome == TaskOutcome.SKIPPED
        result.task(":runLintMainPaidExecutable").outcome == TaskOutcome.SKIPPED
        result.task(":runLintMainProfessionalExecutable").outcome == TaskOutcome.SUCCESS
    }
}
