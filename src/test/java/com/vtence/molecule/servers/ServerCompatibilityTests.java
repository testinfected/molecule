package com.vtence.molecule.servers;

import com.vtence.molecule.Application;
import com.vtence.molecule.BodyPart;
import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import com.vtence.molecule.support.StackTrace;
import com.vtence.molecule.testing.ResourceLocator;
import com.vtence.molecule.testing.http.Form;
import com.vtence.molecule.testing.http.HttpRequest;
import com.vtence.molecule.testing.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.vtence.molecule.http.HttpStatus.CREATED;
import static com.vtence.molecule.ssl.KeyStoreType.DEFAULT;
import static com.vtence.molecule.ssl.SecureProtocol.TLS;
import static com.vtence.molecule.testing.ResourceLocator.locateOnClasspath;
import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.lang.String.valueOf;
import static java.util.concurrent.CompletableFuture.runAsync;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;

public abstract class ServerCompatibilityTests {

    int port = 9999;

    Server server;
    ResourceLocator resources = ResourceLocator.onClasspath();
    HttpRequest request = new HttpRequest(port);
    HttpResponse response;

    Throwable error;

    @Before public void
    configureServer() {
        server = createServer("localhost", port);
        server.reportErrorsTo(error -> ServerCompatibilityTests.this.error = error);
    }

    protected abstract Server createServer(String host, int port);

    @After public void
    stopServer() throws Exception {
        server.shutdown();
    }

    @Test public void
    knowsItsHost() throws IOException {
        assertThat("host", server.host(), equalTo("localhost"));
    }

    @Test public void
    knowsItsPort() throws IOException {
        assertThat("port", server.port(), equalTo(9999));
    }

    @Test public void
    notifiesReportersOfFailures() throws IOException {
        server.run(request -> {
            throw new Exception("Crash!");
        });

        request.send();
        assertThat("error", error, notNullValue());
        assertThat("error message", error.getMessage(), equalTo("Crash!"));
    }

    @Test public void
    notifiesReportersOfErrorsOccurringAsync() throws IOException {
        server.run(request -> Response.ok().done(new Exception("Crash!")));

        request.send();
        assertThat("error", error, notNullValue());
        assertThat("error message", error.getMessage(), equalTo("Crash!"));
    }

    @Test public void
    respondsToRequests() throws IOException {
        server.run(request -> Response.of(CREATED).done());

        response = request.send();
        assertNoError();
        assertThat(response).hasStatusCode(201)
                            .hasStatusMessage("Created");
    }

    @Test public void
    respondsToRequestsAsynchronously() throws IOException {
        server.run(request -> {
            Response response = Response.of(CREATED);
            runAsync(response::done);
            return response;
        });

        response = request.send();
        assertNoError();
        assertThat(response).hasStatusCode(201)
                            .hasStatusMessage("Created");
    }

    @Test public void
    doesNotChunkResponsesWithContentLengthHeader() throws IOException {
        server.run(request -> Response.ok()
                                       .contentLength(16)
                                       .body("<html>...</html>")
                                       .done());

        response = request.send();
        assertNoError();
        assertThat(response).hasBodyText("<html>...</html>")
                            .hasHeader("Content-Length", "16")
                            .isNotChunked();
    }

    @Test public void
    encodesResponsesAccordingToContentType() throws IOException {
        server.run(request -> Response.ok()
                                      .contentType("text/plain; charset=utf-16")
                                      .done("This content requires encoding &âçüè!"));

        response = request.send();
        assertNoError();
        assertThat(response).isOK()
                            .hasContentEncodedAs(containsString("UTF-16"));
    }

    @SuppressWarnings("unchecked")
    @Test public void
    providesGeneralRequestInformation() throws IOException {
        final Map<String, String> info = new HashMap<>();
        server.run(request -> {
            info.put("uri", request.uri());
            info.put("path", request.path());
            info.put("query", request.query());
            info.put("scheme", request.scheme());
            info.put("server-host", request.serverHost());
            info.put("server-port", valueOf(request.serverPort()));
            info.put("remote-ip", request.remoteIp());
            info.put("remote-host", request.remoteHost());
            info.put("remote-port", valueOf(request.remotePort()));
            info.put("protocol", request.protocol());
            info.put("secure", valueOf(request.secure()));
            info.put("timestamp", valueOf(request.timestamp()));
            return Response.ok().done();
        });

        request.get("/over/there?name=ferret");
        assertNoError();


        assertThat("request information", info, allOf(
                hasEntry("uri", "/over/there?name=ferret"),
                hasEntry("path", "/over/there"),
                hasEntry("query", "name=ferret"),
                hasEntry("scheme", "http"),
                hasEntry("server-host", "localhost"),
                hasEntry("server-port", "9999"),
                hasEntry("remote-ip", "127.0.0.1"),
                hasEntry("remote-host", "localhost"),
                hasEntry(equalTo("remote-port"), not(equalTo("0"))),
                hasEntry(equalTo("timestamp"), not(equalTo("0"))),
                hasEntry("protocol", "HTTP/1.1"),
                hasEntry("secure", "false")));
    }

    @SuppressWarnings("unchecked")
    @Test public void
    readsRequestHeaders() throws IOException {
        final Map<String, Iterable<String>> headers = new HashMap<>();
        server.run(request -> {
            headers.put("names", request.headerNames());
            headers.put("encoding", request.headers("Accept-Encoding"));
            return Response.ok().done();
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
        server.run(request -> Response.ok()
                                       .addHeader("Cache-Control", "no-cache")
                                       .addHeader("Cache-Control", "no-store")
                                       .done());

        response = request.send();

        assertNoError();
        assertThat("response headers", response.headers("Cache-Control"), hasItems("no-cache", "no-store"));
    }

    @SuppressWarnings("unchecked")
    @Test public void
    readsRequestContent() throws IOException {
        final Map<String, String> content = new HashMap<>();
        server.run(request -> {
            content.put("contentType", valueOf(request.contentType()));
            content.put("contentLength", valueOf(request.contentLength()));
            content.put("body", request.body());
            return Response.ok().done();
        });

        request.contentType("application/json")
               .body("{\"name\": \"value\"}")
               .post("/uri");
        assertNoError();

        assertThat("request content", content, allOf(
                hasEntry("contentType", "application/json"),
                hasEntry("contentLength", "17"),
                hasEntry("body", "{\"name\": \"value\"}")));
    }

    @Test public void
    readsQueryParameters() throws IOException {
        final Map<String, String> parameters = new HashMap<>();
        server.run(request -> {
            for (String name : request.parameterNames()) {
                parameters.put(name, request.parameter(name));
            }
            return Response.ok().done();
        });

        request.get("/?param1=value1&param2=value2");

        assertNoError();
        assertThat("query parameters", parameters, allOf(
                hasEntry("param1", "value1"),
                hasEntry("param2", "value2")));
    }

    @Test public void
    supportsMultipleQueryParametersWithSameName() throws IOException {
        server.run(request -> Response.ok().done(request.parameters("names").toString()));

        response = request.get("/?names=Alice&names=Bob&names=Charles");

        assertNoError();
        assertThat(response).hasBodyText("[Alice, Bob, Charles]");
    }

    @SuppressWarnings("unchecked")
    @Test public void
    readsFormEncodedParameters() throws IOException {
        final Map<String, String> parameters = new HashMap<>();
        Application application = request -> {
            for (String name : request.parameterNames()) {
                parameters.put(name, request.parameter(name));
            }
            return Response.ok().done();
        };
        server.run(application);

        response = request.content(Form.urlEncoded()
                                       .addField("param1", "value1")
                                       .addField("param2", "value2"))
                          .post("/");

        assertNoError();
        assertThat("form parameters", parameters, allOf(
                hasEntry("param1", "value1"),
                hasEntry("param2", "value2")));
    }

    @Test public void
    supportsMultipleFormEncodedParametersWithSameName() throws IOException {
        server.run(request -> Response.ok()
                                       .done(request.parameters("name").toString()));

        response = request.content(Form.urlEncoded()
                                       .addField("name", "Alice")
                                       .addField("name", "Bob")
                                       .addField("name", "Charles"))
                          .post("/");

        assertNoError();
        assertThat(response).hasBodyText("[Alice, Bob, Charles]");
    }

    @Test public void
    readsMultiPartFormParameters() throws IOException {
        final Map<String, String> parameters = new HashMap<>();
        server.run(request -> {
            List<BodyPart> parts = request.parts();
            for (BodyPart part : parts) {
                parameters.put(part.name(), part.value());
            }
            return Response.ok().done();
        });

        response = request.content(Form.multipart()
                                       .addField("param1", "value1")
                                       .addField("param2", "value2"))
                          .post("/");

        assertNoError();
        assertThat("form data parameters", parameters, allOf(
                hasEntry("param1", "value1"),
                hasEntry("param2", "value2")));
    }

    @Test public void
    downloadsUploadedFiles() throws IOException {
        final Map<String, Integer> files = new HashMap<>();
        final Map<String, String> mimeTypes = new HashMap<>();
        server.run(request -> {
            List<BodyPart> parts = request.parts();
            for (BodyPart part : parts) {
                files.put(part.filename(), part.content().length);
                mimeTypes.put(part.filename(), part.contentType());
            }
            return Response.ok().done();
        });


        response = request.content(Form.multipart()
                                       .addBinaryFile("file", resources.locate("assets/images/minion.png")))
                          .post("/");

        assertNoError();
        assertThat("filenames", files, hasEntry("minion.png", 21134));
        assertThat("mime types", mimeTypes, hasEntry("minion.png", "image/png"));
    }

    @Test public void
    supportsHttps() throws Exception {
        SSLContext sslContext =
                TLS.initialize(DEFAULT.loadKeys(locateOnClasspath("ssl/keystore"), "password", "password"));

        final Map<String, String> info = new HashMap<>();
        server.run(request -> {
            info.put("scheme", request.scheme());
            info.put("secure", valueOf(request.secure()));
            return Response.ok().done();
        }, sslContext);

        response = request.secure(true).get("/");
        assertNoError();

        assertThat("request information", info, allOf(
                hasEntry("scheme", "https"),
                hasEntry("secure", "true")));
    }

    protected void assertNoError() {
        if (error != null) fail(StackTrace.of(error));
    }
}