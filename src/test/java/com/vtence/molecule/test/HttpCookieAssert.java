package com.vtence.molecule.test;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Assert;

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

    private HttpCookieAssert hasPath(Matcher<? super String> matching) {
        Assert.assertThat(message("path"), cookie.getPath(), matching);
        return this;
    }

    public HttpCookieAssert hasPath(String path) {
        return hasPath(equalTo(path));
    }

    public HttpCookieAssert hasDomain(String domain) {
        return hasDomain(equalTo(domain));
    }

    private HttpCookieAssert hasDomain(Matcher<? super String> matching) {
        Assert.assertThat(message("domain"), cookie.getDomain(), matching);
        return this;
    }

    public HttpCookieAssert hasMaxAge(long seconds) {
        return hasMaxAge(Matchers.is(seconds));
    }

    public HttpCookieAssert hasMaxAge(Matcher<? super Long> matching) {
        Assert.assertThat(message("max age"), cookie.getMaxAge(), matching);
        return this;
    }

    public HttpCookieAssert isSecure() {
        Assert.assertTrue(message("is not secure"), cookie.getSecure());
        return this;
    }

    public HttpCookieAssert isHttpOnly() {
        Assert.assertTrue(message("is not http only"), cookie.isHttpOnly());
        return this;
    }

    private String message(String message) {
        return "'" + cookie.getName() + "' cookie " + message;
    }
}