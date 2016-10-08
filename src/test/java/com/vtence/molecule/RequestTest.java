package com.vtence.molecule;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.vtence.molecule.http.HeaderNames.CONTENT_TYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;

public class RequestTest {

    Request request = new Request();

    @Test
    public void maintainsAnOrderedListOfParametersWithSameName() {
        request.addParameter("letters", "a");
        request.addParameter("letters", "b");
        request.addParameter("letters", "c");

        assertThat("has letters?", request.hasParameter("letters"), equalTo(true));
        assertThat("letters", request.parameters("letters"), contains("a", "b", "c"));
    }

    @Test
    public void maintainsAListOfParameterNames() {
        request.addParameter("letters", "a, b, c, etc.");
        request.addParameter("digits", "1, 2, 3, etc.");
        request.addParameter("symbols", "#, $, %, etc.");

        assertThat("parameter names", request.parameterNames(), contains("letters", "digits", "symbols"));
    }

    @Test
    public void removingAParameterRemovesAllParametersWithSameName() {
        request.addParameter("letters", "a");
        request.addParameter("letters", "b");
        request.addParameter("letters", "c");
        request.addParameter("digits", "1, 2, 3, etc.");

        request.removeParameter("letters");
        assertThat("has letters?", request.hasParameter("letters"), equalTo(false));
        assertThat("parameter names", request.parameterNames(), contains("digits"));
    }

    @Test
    public void containsABody() throws IOException {
        request.body("body");
        assertThat("input", request.body(), equalTo("body"));
    }

    @Test
    public void maintainsAnOrderedListOfHeaderNames() throws IOException {
        request.addHeader("Accept", "text/html");
        request.addHeader("Accept", "application/json");
        request.header("Accept-Encoding", "gzip");
        request.header("Accept-Language", "en");

        assertThat("header names", request.headerNames(), contains("Accept", "Accept-Encoding", "Accept-Language"));
    }

    @Test
    public void retrievesHeadersByName() throws IOException {
        request.header("Accept", "text/html; q=0.9, application/json");
        assertThat("header", request.header("Accept"), equalTo("text/html; q=0.9, application/json"));
    }

    @Test
    public void retrievesListOfHeadersWithSameName() throws IOException {
        request.addHeader("Accept-Language", "en").
                addHeader("Accept-Language", "fr");
        assertThat("header", request.headers("Accept-Language"), contains("en", "fr"));
    }

    @Test
    public void joinsHeadersWithSameName() throws IOException {
        request.addHeader("Accept", "text/html; q=0.9").
                addHeader("Accept", "application/json");
        assertThat("header", request.header("Accept"), equalTo("text/html; q=0.9, application/json"));
    }

    @Test
    public void removesHeaders() throws IOException {
        request.addHeader("Accept", "text/html");
        request.addHeader("Accept-Encoding", "gzip");

        assertThat("header?", request.hasHeader("Accept"), equalTo(true));
        request.removeHeader("Accept");
        assertThat("still there?", request.hasHeader("Accept"), equalTo(false));

        assertThat("header names", request.headerNames(), contains("Accept-Encoding"));
    }

    @Test
    public void maintainsAMapOfAttributes() throws IOException {
        request.attribute("name", "Velociraptor");
        request.attribute("family", "Dromaeosauridae");
        request.attribute("clade", "Dinosauria");

        assertThat("attributes", request.attributes(), allOf(containsEntry("name", "Velociraptor"),
                containsEntry("family", "Dromaeosauridae"),
                containsEntry("clade", "Dinosauria")));
        assertThat("attribute names", request.attributeKeys(), containsKeys("name", "family", "clade"));
    }

    @Test
    public void removesAttributeOnDemand() throws IOException {
        request.attribute("name", "Velociraptor");
        request.attribute("family", "Dromaeosauridae");
        request.attribute("clade", "Dinosauria");
        request.removeAttribute("family");

        assertThat("attribute names", request.attributeKeys(), containsKeys("name", "clade"));
    }

    @Test
    public void usesISO8859AsDefaultCharset() {
        assertThat("default charset", request.charset(), equalTo(StandardCharsets.ISO_8859_1));
    }

    @Test
    public void readsCharsetFromContentType() {
        request.header(CONTENT_TYPE, "text/html; charset=utf-8");
        assertThat("charset", request.charset(), equalTo(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void maintainsAnOrderedListOfBodyParts() {
        request.addPart(new BodyPart().name("a"));
        request.addPart(new BodyPart().name("b"));
        request.addPart(new BodyPart().name("c"));
        request.addPart(new BodyPart().name("a"));

        assertThat("body parts", request.parts(),
                contains(partWithName("a"), partWithName("b"), partWithName("c"), partWithName("a")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void removesBodyPartsByName() {
        request.addPart(new BodyPart().name("a"));
        request.addPart(new BodyPart().name("b"));
        request.addPart(new BodyPart().name("c"));
        request.addPart(new BodyPart().name("b"));
        request.removePart("b");

        assertThat("body parts", request.parts(), contains(partWithName("a"), partWithName("c")));
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