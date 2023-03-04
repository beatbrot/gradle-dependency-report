package io.github.beatbrot;

import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.StringJoiner;
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
    Path projectDir;

    @BeforeAll
    static void beforeAll() throws IOException {
        initScript = initScriptDir.resolve("init.gradle");
        String content = String.format(loadTemplate("/initScript.gradle"), printClasspathDeps());
        Files.write(initScript, content.getBytes(UTF_8), CREATE, TRUNCATE_EXISTING);
        benManes = initScriptDir.resolve("manes.gradle");
        final String benManesContent = loadTemplate("/benManes.gradle");
        Files.write(benManes, benManesContent.getBytes(UTF_8), CREATE, TRUNCATE_EXISTING);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("enumerateExternalProjects")
    void apply_Plugin_to_external_project(String name, final Path origPath) throws IOException {
        initProjectDir(origPath);
        final BuildResult mine = gradleRunner(projectDir).withArguments("--init-script", initScript.toString(), "dependencyReport").build();

        assertThat(mine.task(":dependencyReport")).isNotNull().extracting(BuildTask::getOutcome).isEqualTo(TaskOutcome.SUCCESS);
        ReportResult result = ReportResult.parseMine(mine.getOutput());
        switch (name) {
            case "architecture-samples":
                assertThat(result.getOutdatedDeps()).isEqualTo(33);
                break;
            case "idiomatic-gradle":
                assertThat(result.getOutdatedDeps()).isZero();
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @ParameterizedTest
    @MethodSource("enumerateExternalProjects")
    @Disabled("Only needed as reference.")
    void apply_ben_manes_plugin(final Path origPath) throws IOException {
        initProjectDir(origPath);
        final String taskName = "dependencyUpdates";
        final BuildResult result = gradleRunner(projectDir)
            .withArguments("--init-script", benManes.toString(), taskName)
            .build();

        assertThat(result.task(":" + taskName))
            .isNotNull()
            .extracting(BuildTask::getOutcome)
            .isEqualTo(TaskOutcome.SUCCESS);
    }

    private void initProjectDir(Path origDir) throws IOException {
        FileUtils.copyDirectory(origDir.toFile(), projectDir.toFile());
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
        PluginUnderTestMetadataReading.readImplementationClasspath()
            .stream()
            .map(f -> f.toString().replace("\\", "\\\\"))
            .forEach(it -> joiner.add("classpath files(\"" + it.replace("\\", "\\\\") + "\")"));
        return joiner.toString();
    }

    private static List<Arguments> enumerateExternalProjects() throws IOException {
        try (final Stream<Path> s = Files.list(Paths.get("build/external"))) {
            return s.filter(Files::isDirectory)
                .map(p -> Arguments.of(projectName(p), p))
                .collect(Collectors.toList());
        }
    }

    static String loadTemplate(String name) throws IOException {
        try (InputStream stream = ForeignProjectTest.class.getResourceAsStream(name);
             BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(stream), UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private static String projectName(Path path) {
        String dirName = path.getFileName().toString();
        return dirName.substring(0, dirName.lastIndexOf('-'));
    }
}
