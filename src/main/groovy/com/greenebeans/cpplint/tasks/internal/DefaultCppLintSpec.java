package com.greenebeans.cpplint.tasks.internal;

import org.gradle.internal.operations.logging.BuildOperationLogger;

import java.io.File;
import java.util.Collection;

public class DefaultCppLintSpec implements CppLintSpec {

    private final String counting;
    private final String executablePath;
    private final int verbosity;
    private final Collection<File> sourceFiles;
    private final BuildOperationLogger buildOperationLogger;

    public DefaultCppLintSpec(String counting, String executablePath, int verbosity, Collection<File> sourceFiles, BuildOperationLogger buildOperationLogger) {
        this.counting = counting;
        this.executablePath = executablePath;
        this.verbosity = verbosity;
        this.sourceFiles = sourceFiles;
        this.buildOperationLogger = buildOperationLogger;
    }

    @Override
    public String getCounting() {
        return counting;
    }

    @Override
    public String getExecutablePath() {
        return executablePath;
    }

    @Override
    public int getVerbosity() {
        return verbosity;
    }

    @Override
    public Collection<File> getSourceFiles() {
        return sourceFiles;
    }

    @Override
    public BuildOperationLogger getBuildOperationLogger() {
        return buildOperationLogger;
    }
}
