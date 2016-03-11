package com.greenebeans.cpplint.tasks

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingScheme
import org.gradle.process.ExecSpec

public class GcovCoverageTask extends DefaultTask {
    String variant
    Map<File, Iterable<File>> sources = [:]

    @InputFiles
    FileCollection getGcdaFiles() {
        def gcdaCollections = sources.keySet().collect {
            project.fileTree(dir: it, includes: [ '**/*.gcda' ])
        }
        project.files(gcdaCollections)
    }

    @InputFiles
    FileCollection getGcnoFiles() {
        def gcnoCollections = sources.keySet().collect {
            project.fileTree(dir: it, includes: [ '**/*.gcno' ])
        }
        project.files(gcnoCollections)
    }

    void registerSources(File objectFileRoot, Iterable<File> source) {
        sources.put(objectFileRoot, source)
    }

    @OutputDirectory
    File getReportDir() {
        new File(project.buildDir, "reports/coverage/gcov/${variant}")
    }

    @TaskAction
    public void generateGcov() {
        sources.each { objectFileRoot, files ->
            files.each { sourceFile ->
                executeLLVM {
                    File outputFile = new CompilerOutputFileNamingScheme()
                            .withObjectFileNameSuffix(".gcno")
                            .withOutputBaseFolder(objectFileRoot)
                            .map(sourceFile);
                    assert outputFile.exists()
                    workingDir reportDir
                    executable "/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin/llvm-cov"
                    args "gcov", "-o", outputFile.parentFile.absolutePath, sourceFile.absolutePath
                }
            }
        }
    }

    void executeLLVM(@DelegatesTo(ExecSpec)
                     @ClosureParams(value=SimpleType, options="org.gradle.process.ExecSpec") Closure cl) {
        project.exec(cl)
    }
}
