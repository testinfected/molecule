package com.vtence.molecule.simple;

import com.vtence.molecule.Application;
import com.vtence.molecule.HttpMethod;
import com.vtence.molecule.HttpStatus;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.support.HttpRequest;
import com.vtence.molecule.support.HttpResponse;
import com.vtence.molecule.util.Charsets;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.vtence.molecule.support.HttpRequest.aRequest;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class SimpleServerTest {

    SimpleServer server = new SimpleServer(9999);
    HttpRequest request = aRequest().to(server);
    HttpResponse response;

    @After public void
    stopServer() throws Exception {
        server.shutdown();
    }

    @Test public void
    respondsToRequests() throws IOException {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.body("<html>...</html>");
                response.status(HttpStatus.OK);
            }
        });

        response = request.send();
        response.assertHasContent("<html>...</html>");
        response.assertHasStatusCode(200);
    }

    @Test public void
    acceptsADefaultCharsetForEncoding() throws IOException {
        server.defaultCharset(Charsets.UTF_16);
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                response.contentType("text/plain; charset=" + response.charset().displayName());
                response.body("This content requires encoding &âçüè!");
                response.status(HttpStatus.OK);
            }
        });

        response = request.send();
        response.assertContentIsEncodedAs("UTF-16");
        response.assertHasContentType(Matchers.containsString("UTF-16"));
    }

    @Test public void
    providesHttpSessions() throws IOException {
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                if (request.method() == HttpMethod.POST)
                    request.session().put("username", request.parameter("username"));
                else
                    response.body("Hello, " + request.session().get("username"));
            }
        });

        request.withParameter("username", "Vincent").post("/login");

        response = request.but().removeParameters().get("/");
        response.assertHasContent("Hello, Vincent");
    }

    @Test public void
    providesInformationAboutRequest() throws IOException {
        final Map<String, String> info = new HashMap<String, String>();
        server.run(new Application() {
            public void handle(Request request, Response response) throws Exception {
                info.put("ip", request.ip());
                info.put("uri", request.uri());
                info.put("pathInfo", request.pathInfo());
                info.put("protocol", request.protocol());
            }
        });

        request.get("/uri");

        assertThat("request information", info, allOf(
                hasEntry("ip", "127.0.0.1"),
                hasEntry("pathInfo", "/uri"),
                hasEntry("uri", "/uri"),
                hasEntry("protocol", "HTTP/1.1")));
    }

    @SuppressWarnings("unchecked")
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

        request.get("/");

        assertThat("attributes", attributes, allOf(
                containsEntry("name", "Velociraptor"),
                not(containsKey("family")),
                containsEntry("clade", "Dinosauria")));
    }

    private Matcher<Map<?, ?>> containsKey(Object key) {
        return Matchers.hasKey(equalTo(key));
    }

    private Matcher<Map<? extends Object, ? extends Object>> containsEntry(Object key, Object value) {
        return Matchers.hasEntry(equalTo(key), equalTo(value));
    }
}
