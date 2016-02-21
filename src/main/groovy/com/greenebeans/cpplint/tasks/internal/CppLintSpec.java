package com.greenebeans.cpplint.tasks.internal;

import org.gradle.internal.operations.logging.BuildOperationLogger;
import org.gradle.language.base.internal.compile.CompileSpec;

import java.io.File;
import java.util.Collection;

public interface CppLintSpec extends CompileSpec {
    String getCounting();
    String getExecutablePath();
    int getVerbosity();
    Collection<File> getSourceFiles();
    BuildOperationLogger getBuildOperationLogger();
}
