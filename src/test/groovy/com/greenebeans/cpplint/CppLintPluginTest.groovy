package com.greenebeans.cpplint

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class CppLintPluginTest extends Specification {
    @Rule TemporaryFolder temporaryFolder = new TemporaryFolder()

    def "does not add tasks when CPP plugin is not applied"() {
        def project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()
        project.tasks.whenObjectAdded { assert false : "Tasks should not be added" }
        project.apply(plugin: CppLintPlugin)
    }
}
