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

package com.greenebeans.cpplint.tasks

import org.gradle.api.Action
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.nativeplatform.NativeBinarySpec
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec

class RunCppLint extends SourceTask {
    NativeBinarySpec nativeBinarySpec

    @Input
    String executablePath

    @OutputFile
    File reportFile = new File(project.buildDir, "reports/cpplint/cpplint.txt")

    @Input
    int verbosity = 0

    String counting = "total" // total|detailed|toplevel

    @Input
    String outputType = "report" // report|console|both

    @TaskAction
    void run() {
        ExecResult result = project.exec(new Action<ExecSpec>() {
            @Override
            void execute(ExecSpec execSpec) {
                execSpec.executable = executablePath
                execSpec.args "--verbose=${verbosity}", "--counting=${counting}"
                execSpec.args getSource().getFiles().unique()
                execSpec.ignoreExitValue = true
            }
        })

        result.assertNormalExitValue()
    }
}
