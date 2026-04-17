package io.github.beatbrot.dependencyreport

import spock.lang.Specification

class DemoTest extends Specification {

    def "I am failing"() {
        when:
        def actual = "actual"
        def expected = "expected"

        then:
        actual == expected
    }
}
