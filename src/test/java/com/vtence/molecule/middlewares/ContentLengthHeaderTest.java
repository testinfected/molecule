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

    @Test public void
    setsContentLengthOnFixedLengthBodiesIfNoneSet() throws Exception {
        Response response = contentLengthHeader.then(request -> Response.ok()
                                                                        .done("This body has a size of 32 bytes"))
                                               .handle(Request.get("/"));

        assertNoExecutionError(response);
        assertThat(response).hasHeader("Content-Length", "32");
    }

    @Test public void
    doesNotSetContentLengthOnVariableLengthBodies() throws Exception {
        Response response = contentLengthHeader.then(request -> Response.ok()
                                                                        .done(new VariableLengthBody()))
                                               .handle(Request.get("/"));

        assertNoExecutionError(response);
        assertThat(response).hasNoHeader("Content-Length");
    }

    @Test public void
    doesNotSetContentLengthOnEmptyBodies() throws Exception {
        Response response = contentLengthHeader.then(request -> Response.ok()
                                                                        .done())
                                               .handle(Request.get("/"));

        assertNoExecutionError(response);
        assertThat(response).hasNoHeader("Content-Length");
    }

    @Test public void
    doesNotSetContentLengthIfAlreadySet() throws Exception {
        Response response = contentLengthHeader.then(request -> Response.ok()
                                                                        .contentLength(1)
                                                                        .done("This body is definitely larger than 1 byte"))
                                               .handle(Request.get("/"));

        assertNoExecutionError(response);
        assertThat(response).hasHeader("Content-Length", "1");
    }

    @Test public void
    doesNotSetContentLengthForChunkedTransferEncoding() throws Exception {
        Response response = contentLengthHeader.then(request -> Response.ok()
                                                                        .header(TRANSFER_ENCODING, "chunked")
                                                                        .done("This body is chunked encoded"))
                                               .handle(Request.get("/"));

        assertNoExecutionError(response);
        assertThat(response).hasNoHeader("Content-Length");
    }

    private void assertNoExecutionError(Response response) throws ExecutionException, InterruptedException {
        response.await();
    }

    private static class VariableLengthBody extends ChunkedBody {
        public void writeTo(OutputStream out, Charset charset) throws IOException {
            out.write("A variable length body".getBytes(charset));
        }
    }
}