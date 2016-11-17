package com.vtence.molecule.testing;

import com.vtence.molecule.http.Cookie;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

import static org.hamcrest.CoreMatchers.equalTo;

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
        MatcherAssert.assertThat("cookie '" + cookie.name() + "' value", cookie.value(), matching);
        return this;
    }

    public CookieAssert isHttpOnly() {
        MatcherAssert.assertThat("cookie '" + cookie.name() + "' is not http only", cookie.httpOnly());
        return this;
    }

    public CookieAssert hasMaxAge(int seconds) {
        return hasMaxAge(equalTo(seconds));
    }

    public CookieAssert hasMaxAge(Matcher<? super Integer> matching) {
        MatcherAssert.assertThat("cookie '" + cookie.name() + "' max age", cookie.maxAge(), matching);
        return this;
    }
}