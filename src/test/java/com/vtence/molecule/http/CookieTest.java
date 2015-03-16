package com.vtence.molecule.http;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class CookieTest {

    @Test
    public void formatsToSetCookieValue() {
        Cookie cookie = new Cookie("name", "value").path("/path")
                                                   .domain("domain")
                                                   .maxAge(1200);

        assertThat("Set-Cookie value", cookie.toString(), equalTo(
                "name=value; version=1; path=/path; domain=domain; max-age=1200"
        ));
    }

    @Test
    public void usesRootPathAsDefault() {
        Cookie cookie = new Cookie("name", "value").domain("domain")
                                                   .maxAge(1200)
                                                   .httpOnly(true)
                                                   .secure(true);

        assertThat("Set-Cookie value", cookie.toString(),
                equalTo("name=value; version=1; path=/; domain=domain; max-age=1200; secure; httponly"));
    }

    @Test
    public void handlesAbsenceOfDomain() {
        Cookie cookie = new Cookie("name", "value").maxAge(1200)
                                                   .httpOnly(true)
                                                   .secure(true);

        assertThat("Set-Cookie value", cookie.toString(),
                equalTo("name=value; version=1; path=/; max-age=1200; secure; httponly"));
    }

    @Test
    public void formatsSecureCookies() {
        Cookie cookie = new Cookie("name", "value").secure(true);

        assertThat("Set-Cookie value", cookie.toString(), equalTo("name=value; version=1; path=/; secure"));
    }

    @Test
    public void formatsProtectedCookies() {
        Cookie cookie = new Cookie("name", "value").httpOnly(true);

        assertThat("Set-Cookie value", cookie.toString(), equalTo("name=value; version=1; path=/; httponly"));
    }
}
