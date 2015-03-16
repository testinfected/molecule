package com.vtence.molecule.http;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class CookieDecoderTest {

    CookieDecoder decoder = new CookieDecoder();

    @Test
    public void parsesClientCookieNameAndValue() {
        Cookie cookie = decodeSingle("name=value");

        assertThat("cookie name", cookie.name(), equalTo("name"));
        assertThat("cookie value", cookie.value(), equalTo("value"));
    }

    @Test
    public void correctlyHandlesEmptyValues() {
        Cookie cookie = decodeSingle("name=");

        assertThat("cookie value", cookie.value(), equalTo(""));
    }

    @Test
    public void differentiatesValueDelimitersFromDelimitersWithinValues() {
        Cookie cookie = decodeSingle("name=a-value-with-an-=-sign");

        assertThat("cookie value", cookie.value(), equalTo("a-value-with-an-=-sign"));
    }

    @Test
    public void decodesQuotedCookieValues() {
        Cookie cookie = decodeSingle("name=\"value\"");

        assertThat("cookie value", cookie.value(), equalTo("value"));
    }

    @Test
    public void unescapesQuotedCookieValues() {
        Cookie cookie = decodeSingle("name=\";,\"");

        assertThat("cookie value", cookie.value(), equalTo(";,"));
    }

    @Test
    public void correctlyIgnoresValueDelimitersWithinQuotedValues() {
        assertThat("unescaped quote", decodeSingle("name=\"\\\"\"").value(), equalTo("\""));
        assertThat("unescaped backslash", decodeSingle("name=\"\\\\\"").value(), equalTo("\\"));
    }

    @Test
    public void parsesCookieVersionWhenSpecified() {
        Cookie cookie = decodeSingle("$Version=\"0\"; name=\"value\"");

        assertThat("cookie version", cookie.version(), is(0));
    }

    @Test
    public void parsesCookiePathWhenSpecified() {
        Cookie cookie = decodeSingle("name=\"value\"; $Path=path");

        assertThat("cookie path", cookie.path(), equalTo("path"));
    }

    @Test
    public void parsesCookieDomainWhenSpecified() {
        Cookie cookie = decodeSingle("name=\"value\"; $Domain=domain");

        assertThat("cookie domain", cookie.domain(), equalTo("domain"));
    }

    @Test
    public void decodesSingleClientSideCookie() {
        Cookie cookie = decodeSingle("$Version=\"1\"; Customer=\"WILE_E_COYOTE\"; $Path=\"/acme\"");

        assertThat("cookie version", cookie.version(), is(1));
        assertThat("cookie name", cookie.name(), equalTo("Customer"));
        assertThat("cookie value", cookie.value(), equalTo("WILE_E_COYOTE"));
        assertThat("cookie path", cookie.path(), equalTo("/acme"));
    }

    @Test
    public void decodesMultipleClientSideCookies() {
        List<Cookie> cookies = decodeAll("$Version=\"1\"; Customer=\"WILE_E_COYOTE\"; $Path=\"/acme\"; " +
                                         "Part_Number=\"Rocket_Launcher_0001\"; $Path=\"/acme/ammo\"; " +
                                         "Shipping=\"FedEx\"; $Path=\"/acme\"");

        assertThat("decoded cookies", cookies, hasSize(3));

        Cookie customer = cookies.get(0);
        assertThat("cookie version", customer.version(), is(1));
        assertThat("cookie name", customer.name(), equalTo("Customer"));
        assertThat("cookie value", customer.value(), equalTo("WILE_E_COYOTE"));
        assertThat("cookie path", customer.path(), equalTo("/acme"));

        Cookie partNumber = cookies.get(1);
        assertThat("cookie version", partNumber.version(), is(1));
        assertThat("cookie name", partNumber.name(), equalTo("Part_Number"));
        assertThat("cookie value", partNumber.value(), equalTo("Rocket_Launcher_0001"));
        assertThat("cookie path", partNumber.path(), equalTo("/acme/ammo"));

        Cookie shipping = cookies.get(2);
        assertThat("cookie version", shipping.version(), is(1));
        assertThat("cookie name", shipping.name(), equalTo("Shipping"));
        assertThat("cookie value", shipping.value(), equalTo("FedEx"));
        assertThat("cookie path", shipping.path(), equalTo("/acme"));
    }

    @Test
    public void decodesCommaSeparatedPairs() {
        String cookieHeader =
                "$Version=\"1\", " +
                "Part_Number=\"Riding_Rocket_0023\"; $Path=\"/acme/ammo\", " +
                "Part_Number=\"Rocket_Launcher_0001\"; $Path=\"/acme\"";

        List<Cookie> cookies = decodeAll(cookieHeader);

        Cookie rocket = cookies.get(0);
        assertThat("cookie name", rocket.name(), equalTo("Part_Number"));
        assertThat("cookie value", rocket.value(), equalTo("Riding_Rocket_0023"));

        Cookie launcher = cookies.get(1);
        assertThat("cookie version", launcher.version(), is(1));
        assertThat("cookie name", launcher.name(), equalTo("Part_Number"));
        assertThat("cookie value", launcher.value(), equalTo("Rocket_Launcher_0001"));
    }

    private Cookie decodeSingle(String cookieHeader) {
        List<Cookie> cookies = decodeAll(cookieHeader);
        assertThat("cookies found", cookies, hasSize(1));
        return cookies.get(0);
    }

    private List<Cookie> decodeAll(String cookieHeader) {
        return decoder.decode(cookieHeader);
    }
}
