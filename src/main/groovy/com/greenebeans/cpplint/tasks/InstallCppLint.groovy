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

package com.greenebeans.cpplint.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.nativeplatform.filesystem.FileSystem

import javax.inject.Inject

/**
 * TODO: Docs
 */
class InstallCppLint extends DefaultTask {
    /**
     * The URL to download the cpplint.py executable from
     */
	@Input
    String distUrl

    /**
     * Location on disk to install cpplint to (defaults to build/cpplint/cpplint.py)
     */
    @OutputFile
    File installPath

    /**
     * Skip installing.  If this is true, cpplint.py must be available somewhere locally already.
     */
    @Input
    boolean skipInstall

    @Inject
    protected FileSystem getFileSystem() {
        throw new UnsupportedOperationException();
    }

    @TaskAction
    void download() {
        if (skipInstall) {
            checkCppLintExists("cpplint.py (looked for ${installPath}) is not installed, but the build is configured to skip install.")
        } else {
            try {
                def urlConnection = new URL(distUrl).openConnection()
                urlConnection.setConnectTimeout(10)
                urlConnection.connect()
                installPath << urlConnection.inputStream
                checkCppLintExists("cpplint.py (looked for ${installPath}) is not installed, and the build tried to download it from ${distUrl}.")
            } catch (IOException e) {
                throw new GradleException("Could not download cpplint.py from $distUrl", e)
            }
        }
        fileSystem.chmod(installPath, 0744)
    }

    private void checkCppLintExists(String msg) {
        if (!installPath.exists()) {
            throw new GradleException(msg)
        }
    }
}
