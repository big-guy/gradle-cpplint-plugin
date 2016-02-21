package com.greenebeans.cpplint.tasks
import org.gradle.api.Action
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec

class RunCppLint extends SourceTask {
    @Input
    String executablePath

    @OutputFile
    File reportFile = new File(project.buildDir, "reports/cpplint/cpplint.txt")

    @Input
    int verbosity = 0

    String counting = "total" // total|detailed|toplevel

    @Input
    String outputType = "report" // report|console|both

    @TaskAction
    void run() {
        ExecResult result = project.exec(new Action<ExecSpec>() {
            @Override
            void execute(ExecSpec execSpec) {
                execSpec.executable = executablePath
                execSpec.args "--verbose=${verbosity}", "--counting=${counting}"
                execSpec.args getSource().getFiles().unique()
                execSpec.ignoreExitValue = true
            }
        })

        result.assertNormalExitValue()
    }
}
