package com.vtence.molecule.lib;

import com.vtence.molecule.Request;
import com.vtence.molecule.http.Cookie;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

public class CookieJarTest {

    CookieJar jar = new CookieJar() {
        public String toString() {
            return "the one and only jar";
        }
    };

    @Test
    public void canBindToRequest() {
        Request request = new Request();
        jar.bind(request);
        assertThat("bound cookie jar", CookieJar.get(request), sameInstance(jar));
    }

    @Test
    public void isInitiallyEmpty() {
        assertThat("empty jar?", jar.empty(), is(true));
    }

    @Test
    public void isNoLongerEmptyOnceItContainsCookies() {
        jar.add("a cookie", "<value>");
        assertThat("empty jar?", jar.empty(), is(false));
    }

    @Test @SuppressWarnings("unchecked")
    public void maintainsAListOfCookies() {
        jar.add("mr christie", "peanuts");
        jar.add("petit ecolier", "chocolat noir");
        jar.add("delicious", "chocolat au lait");

        assertThat("cookies in jar", jar.list(), contains(
                cookieNamed("mr christie"), cookieNamed("petit ecolier"), cookieNamed("delicious")));
    }

    @Test
    public void knowsWhatCookiesItHolds() {
        jar.add("some cookie", "<some value>");
        jar.add("other cookie", "<other value>");
        assertThat("holds some cookie", jar.has("some cookie"), is(true));
        assertThat("holds eaten cookie", jar.has("eaten cookie"), is(false));
    }

    private Matcher<Cookie> cookieNamed(String name) {
        return new FeatureMatcher<Cookie, String>(equalTo(name), "cookie named", "cookie") {
            protected String featureValueOf(Cookie cookie) {
                return cookie.name();
            }
        };
    }
}
