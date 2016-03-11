package com.greenebeans.cpplint.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class GcovrReportTask extends DefaultTask {
    File gcovFileRoot = new File(project.buildDir, "reports/coverage/gcov/debug")

    @InputFiles
    FileCollection getGcovFiles() {
        project.fileTree(dir: gcovFileRoot, include: '**/*.gcov')
    }

    @OutputDirectory
    File gcovrReport = new File(project.buildDir, "reports/coverage/gcov/debug/html")

    @TaskAction
    public void generateFiles() {
//        project.exec {
//            executable "/usr/local/Cellar/gcovr/3.2/bin/gcovr"
//            args '-g', gcovFileRoot.absolutePath, '-r', project.projectDir, '-k'
//        }
        project.exec {
            executable "/usr/local/Cellar/gcovr/3.2/bin/gcovr"
            args '-g', gcovFileRoot.absolutePath, '-r', project.projectDir, '-k', '-s',
                    '--html', '--html-details',
                    '--output', new File(gcovrReport, "index.html")
        }
        project.exec {
            executable "/usr/local/Cellar/gcovr/3.2/bin/gcovr"
            args '-g', gcovFileRoot.absolutePath, '-r', project.projectDir, '-k',
                    '--xml',
                    '--output', new File(gcovrReport, "coverage.xml")
        }
    }
}
