package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.ChunkedBody;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;

import static com.vtence.molecule.HttpHeaders.TRANSFER_ENCODING;

public class ContentLengthTest {

    ContentLength contentLength = new ContentLength();

    MockRequest request = new MockRequest();
    MockResponse response = new MockResponse();

    @Test public void
    setsContentLengthOnFixedLengthBodiesIfNoneSet() throws Exception {
        contentLength.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("This body has a size of 32 bytes");
            }
        });

        contentLength.handle(request, response);
        response.assertHeader("Content-Length", "32");
    }

    @Test public void
    doesNotSetContentLengthOnVariableLengthBodies() throws Exception {
        contentLength.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body(new ChunkedBody() {
                    public void writeTo(OutputStream out) throws IOException {
                        out.write("A variable length body".getBytes());
                    }

                    public void close() throws IOException {}
                });
            }
        });

        contentLength.handle(request, response);
        response.assertNoHeader("Content-Length");
    }

    @Test public void
    doesNotSetContentLengthOnEmptyBodies() throws Exception {
        contentLength.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
            }
        });

        contentLength.handle(request, response);
        response.assertNoHeader("Content-Length");
    }

    @Test public void
    doesNotSetContentLengthIfAlreadySet() throws Exception {
        contentLength.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.contentLength(1);
                response.body("This body is definitely larger than 1 byte");
            }
        });

        contentLength.handle(request, response);
        response.assertHeader("Content-Length", "1");
    }

    @Test public void
    doesNotSetContentLengthForChunkedTransferEncoding() throws Exception {
        contentLength.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.header(TRANSFER_ENCODING, "chunked");
                response.body("This body is chunked encoded");
            }
        });

        contentLength.handle(request, response);
        response.assertNoHeader("Content-Length");
    }
}
