package com.vtence.molecule.lib;

import com.vtence.molecule.http.Cookie;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public class CookieJarTest {

    CookieJar jar = new CookieJar(Arrays.asList(
            new Cookie("mr christie", "peanuts"),
            new Cookie("petit ecolier", "chocolat noir")));

    @Test
    public void initiallyContainsOriginalCookies() {
        assertThat("empty jar?", jar.empty(), is(false));
        assertThat("total cookies in jar", jar.size(), is(2));
    }

    @Test
    public void addsNewCookiesInOrder() {
        jar.add("delicious", "chocolat au lait");

        assertThat("cookies in jar", jar.all(), contains(
                cookieNamed("mr christie"), cookieNamed("petit ecolier"), cookieNamed("delicious")));
    }

    @Test
    public void knowsWhatCookiesItHolds() {
        assertThat("holds original cookie", jar.has("petit ecolier"), is(true));
        assertThat("holds eaten cookie", jar.has("eaten cookie"), is(false));
    }

    @Test
    public void dropsDiscardedCookies() {
        jar.discard("petit ecolier");
        assertThat("holds rotten cookie", jar.has("petit ecolier"), is(false));
    }

    private Matcher<Cookie> cookieNamed(String name) {
        return new FeatureMatcher<Cookie, String>(equalTo(name), "cookie named", "cookie") {
            protected String featureValueOf(Cookie cookie) {
                return cookie.name();
            }
        };
    }
}
