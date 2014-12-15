package com.vtence.molecule;

import com.vtence.molecule.http.Cookie;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RequestTest {

    Request request = new Request();

    @Test
    public void maintainsAnOrderedListOfParametersWithSameName() {
        request.addParameter("letters", "a");
        request.addParameter("letters", "b");
        request.addParameter("letters", "c");

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
        request.addParameter("letters", "a, b, c, etc.");
        request.addParameter("digits", "1, 2, 3, etc.");
        request.removeParameter("letters");

        assertThat("parameter names", request.parameterNames(), contains("digits"));
    }

    @Test
    public void containsABody() throws IOException {
        request.input("body");
        assertThat("input", request.body(), equalTo("body"));
    }

    @Test
    public void maintainsAListOfHeaders() throws IOException {
        request.addHeader("Accept", "text/html");
        request.addHeader("Accept", "application/json");
        request.header("Accept-Encoding", "gzip");
        request.header("Accept-Language", "en");

        assertThat("headers", request.allHeaders(), allOf(
                hasEntry("Accept", "text/html, application/json"),
                hasEntry("Accept-Encoding", "gzip"),
                hasEntry("Accept-Language", "en")));
        assertThat("header names", request.headerNames(), contains("Accept", "Accept-Encoding", "Accept-Language"));
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
    public void maintainsAListOfCookie() {
        request.cookie("mr christie", "peanuts");
        request.cookie("petit ecolier", "chocolat noir");
        request.cookie("delicious", "chocolat au lait");

        assertThat("cookies", request.cookies(), contains(cookieNamed("mr christie"), cookieNamed("petit ecolier"), cookieNamed("delicious")));
    }

    @Test
    public void removesCookies() {
        request.cookie("mr christie", "peanuts");
        request.cookie("petit ecolier", "chocolat noir");
        assertThat("cookie?", request.hasCookie("mr christie"), equalTo(true));
        request.removeCookie("mr christie");
        assertThat("still there?", request.hasCookie("mr christie"), equalTo(false));

        assertThat("cookies", request.cookies(), contains(cookieNamed("petit ecolier")));
    }

    private Matcher<Cookie> cookieNamed(String name) {
        return new FeatureMatcher<Cookie, String>(equalTo(name), "cookie named", "cookie") {
            protected String featureValueOf(Cookie cookie) {
                return cookie.name();
            }
        };
    }
}
