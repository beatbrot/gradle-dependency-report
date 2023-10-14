package io.github.beatbrot.dependencyreport.internal.analysis;

import io.github.beatbrot.dependencyreport.internal.Serialization;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.artifacts.*;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.beatbrot.dependencyreport.internal.Util.uncheckedCast;

public class AnalyzeDependenciesTask extends DefaultTask {

    public static final String NAME = "analyzeDependencies";

    private static final Spec<Task> NEVER = t -> false;

    private final RegularFileProperty outputFile;

    private final Set<DependencyStatus> result = new LinkedHashSet<>();

    @Inject
    public AnalyzeDependenciesTask(final ProjectLayout projectLayout, final ObjectFactory objects) {
        final Provider<RegularFile> defaultOutFile = projectLayout.getBuildDirectory().file("tmp/dependency-updates/dependencies.ser");
        this.outputFile = objects.fileProperty().convention(defaultOutFile);
        // We cast this to Task so that getOutputs returns the public API.
        ((Task) this).getOutputs().upToDateWhen(NEVER);
        analyzeAll();
    }

    private void analyzeAll() {
        analyze(getProject().getConfigurations());
        analyze(getProject().getBuildscript().getConfigurations());
    }

    private void analyze(final ConfigurationContainer container) {
        for (final Configuration config : new ArrayList<>(container)) {
            final LenientConfiguration current = createResolvableConfig(container, config, ModuleDependency::getVersion);
            final LenientConfiguration latest = createResolvableConfig(container, config, ignored -> "+");
            analyzeConfiguration(result, current, latest);
        }
    }

    @OutputFile
    public RegularFileProperty getOutputFile() {
        return outputFile;
    }

    @TaskAction
    public void writeAnalysis() {
        Serialization.write(outputFile.get().getAsFile(), result);
    }

    private void analyzeConfiguration(final Set<DependencyStatus> result, final LenientConfiguration declared, final LenientConfiguration latest) {
        final List<Coordinate> originalDeps = declared.getFirstLevelModuleDependencies().stream().map(Coordinate::from).collect(Collectors.toList());

        final Map<Coordinate.Key, List<Coordinate>> resolved = latest.getFirstLevelModuleDependencies()
            .stream()
            .map(Coordinate::from)
            .collect(Collectors.groupingBy(Coordinate::key));


        for (final Coordinate original : originalDeps) {
            for (final Coordinate r : resolved.getOrDefault(original.key(), Collections.emptyList())) {
                if (!r.key().equals(original.key())) {
                    throw new NoSuchElementException();
                }
                result.add(ImmutableDependencyStatus.of(original, r.version()));
            }
        }
    }

    private LenientConfiguration createResolvableConfig(ConfigurationContainer container,
                                                        final Configuration configuration,
                                                        final Function<ModuleDependency, String> versionSelector) {
        final Collection<ModuleDependency> deps = gatherExternalDependencies(configuration, versionSelector);

        final Configuration copy = container.detachedConfiguration();
        copy.setVisible(false);
        copy.setCanBeResolved(true);
        copy.setCanBeConsumed(false);
        copy.setTransitive(false);
        copyAttrs(configuration.getAttributes(), copy.getAttributes());
        copy.getDependencies().addAll(deps);
        return copy.getResolvedConfiguration().getLenientConfiguration();
    }

    private static void copyAttrs(AttributeContainer original, AttributeContainer copy) {
        AttributeContainer originalAttrs = original.getAttributes();
        for (Attribute<?> attribute : originalAttrs.keySet()) {
            copy.attribute(attribute, uncheckedCast(Objects.requireNonNull(original.getAttribute(attribute))));
        }
    }

    private Collection<ModuleDependency> gatherExternalDependencies(final Configuration configuration, final Function<ModuleDependency, String> versionExtractor) {
        DependencyHandler depHandler = getProject().getDependencies();
        return configuration.getDependencies().stream().filter(ExternalDependency.class::isInstance).map(ModuleDependency.class::cast).map(d -> {
            final String coordString = d.getGroup() + ":" + d.getName() + ":" + versionExtractor.apply(d);
            return ((ModuleDependency) depHandler.create(coordString)).setTransitive(false);
        }).collect(Collectors.toList());
    }
}
