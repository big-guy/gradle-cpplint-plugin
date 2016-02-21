package com.greenebeans.cpplint.tasks.internal;

import org.gradle.internal.operations.BuildOperation;
import org.gradle.internal.operations.BuildOperationFailure;

public class CppLintFailure extends BuildOperationFailure {
    protected CppLintFailure(BuildOperation operation, String message, Throwable cause) {
        super(operation, message, cause);
    }
}
