package com.vtence.molecule.http;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class HttpDateTest {

    Instant pointInTime = LocalDateTime.of(2014, 3, 6, 8, 49, 37).toInstant(ZoneOffset.UTC);

    @Test public void
    parsesRfc1123Dates() {
        assertThat("date", HttpDate.parse("Thu, 06 Mar 2014 08:49:37 GMT"), equalTo(pointInTime));
    }

    @Test public void
    parsesRfc1036Dates() {
        assertThat("date", HttpDate.parse("Thu, 06-Mar-14 08:49:37 GMT"), equalTo(pointInTime));
    }

    @Test public void
    parsesAscTimeDates() {
        assertThat("date", HttpDate.parse("Thu Mar 6 08:49:37 2014"), equalTo(pointInTime));
    }

    @Test public void
    formatsDatesAccordingToRfc1123() {
        assertThat("http date", HttpDate.httpDate(pointInTime), equalTo("Thu, 6 Mar 2014 08:49:37 GMT"));
    }

    @Test public void
    suppressCoverageNoise() {
        new HttpDate();
    }
}