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

    @Test public void
    deflatesResponseWhenClientAcceptsDeflate() throws Exception {
        Response response = compressor.then(request -> Response.ok()
                                                               .done("uncompressed body"))
                                      .handle(Request.get("/")
                                                     .header("Accept-Encoding", "deflate"));

        assertNoExecutionError(response);
        assertThat(response).hasHeader("Content-Encoding", "deflate");
        assertThat("body", inflate(response), equalTo("uncompressed body"));
    }

    @Test public void
    gzipsResponseWhenClientAcceptsGZip() throws Exception {
        Response response = compressor.then(request -> Response.ok()
                                                               .done("uncompressed body"))
                                      .handle(Request.get("/")
                                                     .header("Accept-Encoding", "gzip"));

        assertNoExecutionError(response);
        assertThat(response).hasHeader("Content-Encoding", "gzip");
        assertThat("response body", unzip(response), equalTo("uncompressed body"));
    }

    @Test public void
    usesFirstAcceptedContentCoding() throws Exception {
        Response response = compressor.then(request -> Response.ok()
                                                               .done("uncompressed body"))
                                      .handle(Request.get("/")
                                                     .addHeader("Accept-Encoding", "gzip")
                                                     .addHeader("Accept-Encoding", "deflate"));

        assertNoExecutionError(response);
        assertThat(response).hasHeader("Content-Encoding", "gzip");
        assertThat("body", unzip(response), equalTo("uncompressed body"));
    }

    @Test public void
    skipsCompressionOfEmptyContent() throws Exception {
        Response response = compressor.then(request -> Response.ok()
                                                               .done())
                                      .handle(Request.get("/")
                                                     .header("Accept-Encoding", "deflate"));

        assertNoExecutionError(response);
        assertThat(response).hasNoHeader("Content-Encoding");
    }

    @Test public void
    removesContentLengthHeaderWhenCompressing() throws Exception {
        Response response = compressor.then(request -> Response.ok()
                                                               .contentLength(128)
                                                               .done("uncompressed body..."))
                                      .handle(Request.get("/")
                                                     .header("Accept-Encoding", "deflate"));

        assertNoExecutionError(response);
        assertThat(response).hasNoHeader("Content-Length");
    }

    @Test public void
    fallsBackToNoCompressionWhenClientDoesNotAcceptOurEncodings() throws Exception {
        Response response = compressor.then(request -> Response.ok()
                                                               .body("uncompressed body")
                                                               .done())
                                      .handle(Request.get("/")
                                                     .header("Accept-Encoding", "compress"));

        assertNoExecutionError(response);
        assertThat(response).hasNoHeader("Content-Encoding")
                            .hasBodyText("uncompressed body");
    }

    @Test public void
    preservesContentLengthOfIdentityResponses() throws Exception {
        Response response = compressor.then(request -> Response.ok()
                                                               .contentLength(128)
                                                               .done("uncompressed body..."))
                                      .handle(Request.get("/")
                                                     .header("Accept-Encoding", "identity"));

        assertNoExecutionError(response);
        assertThat(response).hasHeader("Content-Length", "128");
    }

    @Test public void
    skipsCompressionIfResponseAlreadyEncoded() throws Exception {
        Response response = compressor.then(request -> Response.ok()
                                                               .header("Content-Encoding", "deflate")
                                                               .done("<compressed body>"))
                                      .handle(Request.get("/")
                                                     .header("Accept-Encoding", "gzip"));

        assertNoExecutionError(response);
        assertThat(response).hasHeader("Content-Encoding", "deflate")
                            .hasBodyText("<compressed body>");
    }

    @Test public void
    compressesAnywayWhenContentEncodingIsIdentity() throws Exception {
        Response response = compressor.then(request -> Response.ok()
                                                               .header("Content-Encoding", "identity")
                                                               .done("uncompressed body"))
                                      .handle(Request.get("/")
                                                     .header("Accept-Encoding", "gzip"));

        assertNoExecutionError(response);
        assertThat("body", unzip(response), equalTo("uncompressed body"));
    }

    @Test public void
    reportsLackOfAnAcceptableEncoding() throws Exception {
        Response response = compressor.then(request -> Response.ok()
                                                               .done("uncompressed body"))
                                      .handle(Request.get("/")
                                                     .header("Accept-Encoding", "identity;q=0"));

        assertNoExecutionError(response);
        assertThat(response).hasStatus(NOT_ACCEPTABLE)
                            .hasContentType("text/plain")
                            .hasBodyText("An acceptable encoding could not be found");
    }

    @Test public void
    skipsMimeTypesDeemedNotCompressible() throws Exception {
        compressor.compressibleTypes("text/html");

        Response response = compressor.then(request -> Response.ok()
                                                               .contentType("text/plain")
                                                               .done("uncompressed body"))
                                      .handle(Request.get("/")
                                                     .header("Accept-Encoding", "gzip"));

        assertNoExecutionError(response);
        assertThat(response).hasNoHeader("Content-Encoding")
                            .hasBodyText("uncompressed body");
    }

    @Test public void
    processesCompressibleMimeTypes() throws Exception {
        compressor.compressibleTypes("application/json", "text/*");

        Response response = compressor.then(request -> Response.ok()
                                                               .contentType("text/plain")
                                                               .done("uncompressed body"))
                                      .handle(Request.get("/")
                                                     .header("Accept-Encoding", "gzip"));

        assertNoExecutionError(response);
        assertThat("body", unzip(response), equalTo("uncompressed body"));
    }

    @Test public void
    ignoresContentTypeCharsetToDecideIfContentShouldBeCompressed() throws Exception {
        compressor.compressibleTypes("text/html");

        Response response = compressor.then(request -> Response.ok()
                                                               .contentType("text/html; utf-8")
                                                               .done("<html>uncompressed</html>"))
                                      .handle(Request.get("/")
                                                     .header("Accept-Encoding", "gzip"));

        assertNoExecutionError(response);
        assertThat("body", unzip(response), equalTo("<html>uncompressed</html>"));
    }

    private String inflate(Response response) throws IOException {
        return response.empty() ? "" : Streams.toString(new InflaterInputStream(asStream(response), new Inflater(true)));
    }

    private String unzip(Response response) throws IOException {
        return response.empty() ? "" : Streams.toString(new GZIPInputStream(asStream(response)));
    }

    private void assertNoExecutionError(Response response) throws ExecutionException, InterruptedException {
        response.await();
    }
}