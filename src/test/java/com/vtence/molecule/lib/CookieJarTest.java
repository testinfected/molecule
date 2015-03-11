package com.vtence.molecule.lib;

import com.vtence.molecule.Request;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
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
}
