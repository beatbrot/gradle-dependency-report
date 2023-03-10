package io.github.beatbrot.dependencyreport.internal

import groovy.transform.Canonical
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path
import java.nio.file.Paths

class SerializationTest extends Specification {

    @TempDir
    @Shared
    Path outputPath

    def "Serialization works"(int index, Object input) {
        setup:
        def outFile = outputPath.resolve("${index}.ser")
        Serialization.write(outFile.toFile(), input)
        expect:
        Serialization.read(outFile) == input

        where:
        index | input
        0     | new Person("Foo", 1)
        1     | new ArrayList<Person>().tap { it.add(new Person("Foo", 1)) }
        2     | new HashSet<Person>().tap { it.add(new Person("Foo", 1)) }
        3     | "FooBar"
    }

    def "Exceptions are handled correctly"() {
        when:
        Serialization.read(Paths.get("doesNotExist"))
        then:
        thrown(Serialization.SerializationException)

        when:
        Serialization.write(outputPath.resolve("foo").toFile(), new NonSerializable(""))
        then:
        thrown(Serialization.SerializationException)
    }

    @Canonical
    static class NonSerializable {
        String foo
    }

    @Canonical
    @SuppressWarnings('unused')
    static class Person implements Serializable {
        String name
        int age
    }
}
