package examples.performance;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.helpers.HexEncoder;
import com.vtence.molecule.support.Delorean;
import org.hamcrest.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Arrays;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static examples.performance.CachingAndCompressionTest.StartsWithBytes.startsWithBytes;
import static java.net.http.HttpResponse.BodyHandlers.ofByteArray;
import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class CachingAndCompressionTest {
    Delorean delorean = new Delorean();
    CachingAndCompressionExample caching = new CachingAndCompressionExample(delorean);
    WebServer server = WebServer.create(9999);

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest.Builder request = HttpRequest.newBuilder(server.uri());
    String GZIP_HEADER = "1f8b08";

    @Before
    public void startServer() throws IOException {
        caching.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void compressingResponses() throws Exception {
        request.header("Accept-Encoding", "deflate; q=0.9, gzip");
        var response = client.send(request.build(), ofByteArray());

        assertThat(response).isOK()
                // We expect gzip compression, which is preferred by the client
                .hasHeader("Content-Encoding", "gzip")
                .hasBody(startsWithBytes(GZIP_HEADER));
    }

    @Test
    public void addingETagValidatorAndCacheDirectivesToDynamicContent() throws Exception {
        var response = client.send(request.build(), ofString());

        assertThat(response).isOK()
                            .hasContentType("text/html")
                            // We expect an ETag, since no other validation information is generated
                            .hasHeader("ETag")
                            // These are the default cache directives
                            .hasHeader("Cache-Control", "max-age=0; private; no-cache");
    }

    @Test public void
    notGeneratingTheResponseBodyWhenETagHasNotChanged() throws Exception {
        var response = client.send(request.build(), ofString());

        assertThat(response).isOK()
                            .hasHeader("ETag");

        // Play the same request with freshness information...
        request.header("If-None-Match", response.headers().firstValue("ETag").orElse(null));
        var notModified = client.send(request.GET().build(), ofString());
        // ... and expect a not modified
        assertThat(notModified).hasStatusCode(304);
    }

    @Test
    public void addingLastModifiedValidatorAndCacheDirectivesToStaticFiles() throws Exception {
        var response = client.send(request.uri(server.uri().resolve("/js/fox.js")).build(), ofString());

        assertThat(response).isOK()
                            .hasContentType("application/javascript")
                            // Static files come with validation information
                            .hasHeader("Last-Modified")
                            // We don't expect ETag, since there's already freshness information
                            .hasNoHeader("ETag")
                            // Our own cache directives
                            .hasHeader("Cache-Control", "public; max-age=60");
    }

    @Test public void
    notGeneratingTheResponseBodyWhenResourceHasNotBeenModified() throws Exception {
        // Freeze time to get the same validation information for the resource on subsequent calls
        delorean.freeze();
        // Request freshness information with the response
        var response = client.send(request.uri(server.uri().resolve("/?conditional")).build(), ofString());

        assertThat(response).isOK()
                            // We expect a Last-Modified header with freshness information
                            .hasHeader("Last-Modified");

        request.header("If-Modified-Since", response.headers().firstValue("Last-Modified").orElse(null));
        // Play the same request with freshness information...
        var notModified = client.send(request.uri(server.uri().resolve("/?conditional")).build(), ofString());
        // ... and expect a not modified
        assertThat(notModified).hasStatusCode(304);
    }

    static class StartsWithBytes extends TypeSafeMatcher<byte[]> {
        private final byte[] start;

        public StartsWithBytes(byte[] start) {
            super(byte[].class);
            this.start = start;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("gzipped content");
        }

        @Override
        protected boolean matchesSafely(byte[] actual) {
            return Arrays.mismatch(actual, start) >= start.length;
        }

        public static Matcher<byte[]> startsWithBytes(byte[] bytes) {
            return new StartsWithBytes(bytes);
        }

        public static Matcher<byte[]> startsWithBytes(String hex) {
            return startsWithBytes(new HexEncoder().fromHex(hex));
        }
    }
}