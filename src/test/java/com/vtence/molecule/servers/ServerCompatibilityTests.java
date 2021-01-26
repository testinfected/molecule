package com.vtence.molecule.servers;

import com.vtence.molecule.BodyPart;
import com.vtence.molecule.Response;
import com.vtence.molecule.Server;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.StackTrace;
import com.vtence.molecule.testing.ResourceLocator;
import com.vtence.molecule.testing.http.Form;
import com.vtence.molecule.testing.http.Trust;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.vtence.molecule.http.HttpStatus.CREATED;
import static com.vtence.molecule.ssl.KeyStoreType.DEFAULT;
import static com.vtence.molecule.ssl.SecureProtocol.TLS;
import static com.vtence.molecule.testing.ResourceLocator.locateOnClasspath;
import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static com.vtence.molecule.testing.http.HttpResponseThat.contentEncodedWithCharset;
import static java.lang.String.valueOf;
import static java.net.http.HttpRequest.BodyPublishers.ofString;
import static java.net.http.HttpResponse.BodyHandlers.discarding;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static java.util.concurrent.CompletableFuture.runAsync;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public abstract class ServerCompatibilityTests {

    int port = 9999;

    WebServer server;
    ResourceLocator resources = ResourceLocator.onClasspath();
    HttpClient client = HttpClient.newBuilder().sslContext(setupSSL()).build();
    HttpRequest.Builder request = HttpRequest.newBuilder();

    Throwable error;

    @Before public void
    configureServer() {
        server = new WebServer(createServer("localhost", port));
        server.failureReporter(error -> ServerCompatibilityTests.this.error = error);
    }

    protected abstract Server createServer(String host, int port);

    @After public void
    stopServer() throws Exception {
        server.stop();
    }

    @Test public void
    knowsItsHost() {
        assertThat("host", server.uri().getHost(), equalTo("localhost"));
    }

    @Test public void
    knowsItsPort() {
        assertThat("port", server.uri().getPort(), equalTo(9999));
    }

    @Test public void
    notifiesReportersOfFailures() throws Exception {
        server.start(request -> {
            throw new Exception("Crash!");
        });

        client.send(request.uri(server.uri()).build(), discarding());
        assertThat("error", error, notNullValue());
        assertThat("error message", error.getMessage(), equalTo("Crash!"));
    }

    @Test public void
    notifiesReportersOfErrorsOccurringAsync() throws Exception {
        server.start(request -> Response.ok().done(new Exception("Crash!")));

        client.send(request.uri(server.uri()).build(), discarding());
        assertThat("error", error, notNullValue());
        assertThat("error message", error.getMessage(), equalTo("Crash!"));
    }

    @Test public void
    respondsToRequests() throws Exception {
        server.start(request -> Response.of(CREATED).done());

        var response = client.send(request.uri(server.uri()).build(), discarding());
        assertNoError();
        assertThat(response).hasStatusCode(201);
    }

    @Test public void
    respondsToRequestsAsynchronously() throws Exception {
        server.start(request -> {
            Response response = Response.of(CREATED);
            runAsync(response::done);
            return response;
        });

        var response = client.send(request.uri(server.uri()).build(), discarding());
        assertNoError();
        assertThat(response).hasStatusCode(201);
    }

    @Test public void
    doesNotChunkResponsesWithContentLengthHeader() throws Exception {
        server.start(request -> Response.ok()
                                       .contentLength(16)
                                       .body("<html>...</html>")
                                       .done());

        var response = client.send(request.uri(server.uri()).build(), ofString());
        assertNoError();
        assertThat(response).hasBody("<html>...</html>")
                            .hasHeader("Content-Length", "16")
                            .isNotChunked();
    }

    @Test public void
    encodesResponsesAccordingToContentType() throws Exception {
        server.start(request -> Response.ok()
                                      .contentType("text/plain; charset=utf-16")
                                      .done("This content requires encoding &âçüè!"));

        var response = client.send(request.uri(server.uri()).build(), ofString());
        assertNoError();
        assertThat(response).isOK()
                            .has(contentEncodedWithCharset(hasToString(startsWith("UTF-16"))));
    }

    @Test public void
    providesGeneralRequestInformation() throws Exception {
        final Map<String, String> info = new HashMap<>();
        server.start(request -> {
            info.put("url", request.url().toString());
            info.put("uri", request.uri().uri());
            info.put("path", request.path());
            info.put("query", request.query());
            info.put("scheme", request.scheme());
            info.put("host", request.hostname());
            info.put("port", valueOf(request.port()));
            info.put("remote ip", request.remoteIp());
            info.put("remote host", request.remoteHost());
            info.put("remote port", valueOf(request.remotePort()));
            info.put("protocol", request.protocol());
            info.put("secure", valueOf(request.secure()));
            info.put("timestamp", valueOf(request.timestamp()));
            return Response.ok().done();
        });

        client.send(request.uri(server.uri().resolve("/over/there?name=ferret")).build(), discarding());
        assertNoError();

        assertThat("request information", info, allOf(
                hasEntry("url", "http://localhost:9999/over/there?name=ferret"),
                hasEntry("uri", "/over/there?name=ferret"),
                hasEntry("path", "/over/there"),
                hasEntry("query", "name=ferret"),
                hasEntry("scheme", "http"),
                hasEntry("host", "localhost"),
                hasEntry("port", "9999"),
                hasEntry("remote ip", "127.0.0.1"),
                hasEntry("remote host", "localhost"),
                hasEntry(equalTo("remote port"), not(equalTo("0"))),
                hasEntry(equalTo("timestamp"), not(equalTo("0"))),
                hasEntry("protocol", "HTTP/1.1"),
                hasEntry("secure", "false")));
    }

    @Test public void
    readsRequestHeaders() throws Exception {
        var headers = new HashMap<String, Iterable<String>>();
        server.start(request -> {
            headers.put("names", request.headerNames());
            headers.put("encoding", request.headers("Accept-Encoding"));
            return Response.ok().done();
        });

        client.send(request.uri(server.uri())
                           .headers("Accept", "text/html",
                                    "Accept-Encoding", "gzip",
                                    "Accept-Encoding", "identity; q=0.5",
                                    "Accept-Encoding", "deflate;q=1.0",
                                    "Accept-Encoding", "*;q=0")
                           .build(),
                    discarding());
        assertNoError();

        assertThat("header names", headers.get("names"), hasItems("Accept", "Accept-Encoding"));
        assertThat("accept-encoding", headers.get("encoding"),
                   contains("gzip", "identity; q=0.5", "deflate;q=1.0", "*;q=0"));
    }

    @Test public void
    writesHeadersWithMultipleValues() throws Exception {
        server.start(request -> Response.ok()
                                       .addHeader("Cache-Control", "no-cache")
                                       .addHeader("Cache-Control", "no-store")
                                       .done());

        var response = client.send(request.uri(server.uri()).build(), discarding());

        assertNoError();
        assertThat(response).hasHeaders("Cache-Control", hasItems("no-cache", "no-store"));
    }

    @Test public void
    readsRequestContent() throws Exception {
        final Map<String, String> content = new HashMap<>();
        server.start(request -> {
            content.put("contentType", valueOf(request.contentType()));
            content.put("contentLength", valueOf(request.contentLength()));
            content.put("body", request.body());
            return Response.ok().done();
        });

        client.send(request.uri(server.uri().resolve("/uri"))
                           .header("Content-Type", "application/json")
                           .POST(ofString("{\"name\": \"value\"}"))
                           .build(),
               discarding());
        assertNoError();

        assertThat("request content", content, allOf(
                hasEntry("contentType", "application/json"),
                hasEntry("contentLength", "17"),
                hasEntry("body", "{\"name\": \"value\"}")));
    }

    @Test public void
    readsQueryParameters() throws Exception {
        var parameters = new HashMap<String, String>();
        server.start(request -> {
            for (String name : request.parameterNames()) {
                parameters.put(name, request.parameter(name));
            }
            return Response.ok().done();
        });

        client.send(request.uri(server.uri().resolve("/?param1=value1&param2=value2")).build(), discarding());

        assertNoError();
        assertThat("query parameters", parameters, allOf(
                hasEntry("param1", "value1"),
                hasEntry("param2", "value2")));
    }

    @Test public void
    supportsMultipleQueryParametersWithSameName() throws Exception {
        server.start(request -> Response.ok().done(request.parameters("names").toString()));

        var response = client.send(request.uri(server.uri().resolve("/?names=Alice&names=Bob&names=Charles"))
                                          .build(),
                                   ofString());

        assertNoError();
        assertThat(response).hasBody("[Alice, Bob, Charles]");
    }

    @Test public void
    readsFormEncodedParameters() throws Exception {
        var parameters = new HashMap<String, String>();
        server.start(request -> {
            for (String name : request.parameterNames()) {
                parameters.put(name, request.parameter(name));
            }
            return Response.ok().done();
        });

        client.send(request.uri(server.uri())
                           .header("Content-Type", Form.urlEncoded().contentType())
                           .POST(Form.urlEncoded()
                                     .addField("param1", "value1")
                                     .addField("param2", "value2"))
                           .build(),
                    discarding());

        assertNoError();
        assertThat("form parameters", parameters, allOf(
                hasEntry("param1", "value1"),
                hasEntry("param2", "value2")));
    }

    @Test public void
    supportsMultipleFormEncodedParametersWithSameName() throws Exception {
        server.start(request -> Response.ok()
                                       .done(request.parameters("name").toString()));

        var response = client.send(request.uri(server.uri())
                                          .header("Content-Type", Form.urlEncoded().contentType())
                                          .POST(Form.urlEncoded()
                                                    .addField("name", "Alice")
                                                    .addField("name", "Bob")
                                                    .addField("name", "Charles"))
                                          .build(),
                                   ofString());

        assertNoError();
        assertThat(response).hasBody("[Alice, Bob, Charles]");
    }

    @Test public void
    readsMultiPartFormParameters() throws Exception {
        var parameters = new HashMap<String, String>();
        server.start(request -> {
            List<BodyPart> parts = request.parts();
            for (BodyPart part : parts) {
                parameters.put(part.name(), part.value());
            }
            return Response.ok().done();
        });

        var form = Form.multipart()
                       .addField("param1", "value1")
                       .addField("param2", "value2");
        client.send(request.uri(server.uri())
                           .header("Content-Type", form.contentType())
                           .POST(form)
                           .build(),
                    discarding());

        assertNoError();
        assertThat("form data parameters", parameters, allOf(
                hasEntry("param1", "value1"),
                hasEntry("param2", "value2")));
    }

    @Test public void
    downloadsUploadedFiles() throws Exception {
        var files = new HashMap<String, Integer>();
        var mimeTypes = new HashMap<String, String>();
        server.start(request -> {
            List<BodyPart> parts = request.parts();
            for (BodyPart part : parts) {
                files.put(part.filename(), part.content().length);
                mimeTypes.put(part.filename(), part.contentType());
            }
            return Response.ok().done();
        });


        var form = Form.multipart()
                       .addBinaryFile("file", resources.locate("assets/images/minion.png"));
        client.send(request.uri(server.uri())
                           .header("Content-Type", form.contentType())
                           .POST(form)
                           .build(), discarding());

        assertNoError();
        assertThat("filenames", files, hasEntry("minion.png", 21134));
        assertThat("mime types", mimeTypes, hasEntry("minion.png", "image/png"));
    }

    @Test public void
    supportsHttps() throws Exception {
        var info = new HashMap<String, String>();
        server.enableSSL(setupSSL())
              .start(request -> {
            info.put("scheme", request.scheme());
            info.put("secure", valueOf(request.secure()));
            return Response.ok().done();
        });

        client.send(request.uri(server.uri()).build(), discarding());
        assertNoError();

        assertThat("request information", info, allOf(
                hasEntry("scheme", "https"),
                hasEntry("secure", "true")));
    }

    private SSLContext setupSSL() {
        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
        try {
            return TLS.initialize(
                    DEFAULT.loadKeys(locateOnClasspath("ssl/keystore"), "password", "password"),
                    new TrustManager[] {Trust.allCertificates()});
        } catch (Exception e) {
            throw new AssertionError("Unable to setup SSL", e);
        }
    }

    protected void assertNoError() {
        if (error != null) fail(StackTrace.of(error));
    }
}