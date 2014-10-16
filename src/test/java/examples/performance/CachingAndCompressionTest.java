package examples.performance;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.HttpRequest;
import com.vtence.molecule.support.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static com.vtence.molecule.http.HttpDate.httpDate;
import static com.vtence.molecule.support.Dates.calendarDate;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class CachingAndCompressionTest {
    CachingAndCompressionExample caching = new CachingAndCompressionExample();
    WebServer server = WebServer.create(9999);

    HttpRequest request = new HttpRequest(9999).withTimeout(120000);
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
        response = request.get("/");
        response.assertOK();
        // Stupid Apache Http-Client removes Content-Encoding (and Content-Length) header for compressed content,
        // so we can't assert its there. In fact we know that content was compressed if is not chunked
        // and Content-Length header is missing!
        response.assertNotChunked();
        response.assertHasHeader("Content-Length", nullValue());
    }

    @Test
    public void addingETagValidatorAndCacheDirectivesToDynamicContent() throws IOException {
        response = request.get("/");
        response.assertOK();
        response.assertHasContentType("text/html");
        // We expect an ETag, since no other validation information is generated
        response.assertHasHeader("ETag", notNullValue());
        // These are the default cache directives
        response.assertHasHeader("Cache-Control", "max-age=0; private; no-cache");
    }

    @Test public void
    notGeneratingTheResponseBodyWhenETagHasNotChanged() throws IOException {
        response = request.get("/");
        response.assertOK();
        response.assertHasHeader("ETag", notNullValue());

        // Play the same request with freshness information...
        response = request.but().withHeader("If-None-Match", response.header("ETag")).send();
        // ... and expect a not modified
        response.assertHasStatusCode(304);
    }

    @Test
    public void addingLastModifiedValidatorAndCacheDirectivesToStaticFiles() throws IOException {
        response = request.get("/js/fox.js");
        response.assertOK();
        response.assertHasContentType("application/javascript");
        // Static files come with validation information
        response.assertHasHeader("Last-Modified", notNullValue());
        // We shouldn't add an ETag, since there's already validation information
        response.assertHasHeader("ETag", nullValue());
        // Our own cache directives
        response.assertHasHeader("Cache-Control", "public; max-age=60");
    }

    @Test public void
    notGeneratingTheResponseBodyWhenResourceHasNotBeenModified() throws IOException {
        Date timestamp = calendarDate(2014, 10, 14).atTime(21, 20, 0).toDate();

        response = request.withParameter("timestamp", httpDate(timestamp)).get("/");
        response.assertOK();
        response.assertHasHeader("Last-Modified", notNullValue());

        // Play the same request with freshness information...
        response = request.but().withHeader("If-Modified-Since", response.header("Last-Modified")).send();
        // ... and expect a not modified
        response.assertHasStatusCode(304);
    }
}