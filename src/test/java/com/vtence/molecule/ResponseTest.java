package com.vtence.molecule;

import com.vtence.molecule.helpers.Charsets;
import com.vtence.molecule.http.Cookie;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ResponseTest {

    private Response response = new Response();

    @Test
    public void maintainsAListOfHeaders() throws IOException {
        response.add("Accept", "text/html");
        response.add("Accept", "application/json");
        response.set("Accept-Encoding", "gzip");
        response.set("Accept-Language", "en");

        assertThat("headers", response.all(), allOf(
                hasEntry("Accept", "text/html, application/json"),
                hasEntry("Accept-Encoding", "gzip"),
                hasEntry("Accept-Language", "en")));
        assertThat("header names", response.names(), contains("Accept", "Accept-Encoding", "Accept-Language"));
    }

    @Test
    public void headersCanBeRemoved() throws IOException {
        response.set("Accept", "text/html");
        response.set("Accept-Encoding", "gzip");

        assertThat("header?", response.has("Accept"), equalTo(true));
        response.remove("Accept");
        assertThat("still there?", response.has("Accept"), equalTo(false));

        assertThat("header names", response.names(), contains("Accept-Encoding"));
    }

    @Test
    public void maintainsAListOfCookie() {
        response.cookie("mr christie", "peanuts");
        response.cookie("petit ecolier", "chocolat noir");
        response.cookie("delicious", "chocolat au lait");

        assertThat("cookies", response.cookies(), contains(cookieNamed("mr christie"), cookieNamed("petit ecolier"), cookieNamed("delicious")));
    }

    @Test
    public void canRemoveCookies() {
        response.cookie("mr christie", "peanuts");
        response.cookie("petit ecolier", "chocolat noir");
        assertThat("cookie?", response.hasCookie("mr christie"), equalTo(true));
        response.removeCookie("mr christie");
        assertThat("still there?", response.hasCookie("mr christie"), equalTo(false));

        assertThat("cookies", response.cookies(), contains(cookieNamed("petit ecolier")));
    }

    @Test
    public void canDiscardCookies() {
        response.cookie(new Cookie("mr christie", "peanuts").maxAge(365));
        response.discardCookie("mr christie");

        assertThat("expiration", response.cookie("mr christie"), cookieExpiring(0));
    }

    @Test
    public void usesISO8859AsDefaultCharset() {
        assertThat("default charset", response.charset(), equalTo(Charsets.ISO_8859_1));
    }

    @Test
    public void readsCharsetFromContentType() {
        response.contentType("text/html; charset=utf-8");
        assertThat("charset", response.charset(), equalTo(Charsets.UTF_8));
    }

    @Test
    public void setsContentTypeCharset() {
        response.contentType("text/html; charset=iso-8859-1");
        response.charset("utf-8");
        assertThat("charset", response.contentType(), equalTo("text/html; charset=utf-8"));
        assertThat("charset", response.charset(), equalTo(Charsets.UTF_8));
    }

    private Matcher<Cookie> cookieNamed(String name) {
        return new FeatureMatcher<Cookie, String>(equalTo(name), "cookie named", "cookie") {
            protected String featureValueOf(Cookie cookie) {
                return cookie.name();
            }
        };
    }

    private Matcher<Cookie> cookieExpiring(int maxAge) {
        return new FeatureMatcher<Cookie, Integer>(equalTo(maxAge), "cookie expiring", "max age") {
            protected Integer featureValueOf(Cookie cookie) {
                return cookie.maxAge();
            }
        };
    }
}
