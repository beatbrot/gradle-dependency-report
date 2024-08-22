package io.github.beatbrot.dependencyreport

import groovy.transform.CompileStatic;


@CompileStatic
final class Java8Util {

    public static final String JAVA8_HOME = System.getenv("JAVA8_HOME")
    public static final String JAVA8_HOME_ESCAPED = JAVA8_HOME
        .replace("\\", "\\\\")

    private Java8Util() {

    }

    static void configureJavaHome(File propertiesFile) {
        propertiesFile << "\norg.gradle.java.home=${JAVA8_HOME_ESCAPED}\n"
    }
}
