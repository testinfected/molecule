package com.vtence.molecule.http;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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

    @Test
    public void handlesVeryLongCookieValues() {
        Cookie cookie = decodeSingle("$Version=\"1\"; molecule.session=\"rO0ABXNyACNjb20udnRlbmNlLm1vbGVjdWxlLnNlc3Npb24uU2Vzc2lvbvJqkJ0nBioiAgAGWgAHaW52YWxpZEkABm1heEFnZUwACmF0dHJpYnV0ZXN0AA9MamF2YS91dGlsL01hcDtMAAljcmVhdGVkQXR0ABNMamF2YS90aW1lL0luc3RhbnQ7TAACaWR0ABJMamF2YS9sYW5nL1N0cmluZztMAAl1cGRhdGVkQXRxAH4AAnhwAP////9zcgAmamF2YS51dGlsLmNvbmN1cnJlbnQuQ29uY3VycmVudEhhc2hNYXBkmd4SnYcpPQMAA0kAC3NlZ21lbnRNYXNrSQAMc2VnbWVudFNoaWZ0WwAIc2VnbWVudHN0ADFbTGphdmEvdXRpbC9jb25jdXJyZW50L0NvbmN1cnJlbnRIYXNoTWFwJFNlZ21lbnQ7eHAAAAAPAAAAHHVyADFbTGphdmEudXRpbC5jb25jdXJyZW50LkNvbmN1cnJlbnRIYXNoTWFwJFNlZ21lbnQ7Unc/QTKbOXQCAAB4cAAAABBzcgAuamF2YS51dGlsLmNvbmN1cnJlbnQuQ29uY3VycmVudEhhc2hNYXAkU2VnbWVudB82TJBYkyk9AgABRgAKbG9hZEZhY3RvcnhyAChqYXZhLnV0aWwuY29uY3VycmVudC5sb2Nrcy5SZWVudHJhbnRMb2NrZlWoLCzIausCAAFMAARzeW5jdAAvTGphdmEvdXRpbC9jb25jdXJyZW50L2xvY2tzL1JlZW50cmFudExvY2skU3luYzt4cHNyADRqYXZhLnV0aWwuY29uY3VycmVudC5sb2Nrcy5SZWVudHJhbnRMb2NrJE5vbmZhaXJTeW5jZYgy51N7vwsCAAB4cgAtamF2YS51dGlsLmNvbmN1cnJlbnQubG9ja3MuUmVlbnRyYW50TG9jayRTeW5juB6ilKpEWnwCAAB4cgA1amF2YS51dGlsLmNvbmN1cnJlbnQubG9ja3MuQWJzdHJhY3RRdWV1ZWRTeW5jaHJvbml6ZXJmVahDdT9S4wIAAUkABXN0YXRleHIANmphdmEudXRpbC5jb25jdXJyZW50LmxvY2tzLkFic3RyYWN0T3duYWJsZVN5bmNocm9uaXplcjPfr7mtbW+pAgAAeHAAAAAAP0AAAHNxAH4ACnNxAH4ADgAAAAA/QAAAc3EAfgAKc3EAfgAOAAAAAD9AAABzcQB+AApzcQB+AA4AAAAAP0AAAHNxAH4ACnNxAH4ADgAAAAA/QAAAc3EAfgAKc3EAfgAOAAAAAD9AAABzcQB+AApzcQB+AA4AAAAAP0AAAHNxAH4ACnNxAH4ADgAAAAA/QAAAc3EAfgAKc3EAfgAOAAAAAD9AAABzcQB+AApzcQB+AA4AAAAAP0AAAHNxAH4ACnNxAH4ADgAAAAA/QAAAc3EAfgAKc3EAfgAOAAAAAD9AAABzcQB+AApzcQB+AA4AAAAAP0AAAHNxAH4ACnNxAH4ADgAAAAA/QAAAc3EAfgAKc3EAfgAOAAAAAD9AAABzcQB+AApzcQB+AA4AAAAAP0AAAHQACHVzZXJuYW1ldAAHVmluY2VudHBweHNyAA1qYXZhLnRpbWUuU2VylV2EuhsiSLIMAAB4cHcNAgAAAABf4YaaJOx4CHh0ACRjNGIxMDNlMC0zYzAzLTQxZDYtOWEzNC1lZTczOWE3ODc0YzBxAH4ANA==--r3q1yut2aSCAjMF03DMV2z5A79aeWP744I2pMVoO7qk=\";$Path=\"/\";$Domain=\"0.0.0.0\"");
        assertThat("value", cookie.value(), endsWith("MVoO7qk="));
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
