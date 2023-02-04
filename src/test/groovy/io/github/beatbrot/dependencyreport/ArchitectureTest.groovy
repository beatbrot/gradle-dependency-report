package io.github.beatbrot.dependencyreport

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import spock.lang.Shared
import spock.lang.Specification

import static com.tngtech.archunit.base.DescribedPredicate.anyElementThat
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

class ArchitectureTest extends Specification {

    @Shared
    DescribedPredicate<? super JavaClass> RESIDE_IN_INTERNAL = resideInAPackage("..internal..")
    @Shared
    DescribedPredicate<? super JavaClass> RESIDE_IN_GRADLE_INTERNAL = resideInAPackage("org.gradle..internal..")
    @Shared
    DescribedPredicate<? super JavaClass> RESIDE_OUT_PLUGIN_INTERNAL = resideOutsideOfPackage("io.github.beatbrot.dependencyreport.internal..")

    @Shared
    def importedClasses = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("io.github.beatbrot")


    def "We do not depend on Gradle Internal API"() {
        setup:
        def rule = noClasses().should()
            .dependOnClassesThat(RESIDE_IN_GRADLE_INTERNAL)
        expect:
        rule.check(importedClasses)
    }

    def "Public classes do not expose plugin internal API"() {
        setup:
        def rule = methods().that()
            .areDeclaredInClassesThat(RESIDE_OUT_PLUGIN_INTERNAL)
            .and().arePublic()
            .should()
            .notHaveRawParameterTypes(anyElementThat(RESIDE_IN_INTERNAL))
            .andShould()
            .notHaveRawReturnType(RESIDE_IN_INTERNAL)
            .andShould()
            .notDeclareThrowableOfType(RESIDE_IN_INTERNAL)

        expect:
        rule.check(importedClasses)
    }
}
