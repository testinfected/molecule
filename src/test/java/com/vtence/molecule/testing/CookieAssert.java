package com.vtence.molecule.testing;

import com.vtence.molecule.http.Cookie;
import org.hamcrest.Matcher;
import org.junit.Assert;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class CookieAssert {

    private final Cookie cookie;

    protected CookieAssert(Cookie cookie) {
        this.cookie = cookie;
    }

    public static CookieAssert assertThat(Cookie cookie) {
        return new CookieAssert(cookie);
    }

    public CookieAssert hasValue(String value) {
        return hasValue(equalTo(value));
    }

    public CookieAssert hasValue(Matcher<? super String> matching) {
        Assert.assertThat(cookie.name() + " cookie value", cookie.value(), matching);
        return this;
    }

    public CookieAssert isHttpOnly() {
        Assert.assertThat(cookie.name() + " cookie http only", cookie.httpOnly(), is(true));
        return this;
    }

    public CookieAssert hasMaxAge(int seconds) {
        return hasMaxAge(equalTo(seconds));
    }

    public CookieAssert hasMaxAge(Matcher<? super Integer> matching) {
        Assert.assertThat(cookie.name() + " cookie max age", cookie.maxAge(), matching);
        return this;
    }
}