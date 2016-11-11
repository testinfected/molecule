package com.vtence.molecule.http;

import org.junit.Test;

import static com.vtence.molecule.http.Host.parse;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.junit.MatcherAssert.assertThat;

public class HostTest {

    @Test public void
    parsesHostNameAndPortFromHostHeader() {
        Host host = parse("www.example.org:8443");

        assertThat("hostname", host.name(), equalTo("www.example.org"));
        assertThat("port", host.port(443), equalTo(8443));
    }

    @Test public void
    usesDefaultPortForTheRequestedService() {
        Host host = parse("www.example.org");

        assertThat("port", host.port(80), equalTo(80));
    }

    @Test public void
    handlesIPv6AddressesCorrectly() {
        Host host = parse("[2001:db8:0:85a3:0:0:ac1f:8001]:8080");

        assertThat("hostname", host.name(), equalTo("2001:db8:0:85a3:0:0:ac1f:8001"));
        assertThat("port", host.port(80), equalTo(8080));
    }

    @Test public void
    printsHostString() {
        assertThat("with port", parse("www.example.org:8443"), hasToString("www.example.org:8443"));
        assertThat("without port", parse("www.example.org"), hasToString("www.example.org"));
        assertThat("with IPv6 address", parse("[2001:db8:0:85a3:0:0:ac1f:8001]:8080"), hasToString("[2001:db8:0:85a3:0:0:ac1f:8001]:8080"));
    }
}