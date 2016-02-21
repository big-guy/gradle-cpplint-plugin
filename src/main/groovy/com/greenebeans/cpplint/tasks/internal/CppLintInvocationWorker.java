package com.greenebeans.cpplint.tasks.internal;

import org.gradle.internal.operations.BuildOperationWorker;
import org.gradle.process.internal.ExecAction;
import org.gradle.process.internal.ExecActionFactory;
import org.gradle.process.internal.ExecException;

import java.io.ByteArrayOutputStream;

public class CppLintInvocationWorker implements BuildOperationWorker<CppLintInvocation> {

    private final CppLintSpec spec;
    private final ExecActionFactory execActionFactory;

    public CppLintInvocationWorker(CppLintSpec spec, ExecActionFactory execActionFactory) {
        this.spec = spec;
        this.execActionFactory = execActionFactory;
    }

    @Override
    public String getDisplayName() {
        return "cpplint worker";
    }

    @Override
    public void execute(CppLintInvocation cppLintInvocation) {
        ByteArrayOutputStream errOutput = new ByteArrayOutputStream();
        ByteArrayOutputStream stdOutput = new ByteArrayOutputStream();

        ExecAction execAction = execActionFactory.newExecAction();
        execAction.setExecutable(spec.getExecutablePath());
        execAction.args("--verbose=" + spec.getVerbosity());
        execAction.args("--counting=" + spec.getCounting());
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
