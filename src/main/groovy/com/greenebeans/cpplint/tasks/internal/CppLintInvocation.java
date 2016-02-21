package com.greenebeans.cpplint.tasks.internal;

import org.gradle.internal.operations.BuildOperation;

import java.io.File;

public class CppLintInvocation implements BuildOperation {
    private final File sourceFile;

    public CppLintInvocation(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    @Override
    public String getDescription() {
        return "lint for " + sourceFile;
    }

    public File getSourceFile() {
        return sourceFile;
    }
}
