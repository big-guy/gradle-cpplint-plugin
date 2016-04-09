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

import com.greenebeans.cpplint.internal.exec.Counting
import com.greenebeans.cpplint.internal.exec.CppLintSpec
import com.greenebeans.cpplint.internal.exec.CppLintTool
import com.greenebeans.cpplint.internal.exec.DefaultCppLintSpec
import org.gradle.api.Action
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.tasks.incremental.InputFileDetails
import org.gradle.internal.operations.logging.BuildOperationLogger
import org.gradle.internal.operations.logging.BuildOperationLoggerFactory
import org.gradle.nativeplatform.NativeBinarySpec
import org.gradle.process.internal.ExecActionFactory

import javax.inject.Inject
/**
 * Runs cpplint.py for a given native binary.
 *
 * TODO consider CFG file
 */
class RunCppLint extends SourceTask {
    NativeBinarySpec nativeBinarySpec

    /**
     * Type of counting used in the report.
     *
     * "total" is the default.
     * "detailed"
     * "toplevel"
     */
    @Input
    String counting

    @Input
    Object executablePath

    @Input
    int verbosity

    @OutputDirectory
    File reportFile = new File(project.buildDir, "reports/cpplint")

    @Inject
    protected BuildOperationLoggerFactory getBuildOperationLoggerFactory() {
        throw new UnsupportedOperationException();
    }

    @Inject
    protected ExecActionFactory getExecActionFactory() {
        throw new UnsupportedOperationException()
    }

    @TaskAction
    void run(IncrementalTaskInputs taskInputs) {
        BuildOperationLogger buildOperationLogger = buildOperationLoggerFactory.newOperationLogger(getName(), reportFile)
        try {
            buildOperationLogger.start()

            CppLintTool tool = new CppLintTool(execActionFactory)
            Collection<File> files = []
            if (taskInputs.incremental) {
                taskInputs.outOfDate(new Action<InputFileDetails>() {
                    @Override
                    void execute(InputFileDetails inputFileDetails) {
                        files.add(inputFileDetails.file)
                    }
                })
            } else {
                files.addAll(getSource().getFiles())
            }
            CppLintSpec spec = createSpec(files, buildOperationLogger)
            setDidWork(tool.transform(spec).didWork)
        } finally {
            buildOperationLogger.done()
        }
    }

    private CppLintSpec createSpec(Collection<File> files, BuildOperationLogger buildOperationLogger) {
        Counting enumValue = Counting.valueOf(counting.toUpperCase())
        return new DefaultCppLintSpec(enumValue, project.file(executablePath), verbosity, files, buildOperationLogger)
    }
}
