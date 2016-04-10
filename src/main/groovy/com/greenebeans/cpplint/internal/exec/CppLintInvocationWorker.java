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
package com.greenebeans.cpplint.internal.exec;

import org.gradle.api.Action;
import org.gradle.process.internal.ExecAction;
import org.gradle.process.internal.ExecActionFactory;
import org.gradle.process.internal.ExecException;

import java.io.ByteArrayOutputStream;

public class CppLintInvocationWorker implements Action<CppLintInvocation> {

    private final CppLintSpec spec;
    private final ExecActionFactory execActionFactory;

    public CppLintInvocationWorker(CppLintSpec spec, ExecActionFactory execActionFactory) {
        this.spec = spec;
        this.execActionFactory = execActionFactory;
    }

    public String getDisplayName() {
        return "cpplint worker";
    }

    @Override
    public void execute(CppLintInvocation cppLintInvocation) {
        ByteArrayOutputStream errOutput = new ByteArrayOutputStream();
        ByteArrayOutputStream stdOutput = new ByteArrayOutputStream();

        ExecAction execAction = execActionFactory.newExecAction();
        execAction.setExecutable(spec.getExecutablePath());
        execAction.args("--root=" + spec.getRoot());
        execAction.args("--verbose=" + spec.getVerbosity());
        execAction.args("--counting=" + spec.getCounting().toString().toLowerCase());
        execAction.args(spec.getArgs());
        execAction.args(cppLintInvocation.getSourceFile());

        execAction.setErrorOutput(errOutput);
        execAction.setStandardOutput(stdOutput);

        try {
            execAction.execute();
            spec.getBuildOperationLogger().operationSuccess(cppLintInvocation.getDescription(), stdOutput.toString() + errOutput.toString());
        } catch (ExecException e) {
            spec.getBuildOperationLogger().operationFailed(cppLintInvocation.getDescription(), stdOutput.toString() + errOutput.toString());
            throw new CppLintFailure(cppLintInvocation, "Failed to lint", e);
        }
    }
}
