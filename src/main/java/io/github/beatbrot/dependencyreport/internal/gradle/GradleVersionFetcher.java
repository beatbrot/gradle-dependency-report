package io.github.beatbrot.dependencyreport.internal.gradle;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

class GradleVersionFetcher {

    private static final String REQUEST_METHOD = "GET";
    private static final int HTTP_ERROR_CODE_START = 400;
    private static final String DEFAULT_VERSION_URL = "https://services.gradle.org/versions/current";
    private static final Pattern VERSION_PATTERN = Pattern.compile("\"version\":\"([^\"]*)\"");
    private final URL url;

    // Visible for testing
    GradleVersionFetcher(@SuppressWarnings("SameParameterValue") final String url) {
        try {
            this.url = new URL(url);
        } catch (final MalformedURLException e) {
            throw GradleVersionQueryException.invalidUrl(url, e);
        }
    }

    public GradleVersionFetcher() {
        this(DEFAULT_VERSION_URL);
    }

    public String getLatestGradleVersion() {
        final String rawResponse = doRequest();
        return extractVersion(rawResponse);
    }

    private String doRequest() {
        try {
            final HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod(REQUEST_METHOD);
            final int responseCode = c.getResponseCode();
            if (responseCode >= HTTP_ERROR_CODE_START) {
                throw GradleVersionQueryException.unexpectedResponseCode(responseCode);
            }
            return readUtf8Stream(c.getInputStream());
        } catch (final IOException io) {
            throw GradleVersionQueryException.ioException(io);
        }
    }

    private static String readUtf8Stream(final InputStream i) throws IOException {
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(i, UTF_8))) {
            return in.lines().collect(Collectors.joining());
        }
    }

    private String extractVersion(final String response) {
        final String minified = response.replaceAll("\\s", "");
        final Matcher matcher = VERSION_PATTERN.matcher(minified);
        if (!matcher.find()) {
            throw GradleVersionQueryException.couldNotParse(url);
        }
        return matcher.group(1);
    }

    public static class GradleVersionQueryException extends RuntimeException {
        private GradleVersionQueryException(final String message, @Nullable final Throwable cause) {
            super(message, cause);
        }

        public static GradleVersionQueryException unexpectedResponseCode(final int status) {
            throw new GradleVersionQueryException("Unexpected status code " + status + " when querying latest Gradle version.", null);
        }

        public static GradleVersionQueryException couldNotParse(final URL url) {
            throw new GradleVersionQueryException("Could not parse Gradle version information from " + url, null);
        }

        public static GradleVersionQueryException invalidUrl(final String url, final Exception cause) {
            throw new GradleVersionQueryException("Invalid URL specified: " + url, cause);
        }

        public static GradleVersionQueryException ioException(final IOException ex) {
            throw new GradleVersionQueryException("Could not fetch Gradle version information: " + ex.getMessage(), ex);
        }
    }
}
