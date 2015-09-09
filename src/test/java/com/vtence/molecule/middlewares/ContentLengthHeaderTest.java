package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.lib.ChunkedBody;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

import static com.vtence.molecule.http.HeaderNames.TRANSFER_ENCODING;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;

public class ContentLengthHeaderTest {

    ContentLengthHeader contentLengthHeader = new ContentLengthHeader();

    Request request = new Request();
    Response response = new Response();

    @Test public void
    setsContentLengthOnFixedLengthBodiesIfNoneSet() throws Exception {
        contentLengthHeader.handle(request, response);
        response.body("This body has a size of 32 bytes")
                .done();

        assertNoExecutionError();
        assertThat(response).hasHeader("Content-Length", "32");
    }

    @Test public void
    doesNotSetContentLengthOnVariableLengthBodies() throws Exception {
        contentLengthHeader.handle(request, response);
        response.body(new VariableLengthBody()).done();

        assertNoExecutionError();
        assertThat(response).hasNoHeader("Content-Length");
    }

    @Test public void
    doesNotSetContentLengthOnEmptyBodies() throws Exception {
        contentLengthHeader.handle(request, response);
        response.done();

        assertNoExecutionError();
        assertThat(response).hasNoHeader("Content-Length");
    }

    @Test public void
    doesNotSetContentLengthIfAlreadySet() throws Exception {
        contentLengthHeader.handle(request, response);
        response.contentLength(1)
                .body("This body is definitely larger than 1 byte")
                .done();

        assertNoExecutionError();
        assertThat(response).hasHeader("Content-Length", "1");
    }

    @Test public void
    doesNotSetContentLengthForChunkedTransferEncoding() throws Exception {
        contentLengthHeader.handle(request, response);
        response.header(TRANSFER_ENCODING, "chunked")
                .body("This body is chunked encoded")
                .done();

        assertNoExecutionError();
        assertThat(response).hasNoHeader("Content-Length");
    }

    private void assertNoExecutionError() throws ExecutionException, InterruptedException {
        response.await();
    }

    private static class VariableLengthBody extends ChunkedBody {
        public void writeTo(OutputStream out, Charset charset) throws IOException {
            out.write("A variable length body".getBytes(charset));
        }

        public void close() throws IOException {
        }
    }
}