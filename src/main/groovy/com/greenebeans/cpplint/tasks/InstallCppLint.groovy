package com.greenebeans.cpplint.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.nativeplatform.filesystem.FileSystem

import javax.inject.Inject

class InstallCppLint extends DefaultTask {
	@Input
    String distUrl = "https://raw.githubusercontent.com/google/styleguide/b43afc71a5ae4a2585a583333b45ce664cd2c3c6/cpplint/"

    @OutputFile
    File installPath = new File(project.buildDir, "cpplint/cpplint.py")

    @Input
    boolean skipInstall = false

    @Inject
    protected FileSystem getFileSystem() {
        throw new UnsupportedOperationException();
    }

    InstallCppLint() {
        description = "Installs cpplint.py"
    }

    @TaskAction
    void download() {
        if (skipInstall) {
            checkCppLintExists("cpplint.py (looked for ${installPath}) is not installed, but the build is configured to skip install.")
            return
        }

        installPath << new URL(distUrl).openStream()
        checkCppLintExists("cpplint.py (looked for ${installPath}) is not installed, and the build tried to download it from ${distUrl}.")
        fileSystem.chmod(installPath, 0744)
    }

    private void checkCppLintExists(String msg) {
        if (!installPath.exists()) {
            throw new GradleException(msg)
        }
    }
}
