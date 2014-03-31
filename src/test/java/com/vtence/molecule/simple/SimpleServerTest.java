package com.vtence.molecule.simple;

import com.vtence.molecule.Application;
import com.vtence.molecule.Cookie;
import com.vtence.molecule.HttpMethod;
import com.vtence.molecule.HttpStatus;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Session;
import com.vtence.molecule.simple.session.CookieTracker;
import com.vtence.molecule.simple.session.SessionPool;
import com.vtence.molecule.support.HttpRequest;
import com.vtence.molecule.support.HttpResponse;
import com.vtence.molecule.support.StackTrace;
import com.vtence.molecule.util.Clock;
import com.vtence.molecule.util.FailureReporter;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.vtence.molecule.support.HttpRequest.aRequest;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class SimpleServerTest {

    static final String SESSION_COOKIE = "JSESSIONID";
    static final long THIRTY_MINUTES = MINUTES.toSeconds(30);

    SimpleServer server = new SimpleServer(9999);
    HttpRequest request = aRequest().onPort(server.port());
    HttpResponse response;

    Delorean delorean = new Delorean();
    Exception error;

    @Before public void
    configureServer() {
        server.reportErrorsTo(new FailureReporter() {
            public void errorOccurred(Exception error) {
                SimpleServerTest.this.error = error;
            }
        });
        server.enableSessions(new CookieTracker(new SessionPool(delorean, THIRTY_MINUTES)));
    }

    @After public void
    stopServer() throws Exception {
        delorean.back();
        server.shutdown();
    }

    @Test public void
    respondsToRequests() throws IOException {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.status(HttpStatus.OK);
            }
        });

        response = request.send();
        assertNoError();
        response.assertOK();
    }

    @Test public void
    chunksResponseStreamWhenContentLengthUnknown() throws IOException {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                byte[] content = "<html>...</html>".getBytes(response.charset());
                response.outputStream().write(content);
            }
        });

        response = request.send();
        assertNoError();
        response.assertHasContent("<html>...</html>");
        response.assertChunked();
    }

    @Test public void
    doesNoChunkResponsesWithContentLengthHeader() throws IOException {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                byte[] content = "<html>...</html>".getBytes(response.charset());
                response.contentLength(content.length);
                response.outputStream().write(content);
            }
        });

        response = request.send();
        assertNoError();
        response.assertHasContent("<html>...</html>");
        response.assertNotChunked();
    }

    @Test public void
    doesNotChunkBufferedByteStreams() throws IOException {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                byte[] content = "<html>...</html>".getBytes(response.charset());
                response.outputStream(content.length).write(content);
            }
        });

        response = request.send();
        assertNoError();
        response.assertHasContent("<html>...</html>");
        response.assertNotChunked();
    }

    @Test public void
    doesNotChunkResponseBodies() throws IOException {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("<html>...</html>");
            }
        });

        response = request.send();
        assertNoError();
        response.assertHasContent("<html>...</html>");
        response.assertNotChunked();
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
        response.assertOK();
        response.assertContentIsEncodedAs("UTF-16");
    }

    @Test public void
    supportsArrayParameters() throws IOException {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body(Arrays.toString(request.parameters("names")));
            }
        });

        response = request.withParameter("names", "Alice", "Bob", "Charles").send();
        assertNoError();
        response.assertHasContent("[Alice, Bob, Charles]");
    }

    @SuppressWarnings("unchecked")
    @Test public void
    providesGeneralRequestInformation() throws IOException {
        final Map<String, String> info = new HashMap<String, String>();
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                info.put("ip", request.ip());
                info.put("uri", request.uri());
                info.put("pathInfo", request.pathInfo());
                info.put("protocol", request.protocol());
            }
        });

        request.post("/uri");
        assertNoError();

        assertThat("request information", info, allOf(
                hasEntry("ip", "127.0.0.1"),
                hasEntry("pathInfo", "/uri"),
                hasEntry("uri", "/uri"),
                hasEntry("protocol", "HTTP/1.1")));
    }

    @SuppressWarnings("unchecked")
    @Test public void
    supportsRequestHeaders() throws IOException {
        final Map<String, Iterable<String>> headers = new HashMap<String, Iterable<String>>();
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                headers.put("names", request.headerNames());
                headers.put("accept", Arrays.asList(request.header("Accept")));
                headers.put("encoding", request.headers("Accept-Encoding"));
            }
        });

        request.withHeader("Accept", "text/html").
                withHeader("Accept-Encoding", "gzip, identity; q=0.5, deflate;q=1.0, *;q=0").
                send();
        assertNoError();

        assertThat("Header names", headers.get("names"), hasItems("Accept", "Accept-Encoding"));
        assertThat("Accept header", headers.get("accept"), contains("text/html"));
        assertThat("Accept-Encoding header", headers.get("encoding"), contains("gzip", "deflate",
                "identity"));
    }

    @SuppressWarnings("unchecked")
    @Test public void
    detailsRequestContent() throws IOException {
        final Map<String, String> content = new HashMap<String, String>();
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                content.put("contentType", String.valueOf(request.contentType()));
                content.put("contentLength", String.valueOf(request.contentLength()));
                content.put("body", request.body());
            }
        });

        request.withHeader("Accept", "text/html")
                .withEncodingType("application/x-www-form-urlencoded")
                .withBody("name=value")
                .post("/uri");
        assertNoError();

        assertThat("request content", content, allOf(
                hasEntry("contentType", "application/x-www-form-urlencoded"),
                hasEntry("contentLength", "10"),
                hasEntry("body", "name=value")));
    }

    @Test public void
    readsRequestCookies() throws IOException {
        final Map<String, String> cookies = new HashMap<String, String>();
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                for (Cookie cookie : request.cookies()) {
                    cookies.put(cookie.name(), cookie.value());
                }
            }
        });

        request.withCookie("cookie1", "value1")
                .withCookie("cookie2", "value2")
                .send();
        assertNoError();

        assertThat("request cookies", cookies, allOf(
                hasEntry("cookie1", "value1"),
                hasEntry("cookie2", "value2")));
    }

    @Test public void
    setsResponseCookies() throws IOException {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                Cookie cookie = new Cookie("cookie", "value");
                cookie.httpOnly(true);
                response.cookie(cookie);
            }
        });

        response = request.send();
        assertNoError();
        response.assertHasCookie(equalToIgnoringCase("cookie=value; Version=1; Path=/; HttpOnly"));
    }

    @Test public void
    supportsRequestAttributes() throws IOException {
        final Map<Object, Object> attributes = new HashMap<Object, Object>();
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                request.attribute("name", "Velociraptor");
                request.attribute("family", "Dromaeosauridae");
                request.attribute("clade", "Dinosauria");
                request.removeAttribute("family");
                attributes.putAll(request.attributes());
            }
        });

        request.send();
        assertNoError();

        assertThat("attributes", attributes, allOf(
                containsEntry("name", "Velociraptor"),
                not(containsKey("family")),
                containsEntry("clade", "Dinosauria")));
    }

    @Test public void
    onlyCreatesSessionsOnDemand() throws IOException {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("Hello, World");
            }
        });

        response = request.send();
        assertNoError();
        response.assertHasNoCookie(SESSION_COOKIE);
    }

    @Test public void
    maintainsSessionsAcrossRequestUsingCookies() throws IOException {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                if (request.method() == HttpMethod.POST)
                    request.session().put("username", request.parameter("username"));
                else
                    response.body("Hello, " + request.session(false).get("username"));
            }
        });

        response = request.withParameter("username", "Vincent").post("/login");
        assertNoError();
        response.assertHasCookie(SESSION_COOKIE);

        response = request.but().removeParameters().get("/");
        assertNoError();
        response.assertHasContent("Hello, Vincent");
        response.assertHasNoCookie(SESSION_COOKIE);
    }

    @Test public void
    expiresSessionsAfterTimeout() throws Exception {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                if (request.method() == HttpMethod.POST)
                    request.session().put("username", request.parameter("username"));
                else {
                    Session session = request.session(false);
                    String username = session != null ? session.<String>get("username") : "X";
                    response.body("Hello, " + username);
                }
            }
        });

        response = request.withParameter("username", "Vincent").post("/login");
        assertNoError();

        delorean.travel(SECONDS.toMillis(THIRTY_MINUTES));
        response = request.but().removeParameters().get("/");
        assertNoError();

        response.assertHasContent("Hello, X");
    }

    private Matcher<Map<?, ?>> containsKey(Object key) {
        return Matchers.hasKey(equalTo(key));
    }

    private Matcher<Map<?, ?>> containsEntry(Object key, Object value) {
        return Matchers.hasEntry(equalTo(key), equalTo(value));
    }

    private void assertNoError() {
        if (error != null) fail(StackTrace.of(error));
    }

    private static class Delorean implements Clock {

        private long timeTravel = 0;

        public Date now() {
            return new Date(System.currentTimeMillis() + timeTravel);
        }

        public void travel(long offsetInMillis) {
            this.timeTravel = offsetInMillis;
        }

        public void back() {
            travel(0);
        }
    }
}
