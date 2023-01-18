package io.github.beatbrot.dependencyreport.internal.analysis


import spock.lang.Shared
import spock.lang.Specification

class DependencyStatusTest extends Specification {

    @Shared
    def coordA = ImmutableCoordinate.builder().group("a").name("a").version("1").build()
    @Shared
    def coordB = ImmutableCoordinate.builder().group("a").name("b").version("1").build()


    def "Comparison test"() {
        setup:
        def aOne = ImmutableDependencyStatus.of(coordA, "1")
        def aTwo = aOne.withLatestVersion("2")
        def bOne = aOne.withCoordinate(coordB)
        expect:
        aOne < aTwo
        aOne < bOne
        aTwo < bOne
    }

    def "Test isUpToDate"() {
        def aOne = ImmutableDependencyStatus.of(coordA, "1")
        def aTwo = aOne.withLatestVersion("2")
        def aMinusOne = aOne.withLatestVersion("-1")
        expect:
        aOne.isUpToDate()
        !aTwo.isUpToDate()
        !aMinusOne.isUpToDate()
    }
}
