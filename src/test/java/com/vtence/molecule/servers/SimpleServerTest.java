package com.vtence.molecule.servers;

import com.vtence.molecule.BodyPart;
import com.vtence.molecule.http.HttpStatus;
import com.vtence.molecule.support.StackTrace;
import com.vtence.molecule.testing.ResourceLocator;
import com.vtence.molecule.testing.http.Form;
import com.vtence.molecule.testing.http.HttpRequest;
import com.vtence.molecule.testing.http.HttpResponse;
import com.vtence.molecule.testing.http.MultipartForm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.vtence.molecule.http.HttpStatus.CREATED;
import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.lang.String.valueOf;
import static java.util.concurrent.CompletableFuture.runAsync;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;

public class SimpleServerTest {

    SimpleServer server = new SimpleServer("localhost", 9999);
    ResourceLocator resources = ResourceLocator.onClasspath();
    HttpRequest request = new HttpRequest(server.port());
    HttpResponse response;

    Throwable error;

    @Before public void
    configureServer() {
        server.reportErrorsTo(error -> SimpleServerTest.this.error = error);
    }

    @After public void
    stopServer() throws Exception {
        server.shutdown();
    }

    @Test public void
    knowsItsHostName() throws IOException {
        assertThat("hostname", server.host(), equalTo("localhost"));
    }

    @Test public void
    notifiesReportersOfFailures() throws IOException {
        server.run((request, response) -> {
            throw new Exception("Crash!");
        });
        request.send();
        assertThat("error", error, notNullValue());
        assertThat("error message", error.getMessage(), equalTo("Crash!"));
    }

    @Test public void
    notifiesReportersOfErrorsOccurringAsync() throws IOException {
        server.run((request, response) -> response.done(new Exception("Crash!")));
        request.send();
        assertThat("error", error, notNullValue());
        assertThat("error message", error.getMessage(), equalTo("Crash!"));
    }

    @Test public void
    respondsToRequests() throws IOException {
        server.run((request, response) -> response.status(CREATED).done());

        response = request.send();
        assertNoError();
        assertThat(response).hasStatusCode(201)
                            .hasStatusMessage("Created");
    }

    @Test public void
    respondsToRequestsAsynchronously() throws IOException {
        server.run((request, response) -> runAsync(() -> response.status(CREATED).done()));

        response = request.send();
        assertNoError();
        assertThat(response).hasStatusCode(201)
                            .hasStatusMessage("Created");
    }

    @Test public void
    chunksResponseWhenContentLengthUnknown() throws IOException {
        server.run((request, response) -> response.body("<html>...</html>").done());

        response = request.send();
        assertNoError();
        assertThat(response).hasBodyText("<html>...</html>")
                            .isChunked();
    }

    @Test public void
    doesNotChunkResponsesWithContentLengthHeader() throws IOException {
        server.run((request, response) -> {
            response.contentLength(16);
            response.body("<html>...</html>");
            response.done();
        });

        response = request.send();
        assertNoError();
        assertThat(response).hasBodyText("<html>...</html>")
                            .hasHeader("Content-Length", "16")
                            .isNotChunked();
    }

    @Test public void
    encodesResponsesAccordingToContentType() throws IOException {
        server.run((request, response) -> {
            response.contentType("text/plain; charset=utf-16");
            response.body("This content requires encoding &âçüè!");
            response.status(HttpStatus.OK);
            response.done();
        });

        response = request.send();
        assertNoError();
        assertThat(response).isOK()
                            .hasContentEncodedAs(containsString("UTF-16"));
    }

    @Test public void
    supportsRequestArrayParameters() throws IOException {
        server.run((request, response) -> response.body(request.parameters("names").toString()).done());

        response = request.get("/?names=Alice&names=Bob&names=Charles");
        assertNoError();
        assertThat(response).hasBodyText("[Alice, Bob, Charles]");
    }

    @SuppressWarnings("unchecked")
    @Test public void
    providesGeneralRequestInformation() throws IOException {
        final Map<String, String> info = new HashMap<>();
        server.run((request, response) -> {
            info.put("uri", request.uri());
            info.put("path", request.path());
            info.put("ip", request.remoteIp());
            info.put("hostname", request.remoteHost());
            info.put("port", valueOf(request.remotePort()));
            info.put("protocol", request.protocol());
            info.put("secure", valueOf(request.secure()));
            info.put("timestamp", valueOf(request.timestamp()));
            response.done();
        });

        request.get("/path?query");
        assertNoError();

        assertThat("request information", info, allOf(
                hasEntry("uri", "/path?query"),
                hasEntry("path", "/path"),
                hasEntry("ip", "127.0.0.1"),
                hasEntry(equalTo("hostname"), notNullValue()),
                hasEntry(equalTo("port"), not(equalTo("0"))),
                hasEntry(equalTo("timestamp"), not(equalTo("0"))),
                hasEntry("protocol", "HTTP/1.1"),
                hasEntry("secure", "false")));
    }

    @SuppressWarnings("unchecked")
    @Test public void
    readsRequestHeaders() throws IOException {
        final Map<String, Iterable<String>> headers = new HashMap<>();
        server.run((request, response) -> {
            headers.put("names", request.headerNames());
            headers.put("encoding", request.headers("Accept-Encoding"));
            response.done();
        });

        request.header("Accept", "text/html")
               .header("Accept-Encoding", "gzip", "identity; q=0.5", "deflate;q=1.0", "*;q=0")
               .send();
        assertNoError();

        assertThat("header names", headers.get("names"), hasItems("Accept", "Accept-Encoding"));
        assertThat("accept-encoding", headers.get("encoding"),
                contains("gzip", "identity; q=0.5", "deflate;q=1.0", "*;q=0"));
    }

    @Test public void
    writesHeadersWithMultipleValues() throws IOException {
        server.run((request, response) -> {
            response.addHeader("Cache-Control", "no-cache");
            response.addHeader("Cache-Control", "no-store");
            response.done();
        });

        response = request.send();

        assertNoError();
        assertThat("response headers", response.headers("Cache-Control"), hasItems("no-cache", "no-store"));
    }

    @SuppressWarnings("unchecked")
    @Test public void
    detailsRequestContent() throws IOException {
        final Map<String, String> content = new HashMap<>();
        server.run((request, response) -> {
            content.put("contentType", valueOf(request.contentType()));
            content.put("contentLength", valueOf(request.contentLength()));
            content.put("body", request.body());
            response.done();
        });

        request.header("Accept", "text/html")
               .contentType("application/x-www-form-urlencoded")
               .body("name=value")
               .post("/uri");
        assertNoError();

        assertThat("request content", content, allOf(hasEntry("contentType", "application/x-www-form-urlencoded"),
                hasEntry("contentLength", "10"),
                hasEntry("body", "name=value")));
    }

    @Test public void
    readsMultiPartFormParameters() throws IOException {
        final Map<String, String> parameters = new HashMap<>();
        server.run((request, response) -> {
            List<BodyPart> parts = request.parts();
            for (BodyPart part : parts) {
                parameters.put(part.name(), part.value());
            }
            response.done();
        });

        Form form = new MultipartForm().addField("param1", "value1")
                                      .addField("param2", "value2");
        response = request.content(form).post("/");

        assertNoError();
        assertThat("form data parameters", parameters,
                allOf(hasEntry("param1", "value1"), hasEntry("param2", "value2")));
    }

    @Test public void
    downloadsUploadedFiles() throws IOException {
        final Map<String, Integer> files = new HashMap<>();
        final Map<String, String> mimeTypes = new HashMap<>();
        server.run((request, response) -> {
            List<BodyPart> parts = request.parts();
            for (BodyPart part : parts) {
                files.put(part.filename(), part.content().length);
                mimeTypes.put(part.filename(), part.contentType());
            }
            response.done();
        });


        Form form = new MultipartForm().addBinaryFile("file", resources.locate("assets/images/minion.png"));
        response = request.content(form).post("/");

        assertNoError();
        assertThat("filenames", files, hasEntry("minion.png", 21134));
        assertThat("mime types", mimeTypes, hasEntry("minion.png", "image/png"));
    }

    private void assertNoError() {
        if (error != null) fail(StackTrace.of(error));
    }
}