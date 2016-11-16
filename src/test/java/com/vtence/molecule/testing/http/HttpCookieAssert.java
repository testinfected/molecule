package com.vtence.molecule.testing.http;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import java.net.HttpCookie;

import static org.hamcrest.Matchers.equalTo;

public class HttpCookieAssert {
    private final HttpCookie cookie;

    protected HttpCookieAssert(HttpCookie cookie) {
        this.cookie = cookie;
    }

    public static HttpCookieAssert assertThat(HttpCookie cookie) {
        return new HttpCookieAssert(cookie);
    }

    public HttpCookieAssert hasValue(String value) {
        return hasValue(equalTo(value));
    }

    public HttpCookieAssert hasValue(Matcher<String> matching) {
        MatcherAssert.assertThat(message("value"), cookie.getValue(), matching);
        return this;
    }

    private HttpCookieAssert hasPath(Matcher<? super String> matching) {
        MatcherAssert.assertThat(message("path"), cookie.getPath(), matching);
        return this;
    }

    public HttpCookieAssert hasPath(String path) {
        return hasPath(equalTo(path));
    }

    public HttpCookieAssert hasDomain(String domain) {
        return hasDomain(equalTo(domain));
    }

    private HttpCookieAssert hasDomain(Matcher<? super String> matching) {
        MatcherAssert.assertThat(message("domain"), cookie.getDomain(), matching);
        return this;
    }

    public HttpCookieAssert hasMaxAge(long seconds) {
        return hasMaxAge(Matchers.is(seconds));
    }

    public HttpCookieAssert hasMaxAge(Matcher<? super Long> matching) {
        MatcherAssert.assertThat(message("max age"), cookie.getMaxAge(), matching);
        return this;
    }

    public HttpCookieAssert isSecure() {
        MatcherAssert.assertThat(message("is not secure"), cookie.getSecure());
        return this;
    }

    public HttpCookieAssert isHttpOnly() {
        MatcherAssert.assertThat(message("is not http only"), cookie.isHttpOnly());
        return this;
    }

    private String message(String message) {
        return "'" + cookie.getName() + "' cookie " + message;
    }
}