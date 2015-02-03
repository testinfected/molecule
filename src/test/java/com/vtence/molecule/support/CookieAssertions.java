package com.vtence.molecule.support;

import com.vtence.molecule.http.Cookie;
import org.hamcrest.Matcher;
import org.junit.Assert;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class CookieAssertions {

    private final Cookie cookie;

    protected CookieAssertions(Cookie cookie) {
        this.cookie = cookie;
    }

    public static CookieAssertions assertThat(Cookie cookie) {
        return new CookieAssertions(cookie);
    }

    public CookieAssertions hasValue(String value) {
        return hasValue(equalTo(value));
    }

    public CookieAssertions hasValue(Matcher<? super String> matching) {
        Assert.assertThat(cookie.name() + " cookie value", cookie.value(), matching);
        return this;
    }

    public CookieAssertions isHttpOnly() {
        Assert.assertThat(cookie.name() + " cookie http only", cookie.httpOnly(), is(true));
        return this;
    }

    public CookieAssertions hasMaxAge(int seconds) {
        return hasMaxAge(equalTo(seconds));
    }

    public CookieAssertions hasMaxAge(Matcher<? super Integer> matching) {
        Assert.assertThat(cookie.name() + " cookie max age", cookie.maxAge(), matching);
        return this;
    }
}