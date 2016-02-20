package com.greenebeans.cpplint

class CppLintPluginSpec extends AbstractIntegrationSpec {
    def setup() {
        buildFile << """
            apply plugin: 'org.gradle.cpp'
            apply plugin: 'com.greenebeans.cpplint'
"""
    }

    def "does not explode"() {
        expect:
        build("tasks")
    }
}
