package com.greenebeans.cpplint

import groovy.io.FileType
import org.gradle.testkit.runner.TaskOutcome

class GcovIntegrationSpec extends AbstractIntegrationSpec {
    def buildDir

    def setup() {
        buildDir = file("build")
        buildFile << """
apply plugin: 'org.gradle.cpp'
apply plugin: 'org.gradle.google-test'

model {
    platforms {
        x86 {
            architecture 'i386'
        }
    }
    repositories {
        libs(PrebuiltLibraries) {
            googleTest {
                headers.srcDir "/Users/sterling/gits/gradle/subprojects/docs/src/samples/native-binaries/google-test/libs/googleTest/1.7.0/include"
                binaries.withType(StaticLibraryBinary) {
                    staticLibraryFile =
                        file("/Users/sterling/gits/gradle/subprojects/docs/src/samples/native-binaries/google-test/libs/googleTest/1.7.0/lib/osx/libgtest.a")
                }
            }
        }
    }
    components {
        main(NativeLibrarySpec) {
            targetPlatform 'x86'
        }
    }

    binaries {
        withType(GoogleTestTestSuiteBinarySpec) {
            lib library: "googleTest", linkage: "static"
            cppCompiler.args '--coverage'
            linker.args '--coverage'
        }
    }
}

tasks.withType(RunTestExecutable) {
    // environment "GCOV_PREFIX", "test"
    // environment "GCOV_PREFIX_STRIP", "0"
    args "--gtest_output=xml:test_detail.xml"
    doFirst {
        // TODO: Delete all gcda files before executing
        // TODO: Add gcda files as outputs to task
    }
}

task coverMainExe(type: com.greenebeans.cpplint.tasks.GcovCoverageTask) {
    variant = "debug"
    registerSources new File(buildDir, "objs/mainTest/mainCpp"), fileTree(dir: "src/main/cpp", include: '**/*.cpp')
    dependsOn "runMainTestGoogleTestExe"
}

task generateCoverageReportMainExe(type: com.greenebeans.cpplint.tasks.GcovrReportTask) {
    dependsOn coverMainExe
}
check.dependsOn generateCoverageReportMainExe
"""
        def srcDir = file("src/main/cpp")
        def headersDir = file("src/main/headers")
        def testDir = file("src/mainTest/cpp")
        [ srcDir, headersDir, testDir ]*.mkdirs()

        def srcFile = new File(srcDir, "main.cpp")
        srcFile << """
#include "main.h"

int plus(int a, int b) {
    return a + b;
}

int minus(int a, int b) {
    return a - b;
}
"""
        def headerFile = new File(headersDir, "main.h")
        headerFile << """
#ifndef HEADER_H
#define HEADER_H
int plus(int a, int b);
#endif // HEADER_H
"""
        def testFile = new File(testDir, "test.cpp")
        testFile << """
#include "gtest/gtest.h"
#include "main.h"

using namespace testing;

TEST(OperatorTests, test_plus) {
  ASSERT_TRUE(plus(0, 2) == 2);
  ASSERT_TRUE(plus(0, -2) == -2);
  ASSERT_TRUE(plus(2, 2) == 4);
}

int main(int argc, char **argv) {
  testing::InitGoogleTest(&argc, argv);
  return RUN_ALL_TESTS();
}
"""
    }

    def "generates GCOV data files"() {
        when:
        build("mainTestGoogleTestExe")
        then:
        result.task(":mainTestGoogleTestExe").outcome == TaskOutcome.SUCCESS
        and:
        // gcno files are generated when the source is compiled
        findFiles("objs/mainTest/", ".gcno") == [ "main.gcno", "test.gcno" ]
        findFiles("objs/mainTest/", ".gcda").isEmpty()
        when:
        build("check")
        then:
        // gcda files are generated when the executable runs
        result.task(":runMainTestGoogleTestExe").outcome == TaskOutcome.SUCCESS
        findFiles("objs/mainTest/", ".gcda") == [ "main.gcda", "test.gcda" ]
    }

    def "generates GCOV coverage files"() {
        when:
        build("check")
        then:
        result.task(":mainTestGoogleTestExe").outcome == TaskOutcome.SUCCESS
        result.task(":runMainTestGoogleTestExe").outcome == TaskOutcome.SUCCESS
        result.task(":coverMainExe").outcome == TaskOutcome.SUCCESS
        and:
        findFiles("reports/coverage/gcov/debug", ".gcov") == [ "main.cpp.gcov" ]
    }

    List<String> findFiles(String path, String extension) {
        def rootOfObjs = new File(buildDir, path)
        def matchingFiles = []
        rootOfObjs.eachFileRecurse(FileType.FILES) {
            if (it.name.endsWith(extension)) {
                matchingFiles << it.name
            }
        }
        return matchingFiles
    }
}
