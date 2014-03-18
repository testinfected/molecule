package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;
import com.vtence.molecule.util.Streams;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.InflaterInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class CompressorTest {

    static final int BUFFER_SIZE = 128;
    Compressor compressor = new Compressor();

    MockRequest request = MockRequest.aRequest();
    MockResponse response = MockResponse.aResponse();

    @Test public void
    deflatesResponseChunks() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.outputStream().write("uncompressed".getBytes());
                response.outputStream().write(" ".getBytes());
                response.outputStream().write("content".getBytes());
            }
        });

        request.withHeader("Accept-Encoding", "deflate");
        compressor.handle(request, response);
        response.assertHeader("Content-Encoding", "deflate");
        assertThat("inflated content", inflate(response), equalTo("uncompressed content"));
    }

    @Test public void
    deflatesBufferedResponses() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                OutputStream out = response.outputStream(BUFFER_SIZE);
                out.write("uncompressed content".getBytes());
                out.flush();
            }
        });

        request.withHeader("Accept-Encoding", "deflate");
        compressor.handle(request, response);
        response.assertHeader("Content-Encoding", "deflate");
        assertThat("inflated content", inflate(response), equalTo("uncompressed content"));
    }

    @Test public void
    deflatesBufferedResponseBodies() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("uncompressed body");
            }
        });

        request.withHeader("Accept-Encoding", "deflate");
        compressor.handle(request, response);
        response.assertHeader("Content-Encoding", "deflate");
        assertThat("inflated body", inflate(response), equalTo("uncompressed body"));
    }

    @Test public void
    gracefullyHandlesLackOfContent() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
            }
        });

        request.withHeader("Accept-Encoding", "deflate");
        compressor.handle(request, response);
        response.assertHeader("Content-Encoding", "deflate");
    }

    @Test public void
    fallsBackToNoCompression() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("uncompressed body");
            }
        });

        request.withHeader("Accept-Encoding", "compress");
        compressor.handle(request, response);
        response.assertHeader("Content-Encoding", nullValue());
        response.assertBody("uncompressed body");
    }

    private String inflate(MockResponse response) throws IOException {
        return response.empty() ? "" : Streams.toString(new InflaterInputStream(response.stream()));
    }
}
