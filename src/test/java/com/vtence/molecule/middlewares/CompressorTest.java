package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.support.MockRequest;
import com.vtence.molecule.support.MockResponse;
import com.vtence.molecule.helpers.Streams;
import org.junit.Test;

import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import static com.vtence.molecule.http.HttpStatus.NOT_ACCEPTABLE;
import static com.vtence.molecule.support.ResponseAssertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CompressorTest {

    Compressor compressor = new Compressor();

    MockRequest request = new MockRequest();
    MockResponse response = new MockResponse();

    @Test public void
    deflatesResponseWhenClientAcceptsDeflate() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("uncompressed body");
            }
        });

        request.header("Accept-Encoding", "deflate");
        compressor.handle(request, response);
        response.assertHeader("Content-Encoding", "deflate");
        assertThat("body", inflate(response), equalTo("uncompressed body"));
    }

    @Test public void
    gzipsResponseWhenClientAcceptsGZip() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("uncompressed body");
            }
        });

        request.header("Accept-Encoding", "gzip");
        compressor.handle(request, response);
        response.assertHeader("Content-Encoding", "gzip");
        assertThat("body", unzip(response), equalTo("uncompressed body"));
    }

    @Test public void
    usesFirstAcceptedContentCoding() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("uncompressed body");
            }
        });

        request.addHeader("Accept-Encoding", "gzip");
        request.addHeader("Accept-Encoding", "deflate");
        compressor.handle(request, response);
        response.assertHeader("Content-Encoding", "gzip");
        assertThat("body", unzip(response), equalTo("uncompressed body"));
    }

    @Test public void
    skipsCompressionOfEmptyContent() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
            }
        });

        request.header("Accept-Encoding", "deflate");
        compressor.handle(request, response);
        response.assertNoHeader("Content-Encoding");
    }

    @Test public void
    removesContentLengthHeaderWhenCompressing() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.contentLength(128);
                response.body("uncompressed body...");
            }
        });

        request.header("Accept-Encoding", "deflate");
        compressor.handle(request, response);
        response.assertNoHeader("Content-Length");
    }

    @Test public void
    fallsBackToNoCompressionWhenClientDoesNotAcceptOurEncodings() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("uncompressed body");
            }
        });

        request.header("Accept-Encoding", "compress");
        compressor.handle(request, response);
        response.assertNoHeader("Content-Encoding");
        response.assertBody("uncompressed body");
    }

    @Test public void
    preservesContentLengthOfIdentityResponses() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.contentLength(128);
                response.body("uncompressed body...");
            }
        });

        request.header("Accept-Encoding", "identity");
        compressor.handle(request, response);
        response.assertHeader("Content-Length", "128");
    }

    @Test public void
    skipsCompressionIfResponseAlreadyEncoded() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.header("Content-Encoding", "deflate");
                response.body("<compressed body>");
            }
        });

        request.header("Accept-Encoding", "gzip");
        compressor.handle(request, response);
        response.assertHeader("Content-Encoding", "deflate");
        response.assertBody("<compressed body>");
    }

    @Test public void
    compressesAnywayWhenContentEncodingIsIdentity() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.header("Content-Encoding", "identity");
                response.body("uncompressed body");
            }
        });

        request.header("Accept-Encoding", "gzip");
        compressor.handle(request, response);
        assertThat("body", unzip(response), equalTo("uncompressed body"));
    }

    @Test public void
    reportsLackOfAnAcceptableEncoding() throws Exception {
        compressor.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("uncompressed body");
            }
        });
        request.header("Accept-Encoding", "identity;q=0");
        compressor.handle(request, response);
        assertThat(response).hasStatus(NOT_ACCEPTABLE);
        response.assertContentType("text/plain");
        response.assertBody("An acceptable encoding could not be found");
    }

    @Test public void
    skipsMimeTypesDeemedNotCompressible() throws Exception {
        compressor.connectTo(new Application() {
            @Override
            public void handle(Request request, Response response) throws Exception {
                response.contentType("text/plain");
                response.body("uncompressed body");
            }
        });
        request.header("Accept-Encoding", "gzip");
        compressor.compressibleTypes("text/html");
        compressor.handle(request, response);
        response.assertNoHeader("Content-Encoding");
        assertThat("body", response.text(), equalTo("uncompressed body"));
    }

    @Test public void
    processesCompressibleMimeTypes() throws Exception {
        compressor.connectTo(new Application() {
            @Override
            public void handle(Request request, Response response) throws Exception {
                response.contentType("text/plain");
                response.body("uncompressed body");
            }
        });
        request.header("Accept-Encoding", "gzip");
        compressor.compressibleTypes("application/json", "text/*");
        compressor.handle(request, response);
        assertThat("body", unzip(response), equalTo("uncompressed body"));
    }

    private String inflate(MockResponse response) throws IOException {
        return response.empty() ? "" : Streams.toString(new InflaterInputStream(response.stream(), new Inflater(true)));
    }

    private String unzip(MockResponse response) throws IOException {
        return response.empty() ? "" : Streams.toString(new GZIPInputStream(response.stream()));
    }
}
