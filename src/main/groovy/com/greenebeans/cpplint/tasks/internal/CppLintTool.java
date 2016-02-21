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
