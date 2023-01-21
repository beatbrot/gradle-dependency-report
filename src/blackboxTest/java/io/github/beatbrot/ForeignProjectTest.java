package io.github.beatbrot;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.assertj.core.api.Assertions.assertThat;

class ForeignProjectTest {
    @TempDir
    static Path initScriptDir;

    static Path initScript;
    static Path benManes;

    @TempDir
    Path gradleCacheDir;

    @BeforeAll
    static void beforeAll() throws IOException {
        initScript = initScriptDir.resolve("init.gradle");
        final String content = "            initscript {\n" + "                dependencies {\n" + printClasspathDeps() + "\n" + "                }\n" + "            }\n" + "            allprojects { p ->\n" + "                if(p == p.rootProject) {\n" + "                    p.apply plugin: io.github.beatbrot.dependencyreport.DependencyReportPlugin\n" + "                }\n" + "            }";
        Files.write(initScript, content.getBytes(UTF_8), CREATE, TRUNCATE_EXISTING);
        benManes = initScriptDir.resolve("manes.gradle");
        final String benManesContent = "initscript {\n" + "  repositories {\n" + "     gradlePluginPortal()\n" + "  }\n" + "\n" + "  dependencies {\n" + "    classpath 'com.github.ben-manes:gradle-versions-plugin:+'\n" + "  }\n" + "}\n" + "\n" + "allprojects {\n" + "  apply plugin: com.github.benmanes.gradle.versions.VersionsPlugin\n" + "\n" + "  tasks.named(\"dependencyUpdates\").configure {\n" + "    outputFormatter = \"json\"" + "  }\n" + "}";
        Files.write(benManes, benManesContent.getBytes(UTF_8), CREATE, TRUNCATE_EXISTING);
    }

    @ParameterizedTest
    @MethodSource("enumerateExternalProjects")
    void apply_Plugin_to_external_project(final Path project) throws IOException {
        final BuildResult mine = gradleRunner(project).withArguments(args("--init-script", initScript.toString(), "dependencyReport")).build();

        assertThat(mine.task(":dependencyReport")).isNotNull().extracting(BuildTask::getOutcome).isEqualTo(TaskOutcome.SUCCESS);
        ReportResult.parseMine(mine.getOutput());
    }

    @ParameterizedTest
    @MethodSource("enumerateExternalProjects")
    @Disabled("Only needed as reference.")
    void apply_ben_manes_plugin(final Path project) throws IOException {
        final String taskName = "dependencyUpdates";
        final BuildResult result = gradleRunner(project)
            .withArguments("--init-script", benManes.toString(), taskName)
            .build();

        assertThat(result.task(":" + taskName))
            .isNotNull()
            .extracting(BuildTask::getOutcome)
            .isEqualTo(TaskOutcome.SUCCESS);
    }

    private static GradleRunner gradleRunner(final Path project) throws IOException {
        final GradleRunner result = GradleRunner.create();
        result.withProjectDir(project.toFile());
        final Path wrapperProps = project.resolve("gradle/wrapper/gradle-wrapper.properties");
        if (Files.isRegularFile(wrapperProps)) {
            final Properties props = new Properties();
            try (final BufferedReader r = Files.newBufferedReader(wrapperProps)) {
                props.load(r);
            }
            final Object url = props.get("distributionUrl");
            result.withGradleDistribution(URI.create(url.toString()));
        }
        return result;
    }


    private static String printClasspathDeps() {
        final StringJoiner joiner = new StringJoiner("\n");
        PluginUnderTestMetadataReading.readImplementationClasspath().forEach(it -> joiner.add("classpath files(\"" + it + "\")"));
        return joiner.toString();
    }

    private static List<Path> enumerateExternalProjects() throws IOException {
        try (final Stream<Path> s = Files.list(Paths.get("build/external"))) {
            return s.filter(Files::isDirectory)
                .collect(Collectors.toList());
        }
    }

    private List<String> args(String... otherArgs) {
        ArrayList<String> result = new ArrayList<>();
        result.addAll(Arrays.asList("--project-cache-dir", gradleCacheDir.toString()));
        result.addAll(Arrays.asList(otherArgs));
        return result;
    }
}
