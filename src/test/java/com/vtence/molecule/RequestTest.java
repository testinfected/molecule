package com.vtence.molecule;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.vtence.molecule.http.HeaderNames.CONTENT_TYPE;
import static com.vtence.molecule.http.HeaderNames.HOST;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class RequestTest {

    @Test
    public void hasSensibleDefaults() throws IOException {
        assertThat("default timestamp", Request.get("/").timestamp(), is(-1L));
        assertThat("default remote ip", Request.get("/").remoteIp(), nullValue());
        assertThat("default remote host", Request.get("/").remoteHost(), nullValue());
        assertThat("default remote port", Request.get("/").remotePort(), is(-1));
        assertThat("default protocol", Request.get("/").protocol(), is("HTTP/1.1"));
        assertThat("relative uri secure", Request.get("/").secure(), is(false));
        assertThat("http secure", Request.get("http://localhost").secure(), is(false));
        assertThat("https secure", Request.get("https://localhost").secure(), is(true));
        assertThat("default body", Request.get("https://localhost").body(), is(""));
    }

    @Test
    public void maintainsAnOrderedListOfParametersWithSameName() {
        Request request = Request.get("/")
                                 .addParameter("letters", "a")
                                 .addParameter("letters", "b")
                                 .addParameter("letters", "c");

        assertThat("has letters?", request.hasParameter("letters"), equalTo(true));
        assertThat("letters", request.parameters("letters"), contains("a", "b", "c"));
    }

    @Test
    public void usesLastSetParameterWhenMultipleParametersWithSameNameExist() {
        Request request = Request.get("/")
                                 .addParameter("letter", "a")
                                 .addParameter("letter", "b")
                                 .addParameter("letter", "c");

        assertThat("authoritative letter", request.parameter("letter"), equalTo("c"));
    }

    @Test
    public void maintainsAListOfParameterNames() {
        Request request = Request.get("/")
                                 .addParameter("letters", "a, b, c, etc.")
                                 .addParameter("digits", "1, 2, 3, etc.")
                                 .addParameter("symbols", "#, $, %, etc.");

        assertThat("parameter names", request.parameterNames(), contains("letters", "digits", "symbols"));
    }

    @Test
    public void removingAParameterRemovesAllParametersWithSameName() {
        Request request = Request.get("/")
                                 .addParameter("letters", "a")
                                 .addParameter("letters", "b")
                                 .addParameter("letters", "c")
                                 .addParameter("digits", "1, 2, 3, etc.");

        request.removeParameter("letters");
        assertThat("has letters?", request.hasParameter("letters"), equalTo(false));
        assertThat("parameter names", request.parameterNames(), contains("digits"));
    }

    @Test
    public void containsABody() throws IOException {
        Request request = Request.get("/")
                                 .body("body");
        assertThat("input", request.body(), equalTo("body"));
    }

    @Test
    public void maintainsAnOrderedListOfHeaderNames() throws IOException {
        Request request = Request.get("/")
                                 .addHeader("Accept", "text/html")
                                 .addHeader("Accept", "application/json")
                                 .header("Accept-Encoding", "gzip")
                                 .header("Accept-Language", "en");

        assertThat("header names", request.headerNames(), contains("Accept", "Accept-Encoding", "Accept-Language"));
    }

    @Test
    public void retrievesHeadersByName() throws IOException {
        Request request = Request.get("/")
                                 .header("Accept", "text/html; q=0.9, application/json");
        assertThat("header", request.header("Accept"), equalTo("text/html; q=0.9, application/json"));
    }

    @Test
    public void retrievesListOfHeadersWithSameName() throws IOException {
        Request request = Request.get("/")
                                 .addHeader("Accept-Language", "en").
                                         addHeader("Accept-Language", "fr");
        assertThat("header", request.headers("Accept-Language"), contains("en", "fr"));
    }

    @Test
    public void joinsHeadersWithSameName() throws IOException {
        Request request = Request.get("/")
                                 .addHeader("Accept", "text/html; q=0.9").
                                         addHeader("Accept", "application/json");
        assertThat("header", request.header("Accept"), equalTo("text/html; q=0.9, application/json"));
    }

    @Test
    public void removesHeaders() throws IOException {
        Request request = Request.get("/")
                                 .addHeader("Accept", "text/html")
                                 .addHeader("Accept-Encoding", "gzip");

        assertThat("header?", request.hasHeader("Accept"), equalTo(true));
        request.removeHeader("Accept");
        assertThat("still there?", request.hasHeader("Accept"), equalTo(false));

        assertThat("header names", request.headerNames(), contains("Accept-Encoding"));
    }

    @Test
    public void maintainsAMapOfAttributes() throws IOException {
        Request request = Request.get("/")
                                 .attribute("name", "Velociraptor")
                                 .attribute("family", "Dromaeosauridae")
                                 .attribute("clade", "Dinosauria");

        assertThat("attributes", request.attributes(), allOf(containsEntry("name", "Velociraptor"),
                containsEntry("family", "Dromaeosauridae"),
                containsEntry("clade", "Dinosauria")));
        assertThat("attribute names", request.attributeKeys(), containsKeys("name", "family", "clade"));
    }

    @Test
    public void removesAttributeOnDemand() throws IOException {
        Request request = Request.get("/")
                                 .attribute("name", "Velociraptor")
                                 .attribute("family", "Dromaeosauridae")
                                 .attribute("clade", "Dinosauria")
                                 .removeAttribute("family");

        assertThat("attribute names", request.attributeKeys(), containsKeys("name", "clade"));
    }

    @Test
    public void usesISO8859AsDefaultCharset() {
        assertThat("default charset", Request.get("/").charset(), equalTo(StandardCharsets.ISO_8859_1));
    }

    @Test
    public void readsCharsetFromContentType() {
        Request request = Request.get("/")
                                 .header(CONTENT_TYPE, "text/html; charset=utf-8");
        assertThat("charset", request.charset(), equalTo(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void maintainsAnOrderedListOfBodyParts() {
        Request request = Request.get("/")
                                 .addPart(new BodyPart().name("a"))
                                 .addPart(new BodyPart().name("b"))
                                 .addPart(new BodyPart().name("c"))
                                 .addPart(new BodyPart().name("a"));

        assertThat("body parts", request.parts(),
                contains(partWithName("a"), partWithName("b"), partWithName("c"), partWithName("a")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void removesBodyPartsByName() {
        Request request = Request.get("/")
                                 .addPart(new BodyPart().name("a"))
                                 .addPart(new BodyPart().name("b"))
                                 .addPart(new BodyPart().name("c"))
                                 .addPart(new BodyPart().name("b"))
                                 .removePart("b");

        assertThat("body parts", request.parts(), contains(partWithName("a"), partWithName("c")));
    }

    @Test
    public void readsHostnameFromHostHeader() {
        Request request = Request.get("http://127.0.0.1")
                                 .header(HOST, "www.example.com");

        assertThat("hostname", request.hostname(), equalTo("www.example.com"));
    }

    @Test
    public void fallbacksToServerHostIfHostHeaderMissing() {
        Request request = Request.get("http://www.example.com");

        assertThat("hostname", request.hostname(), equalTo("www.example.com"));
    }

    @Test
    public void readsPortFromHostHeader() {
        Request request = Request.get("http://localhost:5432/");
        request.header(HOST, "www.example.com:8080");

        assertThat("port", request.port(), equalTo(8080));
    }

    @Test
    public void usesServerPortAsFallback() {
        Request request = Request.get("http://localhost:5432/");

        assertThat("fallback port", request.port(), equalTo(5432));
    }

    @Test
    public void knowsHttpSchemeDefaultPort() {
        Request request = Request.get("http://localhost:8080")
                                 .header(HOST, "www.example.com");

        assertThat("http port", request.port(), equalTo(80));
    }

    @Test
    public void knowsHttpsSchemeDefaultPort() {
        Request request = Request.get("https://localhost:8443")
                                 .header(HOST, "www.example.com");

        assertThat("https port", request.port(), equalTo(443));
    }

    @Test
    public void reconstructsOriginalUrl() {
        Request request = Request.get("http://localhost:8080/over/there?name=ferret");

        request.header(HOST, "www.example.com");
        assertThat("without port", request.url().toString(),
                   equalTo("http://www.example.com/over/there?name=ferret"));

        request.header(HOST, "www.example.com:80");
        assertThat("using default port", request.url().toString(),
                   equalTo("http://www.example.com/over/there?name=ferret"));

        request.header(HOST, "www.example.com:3000");
        assertThat("using custom port", request.url().toString(),
                   equalTo("http://www.example.com:3000/over/there?name=ferret"));

    }

    @Test
    public void reconstructsOriginalSecureUrl() {
        Request request = Request.get("https://localhost:8443/over/there?name=ferret");

        request.header(HOST, "www.example.com");
        assertThat("without port", request.url().toString(),
                   equalTo("https://www.example.com/over/there?name=ferret"));

        request.header(HOST, "www.example.com:443");
        assertThat("using default port", request.url().toString(),
                   equalTo("https://www.example.com/over/there?name=ferret"));

        request.header(HOST, "www.example.com:3333");
        assertThat("using custom port", request.url().toString(),
                   equalTo("https://www.example.com:3333/over/there?name=ferret"));
    }

    private Matcher<Iterable<?>> containsKeys(Object... keys) {
        return Matchers.containsInAnyOrder(keys);
    }

    private Matcher<Map<?, ?>> containsEntry(Object key, Object value) {
        return Matchers.hasEntry(equalTo(key), equalTo(value));
    }

    private Matcher<? super BodyPart> partWithName(String name) {
        return new FeatureMatcher<BodyPart, String>(equalTo(name), "part named", "name") {
            @Override
            protected String featureValueOf(BodyPart actual) {
                return actual.name();
            }
        };
    }
}