package com.vtence.molecule.http;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.junit.MatcherAssert.assertThat;

public class HostTest {

    @Test public void
    parsesHostNameAndPortFromHostHeader() {
        Host host = Host.parse("www.example.org:8443");

        assertThat("hostname", host.name(), equalTo("www.example.org"));
        assertThat("port", host.port(443), equalTo(8443));
    }

    @Test public void
    usesDefaultPortForTheRequestedService() {
        Host host = Host.parse("www.example.org");

        assertThat("port", host.port(80), equalTo(80));
    }

    @Test public void
    handlesIPv6AddressesCorrectly() {
        Host host = Host.parse("[2001:db8:0:85a3:0:0:ac1f:8001]:8080");

        assertThat("hostname", host.name(), equalTo("2001:db8:0:85a3:0:0:ac1f:8001"));
        assertThat("port", host.port(80), equalTo(8080));
    }
}
