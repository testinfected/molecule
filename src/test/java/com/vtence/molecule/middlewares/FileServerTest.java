package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.helpers.Streams;
import com.vtence.molecule.http.HttpDate;
import com.vtence.molecule.http.HttpMethod;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.vtence.molecule.http.HttpMethod.GET;
import static com.vtence.molecule.http.HttpStatus.METHOD_NOT_ALLOWED;
import static com.vtence.molecule.http.HttpStatus.NOT_FOUND;
import static com.vtence.molecule.http.HttpStatus.NOT_MODIFIED;
import static com.vtence.molecule.http.HttpStatus.OK;
import static com.vtence.molecule.testing.ResourceLocator.onClasspath;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static java.lang.String.valueOf;

public class FileServerTest {

    static final String SAMPLE_IMAGE = "/images/sample.png";

    File base = onClasspath().locate("assets");
    FileServer fileServer = new FileServer(base);
    File file = new File(base, SAMPLE_IMAGE);
    Request request = new Request().method(GET).path(SAMPLE_IMAGE);
    Response response = new Response();

    @Test public void
    servesFiles() throws Exception {
        fileServer.handle(request, response);

        assertThat(response).hasStatus(OK)
                            .hasHeader("Content-Length", valueOf(file.length()))
                            .hasBodySize(file.length())
                            .hasBodyContent(contentOf(file));
    }

    @Test public void
    guessesMimeTypeFromExtension() throws Exception {
        fileServer.handle(request, response);

        assertThat(response).hasContentType("image/png");
    }

    @Test public void
    learnsNewMediaTypes() throws Exception {
        fileServer.registerMediaType("png", "image/custom-png");
        fileServer.handle(request, response);

        assertThat(response).hasContentType("image/custom-png");
    }

    @Test public void
    setsLastModifiedHeader() throws Exception {
        fileServer.handle(request, response);

        assertThat(response).hasHeader("Last-Modified", HttpDate.format(file.lastModified()));
    }

    @Test public void
    rendersNotFoundWhenFileIsNotFound() throws Exception {
        fileServer.handle(request.path("/images/missing.png"), response);

        assertThat(response).hasStatus(NOT_FOUND)
                            .hasContentType("text/plain")
                            .hasBodyText("File not found: /images/missing.png");
    }

    @Test public void
    rendersNotFoundWhenFileIsNotReadable() throws Exception {
        fileServer.handle(request.path("/images"), response);

        assertThat(response).hasStatus(NOT_FOUND);
    }

    @Test public void
    sendsNotModifiedIfFileHasNotBeenModifiedSinceLastServe() throws Exception {
        request.header("If-Modified-Since", HttpDate.format(file.lastModified()));
        fileServer.handle(request, response);

        assertThat(response).hasStatus(NOT_MODIFIED);
    }

    @Test public void
    addsConfiguredCustomHeadersToResponse() throws Exception {
        fileServer.header("Cache-Control", "public, max-age=60")
                  .header("Access-Control-Allow-Origin", "*");

        fileServer.handle(request, response);
        assertThat(response).hasHeader("Cache-Control", "public, max-age=60")
                            .hasHeader("Access-Control-Allow-Origin", "*");
    }

    @Test public void
    allowsHeadRequests() throws Exception {
        fileServer.handle(request.method(HttpMethod.HEAD), response);

        assertThat(response).hasStatus(OK)
                            .hasHeader("Content-Length", valueOf(file.length()))
                            .hasBodySize(0);
    }

    @Test public void
    rejectsUnsupportedMethod() throws Exception {
        fileServer.handle(request.method(HttpMethod.POST), response);

        assertThat(response).hasStatus(METHOD_NOT_ALLOWED)
                            .hasHeader("Allow", "GET, HEAD")
                            .hasNoHeader("Last-Modified");
    }

    private byte[] contentOf(final File file) throws IOException {
        return Streams.toBytes(new FileInputStream(file));
    }
}