package com.greenebeans.cpplint

class CppLintPluginSpec extends AbstractIntegrationSpec {

    def "does nothing when CPP plugin is not applied"() {
        given:
        buildFile << """
            apply plugin: 'com.greenebeans.cpplint'
        """

        expect:
        def result = build("tasks", "--all")
        !result.output.contains("installCppLint - Installs cpplint.py")
    }

    def "adds task when CPP plugin is applied"() {
        given:
        buildFile << """
            apply plugin: 'org.gradle.cpp'
            apply plugin: 'com.greenebeans.cpplint'
        """

        expect:
        def result = build("tasks", "--all")
        result.output.contains("installCppLint - Installs cpplint.py")
    }
}
