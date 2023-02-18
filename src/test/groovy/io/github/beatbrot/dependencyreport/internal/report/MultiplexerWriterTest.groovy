//file:noinspection GroovyAssignabilityCheck
package io.github.beatbrot.dependencyreport.internal.report


import spock.lang.Specification

class MultiplexerWriterTest extends Specification {
    def "Calls are forwarded"() {
        setup:
        def writerA = Mock(Writer)
        def writerB = Mock(Writer)
        def multiplexer = new MultiplexerWriter([writerA, writerB])
        when:
        multiplexer.write("Foo")
        multiplexer.flush()
        multiplexer.close()
        then:
        1 * writerA.write((char[]) _, _, _)
        1 * writerA.flush()
        1 * writerA.close()
        0 * writerA._

        1 * writerB.write((char[]) _, _, _)
        1 * writerB.flush()
        1 * writerB.close()
        0 * writerB._
    }

    def "Calls are forwarded even when exception is thrown"() {
        setup:
        def writerA = Mock(Writer)
        def writerB = Mock(Writer)
        def multiplexer = new MultiplexerWriter([writerA, writerB])
        when:
        multiplexer.write("Foo")
        then:
        def ex = thrown(IOException)
        ex.getSuppressed().length == 1
        1 * writerA.write((char[]) _, _, _) >> { throw new IOException("Foo") }

        1 * writerB.write((char[]) _, _, _)
    }

    def "Multiple exceptions are collected"() {
        setup:
        def writerA = Mock(Writer) {
            1 * it.write((char[]) _, _, _) >> { throw new RuntimeException("exA") }
        }
        def writerB = Mock(Writer) {
            1 * it.write((char[]) _, _, _) >> { throw new RuntimeException("exB") }
        }
        def multiplexer = new MultiplexerWriter([writerA, writerB])
        when:
        multiplexer.write("foo")
        then:
        def ex = thrown(IOException)
        ex.getSuppressed().length == 2
        ex.suppressed[0].message == "exA"
        ex.suppressed[1].message == "exB"
    }
}
