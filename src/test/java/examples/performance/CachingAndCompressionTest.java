package examples.performance;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.Delorean;
import com.vtence.molecule.testing.http.HttpRequest;
import com.vtence.molecule.testing.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

public class CachingAndCompressionTest {
    Delorean delorean = new Delorean();
    CachingAndCompressionExample caching = new CachingAndCompressionExample(delorean);
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        caching.run(server);
    }

    @After
    public void stopServer() throws IOException {
        server.stop();
    }

    @Test
    public void compressingResponses() throws IOException {
        String contentLengthUnderJvm16 = "170";
        String contentLengthUnderJvm18 = "173";

        response = request.header("Accept-Encoding", "gzip; q=0.9, deflate").get("/");

        assertThat(response).isOK()
                            // We expect deflate compression, which is preferred by the client
                            .hasHeader("Content-Encoding", "deflate")
                            .hasHeader("Content-Length", anyOf(equalTo(contentLengthUnderJvm16), equalTo(contentLengthUnderJvm18)));
    }

    @Test
    public void addingETagValidatorAndCacheDirectivesToDynamicContent() throws IOException {
        response = request.get("/");

        assertThat(response).isOK()
                            .hasContentType("text/html")
                            // We expect an ETag, since no other validation information is generated
                            .hasHeader("ETag")
                            // These are the default cache directives
                            .hasHeader("Cache-Control", "max-age=0; private; no-cache");
    }

    @Test public void
    notGeneratingTheResponseBodyWhenETagHasNotChanged() throws IOException {
        response = request.get("/");

        assertThat(response).isOK()
                            .hasHeader("ETag");

        // Play the same request with freshness information...
        response = request.header("If-None-Match", response.header("ETag")).get("/");
        // ... and expect a not modified
        assertThat(response).hasStatusCode(304);
    }

    @Test
    public void addingLastModifiedValidatorAndCacheDirectivesToStaticFiles() throws IOException {
        response = request.get("/js/fox.js");

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
    notGeneratingTheResponseBodyWhenResourceHasNotBeenModified() throws IOException {
        // Freeze time to get the same validation information for the resource on subsequent calls
        delorean.freeze();
        // Request freshness information with the response
        response = request.get("/?conditional");

        assertThat(response).isOK()
                            // We expect a Last-Modified header with freshness information
                            .hasHeader("Last-Modified");

        // Play the same request with freshness information...
        response = request.header("If-Modified-Since", response.header("Last-Modified")).get("/?conditional");
        // ... and expect a not modified
        assertThat(response).hasStatusCode(304);
    }
}