package io.github.beatbrot.dependencyreport.internal.gradle


import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpError
import org.mockserver.model.HttpResponse
import spock.lang.AutoCleanup
import spock.lang.ResourceLock
import spock.lang.Shared
import spock.lang.Specification

import static org.mockserver.model.HttpRequest.request

@ResourceLock("mockServer")
class GradleVersionFetcherTest extends Specification {
    @Shared
    @AutoCleanup("stop")
    ClientAndServer mockServer = ClientAndServer.startClientAndServer()

    @Shared
    def fetcher = new GradleVersionFetcher("http://localhost:${mockServer.port}/versions/current")


    @Shared
    def defaultRequest = request("/versions/current").withMethod("GET")

    def cleanup() {
        mockServer.reset()
    }

    def "404 Response"() {
        setup:
        mockServer.when(defaultRequest)
                .respond(HttpResponse.notFoundResponse())

        when:
        fetcher.getLatestGradleVersion()
        then:
        thrown(GradleVersionFetcher.GradleVersionQueryException)
    }

    def "Connection not successful"() {
        setup:
        mockServer.when(defaultRequest)
                .error(HttpError.error().withDropConnection(true))

        when:
        fetcher.getLatestGradleVersion()
        then:
        thrown(GradleVersionFetcher.GradleVersionQueryException)
    }

    def "Minified response"(String versionString) {
        setup:
        String response = '{"version":"' + versionString + '","buildTime":"20221125133510+0000","current":true,"snapshot":false,"nightly":false,"releaseNightly":false,"activeRc":false,"rcFor":"","milestoneFor":"","broken":false,"downloadUrl":"https://services.gradle.org/distributions/gradle-7.6-bin.zip","checksumUrl":"https://services.gradle.org/distributions/gradle-7.6-bin.zip.sha256","wrapperChecksumUrl":"https://services.gradle.org/distributions/gradle-7.6-wrapper.jar.sha256"}'
        mockServer.when(defaultRequest)
                .respond(HttpResponse.response(response))
        when:
        def result = fetcher.getLatestGradleVersion()
        then:
        result == versionString

        where:
        versionString << ["7.6", "8.0-rc-1", "8.1-20230113104322+0000", "7.6.1-20230112032253+0000"]
    }

    def "Non-Minified response"(String versionString) {
        setup:
        String response = """{
  "version" : "$versionString",
  "buildTime" : "20221125133510+0000",
  "current" : true,
  "snapshot" : false,
  "nightly" : false,
  "releaseNightly" : false,
  "activeRc" : false,
  "rcFor" : "",
  "milestoneFor" : "",
  "broken" : false,
  "downloadUrl" : "https://services.gradle.org/distributions/gradle-7.6-bin.zip",
  "checksumUrl" : "https://services.gradle.org/distributions/gradle-7.6-bin.zip.sha256",
  "wrapperChecksumUrl" : "https://services.gradle.org/distributions/gradle-7.6-wrapper.jar.sha256"
}"""
        mockServer.when(defaultRequest)
                .respond(HttpResponse.response(response))
        when:
        def result = fetcher.getLatestGradleVersion()
        then:
        result == versionString

        where:
        versionString << ["7.6", "8.0-rc-1", "8.1-20230113104322+0000", "7.6.1-20230112032253+0000"]
    }
}
