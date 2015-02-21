package com.vtence.molecule.servers;

import com.vtence.molecule.Application;
import com.vtence.molecule.BodyPart;
import com.vtence.molecule.FailureReporter;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.Cookie;
import com.vtence.molecule.http.HttpStatus;
import com.vtence.molecule.support.StackTrace;
import com.vtence.molecule.testing.FormData;
import com.vtence.molecule.testing.HttpRequest;
import com.vtence.molecule.testing.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.vtence.molecule.http.HttpStatus.CREATED;
import static com.vtence.molecule.support.ResourceLocator.locateOnClasspath;
import static com.vtence.molecule.testing.FileUpload.binaryFile;
import static com.vtence.molecule.testing.HttpResponseAssert.assertThat;
import static java.lang.String.valueOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class SimpleServerTest {

    SimpleServer server = new SimpleServer("localhost", 9999);
    HttpRequest request = new HttpRequest(server.port());
    HttpResponse response;

    Throwable error;

    @Before public void
    configureServer() {
        server.reportErrorsTo(new FailureReporter() {
            public void errorOccurred(Throwable error) {
                SimpleServerTest.this.error = error;
            }
        });
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
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                throw new RuntimeException("Crash!");
            }
        });
        request.send();
        assertThat("error", error, notNullValue());
        assertThat("error message", error.getMessage(), equalTo("Crash!"));
    }

    @Test public void
    respondsToRequests() throws IOException {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.status(CREATED);
            }
        });

        response = request.send();
        assertNoError();
        assertThat(response).hasStatusCode(201)
                            .hasStatusMessage("Created");
    }

    @Test public void
    chunksResponseWhenContentLengthUnknown() throws IOException {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("<html>...</html>");
            }
        });

        response = request.send();
        assertNoError();
        assertThat(response).hasBodyText("<html>...</html>")
                            .isChunked();
    }

    @Test public void
    doesNotChunkResponsesWithContentLengthHeader() throws IOException {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.contentLength(16);
                response.body("<html>...</html>");
            }
        });

        response = request.send();
        assertNoError();
        assertThat(response).hasBodyText("<html>...</html>")
                            .hasHeader("Content-Length", "16")
                            .isNotChunked();
    }

    @Test public void
    encodesResponsesAccordingToContentType() throws IOException {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.contentType("text/plain; charset=utf-16");
                response.body("This content requires encoding &âçüè!");
                response.status(HttpStatus.OK);
            }
        });

        response = request.send();
        assertNoError();
        assertThat(response).isOK()
                            .hasContentEncodedAs(containsString("UTF-16"));
    }

    @Test public void
    supportsRequestArrayParameters() throws IOException {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body(request.parameters("names").toString());
            }
        });

        response = request.get("/?names=Alice&names=Bob&names=Charles");
        assertNoError();
        assertThat(response).hasBodyText("[Alice, Bob, Charles]");
    }

    @SuppressWarnings("unchecked")
    @Test public void
    providesGeneralRequestInformation() throws IOException {
        final Map<String, String> info = new HashMap<String, String>();
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                info.put("uri", request.uri());
                info.put("path", request.path());
                info.put("ip", request.remoteIp());
                info.put("hostname", request.remoteHost());
                info.put("port", valueOf(request.remotePort()));
                info.put("protocol", request.protocol());
                info.put("secure", valueOf(request.secure()));
                info.put("timestamp", valueOf(request.timestamp()));
            }
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
        final Map<String, Iterable<String>> headers = new HashMap<String, Iterable<String>>();
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                headers.put("names", request.headerNames());
                headers.put("encoding", request.headers("Accept-Encoding"));
            }
        });

        request.header("Accept", "text/html")
               .header("Accept-Encoding", "gzip", "identity; q=0.5", "deflate;q=1.0", "*;q=0")
               .send();
        assertNoError();

        assertThat("header names", headers.get("names"), hasItems("Accept", "Accept-Encoding"));
        assertThat("accept-encoding", headers.get("encoding"),
                contains("gzip", "identity; q=0.5", "deflate;q=1.0", "*;q=0"));
    }

    @SuppressWarnings("unchecked")
    @Test public void
    detailsRequestContent() throws IOException {
        final Map<String, String> content = new HashMap<String, String>();
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                content.put("contentType", valueOf(request.contentType()));
                content.put("contentLength", valueOf(request.contentLength()));
                content.put("body", request.body());
            }
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
    readsAllRequestCookies() throws IOException {
        final Map<String, String> cookies = new HashMap<String, String>();
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                for (Cookie cookie : request.cookies()) {
                    cookies.put(cookie.name(), cookie.value());
                }
            }
        });

        request.cookie("cookie1", "value1")
               .cookie("cookie2", "value2")
               .send();
        assertNoError();

        assertThat("request cookies", cookies, allOf(hasEntry("cookie1", "value1"), hasEntry("cookie2", "value2")));
    }

    @Test public void
    setsResponseCookies() throws IOException {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                Cookie cookie = new Cookie("name", "value").maxAge(1800)
                                                           .domain("localhost")
                                                           .path("/uri")
                                                           .secure(true)
                                                           .httpOnly(true);
                response.cookie(cookie);
            }
        });

        response = request.send();
        assertNoError();
        assertThat(response).hasCookie("name")
                            .hasPath("/uri")
                            .hasDomain("localhost")
                            .hasMaxAge(1800)
                            .isSecure()
                            .isHttpOnly();
    }

    @Test public void
    readsMultiPartFormParameters() throws IOException {
        final Map<String, String> parameters = new HashMap<String, String>();
        server.run(new Application() {
            @Override
            public void handle(Request request, Response response) throws Exception {
                List<BodyPart> parts = request.parts();
                for (BodyPart part : parts) {
                    parameters.put(part.name(), part.content());
                }
            }
        });

        response = request.body(new FormData().add("param1", "value1")
                                              .add("param2", "value2")).post("/");

        assertNoError();
        assertThat("form data parameters", parameters, allOf(hasEntry("param1", "value1"),
                                                             hasEntry("param2", "value2")));
    }

    @Test public void
    downloadsUploadedFiles() throws IOException {
        final Map<String, Integer> files = new HashMap<String, Integer>();
        server.run(new Application() {
            @Override
            public void handle(Request request, Response response) throws Exception {
                List<BodyPart> parts = request.parts();
                for (BodyPart part : parts) {
                    files.put(part.filename(), part.contentBytes().length);
                }
            }
        });

        response = request.body(binaryFile(locateOnClasspath("assets/images/minion.png"))).post("/");

        assertNoError();
        assertThat("filenames", files, hasEntry("minion.png", 21134));
    }

    private void assertNoError() {
        if (error != null) fail(StackTrace.of(error));
    }
}