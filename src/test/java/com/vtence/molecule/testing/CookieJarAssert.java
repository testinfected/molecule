package com.vtence.molecule.testing;

import com.vtence.molecule.lib.CookieJar;
import org.junit.Assert;

public class CookieJarAssert {

    private final CookieJar jar;

    protected CookieJarAssert(CookieJar jar) {
        this.jar = jar;
    }

    public static CookieJarAssert assertThat(CookieJar cookie) {
        return new CookieJarAssert(cookie);
    }

    public CookieAssert hasCookie(String named) {
        Assert.assertTrue("cookie jar has no fresh cookie '" + named + "'", jar.fresh(named));
        return new CookieAssert(jar.get(named));
    }

    public CookieJarAssert hasNoCookie(String named) {
        Assert.assertFalse("cookie jar has unexpected fresh cookie '" + named + "'", jar.fresh(named));
        return this;
    }

    public CookieJarAssert hasDiscardedCookie(String named) {
        Assert.assertTrue("cookie jar has no discarded cookie '" + named + "'", jar.discarded(named));
        return this;
    }
}