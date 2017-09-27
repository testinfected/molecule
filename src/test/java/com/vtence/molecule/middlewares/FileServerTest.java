package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.vtence.molecule.http.HttpDate.format;
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

    @Test public void
    servesFiles() throws Exception {
        Response response = fileServer.handle(Request.get(SAMPLE_IMAGE));

        assertThat(response).hasStatus(OK)
                            .hasHeader("Content-Length", valueOf(file.length()))
                            .hasBodySize(file.length())
                            .hasBodyContent(contentOf(file))
                            .isDone();
    }

    @Test public void
    guessesMimeTypeFromExtension() throws Exception {
        Response response = fileServer.handle(Request.get(SAMPLE_IMAGE));

        assertThat(response).hasContentType("image/png");
    }

    @Test public void
    learnsNewMediaTypes() throws Exception {
        fileServer.registerMediaType("png", "image/custom-png");
        Response response = fileServer.handle(Request.get(SAMPLE_IMAGE));

        assertThat(response).hasContentType("image/custom-png");
    }

    @Test public void
    setsLastModifiedHeader() throws Exception {
        Response response = fileServer.handle(Request.get(SAMPLE_IMAGE));

        assertThat(response).hasHeader("Last-Modified", format(file.lastModified()));
    }

    @Test public void
    rendersNotFoundWhenFileIsNotFound() throws Exception {
        Response response = fileServer.handle(Request.get("/images/missing.png"));

        assertThat(response).hasStatus(NOT_FOUND)
                            .hasContentType("text/plain")
                            .hasBodyText("File not found: /images/missing.png")
                            .isDone();
    }

    @Test public void
    rendersNotFoundWhenFileIsNotReadable() throws Exception {
        Response response = fileServer.handle(Request.get("/images"));

        assertThat(response).hasStatus(NOT_FOUND);
    }

    @Test public void
    sendsNotModifiedIfFileHasNotBeenModifiedSinceLastServe() throws Exception {
        Response response = fileServer.handle(Request.get(SAMPLE_IMAGE)
                                                     .header("If-Modified-Since", format(file.lastModified())));

        assertThat(response).hasStatus(NOT_MODIFIED).isDone();
    }

    @Test public void
    addsConfiguredCustomHeadersToResponse() throws Exception {
        fileServer.header("Cache-Control", "public, max-age=60")
                  .header("Access-Control-Allow-Origin", "*");

        Response response = fileServer.handle(Request.get(SAMPLE_IMAGE));
        assertThat(response).hasHeader("Cache-Control", "public, max-age=60")
                            .hasHeader("Access-Control-Allow-Origin", "*");
    }

    @Test public void
    allowsHeadRequests() throws Exception {
        Response response = fileServer.handle(Request.head(SAMPLE_IMAGE));

        assertThat(response).hasStatus(OK)
                            .hasHeader("Content-Length", valueOf(file.length()))
                            .hasBodySize(0)
                            .isDone();
    }

    @Test public void
    rejectsUnsupportedMethod() throws Exception {
        Response response = fileServer.handle(Request.post(SAMPLE_IMAGE));

        assertThat(response).hasStatus(METHOD_NOT_ALLOWED)
                            .hasHeader("Allow", "GET, HEAD")
                            .hasNoHeader("Last-Modified")
                            .isDone();
    }

    private byte[] contentOf(final File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }
}