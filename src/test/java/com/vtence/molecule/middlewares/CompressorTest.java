package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.helpers.Streams;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import static com.vtence.molecule.http.HttpStatus.NOT_ACCEPTABLE;
import static com.vtence.molecule.testing.BodyContent.asStream;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CompressorTest {

    Compressor compressor = new Compressor();

    Request request = new Request();
    Response response = new Response();

    @Test public void
    deflatesResponseWhenClientAcceptsDeflate() throws Exception {
        request.header("Accept-Encoding", "deflate");
        compressor.handle(request, response);
        response.body("uncompressed body").done();

        assertNoExecutionError();
        assertThat(response).hasHeader("Content-Encoding", "deflate");
        assertThat("body", inflate(response), equalTo("uncompressed body"));
    }

    @Test public void
    gzipsResponseWhenClientAcceptsGZip() throws Exception {
        request.header("Accept-Encoding", "gzip");
        compressor.handle(request, response);
        response.body("uncompressed body").done();

        assertNoExecutionError();
        assertThat(response).hasHeader("Content-Encoding", "gzip");
        assertThat("response body", unzip(response), equalTo("uncompressed body"));
    }

    @Test public void
    usesFirstAcceptedContentCoding() throws Exception {
        request.addHeader("Accept-Encoding", "gzip");
        request.addHeader("Accept-Encoding", "deflate");
        compressor.handle(request, response);
        response.body("uncompressed body").done();

        assertNoExecutionError();
        assertThat(response).hasHeader("Content-Encoding", "gzip");
        assertThat("body", unzip(response), equalTo("uncompressed body"));
    }

    @Test public void
    skipsCompressionOfEmptyContent() throws Exception {
        request.header("Accept-Encoding", "deflate");
        compressor.handle(request, response);
        response.done();

        assertNoExecutionError();
        assertThat(response).hasNoHeader("Content-Encoding");
    }

    @Test public void
    removesContentLengthHeaderWhenCompressing() throws Exception {
        request.header("Accept-Encoding", "deflate");
        compressor.handle(request, response);
        response.contentLength(128).body("uncompressed body...").done();

        assertNoExecutionError();
        assertThat(response).hasNoHeader("Content-Length");
    }

    @Test public void
    fallsBackToNoCompressionWhenClientDoesNotAcceptOurEncodings() throws Exception {
        request.header("Accept-Encoding", "compress");
        compressor.handle(request, response);
        response.body("uncompressed body").done();

        assertNoExecutionError();
        assertThat(response).hasNoHeader("Content-Encoding")
                            .hasBodyText("uncompressed body");
    }

    @Test public void
    preservesContentLengthOfIdentityResponses() throws Exception {
        request.header("Accept-Encoding", "identity");
        compressor.handle(request, response);
        response.contentLength(128).body("uncompressed body...").done();

        assertNoExecutionError();
        assertThat(response).hasHeader("Content-Length", "128");
    }

    @Test public void
    skipsCompressionIfResponseAlreadyEncoded() throws Exception {
        request.header("Accept-Encoding", "gzip");
        compressor.handle(request, response);
        response.header("Content-Encoding", "deflate").body("<compressed body>").done();

        assertNoExecutionError();
        assertThat(response).hasHeader("Content-Encoding", "deflate")
                            .hasBodyText("<compressed body>");
    }

    @Test public void
    compressesAnywayWhenContentEncodingIsIdentity() throws Exception {
        request.header("Accept-Encoding", "gzip");
        compressor.handle(request, response);
        response.header("Content-Encoding", "identity").body("uncompressed body").done();

        assertNoExecutionError();
        assertThat("body", unzip(response), equalTo("uncompressed body"));
    }

    @Test public void
    reportsLackOfAnAcceptableEncoding() throws Exception {
        request.header("Accept-Encoding", "identity;q=0");
        compressor.handle(request, response);
        response.body("uncompressed body").done();

        assertNoExecutionError();
        assertThat(response).hasStatus(NOT_ACCEPTABLE)
                            .hasContentType("text/plain")
                            .hasBodyText("An acceptable encoding could not be found");
    }

    @Test public void
    skipsMimeTypesDeemedNotCompressible() throws Exception {
        request.header("Accept-Encoding", "gzip");
        compressor.compressibleTypes("text/html");
        compressor.handle(request, response);
        response.contentType("text/plain").body("uncompressed body").done();

        assertNoExecutionError();
        assertThat(response).hasNoHeader("Content-Encoding")
                            .hasBodyText("uncompressed body");
    }

    @Test public void
    processesCompressibleMimeTypes() throws Exception {
        request.header("Accept-Encoding", "gzip");
        compressor.compressibleTypes("application/json", "text/*");
        compressor.handle(request, response);
        response.contentType("text/plain").body("uncompressed body").done();

        assertNoExecutionError();
        assertThat("body", unzip(response), equalTo("uncompressed body"));
    }

    @Test public void
    ignoresContentTypeCharsetToDecideIfContentShouldBeCompressed() throws Exception {
        request.header("Accept-Encoding", "gzip");
        compressor.compressibleTypes("text/html");
        compressor.handle(request, response);
        response.contentType("text/html; utf-8")
                .body("<html>uncompressed</html>")
                .done();

        assertNoExecutionError();
        assertThat("body", unzip(response), equalTo("<html>uncompressed</html>"));
    }

    private String inflate(Response response) throws IOException {
        return response.empty() ? "" : Streams.toString(new InflaterInputStream(asStream(response), new Inflater(true)));
    }

    private String unzip(Response response) throws IOException {
        return response.empty() ? "" : Streams.toString(new GZIPInputStream(asStream(response)));
    }

    private void assertNoExecutionError() throws ExecutionException, InterruptedException {
        response.await();
    }
}