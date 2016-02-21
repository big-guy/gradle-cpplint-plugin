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
package com.greenebeans.cpplint.tasks.internal;

import org.gradle.api.internal.tasks.SimpleWorkResult;
import org.gradle.api.tasks.WorkResult;
import org.gradle.internal.operations.BuildOperationProcessor;
import org.gradle.internal.operations.BuildOperationQueue;
import org.gradle.process.internal.ExecActionFactory;

import java.io.File;

public class CppLintTool implements org.gradle.language.base.internal.compile.Compiler<CppLintSpec> {
    private final BuildOperationProcessor buildOperationProcessor;
    private final ExecActionFactory execActionFactory;

    public CppLintTool(BuildOperationProcessor buildOperationProcessor, ExecActionFactory execActionFactory) {
        this.buildOperationProcessor = buildOperationProcessor;
        this.execActionFactory = execActionFactory;
    }

    @Override
    public WorkResult execute(CppLintSpec cppLintSpec) {
        final BuildOperationQueue<CppLintInvocation> queue = buildOperationProcessor.newQueue(
                new CppLintInvocationWorker(cppLintSpec, execActionFactory), cppLintSpec.getBuildOperationLogger().getLogLocation());

        for (File sourceFile : cppLintSpec.getSourceFiles()) {
            queue.add(new CppLintInvocation(sourceFile));
        }

        queue.waitForCompletion();

        return new SimpleWorkResult(!cppLintSpec.getSourceFiles().isEmpty());
    }
}
