package com.vtence.molecule.middlewares;

import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;
import com.vtence.molecule.util.Streams;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static com.vtence.molecule.HttpStatus.NOT_FOUND;
import static com.vtence.molecule.HttpStatus.OK;
import static com.vtence.molecule.support.MockRequest.GET;
import static com.vtence.molecule.support.MockResponse.aResponse;

public class FileServerTest {

    static final String TEST_IMAGE = "assets/image.png";

    File base = locateBase();
    FileServer fileServer = new FileServer(base);
    File file = new File(base, TEST_IMAGE);

    MockRequest request = GET(TEST_IMAGE);
    MockResponse response = aResponse();

    private static File locateBase() {
        URL fileLocation = FileServerTest.class.getClassLoader().getResource(TEST_IMAGE);
        if (fileLocation == null) throw new AssertionError("Image not found: " + TEST_IMAGE);
        File asset;
        try {
            asset = new File(fileLocation.toURI());
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        return asset.getParentFile().getParentFile();
    }

    @Test public void
    rendersFile() throws Exception {
        fileServer.handle(request, response);

        response.assertStatus(OK);
        response.assertContentSize(file.length());
        response.assertContent(contentOf(file));
    }

    @Test public void
    guessesMimeTypeFromExtension() throws Exception {
        fileServer.handle(request, response);

        response.assertContentType("image/png");
    }

    @Test public void
    learnsNewMediaTypes() throws Exception {
        fileServer.registerMediaType("png", "image/custom-png");
        fileServer.handle(request, response);

        response.assertContentType("image/custom-png");
    }

    @Test public void
    setsFileResponseHeaders() throws Exception {
        fileServer.handle(request, response);

        response.assertHeader("Content-Length", String.valueOf(file.length()));
        response.assertHeader("Last-Modified", file.lastModified());
    }

    @Test public void
    rendersNotFoundWhenFileIsNotFound() throws Exception {
        fileServer.handle(request.withPath("/images/missing.png"), response);
        response.assertStatus(NOT_FOUND);
    }

    private byte[] contentOf(final File file) throws IOException, URISyntaxException {
        return Streams.toBytes(new FileInputStream(file));
    }
}